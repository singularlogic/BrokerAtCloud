package org.seerc.brokeratcloud.policycompletenesscompliance;

// Helper Class: objects of this class are used for subsequent calls to stepCompletenessCheck method
// Also a list of QuantitativeValue ClassInstancePair objects is the input to the complianceCheck method
public class ClassInstancePair {
	private String classUri; //subclass URI
	private String instanceUri; // URI of an instance of the above subclass 
	public String getClassUri() {
		return classUri;
	}
	public void setClassUri(String classUri) {
		this.classUri = classUri;
	}
	public String getInstanceUri() {
		return instanceUri;
	}
	public void setInstanceUri(String instanceUri) {
		this.instanceUri = instanceUri;
	}
	public ClassInstancePair() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ClassInstancePair(String classUri, String instanceUri) {
		super();
		this.classUri = classUri;
		this.instanceUri = instanceUri;
	}
	
	
}
