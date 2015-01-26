package eu.brokeratcloud.common.policy;

import eu.brokeratcloud.common.RootObject;
import eu.brokeratcloud.persistence.annotations.*;
import javax.xml.bind.annotation.XmlAttribute;
import org.codehaus.jackson.annotate.JsonIgnore;

@RdfSubject(
	appendName=false,
	suppressRdfType=true
)
public class BrokerPolicyProperty extends RootObject {
	@Id
	@RdfPredicate(dontSerialize=true)
	protected String id;
	@RdfPredicate(isUri=true, uri="http://www.w3.org/2000/01/rdf-schema#subPropertyOf", omitIfNull=true)
	protected String subPropertyOf;
	@RdfPredicate(isUri=true, uri="http://www.w3.org/2000/01/rdf-schema#domain", omitIfNull=true)
	protected String domain;
	@RdfPredicate(uri="http://www.w3.org/2000/01/rdf-schema#range", omitIfNull=true)
	protected AllowedPropertyValue range;
	
	public String getId() { return id; }
	public void setId(String s) { id = s; }
	public String getSubPropertyOf() { return subPropertyOf; }
	public void setSubPropertyOf(String s) { /*subPropertyOf = s;*/ }
	public String getDomain() { return domain; }
	public void setDomain(String s) { domain = s; }
	public AllowedPropertyValue getRange() { return range; }
	public void setRange(AllowedPropertyValue s) { range = s; }
	
	public String toString() {
		return getClass().getSimpleName()+": {"+
				"\n\t"+super.toString()+
				"\n\tid = "+id+
				"\n\tsubPropertyOf = "+subPropertyOf+
				"\n\tdomain = "+domain+
				"\n\trange = "+range+
				"}\n";
	}
}
