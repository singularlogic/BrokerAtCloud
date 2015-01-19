package eu.brokeratcloud.common.policy;

import eu.brokeratcloud.persistence.annotations.*;
import org.codehaus.jackson.annotate.JsonIgnore;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://purl.org/goodrelations/v1#datatypeProductOrServiceProperty",
	suppressRdfType=true
)
public class BrokerPolicyDatatypeProperty extends BrokerPolicyProperty {
	@RdfPredicate(isUri=true, uri="http://www.w3.org/2000/01/rdf-schema#range")
	protected String datatype;
	
	public BrokerPolicyDatatypeProperty() { subPropertyOf = "http://purl.org/goodrelations/v1#datatypeProductOrServiceProperty"; }
	
	public String getDatatype() { return datatype; }
	public void setDatatype(String s) { datatype = s; }
	
	@JsonIgnore public boolean isBoolean() { return datatype.equals("http://www.w3.org/2001/XMLSchema#boolean"); }
	@JsonIgnore public boolean isDateTime() { return datatype.equals("http://www.w3.org/2001/XMLSchema#dateTime"); }
	@JsonIgnore public void setBoolean() { datatype = "http://www.w3.org/2001/XMLSchema#boolean"; }
	@JsonIgnore public void setDateTime() { datatype = "http://www.w3.org/2001/XMLSchema#dateTime"; }
	
	public String toString() {
		String s = super.toString();
		s = s.substring(0, s.length()-3);
		s = s +	"\n\tdatatype = "+datatype+
				"}\n";
		return s;
	}
}
