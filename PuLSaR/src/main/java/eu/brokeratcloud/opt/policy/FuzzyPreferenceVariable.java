package eu.brokeratcloud.opt.policy;

import eu.brokeratcloud.common.policy.AllowedFuzzyPropertyValue;
import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://www.linked-usdl.org/ns/usdl-pref#FuzzyVariable",
	suppressRdfType=true
)
public class FuzzyPreferenceVariable extends PreferenceVariable {
	public FuzzyPreferenceVariable() { subClassOf = "http://www.linked-usdl.org/ns/usdl-pref#FuzzyVariable"; }
	
	public String toString() {
		String tmp = super.toString();
		return tmp.substring(0,tmp.length()-3) + 
				"\n}\n";
	}
}
