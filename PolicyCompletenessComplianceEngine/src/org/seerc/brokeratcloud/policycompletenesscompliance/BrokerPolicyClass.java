package org.seerc.brokeratcloud.policycompletenesscompliance;

import java.util.Map;

// Corresponds to a subclass of a usdl-core (ServiceModel) or usdl-sla (ServiceLevelProfile, ServiceLevel etc) class  
// or a subclass of gr:QuantitativeValue subclass (gr:QuantitativeValueInteger, gr:QuantitativeValueFloat) 
// found in the BP
public class BrokerPolicyClass {

	private String uri; // URI of the subclass
	private Map<String, Subproperty> propertyMap; // map that contains the subproperties defined in BP whose domain is this subclass

	public BrokerPolicyClass(String uri) {
		super();
		this.uri = uri;
	}

	public BrokerPolicyClass() {
		super();
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Map<String, Subproperty> getPropertyMap() {
		return propertyMap;
	}

	public void setPropertyMap(Map<String, Subproperty> propertyList) {
		this.propertyMap = propertyList;
	}
}
