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

import eu.brokeratcloud.opt.Recommendation;
import eu.brokeratcloud.opt.RecommendationItem;
import eu.brokeratcloud.opt.RecommendationManager;
import eu.brokeratcloud.persistence.RdfPersistenceManager;
import eu.brokeratcloud.persistence.RdfPersistenceManagerFactory;

@Path("/opt/recommendation")
public class RecommendationManagementService extends AbstractManagementService {

	// GET /opt/recommendation/sc/{scId}/profile/{profileId}/period/{period-spec}/list
	// Description: Get a list of recommendations suggested to SC in the specified period
	@GET
	@Path("/sc/{scId}/profile/{profileId}/period/{period_spec}/list")
	@Produces("application/json")
	public Recommendation[] getRecommendationsForPeriod(@PathParam("scId") String scId, @PathParam("profileId") String profileId, @PathParam("period_spec") String period) {
		logger.debug("getRecommendationsForPeriod: calculating time limit from period = {}", period);
		long diff = Long.parseLong(period);
		Date limitTm = new Date( System.currentTimeMillis() - diff );
		//
		logger.debug("getRecommendationsForPeriod: Calling getRecommendations with the same parameters: profile = {}", profileId);
		Recommendation[] recomArr = getRecommendations(scId, profileId);
		logger.debug("getRecommendationsForPeriod: Filtering retrieved recommendations using period: {}", period);
		int cnt = 0;
		for (int i=0; i<recomArr.length; i++) {
			if (recomArr[i].getCreateTimestamp()==null) recomArr[i] = null;		// No create-timestamp means 01/01/1900, therefore this recommendation is NOT included
			else if (recomArr[i].getCreateTimestamp().getTime()<limitTm.getTime()) recomArr[i] = null;
			else cnt++;
		}
		Recommendation[] recomArr2 = new Recommendation[cnt];
		for (int i=0, j=0; i<recomArr.length; i++) {
			if (recomArr[i]!=null) recomArr2[j++] = recomArr[i];
		}
		return recomArr2;
	}

	// GET /opt/recommendation/sc/{scId}/profile/{profileId}
	// Description: Gets a recommendations list for the specified profile and user
	@GET
	@Path("/sc/{scId}/profile/{profileId}")
	@Produces("application/json")
	public Recommendation[] getRecommendations(@PathParam("scId") String scId, @PathParam("profileId") String profileId) {
		return getRecommendations(scId, profileId, false);	// return only active recommendation(s)
	}
	
	public Recommendation[] getRecommendations(@PathParam("scId") String scId, @PathParam("profileId") String profileId, boolean includeInactive) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			
			/*String recomRdfType = "http://www.brokeratcloud.eu/v1/opt/RECOMMENDATION";
			String profileUri = "http://www.brokeratcloud.eu/v1/opt/RECOMMENDATION/profile";
			String ownerUri = "http://www.brokeratcloud.eu/v1/common/hasOwner";
			String activeUri = "http://www.brokeratcloud.eu/v1/opt/RECOMMENDATION/active";*/
			
			String recomRdfType = pm.getClassRdfType(Recommendation.class);
			String profileUri = pm.getFieldUri(Recommendation.class, "profile");
			String ownerUri = pm.getFieldUri(Recommendation.class, "owner");
			String activeUri = pm.getFieldUri(Recommendation.class, "active");
			String queryStr = (includeInactive)
						? "SELECT ?s WHERE { ?s  a  <"+recomRdfType+"> ; <"+profileUri+">  \"%s\"^^<http://www.w3.org/2001/XMLSchema#string> ; <"+ownerUri+"> \"%s\"^^<http://www.w3.org/2001/XMLSchema#string> }"
						: "SELECT ?s WHERE { ?s  a  <"+recomRdfType+"> ; <"+profileUri+">  \"%s\"^^<http://www.w3.org/2001/XMLSchema#string> ; <"+ownerUri+"> \"%s\"^^<http://www.w3.org/2001/XMLSchema#string> ; <"+activeUri+"> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> }"	;
			
			logger.debug("getRecommendations: Retrieving Recommendations for profile = {}", profileId);
			List<Object> list = pm.findByQuery( String.format(queryStr, profileId, scId) );
			logger.debug("{} recommendations found", list.size());
			return list.toArray(new Recommendation[list.size()]);
		} catch (Exception e) {
			logger.error("getRecommendations: EXCEPTION THROWN: {}", e);
			logger.debug("getRecommendations: Returning an empty array of {}", Recommendation.class);
			return new Recommendation[0];
		}
	}

	// GET /opt/recommendation/sc/{scId}
	// Description: Gets a recommendations list for the specified profile and user
	@GET
	@Path("/sc/{scId}")
	@Produces("application/json")
	public Recommendation[] getAllRecommendations(@PathParam("scId") String scId) {
		return getAllRecommendations(scId, false);	// return only active recommendation(s)
	}
	
	public Recommendation[] getAllRecommendations(String scId, boolean includeInactive) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			
			String recomRdfType = pm.getClassRdfType(Recommendation.class);
			String profileUri = pm.getFieldUri(Recommendation.class, "profile");
			String ownerUri = pm.getFieldUri(Recommendation.class, "owner");
			String activeUri = pm.getFieldUri(Recommendation.class, "active");
			String queryStr = (includeInactive) 
						? "SELECT ?s WHERE { ?s  a  <"+recomRdfType+"> . ?s <"+ownerUri+"> \"%s\"^^<http://www.w3.org/2001/XMLSchema#string> }"
						: "SELECT ?s WHERE { ?s  a  <"+recomRdfType+"> . ?s <"+ownerUri+"> \"%s\"^^<http://www.w3.org/2001/XMLSchema#string> . ?s <"+activeUri+"> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> }";
			
			logger.debug("getAllRecommendations: Retrieving Recommendations for ALL profiles");
			List<Object> list = pm.findByQuery( String.format(queryStr, scId) );
			logger.debug("{} recommendations found", list.size());
			return list.toArray(new Recommendation[list.size()]);
		} catch (Exception e) {
			logger.error("getAllRecommendations: EXCEPTION THROWN: {}", e);
			logger.debug("getAllRecommendations: Returning an empty array of {}", Recommendation.class);
			return new Recommendation[0];
		}
	}

	// GET /opt/recommendation/{recom_id}
	// Description: Get a recommendation
	@GET
	@Path("/{recom_id}")
	@Produces("application/json")
	public Recommendation getRecommendation(@PathParam("recom_id") String id) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("getRecommendation: Retrieving Recommendation with id = {}", id);
			Recommendation recom = (Recommendation)pm.find(id, Recommendation.class);
			logger.debug("Recommendation {} :\n{}", id, recom);
			return recom;
		} catch (Exception e) {
			logger.error("getRecommendation: EXCEPTION THROWN: {}", e);
			logger.debug("getRecommendation: Returning an empty instance of {}", Recommendation.class);
			return new Recommendation();
		}
	}

	// GET /opt/recommendation/sc/{scId}/profile/{profileId}/request
	// Description: Requests a new recommendation list for the specified profile and user
	@GET
	@Path("/sc/{scId}/profile/{profileId}/request")
	@Produces("application/json")
	public Recommendation[] requestNewRecommendations(@PathParam("scId") String scId, @PathParam("profileId") String profileId) {
		logger.info("requestNewRecommendations: Calling Recommendation Manager to generate new recommendation(s)...");
		RecommendationManager rm = RecommendationManager.getInstance();
		Recommendation recom = rm.createNewRecommendation(scId, profileId, true);	// true: force recommendation creation, because we've a request from GUI
		logger.info("requestNewRecommendations: Calling Recommendation Manager to generate new recommendation(s)... done");
		logger.info("requestNewRecommendations: Recommendation returned: {}", recom!=null ? recom.getId() : null);
		logger.debug("requestNewRecommendations: Recommendation details: {}", recom);
		if (recom!=null) {
			Recommendation[] list = new Recommendation[1];
			list[0] = recom;
			return list;
		} else {
			Recommendation[] list = new Recommendation[0];
			return list;
		}
	}
	
	
	// PUT /opt/recommendation/{recom_id}
	// Description: Create a recommendation
	@PUT
	@Path("/{recom_id}")
	@Consumes("application/json")
	public Response createRecommendation(Recommendation recom) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("createRecommendation: Creating a new Recommendation with id = {}", recom.getId());
			logger.debug("Clearing active flag from existing recommendations of profile: {}", recom.getProfile());
			clearProfileRecommendations(recom.getOwner(), recom.getProfile());
			//
			logger.debug("New recommendation values:\n{}", recom);
			recom.setActive(true);
			pm.persist(recom);
			logger.debug("Object added to RDF persistent store");
			return createResponse(HTTP_STATUS_CREATED, "Result=Created");
		} catch (Exception e) {
			logger.error("createRecommendation: EXCEPTION THROWN: {}", e);
			logger.debug("createRecommendation: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}

	// POST /opt/recommendation/sc/{scId}/profile/{profileId}/clear-recommendations
	// Description: Clear active flags of all recommendations pertaining to the specified profile
	@POST
	@Path("/sc/{scId}/profile/{profileId}/clear-recommendations")
	@Produces("application/json")
	public Response clearProfileRecommendations(@PathParam("scId") String scId, @PathParam("profileId") String profileId) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			
			String recomRdfType = pm.getClassRdfType(Recommendation.class);
			String profileUri = pm.getFieldUri(Recommendation.class, "profile");
			String ownerUri = pm.getFieldUri(Recommendation.class, "owner");
			String activeUri = pm.getFieldUri(Recommendation.class, "active");
			String queryStr = "SELECT ?s WHERE { ?s  a  <"+recomRdfType+"> . ?s <"+ownerUri+"> \"%s\"^^<http://www.w3.org/2001/XMLSchema#string> . ?s <"+profileUri+"> \"%s\"^^<http://www.w3.org/2001/XMLSchema#string> . ?s <"+activeUri+"> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> }";
			
			logger.debug("clearProfileRecommendations: Retrieving active Recommendations for profile: {}", profileId);
			List<Object> list = pm.findByQuery( String.format(queryStr, scId, profileId) );
			logger.debug("{} recommendations found", list.size());
			
			for (Object o : list) {
				Recommendation r = (Recommendation)o;
				logger.debug("Clearing active flag of recommendation: {}, previous flag value = {}", r.getId(), r.isActive());
				r.setActive(false);
				pm.merge(r);
				logger.debug("Recommendation state persistened: {}", r.getId());
			}
			logger.debug("Persistent store state updated");
			
			return createResponse(HTTP_STATUS_OK, "Result=Cleared");
		} catch (Exception e) {
			logger.error("clearProfileRecommendations: EXCEPTION THROWN: {}", e);
			logger.debug("clearProfileRecommendations: Returning an empty array of {}", Recommendation.class);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
	
	public Response clearRecommendation(String recomId) {
		logger.debug("clearRecommendation: Clearing active flag of recommendation with id: {}", recomId);
		return _setRecommendationActiveFlag(recomId, false);
	}
	
	public Response setRecommendation(String recomId) {
		logger.debug("setRecommendation: Setting active flag of recommendation with id: {}", recomId);
		return _setRecommendationActiveFlag(recomId, true);
	}
	
	protected Response _setRecommendationActiveFlag(String recomId, boolean active) {
		try {
			logger.debug("Retrieving Recommendation : {}", recomId);
			Recommendation recom = getRecommendation(recomId);
			if (recom==null || recom.getId()==null || recom.getId().trim().isEmpty()) {
				logger.debug("Recommendation not found: {}", recomId);
				return createResponse(HTTP_STATUS_ERROR, "Result=Recommendation not found: "+recomId);
			}
			logger.debug("Recommendation retrieved : {}", recom);
			
			if (recom.isActive()==active) {
				logger.debug("Recommendation active flag needs no change: id={}, active={}", recomId, active);
				return createResponse(HTTP_STATUS_OK, "Result=Ok");
			}
			
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			if (active) {
				logger.debug("Retrieving recommendations with active flags of profile: {}", recom.getProfile());
				Recommendation[] list = getRecommendations(recom.getOwner(), recom.getProfile());
				if (list!=null) {
					for (int i=0; i<list.length; i++) {
						String rid = list[i].getId();
						if (rid.equals(recomId)) continue;
						logger.debug("Clearing active flags of recommendation : {}", rid);
						list[i].setActive(false);
						pm.attach(list[i]);
						pm.merge(list[i]);
						logger.debug("Recommendation state persistened: {}", rid);
					}
				}
			}
			
			logger.debug("Setting recommendation active flag: id={}, active={}", recomId, active);
			recom.setActive(active);
			
			pm.attach(recom);
			pm.merge(recom);
			logger.debug("Recommendation state persistened: {}", recomId);
			
			return createResponse(HTTP_STATUS_OK, "Result=Active flag "+(active?"set":"cleared"));
		} catch (Exception e) {
			logger.error("EXCEPTION THROWN: {}", e);
			logger.debug("Returning an empty array of {}", Recommendation.class);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
		
	// POST /opt/recommendation/{recom_id}/clear
	// Description: Clear a recommendation's active flag
	@POST
	@Path("/{recom_id}/clear")
	@Produces("application/json")
	public Response clearProfileRecommendations(@PathParam("recom_id") String id) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("clearRecommendation: Retrieving Recommendation with id = {}", id);
			Recommendation recom = (Recommendation)pm.find(id, Recommendation.class);
			logger.debug("Clearing recommendation active flag: previous value = {}", recom.isActive());
			recom.setActive(false);
			pm.merge(recom);
			logger.debug("Persistent store state updated\n");
			return createResponse(HTTP_STATUS_OK, "Result=Cleared");
		} catch (Exception e) {
			logger.error("clearRecommendation: EXCEPTION THROWN: {}", e);
			logger.debug("clearRecommendation: Returning an empty instance of {}", Recommendation.class);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}

	// POST /opt/recommendation/{recom_id}
	// Description: Update the response status of a recommendation (accept, reject, unknown)
	@POST
	@Path("/{recom_id}")
	@Consumes("application/json")
	public Response updateRecommendation(@PathParam("recom_id") String id, Recommendation recom) {
//XXX: TODO: DISCUSS WITH VERGI
		/*try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("updateRecommendation: Updating Recommendation with id = {}", id);
			logger.debug("New attribute values:\n{}", attr);
			pm.attach(recom);
			logger.debug("Object attached to RDF persistent store manager");
			pm.merge(recom);
			logger.debug("Persistent store state updated\n");
			return createResponse(HTTP_STATUS_OK, "Result=Updated");
		} catch (Exception e) {
			logger.error("updateRecommendation: EXCEPTION THROWN: {}", e);
			logger.debug("updateRecommendation: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}*/
//XXX: TODO: DISCUSS WITH VERGI
		logger.debug("updateRecommendation: ** NOT SUPPORTED **");
		return createResponse(HTTP_STATUS_OK, "** NOT SUPPORTED **");
	}

	// POST /opt/recommendation/item/{item_id}/{response}
	// Description: Update the response status of a recommendation item (accept, reject, unknown)
	@POST
	@Path("/item/{item_id}/{response}")
	@Consumes("application/json")
	public Response updateResponse(@PathParam("item_id") String id, @PathParam("response") String response) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("updateResponse: Updating response of Recommendation Item with id = {}  to {}", id, response);
			logger.debug("New response value:\n{}", response);
			logger.debug("updateResponse: Retrieving Recommendation Item with id = {}", id);
			RecommendationItem item = (RecommendationItem)pm.find(id, RecommendationItem.class);
			item.setResponse(response);
			logger.debug("Recommendation Item {} new values :\n{}", id, item);
			pm.merge(item);
			logger.debug("Persistent store state updated");
			return createResponse(HTTP_STATUS_OK, "Result=Updated");
		} catch (Exception e) {
			logger.error("updateResponse: EXCEPTION THROWN: {}", e);
			logger.debug("updateResponse: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}

	// DELETE /opt/recommendation/{recom_id}
	// Description: Delete a recommendation 
	@DELETE
	@Path("/{recom_id}")
	public Response deleteRecommendation(@PathParam("recom_id") String id) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("deleteRecommendation: Deleting Recommendation with id = {}", id);
			Object o = pm.find(id, Recommendation.class);
			if (o==null) {
				logger.error("deleteRecommendation: Recommendation not found");
				return createResponse(HTTP_STATUS_OK, "Result=Not Found");
			}
			logger.debug("Object retrieved from RDF persistent store: {}", o);
			pm.remove(o);
			logger.debug("Object deleted from RDF persistent store");
			return createResponse(HTTP_STATUS_OK, "Result=Deleted");
		} catch (Exception e) {
			logger.error("deleteRecommendation: EXCEPTION THROWN: {}", e);
			logger.debug("deleteRecommendation: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
}