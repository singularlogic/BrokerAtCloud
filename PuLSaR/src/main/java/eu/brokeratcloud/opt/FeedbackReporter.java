package eu.brokeratcloud.opt;

import eu.brokeratcloud.common.RootObject;
import eu.brokeratcloud.common.ServiceDescription;
import eu.brokeratcloud.rest.opt.AttributeManagementService;
import eu.brokeratcloud.rest.opt.AuxiliaryService;
import eu.brokeratcloud.rest.opt.FeedbackManagementService;
import eu.brokeratcloud.rest.opt.ServiceCategoryAttributeManagementServiceNEW;
import eu.brokeratcloud.opt.ServiceCategoryAttribute;
import eu.brokeratcloud.opt.type.TFN;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.RowListenerAdapter;
import org.javalite.activejdbc.annotations.Table;

public class FeedbackReporter extends RootObject {
	private static final Logger logger = LoggerFactory.getLogger("eu.brokeratcloud.opt.FeedbackReporter");
	
	// ================================================================================================================
	// Constructors and factory methods
	
	protected static Properties defaultSettings;
	protected Properties settings;
	
	/* Factory paradigm methods (public) */
	public static FeedbackReporter getInstance() {
		return new FeedbackReporter();
	}
	public static FeedbackReporter getInstance(String propertiesFile) {
		return new FeedbackReporter(propertiesFile);
	}
	public static FeedbackReporter getInstance(Properties properties) {
		return new FeedbackReporter(properties);
	}
	
	/* Constructors (protected) */
	protected FeedbackReporter() {
		if (defaultSettings==null) {
			logger.debug("FeedbackReporter.<init> : initializing default settings");
			defaultSettings = _createDefaultSettings();
		}
		_initFromFile("/feedback.properties");
	}
	
	protected FeedbackReporter(String propertiesFile) {
		logger.debug("FeedbackReporter.<init> : when = {}", new java.util.Date());
		_initFromFile(propertiesFile);
	}
	
	protected FeedbackReporter(Properties props) {
		logger.debug("FeedbackReporter.<init> : when = {}", new java.util.Date());
		settings = new Properties(defaultSettings);
		settings.putAll(props);
	}
	
	// ================================================================================================================
	// Initialization helper methods
	
	protected Properties _createDefaultSettings() {
		Properties p = new Properties();
		// general settings
/*		p.setProperty("++++++", "+++");
		p.setProperty("++++++", "+++");*/
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
			logger.debug("FeedbackReporter: Reading default properties from file: {}...", file);
			java.io.InputStream is = getClass().getResourceAsStream(file);
			if (is==null) {
				logger.debug("FeedbackReporter: Reading default properties from file: {}... Not found", file);
				return null;
			}
			p.load( is );
			logger.debug("FeedbackReporter: Reading default properties from file: {}... done", file);
			return p;
		} catch (Exception e) {
			logger.error("FeedbackReporter: Exception while reading default properties from file: {},  Exception: {}", file, e);
			return null;
		}
	}
	
	// ================================================================================================================
	// Helper methods
	
	// ======================================================================================================
	// Feedback Report generation API and implementation
	
	protected AuxiliaryService auxWs;
	protected FeedbackManagementService fbMgntWs;
	protected Vector<String> attributes;
	protected Vector<String> services;
	
	public void generateFeedbackReports() {
		if (auxWs==null) { auxWs = new AuxiliaryService(); }
		if (fbMgntWs==null) { fbMgntWs = new FeedbackManagementService(); fbMgntWs.initActiveJdbc(); }
		
		try {
			logger.debug("generateFeedbackReports: BEGIN");
			
			// Initialize query templates
			String qryActiveFbAttrs = settings.getProperty("qry.active-feedback-attributes");
			String qryActiveFbSrvs = settings.getProperty("qry.active-feedback-services");
			String qryAttrLinguisticStat = settings.getProperty("qry.linguistic-attribute-feedback-statistics");
			String qryAttrFuzzyStat = settings.getProperty("qry.fuzzy-attribute-feedback-statistics");
			long RECENT_THRESHOLD = Long.parseLong( settings.getProperty("qry.RECENT_THRESHOLD") );
			int MIN_COUNT = Integer.parseInt( settings.getProperty("qry.MIN_COUNT") );
			boolean DELETE_PREVIOUS = Boolean.parseBoolean( settings.getProperty("qry.DELETE_PREVIOUS", "false") );
			
			// Connect to local datastore
			fbMgntWs.connectActiveJdbc();
			
			// Retrieve active feedback attributes and services
			attributes = new Vector<String>();
			services = new Vector<String>();
			
			Timestamp tm = new Timestamp( new Date().getTime() - RECENT_THRESHOLD );
			int cnt = MIN_COUNT;
			
			logger.trace("generateFeedbackReports: Retrieving active feedback attributes...");
			Base.find(qryActiveFbAttrs, tm, cnt).with(new RowListenerAdapter() {
				public void onNext(Map row) {
					attributes.add( row.get("attributeid").toString() );
				}
			});
			logger.trace("generateFeedbackReports: Retrieving active feedback attributes... done");
			logger.trace("generateFeedbackReports: Attributes returned: {}", attributes);
			
			logger.trace("generateFeedbackReports: Retrieving active feedback services...");
			Base.find(qryActiveFbSrvs, tm, cnt).with(new RowListenerAdapter() {
				public void onNext(Map row) {
					services.add( row.get("serviceid").toString() );
				}
			});
			logger.trace("generateFeedbackReports: Retrieving active feedback services... done");
			logger.trace("generateFeedbackReports: Services returned: {}", services);
			
			// Retrieve attribute information and types
			HashMap<String,ServiceCategoryAttribute> attrInfo = new HashMap<String,ServiceCategoryAttribute>();
			logger.trace("generateFeedbackReports: Retrieving attribute info: BEGIN");
			for (String atId : attributes) {
				// retrieve sca object for attribute
				logger.trace("generateFeedbackReports: retrieving attribute info: pv-uri={}", atId);
				eu.brokeratcloud.opt.ServiceCategoryAttribute sca = eu.brokeratcloud.rest.opt.ServiceCategoryAttributeManagementServiceNEW._getServiceCategoryAttribute( atId );
				logger.trace("generateFeedbackReports: attribute info: \n{}", sca);
				
				if (sca==null) continue;
				
				// cache sca objects of imprecise criteria
				if (sca.isFuzzyInc() || sca.isFuzzyDec() || sca.isFuzzyRange() || sca.isLinguistic()) {
					logger.trace("generateFeedbackReports: Attribute is imprecise. Keeping it: attr-name={}", sca.getName());
					attrInfo.put(atId, sca);
				} else {
					logger.error("generateFeedbackReports: Attribute is NOT imprecise. Ignoring it: attr-name={}", sca.getName());
				}
			}
			logger.trace("generateFeedbackReports: Retrieving attribute info: END");
			logger.trace("generateFeedbackReports: Attribute Info (SCA) retrieved:\n{}", attrInfo);
			
			// Retrieve service descriptions
			HashMap<String,ServiceDescription> srvInfo = new HashMap<String,ServiceDescription>();
			logger.trace("generateFeedbackReports: Retrieving service descriptions: BEGIN");
			for (String srvId : services) {
				// retrieve service description
				logger.trace("generateFeedbackReports: retrieving service description: srv-uri={}", srvId);
				ServiceDescription sd = auxWs.getServiceDescription( srvId );
				logger.trace("generateFeedbackReports: service description: \n{}", sd);
				
				if (sd==null) continue;
				
				// cache sca objects of imprecise criteria
				srvInfo.put(srvId, sd);
			}
			logger.trace("generateFeedbackReports: Retrieving service descriptions: END");
			logger.trace("generateFeedbackReports: Service Descriptions retrieved:\n{}", srvInfo);
			
			// Retrieve attribute statistics per service (using active feedback)
			
			// separate attributes into linguistic and fuzzy
			HashMap<String,ServiceCategoryAttribute> attrLinguistic = new HashMap<String,ServiceCategoryAttribute>();
			HashMap<String,ServiceCategoryAttribute> attrFuzzy = new HashMap<String,ServiceCategoryAttribute>();
			for (String atId : attrInfo.keySet()) {
				ServiceCategoryAttribute sca = attrInfo.get(atId);
				if (sca.isLinguistic()) {
					attrLinguistic.put(atId, sca);
				} else {	// is fuzzy
					attrFuzzy.put(atId, sca);
				}
			}
			
			// calculate linguistic attributes average per service from consumer feedback
			HashMap<String,HashMap<String,String>> attrLinguisticStats = new HashMap<String,HashMap<String,String>>();
			logger.trace("generateFeedbackReports: Querying for linguistic attributes feedback statistics: BEGIN");
			for (String atId : attrLinguistic.keySet()) {
				ServiceCategoryAttribute sca = attrLinguistic.get(atId);
				String[] term = sca.getTerms();
				
				// Build feedback statistics query for linguistic attribute
				StringBuilder sb1 = new StringBuilder();
				StringBuilder sb2 = new StringBuilder();
				for (int i=0, n=term.length; i<n; i++) {
					sb1.append(" WHEN '").append(term[i]).append("' THEN ").append(i);
					sb2.append(" WHEN ").append(i).append(" THEN '").append(term[i]).append("'");
				}
				String qry = String.format(qryAttrLinguisticStat, sb1.toString(), sb2.toString());
				logger.trace("generateFeedbackReports: Using query:\n{}", qry);
				
				// Execute feedback statistics query for linguistic attribute and cache results
				logger.trace("generateFeedbackReports: Querying for linguistic attribute '{}' feedback statistics...", atId);
				Base.find(qry, atId, tm, cnt).with(new RowListenerAdapter() {
					private HashMap<String,String> map;
					RowListenerAdapter init(HashMap<String,HashMap<String,String>> hmap, String id) {
						map = new HashMap<String,String>();
						hmap.put(id, map);
						return this;
					}
					public void onNext(Map row) {
						String srv  = row.get("serviceid").toString();
						String mean = row.get("meanvalue").toString();
						map.put(srv, mean);
					}
				}.init(attrLinguisticStats, atId));
				logger.trace("generateFeedbackReports: Querying for linguistic attribute '{}' feedback statistics... done", atId);
				logger.trace("generateFeedbackReports: Linguistic attribute '{}' feedback statistics:\n{}", atId, attrLinguisticStats.get(atId));
			}
			logger.trace("generateFeedbackReports: Querying for linguistic attributes feedback statistics: END");
			logger.trace("generateFeedbackReports: Linguistic attributes feedback statistics:\n{}", attrLinguisticStats);
			
			// calculate fuzzy attributes average per service from consumer feedback
			HashMap<String,HashMap<String,TFN>> attrFuzzyStats = new HashMap<String,HashMap<String,TFN>>();
			logger.trace("generateFeedbackReports: Querying for fuzzy attributes feedback statistics: BEGIN");
			for (String atId : attrFuzzy.keySet()) {
				ServiceCategoryAttribute sca = attrFuzzy.get(atId);
				String[] term = sca.getTerms();
				
				// Build feedback statistics query for attribute
				String qry = qryAttrFuzzyStat;
				logger.trace("generateFeedbackReports: Using query:\n{}", qry);
				
				// Execute feedback statistics query for attribute and cache results
				logger.trace("generateFeedbackReports: Querying for fuzzy attribute '{}' feedback statistics...", atId);
				Base.find(qry, atId, tm, cnt).with(new RowListenerAdapter() {
					private HashMap<String,TFN> map;
					RowListenerAdapter init(HashMap<String,HashMap<String,TFN>> hmap, String id) {
						map = new HashMap<String,TFN>();
						hmap.put(id, map);
						return this;
					}
					public void onNext(Map row) {
						String srv  = row.get("serviceid").toString();
						String lb = row.get("lb").toString();
						String mv = row.get("mv").toString();
						String ub = row.get("ub").toString();
						if (lb.trim().isEmpty() || mv.trim().isEmpty() || ub.trim().isEmpty()) return;
						TFN mean = new TFN( Double.parseDouble(lb), Double.parseDouble(mv), Double.parseDouble(ub) );
						map.put(srv, mean);
					}
				}.init(attrFuzzyStats, atId));
				logger.trace("generateFeedbackReports: Querying for fuzzy attribute '{}' feedback statistics... done", atId);
				logger.trace("generateFeedbackReports: Fuzzy attribute '{}' feedback statistics:\n{}", atId, attrFuzzyStats.get(atId));
			}
			logger.trace("generateFeedbackReports: Querying for fuzzy attributes feedback statistics: END");
			logger.trace("generateFeedbackReports: Fuzzy attributes feedback statistics:\n{}", attrFuzzyStats);
			
			// Group attribute averages per  service
			logger.trace("generateFeedbackReports: Grouping attribute statistics per service...");
			HashMap<String,HashMap<String,Object>> srvStats = new HashMap<String,HashMap<String,Object>>();
			for (String atId : attrLinguisticStats.keySet()) {
				HashMap<String,String> map = attrLinguisticStats.get(atId);
				for (String srvId : map.keySet()) {
					String value = map.get(srvId);
					//
					if (!srvStats.containsKey(srvId)) srvStats.put(srvId, new HashMap<String,Object>());
					HashMap<String,Object> attrMap = srvStats.get(srvId);
					attrMap.put(atId, value);
				}
			}
			for (String atId : attrFuzzyStats.keySet()) {
				HashMap<String,TFN> map = attrFuzzyStats.get(atId);
				for (String srvId : map.keySet()) {
					TFN value = map.get(srvId);
					//
					if (!srvStats.containsKey(srvId)) srvStats.put(srvId, new HashMap<String,Object>());
					HashMap<String,Object> attrMap = srvStats.get(srvId);
					attrMap.put(atId, value);
				}
			}
			logger.trace("generateFeedbackReports: Grouping attribute statistics per service... done");
			logger.trace("generateFeedbackReports: Attributes statistics per service:\n{}", srvStats);
			
			// Retrieve service descriptions, compare to statistics and generate notifications/reports
			logger.trace("generateFeedbackReports: Notification generation per service...");
			AttributeManagementService attrMgntWs = new AttributeManagementService();
			HashMap<String,String> attrNames = new HashMap<String,String>();
			HashMap<String,HashMap<String,String>> notifSrv = new HashMap<String,HashMap<String,String>>();
			for (String srvId : srvStats.keySet()) {
				ServiceDescription sd = srvInfo.get(srvId);
				if (sd==null) continue;
				HashMap<String,Object> attrStats = srvStats.get(srvId);
				if (attrStats==null || attrStats.size()==0) continue;
				//
				HashMap<String,String> notifAttr;
				notifSrv.put(srvId, notifAttr = new HashMap<String,String>());
				for (String atId : attrStats.keySet()) {
					Object fbAttrValue = attrStats.get(atId);
					if (fbAttrValue==null) continue;
					logger.trace("generateFeedbackReports: getting attribute's allowed values uri: pv-uri={}", atId);
					String avAtId = ServiceCategoryAttributeManagementServiceNEW._getAllowedValueFromPV(atId);	// atId contains attribute's pref. var. uri, since this uri is stored in local datastore
					
					// Retrieve attribute name
					String atName = null;
					if (attrNames.containsKey(atId)) {
						atName = attrNames.get(atId);
					} else {
						atName = ServiceCategoryAttributeManagementServiceNEW._getAttributeNameFromPV(atId);	// atId contains attribute's pref. var. uri, since this uri is stored in local datastore
						if (atName==null || atName.trim().isEmpty()) atName = atId;
						attrNames.put(atId, atName);
					}
					
					logger.trace("generateFeedbackReports: attribute's allowed values uri: av-uri={}", avAtId);
					Object sdAttrValue = sd.getServiceAttributeValue( avAtId );
					
					// Prepare notification message
					if (sdAttrValue==null) {
						notifAttr.put(atId, "Attribute '"+atName+"': Service description does not specify a value for this attribute");
					} else {
						if (fbAttrValue instanceof TFN) {		// fuzzy
							TFN fbVal = (TFN)fbAttrValue;
							TFN sdVal = null;
							if (sdAttrValue instanceof TFN) sdVal = (TFN)sdAttrValue;
							else sdVal = TFN.valueOf( sdAttrValue.toString() );
							if (fbVal.ltStrict(sdVal)) {
								notifAttr.put(atId, "Attribute '"+atName+"': Service description value is higher than consumers' perceivable value: sd-value="+sdVal+", user-perception-value="+fbVal);
							} else
							if (fbVal.gtStrict(sdVal)) {
								notifAttr.put(atId, "Attribute '"+atName+"': Service description value is lower than consumers' perceivable value: sd-value="+sdVal+", user-perception-value="+fbVal);
							}
						} else
						if (fbAttrValue instanceof String) {	// linguistic
							String fbVal = (String)fbAttrValue;
							String sdVal = (String)sdAttrValue;
							if (!fbVal.equals(sdVal)) {
								notifAttr.put(atId, "Attribute '"+atName+"': Service description value is different than consumers' perceivable value: sd-value="+sdVal+", user-perception-value="+fbVal);
							}
						}
					}
				}
			}
			logger.trace("generateFeedbackReports: Notification generation per service... done");
			logger.trace("generateFeedbackReports: Notifications per service:\n{}", notifSrv);
			
			// Store notifications to local datastore
			logger.trace("generateFeedbackReports: Storing notifications to local datastore...");
			logger.trace("generateFeedbackReports: Delete previous notifications: {}", DELETE_PREVIOUS);
			for (String srvId : notifSrv.keySet()) {
				// Clear previous notifications for service from local datastore
				if (DELETE_PREVIOUS) {
					FeedbackNotification.update("deleted = 1", "serviceId = ? and deleted = 0", srvId);
				}
				
				// Store new notifications for service to local datastore
				HashMap<String,String> notifAttr = notifSrv.get(srvId);
				for (String atId : notifAttr.keySet()) {
					String notifText = notifAttr.get(atId);
					FeedbackNotification fb = new FeedbackNotification();
					fb.setString("serviceId", srvId);
					fb.setString("attributeId", atId);
					fb.setString("message", notifText);
					fb.saveIt();
				}
			}
			logger.trace("generateFeedbackReports: Storing notifications to local datastore... done");
			
			// Disconnect from local datastore
			fbMgntWs.disconnectActiveJdbc();
			
			logger.debug("generateFeedbackReports: END");
			
		} catch (Exception e) {
			logger.error("generateFeedbackReports: EXCEPTION THROWN:\n", e);
		} finally {
			fbMgntWs.disconnectActiveJdbc();
		}
	}
	
	@Table("feedback_notifications") 
	public static class FeedbackNotification extends Model {}
}
