package eu.brokeratcloud.common;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	name="categories",
	rdfType="http://www.w3.org/2004/02/skos/core#ConceptScheme"
)
public class ClassificationDimensionScheme extends ClassificationDimension {
	@RdfPredicate(isUri=true,uri="http://www.w3.org/2004/02/skos/core#hasTopConcept", update="no-cascade", delete="no-cascade", omitIfNull=true)
	protected String hasTopConcept;
	
	public String getHasTopConcept() { return hasTopConcept; }
	public void setHasTopConcept(String s) { hasTopConcept = s; }
	
	//Override ClassificationDimension methods
	public ClassificationDimension getParent() { return null; }
	public void setParent(ClassificationDimension p) { }
	public ClassificationDimensionScheme getTopConceptOf() { return null; }
	public void setTopConceptOf(ClassificationDimensionScheme p) { }
	public ClassificationDimensionScheme getInScheme() { return null; }
	public void setInScheme(ClassificationDimensionScheme p) { }
	
	
	public String toString() {
		String tmp = super.toString();
		int p = tmp.indexOf(":");
		return tmp.substring(0, p).replace("ClassificationDimension", "ClassificationDimensionScheme") + tmp.substring(p);
	}
}
