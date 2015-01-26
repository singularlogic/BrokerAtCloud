package eu.brokeratcloud.persistence.processors;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.annotation.processing.AbstractProcessor;
import javax.lang.model.element.TypeElement;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeKind;

import eu.brokeratcloud.persistence.annotations.*;

@SupportedAnnotationTypes({	"eu.brokeratcloud.persistence.annotations.RdfSubject",
							"eu.brokeratcloud.persistence.annotations.RdfPredicate",
							"eu.brokeratcloud.persistence.annotations.Id" })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class RdfAnnotationsProcessor extends AbstractProcessor {
	public RdfAnnotationsProcessor() {
		super();
	}
	
	private static final String errorHead = RdfAnnotationsProcessor.class.getSimpleName()+"\n";
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		//processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "** START OF RDF ANNOTATION PROCESSING");
		//processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "** annotations="+annotations);
		if (annotations.size()==0) return true;
		
		// Check if all specified annotations can be handled by this processor
		for (TypeElement te : annotations) {
			String teType = te.asType().toString();
			if (!teType.equals(RdfSubject.class.getName()) &&
			    !teType.equals(RdfPredicate.class.getName()) &&
				!teType.equals(Id.class.getName()))
			{
				return false;
			}
		}
		
		// Process @RdfSubject annotations (added to public classes)
		for (Element elem : roundEnv.getElementsAnnotatedWith(RdfSubject.class)) {
			// Get qualified object name
			String qn = getQualifiedName(elem);
			//processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Element's qualified name: "+qn+"    "+elem);
			// Check that @RdfSubject is used only to annotate classes
			if (!elem.getKind().equals(ElementKind.CLASS)) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason  : Improper use of @RdfSubject. Annotated element is not a class\nElement : "+qn+"\nType    : "+elem.getKind()+"\nNote    : @RdfSubject annotation can be applied only on public classes");
				continue;
			}
			// Check that annotated class is public
			boolean isPublic = false;
			for (Modifier m : elem.getModifiers()) {
				if (m==Modifier.PUBLIC) { isPublic = true; break; }
			}
			if (!isPublic) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason : @RdfSubject applied on a non-public class\nClass  : "+qn+"\nNote   : @RdfSubject annotation can be applied only on public classes");
				continue;
			}
			// Check that class has default constructor (i.e. one with no parameters)
			Element defaultConstructor = null;
			for (Element ee : elem.getEnclosedElements()) {
				if (ee.getKind()!=ElementKind.CONSTRUCTOR) continue;	// Not a constructor
				// it is a constructor element
				ExecutableElement exe = (ExecutableElement)ee;
				if (exe.getParameters().size()>0) continue;		// Not the default constructor
				// It is the default constructor!
				defaultConstructor = ee;
				break;
			}
			if (defaultConstructor==null) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason : @RdfSubject applied on a class with no default constructor\nClass  : "+qn+"\nNote   : @RdfSubject annotation can be applied on classes with public default constructor");
				continue;
			} else {
				boolean isDefaultConstructorPublic = false;
				for (Modifier m : defaultConstructor.getModifiers()) {
					if (m==Modifier.PUBLIC) { isDefaultConstructorPublic = true; break; }
				}
				if (!isDefaultConstructorPublic) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason : @RdfSubject applied on a class with non-public default constructor\nClass  : "+qn+"\nNote   : @RdfSubject annotation can be applied only on classes with public default constructor");
					continue;
				}
			}
			// Check that class contains exactly one field annotated with @Id and @RdfPredicate
			boolean hasFields = false;
			boolean hasPredicates = false;
			boolean hasId = false;
			boolean manyIds = false;
			Element idFld = null;
			for (Element ee : elem.getEnclosedElements()) {
				if (ee.getKind()==ElementKind.FIELD) {  // it is a field element
					hasFields = true;
					if (ee.getAnnotation(RdfPredicate.class)!=null) {  // field has @RdfPredicate annotation
						hasPredicates = true;
						if (ee.getAnnotation(Id.class)!=null) {  // field has @Id and @RdfPredicate annotations
							if (!hasId) {  // first @Id occurence
								hasId = true;
								idFld = ee;
							} else {  // subsequent @Id occurence - THIS IS AN ERROR
								manyIds = true;
								if (idFld!=null) {  // report first occurence of @Id
									processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason : @Id has been applied on multiple fields of the same class\nClass  : "+qn+"\nField  : "+idFld.getSimpleName()+"\nNote   : @Id annotation can be used exactly one time per class");
									idFld = null;
								}
								// report subsequent occurence of @Id
								processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason : @Id has been applied on multiple fields of the same class\nClass  : "+qn+"\nField  : "+ee.getSimpleName()+"\nNote   : @Id annotation can be used exactly one time per class");
							}
						}
					} else
					if (ee.getAnnotation(Id.class)!=null) {  // field is annotated with @Id BUT IS NOT annotated with @RdfPredicate - THIS IS AN ERROR
						processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason : @Id annotation MUST be used with @RdfPredicate annotation\nClass  : "+qn+"\nField  : "+ee.getSimpleName());
					}  // else: field not annotated with @RdfPredicate or @Id - ignore it
				} // else: not a field element - ignore it
			}
			if (hasFields) {
				if (hasPredicates) {
					if (!hasId) {
						processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason : No field annotated with @Id and @RdfPredicate found in class\nClass  : "+qn+"\nNote   : @Id annotation can be used exactly one time per class");
					}
				} else {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason : No fields annotated with @RdfPredicate found in class\nClass  : "+qn);
				}
			} else {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason : Class annotated with @RdfSubject does not have any fields\nClass  : "+qn);
			}
			// Check if necessary @RdfSupport annotation values are present
			RdfSubject subject = elem.getAnnotation(RdfSubject.class);
			if (subject==null) continue;
			String name = subject.name();
			String ns = subject.namespace();
			String uri = subject.uri();
			//String message = "RdfSubject annotation found in " + elem.getSimpleName()
			//			   + " with name=" + subject.name() + ", namespace=" + subject.namespace() + ", uri=" + subject.uri();
			//processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
		}
		
		// Process @RdfPredicate annotations (added to public fields or fields with public setter/getter methods, declared inside public classes)
		for (Element elem : roundEnv.getElementsAnnotatedWith(RdfPredicate.class)) {
			RdfPredicate predicate = elem.getAnnotation(RdfPredicate.class);
			if (predicate==null) continue;
			String predicateSetter = predicate.setter().trim();
			String predicateGetter = predicate.getter().trim();
			// Get qualified object name
			String qn = getQualifiedName(elem);
			//processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Element's qualified name: "+qn+"    "+elem);
			
			// Check that @RdfPredicate is used only to annotate fields
			if (!elem.getKind().equals(ElementKind.FIELD)) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason  : Improper use of @RdfPredicate. Annotated element is not a field\nElement : "+qn+"\nType    : "+elem.getKind()+"\nNote    : @RdfPredicate annotation can only be used on fields");
				continue;
			}
			// Get enclosing element
			Element classElem = elem.getEnclosingElement();
			// check that enclosing element is public class
			if (classElem.getKind().equals(ElementKind.CLASS)) {
				if (classElem.getAnnotation(RdfSubject.class)==null) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason : Enclosing class isn't annotated with @RdfSubject\nClass  : "+getQualifiedName(classElem)+"\nField  : "+elem.getSimpleName()+"\nNote   : @RdfPredicate is used only on fields declared in @RdfSubject classes");
					continue;
				}
				// check that enclosing class is public
				boolean isEcPublic = false;
				for (Modifier m : classElem.getModifiers()) {
					if (m==Modifier.PUBLIC) { isEcPublic = true; break; }
				}
				if (!isEcPublic) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason : Enclosing class is not public\nClass  : "+getQualifiedName(classElem)+"\nField  : "+elem.getSimpleName()+"\nNote   : @RdfPredicate can be used only on fields declared in public classes");
					continue;
				}
			} else {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason  : Improper use of @RdfPredicate. Enclosing element isn't public class\nElement : "+getQualifiedName(classElem)+"\nType    : "+classElem.getKind()+"\nField   : "+elem.getSimpleName()+"\nNote    : @RdfPredicate can be used only on fields declared in public classes");
				continue;
			}
			// Check that annotated field is NOT static or volatile
			boolean isFinal = false;
			boolean isStatic = false;
			boolean isTransient = false;
			boolean isVolatile = false;
			for (Modifier m : elem.getModifiers()) {
				if (m==Modifier.FINAL) isFinal = true;
				if (m==Modifier.STATIC) isStatic = true;
				if (m==Modifier.TRANSIENT) isTransient = true;
				if (m==Modifier.VOLATILE) isVolatile = true;
			}
			if (isFinal || isStatic || isTransient || isVolatile) {
				StringBuffer sb = new StringBuffer();
				if (isFinal) sb.append(" FINAL");
				if (isStatic) sb.append(" STATIC");
				if (isTransient) sb.append(" TRANSIENT");
				if (isVolatile) sb.append(" VOLATILE");
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason : @RdfPredicate annotated field must not be"+sb.toString()+"\nClass  : "+getQualifiedName(classElem)+"\nField  : "+elem.getSimpleName()+"\nNote  : @RdfPredicate can't be used on final,static,transient,volatile fields");
				continue;
			}
			// Check that annotated field is public or getter/setter methods exist
			boolean isPublic = false;
			for (Modifier m : elem.getModifiers()) {
				if (m==Modifier.PUBLIC) { isPublic = true; break; }
			}
			if (!isPublic) {	// field is not public. Search for appropriate setter/getter methods. Use Beans name conventions
				// prepare names if setter/getter methods sought
				String sn = elem.getSimpleName().toString();
				sn = Character.toUpperCase(sn.charAt(0))+sn.substring(1);
				String setterName = "set"+sn;
				String getterName = (elem.asType().getKind()!=TypeKind.BOOLEAN) ? "get"+sn : "is"+sn;
				// If setter/getter names are provided in annotation then use them instead
				if (!predicateSetter.isEmpty()) setterName = predicateSetter;
				if (!predicateGetter.isEmpty()) getterName = predicateGetter;
				// search for setter/getter methods
				boolean hasSetter = false;
				boolean hasGetter = false;
				// Get sibling elements (declared in the enclosing class)
				for (Element ee : classElem.getEnclosedElements()) {
					// consider only public methods
					if (!ee.getKind().equals(ElementKind.METHOD)) continue;
					boolean isEemPublic = false;
					for (Modifier eem : ee.getModifiers()) {
						if (eem==Modifier.PUBLIC) { isEemPublic = true; break; }
					}
					if (!isEemPublic) continue;
					// check if method matches the name of setter or getter method sought
					String eesn = ee.getSimpleName().toString();
					if (eesn.equals(setterName)) hasSetter = true;
					if (eesn.equals(getterName)) hasGetter = true;
					if (hasSetter && hasGetter) break;
				}
				if (!hasSetter || !hasGetter) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason : Non-public @RdfPredicate annotated field has no public setter/getter\nClass  : "+getQualifiedName(classElem)+"\nField  : "+elem.getSimpleName().toString());
					continue;
				}
			} // else : field is public
			// Check if necessary @RdfPredicate annotation values are present
			String name = predicate.name();
			String ns = predicate.namespace();
			String uri = predicate.uri();
			//String message = "RdfPredicate annotation found in " + elem.getSimpleName()
			//			   + " with name=" + predicate.name() + ", namespace="+ns+", uri=" + predicate.uri();
			//processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
		}
		
		// Process @Id annotations (added to public fields or fields with public setter/getter methods, declared inside public classes)
		// Only one @Id per class is permitted
		boolean manyIds = false;
		for (Element elem : roundEnv.getElementsAnnotatedWith(Id.class)) {
			// Get qualified object name
			String qn = getQualifiedName(elem);
			// check that element is also annotated with @RdfPredicate. If so then it has already been checked
			if (elem.getAnnotation(RdfPredicate.class)==null) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorHead+"Reason : @Id can only be applied on fields annotated with @RdfPredicate too\nClass  : "+getQualifiedName(elem.getEnclosingElement())+"\nField  : "+elem.getSimpleName()+"\nNote   : Also, @Id annotated fields must be defined in public classes");
				continue;
			}
			
			// No @Id parameters exist
			Id idField = elem.getAnnotation(Id.class);
			if (idField==null) continue;
			//String message = "Id annotation found in " + elem.getSimpleName();
			//processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
		}
		
		//processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "** END OF RDF ANNOTATION PROCESSING");
		return true;	 // no further annotation processing is required
	}
	
	protected String getQualifiedName(Element elem) {
		StringBuffer sb = new StringBuffer();
		Element tmp = elem;
		while (tmp!=null) {
			Name name = tmp.getSimpleName();
			if (name!=null && !name.toString().trim().isEmpty()) { sb.insert(0,name.toString().trim()); sb.insert(0,"."); }
			tmp = tmp.getEnclosingElement();
		}
		return (sb.length()>0) ? sb.substring(1) : "<>";
	}
}