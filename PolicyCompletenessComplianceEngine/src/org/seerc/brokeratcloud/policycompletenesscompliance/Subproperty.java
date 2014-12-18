package org.seerc.brokeratcloud.policycompletenesscompliance;

// Corresponds to subproperty of a usdl-sla property (hasServiceLevelProfile, hasServiceLevel etc) found in the BP 
public class Subproperty {
	private String uri; // URI of the Subproperty
	private String domainUri; // URI of the domain class
	private String rangeUri; // URI of the range class

	public Subproperty() {
		super();
	}

	public Subproperty(String uri) {
		super();
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getRangeUri() {
		return rangeUri;
	}

	public void setRangeUri(String rangeUri) {
		this.rangeUri = rangeUri;
	}

	public String getDomainUri() {
		return domainUri;
	}

	public void setDomainUri(String domainUri) {
		this.domainUri = domainUri;
	}
}
