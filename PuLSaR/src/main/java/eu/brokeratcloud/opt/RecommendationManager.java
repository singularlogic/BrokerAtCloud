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
package eu.brokeratcloud.opt;

import eu.brokeratcloud.common.RootObject;
import eu.brokeratcloud.common.ClassificationDimension;
import eu.brokeratcloud.common.ServiceDescription;
import eu.brokeratcloud.common.SLMEvent;
import eu.brokeratcloud.opt.ahp.AhpHelper;
import eu.brokeratcloud.opt.engine.EventManager;
import eu.brokeratcloud.opt.policy.*;
import eu.brokeratcloud.rest.opt.ProfileManagementService;
import eu.brokeratcloud.rest.opt.ServiceCategoryAttributeManagementService;
import eu.brokeratcloud.util.*;

import eu.brokeratcloud.persistence.DateParser;
import eu.brokeratcloud.persistence.RdfPersistenceManager;
import eu.brokeratcloud.persistence.RdfPersistenceManagerFactory;
import eu.brokeratcloud.persistence.SparqlServiceClient;
import eu.brokeratcloud.persistence.SparqlServiceClientFactory;

import java.net.URLEncoder;
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
import javax.xml.bind.DatatypeConverter;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.RDFNode;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;

public class RecommendationManager extends RootObject {
	private static final Logger logger = LoggerFactory.getLogger("eu.brokeratcloud.opt.RecommendationManager");
	
	// ================================================================================================================
	// Constructors and factory methods
	
	protected static Properties defaultSettings;
	protected static HashMap<String,Vector<String>> servicesBlacklist;
	protected static HashMap<String,Vector<String>> servicesWhitelist;
	protected Properties settings;
	protected int threadPoolSize = -1;
	
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
			_initFromFile("/recommendations-manager.properties", defaultSettings);
			settings = new Properties(defaultSettings);
		}
	}
	
	protected RecommendationManager(String propertiesFile) {
		this();
		logger.debug("RecommendationManager.<init> : when = {}", new Date());
		settings = new Properties(defaultSettings);
		_initFromFile(propertiesFile, settings);
	}
	
	protected RecommendationManager(Properties props) {
		this();
		logger.debug("RecommendationManager.<init> : when = {}", new Date());
		settings = new Properties(defaultSettings);
		settings.putAll(props);
	}
	
	// ================================================================================================================
	// Thread Pool methods
	public int getThreadPoolSize() {
		return threadPoolSize<1 ? 1 : threadPoolSize;
	}
	
	public void setThreadPoolSize(int n) {
		if (n<=0) throw new IllegalArgumentException("setThreadPoolSize: Invalid 'thread pool size': "+n);
		threadPoolSize = n; 
		logger.debug("Thread Pool size set to: {}", n);
	}
	
	// ================================================================================================================
	// Initialization helper methods
	
	protected Properties _createDefaultSettings() {
		Properties p = new Properties();
		// general settings
		p.setProperty("serviceDescriptionRetrievalUrl", "");
		p.setProperty("serviceDescriptionRetrievalWithProgrammaticCall", "true");
		p.setProperty("pruneMode", "NONE");
		p.setProperty("ahpImplementation", "");
		p.setProperty("updateConsumerPreferenceProfile", "false");
		p.setProperty("alwaysGenerateRecommendation", "false");		// don't generate a new recommendation if there are no items/services to suggest
		p.setProperty("alwaysCheckRecoms", "false");
		p.setProperty("dontStoreRecommendation", "true");
		p.setProperty("recommendationsTopic", "");
		// filtering settings
		p.setProperty("periodSinceLastRecom", "0");
		p.setProperty("relevanceThreshold", "0");
		p.setProperty("periodOfIgnores", "0");
		p.setProperty("ignoresThreshold", "0");
		p.setProperty("fprcQueryWsUrlTemplate", "");
		p.setProperty("fprRecomUsersWildcard", "");
		p.setProperty("periodSinceLastRecomOfFRPC", "0");
		p.setProperty("relevanceThresholdForFRPC", "0");
		// services blacklist/whitelist
		p.setProperty("thread-pool-size", "1");
		// services blacklist/whitelist
		p.setProperty("services-blacklist-file", "");
		p.setProperty("services-whitelist-file", "");
		return p;
	}
	
	protected void _initFromFile(String file, Properties settings) {
		Properties p = _loadSettings(file);
		if (p!=null) settings.putAll(p);
	}
	
	protected Properties _loadSettings(String file) {
		logger.debug("Reading default properties from file: {}...", file);
		Properties p = eu.brokeratcloud.util.Config.getConfig(file);
		if (p==null) {
			logger.debug("Reading default properties from file: {}... Not found", file);
			return null;
		}
		logger.debug("Reading default properties from file: {}... done", file);
		return p;
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
			ServiceCategoryAttributeManagementService.PolicyObjects po = ServiceCategoryAttributeManagementService.getBrokerPolicyObjects(pvUri, false);
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
			ServiceCategoryAttributeManagementService.PolicyObjects po = ServiceCategoryAttributeManagementService.getBrokerPolicyObjects(pvUri, false);
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
			ServiceCategoryAttributeManagementService.PolicyObjects po = ServiceCategoryAttributeManagementService.getBrokerPolicyObjects(pvUri, false);
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
		int mtm;
		Stats.get().startSplit( mtm = Stats.get().getOrCreateSplitByName("RM.requestRecom: OVERALL-TIMER") );
		try{
			
			String sdId = evt.getProperty("service-description");
			if (sdId==null || (sdId=sdId.trim()).isEmpty()) {
				logger.error("SLM Event does not contain a valid service description identifier");
				return null;
			}
			eu.brokeratcloud.rest.opt.AuxiliaryService sgqcWs = new eu.brokeratcloud.rest.opt.AuxiliaryService();
			try {
				logger.debug("Applying active broker policy defaults to persistence framework");
				HashMap<String,String> hm=sgqcWs.applyBrokerPolicy("#");
				logger.debug("Defaults applied to persistence framework: status={}", hm.get("status"));
				logger.debug("Active broker policy: {}", hm.get("bp-uri"));
			} catch (Exception e) {
				logger.error("Failed to apply active broker policy defaults to persistence framework: Reason: {}", e);
				return null;
			}
			
			logger.debug("Retrieving service description: {}", sdId);
			ServiceDescription sd = sgqcWs.getServiceDescription(null, sdId);
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
			int spl1 = Stats.get().getOrCreateSplitByName("RM.requestRecom: profiles In CATEGORIES");
			for (String catId : categoryId.split("[,]")) {
				catId = catId.trim();
				if (catId.isEmpty()) continue;
				logger.debug("Retrieving profiles in category: {}", catId);
				Stats.get().startSplit(spl1);
				ConsumerPreferenceProfile[] list = profileMgntWs.getProfilesInCategory(catId);
				Stats.get().endSplit(spl1);
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
				int nWorkers = -1;
				if (getThreadPoolSize()<=0) {
					try {
						String s = settings.getProperty("thread-pool-size", null);
						logger.info("Thread pool size setting: {}", s);
						if (s!=null && !s.trim().isEmpty()) nWorkers = Integer.parseInt( s ); 
					} catch (Exception e) {
						logger.error("Invalid 'thread-pool-size' setting: {}. Exception: {}", p.getProperty("thread-pool-size"), e);
					}
				} else {
					nWorkers = getThreadPoolSize();
					logger.info("Thread pool size has been set using 'setThreadPoolSize': {}", nWorkers);
				}
				if (nWorkers<=0) nWorkers = 1;
				logger.trace("Workers:-A-:  nWorkers={}", nWorkers);
				_initThreadPool( nWorkers );
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
						logger.trace("Workers:-B-:  NEW WORKER");
						_startWorkerThread(ownerId, profileId, p, false, newRecoms);
					} catch (Exception e) {
						logger.error("requestRecommendations: Input: consumer={}, profile={}. Exception caught:\n{}", ownerId, profileId, e);
						return null;
					}
				}
				_waitWorkerThreadsToComplete(profilesList.length);
				logger.trace("Workers:-C-:  WORKERS DONE");
			}
			return newRecoms;
			
		} finally {
			Stats.get().endSplit( mtm );
			
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			java.io.PrintStream ps = new java.io.PrintStream(baos);
			Stats.get().printAll(ps);
			logger.debug("{}", baos.toString());
		}
	}
	
	// Worker threads implementation - BEGIN
	protected java.util.concurrent.ExecutorService executor;
	
	protected static long totalThreadDuration;
	protected static long minThreadDuration;
	protected static long maxThreadDuration;
	protected static long threadsStarted;
	protected static long threadsCompleted;
	protected static long threadsFailed;
	protected static long threadsActive;
	
	public static synchronized void initWorkerThreadStats() {
		totalThreadDuration = 0;
		minThreadDuration = Long.MAX_VALUE;
		maxThreadDuration = 0;
		threadsStarted = 0;
		threadsCompleted = 0;
		threadsFailed = 0;
		threadsActive = 0;
	}
	
	static {
		initWorkerThreadStats();
	}
	
	public static synchronized long[] getWorkerThreadStats() {
		long[] stat = new long[8];
		stat[0] = totalThreadDuration;
		stat[1] = minThreadDuration;
		stat[2] = maxThreadDuration;
		stat[3] = threadsStarted;
		stat[4] = threadsCompleted;
		stat[5] = threadsFailed;
		stat[6] = threadsActive;
		stat[7] = (threadsCompleted>0) ? totalThreadDuration/threadsCompleted : -1;
		return stat;
	}
	
	public static String getWorkerThreadStatsToString() {
		long[] stat = getWorkerThreadStats();
		return String.format( "Thread-Stats: total-dur=%d, min-dur=%d, max-dur=%d, avg-dur=%f, started=%d, completed=%d, fails=%d, active=%d",
					stat[0], stat[1], stat[2], stat[7], stat[3], stat[4], stat[5], stat[6] );
					
	}
	
	protected static class WorkerThread implements Runnable {
		private RecommendationManager manager;
		private String consumerId;
		private String profileId;
		private Properties settings;
		private boolean forceCreation;
		private HashMap<String,Recommendation> newRecoms;
		
		public WorkerThread(RecommendationManager manager, String consumerId, String profileId, Properties settings, boolean forceCreation, HashMap<String,Recommendation> newRecoms){
			this.manager = manager;
			this.consumerId = consumerId;
			this.profileId = profileId;
			this.settings = settings;
			this.forceCreation = forceCreation;
			this.newRecoms = newRecoms;
		}
		
		@Override
		public void run() {
			logger.trace("Workers:-D-: RecommendationManager.WorkerThread: START: thread="+Thread.currentThread().getName()+", consumer="+consumerId+", profile="+profileId+", ...");
			RecommendationManager.logger.debug("RecommendationManager.WorkerThread: START: thread={}, consumer={}, profile={}, settings={}, force-create={}, new-recoms={}", Thread.currentThread().getName(), consumerId, profileId, settings, forceCreation, newRecoms);
			long startTm = 0;
			long duration = 0;
			boolean failed = false;
			int spl = Stats.get().getOrCreateSplitByName("RM: WORKER.run()");
			try {
				synchronized (manager) { threadsStarted++; threadsActive++; }
				startTm = System.nanoTime();
				Stats.get().startSplit(spl);
				Recommendation recom = manager._createNewRecommendation(consumerId, profileId, settings, forceCreation);		// false: don't force recom. creation
				Stats.get().endSplit(spl);
				synchronized (newRecoms) {
					newRecoms.put(profileId, recom);
				}
				duration = System.nanoTime() - startTm;
				RecommendationManager.logger.debug("RecommendationManager.WorkerThread: COMPLETED: thread={}, consumer={}, profile={}, recom={}", Thread.currentThread().getName(), consumerId, profileId, recom);
				logger.trace("Workers:-E-:  RecommendationManager.WorkerThread: COMPLETED: thread="+Thread.currentThread().getName()+", consumer="+consumerId+", profile="+profileId+", recom="+recom);
			} catch (Exception e) {
				duration = System.nanoTime() - startTm;
				failed = true;
				logger.error("RecommendationManager.WorkerThread: EXCEPTION: thread={}, consumer={}, profile={}, exception={}", Thread.currentThread().getName(), consumerId, profileId, e);
				logger.trace("Workers:-EXCEPTION-:  RecommendationManager.WorkerThread: EXCEPTION: thread="+Thread.currentThread().getName()+", consumer="+consumerId+", profile="+profileId+", exception={}", e);
			} finally {
				logger.trace("Workers:-F-:  RecommendationManager.WorkerThread: Duration: thread="+Thread.currentThread().getName()+", consumer="+consumerId+", profile="+profileId+", duration="+duration);
				synchronized (manager) {
					totalThreadDuration += duration;
					if (duration<minThreadDuration) minThreadDuration = duration;
					if (duration>maxThreadDuration) maxThreadDuration = duration;
					threadsCompleted++;
					if (failed) threadsFailed++;
					threadsActive--; 
				}
			}
		}
	}
	
	protected void _initThreadPool(int threadPoolSize) {
		logger.debug("_initThreadPool: BEGIN: thread-pool-size: {}", threadPoolSize);
		if (executor==null) {
			if (threadPoolSize>1) {
				executor = java.util.concurrent.Executors.newFixedThreadPool(threadPoolSize);
				logger.debug("_initThreadPool: Executor service initialized");
			} else {
				logger.debug("_initThreadPool: Single-thread execution: Executor service NOT initialized");
			}
		} else {
			logger.error("_initThreadPool: Executor Service is already initialized");
		}
		logger.debug("_initThreadPool: END");
	}
	
	protected void _startWorkerThread(String consumerId, String profileId, Properties settings, boolean forceCreation, HashMap<String,Recommendation> newRecoms) {
		logger.debug("_startWorkerThread: BEGIN: consumer={}, profile={}, settings={}, force-create={}, new-recoms={}", consumerId, profileId, settings, forceCreation, newRecoms);
		Runnable worker = new WorkerThread(this, consumerId, profileId, settings, forceCreation, newRecoms);
		if (executor!=null) {
			logger.debug("_startWorkerThread: Executor Service initialized. Using it to assign recommendation creation to a worker thread");
			executor.execute(worker);
		} else {
			logger.debug("_startWorkerThread: Executor Service NOT found. Assuming single threaded execution");
			worker.run();
		}
		logger.debug("_startWorkerThread: END: Task assigned to worker thread");
	}
	
	protected void _waitWorkerThreadsToComplete(int howmany) {
		logger.debug("_waitWorkerThreadsToComplete: BEGIN: Waiting worker threads to complete");
		boolean keepChecking = true;
		while (keepChecking) {
			try { Thread.sleep(500); } catch (InterruptedException e) {}
			synchronized (this) {
				//if (threadsCompleted < howmany) keepChecking = false;
				if (threadsActive==0) keepChecking = false;
			}
		}
		// Shutdown executor
		if (executor!=null) {
			executor.shutdown();
			while (!executor.isTerminated()) {
				try { Thread.sleep(500); } catch (InterruptedException e) {}
			}
		}
		logger.debug("_waitWorkerThreadsToComplete: END: Worker threads completed");
	}
	// Worker threads implementation - END
	
	// Event Manager methods
	protected EventManager eventManager;
	//
	public EventManager getEventManager() { return eventManager; }
	public void setEventManager(EventManager em) { eventManager = em; }
	
	public Recommendation createNewRecommendation(String consumerId, String profileId) {
		return createNewRecommendation(consumerId, profileId, false);
	}
	
	public Recommendation createNewRecommendation(String consumerId, String profileId, boolean forceCreation) {
		logger.info("RecommendationManager.createNewRecommendation() invoked : when = {}: profile = {}", new Date(), profileId);
		try {
			return _createNewRecommendation(consumerId, profileId, settings, forceCreation);
		} catch (Exception e) {
			logger.error("createNewRecommendation: EXCEPTION: consumer={}, profile={}", consumerId, profileId);
			logger.error("Exception caught: {}", e);
			return null;
		}
	}
	
	protected Recommendation _createNewRecommendation(String consumerId, String profileId, Properties settings, boolean forceCreation) throws java.io.IOException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, InstantiationException, java.lang.reflect.InvocationTargetException {
		eu.brokeratcloud.rest.opt.AuxiliaryService sgqcWS = new eu.brokeratcloud.rest.opt.AuxiliaryService();
		try {
			logger.debug("Re-Applying active broker policy defaults to persistence framework");
			HashMap<String,String> hm=sgqcWS.applyBrokerPolicy("#");
			logger.debug("Defaults applied to persistence framework: status={}", hm.get("status"));
			logger.debug("Active broker policy: {}", hm.get("bp-uri"));
		} catch (Exception e) {
			logger.error("Failed to apply active broker policy defaults to persistence framework: Reason: {}", e);
			return null;
		}
		
		Properties prop = new Properties(defaultSettings);		// or this.settings
		if (settings!=null) prop.putAll(settings);
		// General settings
		String sdWsUrlTemplate = prop.getProperty("serviceDescriptionRetrievalUrl");
		boolean sgqcProgrammaticCall = Boolean.valueOf( prop.getProperty("serviceDescriptionRetrievalWithProgrammaticCall", "false") );
		PRUNE_MODE pruneMode = PRUNE_MODE.valueOf( prop.getProperty("pruneMode", "NONE") );
		String ahpImplementation = prop.getProperty("ahpImplementation", "");
		boolean updateProfile = Boolean.valueOf( prop.getProperty("updateConsumerPreferenceProfile") );
		boolean alwaysGenerateRecom = Boolean.valueOf( prop.getProperty("alwaysGenerateRecommendation", "false") );		// don't generate a new recommendation if there are no items/services to suggest
		boolean alwaysCheckRecoms = Boolean.valueOf( prop.getProperty("alwaysCheckRecoms") );
		boolean dontStoreRecom = Boolean.valueOf( prop.getProperty("dontStoreRecommendation") );
		String recommendationsTopic = prop.getProperty("recommendationsTopic", "").trim();
		String recomItemSuggestion = prop.getProperty("recomItemSuggestion", "");
		// Filtering settings
		long periodOfLastRecom = Long.parseLong( prop.getProperty("periodSinceLastRecom", "0") );
		double relevanceThreshold = Long.parseLong( prop.getProperty("relevanceThreshold", "0") );
		long periodOfIgnores = Long.parseLong( prop.getProperty("periodOfIgnores", "0") );
		int ignoresThreshold = (int)Long.parseLong( prop.getProperty("ignoresThreshold", "0") );
		String fprcQueryWsUrlTemplate = prop.getProperty("fprcQueryWsUrlTemplate", "");
		String fprRecomUsersWildcard = prop.getProperty("fprRecomUsersWildcard", "");
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
			logger.debug("ahpImplementation: {}", prop.getProperty("ahpImplementation"));
			logger.debug("updateConsumerPreferenceProfile: {}", prop.getProperty("updateConsumerPreferenceProfile"));
			logger.debug("alwaysGenerateRecommendation: {}", prop.getProperty("alwaysGenerateRecommendation"));
			logger.debug("alwaysCheckRecoms: {}", prop.getProperty("alwaysCheckRecoms"));
			logger.debug("dontStoreRecommendation: {}", prop.getProperty("dontStoreRecommendation"));
			logger.debug("recommendationsTopic: {}", prop.getProperty("recommendationsTopic"));
			logger.debug("recomItemSuggestion: {}", prop.getProperty("recomItemSuggestion"));
			logger.debug("thread-pool-size: {}", prop.getProperty("thread-pool-size"));
			// filtering settings
			logger.debug("FILTERING SETTINGS:");
			logger.debug("periodSinceLastRecom: {}", prop.getProperty("periodSinceLastRecom"));
			logger.debug("relevanceThreshold: {}", prop.getProperty("relevanceThreshold"));
			logger.debug("fprcQueryWsUrlTemplate: {}", prop.getProperty("fprcQueryWsUrlTemplate"));
			logger.debug("fprRecomUsersWildcard: {}", prop.getProperty("fprRecomUsersWildcard"));
			logger.debug("periodOfIgnores: {}", prop.getProperty("periodOfIgnores"));
			logger.debug("ignoresThreshold: {}", prop.getProperty("ignoresThreshold"));
			logger.debug("services-blacklist-file: {}", prop.getProperty("services-blacklist-file"));
			logger.debug("services-whitelist-file: {}", prop.getProperty("services-whitelist-file"));
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
			if (pref==null) { logger.error("Profile {} contains 'null' preference(s): pref #{}", profileId, i); hasErrors = true; continue; }
			String prefId = pref.getId();
			String prefAttrId = _getServiceAttributeId(pref);
			if (prefId==null || prefId.trim().isEmpty()) { logger.error("Profile {} contains preference(s) with no Id: ConsumerPreference #{}\n{}", profileId, i, pref); preferences[i] = null; hasErrors = true; continue; }
			if (prefAttrId==null || prefAttrId.trim().isEmpty()) { logger.error("Profile {} contains preference(s) with no attribute id: ConsumerPreference #{}\n{}", profileId, i, pref); preferences[i] = null; hasErrors = true; continue; }
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
			logger.info("Retrieving service descriptions for category {}", categoryId);
			//eu.brokeratcloud.rest.opt.AuxiliaryService sgqcWS = new eu.brokeratcloud.rest.opt.AuxiliaryService();
			int spl3 = Stats.get().getOrCreateSplitByName("RM._createNewRecom: getServiceDescriptionsForCategories");
			Stats.get().startSplit(spl3);
			SDs = sgqcWS.getServiceDescriptionsForCategories( categoryId );
			Stats.get().endSplit(spl3);
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
			// Don't terminate recommendation when SDs are missing service name. Instead set name to service URI and issue a warning
			//if (sdName==null || sdName.trim().isEmpty()) { logger.error("Service category {} contains service description(s) with no Name", categoryId); hasErrors = true; continue; }
			if (sdName==null || sdName.trim().isEmpty()) {
				logger.warn("Service description with no Name. Setting name to Id: {}", sd.getId());
				sd.setName( sd.getId() );
			}
			logger.debug("\tService: uri={}, name={}", sdId, sdName);
		}
		if (hasErrors) {
			logger.error("Service Descriptions list for category '{}' contains errors. No recommendations will be generated", categoryId);
			return null;
		}
		
		// filter out services by checking preference constraints
		logger.info("Checking service descriptions against preference constraints...");
		int spl4 = Stats.get().getOrCreateSplitByName("RM._createNewRecom: FOR-LOOP... preferences");
		Stats.get().startSplit(spl4);
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
		Stats.get().endSplit(spl4);
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
				Stats.get().startSplit("RM._createNewRecom: updateProfile");
				int status = profileMgntWs.updateProfile(consumerId, profileId, profile).getStatus();
				Stats.get().endSplit();
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
		AhpHelper helper = AhpHelper.getInstance(ahpImplementation);
		int spl6 = Stats.get().getOrCreateSplitByName("RM._createNewRecom: helper.rank");
		Stats.get().startSplit(spl6);
		List<AhpHelper.RankedItem> rankedSDs = helper.rank(this, topLevelGoal, criteria, SDs);
		Stats.get().endSplit(spl6);
		logger.debug("Ranked list of service descriptions:\n{}", rankedSDs);
		
		// generate recom-id base and timestamp
		Date createTm = new Date();
		String recomId = UUID.randomUUID().toString();
		
		// filter out services
		logger.info("Post-ranking service filtering...");
		List<RecommendationItem> recomItems = new ArrayList<RecommendationItem>();
		double sumRel = 0;
		int cntItems = 0;
		int spl7 = Stats.get().getOrCreateSplitByName("RM._createNewRecom: FILTER-OUT-rankedSDs");
		Stats.get().startSplit(spl7);
		for (AhpHelper.RankedItem item : rankedSDs) {
			// Check if service relevance is zero or negative or NaN (ie AhpHelper failed to calculate it; possible causes are missing weights or division by zero)
			if (Double.isNaN( item.relevance ) || item.relevance<=0) {
				String sdId = item.item.getId();
				logger.warn("Service was filtered-out: service-id={}  Reason: service relevance is NaN", sdId);
				continue;
			}
			
			// Check if service is whitelisted (then don't run the next checks)
			boolean rv0 = _checkIfWhitelisted(consumerId, profileId, item, -1, -1);
			
			// Check if service was previously recommended, or marked as ignored, or has already been recommended by Failure Prevention and Recovery component of Broker@Cloud platform
			boolean rv1 = false;
			boolean rv2 = false;
			boolean rv3 = false;
			if (rv0==false) {
				if (!forceCreation || alwaysCheckRecoms) {
					rv1 = _checkIfAlreadyRecommended(consumerId, profileId, item, periodOfLastRecom, relevanceThreshold);
					rv2 = _checkIfIgnored(consumerId, profileId, item, periodOfIgnores, ignoresThreshold);
					rv3 = _checkIfAlreadyRecommendedByFailurePreventionAndRecoveryComponent(consumerId, item, fprcQueryWsUrlTemplate, fprRecomUsersWildcard, periodOfLastRecomOfFPRC, relevanceThresholdForFRPC);
				}
			} else {
				String sdId = item.item.getId();
				logger.debug("Service is whitelisted: service-id={}", sdId);
			}
			if (rv1 || rv2 || rv3) {
				String sdId = item.item.getId();
				logger.warn("Service was filtered-out: service-id={}  Reason: already recommended={}, ignored={}, recommended by Failure Prevention & Recovery Component={}", sdId, rv1, rv2, rv3);
			} else {
				RecommendationItem rit = new RecommendationItem();
				rit.setId( "RECOM-ITEM-"+recomId+"-"+(cntItems++) );
				rit.setCreateTimestamp( createTm );
				rit.setOwner( consumerId );
				rit.setServiceDescription( item.item.getId() );
				rit.setWeight( item.relevance );
				String srvUri = item.item.getId();		// contains URI
				int pa = srvUri.lastIndexOf("#"), pb = srvUri.lastIndexOf("/"); pa = (pa>pb) ? pa : pb; 
				String srvId = (pa>-1 && pa+1<srvUri.length()) ? srvUri.substring(pa+1) : srvUri;
				//
				String smUri = ""+item.item.getServiceModelUri();
				int p1 = smUri.lastIndexOf("#"), p2 = smUri.lastIndexOf("/"); p1 = (p1>p2) ? p1 : p2; 
				String smName = (p1>-1 && p1+1<smUri.length()) ? smUri.substring(p1+1) : smUri;
				//
				rit.setSuggestion( recomItemSuggestion
										.replace( "{{SERVICE-ID}}", srvId )
										.replace( "{{SERVICE-URI}}", item.item.getId() )
										.replace( "{{SERVICE-NAME}}", item.item.getName() )
										.replace( "{{SERVICE-OWNER}}", item.item.getOwner() )
										.replace( "{{SERVICE-DESCRIPTION}}", item.item.getDescription() )
										.replace( "{{SERVICE-LEVEL-PROFILE-ID}}", ""+item.item.getServiceAttributeValue(".SERVICE-LEVEL-PROFILE-ID") )
										.replace( "{{SERVICE-LEVEL-PROFILE-URI}}", ""+item.item.getServiceAttributeValue(".SERVICE-LEVEL-PROFILE-URI") )
										.replace( "{{SERVICE-MODEL-URI}}", smUri )
										.replace( "{{SERVICE-MODEL-NAME}}", smName )
										//URL-encoded versions of variables
										.replace( "{{URLENC-SERVICE-ID}}", URLEncoder.encode( srvId, "UTF-8" ) )
										.replace( "{{URLENC-SERVICE-URI}}", URLEncoder.encode( item.item.getId(), "UTF-8" ) )
										.replace( "{{URLENC-SERVICE-NAME}}", URLEncoder.encode( item.item.getName(), "UTF-8" ) )
										.replace( "{{URLENC-SERVICE-OWNER}}", URLEncoder.encode( item.item.getOwner(), "UTF-8" ) )
										.replace( "{{URLENC-SERVICE-DESCRIPTION}}", URLEncoder.encode( item.item.getDescription(), "UTF-8" ) )
										.replace( "{{URLENC-SERVICE-LEVEL-PROFILE-ID}}", URLEncoder.encode( ""+item.item.getServiceAttributeValue(".SERVICE-LEVEL-PROFILE-ID"), "UTF-8" ) )
										.replace( "{{URLENC-SERVICE-LEVEL-PROFILE-URI}}", URLEncoder.encode( ""+item.item.getServiceAttributeValue(".SERVICE-LEVEL-PROFILE-URI"), "UTF-8" ) )
										.replace( "{{URLENC-SERVICE-MODEL-URI}}", URLEncoder.encode( smUri, "UTF-8" ) )
										.replace( "{{URLENC-SERVICE-MODEL-NAME}}", URLEncoder.encode( smName, "UTF-8" ) )
										//Base64-encoded versions of variables
										.replace( "{{BASE64-SERVICE-ID}}", DatatypeConverter.printBase64Binary( srvId.getBytes() ) )
										.replace( "{{BASE64-SERVICE-URI}}", DatatypeConverter.printBase64Binary( item.item.getId().getBytes() ) )
										.replace( "{{BASE64-SERVICE-NAME}}", DatatypeConverter.printBase64Binary( item.item.getName().getBytes() ) )
										.replace( "{{BASE64-SERVICE-OWNER}}", DatatypeConverter.printBase64Binary( item.item.getOwner().getBytes() ) )
										.replace( "{{BASE64-SERVICE-DESCRIPTION}}", DatatypeConverter.printBase64Binary( item.item.getDescription().getBytes() ) )
										.replace( "{{BASE64-SERVICE-LEVEL-PROFILE-ID}}", DatatypeConverter.printBase64Binary( (""+item.item.getServiceAttributeValue(".SERVICE-LEVEL-PROFILE-ID")).getBytes() ) )
										.replace( "{{BASE64-SERVICE-LEVEL-PROFILE-URI}}", DatatypeConverter.printBase64Binary( (""+item.item.getServiceAttributeValue(".SERVICE-LEVEL-PROFILE-URI")).getBytes() ) )
										.replace( "{{BASE64-SERVICE-MODEL-URI}}", DatatypeConverter.printBase64Binary( smUri.getBytes() ) )
										.replace( "{{BASE64-SERVICE-MODEL-NAME}}", DatatypeConverter.printBase64Binary( smName.getBytes() ) )
								);
				
				Map<String,Object> attrs = item.item.getServiceAttributes();
				if (attrs!=null) {
					String[][] attrsArr = new String[attrs.size()][2];
					int i=0;
					for (String at : attrs.keySet()) {
						Object val = attrs.get(at);
						if (at.indexOf('#')>0) at = at.substring(at.indexOf('#'));
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
		if (sumRel==0) sumRel = 1;	// to avoid NaN due to 0/0 division
		Stats.get().endSplit(spl7);
		// Re-calculate relevances
		for (RecommendationItem item : recomItems) {
			item.setWeight( item.getWeight() / sumRel );
		}
		
		// Apply selection policy
		int spl8 = Stats.get().getOrCreateSplitByName("RM._createNewRecom: APPLY-SEL-POLICY");
		Stats.get().startSplit(spl8);
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
						if (sumRel==0) sumRel = 1;	// to avoid NaN due to 0/0 division
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
		Stats.get().endSplit(spl8);
		
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
			Stats.get().startSplit("RM._createNewRecom: clearProfileRecommendations");
			recomMgntWS.clearProfileRecommendations(owner, profileId);
			Stats.get().endSplit();
			
			// save new recommendation
			newRecom.setActive(true);
			int spl9 = Stats.get().getOrCreateSplitByName("RM._createNewRecom: createRecommendation");
			Stats.get().startSplit(spl9);
			int status = recomMgntWS.createRecommendation(newRecom).getStatus();
			Stats.get().endSplit(spl9);
			logger.info("New recommendation for profile '{}' stored in persistent store: status = {}", profileId, status);
			
			// publish new recommendation (if an event topic has been specified)
			if (eventManager!=null && recommendationsTopic!=null && !recommendationsTopic.isEmpty()) {
				try {
					// get new recommendation URI
					if (pm==null) pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
					Stats.get().startSplit("RM._createNewRecom: getObjectUri");
					String recomUri = pm.getObjectUri(newRecom.getId(), Recommendation.class);
					Stats.get().endSplit();
					logger.trace("New recommendation URI: {}", recomUri);
					
					// prepare recom. event
					Properties p = new Properties();
					p.setProperty(".EVENT-CONTENT", String.format("{ \"recommendationId\" : \"%s\" }", recomUri));
					SLMEvent recomEvent = new SLMEvent(UUID.randomUUID().toString(), new Date(), recommendationsTopic, "-", p);
					logger.debug("Recom. event to publish: {}", recomEvent);
					
					// publish recommendation event
					Stats.get().startSplit("RM._createNewRecom: publish");
					boolean pubStatus = eventManager.publish(recomEvent);
					Stats.get().endSplit();
					logger.info("New recommendation for profile '{}' published in topic '{}': status = {}", profileId, recommendationsTopic, pubStatus?"SUCCESS":"FAIL");
				} catch (Exception e) {
					logger.error("Event publishing... CAUSED EXCEPTION: {}", e);
				}
			}
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
	
	protected HashMap<String,String[]> fprRecomCache;
	
	protected boolean _checkIfAlreadyRecommendedByFailurePreventionAndRecoveryComponent(String owner, AhpHelper.RankedItem item, String fprcQueryWsUrlTemplate, String fprRecomUsersWildcard, long period, double relevanceDiffThreshold) {
		// period: 0=only active/most recent recommendation should be considered
		// relevanceDiffThreshold: used in order to recommend item even if already recommended but with lower relevance (feature not supported by FPR)
		logger.trace("_checkIfAlreadyRecommendedByFailurePreventionAndRecoveryComponent: BEGIN: item={}, period={}, threshold={}", item, period, relevanceDiffThreshold);
		
		// check if an FPR query URL is available (from configuration)
		if (fprcQueryWsUrlTemplate==null || fprcQueryWsUrlTemplate.trim().isEmpty()) return false;
		
		// query FPR if have already recommended service
		try {
			if (pm==null) pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			if (client==null) client = SparqlServiceClientFactory.getClientInstance();
			
			// service URI
			String sdUri = item.item.getId();
			String sdEnc = URLEncoder.encode(sdUri, java.nio.charset.StandardCharsets.UTF_8.toString());
			
			// prepare threshold timestamp
			if (period<0) period = 0;
			Date dt = new Date( System.currentTimeMillis() - period );
			String dtStr = DateParser.formatW3CDateTime(dt);
			
			// check if an FPR recommendation has already been requested and cached
			if (fprRecomCache==null) fprRecomCache = new HashMap<String,String[]>();
			String[] users = fprRecomCache.get(sdUri);
			if (users!=null) {
				if (_findUser(owner, users, fprRecomUsersWildcard)) {
					// owner found in cached recommended users list from FPR
					// check if cached data has expired
					String[] tmStr = fprRecomCache.get(".timestamp");
					if (tmStr!=null && tmStr.length>0 && tmStr[0]!=null) {
						try {
							Date dt2 = DateParser.parseW3CDateTime(tmStr[0].trim());
							if (dt2.getTime() > dt.getTime()) return true;		// owner in the cached, non-expired recommended users list (for service)
						} catch (Exception e) {}
					}
				}
			}
			
			// prepare FPR query URL
			String fprWsUrl = String.format(fprcQueryWsUrlTemplate, sdEnc, dtStr);
			
			// query FPR
			logger.trace("_checkIfAlreadyRecommendedByFailurePreventionAndRecoveryComponent: Querying FPR for previous recommendations for service: {}, in period: {}, from url: {}", sdUri, period, fprWsUrl);
			long callStartTm = System.currentTimeMillis();
			FprRecommendation fprRecom = (FprRecommendation)_callBrokerRestWS(fprWsUrl, "GET", FprRecommendation.class, null);
			long callEndTm = System.currentTimeMillis();
			
			// log FPR response
			logger.trace("_checkIfAlreadyRecommendedByFailurePreventionAndRecoveryComponent: FPR replied: #items={}", fprRecom.getRecommended().size());
			logger.trace("_checkIfAlreadyRecommendedByFailurePreventionAndRecoveryComponent: Users in FPR recommendation: {}", fprRecom.getRecommended());
			
			// cache FPR response for future use
			String[] fprRecomToUsers = fprRecom.getRecommended().toArray(new String[0]);
			synchronized (fprRecomCache) {
				String[] tmp = new String[1];
				tmp[0] = DateParser.formatW3CDateTime(new Date());
				fprRecomCache.put(".timestamp", tmp);
				fprRecomCache.put(sdUri, fprRecomToUsers);
			}
			
			// check if the specified 'owner' is in user list returned by FPR
			boolean found = _findUser(owner, fprRecomToUsers, fprRecomUsersWildcard);
			logger.trace("_checkIfAlreadyRecommendedByFailurePreventionAndRecoveryComponent: END: service={}, period={}, result={}", sdUri, period, found);
			return found;
			
		} catch (Exception e) {
			logger.error("_checkIfAlreadyRecommendedByFailurePreventionAndRecoveryComponent: EXCEPTION THROWN: {}", e);
		}
		return false;
	}
	
	protected boolean _findUser(String owner, String[] users, String wildcard) {
		if (users!=null) {
			for (int i=0; i<users.length; i++) {
				//logger.trace("_findUser: checking if owner:{} = {}...", owner, users[i]);
				String u = users[i].trim();
				if (u.equals(owner)) return true;
				if (wildcard!=null && u.equalsIgnoreCase(wildcard)) return true;
			}
		}
		//logger.trace("_findUser: owner {} is not in the recommended users list returned by FPR", owner);
		return false;
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	protected static class FprRecommendation {
		@XmlAttribute
		protected Collection<String> recommended;
		//
		public Collection<String> getRecommended() { return recommended; }
		public void setRecommended(Collection<String> c) { recommended = c; }
	}
	
	protected boolean _checkIfIgnored(String owner, String profile, AhpHelper.RankedItem item, long period, int ignoresThreshold) {
		// period: time window in which ignores count. A value of '0' will effectively accept all items
		// ignoresThreshold: if ignores count (in period window) is over this threshold, item must not be recommended
		return _checkIfBlacklisted(owner, profile, item, period, ignoresThreshold);
	}
	
	protected boolean _checkIfBlacklisted(String owner, String profile, AhpHelper.RankedItem item, long period, int ignoresThreshold) {
		if (servicesBlacklist==null) servicesBlacklist = _initList("blacklist", servicesBlacklist);
		return _checkIfListed(owner, item.item.getId(), "blacklist", servicesBlacklist);
	}
	
	protected boolean _checkIfWhitelisted(String owner, String profile, AhpHelper.RankedItem item, long period, int ignoresThreshold) {
		if (servicesWhitelist==null) servicesWhitelist = _initList("whitelist", servicesWhitelist);
		return _checkIfListed(owner, item.item.getId(), "whitelist", servicesWhitelist);
	}
	
	protected HashMap<String,Vector<String>> _initList(String what, HashMap<String,Vector<String>> serviceList) {
		logger.trace("_initList: BEGIN: what={}, list={}", what, serviceList);
		if (serviceList==null) {
			logger.trace("_initList: services {} not yet initialized or no {} has been configured", what, what);
			// Get list file name
			String file = null;
			String settingKey = "services-"+what+"-file";
			if (settings!=null) file = settings.getProperty(settingKey);
			if (file==null && defaultSettings!=null) file = defaultSettings.getProperty(settingKey);
			if (file==null) {
				logger.trace("_initList: END: No services {} file has been specified", what);
				return null;
			}
			// Load list from file
			logger.trace("_initList: Loading services {} from file {}...", what, file);
			Properties prop = _loadSettings(file);
			if (prop==null) {
				logger.trace("_initList: END: Services {} file not found or could not be loaded", what);
				return null;
			}
			// Processing entries
			serviceList = new HashMap<String,Vector<String>>();
			for (String key : prop.stringPropertyNames()) {
				String value = prop.getProperty(key);
				String[] users;
				if (value==null || (value=value.trim()).isEmpty() || value.equals("*")) users = null;
				else users = value.split("[ ,\t\r\n]+");
				Vector<String> vect = users!=null ? new Vector<String>(Arrays.asList(users)) : null;
				serviceList.put(key, vect);
			}
			logger.trace("_initList: Services {} initialized", what);
		}
		logger.trace("_initList: Services {} contents:\n{}", what, serviceList);
		return serviceList;
	}
	
	protected boolean _checkIfListed(String owner, String sdId, String what, HashMap<String,Vector<String>> serviceList) {
		logger.trace("_checkIfListed: BEGIN: owner={}, service-id={}, what={}, list={}", owner, sdId, what, serviceList);
		
		// Check if list has been configured
		if (serviceList==null) {
			logger.trace("_checkIfListed: END: Services {} not configured", what);
			return false;	// not configured
		}
		
		// Check if services is listed
		logger.trace("_checkIfListed: Checking if service is {}ed: service-id={}", what, sdId);
		if (!serviceList.containsKey(sdId)) {
			logger.trace("_checkIfListed: END: Service is NOT {}ed: service-id={}", what, sdId);
			return false;	// not listed
		}
		Vector<String> listedForUsers = serviceList.get( sdId );
		logger.trace("_checkIfListed: Service is {}ed: service-id={}, for-users={}", what, sdId, listedForUsers);
		
		// If 'null' or zero-length then service is listed for all users
		if (listedForUsers==null || listedForUsers.size()==0) {
			logger.debug("_checkIfListed: END: Service is {}ed for ALL users: service-id={}", what, sdId);
			return true;
		}
		
		// If a user-specific list is specified, search if 'owner' is in the 'listedForUsers' list
		boolean bb = _findUser(owner, listedForUsers.toArray(new String[0]), null);
		logger.trace("_checkIfListed: END: Service is {}{}ed for user: service-id={}, user={}", bb?"":"NOT ", what, sdId, owner);
		return bb;
	}
}
