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
import eu.brokeratcloud.opt.Notification;
import eu.brokeratcloud.opt.ServiceCategoryAttribute;
import eu.brokeratcloud.persistence.RdfPersistenceManager;
import eu.brokeratcloud.persistence.RdfPersistenceManagerFactory;
import eu.brokeratcloud.persistence.SparqlServiceClient;
import eu.brokeratcloud.persistence.SparqlServiceClientFactory;

import java.sql.Timestamp;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Path("/opt/feedback")
public class FeedbackManagementService extends AbstractManagementService {
	protected static final Logger logger = LoggerFactory.getLogger("eu.brokeratcloud.rest.opt.feedback");
	
	// =============================================================================================================================
	// DATASTORE VARIABLES AND METHODS
	// =============================================================================================================================
	
	// ActiveJDBC variables and methods
	protected static final String defaultActiveJdbcConfigFile = "feedback.properties";
	protected Properties activeJdbcConfig;
	
	protected String dbDriver;
	protected String dbConnStr;
	protected String dbUsername;
	protected String dbPassword;
	protected String qrySrvByCons;
	protected String qryConsFeedback;
	protected String qrySrvNotifs;
	protected boolean connected;
	
	public synchronized void initActiveJdbc() {
		if (activeJdbcConfig!=null) {
			logger.trace("initActiveJdbc: ActiveJDBC configuration already loaded");
			return;
		}
		try {
			String propertiesFile = defaultActiveJdbcConfigFile;
			logger.debug("initActiveJdbc: Loading ActiveJDBC configuration from file: {}", propertiesFile);
			Properties properties = new Properties();
			properties.load( getClass().getClassLoader().getResourceAsStream(propertiesFile) );
			this.activeJdbcConfig = properties;
			String pwd = activeJdbcConfig.getProperty("db.password");
			activeJdbcConfig.setProperty("db.password", "********");
			logger.trace("initActiveJdbc: ActiveJDBC configuration: BEGIN\n{}\ninitActiveJdbc: ActiveJDBC configuration: END", activeJdbcConfig);
			activeJdbcConfig.setProperty("db.password", pwd);
			
			dbDriver = activeJdbcConfig.getProperty("db.driver");
			dbConnStr = activeJdbcConfig.getProperty("db.conn-str");
			dbUsername = activeJdbcConfig.getProperty("db.username");
			dbPassword = activeJdbcConfig.getProperty("db.password");
			
			qrySrvByCons = activeJdbcConfig.getProperty("qry.used-srv-per-user");
			qryConsFeedback = activeJdbcConfig.getProperty("qry.consumer-feedback");
			qrySrvNotifs = activeJdbcConfig.getProperty("qry.service-feedback-notifications");
		} catch (Exception e) {
			logger.error("initActiveJdbc: Exception during ActiveJDBC initialization. EXCEPTION: ", e);
		}
	}
	
	public synchronized void connectActiveJdbc() {
		try {
			if (connected) return;		// already connected
			
			if (activeJdbcConfig==null) initActiveJdbc();
			
			// Connect to local datastore
			logger.trace("connectActiveJdbc: Opening connection to local datastore...");
			Base.open(dbDriver, dbConnStr, dbUsername, dbPassword);
			logger.trace("connectActiveJdbc: Opening connection to local datastore... done");
			connected = true;
		} catch (Exception e) {
			logger.error("connectActiveJdbc: ActiveJDBC Exception during connection to local datastore. EXCEPTION: ", e);
		}
	}
	
	public synchronized void disconnectActiveJdbc() {
		try {
			if (!connected) return;		// already disconnected
			
			// Disconnect from local datastore
			logger.trace("disconnectActiveJdbc: Closing connection from local datastore...");
			Base.close();
			logger.trace("disconnectActiveJdbc: Closing connection from local datastore... done");
			connected = false;
		} catch (Exception e) {
			logger.error("disconnectActiveJdbc: ActiveJDBC Exception during disconnection from local datastore. EXCEPTION: ", e);
		}
	}
	
	// =============================================================================================================================
	// CONSUMER FEEDBACK METHODS
	// =============================================================================================================================
	
	protected AuxiliaryService auxWs;
	
	@Table("used_services") 
	public static class UsedService extends Model {}
	
	@Table("consumer_feedback") 
	public static class ConsumerFeedback extends Model {}
	
	@Table("feedback_notifications") 
	public static class FeedbackNotification extends Model {}
	
	// Retrieves service descriptions used by the specified consumer
	@GET
	@Path("/sc/{sc_id}/list-used-services")
	@Produces("application/json")
	public ServiceDescription[] getServiceDescriptionsUsedByUser(@PathParam("sc_id") String scId) {
		return getServiceDescriptionsUsedByUser(scId, -1);
	}
	
	// Retrieves service descriptions used by the specified consumer in a specified period of time (last 'period' seconds)
	@GET
	@Path("/sc/{sc_id}/list-used-services/period/{period}")
	@Produces("application/json")
	public ServiceDescription[] getServiceDescriptionsUsedByUser(@PathParam("sc_id") String scId, @PathParam("period") long period) {
		try {
			logger.trace("getServiceDescriptionsUsedByUser: BEGIN: consumer={}, period={}", scId, period);
			
			initActiveJdbc();
			
			Timestamp thresholdTm = (period>=0) ? new Timestamp( new Date().getTime() - period ) : new Timestamp(0);
			logger.trace("getServiceDescriptionsUsedByUser: period-threshold={}", thresholdTm);
			
			// Connect to local datastore and retrieve used services list for the specified user
			connectActiveJdbc();
			
			// Query local datastore for currently and recently used services
			logger.trace("getServiceDescriptionsUsedByUser: Querying table 'used_services' where consumerId='{}'...", scId);
			List<UsedService> results = UsedService.findBySQL(qrySrvByCons, scId, thresholdTm);
			logger.trace("getServiceDescriptionsUsedByUser: Querying table 'used_services' where consumerId='{}'... done", scId);
			logger.trace("getServiceDescriptionsUsedByUser: Query results: {}", results);
			
			// Retrieve service descriptions from RDF repository
			logger.trace("getServiceDescriptionsUsedByUser: Retrieving service descriptions for used services from RDF repository...");
			if (auxWs==null) auxWs = new AuxiliaryService();
			Vector<ServiceDescription> vect = new Vector<ServiceDescription>();
			int rCnt = 0;
			for (UsedService s : results) {
				// get service data from result set
				String sdId = s.getString("serviceId");
				Timestamp tm = s.getTimestamp("lastUsedTimestamp");
				String status = s.getString("status");
				logger.trace("getServiceDescriptionsUsedByUser: Processing record #{}: sd-uri={}, last-used={}, status={}", ++rCnt, sdId, tm, status);
				
				if (sdId==null || sdId.trim().isEmpty() || status==null || status.trim().isEmpty()) continue;
				
				String uri = sdId.trim();
				logger.trace("getServiceDescriptionsUsedByUser: Retrieving service description for uri: {}", uri);
				ServiceDescription sd = auxWs.getServiceDescription( uri );
				if (sd!=null) vect.add(sd);
				else logger.error("getServiceDescriptionsUsedByUser: Service description NOT FOUND in RDF repository: uri={}", uri);
				
				if (status.equals("IN-USE")) sd.setLastUsedTimestamp(null);
				else sd.setLastUsedTimestamp( (tm!=null) ? tm : new Date() );
			}
			logger.trace("getServiceDescriptionsUsedByUser: Retrieving service descriptions for used services from RDF repository... done");
			logger.trace("getServiceDescriptionsUsedByUser: Retrieved service descriptions: {}", vect);
			
			ServiceDescription[] list = vect.toArray( new ServiceDescription[vect.size()] );
			
			// Disconnect from local datastore
			disconnectActiveJdbc();
			
			logger.trace("getServiceDescriptionsUsedByUser: END: results={}", list);
			return list;
		} catch (Exception e) {
			logger.error("getServiceDescriptionsUsedByUser: EXCEPTION THROWN:\n", e);
			logger.error("getServiceDescriptionsUsedByUser: Returning an empty array of {}", ServiceDescription.class);
			return new ServiceDescription[0];
		} finally {
			disconnectActiveJdbc();
		}
	}
	
	// Retrieve stored feedback data for the specified service
	@GET
	@Path("/sc/{sc_id}/sd/{sd_id}")
	@Produces("application/json")
	public HashMap<String,String> getConsumerFeedbackForService(@PathParam("sc_id") String scId, @PathParam("sd_id") String sdId) {
		try {
			logger.trace("getConsumerFeedbackForService: BEGIN: consumer={}, service={}", scId, sdId);
			
			initActiveJdbc();
			
			// Connect to local datastore and retrieve used services list for the specified user
			connectActiveJdbc();
			
			logger.trace("getConsumerFeedbackForService: Querying table 'consumer_feedback' where consumerId='{}' and serviceId='{}'...", scId, sdId);
			List<ConsumerFeedback> results = ConsumerFeedback.findBySQL(qryConsFeedback, scId, sdId);
			logger.trace("getConsumerFeedbackForService: Querying table 'consumer_feedback' where consumerId='{}' and serviceId='{}'... done", scId, sdId);
			logger.trace("getConsumerFeedbackForService: Query results: {}", results);
			
			// Retrieve service descriptions from RDF repository
			logger.trace("getConsumerFeedbackForService: Retrieving consumer '{}' feedback for service '{}' from RDF repository...", scId, sdId);
			HashMap<String,String> map = new HashMap<String,String>();
			int rCnt = 0;
			for (ConsumerFeedback cf : results) {
				// get feedback data from result set
				String attrId = cf.getString("attributeId");
				String value = cf.getString("value");
				Timestamp tm = cf.getTimestamp("lastChangedTimestamp");
				logger.trace("getConsumerFeedbackForService: Processing record #{}: attr-uri={}, value={}, last-changed={}", ++rCnt, attrId, value, tm);
				
				if (attrId==null || attrId.trim().isEmpty() || value==null || value.trim().isEmpty()) continue;
				
				map.put(attrId, value);
			}
			logger.trace("getConsumerFeedbackForService: Retrieving consumer '{}' feedback for service '{}' from RDF repository... done", scId, sdId);
			
			// Disconnect from local datastore
			disconnectActiveJdbc();
			
			logger.trace("getConsumerFeedbackForService: END: Retrieved consumer feedback: {}", map);
			return map;
			
		} catch (Exception e) {
			logger.error("getConsumerFeedbackForService: EXCEPTION THROWN:\n", e);
			logger.error("getConsumerFeedbackForService: Returning an empty Map<String,String>");
			return new HashMap<String,String>();
		} finally {
			disconnectActiveJdbc();
		}
	}
	
	// Store submitted feedback data for the specified service
	@POST
	@Path("/sc/{sc_id}/sd/{sd_id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response saveConsumerFeedbackForService(@PathParam("sc_id") String scId, @PathParam("sd_id") String sdId, HashMap<String,String> feedback) {
		try {
			logger.trace("saveConsumerFeedbackForService: BEGIN: consumer={}, service={}, feedback-data={}", scId, sdId, feedback);
			
			initActiveJdbc();
			
			// Connect to local datastore
			connectActiveJdbc();
			
			// Delete any previously saved feedback for the specified service, from local datastore
			logger.trace("saveConsumerFeedbackForService: Deleting existing feedback records from table 'consumer_feedback' where consumerId='{}' and serviceId='{}'...", scId, sdId);
			ConsumerFeedback.update("deleted = 1", "consumerId = ? and serviceId = ? and deleted = 0", scId, sdId);
			logger.trace("saveConsumerFeedbackForService: Deleting existing feedback records from table 'consumer_feedback' where consumerId='{}' and serviceId='{}'... done", scId, sdId);
			
			// Insert new feedback data for the specified service, into local datastore 
			logger.trace("saveConsumerFeedbackForService: Inserting new feedback records into table 'consumer_feedback' where consumerId='{}' and serviceId='{}'...", scId, sdId);
			for (String atId : feedback.keySet()) {
				String fbVal = feedback.get(atId);
				if (fbVal==null) fbVal = "";
				logger.trace("saveConsumerFeedbackForService: \tattributeId='{}' and value='{}'...", atId, fbVal);
				ConsumerFeedback cf = new ConsumerFeedback();
				cf.setString("consumerId", scId);
				cf.setString("serviceId", sdId);
				cf.setString("attributeId", atId);
				cf.setString("value", fbVal);
				cf.saveIt();
				logger.trace("saveConsumerFeedbackForService: \tattributeId='{}' and value='{}'... done", atId, fbVal);
			}
			logger.trace("saveConsumerFeedbackForService: Inserting new feedback records into table 'consumer_feedback' where consumerId='{}' and serviceId='{}'... done", scId, sdId);
			
		} catch (Exception e) {
			logger.error("saveConsumerFeedbackForService: EXCEPTION THROWN:\n", e);
			logger.debug("saveConsumerFeedbackForService: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Feedback NOT saved: Reason: "+e);
		} finally {
			// Disconnecting from local datastore
			disconnectActiveJdbc();
		}
		
		logger.trace("saveConsumerFeedbackForService: END: n/a");
		return createResponse(HTTP_STATUS_OK, "Feedback saved");
	}
	
	// Delete stored feedback data for the specified service
	@DELETE
	@Path("/sc/{sc_id}/sd/{sd_id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response deleteConsumerFeedbackForService(@PathParam("sc_id") String scId, @PathParam("sd_id") String sdId) {
		try {
			logger.trace("deleteConsumerFeedbackForService: BEGIN: consumer={}, service={}", scId, sdId);
			
			initActiveJdbc();
			
			// Connect to local datastore
			connectActiveJdbc();
			
			// Delete any previously saved feedback for the specified service, from local datastore
			logger.trace("deleteConsumerFeedbackForService: Deleting existing feedback records from table 'consumer_feedback' where consumerId='{}' and serviceId='{}'...", scId, sdId);
			ConsumerFeedback.update("deleted = 1", "consumerId = ? and serviceId = ? and deleted = 0", scId, sdId);
			logger.trace("deleteConsumerFeedbackForService: Deleting existing feedback records from table 'consumer_feedback' where consumerId='{}' and serviceId='{}'... done", scId, sdId);
			
		} catch (Exception e) {
			logger.error("deleteConsumerFeedbackForService: EXCEPTION THROWN:\n", e);
			logger.debug("deleteConsumerFeedbackForService: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Feedback NOT saved: Reason: "+e);
		} finally {
			// Disconnecting from local datastore
			disconnectActiveJdbc();
		}
		
		logger.trace("deleteConsumerFeedbackForService: END: n/a");
		return createResponse(HTTP_STATUS_OK, "Feedback saved");
	}
	
	// Retrieves service description imprecise attributes (i.e. Fuzzy and Linguistic attributes)
	@GET
	@Path("/sd/{sd_id}/imprecise-attributes")
	@Produces("application/json")
	public List<ServiceCategoryAttribute> getServiceDescriptionImpreciseAttributes(@PathParam("sd_id") String sdId) {
		try {
			logger.trace("getServiceDescriptionImpreciseAttributes: BEGIN: sd-id={}", sdId);
			
			if (auxWs==null) auxWs = new AuxiliaryService();
			ServiceDescription sd = auxWs.getServiceDescription(sdId);
			logger.trace("getServiceDescriptionImpreciseAttributes: service description: \n{}", sd);
			Map<String,Object> attrs = sd.getServiceAttributes();
			logger.trace("getServiceDescriptionImpreciseAttributes: service attributes: {}", attrs.keySet());
			List<ServiceCategoryAttribute> list = new ArrayList<ServiceCategoryAttribute>();
			for (String aid : attrs.keySet()) {
				Object val = attrs.get(aid);
				if (aid.startsWith(".")) continue;
				logger.trace("getServiceDescriptionImpreciseAttributes: getting pref. var. uri for attribute: av-uri={}", aid);
				String pvUri = eu.brokeratcloud.rest.opt.ServiceCategoryAttributeManagementService._getPrefVarFromAV(aid);
				if (pvUri==null || pvUri.trim().isEmpty()) continue;
				
				logger.trace("getServiceDescriptionImpreciseAttributes: attribute pref. var.: pv-uri={}", pvUri);
				logger.trace("getServiceDescriptionImpreciseAttributes: retrieving attribute info: pv-uri={}", pvUri);
				eu.brokeratcloud.opt.ServiceCategoryAttribute sca = eu.brokeratcloud.rest.opt.ServiceCategoryAttributeManagementService._getServiceCategoryAttribute( pvUri );
				logger.trace("getServiceDescriptionImpreciseAttributes: attribute info: \n{}", sca);
				
				if (sca==null) continue;
				
				if (sca.isFuzzyInc() || sca.isFuzzyDec() || sca.isFuzzyRange() || sca.isLinguistic()) {
					logger.trace("getServiceDescriptionImpreciseAttributes: Attribute is imprecise. Keeping it: attr-name={}", sca.getName());
					list.add(sca);
				} else {
					logger.trace("getServiceDescriptionImpreciseAttributes: Attribute is NOT imprecise. Ignoring it: attr-name={}", sca.getName());
				}
			}
			logger.trace("getServiceDescriptionImpreciseAttributes: DONE retrieving attribute info");
			
			logger.trace("getServiceDescriptionImpreciseAttributes: END: results={}", list);
			return list;
		} catch (Exception e) {
			logger.error("getServiceDescriptionImpreciseAttributes: EXCEPTION THROWN:\n", e);
			logger.error("getServiceDescriptionImpreciseAttributes: Returning an empty array of {}", String.class);
			//return new String[0];
			return new ArrayList<ServiceCategoryAttribute>();
		}
	}
	
	// Retrieves feedback notifications for service
	@GET
	@Path("/sd/{sd_id}/notifications")
	@Produces("application/json")
	public List<Notification> getNotificationsForService(@PathParam("sd_id") String sdId) {
		return getNotificationsForService(sdId, 0);
	}
	
	// Retrieves feedback notifications for service
	@GET
	@Path("/sd/{sd_id}/notifications/period/{period}")
	@Produces("application/json")
	public List<Notification> getNotificationsForService(@PathParam("sd_id") String sdId, @PathParam("period") long period) {
		try {
			logger.trace("getNotificationsForService: BEGIN: service={}, period={}", sdId, period);
			
			long threshold = (period<=0) ? 0 : new Date().getTime()-period;
			if (threshold<0) threshold = 0;
			Timestamp tm = new Timestamp( threshold );
			
			initActiveJdbc();
			
			// Connect to local datastore and retrieve used services list for the specified user
			connectActiveJdbc();
			
			logger.trace("getNotificationsForService: Querying table 'feedback_notifications' where serviceId='{}'...", sdId);
			List<FeedbackNotification> results = FeedbackNotification.findBySQL(qrySrvNotifs, sdId, tm);
			logger.trace("getNotificationsForService: Querying table 'feedback_notifications' where serviceId='{}'... done", sdId);
			logger.trace("getNotificationsForService: Query results: {}", results);
			
			// Transcribe results to notifications
			List<Notification> notifList = new ArrayList<Notification>();
			for (FeedbackManagementService.FeedbackNotification fnf : results) {
				Notification notif = new Notification();
				notif.setCreateTimestamp( fnf.getTimestamp("creationtimestamp") );
				notif.setService( sdId );
				notif.setMessage( fnf.getString("message") );
				
				notifList.add(notif);
			}
			
			disconnectActiveJdbc();
			
			logger.trace("getNotificationsForService: END: Retrieved feedback notifications: {}", notifList);
			return notifList;
			
		} catch (Exception e) {
			logger.error("getNotificationsForService: EXCEPTION THROWN:\n", e);
			logger.error("getNotificationsForService: Returning an list of {}", Notification.class);
			return new ArrayList<Notification>();
		} finally {
			disconnectActiveJdbc();
		}
	}
}