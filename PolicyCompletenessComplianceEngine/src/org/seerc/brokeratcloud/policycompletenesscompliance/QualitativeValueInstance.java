package org.seerc.brokeratcloud.policycompletenesscompliance;

//Corresponds to an instance of either gr:QuantitativeValueInteger or gr:QuantitativeValueFloat subclass found in the BP 
public class QualitativeValueInstance {
	private String uri;
	
	public QualitativeValueInstance(String uri) {
		super();
		this.uri = uri;
	}
	public QualitativeValueInstance() {
		super();
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
}
