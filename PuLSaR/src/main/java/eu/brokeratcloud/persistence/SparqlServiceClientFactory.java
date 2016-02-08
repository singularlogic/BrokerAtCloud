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

public class SparqlServiceClientFactory {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("eu.brokeratcloud.sparql");
	
	protected static final String defaultClient = "eu.brokeratcloud.fuseki.FusekiClient";
	
	protected static SparqlServiceClient instance = null;
	
	public static SparqlServiceClient getClientInstance() {
		if (instance!=null) return instance;
		
		String clss = defaultClient;
		try {
			return instance = (SparqlServiceClient) Class.forName(clss).newInstance();
		} catch (Exception e) {
			logger.error("SparqlServiceClientFactory.getClientInstance: class={}: EXCEPTION THROWN: {}", clss, e);
			throw new RuntimeException(e);
		}
	}
}