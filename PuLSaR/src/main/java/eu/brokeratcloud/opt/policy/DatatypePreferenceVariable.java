package eu.brokeratcloud.opt.policy;

import eu.brokeratcloud.common.policy.AllowedQuantitativePropertyValue;
import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	appendName=false,
	suppressRdfType=true
)
public class DatatypePreferenceVariable extends PreferenceVariable {
//	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-pref#hasDefaultQuantitativeValue", omitIfNull=true)
//	protected AllowedQuantitativePropertyValue hasDefaultQuantitativeValue;
	
	public DatatypePreferenceVariable() { subClassOf = "http://www.linked-usdl.org/ns/usdl-pref#DatatypeVariable"; }
	
//	public AllowedQuantitativePropertyValue getHasDefaultQuantitativeValue() { return hasDefaultQuantitativeValue; }
//	public void setHasDefaultQuantitativeValue(AllowedQuantitativePropertyValue aqvp) { hasDefaultQuantitativeValue =aqvp; }
	
	public String toString() {
		String tmp = super.toString();
		return tmp.substring(0,tmp.length()-3) + 
				//"\n\thas-default = "+hasDefaultQuantitativeValue+
				"\n}\n";
	}
}
