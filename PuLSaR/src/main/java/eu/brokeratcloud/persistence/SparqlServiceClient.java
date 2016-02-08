/*
 * #%L
 * Preference-based cLoud Service Recommender (PuLSaR) - Broker@Cloud optimisation engine
 * %%
 * Copyright (C) 2014 - 2016 Information Management Unit, Institute of Communication and Computer Systems, National Technical University of Athens
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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