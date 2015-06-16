package eu.brokeratcloud.rest.gui;

import java.io.*;
import java.util.*;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

import eu.brokeratcloud.common.ServiceCategory;
import eu.brokeratcloud.common.ClassificationDimension;
import eu.brokeratcloud.common.ClassificationDimensionScheme;
import eu.brokeratcloud.opt.ComparisonPair;
import eu.brokeratcloud.opt.ConsumerPreference;
import eu.brokeratcloud.opt.ConsumerPreferenceExpression;
import eu.brokeratcloud.opt.ConsumerPreferenceProfile;
import eu.brokeratcloud.opt.OptimisationAttribute;
import eu.brokeratcloud.opt.ServiceCategoryAttribute;
import eu.brokeratcloud.opt.ServiceCategoryAttributesContainer;
import eu.brokeratcloud.opt.policy.PreferenceVariable;
import eu.brokeratcloud.opt.type.TFN;
import eu.brokeratcloud.persistence.annotations.RdfSubject;
import eu.brokeratcloud.rest.opt.ProfileManagementService;
import eu.brokeratcloud.rest.opt.ServiceCategoryAttributeManagementServiceNEW;


@Path("/gui/consumer")
public class ConsumerFacingComponent extends AbstractFacingComponent {

	public ConsumerFacingComponent(@Context HttpServletRequest request) throws IOException {
		super(request);
		loadConfig();
	}
	
	public ConsumerFacingComponent(String propertiesFile) throws IOException {
		loadConfig(propertiesFile);
	}
	
	// Transcribe the local CPP object used in communications with AJAX form, to the actual CPP (that will be sent to Opt.REST WS)
	@DenyAll
	protected ConsumerPreferenceProfile _transcribe(CPP prIn) {
		try {
			eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
			ConsumerPreferenceProfile profile = null;
			try { profile = (ConsumerPreferenceProfile)pm.find( prIn.getId(), ConsumerPreferenceProfile.class); }
			catch (Exception e) { logger.warn("_transcribe: New profile? Exception while looking for profile: {}", prIn.getId()); }
			if (profile==null) profile = new ConsumerPreferenceProfile();
			
			ClassificationDimension[] classifications = null;
			if (prIn.getServiceClassifications()!=null) {
				String[] clsf = prIn.getServiceClassifications().split("[,]");
				classifications = new ClassificationDimension[ clsf.length ];
				int valid = 0;
				for (int i=0; i<clsf.length; i++) {
					try {
						if (clsf[i]!=null && !clsf[i].trim().isEmpty()) classifications[i] = (ClassificationDimension)pm.find( clsf[i].trim(), ClassificationDimension.class );
						if (classifications[i]!=null) valid++;
					} catch (Exception e) { logger.error("_transcribe: Service classification not found: {}", clsf[i]); return null; }
				}
				if (valid != classifications.length) {
					ClassificationDimension[] tmp = new ClassificationDimension[valid];
					for (int i=0, j=0; i<clsf.length; i++) {
						if (classifications[i]!=null) tmp[j++] = classifications[i];
					}
					classifications = tmp;
				}
			}
			
			profile.setId( prIn.getId() );
			profile.setOwner( prIn.getOwner() );
			profile.setName( prIn.getName() );
			profile.setDescription( prIn.getDescription() );
			profile.setCreateTimestamp( prIn.getCreateTimestamp() );
			profile.setLastUpdateTimestamp( prIn.getLastUpdateTimestamp() );
			profile.setSelectionPolicy( prIn.getSelectionPolicy() );
			profile.setServiceClassifications( classifications );
			int order = (prIn.getOrder()==null || prIn.getOrder().trim().isEmpty()) ? 0 : Integer.parseInt( prIn.getOrder() );
			profile.setOrder( order );
			
			return profile;
		} catch (Exception e) {
			logger.error("_transcribe: EXCEPTION CAUGHT: {}", e);
			return null;
		}
	}
	
	// Transcribe the actual CPP (returned from Opt.REST WS) to local CPP object used in communications with AJAX form
	@DenyAll
	protected CPP _reverseTranscribe(ConsumerPreferenceProfile profile) {
		CPP prOut = new CPP();
		prOut.setId( profile.getId() );
		prOut.setOwner( profile.getOwner() );
		prOut.setName( profile.getName() );
		prOut.setDescription( profile.getDescription() );
		prOut.setCreateTimestamp( profile.getCreateTimestamp() );
		prOut.setLastUpdateTimestamp( profile.getLastUpdateTimestamp() );
		prOut.setSelectionPolicy( profile.getSelectionPolicy() );
		String clsfStr = "";
		if (profile.getServiceClassifications()!=null) {
			ClassificationDimension[] classifications = profile.getServiceClassifications();
			String[] clsf = new String[ classifications.length ];
			int valid = 0;
			for (int i=0; i<classifications.length; i++) {
				ClassificationDimension cd = classifications[i];
				if (cd!=null && cd.getId()!=null) {
					clsf[i] = cd.getId();
					valid++;
				}
			}
			if (valid!=classifications.length) {
				String[] tmp = new String[valid];
				for (int i=0,j=0; i<classifications.length; i++) {
					if (clsf[i]!=null) tmp[j++] = clsf[i];
				}
				clsf = tmp;
			}
			StringBuilder sb = new StringBuilder();
			boolean first=true;
			for (String s : clsf) {
				if (first) first=false; else sb.append(",");
				sb.append(s);
			}
			clsfStr = sb.toString();
		}
		prOut.setServiceClassifications( clsfStr );
		prOut.setOrder( Integer.toString( profile.getOrder() ) );
		return prOut;
	}
	
	// =====================================================================================================
	
	@GET
	@POST
	@Path("/profile-list")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public String getProfileList() throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- getProfileList: INPUT: n/a");
		
		// Call REST service in order to get profiles list
		long callStartTm = System.currentTimeMillis();
		CPPShort[] prList = (CPPShort[])_callBrokerRestWS(baseUrl+"/opt/profile/sc/"+getUsername()+"/list", "GET", java.lang.reflect.Array.newInstance(CPPShort.class, 0).getClass(), null);
		long callEndTm = System.currentTimeMillis();
		
		// Prepare JSON to retrurn to page
		StringBuilder sb = new StringBuilder("[");
		String parent = "__root__";
		sb.append( String.format(" { \"id\":\"%s\", \"text\":\"%s\", \"parent\":\"%s\", \"state\":{ \"opened\":true, \"selected\":true } }", parent /*node id*/, "Pref. Profiles", "#") );
		String comma = ", ";
		for (CPPShort cpp : prList) {
			if (cpp==null) continue;
			sb.append(comma);
			sb.append( String.format("{ \"id\":\"%s\", \"text\":\"%s\", \"parent\":\"%s\", \"icon\":\"/forms/consumer/profile-tree-node.jpg\" }", cpp.getId(), cpp.getName(), parent) );
		}
		sb.append(" ]");
		String str = sb.toString();
		
		logger.info("-------------- getProfileList: OUTPUT: {}", str);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", (endTm-startTm), (callEndTm-callStartTm));
		return str;
	}
	
	@GET
	@POST
	@Path("/profile-data/{profile_id}")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public CPP getProfile(@PathParam("profile_id") String id) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- getProfile: INPUT: {}", id);
		
		// Call REST service in order to get profile's data
		long callStartTm = System.currentTimeMillis();
		ConsumerPreferenceProfile profile = (ConsumerPreferenceProfile)_callBrokerRestWS(baseUrl+"/opt/profile/sc/"+getUsername()+"/profile/"+id, "GET", ConsumerPreferenceProfile.class, null);
		long callEndTm = System.currentTimeMillis();
		
		// Transcribe the actual CPP (returned from Opt.REST WS) to local CPP object used in communications with AJAX form
		CPP prOut = null;
		if (profile!=null) prOut = _reverseTranscribe(profile);
		else logger.warn("Profile not found: {}", id);
		
		logger.info("-------------- getProfile: OUTPUT: {}", prOut);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", (endTm-startTm), (callEndTm-callStartTm));
		return prOut;
	}
	
	@POST
	@Path("/profile-save")
	@Consumes("application/json")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public CPP saveProfile(CPP prIn) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- saveProfile: INPUT: {}", prIn);
		prIn.setLastUpdateTimestamp(new java.util.Date());
		boolean newProfile = false;
		
		// Assign a new Id if missing
		String id = prIn.getId();
		if (id==null || id.trim().isEmpty()) {
			logger.warn("saveProfile: Id is missing: A new one Will be generated and assigned");
			id = "CPP-"+UUID.randomUUID();
			logger.info("saveProfile: Setting Id to: {}", id);
			prIn.setId(id);
			newProfile = true;
		}
		
		// Autocomplete owner if missing
		String owner = prIn.getOwner();
		if (owner==null || owner.trim().isEmpty()) {
			logger.warn("saveProfile: Owner is missing: Will be set from session data");
			owner = getUsername();
			logger.info("saveProfile: Setting owner from session info: '{}'", owner);
			if (owner!=null && !owner.trim().isEmpty()) prIn.setOwner(owner);
			else throw new RuntimeException("Could not set profile owner from session data: profile="+id);
		}
		logger.info("-------------- saveProfile: *** CHECK OWNER ***: {}", prIn);
		
		// Transcribe the local CPP object used in communications with AJAX form, to the actual CPP (that will be sent to Opt.REST WS)
		ConsumerPreferenceProfile profile = _transcribe(prIn);
		if (profile==null) {
			logger.info("-------------- saveProfile: OUTPUT: null");
			logger.debug("An error occurred. Profile not saved: profile={}", prIn.getId());
			long endTm = System.currentTimeMillis();
			logger.debug("duration={}ms", (endTm-startTm));
			return null;
		}
		
		// Call REST service in order to store profile's data
		long callStartTm = System.currentTimeMillis();
		if (newProfile)
			_callBrokerRestWS(baseUrl+"/opt/profile/sc/"+getUsername()+"/profile/"+profile.getName(), "PUT", null, profile);
		else
			_callBrokerRestWS(baseUrl+"/opt/profile/sc/"+getUsername()+"/profile/"+id, "POST", null, profile);
		long callEndTm = System.currentTimeMillis();
		
		CPP prOut = prIn;
		logger.info("-------------- saveProfile: OUTPUT: {}", prOut);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", (endTm-startTm), (callEndTm-callStartTm));
		return prOut;
	}
	
	@GET
	@Path("/profile-delete/{profile_id:.+}")
	@RolesAllowed({"admin","sc"})
	public Response deleteProfile(@PathParam("profile_id") String id) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- deleteProfile: INPUT: {}", id);
		
		// Call REST service in order to delete profile
		long callStartTm = System.currentTimeMillis();
		_callBrokerRestWS(baseUrl+"/opt/profile/sc/"+getUsername()+"/profile/"+id, "DELETE", null, null);
		long callEndTm = System.currentTimeMillis();
		
		logger.info("-------------- deleteProfile: OUTPUT: n/a");
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", (endTm-startTm), (callEndTm-callStartTm));
		return Response.status(200).entity("Response:  "+"response.getStatus()"+"\nDuration:  "+(endTm-startTm)).build();
	}
	
	@POST
	@Path("/profile-create")
	@Consumes("application/json")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public CPP createProfile(CPP prIn) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- createProfile: INPUT: {}", prIn);
		prIn.setLastUpdateTimestamp(new java.util.Date());
		String id = prIn.getId();
		if (id==null || id.trim().isEmpty()) {
			// generate a new Id
			id = "CPP-"+UUID.randomUUID();
			prIn.setId(id);
			logger.info("New Preference Profile Id:  {}", id);
		}
		
		// Set creation date
		prIn.setCreateTimestamp(new Date());
		
		// Autocomplete owner if missing
		String owner = prIn.getOwner();
		if (owner==null || owner.trim().isEmpty()) {
			logger.warn("createProfile: Owner is missing: Will be set from session data");
			owner = getUsername();
			logger.info("createProfile: Setting owner from session info: '{}'", owner);
			if (owner!=null && !owner.trim().isEmpty()) prIn.setOwner(owner);
			else throw new RuntimeException("Could not set profile owner from session data: profile="+id);
		}
		logger.info("-------------- createProfile: *** CHECK OWNER ***: {}", prIn);
		
		// Transcribe the local CPP object used in communications with AJAX form, to the actual CPP (that will be sent to Opt.REST WS)
		ConsumerPreferenceProfile profile = _transcribe(prIn);
		if (profile==null) {
			logger.info("-------------- createProfile: OUTPUT: null");
			logger.debug("An error occurred. Profile not saved: profile={}", prIn.getId());
			long endTm = System.currentTimeMillis();
			logger.debug("duration={}ms", (endTm-startTm));
			return null;
		}
		
		// Call REST service in order to store a new profile
		long callStartTm = System.currentTimeMillis();
		_callBrokerRestWS(baseUrl+"/opt/profile/sc/"+getUsername()+"/profile/DUMMY", "PUT", null, profile);
		long callEndTm = System.currentTimeMillis();
		
		CPP prOut = prIn;
		logger.info("-------------- createProfile: OUTPUT: {}", prOut);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", (endTm-startTm), (callEndTm-callStartTm));
		return prOut;
	}
	
// =====================================================================================================
	
	@GET
	@POST
	@Path("/category-list")
	@Produces("application/json")
	public String getCategoryTree() throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- getCategoryTree: INPUT: n/a");
		
		long callStartTm=0, callEndTm=0;
		String str = "";
		try {
				// Get service classification dimensions
				eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
				List<Object> list = null;
				callStartTm=System.currentTimeMillis();
				list = pm.findAll(ClassificationDimension.class);
				//list = pm.findByQuery("SELECT ?s WHERE { ?s a <http://www.linked-usdl.org/ns/usdl-core/cloud-brokerClassificationDimension> }");
				callEndTm=System.currentTimeMillis();
				logger.debug("{} service classification dimensions found", list.size());
				
				// Process service classification dimensions - group by classification dimension scheme
				boolean first = true;
				StringBuilder sb = new StringBuilder("[ ");
				if (list!=null) {
					// Process classification dimensions
					List<ClassificationDimensionScheme> dimList = new LinkedList<ClassificationDimensionScheme>();
					for (Object o : list) {
						ClassificationDimension sc = (ClassificationDimension)o;
						String id = sc.getId();
						if (id==null) { id = pm.getObjectUri(sc); sc.setId(id); }
						String text = sc.getPrefLabel();
						String parent;
						if (sc.getParent()!=null) { parent = sc.getParent().getId(); if (parent==null) { parent = pm.getObjectUri(sc.getParent()); sc.getParent().setId(parent); } }
						else { parent = "#"; }
						if (first) first=false; else sb.append(", ");
						sb.append( String.format("{ 'id':'%s', 'text':'%s', 'parent':'%s' }", id, text, parent) );
					}
				}
				sb.append(" ]");
				str = sb.toString().replace("'", "\"");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}: Returning an empty array of {}", getClass().getName(), ClassificationDimension.class);
		}
		
		logger.info("-------------- getCategoryTree: OUTPUT: {}", str);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", (endTm-startTm), (callEndTm-callStartTm));
		return str;
	}
	
	@GET
	@POST
	@Path("/preference-attributes/category/{cat_id}/profile/{profile_id}")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public String getAllPreferenceAttributes(@PathParam("cat_id") String catId, @PathParam("profile_id") String profileId) throws IOException {
		return _getPreferenceAttributes(catId, profileId, false);
	}
	
	@GET
	@POST
	@Path("/preference-attributes/category/{cat_id}/profile/{profile_id}/selected")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public String getSelectedPreferenceAttributes(@PathParam("cat_id") String catId, @PathParam("profile_id") String profileId) throws IOException {
		return _getPreferenceAttributes(catId, profileId, true);
	}
	
	@DenyAll
	protected String _getPreferenceAttributes(String catId, String profileId, boolean selectedOnly) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- _getPreferenceAttributes: INPUT: service-category={},  profile={},  selected-only={}", catId, profileId, selectedOnly);
		
		// Call REST service in order to get service category attributes list
		long callStartTm = System.currentTimeMillis();
		ServiceCategoryAttributesContainer[] containers = (ServiceCategoryAttributesContainer[])_callBrokerRestWS(baseUrl+"/opt/service-category/"+catId+"/attributes-all", "GET", java.lang.reflect.Array.newInstance(ServiceCategoryAttributesContainer.class, 0).getClass(), null);
		long callEndTm = System.currentTimeMillis();
		
		// Call REST service in order to get profile
		long callStartTm2 = System.currentTimeMillis();
		ConsumerPreferenceProfile profile = (ConsumerPreferenceProfile)_callBrokerRestWS(baseUrl+"/opt/profile/sc/"+getUsername()+"/profile/"+profileId, "GET", ConsumerPreferenceProfile.class, null);
		long callEndTm2 = System.currentTimeMillis();
		ConsumerPreference[] preferences = profile.getPreferences();
		
		// Put consumer preferences into a hashtable
		Hashtable<String,ConsumerPreference> prefs = new Hashtable<String,ConsumerPreference>();
		if (preferences!=null) {
			for (ConsumerPreference p : preferences) {
				String cpid = p.getPrefVariable();
				prefs.put(cpid, p);
			}
		}
		
		// Prepare JSON to return to page
		String rowFormatter = "{ \"id\":\"%s\", \"aid\":\"%s\", \"cpid\":\"%s\", \"selected\":%b, \"name\":\"%s\", \"mandatory\":%b, \"weight\":%f, \"type\":\"%s\", \"category\":\"%s\", \"constraints\":\"%s\", ";
		StringBuilder sb = new StringBuilder("[");
		String comma = ", \n";
		boolean first = true;
		for (ServiceCategoryAttributesContainer sc : containers) {
			if (sc==null) continue;
			String scId = sc.getServiceCategory();
			String scName = sc.getServiceCategoryName();
			ServiceCategoryAttribute[] scaList = sc.getServiceCategoryAttributes();
			if (scaList!=null && scaList.length>0) {
				for (ServiceCategoryAttribute sca : scaList) {
					// Attribute data
					String scaId = sca.getId();			// Mapping id
					String atId = sca.getAttribute();	// Attribute id
					String atName = sca.getName();		// Attribute name
					String cpId = "";					// Consumer preference id
					String type = sca.getType();
					// Consumer preference data
					ConsumerPreference cp = prefs.get( java.net.URLDecoder.decode(scaId) );
					if (selectedOnly && cp==null) continue;
					
					boolean selected = false;
					boolean mandatory = sca.getMandatory();
					double weight = 0;
					ConsumerPreferenceExpression constraint = null;
					String cExpr = "-";
					if (cp!=null) {
						selected = true;
						mandatory = cp.getMandatory();
						weight = cp.getWeight();
						constraint = cp.getExpression();
						if (constraint!=null) cExpr = constraint.getExpression();
						if (cExpr==null) cExpr = "-";
						cpId = cp.getId();
					}
					
					if (first) first=false; else sb.append(comma);
					
					// Common fields
					String scFld = "("+scId+") "+scName;
					sb.append( String.format((java.util.Locale)null, rowFormatter, scaId, atId, cpId, selected, atName, mandatory, weight, type, scFld /*scId*/, cExpr) );
					// Type-specific fields
					if (sca.isNumericType(type)) {
						double from = sca.getMin();
						double to = sca.getMax();
						sb.append( String.format((java.util.Locale)null, "\"from\":\"%f\", \"to\":\"%f\" }",
										from, to ) );
					} else
					if (sca.isFuzzyType(type)) {
						TFN from = sca.getFmin();
						TFN to = sca.getFmax();
						sb.append( String.format((java.util.Locale)null, "\"fromL\":\"%f\", \"from\":\"%f\", \"fromU\":\"%f\", \"toL\":\"%f\", \"to\":\"%f\", \"toU\":\"%f\" }",
										from.getLowerBound(), from.getMeanValue(), from.getUpperBound(), 
										to.getLowerBound(), to.getMeanValue(), to.getUpperBound() ) );
					} else
					if (sca.isBooleanType(type)) {
						String[] terms = sca.getTerms();
						String from = "";
						if (terms!=null) from = Arrays.asList(terms).toString();
						sb.append( String.format((java.util.Locale)null, "\"from\":\"%s\" }", from) );
					} else
					if (sca.isUnorderedSetType(type)) {
						String[] members = sca.getMembers();
						String from = Arrays.asList(members).toString();
						if (from.length()>1) from = from.substring(1, from.length()-1);
						sb.append( String.format((java.util.Locale)null, "\"from\":\"%s\" }", from) );
					} else
					if (sca.isLinguisticType(type)) {
						String[] terms = sca.getTerms();
						String from = Arrays.asList(terms).toString();
						if (from.length()>1) from = from.substring(1, from.length()-1);
						sb.append( String.format((java.util.Locale)null, "\"from\":\"%s\" }", from) );
					} else {
						sb.append( String.format((java.util.Locale)null, "\"from\":\"UNKNOWN TYPE: %s\" }", type) );
					}
				}
			}
		}
		sb.append(" ]");
		String str = sb.toString();
		
		logger.info("-------------- _getPreferenceAttributes: OUTPUT: {}", str);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", (endTm-startTm), (callEndTm2-callStartTm));
		return str;
	}

	@POST
	@Path("/preference-attributes/profile/{profile_id}/save")
	@Consumes("application/json")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public Response saveSelectedAttributes(@PathParam("profile_id") String profileId, String[] attrIds) throws IOException {
		long startTm = System.currentTimeMillis();
		long endTm;
		logger.info("-------------- saveSelectedAttributes: INPUT: profile={}  entries={}", profileId, attrIds.length);
		
		long callStartTm=0, callStartTm2, callStartTm3, callEndTm, callEndTm2, callEndTm3=0;
		
		try {
			// Keep service category attribute (SCA) keys only once
			HashMap<String,String> prefs = new HashMap<String,String>();
			for (int i=0; i<attrIds.length; i++) {
				prefs.put(attrIds[i],"");
			}
			
			// Call REST service in order to get profile's data
			callStartTm = System.currentTimeMillis();
			ConsumerPreferenceProfile profile = (ConsumerPreferenceProfile)_callBrokerRestWS(baseUrl+"/opt/profile/sc/"+getUsername()+"/profile/"+profileId, "GET", ConsumerPreferenceProfile.class, null);
			callEndTm = System.currentTimeMillis();
			
			// Get current profile preferences
			ConsumerPreference[] currPreferences = profile.getPreferences();
			HashMap<String,ConsumerPreference> currPrefs = new HashMap<String,ConsumerPreference>();
			for (ConsumerPreference cp : currPreferences) {
				currPrefs.put(cp.getPrefVariable(), cp);
			}
			
			
			// Update consumer preference profile with selected attributes
			callStartTm2 = System.currentTimeMillis();
			ConsumerPreference[] preferences = new ConsumerPreference[prefs.size()];
			int nn=0;
			for (String attrId : prefs.keySet()) {
				logger.debug("\tProcessing consumer preference: {}", attrId);
				
				if (currPrefs.containsKey(attrId)) {
					preferences[nn++] = currPrefs.get(attrId);
					currPrefs.remove(attrId);
				} 
				else {	// Not contains attribute
					String attrIdEnc = java.net.URLEncoder.encode( attrId );
					ServiceCategoryAttribute attr = (ServiceCategoryAttribute)_callBrokerRestWS(baseUrl+"/opt/service-category/attributes/"+attrIdEnc, "GET", ServiceCategoryAttribute.class, null);
					
					// get preference variable from SCA
					String pvUri = java.net.URLDecoder.decode(attr.getId());
					logger.trace("saveSelectedAttributes: SCA id={}", pvUri);
					
					// create new consumer preference
					ConsumerPreference tmp = new ConsumerPreference();
					tmp.setId("PREFERENCE-"+UUID.randomUUID());
					tmp.setCreateTimestamp(new Date());
					tmp.setPrefVariable( pvUri );
					preferences[nn++] = tmp;
				}
			}
			profile.setPreferences(preferences);
			profile.setLastUpdateTimestamp(new Date());
			
			callEndTm2 = System.currentTimeMillis();
			
			// Call REST service in order to store profile's data
			callStartTm3 = System.currentTimeMillis();
			_callBrokerRestWS(baseUrl+"/opt/profile/sc/"+getUsername()+"/profile/"+profileId, "POST", null, profile);
			callEndTm3 = System.currentTimeMillis();
			
		} catch (Exception e) {
			endTm = System.currentTimeMillis();
			logger.error("-------------- saveSelectedAttributes: EXCEPTION THROWN", e);
			logger.debug("duration={}ms", (endTm-startTm));
			return Response.status(500).entity("{\"exception\":\""+e+"\", \"duration\":"+(endTm-startTm)+"}").build();
		}
		
		logger.info("-------------- saveSelectedAttributes: OUTPUT: n/a");
		endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", (endTm-startTm), (callEndTm3-callStartTm));
		return Response.status(200).entity("{\"response\":\""+"response.getStatus()"+"\", \"duration\":"+(endTm-startTm)+"}").build();
	}
	
	@POST
	@Path("/preference-attributes/profile/{profile_id}/preferences/save")
	@Consumes("application/json")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public Response savePreferences(@PathParam("profile_id") String profileId, ConsumerPrefItem[] preferences) throws IOException {
		long startTm = System.currentTimeMillis();
		long endTm;
		logger.info("-------------- savePreferences: INPUT: profile={}  preferences={}", profileId, preferences.length);
		
		long callStartTm=0, callStartTm2, callEndTm, callEndTm2=0;
		
		try {
			ConsumerPreferenceProfile profile = null;
			{
				eu.brokeratcloud.rest.opt.ProfileManagementService profileMgntWS = new eu.brokeratcloud.rest.opt.ProfileManagementService();
				logger.debug("savePreferences: HACK to retrieve consumer preference profile: id = {}", profileId);
				profile = profileMgntWS.getProfile(getUsername(), profileId);
			}
			
			// Hash current profile preferences
			ConsumerPreference[] currPreferences = profile.getPreferences();
			HashMap<String,ConsumerPreference> currPrefs = new HashMap<String,ConsumerPreference>();
			for (ConsumerPreference cp : currPreferences) {
				currPrefs.put(cp.getId(), cp);
			}
			
			// Update consumer preferences
			logger.debug("Updating profile preferences: {}", profileId);
			for (ConsumerPrefItem item : preferences) {
				String cpid = item.getCpid();
				logger.debug("\tUpdating preference: {}", cpid);
				ConsumerPreference pref = currPrefs.get(cpid);
				// Update consumer preference parameters
				pref.setWeight( item.getWeight() );
				pref.setMandatory( item.getMandatory() );
				// update constraints expression
				String strExpr = item.getConstraints();
				if (strExpr!=null) strExpr = strExpr.trim();
				if (strExpr==null || strExpr.isEmpty() /*|| strExpr.equals("-")*/ ) {
					pref.setExpression( null );
				} else {
					ConsumerPreferenceExpression expr = pref.getExpression();
					if (expr==null) {
						expr = new ConsumerPreferenceExpression();
						expr.setId("EXPRESSION-"+UUID.randomUUID());
						expr.setConsumerPreference(pref);
						pref.setExpression(expr);
					}
					expr.setExpression( strExpr );
				}
				// update last updated timestamp
				pref.setLastUpdateTimestamp(new Date());
			}
			logger.debug("Updating profile preferences: {} done", profileId);
			profile.setWeightCalculation(false);
			logger.debug("No weight calculation will occur");
			
			// Call REST service in order to store profile's data
			logger.debug("Storing profile: {}", profileId);
			callStartTm2 = System.currentTimeMillis();
			_callBrokerRestWS(baseUrl+"/opt/profile/sc/"+getUsername()+"/profile/"+profileId, "POST", null, profile);
			callEndTm2 = System.currentTimeMillis();
			logger.debug("Storing profile: {} done", profileId);
			
		} catch (Exception e) {
			endTm = System.currentTimeMillis();
			logger.error("-------------- savePreferences: EXCEPTION THROWN", e);
			logger.debug("duration={}ms", (endTm-startTm));
			return Response.status(500).entity("{\"exception\":\""+e+"\", \"duration\":"+(endTm-startTm)+"}").build();
		}
		
		logger.info("-------------- savePreferences: OUTPUT: n/a");
		endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", (endTm-startTm), (callEndTm2-callStartTm));
		return Response.status(200).entity("{\"response\":\""+"response.getStatus()"+"\", \"duration\":"+(endTm-startTm)+"}").build();
	}
	
	@GET
	@POST
	@Path("/preference-attributes/pairs/profile/{profile_id}")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public Vector<GridComparisonPair> getComparisonPairs(@PathParam("profile_id") String profileId) throws IOException {
		long startTm = System.currentTimeMillis();
		long endTm;
		logger.info("-------------- getComparisonPairs: INPUT: profile={}", profileId);
		
		long callStartTm, callEndTm;
		long sumDuration = 0, cntCalls = 0;
		
		// Call REST service in order to get preference data
		logger.debug("Retrieving profile: {}", profileId);
		ConsumerPreferenceProfile profile = null;
		{
			eu.brokeratcloud.rest.opt.ProfileManagementService profileMgntWS = new eu.brokeratcloud.rest.opt.ProfileManagementService();
			logger.debug("savePreferences: HACK to retrieve consumer preference profile: id = {}", profileId);
			profile = profileMgntWS.getProfile(getUsername(), profileId);
		}
		cntCalls++;
		logger.debug("Retrieving profile: {} done", profileId);
		
		// Convert preferences into attributes hierarchy
		logger.debug("Converting preferences to attribute hierarchy: \n{} ", java.util.Arrays.deepToString(profile.getPreferences()));
		ConsumerPreference[] currPreferences = profile.getPreferences();
		HashMap<String,OptimisationAttribute> cache = new HashMap<String,OptimisationAttribute>();
		HashMap<String,HashMap> root = new HashMap<String,HashMap>();
		Stack<String> path = new Stack<String>();
		for (ConsumerPreference cp : currPreferences) {
			// get attribute id from preference
			String pvUri = cp.getPrefVariable();
			ServiceCategoryAttributeManagementServiceNEW.PolicyObjects po = ServiceCategoryAttributeManagementServiceNEW.getBrokerPolicyObjects(pvUri, false);
			logger.trace("getComparisonPairs: policy objects={}", po);
			if (po==null) continue;
			PreferenceVariable pv = po.pv;
			String attrId = pv.getRefToServiceAttribute().getId();
			
			// retrieve parent attributes
			while (attrId!=null) {
				// retrieve attribute
				OptimisationAttribute attr = null;
				if (cache.containsKey(attrId)) {
					attr = cache.get(attrId);
				} else {
					callStartTm = System.currentTimeMillis();
					attr = (OptimisationAttribute)_callBrokerRestWS(baseUrl+"/opt/attributes/"+attrId, "GET", OptimisationAttribute.class, null);
					callEndTm = System.currentTimeMillis();
					sumDuration += (callEndTm-callStartTm);
					cntCalls++;
					cache.put(attr.getId(), attr);
				}
				// store attribute in path
				path.push(attrId);
				// get parent attribute id
				if (attr.getParent()!=null) {
					attrId = attr.getParent().getId();
				} else {
					attrId = null;
				}
			}
			// store path attributes into tree hierarchy (creating path if needed)
			HashMap<String,HashMap> prev, curr = root;
			while (path.size()>0) {
				String atId = path.pop();
				prev = curr;
				curr = (HashMap<String,HashMap>)curr.get(atId);
				if (curr==null) prev.put(atId, curr=new HashMap<String,HashMap>());
			}
		}
		logger.debug("Converting preferences to attribute hierarchy: done\n{} ", root);
		
		// Generate pair reasons (ie why a pair was generated. Mentions child attributes that cause a parent attribute comparison)
		logger.debug("Generating pair reasons:");
		HashMap<String,String> reasons = new HashMap<String,String>();
		if (root.size()>0) {
			String rootId = root.keySet().iterator().next().toString();
			HashMap rootNode = (HashMap)root.get(rootId);
			_generateReasonsForNode("", rootNode, cache, reasons);
		}
		logger.debug("Generating pair reasons: done\nREASONS: {}", reasons);
		
		// Retrieve stored pairwise comparison values and transcribe them into a hashmap
		logger.debug("Transcribing stored comparison pairs to hashmap: \n{}", profile.getComparisonPairs());
		ComparisonPair[] prevPairs = profile.getComparisonPairs();
		HashMap<String,HashMap<String,String>> values = new HashMap<String,HashMap<String,String>>();
		for (ComparisonPair pr : prevPairs) {
			String at1 = pr.getAttribute1();
			String at2 = pr.getAttribute2();
			String val = pr.getValue();
			
			HashMap<String,String> hm1 = values.get(at1);
			if (hm1==null) values.put(at1, hm1=new HashMap<String,String>());
			hm1.put(at2, val);
			HashMap<String,String> hm2 = values.get(at2);
			if (hm2==null) values.put(at2, hm2=new HashMap<String,String>());
			hm2.put(at1, val);
		}
		logger.debug("Transcribing stored comparison pairs to hashmap: done\n{}", values);
		
		// Generate comparison pairs per non-leaf node
		logger.debug("Generate comparison pairs per node: ");
		Vector<GridComparisonPair> pairs = new Vector<GridComparisonPair>();
		_generatePairsForNode("", root, pairs, cache, values, reasons);
		for (int i=0, n=pairs.size(); i<n; i++) pairs.elementAt(i).setId("PAIR-"+i);
		logger.debug("Generate comparison pairs per node: done\n{}", pairs);
		
		logger.info("-------------- getComparisonPairs: OUTPUT: {} pairs", pairs.size());
		endTm = System.currentTimeMillis();
		logger.debug("duration={}ms  contacting WS (cumulative): {}ms in {} calls", endTm-startTm, sumDuration, cntCalls);
		
		return pairs;
	}
	
	@DenyAll
	protected void _generateReasonsForNode(String nodeId, HashMap<String,HashMap> nodes, HashMap<String,OptimisationAttribute> cache, HashMap<String,String> reasons) {
		logger.trace("### _generateReasonsForNode: BEGIN: node-id={}", nodeId);
		
		if (nodeId==null || nodeId.isEmpty()) {
			// Update reason for root node
			logger.trace("### _generateReasonsForNode: ROOT-NODE: ");
			StringBuilder sb = new StringBuilder();
			String reason = null;
			if (reason!=null) sb.append(reasons);
			for (String id : nodes.keySet()) {
				String name = cache.get(id).getName();
				logger.trace("### _generateReasonsForNode: ROOT-NODE: LOOP-1: Append attrib: id={}, attr-name={}", id, name);
				sb.append(", ").append(name);
			}
			reason = sb.toString();
			if (reason.startsWith(", ")) reason = reason.substring(2);
			logger.trace("### _generateReasonsForNode: ROOT-NODE: Update reason: id={}, new-reason={}", nodeId, reason);
			reasons.put(nodeId, reason);
			
			// Recursive call for each child node
			for (String id : nodes.keySet()) {
				logger.trace("### _generateReasonsForNode: ROOT-NODE: LOOP-2: Recursive CALL to _generateReasonsForNode for nodeId={}", id);
				_generateReasonsForNode(id, nodes, cache, reasons);
			}
			return;
		}
		
		HashMap hm = nodes.get(nodeId);
		logger.trace("### _generateReasonsForNode: hm={}", hm);
		if (hm==null) return;
		logger.trace("### _generateReasonsForNode: hm-size={}", hm.size());
		if (hm.size()==0) return;
		
		if (hm.size()==1) {
			// find node with more than one children nodes (ie traverse down a chain of nodes until one has >1 children)
			// if none exists, select the leaf (bottom-most) node
			logger.trace("### _generateReasonsForNode: ONE: {}", hm.size());
			HashMap prev = hm;
			String hmId = nodeId;
			String prevId = nodeId;
			while (hm!=null && hm.size()==1) {
				logger.trace("### _generateReasonsForNode: ONE: LOOP START: hm-id={}, hm={}", hmId, hm);
				prev = hm;
				prevId = hmId;
				hmId = hm.keySet().iterator().next().toString();
				hm = (HashMap)hm.get(hmId);
				logger.trace("### _generateReasonsForNode: ONE: LOOP END: hm-id={}, hm={}", hmId, hm);
			}
			logger.trace("### _generateReasonsForNode: ONE: AFTER LOOP: hm-id={}, hm={}", hmId, hm);
			if (hm!=null && hm.size()>1) {
				// found a node with many children
				logger.trace("### _generateReasonsForNode: ONE: Recursive call of _generateReasonsForNode with nodeId={}", hmId);
				_generateReasonsForNode(hmId, hm, cache, reasons);
			}
			if (hm.size()==0) {
				// found leaf node
				logger.trace("### _generateReasonsForNode: ONE: LEAF NODE 1: node-id={}, attr-name={}", nodeId, cache.get(hmId).getName());
				reasons.put(nodeId, cache.get(hmId).getName());
			}
			if (hm==null) {
				// parent (in 'prev' variable) is leaf node
				logger.trace("### _generateReasonsForNode: ONE: LEAF NODE 2: node-id={}, attr-name={}", nodeId, cache.get(prevId).getName());
				reasons.put(nodeId, cache.get(prevId).getName());
			}
			return;
		}
		
		if (hm.size()>1) {
			StringBuilder sb = new StringBuilder();
			String reason = reasons.get(nodeId);
			if (reason!=null) sb.append(reasons);
			logger.trace("### _generateReasonsForNode: MANY: Previous reason={}", reason);
			for (Object o : hm.keySet()) {
				String id = o.toString();
				String name = cache.get(id).getName();
				
				logger.trace("### _generateReasonsForNode: MANY: LOOP: Recursive CALL for nodeId={}", id);
				_generateReasonsForNode(id, hm, cache, reasons);
				String innerAttrName = reasons.get(id);
				
				logger.trace("### _generateReasonsForNode: MANY: LOOP: Recursive CALL for nodeId={}  ==>  Result: {}", id, innerAttrName);
				if (innerAttrName!=null) name = innerAttrName;
				
				logger.trace("### _generateReasonsForNode: MANY: LOOP: id={}, attr-name={}", id, name);
				sb.append(", ").append(name);
			}
			reason = sb.toString();
			if (reason.startsWith(", ")) reason = reason.substring(2);
			logger.trace("### _generateReasonsForNode: MANY: Update reason: id={}, new-reason={}", nodeId, reason);
			reasons.put(nodeId, reason);
		}
	}
	
	@DenyAll
	protected void _generatePairsForNode(String nodeId, HashMap<String,HashMap> nodes, Vector<GridComparisonPair> pairs, HashMap<String,OptimisationAttribute> cache, HashMap<String,HashMap<String,String>> values, HashMap<String,String> reasons) {
		// Get path to parent node 'nodeId'
		OptimisationAttribute oa = cache.get(nodeId);
		StringBuilder sb = new StringBuilder("/");
		while (oa!=null) {
			if (oa.getParent()==null) break;	// omit root attribute. Comment out to include root attribute (useful if a multi-root attribute scheme is used)
			String attrId = oa.getParent().getId();
			if (attrId==null) break;	// omit root attribute. Comment out to include root attribute (useful if a multi-root attribute scheme is used)
			
			// prepend attribute to path
			sb.insert(0, oa.getName());
			sb.insert(0, "/");
			
			// get parent attribute
			if (attrId!=null && !attrId.isEmpty()) oa = cache.get(attrId);
			else oa = null;
		}
		String parent = sb.toString().trim();
		
		// Generate comparison pairs for node 'nodeId'
		Set<String> tmp = nodes.keySet();
		int N = tmp.size();
		String[] attrIds = tmp.toArray(new String[N]);
		if (parent!=null && N>1) {
			for (int i=0; i<N-1; i++) {
				String id1 = attrIds[i];
				String name1 = cache.get(id1).getName();
				for (int j=i+1; j<N; j++) {
					String id2 = attrIds[j];
					String name2 = cache.get(id2).getName();
					String val = null;
					if (values.containsKey(id1)) {
						HashMap<String,String> hm = values.get(id1);
						if (hm!=null) val = hm.get(id2);
					} else {
						HashMap<String,String> hm = values.get(id2);
						if (hm!=null) val = hm.get(id1);
					}
					if (val==null) val = "0";
					
					GridComparisonPair cp = new GridComparisonPair();
					cp.setParent(parent);
					cp.setAttr1_id(id1);
					cp.setAttr2_id(id2);
					cp.setAttr1_name(name1);
					cp.setAttr2_name(name2);
					cp.setAttr1_reason( reasons.get(id1) );
					cp.setAttr2_reason( reasons.get(id2) );
					cp.setValue(val);
					pairs.add(cp);
				}
			}
		}
		
		// Generate pairs for each child of 'nodeId' too
		for (int i=0; i<N; i++) {
			String id = attrIds[i];
			_generatePairsForNode(id, nodes.get(id), pairs, cache, values, reasons);
		}
	}
	
	@DenyAll
	protected String _getAttrName(String attrId) {
		// Call REST service in order to get attribute data
		logger.debug(">> Retrieving attribute: {}", attrId);
		long callStartTm = System.currentTimeMillis();
		OptimisationAttribute attr = (OptimisationAttribute)_callBrokerRestWS(baseUrl+"/opt/attributes/"+attrId, "GET", OptimisationAttribute.class, null);
		long callEndTm = System.currentTimeMillis();
		logger.debug(">> Retrieving attribute: {} done.... duration={}ms", attrId, (callEndTm-callStartTm));
		
		return attr.getName();
	}

	@POST
	@Path("/preference-attributes/profile/{profile_id}/calculate-weights")
	@Produces("application/json")
	@RolesAllowed({"admin","sc"})
	public Response saveComparisonPairs(@PathParam("profile_id") String profileId, GridComparisonPair[] pairs) throws IOException {
		long startTm = System.currentTimeMillis();
		long endTm;
		logger.info("-------------- saveComparisonPairs: INPUT: profile={},  pairs={}", profileId, pairs.length);
		
		long callStartTm=0, callEndTm=0;
		
		// Transcribe ConsumerFacingComponent.GridComparisonPair  to  opt.ComparisonPair
		ComparisonPair[] pairs2 = new ComparisonPair[pairs.length];
		for (int i=0; i<pairs.length; i++) {
			pairs2[i] = new ComparisonPair();
			pairs2[i].setAttribute1(pairs[i].getAttr1_id());
			pairs2[i].setAttribute2(pairs[i].getAttr2_id());
			pairs2[i].setValue(pairs[i].getValue());
		}
		
		// Call REST service in order to store pairs
		callStartTm = System.currentTimeMillis();
		_callBrokerRestWS(baseUrl+"/opt/profile/"+profileId+"/criteria/weights", "POST", null, pairs2);
		callEndTm = System.currentTimeMillis();
		
		logger.info("-------------- saveComparisonPairs: OUTPUT: n/a");
		endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", (endTm-startTm), (callEndTm-callStartTm));
		return Response.status(200).entity("{\"response\":\""+"response.getStatus()"+"\", \"duration\":"+(endTm-startTm)+"}").build();
	}
	
	
// ===============================================================================================================
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class CPP extends eu.brokeratcloud.common.BrokerObject {
		@XmlAttribute
		protected String selectionPolicy;
		@XmlAttribute
		protected String serviceClassifications;
		@XmlAttribute
		protected String order;
		
		public String getSelectionPolicy() { return selectionPolicy; }
		public void setSelectionPolicy(String sp) { selectionPolicy = sp; }
		public String getServiceClassifications() { return serviceClassifications; }
		public void setServiceClassifications(String sc) { serviceClassifications = sc; }
		public String getOrder() { return order; }
		public void setOrder(String o) { order = o; }
		
		public String toString() {
			return 	"CPP: {\n"+super.toString()+
					", selection-policy = "+selectionPolicy+
					"\tservice-classifications = "+serviceClassifications+
					", order="+order+" }\n";
		}
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	protected static class CPPShort {
		@XmlAttribute
		protected String id;
		@XmlAttribute
		protected String name;
		
		public String getId() { return id; }
		public void setId(String s) { id = s; }
		public String getName() { return name; }
		public void setName(String s) { name = s; }
		
		public String toString() {
			return 	"CPPShort: { id="+id+", name="+name+" }\n";
		}
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ConsumerPrefItem {
		@XmlAttribute
		String cpid;		// This is the Key!!!
		@XmlAttribute
		double weight;		// Weight
		@XmlAttribute
		boolean mandatory;	// Mandatory flag
		@XmlAttribute
		String constraints;	// Constraints
		
		public String getCpid() { return cpid; }
		public double getWeight() { return weight; }
		public boolean getMandatory() { return mandatory; }
		public String getConstraints() { return constraints; }
		public void setCpid(String id) { this.cpid = id; }
		public void setWeight(double w) { this.weight = w; }
		public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }
		public void setConstraints(String c) { constraints = c; }
		
		public String toString() {
			return String.format("CONSUMER-PREF-ITEM: cpid=%s, weight=%f, mandatory=%b", cpid, weight, mandatory);
		}
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	@JsonIgnoreProperties(ignoreUnknown = true)
	protected static class GridComparisonPair {
		@XmlAttribute
		protected String id;
		@XmlAttribute
		protected String parent;
		@XmlAttribute
		protected String attr1_id;
		@XmlAttribute
		protected String attr2_id;
		@XmlAttribute
		protected String attr1_name;
		@XmlAttribute
		protected String attr2_name;
		@XmlAttribute
		protected String attr1_reason;
		@XmlAttribute
		protected String attr2_reason;
		@XmlAttribute
		protected String value;

		public GridComparisonPair() {}
		public GridComparisonPair(ConsumerPreference cp1, ConsumerPreference cp2) {}
		
		public String getId() { return id; }
		public void setId(String s) { id = s; }
		public String getParent() { return parent; }
		public void setParent(String s) { parent = s; }
		public String getAttr1_id() { return attr1_id; }
		public void setAttr1_id(String a) { attr1_id = a; }
		public String getAttr2_id() { return attr2_id; }
		public void setAttr2_id(String a) { attr2_id = a; }
		public String getAttr1_name() { return attr1_name; }
		public void setAttr1_name(String a) { attr1_name = a; }
		public String getAttr2_name() { return attr2_name; }
		public void setAttr2_name(String a) { attr2_name = a; }
		public String getAttr1_reason() { return attr1_reason; }
		public void setAttr1_reason(String r) { attr1_reason = r; }
		public String getAttr2_reason() { return attr2_reason; }
		public void setAttr2_reason(String r) { attr2_reason = r; }
		public String getValue() { return value; }
		public void setValue(String v) { value = v; }
		
		public String toString() {
			return 	"GridComparisonPair: { id="+id+", parent="+parent+", attr-1="+attr1_id+"/"+attr1_name+", attr-2="+attr2_id+"/"+attr2_name+", value="+value+" }\n";
		}
	}
}