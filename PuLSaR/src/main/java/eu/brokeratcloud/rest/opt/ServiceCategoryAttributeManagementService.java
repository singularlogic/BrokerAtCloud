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

import eu.brokeratcloud.common.ClassificationDimension;
import eu.brokeratcloud.common.ClassificationDimensionScheme;
import eu.brokeratcloud.common.Util;
import eu.brokeratcloud.common.policy.*;
import eu.brokeratcloud.opt.*;
import eu.brokeratcloud.opt.policy.*;
import eu.brokeratcloud.persistence.RdfPersistenceManager;
import eu.brokeratcloud.persistence.RdfPersistenceManagerFactory;
import eu.brokeratcloud.persistence.SparqlServiceClient;
import eu.brokeratcloud.persistence.SparqlServiceClientFactory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/opt/service-category/")
public class ServiceCategoryAttributeManagementService extends AbstractManagementService {
	protected static final Logger logger = LoggerFactory.getLogger("eu.brokeratcloud.rest.opt.SCA");
	
	protected static final String qryAllCategories =
		"SELECT ?concept \n" +
		//"#SELECT ?bpi ?bpsm ?rootConcept ?concept \n" +
		"WHERE { \n" +
		"	FILTER ( ?bpi = <%s> ) \n" +
		"	?bpsm <http://www.w3.org/2000/01/rdf-schema#subClassOf> * <http://www.linked-usdl.org/ns/usdl-core#ServiceModel> . \n" +
		"	?bpi a ?bpsm . \n" +
		"	?bpi <http://www.linked-usdl.org/ns/usdl-core#hasEntityInvolvement> ?involvement . \n" +
		"	FILTER NOT EXISTS { ?successor <http://purl.org/goodrelations/v1#successorOf> ?bpi . } \n" +
		"	FILTER NOT EXISTS {  \n" +
		"		?bpi <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validFrom> ?validFrom .  \n" +
		"		FILTER ( \n" +
		"			<http://www.w3.org/2001/XMLSchema#date>( CONCAT(STR(year(now())),'-',STR(month(now())),'-',STR(day(now()))) ) < ?validFrom  \n" +
		"		) . \n" +
		"	} \n" +
		"	FILTER NOT EXISTS {  \n" +
		"		?bpi <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validThrough> ?validTo .  \n" +
		"		FILTER ( \n" +
		"			<http://www.w3.org/2001/XMLSchema#date>( CONCAT(STR(year(now())),'-',STR(month(now())),'-',STR(day(now()))) ) > ?validTo  \n" +
		"		) . \n" +
		"	} \n" +
		"	?bpi <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasClassificationDimension> ?rootConcept . \n" +
		"	?concept a <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#ClassificationDimension> . \n" +
		"	?concept <http://www.w3.org/2004/02/skos/core#broader> * ?rootConcept . \n" +
		"} \n" +
		"";
	
	protected static SparqlServiceClient client = null;
	
	@GET
	@Path("/list")
	@Produces("application/json")
	public String getCategoryTreeAsString() {
		try {
			// Get active broker policy URI
			HashMap<String,String> hm = new AuxiliaryService().getAppliedBrokerPolicy();
			String activeBpUri = hm!=null ? hm.get("bp-uri") : null;
			logger.debug("Active broker policy uri: {}", hm);
			
			// Get service classification dimensions
			eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
			List<Object> list = null;
			list = pm.findByQuery( String.format(qryAllCategories, activeBpUri) );
			logger.debug("{} service classification dimensions found", list.size());
			
			// Process service classification dimensions - group by classification dimension scheme
			boolean first = true;
			StringBuilder sb = new StringBuilder("[ ");
			if (list!=null) {
				// Process classification dimensions
				//List<ClassificationDimensionScheme> dimList = new LinkedList<ClassificationDimensionScheme>();
				for (Object o : list) {
					ClassificationDimension sc = (ClassificationDimension)o;
					String id = sc.getId();
					if (id==null) { id = pm.getObjectUri(sc); sc.setId(id); }
					String text = sc.getPrefLabel();
					String parent;
					if (sc.getParent()!=null) { parent = sc.getParent().getId(); if (parent==null) { parent = pm.getObjectUri(sc.getParent()); sc.getParent().setId(parent); } }
					else { parent = "#"; }
					if (first) first=false; else sb.append(", ");
					sb.append( String.format("{ 'id':'%s', 'text':'%s', 'parent':'%s' }", id, text, parent) );
				}
			}
			sb.append(" ]");
			String str = sb.toString().replace("'", "\"");
			return str;
		} catch (Exception e) {
			logger.error("getCategoryTreeAsString: EXCEPTION THROWN: {}", e);
			logger.debug("getCategoryTreeAsString: Returning an empty String");
			return "";
		}
	}
	
	// GET /opt/service-category/{cat_id}/attributes/
	// Description: Get a list of service category attributes for a service category
	@GET
	@Path("/{cat_id}/attributes")
	@Produces("application/json")
	public ServiceCategoryAttribute[] getServiceCategoryAttributes(@PathParam("cat_id") String catId) {
		try {
			return _getServiceCategoryAttributes(catId);
		} catch (Exception e) {
			logger.error("getServiceCategoryAttributes: EXCEPTION THROWN: {}", e);
			logger.debug("getServiceCategoryAttributes: Returning an empty array of {}", ServiceCategoryAttribute.class);
			return new ServiceCategoryAttribute[0];
		}
	}
	
	// GET /opt/service-category/{cat_id}/attributes-all
	// Description: Get a list of service category attributes for a service category as well as the attributes of its parent categories
	@GET
	@Path("/{cat_id}/attributes-all")
	@Produces("application/json")
	public ServiceCategoryAttributesContainer[] getServiceCategoryAttributesALL(@PathParam("cat_id") String catId) throws java.io.IOException {
		try {
			return _getServiceCategoryAttributesALL(catId);
		} catch (Exception e) {
			logger.error("getServiceCategoryAttributesALL: EXCEPTION THROWN: {}", e);
			logger.debug("getServiceCategoryAttributesALL: Returning an empty array of {}", ServiceCategoryAttributesContainer.class);
			return new ServiceCategoryAttributesContainer[0];
		}
	}
	
	// GET /opt/service-category/attributes/{id}
	// Description: Get service category attribute description
	@GET
	@Path("/attributes/{id}")
	@Produces("application/json")
	public ServiceCategoryAttribute getServiceCategoryAttribute(@PathParam("id") String id) {
		try {
			return _getServiceCategoryAttribute(id);
		} catch (Exception e) {
			logger.error("getServiceCategoryAttribute: EXCEPTION THROWN: {}", e);
			logger.debug("getServiceCategoryAttribute: Returning an empty instance of {}", ServiceCategoryAttribute.class);
			return new ServiceCategoryAttribute();
		}
	}
	
	// PUT /opt/service-category/attributes
	// Description: Create a new service category attribute
	@PUT
	@Path("/attributes")
	@Consumes("application/json")
	public Response createServiceCategoryAttribute(ServiceCategoryAttribute attr) {
		try {
			String newId = _createServiceCategoryAttribute(attr);
			logger.debug("createServiceCategoryAttribute: New Service Category Attribute id = {}", newId);
			return createResponse(HTTP_STATUS_CREATED, "Result=Created");
		} catch (Exception e) {
			logger.error("createServiceCategoryAttribute: EXCEPTION THROWN: {}", e);
			logger.debug("createServiceCategoryAttribute: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
	
	private enum UPDATE_STRATEGY {
		DELETE_CREATE, UPDATE_FIELDS
	};
	
	// POST /opt/service-category/attributes
	// Description: Update a service category attribute
	@POST
	@Path("/attributes")
	@Consumes("application/json")
	public Response updateServiceCategoryAttribute(ServiceCategoryAttribute attr) {
		try {
			UPDATE_STRATEGY updateStrategy = UPDATE_STRATEGY.DELETE_CREATE;
			String updStr = optConfig.getProperty("broker-policy-update-strategy");
			try {
				if (updStr!=null && !updStr.trim().isEmpty()) updateStrategy = UPDATE_STRATEGY.valueOf(updStr);
				else logger.debug("updateServiceCategoryAttribute: No Update Strategy specified in configuration");
			} catch (Exception e) {
				logger.warn("updateServiceCategoryAttribute: Invalid Update Strategy: {}", updStr);
			} finally {
				logger.debug("updateServiceCategoryAttribute: Using Update Strategy: {}", updateStrategy);
			}
			
			if (updateStrategy==UPDATE_STRATEGY.DELETE_CREATE) {
				String newId = _updateServiceCategoryAttribute(attr);
				logger.debug("updateServiceCategoryAttribute: New Service Category Attribute id = {}", newId);
			} else
			if (updateStrategy==UPDATE_STRATEGY.UPDATE_FIELDS) {
				String fieldsToUpdate = optConfig.getProperty("broker-policy-update-strategy-allowed-fields");
				String id = _updateSCAFields(fieldsToUpdate, attr);
				logger.debug("updateServiceCategoryAttribute: Keep the same Service Category Attribute id = {}", id);
			}
			return createResponse(HTTP_STATUS_OK, "Result=Updated");
		} catch (Exception e) {
			logger.error("updateServiceCategoryAttribute: EXCEPTION THROWN: {}", e);
			logger.debug("updateServiceCategoryAttribute: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
	
	// DELETE /opt/service-category/attributes/{id}
	// Description: Delete a service category attribute
	@DELETE
	@Path("/attributes/{sca_id}")
	public Response deleteServiceCategoryAttribute(@PathParam("sca_id") String id) {
		try {
			_deleteServiceCategoryAttribute(id);
			return createResponse(HTTP_STATUS_OK, "Result=Deleted");
		} catch (Exception e) {
			logger.error("deleteServiceCategoryAttribute: EXCEPTION THROWN: {}", e);
			logger.debug("deleteServiceCategoryAttribute: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
	
	// =====================================================================================

	protected static final String qryServCatAttrs = 
		"SELECT ?pv ?oa \n" +
		"WHERE {  \n" +
		"	?pv  <http://www.w3.org/2000/01/rdf-schema#subClassOf>*  <http://www.linked-usdl.org/ns/usdl-pref#PreferenceVariable> . \n" +
		"	?pv  <http://www.linked-usdl.org/ns/usdl-pref#belongsTo> <%s> . \n" +
		"	?pv  <http://www.linked-usdl.org/ns/usdl-pref#refToServiceAttribute> ?oa . \n" +
		"	# \n" +
		"	?dpvp <http://www.w3.org/2000/01/rdf-schema#domain> ?pv . \n" +
		"	?dpvp <http://www.w3.org/2000/01/rdf-schema#range> ?apv . \n" +
		"	# \n" +
		"	?bpsm <http://www.w3.org/2000/01/rdf-schema#subClassOf> * <http://www.linked-usdl.org/ns/usdl-core#ServiceModel> . \n" +
		"	?bpi a ?bpsm . \n" +
		"	?bpi <http://www.linked-usdl.org/ns/usdl-core#hasEntityInvolvement> ?involvement . \n" +
		"	?at <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> ?sth . \n" +
		"	?at <http://www.w3.org/2000/01/rdf-schema#domain> ?bpsm . \n" +
		"	?at <http://www.w3.org/2000/01/rdf-schema#range> ?apv \n" +
		"	# \n" +
		"	FILTER NOT EXISTS { \n" +
		"		?successor <http://purl.org/goodrelations/v1#successorOf> ?bpi . \n" +
		"		?successor <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validFrom> ?succValidFrom . \n" +
		"		FILTER ( \n" +
		"			<http://www.w3.org/2001/XMLSchema#date>(CONCAT(STR(year(now())),'-',STR(month(now())),'-',STR(day(now())),'Z')) >= ?succValidFrom \n" +
		"		) . \n" +
		"	} \n" +
		"	FILTER NOT EXISTS {  \n" +
		"		?bpi <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validFrom> ?validFrom .  \n" +
		"		FILTER ( \n" +
		"			<http://www.w3.org/2001/XMLSchema#date>( CONCAT(STR(year(now())),'-',STR(month(now())),'-',STR(day(now()))) ) < ?validFrom  \n" +
		"		) . \n" +
		"	} \n" +
		"	FILTER NOT EXISTS {  \n" +
		"		?bpi <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#validThrough> ?validTo .  \n" +
		"		FILTER ( \n" +
		"			<http://www.w3.org/2001/XMLSchema#date>( CONCAT(STR(year(now())),'-',STR(month(now())),'-',STR(day(now()))) ) > ?validTo  \n" +
		"		) . \n" +
		"	} \n" +
		"} ORDER BY ?s \n" +
		"";

	protected static ServiceCategoryAttribute[] _getServiceCategoryAttributes(String catId) throws Exception {
		logger.trace("getServiceCategoryAttributes: BEGIN: classification dimension id={}", catId);
		eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
		
		String cdUri = pm.getObjectUri(catId, ClassificationDimension.class);
		String queryStr = String.format( qryServCatAttrs, cdUri );
		logger.trace("getServiceCategoryAttributes: Preference Variable Id retrieval query: {}", queryStr);
		
		if (client==null) client = SparqlServiceClientFactory.getClientInstance();
 		logger.trace("getServiceCategoryAttributes: Querying preference variable ID's for classification dimension: {}", catId);
		List<String> results = client.queryForIds(queryStr, "?pv");
		logger.debug("getServiceCategoryAttributes: preference variables for classification dimension found: {}", results.size());
 		logger.trace("getServiceCategoryAttributes: preference variable Id's returned: {}", results);
		
		logger.debug("getServiceCategoryAttributes: Retrieving service category attributes for classification dimension: {}", catId);
		Vector<ServiceCategoryAttribute> scaVect = new Vector<ServiceCategoryAttribute>();
		for (String uri : results) {
			// remove brackets from uri
			if (uri.startsWith("<") && uri.endsWith(">")) uri = uri.substring(1, uri.length()-1);
			
			// Retrieve SCA objects for each valid preference variable Id
			logger.trace("getServiceCategoryAttributes: Retrieving SCA for Pref. Var.: {}", uri);
			ServiceCategoryAttribute sca = _getServiceCategoryAttribute(uri);
			logger.trace("getServiceCategoryAttributes: SCA retrieved: {}", sca);
			if (sca!=null) scaVect.add(sca);
		}
		ServiceCategoryAttribute[] scaList = scaVect.toArray(new ServiceCategoryAttribute[scaVect.size()]);
		
		logger.trace("getServiceCategoryAttributes: END: results={}", scaList);
		return scaList;
	}
	
	protected static ServiceCategoryAttributesContainer[] _getServiceCategoryAttributesALL(String catIdStr) throws Exception {
		logger.trace("getServiceCategoryAttributesALL: BEGIN: service classification id's={}", catIdStr);
		eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
		
		java.util.Stack<String> categories = new java.util.Stack<String>();
		java.util.Stack<String> categoryNames = new java.util.Stack<String>();
		
		String[] catIdArr = catIdStr.split("[,]");
		for (String catId : catIdArr) {
			// Build query
			String catUri = pm.getObjectUri(catId, ClassificationDimension.class);
			int p = catUri.indexOf('#');
			if (p==-1) catUri.lastIndexOf('/');
			String cdUri = catUri.substring(0, p+1);
			String queryStr = 
				"select ?scId ?scName where { \n"+
				"  { \n"+
				"    select ?scId ?scName where { \n"+
				"      <"+catUri+"> <http://www.w3.org/2004/02/skos/core#broader>*   ?mid . \n"+
				"      ?mid   <http://www.w3.org/2004/02/skos/core#broader>*   ?class . \n"+
				"      ?class   <http://www.w3.org/2004/02/skos/core#prefLabel>   ?name . \n"+
				"      bind( replace(str(?class),'"+cdUri+"','') as ?scId ) . \n"+
				"      bind( str(?name) as ?scName ) . \n"+
				"    } \n"+
				"    group by ?class ?scId ?scName \n"+
				"    order by count(?mid) \n"+
				"  } \n"+
				"} ";
			logger.trace("getServiceCategoryAttributesALL: Service classifications retrieval query: \n{}", queryStr);
			logger.debug("getServiceCategoryAttributesALL: Retrieving service attributes for ALL classifications in hierarchy: {}", catId);
			
			// Retrieve service category hierarchy
			if (client==null) client = SparqlServiceClientFactory.getClientInstance();
			QueryExecution qeSelect = client.query(queryStr);
			try {
				ResultSet results = qeSelect.execSelect();
				// Iterating over the SPARQL Query results
				while (results.hasNext()) {
					QuerySolution soln = results.nextSolution();
					if (soln==null) continue;
					String scId = java.net.URLDecoder.decode( soln.get("scId").toString(), "utf-8");
					String scName = java.net.URLDecoder.decode( soln.get("scName").toString(), "utf-8");
					if (scId==null || scName==null) continue;
					if (!categories.contains(scId)) {
						categories.push(scId);
						categoryNames.push(scName);
					}
				}
			} finally {
				qeSelect.close();
			}
			logger.debug("Service Category hierarchy: {}", categories);
		}
		
		// Retrieve service category attributes for every service category in hierarchy
		ServiceCategoryAttributesContainer[] all_categories = new ServiceCategoryAttributesContainer[categories.size()];
		for (int i=0, n=categories.size(); i<n; i++) {
			String scId = categories.get(i);
			String scName = categoryNames.get(i);
			ServiceCategoryAttribute[] tmp = _getServiceCategoryAttributes(scId);
			ServiceCategoryAttributesContainer container = new ServiceCategoryAttributesContainer();
			container.setServiceCategory(scId);
			container.setServiceCategoryName(scName);
			container.setServiceCategoryAttributes(tmp);
			all_categories[i] = container;
		}
		
		logger.debug("getServiceCategoryAttributesALL: Results: \n{}", java.util.Arrays.deepToString(all_categories));
		
		return all_categories;
	}
	
	/* Helper class containing references to all broker policy objects referring to the same service attribute/property */
	public static class PolicyObjects {
		public String bppUri;
		public String apvUri;
		public String pvUri;
		public String dpvvUri;
		public BrokerPolicyProperty bpp;
		public AllowedPropertyValue apv;
		public PreferenceVariable pv;
		public DefaultPreferenceVariableValue dpvv;
		public List<QualitativePropertyValue> individuals;
		public QualitativePropertyValue topIndividual;
		public String datatype;
		
		public PolicyObjects(String bppUri, BrokerPolicyProperty bpp, String apvUri, AllowedPropertyValue apv, String pvUri, PreferenceVariable pv, 
							String dpvvUri, DefaultPreferenceVariableValue dpvv, List<QualitativePropertyValue> individuals, QualitativePropertyValue topIndividual,
							String datatype)
		{
			this.bppUri = bppUri;
			this.apvUri = apvUri;
			this.pvUri = pvUri;
			this.dpvvUri = dpvvUri;
			this.bpp = bpp;
			this.apv = apv;
			this.pv = pv;
			this.dpvv = dpvv;
			this.individuals = individuals;
			this.topIndividual = topIndividual;
			this.datatype = datatype;
		}
		
		public String toString() {
			return String.format("POLICY-OBJECTS: { bpp-uri=%s, bpp=%s, apv-uri=%s, apv=%s, datattype=%s, pv-uri=%s, pv=%s, dpvv-uri=%s, dpvv=%s }", bppUri, bpp, apvUri, apv, datatype, pvUri, pv, dpvvUri, dpvv);
		}
	} // End of PolicyObjects
	
	// It is public because it is used by feedback component
	public static String _getPrefVarFromAV(String avUri) {
		logger.trace("_getPrefVarFromAV: BEGIN: av-uri={}", avUri);		// attribute allowed value uri
		String queryStr = 
				"select ?pv \n" +
				"where { \n" +
				"	?hasDef <http://www.w3.org/2000/01/rdf-schema#range> <%s> . \n" +
				"	?hasDef <http://www.w3.org/2000/01/rdf-schema#domain> ?pv . \n" +
				"	?pv <http://www.w3.org/2000/01/rdf-schema#subClassOf> * <http://www.linked-usdl.org/ns/usdl-pref#PreferenceVariable> . \n" +
				"} \n";
		if (client==null) client = SparqlServiceClientFactory.getClientInstance();
		String qry = String.format(queryStr, avUri);
		logger.trace("_getPrefVarFromAV: Query=\n{}", qry);
		Object val = client.queryValue(qry);
		logger.trace("_getPrefVarFromAV: result: {}", val);
		if (val==null) {
			logger.trace("_getPrefVarFromAV: END: pv-uri=NULL");
			return null;
		}
		String pvUri = val.toString();									// attribute preference variable uri
		logger.trace("_getPrefVarFromAV: END: pv-uri={}", pvUri);
		
		return pvUri;
	}
	
	// It is public because it is used by feedback component
	public static String _getAllowedValueFromPV(String pvUri) {
		logger.trace("_getAllowedValueFromPV: BEGIN: pv-uri={}", pvUri);		// attribute preference variable uri
		String queryStr = 
				"select ?av \n" +
				"where { \n" +
				"	?hasDef <http://www.w3.org/2000/01/rdf-schema#range> ?av . \n" +
				"	?hasDef <http://www.w3.org/2000/01/rdf-schema#domain> <%s> . \n" +
				"	?hasDef <http://www.w3.org/2000/01/rdf-schema#domain> ?pv . \n" +
				"	?pv <http://www.w3.org/2000/01/rdf-schema#subClassOf> * <http://www.linked-usdl.org/ns/usdl-pref#PreferenceVariable> . \n" +
				"} \n";
		if (client==null) client = SparqlServiceClientFactory.getClientInstance();
		String qry = String.format(queryStr, pvUri);
		logger.trace("_getAllowedValueFromPV: Query=\n{}", qry);
		Object val = client.queryValue(qry);
		logger.trace("_getAllowedValueFromPV: result: {}", val);
		if (val==null) {
			logger.trace("_getAllowedValueFromPV: END: av-uri=NULL");
			return null;
		}
		String avUri = val.toString();									// attribute allowed values uri
		logger.trace("_getAllowedValueFromPV: END: av-uri={}", avUri);
		
		return avUri;
	}
	
	// It is public because it is used by feedback component
	public static String _getAttributeNameFromPV(String pvUri) {
		logger.trace("_getAttributeNameFromPV: BEGIN: pv-uri={}", pvUri);		// attribute preference variable uri
		String queryStr = 
				"select ?attrName \n" +
				"where { \n" +
				"	BIND ( <%s> as ?pv ) . \n" +
				"	?pv <http://www.w3.org/2000/01/rdf-schema#subClassOf> * <http://www.linked-usdl.org/ns/usdl-pref#PreferenceVariable> . \n" +
				"	?pv <http://www.linked-usdl.org/ns/usdl-pref#refToServiceAttribute> ?attr . \n" +
				"	?attr <http://purl.org/dc/terms/title> ?name . \n" +
				"	BIND ( str(?name) as ?attrName ) . \n" +
				"} \n";
		if (client==null) client = SparqlServiceClientFactory.getClientInstance();
		String qry = String.format(queryStr, pvUri);
		logger.trace("_getAttributeNameFromPV: Query=\n{}", qry);
		Object val = client.queryValue(qry);
		logger.trace("_getAttributeNameFromPV: result: {}", val);
		if (val==null) {
			logger.trace("_getAttributeNameFromPV: END: attr-name=NULL");
			return null;
		}
		String attrName = val.toString();									// attribute allowed values uri
		logger.trace("_getAttributeNameFromPV: END: attr-name={}", attrName);
		
		return attrName;
	}
	
	public static ServiceCategoryAttribute _getServiceCategoryAttribute(String id) throws Exception {
		logger.trace("getServiceCategoryAttribute: BEGIN: id={}", id);
		eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
		
		PolicyObjects po = _retrieveBrokerPolicyObjects(pm, id, false);
		if (po==null) {
			logger.trace("getServiceCategoryAttribute: END: sca=NULL");
			return null;
		}
		
		ServiceCategoryAttribute sca = _prepareServiceCategoryAttribute(id, po.bpp, po.apv, po.pv, po.individuals, po.topIndividual);
		logger.trace("getServiceCategoryAttribute: END: sca={}", sca);
		return sca;
	}
	
	public static PolicyObjects getBrokerPolicyObjects(String pvUri, boolean includePrefVarDefault) {
		try {
			logger.trace("getBrokerPolicyObjects: BEGIN: pref.var. uri={}", pvUri);
			eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
			PolicyObjects po = _retrieveBrokerPolicyObjects(pm, pvUri, includePrefVarDefault);
			logger.trace("getBrokerPolicyObjects: END: po={}", po);
			return po;
		} catch (Exception e) {
			logger.error("getBrokerPolicyObjects: EXCEPTION THROWN: ", e);
			return null;
		}
	}
	
	public static ServiceCategoryAttribute getServiceCategoryAttributeFromPreference(ConsumerPreference pref) {
		ServiceCategoryAttribute sca = null;
		String pvUri = pref.getPrefVariable();
		ServiceCategoryAttributeManagementService.PolicyObjects po = ServiceCategoryAttributeManagementService.getBrokerPolicyObjects(pvUri, false);
		logger.trace("getServiceCategoryAttributeFromPreference: policy objects={}", po);
		if (po==null) { logger.error("getServiceCategoryAttributeFromPreference: END: po is null : pvUri={}", pvUri); return null; }
		PreferenceVariable pv = po.pv;
		
		if (pv==null) { logger.error("getServiceCategoryAttributeFromPreference: END: pv is null : po={}", po); return null; }
		eu.brokeratcloud.common.policy.BrokerPolicyProperty bpp = po.bpp;
		if (bpp==null) { logger.error("getServiceCategoryAttributeFromPreference: END: bpp is null : po={}", po); return null; }
		eu.brokeratcloud.common.policy.AllowedPropertyValue apv = po.apv;
		if (apv==null) { logger.error("getServiceCategoryAttributeFromPreference: END: apv is null : po={}", po); return null; }
		// get individuals
		List<eu.brokeratcloud.common.policy.QualitativePropertyValue> individuals = null;
		eu.brokeratcloud.common.policy.QualitativePropertyValue topIndividual = null;
		if (bpp instanceof eu.brokeratcloud.common.policy.BrokerPolicyQualitativeProperty) {
			//ASSERT: 'apv' allowed values have already been initialized
			individuals = Arrays.asList( ((eu.brokeratcloud.common.policy.AllowedQualitativePropertyValue)apv).getAllowedValues() );
			logger.trace("getServiceCategoryAttributeFromPreference: Individuals: {}", individuals);
			if (individuals!=null && individuals.size()>0) topIndividual = individuals.get(individuals.size()-1);
		}
		
		String apvUri = po.apvUri;	// use apvUri as SCA id (which is used to lookup service attributes in 'AhpHelper.rank' method)
		sca = _prepareServiceCategoryAttribute(apvUri, bpp, apv, pv, individuals, topIndividual);
		return sca;
	}
	
	protected static PolicyObjects _retrieveBrokerPolicyObjects(eu.brokeratcloud.persistence.RdfPersistenceManager pm, String pvUri, boolean includePrefDefault) throws Exception {
		logger.trace("_retrieveBrokerPolicyObjects: BEGIN: Retrieving broker policy object URIs related to pref. var.: {}", pvUri);
		
		if (client==null) client = SparqlServiceClientFactory.getClientInstance();
		
		Object o = null;
		if ((o=client.queryValue( String.format("SELECT ?s WHERE { ?s <http://www.w3.org/2000/01/rdf-schema#domain> <%s> }", pvUri) ))==null) return null;
		String dpvvUri = o.toString();
		if ((o=client.queryValue( String.format("SELECT ?s WHERE { <%s> <http://www.w3.org/2000/01/rdf-schema#range> ?s }", dpvvUri) ))==null) return null;
		String apvUri = o.toString();
		if ((o=client.queryValue( String.format("SELECT ?s WHERE { ?s <http://www.w3.org/2000/01/rdf-schema#range> <%s> . {?s <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://purl.org/goodrelations/v1#quantitativeProductOrServiceProperty> } union {?s <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://purl.org/goodrelations/v1#qualitativeProductOrServiceProperty> } union {?s <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://purl.org/goodrelations/v1#datatypeProductOrServiceProperty> } union {?s <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#fuzzyServiceProperty> } }", apvUri) ))==null) return null;
		String bppUri = o.toString();
		logger.trace("_retrieveBrokerPolicyObjects: Broker policy objects' URIs: bp.property={}, allowed-values={}, pref-var={}, pref-var-default={}", bppUri, apvUri, pvUri, dpvvUri);
		
		//logger.trace("_retrieveBrokerPolicyObjects: Retrieving Broker policy property: {}", bppUri);
		BrokerPolicyProperty bpp = (BrokerPolicyProperty)pm.find(bppUri);
		//logger.trace("_retrieveBrokerPolicyObjects: Retrieving Allowed property values: {}", apvUri);
		AllowedPropertyValue apv = (AllowedPropertyValue)pm.find(apvUri);
		//logger.trace("_retrieveBrokerPolicyObjects: Retrieving Preference Variable: {}", pvUri);
		PreferenceVariable pv = (PreferenceVariable)pm.find(pvUri);
		logger.trace("_retrieveBrokerPolicyObjects: Retrieving Default Preference Variable Value property: {}", includePrefDefault ? dpvvUri : "** Not included **");
		DefaultPreferenceVariableValue dpvv = null;
		if (includePrefDefault) {
			dpvv = (DefaultPreferenceVariableValue)pm.find(dpvvUri);
		}
		
		logger.trace("_retrieveBrokerPolicyObjects: Retrieving Broker policy objects using their URIs: Done\n** BPP={}\n** APV={}\n** PV={}\n** DPVV={}", bpp, apv, pv, dpvv);
		
		// Retrieve related individuals if qualitative property
		List<QualitativePropertyValue> individuals = null;
		QualitativePropertyValue topIndividual = null;
		if (bpp instanceof BrokerPolicyQualitativeProperty) {
			individuals = getIndividuals(pm, apvUri, (AllowedQualitativePropertyValue)apv);
			if (individuals.size()>0) {
				topIndividual = individuals.get(individuals.size()-1);
				((AllowedQualitativePropertyValue)apv).setAllowedValues( individuals.toArray(new QualitativePropertyValue[ individuals.size() ]) );
			}
		}
		
		// If it is a datatype property then get datatype
		String datatype = null;
		if (bpp instanceof BrokerPolicyDatatypeProperty) {
			datatype = ((BrokerPolicyDatatypeProperty)bpp).getDatatype();
		}
		
		logger.trace("_retrieveBrokerPolicyObjects: BrokerPolicyProperty:\n{}", bpp);
		logger.trace("_retrieveBrokerPolicyObjects: Datatype:\n{}", datatype);
		logger.trace("_retrieveBrokerPolicyObjects: AllowedPropertyValues:\n{}", apv);
		logger.trace("_retrieveBrokerPolicyObjects: Individuals:  {}", individuals);
		logger.trace("_retrieveBrokerPolicyObjects: Top Individual:  {}", topIndividual);
		logger.trace("_retrieveBrokerPolicyObjects: PreferenceVariable:\n{}", pv);
		logger.trace("_retrieveBrokerPolicyObjects: DefaultPreferenceVariableValue:\n{}", dpvv);
		
		// Create and populate a new 'po' instance
		PolicyObjects po = new PolicyObjects(bppUri, bpp, apvUri, apv, pvUri, pv, dpvvUri, dpvv, individuals, topIndividual, datatype);
		
		logger.trace("_retrieveBrokerPolicyObjects: END: results = ** see above **");
		return po;
	}
	
	public static List<QualitativePropertyValue> getIndividuals(eu.brokeratcloud.persistence.RdfPersistenceManager pm, String aqpvUri, AllowedQualitativePropertyValue aqpv) throws Exception {
		logger.trace("getIndividuals: BEGIN: apv-uri={}", aqpvUri);
		if (aqpvUri==null || aqpvUri.trim().isEmpty()) {
			aqpvUri = pm.getObjectUri(aqpv);
			logger.trace("getIndividuals: USING: apv-uri={}", aqpvUri);
		}
		List<QualitativePropertyValue> individuals = null;
		QualitativePropertyValue topIndividual = null;
		String qry = String.format("SELECT ?s WHERE { ?s a <%s> . }", aqpvUri);
		logger.trace("getIndividuals: individuals query={}", qry);
		List<Object> items = pm.findByQuery(QualitativePropertyValue.class, qry);
		logger.trace("getIndividuals: individuals={}", items);
		
		// find highest order individuals
		int maxRank = 0;
		TreeMap<Integer,QualitativePropertyValue> tmp = new TreeMap<Integer,QualitativePropertyValue>();
		for (Object o : items) {
			QualitativePropertyValue qpv = (QualitativePropertyValue)o;
			
			// search for highest ranked individual (if ordered)
			QualitativePropertyValue q = qpv;
			int rank = 0;
			while ((q=q.getGreater())!=null) rank++;
			if (rank>maxRank) { maxRank = rank; topIndividual = qpv; }
			
			tmp.put(rank, qpv);
		}
		if (maxRank>0) {
			individuals = new Vector(tmp.values());
		}
		
		// if greater relation is not used, try lesser relation
		if (maxRank==0) {
			tmp.clear();
			// find lowest order individuals
			for (Object o : items) {
				QualitativePropertyValue qpv = (QualitativePropertyValue)o;
				
				// search for lowest ranked individual (if ordered)
				QualitativePropertyValue q = qpv;
				int rank = 0;
				while ((q=q.getLesser())!=null) rank++;
				if (rank>maxRank) { maxRank = rank; topIndividual = qpv; }
				
				tmp.put(rank, qpv);
			}
			if (maxRank>0) {
				individuals = new Vector(tmp.values());
				java.util.Collections.reverse(individuals);
			}
		}
		
		// if neither greater nor lesser relations are used (i.e. unordered set)...
		if (maxRank==0) {
			individuals = new Vector(items);
		}
		
		return individuals;
	}
	
	protected static ServiceCategoryAttribute _prepareServiceCategoryAttribute(String id, BrokerPolicyProperty bpp, AllowedPropertyValue apv, PreferenceVariable pv, List<QualitativePropertyValue> individuals, QualitativePropertyValue topIndividual) {
		// Prepare SCA object to return
		ServiceCategoryAttribute sca = new ServiceCategoryAttribute();
		//sca.setId( java.net.URLEncoder.encode( id ) );
		sca.setId( id );
		
		if (pv.getBelongsTo()!=null) sca.setServiceCategory( pv.getBelongsTo().getId() );
		if (pv.getRefToServiceAttribute()!=null) sca.setAttribute( pv.getRefToServiceAttribute().getId() );
		if (apv!=null) sca.setMandatory( apv.isMandatory() );
		
		// Deduce SCA type
		if (bpp instanceof BrokerPolicyQuantitativeProperty) {
			AllowedQuantitativePropertyValue aqpv = (AllowedQuantitativePropertyValue)apv;
			sca.setUnit( aqpv.getUnitOfMeasurement() );
			sca.setMin( aqpv.getMinValue() );
			sca.setMax( aqpv.getMaxValue() );
			
			if (aqpv.isRange()) sca.setType("NUMERIC_RANGE");
			else if (aqpv.isHigherIsBetter()) sca.setType("NUMERIC_INC");
			else sca.setType("NUMERIC_DEC");
			sca.setHigherIsBetter( aqpv.isHigherIsBetter() );
		} else
		if (bpp instanceof BrokerPolicyFuzzyProperty) {
			AllowedFuzzyPropertyValue afpv = (AllowedFuzzyPropertyValue)apv;
			sca.setUnit( afpv.getUnitOfMeasurement() );
			sca.setFmin( new eu.brokeratcloud.opt.type.TFN(afpv.getMinSupport(), afpv.getMinKernel(), afpv.getMinKernel()) );
			sca.setFmax( new eu.brokeratcloud.opt.type.TFN(afpv.getMaxKernel(), afpv.getMaxKernel(), afpv.getMaxSupport()) );
			
			if (afpv.isRange()) sca.setType("FUZZY_RANGE");
			else if (afpv.isHigherIsBetter()) sca.setType("FUZZY_INC");
			else sca.setType("FUZZY_DEC");
			sca.setHigherIsBetter( afpv.isHigherIsBetter() );
		} else
		if (bpp instanceof BrokerPolicyQualitativeProperty) {
			AllowedQualitativePropertyValue aqpv = (AllowedQualitativePropertyValue)apv;
			// Boolean
			if (pv instanceof BooleanPreferenceVariable) {
				if (topIndividual!=null && topIndividual.getLesser()!=null) java.util.Collections.reverse(individuals);
				String[] terms = new String[individuals.size()];
				int i=0;
				for (QualitativePropertyValue qpv : individuals) terms[i++] = qpv.getLabel();
				sca.setTerms(terms);
				sca.setType("BOOLEAN");
			} else
			// Unordered Set
			if (aqpv.getHasOrder()==false) {
				String[] members = new String[individuals.size()];
				int i=0;
				for (QualitativePropertyValue qpv : individuals) members[i++] = qpv.getLabel();
				sca.setMembers(members);
				sca.setType("UNORDERED_SET");
			} else 
			// Linguistic
			{
				if (topIndividual!=null && topIndividual.getLesser()!=null) java.util.Collections.reverse(individuals);
				String[] terms = new String[individuals.size()];
				int i=0;
				for (QualitativePropertyValue qpv : individuals) terms[i++] = qpv.getLabel();
				sca.setTerms(terms);
				sca.setType("LINGUISTIC");
			}
		} else {
			throw new IllegalArgumentException("getServiceCategoryAttribute: Could not resolve SCA type: bpp="+bpp);
		}
		
		if (pv.getRefToServiceAttribute()!=null) {
			sca.setName(pv.getRefToServiceAttribute().getName());
		}
		
		// Copy BP property or allowed value descriptions
		String labelEn = bpp.getLabelEn();
		String labelDe = bpp.getLabelDe();
		String comment = bpp.getComment();
		if ((labelEn==null || labelEn.trim().isEmpty()) &&
		    (labelDe==null || labelDe.trim().isEmpty()) &&
			(comment==null || comment.trim().isEmpty()))
		{
			labelEn = apv.getLabelEn();
			labelDe = apv.getLabelDe();
			comment = apv.getComment();
		}
		if (labelEn==null || labelEn.trim().isEmpty()) { if (id.indexOf('#')>-1) labelEn=id.substring(id.indexOf('#')).trim(); if (labelEn.isEmpty()) labelEn=id; }
		if (labelDe==null || labelDe.trim().isEmpty()) labelDe="";
		if (comment==null || comment.trim().isEmpty()) comment="";
		sca.setLabelEn( labelEn );
		sca.setLabelDe( labelDe );
		sca.setComment( comment );
		String bppName = bpp.getId();
		if (bppName==null || (bppName=bppName.trim()).isEmpty()) {
			bppName = "<BP property Id not found>";
		} else {
			// keep only the last part after '/' or '#'
			int p1 = bppName.lastIndexOf("#");
			int p2 = bppName.lastIndexOf("/");
			p1 = (p1>p2) ? p1 : p2;
			String tmp;
			if (p1>-1 && p1+1<bppName.length()) tmp = bppName.substring(p1+1).trim();
			else tmp = bppName;
			if ( ! tmp.isEmpty() ) bppName = tmp;
			// remove 'has' from the begin
			if (bppName.substring(0,3).equalsIgnoreCase("has")) tmp = bppName.substring(3);
			if ( ! tmp.isEmpty() ) bppName = tmp;
		}
		sca.setBppName( bppName.trim() );
		
		// CAS-specific stuff
		sca.setMeasuredBy( apv.getMeasuredBy() );
		
		logger.trace("getServiceCategoryAttribute: END: RESULT={}", sca);
		return sca;
	}
	
	protected static boolean _checkExistenceOfNewId(ServiceCategoryAttribute sca) throws Exception {
		logger.trace("_checkExistenceOfNewId: BEGIN: {}", sca);
		eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
		
		// Retrieve referenced objects
		OptimisationAttribute oa = null;
		ClassificationDimension cd = null;
		
		if (sca.getAttribute()!=null && !sca.getAttribute().trim().isEmpty()) {
			logger.trace("_checkExistenceOfNewId: Retrieving service attribute: {}", sca.getAttribute());
			oa = (OptimisationAttribute)pm.find( sca.getAttribute(), OptimisationAttribute.class );
																// or  find( sca.getAttribute() );	IF 'URIs' ARE USED
			logger.trace("_checkExistenceOfNewId: Service Attribute:\n{}", oa);
		} else {
			throw new IllegalArgumentException("_checkExistenceOfNewId: No Service Attribute specified in SCA instance: Throwing exception");
		}
		if (sca.getServiceCategory()!=null && !sca.getServiceCategory().trim().isEmpty()) {
			logger.trace("_checkExistenceOfNewId: Retrieving classification category: {}", sca.getServiceCategory());
			cd = (ClassificationDimension) pm.find( sca.getServiceCategory(), ClassificationDimension.class );
																// or  find( sca.getServiceCategory() );  IF 'URIs' ARE USED
			logger.trace("_checkExistenceOfNewId: Classification Category:\n{}", cd);
		} else {
			logger.trace("_checkExistenceOfNewId: No Classification Category in SCA instance: Assuming Classification Root");
		}
		
		// Generate service attribute internal name and check if exists!
		String internalAttributeName = generateInternalName(oa, cd!=null && cd.getParent()!=null ? cd.getId() :  null);
		
		// Checking internal attribute name existence
		logger.trace("_checkExistenceOfNewId: Checking if attribute internal name already exists...");
		boolean exists = pm.exist("has"+internalAttributeName, BrokerPolicyProperty.class);
		if (exists) logger.debug("_checkExistenceOfNewId: attribute internal name already exists: NOT UNIQUE");
		else logger.debug("_checkExistenceOfNewId: attribute internal name does not exist: UNIQUE");
		
		logger.trace("_checkExistenceOfNewId: END:  new-id={}, result={}", internalAttributeName, exists);
		return exists;
	}
	
	protected static String _createServiceCategoryAttribute(ServiceCategoryAttribute sca) throws Exception {
		logger.trace("createServiceCategoryAttribute: BEGIN: {}", sca);
		eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
		
		// Retrieve referenced objects
		OptimisationAttribute oa = null;
		ClassificationDimension cd = null;
		
		if (sca.getAttribute()!=null && !sca.getAttribute().trim().isEmpty()) {
			logger.trace("createServiceCategoryAttribute: Retrieving service attribute: {}", sca.getAttribute());
			oa = (OptimisationAttribute)pm.find( sca.getAttribute(), OptimisationAttribute.class );
																// or  find( sca.getAttribute() );	IF 'URIs' ARE USED
			logger.trace("createServiceCategoryAttribute: Service Attribute:\n{}", oa);
		} else {
			throw new IllegalArgumentException("createServiceCategoryAttribute: No Service Attribute specified in SCA instance: Throwing exception");
		}
		if (sca.getServiceCategory()!=null && !sca.getServiceCategory().trim().isEmpty()) {
			logger.trace("createServiceCategoryAttribute: Retrieving classification category: {}", sca.getServiceCategory());
			cd = (ClassificationDimension) pm.find( sca.getServiceCategory(), ClassificationDimension.class );
																// or  find( sca.getServiceCategory() );  IF 'URIs' ARE USED
			logger.trace("createServiceCategoryAttribute: Classification Category:\n{}", cd);
		} else {
			logger.trace("createServiceCategoryAttribute: No Classification Category in SCA instance: Assuming Classification Root");
		}
		
		// Generate service attribute internal name and check if exists!
		String internalAttributeName = generateInternalName(oa, cd!=null && cd.getParent()!=null ? cd.getId() :  null);
		
		// Checking internal attribute name existence
		logger.trace("createServiceCategoryAttribute: Checking if attribute internal name already exists...");
		boolean exists = pm.exist("has"+internalAttributeName, BrokerPolicyProperty.class);
		if (exists) throw new Exception("Broker Policy Property with this internal name already exists: "+internalAttributeName);
		logger.trace("createServiceCategoryAttribute: attribute internal name is unique: keep going...");
		
		// update SCA id with the generated id used to persist it in RDF repository
		logger.trace("createServiceCategoryAttribute: Setting SCA id from '{}' to '{}'...", sca.getId(), internalAttributeName);
		sca.setId(internalAttributeName);
		
		// Get Broker Policy class (service model sub-class)
		if (client==null) client = SparqlServiceClientFactory.getClientInstance();
 		logger.trace("createServiceCategoryAttribute: Querying for Service Model...");
		Object tmpSm = client.queryValue("SELECT DISTINCT ?s WHERE { ?s <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.linked-usdl.org/ns/usdl-core#ServiceModel> }");
		String serviceModel = tmpSm!=null ? tmpSm.toString() : null;
 		logger.trace("createServiceCategoryAttribute: Service Model={}", serviceModel);
		if (serviceModel==null) {
			throw new Exception("createServiceCategoryAttribute: Broker Policy NOT FOUND");
		}
		
		// Prepare Broker Policy objects
		AllowedPropertyValue apv = null;
		String datatype = null;
		PreferenceVariable pv = null;
		BrokerPolicyProperty bpp = null;
		DefaultPreferenceVariableValue dpvv = null;
		DefaultDatatypePreferenceVariableValue dtpvv = null;
		String prefVarClass = null;
		String propertySuperClass = null;
		List<QualitativePropertyValue> individuals = null;
		
		// set type-specific fields
		if (sca.isNumericType(sca.getType())) {
			// prepare allowed property values
			AllowedQuantitativePropertyValue aqpv = new AllowedQuantitativePropertyValue();
			aqpv.setId("Allowed"+internalAttributeName+"Value");
			aqpv.setMandatory( sca.getMandatory() );
			aqpv.setHigherIsBetter( sca.isNumericInc() );
			aqpv.setRange( sca.isNumericRange() );
			aqpv.setUnitOfMeasurement( sca.getUnit()!=null ? sca.getUnit() : "" );
			aqpv.setMinValue( sca.getMin() );
			aqpv.setMaxValue( sca.getMax() );
			apv = aqpv;
			
			bpp = new BrokerPolicyQuantitativeProperty();
			dpvv = new DefaultPreferenceVariableValue();
			dpvv.setQuantitative();
			
			// initialize preference variable and broker policy property
			QuantitativePreferenceVariable qpv = new QuantitativePreferenceVariable();
			pv = qpv;
		} else
		if (sca.isFuzzyType(sca.getType())) {
			// prepare allowed property values
			AllowedFuzzyPropertyValue afpv = new AllowedFuzzyPropertyValue();
			afpv.setId("Allowed"+internalAttributeName+"Value");
			afpv.setMandatory( sca.getMandatory() );
			afpv.setHigherIsBetter( sca.isFuzzyInc() );
			afpv.setRange( sca.isFuzzyRange() );
			afpv.setUnitOfMeasurement( sca.getUnit()!=null ? sca.getUnit() : "" );
			afpv.setMinSupport( sca.getFmin().getLowerBound() );
			afpv.setMinKernel( sca.getFmin().getMeanValue() );
			afpv.setMaxKernel( sca.getFmax().getMeanValue() );
			afpv.setMaxSupport( sca.getFmax().getUpperBound() );
			apv = afpv;
			
			bpp = new BrokerPolicyFuzzyProperty();
			dpvv = new DefaultPreferenceVariableValue();
			dpvv.setFuzzy();
			
			// initialize preference variable and broker policy property
			FuzzyPreferenceVariable fpv = new FuzzyPreferenceVariable();
			pv = fpv;
		} else
		if (sca.isSetType(sca.getType()) || sca.isBooleanType(sca.getType())) {
			// prepare allowed property values
			AllowedQualitativePropertyValue aqpv = new AllowedQualitativePropertyValue();
			aqpv.setId("Allowed"+internalAttributeName+"Value");
			aqpv.setMandatory( sca.getMandatory() );
			apv = aqpv;
			
			// also prepare allowed property values' individuals !!!
			String[] terms = null;
			String[] tmp = null;
			
			if (sca.isLinguistic()) {
				terms = sca.getTerms();
				aqpv.setHasOrder(true);
				
				if (terms==null || terms.length==0) { terms = new String[3]; terms[0]="LOW"; terms[1]="MEDIUM"; terms[2]="HIGH"; }
				if (terms[0]==null || terms[0].trim().isEmpty()) terms[0] = "LT 1";
			} else
			if (sca.isUnorderedSet()) {
				terms = sca.getMembers();
				aqpv.setHasOrder(false);
				
				if (terms==null || terms.length==0) { terms = new String[1]; terms[0] = "Option 1"; }
				if (terms[0]==null || terms[0].trim().isEmpty()) terms[0] = "Option 1";
			} else
			if (sca.isBoolean()) {
				terms = sca.getTerms();
				aqpv.setHasOrder(true);
				
				if (terms==null || terms.length==0) { terms = new String[2]; terms[0]="False"; terms[1]="True"; }
				else if (terms.length==1) { tmp=new String[2]; tmp[0]=terms[0]; tmp[1]="True"; terms = tmp; }
				if (terms[0]==null || terms[0].trim().isEmpty()) terms[0] = "False";
				if (terms[1]==null || terms[1].trim().isEmpty()) terms[1] = "True";
			} else {
				throw new Exception("createServiceCategoryAttribute: IMPLEMENTATION ERROR: CODE MUST NEVEL REACH THIS POINT: SCA type is neither LINGUISTIC nor UNORDERED_SET despite the preciding 'sca.isSetType()' check!!! : sca.type="+sca.getType());
			}
			
			String aqpvUri = pm.getObjectUri(aqpv);
			if (terms!=null) {
				individuals = new Vector<QualitativePropertyValue>();
				QualitativePropertyValue previous = null;
				boolean ordered = sca.isLinguistic() || sca.isBoolean();
				for (int i=0; i<terms.length; i++) {
					String id = generateIndividualName(terms[i], internalAttributeName);
					QualitativePropertyValue idvl = new QualitativePropertyValue();
					idvl.setId(id);
					idvl.setRdfType(aqpvUri);
					idvl.setLabel(terms[i]);
					if (ordered && previous!=null) { idvl.setGreater(previous); previous.setLesser(idvl); }
					previous = idvl;
					individuals.add(idvl);
				}
				if (individuals.size()==0) individuals = null;
			} else {
				logger.debug("createServiceCategoryAttribute: No individuals found for qualitative service attribute: "+internalAttributeName);
			}
			
			// if not individuals are specified then give some defaults
			if (individuals==null) {
				individuals = new Vector<QualitativePropertyValue>();
				
				if (sca.isLinguistic()) {
					QualitativePropertyValue previous = null;
					String[] termLabels = { "LOW", "MEDIUM", "HIGH" };
					for (String term : termLabels) {
						String id = generateIndividualName(term, internalAttributeName);
						QualitativePropertyValue idvl = new QualitativePropertyValue();
						idvl.setId(id);
						idvl.setRdfType(aqpvUri);
						idvl.setLabel(term);
						if (previous!=null) {
							idvl.setGreater(previous);
							previous.setLesser(idvl);
						}
						previous = idvl;
						individuals.add(idvl);
					}
				} else
				if (sca.isUnorderedSet()) {
					String[] termLabels = { "Option 1" };
					for (String term : termLabels) {
						String id = generateIndividualName(term, internalAttributeName);
						QualitativePropertyValue idvl = new QualitativePropertyValue();
						idvl.setId(id);
						idvl.setRdfType(aqpvUri);
						idvl.setLabel(term);
						individuals.add(idvl);
					}
				}
			}
			
			if (individuals!=null) aqpv.setAllowedValues( individuals.toArray(new QualitativePropertyValue[ individuals.size() ]) );
			
			bpp = new BrokerPolicyQualitativeProperty();
			dpvv = new DefaultPreferenceVariableValue();
			dpvv.setQualitative();
			
			// initialize preference variable and broker policy property
			if (!sca.isBoolean()) {
				QualitativePreferenceVariable qpv = new QualitativePreferenceVariable();
				pv = qpv;
			} else {
				BooleanPreferenceVariable bpv = new BooleanPreferenceVariable();
				pv = bpv;
			}
		} else {
			throw new IllegalArgumentException("createServiceCategoryAttribute: Unknown SCA type: "+sca.getType());
		}
		
		// prepare broker policy property
		bpp.setId("has"+internalAttributeName);
		bpp.setSubPropertyOf( propertySuperClass );
		bpp.setDomain( serviceModel );
		bpp.setRange(apv);
		
		// Copy RootObject properties
		bpp.setLabelEn( sca.getLabelEn() );
		bpp.setLabelDe( sca.getLabelDe() );
		bpp.setComment( sca.getComment() );
		
		// CAS-specific stuff
		apv.setMeasuredBy( sca.getMeasuredBy() );
		
		// prepapre Preference Variable object
		pv.setId( internalAttributeName+"PreferenceVariable" );
		pv.setRefToServiceAttribute( oa );
		pv.setBelongsTo( cd );
		
		// prepapre hasDefaultPrefVarValue property object
		if (dpvv!=null) {
			dpvv.setId("hasDefault"+internalAttributeName);
			dpvv.setDomain(pv);
			dpvv.setRange(apv);
		} else
		if (dtpvv!=null) {
			dtpvv.setId("hasDefault"+internalAttributeName);
			dtpvv.setDomain(pv);
		}
		
		logger.trace("createServiceCategoryAttribute: BrokerPolicyProperty:\n{}", bpp);
		logger.trace("createServiceCategoryAttribute: AllowedPropertyValues:\n{}", apv);
		logger.trace("createServiceCategoryAttribute: Individuals:  {}", individuals);
		logger.trace("createServiceCategoryAttribute: PreferenceVariable:\n{}", pv);
		logger.trace("createServiceCategoryAttribute: DefaultPreferenceVariableValue:\n{}", dpvv);
		logger.trace("createServiceCategoryAttribute: DefaultDatatypePreferenceVariableValue:\n{}", dtpvv);
		
		// Persist objects
		logger.trace("createServiceCategoryAttribute: Persisting BrokerPolicyProperty and AllowedPropertyValues...");
		pm.persist(bpp);	// bpp references apv, therefore there is no need to persist it explicitly
		pm.persist(pv);
		if (dpvv!=null) pm.persist(dpvv);
		else if (dtpvv!=null) pm.persist(dtpvv);
		if (individuals!=null) for (Object idvl : individuals) if (!pm.exist(idvl)) pm.persist(idvl);	// persist apv individuals
		
		logger.trace("createServiceCategoryAttribute: END");
		return internalAttributeName;
	}
	
	protected static String generateInternalName(OptimisationAttribute attr, String cdId) {
		String attrId = attr!=null ? attr.getId() : null;
		logger.trace("generateInternalName: generating attribute internal name from attribute and classification id's: attr={}, classification-dimension-id={}", attrId, cdId);
		
		boolean removeParentIdPart = Util.checkBoolean( optConfig.getProperty("broker-policy-var-naming-remove-parent-attribute-id-part"), false );
		logger.trace("generateInternalName: remove-parent-attribute-id-part={}", removeParentIdPart);
		if (removeParentIdPart) {
			String parentId = attr.getParent()!=null ? attr.getParent().getId() : null;
			if (parentId!=null) attrId = attrId.replace(parentId, "");
			logger.trace("generateInternalName: attribute id after parent id part removal: attr-id={}", attrId);
		}
		
		String[] attrIdPart = attrId.split("[^A-Za-z0-9_]+");
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<attrIdPart.length; i++) {
			if (attrIdPart[i].trim().isEmpty()) continue;
			if (i==0 && attrIdPart[0].equalsIgnoreCase("attr")) continue;
			sb.append( attrIdPart[i].substring(0,1).toUpperCase() );
			if (attrIdPart[i].length()>1) sb.append( attrIdPart[i].substring(1) );
		}
		String internalAttributeName = sb.toString();
		
		boolean useClassificationDimension = Util.checkBoolean( optConfig.getProperty("broker-policy-var-naming-must-use-classification-dimension"), true );
		logger.trace("generateInternalName: use-classification-dimension={}", useClassificationDimension);
		if (useClassificationDimension) {
			if (cdId!=null) {
				String[] cdIdPart = cdId.split("[^A-Za-z0-9_]+");
				StringBuilder sb2 = new StringBuilder();
				for (int i=0; i<cdIdPart.length; i++) {
					if (cdIdPart[i].trim().isEmpty()) continue;
					if (i==0 && cdIdPart[0].equalsIgnoreCase("sc")) continue;
					sb2.append( cdIdPart[i].substring(0,1).toUpperCase() );
					if (cdIdPart[i].length()>1) sb2.append( cdIdPart[i].substring(1) );
				}
				internalAttributeName += "On"+sb2.toString();
			}
		}
		logger.trace("generateInternalName: attribute internal name: {}", internalAttributeName);
		return internalAttributeName;
	}
	
	public static String generateIndividualName(String term, String internalAttributeName) {
		String[] part = term.split("[^A-Za-z0-9]");
		StringBuilder sb = new StringBuilder(internalAttributeName).append("_");
		for (int j=0; j<part.length; j++) {
			String tmp = part[j].trim();
			if (tmp.isEmpty()) continue;
			sb.append(tmp.substring(0,1).toUpperCase());
			if (tmp.length()>1) sb.append(tmp.substring(1));
		}
		return sb.toString();
	}
	
	// Implements DELETE_CREATE update strategy
	// NOTE: This method changes SCA id and consequently the id's of all objects and RDF artifacts
	// See also _updateSCAFields()
	protected static String _updateServiceCategoryAttribute(ServiceCategoryAttribute sca) throws Exception {
		logger.trace("updateServiceCategoryAttribute: BEGIN: sca={}", sca);
		
		logger.trace("updateServiceCategoryAttribute: Calling '_checkExistenceOfNewId' to check if it's ok to use the new internal attribute name that will be generated");
		boolean newIdExists = _checkExistenceOfNewId(sca);
		
		//if ( newIdExists ) {
			// The internal attribute name that will be generated is UNIQUE. It's ok to continue
			String scaId = java.net.URLDecoder.decode( sca.getId(), java.nio.charset.StandardCharsets.UTF_8.toString() );
			logger.trace("updateServiceCategoryAttribute: Current SCA id={}", scaId);
			
			logger.trace("updateServiceCategoryAttribute: Calling 'deleteServiceCategoryAttribute' to clear persisted objects");
			_deleteServiceCategoryAttribute(scaId);
			logger.trace("updateServiceCategoryAttribute: Calling 'createServiceCategoryAttribute' to create and persist new objects");
			String newId = _createServiceCategoryAttribute(sca);
			
			logger.trace("updateServiceCategoryAttribute: END: new-id={}", newId);
			return newId;
		/*} else {
			// The internal attribute name that will be generated is NOT UNIQUE. It's NOT ok to continue
			// Most likely user has selected the same attribute twice
			Exception exc = new Exception("_updateServiceCategoryAttribute: The internal attribute name that would be used DOES NOT exist");
			logger.error("updateServiceCategoryAttribute: EXCEPTION THROWN");
			throw exc;
		}*/
	}
	
	protected static void _deleteServiceCategoryAttribute(String id) throws Exception {
		logger.trace("deleteServiceCategoryAttribute: BEGIN: id={}", id);
		eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
		
		PolicyObjects po = _retrieveBrokerPolicyObjects(pm, id, true);
		if (po==null) {
			throw new Exception("deleteServiceCategoryAttribute: Could not find some or all broker policy objects: pv-uri="+id+"\nPolicy-Objects: "+po);
		}
		
		logger.trace("deleteServiceCategoryAttribute: BrokerPolicyProperty:\n{}", po.bpp);
		logger.trace("deleteServiceCategoryAttribute: AllowedPropertyValues:\n{}", po.apv);
		logger.trace("deleteServiceCategoryAttribute: datatype:\n{}", po.datatype);
		logger.trace("deleteServiceCategoryAttribute: Individuals:  {}", po.individuals);
		//logger.trace("deleteServiceCategoryAttribute: Top Individual:  {}", po.topIndividual);
		logger.trace("deleteServiceCategoryAttribute: PreferenceVariable:\n{}", po.pv);
		logger.trace("deleteServiceCategoryAttribute: DefaultPreferenceVariableValue:\n{}", po.dpvv);
		
		// Get Broker Policy instance (is_A serviceModel)
		if (client==null) client = SparqlServiceClientFactory.getClientInstance();
		
		// Delete objects
		if (po.individuals!=null) {
			for (QualitativePropertyValue qpv : po.individuals) {
				try { pm.remove(qpv); } catch (eu.brokeratcloud.persistence.RdfPersistenceException e) { logger.error("deleteServiceCategoryAttribute: Allowed-Property-Value Individual deletion failed: id={}: {}", qpv.getId(), e); }
			}
		}
		if (po.dpvv!=null) try { pm.remove(po.dpvv); } catch (eu.brokeratcloud.persistence.RdfPersistenceException e) { logger.error("deleteServiceCategoryAttribute: Default-Preference-Variable-Value deletion failed: id={}, {}", po.dpvv.getId(), e); }
		try { pm.remove(po.pv); } catch (eu.brokeratcloud.persistence.RdfPersistenceException e) { logger.error("deleteServiceCategoryAttribute: Preference-Variable deletion failed: id={}, {}", po.pv.getId(), e); }
		if (po.apv!=null) 	try { pm.remove(po.apv); } catch (eu.brokeratcloud.persistence.RdfPersistenceException e) { logger.error("deleteServiceCategoryAttribute: Allowed-Property-Value deletion failed: id={}, {}", po.apv.getId(), e); }
		try { pm.remove(po.bpp); } catch (eu.brokeratcloud.persistence.RdfPersistenceException e) { logger.error("deleteServiceCategoryAttribute: Broker-Policy-Property deletion failed: id={}, {}", po.bpp.getId(), e); }
		
		logger.trace("deleteServiceCategoryAttribute: END");
	}
	
	// Implements UPDATE_FIELDS Update Strategy
	// See also _updateServiceCategoryAttribute()
	protected static String _updateSCAFields(String fieldsToUpdate, ServiceCategoryAttribute sca) throws Exception {
		logger.trace("_updateSCAFields: BEGIN: fields-to-update={}, sca={}", fieldsToUpdate, sca);
		
		fieldsToUpdate = ","+fieldsToUpdate.replace("[ \t\r\n]", "").replace("[:;\\-]",",").toUpperCase()+",";
		logger.trace("_updateSCAFields: Using fields filter: {}", fieldsToUpdate);
		
		eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
		
		// Retrieve referenced objects
		OptimisationAttribute oa = null;
		ClassificationDimension cd = null;
		
		if (sca.getAttribute()!=null && !sca.getAttribute().trim().isEmpty()) {
			logger.trace("_updateSCAFields: Retrieving service attribute: {}", sca.getAttribute());
			oa = (OptimisationAttribute)pm.find( sca.getAttribute(), OptimisationAttribute.class );
																// or  find( sca.getAttribute() );	IF 'URIs' ARE USED
			logger.trace("_updateSCAFields: Service Attribute:\n{}", oa);
		} else {
			throw new IllegalArgumentException("_updateSCAFields: No Service Attribute specified in SCA instance: Throwing exception");
		}
		if (sca.getServiceCategory()!=null && !sca.getServiceCategory().trim().isEmpty()) {
			logger.trace("_updateSCAFields: Retrieving classification category: {}", sca.getServiceCategory());
			cd = (ClassificationDimension) pm.find( sca.getServiceCategory(), ClassificationDimension.class );
																// or  find( sca.getServiceCategory() );  IF 'URIs' ARE USED
			logger.trace("_updateSCAFields: Classification Category:\n{}", cd);
		} else {
			logger.trace("_updateSCAFields: No Classification Category in SCA instance: Assuming Classification Root");
		}
		
		// Retrieve Broker Policy objects
		String id = java.net.URLDecoder.decode( sca.getId(), java.nio.charset.StandardCharsets.UTF_8.toString() );
		PolicyObjects po = _retrieveBrokerPolicyObjects(pm, id, true);
		if (po==null) {
			throw new Exception("_updateSCAFields: Could not find some or all broker policy objects: pv-uri="+id+"\nPolicy-Objects: "+po);
		}
		logger.trace("_updateSCAFields: BrokerPolicyProperty:\n{}", po.bpp);
		logger.trace("_updateSCAFields: AllowedPropertyValues:\n{}", po.apv);
		logger.trace("_updateSCAFields: datatype:\n{}", po.datatype);
		logger.trace("_updateSCAFields: Individuals:  {}", po.individuals);
		//logger.trace("_updateSCAFields: Top Individual:  {}", po.topIndividual);
		logger.trace("_updateSCAFields: PreferenceVariable:\n{}", po.pv);
		logger.trace("_updateSCAFields: DefaultPreferenceVariableValue:\n{}", po.dpvv);
		
		// Update indicated fields
		boolean updatedBpp = false;
		boolean updatedApv = false;
		boolean updatedDatatype = false;
		boolean updatedIndividuals = false;
		boolean updatedPv = false;
		boolean updatedDpvv = false;
		
		if (fieldsToUpdate.indexOf(",ATTRIBUTE,")>-1) {
			logger.trace("_updateSCAFields: Updating Optimisation Attribute in PreferenceVariable...");
			po.pv.setRefToServiceAttribute(oa);
			updatedPv = true;
		}
		
		// ...updated objects
		if (updatedBpp) logger.trace("_updateSCAFields: AFTER UPDATE: BrokerPolicyProperty:\n{}", po.bpp);
		if (updatedApv) logger.trace("_updateSCAFields: AFTER UPDATE: AllowedPropertyValues:\n{}", po.apv);
		if (updatedDatatype) logger.trace("_updateSCAFields: AFTER UPDATE: Datatype:\n{}", po.datatype);
		if (updatedIndividuals) logger.trace("_updateSCAFields: AFTER UPDATE: Individuals:\n{}", po.individuals);
		if (updatedPv) logger.trace("_updateSCAFields: AFTER UPDATE: PreferenceVariable:\n{}", po.pv);
		if (updatedDpvv) logger.trace("_updateSCAFields: AFTER UPDATE: DefaultPreferenceVariableValue:\n{}", po.dpvv);
		if ( ! (updatedBpp || updatedApv || updatedDatatype || updatedIndividuals || updatedPv || updatedDpvv) ) {
			logger.warn("_updateSCAFields: AFTER UPDATE: No objects updated. Possible misconfiguration");
		}
		
		// Merge updated objects
		/*
		 * Currently only PV is updated in persistent store. This is adequate for the current implementation
		 * of PuLSaR and especially user-facing component. If in the future more objects will need to be updated
		 * in persistent stored (i.e. BPP, APV, datatype, individuals etc) then the followinf section should
		 * be uncommented and thoroughly tested!! In such case we must take care of the referenced objects that
		 * might be replaced (e.g. APV by another APV/datatype, individuals by other individuals/nothing,
		 * datatype by a new APV, nothing by individuals)
		 * NOTE-1: A BPP might hold a reference to an APV, which might hold refs to individuals!!
		 * NOTE-2: Currently, only PV are safe to update!!
		 */
/*		if (updatedBpp) {
			logger.trace("_updateSCAFields: Merging BrokerPolicyProperty and AllowedPropertyValues...");
			pm.merge(po.bpp);	// bpp references apv, therefore there is no need to merge it explicitly
		}*/
/*		if (updatedApv || updatedIndividuals) {
			logger.trace("_updateSCAFields: Merging AllowedPropertyValues and their individuals (if any)...");
			pm.merge(po.apv);
		}*/
/*		if (updatedIndividuals) {
			//logger.trace("_updateSCAFields: Merging AllowedPropertyValues individuals...");
			//if (po.individuals!=null) for (Object idvl : po.individuals) if (!pm.exist(idvl)) pm.merge(idvl);	// merge apv individuals
		}*/
/*		if (updatedDatatype) {
			logger.trace("_updateSCAFields: Merging Datatype...");
			pm.merge(po.datatype);
		}*/
		if (updatedPv) {
			logger.trace("_updateSCAFields: Merging PreferenceVariable...");
			pm.merge(po.pv);
		}
/*		if (updatedDpvv) {
			logger.trace("_updateSCAFields: Merging DefaultPreferenceVariableValue...");
			pm.merge(po.dpvv);
		}*/
		
		logger.trace("_updateSCAFields: END");
		return sca.getId();		//return current SCA.id unmodified
	}
}
