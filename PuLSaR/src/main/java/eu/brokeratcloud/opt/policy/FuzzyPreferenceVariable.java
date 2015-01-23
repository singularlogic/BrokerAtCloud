package eu.brokeratcloud.opt.policy;

import eu.brokeratcloud.common.policy.AllowedFuzzyPropertyValue;
import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://www.linked-usdl.org/ns/usdl-pref#FuzzyVariable",
	suppressRdfType=true
)
public class FuzzyPreferenceVariable extends PreferenceVariable {
//	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-pref#hasDefaultFuzzyValue", omitIfNull=true)
//	protected AllowedFuzzyPropertyValue hasDefaultFuzzyValue;
	
	public FuzzyPreferenceVariable() { subClassOf = "http://www.linked-usdl.org/ns/usdl-pref#FuzzyVariable"; }
	
//	public AllowedFuzzyPropertyValue getHasDefaultFuzzyValue() { return hasDefaultFuzzyValue; }
//	public void setHasDefaultFuzzyValue(AllowedFuzzyPropertyValue aqvp) { hasDefaultFuzzyValue =aqvp; }
	
	public String toString() {
		String tmp = super.toString();
		return tmp.substring(0,tmp.length()-3) + 
				//"\n\thas-default = "+hasDefaultFuzzyValue+
				"\n}\n";
	}
}
