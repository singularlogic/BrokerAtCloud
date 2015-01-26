package eu.brokeratcloud.opt.policy;

import eu.brokeratcloud.persistence.annotations.RdfSubject;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://www.linked-usdl.org/ns/usdl-pref#hasDefaultFuzzyValue",
	suppressRdfType=true
)
public class DefaultFuzzyPreferenceVariableValue extends DefaultPreferenceVariableValue {
	public DefaultFuzzyPreferenceVariableValue() {
		setFuzzy();
	}
}
