package eu.brokeratcloud.common.policy;

import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://purl.org/goodrelations/v1#quantitativeProductOrServiceProperty",
	suppressRdfType=true
)
public class BrokerPolicyQuantitativeProperty extends BrokerPolicyProperty {
	public BrokerPolicyQuantitativeProperty() { subPropertyOf = "http://purl.org/goodrelations/v1#quantitativeProductOrServiceProperty"; }
}
