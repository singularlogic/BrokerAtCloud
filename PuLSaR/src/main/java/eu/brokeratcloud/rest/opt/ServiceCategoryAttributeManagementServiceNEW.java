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

@Path("/opt/service-category/")
public class ServiceCategoryAttributeManagementServiceNEW extends AbstractManagementService {

	protected static SparqlServiceClient client = null;
	
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
	
	// POST /opt/service-category/attributes
	// Description: Update a service category attribute
	@POST
	@Path("/attributes")
	@Consumes("application/json")
	public Response updateServiceCategoryAttribute(ServiceCategoryAttribute attr) {
		try {
			String newId = _updateServiceCategoryAttribute(attr);
			logger.debug("updateServiceCategoryAttribute: New Service Category Attribute id = {}", newId);
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
	
	protected static ServiceCategoryAttribute[] _getServiceCategoryAttributes(String catId) throws Exception {
		logger.trace("getServiceCategoryAttributes: BEGIN: classification dimension id={}", catId);
		eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
		
		String btUri = pm.getFieldUri(PreferenceVariable.class, "belongsTo");
		String rtUri = pm.getFieldUri(PreferenceVariable.class, "refToServiceAttribute");
		String cdUri = pm.getObjectUri(catId, ClassificationDimension.class);
		String queryStr = "SELECT ?s WHERE { ?s  <http://www.w3.org/2000/01/rdf-schema#subClassOf>*  <http://www.linked-usdl.org/ns/usdl-pref#PreferenceVariable> . "+
						  " ?s  <"+btUri+">  <"+cdUri+"> .  ?s  <"+rtUri+">  ?oa . } ORDER BY ?s ";
		logger.trace("getServiceCategoryAttributes: Preference Variable Id retrieval query: {}", queryStr);
		
		if (client==null) client = SparqlServiceClientFactory.getClientInstance();
 		logger.trace("getServiceCategoryAttributes: Querying preference variable ID's for classification dimension: {}", catId);
		List<String> results = client.queryForIds(queryStr, "?s");
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
	
	protected static ServiceCategoryAttribute _getServiceCategoryAttribute(String id) throws Exception {
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
			logger.debug("getBrokerPolicyObjects: EXCEPTION THROWN: {}", e);
			return null;
		}
	}
	
	public static ServiceCategoryAttribute getServiceCategoryAttributeFromPreference(ConsumerPreference pref) {
		ServiceCategoryAttribute sca = null;
		String pvUri = pref.getPrefVariable();
		ServiceCategoryAttributeManagementServiceNEW.PolicyObjects po = ServiceCategoryAttributeManagementServiceNEW.getBrokerPolicyObjects(pvUri, false);
		logger.trace("getServiceCategoryAttributeFromPreference: policy objects={}", po);
		if (po==null) return null;
		PreferenceVariable pv = po.pv;
		
		if (pv==null) { logger.trace("getServiceCategoryAttributeFromPreference: END: pv is null"); return null; }
		eu.brokeratcloud.common.policy.BrokerPolicyProperty bpp = po.bpp;
		if (bpp==null) { logger.trace("getServiceCategoryAttributeFromPreference: END: bpp is null"); return null; }
		eu.brokeratcloud.common.policy.AllowedPropertyValue apv = po.apv;
		if (apv==null) { logger.trace("getServiceCategoryAttributeFromPreference: END: apv is null"); return null; }
		// get individuals
		List<eu.brokeratcloud.common.policy.QualitativePropertyValue> individuals = null;
		eu.brokeratcloud.common.policy.QualitativePropertyValue topIndividual = null;
		if (bpp instanceof eu.brokeratcloud.common.policy.BrokerPolicyQualitativeProperty) {
			//XXX: ASSERT: 'apv' allowed values have already been initialized
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
		
		logger.trace("_retrieveBrokerPolicyObjects: END: results = ** see above **");
		return new PolicyObjects(bppUri, bpp, apvUri, apv, pvUri, pv, dpvvUri, dpvv, individuals, topIndividual, datatype);
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
//		sca.setId( java.net.URLEncoder.encode( id ) );
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
		
		// Copy RootObject properties
		sca.setLabelEn( bpp.getLabelEn() );
		sca.setLabelDe( bpp.getLabelDe() );
		sca.setComment( bpp.getComment() );
		
		// CAS-specific stuff
		sca.setMeasuredBy( apv.getMeasuredBy() );
		
		logger.trace("getServiceCategoryAttribute: END: RESULT={}", sca);
		return sca;
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
		
		// prepapre hasDefaultXXXX property object
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
	
	protected static String _updateServiceCategoryAttribute(ServiceCategoryAttribute sca) throws Exception {
		logger.trace("updateServiceCategoryAttribute: BEGIN: sca={}", sca);
		
		String scaId = java.net.URLDecoder.decode( sca.getId() );
		logger.trace("updateServiceCategoryAttribute: Current SCA id={}", scaId);
		
		logger.trace("updateServiceCategoryAttribute: Calling 'deleteServiceCategoryAttribute' to clear persisted objects");
		_deleteServiceCategoryAttribute(scaId);
		logger.trace("updateServiceCategoryAttribute: Calling 'createServiceCategoryAttribute' to create and persist new objects");
		String newId = _createServiceCategoryAttribute(sca);
		
		logger.trace("updateServiceCategoryAttribute: END: new-id={}", newId);
		return newId;
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
}