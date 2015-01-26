package eu.brokeratcloud.opt;

import eu.brokeratcloud.common.RootObject;
import eu.brokeratcloud.common.ClassificationDimension;
import eu.brokeratcloud.common.ServiceDescription;
import eu.brokeratcloud.common.SLMEvent;
import eu.brokeratcloud.opt.ahp.AhpHelper;
import eu.brokeratcloud.opt.policy.*;
import eu.brokeratcloud.rest.opt.ProfileManagementService;
import eu.brokeratcloud.rest.opt.ServiceCategoryAttributeManagementServiceNEW;

import eu.brokeratcloud.persistence.RdfPersistenceManager;
import eu.brokeratcloud.persistence.RdfPersistenceManagerFactory;
import eu.brokeratcloud.persistence.SparqlServiceClient;
import eu.brokeratcloud.persistence.SparqlServiceClientFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.UUID;
import java.util.Vector;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.RDFNode;

public class RecommendationManager extends RootObject {
	private static final Logger logger = LoggerFactory.getLogger("eu.brokeratcloud.opt.RecommendationManager");
	
	// ================================================================================================================
	// Constructors and factory methods
	
	protected static Properties defaultSettings;
	protected Properties settings;
	
	/* Factory paradigm methods (public) */
	public static RecommendationManager getInstance() {
		return new RecommendationManager();
	}
	public static RecommendationManager getInstance(String propertiesFile) {
		return new RecommendationManager(propertiesFile);
	}
	public static RecommendationManager getInstance(Properties properties) {
		return new RecommendationManager(properties);
	}
	
	/* Constructors (protected) */
	protected RecommendationManager() {
		if (defaultSettings==null) {
			logger.debug("RecommendationManager.<init> : initializing default settings");
			defaultSettings = _createDefaultSettings();
			_initFromFile("/recommendations-manager.properties");
		}
	}
	
	protected RecommendationManager(String propertiesFile) {
		logger.debug("RecommendationManager.<init> : when = {}", new java.util.Date());
		_initFromFile(propertiesFile);
	}
	
	protected RecommendationManager(Properties props) {
		logger.debug("RecommendationManager.<init> : when = {}", new java.util.Date());
		settings = new Properties(defaultSettings);
		settings.putAll(props);
	}
	
	// ================================================================================================================
	// Initialization helper methods
	
	protected Properties _createDefaultSettings() {
		Properties p = new Properties();
		// general settings
		p.setProperty("serviceDescriptionRetrievalUrl", "");
		p.setProperty("serviceDescriptionRetrievalWithProgrammaticCall", "true");
		p.setProperty("pruneMode", "NONE");
		p.setProperty("updateConsumerPreferenceProfile", "false");
		p.setProperty("alwaysGenerateRecommendation", "false");		// don't generate a new recommendation if there are no items/services to suggest
		p.setProperty("dontStoreRecommendation", "true");
		// filtering settings
		p.setProperty("periodSinceLastRecom", "0");
		p.setProperty("relevanceThreshold", "0");
		p.setProperty("periodOfIgnores", "0");
		p.setProperty("ignoresThreshold", "0");
		p.setProperty("periodSinceLastRecomOfFRPC", "0");
		p.setProperty("relevanceThresholdForFRPC", "0");
		return p;
	}
	
	protected void _initFromFile(String file) {
		settings = new Properties(defaultSettings);
		Properties p = _loadSettings(file);
		if (p!=null) settings.putAll(p);
	}
	
	protected Properties _loadSettings(String file) {
		try {
			Properties p = new Properties();
			logger.debug("Reading default properties from file: {}...", file);
			java.io.InputStream is = getClass().getResourceAsStream(file);
			if (is==null) {
				logger.debug("Reading default properties from file: {}... Not found", file);
				return null;
			}
			p.load( is );
			logger.debug("Reading default properties from file: {}... done", file);
			return p;
		} catch (Exception e) {
			logger.error("Exception while reading default properties from file: {},  Exception: {}", file, e);
			return null;
		}
	}
	
	// ================================================================================================================
	// Helper methods
	
	@SuppressWarnings("unchecked")
	protected static Object _callBrokerRestWS(String url, String method, Class clss, Object entity) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(url);
		
		Response response = null;
		method = method.trim().toLowerCase();
		if (method.equals("get")) response = target.request().get();
		else if (method.equals("put")) response = target.request().put( Entity.json(entity) );
		else if (method.equals("post")) response = target.request().post( Entity.json(entity) );
		else if (method.equals("delete")) response = target.request().delete();
		int status = response.getStatus();
		logger.debug("--> Response: {}", status);
		if (status>299) throw new RuntimeException("Operation failed: Status="+status+", URL="+url);
		
		Object obj = null;
		if (clss!=null) {
			obj = response.readEntity( clss );
		}
		response.close();
		return obj;
	}
	
	protected void _updatePreferenceWeights(ConsumerPreferenceProfile profile, HierarchyNode<OptimisationAttribute> topLevelNode) {
		ConsumerPreference[] preferences = profile.getPreferences();
		for (ConsumerPreference pref : preferences) {
			if (pref==null) continue;
			String prefAttrId = _getServiceAttributeId(pref);
			if (prefAttrId==null) continue;
			HierarchyNode<OptimisationAttribute> node = topLevelNode.findNodeByAttributeId(prefAttrId);
			double product = 1;
			while (node!=null) { product *= node.getWeight(); node = node.getParent(); }
			pref.setWeight( product );
		}
		profile.setWeightCalculation( false );
		// it's up to the calling methods to actually persist new profile state to persistence store
	}
	
	protected HashMap<ConsumerPreference,String> hmap1 = new HashMap<ConsumerPreference,String>();
	
	public String _getServiceAttributeId(ConsumerPreference pref) {
		if (hmap1.containsKey(pref)) {
			return hmap1.get(pref);
		} else {
			String pvUri = pref.getPrefVariable();
			ServiceCategoryAttributeManagementServiceNEW.PolicyObjects po = ServiceCategoryAttributeManagementServiceNEW.getBrokerPolicyObjects(pvUri, false);
			logger.trace("_getServiceAttributeId: policy objects={}", po);
			if (po==null) return null;
			PreferenceVariable pv = po.pv;
			if (pv.getRefToServiceAttribute()==null) {
				logger.trace("_getServiceAttributeId: pref.var. ref'ed attribute is NULL: pv-uri={}", pref.getPrefVariable());
				return null;
			}
			String id = pv.getRefToServiceAttribute().getId();
			hmap1.put(pref, id);
			return id;
		}
	}
	
	protected HashMap<ConsumerPreference,PreferenceVariable> hmap2 = new HashMap<ConsumerPreference,PreferenceVariable>();
	
	public PreferenceVariable _getPrefVariable(ConsumerPreference pref) {
		if (hmap2.containsKey(pref)) {
			return hmap2.get(pref);
		} else {
			String pvUri = pref.getPrefVariable();
			ServiceCategoryAttributeManagementServiceNEW.PolicyObjects po = ServiceCategoryAttributeManagementServiceNEW.getBrokerPolicyObjects(pvUri, false);
			logger.trace("_getPrefVariable: policy objects={}", po);
			if (po==null) return null;
			PreferenceVariable pv = po.pv;
			hmap2.put(pref, pv);
			return pv;
		}
	}
	
	protected HashMap<ConsumerPreference,String> hmap3 = new HashMap<ConsumerPreference,String>();
	
	public String _getServiceAttributeUri(ConsumerPreference pref) {
		if (hmap3.containsKey(pref)) {
			return hmap3.get(pref);
		} else {
			String pvUri = pref.getPrefVariable();
			ServiceCategoryAttributeManagementServiceNEW.PolicyObjects po = ServiceCategoryAttributeManagementServiceNEW.getBrokerPolicyObjects(pvUri, false);
			logger.trace("_getServiceAttributeUri: policy objects={}", po);
			if (po==null) return null;
			String uri = po.apvUri;
			logger.trace("_getServiceAttributeUri: uri={}", uri);
			hmap3.put(pref, uri);
			return uri;
		}
	}
	
	// ======================================================================================================
	// Recommendation generation API and implementation
	
	protected ProfileManagementService profileMgntWs;
	
	public HashMap<String,Recommendation> requestRecommendations(SLMEvent evt) {
		String sdId = evt.getProperty("service-description");
		if (sdId==null || (sdId=sdId.trim()).isEmpty()) {
			logger.error("SLM Event does not contain a valid service description identifier");
			return null;
		}
//XXX: Replace with actual SGQC code when given
		eu.brokeratcloud.rest.opt.AuxiliaryService sgqcWs = new eu.brokeratcloud.rest.opt.AuxiliaryService();
		logger.debug("Retrieving service description: {}", sdId);
//XXX: Replace with actual SGQC code when given
		ServiceDescription sd = sgqcWs.getServiceDescription(null, sdId);		//XXX: 2014-11-22: BUGGY implementation of 'getServiceDescription'
		if (sd==null) {
			logger.error("Service description not found: {}", sdId);
			return null;
		}
		logger.debug("Service description:\n{}", sd);
		String categoryId = sd.getServiceCategory();
		logger.debug("Service category: {}", categoryId);
		if (categoryId==null || (categoryId=categoryId.trim()).isEmpty()) {
			logger.error("Service category is null or empty");
			return null;
		}
		
		if (profileMgntWs==null) profileMgntWs = new ProfileManagementService();
		List<ConsumerPreferenceProfile> vect = new Vector<ConsumerPreferenceProfile>();
		for (String catId : categoryId.split("[,]")) {
			catId = catId.trim();
			if (catId.isEmpty()) continue;
			logger.debug("Retrieving profiles in category: {}", catId);
			ConsumerPreferenceProfile[] list = profileMgntWs.getProfilesInCategory(catId);
			logger.debug("Profiles in category:\n{}", Arrays.deepToString(list));
			
			// merging into overall profiles list
			for (ConsumerPreferenceProfile cpp : list) {
				if (!vect.contains(cpp)) vect.add(cpp);
			}
		}
		ConsumerPreferenceProfile[] profilesList = vect.toArray(new ConsumerPreferenceProfile[vect.size()]);
		logger.debug("Profiles in all categories:\n{}", Arrays.deepToString(profilesList));
		
		logger.debug("Generating recommendations for retrieved profiles...");
		HashMap<String,Recommendation> newRecoms = new HashMap<String,Recommendation>();
		if (profilesList!=null) {
			Properties p = new Properties();
			p.setProperty("dontStoreRecommendation", "false");	// force recommendation save
			for (int i=0; i<profilesList.length; i++) {
				ConsumerPreferenceProfile profile = profilesList[i];
				if (profile==null || profile.getId()==null || profile.getId().trim().isEmpty()
					|| profile.getOwner()==null || profile.getOwner().trim().isEmpty())
				{
					logger.trace("Profile is null, empty or not valid: list item #{}", i);
					continue;
				}
				String profileId = profile.getId();
				String ownerId = profile.getOwner();
				logger.debug("Generating recommendation for profile: id={}, owner={}", profileId, ownerId);
				try {
					Recommendation recom = _createNewRecommendation(ownerId, profileId, p, false);		// false: don't force recom. creation
					logger.debug("New recommendation for profile: id={}, owner={}\nrecommendation={}", profileId, ownerId, recom);
					newRecoms.put(profileId, recom);
				} catch (Exception e) {
					logger.error("requestRecommendations: Input: consumer={}, profile={}. Exception caught:\n{}", ownerId, profileId, e);
					return null;
				}
			}
		}
		return newRecoms;
	}
	
	public Recommendation createNewRecommendation(String consumerId, String profileId) {
		return createNewRecommendation(consumerId, profileId, false);
	}
	
	public Recommendation createNewRecommendation(String consumerId, String profileId, boolean forceCreation) {
		logger.info("RecommendationManager.createNewRecommendation() invoked : when = {}: profile = {}", new java.util.Date(), profileId);
		try {
			return _createNewRecommendation(consumerId, profileId, settings, forceCreation);
		} catch (Exception e) {
			logger.error("createNewRecommendation: EXCEPTION: consumer={}, profile={}", consumerId, profileId);
			logger.error("Exception caught: {}", e);
			return null;
		}
	}
	
	protected Recommendation _createNewRecommendation(String consumerId, String profileId, Properties settings, boolean forceCreation) throws java.io.IOException, IllegalAccessException, NoSuchMethodException, java.lang.reflect.InvocationTargetException {
		Properties prop = new Properties(defaultSettings);
		if (settings!=null) prop.putAll(settings);
		// General settings
		String sdWsUrlTemplate = prop.getProperty("serviceDescriptionRetrievalUrl");
		boolean sgqcProgrammaticCall = Boolean.valueOf( prop.getProperty("serviceDescriptionRetrievalWithProgrammaticCall", "false") );
		PRUNE_MODE pruneMode = PRUNE_MODE.valueOf( prop.getProperty("pruneMode", "NONE") );
		boolean updateProfile = Boolean.valueOf( prop.getProperty("updateConsumerPreferenceProfile") );
		boolean alwaysGenerateRecom = Boolean.valueOf( prop.getProperty("alwaysGenerateRecommendation", "false") );		// don't generate a new recommendation if there are no items/services to suggest
		boolean dontStoreRecom = Boolean.valueOf( prop.getProperty("dontStoreRecommendation") );
		// Filtering settings
		long periodOfLastRecom = Long.parseLong( prop.getProperty("periodSinceLastRecom", "0") );
		double relevanceThreshold = Long.parseLong( prop.getProperty("relevanceThreshold", "0") );
		long periodOfIgnores = Long.parseLong( prop.getProperty("periodOfIgnores", "0") );
		int ignoresThreshold = (int)Long.parseLong( prop.getProperty("ignoresThreshold", "0") );
		long periodOfLastRecomOfFPRC = Long.parseLong( prop.getProperty("periodSinceLastRecomOfFPRC", "0") );
		double relevanceThresholdForFRPC = Long.parseLong( prop.getProperty("relevanceThresholdForFPRC", "0") );
		// debug print
		if (logger.isDebugEnabled()) {
			logger.debug("------------------------------------------------------------------------------");
			// general settings
			logger.debug("RECOMMENDATION GENERATION PROPERTIES:");
			logger.debug("serviceDescriptionRetrievalUrl: {}", prop.getProperty("serviceDescriptionRetrievalUrl"));
			logger.debug("serviceDescriptionRetrievalWithProgrammaticCall: {}", prop.getProperty("serviceDescriptionRetrievalWithProgrammaticCall"));
			logger.debug("pruneMode: {}", prop.getProperty("pruneMode"));
			logger.debug("updateConsumerPreferenceProfile: {}", prop.getProperty("updateConsumerPreferenceProfile"));
			logger.debug("alwaysGenerateRecommendation: {}", prop.getProperty("alwaysGenerateRecommendation"));
			logger.debug("dontStoreRecommendation: {}", prop.getProperty("dontStoreRecommendation"));
			// filtering settings
			logger.debug("FILTERING SETTINGS:");
			logger.debug("periodSinceLastRecom: {}", prop.getProperty("periodSinceLastRecom"));
			logger.debug("relevanceThreshold: {}", prop.getProperty("relevanceThreshold"));
			logger.debug("periodOfIgnores: {}", prop.getProperty("periodOfIgnores"));
			logger.debug("ignoresThreshold: {}", prop.getProperty("ignoresThreshold"));
			logger.debug("------------------------------------------------------------------------------");
		}
		
		// get consumer preference profile
		logger.info("Retrieving profile {}... ", profileId);
		if (profileMgntWs==null) profileMgntWs = new ProfileManagementService();
		ConsumerPreferenceProfile profile = profileMgntWs.getProfile(consumerId, profileId);
		logger.debug("Initial Consumer Preference Profile:\n{}", profile);
		
		// get needed profile data
		String owner = profile.getOwner();
		String selectionPolicy = profile.getSelectionPolicy();
		ClassificationDimension[] classifications = profile.getServiceClassifications();
		StringBuilder sb = new StringBuilder();
		boolean first=true;
		for (ClassificationDimension cd : classifications) {
			if (first) first=false; else sb.append(",");
			sb.append(cd.getId());
		}
		String categoryId = sb.toString();
		ConsumerPreference[] preferences = profile.getPreferences();
		boolean weightCalculation = profile.getWeightCalculation();
		logger.info("Profile data : owner={}, sel policy={}, classifications={}, prefs={}, wght calc={}", owner, selectionPolicy, categoryId, preferences.length, weightCalculation);
		
		// print-debug profile preferences
		if (preferences==null ||preferences.length==0) {
			logger.info("Profile {} has no preferences", profileId);
			return null;
		}
		logger.info("Profile {} has {} preferences", profileId, preferences.length);
		boolean hasErrors = false;
		for (int i=0, size=preferences.length; i<size; i++) {
			ConsumerPreference pref = preferences[i];
			if (pref==null) { logger.error("Profile {} contains 'null' preference(s)", profileId); hasErrors = true; continue; }
			String prefId = pref.getId();
			String prefAttrId = _getServiceAttributeId(pref);
			if (prefId==null || prefId.trim().isEmpty()) { logger.error("Profile {} contains preference(s) with no Id", profileId); preferences[i] = null; hasErrors = true; continue; }
			if (prefAttrId==null || prefAttrId.trim().isEmpty()) { logger.error("Profile {} contains preference(s) with no attribute id", profileId); preferences[i] = null; hasErrors = true; continue; }
			logger.debug("\tPreference: {}", prefAttrId);
		}
		if (hasErrors) {
			logger.error("Profile '{}' contains errors. No recommendations will be generated", profileId);
			return null;
		}
		
		// get services
		ServiceDescription[] SDs = null;
		if (!sgqcProgrammaticCall) {
			String sgqcWsUrl = String.format(sdWsUrlTemplate, categoryId);
			logger.info("Retrieving service descriptions with classification(s): {}, from url: {}", categoryId, sgqcWsUrl);
			long callStartTm = System.currentTimeMillis();
			SDs = (ServiceDescription[])_callBrokerRestWS(sgqcWsUrl, "GET", java.lang.reflect.Array.newInstance(ServiceDescription.class, 0).getClass(), null);
			long callEndTm = System.currentTimeMillis();
		} else {
			// If PuLSaR is collocated with SGQC component, it is possible to programmatically call the relevant method
			// Thus code becomes simpler and faster
			logger.info("Retrieving service descriptions for category {}", categoryId);
//XXX: These statements MUST BE REPLACED with the correct, as soon as Service Gov. & Qualtiy Control component details becomes available
			eu.brokeratcloud.rest.opt.AuxiliaryService sgqcWS = new eu.brokeratcloud.rest.opt.AuxiliaryService();
			SDs = sgqcWS.getServiceDescriptionsForCategories( categoryId );
		}
		
		// print-debug service descriptions
		if (SDs==null || SDs.length==0) { logger.error("No service descriptions found in service category {}", categoryId); return null; }
		logger.info("Service descriptions with classifications {}: {}", categoryId, SDs.length);
		hasErrors = false;
		for (ServiceDescription sd : SDs) {
			if (sd==null) { logger.error("Service category {} contains 'null' service description(s)", categoryId); hasErrors = true; continue; }
			String sdId = sd.getId();
			String sdName = sd.getName();
			if (sdId==null || sdId.trim().isEmpty()) { logger.error("Service category {} contains service description(s) with no Id", categoryId); hasErrors = true; continue; }
			if (sdName==null || sdName.trim().isEmpty()) { logger.error("Service category {} contains service description(s) with no Name", categoryId); hasErrors = true; continue; }
			logger.debug("\tService: uri={}, name={}", sdId, sdName);
		}
		if (hasErrors) {
			logger.error("Service Descriptions list for category '{}' contains errors. No recommendations will be generated", categoryId);
			return null;
		}
		
		// filter out services by checking preference constraints
		logger.info("Checking service descriptions against preference constraints...");
		for (ConsumerPreference pref : preferences) {
			if (pref==null) continue;
			ConsumerPreferenceExpression expression = pref.getExpression();
			if (expression==null) { logger.debug("\tPreference {} has no constraint expression. Skipping it", _getServiceAttributeId(pref)); continue; }
			
			String prefAttrId = _getServiceAttributeId(pref);
			String prefAttrUri = _getServiceAttributeUri(pref);
			String formatter = String.format("Checking service description '{}' against preference '%s' constraint (uri: %s)", prefAttrId, prefAttrUri);
			String formatter2 = String.format("Service description '{}' was rejected due to preference '%s' constraint (uri: %s)", prefAttrId, prefAttrUri);
			// check every service description against current constraint
			for (int i=0; i<SDs.length; i++) {
				ServiceDescription sd = SDs[i];
				if (sd==null) continue;		// this service description has been filtered out in a previous iteration of the outer for-loop (i.e. rejected due to another constraint)
				logger.debug(formatter, sd.getId());
				Object attrVal = sd.getServiceAttributeValue(prefAttrUri);
				logger.trace("Evaluating constraint for service attribute value: {}", attrVal);
				boolean exprVal = false;
				try {
					exprVal = expression.evaluate(attrVal);
				} catch (Exception e) {
					logger.warn("Constraint evaluating caused an exception: setting result to 'false': ", e);
					exprVal = false;
				}
				logger.trace("Evaluation result: {}", exprVal);
				if (exprVal==false) {
					logger.debug(formatter2, sd.getId());
					SDs[i] = null;
					continue;
				}
			}
		}
		logger.debug("Service descriptions remaining after preference constraints checking...");
		int cntRemainingSDs = 0;
		for (int i=0; i<SDs.length; i++) {
			if (SDs[i]==null) continue;
			logger.debug("\t{}", SDs[i].getId());
			cntRemainingSDs++;
		}
		logger.info("Service descriptions remaining after preference constraints checking: {}", cntRemainingSDs);
		// This is not necessary if it is guaranteed that 'SDs' contains only valid (and not null) items
		ServiceDescription[] newSDs = new ServiceDescription[cntRemainingSDs];
		for (int i=0, j=0; i<SDs.length; i++) {
			if (SDs[i]==null) continue;
			newSDs[j++] = SDs[i];
		}
		SDs = newSDs;
		
		// prepare structures for ranking
		// generate hierarchy model
		logger.info("Generating Hierarchy Model...");
		HierarchyNode<OptimisationAttribute> topLevelGoal = _prepareHierarchy(preferences);
		logger.debug("Hierarchy Model generated:\n{}", topLevelGoal.toString());
		
		// calculate hierarchy model node weights
		if (weightCalculation) {
			logger.info("Calculating weights of Hierarchy Model nodes...");
			if (logger.isDebugEnabled()) for (ComparisonPair pr : profile.getComparisonPairs()) logger.debug("C-PAIR: {} / {} : {}", pr.getAttribute1(), pr.getAttribute2(), pr.getValue());
			HashMap<String,HashMap<String,Double>> pairs = _prepareComparisonPairs(profile);
			if (logger.isDebugEnabled()) logger.debug("PAIRS: {}", pairs);
			_calculateChildrenWeights(topLevelGoal, pairs);
			topLevelGoal.setWeight(1);
			logger.debug("Hierarchy Model with weights:\n{}", topLevelGoal.toString());
			
			if (updateProfile) {
				// update profile with newly calculated weights (Note that weight-calculation flag is cleared)
				_updatePreferenceWeights(profile, topLevelGoal);
				logger.debug("Consumer Preference Profile updated with preference weights:\n{}", profile);
				// for better performance updated profile can also be updated (merged) to persistence store
				int status = profileMgntWs.updateProfile(consumerId, profileId, profile).getStatus();
				logger.info("Consumer Preference Profile state updated in peristent store: id={}: result={}", profileId, status );
			}
		} else
			logger.info("Weights of Hierarchy Model nodes have been manually set!");
		
		// prune hierarchy model
		if (pruneMode!=PRUNE_MODE.NONE) {
			logger.info("Pruning Hierarchy Model...");
			topLevelGoal = _pruneModel(topLevelGoal, pruneMode);
			logger.debug("Pruned Hierarchy Model:\n{}", topLevelGoal.toString());
		}
		
		// gather criteria/preferences into a hash map
		HashMap<String,ConsumerPreference> criteria = new HashMap<String,ConsumerPreference>();
		for (int i=0, size=preferences.length; i<size; i++) {
			if (preferences[i]==null) continue;
			String prefAttrId = _getServiceAttributeId(preferences[i]);
			if (prefAttrId==null || prefAttrId.trim().isEmpty()) continue;
			criteria.put(prefAttrId, preferences[i]);
		}
		
		// call ranking engine
		logger.info("Ranking service descriptions...");
		AhpHelper helper = new AhpHelper();
		List<AhpHelper.RankedItem> rankedSDs = helper.rank(this /*2014-11-21: Addition*/, topLevelGoal, criteria, SDs);
		logger.debug("Ranked list of service descriptions:\n{}", rankedSDs);
		
		// generate recom-id base and timestamp
		Date createTm = new Date();
		String recomId = UUID.randomUUID().toString();
		
		// filter out services by comparing to Failure Prevention and Recovery component's recommendations
		logger.info("Post-ranking service filtering...");
		List<RecommendationItem> recomItems = new ArrayList<RecommendationItem>();
		double sumRel = 0;
		int cntItems = 0;
		for (AhpHelper.RankedItem item : rankedSDs) {
			boolean rv1 = false;
			boolean rv2 = false;
			boolean rv3 = false;
			if (!forceCreation) {
				rv1 = _checkIfAlreadyRecommended(consumerId, profileId, item, periodOfLastRecom, relevanceThreshold);
				rv2 = _checkIfIgnored(consumerId, profileId, item, periodOfIgnores, ignoresThreshold);
				rv3 = _checkIfAlreadyRecommendedByFailurePreventionAndRecoveryComponent(item, periodOfLastRecomOfFPRC, relevanceThresholdForFRPC);
			}
			if (rv1 || rv2 || rv3) {
				logger.debug("Service was filtered-out: Reason: already recommended={}, ignored={}, recommended by Failure Prevention & Recovery Component={}", rv1, rv2, rv3);
			} else {
				RecommendationItem rit = new RecommendationItem();
				rit.setId( "RECOM-ITEM-"+recomId+"-"+(cntItems++) );
				rit.setCreateTimestamp( createTm );
				rit.setOwner( consumerId );
				rit.setServiceDescription( item.item.getId() );
				rit.setSuggestion( String.format("Use service: <i>%s</i><br/>Creator: <i>%s</i>&nbsp;&nbsp;&nbsp;Profile: <i>%s</i>", 
													item.item.getName(), item.item.getOwner(), item.item.getServiceAttributeValue(".SERVICE-LEVEL-PROFILE-ID")) );
				rit.setWeight( item.relevance );
				
				Map<String,Object> attrs = item.item.getServiceAttributes();
				if (attrs!=null) {
					String[][] attrsArr = new String[attrs.size()][2];
					int i=0;
					for (String at : attrs.keySet()) {
						Object val = attrs.get(at);
						attrsArr[i][0] = at;
						attrsArr[i][1] = val!=null ? val.toString() : "";
						i++;
					}
					rit.setExtra(attrsArr);
				}

				recomItems.add( rit );
				sumRel += item.relevance;
			}
		}
		// Re-calculate relevances
		for (RecommendationItem item : recomItems) {
			item.setWeight( item.getWeight() / sumRel );
		}
		
		// Apply selection policy
		if (recomItems.size()>0) {
			if (selectionPolicy==null || selectionPolicy.trim().isEmpty()) {
				logger.warn("No selection policy specified");
				logger.warn("Returning the full list");
			} else
			if (selectionPolicy.trim().equalsIgnoreCase("ALL")) {
				logger.debug("Applying selection policy: type=ALL");
				logger.debug("Returning the full list");
			} else {
				String[] spPart = selectionPolicy.split("[ \t]");
				if (spPart.length==1) {
					logger.warn("Invalid selection policy specification: {}", selectionPolicy);
					logger.warn("Returning the full list");
				} else
				if (spPart.length>1) {
					spPart[0] = spPart[0].trim().toUpperCase();
					spPart[1] = spPart[1].trim();
					boolean isPercentage = spPart[1].trim().endsWith("%") || spPart.length>2 && spPart[2].trim().startsWith("%");
					if (isPercentage && spPart[1].endsWith("%")) spPart[1] = spPart[1].substring(0, spPart[1].length()-1).trim();
					int nItems = recomItems.size();
					boolean recalcWeights = false;
					logger.debug("Applying selection policy: type={}, param={}, is percentage={}, list-size={}", spPart[0], spPart[1], isPercentage, nItems);
					if (spPart[0].equals("TOP")) {
						int num = isPercentage ?  (int)Math.round(Double.parseDouble(spPart[1]) * nItems / 100) : Integer.parseInt(spPart[1]);
						if (num>nItems) num = nItems;
						else if (num<0) num = 0;
						logger.debug("Final list size limit: {}", num);
						if (num>0 && nItems>num) {
							recomItems = recomItems.subList(0, num);
							recalcWeights = true;
							logger.debug("Final list truncated from {} to {} items", nItems, recomItems.size());
						}
					} else
					if (spPart[0].equals("OVER")) {
						double threshold;
						if (isPercentage) {
							double percentage = Double.parseDouble(spPart[1]);
							double topRelevance = recomItems.get(0).getWeight();
							threshold = percentage * topRelevance / 100;
							logger.debug("Item selection parameters: percentage={}, top-relevance={}", percentage, topRelevance);
						} else {
							threshold = Double.parseDouble(spPart[1]);
						}
						if (threshold>1) threshold = 1;
						else if (threshold<0) threshold = 0;
						logger.debug("Item selection relevance threshold: {}", threshold);
						int num = nItems;
						for (int i=0; i<nItems; i++) {
							double itemRel = recomItems.get(i).getWeight();
							logger.trace("Checking item {}: relevance={}", i, itemRel);
							if (itemRel<threshold) {
								num = i;
								break;
							}
						}
						logger.debug("Final list size limit: {}", num);
						if (num < nItems && num>=0) {
							recomItems = recomItems.subList(0, num);
							recalcWeights = true;
							logger.debug("Final list truncated from {} to {} items", nItems, recomItems.size());
						}
					} else {
						logger.warn("Invalid selection policy specification: {}", selectionPolicy);
						logger.warn("Returning the full list");
					}
					// Re-calculate relevances (if needed)
					if (recalcWeights) {
						sumRel = 0;
						for (RecommendationItem item : recomItems) {
							sumRel += item.getWeight();
						}
						if (sumRel>0) {
							for (RecommendationItem item : recomItems) {
								item.setWeight( item.getWeight() / sumRel );
							}
						}
					}
				}
			}
		} else {
			logger.debug("No recommendation items generated. No need to apply selection policy.");
		}
		
		// prepare a new recommendation object
		Recommendation newRecom = null;
		if (alwaysGenerateRecom || recomItems.size() > 0) {
			newRecom = new Recommendation();
			newRecom.setId( "RECOMMENDATION-"+recomId );
			newRecom.setCreateTimestamp(createTm);
			newRecom.setOwner(consumerId);
			newRecom.setProfile(profileId);
			newRecom.setItems(recomItems);
			logger.info("New recommendation: {}", newRecom.getId());
			logger.debug("New recommendation details:\n{}", newRecom);
		} else {
			logger.warn("No items to recommend. No recommendation will be generated");
		}
		
		// store new recommendation (if one generated)
		if (!dontStoreRecom && newRecom!=null) {
			// clear previous recommendations' active flags (ie mark them as inactive)
			eu.brokeratcloud.rest.opt.RecommendationManagementService recomMgntWS = new eu.brokeratcloud.rest.opt.RecommendationManagementService();
			recomMgntWS.clearProfileRecommendations(owner, profileId);
			
			// save new recommendation
			newRecom.setActive(true);
			int status = recomMgntWS.createRecommendation(newRecom).getStatus();
			logger.info("New recommendation for profile '{}' stored in persistent store: status = {}", profileId, status );
		}
		
		return newRecom;
	}
	
	// Calculates profile preference weights using comparison pairs (by calling RecommendationManager)
	// It honours the 'weightCalculation' flag (if it is true then weight (re-)calculation is need; 
	//   if it is false then weights have been set manually)
	public void calculatePreferenceWeights(ConsumerPreferenceProfile profile) {
		String profileId = profile.getId();
		ConsumerPreference[] preferences = profile.getPreferences();
		logger.info("Calculating preference weights for profile: {}", profileId);

		// check profile preferences
		if (preferences==null ||preferences.length==0) {
			logger.info("Profile {} has no preferences", profileId);
			return;
		}
		logger.info("Profile {} has {} preferences", profileId, preferences.length);
		boolean hasErrors = false;
		for (int i=0, size=preferences.length; i<size; i++) {
			ConsumerPreference pref = preferences[i];
			if (pref==null) { logger.error("Profile {} contains 'null' preference(s)", profileId); hasErrors = true; continue; }
			String prefId = pref.getId();
			if (prefId==null || prefId.trim().isEmpty()) { logger.error("Profile {} contains preference(s) with no Id", profileId); preferences[i] = null; hasErrors = true; continue; }
			PreferenceVariable pv = _getPrefVariable(pref);
			if (pv==null) { logger.error("Profile {} contains preference with no reference to a preference variable", profileId); preferences[i] = null; hasErrors = true; continue; }
			OptimisationAttribute attr = pv.getRefToServiceAttribute();
			if (attr==null) { logger.error("Profile {} contains preference with preference variable that references no service attribute", profileId); preferences[i] = null; hasErrors = true; continue; }
			String prefAttrId = attr.getId();
			if (prefAttrId==null || prefAttrId.trim().isEmpty()) { logger.error("Profile {} contains preference that reference preference variable that references a service attribute with no attribute id", profileId); preferences[i] = null; hasErrors = true; continue; }
			logger.debug("\t{}", prefAttrId);
		}
		if (hasErrors) {
			logger.error("Profile '{}' contains errors. No weights update will occur", profileId);
			return;
		}
		
		// prepare structures for ranking
		// generate hierarchy model
		logger.info("Generating Hierarchy Model...");
		HierarchyNode<OptimisationAttribute> topLevelGoal = _prepareHierarchy(preferences);
		logger.debug("Hierarchy Model generated:\n{}", topLevelGoal.toString());
		
		// calculate hierarchy model node weights
		if (profile.getWeightCalculation()) {
			logger.info("Calculating weights of Hierarchy Model nodes...");
			if (logger.isDebugEnabled()) for (ComparisonPair pr : profile.getComparisonPairs()) logger.debug("C-PAIR: {} / {} : {}", pr.getAttribute1(), pr.getAttribute2(), pr.getValue());
			HashMap<String,HashMap<String,Double>> pairs = _prepareComparisonPairs(profile);
			if (logger.isDebugEnabled()) logger.debug("PAIRS: {}", pairs);
			_calculateChildrenWeights(topLevelGoal, pairs);
			topLevelGoal.setWeight(1);
			logger.debug("Hierarchy Model with weights:\n{}", topLevelGoal.toString());
			
			// update profile with newly calculated weights (Note that weight-calculation flag is cleared)
			_updatePreferenceWeights(profile, topLevelGoal);
			logger.debug("Consumer Preference Profile updated with preference weights:\n{}", profile);
			logger.info("Profile preference weights have not been saved in persistent store");
		} else
			logger.info("Weights of Hierarchy Model nodes have been manually set!");
	}
	
	// ================================================================================================================
	// Hierarchy tree creation methods (AHP phase II)
	
	public static class HierarchyNode<T> {
		protected String name;
		protected T attribute;
		protected double weight;
		protected HierarchyNode<T> parent;
		protected HashMap<String,HierarchyNode<T>> children;
		
		public HierarchyNode() { children = new HashMap<String,HierarchyNode<T>>(); }
		public HierarchyNode(String name, T attrObj, HierarchyNode<T> parent) { this(); this.name = name; this.attribute = attrObj; this.parent = parent; }
		
		public String getName() { return name; }
		public T getAttribute() { return attribute; }
		public double getWeight() { return weight; }
		public void setWeight(double w) { weight = w; }
		public HierarchyNode<T> getParent() { return parent; }
		public void setParent(HierarchyNode<T> p) { parent = p; }
		
		public int getChildCount() { return children.size(); }
		public HierarchyNode<T> getChild(String name) { return children.get(name); }
		public void addChild(HierarchyNode<T> newChild) { children.put(newChild.getName(), newChild); newChild.setParent(this); }
		public void removeChild(HierarchyNode<T> child) { children.remove(child.getName()); }
		public List<HierarchyNode<T>> getChildren() { return new ArrayList<HierarchyNode<T>>(children.values()); }
		public void removeAllChildren() { children.clear(); }
		
		public HierarchyNode<T> findNodeByAttributeId(String attrId) {
			if (name!=null && name.equals(attrId)) return this;
			if (children!=null) {
				if (children.containsKey(attrId)) return children.get(attrId);
				for (HierarchyNode<T> child : children.values()) {
					HierarchyNode<T> rv = child.findNodeByAttributeId(attrId);
					if (rv!=null) return rv;
				}
			}
			return null;
		}
		
		public List<HierarchyNode<T>> getLeafNodes() {
			ArrayList<HierarchyNode<T>> list = new ArrayList<HierarchyNode<T>>();
			return _getLeafNodes(list);
		}
		
		protected List<HierarchyNode<T>> _getLeafNodes(List<HierarchyNode<T>> list) {
			int cnt = children.size();
			if (cnt==0) {	// this is a leaf node
				list.add(this);
			} else {		// non-leaf node
				for (HierarchyNode<T> child : children.values()) {
					child._getLeafNodes(list);
				}
			}
			return list;
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			_toString(sb, "");
			return sb.toString();
		}
		
		protected void _toString(StringBuffer sb, String ident) {
			sb.append(ident); sb.append(name); sb.append(" : "); sb.append(weight); sb.append("\n");
			String newIdent = ident+"  ";
			for (HierarchyNode<T> child : children.values()) {
				child._toString(sb, newIdent);
			}
		}
	}
	
	protected String _prepareAttributeId(String attrId) {
		int p = attrId.lastIndexOf('#');
		if(p>0) attrId = attrId.substring(p+1);
		return attrId;
	}
	
	protected HierarchyNode<OptimisationAttribute> _prepareHierarchy(ConsumerPreference[] preferences) {
		// Convert preferences into attributes hierarchy
		HashMap<String,OptimisationAttribute> cache = new HashMap<String,OptimisationAttribute>();
		HierarchyNode<OptimisationAttribute> root = new HierarchyNode<OptimisationAttribute>();
		Stack<String> path = new Stack<String>();
		eu.brokeratcloud.rest.opt.AttributeManagementService attrMgntWS = null;
		
		for (ConsumerPreference cp : preferences) {
			if (cp==null) continue;
			PreferenceVariable pv = _getPrefVariable(cp);
			if (pv==null || pv.getRefToServiceAttribute()==null || pv.getRefToServiceAttribute().getId()==null) continue;
			
			// get attribute id from preference
			String attrId = _prepareAttributeId( pv.getRefToServiceAttribute().getId() );
			// retrieve parent attributes
			while (attrId!=null) {
				// retrieve attribute
				OptimisationAttribute attr = null;
				if (cache.containsKey(attrId)) {
					attr = cache.get(attrId);
				} else {
					if (attrMgntWS==null) attrMgntWS = new eu.brokeratcloud.rest.opt.AttributeManagementService();
					logger.debug("Retrieving attribute {}", attrId);
					attr = attrMgntWS.getAttribute( attrId );
					
					// assert that attr.getId()==attrId
					if (!attrId.equals(attr.getId())) throw new RuntimeException("Attribute with id '"+attr.getId()+"' does not match the requested attribute id '"+attrId+"'");
					cache.put(attrId, attr);
				}
				// store attribute in path
				path.push(attrId);
				// get parent attribute id
				if (attr.getParent()==null) {
					attrId = null;
				} else {
					attrId = attr.getParent().getId();
					if (attrId!=null && !attrId.trim().isEmpty()) {
						attrId = _prepareAttributeId( attrId );
					} else attrId = null;
				}
			}
			// store path attributes into tree hierarchy (creating path if needed)
			HierarchyNode<OptimisationAttribute> prev, curr = root;
			while (path.size()>0) {
				String atId = path.pop();
				prev = curr;
				curr = curr.getChild(atId);
				if (curr==null) prev.addChild(curr = new HierarchyNode<OptimisationAttribute>(atId, cache.get(atId), null));
			}
			// set leaf node's weight to preference weight
			curr.setWeight( cp.getWeight() );
		}
		// sum leaf weights to calculate intermediate and root node weights (root node weight must be '1')
		_sumWeights(root);
		return root;
	}
	
	protected void _sumWeights(HierarchyNode<OptimisationAttribute> node) {
		if (node==null) return;
		if (node.getWeight()>0) return;
		double sum = 0;
		for (HierarchyNode<OptimisationAttribute> child : node.getChildren()) {
			if (child==null) continue;
			_sumWeights(child);
			sum += child.getWeight();
		}
		node.setWeight(sum);
	}
	
	// ================================================================================================================
	// Hierarchy tree pruning methods (for removing redundant nodes - OPTIONAL)
	
	public static enum PRUNE_MODE { KEEP_HIGHER, KEEP_LOWER, NONE };
	
	protected HierarchyNode<OptimisationAttribute> _pruneModel(HierarchyNode<OptimisationAttribute> node, PRUNE_MODE mode) {
		if (mode==PRUNE_MODE.NONE) return node;
		int cnt = node.getChildCount();
		if (mode==PRUNE_MODE.KEEP_HIGHER) {
			if (cnt==1) {
				HierarchyNode<OptimisationAttribute> child = node;
				while (child!=null && child.getChildCount()==1) child = child.getChildren().get(0);
				if (child==null || child.getChildCount()==0) {
					node.removeAllChildren();	// prune this branch
					return node;
				}
				// else 'getChildCount' > 1
				// copy lowest node's children to 'node' !!!
				node.removeAllChildren();
				for (HierarchyNode<OptimisationAttribute> newChild : child.getChildren()) {
					node.addChild(newChild);
				}
				_pruneModel(node, mode);	// this will effectively call the 'if (cnt>1)' branch
				return node;
			} else
			if (cnt>1) {
				for (HierarchyNode<OptimisationAttribute> child : node.getChildren()) {
					_pruneModel(child, mode);
				}
				return node;
			} else {	// cnt==0
				return node;	// leaf node : nothing to prune
			}
		} else
		if (mode==PRUNE_MODE.KEEP_LOWER) {
			if (cnt==1) {
				HierarchyNode<OptimisationAttribute> child = node.getChildren().get(0);
				HierarchyNode<OptimisationAttribute> newChild = _pruneModel(child, mode);
				newChild.setParent( node.getParent() );
				newChild.setWeight( node.getWeight() );
				return newChild;
			} else
			if (cnt>1) {
				Vector<HierarchyNode<OptimisationAttribute>> tmp = new Vector<HierarchyNode<OptimisationAttribute>>();
				for (HierarchyNode<OptimisationAttribute> child : node.getChildren()) {
					HierarchyNode<OptimisationAttribute> newChild = _pruneModel(child, mode);
					tmp.add(newChild);
				}
				node.removeAllChildren();
				for (HierarchyNode<OptimisationAttribute> newChild : tmp) {
					node.addChild(newChild);
				}
				return node;
			} else {	// cnt==0
				return node;	// leaf node : nothing to prune
			}
		} else
			throw new RuntimeException("Invalid PRUNE_MODE: "+mode);
	}
	
	// ================================================================================================================
	// Hierarchy node (ie criteria) weight calculation (AHP phase II)
	
	protected HashMap<String,HashMap<String,Double>> _prepareComparisonPairs(ConsumerPreferenceProfile profile) {
		ComparisonPair[] cpair = profile.getComparisonPairs();
		HashMap<String,HashMap<String,Double>> pairs = new HashMap<String,HashMap<String,Double>>();
		HashMap<String,Double> level2;
		for (int i=0; i<cpair.length; i++) {
			String id1 = cpair[i].getAttribute1();
			String id2 = cpair[i].getAttribute2();
			if (pairs.containsKey(id1)) {
				level2 = pairs.get(id1);
			} else {
				level2 = new HashMap<String,Double>();
				pairs.put(id1, level2);
			}
			double value = Double.parseDouble(cpair[i].getValue());
			if (value<-8) value = -8; else if (value>8) value = 8;
			if (value<0) value = -(value-1); else value = 1/(value+1);
			level2.put(id2, new Double(value));
		}
		return pairs;
	}
	
	protected void _calculateChildrenWeights(HierarchyNode<OptimisationAttribute> node, HashMap<String,HashMap<String,Double>> pairs) {
		int cnt = node.getChildCount();
		if (cnt<1) return;
		else if (cnt==1) {
			HierarchyNode<OptimisationAttribute> child = node.getChildren().get(0);
			child.setWeight(1);
			_calculateChildrenWeights(child, pairs);
			return;
		}
		// if (cnt > 1)
		
		// create relative importance matrix
		List<HierarchyNode<OptimisationAttribute>> children = node.getChildren();
		int size = children.size();
		double[][] relImp = new double[size][size];
		for (int i=0; i<size; i++) relImp[i][i] = 1;
		for (int i=0; i<size-1; i++) {
			for (int j=i+1; j<size; j++) {
				String id1 = children.get(i).getAttribute().getId();
				String id2 = children.get(j).getAttribute().getId();
				if (pairs.containsKey(id1) && pairs.get(id1).containsKey(id2)) {
					relImp[i][j] = pairs.get(id1).get(id2); 
					relImp[j][i] = 1/relImp[i][j];
				} else
				if (pairs.containsKey(id2) && pairs.get(id2).containsKey(id1)) {
					relImp[j][i] = pairs.get(id2).get(id1);
					relImp[i][j] = 1/relImp[j][i];
				} else {
					throw new RuntimeException( String.format("Missing relative importance pair for attributes (%s, %s)", id1, id2) );
				}
			}
		}
		if (logger.isTraceEnabled()) {
			StringBuffer sb = new StringBuffer();
			sb.append("--------------------------------------------------------------------------\n");
			for (int i=0; i<size; i++) {
				sb.append("[\t");
				for (int j=0; j<size; j++) { sb.append(relImp[i][j]); sb.append("\t"); }
				sb.append("]\n");
			}
			sb.append("--------------------------------------------------------------------------");
			logger.trace("Relative importance matrix for node: {}\n{}", node.getAttribute().getId(), sb.toString());
		}
		
		// calculate eigenvector
		double[] eigen1 = new double[size], eigen2 = null, tmp1;
		logger.trace("Initial eigenvector:");
		_calcEigenVector(relImp, eigen1);
		double[][] relImp2 = null, tmp2;
		do {
			if (relImp2==null) relImp2 = new double[size][size];
			if (eigen2==null) eigen2 = new double[size];
			_squareMatrix(relImp, relImp2);
			_calcEigenVector(relImp2, eigen2);
			// swap matrices and eigenvectors
			tmp1 = eigen1; eigen1 = eigen2; eigen2 = tmp1;
			tmp2 = relImp; relImp = relImp2; relImp2 = tmp2;
		} while (_areVectorsDifferent(eigen1, eigen2, 0.0001));
		
		// update children node weights
		for (int i=0; i<size; i++) children.get(i).setWeight( eigen1[i] );
		
		// recurse process to child nodes
		for (int i=0; i<size; i++) _calculateChildrenWeights(children.get(i), pairs);
	}
	
	// ================================================================================================================
	// Eigenvector calculation methods (for crisp numbers)
	
	protected void _calcEigenVector(double[][] M, double[] eigenvector) {
		int size=M.length;
		double sum = 0;
		for (int i=0; i<size; i++) {
			double rowSum = 0;
			for (int j=0; j<size; j++) rowSum += M[i][j];
			eigenvector[i] = rowSum;
			sum += rowSum;
		}
		// normalize row sums
		for (int i=0; i<size; i++) eigenvector[i] /= sum;
		
		// debug print
		if (logger.isTraceEnabled()) {
			StringBuffer sb = new StringBuffer();
			sb.append("[\t");
			for (int i=0; i<size; i++) { sb.append(eigenvector[i]); sb.append("\t"); }
			sb.append("]");
			logger.trace("Eigenvector: {}", sb.toString());
		}
	}
	
	protected void _squareMatrix(double[][] M1, double[][] M2) {
		int size = M1.length;
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				M2[i][j] = 0;
				for (int k=0; k<size; k++) M2[i][j] += M1[i][k]*M1[k][j];
			}
		}
	}
	
	protected boolean _areVectorsDifferent(double[] eigen1, double[] eigen2, double threshold) {
		int size = eigen1.length;
		for (int i=0; i<size; i++) {
			double diff = eigen1[i] - eigen2[i];
			if (diff < 0) diff = -diff;
			if (logger.isTraceEnabled()) logger.trace("\tabs({} - {}) = {}", eigen1[i], eigen2[i], diff);
			if (diff>threshold) return true;
		}
		if (logger.isTraceEnabled()) logger.trace("\teigenvectors are (almost) identical");
		return false;
	}
	
	// ================================================================================================================
	// Recommendation filtering methods
	
	//2014-11-23: Addtion
	protected RdfPersistenceManager pm;
	protected SparqlServiceClient client;
	
	protected boolean _checkIfAlreadyRecommended(String owner, String profile, AhpHelper.RankedItem item, long period, double relevanceDiffThreshold) {
		// period: 0=only active/most recent recommendation should be considered
		// relevanceDiffThreshold: used in order to recommend item even if already recommended but with lower relevance
		
		logger.trace("_checkIfAlreadyRecommended: BEGIN: item={}, period={}, threshold={}", item, period, relevanceDiffThreshold);
		try {
			if (pm==null) pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			if (client==null) client = SparqlServiceClientFactory.getClientInstance();
			
			// suggested service URI
			String sd = item.item.getId();
			
			// class and field URIs
			String recomTypeUri = pm.getClassRdfType(Recommendation.class);
			String profileUri = pm.getFieldUri(Recommendation.class, "profile");
			String creatorUri = pm.getFieldUri(Recommendation.class, "owner");
			String createdUri = pm.getFieldUri(Recommendation.class, "createTimestamp");
			String activeUri = pm.getFieldUri(Recommendation.class, "active");
			String hasItemUri = pm.getFieldUri(Recommendation.class, "items");
			String itemTypeUri = pm.getClassRdfType(RecommendationItem.class);
			String sdUri = pm.getFieldUri(RecommendationItem.class, "serviceDescription");
			String weightUri = pm.getFieldUri(RecommendationItem.class, "weight");
			String responseUri = pm.getFieldUri(RecommendationItem.class, "response");
			
			String qry =
				"SELECT ?recom ?item ?weight ?response WHERE { \n"+
				"	?recom a <" + recomTypeUri + "> . \n"+
				"	?recom <" + profileUri + "> \"" + profile + "\"^^<http://www.w3.org/2001/XMLSchema#string> . \n"+
				"	?recom <" + creatorUri + "> \"" + owner + "\"^^<http://www.w3.org/2001/XMLSchema#string> . \n"+
				"	?recom <" + createdUri + "> ?createTm . \n";
			if (period>0) qry +=
				"	FILTER ( now() - ?createTm < \"PT" + (period/1000d) + "S\"^^<http://www.w3.org/2001/XMLSchema#duration> ) . \n";
			else if (period==0) qry +=
				"	?recom <" + activeUri + "> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> . \n";
			qry +=
				"	# \n"+
				"	?recom ?hasItem ?item . \n"+
				"	filter STRSTARTS( str(?hasItem), '" + hasItemUri + ":_' ) . \n"+
				"	# \n"+
				"	?item <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + itemTypeUri + "> . \n"+
				"	?item <" + sdUri + "> \"" + sd + "\"^^<http://www.w3.org/2001/XMLSchema#string> . \n"+
				"	?item <" + weightUri + "> ?weight . \n"+
				"	?item <" + responseUri + "> ?response . \n"+
				"} \n" ;
			
			logger.trace("_checkIfAlreadyRecommended: Query=\n{}", qry);
			List<Map<String,RDFNode>> results = client.queryAndProcess(qry);
			logger.trace("_checkIfAlreadyRecommended: Results=\n{}", results);
			
			if (results!=null && results.size()>0) {
				int ii = 0;
				for (Map<String,RDFNode> soln : results) {
					ii++;
					double weight = -1;
					String response = null;
					RDFNode node = soln.get("weight");
					if (node!=null) weight = node.asLiteral().getDouble();
					node = soln.get("response");
					if (node!=null) response = node.asLiteral().getString();
					logger.trace("_checkIfAlreadyRecommended: Result #{}: weight={}, response={}", ii, weight, response);
					
					// if relevance/importance difference is more than threshold then keep (recommend) this item
					if (item.relevance - weight > relevanceDiffThreshold) continue;
					
					// item has already been recommended (during last 'period' milliseconds) and 
					// relevance difference between this item and past recommendation item is NOT over threshold
					// Therefore item can be discarded
					logger.trace("_checkIfAlreadyRecommended: Item has already been recommended: uri={}, period={}ms, relevance-diff={} < {}", sd, period, item.relevance - weight, relevanceDiffThreshold);
					return true;
				}
			}
			// item has not been recommended during last 'period' milliseconds or the relevance difference between this item
			// and the any past recommended item is OVER threshold
			logger.trace("_checkIfAlreadyRecommended: Item has not been recommended or relevance difference is over threshold: uri={}, period={}ms, item-relevance={}, threshold={}", sd, period, item.relevance, relevanceDiffThreshold);
			
		} catch (Exception e) {
			logger.error("_checkIfAlreadyRecommended: EXCEPTION THROWN: {}", e);
		}
		return false;
	}
	
	protected boolean _checkIfAlreadyRecommendedByFailurePreventionAndRecoveryComponent(AhpHelper.RankedItem item, long period, double relevanceDiffThreshold) {
		// period: 0=only active/most recent recommendation should be considered
		// relevanceDiffThreshold: used in order to recommend item even if already recommended but with lower relevance
//XXX: TODO: during integration phase
		return false;
	}
	
	protected boolean _checkIfIgnored(String owner, String profile, AhpHelper.RankedItem item, long period, int ignoresThreshold) {
		// period: time window in which ignores count. A value of '0' will effectively accept all items
		// ignoresThreshold: if ignores count (in period window) is over this threshold, item must not be recommended
//XXX: TODO: to-be defined
		return false;
	}
}
