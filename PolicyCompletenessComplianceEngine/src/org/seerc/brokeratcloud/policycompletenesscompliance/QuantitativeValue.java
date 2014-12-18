package org.seerc.brokeratcloud.policycompletenesscompliance;

import java.util.Map;
//Corresponds to subclass of either gr:QuantitativeValueInteger or gr:QuantitativeValueFloat found in the BP 
public class QuantitativeValue {
	private String uri; // URI of the subclass
	private Map<String, QuantitativeValueInstance> instanceMap; // map that contains the instances of this subclass defined in BP
	
	public QuantitativeValue() {
		super();
	}

	public QuantitativeValue(String uri) {
		super();
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Map<String, QuantitativeValueInstance> getInstanceMap() {
		return instanceMap;
	}

	public void setInstanceMap(
			Map<String, QuantitativeValueInstance> instanceMap) {
		this.instanceMap = instanceMap;
	}
}
