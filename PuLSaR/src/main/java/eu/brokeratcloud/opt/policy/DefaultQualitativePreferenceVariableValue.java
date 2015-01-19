package eu.brokeratcloud.opt.policy;

import eu.brokeratcloud.persistence.annotations.RdfSubject;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://www.linked-usdl.org/ns/usdl-pref#hasDefaultQualitativeValue",
	suppressRdfType=true
)
public class DefaultQualitativePreferenceVariableValue extends DefaultPreferenceVariableValue {
	public DefaultQualitativePreferenceVariableValue() {
		setQualitative();
	}
}
