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

import eu.brokeratcloud.opt.OptimisationAttribute;
import eu.brokeratcloud.persistence.RdfPersistenceManager;
import eu.brokeratcloud.persistence.RdfPersistenceManagerFactory;

@Path("/opt/attributes")
public class AttributeManagementService extends AbstractManagementService {

	// GET /opt/attributes/
	// Description: Get a list of top-level optimisation attributes
	@GET
	@Path("/")
	@Produces("application/json")
	public OptimisationAttribute[] getTopLevelAttributes() {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			
			String rdfType = pm.getClassRdfType(OptimisationAttribute.class);
			String parentUri = pm.getFieldUri(OptimisationAttribute.class, "parent");
			String queryStr = "SELECT ?s WHERE { ?s  a  <"+rdfType+"> .  ?s <http://purl.org/dc/terms/title> ?title .  FILTER NOT EXISTS { ?s  <"+parentUri+">  ?s1 } .  FILTER ( ?s != <http://www.brokeratcloud.eu/v1/opt/SERVICE-ATTRIBUTE#null> ) } ORDER BY ?title";
			
			logger.debug("getTopLevelAttributes: Retrieving top-level optimisation attribute");
			List<Object> list = pm.findByQuery(queryStr);
			logger.debug("{} top-level Optimisation Attributes found", list.size());
			return list.toArray(new OptimisationAttribute[list.size()]);
		} catch (Exception e) {
			logger.error("getTopLevelAttributes: EXCEPTION THROWN: {}", e);
			logger.debug("getTopLevelAttributes: Returning an empty array of {}", OptimisationAttribute.class);
			return new OptimisationAttribute[0];
		}
	}
	
	// GET /opt/attributes/all
	// Description: Get a list of ALL optimisation attributes
	@GET
	@Path("/all")
	@Produces("application/json")
	public OptimisationAttribute[] getAllAttributes() {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();

			String rdfType = pm.getClassRdfType(OptimisationAttribute.class);
			String parentUri = pm.getFieldUri(OptimisationAttribute.class, "parent");
			String queryStr = "SELECT ?s WHERE { ?s  a  <"+rdfType+"> .  ?s <http://purl.org/dc/terms/title> ?title .  FILTER ( ?s != <http://www.brokeratcloud.eu/v1/opt/SERVICE-ATTRIBUTE#null> ) } ORDER BY ?title";
			
			logger.debug("getAllAttributes: Retrieving ALL optimisation attribute");
			List<Object> list = pm.findByQuery(queryStr);
			logger.debug("{} Optimisation Attributes found", list.size());
			return list.toArray(new OptimisationAttribute[list.size()]);
		} catch (Exception e) {
			logger.error("getAllAttributes: EXCEPTION THROWN: {}", e);
			logger.debug("getAllAttributes: Returning an empty array of {}", OptimisationAttribute.class);
			return new OptimisationAttribute[0];
		}
	}
	
	// GET /opt/attributes/{attr_id}
	// Description: Get attribute description
	@GET
	@Path("/{attr_id}")
	@Produces("application/json")
	public OptimisationAttribute getAttribute(@PathParam("attr_id") String id) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("getAttribute: Retrieving optimisation attribute with id = {}", id);
			OptimisationAttribute attr = (OptimisationAttribute)pm.find(id, OptimisationAttribute.class);
			logger.debug("Optimisation Attribute {} :\n{}", id, attr);
			return attr;
		} catch (Exception e) {
			logger.error("getAttribute: EXCEPTION THROWN: {}", e);
			logger.debug("getAttribute: Returning an empty instance of {}", OptimisationAttribute.class);
			return new OptimisationAttribute();
		}
	}
	
	// GET /opt/attributes/{attr_id}/subattributes
	// Description: Get a list of attribute subattributes (if any)
	@GET
	@Path("/{attr_id}/subattributes")
	@Produces("application/json")
	public OptimisationAttribute[] getAttributeSubattributes(@PathParam("attr_id") String id) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			
			String attrRdfType = pm.getClassRdfType(OptimisationAttribute.class);
			String parentFieldUri = pm.getFieldUri(OptimisationAttribute.class, "parent");
			String parentId = pm.getObjectUri(id, OptimisationAttribute.class);
			String queryStr = "SELECT ?s WHERE { ?s  a  <"+attrRdfType+"> ; <"+parentFieldUri+">  <"+parentId+"> ; <http://purl.org/dc/terms/title> ?title } ORDER BY ?title";
			
			logger.debug("getAttributeSubattributes: Retrieving sub-attributes of Optimisation Attribute with id = {}", id);
			List<Object> list = pm.findByQuery( String.format(queryStr, id) );
			logger.debug("{} sub-attributes found for Optimisation Attribute with id = {}", list.size(), id);
			return list.toArray(new OptimisationAttribute[list.size()]);
		} catch (Exception e) {
			logger.error("getAttributeSubattributes: EXCEPTION THROWN: {}", e);
			logger.debug("getAttributeSubattributes: Returning an empty array of {}", OptimisationAttribute.class);
			return new OptimisationAttribute[0];
		}
	}
	
	// PUT /opt/attributes/
	// Description: Create a new optimisation attribute
	@PUT
	@Path("/")
	@Consumes("application/json")
	public Response createAttribute(OptimisationAttribute attr) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("createAttribute: Creating a new Optimisation Attribute with id = {}", attr.getId());
			logger.debug("New attribute values:\n{}", attr);
			pm.persist(attr);
			logger.debug("Object added to RDF persistent store");
			return createResponse(HTTP_STATUS_CREATED, "Result=Created");
		} catch (Exception e) {
			logger.error("createAttribute: EXCEPTION THROWN: {}", e);
			logger.debug("createAttribute: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
	
	// POST /opt/attributes/{attr_id}
	// Description: Update an optimisation attribute's description
	@POST
	@Path("/{attr_id}")
	@Consumes("application/json")
	public Response updateAttribute(@PathParam("attr_id") String id, OptimisationAttribute attr) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("updateAttribute: Updating Optimisation Attribute with id = {}", id);
			logger.debug("New attribute values:\n{}", attr);
			pm.attach(attr);
			logger.debug("Object attached to RDF persistent store manager");
			pm.merge(attr);
			logger.debug("Persistent store state updated");
			return createResponse(HTTP_STATUS_OK, "Result=Updated");
		} catch (Exception e) {
			logger.error("updateAttribute: EXCEPTION THROWN: {}", e);
			logger.debug("updateAttribute: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
	
	// DELETE /opt/attributes/{attr_id}
	// Description: Delete an optimisation attribute and its sub-attributes
	@DELETE
	@Path("/{attr_id}")
	public Response deleteAttribute(@PathParam("attr_id") String id) {
		try {
			RdfPersistenceManager pm = RdfPersistenceManagerFactory.createRdfPersistenceManager();
			logger.debug("deleteAttribute: Deleting Optimisation Attribute with id = {}", id);
			Object o = pm.find(id, OptimisationAttribute.class);
			logger.debug("Object retrieved from RDF persistent store");
			pm.remove(o);
			logger.debug("Object deleted from RDF persistent store");
			return createResponse(HTTP_STATUS_OK, "Result=Deleted");
		} catch (Exception e) {
			logger.error("deleteAttribute: EXCEPTION THROWN: {}", e);
			logger.debug("deleteAttribute: Returning Status {}", HTTP_STATUS_ERROR);
			return createResponse(HTTP_STATUS_ERROR, "Result=Exception: "+e);
		}
	}
}
