package eu.brokeratcloud.persistence;

public class SparqlServiceClientFactory {
	protected static final String defaultClient = "eu.brokeratcloud.fuseki.FusekiClient";
	
	protected static SparqlServiceClient instance = null;
	
	public static SparqlServiceClient getClientInstance() {
		if (instance!=null) return instance;
		
		String clss = defaultClient;
		try {
			return instance = (SparqlServiceClient) Class.forName(clss).newInstance();
		} catch (Exception e) {
			System.err.println("SparqlServiceClientFactory.getClientInstance: class="+clss+": EXCEPTION THROWN: "+e);
			throw new RuntimeException(e);
		}
	}
}