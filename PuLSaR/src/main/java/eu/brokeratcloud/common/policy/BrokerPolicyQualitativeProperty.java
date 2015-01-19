package eu.brokeratcloud.common.policy;

import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://purl.org/goodrelations/v1#qualitativeProductOrServiceProperty",
	suppressRdfType=true
)
public class BrokerPolicyQualitativeProperty extends BrokerPolicyProperty {
	public BrokerPolicyQualitativeProperty() { subPropertyOf = "http://purl.org/goodrelations/v1#qualitativeProductOrServiceProperty"; }
}
