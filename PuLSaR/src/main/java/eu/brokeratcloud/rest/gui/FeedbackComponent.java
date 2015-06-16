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

import eu.brokeratcloud.common.ServiceDescription;
import eu.brokeratcloud.opt.ConsumerPreferenceProfile;
import eu.brokeratcloud.opt.Recommendation;
import eu.brokeratcloud.opt.RecommendationItem;
import eu.brokeratcloud.opt.ServiceCategoryAttribute;
import eu.brokeratcloud.persistence.annotations.RdfSubject;
import eu.brokeratcloud.rest.opt.ProfileManagementService;


@Path("/gui/feedback")
public class FeedbackComponent extends AbstractFacingComponent {

	protected static final String feedbackConfigFile = "feedback.properties";
	
	protected long RECENT_THRESHOLD = 86400000l;	// approx. millis of a day
	protected String srvUriFormFld = "_SERVICE_URI";
	
	public FeedbackComponent(@Context HttpServletRequest request) throws IOException {
		super(request);
		loadConfig();
		initFeedbackConfig();
	}
	
	public FeedbackComponent(String propertiesFile) throws IOException {
		loadConfig(propertiesFile);
		initFeedbackConfig();
	}
	
	protected void initFeedbackConfig() {
		try {
			logger.debug("Loading feedback properties file: {}", feedbackConfigFile);
			Properties properties = new Properties();
			properties.load( getClass().getClassLoader().getResourceAsStream(feedbackConfigFile) );
			//
			String str = properties.getProperty("gui.RECENT_THRESHOLD");
			RECENT_THRESHOLD = Long.MAX_VALUE;
			if (str!=null && !str.trim().isEmpty()) {
				try { RECENT_THRESHOLD = Long.parseLong( str ); } catch (Exception e) {}
			}
			
			srvUriFormFld = properties.getProperty("gui.SERVICE_URI_FIELD");
			if (srvUriFormFld==null || srvUriFormFld.trim().isEmpty()) {
				srvUriFormFld = "_SERVICE_URI";
			}
		} catch (Exception e) {
			logger.error("Exception during initialization. Using defaults: ", e);
		}
	}
	
	// =====================================================================================================
	
	@GET
	@Path("/used-services-list")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public String getUsedServices() throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- getUsedServices: INPUT: n/a");
		
		// Call REST service in order to get profiles list
		long callStartTm = System.currentTimeMillis();
		ServiceDescription[] sdList = (ServiceDescription[])_callBrokerRestWS(baseUrl+"/opt/feedback/sc/"+getUsername()+"/list-used-services", "GET", java.lang.reflect.Array.newInstance(ServiceDescription.class, 0).getClass(), null);
		long callEndTm = System.currentTimeMillis();
		long duration = callEndTm-callStartTm;
		
		// Prepare JSON to retrurn to page
		StringBuilder sb = new StringBuilder("[");
		String rowTmpl = "{ \"id\":\"%s\", \"lastUsedTimestamp\":%d, \"service-name\":\"%s\", \"service\":\"%s\", \"status\":\"%s\" }";
		boolean first = true;
		for (int i=0; i<sdList.length; i++) {
			ServiceDescription sd = sdList[i];
			if (sd==null || sd.getId()==null || sd.getId().isEmpty()) continue;  // i.e. sd is null or empty
			
			// get service information
			String serviceId = sd.getId();
			String consumer = sd.getOwner();  //or  (sd.getOwner!=null && !sd.getOwner().isEmpty()) ? sd.getOwner() : getUsername();
			
			// append service info to string buffer
			if (first) first=false; else sb.append(",\n");
			long tm = sd.getLastUsedTimestamp()!=null ? sd.getLastUsedTimestamp().getTime() : -1;
			String status = (tm==-1) ? "IN-USE" : ( (new Date().getTime() - tm < RECENT_THRESHOLD) ? "NOT-USED-RECENT" : "NOT-USED-OLD" );
			sb.append( String.format(Locale.US, rowTmpl, sd.getId(), tm, sd.getName(), sd.getId(), status) );
		}
		sb.append(" ]");
		String str = sb.toString();
		
		logger.info("-------------- getUsedServices: OUTPUT: {}", str);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, duration);
		return str;
	}
	
	@GET
	@Path("/sd/{sd_id}")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public String getUsedServiceFeedbackForm(@PathParam("sd_id") String sdId) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- getUsedServiceFeedbackForm: INPUT: service-uri: {}", sdId);
		
		// Call REST service in order to get imprecise attributes list for service
		logger.trace("getUsedServiceFeedbackForm: retrieving imprecise attributes from PuLSaR...");
		long callStartTm = System.currentTimeMillis();
		ServiceCategoryAttribute[] scaArr = (ServiceCategoryAttribute[]) _callBrokerRestWS(baseUrl+"/opt/feedback/sd/"+java.net.URLEncoder.encode(sdId)+"/imprecise-attributes", "GET", java.lang.reflect.Array.newInstance(ServiceCategoryAttribute.class, 0).getClass(), null);
		long callEndTm = System.currentTimeMillis();
		long duration = callEndTm-callStartTm;
		logger.trace("getUsedServiceFeedbackForm: retrieving imprecise attributes from PuLSaR... done");
		
		// Sorting attributes by name
		List<ServiceCategoryAttribute> scaList = Arrays.asList(scaArr);
		Collections.sort(scaList, new Comparator<ServiceCategoryAttribute>() {
			public int compare(ServiceCategoryAttribute sca1, ServiceCategoryAttribute sca2) {
				String name1 = sca1.getName();
				String name2 = sca2.getName();
				if (name1==null) name1 = "";
				if (name2==null) name2 = "";
				return name1.compareTo(name2);
			}
			
			public boolean equals(ServiceCategoryAttribute sca) {
				return false;
			}
		});
		logger.trace("getUsedServiceFeedbackForm: Sorted list of imprecise attributes:\n{}", scaList);
		
		// Call REST service in order to get consumer feedback for service
		logger.trace("getUsedServiceFeedbackForm: retrieving consumer feedback from PuLSaR...");
		callStartTm = System.currentTimeMillis();
		Map<String,String> map = (Map<String,String>) _callBrokerRestWS(baseUrl+"/opt/feedback/sc/"+getUsername()+"/sd/"+java.net.URLEncoder.encode(sdId), "GET", Map.class, null);
		callEndTm = System.currentTimeMillis();
		duration += callEndTm-callStartTm;
		logger.trace("getUsedServiceFeedbackForm: retrieving consumer feedback from PuLSaR... done");
		logger.trace("getUsedServiceFeedbackForm: consumer feedback:\n{}", map);
		
		// Prepare JSON to retrurn to page
		// response template and buffers
		String jsonResponse = "{\n\t\"schema\": {\n%s\t},\n\t\"form\": [\n%s\t],\n\t\"value\": {\n%s\n\t}\n}\n";
		StringBuilder sbSchema = new StringBuilder();
		StringBuilder sbForm = new StringBuilder();
		StringBuilder sbValues = new StringBuilder();
		
		// add header fields
		sbSchema.append( String.format("\t\t\"%s\": {\n\t\t\t\"type\": \"string\",\n\t\t\t\"required\": true\n\t\t}", srvUriFormFld) );
		sbForm.append( String.format("\t\t{\t\"key\": \"%s\",\n\t\t\t\"type\": \"hidden\"\n\t\t}", srvUriFormFld) );
		sbValues.append( String.format("\t\t\"%s\": \"%s\"", srvUriFormFld, sdId) );
		
		// field templates
		String uriSchemaTmpl = ",\n\t\t\"%s\": {\n\t\t\t\"type\": \"string\",\n\t\t\t\"required\": true\n\t\t}";
		String uriFormTmpl = ",\n\t\t{\t\"key\": \"%s\",\n\t\t\t\"type\": \"hidden\"\n\t\t}";
		String uriValueTmpl = ",\n\t\t\"%s\": \"%s\"";
		// text field templates
		String fldSchemaTmpl = ",\n\t\t\"%s\": {\n\t\t\t\"type\": \"%s\",\n\t\t\t\"size\": 10,\n\t\t\t\"title\": \"%s\",\n\t\t\t\"required\": %b\n\t\t}";
		String fldFormTmpl = ",\n\t\t{\t\"key\": \"%s\",\n\t\t\t\"notitle\": %b,\n\t\t\t\"prepend\": \"%s\",\n\t\t\t\"append\": \"%s\",\n\t\t\t\"description\": \"%s\",\n\t\t\t\"htmlClass\": \"%s\",\n\t\t\t\"fieldHtmlClass\": \"%s\"\n\t\t}";
		String fldValueTmpl = ",\n\t\t\"%s\": %s";
		// linguistic field templates
		String fldSchemaTmpl_LINGUISTIC = ",\n\t\t\"%s\": {\n\t\t\t\"type\": \"%s\",\n\t\t\t\"size\": 10,\n\t\t\t\"title\": \"%s\",\n\t\t\t\"required\": %b,\n\t\t\t\"enum\": [ \"\"%s ]\n\t\t}";
		String fldFormTmpl_LINGUISTIC = ",\n\t\t{\t\"key\": \"%s\",\n\t\t\t\"notitle\": %b,\n\t\t\t\"prepend\": \"%s\",\n\t\t\t\"append\": \"%s\",\n\t\t\t\"description\": \"%s\",\n\t\t\t\"htmlClass\": \"%s\",\n\t\t\t\"fieldHtmlClass\": \"%s\",\n\t\t\t\"titleMap\": {\n\t\t\t\t\"\": \"\"%s\n\t\t\t}\n\t\t}";
		String fldValueTmpl_LINGUISTIC = ",\n\t\t\"%s\": %s";
		
		// find max label length
		int maxLabelLength = 30;
		for (ServiceCategoryAttribute sca : scaList) {
			maxLabelLength = (int)Math.max( sca.getName().length(), maxLabelLength);
		}
		
		// add field specs to schema and form buffers
		int i=0;
		for (ServiceCategoryAttribute sca : scaList) {
			// get attribute information and display settings
			i++;
			String id = "ATTR-ID-"+i;
			String id2 = "ATTR-URI-"+i;
			String type = "string";
			String title = sca.getName();
			boolean required = false;
			boolean notitle = true;
			
			// prepare allowed values
			String allowecValues;
			if (sca.isLinguistic()) {
				allowecValues = Arrays.toString( sca.getTerms() );
			} else {	// is fuzzy
				double a = sca.getFmin().getLowerBound();
				double b = sca.getFmin().getMeanValue();
				double c = sca.getFmax().getMeanValue();
				double d = sca.getFmax().getUpperBound();
				allowecValues = String.format("( %f ; %f ; %f ; %f )", a, b, c, d);
			}
			
			// prepare display settings
			int len = maxLabelLength-title.length();
			if (len<1) len = 1;
			String prepend = "<b>"+i+".</b> "+ String.format("%0"+(len)+"d", 0).replaceAll("0", "&nbsp;") +title+" : ";
			String append = "<p>" + (sca.getUnit()!=null ? sca.getUnit() : "") + "</p>";
			String descr = "Allowed values for this "+sca.getType()+" attribute: "+allowecValues;
			String htmlClass = "";
			String fldHtmlClass = "input-large";
			
			logger.trace("getUsedServiceFeedbackForm: preparing form spec for attribute: id={}, name={}, type={}, descr={}", id, title, type, descr);
			
			// get previous/storted feedback value fro attribute (if any)
			String aid = sca.getId();
			String value = map.get(aid);
			if (value==null) value = "";
			value = "\""+value.replace("\"","\\\"")+"\"";
			
			// prepare feedback form specifications for attribute and append them to corresponding buffers
			// (process depending on attribute type)
			if (sca.isLinguistic()) {
				StringBuilder sb1 = new StringBuilder();
				StringBuilder sb2 = new StringBuilder();
				String[] terms = sca.getTerms();
				for (int j=0; j<terms.length; j++) {
					sb1.append(", \"").append(terms[j]).append("\"");
					sb2.append(",\n\t\t\t\t\"").append(terms[j]).append("\": \"").append(terms[j]).append("\"");
				}
				String valuesEnum = sb1.toString();
				String titleMap = sb2.toString();
				sbSchema.append( String.format(fldSchemaTmpl_LINGUISTIC, id, type, title, required, valuesEnum) );
				sbForm.append( String.format(fldFormTmpl_LINGUISTIC, id, notitle, prepend, append, descr, htmlClass, fldHtmlClass, titleMap) );
				sbValues.append( String.format(fldValueTmpl_LINGUISTIC, id, value) );
			} else
			if (sca.isFuzzyRange()) {
				sbSchema.append( String.format(fldSchemaTmpl, id, type, title, required) );
				sbForm.append( String.format(fldFormTmpl, id, notitle, prepend, append, descr, htmlClass, fldHtmlClass) );
				sbValues.append( String.format(fldValueTmpl, id, value) );
			} else {	// sca.isFuzzyInc() || sca.isFuzzyDec()
				sbSchema.append( String.format(fldSchemaTmpl, id, type, title, required) );
				sbForm.append( String.format(fldFormTmpl, id, notitle, prepend, append, descr, htmlClass, fldHtmlClass) );
				sbValues.append( String.format(fldValueTmpl, id, value) );
			}
			
			// prepapre hidden field containing attribute id
			sbSchema.append( String.format(uriSchemaTmpl, id2) );
			sbForm.append( String.format(uriFormTmpl, id2) );
			sbValues.append( String.format(uriValueTmpl, id2, sca.getId()) );
		}
		
		// generate final json response
		sbSchema.append("\n");
		sbForm.append("\n");
		String str = String.format( jsonResponse, sbSchema.toString(), sbForm.toString(), sbValues.toString() );
		
		logger.info("-------------- getUsedServiceFeedbackForm: OUTPUT: {}", str);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, duration);
		return str;
	}
	
	@POST
	@Path("/sd/{sd_id}")
	@Consumes("application/json")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public Response saveFeedback(@PathParam("sd_id") String sdId, Map<String,String> params) throws IOException {
		logger.info("-------------- saveFeedback: INPUT: service-uri={}, param-map={}", sdId, params);
		long startTm=System.currentTimeMillis(), endTm=0;
		long callStartTm=0, callEndTm=0;
		
		// remove service uri from form data
		params.remove( srvUriFormFld );
		
		try {
			// Call REST service in order to store feedback data
			callStartTm = System.currentTimeMillis();
			_callBrokerRestWS(baseUrl+"/opt/feedback/sc/"+getUsername()+"/sd/"+java.net.URLEncoder.encode(sdId), "POST", null, params);
			callEndTm = System.currentTimeMillis();
		} catch (Exception e) {
			logger.error("-------------- saveFeedback: EXCEPTION THROWN", e);
			endTm = System.currentTimeMillis();
			logger.debug("duration={}ms", endTm-startTm);
			return Response.status(500).entity("{\"exception\":\""+e+"\", \"duration\":"+(endTm-startTm)+"}").build();
		}
		
		logger.info("-------------- saveFeedback: OUTPUT: n/a");
		endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, callEndTm-callStartTm);
		return Response.status(200).entity("{\"response\":\"feedback saved\", \"duration\":"+(endTm-startTm)+"}").build();
	}
	
	@DELETE
	@Path("/sd/{sd_id}")
	@Consumes("application/json")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public Response deleteFeedback(@PathParam("sd_id") String sdId) throws IOException {
		logger.info("-------------- deleteFeedback: INPUT: service-uri={}", sdId);
		long startTm=System.currentTimeMillis(), endTm=0;
		long callStartTm=0, callEndTm=0;
		
		try {
			// Call REST service in order to delete stored feedback data
			callStartTm = System.currentTimeMillis();
			_callBrokerRestWS(baseUrl+"/opt/feedback/sc/"+getUsername()+"/sd/"+java.net.URLEncoder.encode(sdId), "DELETE", null, "");
			callEndTm = System.currentTimeMillis();
		} catch (Exception e) {
			logger.error("-------------- deleteFeedback: EXCEPTION THROWN", e);
			endTm = System.currentTimeMillis();
			logger.debug("duration={}ms", endTm-startTm);
			return Response.status(500).entity("{\"exception\":\""+e+"\", \"duration\":"+(endTm-startTm)+"}").build();
		}
		
		logger.info("-------------- deleteFeedback: OUTPUT: n/a");
		endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, callEndTm-callStartTm);
		return Response.status(200).entity("{\"response\":\"feedback deleted\", \"duration\":"+(endTm-startTm)+"}").build();
	}
}