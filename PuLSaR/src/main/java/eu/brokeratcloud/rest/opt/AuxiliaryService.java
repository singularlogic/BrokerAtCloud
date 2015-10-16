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
	
	// Stats Counters
	protected static int spl10 = -1;
	protected static int spl11 = -1;
	protected static int spl12 = -1;
	protected static int cnt13 = -1;
	protected static int cnt14 = -1;
	
	// Retrieves service descriptions belonging to given category/ies
	// NOTE: 'cat_id' can be a comma-separated list of classification dimension IDs (e.g. maps,energy,developer)
	@GET
	@Path("/offerings/category/{cat_id}/specifications/")
	@Produces("application/json")
	public ServiceDescription[] getServiceDescriptionsForCategories(@PathParam("cat_id") String catId) {
		try {
			logger.trace("getServiceDescriptionsForCategories: BEGIN: classifications={}", catId);
			
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			
			// Retrieve service descriptions based on classification dimensions given
			StringBuilder sb = new StringBuilder();
			sb.append(
				"SELECT DISTINCT ?bpi ?bpsmClass ?srv ?sm ?slp \n"+
				"WHERE { \n"+
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
				"	# Classification Filter \n"
			);
			if (!catId.equals("*")) {
				catId = catId.trim();
				if (catId.isEmpty()) {
					throw new Exception("getServiceDescriptionsForCategories: No classification dimensions specified");
				}
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
			
			ServiceDescription[] list = new ServiceDescription[results.size()];
			for (int i=0; i<list.length; i++) {
				Map<String,RDFNode> soln = results.get(i);
				
				String bpiUri = node2url( soln.get("bpi") );
				String bpsmc  = node2url( soln.get("bpsmClass") );
				String srvUri = node2url( soln.get("srv") );
				String smUri  = node2url( soln.get("sm") );
				String slpUri = node2url( soln.get("slp") );
				
				logger.trace("getServiceDescriptionsForCategories: calling _getServiceDescription for service: uri={}, sm={}, slp={}, bpi={}, bpsm-class={}", srvUri, smUri, slpUri, bpiUri, bpsmc);
				list[i] = _getServiceDescription(srvUri, smUri, slpUri, bpiUri, bpsmc, client);
			}
			
			logger.trace("getServiceDescriptionsForCategories: END: results={}", list);
			return list;
		} catch (Exception e) {
			logger.error("getServiceDescriptionsForCategories: EXCEPTION THROWN:\n", e);
			logger.error("getServiceDescriptionsForCategories: Returning an empty array of {}", ServiceDescription.class);
			return new ServiceDescription[0];
		}
	}
	
	// Retrieves service descriptions belonging to given service provider
	@GET
	@Path("/offerings/sp/{sp}/list")
	@Produces("application/json")
	public ServiceDescription[] getServiceProviderOfferings(@PathParam("sp") String sp) {
		try {
			logger.trace("getServiceProviderOfferings: BEGIN: sp={}", sp);
			
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			
			// Retrieve service descriptions of service provider 'sp'
			StringBuilder sb = new StringBuilder();
			sb.append(
				"SELECT DISTINCT ?bpi ?bpsmClass ?srv ?sm ?slp ?creator \n"+
				"WHERE { \n"+
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
				"	# Owner info\n"+
				"	?srv <http://purl.org/dc/terms/creator> ?creator .\n"+
				"	?creator <http://purl.org/goodrelations/v1#taxID> ?taxId . \n"+
				"	FILTER ( ?taxId = \""+sp+"\" ) . \n"+
				"} \n"+
				"ORDER BY ?srv ?sm ?slp \n"
			);
			String queryStr = sb.toString();
			logger.trace("getServiceProviderOfferings: Query: \n"+queryStr);
			
			SparqlServiceClient client = SparqlServiceClientFactory.getClientInstance();
			List<Map<String,RDFNode>> results = client.queryAndProcess(queryStr);
			logger.trace("getServiceProviderOfferings: Results: "+results);
			
			ServiceDescription[] list = new ServiceDescription[results.size()];
			for (int i=0; i<list.length; i++) {
				Map<String,RDFNode> soln = results.get(i);
				
				String bpiUri = node2url( soln.get("bpi") );
				String bpsmc  = node2url( soln.get("bpsmClass") );
				String srvUri = node2url( soln.get("srv") );
				String smUri  = node2url( soln.get("sm") );
				String slpUri = node2url( soln.get("slp") );
				
				logger.trace("getServiceProviderOfferings: calling _getServiceDescription for service: uri={}, sm={}, slp={}, bpi={}, bpsm-class={}", srvUri, smUri, slpUri, bpiUri, bpsmc);
				list[i] = _getServiceDescription(srvUri, smUri, slpUri, bpiUri, bpsmc, client);
			}
			
			logger.trace("getServiceProviderOfferings: END: results={}", list);
			return list;
		} catch (Exception e) {
			logger.error("getServiceProviderOfferings: EXCEPTION THROWN:\n", e);
			logger.error("getServiceProviderOfferings: Returning an empty array of {}", ServiceDescription.class);
			return new ServiceDescription[0];
		}
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
			//e.printStackTrace(System.err);
			logger.debug("getServiceDescription(URI): OUTPUT: Returning null");
			return null;
		}
	}
	
//TODO: DELETE AFTER INTEGRATION WITH FPR !!!
	// Simulates FPR query operation for recommended services
	@GET
	@Path("/frp-recommended/service/{serv_id}/timestamp/{timestamp}")
	@Produces("application/json")
	public String simulateFprRecomResponse(@PathParam("serv_id") String srvUri, @PathParam("timestamp") String tm) {
		srvUri = java.net.URLDecoder.decode(srvUri);
		logger.warn("simulateFprRecomResponse: FPR QUERIED:  service={}, timestamp={}", srvUri, tm);
		logger.warn("simulateFprRecomResponse: ** REMOVE AFTER INTEGRATING WITH FPRC **");
		return String.format("{ \"recommended\" : [ \"admin\", \"sc1\", \"sc2\" ] }");
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
			//e.printStackTrace(System.err);
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