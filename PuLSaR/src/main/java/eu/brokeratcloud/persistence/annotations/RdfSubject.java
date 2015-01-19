package eu.brokeratcloud.persistence.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RdfSubject {
	String name() default "";
	String namespace() default "";
	String uri() default "";
	String rdfType() default "";
	boolean appendName() default true;
	boolean suppressRdfType() default false;
	boolean suppressJavaType() default false;
	String registerWithRdfType() default "";
}
