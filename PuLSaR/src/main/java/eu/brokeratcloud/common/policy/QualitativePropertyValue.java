package eu.brokeratcloud.common.policy;

import eu.brokeratcloud.common.RootObject;
import eu.brokeratcloud.persistence.annotations.*;

import org.codehaus.jackson.annotate.JsonIgnore;

@RdfSubject(
	appendName=false,
	registerWithRdfType="instance_of:http://purl.org/goodrelations/v1#QualitativeValue",
	suppressRdfType=true
)
public class QualitativePropertyValue extends RootObject {
	@Id
	@RdfPredicate(dontSerialize=true)
	protected String id;
	@RdfPredicate(uri="http://www.w3.org/1999/02/22-rdf-syntax-ns#type", isUri=true)
	protected String rdfType;
	@RdfPredicate(uri="http://purl.org/goodrelations/v1#lesser", omitIfNull=true)
	protected QualitativePropertyValue lesser;
	@RdfPredicate(uri="http://purl.org/goodrelations/v1#greater", omitIfNull=true)
	protected QualitativePropertyValue greater;
	
	public String getId() { return id; }
	public void setId(String s) { id = s; }
	public String getRdfType() { return rdfType; }
	public void setRdfType(String rt) { rdfType = rt; }
	public QualitativePropertyValue getLesser() { return lesser; }
	public void setLesser(QualitativePropertyValue v) { lesser = v; }
	public QualitativePropertyValue getGreater() { return greater; }
	public void setGreater(QualitativePropertyValue v) { greater = v; }
	
	@JsonIgnore
	public String getValue() { return getLabel(); }
	
	public String toString() {
		String rootStr = super.toString();
		return String.format("QualitativePropertyValue: { %s%s id=%s, type=%s, lesser=%s, greater=%s }", rootStr, !rootStr.isEmpty()?",":"", id,
								rdfType, lesser!=null?lesser.getId():null, greater!=null?greater.getId():null);
	}
}
