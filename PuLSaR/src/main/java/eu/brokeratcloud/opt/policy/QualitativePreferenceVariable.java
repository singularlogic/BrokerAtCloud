package eu.brokeratcloud.opt.policy;

import eu.brokeratcloud.common.policy.AllowedQualitativePropertyValue;
import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://www.linked-usdl.org/ns/usdl-pref#QualitativeVariable",
	suppressRdfType=true
)
public class QualitativePreferenceVariable extends PreferenceVariable {
//	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-pref#hasDefaultQualitativeValue", omitIfNull=true)
//	protected AllowedQualitativePropertyValue hasDefaultQualitativeValue;
	
	public QualitativePreferenceVariable() { subClassOf = "http://www.linked-usdl.org/ns/usdl-pref#QualitativeVariable"; }
	
//	public AllowedQualitativePropertyValue getHasDefaultQualitativeValue() { return hasDefaultQualitativeValue; }
//	public void setHasDefaultQualitativeValue(AllowedQualitativePropertyValue aqvp) { hasDefaultQualitativeValue =aqvp; }
	
	public String toString() {
		String tmp = super.toString();
		return tmp.substring(0,tmp.length()-3) + 
				//"\n\thas-default = "+hasDefaultQualitativeValue+
				"\n}\n";
	}
}
