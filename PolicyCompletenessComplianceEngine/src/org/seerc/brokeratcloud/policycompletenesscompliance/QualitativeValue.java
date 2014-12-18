package org.seerc.brokeratcloud.policycompletenesscompliance;

import java.util.Map;
//Corresponds to subclass of either gr:QualitativeValue found in the BP 
public class QualitativeValue {
	private String uri; // URI of the subclass
	private Map<String, QualitativeValueInstance> instanceMap; // map that contains the instances of this subclass defined in BP
	
	public QualitativeValue() {
		super();
	}

	public QualitativeValue(String uri) {
		super();
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Map<String, QualitativeValueInstance> getInstanceMap() {
		return instanceMap;
	}

	public void setInstanceMap(
			Map<String, QualitativeValueInstance> instanceMap) {
		this.instanceMap = instanceMap;
	}
}
