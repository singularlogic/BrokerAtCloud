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
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import javax.ws.rs.client.Entity;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import eu.brokeratcloud.opt.OptimisationAttribute;
import eu.brokeratcloud.opt.ServiceCategoryAttribute;
import eu.brokeratcloud.opt.type.TFN;
import eu.brokeratcloud.persistence.annotations.RdfSubject;


@Path("/gui/admin")
public class AdminFacingComponent extends AbstractFacingComponent {

	public AdminFacingComponent() throws IOException {
		loadConfig();
	}
	
	public AdminFacingComponent(String propertiesFile) throws IOException {
		loadConfig(propertiesFile);
	}
	
	
// ===============================================================================================================
	
	@GET
	@Path("/exportBrokerPolicy")
	@Produces("text/turtle;charset=utf-8")
	public String exportBrokerPolicy() {
		logger.debug("exportBrokerPolicy: BEGIN: n/a");
		
		ResteasyClient client = new ResteasyClientBuilder().build();
		String url = this.configProperties.getProperty("triplestore-query");
		logger.trace("exportBrokerPolicy: Querying export service: {}", url);
		ResteasyWebTarget target = client.target(url);
		
		Response response = response = target.request().get();
		int status = response.getStatus();
		logger.trace("exportBrokerPolicy: Response Status: {} - {}", status, response.getStatusInfo());
		logger.trace("exportBrokerPolicy: Response Type  : {}", response.getMediaType());
		
		String str = "";
		if (status>=200 && status<300) {
			str = response.readEntity( String.class );
		}
		response.close();
		
		logger.debug("exportBrokerPolicy: END: \n{}", str);
		return str;
	}
	
	@GET
	@POST
	@Path("/get-all-attributes/")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed("admin")
	public String getAllAttributes(@Context HttpServletRequest request) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- getAllAttributes: INPUT: n/a");
		
		// Call REST service in order to get attribute list
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(baseUrl+"/opt/attributes/all");
		long callStartTm = System.currentTimeMillis();
		Response response = target.request().get();
		OptimisationAttribute[] atList = response.readEntity( OptimisationAttribute[].class );
		long callEndTm = System.currentTimeMillis();
		logger.debug("Response:  {}", response.getStatus());
		response.close();
		
		// Prepare JSON to retrurn to page
		StringBuilder sb = new StringBuilder("[");
		String comma = " ";
		for (OptimisationAttribute oa : atList) {
			if (oa==null) continue;
			sb.append(comma); comma = ", ";
			String parent = oa.getParent()!=null && oa.getParent().getId()!=null && !oa.getParent().getId().trim().isEmpty() ? oa.getParent().getId() : "#";
			int p = parent.lastIndexOf('#');
			if (!parent.equals("#")) parent = parent.substring(p+1);
			sb.append( String.format("{ \"id\":\"%s\", \"text\":\"%s\", \"parent\":\"%s\" }", oa.getId(), oa.getName(), parent) );
		}
		sb.append("]");
		
		String str = sb.toString();
		
		logger.info("-------------- getAllAttributes: OUTPUT: {}", str);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, callEndTm-callStartTm);
		return str;
	}
	
	@GET
	@POST
	@Path("/get-attribute/{attr_id}")
	@Produces("application/json")
	@RolesAllowed("admin")
	public OptimisationAttribute getAttribute(@PathParam("attr_id") String id) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- getAttribute: INPUT: {}", id);
		
		// Call REST service in order to get an attribute's data
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(baseUrl+"/opt/attributes/"+id);
		long callStartTm = System.currentTimeMillis();
		Response response = target.request().get();
		OptimisationAttribute atOut = response.readEntity( OptimisationAttribute.class );
		long callEndTm = System.currentTimeMillis();
		logger.debug("Response: {}", response.getStatus());
		response.close();
		
		logger.info("-------------- getAttribute: OUTPUT: {}", atOut);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, callEndTm-callStartTm);
		return atOut;
	}
	
	@POST
	@Path("/save-attribute")
	@Consumes("application/json")
	@Produces("application/json")
	@RolesAllowed("admin")
	public OptimisationAttribute saveAttribute(OptimisationAttribute atIn) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- saveAttribute: INPUT: {}", atIn);
		atIn.setLastUpdateTimestamp(new java.util.Date());
		String id = atIn.getId();
		if (id==null || id.trim().isEmpty()) {
			// Fail...
			// return Response...
		}
		
		// Call REST service for update
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(baseUrl+"/opt/attributes/"+id);
		long callStartTm = System.currentTimeMillis();
		Response response = target.request().post( Entity.json(atIn) );
		long callEndTm = System.currentTimeMillis();
		logger.debug("Response: {}", response.getStatus());
		
		OptimisationAttribute atOut = atIn;
		logger.info("-------------- saveAttribute: OUTPUT: {}", atOut);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, callEndTm-callStartTm);
		return atOut;
	}
	
	@GET
	@Path("/delete-attribute/{attr_id:.+}")
	@RolesAllowed("admin")
	public Response deleteAttribute(@PathParam("attr_id") String id) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- deleteAttribute: INPUT: {}", id);
		
		// Check if cascade
		boolean cascade = false;
		if (id.endsWith("/cascade")) {
			id = id.substring(0,id.lastIndexOf('/'));
			cascade = true;
		}
		
		// Call REST service in order to get an attribute's data
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(baseUrl+"/opt/attributes/"+id);	// Cascade delete NOT IMPLEMENTED
		long callStartTm = System.currentTimeMillis();
		Response response = target.request().delete();
		long callEndTm = System.currentTimeMillis();
		logger.debug("Response: {}", response.getStatus());
		response.close();
		
		logger.info("-------------- deleteAttribute: OUTPUT: n/a");
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, callEndTm-callStartTm);
		return Response.status(200).entity("Response:  "+response.getStatus()+"\nDuration:  "+(endTm-startTm)).build();
	}
	
	@POST
	@Path("/create-attribute")
	@Consumes("application/json")
	@Produces("application/json")
	@RolesAllowed("admin")
	public Response createAttribute(OptimisationAttribute atIn) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- createAttribute: INPUT: {}", atIn);
		atIn.setLastUpdateTimestamp(new java.util.Date());
		String id = atIn.getId();
		if (id==null || id.trim().isEmpty()) {
			// generate a new Id
			id = "attr-"+UUID.randomUUID();
			atIn.setId(id);
			logger.info("New attribute Id:  {}", id);
		}
		
		// Set parent
		OptimisationAttribute parent = atIn.getParent();
		if (parent!=null) {
			atIn.setParent(parent);
			logger.debug("Parent set to:  {}", parent.getId());
		}
		
		// Set creation date
		atIn.setCreateTimestamp(new Date());
		
		// Call REST service for update
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(baseUrl+"/opt/attributes/");
		long callStartTm = System.currentTimeMillis();
		Response response = target.request().put( Entity.json(atIn) );
		long callEndTm = System.currentTimeMillis();
		int status = response.getStatus();
		logger.debug("Response:  {}", status);
		logger.debug("Response-entity:  {}", response.getEntity());
		
		logger.info("-------------- createAttribute: OUTPUT: n/a");
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms   contacting WS: {}ms", endTm-startTm, callEndTm-callStartTm);
		return Response.status(status).entity("{ \"response\":  \""+response.getStatus()+"\", \"duration\":  \""+(endTm-startTm)+"\", \"entity\": \""+response.getEntity()+"\" }").build();
	}
	
	
// ===============================================================================================================
	
	@GET
	@Path("/category-attribute-mappings/{cat_id}")
	@Produces("application/json;charset=UTF-8")
	@RolesAllowed("admin")
	public String getServiceCategoryAttributes(@PathParam("cat_id") String catId) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- getServiceCategoryAttributes: INPUT: service-category={}", catId);
		
		// Call REST service in order to get service category attributes
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(baseUrl+"/opt/service-category/"+catId+"/attributes");
		long callStartTm = System.currentTimeMillis();
		Response response = target.request().get();
		ServiceCategoryAttribute[] scaList = response.readEntity( ServiceCategoryAttribute[].class );
		long callEndTm = System.currentTimeMillis();
		logger.debug("Response: {}, Duration: {}", response.getStatus(), callEndTm-callStartTm);
		response.close();
		
		// order SCA list by name
		Arrays.sort(scaList, new Comparator<ServiceCategoryAttribute>() {
			public int compare(ServiceCategoryAttribute sca1, ServiceCategoryAttribute sca2) {
				return sca1.getName().compareToIgnoreCase( sca2.getName() );
			}
		});
		
		// Prepare JSON to retrurn to page
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;
		String[] types = { "NUMERIC_INC", "NUMERIC_DEC", "NUMERIC_RANGE", "BOOLEAN", "UNORDERED_SET", "FUZZY_INC", "FUZZY_DEC", "FUZZY_RANGE", "LINGUISTIC" };
		String fmtCommon = "{ \"rownum\" : \"%d\", \"id\" : \"%s\", \"aid\" : \"%s\", \"name\" : \"%s\", \"type\" : \"%s\", \"unit\" : \"%s\", \"mandatory\" : %s, \"labelEn\" : \"%s\", \"labelDe\" : \"%s\", \"comment\" : \"%s\", \"measuredBy\" : \"%s\", ";
		String fmtNum = " \"from\" : \"%s\", \"to\" : \"%s\" }";
		String fmtFuzzy = " \"fromL\" : \"%s\", \"from\" : \"%s\", \"fromU\" : \"%s\", \"toL\" : \"%s\", \"to\" : \"%s\", \"toU\" : \"%s\" }";
		String fmtText = " \"from\" : \"%s\" }";
		int typesLen = types.length;
		int row = 1;
		for (ServiceCategoryAttribute sca : scaList) {
			logger.debug("'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''");
			logger.debug("SCA={}", sca);
			logger.debug(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
			if (first) first=false; else sb.append(", \n");
			String typ = sca.getType();
			
			String id = sca.getId();
			String attrId = sca.getAttribute();
			String attrName = sca.getName();	// Note: As a convenience we use 'ServiceCategoryAttribute.name' to convey the 'OptimisationAtttibute.name' of the referenced opt. attribute
			String unit = sca.getUnit();
			unit = (unit!=null && !unit.trim().isEmpty()) ? unit.trim() : "";
			boolean mandatory = sca.getMandatory();
			
			// RootObject properties
			String labelEn = sca.getLabelEn();
			String labelDe = sca.getLabelDe();
			String comment = sca.getComment();
			String measuredBy = sca.getMeasuredBy();
			if (labelEn==null) labelEn = "";
			if (labelDe==null) labelDe = "";
			if (comment==null) comment = "";
			if (measuredBy==null) measuredBy = "";
			
			sb.append( String.format(fmtCommon, row++, java.net.URLEncoder.encode(_jsonVal(id)), _jsonVal(attrId), _jsonVal(attrName), _jsonVal(typ), _jsonVal(unit), 
												(!mandatory ? "false" : "true"), _jsonVal(labelEn), _jsonVal(labelDe), _jsonVal(comment), _jsonVal(measuredBy)) );
			if (ServiceCategoryAttribute.isNumericType(typ)) {
				double m = sca.getMin();
				double M = sca.getMax();
				sb.append( String.format(fmtNum, _jsonVal(m), _jsonVal(M)) );
			} else 
			if (ServiceCategoryAttribute.isFuzzyType(typ)) {
				double ml = Double.NEGATIVE_INFINITY;
				double mm = Double.NEGATIVE_INFINITY;
				double mu = Double.NEGATIVE_INFINITY;
				double Ml = Double.POSITIVE_INFINITY;
				double Mm = Double.POSITIVE_INFINITY;
				double Mu = Double.POSITIVE_INFINITY;
				TFN m = sca.getFmin();
				TFN M = sca.getFmax();
				if (m!=null) {
					ml = m.getLowerBound();
					mm = m.getMeanValue();
					mu = m.getUpperBound();
				}
				if (M!=null) {
					Ml = M.getLowerBound();
					Mm = M.getMeanValue();
					Mu = M.getUpperBound();
				}
				sb.append( String.format(fmtFuzzy, _jsonVal(ml),_jsonVal(mm),_jsonVal(mu), _jsonVal(Ml),_jsonVal(Mm),_jsonVal(Mu)) );
			} else 
			if (ServiceCategoryAttribute.isBooleanType(typ)) {
				String[] terms = sca.getTerms();
				String labels = "";
				if (terms!=null) {
					boolean _first = true;
					for (int k=0, n=terms.length; k<n; k++) { if (_first) _first=false; else labels+=","; labels+=_jsonVal(terms[k]); }
				}
				if (labels.trim().isEmpty()) labels = "No, Yes";
				sb.append( String.format(fmtText, labels) );
			} else 
			if (ServiceCategoryAttribute.isUnorderedSetType(typ)) {
				String[] members = sca.getMembers();
				String labels = "";
				if (members!=null) {
					boolean frst = true;
					for (int k=0, n=members.length; k<n; k++) { if (frst) frst=false; else labels+=","; labels+=_jsonVal(members[k]); }
				}
				sb.append( String.format(fmtText, labels) );
			} else 
			if (ServiceCategoryAttribute.isLinguisticType(typ)) {
				String[] terms = sca.getTerms();
				String labels = "";
				if (terms!=null) {
					boolean frst = true;
					for (int k=0, n=terms.length; k<n; k++) { if (frst) frst=false; else labels+=","; labels+=_jsonVal(terms[k]); }
				}
				sb.append( String.format(fmtText, labels) );
			} else {
				throw new RuntimeException("UNKNOWN TYPE: "+typ);
			}
		}

		sb.append(" ]");
		String str = sb.toString();
		
		logger.info("-------------- getServiceCategoryAttributes: OUTPUT: {}", str);
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms", endTm-startTm);
		
		return str;
	}
	
	protected static String _jsonVal(String str) {
		return str.replace(",", ".");
	}
	protected static String _jsonVal(double num) {
		return Double.toString(num).replace(",", ".");
	}
	
	@POST
	@Path("/category-attribute-mappings/{cat_id}")
	@Consumes("application/json")
	@Produces("application/json;charset=UTF-8")
	@RolesAllowed("admin")
	public String saveServiceCategoryAttributes(@PathParam("cat_id") String catId, GridItem item) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- saveServiceCategoryAttributes: INPUT: cat_id={},  Item: {}", catId, item);
		
		if (item==null || ServiceCategoryAttribute.isUnknownType(item.getType())) {
			throw new RuntimeException("NO DATA SENT!!!");
		}
		
		// Prepare Service Category Attribute object
		// get service category
		String category = catId;
		
		// get attribute
		String attribute = item.getAid();
		
		//ServiceCategoryAttribute.TYPE type = ServiceCategoryAttribute.TYPE.valueOf( item.getType() );
		String type = item.getType();
		
		// create SCA object
		ServiceCategoryAttribute sca = new ServiceCategoryAttribute();
		sca.setId( item.getId() );
		sca.setServiceCategory( category );
		sca.setAttribute( attribute );
		sca.setType( type );
		sca.setUnit( item.getUnit() );
		sca.setMandatory( item.getMandatory() );
		
		// set allowed values constraints
		if (item.getFrom()!=null && !item.getFrom().trim().isEmpty()) {
			if (ServiceCategoryAttribute.isNumericType(type)) {
				sca.setMin( Double.valueOf(item.getFrom()) );
				sca.setMax( Double.valueOf(item.getTo()) );
			} else
			if (ServiceCategoryAttribute.isFuzzyType(type)) {
				TFN min = new TFN( item.getFromL(), Double.parseDouble(item.getFrom()), item.getFromU() );
				TFN max = new TFN( item.getToL(), item.getTo(), item.getToU() );
				sca.setFmin( min );
				sca.setFmax( max );
			} else
			if (ServiceCategoryAttribute.isBooleanType(type)) {
				String[] tmp1 = item.getFrom().split("[,;]");
				String[] tmp2 = null;
				if (tmp1==null || tmp1.length==0) { tmp2 = new String[2]; tmp2[0]="No"; tmp2[1]="Yes"; }
				else if (tmp1.length==1) { tmp2 = new String[2]; tmp2[0]=tmp1[0]; tmp2[1]="Yes"; }
				else if (tmp1.length==2) { tmp2 = tmp1; }
				else { tmp2 = new String[2]; tmp2[0]=tmp1[0]; tmp2[1]=tmp1[1]; }
				if (tmp2[0]==null || tmp2[0].trim().isEmpty()) tmp2[0] = "No";
				if (tmp2[1]==null || tmp2[1].trim().isEmpty()) tmp2[1] = "Yes";
				sca.setTerms( tmp2 );
			} else
			if (ServiceCategoryAttribute.isUnorderedSetType(type)) {
				sca.setMembers( item.getFrom().split("[,;]") );
			} else
			if (ServiceCategoryAttribute.isLinguisticType(type)) {
				sca.setTerms( item.getFrom().split("[,;]") );
			} else {
				// UNKNOWN TYPE
				throw new RuntimeException("UNKNOWN TYPE: "+type);
			}
		}
		
		// Copy RootObject properties
		sca.setLabelEn(item.getLabelEn());
		sca.setLabelDe(item.getLabelDe());
		sca.setComment(item.getComment());
		sca.setMeasuredBy(item.getMeasuredBy());
		
		// Call REST service for update
		String id = item.getId();
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(baseUrl+"/opt/service-category/attributes");
		long callStartTm = System.currentTimeMillis();
		Response response = null;
		if (id==null || id.trim().isEmpty()) {	// Create - PUT
			response = target.request().put( Entity.json(sca) );
		} else {									// Update - POST
			response = target.request().post( Entity.json(sca) );
		}
		long callEndTm = System.currentTimeMillis();
		logger.debug("Response:  {} : {} - {}", response.getStatus(), response.getStatusInfo().getFamily(), response.getStatusInfo().getReasonPhrase());
		
		logger.info("-------------- saveServiceCategoryAttributes: OUTPUT: n/a");
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms", endTm-startTm);
		
		logger.info("-------------- CALLING: getServiceCategoryAttributes...");
		
		return getServiceCategoryAttributes(catId);
	}
	
	@DELETE
	@Path("/category-attribute-mappings/{id}")
	@Produces("text/plain")
	@RolesAllowed("admin")
	public Response deleteServiceCategoryAttributes(@PathParam("id") String id) throws IOException {
		long startTm = System.currentTimeMillis();
		logger.info("-------------- deleteServiceCategoryAttributes: INPUT: id={}", id);
		
		// Call REST service for update
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(baseUrl+"/opt/service-category/attributes/" + java.net.URLEncoder.encode(id) );
		long callStartTm = System.currentTimeMillis();
		Response response = target.request().delete();
		long callEndTm = System.currentTimeMillis();
		logger.debug("Response:  {} : {} - {}", response.getStatus(), response.getStatusInfo().getFamily(), response.getStatusInfo().getReasonPhrase());
		
		logger.info("-------------- deleteServiceCategoryAttributes: OUTPUT: n/a");
		long endTm = System.currentTimeMillis();
		logger.debug("duration={}ms", endTm-startTm);
		return Response.status( response.getStatus() ).entity("Duration:  "+(endTm-startTm)).build();
	}
	
// ===============================================================================================================
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class OptAttr extends eu.brokeratcloud.common.BrokerObject {
		@XmlAttribute
		protected String parent;
		
		public static OptAttr transcribeToOptAttr(OptimisationAttribute attr) {
			OptAttr oa = new OptAttr();
			oa.setId(attr.getId());
			oa.setName(attr.getName());
			oa.setDescription(attr.getDescription());
			oa.setCreateTimestamp(attr.getCreateTimestamp());
			oa.setLastUpdateTimestamp(attr.getLastUpdateTimestamp());
			oa.setOwner(attr.getOwner());
			oa.parent = attr.getParent().getId();
			return oa;
		}
		
		public static OptimisationAttribute transcribeToOptimisationAttribute(OptAttr oa, OptimisationAttribute parent) {
			OptimisationAttribute attr = new OptimisationAttribute();
			attr.setId(oa.getId());
			attr.setName(oa.getName());
			attr.setDescription(oa.getDescription());
			attr.setCreateTimestamp(oa.getCreateTimestamp());
			attr.setLastUpdateTimestamp(oa.getLastUpdateTimestamp());
			attr.setOwner(oa.getOwner());
			attr.setParent(parent);
			return attr;
		}
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class GridItem extends eu.brokeratcloud.common.RootObject {
		@XmlAttribute
		String id;			// This is the Key!!!
		@XmlAttribute
		String aid;			// Attribute Id
		@XmlAttribute
		String name;		// Attribute name
		@XmlAttribute
		String type;		// Attribute type (numeric, fuzzy, boolean etc)
		@XmlAttribute
		String unit;		// Unit of measurement (if any)
		@XmlAttribute
		String measuredBy;		// measurement by (if any)
		@XmlAttribute
		boolean mandatory;	// Is mandatory? (Admin setting overrides users' setting. If true attribute must always be included in consumer preferences, when pref. profile regards the same service category)
		@XmlAttribute
		String from;
		@XmlAttribute
		double fromL = Double.NEGATIVE_INFINITY;
		@XmlAttribute
		double fromU = Double.NEGATIVE_INFINITY;
		@XmlAttribute
		double to = Double.POSITIVE_INFINITY;
		@XmlAttribute
		double toL = Double.POSITIVE_INFINITY;
		@XmlAttribute
		double toU = Double.POSITIVE_INFINITY;
		@XmlAttribute
		boolean fromInf;
		@XmlAttribute
		boolean toInf;
		
		protected void _correctFromField() {
			try {
				if (from!=null) {
					if (from.trim().isEmpty() && (type.startsWith("FUZZY") || type.startsWith("NUMERIC") || type.equals("RANGE"))) {
						from = "-9218868437227405312";
					}
				} else {
					from = "-9218868437227405312";
				}
			} catch (Exception e) {
				logger.error("AdminFacingComponent.GridItem._correctFromField: EXCEPTION: {}", e);
			}
		}
		
		public String getId() { return id; }
		public String getAid() { return aid; }
		public String getName() { return name; }
		public String getType() { return type; }
		public String getUnit() { return unit; }
		public String getMeasuredBy() { return measuredBy; }
		public boolean getMandatory() { return mandatory; }
		public String getFrom() { return from; }
		public double getFromL() { return fromL; }
		public double getFromU() { return fromU; }
		public double getTo() { return to; }
		public double getToL() { return toL; }
		public double getToU() { return toU; }
		public void setId(String id) { this.id = id; }
		public void setAid(String aid) { this.aid = aid; }
		public void setName(String name) { this.name = name; }
		public void setType(String type) { this.type = type; _correctFromField(); }
		public void setUnit(String u) { this.unit = u; }
		public void setMeasuredBy(String s) { this.measuredBy = s; }
		public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }
		public void setFrom(String from) { this.from = from; _correctFromField(); }
		public void setFromL(double fromL) { this.fromL = fromL; }
		public void setFromU(double fromU) { this.fromU = fromU; }
		public void setTo(double to) { this.to = to; }
		public void setToL(double toL) { this.toL = toL; }
		public void setToU(double toU) { this.toU = toU; }
		
		public boolean getFromInf() { return fromInf; }
		public boolean getToInf() { return toInf; }
		public void setFromInf(boolean b) { fromInf = b; }
		public void setToInf(boolean b) { toInf = b; }
		
		public String toString() {
			if (type==null || type.trim().isEmpty()) return "GRID-ITEM: MISSING TYPE: id="+id;
			type = type.trim();
			String rootStr = super.toString();
			String common = String.format("%s => GRID-ITEM: id=%s, aid=%s, name=%s, type=%s, unit=%s, measured-by=%s, mandatory=%b,", rootStr, id, aid, name, type, unit, measuredBy, mandatory);
			if (type.equals("FUZZY_INC") || type.equals("FUZZY_DEC") || type.equals("FUZZY_RANGE"))
				return String.format("%s from=(%f, %s, %f), to=(%f, %f, %f)", common, fromL, from, fromU, toL, to, toU);
			if (type.equals("NUMERIC_INC") || type.equals("NUMERIC_DEC") || type.equals("RANGE"))
				return String.format("%s from=%s, to=%f", common, from, to);
			return String.format("%s from=%s", common, from);
		}
	}
}