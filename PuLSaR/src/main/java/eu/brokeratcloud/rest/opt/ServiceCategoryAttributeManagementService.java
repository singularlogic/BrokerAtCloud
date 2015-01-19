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

import eu.brokeratcloud.fuseki.FusekiClient;
import eu.brokeratcloud.opt.OptimisationAttribute;
import eu.brokeratcloud.opt.ServiceCategoryAttribute;
import eu.brokeratcloud.opt.ServiceCategoryAttributesContainer;
import eu.brokeratcloud.persistence.RdfPersistenceManager;
import eu.brokeratcloud.persistence.RdfPersistenceManagerFactory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

@Path("/opt/service-category/")
public class ServiceCategoryAttributeManagementService extends AbstractManagementService {

	protected FusekiClient client = null;
	
	// GET /opt/service-category/{cat_id}/attributes/
	// Description: Get a list of service category attributes for a service category
	@GET
	@Path("/{cat_id}/attributes")
	@Produces("application/json")
	public ServiceCategoryAttribute[] getServiceCategoryAttributes(@PathParam("cat_id") String catId) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			
			String scaRdfType = pm.getClassRdfType(ServiceCategoryAttribute.class);
			String fcUri = pm.getFieldUri(ServiceCategoryAttribute.class, "serviceCategory");
			String queryStr = "SELECT ?s WHERE { ?s  a  <"+scaRdfType+"> .  ?s  <"+fcUri+">  \""+catId+"\"^^<http://www.w3.org/2001/XMLSchema#string> }";
			
			logger.debug("getServiceCategoryAttributes: Retrieving service category optimisation attribute: {}", catId);
			List<Object> list = pm.findByQuery(queryStr);
			logger.debug("{} Service Category Attributes found", list.size());
			ServiceCategoryAttribute[] scaList = list.toArray(new ServiceCategoryAttribute[list.size()]);
			for (ServiceCategoryAttribute sca : scaList) {
				String atId = sca.getAttribute();
				OptimisationAttribute oa = (OptimisationAttribute)pm.find(atId, OptimisationAttribute.class);
				if (oa!=null) {
					sca.setName(oa.getName());
				}
			}
			return scaList;
		} catch (Exception e) {
			logger.error("getServiceCategoryAttributes: EXCEPTION THROWN: {}", e);
			logger.debug("getServiceCategoryAttributes: Returning an empty array of {}", ServiceCategoryAttribute.class);
			return new ServiceCategoryAttribute[0];
		}
	}
	
	// GET /opt/service-category/{cat_id}/attributes-all
	// Description: Get a list of service category attributes for a service category as well as the attributes of its parent categories
	@GET
	@Path("/{cat_id}/attributes-all")
	@Produces("application/json")
	public ServiceCategoryAttributesContainer[] getServiceCategoryAttributesALL(@PathParam("cat_id") String catId) throws java.io.IOException {
		if (client==null) client = new FusekiClient();
		
		logger.debug("getServiceCategoryAttributesALL: Retrieving service category optimisation attribute for ALL categories in hierarchy: {}", catId);
		
		// Retrieve service category hierarchy
		String queryStr1 = 
			"select ?scId ?scName where { \n"+
			"  { \n"+
			"	select ?scId ?scName where { \n"+
			"	  <http://www.brokeratcloud.eu/v1/common/SERVICE-CATEGORY#"+catId+"> \n"+
			"		 <http://www.w3.org/2004/02/skos/core#broader>*   ?mid . \n"+
			"	  ?mid   <http://www.w3.org/2004/02/skos/core#broader>*   ?class . \n"+
			"	  ?class   <http://www.brokeratcloud.eu/v1/common/hasId>   ?id . \n"+
			"	  ?class   <http://www.brokeratcloud.eu/v1/common/hasName>   ?name . \n"+
			"	  bind( str(?id) as ?scId ) . \n"+
			"	  bind( str(?name) as ?scName ) . \n"+
			"	} \n"+
			"	group by ?class ?scId ?scName \n"+
			"	order by count(?mid) \n"+
			"  } \n"+
			"} ";
		QueryExecution qeSelect = client.query(queryStr1);
		java.util.Stack<String> categories = new java.util.Stack<String>();
		java.util.Stack<String> categoryNames = new java.util.Stack<String>();
		try {
			ResultSet results = qeSelect.execSelect();
			// Iterating over the SPARQL Query results
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				if (soln==null) continue;
				String scId = java.net.URLDecoder.decode( soln.get("scId").toString(), "utf-8");
				String scName = java.net.URLDecoder.decode( soln.get("scName").toString(), "utf-8");
				if (scId==null || scName==null) continue;
				categories.push(scId);
				categoryNames.push(scName);
			}
		} finally {
			qeSelect.close();
		}
		logger.debug("Service Category hierarchy: {}", categories);
		
		// Retrieve service category attributes for every service category in hierarchy
		ServiceCategoryAttributesContainer[] all_categories = new ServiceCategoryAttributesContainer[categories.size()];
		for (int i=0, n=categories.size(); i<n; i++) {
			String scId = categories.get(i);
			String scName = categoryNames.get(i);
			ServiceCategoryAttribute[] tmp = getServiceCategoryAttributes(scId);
			ServiceCategoryAttributesContainer container = new ServiceCategoryAttributesContainer();
			container.setServiceCategory(scId);
			container.setServiceCategoryName(scName);
			container.setServiceCategoryAttributes(tmp);
			all_categories[i] = container;
		}
		
		logger.debug("getServiceCategoryAttributesALL: Results: \n{}", java.util.Arrays.deepToString(all_categories));
		
		return all_categories;
	}
	
	// GET /opt/service-category/attributes/{id}
	// Description: Get service category attribute description
	@GET
	@Path("/attributes/{id}")
	@Produces("application/json")
	public ServiceCategoryAttribute getServiceCategoryAttribute(@PathParam("id") String id) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("getServiceCategoryAttribute: Retrieving service category attribute with id = {}", id);
			ServiceCategoryAttribute attr = (ServiceCategoryAttribute)pm.find(id, ServiceCategoryAttribute.class);
			logger.debug("Service Category Attribute {} :\n{}", id, attr);
			return attr;
		} catch (Exception e) {
			logger.error("getServiceCategoryAttribute: EXCEPTION THROWN: {}", e);
			logger.debug("getServiceCategoryAttribute: Returning an empty instance of {}", ServiceCategoryAttribute.class);
			return new ServiceCategoryAttribute();
		}
	}
	
	// PUT /opt/service-category/attributes
	// Description: Create a new service category attribute
	@PUT
	@Path("/attributes")
	@Consumes("application/json")
	public Response createServiceCategoryAttribute(ServiceCategoryAttribute attr) {
		try {
			String newId = "SCA-"+java.util.UUID.randomUUID().toString();
			attr.setId(newId);
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("createServiceCategoryAttribute: Creating a new Service Category Attribute with id = {}", newId);
			logger.debug("New attribute values:\n{}", attr);
			pm.persist(attr);
			logger.debug("Object added to RDF persistent store");
			return createResponse(HTTP_STATUS_CREATED, "Result=Created");
		} catch (Exception e) {
			logger.error("createServiceCategoryAttribute: EXCEPTION THROWN: {}", e);
			logger.debug("createServiceCategoryAttribute: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
	
	// POST /opt/service-category/attributes
	// Description: Update a service category attribute
	@POST
	@Path("/attributes")
	@Consumes("application/json")
	public Response updateServiceCategoryAttribute(ServiceCategoryAttribute attr) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("updateServiceCategoryAttribute: Updating Service Category Attribute with id = {}", attr.getId());
			logger.debug("New attribute values:\n{}", attr);
			pm.attach(attr);
			logger.debug("Object attached to RDF persistent store manager");
			pm.merge(attr);
			logger.debug("Persistent store state updated");
			return createResponse(HTTP_STATUS_OK, "Result=Updated");
		} catch (Exception e) {
			logger.error("updateServiceCategoryAttribute: EXCEPTION THROWN: {}", e);
			logger.debug("updateServiceCategoryAttribute: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
	
	// DELETE /opt/service-category/attributes/{id}
	// Description: Delete a service category attribute
	@DELETE
	@Path("/attributes/{sca_id}")
	public Response deleteServiceCategoryAttribute(@PathParam("sca_id") String id) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("deleteServiceCategoryAttribute: Deleting Service Category Attribute with id = {}", id);
			Object o = pm.find(id, ServiceCategoryAttribute.class);
			logger.debug("Object retrieved from RDF persistent store");
			pm.remove(o);
			logger.debug("Object deleted from RDF persistent store");
			return createResponse(HTTP_STATUS_OK, "Result=Deleted");
		} catch (Exception e) {
			logger.error("deleteServiceCategoryAttribute: EXCEPTION THROWN: {}", e);
			logger.debug("deleteServiceCategoryAttribute: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
}