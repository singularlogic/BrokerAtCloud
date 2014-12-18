package org.seerc.brokeratcloud.policycompletenesscompliance;

public class EvaluationReport {

	private String serviceInstance;
	private CompletenessReportObject completenessReport;
	private ComplianceReportObject complianceReport;
	
	public EvaluationReport() {
		this.completenessReport = new CompletenessReportObject();
		this.complianceReport = new ComplianceReportObject();
	}
	
	public String getServiceInstance() {
		return serviceInstance;
	}
	public void setServiceInstance(String serviceInstance) {
		this.serviceInstance = serviceInstance;
	}
	public CompletenessReportObject getCompletenessReport() {
		return completenessReport;
	}
	public void setCompletenessReport(CompletenessReportObject completenessReport) {
		this.completenessReport = completenessReport;
	}
	public ComplianceReportObject getComplianceReport() {
		return complianceReport;
	}
	public void setComplianceReport(ComplianceReportObject complianceReport) {
		this.complianceReport = complianceReport;
	}
}
