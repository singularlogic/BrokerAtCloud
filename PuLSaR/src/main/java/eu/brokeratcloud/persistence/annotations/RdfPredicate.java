package eu.brokeratcloud.persistence.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RdfPredicate {
	String name() default "";
	String namespace() default "";
	String uri() default "";
	String setter() default "";
	String getter() default "";
	String refresh() default "cascade";
	String update() default "cascade";
	String delete() default "";
	boolean appendName() default true;
	boolean isUri() default false;
	boolean dontSerialize() default false;
	String lang() default "";
	boolean omitIfNull() default false;
}
