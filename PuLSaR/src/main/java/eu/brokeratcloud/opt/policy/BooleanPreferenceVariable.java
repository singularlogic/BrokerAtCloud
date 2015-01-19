package eu.brokeratcloud.opt.policy;

import eu.brokeratcloud.common.policy.AllowedQualitativePropertyValue;
import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://www.linked-usdl.org/ns/usdl-pref#BooleanVariable",
	suppressRdfType=true
)
public class BooleanPreferenceVariable extends QualitativePreferenceVariable {
	public BooleanPreferenceVariable() { subClassOf = "http://www.linked-usdl.org/ns/usdl-pref#BooleanVariable"; }
}
