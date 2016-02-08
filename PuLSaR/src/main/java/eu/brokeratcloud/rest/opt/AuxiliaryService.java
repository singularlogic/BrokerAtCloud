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
package eu.brokeratcloud.rest.opt;

import java.util.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;

import com.hp.hpl.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.brokeratcloud.common.ServiceDescription;
import eu.brokeratcloud.opt.ConsumerPreferenceProfile;
import eu.brokeratcloud.persistence.RdfPersistenceManager;
import eu.brokeratcloud.persistence.RdfPersistenceManagerFactory;
import eu.brokeratcloud.persistence.SparqlServiceClient;
import eu.brokeratcloud.persistence.SparqlServiceClientFactory;
import eu.brokeratcloud.util.*;

@Path("/opt/aux")
public class AuxiliaryService extends AbstractManagementService {
	protected static final Logger logger = LoggerFactory.getLogger("eu.brokeratcloud.rest.opt.AuxiliaryService");
	
	protected static final String auxConfigFile = "auxiliary.properties";
	protected static Properties auxConfig;
	protected static Properties providerServicesCache;			// Caches provider services loaded from file (see retrieveProviderServices)
	protected static String activeBrokerPolicyUri;				// Set when calling 'applyBrokerPolicy'
	
	static {
		AuxiliaryService auxWS = new AuxiliaryService();
		try {
			logger.debug("AuxiliaryService.<static>: Applying active broker policy defaults to persistence framework");
			HashMap<String,String> hm=auxWS.applyBrokerPolicy("#");
			logger.debug("AuxiliaryService.<static>: Defaults applied to persistence framework: status={}", hm.get("status"));
			logger.debug("AuxiliaryService.<static>: Active broker policy: {}", hm.get("bp-uri"));
		} catch (Exception e) {
			logger.error("AuxiliaryService.<static>: Failed to apply active broker policy defaults to persistence framework: Reason: {}", e);
		}
	}
	
	// Stats Counters
	protected static int spl10 = -1;
	protected static int spl11 = -1;
	protected static int spl12 = -1;
	protected static int cnt13 = -1;
	protected static int cnt14 = -1;
	
	// Constructor
	public AuxiliaryService() {
		if (auxConfig==null) auxConfig = loadConfig(auxConfigFile);
	}
	
	// Retrieves service descriptions belonging to given category/ies
	// NOTE: 'cat_id' can be a comma-separated list of classification dimension IDs (e.g. maps,energy,developer)
	@GET
	@Path("/offerings/category/{cat_id}/specifications/")
	@Produces("application/json")
	public ServiceDescription[] getServiceDescriptionsForCategories(@PathParam("cat_id") String catId) {
		try {
			logger.trace("getServiceDescriptionsForCategories: BEGIN: classifications={}", catId);
			
			// Retrieve service descriptions based on classification dimensions given
			StringBuilder sb = new StringBuilder();
			sb.append(
				"SELECT DISTINCT ?bpi ?bpsmClass ?bpiValidFrom ?bpiValidTo ?srv ?sm ?slp ?validFrom ?validTo ?successor ?recomDepr \n"+
				"WHERE { \n"+
				"	?srv a ?srvClass . \n"+
				"	?srvClass <http://www.w3.org/2000/01/rdf-schema#subClassOf> * <http://www.linked-usdl.org/ns/usdl-core#Service> . \n"+
				"	?srv <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasServiceModel> ?sm. \n"+
				"	?sm <http://purl.org/goodrelations/v1#isVariantOf> ?bpi . \n"+
				"	# \n"+
				"	?sm a ?bpsmClass . \n"+
				"	?bpsmClass <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.linked-usdl.org/ns/usdl-core#ServiceModel> . \n"+
				"	OPTIONAL { ?bpi <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validFrom> ?bpiValidFrom . } \n"+
				"	OPTIONAL { ?bpi <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validThrough> ?bpiValidTo . } \n"+
				"	# \n"+
				"	?sm ?hasSLP ?slp. \n"+
				"	?hasSLP <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://www.linked-usdl.org/ns/usdl-sla#hasServiceLevelProfile> . \n"+
				"	# \n"+
				"	OPTIONAL { ?srv <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validFrom> ?validFrom . } \n"+
				"	OPTIONAL { ?srv <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validThrough> ?validTo . } \n"+
				"	OPTIONAL { \n"+
				"		?successor <http://purl.org/goodrelations/v1#successorOf> + ?srv. \n"+
				"		?successor <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#deprecationRecommendationTimePointSD> ?recomDepr \n"+
				"	} \n"+
				"	# \n"+
				"	# Classification Filter \n"
			);
			if (!catId.equals("*")) {
				catId = catId.trim();
				if (catId.isEmpty()) {
					throw new Exception("getServiceDescriptionsForCategories: No classification dimensions specified");
				}
				
				RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
				//boolean first = true;
				int ii = 1;
				for (String cdId : catId.split("[,]")) {
					cdId = cdId.trim();
					if (cdId.isEmpty()) continue;
					//if (first) first=false; else sb.append(",");
					String cdUri = pm.getObjectUri(cdId, eu.brokeratcloud.common.ClassificationDimension.class);
					sb.append( String.format("\t?sm <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasClassificationDimension> ?cd%d . \n", ii) );
					sb.append( String.format("\t?cd%d <http://www.w3.org/2004/02/skos/core#broader> * <%s> . \n", ii, cdUri) );
					ii++;
				}
			}
			sb.append(
				"} \n"+
				"ORDER BY ?srv ?sm ?slp \n"
			);
			String queryStr = sb.toString();
			logger.trace("getServiceDescriptionsForCategories: Query: \n"+queryStr);
			
			SparqlServiceClient client = SparqlServiceClientFactory.getClientInstance();
			List<Map<String,RDFNode>> results = client.queryAndProcess(queryStr);
			logger.trace("getServiceDescriptionsForCategories: Results: "+results);
			
			Vector<ServiceDescription> v = new Vector<ServiceDescription>();
			for (int i=0, n=results.size(); i<n; i++) {
				Map<String,RDFNode> soln = results.get(i);
				
				String bpiUri = node2url( soln.get("bpi") );
				String bpsmc  = node2url( soln.get("bpsmClass") );
				String srvUri = node2url( soln.get("srv") );
				String smUri  = node2url( soln.get("sm") );
				String slpUri = node2url( soln.get("slp") );
				
				// Check service validity - BEGIN
				String bpiValidFrom = val2str( soln.get("bpiValidFrom") );
				String bpiValidTo = val2str( soln.get("bpiValidTo") );
				String validFrom = val2str( soln.get("validFrom") );
				String validTo = val2str( soln.get("validTo") );
				String successorUri = node2url( soln.get("successor") );
				String recomDepr = val2str( soln.get("recomDepr") );
				if (successorUri.equals(srvUri)) { successorUri=""; recomDepr=""; }
				if ( ! checkValidAndActive(bpiUri, bpiValidFrom, bpiValidTo, srvUri, validFrom, validTo, successorUri, recomDepr)) {
					logger.debug("getServiceDescriptionsForCategories:  Ignoring service: uri={}");
					continue;
				}
				// Check service validity - END
				
				logger.trace("getServiceDescriptionsForCategories: calling _getServiceDescription for service: uri={}, sm={}, slp={}, bpi={}, bpsm-class={}", srvUri, smUri, slpUri, bpiUri, bpsmc);
				v.add( _getServiceDescription(srvUri, smUri, slpUri, bpiUri, bpsmc, client) );
			}
			ServiceDescription[] list = v.toArray(new ServiceDescription[v.size()]);
			
			logger.trace("getServiceDescriptionsForCategories: END: results={}", list);
			return list;
		} catch (Exception e) {
			logger.error("getServiceDescriptionsForCategories: EXCEPTION THROWN:\n", e);
			logger.error("getServiceDescriptionsForCategories: Returning an empty array of {}", ServiceDescription.class);
			return new ServiceDescription[0];
		}
	}
	
	protected Date parseXsdDate(String dtStr) {
		if (dtStr!=null && !(dtStr=dtStr.trim()).isEmpty()) {
			return eu.brokeratcloud.persistence.DateParser.parseW3CDateTime( dtStr );
		}
		return null;
	}
	
	protected boolean checkValidAndActive(String bpiUri, String bpiValidFrom, String bpiValidTo, String srvUri, String validFrom, String validTo, String successorUri, String recomDepr) {
		logger.trace("checkValidAndActive: BEGIN: bp-uri={}, bp-valid-from={}, bp-valid-to={}, uri={}, valid-from={}, valid-to={}, successor-uri={}, recom-depreciation-date={}", bpiUri, bpiValidFrom, bpiValidTo, srvUri, validFrom, validTo, successorUri, recomDepr);
		/*if (successorUri!=null && !(successorUri=successorUri.trim()).isEmpty()) {
			logger.warn("checkValidAndActive:  REPLACED service: uri={}, successor-uri={}", srvUri, successorUri);
			return false;
		}*/
		
		Date dtBpiValidFrom = parseXsdDate(bpiValidFrom);
		Date dtBpiValidTo = parseXsdDate(bpiValidTo);
		Date dtValidFrom = parseXsdDate(validFrom);
		Date dtValidTo = parseXsdDate(validTo);
		Date dtRecomDepr = parseXsdDate(recomDepr);
		Date dtNow = new Date();
		
		logger.trace("checkValidAndActive: Checking broker policy validity: uri={}, valid-from={} / {} --> valid-to={} / {}, now={}", bpiUri, dtBpiValidFrom, bpiValidFrom, dtBpiValidTo, bpiValidTo, dtNow);
		if ( (dtBpiValidFrom!=null && (dtNow.getTime() < dtBpiValidFrom.getTime())) ||
			 (dtBpiValidTo!=null && (dtBpiValidTo.getTime() < dtNow.getTime())) )
		{
			logger.warn("checkValidAndActive:  Ignoring service of INACTIVE POLICY: uri={}, bp-uri={}, valid-from={} --> valid-to={}", srvUri, bpiUri, bpiValidFrom, bpiValidTo);
			return false;
		}
		logger.trace("checkValidAndActive: Checking service validity: uri={}, valid-from={} / {} --> valid-to={} / {}, now={}", srvUri, dtValidFrom, validFrom, dtValidTo, validTo, dtNow);
		if ( (dtValidFrom!=null && (dtNow.getTime() < dtValidFrom.getTime())) ||
			 (dtValidTo!=null && (dtValidTo.getTime() < dtNow.getTime())) )
		{
			logger.warn("checkValidAndActive:  Ignoring INACTIVE service: uri={}, valid-from={} --> valid-to={}", srvUri, validFrom, validTo);
			return false;
		}
		logger.trace("checkValidAndActive: Checking if service in grace period: uri={}, successor-uri={}, recom-depreciation={} / {}, now={}", srvUri, successorUri, dtRecomDepr, recomDepr, dtNow);
		if ( (successorUri!=null && !(successorUri=successorUri.trim()).isEmpty()) &&
		     (dtRecomDepr!=null && (dtRecomDepr.getTime() < dtNow.getTime())) )
		{
			logger.warn("checkValidAndActive:  Ignoring DEPRECATED service: uri={}, valid-from={} --> valid-to={}, deprication-date={}, successor-uri={}", srvUri, validFrom, validTo, recomDepr, successorUri);
			return false;
		}
		logger.debug("checkValidAndActive:  VALID service: uri={}", srvUri);
		return true;
	}
	
	// Retrieves service descriptions belonging to given service provider
	@GET
	@Path("/offerings/sp/{sp}/list")
	@Produces("application/json")
	public ServiceDescription[] getServiceProviderOfferings(@PathParam("sp") String sp) {
		try {
			logger.trace("getServiceProviderOfferings: BEGIN: sp={}", sp);
			
			// Retrieve provider service descriptions
			String[] providerSDsArray = retrieveProviderServices(sp);
			
			// Prepare service selection filter
			StringBuilder sb1 = new StringBuilder();
			for (String sd : providerSDsArray) {
				//sb1.append( String.format("\t\t|| str(?sm) = \"%s\" \n", sd) );
				sb1.append( String.format("\t\t|| str(?srv) = \"%s\" \n", sd) );
			}
			String sdFilter = sb1.toString();
			
			// Retrieve service descriptions of service provider 'sp'
			StringBuilder sb = new StringBuilder();
			sb.append(
				"SELECT DISTINCT ?bpi ?bpsmClass ?bpiValidFrom ?bpiValidTo ?srv ?sm ?slp ?creator ?validFrom ?validTo ?successor ?recomDepr \n"+
				"WHERE { \n"+
				"	?srv a ?srvClass . \n"+
				"	?srvClass <http://www.w3.org/2000/01/rdf-schema#subClassOf> * <http://www.linked-usdl.org/ns/usdl-core#Service> . \n"+
				"	?srv <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasServiceModel> ?sm. \n"+
				"	?sm <http://purl.org/goodrelations/v1#isVariantOf> ?bpi . \n"+
				"	# \n"+
				"	?sm a ?bpsmClass . \n"+
				"	?bpsmClass <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.linked-usdl.org/ns/usdl-core#ServiceModel> . \n"+
				"	OPTIONAL { ?bpi <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validFrom> ?bpiValidFrom . } \n"+
				"	OPTIONAL { ?bpi <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validThrough> ?bpiValidTo . } \n"+
				"	# \n"+
				"	?sm ?hasSLP ?slp. \n"+
				"	?hasSLP <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://www.linked-usdl.org/ns/usdl-sla#hasServiceLevelProfile> . \n"+
				"	# \n"+
				"	OPTIONAL { ?srv <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validFrom> ?validFrom . } \n"+
				"	OPTIONAL { ?srv <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validThrough> ?validTo . } \n"+
				"	OPTIONAL { \n"+
				"		?successor <http://purl.org/goodrelations/v1#successorOf> + ?srv. \n"+
				"		?successor <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#deprecationRecommendationTimePointSD> ?recomDepr \n"+
				"	} \n"+
				"	# \n"+
				"	# Owner info\n"+
				"	?srv <http://purl.org/dc/terms/creator> ?creator .\n"+
				"	FILTER ( \n"+
				"		1 = 0 \n"
			);
			sb.append( sdFilter );
			sb.append(
				"	) . \n"+
				"} \n"+
				"ORDER BY ?srv ?sm ?slp \n"
			);
			String queryStr = sb.toString();
			logger.trace("getServiceProviderOfferings: Query: \n"+queryStr);
			
			SparqlServiceClient client = SparqlServiceClientFactory.getClientInstance();
			List<Map<String,RDFNode>> results = client.queryAndProcess(queryStr);
			logger.trace("getServiceProviderOfferings: Results: "+results);
			
			Vector<ServiceDescription> v = new Vector<ServiceDescription>();
			for (int i=0, n=results.size(); i<n; i++) {
				Map<String,RDFNode> soln = results.get(i);
				
				String bpiUri = node2url( soln.get("bpi") );
				String bpsmc  = node2url( soln.get("bpsmClass") );
				String srvUri = node2url( soln.get("srv") );
				String smUri  = node2url( soln.get("sm") );
				String slpUri = node2url( soln.get("slp") );
				
				// Check service validity - BEGIN
				String bpiValidFrom = val2str( soln.get("bpiValidFrom") );
				String bpiValidTo = val2str( soln.get("bpiValidTo") );
				String validFrom = val2str( soln.get("validFrom") );
				String validTo = val2str( soln.get("validTo") );
				String successorUri = node2url( soln.get("successor") );
				String recomDepr = node2url( soln.get("recomDepr") );
				if ( ! checkValidAndActive(bpiUri, bpiValidFrom, bpiValidTo, srvUri, validFrom, validTo, successorUri, recomDepr)) {
					logger.debug("getServiceDescriptionsForCategories:  Ignoring service: uri={}");
					continue;
				}
				// Check service validity - END
				
				logger.trace("getServiceProviderOfferings: calling _getServiceDescription for service: uri={}, sm={}, slp={}, bpi={}, bpsm-class={}", srvUri, smUri, slpUri, bpiUri, bpsmc);
				v.add( _getServiceDescription(srvUri, smUri, slpUri, bpiUri, bpsmc, client) );
			}
			ServiceDescription[] list = v.toArray(new ServiceDescription[v.size()]);
			
			logger.trace("getServiceProviderOfferings: END: results={}", list);
			return list;
		} catch (Exception e) {
			logger.error("getServiceProviderOfferings: EXCEPTION THROWN:\n", e);
			logger.error("getServiceProviderOfferings: Returning an empty array of {}", ServiceDescription.class);
			return new ServiceDescription[0];
		}
	}
	
	protected String[] retrieveProviderServices(String username) {
		logger.trace("retrieveProviderServices: BEGIN: username={}", username);
		if (username==null || (username=username.trim()).isEmpty()) {
			logger.error("retrieveProviderServices: ERROR: Invalid argument 'username': {}\nThrowing exception", username);
			throw new IllegalArgumentException("retrieveProviderServices: Invalid argument 'username': "+username);
		}
		
		String providerServicesSource = auxConfig.getProperty("provider-services-source");
		logger.trace("retrieveProviderServices: CFG: providerServicesSource={}", providerServicesSource);
		String src = providerServicesSource;
		if (src==null || (src=src.trim()).isEmpty()) {
			logger.error("retrieveProviderServices: ERROR: Missing 'providerServicesSource' configuration parameter. Returning null");
			return null;
		}
		
		Vector sdVect = null;
		String[] part = src.split("[ \t]+", 2);
		String srcType=part[0].trim().toUpperCase();
		src = part[1].trim();
		if (srcType.equals("URL")) {
			// Call web service...
			String url = String.format( src, username );
			logger.trace("retrieveProviderServices: Contacting URL source: {}", url);
			sdVect = (Vector)_callWebService(url, "GET", Vector.class, null);
			logger.trace("retrieveProviderServices: URL source returned: {}", sdVect);
		} else
		if (srcType.equals("FILE")) {
			logger.debug("retrieveProviderServices: FILE source: {}", src);
			//
			if (providerServicesCache==null) {
				logger.debug("retrieveProviderServices: Loading provider services from FILE source: {}", src);
				providerServicesCache = loadConfig(src);
				if (providerServicesCache!=null) {
					logger.debug("retrieveProviderServices: FILE source returned:\n{}", providerServicesCache);
				} else {
					logger.error("retrieveProviderServices: ERROR: Failed to load FILE source contents: {}", src);
					throw new RuntimeException("retrieveProviderServices: Failed to load FILE source contents: "+src);
				}
			} else {
				logger.debug("retrieveProviderServices: FILE source already loaded: {}", src);
			}
			String sds = providerServicesCache.getProperty(username);
			if (sds==null) sds = "";
			sds=sds.trim();
			String[] sd = sds.split("[ \t\n]+");
			sdVect = new Vector( java.util.Arrays.asList(sd) );
		} else
		/*if (srcType.equals("SPARQL")) {
			logger.debug("retrieveProviderServices: SPARQL query source: {}", src);
			
			// NOT IMPLEMENTED
		} else */
		{
			logger.error("retrieveProviderServices: ERROR: Invalid 'providerServicesSource' configuration parameter value: {}\nThrowing exception", providerServicesSource);
			throw new RuntimeException("retrieveProviderServices: Invalid 'providerServicesSource' configuration parameter: "+providerServicesSource);
		}
		
		// Remove invalid entries
		logger.trace("retrieveProviderServices: Filtering provider '{}' services", username);
		for (int i=sdVect.size()-1; i>=0; i--) {
			String sd = sdVect.get(i).toString().trim();
			if (!sd.startsWith("http")) sdVect.remove(i);
		}
		logger.trace("retrieveProviderServices: Provider '{}' services after filtering: {}", username, sdVect);
		
		String[] sdUriArr = (String[])sdVect.toArray(new String[sdVect.size()]);
		logger.trace("retrieveProviderServices: END: result={}", sdUriArr);
		return sdUriArr;
	}
	
	@SuppressWarnings("unchecked")
	protected Object _callWebService(String url, String method, Class clss, Object entity) {
		org.jboss.resteasy.client.jaxrs.ResteasyClient client = new org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder().build();
		org.jboss.resteasy.client.jaxrs.ResteasyWebTarget target = client.target(url);
		
		javax.ws.rs.core.Response response = null;
		method = method.trim().toLowerCase();
		if (method.equals("get")) response = target.request().accept(javax.ws.rs.core.MediaType.APPLICATION_JSON).get();
		else if (method.equals("put")) response = target.request().put( javax.ws.rs.client.Entity.json(entity) );
		else if (method.equals("post")) response = target.request().post( javax.ws.rs.client.Entity.json(entity) );
		else if (method.equals("delete")) response = target.request().delete();
		int status = response.getStatus();
		if (status>299) throw new RuntimeException("Operation failed: Status="+status+", URL="+url);
		
		Object obj = null;
		if (clss!=null) {
			obj = response.readEntity( clss );
		} else {
			obj = response.getEntity();
		}
		response.close();
		return obj;
	}
	
	// Retrieves service description based on category/ies (cat_id) and service URI (serv_id)
	// Note: category/ies is/are ignored. Retained for backward compatibility to specified Broker@Cloud Rest API
	@GET
	@Path("/offerings/category/{cat_id}/services/{serv_id}")
	@Produces("application/json")
	public ServiceDescription getServiceDescription(@PathParam("cat_id") String catId, @PathParam("serv_id") String srvUri) {
		return getServiceDescription(srvUri);
	}
	
	// Retrieves service description based on service URI (serv_id)
	@GET
	@Path("/offerings/{serv_id}")
	@Produces("application/json")
	public ServiceDescription getServiceDescription(@PathParam("serv_id") String srvUri) {
		try {
			logger.debug("getServiceDescription: Retrieving Service Description: {}", srvUri);
			
			// Check if SD is already cached
			synchronized (sdCacheLock) {
				//logger.trace("SD-CACHE: size={}", sdCache.size());
				ServiceDescription sd_ = sdCache.get(srvUri);
				//logger.trace("SD-CACHE: lookup for srvUri: {}", srvUri);
				if (sd_!=null) {
					//logger.trace("SD-CACHE: hit: {}", srvUri);
					logger.debug("getServiceDescription(URI): END: Retrieved from SD-CACHE: uri={}:\nOUTPUT:\n{}", srvUri, sd_);
					return sd_;
				}
				//logger.trace("SD-CACHE: missed: {}", srvUri);
			}
			
			// Retrieve service description based on uri given
			String queryStr = 
				"SELECT DISTINCT ?bpi ?bpsmClass ?srv ?sm ?slp \n"+
				"WHERE { \n"+
				"	BIND ( <" + srvUri + "> as ?srv ) . \n"+
				"	?srv a ?srvClass . \n"+
				"	?srvClass <http://www.w3.org/2000/01/rdf-schema#subClassOf> * <http://www.linked-usdl.org/ns/usdl-core#Service> . \n"+
				"	?srv <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasServiceModel> ?sm. \n"+
				"	?sm <http://purl.org/goodrelations/v1#isVariantOf> ?bpi . \n"+
				"	# \n"+
				"	?sm a ?bpsmClass . \n"+
				"	?bpsmClass <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.linked-usdl.org/ns/usdl-core#ServiceModel> . \n"+
				"	# \n"+
				"	?sm ?hasSLP ?slp. \n"+
				"	?hasSLP <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://www.linked-usdl.org/ns/usdl-sla#hasServiceLevelProfile> . \n"+
				"	# \n"+
				"}\n";
			logger.trace("getServiceDescription: Query: \n"+queryStr);
			
			SparqlServiceClient client = SparqlServiceClientFactory.getClientInstance();
			List<Map<String,RDFNode>> results = client.queryAndProcess(queryStr);
			logger.trace("getServiceDescription: Results: "+results);
			
			if (results.size()==1) {
				Map<String,RDFNode> soln = results.get(0);
				String bpiUri = node2url( soln.get("bpi") );
				String bpsmc  = node2url( soln.get("bpsmClass") );
				String smUri  = node2url( soln.get("sm") );
				String slpUri = node2url( soln.get("slp") );
				
				logger.trace("getServiceDescription: calling _getServiceDescription for service: uri={}, sm={}, slp={}, bpi={}, bpsm-class={}", srvUri, smUri, slpUri, bpiUri, bpsmc);
				ServiceDescription sd = _getServiceDescription(srvUri, smUri, slpUri, bpiUri, bpsmc, client);
				return sd;
			} else
			if (results.size()==0) {
				throw new Exception("getServiceDescription: No service found with uri: "+srvUri);
			} else {
				throw new Exception("getServiceDescription: "+results.size()+" services found with uri: "+srvUri);
			}
		} catch (Exception e) {
			logger.error("getServiceDescription(URI): EXCEPTION THROWN: \n", e);
			logger.debug("getServiceDescription(URI): OUTPUT: Returning null");
			return null;
		}
	}
	
	// =============================================================================================================================
	
	@GET @POST
	@Path("/list-active-policies")
	@Produces("application/json")
	public String[] getActiveBrokerPolicies() throws java.io.IOException, ClassNotFoundException {
		String queryStr =
			"SELECT DISTINCT ?bp \n" +
			"WHERE { \n" +
			"    ?bpsm <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.linked-usdl.org/ns/usdl-core#ServiceModel> .  \n" +
			"    ?bp a ?bpsm . \n" +
			"    FILTER NOT EXISTS { ?bp <http://purl.org/goodrelations/v1#isVariantOf> ?bp2 } \n" +
			"    FILTER NOT EXISTS { \n" +
			"        ?bp <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validFrom> ?from . \n" +
			"        FILTER ( <http://www.w3.org/2001/XMLSchema#date>(CONCAT(STR(year(now())),'-',STR(month(now())),'-',STR(day(now())))) < ?from ) \n" +
			"    } \n" +
			"    FILTER NOT EXISTS { \n" +
			"        ?bp <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validThrough> ?to . \n" +
			"        FILTER ( <http://www.w3.org/2001/XMLSchema#date>(CONCAT(STR(year(now())),'-',STR(month(now())),'-',STR(day(now())))) > ?to ) \n" +
			"    } \n" +
			"    FILTER NOT EXISTS { \n" +
			"        ?bp3 <http://purl.org/goodrelations/v1#successorOf> ?bp . \n" +
			"        ?bp3 <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validFrom> ?from3 . \n" +
			"        FILTER ( <http://www.w3.org/2001/XMLSchema#date>(CONCAT(STR(year(now())),'-',STR(month(now())),'-',STR(day(now())))) >= ?from3 ) \n" +
			"    } \n" +
			"    BIND (STRBEFORE(STR(?bp),'#') as ?bpNsBase) \n" +
			"    # \n" +
			"    ?bp <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasClassificationDimension> ?cd . \n" +
			"    BIND (CONCAT(STRBEFORE(STR(?cd),'#'),'#') AS ?cdNs) \n" +
			"} \n" +
			"ORDER BY ?bp ";		
		
		// Querying triplestore to get a list of active broker policies
		SparqlServiceClient client = SparqlServiceClientFactory.getClientInstance();
		List<Map<String,RDFNode>> results = client.queryAndProcess(queryStr);
		if (results==null || results.size()==0) return new String[0];
		
		String[] list = new String[results.size()];
		int i=0;
		for (Map<String,RDFNode> bpData : results) {
			if (bpData==null || bpData.size()==0) continue;
			String bpUri = node2url( bpData.get("bp") );
			list[i++] = bpUri;
		}
		
		return list;
	}
	
	@GET @POST
	@Path("/apply-policy/{bp-uri}")
	@Produces("application/json")
	public HashMap applyBrokerPolicy(@PathParam("bp-uri") String bpUri) throws java.io.IOException, ClassNotFoundException {
		logger.warn("applyBrokerPolicy: BEGIN: bp-uri={}", bpUri);   
		
		if (bpUri==null || (bpUri=bpUri.trim()).isEmpty() || bpUri.equals(".") || bpUri.equals("#")) bpUri = null;	// i.e. apply (unique) active broker policy
		logger.warn("applyBrokerPolicy: Calling _setPersistenceDefaults: bp-uri={}", bpUri);   
		String rr = _setPersistenceDefaults(bpUri);
		String s = rr;
		boolean status = s.startsWith("_setPersistenceDefaults: OK:");
		if (status) {
			s = s.substring("_setPersistenceDefaults: OK: bp-uri=".length()).trim();
			bpUri = s.substring(0, s.indexOf(","));
			activeBrokerPolicyUri = bpUri;
		} else {
			s = s.substring("_setPersistenceDefaults: ERROR: ".length()).trim();
		}
		
		// Return response
		HashMap<String,String> hm = new HashMap<String,String>();
		hm.put("bp-uri", bpUri);
		if (status) hm.put("status", "OK");
		else hm.put("status", "Broker policy not found or it contains errors \n("+rr+")");
		logger.trace("applyBrokerPolicy: END: return={}", hm);   
		return hm;
	}
	
	@GET @POST
	@Path("/get-applied-policy")
	@Produces("application/json")
	public HashMap<String,String> getAppliedBrokerPolicy() throws java.io.IOException, ClassNotFoundException {
		logger.warn("getAppliedBrokerPolicy: BEGIN: ");   
		
		// Get applied broker policy defaults
		logger.warn("getAppliedBrokerPolicy: Getting applied broker policy defaults");   
		String[] bpDefaults = eu.brokeratcloud.persistence.RdfPersistenceManagerImpl.getDefaultUris();
		
		// Return response
		HashMap<String,String> hm = new HashMap<String,String>();
		
		if (bpDefaults==null || bpDefaults[0]==null || bpDefaults[1]==null || bpDefaults[2]==null ||
			bpDefaults[0].trim().isEmpty() || bpDefaults[1].trim().isEmpty() || bpDefaults[2].trim().isEmpty())
		{
			hm.put("status", "Broker policy not initialized");
		} else {
			hm.put("status", "OK");
			hm.put("ns-pref", bpDefaults[0]);
			hm.put("bp-ns", bpDefaults[1]);
			hm.put("cd-ns", bpDefaults[2]);
			hm.put("bp-uri", activeBrokerPolicyUri);
		}
		
		logger.trace("getAppliedBrokerPolicy: END: return={}", hm);   
		return hm;
	}
	
	protected String _setPersistenceDefaults(String bpUri) throws java.io.IOException, ClassNotFoundException {
		String queryStr =
			"SELECT ?bp (CONCAT(?bpNsBase,'#') AS ?bpNs) (CONCAT(?bpNsBase,'/') AS ?nsPref) ?cdNs \n" +
			"WHERE { \n" +
			"    ?bpsm <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.linked-usdl.org/ns/usdl-core#ServiceModel> . \n" +
			"    ?bp a ?bpsm . \n" +
			( (bpUri!=null && !(bpUri=bpUri.trim()).isEmpty()) ? String.format(
			"    FILTER ( STR(?bp) = '%s' ) \n", bpUri) : 
			"" ) +
			"    FILTER NOT EXISTS { ?bp <http://purl.org/goodrelations/v1#isVariantOf> ?bp2 } \n" +
			"    FILTER NOT EXISTS { \n" +
			"        ?bp <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validFrom> ?from . \n" +
			"        FILTER ( <http://www.w3.org/2001/XMLSchema#date>(CONCAT(STR(year(now())),'-',STR(month(now())),'-',STR(day(now())))) < ?from ) \n" +
			"    } \n" +
			"    FILTER NOT EXISTS { \n" +
			"        ?bp <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validThrough> ?to . \n" +
			"        FILTER ( <http://www.w3.org/2001/XMLSchema#date>(CONCAT(STR(year(now())),'-',STR(month(now())),'-',STR(day(now())))) > ?to ) \n" +
			"    } \n" +
			"    FILTER NOT EXISTS { \n" +
			"        ?bp3 <http://purl.org/goodrelations/v1#successorOf> ?bp . \n" +
			"        ?bp3 <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validFrom> ?from3 . \n" +
			"        FILTER ( <http://www.w3.org/2001/XMLSchema#date>(CONCAT(STR(year(now())),'-',STR(month(now())),'-',STR(day(now())))) >= ?from3 ) \n" +
			"    } \n" +
			"    BIND (STRBEFORE(STR(?bp),'#') as ?bpNsBase) \n" +
			"    # \n" +
			"    ?bp <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasClassificationDimension> ?cd . \n" +
			"    BIND (CONCAT(STRBEFORE(STR(?cd),'#'),'#') AS ?cdNs) \n" +
			"} ";		
		
		// Querying triplestore to get the new defaults (for broker policy namsespace and classification dimensions namespace)
		SparqlServiceClient client = SparqlServiceClientFactory.getClientInstance();
		List<Map<String,RDFNode>> results = client.queryAndProcess(queryStr);
		if (results==null || results.size()==0) return "_setPersistenceDefaults: ERROR: No active Broker Policy found or Classification dimension is missing";
		if (results.size()>1) return "_setPersistenceDefaults: ERROR: More than one active Broker Policy or Classification dimension found";
		
		// Get the new defaults
		Map<String,RDFNode> soln = results.get(0);
		String bp = node2url( soln.get("bp") ).trim();
		String bpNs = val2str( soln.get("bpNs") ).trim();
		String nsPref = val2str( soln.get("nsPref") ).trim();
		String cdNs = val2str( soln.get("cdNs") ).trim();
		if (bp.isEmpty() || bpNs.isEmpty() || nsPref.isEmpty() || cdNs.isEmpty()) return String.format("_setPersistenceDefaults: Results are empty: bp-uri=%s, bpNs=%s, nsPref=%s, cdNs=%s", bp, bpNs, nsPref, cdNs);
		
		// Apply new defaults
		eu.brokeratcloud.persistence.RdfPersistenceManagerImpl.setDefaultUris(bpNs, nsPref, cdNs);
		
		return String.format("_setPersistenceDefaults: OK: bp-uri=%s, bpNs=%s, nsPref=%s, cdNs=%s", bp, bpNs, nsPref, cdNs);
	}
	
	@GET @POST
	@Path("/flush-caches")
	@Produces("application/json")
	public void flushCaches() throws java.io.IOException, ClassNotFoundException {
		synchronized (slpCacheLock) {
			sdCache = new HashMap<String,ServiceDescription>();
			sdCacheLock = new Object();
			slpCache = new HashMap<String,HashMap<String,String>>();
		}
		logger.info("flushCaches: All /opt/aux caches were flushed");
	}
	
	// =============================================================================================================================
	
	// Used for testing. Simulates Cloud Service platform web services for retrieving a provider's services
	// The actual implementation of this service must be provided by Cloud Service platform
	@GET
	@Path("/offerings/by/provider/{username}")
	@Produces("application/json")
	public String[] getProviderOfferings(@PathParam("username") String username) {
		logger.warn("getProviderOfferings: SIMULATES QUERING CLOUD PLATFORM:  username={}", username);
		return _processDummyResponse("dummy-provider-services-source-response", username);
	}
	
	// Used for testing. Simulates FPR web service for checking if a service has already been recommended by FPR
	@GET
	@Path("/frp-recommended/service/{serv_id}/timestamp/{timestamp}")
	@Produces("application/json")
	public String simulateFprRecomResponse(@PathParam("serv_id") String srvUri, @PathParam("timestamp") String tm) throws java.io.UnsupportedEncodingException {
		srvUri = java.net.URLDecoder.decode(srvUri, java.nio.charset.StandardCharsets.UTF_8.toString());
		logger.warn("simulateFprRecomResponse: SIMULATES QUERING FPR:  service={}, timestamp={}", srvUri, tm);
		// Get configured response
		String[] response = _processDummyResponse("dummy-fpr-response", srvUri);
		// Build final response
		StringBuilder sb = new StringBuilder("{ \"recommended\" : [ ");
		boolean first=true;
		for (int i=0; i<response.length; i++) {
			if (first) first=false; else sb.append(", ");
			sb.append("\"").append(response[i]).append("\"");
		}
		sb.append(" ] }");
		return sb.toString();
	}
	
	protected String[] _processDummyResponse(String setting, String username) {
		String[] response = auxConfig.getProperty(setting).split("[\r\n]");
		String[] uri = null;
		// Search for user/service-specific response
		for (int i=0; i<response.length; i++) {
			String line = response[i].trim();
			int p = line.indexOf(":");
			if (p>0 && line.substring(0,p).equalsIgnoreCase(username)) {
				uri = line.substring(p+1).trim().split("[\t, ]");
				break;
			}
		}
		// Search for a default (*) response
		if (uri==null) {
			for (int i=0; i<response.length; i++) {
				String line = response[i].trim();
				int p = line.indexOf(":");
				if (p>0 && line.substring(0,p).equals("*")) {
					uri = line.substring(p+1).trim().split("[\t, ]");
					break;
				}
			}
		}
		// Prepare services/user array
		String[] uriArr = null;
		if (uri!=null) {
			int k=0;
			for (int j=0; j<uri.length; j++) {
				if (!uri[j].trim().isEmpty()) k++;
				else uri[j]=null;
			}
			uriArr = new String[k];
			k=0;
			for (int j=0; j<uri.length; j++) {
				if (uri[j]!=null) uriArr[k++] = uri[j].trim();
			}
		} else {
			uriArr = new String[0];
		}
		return uriArr;
	}
	
	// =============================================================================================================================
	
	protected static String serviceBasicInfoQueryTemplate =
				"SELECT ?title ?description ?creatorName ?creatorLogo ?creatorWeb (group_concat(?cd ; separator = \", \") as ?categories) \n"+
				"WHERE { \n"+
				"	FILTER (?srv = <%s>) . \n"+										// 'srvUri' goes here !!
				"	?srv <http://purl.org/dc/terms/title> ?title . \n"+
				"	OPTIONAL { ?srv <http://purl.org/dc/terms/description> ?description } . \n"+
				"	?srv <http://purl.org/dc/terms/creator> ?creator . \n"+
				"	OPTIONAL { ?creator <http://purl.org/goodrelations/v1#legalName> ?creatorName } . \n"+
				"	OPTIONAL { ?creator <http://xmlns.com/foaf/0.1/homepage> ?creatorHomepage } . \n"+
				"	OPTIONAL { ?creator <http://xmlns.com/foaf/0.1/logo> ?creatorLogo } . \n"+
				"	OPTIONAL { \n"+
				"		?srv <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasServiceModel> ?sm . \n"+
				"		?sm <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasClassificationDimension> ?cd . \n"+
				"	} \n"+
				"} \n"+
				"GROUP BY ?title ?description ?creatorName ?creatorLogo ?creatorWeb \n".intern();
	protected static String serviceLevelProfileQueryTemplate =
					"SELECT DISTINCT ?typ ?allowedValues ?defVal ?defValLabel ?uom ?oneVal ?minVal ?maxVal \n"+
					"WHERE { \n"+
					"	# BROKER POLICY \n"+
					"	BIND ( <%s> as ?slp ). \n"+									// 'slpUri' goes here !!
					"	?bpi ?hasSLP ?slp . \n"+
					"	?hasSLP <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://www.linked-usdl.org/ns/usdl-sla#hasServiceLevelProfile> . \n"+
					"	?bpsmClass <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.linked-usdl.org/ns/usdl-core#ServiceModel> . \n"+
					"	?bpi a ?bpsmClass . \n"+
					"	# SLP \n"+
					"	?slp a ?slpClass . \n"+
					"	?slpClass <http://www.w3.org/2000/01/rdf-schema#subClassOf> * <http://www.linked-usdl.org/ns/usdl-sla#ServiceLevelProfile> . \n"+
					"	?slp ?hasSL ?sl . \n"+
					"	?hasSL <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://www.linked-usdl.org/ns/usdl-sla#hasServiceLevel> . \n"+
					"	# SL \n"+
					"	?sl a ?slClass . \n"+
					"	?slClass <http://www.w3.org/2000/01/rdf-schema#subClassOf> * <http://www.linked-usdl.org/ns/usdl-sla#ServiceLevel> . \n"+
					"	?sl ?hasSLE ?sle . \n"+
					"	?hasSLE <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://www.linked-usdl.org/ns/usdl-sla#hasServiceLevelExpression> . \n"+
					"	# SLE \n"+
					"	?sle a ?sleClass . \n"+
					"	?sleClass <http://www.w3.org/2000/01/rdf-schema#subClassOf> * <http://www.linked-usdl.org/ns/usdl-sla#ServiceLevelExpression> . \n"+
					"	?sle ?hasVar ?var . \n"+
					"	?hasVar <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://www.linked-usdl.org/ns/usdl-sla#hasVariable> . \n"+
					"	# VAR \n"+
					"	?var a ?varClass . \n"+
					"	?varClass <http://www.w3.org/2000/01/rdf-schema#subClassOf> * <http://www.linked-usdl.org/ns/usdl-sla#Variable> . \n"+
					"	?hasDef <http://www.w3.org/2000/01/rdf-schema#range> ?allowedValues . \n"+
					"	{ \n"+
					"		?hasDef <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://www.linked-usdl.org/ns/usdl-core/cloud-broker-sla#hasDefaultQuantitativeValue> . \n"+
					"		{ \n"+
					"			?allowedValues <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#isRange> 'true'^^<http://www.w3.org/2001/XMLSchema#boolean> \n"+
					"				BIND ( 'NUMERIC_RANGE' as ?typ ) \n"+
					"		} \n"+
					"		UNION \n"+
					"		{ \n"+
					"			?allowedValues <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#isRange> 'false'^^<http://www.w3.org/2001/XMLSchema#boolean> . \n"+
					"			{ \n"+
					"				?allowedValues <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#higherIsBetter> 'true'^^<http://www.w3.org/2001/XMLSchema#boolean> \n"+
					"					BIND ( 'NUMERIC_INC' as ?typ ) } \n"+
					"			UNION \n"+
					"			{ \n"+
					"				?allowedValues <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#higherIsBetter> 'false'^^<http://www.w3.org/2001/XMLSchema#boolean> \n"+
					"					BIND ( 'NUMERIC_DEC' as ?typ ) } \n"+
					"		} . \n"+
					"	} \n"+
					"	UNION \n"+
					"	{ \n"+
					"		?hasDef <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://www.linked-usdl.org/ns/usdl-core/cloud-broker-sla#hasDefaultQualitativeValue> . \n"+
					"		?ind a ?allowedValues . \n"+
					"			{ \n"+
					"				?dpvv <http://www.w3.org/2000/01/rdf-schema#domain> ?pv . \n"+
					"				?dpvv <http://www.w3.org/2000/01/rdf-schema#range> ?allowedValues . \n"+
					"				?pv <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.linked-usdl.org/ns/usdl-pref#QualitativeVariable> . \n"+
					"				{ ?ind <http://purl.org/goodrelations/v1#greater> ?ind1 . BIND ( 'LINGUISTIC' as ?typ ) } \n"+
					"				UNION \n"+
					"				{ ?ind <http://purl.org/goodrelations/v1#lesser> ?ind1 . BIND ( 'LINGUISTIC' as ?typ ) } . \n"+
					"			} \n"+
					"			UNION \n"+
					"			{ \n"+
					"				?dpvv <http://www.w3.org/2000/01/rdf-schema#domain> ?pv . \n"+
					"				?dpvv <http://www.w3.org/2000/01/rdf-schema#range> ?allowedValues . \n"+
					"				?pv <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.linked-usdl.org/ns/usdl-pref#BooleanVariable> . \n"+
					"				BIND ( 'BOOLEAN' as ?typ ) . \n"+
					"			} \n"+
					"	} . \n"+
					"	?var ?hasDef ?defVal . \n"+
					"	# DEFAULT VALUES \n"+
					"	OPTIONAL { ?defVal <http://www.w3.org/2000/01/rdf-schema#label> ?defValLabel } . \n"+
					"	OPTIONAL { ?defVal <http://purl.org/goodrelations/v1#hasUnitOfMeasurement> ?uom } . \n"+
					"	OPTIONAL { \n"+
					"		?rel1 <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://purl.org/goodrelations/v1#hasMinValue> . \n"+
					"		?rel1 <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://purl.org/goodrelations/v1#hasMaxValue> . \n"+
					"		?defVal ?rel1 ?oneVal \n"+
					"	} . \n"+
					"	OPTIONAL { \n"+
					"		?rel2 <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://purl.org/goodrelations/v1#hasMinValue> . \n"+
					"		?defVal ?rel2 ?minVal \n"+
					"	} . \n"+
					"	OPTIONAL { \n"+
					"		?rel3 <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://purl.org/goodrelations/v1#hasMaxValue> . \n"+
					"		?defVal ?rel3 ?maxVal \n"+
					"	} . \n"+
					"} \n"+
					"ORDER BY ?allowedValues \n".intern();
	protected static String serviceModelQueryTemplate =
					"SELECT DISTINCT ?typ ?allowedValues ?hasVal ?val ?valLabel ?uom ?oneVal ?minVal ?maxVal ?minSupVal ?maxSupVal ?minKernVal ?maxKernVal ?meanVal \n"+
					"WHERE { \n"+
					"	BIND ( <%s> as ?sm ) . \n"+									// 'smUri' goes here !!
					"	?sm ?hasVal ?val . \n"+
					"	?hasVal <http://www.w3.org/2000/01/rdf-schema#range> ?allowedValues . \n"+
					"	{ ?hasVal <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://purl.org/goodrelations/v1#quantitativeProductOrServiceProperty> .\n"+
					"	  { ?allowedValues <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#isRange> 'true'^^<http://www.w3.org/2001/XMLSchema#boolean> \n"+
					"				BIND ( 'NUMERIC_RANGE' as ?typ ) \n"+
					"	  } \n"+
					"	  UNION \n"+
					"	  { ?allowedValues <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#isRange> 'false'^^<http://www.w3.org/2001/XMLSchema#boolean> . \n"+
					"		{ ?allowedValues <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#higherIsBetter> 'true'^^<http://www.w3.org/2001/XMLSchema#boolean> \n"+
					"				BIND ( 'NUMERIC_INC' as ?typ ) } \n"+
					"		UNION\n"+
					"		{ ?allowedValues <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#higherIsBetter> 'false'^^<http://www.w3.org/2001/XMLSchema#boolean> \n"+
					"				BIND ( 'NUMERIC_DEC' as ?typ ) } \n"+
					"	  } . \n"+
					"	} \n"+
					"	UNION \n"+
					"	{ ?hasVal <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#fuzzyServiceProperty> .\n"+
					"	  { ?allowedValues <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#isRange> 'true'^^<http://www.w3.org/2001/XMLSchema#boolean> \n"+
					"				BIND ( 'FUZZY_RANGE' as ?typ ) \n"+
					"	  } \n"+
					"	  UNION \n"+
					"	  { ?allowedValues <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#isRange> 'false'^^<http://www.w3.org/2001/XMLSchema#boolean> . \n"+
					"		{ ?allowedValues <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#higherIsBetter> 'true'^^<http://www.w3.org/2001/XMLSchema#boolean> \n"+
					"				BIND ( 'FUZZY_INC' as ?typ ) } \n"+
					"		UNION\n"+
					"		{ ?allowedValues <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#higherIsBetter> 'false'^^<http://www.w3.org/2001/XMLSchema#boolean> \n"+
					"				BIND ( 'FUZZY_DEC' as ?typ ) } \n"+
					"	  } . \n"+
					"	} \n"+
					"	UNION \n"+
					"	{ ?hasVal <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://purl.org/goodrelations/v1#qualitativeProductOrServiceProperty> . \n"+
					"	  ?ind a ?allowedValues . \n"+
					"		{ \n"+
					"			?dpvv <http://www.w3.org/2000/01/rdf-schema#domain> ?pv . \n"+
					"			?dpvv <http://www.w3.org/2000/01/rdf-schema#range> ?allowedValues . \n"+
					"			?pv <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.linked-usdl.org/ns/usdl-pref#QualitativeVariable> . \n"+
					"			{ ?ind <http://purl.org/goodrelations/v1#greater> ?ind1 . BIND ( 'LINGUISTIC' as ?typ ) } \n"+
					"			UNION \n"+
					"			{ ?ind <http://purl.org/goodrelations/v1#lesser> ?ind1 . BIND ( 'LINGUISTIC' as ?typ ) } . \n"+
					"		} \n"+
					"		UNION \n"+
					"		{ \n"+
					"			?dpvv <http://www.w3.org/2000/01/rdf-schema#domain> ?pv . \n"+
					"			?dpvv <http://www.w3.org/2000/01/rdf-schema#range> ?allowedValues . \n"+
					"			?pv <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.linked-usdl.org/ns/usdl-pref#BooleanVariable> . \n"+
					"			BIND ( 'BOOLEAN' as ?typ ) . \n"+
					"		} \n"+
					"	} \n"+
					"	UNION \n"+
					"	{ ?hasVal <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://purl.org/goodrelations/v1#datatypeProductOrServiceProperty> . \n"+
					"	  BIND ( 'DATATYPE' as ?typ ) . \n"+
					"	} . \n"+
					"	OPTIONAL { ?val <http://www.w3.org/2000/01/rdf-schema#label> ?valLabel } . \n"+
					"	OPTIONAL { ?val <http://purl.org/goodrelations/v1#hasUnitOfMeasurement> ?uom } . \n"+
					"	OPTIONAL { \n"+
					"		?rel1 <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://purl.org/goodrelations/v1#hasMinValue> . \n"+
					"		?rel1 <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://purl.org/goodrelations/v1#hasMaxValue> . \n"+
					"		?val ?rel1 ?oneVal \n"+
					"	} . \n"+
					"	OPTIONAL { \n"+
					"		?rel2 <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://purl.org/goodrelations/v1#hasMinValue> . \n"+
					"		?val ?rel2 ?minVal \n"+
					"	} . \n"+
					"	OPTIONAL { \n"+
					"		?rel3 <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://purl.org/goodrelations/v1#hasMaxValue> . \n"+
					"		?val ?rel3 ?maxVal \n"+
					"	} . \n"+
					"	OPTIONAL { \n"+
					"		?rel4 <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasMinSupport> . \n"+
					"		?val ?rel4 ?minSupVal \n"+
					"	} . \n"+
					"	OPTIONAL { \n"+
					"		?rel5 <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasMaxSupport> . \n"+
					"		?val ?rel5 ?maxSupVal \n"+
					"	} . \n"+
					"	OPTIONAL { \n"+
					"		?rel6 <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasMinKernel> . \n"+
					"		?val ?rel6 ?minKernVal \n"+
					"	} . \n"+
					"	OPTIONAL { \n"+
					"		?rel7 <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasMaxKernel> . \n"+
					"		?val ?rel7 ?maxKernVal \n"+
					"	} . \n"+
					"	OPTIONAL { \n"+
					"		?rel8 <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> * <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasMaxMembershipValue> . \n"+
					"		?val ?rel8 ?meanVal \n"+
					"	} . \n"+
					"} \n"+
					"ORDER BY ?allowedValues \n".intern();
	
	protected static HashMap<String,ServiceDescription> sdCache = new HashMap<String,ServiceDescription>();
	protected static Object sdCacheLock = new Object();
	protected static HashMap<String,HashMap<String,String>> slpCache = new HashMap<String,HashMap<String,String>>();
	protected static Object slpCacheLock = new Object();
	
	// INTERNAL USE ONLY!!!
	// Retrieves service description for given URI
	protected ServiceDescription _getServiceDescription(String srvUri, String smUri, String slpUri, String bpi, String bpsmc, SparqlServiceClient client) {
		//Get Stat Counters
		if (spl10==-1) spl10 = Stats.get().getOrCreateSplitByName("SOS: AUX._getServiceDescription: QUERY-1");
		if (spl11==-1) spl11 = Stats.get().getOrCreateSplitByName("SOS: AUX._getServiceDescription: QUERY-2");
		if (spl12==-1) spl12 = Stats.get().getOrCreateSplitByName("SOS: AUX._getServiceDescription: QUERY-3");
		if (cnt13==-1) cnt13 = Stats.get().getOrCreateCounterByName("SOS: AUX._getServiceDescription: SD-CACHE hit");
		if (cnt14==-1) cnt14 = Stats.get().getOrCreateCounterByName("SOS: AUX._getServiceDescription: SD-CACHE missed");
		try {
			logger.debug("_getServiceDescription(URI): BEGIN: service-uri={}", srvUri);
			logger.debug("_getServiceDescription(URI): Retrieving Service Description: uri={}", srvUri);
			
			// Check if SD is already cached
			synchronized (sdCacheLock) {
				if (sdCache==null) sdCache = new HashMap<String,ServiceDescription>();
				//logger.trace("SD-CACHE: size={}", sdCache.size());
				ServiceDescription sd_ = sdCache.get(srvUri);
				//logger.trace("SD-CACHE: lookup for srvUri: {}", srvUri);
				if (sd_!=null) {
					//logger.trace("SD-CACHE: hit: {}", srvUri);
					Stats.get().increase(cnt13);
					logger.debug("_getServiceDescription: END: Retrieved from SD-CACHE: uri={}:\nOUTPUT:\n{}", srvUri, sd_);
					return sd_;
				}
				//logger.trace("SD-CACHE: missed: {}", srvUri);
				Stats.get().increase(cnt14);
			}
			
			// Query for basic service information
			String queryStr1 = String.format( serviceBasicInfoQueryTemplate, srvUri );
			logger.trace("_getServiceDescription(URI): Query-1: \n{}", queryStr1);
			queryStr1 = String.format(queryStr1, srvUri);
			Stats.get().startSplit(spl10);
			List<Map<String,RDFNode>> results = client.queryAndProcess(queryStr1);
			Stats.get().endSplit(spl10);
			if (results==null || results.size()==0) throw new Exception("_getServiceDescription(URI): Service description not found");
			if (results.size()>1) throw new Exception("_getServiceDescription(URI): More than one service descriptions returned");
			
			// create service description object
			Map<String,RDFNode> soln = results.get(0);
			String title = val2str( soln.get("title") ).trim();
			String description = val2str( soln.get("description") ).trim();
			String creatorName = val2str( soln.get("creatorName") ).trim();
			String creatorLogo = node2url( soln.get("creatorLogo") ).trim();
			String creatorWeb  = node2url( soln.get("creatorHomepage") ).trim();
			String categories  = node2url( soln.get("categories") ).trim();
			
			String tmp = (creatorLogo.isEmpty()) ? creatorName : String.format("<img src=\"%s\" /> %s", creatorLogo, creatorName).trim();
			if (!tmp.isEmpty() && !creatorWeb.isEmpty()) tmp = String.format("<a href=\"%s\">%s</a>", creatorWeb, tmp);
			creatorName = tmp;
			
			//logger.trace("_getServiceDescription(URI): title={}, sm-uri={}, sm={}, descr={}", title, smUri, description);
			
			ServiceDescription sd = new ServiceDescription();
			sd.setId( srvUri );			// service id is not really used anywhere else but internally in RecommendationManager. No need to assign a new service id
			sd.setName( title );
			sd.setServiceName( title );
			sd.setOwner( creatorName );
			sd.setDescription( description );
			sd.setServiceCategory( categories );
			sd.setServiceModelUri( smUri );
			
			logger.trace("_getServiceDescription(URI): Service description object created: \n{}", sd);
			
			// Check SLP is already in cache
			HashMap<String,String> attrs = null;
			synchronized (slpCacheLock) {
				attrs = slpCache.get(slpUri);
			}
			
			if (attrs==null) {
				// Query for SERVICE LEVEL PROFILE attribute values
				String queryStrSLP = String.format( serviceLevelProfileQueryTemplate, slpUri );
				
				results = null;
				if (slpUri!=null && !slpUri.trim().isEmpty()) {
					logger.trace("_getServiceDescription(URI): Query-SLP: \n{}", queryStrSLP);
					Stats.get().startSplit(spl11);
					results = client.queryAndProcess(queryStrSLP);
					Stats.get().endSplit(spl11);
				} else {
					logger.debug("_getServiceDescription(URI): No Service-Level-Profile specified for Service-Model: {}", smUri);
				}
				attrs = new HashMap<String,String>();
				if (results!=null && results.size()>0) {
					for (Map<String,RDFNode> slpData : results) {
						if (slpData==null || slpData.size()==0) continue;
						// for each attribute...
						String typ = val2str( slpData.get("typ") );
						String apvUri = node2url( slpData.get("allowedValues") );
						String valUri = node2url( slpData.get("defVal") );
						String label  = val2str( slpData.get("defValLabel") );
						String uom    = val2str( slpData.get("uom") );
						String oneVal = val2str( slpData.get("oneVal") );
						String minVal = val2str( slpData.get("minVal") );
						String maxVal = val2str( slpData.get("maxVal") );
						String minSupVal = val2str( slpData.get("minSupVal") );
						String maxSupVal = val2str( slpData.get("maxSupVal") );
						String minKernVal = val2str( slpData.get("minKernVal") );
						String maxKernVal = val2str( slpData.get("maxKernVal") );
						String meanVal = val2str( slpData.get("meanVal") );
						
						typ = typ.trim().toUpperCase();
						if (typ.isEmpty()) typ = "UNORDERED_SET";
						if (typ.isEmpty()) {
							throw new RuntimeException("AuxiliaryService: _getServiceDescription: Encountered EMPTY TYPE while retrieving data for SLP: "+slpUri);
						} else
						if (typ.equals("NUMERIC_INC") || typ.equals("NUMERIC_DEC")) {
							attrs.put(apvUri, oneVal);	// +" "+uom);
						} else
						if (typ.equals("NUMERIC_RANGE")) {
							attrs.put(apvUri, "["+minVal+"-"+maxVal+"]");	// +" "+uom);
						} else
						if (typ.equals("FUZZY_INC") || typ.equals("FUZZY_DEC")) {
							if (!minSupVal.isEmpty() && !meanVal.isEmpty() && !maxSupVal.isEmpty()) {
								attrs.put(apvUri, "("+minSupVal+";"+meanVal+";"+maxSupVal+")");	// +" "+uom);
							} else if (!minVal.isEmpty() && !maxVal.isEmpty()) {
								double m = Double.parseDouble(minVal);
								double M = Double.parseDouble(maxVal);
								double mean = (m+M)/2;
								attrs.put(apvUri, "("+minVal+";"+mean+";"+maxVal+")");	// +" "+uom);
							} else if (!oneVal.isEmpty()) {
								attrs.put(apvUri, "("+oneVal+";"+oneVal+";"+oneVal+")");	// +" "+uom);
							}
						} else
						if (typ.equals("FUZZY_RANGE")) {
							if (!minSupVal.isEmpty() && !minKernVal.isEmpty() && !maxKernVal.isEmpty() && !maxSupVal.isEmpty()) {
								attrs.put(apvUri, "["+minSupVal+";"+minKernVal+";"+maxKernVal+";"+maxSupVal+"]");	// +" "+uom);
							} else if (!minVal.isEmpty() && !maxVal.isEmpty()) {
								attrs.put(apvUri, "["+minVal+";"+minVal+";"+maxVal+";"+maxVal+"]");	// +" "+uom);
							}
						} else
						if (typ.equals("BOOLEAN") || typ.equals("UNORDERED_SET") || typ.equals("LINGUISTIC")) {
							attrs.put(apvUri, label.isEmpty() ? valUri : label);
						} else
						{
							throw new RuntimeException("AuxiliaryService: _getServiceDescription: Encountered UNKNOWN TYPE '"+typ+"' while retrieving data for SLP: "+slpUri);
						}
					}
				}
				
				// Store SLP object is cache
				synchronized (slpCacheLock) {
					if (attrs!=null) slpCache.put(slpUri, attrs);
				}
			}
			// END if (attrs==null)
			attrs = (HashMap<String,String>)attrs.clone();
			logger.trace("_getServiceDescription(URI): SLP values: \n{}", attrs);
			
			// Store SLP in service attributes too
			if (slpUri!=null && !slpUri.trim().isEmpty()) {
				attrs.put(".SERVICE-LEVEL-PROFILE-URI", slpUri);
				int p11 = slpUri.lastIndexOf("#"), p22 = slpUri.lastIndexOf("/");
				p11 = p11>p22 ? p11 : p22;
				String slpId = (p11>-1 && p11+1<slpUri.length()) ? slpUri.substring(p11+1) : slpUri;
				attrs.put(".SERVICE-LEVEL-PROFILE-ID", slpId);
			}
			
			// Query for SERVICE MODEL attribute values
			String queryStrSM = String.format( serviceModelQueryTemplate, smUri );
			
			logger.trace("getServiceDescription(URI): Query-SM: \n{}", queryStrSM);
			Stats.get().startSplit(spl12);
			results = client.queryAndProcess(queryStrSM);
			Stats.get().endSplit(spl12);
			if (results!=null && results.size()>0) {
				for (Map<String,RDFNode> smData : results) {
					if (smData==null || smData.size()==0) continue;
					// for each attribute...
					String typ = val2str( smData.get("typ") );
					String apvUri = node2url( smData.get("allowedValues") );
					String valUri = node2url( smData.get("val") );
					String label  = val2str( smData.get("valLabel") );
					String uom    = val2str( smData.get("uom") );
					String oneVal = val2str( smData.get("oneVal") );
					String minVal = val2str( smData.get("minVal") );
					String maxVal = val2str( smData.get("maxVal") );
					String minSupVal = val2str( smData.get("minSupVal") );
					String maxSupVal = val2str( smData.get("maxSupVal") );
					String minKernVal = val2str( smData.get("minKernVal") );
					String maxKernVal = val2str( smData.get("maxKernVal") );
					String meanVal = val2str( smData.get("meanVal") );
					
					typ = typ.trim().toUpperCase();
					if (typ.isEmpty()) typ = "UNORDERED_SET";
					if (typ.isEmpty()) {
						throw new RuntimeException("AuxiliaryService: _getServiceDescription: Encountered EMPTY TYPE while retrieving data for Service-Model: "+smUri);
					} else
					if (typ.equals("NUMERIC_INC") || typ.equals("NUMERIC_DEC")) {
						attrs.put(apvUri, oneVal);	// +" "+uom);
					} else
					if (typ.equals("NUMERIC_RANGE")) {
						attrs.put(apvUri, "["+minVal+"-"+maxVal+"]");	// +" "+uom);
					} else
					if (typ.equals("FUZZY_INC") || typ.equals("FUZZY_DEC")) {
						if (!minSupVal.isEmpty() && !meanVal.isEmpty() && !maxSupVal.isEmpty()) {
							attrs.put(apvUri, "("+minSupVal+";"+meanVal+";"+maxSupVal+")");	// +" "+uom);
						} else if (!minVal.isEmpty() && !maxVal.isEmpty()) {
							double m = Double.parseDouble(minVal);
							double M = Double.parseDouble(maxVal);
							double mean = (m+M)/2;
							attrs.put(apvUri, "("+minVal+";"+mean+";"+maxVal+")");	// +" "+uom);
						} else if (!oneVal.isEmpty()) {
							attrs.put(apvUri, "("+oneVal+";"+oneVal+";"+oneVal+")");	// +" "+uom);
						}
					} else
					if (typ.equals("FUZZY_RANGE")) {
						if (!minSupVal.isEmpty() && !minKernVal.isEmpty() && !maxKernVal.isEmpty() && !maxSupVal.isEmpty()) {
							attrs.put(apvUri, "["+minSupVal+";"+minKernVal+";"+maxKernVal+";"+maxSupVal+"]");	// +" "+uom);
						} else if (!minVal.isEmpty() && !maxVal.isEmpty()) {
							attrs.put(apvUri, "["+minVal+";"+minVal+";"+maxVal+";"+maxVal+"]");	// +" "+uom);
						}
					} else
					if (typ.equals("BOOLEAN") || typ.equals("UNORDERED_SET") || typ.equals("LINGUISTIC")) {
						attrs.put(apvUri, label.isEmpty() ? valUri : label);
					} else
					{
						throw new RuntimeException("AuxiliaryService: _getServiceDescription: Encountered UNKNOWN TYPE '"+typ+"' while retrieving data for Service-Model: "+smUri);
					}
				}
			}
			logger.trace("_getServiceDescription(URI): SLP+SM values: \n{}", attrs);
			
			// store combined attribute values to service description object
			for (String at : attrs.keySet()) {
				sd.setServiceAttributeValue(at, attrs.get(at));
			}
			
			// Cache SD for future reference
			if (sd!=null) {
				synchronized (sdCacheLock) {
					//logger.trace("SD-CACHE: caching srvUri: {}", srvUri);
					sdCache.put(srvUri, sd);
					//logger.trace("SD-CACHE: size={}", sdCache.size());
				}
			}
			
			logger.debug("_getServiceDescription: END: uri={}:\nOUTPUT:\n{}", srvUri, sd);
			return sd;
		} catch (Exception e) {
			logger.error("getServiceDescription(URI): EXCEPTION THROWN:\n", e);
			logger.error("getServiceDescription(URI): OUTPUT: Returning null");
			return null;
		}
	}
	
	// INTERNAL USE ONLY!!!
	protected String val2str(Object o) {
		if (o==null) return "";
		String str = o.toString().trim();
		String type = "";
		int p = str.indexOf("^^");
		if (p!=-1) {
			String tmp = str;
			str = tmp.substring(0,p);
			type = tmp.substring(p+2);
			if (type.startsWith("<") && type.endsWith(">")) type = type.substring(1, type.length()-1);
			p = type.lastIndexOf("#");
			if (p==-1) p = type.lastIndexOf(":");
			if (p!=-1) {
				type = type.substring(p+1);
			}
			type = type.toLowerCase();
		} else {
			p = str.lastIndexOf("@");
			if (p!=-1) {
				int p2 = str.lastIndexOf("\"");
				if (p>p2) str = str.substring(0,p);
			}
		}
		if (str.startsWith("\"") && str.endsWith("\"") && str.length()>1) str = str.substring(1,str.length()-1);
		if (type.equals("int") || type.equals("integer") || type.equals("float") || type.equals("double")) str = str.replace(",", ".");
		return str;
	}
	
	// INTERNAL USE ONLY!!!
	protected String node2url(RDFNode r) {
		if (r==null) return "";
		String s = r.toString();
		if (s.startsWith("<")  && s.endsWith(">"))  s  = s.substring(1, s.length()-1);
		return s;
	}
}
