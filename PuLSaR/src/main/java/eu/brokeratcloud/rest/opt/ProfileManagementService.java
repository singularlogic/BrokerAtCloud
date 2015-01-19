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

import eu.brokeratcloud.common.ServiceCategory;
import eu.brokeratcloud.common.policy.*;
import eu.brokeratcloud.opt.ComparisonPair;
import eu.brokeratcloud.opt.ConsumerPreference;
import eu.brokeratcloud.opt.ConsumerPreferenceProfile;
import eu.brokeratcloud.opt.*;
import eu.brokeratcloud.opt.policy.*;
import eu.brokeratcloud.opt.RecommendationManager;
import eu.brokeratcloud.persistence.RdfPersistenceManager;
import eu.brokeratcloud.persistence.RdfPersistenceManagerFactory;
import eu.brokeratcloud.rest.opt.ServiceCategoryAttributeManagementServiceNEW;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Path("/opt/profile")
public class ProfileManagementService extends AbstractManagementService {

	// GET /opt/profile/sc/{scId}/detailed-list
	// Description: Get a list of detailed consumer preference profiles
	@GET
	@Path("/sc/{sc_id}/detailed-list")
	@Produces("application/json")
	public ConsumerPreferenceProfile[] getProfilesDetailed(@PathParam("sc_id") String scId) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			
			String profRdfType = pm.getClassRdfType(ConsumerPreferenceProfile.class);
			String ownerUri = pm.getFieldUri(ConsumerPreferenceProfile.class, "owner");
			String queryStr = "SELECT ?s WHERE { ?s  a  <"+profRdfType+"> . " +
								" ?s <"+ownerUri+"> \""+scId+"\"^^<http://www.w3.org/2001/XMLSchema#string> " +
								"}";
			
			logger.debug("getProfilesDetailed: Retrieving consumer preference profiles");
			List<Object> list = pm.findByQuery(queryStr);
			logger.debug("{} consumer preference profiles found", list.size());
			
			// also retrieve criteria individual values
			for (Object o : list) retrieveCriteriaIndividuals(pm, (ConsumerPreferenceProfile)o);
			
			return list.toArray(new ConsumerPreferenceProfile[list.size()]);
		} catch (Exception e) {
			logger.error("getProfilesDetailed: EXCEPTION THROWN: {}", e);
			logger.debug("getProfilesDetailed: Returning an empty array of {}", ConsumerPreferenceProfile.class);
			return new ConsumerPreferenceProfile[0];
		}
	}
	
	// GET /opt/profile/sc{scId}/list
	// Description: Get a list (id and names only) of consumer preference profiles
	@GET
	@Path("/sc/{sc_id}/list")
	@Produces("application/json")
	public String getProfilesShort(@PathParam("sc_id") String scId) {
		ConsumerPreferenceProfile[] profiles = getProfilesDetailed(scId);
		StringBuilder sb = new StringBuilder("[ ");
		String comma = null;
		for (int i=0, n=profiles.length; i<n; i++) {
			if (comma!=null) sb.append(comma); else comma=", ";
			sb.append( String.format("{ \"id\":\"%s\", \"name\":\"%s\" }", profiles[i].getId(), profiles[i].getName()) );
		}
		sb.append(" ]");
		return sb.toString();
	}
	
	// GET /opt/profile/sc/{sc_id}/profile/{profile_id}
	// Description: Get consumer preference profile data
	@GET
	@Path("/sc/{sc_id}/profile/{profile_id}")
	@Produces("application/json")
	public ConsumerPreferenceProfile getProfile(@PathParam("sc_id") String scId, @PathParam("profile_id") String prId) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("getProfile: Retrieving consumer preference profile with id = {}", prId);
			ConsumerPreferenceProfile profile = (ConsumerPreferenceProfile)pm.find(prId, ConsumerPreferenceProfile.class);
			logger.debug("getProfile: Consumer Preference Profile {} :\n{}", prId, profile);
			if (profile!=null && (profile.getOwner()==null || !profile.getOwner().equals(scId))) return null;
			
			// also retrieve criteria individual values
			retrieveCriteriaIndividuals(pm, profile);
			
			return profile;
		} catch (Exception e) {
			logger.error("getProfile: EXCEPTION THROWN: ", e);
			logger.debug("getProfile: Returning an empty instance of {}", ConsumerPreferenceProfile.class);
			return null; //new ConsumerPreferenceProfile();
		}
	}
	
	// GET /opt/profile/category/{cat_id}/profiles
	// Description: Get consumer preference profiles data belonging to category
	@GET
	@Path("/category/{cat_id}/profiles")
	@Produces("application/json")
	public ConsumerPreferenceProfile[] getProfilesInCategory(@PathParam("cat_id") String catId) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("getProfilesInCategory: Retrieving category uri: {}", catId);
//			String catUri = pm.getObjectUri(catId, ServiceCategory.class);
			String catUri = catId;
			logger.debug("getProfilesInCategory: Category uri: {}", catUri);
			
			String profRdfType = pm.getClassRdfType(ConsumerPreferenceProfile.class);
			String srvCatUri = pm.getFieldUri(ConsumerPreferenceProfile.class, "serviceClassifications");
			String queryStr = String.format( "SELECT ?s WHERE { ?s a <%s> . ?s ?hasCD <%s> . FILTER STRSTARTS(str(?hasCD),'%s:') . } ", profRdfType, catUri, srvCatUri);
			
			logger.debug("getProfilesInCategory: Query: {}", queryStr);
			logger.debug("getProfilesInCategory: Retrieving consumer preference profiles in category: {}", catId);
			List<Object> list = pm.findByQuery(queryStr);
			logger.debug("{} consumer preference profiles found", list.size());
			
			// also retrieve criteria individual values
			for (Object o : list) retrieveCriteriaIndividuals(pm, (ConsumerPreferenceProfile)o);
			
			return list.toArray(new ConsumerPreferenceProfile[list.size()]);
		} catch (Exception e) {
			logger.error("getProfilesInCategory: EXCEPTION THROWN: ", e);
			logger.debug("getProfilesInCategory: Returning an empty instance of {}", ConsumerPreferenceProfile.class);
			return null; //new ConsumerPreferenceProfile();
		}
	}
	
	protected void retrieveCriteriaIndividuals(RdfPersistenceManager pm, ConsumerPreferenceProfile profile) throws Exception {
		// also retrieve criteria individual values and add them to criteria instances
		if (profile==null) return;
		ConsumerPreference[] prefs = profile.getPreferences();
		if (prefs==null) return;
		for (int i=0; i<prefs.length; i++) {
//XXX:2014-11-21			PreferenceVariable pv = prefs[i].getCriterion();
			String pvUri = prefs[i].getPrefVariable();
			ServiceCategoryAttributeManagementServiceNEW.PolicyObjects po = ServiceCategoryAttributeManagementServiceNEW.getBrokerPolicyObjects(pvUri, false);
			logger.trace("retrieveCriteriaIndividuals: #{}: policy objects={}", i, po);
			if (po==null) continue;
			PreferenceVariable pv = po.pv;
			
			if (pv==null) continue;
			if (!(pv instanceof QualitativePreferenceVariable)) continue;
//XXX:2014-11-21			AllowedQualitativePropertyValue aqpv = (AllowedQualitativePropertyValue)pv.getRefToBrokerPolicyProperty().getRange();
			AllowedQualitativePropertyValue aqpv = (AllowedQualitativePropertyValue)po.apv;
			if (aqpv==null) continue;
			if (aqpv.getAllowedValues()!=null) continue;	// It's ready!  How??
			
			// ...else, retrieve individual values and add them to 'aqpv'
			List<QualitativePropertyValue> individuals = ServiceCategoryAttributeManagementServiceNEW.getIndividuals(pm, null, aqpv);
			if (individuals.size()>0) {
				aqpv.setAllowedValues( individuals.toArray(new QualitativePropertyValue[ individuals.size() ]) );
			}
		}
	}
	
	// PUT /opt/profile/sc/{sc_id}/profile/{profile_name}
	// Description: Create a new consumer preference profile
	@PUT
	@Path("/sc/{sc_id}/profile/{profile_name}")
	@Consumes("application/json")
	public Response createProfile(@PathParam("sc_id") String scId, @PathParam("profile_name") String prName, ConsumerPreferenceProfile profile) {
		try {
			//_prepareProfileForStore(profile);
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("createProfile: Creating a new Consumer Preference Profile with id = {}", profile.getId());
			profile.setCreateTimestamp(new Date());
			logger.debug("createProfile: New profile values:\n{}", profile);
			pm.persist(profile);
			logger.debug("createProfile: Object added to RDF persistent store");
			return createResponse(HTTP_STATUS_CREATED, "Result=Created");
		} catch (Exception e) {
			logger.error("createProfile: EXCEPTION THROWN: ", e);
			logger.debug("createProfile: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
	
	// POST /opt/profile/sc/{sc_id}/profile/{profile_id}
	// Description: Update a consumer preference profile data
	@POST
	@Path("/sc/{sc_id}/profile/{profile_id}")
	@Consumes("application/json")
	public Response updateProfile(@PathParam("sc_id") String scId, @PathParam("profile_id") String prId, ConsumerPreferenceProfile profile) {
		if (scId==null || scId.trim().isEmpty() || prId==null || prId.trim().isEmpty() || profile==null || profile.getId()==null || profile.getId().trim().isEmpty()) {
			logger.error("updateProfile: WRONG URL PARAMETERS: sc-id={}, profile-id={}, profile=\n{}", scId, prId, profile);
			logger.debug("updateProfile: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, String.format("Result=WRONG PARAMETERS: sc-id=%s, profile-id=%s, profile=\n%s", scId, prId, profile));
		}
		try {
			//_prepareProfileForStore(profile);
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("updateProfile: Updating Consumer Preference Profile with id = {}", prId);
			if (profile.getOwner()==null || profile.getOwner().trim().isEmpty() && scId!=null && !scId.trim().isEmpty()) {
				profile.setOwner(scId.trim());
				logger.debug("updateProfile: Autocompleted 'owner' because it was missing: owner={}", scId.trim());
			}
			profile.setLastUpdateTimestamp(new Date());
			logger.debug("updateProfile: Profile new values:\n{}", profile);
			pm.attach(profile);
			logger.debug("updateProfile: Object attached to RDF persistent store manager");
			pm.merge(profile);
			logger.debug("updateProfile: Persistent store state updated");
			return createResponse(HTTP_STATUS_OK, "Result=Updated");
		} catch (Exception e) {
			logger.error("updateProfile: EXCEPTION THROWN: ", e);
			logger.debug("updateProfile: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
	
	// DELETE /opt/profile/sc/{sc_id}/profile/{profile_id}
	// Description: Delete a consumer preference profile and its preferences
	@DELETE
	@Path("/sc/{sc_id}/profile/{profile_id}")
	public Response deleteProfile(@PathParam("sc_id") String scId, @PathParam("profile_id") String prId) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("deleteProfile: Deleting Consumer Preference Profile with id = {}", prId);
			Object o = pm.find(prId, ConsumerPreferenceProfile.class);
			logger.debug("deleteProfile: Object retrieved from RDF persistent store");
			pm.remove(o);
			logger.debug("deleteProfile: Object deleted from RDF persistent store");
			return createResponse(HTTP_STATUS_OK, "Result=Deleted");
		} catch (Exception e) {
			logger.error("deleteProfile: EXCEPTION THROWN: ", e);
			logger.debug("deleteProfile: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
	
/*	protected void _prepareProfileForStore(ConsumerPreferenceProfile profile) {
		ConsumerPreference[] prefs = profile.getPreferences();
		if (prefs!=null) {
			for (int i=0; i<prefs.length; i++) {
				if (prefs[i]==null) continue;
				String cpId = prefs[i].getId();
				if (cpId!=null && !cpId.trim().isEmpty()) continue;
				cpId = java.util.UUID.randomUUID();
				prefs[i].setId(cpId);
			}
		}
	}*/
	
	// ================================================================================================================
	
/*	// GET /opt/profile/{profile_id}/pairs
	// Description: Get a list of criteria comparison pairs for the specified consumer preference profile
	@GET
	@Path("/{profile_id}/pairs")
	@Produces("application/json")
	public String createProfileComparisonPairs(@PathParam("profile_id") String prId) {
		logger.debug("createProfileComparisonPairs: Creating comparison pairs for Consumer Preference Profile with id = {}", prId);
		logger.warn("{}: createProfileComparisonPairs: !!! NOT YET IMPLEMENTED !!!", getClass().getName());
//XXX: TODO
		return "[ ]";
	}
	
	// GET /opt/profile/{profile_id}/criteria/weights
	// Description: Get a list of criteria comparison pairs for the specified consumer preference profile
	@GET
	@Path("/{profile_id}/criteria/weights")
	@Produces("application/json")
	public String getProfileComparisonPairs(@PathParam("profile_id") String prId) {
		logger.debug("getProfileComparisonPairs: Retrieving comparison pairs & weights for Consumer Preference Profile with id = {}", prId);
		logger.warn("{}: getProfileComparisonPairs: !!! NOT YET IMPLEMENTED !!!", getClass().getName());
//XXX: TODO
		return "[ ]";
	}*/
	
	// POST /opt/profile/{profile_id}/criteria/weights
	// Description: Store a list of criteria comparison pairs for the specified consumer preference profile
	@POST
	@Path("/{profile_id}/criteria/weights")
	@Consumes("application/json")
	@Produces("application/json")
	public Response storeProfileComparisonPairs(@PathParam("profile_id") String prId, ComparisonPair[] pairs) {
		logger.debug("storeProfileComparisonPairs: Storing comparison pairs & weights for Consumer Preference Profile with id = {}", prId);
		try {
			// retrieving consumer preference profile
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("storeProfileComparisonPairs: Retrieving consumer preference profile with id = {}", prId);
			ConsumerPreferenceProfile profile = (ConsumerPreferenceProfile)pm.find(prId, ConsumerPreferenceProfile.class);
			logger.debug("Consumer Preference Profile {} :\n{}", prId, profile);
			
			// replacing comparison pair values for profile
			logger.debug("storeProfileComparisonPairs: Deleting existing comparison pairs for Consumer Preference Profile with id = {}", prId);
			//profile.clearComparisonPairs();
			profile.setComparisonPairs(pairs);
			logger.debug("Comparison pairs cleared and replaced for profile: {}", prId);
			
			// trigger weights calculation
			profile.setWeightCalculation(true);
			_calculateWeights(profile);
			
			// store profile
			pm.merge(profile);
			logger.debug("Consumer preference profile comparison pairs updated");
			
			return createResponse(HTTP_STATUS_OK, "Result=Comparison pairs updated");
		} catch (Exception e) {
			logger.error("storeProfileComparisonPairs: EXCEPTION THROWN: ", e);
			logger.debug("storeProfileComparisonPairs: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
	
	protected void _calculateWeights(ConsumerPreferenceProfile profile) {
		RecommendationManager rm = RecommendationManager.getInstance();
		rm.calculatePreferenceWeights(profile);
		
		// XXX: The following code was used for testing/debugging purposes. It assigns random weights to profile preferences
		/*ConsumerPreference[] pref = profile.getPreferences();
		for (int i=0; i<pref.length; i++) {
			pref[i].setWeight( Math.random() );
		}
		_normalizeWeights(profile);
		profile.setWeightCalculation(true);*/
	}
	
	/*protected void _normalizeWeights(ConsumerPreferenceProfile profile) {
		ConsumerPreference[] pref = profile.getPreferences();
		double sum = 0;
		for (int i=0; i<pref.length; i++) {
			sum += pref[i].getWeight();
		}
		for (int i=0; i<pref.length; i++) {
			double w = pref[i].getWeight();
			pref[i].setWeight( w/sum );
		}
	}*/
}