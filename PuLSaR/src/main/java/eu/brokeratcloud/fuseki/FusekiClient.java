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
package eu.brokeratcloud.fuseki;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class FusekiClient implements eu.brokeratcloud.persistence.SparqlServiceClient {
	private static final org.slf4j.Logger _logger = org.slf4j.LoggerFactory.getLogger("eu.brokeratcloud.sparql.fuseki");

	private Properties properties;
	private String updateService;
	private String queryService;
	private boolean debugOn = false;
	private java.io.PrintStream logger;
	
	public FusekiClient() throws java.io.IOException {
		this("fuseki-client.properties");
	}
	
	public FusekiClient(String propertiesFile) throws java.io.IOException {
		properties = eu.brokeratcloud.util.Config.getConfig(propertiesFile);
		updateService = properties.getProperty("update-service");
		queryService = properties.getProperty("query-service");
		
		// debugging
		String debugStr = properties.getProperty("debug", "off").trim().toLowerCase();
		debugOn = debugStr.equals("on") || debugStr.equals("yes") || debugStr.equals("1");
		debug("<init>: Debug messages are 'on'");
		
		// log file
		String logFile = properties.getProperty("log-file");
		if (debugOn && logFile!=null && !logFile.isEmpty()) {
			logger = new java.io.PrintStream(new java.io.FileOutputStream(logFile, true));
		}
	}
	
	protected void debug(String mesg) {
		if (!debugOn) return;
		if (logger!=null) { logger.println(mesg); logger.flush(); }
		else { _logger.debug("{}: {}", getClass(), mesg); }
	}
	
	public String getSelectEndpoint() { return queryService; }
	public String getUpdateEndpoint() { return updateService; }
	
	public void execute(String sparqlUpdate) {
		long startTm = System.nanoTime();
		debug("execute: Query=\n"+sparqlUpdate);
		UpdateRequest updReq = new UpdateRequest();
		updReq.add(sparqlUpdate);
		UpdateProcessor upInsertData = UpdateExecutionFactory.createRemote(updReq, updateService);
		
		// Perform the SPARQL Update operation
		upInsertData.execute();
		sumElapsedTime( System.nanoTime()-startTm );
	}
	
	public Map<String,String> queryBySubject(String subjectUri) {
		long startTm = System.nanoTime();
		debug("queryBySubject: ...");
		String qry = String.format("SELECT ?p ?o WHERE { <%s> ?p ?o }", subjectUri);
		debug("queryBySubject: Query="+qry);
		QueryExecution qeSelect = query(qry);
		try {
			ResultSet resultSet = qeSelect.execSelect();
			
			// Iterating over the SPARQL Query results
			Map<String,String> results = new HashMap<String,String>();
			while (resultSet.hasNext()) {
				QuerySolution soln = resultSet.nextSolution();
				String p = soln.get("?p").asResource().getURI();
				RDFNode oNd = soln.get("?o");
				String o;
				if (oNd.isLiteral()) {
					com.hp.hpl.jena.rdf.model.Literal lit = oNd.asLiteral();
					if (lit.getDatatypeURI()!=null && !lit.getDatatypeURI().isEmpty()) {
						o = String.format("\"%s\"^^<%s>", lit.getLexicalForm(), lit.getDatatypeURI());
					} else {
						o = String.format("\"%s\"", lit.getLexicalForm());
					}
					// append language suffix if specified
					if (!lit.getLanguage().isEmpty()) {
						String langSuffix = "@"+lit.getLanguage();
						o += langSuffix;
						p += langSuffix;
					}
				} else o = "<"+oNd.asResource().getURI()+">";
				
				if (results.containsKey(p)) o = results.get(p)+", "+o;
				results.put(p, o);
			}
			debug("queryBySubject: Results=\n"+results);
			return results;
		} finally {
			qeSelect.close();
			sumElapsedTime( System.nanoTime()-startTm );
		}
	} // end method
	
	public List<Map<String,RDFNode>> queryAndProcess(String selectQuery) {
		long startTm = System.nanoTime();
		debug("queryAndProcess: ...");
		debug("queryAndProcess: Query="+selectQuery);
		QueryExecution qeSelect = query(selectQuery);
		try {
			ResultSet results = qeSelect.execSelect();
			
			// Iterating over the SPARQL Query results
			List<Map<String,RDFNode>> resultsList = new LinkedList<Map<String,RDFNode>>();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				HashMap<String,RDFNode> row = new HashMap<String,RDFNode>();
				Iterator<String> it = soln.varNames();
				while (it.hasNext()) {
					String varName = it.next();
					RDFNode value = soln.get(varName);
					row.put(varName, value);
				}
				resultsList.add(row);
			}
			debug("queryAndProcess: Results=\n"+resultsList);
			return resultsList;
		} finally {
			qeSelect.close();
			sumElapsedTime( System.nanoTime()-startTm );
		}
	} // end method
	
	public List<String> queryForIds(String selectQuery, String idCol) {
		long startTm = System.nanoTime();
		debug("queryForIds: ...");
		debug("queryForIds: Query="+selectQuery);
		QueryExecution qeSelect = query(selectQuery);
		try {
			ResultSet results = qeSelect.execSelect();
			
			// Iterating over the SPARQL Query results
			List<String> resultsList = new LinkedList<String>();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				if (idCol==null) {
					Iterator<String> it = soln.varNames();
					while (idCol==null && it.hasNext()) idCol = it.next();
				}
				RDFNode node = soln.get(idCol);
				String id;
				if (node.isLiteral()) id = node.asLiteral().getLexicalForm();
				else id = "<"+node.asResource().getURI()+">";
				resultsList.add(id);
			}
			debug("queryForIds: Results=\n"+resultsList);
			return resultsList;
		} finally {
			qeSelect.close();
			sumElapsedTime( System.nanoTime()-startTm );
		}
	} // end method
	
	public QueryExecution query(String selectQuery) {
		debug("query: ...");
		debug("query: Query="+selectQuery);
		return _query(selectQuery, true);
	}
	
	protected QueryExecution _query(String selectQuery, boolean updateStats) {
		return QueryExecutionFactory.sparqlService(queryService, selectQuery);
	} // end method
	
	public Object queryValue(String selectQuery) {
		long startTm = System.nanoTime();
		debug("queryValue: ...");
		debug("queryValue: Query="+selectQuery);
		QueryExecution qeSelect = _query(selectQuery, false);
		try {
			ResultSet rs = qeSelect.execSelect();
			if (rs.hasNext()) {
				QuerySolution soln = rs.next();
				Iterator<String> it = soln.varNames();
				if (it.hasNext()) {
					String key = it.next();
					RDFNode val = soln.get(key);
					debug("queryValue: Result="+val);
					return val;
				}
			}
			debug("queryValue: Result=<null>");
			return null;
		} finally {
			qeSelect.close();
			sumElapsedTime( System.nanoTime()-startTm );
		}
	} // end method

	public boolean ask(String askQuery) {
		long startTm = System.nanoTime();
		debug("ask: ...");
		debug("ask: Query="+askQuery);
		QueryExecution qeAsk = query(askQuery);
		try {
			return qeAsk.execAsk();
		} finally {
			qeAsk.close();
			sumElapsedTime( System.nanoTime()-startTm );
		}
	} // end method
	
	// Time measurement methods and variables
	protected static long cntSplits = 0;
	protected static long sumElapsedTime = 0;
	protected static long minElapsedTime = Long.MAX_VALUE;
	protected static long maxElapsedTime = 0;
	private static Object statsLock = new Object();
	
	protected static void sumElapsedTime( long elapsedTime ) {
		synchronized (statsLock) {
			cntSplits++;
			sumElapsedTime += elapsedTime;
			if (minElapsedTime>elapsedTime) minElapsedTime = elapsedTime;
			if (maxElapsedTime<=elapsedTime) maxElapsedTime = elapsedTime;
		}
	}
	
	public static long getSplitCount() { return cntSplits; }
	public static long getTotalElapsedTime() { return sumElapsedTime; }
	public static long getMinElapsedTime() { return minElapsedTime; }
	public static long getMaxElapsedTime() { return maxElapsedTime; }
	public static void resetTimers() {
		synchronized(statsLock) {
			cntSplits = 0; sumElapsedTime = 0; minElapsedTime = Long.MAX_VALUE; maxElapsedTime = 0;
		}
	}
	
	public static void main(String[] args) throws Exception {
		FusekiClient client = new FusekiClient();
		long dur = 0;
		long cnt = Long.parseLong(args[0]);
		int qryCnt = args.length-1;
		long startTm = System.currentTimeMillis();
		for (int i=0; i<cnt; i++) {
			long startTm1 = System.currentTimeMillis();
			client.queryAndProcess(args[ i % qryCnt + 1 ]);
			dur += (System.currentTimeMillis() - startTm1);
			if (i%1000==0) { System.out.print("."); System.out.flush(); }
		}
		System.out.println();
		long endTm = System.currentTimeMillis();
		double avgDur = dur / cnt;
		dur = endTm - startTm;
		System.out.println(String.format("Run duration: %dms,  iterations: %d,  avg. iteration time: %fms", dur, cnt, avgDur));
	}
}