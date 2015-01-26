package eu.brokeratcloud.persistence;

import java.util.List;
import java.util.Map;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.RDFNode;

public interface SparqlServiceClient {
	public String getSelectEndpoint();
	public String getUpdateEndpoint();
	
	public boolean ask(String askQuery);
	public void execute(String sparqlUpdate);
	public QueryExecution query(String selectQuery);
	
	public Map<String,String> queryBySubject(String subjectUri);
	public List<Map<String,RDFNode>> queryAndProcess(String selectQuery);
	public List<String> queryForIds(String selectQuery, String idCol);
	public Object queryValue(String selectQuery);
}