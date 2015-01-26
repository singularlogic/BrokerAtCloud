package eu.brokeratcloud.opt.policy;

import eu.brokeratcloud.common.policy.AllowedQuantitativePropertyValue;
import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://www.linked-usdl.org/ns/usdl-pref#QuantitativeVariable",
	suppressRdfType=true
)
public class QuantitativePreferenceVariable extends PreferenceVariable {
	public QuantitativePreferenceVariable() { subClassOf = "http://www.linked-usdl.org/ns/usdl-pref#QuantitativeVariable"; }
	
	public String toString() {
		String tmp = super.toString();
		return tmp.substring(0,tmp.length()-3) + 
				"\n}\n";
	}
}
