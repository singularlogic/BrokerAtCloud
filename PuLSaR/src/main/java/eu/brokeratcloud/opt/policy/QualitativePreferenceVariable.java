package eu.brokeratcloud.opt.policy;

import eu.brokeratcloud.common.policy.AllowedQualitativePropertyValue;
import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://www.linked-usdl.org/ns/usdl-pref#QualitativeVariable",
	suppressRdfType=true
)
public class QualitativePreferenceVariable extends PreferenceVariable {
	public QualitativePreferenceVariable() { subClassOf = "http://www.linked-usdl.org/ns/usdl-pref#QualitativeVariable"; }
	
	public String toString() {
		String tmp = super.toString();
		return tmp.substring(0,tmp.length()-3) + 
				"\n}\n";
	}
}
