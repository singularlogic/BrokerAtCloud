package eu.brokeratcloud.rest.gui;

import java.io.*;
import java.util.*;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import javax.ws.rs.client.Entity;

import eu.brokeratcloud.opt.ConsumerPreferenceProfile;
import eu.brokeratcloud.opt.Recommendation;
import eu.brokeratcloud.opt.RecommendationItem;
import eu.brokeratcloud.persistence.annotations.RdfSubject;
import eu.brokeratcloud.rest.opt.ProfileManagementService;


@Path("/gui/recommendations")
public class RecommendationsComponent extends AbstractFacingComponent {

	public RecommendationsComponent(@Context HttpServletRequest request) throws IOException {
		super(request);
		loadConfig();
	}
	
	public RecommendationsComponent(String propertiesFile) throws IOException {
		loadConfig(propertiesFile);
	}
	
	// =====================================================================================================
	
	@GET
	@Path("/list")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public String getRecommendations() throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- getRecommendations: INPUT: n/a");
		
		// Call REST service in order to get profiles list
		long callStartTm = System.currentTimeMillis();
		Recommendation[] recomList = (Recommendation[])_callBrokerRestWS(baseUrl+"/opt/recommendation/sc/"+getUsername(), "GET", java.lang.reflect.Array.newInstance(Recommendation.class, 0).getClass(), null);
		long callEndTm = System.currentTimeMillis();
		long duration = callEndTm-callStartTm;
		
		// Prepare JSON to retrurn to page
		StringBuilder sb = new StringBuilder("[");
		String rowHead = "{ \"id\":\"%s\", \"createTimestamp\":%d, \"profile-name\":\"%s\", \"profile\":\"%s\", \"items\":[";
		String rowItem = "{ \"id\":\"%s\", \"suggestion\":\"%s\", \"relevance\":%.2f, \"order\":%d, \"response\":\"%s\" }";
		String rowTail = "] }";
		boolean first = true;
		for (int i=0; i<recomList.length; i++) {
			Recommendation rec = recomList[i];
			if (rec==null || rec.getId()==null || rec.getId().isEmpty()) continue;  // i.e. rec is null or empty
			
			// get profile information
			String profileId = rec.getProfile();
			String consumer = rec.getOwner();  //or  (rec.getOwner!=null && !rec.getOwner().isEmpty()) ? rec.getOwner() : getUsername();
			long callStartTm2 = System.currentTimeMillis();
			ConsumerPreferenceProfile profile = (ConsumerPreferenceProfile)_callBrokerRestWS(baseUrl+"/opt/profile/sc/"+consumer+"/profile/"+profileId, "GET", ConsumerPreferenceProfile.class, null);
			long callEndTm2 = System.currentTimeMillis();
			duration += callEndTm2-callStartTm2;
			
			if (profile==null) continue;  // if profile is null then probably this user is not the profile owner
			
			// append recommendation info to string buffer
			if (first) first=false; else sb.append(",\n");
			long tm = rec.getCreateTimestamp()!=null ? rec.getCreateTimestamp().getTime() : new Date().getTime();
			sb.append( String.format(Locale.US, rowHead, rec.getId(), tm, profile.getName(), rec.getProfile()) );
			if (rec.getItems()!=null) {
				boolean first2 = true;
				int ord = 0;
				for (RecommendationItem item : rec.getItems()) {
					if (first2) first2=false; else sb.append(",\n");
					
					String iid = item.getId();
					String suggestion = item.getSuggestion();
					double relevance = 100*item.getWeight();
					String response = item.getResponse();
					
					sb.append( String.format(Locale.US, rowItem, iid, suggestion, relevance, ord++, response) );
				}
			}
			sb.append( rowTail );
		}
		sb.append(" ]");
		String str = sb.toString();
		
		logger.info("-------------- getRecommendations: OUTPUT: {}", str);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, duration);
		return str;
	}
	
	@GET
	@Path("/{profile_id}/list")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public String getRecommendationsForProfile(@PathParam("profile_id") String profileId) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- getRecommendationsForProfile: INPUT: profile={}", profileId);
		
		// Call REST service in order to get profiles list
		long callStartTm = System.currentTimeMillis();
		Recommendation[] recomList = (Recommendation[])_callBrokerRestWS(baseUrl+"/opt/recommendation/sc/"+getUsername()+"/profile/"+profileId, "GET", java.lang.reflect.Array.newInstance(Recommendation.class, 0).getClass(), null);
		long callEndTm = System.currentTimeMillis();
		long duration = callEndTm-callStartTm;
		
		// Prepare JSON to retrurn to page
		Object[] retVal = _prepareRecomListJson(profileId, recomList);
		String str = (String)retVal[0];
		duration += (Long)retVal[1];
		
		logger.info("-------------- getRecommendationsForProfile: OUTPUT: {}", str);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, duration);
		return str;
	}
	
	@GET
	@Path("/{profile_id}/request")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public String requestRecommendationsForProfile(@PathParam("profile_id") String profileId) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- requestRecommendationsForProfile: INPUT: profile={}", profileId);
		
		// Call REST service in order to get profiles list
		long callStartTm = System.currentTimeMillis();
		Recommendation[] recomList = (Recommendation[])_callBrokerRestWS(baseUrl+"/opt/recommendation/sc/"+getUsername()+"/profile/"+profileId+"/request", "GET", java.lang.reflect.Array.newInstance(Recommendation.class, 0).getClass(), null);
		long callEndTm = System.currentTimeMillis();
		long duration = callEndTm-callStartTm;
		
		// Prepare JSON to retrurn to page
		Object[] retVal = _prepareRecomListJson(profileId, recomList);
		String str = (String)retVal[0];
		duration += (Long)retVal[1];
		
		// cache recommendation in session
		HttpSession session = request.getSession();
		if (session!=null) {
			HashMap<String,Recommendation> cache = (HashMap<String,Recommendation>)session.getAttribute("cached-recommendations");
			if (cache==null) {
				cache = new HashMap<String,Recommendation>();
				session.setAttribute("cached-recommendations", cache);
			}
			if (recomList.length>0 && recomList[0]!=null) cache.put(profileId, recomList[0]);
			else cache.remove(profileId);
		}
		
		logger.info("-------------- requestRecommendationsForProfile: OUTPUT: {}", str);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, duration);
		return str;
	}
	
	protected Object[] _prepareRecomListJson(String profileId, Recommendation[] recomList) {
		// Prepare JSON to retrurn to page
		StringBuilder sb = new StringBuilder("[");
		long duration = 0;
		// process recommendations
		String recomHeadFmt = "{'id':'%s','createTimestamp':%d,'profile-name':'%s','items':[";
		String recomItemFmt = "{'id':'%s','suggestion':'%s','relevance':%.2f,'order':%d,'response':'%s', 'extra':%s}";
		String recomTail = "]}";
		boolean first = true;
		for (int i=0; i<recomList.length; i++) {
			if (first) first=false; else sb.append(",");
			
			// get profile information
			long callStartTm2 = System.currentTimeMillis();
			ConsumerPreferenceProfile profile = (ConsumerPreferenceProfile)_callBrokerRestWS(baseUrl+"/opt/profile/sc/"+getUsername()+"/profile/"+profileId, "GET", ConsumerPreferenceProfile.class, null);
			long callEndTm2 = System.currentTimeMillis();
			duration += callEndTm2-callStartTm2;
			
			Recommendation recom = recomList[i];
			if (recom==null || recom.getId()==null || recom.getId().trim().isEmpty()) continue;
			
			// prepapre recommendation head
			String rcId = recom.getId();
			Date rcCrTm = recom.getCreateTimestamp();
			long rcMillis = rcCrTm!=null ? rcCrTm.getTime() : 0;
			String pfName = profile.getName();
			List<RecommendationItem> items = recom.getItems(); 
			sb.append( String.format(Locale.US, recomHeadFmt, rcId, rcMillis, pfName) );
			// prepare recommendation items
			boolean first2=true;
			int j=0;
			if (items!=null) {
				for (RecommendationItem rit : items) {
					if (first2) first2=false; else sb.append(",");
					
					String iid = rit.getId();
					String suggestion = rit.getSuggestion();
					double relevance = 100*rit.getWeight();
					String response = rit.getResponse().trim();
					String extra = extra2str( rit.getExtra() );
					sb.append( String.format(Locale.US, recomItemFmt, iid, suggestion, relevance, j++, response, extra) );
				}
			}
			// prepapre recommendation tail
			sb.append( recomTail );
		}
		sb.append("]");
		String str = sb.toString();
		str = str.replace("'","\"");
		
		Object[] retVal = new Object[2];
		retVal[0] = str;
		retVal[1] = new Long(duration);
		return retVal;
	}
	
	protected String extra2str(Object o) {
		if (o==null) {
			return "";
		} else
		if (o instanceof java.util.List) {
			List attrs = (List)o;
			StringBuilder sb = new StringBuilder("[");
			String tpl = "{'attribute':'%s', 'value':'%s'}";
			boolean first = true;
			for (Object el : attrs) {
				if (el instanceof java.util.List) {
					List row = (List)el;
					String atName = row.get(0).toString();
					String atValue= row.get(1).toString();
					if (atName.startsWith(".")) continue;	// ignore (.*) attributes
					
					int p1 = atName.indexOf("#Allowed");
					int p2 = atName.lastIndexOf("Value");
					if (p1>=0 && p2>p1+8) atName = atName.substring(p1+8, p2);
					if (first) first=false; else sb.append(", ");
					sb.append( String.format(tpl, atName, atValue) );
				}
			}
			sb.append("]");
			return sb.toString();
		} else {
			return o.toString();
		}
	}
	
	@GET
	@Path("/profile/{profileId}/make-permenent/{recomId}")
	@Produces("text/html")
	@RolesAllowed({"admin","sc"})
	public String makePermenent(@PathParam("profileId") String profileId, @PathParam("recomId") String recomId, @Context HttpServletRequest request) {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- makePermenent: INPUT: profile={}, recom={}", profileId, recomId);
		long duration = -1;
		
		String text = null;
		HttpSession session = request.getSession();
		logger.trace("Session={}", session);
		if (session!=null) {
			logger.trace("Session-Id={}", session.getId());
			HashMap<String,Recommendation> cache = (HashMap<String,Recommendation>)session.getAttribute("cached-recommendations");
			logger.trace("Cache={}", cache);
			if (cache!=null) {
				Recommendation recom = cache.remove(profileId);
				logger.trace("Recom={}", recom);
				if (recom!=null) {
					if (recom.getId().equals(recomId)) {
					logger.trace("Ready to save");
						try {
							// Call REST service in order to get profiles list
							long callStartTm = System.currentTimeMillis();
							_callBrokerRestWS(baseUrl+"/opt/recommendation/"+recomId, "PUT", null, recom);
							logger.trace("Saved");
							long callEndTm = System.currentTimeMillis();
							duration = callEndTm-callStartTm;
							text = "Saved!";
						} catch (Exception e) {
							logger.error("makePermanent: EXCEPTION: ", e);
							text = "Cannot save recommendation. Error occurred: "+e.getMessage();
							Throwable t=e;
							while ((t=t.getCause())!=null) text += ", Cause by: "+e.getMessage();
						}
					} else {
						text = "Cannot save recommendation. A newer one has been generated";
					}
				}
			}
		}
		if (text==null) text = "Cannot save recommendation. Session has expired or reset";
		
		if (text.equals("Saved!")) text = "<font color=\"green\"><b>"+text+"</b></font>";
		else text = "<br/><font color=\"red\"><b>"+text+"</b></font>";
		logger.trace("Returning string={}", text);
		
		logger.info("-------------- makePermenent: OUTPUT: {}", text);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, duration);
		return text;
	}
	
	@GET
	@Path("/{recom_id}")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public Recommendation getRecommendation(@PathParam("recom_id") String recomId) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- getRecommendation: INPUT: n/a");
		
		// Call REST service in order to get profiles list
		long callStartTm = System.currentTimeMillis();
		Recommendation recom = (Recommendation)_callBrokerRestWS(baseUrl+"/opt/recommendation/"+recomId, "GET", Recommendation.class, null);
		long callEndTm = System.currentTimeMillis();
		long duration = callEndTm-callStartTm;
		
		logger.info("-------------- getRecommendation: OUTPUT: {}", recom);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, duration);
		return recom;
	}
	
	
	@POST
	@Path("/{item_id}/{response}")
	@Consumes("application/json")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public Response saveResponse(@PathParam("item_id") String itemId, @PathParam("response") String response) throws IOException {
		long startTm = System.currentTimeMillis();
		long endTm;
		logger.info("-------------- saveResponse: INPUT: item={},  response={}", itemId, response);
		
		long callStartTm=0, callEndTm=0;
		
		try {
			// Call REST service in order to store profile's data
			callStartTm = System.currentTimeMillis();
			_callBrokerRestWS(baseUrl+"/opt/recommendation/item/"+itemId+"/"+response, "POST", null, "");
			callEndTm = System.currentTimeMillis();
		} catch (Exception e) {
			logger.error("-------------- saveResponse: EXCEPTION THROWN", e);
			endTm = System.currentTimeMillis();
			logger.debug("duration={}ms", endTm-startTm);
			return Response.status(500).entity("{\"exception\":\""+e+"\", \"duration\":"+(endTm-startTm)+"}").build();
		}
		
		logger.info("-------------- saveResponse: OUTPUT: n/a");
		endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, callEndTm-callStartTm);
		return Response.status(200).entity("{\"response\":\""+"response.getStatus()"+"\", \"duration\":"+(endTm-startTm)+"}").build();
	}
}