package eu.brokeratcloud.opt.policy;

import eu.brokeratcloud.persistence.annotations.RdfSubject;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://www.linked-usdl.org/ns/usdl-pref#hasDefaultQuantitativeValue",
	suppressRdfType=true
)
public class DefaultQuantitativePreferenceVariableValue extends DefaultPreferenceVariableValue {
	public DefaultQuantitativePreferenceVariableValue() {
		setQuantitative();
	}
}
