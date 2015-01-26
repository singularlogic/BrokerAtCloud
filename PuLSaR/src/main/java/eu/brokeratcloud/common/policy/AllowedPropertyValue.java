package eu.brokeratcloud.common.policy;

import eu.brokeratcloud.common.RootObject;
import eu.brokeratcloud.persistence.annotations.*;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@RdfSubject(
	appendName=false,
	suppressRdfType=true
)
// Addition, in order to ignore AllowedQuantitativePropertyValue specific fields, during JSON serialization/unserialization
@JsonIgnoreProperties(ignoreUnknown=true)
public class AllowedPropertyValue extends RootObject {
	@Id
	@RdfPredicate(dontSerialize=true)
	protected String id;
	@RdfPredicate(isUri=true, uri="http://www.w3.org/2000/01/rdf-schema#subClassOf", omitIfNull=true)
	protected String subClassOf;	// Read-Only
	@RdfPredicate(isUri=true, omitIfNull=true)	// Use object's uri (not a fixed one in the USDL-CORE-CB model)
	protected String measuredBy;
	
	public String getId() { return id; }
	public void setId(String s) { id = s; }
	public String getSubClassOf() { return subClassOf; }
	public void setSubClassOf(String s) { /* It is a Read-Only field. MUST BE SET in subclasses' constructors */ }
	public boolean isMandatory() { return false; /*mandatory;*/ }
	public void setMandatory(boolean b) { /*mandatory = b;*/ }
	public String getMeasuredBy() { return measuredBy; }
	public void setMeasuredBy(String s) { measuredBy = s; }
	
	public String toString() {
		return String.format("id=%s, sub-class-of=%s, measured-by=%s, %s", id, subClassOf, measuredBy, super.toString());
	}
}
