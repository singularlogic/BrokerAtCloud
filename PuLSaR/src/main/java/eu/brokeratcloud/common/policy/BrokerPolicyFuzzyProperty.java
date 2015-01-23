package eu.brokeratcloud.common.policy;

import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://www.linked-usdl.org/ns/usdl-core/cloud-broker#fuzzyServiceProperty",
	suppressRdfType=true
)
public class BrokerPolicyFuzzyProperty extends BrokerPolicyProperty {
	public BrokerPolicyFuzzyProperty() { subPropertyOf = "http://www.linked-usdl.org/ns/usdl-core/cloud-broker#fuzzyServiceProperty"; }
}
