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
package eu.brokeratcloud.persistence;

import eu.brokeratcloud.persistence.annotations.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.*;
import java.math.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.bind.DatatypeConverter;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdfPersistenceManagerImpl implements RdfPersistenceManager {
	private static final Logger logger = LoggerFactory.getLogger( (new Object() { }.getClass().getEnclosingClass()).getName() );
	private static final String RDF_CONFIG_BASE = "/rdf-persistence";
	
	// ========================================================================================
	// Constructors and (static) factory methods
	
	public static RdfPersistenceManagerImpl getInstance() throws IOException {
		return new RdfPersistenceManagerImpl();
	}
	
	protected RdfPersistenceManagerImpl() throws IOException {
		logger.debug("{}.<init>: Creating new manager instance", getClass().getSimpleName());
		initManagerState();
		logger.debug("{}.<init>: Manager instance initialized", getClass().getSimpleName());
	}
	
	// ========================================================================================
	// RdfPersistenceManager Public API
	
	public synchronized void attach(Object o) {
		try {
			logger.debug("attach: BEGIN: object={}", o);
			if (managedObjectUris.containsKey(o)) {
				String uri = getObjectUri(o);
				attach(o, uri);
				logger.debug("attach: END: object attached");
			} else {
				logger.debug("attach: END: object is already attached");
			}
		} catch (RdfPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new RdfPersistenceException(e);
		}
	}
	
	public synchronized void detach(Object o) {
		logger.debug("detach: BEGIN: object={}", o);
		String uri = managedObjectUris.get(o);
		if (uri!=null) detach(uri);
		else logger.debug("detach: END: object has already been detached");
		logger.debug("detach: END: object detached");
	}
	
	public synchronized void clear() {
		resetManagerState();
		managedObjects.clear();
		managedObjectUris.clear();
	}
	
	public synchronized void reset() {
		resetManagerState();
	}
	
	public synchronized void persist(Object o) {
		try {
			logger.debug("persist: BEGIN: object={}", o);
			processObjectGraph(o, PERSIST);
			logger.debug("persist: END");
		} catch (RdfPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new RdfPersistenceException(e);
		}
	}
	
	public synchronized void merge(Object o) {
		try {
			logger.debug("merge: BEGIN: object={}", o);
			processObjectGraph(o, MERGE);
			logger.debug("merge: END");
		} catch (RdfPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new RdfPersistenceException(e);
		}
	}
	
	public synchronized void remove(Object o) {
		try {
			logger.debug("remove: BEGIN: object={}", o);
			processObjectGraph(o, DELETE);
			logger.debug("remove: END");
		} catch (RdfPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new RdfPersistenceException(e);
		}
	}
	
	public synchronized void refresh(Object o) {
		try {
			logger.debug("refresh: BEGIN: object={}", o);
			refreshObjectState(o);
			logger.debug("refresh: END: object={}", o);
		} catch (RdfPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new RdfPersistenceException(e);
		}
	}
	
	public List<Object> findAll(Class type) {
		try {
			logger.debug("findAll: BEGIN: type={}", type.getName());
			PersistentClass pc = getTypeDescriptor(type);
			logger.trace("findAll: pc={}", pc);
			
			String typeUri = pc.typeUri;
			String rdfType = pc.rdfType;
			String isAValue = (rdfType!=null && !rdfType.isEmpty()) ? rdfType : typeUri;
			logger.trace("findAll: isAValue: rdf-type={}, type-uri={}", rdfType, typeUri);
			
			// prepare isAValue (if it is a list)
			String[] apart = isAValue.split("[,]");
			if (apart.length>1) {
				StringBuilder sb = new StringBuilder();
				boolean first=true;
				for (int i=0; i<apart.length; i++) {
					if (!(apart[i]=apart[i].trim()).isEmpty()) {
						if (first) first=false; else sb.append(", ");
						sb.append("<").append(apart[i]).append(">");
					}
				}
				isAValue = sb.toString();
			} else {
				isAValue = "<"+isAValue+">";
			}
			
			// retrieve all object uri's of this class
			StringBuilder sb = new StringBuilder();
			sb.append("# Class URI : "); sb.append(isAValue); sb.append("\n");
			sb.append("# Command   : SELECT\n");
			sb.append("SELECT ?s WHERE { ?s  a  "); sb.append(isAValue); sb.append(" . }\n\n");
			String query = sb.toString();
			logger.trace("findAll: query={}", query);
			
			logger.debug("findAll: CALLING findByQuery");
			return findByQuery(type, query);
		} catch (RdfPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new RdfPersistenceException(e);
		}
	}
	
	public List<Object> findByQuery(String queryStr) {
		return findByQuery(null, queryStr);
	}
	//
	public List<Object> findByQuery(Class useType, String queryStr) {
		try {
			logger.debug("findByQuery: BEGIN: query={}", queryStr);
			
			// Use SPARQL service client to send statement to Fuseki server
			logger.trace("findByQuery: Sending SPARQL query...");
			QueryExecution qeSelect = client.query(queryStr);
			logger.trace("findByQuery: SPARQL query sent");
			
			// Perform the simple SPARQL SELECT query
			List<Object> list = null;
			try {
				HashMap<String,Object> visited = new HashMap<String,Object>();
				HashSet<String> uris = new HashSet<String>();
				
				// Retrieving the SPARQL Query results
				logger.trace("findByQuery: Retrieving matching object URIs");
				ResultSet results = qeSelect.execSelect();
				String var = results.getResultVars().get(0);
				logger.trace("findByQuery: LOOP-1: BEGIN: looking for URIs in variable: {}", var);
				while (results.hasNext()) {
					logger.trace("findByQuery: LOOP-1: Iteration Start");
					QuerySolution soln = results.nextSolution();
					String uri = soln.get(var).toString();
					uris.add(uri);
					logger.trace("findByQuery: LOOP-1: Iteration End: uri={}", uri);
				}
				logger.trace("findByQuery: LOOP-1: END");
				
				logger.trace("findByQuery: LOOP-2: BEGIN: retrieving object data using matching URIs");
				list = new LinkedList<Object>();
				for (String uri : uris) {
					logger.trace("findByQuery: LOOP-2: Iteration Start: Retrieving object with uri: {}", uri);
					Object o = retrieveObject(uri, visited, useType);
					list.add(o);
					logger.trace("findByQuery: LOOP-2: Iteration End: Object retrieved: {}", objectHash(o));
				}
				logger.trace("findByQuery: LOOP-2: END");
				return list;
			} finally {
				qeSelect.close();
				logger.debug("findByQuery: END: result={}", list);
			}
		} catch (RdfPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new RdfPersistenceException(e);
		}
	}
	
	public Object find(Object id, Class type) {
		try {
			String idStr = id.toString();
			logger.debug("find (id,type): BEGIN: id={}, type={}", idStr, type.getName());
			
			String oUri = getObjectUri(idStr, type);
			logger.trace("find (id,type): object uri={}", oUri);
			
			logger.debug("find (id,type): CALLING 'find (uri)'");
			return find(type, oUri);
		} catch (RdfPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new RdfPersistenceException(e);
		}
	}
	
	public Object find(String oUri) {
		return find(null, oUri);
	}
	//
	public Object find(Class useType, String oUri) {
		try {
			logger.debug("find (uri): BEGIN: uri={}, use-type={}", oUri, useType);
			Object o = retrieveObject(oUri, new HashMap<String,Object>(), useType);
			logger.debug("find (uri): END: result={}", o);
			return o;
		} catch (RdfPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new RdfPersistenceException(e);
		}
	}
	
	public boolean exist(Object o) {
		try {
			logger.debug("exist (object): BEGIN: object={}", o);
			String oUri = getObjectUri(o);
			boolean result = find(oUri)!=null;
			logger.debug("exist (object): END: result={}", result);
			return result;
		} catch (RdfPersistenceException e) {
			throw e;
		} catch (Exception e) {
			throw new RdfPersistenceException(e);
		}
	}
	
	public boolean exist(Object id, Class type) {
		logger.debug("exist (id,type): BEGIN: id={}, type={}", id.toString(), type.getName());
		boolean result = find(id,type)!=null;
		logger.debug("exist (id,type): END: result={}", result);
		return result;
	}
	
	public boolean exist(String oUri) {
		logger.debug("exist (uri): BEGIN: uri={}", oUri);
		boolean result = find(oUri)!=null;
		logger.debug("exist (uri): END: result={}", result);
		return result;
	}
	
	public boolean exist(Class useType, String oUri) {
		logger.debug("exist (uri): BEGIN: uri={}, use-type={}", oUri, useType);
		boolean result = find(useType, oUri)!=null;
		logger.debug("exist (uri): END: result={}", result);
		return result;
	}
	
	public String getObjectUri(Object o) throws IllegalAccessException, InvocationTargetException {
		return _getObjectUri(o);
	}
	
	// Useful when writing SPARQL queries involving object types
	public String getObjectUri(Object id, Class type) {
		return _getObjectUri(id.toString(), type);
	}
	
	// Useful when writing SPARQL queries involving object types
	public synchronized String getClassRdfType(Class type) {
		PersistentClass pc = getTypeDescriptor(type);
		return pc!=null ? (pc.rdfType!=null ? pc.rdfType : pc.typeUri) : null;
	}
	
	// Useful when writing SPARQL queries involving object types
	public synchronized String getFieldUri(Class type, String fieldName) {
		PersistentClass pc = getTypeDescriptor(type);
		if (pc==null) return null;
		PersistentField pf = pc.getFieldByName(fieldName);
		if (pf==null) return null;
		return pf.fieldUri;
	}
	
	public void insertRdfTriple(String s, String p, String o) {
		String stmt = String.format("INSERT DATA { <%s> <%s> <%s> }", s, p, o);
		logger.trace("insertRdfTriple: BEGIN: Inserting RDF triple: Statement: {}", stmt);
		client.execute(stmt);
		logger.trace("insertRdfTriple: END");
	}
	
	public void removeRdfTriple(String s, String p, String o) {
		String stmt = String.format("DELETE DATA { <%s> <%s> <%s> }", s, p, o);
		logger.trace("removeRdfTriple: BEGIN: Removing RDF triple: Statement: {}", stmt);
		client.execute(stmt);
		logger.trace("removeRdfTriple: END");
	}
	
	
	// ========================================================================================
	// RdfPersistenceManager API Implementation - Instance fields and methods
	
	// Constants
	protected static final int PERSIST = 1;
	protected static final int MERGE   = 2;
	protected static final int DELETE = 3;
	
	// Member fields
	protected SparqlServiceClient client;
	
	protected LinkedList<Object> processingQueue;
	protected HashSet<Object> visited;
	protected HashMap<String,Object> visitedUris;	// contains <URI,Object> pairs, for faster searches using object URIs
	protected HashSet<String> visitedUrisForDelete;	// contains <URIs>, for faster searches using object URIs during Delete
	protected Queue<String> pendingDeletes;
	protected StringBuilder insertStatements;
	protected StringBuilder deleteStatements;
	
	// Methods
	protected void initManagerState() throws IOException {
		client = SparqlServiceClientFactory.getClientInstance();
		logger.debug("SPARQL service endpoints: type={}, query={}, update={}", client.getClass().getName(), client.getSelectEndpoint(), client.getUpdateEndpoint());
		
		processingQueue = new LinkedList<Object>();
		visited = new HashSet<Object>();
		visitedUris = new HashMap<String,Object>();
		visitedUrisForDelete = new HashSet<String>();
		pendingDeletes = new LinkedList<String>();
	}
	
	protected void resetManagerState() {
		processingQueue.clear();
		visited.clear();
		visitedUris.clear();
		visitedUrisForDelete.clear();
		pendingDeletes.clear();
		insertStatements = new StringBuilder();
		deleteStatements = new StringBuilder();
	}
	
	protected static String objectHash(Object o) {
		if (o==null) return "null";
		return o.getClass().getName()+"@"+o.hashCode();
	}
	
	protected synchronized void attach(Object o, String uri) {
		if (uri!=null && !(uri=uri.trim()).isEmpty()) {
			managedObjects.put(uri, o);
			managedObjectUris.put(o, uri);
		}
	}
	
	protected synchronized void detach(String uri) {
		Object o = null;
		if (uri!=null) o = managedObjects.remove(uri);
		if (o!=null) managedObjectUris.remove(o);
	}
	
	// Object and object graph processing methods
	//
	protected void processObjectGraph(Object o, int mode) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, UnsupportedEncodingException {
		logger.debug("processObjectGraph: BEGIN: object={}", objectHash(o));
		String oUri = getObjectUri(o);
		logger.trace("processObjectGraph: object uri={}", oUri);
		
		// check if object already persisted
		if (mode==PERSIST || mode==MERGE || mode==DELETE) {
			logger.debug("processObjectGraph: Checking if object has been previously persisted: ASKing for URI: {}", oUri);
			boolean _is_persisted = client.ask( String.format("ASK WHERE { <%s> ?p ?o }", oUri) );
			logger.debug("processObjectGraph: _is_persisted={}", _is_persisted );
			if (mode==PERSIST && _is_persisted) throw new RdfPersistenceException("Object is already persisted: "+o);
			if (mode==MERGE && !_is_persisted) throw new RdfPersistenceException("Object has not been persisted: "+o);
			if (mode==DELETE && !_is_persisted) throw new RdfPersistenceException("Object has not been persisted: "+o);
			// else it's ok to continue
		}
		
		// reset manager state
		logger.trace("processObjectGraph: resetting manager state");
		resetManagerState();
		
		//traverseObjectGraph		// traverse live object graph to get object uri's
		//acquire object locks		// using object uri's discovered by 'traverseObjectGraph'. A transcation must start and a transaction id must be returned
		
		if (mode!=DELETE) {
			// Process objects in processing queue
			logger.trace("processObjectGraph: LOOP-1: BEGIN: Processing object graph: Start object: uri={}", oUri);
			processingQueue.offer(o);
			for (Object vo; (vo=processingQueue.poll())!=null; ) {
				logger.trace("processObjectGraph: LOOP-1: ITERATION: Processing object: uri={}", oUri);
				if (visited.contains(vo)) {
					logger.trace("processObjectGraph: LOOP-1: ITERATION: Object has already been processed: uri={}", oUri);
					continue;
				}
				processObject(vo);	//, null, mode==DELETE);
			}
			logger.trace("processObjectGraph: LOOP-1: END");
		} else {
			// Mark object for delete
			logger.trace("processObjectGraph: Marking object for delete: uri={}", oUri);
			markForDelete( oUri );
		}
		
		// Process URIs in pendingDeletes set
		logger.trace("processObjectGraph: LOOP-2: BEGIN: Processing object pending for delete");
		String dUri;
		while ((dUri=pendingDeletes.poll())!=null) {
			logger.trace("processObjectGraph: LOOP-2: ITERATION: Processing object marked for delete: uri={}", dUri);
			processObjectDelete(dUri);
		}
		logger.trace("processObjectGraph: LOOP-2: END");
		
		// Prepare statements for execution
		String insStmtStr = insertStatements.length()>0  ?  "INSERT DATA {\n"+insertStatements.toString()+"} ;\n"  :  "";
		String delStmtStr = deleteStatements.toString();
		String allStmtStr = delStmtStr + insStmtStr;
		logger.debug("processObjectGraph: DELETE STATEMENTS:\n{}", delStmtStr);
		logger.debug("processObjectGraph: INSERT STATEMENTS:\n{}", insStmtStr);
		logger.debug("processObjectGraph: ALL STATEMENTS for submit:\n{}", allStmtStr);
		
		// Execute statements...
		client.execute(allStmtStr);
		
		//release object locks IN a "finally" CLAUSE		// Commit or rollback transcation using transaction id
		
		// reset manager state (ie clear lists and maps) to release memory
		resetManagerState();
		
		logger.debug("processObjectGraph: END");
	}
	
	protected void processObjectDelete(String dUri) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, UnsupportedEncodingException {
		logger.debug("processObjectDelete: BEGIN: object to delete: uri={}", dUri);
		
		// check if object has already been processed for delete
		if (visitedUrisForDelete.contains(dUri)) {
			logger.debug("processObjectDelete: END: object has already been processed for delete: Nothing to do");
			return;
		}
		// cache object reference to prevent processing twice during persisted data traversal
		visitedUrisForDelete.add(dUri);
		logger.trace("processObjectDelete: object uri cached to prevent processing it twice during persisted data traversal");
		
		// Get object's persisted state
		PersistentObject po = getPersistentState(dUri);
		
		// Get object's type descriptor
		PersistentClass pc = getTypeDescriptor( getTypeFromUri(dUri, po) );
		
		// Call processObjectDelete(PersistentObject, PersistentClass) to do the actual processing
		processObjectDelete(po, pc);
	}
	
	protected void processObjectDelete(PersistentObject po, PersistentClass pc) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, UnsupportedEncodingException {
		logger.debug("processObjectDelete: BEGIN");
		
		// Generate DELETE statements
		if (po.isPersisted) {
			// generate DELETE statement for main (persisted) object
			logger.trace("processObjectDelete: Generating DELETE statements for object: uri={}", po.objectUri);
			generateDeleteWhereStatement(po.objectUri);
			detach(po.objectUri);
			
			// scan all persistent object data and mark any referenced objects with cascade-delete for delete
			logger.trace("processObjectDelete: LOOP-1: BEGIN: marking for delete any referenced objects");
			for (String fieldUri : po.data.keySet()) {
				logger.trace("processObjectDelete: LOOP-1: ITERATION: field-uri={}", fieldUri);
				
				// ignore reserved field-uri's
				if (isReservedFieldUri(fieldUri)) {
					logger.trace("processObjectDelete: LOOP-1: ignoring this field-uri={}", fieldUri);
					continue;
				}
				
				String pValStr = po.data.get(fieldUri);
				pValStr = pValStr.trim();
				logger.trace("processObjectDelete: LOOP-1: Persistent value={}", pValStr);
				if (pValStr==null || pValStr.equals(nullUriInBrackets) || pValStr.equals(nullUri)) {
					logger.trace("processObjectDelete: LOOP-1: Field is NULL or contains NULL URI: Nothing to do");
					continue;
				}
				// checking if it is a reference
				if (pValStr.startsWith("<") && pValStr.endsWith(">")) {		// It's a reference
					logger.trace("processObjectDelete: LOOP-1: retrieving field descriptor from uri: {}", fieldUri);
					PersistentField pf = pc.getFieldFromUri(fieldUri, null);
					if (pf.cascadeDelete) {
						logger.trace("processObjectDelete: LOOP-1: Field value IS A reference: {}", pValStr);
						pValStr = pValStr.substring(1, pValStr.length()-1);
						markForDelete( pValStr );
					} else {
						logger.trace("processObjectDelete: LOOP-1: No cascade delete for field: Nothing to do");
					}
				} else {
					logger.trace("processObjectDelete: LOOP-1: Field value is NOT a reference: Nothing to do");
				}
			}
			logger.trace("processObjectDelete: LOOP-1: END");
		}
		else logger.trace("processObjectDelete: object is not persisted: uri={}", po.objectUri);
		
		logger.debug("processObjectDelete: END");
	}
	
	protected void processObject(Object o) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, UnsupportedEncodingException {
		processObject(o, false);
	}
	
	protected void processObject(Object o, boolean delete) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, UnsupportedEncodingException {
		logger.debug("processObject: BEGIN: object={}, delete={}", objectHash(o), delete);
		if (o==null) {
			logger.debug("processObject: END: object ref is null: Nothing to do");
			return;
		}
		
		// check if object has already been processed
		if (visited.contains(o)) {
			logger.debug("processObject: END: object has already been processed: Nothing to do");
			return;
		}
		// cache object reference to prevent processing twice during graph traversal
		visited.add(o);
		logger.trace("processObject: object reference cached to prevent processing it twice during graph traversal");
		
		// Get object's type descriptor
		PersistentClass pc = getTypeDescriptor(o.getClass());
		
		// Get object's persisted state
		PersistentObject po = getPersistentState(o);
		// ...also cache object uri and descriptor
		visitedUris.put(po.objectUri, pc);
		
		// Generate DELETE statements
		// Call processObjectDelete(PersistentObject, PersistentClass) to do the actual processing
		processObjectDelete(po, pc);
		
		// Generate INSERT statements from live object
		logger.trace("processObject: Generating insert statements for object header information");		// e.g. java-type, rdfs:subClass, rdf:type etc
		generateObjectHeaders(po, pc);
		attach(o, po.objectUri);
		
		logger.trace("processObject: LOOP-2: BEGIN: Generating insert statements for object fields");
		for (PersistentField pf : pc.fields) {
			logger.trace("processObject: LOOP-2: Iteration Start: processing field: name={}, is-literal={}, is-reference={}", pf.name, pf.isLiteral, pf.isReference);
			Object value = getFieldValue(o, pf);
			
			// process field value according to field type
			processFieldValue(value, null, null, -1, po, pc, pf);
			
			logger.trace("processObject: LOOP-2: Iteration End: field={}", pf.name);
		}
		logger.trace("processObject: LOOP-2: END");
		
		// Populate id field if it hasn't been already
		_setIdField(o, pc, po);
		
		logger.debug("processObject: END");
	}
	
	protected void _setIdField(Object o, PersistentClass pc, PersistentObject po) throws IllegalAccessException, InvocationTargetException {
		logger.debug("_setIdField: BEGIN: object={}, pc={}", o, pc);
		PersistentField idf = pc.idField;
		Object ido = getFieldValue(o, idf);
		logger.debug("_setIdField: Checking id field has been populated: {}", ido);
		if (ido==null || ido.toString().trim().isEmpty()) {
			String ids = po.objectUri;
			logger.debug("_setIdField: Extracting id field from object URI: {}", ids);
			// extract id from id-pattern
			if (ids!=null) {
				Matcher mat = pc.idPattern.matcher(ids);
				boolean mr = mat.matches();
				logger.debug("_setIdField: Checking object URI against id-pattern: matched={}, group-count={}", mr, mat.groupCount());
				if (mr && mat.groupCount()>0) {
					ids = mat.group(1);
					logger.debug("_setIdField: Extracted id value: {}", ids);
					if (ids!=null && !ids.trim().isEmpty()) {
						logger.debug("_setIdField: Setting id field value: value={}", ids);
						setFieldValue(o, ids, idf);
						logger.debug("_setIdField: New object state: object={}", o);
					} else {
						logger.error("_setIdField: Extracted id value is null or empty: object={}", o);
					}
				} else {
					logger.error("_setIdField: Object URI does not match to id pattern: uri={}, pattern={}", ids, pc.idPattern);
				}
			} else {
				logger.error("_setIdField: Object URI not captured in 'po': object={}", o);
			}
		}
		logger.debug("_setIdField: END");
	}
	
	protected void processFieldValue(Object value, Class type, String fieldUri, int dim, PersistentObject po, PersistentClass pc, PersistentField pf) throws IllegalAccessException, InvocationTargetException {
		logger.debug("processFieldValue: BEGIN: value={}", objectHash(value));
		
		if (pf.dontSerialize) {
			logger.debug("processFieldValue: END: Not serializing field. 'dontSerialize' flag is set for field: {}", fieldUri);
			return;
		}
		
		if (type==null) type = pf.type;
		if (fieldUri==null) fieldUri = pf.fieldUri;
		if (dim<0) dim = pf.arrayDimensions;
		logger.trace("processFieldValue: Arguments: field-uri={}, type={}, dim={}", fieldUri, type.getName(), dim);
		
		if (value==null) {
			logger.trace("processFieldValue: \tvalue is null");
			if (!pf.omitIfNull) generateInsertStatement(po.objectUri, fieldUri, nullUriInBrackets);
			else logger.trace("processFieldValue: \tOmitting null value from RDF generation");
		} else if (pf.isUri) {
			String uri = value.toString().trim();
			if (!uri.isEmpty()) {
				//uri = URLEncoder.encode( uri, "UTF-8" );		// Is URI encoding needed???
				if (!uri.startsWith("<") || !uri.endsWith(">")) uri = "<"+uri+">";
				logger.trace("processFieldValue: \tvalue is URI: {}", uri);
				generateInsertStatement(po.objectUri, fieldUri, uri);
			} else {
				if (!pf.omitIfNull) {
					logger.trace("processFieldValue: \tvalue is URI: {}", nullUriInBrackets);
					generateInsertStatement(po.objectUri, fieldUri, nullUriInBrackets);
				} else logger.trace("processFieldValue: \tOmitting empty URI value from RDF generation");
			}
		} else if (type.isArray() && dim>0) {
			logger.trace("processFieldValue: \tvalue is Array");
			processArray((Object[])value, fieldUri, dim, po, pc, pf);
		} else if (type.isArray() && dim<=0) {
			throw new RdfPersistenceException("*** INTERNAL ERROR: zero or negative array dim="+dim+" : Possibly a bug ***");
		} else if (Collection.class.isAssignableFrom(type)) {
			logger.trace("processFieldValue: \tvalue is Collection");
			processCollection((Collection)value, fieldUri, po, pc, pf);
		} else if (Map.class.isAssignableFrom(type)) {
			logger.trace("processFieldValue: \tfield value is Map");
			processMap((Map)value, fieldUri, po, pc, pf);
		} else if (isLiteralType(type)) {
			logger.trace("processFieldValue: \tfield value is single literal value");
			if (value.toString().isEmpty() && pf.omitIfNull) {
				logger.trace("processFieldValue: \tOmitting empty field value");
			} else {
				String valStr = formatValue(value, type, pf.lang);
				logger.trace("processFieldValue: \tfield value is literal: {}", valStr);
				generateInsertStatement(po.objectUri, fieldUri, valStr);
			}
		} else {	// if (pf.isReference)
			Object refObj = value;
			String roUri = getObjectUri(refObj);
			logger.trace("processFieldValue: \tfield value is reference: referenced object's uri: {}", roUri);
			generateInsertStatement(po.objectUri, fieldUri, "<"+roUri+">");
			if (pf.cascadeUpdate) {
				logger.trace("processFieldValue: \tQueuing referenced object for cascade update: uri={}", roUri);
				addObjectInProcessingQueue(refObj);
			} else {
				logger.trace("processFieldValue: \tNo cascade update for field: Nothing to do: field={}", pf.name);
			}
		}
		logger.debug("processFieldValue: END");
	}
	
	protected void processArray(Object[] arr, String fieldUri, int dim, PersistentObject po, PersistentClass pc, PersistentField pf) throws IllegalAccessException, InvocationTargetException {
		logger.debug("processArray: BEGIN: dim={}, array-length={}", dim, arr!=null ? arr.length : "null");
		if (dim<=0) throw new IllegalArgumentException("processArray: Invalid array dimension: "+dim);
		
		// if array is null
		if (arr==null) {
			logger.debug("processArray: Array passed is NULL");
			generateInsertStatement(po.objectUri, fieldUri, nullUriInBrackets);
			logger.debug("processArray: END");
			return;
		}
		// else if array is not null...
		
		// store array length
		generateInsertStatement(po.objectUri, fieldUri, formatValue(arr.length, Integer.TYPE));
		fieldUri = fieldUri+asep;
		
		// process array elements
		if (dim==1) {	// 1-d array or lowest-level dimension of a multidimensional array
			logger.trace("processArray: LOOP-1: BEGIN: Processing array elements: 1-D array or last-level of a multidimensional array");
			for (int i=0; i<arr.length; i++) {
				Object ao = arr[i];
				logger.trace("processArray: LOOP-1: ITERATION #{}: Processing element={}", i, ao);
				processFieldValue(ao, pf.arrayComponentType, fieldUri+i, -1, po, pc, pf);
			}
			logger.trace("processArray: LOOP-1: END");
		} else {
			logger.trace("processArray: LOOP-1: BEGIN: Processing array elements: Multidimensional array: dim={}", dim);
			int dim1 = dim-1;
			for (int i=0; i<arr.length; i++) {
				logger.trace("processArray: LOOP-1: ITERATION #{}: processing sub-array with field-base-uri={}", i, fieldUri+i);
				processArray((Object[])arr[i], fieldUri+i, dim1, po, pc, pf);
			}
			logger.trace("processArray: LOOP-1: END");
		}
		
		logger.debug("processArray: END");
	}
	
	protected void processCollection(Collection col, String fieldUri, PersistentObject po, PersistentClass pc, PersistentField pf) throws IllegalAccessException, InvocationTargetException {
		logger.debug("processCollection: BEGIN: collection-size={}, collection={}", col!=null ? col.size() : "null", col);
		
		// if collection is null
		if (col==null) {
			logger.debug("processCollection: Collection passed is NULL");
			generateInsertStatement(po.objectUri, fieldUri, nullUriInBrackets);
			logger.debug("processCollection: END");
			return;
		}
		// else if collection is not null...
		
		// store collection size
		generateInsertStatement(po.objectUri, fieldUri, formatValue(col.size(), Integer.TYPE));
		fieldUri = fieldUri+csep;
		
		// process collection elements
		logger.trace("processCollection: LOOP-1: BEGIN: Processing collection elements");
		int i=0;
		for (Object elem : col) {
			logger.trace("processCollection: LOOP-1: ITERATION #{}: Processing element={}", i, objectHash(elem));
			processFieldValue(elem, elem.getClass(), fieldUri+i, -1, po, pc, pf);
			i++;
		}
		logger.trace("processCollection: LOOP-1: END");
		
		logger.debug("processCollection: END");
	}
	
	protected void processMap(Map map, String fieldUri, PersistentObject po, PersistentClass pc, PersistentField pf) throws IllegalAccessException, InvocationTargetException {
		logger.debug("processMap: BEGIN: map-size={}, map={}", map!=null ? map.size() : "null", map);
		
		// if map is null
		if (map==null) {
			logger.debug("processMap: Map passed is NULL");
			generateInsertStatement(po.objectUri, fieldUri, nullUriInBrackets);
			logger.debug("processMap: END");
			return;
		}
		// else if map is not null...
		
		// store map size
		generateInsertStatement(po.objectUri, fieldUri, formatValue(map.size(), Integer.TYPE));
		fieldUri = fieldUri+msep;
		
		// process collection elements
		logger.trace("processMap: LOOP-1: BEGIN: Processing map pairs");
		int i=0;
		for (Object key : map.keySet()) {
			Object value = map.get(key);
			logger.trace("processMap: LOOP-1: ITERATION #{}: Processing pair={{}, {}}", i, objectHash(key), objectHash(value));
			processFieldValue(key, key!=null ? key.getClass() : null, fieldUri+i+ksep, -1, po, pc, pf);
			processFieldValue(value, value!=null ? value.getClass() : null, fieldUri+i+vsep, -1, po, pc, pf);
			i++;
		}
		logger.trace("processMap: LOOP-1: END");
		
		logger.debug("processMap: END");
	}
	
	// Object and object graph retrieval methods
	//
	protected synchronized Object retrieveObject(String oUri, HashMap<String,Object> visited) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, UnsupportedEncodingException {
		return retrieveObject(oUri, visited, null);
	}
	
	protected synchronized Object retrieveObject(String oUri, HashMap<String,Object> visited, Class useType) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, UnsupportedEncodingException {
		logger.debug("retrieveObject: BEGIN: uri={}, use-type={}, visited={}", oUri, useType, visited);

		// check if object has already been visisted and processed
		if (visited.containsKey(oUri)) {
			logger.debug("retrieveObject: END: object has already been processed: returning cached reference");
			return visited.get(oUri);
		}
		if (managedObjects.containsKey(oUri)) {
			//logger.debug("retrieveObject: object is already being managed: refreshing object data");
			Object mo = managedObjects.get(oUri);
			//refreshObjectState(mo);
			logger.debug("retrieveObject: END: object is already being managed: returning cached reference");
			return mo;
		}
		
		// Get object's persisted state
		PersistentObject po = getPersistentState(oUri);
		
		// Check if object has been persisted
		if (!po.isPersisted) {
			logger.debug("retrieveObject: END: object not persisted: result=null");
			return null;
		}
		
		// Get object's type descriptor
		Class typeFromUri = getTypeFromUri(oUri, po);
		if (useType!=null && useType.isArray()) useType = useType.getComponentType();
		if (useType!=null && (List.class.isAssignableFrom(useType) || Map.class.isAssignableFrom(useType))) useType = null;
		Class type = (useType==null) ? typeFromUri : ((useType.isAssignableFrom(typeFromUri)) ? typeFromUri : useType);
		logger.trace("retrieveObject: Object type from uri: oUri={}, type={}", oUri, type);
		PersistentClass pc = getTypeDescriptor( type );
		
		// create object instance
		logger.trace("retrieveObject: Instantiating object: persisted object type: {}", pc.type.getName());
		Object o = pc.type.newInstance();
		logger.trace("retrieveObject: object instantiated: {}", o);
		
		// cache object uri and reference to prevent processing twice during persisted data traversal
		visited.put(oUri, o);
		logger.trace("retrieveObject: object uri and reference were cached to prevent processing it twice during persisted data traversal");
		attach(o, oUri);
		
		// scan all persistent object data and populate object's fields
		logger.trace("retrieveObject: Calling 'retrieveObjectData' to populate object fields");
		retrieveObjectData(o, po, pc, visited, true, true);
		
		// Populate id field if it hasn't been already
		_setIdField(o, pc, po);
		
		logger.debug("retrieveObject: END: result={}", o);
		return o;
	}
	
	protected void refreshObjectState(Object o) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, UnsupportedEncodingException {
		logger.debug("refreshObjectState: BEGIN: object={}", objectHash(o));

		// get object's uri
		String oUri = getObjectUri(o);
		logger.trace("refreshObjectState: object uri={}", oUri);
		
		// Get object's persisted state
		PersistentObject po = getPersistentState(oUri);
		
		// Check if object has been persisted
		if (!po.isPersisted) {
			logger.debug("refreshObjectState: object not persisted: THROWING EXCEPTION");
			throw new RdfPersistenceException("refreshObjectState: Object not persisted: "+oUri);
		}
		
		// Get object's type descriptor
		PersistentClass pc = getTypeDescriptor( getTypeFromUri(oUri, po) );
		
		// scan all persistent object data and populate object's fields
		logger.trace("refreshObjectState: Calling 'retrieveObjectData' to populate object fields");
		retrieveObjectData(o, po, pc, new HashMap<String,Object>(), true, true);
		
		logger.debug("refreshObjectState: END: object={}", o);
	}
	
	// Could also be named: retrieveObjectGraph :)
	protected void retrieveObjectData(Object o, PersistentObject po, PersistentClass pc, HashMap<String,Object> visited, boolean deepRefresh, boolean clearFields) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, UnsupportedEncodingException {
		logger.debug("retrieveObjectData: BEGIN: object={}", objectHash(o));

		// scan all persistent object data and populate object's fields
		for (String fieldUri : po.data.keySet()) {
			logger.trace("retrieveObjectData: LOOP-1: ITERATION START: field-uri={}", fieldUri);
			
			// ignore reserved field-uri's
			if (isReservedFieldUri(fieldUri)) {
				logger.trace("retrieveObjectData: LOOP-1: ignoring this field-uri={}", fieldUri);
				continue;
			}
			
			// get fieldUri with no language tag
			String lang = null;
			int lt;
			if ((lt=fieldUri.lastIndexOf("@"))>-1) {
				if (lt+1<fieldUri.length()) lang = fieldUri.substring(lt+1).trim();
				fieldUri = fieldUri.substring(0,lt).trim();
			}
			
			// get field descriptor
			logger.trace("retrieveObjectData: LOOP-1: Retrieving Persistent field descriptor for: field-uri={}, lang={}", fieldUri, lang);
			PersistentField pf = pc.getFieldFromUri(fieldUri, lang);
			logger.trace("retrieveObjectData: LOOP-1: Persistent field descriptor: pf={}", pf);
			if (pf==null) logger.debug("retrieveObjectData: LOOP-1: Unknown property: {}", fieldUri);
			
			if (ignoreUnknownProperties && pf==null) {
				logger.debug("retrieveObjectData: LOOP-1: END: ignoring property: {}", fieldUri);
				continue;
			}
			
			// process field value according to field type
			Object ignoreToken = this;
			Object pValue = retrieveFieldValue(null, null, -1, po, pc, pf, visited, deepRefresh, clearFields, ignoreToken);
			logger.trace("retrieveObjectData: LOOP-1: \tsetting field value: field={}, value={}", pf.name, pValue);
			
			// setting field value
			if (pValue!=ignoreToken) {
				setFieldValue(o, pValue, pf);
			} else {
				logger.trace("retrieveObjectData: LOOP-1: \tignoring this field (ignore token returned): field={}", pf.name);
			}
			logger.trace("retrieveObjectData: LOOP-1: ITERATION END");
		}
		logger.trace("retrieveObjectData: LOOP-1: END");
		
		logger.debug("retrieveObjectData: END: object={}", o);
	}
	
	protected Object retrieveFieldValue(Class type, String fieldUri, int dim, PersistentObject po, PersistentClass pc, PersistentField pf, HashMap<String,Object> visited, boolean deepRefresh, boolean clearFields, Object ignoreToken) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, UnsupportedEncodingException {
		logger.debug("retrieveFieldValue: BEGIN: type={}, field-uri={}, dim={}, persistent data={}", type, fieldUri, dim, po.data);
		
		if (pf.dontSerialize) {
			logger.debug("retrieveFieldValue: END: Not unserializing field. 'dontSerialize' flag is set for field: {}", fieldUri);
			return ignoreToken;
		}
		
		if (type==null) type = pf.type;
		if (fieldUri==null) fieldUri = pf.fieldUri;
		if (dim<0) dim = pf.arrayDimensions;
		logger.trace("retrieveFieldValue: Arguments: field-uri={}, type={}, pf.type={}, dim={}", fieldUri, type, pf.type, dim);
		
		// retrieve persisted field value
		String langSuffix = (pf.lang!=null && !pf.lang.isEmpty()) ? "@"+pf.lang : "";
		String pValStr = po.data.get(fieldUri + langSuffix);
		logger.trace("retrieveFieldValue: Arguments: persistent value={}", pValStr);
		
		// process field value and convert it to an object
		Object pValue = null;
		if (pValStr==null) {
			logger.trace("retrieveFieldValue: \t persistent value NOT FOUND");
			if (clearFields || pf.omitIfNull) {
				pValue = getDefaultValueForType(pf);
				logger.trace("retrieveFieldValue: \t returning null or primitive type default: {}", pValue);
			} else {
				logger.trace("retrieveFieldValue: \t field must be ignored");
				return ignoreToken;
			}
		} else
		if (pf.isUri) {
			pValue = (pValStr.startsWith("<") && pValStr.endsWith(">")) ? pValStr.substring(1, pValStr.length()-1) : pValStr;
			logger.trace("retrieveFieldValue: \t persistent value is URI: {}", pValue);
		} else
		if (pValStr.equals(nullUriInBrackets) || pValStr.equals(nullUri)) {
			logger.trace("retrieveFieldValue: \t persistent value is null");
			pValue = null;
		} else
		
		// checking if it is an array
		if (type.isArray() && dim>0) {
			logger.trace("retrieveFieldValue: \t value is an Array");
			pValue = retrieveArray(fieldUri, pf.arrayDimensions, po, pc, pf, visited, deepRefresh, clearFields, ignoreToken);
			if (pValue==ignoreToken) logger.trace("retrieveFieldValue: \t value must be ignored" );
			else logger.trace("retrieveFieldValue: \tArray length={}", pValue!=null ? ((Object[])pValue).length : "null" );
		} else
		if (type.isArray() && dim<=0) {
			throw new RdfPersistenceException("*** INTERNAL ERROR: zero or negative array dim="+dim+" : Possibly a bug ***");
		} else
		
		// checking if it is a collection
		if (Collection.class.isAssignableFrom(type)) {
			logger.trace("retrieveFieldValue: \t value is a Collection");
			//logger.trace("CALLING: retrieveCollection: type={}, pf.type={}", type, pf.type);
			pValue = retrieveCollection(type, fieldUri, po, pc, pf, visited, deepRefresh, clearFields, ignoreToken);
		} else
		// checking if it is a map
		if (Map.class.isAssignableFrom(type)) {
			logger.trace("retrieveFieldValue: \t value is a Map");
			//logger.trace("CALLING: retrieveMap: type={}, pf.type={}", type, pf.type);
			pValue = retrieveMap(type, fieldUri, po, pc, pf, visited, deepRefresh, clearFields, ignoreToken);
		} else
		
		// checking if it is a reference
		if (pValStr.startsWith("<") && pValStr.endsWith(">")) {		// It's a reference
			logger.trace("retrieveFieldValue: \t field value IS A reference: {}", pValStr);
			if (pf.cascadeRefresh || deepRefresh) {
				logger.trace("retrieveFieldValue: \t cascade refresh/retrieve is allowed");
				String roUri = pValStr.substring(1, pValStr.length()-1);
				logger.trace("retrieveFieldValue: \t retrieving referenced object: recursive call to retrieveObject: uri={}", roUri);
				//
				Object refObj = retrieveObject( roUri, visited, pf.type );
				logger.trace("retrieveFieldValue: \t referenced object: {}", refObj);
				pValue = refObj;
				if (pValue==ignoreToken) logger.trace("retrieveFieldValue: \t value must be ignored" );
			} else {
				logger.trace("retrieveFieldValue: \t No cascade refresh for field: Field value must remain unchanged");
				pValue = ignoreToken;
			}
		} else
		//if (isLiteralType(type)) {
		//...or just:
		{
			logger.trace("retrieveFieldValue: \t field value is single literal value: {}", pValStr);
			pValue = parseValue(pValStr);
			logger.trace("retrieveFieldValue: \t field value: {}", pValue);
		}
		/*else {
			throw new RuntimeException("*** INTERNAL ERROR: Execution MUST NEVER have reached this statement : IT'S A BUG!!! ***");
		}*/
		
		logger.debug("retrieveFieldValue: END: result={}", pValue);
		return pValue;
	}
	
	protected Object retrieveArray(String fieldUri, int dim, PersistentObject po, PersistentClass pc, PersistentField pf, HashMap<String,Object> visited, boolean deepRefresh, boolean clearFields, Object ignoreToken) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, UnsupportedEncodingException {
		logger.debug("retrieveArray: BEGIN: field-uri={}, dim={}, persistent-data={}", fieldUri, dim, po.data);
		if (dim<=0) throw new IllegalArgumentException("retrieveArray: Invalid array dimension: "+dim);
		
		// retrieve array length
		String pValStr = po.data.get(fieldUri);
		if (pValStr==null) {
			logger.debug("retrieveArray: END: Array length value IS MISSING. Value must be ignored. Possibly corrupted persistent data?");
			return ignoreToken;
		}
		logger.trace("retrieveArray: Persisted array length value: {}", pValStr);

		// if array is null
		if (pValStr.equals(nullUri) || pValStr.equals(nullUriInBrackets)) {
			logger.debug("retrieveArray: END: Array length value IS NULL. Returning null array");
			return null;
		}
		// else if array value is not null...
		
		// instantiate array
		int arrLen = (Integer)parseValue(pValStr);
		int[] dimensions = new int[dim];
		dimensions[0] = arrLen;
		Object[] arr = (Object[])Array.newInstance(pf.arrayComponentType, dimensions);
		
		// process array elements
		fieldUri = fieldUri+asep;
		if (dim==1) {	// 1-d array or lowest-level dimension of a multidimensional array
			logger.trace("retrieveArray: LOOP-1: BEGIN: Processing array elements: 1-D array or last-level of a multidimensional array");
			for (int i=0; i<arrLen; i++) {
				logger.trace("retrieveArray: LOOP-1: ITERATION #{}: Processing element={}", i);
				arr[i] = retrieveFieldValue(pf.arrayComponentType, fieldUri+i, -1, po, pc, pf, visited, deepRefresh, clearFields, ignoreToken);
				logger.trace("retrieveArray: LOOP-1: \t element value={}", arr[i]);
			}
			logger.trace("retrieveArray: LOOP-1: END");
		} else {
			logger.trace("retrieveArray: LOOP-1: BEGIN: Processing array elements: Multidimensional array: dim={}", dim);
			int dim1 = dim-1;
			for (int i=0; i<arrLen; i++) {
				logger.trace("retrieveArray: LOOP-1: ITERATION #{}: Processing element={}", i);
				Object ret = retrieveArray(fieldUri+i, dim1, po, pc, pf, visited, deepRefresh, clearFields, ignoreToken);
				logger.trace("retrieveArray: LOOP-1: \t element type={}, dim={}", ret!=null ? ret.getClass() : "null", dim);
				arr[i] = ret;
				logger.trace("retrieveArray: LOOP-1: \t sub-array length={}", arr[i]!=null ? ((Object[])arr[i]).length : "null");
			}
			logger.trace("retrieveArray: LOOP-1: END");
		}
		
		logger.debug("retrieveArray: END: dim={}", dim);
		return arr;
	}
	
	protected Object retrieveCollection(Class type, String fieldUri, PersistentObject po, PersistentClass pc, PersistentField pf, HashMap<String,Object> visited, boolean deepRefresh, boolean clearFields, Object ignoreToken) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, UnsupportedEncodingException {
		logger.debug("retrieveCollection: BEGIN: type={}, pf.type={}, field-uri={}, persistent-data={}", type, pf.type, fieldUri, po.data);
		
		if (!Collection.class.isAssignableFrom(type)) throw new IllegalArgumentException("retrieveCollection: Passed 'type' argument is not Collection: "+type);
		
		// retrieve collection size
		String pValStr = po.data.get(fieldUri);
		if (pValStr==null) {
			logger.debug("retrieveCollection: END: Collection size value IS MISSING. Value must be ignored. Possibly corrupted persistent data?");
			return ignoreToken;
		}
		logger.trace("retrieveCollection: Persisted collection size value: {}", pValStr);

		// if collection is null
		if (pValStr.equals(nullUri) || pValStr.equals(nullUriInBrackets)) {
			logger.debug("retrieveCollection: END: Collection size value IS NULL. Returning null collection");
			return null;
		}
		// else if collection value is not null...
		int size = (Integer)parseValue(pValStr);
		logger.trace("retrieveCollection: Collection size: {}", size);
		
		// instantiate collection
		Collection col = null;
		if (!type.isInterface()) {
			col = (Collection)type.newInstance();
			logger.trace("retrieveCollection: Collection instantiated using passed type: {}", col.getClass());
		} else {
			if (List.class.isAssignableFrom(pf.type)) {
				col = new LinkedList();
			} else
			if (Queue.class.isAssignableFrom(pf.type)) {
				col = new LinkedList();		// It's also a queue
			} else
			if (Set.class.isAssignableFrom(pf.type)) {
				col = new HashSet();
			} else {
				throw new RuntimeException("retrieveCollection: Cannot resolve actual collection type: Passed type: "+type);
			}
			logger.trace("retrieveCollection: Collection type passed is an interface: using default implementation for type: {}", col.getClass());
		}
		
		// get declared collection elements type
		logger.trace("retrieveCollection: Detecting collection element type...");
		Class elemType = Object.class;
		//logger.trace("retrieveCollection: Class.getTypeParameters: {}", Arrays.deepToString(type.getTypeParameters()));
		if (type.getTypeParameters().length==1) {
			Type genericType = type.getTypeParameters()[0];
			//logger.trace("retrieveCollection: genericType: {}  ({})", genericType, genericType.getClass());
			if(genericType instanceof ParameterizedType){
				ParameterizedType aType = (ParameterizedType) genericType;
				Type[] argTypes = aType.getActualTypeArguments();
				//logger.trace("retrieveCollection: ParameterizedType.getActualTypeArguments(): {}", Arrays.deepToString(argTypes));
				for(Type argType : argTypes){
					elemType = getClass(argType);	//(Class) argType;
					logger.trace("retrieveCollection: Detected collection elements type: {}", elemType);
				}
			}
		}
		logger.trace("retrieveCollection: Using collection elements type: {}", elemType);

		logger.trace("retrieveCollection: pf.type: {}", pf.type);
		if (pf.type.getTypeParameters().length==1) {
			Type genericType = pf.type.getTypeParameters()[0];
			//logger.trace("retrieveCollection: pf.type: genericType: {}  ({})", genericType, genericType.getClass());
			if(genericType instanceof ParameterizedType){
				ParameterizedType aType = (ParameterizedType) genericType;
				Type[] argTypes = aType.getActualTypeArguments();
				//logger.trace("retrieveCollection: pf.type: ParameterizedType.getActualTypeArguments(): {}", Arrays.deepToString(argTypes));
				for(Type argType : argTypes){
					Class typ = getClass(argType);	//(Class) argType;
					logger.trace("retrieveCollection: pf.type: Detected collection elements type: {}", typ);
				}
			}
		}
		logger.trace("retrieveCollection: pf.type/generics: {}", pf.type);
		
		// retrieve collection elements
		logger.trace("retrieveCollection: LOOP-1: BEGIN: Retrieving collection elements");
		fieldUri = fieldUri+csep;
		for (int i=0; i<size; i++) {
			logger.trace("retrieveCollection: LOOP-1: ITERATION #{}: Retrieving element...", i);
			//logger.trace("CALLING: retrieveFieldValue: elem-type={}, pf.type={}", elemType, pf.type);
			Object elem = retrieveFieldValue(elemType, fieldUri+i, -1, po, pc, pf, visited, deepRefresh, clearFields, ignoreToken);
			logger.trace("retrieveCollection: LOOP-1: \t element value={}", elem);
			col.add( elem );
		}
		logger.trace("retrieveCollection: LOOP-1: END");
		
		logger.debug("retrieveCollection: END: result={}", col);
		return col;
	}
	
	// --------------------------------------------------------------------------------------------------------
	/**
	 * Get the underlying class for a type, or null if the type is a variable
	 * type.
	 *
	 * @param type the type
	 * @return the underlying class
	 */
	public static Class<?> getClass(Type type)
	{
		if (type instanceof Class) {
			return (Class) type;
		} else if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		} else if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			Class<?> componentClass = getClass(componentType);
			if (componentClass != null) {
				return Array.newInstance(componentClass, 0).getClass();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	// --------------------------------------------------------------------------------------------------------
	
	protected Object retrieveMap(Class type, String fieldUri, PersistentObject po, PersistentClass pc, PersistentField pf, HashMap<String,Object> visited, boolean deepRefresh, boolean clearFields, Object ignoreToken) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, UnsupportedEncodingException {
		logger.debug("retrieveMap: BEGIN: type={}, pf.type={}, field-uri={}, persistent-data={}", type, pf.type, fieldUri, po.data);
		
		if (!Map.class.isAssignableFrom(type)) throw new IllegalArgumentException("retrieveMap: Passed 'type' argument is not Map: "+type);
		
		// retrieve map size
		String pValStr = po.data.get(fieldUri);
		if (pValStr==null) {
			logger.debug("retrieveMap: END: Map size value IS MISSING. Value must be ignored. Possibly corrupted persistent data?");
			return ignoreToken;
		}
		logger.trace("retrieveMap: Persisted map size value: {}", pValStr);

		// if map is null
		if (pValStr.equals(nullUri) || pValStr.equals(nullUriInBrackets)) {
			logger.debug("retrieveMap: END: Map size value IS NULL. Returning a null map");
			return null;
		}
		// else if map value is not null...
		int size = (Integer)parseValue(pValStr);
		logger.trace("retrieveMap: Map size: {}", size);
		
		// instantiate map
		Map map = null;
		if (!type.isInterface()) {
			map = (Map)type.newInstance();
			logger.trace("retrieveMap: Map instantiated using passed type: {}", map.getClass());
		} else {
			if (Map.class.isAssignableFrom(pf.type)) {
				map = new HashMap();
			} else {
				throw new RuntimeException("retrieveMap: Cannot resolve actual Map type: Passed type: "+type);
			}
			logger.trace("retrieveMap: Map type passed is an interface: using default implementation for type: {}", map.getClass());
		}
		
		// get map key type
		logger.trace("retrieveMap: Detecting map element type...");
		Class keyType = Object.class;
		//logger.trace("retrieveMap: KEY: Class.getTypeParameters: {}", Arrays.deepToString(type.getTypeParameters()));
		if (type.getTypeParameters().length==2) {
			Type genericType = type.getTypeParameters()[0];
			//logger.trace("retrieveMap: KEY: genericType: {}  ({})", genericType, genericType.getClass());
			if(genericType instanceof ParameterizedType){
				ParameterizedType aType = (ParameterizedType) genericType;
				Type[] argTypes = aType.getActualTypeArguments();
				//logger.trace("retrieveMap: KEY: ParameterizedType.getActualTypeArguments(): {}", Arrays.deepToString(argTypes));
				for(Type argType : argTypes){
					keyType = getClass(argType);	//(Class) argType;
					logger.trace("retrieveMap: KEY: Detected map elements type: {}", keyType);
				}
			}
		}
		logger.trace("retrieveMap: Using map elements type: {}", keyType);

		// get map value type
		logger.trace("retrieveMap: Detecting map value type...");
		Class valueType = Object.class;
		//logger.trace("retrieveMap: VALUE: Class.getTypeParameters: {}", Arrays.deepToString(type.getTypeParameters()));
		if (type.getTypeParameters().length==2) {
			Type genericType = type.getTypeParameters()[1];
			//logger.trace("retrieveMap: VALUE: genericType: {}  ({})", genericType, genericType.getClass());
			if(genericType instanceof ParameterizedType){
				ParameterizedType aType = (ParameterizedType) genericType;
				Type[] argTypes = aType.getActualTypeArguments();
				//logger.trace("retrieveMap: VALUE: ParameterizedType.getActualTypeArguments(): {}", Arrays.deepToString(argTypes));
				for(Type argType : argTypes){
					valueType = getClass(argType);	//(Class) argType;
					logger.trace("retrieveMap: VALUE: Detected map elements type: {}", valueType);
				}
			}
		}
		logger.trace("retrieveMap: Using map value type: {}", valueType);
		
		// retrieve map pairs
		logger.trace("retrieveMap: LOOP-1: BEGIN: Retrieving map pairs");
		fieldUri = fieldUri+msep;
		for (int i=0; i<size; i++) {
			logger.trace("retrieveMap: LOOP-1: ITERATION #{}: Retrieving pairs...", i);
			//logger.trace("CALLING: retrieveFieldValue: key-type={}, pf.type={}", keyType, pf.type);
			Object key = retrieveFieldValue(keyType, fieldUri+i+ksep, -1, po, pc, pf, visited, deepRefresh, clearFields, ignoreToken);
			//logger.trace("CALLING: retrieveFieldValue: value-type={}, pf.type={}", valueType, pf.type);
			Object value = retrieveFieldValue(valueType, fieldUri+i+vsep, -1, po, pc, pf, visited, deepRefresh, clearFields, ignoreToken);
			logger.trace("retrieveMap: LOOP-1: \t pair: key={}, value={}", key, value);
			map.put( key, value );
		}
		logger.trace("retrieveMap: LOOP-1: END");
		
		logger.debug("retrieveMap: END: result={}", map);
		return map;
	}
	
	// =======================================================================================================================
	
	// SPARQL statements generation methods
	
	protected void addObjectInProcessingQueue(Object o) throws IllegalAccessException, InvocationTargetException {
		logger.debug("addObjectInProcessingQueue: BEGIN: object={}", objectHash(o));
		logger.trace("addObjectInProcessingQueue: Processing queue BEFORE: {}", processingQueue);
		if (visited.contains(o)) {
			logger.debug("addObjectInProcessingQueue: END: object has already been processed");
			return;
		}
		String oUri = getObjectUri(o);
		if (pendingDeletes.contains(oUri)) {
			pendingDeletes.remove(oUri);
			visitedUrisForDelete.remove(oUri);
			logger.trace("addObjectInProcessingQueue: object removed from marked for delete list");
		}
		processingQueue.offer(o);
		logger.trace("addObjectInProcessingQueue: Processing queue AFTER: {}", processingQueue);
		logger.debug("addObjectInProcessingQueue: END: object added in processing queue");
	}
	
	protected void markForDelete(String oUri) {
		logger.debug("markForDelete: BEGIN: Object for delete: uri={}", oUri);
		if (oUri==null) { logger.debug("markForDelete: END: Argument URI is null: Nothing to do"); return; }
		if (logger.isTraceEnabled()) logger.trace("markForDelete: Visited URIs: {}", visitedUris.keySet());
		if (visitedUris.containsKey(oUri)) {
			logger.debug("markForDelete: END: Object is used. Cannot be marked for delete: uri={}", oUri);
			return;		// if object has been visited then it is included in the live object graph. therefore it MUST NOT BE DELETED !!!
		}
		
		pendingDeletes.add(oUri);
		logger.trace("markForDelete: END: Object marked for Delete: uri={}", oUri);
	}
	
	protected void generateDeleteWhereStatement(String oUri) {
		int p = deleteStatements.length();
		deleteStatements.append( "DELETE { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER (?s = <" ).append(oUri).append( ">) . } ;\n" );
		if (logger.isDebugEnabled()) logger.debug("generateDeleteWhereStatement: Appended DELETE statement: {}", deleteStatements.substring(p).trim());
	}
	
	protected void generateInsertStatement(String oUri, String fUri, String valStr) {
		int p = insertStatements.length();
		insertStatements.append("\t<").append(oUri).append(">\t<").append(fUri).append(">\t").append(valStr).append(" .\n");
		if (logger.isDebugEnabled()) logger.debug("generateInsertStatement: Appended INSERT statement: {}", insertStatements.substring(p).trim());
	}
	
	protected void generateObjectHeaders(PersistentObject po, PersistentClass pc) {
		String javaTypeStr = formatValue( pc.type.getName(), String.class );
		int p = insertStatements.length();
		String isAValue = (pc.rdfType!=null && !pc.rdfType.isEmpty()) ? pc.rdfType : pc.typeUri;
		
		// prepare isAValue (if it is a list)
		String[] apart = isAValue.split("[,]");
		if (apart.length>1) {
			StringBuilder sb = new StringBuilder();
			boolean first=true;
			for (int i=0; i<apart.length; i++) {
				if (!(apart[i]=apart[i].trim()).isEmpty()) {
					if (first) first=false; else sb.append(", ");
					sb.append("<").append(apart[i]).append(">");
				}
			}
			isAValue = sb.toString();
		} else {
			isAValue = "<"+isAValue+">";
		}
		
		if (!pc.suppressRdfType) {
			insertStatements.append("\t<").append(po.objectUri).append(">\ta\t").append(isAValue).append(" .\n");
		}
		if (!pc.suppressJavaType) {
			insertStatements.append("\t<").append(po.objectUri).append(">\t").append(defaultTypeFieldUriInBrackets).append("\t").append(javaTypeStr).append(" .\n");
		}
		if (logger.isDebugEnabled()) logger.debug("generateObjectHeaders: Appended INSERT statements for object header: \"{}\"", insertStatements.substring(p).trim());
	}
	
	// Auxiliary methods
	
	protected static boolean isReservedFieldUri(String fieldUri) {
		return fieldUri.equals(defaultTypeFieldUri) || fieldUri.equals(rdfTypeUri);
	}
	
	protected PersistentObject getPersistentState(Object o) throws IllegalAccessException, InvocationTargetException {
		logger.debug("getPersistentState: BEGIN: object={}", objectHash(o));
		String oUri = getObjectUri(o);
		logger.trace("getPersistentState: uri={}", oUri);
		return getPersistentState(oUri);
	}
	protected PersistentObject getPersistentState(String oUri) throws IllegalAccessException, InvocationTargetException {
		logger.debug("getPersistentState: BEGIN: object uri={}", oUri);
		logger.trace("getPersistentState: Querying SPARQL service for persistent object state");
		Map<String,String> data = client.queryBySubject(oUri);
		logger.trace("getPersistentState: Persistent object state retrieved: {}", data);
		PersistentObject po = new PersistentObject(oUri, data);
		logger.debug("getPersistentState: END: result={}", po);
		return po;
	}
	
	protected static String formatValue(Object o, PersistentField pf) {
		return formatValue(o, pf.type, pf.lang);
	}
	
	protected static String formatValue(Object o, Class type) {
		return formatValue(o, type, null);
	}
	
	protected static String formatValue(Object o, Class type, String lang) {
		logger.debug("formatValue: BEGIN: object={}, type={}", o, type);
		if (o==null) return nullUriInBrackets;
		// if a binding is found then use binding's formatter, else generate a URI (<...>) string
		String formatter = java2xsdBinds.get(type);
		logger.trace("formatValue: formatter={}", formatter);
		if (type.equals(Date.class)) {
			logger.trace("formatValue: Converting Date object to Calendar (needed in order to render it in W3C dateTime format)");
			Calendar cal = Calendar.getInstance();
			cal.setTime((Date)o);
			o = DatatypeConverter.printDateTime(cal);
		}
		if (formatter!=null) {
			String valStr;
			String so = o.toString();
			// escape double quotes
			if (type.equals(String.class)) {
				so = so.replace("\"", "\\\"");
			}
			if (lang==null || lang.isEmpty()) {
				//String valStr = URLEncoder.encode( String.format(formatter, o), "UTF-8" );
				valStr = String.format(formatter, so);
			} else {
				valStr = String.format(langStringBindType, so, lang);
			}
			logger.debug("formatValue: END: result={}", valStr);
			return valStr;
		} else {
			logger.error("formatValue: ERROR: Throwing Exception: No class-to-xsd bind exists for class: {}", type.getClass());
			throw new RdfPersistenceException("No class-to-xsd bind exists for class: "+type);
		}
	}
	
	public static Object parseValue(String valStr) throws UnsupportedEncodingException {
		logger.debug("parseValue: BEGIN: value={}", valStr);
		valStr = valStr.trim();
		if (valStr.equals(nullUri) || valStr.equals(nullUriInBrackets)) {
			logger.debug("parseValue: END: result=null");
			return null;
		}
		
		// split value to lexical part and datatype part (XSD format "..."^^<....#datatype>
		String lexicalPart0 = null;
		String lexicalPart = null;
		String datatypePart = null;
		String lang = null;
		
		// check if value string is of the form: "....."^^<.....>
		int p1=valStr.lastIndexOf("^^");
		if (p1>-1) {
			//logger.trace("parseValue: value has datatype");
			lexicalPart = valStr.substring(0,p1);	//.trim();
			datatypePart = (p1+2<valStr.length()) ? valStr.substring(p1+2).trim().toLowerCase() : "";
		} else {
			// check if value string is of the form: "....."@XX
			p1=valStr.lastIndexOf("@");
			if (p1>-1) {
				//logger.trace("parseValue: value has language tag");
				lexicalPart = valStr.substring(0,p1);	//.trim();
				lang = (p1+1<valStr.length()) ? valStr.substring(p1+1).trim() : null;
			} else {
				// check if no datatype and no language tag have been specified
				//logger.trace("parseValue: value has NO datatype and NO language tag. Using defaults");
				lexicalPart = valStr;
			}
			datatypePart = defaultXsdType;
		}
		
		// prepare lexical part
		//logger.trace("parseValue: before processing: lexical-part={}, datatype-part={}, lang={}", lexicalPart, datatypePart, lang);
		if (lexicalPart.startsWith("\"") && lexicalPart.endsWith("\"")) {
			lexicalPart = lexicalPart.substring(1,lexicalPart.length()-1).trim();
			lexicalPart = lexicalPart.replace("\\\"","\"");
		}
		lexicalPart0 = lexicalPart;
		//lexicalPart = URLDecoder.decode( lexicalPart, "UTF-8" );
		
		//prepare datatype part
		if (datatypePart!=null && !datatypePart.isEmpty()) {
			int p2 = datatypePart.lastIndexOf("#");
			if (p2>-1 && p2+1<datatypePart.length()) datatypePart = datatypePart.substring(p2+1,datatypePart.length()-1).trim();
			if (datatypePart.isEmpty()) datatypePart = defaultXsdType;
			//logger.trace("parseValue: after processing: lexical-part={}, datatype-part={}", lexicalPart, datatypePart);
		}
		
		// lookup xsd-to-java datatype binds
		Class type = xsd2javaDatatypes.get(datatypePart);
		//logger.trace("parseValue: xsd-to-java datatype bind: {}", type.getSimpleName());
		
		// instantiate value
		//logger.trace("parseValue: instantiating value...");
		Object val = null;
		if (type==Boolean.class) {
			val = Boolean.valueOf(lexicalPart);
		} else
		if (type==Character.class) {
			val = Character.valueOf(lexicalPart.charAt(0));
		} else
		if (type==Byte.class) {
			val = Byte.valueOf(lexicalPart);
		} else
		if (type==Short.class) {
			val = Short.valueOf(lexicalPart);
		} else
		if (type==Integer.class) {
			val = Integer.valueOf(lexicalPart);
		} else
		if (type==Long.class) {
			val = Long.valueOf(lexicalPart);
		} else
		if (type==Float.class) {
			val = Float.valueOf(lexicalPart);
		} else
		if (type==Double.class) {
			val = Double.valueOf(lexicalPart);
		} else
		if (type==BigInteger.class) {
			val = new BigInteger(lexicalPart);
		} else
		if (type==BigDecimal.class) {
			val = new BigDecimal(lexicalPart);
		} else
		if (type==String.class) {
			val = lexicalPart;
		} else
		if (type==Date.class) {
			val = DatatypeConverter.parseDateTime(lexicalPart0).getTime();
		} else {
			throw new RdfPersistenceException("Unsupported value datatype: "+datatypePart+", value="+valStr);
		}
		logger.debug("parseValue: END: result={}", val);
		return val;
	}
	
	protected static Class getTypeFromUri(String uri, PersistentObject po) throws ClassNotFoundException, UnsupportedEncodingException {
		logger.trace("getTypeFromUri: BEGIN: uri={}", uri);
		if (uri==null) {
			logger.trace("getTypeFromUri: END: result=null");
			return null;
		}
		logger.trace("getTypeFromUri: getting object type from persisted data...");
		String typeStr = null;
		try {
			typeStr = po.getFieldXsdValue(defaultTypeFieldUri);
		} catch (RdfPersistenceException e) {
			// Actual java class for the given URI has not been persisted.
			// Next we try to figure it out using class information collected previously, during class loading and analysis
			// Information originate from class annotations (rdfType) and information from 'preload-types.properties' file
			
			// Looking for an RDF class specification (i.e. an 'rdf:type' or 'a' property indicating RDF class)
			// If one or more exist and one of them is registered with a java class then that (java) class is returned
			logger.debug("getTypeFromUri: object type is not in persisted data. Looking up its class");			
			try {
				String classStr = po.getFieldXsdValue(rdfTypeUri);
				if (classStr!=null && !classStr.trim().isEmpty()) {
					logger.trace("getTypeFromUri: object class specified using 'a' property: {}", classStr);
					for (String s : classStr.split(",")) {
						s = s.trim();
						logger.trace("getTypeFromUri: Checking class: {}", s);
						if (s.isEmpty()) continue;
						if (s.startsWith("<") && s.endsWith(">")) s = s.substring(1, s.length()-1);		// remove brackets
						
						//...find class with this URI
						Class type = getJavaTypeFromRdfType(s, true);
						//...IF FOUND get java class and return it....
						if (type!=null) {
							logger.trace("getTypeFromUri: END: result={}", type);
							return type;
						}
						//...if NOT FOUND... CONTINUE
					}
				} else {
					logger.trace("getTypeFromUri: No object class specified using 'a' property: {}", classStr);
				}
			} catch (RdfPersistenceException e2) {
				logger.debug("getTypeFromUri: object class not found");
			}
			
			// Looking for an 'rdfs:subClassOf' property indicating RDF parent class
			// If one or more exist and one of them is registered with a java class then that (java) class is returned
			logger.debug("getTypeFromUri: object type is not in persisted data. Checking if it is a sub-class");			
			try {
				String classStr = po.getFieldXsdValue(rdfsSubClassOf);
				if (classStr!=null && !classStr.trim().isEmpty()) {
					logger.trace("getTypeFromUri: object class is defined using 'rdfs:subClassOf': {}", classStr);
					for (String s : classStr.split(",")) {
						s = s.trim();
						logger.trace("getTypeFromUri: Checking parent class: {}", s);
						if (s.isEmpty()) continue;
						if (s.startsWith("<") && s.endsWith(">")) s = s.substring(1, s.length()-1);		// remove brackets
						
						// find java class with this URI
						Class type = getJavaTypeFromRdfType(s, false);
						if (type!=null) {
							logger.trace("getTypeFromUri: END: result={}", type);
							return type;
						}
						//...if NOT FOUND... CONTINUE
					}
				} else {
					logger.trace("getTypeFromUri: No object class specified using 'rdfs:subClassOf': {}", classStr);
				}
			} catch (RdfPersistenceException e2) {
				logger.debug("getTypeFromUri: object is not a sub-class");
			}
			
			// Looking for an 'rdfs:subPropertyOf' property indicating RDF parent property
			// If one or more exist and one of them is registered with a java class then that (java) class is returned
			logger.debug("getTypeFromUri: object type is not in persisted data. Checking if it is a sub-property");			
			try {
				String propStr = po.getFieldXsdValue(rdfsSubPropertyOf);
				if (propStr!=null && !propStr.trim().isEmpty()) {
					logger.trace("getTypeFromUri: object class is defined using 'rdfs:subPropertyOf': {}", propStr);
					for (String s : propStr.split(",")) {
						s = s.trim();
						logger.trace("getTypeFromUri: Checking parent property: {}", s);
						if (s.isEmpty()) continue;
						if (s.startsWith("<") && s.endsWith(">")) s = s.substring(1, s.length()-1);		// remove brackets
						
						// find java class with this URI
						Class type = getJavaTypeFromRdfType(s, false);
						if (type!=null) {
							logger.trace("getTypeFromUri: END: result={}", type);
							return type;
						}
						//...if NOT FOUND... CONTINUE
					}
				} else {
					logger.trace("getTypeFromUri: No object class specified using 'rdfs:subPropertyOf': {}", propStr);
				}
			} catch (RdfPersistenceException e2) {
				logger.debug("getTypeFromUri: object is not a sub-property");
			}
			
			// If everything else fails try to find java class by comparing uri to various pre-registered URI patterns
			// This is by no means a secure and correct way to do the job. It's a means of last resort.
			
			// Nothing found. Give up
			if (typeStr==null) {
				logger.debug("@@@@ Object URI: {}", uri);
				logger.debug("@@@@ Persisted data: {}", po.data);
				logger.debug("@@@@ Registered RDF classes: {}", _typeRdfClasses);
				throw new RdfPersistenceException("getTypeFromUri: Object type not found in registered rdf types and patterns: "+uri);
			}
		}
		logger.trace("getTypeFromUri: object type from persisted data: {}", typeStr);
		typeStr = (String)parseValue(typeStr);
		logger.trace("getTypeFromUri: object type: {}", typeStr);
		
		// getting class from typeStr
		Class type = Class.forName(typeStr);
		logger.trace("getTypeFromUri: END: result={}", type);
		return type;
	}
	
	protected static String _getObjectUri(Object o) throws IllegalAccessException, InvocationTargetException {
		logger.debug("getObjectUri: BEGIN: object={}", objectHash(o));
		
		// check if object is already managed
		if (managedObjectUris.containsKey(o)) {
			String oUri = managedObjectUris.get(o);
			logger.debug("getObjectUri: object is already managed: uri={}", oUri);
			logger.debug("getObjectUri: END: result={}", oUri);
			return oUri;
		}
		
		PersistentClass pc = getTypeDescriptor(o.getClass());
		Object idVal = getFieldValue( o, pc.idField );
		logger.trace("getObjectUri: idVal={}", idVal);
		String idStr = idVal.toString();
		logger.trace("getObjectUri: idStr={}, id-formatter={}", idVal, pc.idFormatter);
		String oUri = String.format( pc.idFormatter, idStr );
		logger.debug("getObjectUri: END: result={}", oUri);
		return oUri;
	}
	
	protected static String _getObjectUri(String id, Class type) {
		logger.debug("getObjectUri: BEGIN: id={}, type={}", id, type.getName());
		PersistentClass pc = getTypeDescriptor(type);
		logger.trace("getObjectUri: id-formatter={}", pc.idFormatter);
		String oUri = String.format( pc.idFormatter, id );
		logger.debug("getObjectUri: END: result={}", oUri);
		return oUri;
	}
	
	protected static PersistentClass getTypeDescriptor(Object o) {
		if (o instanceof Class) return getTypeDescriptor((Class)o);
		else return getTypeDescriptor(o.getClass());
	}
	
	protected static PersistentClass getTypeDescriptor(Class type) {
		logger.debug("getTypeDescriptor: BEGIN: type={}", type);
		PersistentClass pc = null;
		if (!_analyzedTypes.containsKey(type)) {
			logger.trace("getTypeDescriptor: First occurence of object type: Analyzing: {}", type.getName());
			analyzeObjectType(type);
		}
		pc = _analyzedTypes.get(type);
		if (pc==null) throw new RdfPersistenceException("Object type is not persistable: "+type.getName());
		logger.debug("getTypeDescriptor: END: object type description={}", pc);
		return pc;
	}
	
	protected static Object getFieldValue(Object o, PersistentField f) throws IllegalAccessException, InvocationTargetException {
		if (f.hasGetter) {
			return f.getter.invoke(o);
		} else {
			return f.field.get(o);
		}
	}
	
	protected static void setFieldValue(Object o, Object value, PersistentField f) throws IllegalAccessException, InvocationTargetException {
		if (f.hasSetter) {
			f.setter.invoke(o, f.type.isPrimitive() ? value : f.type.cast(value));
		} else {
			f.field.set(o, f.type.isPrimitive() ? value : f.type.cast(value));
		}
	}
	
	protected static Object getDefaultValueForType(PersistentField f) {
		Object defVal = null;
		Class type = f.type;
		if (type.isPrimitive()) {
			if (type==Boolean.TYPE) defVal = false;
			else
			if (type==Character.TYPE) defVal = '\0';
			else
			if (type==Byte.TYPE) defVal = 0;
			else
			if (type==Short.TYPE) defVal = 0;
			else
			if (type==Integer.TYPE) defVal = 0;
			else
			if (type==Long.TYPE) defVal = 0;
			else
			if (type==Float.TYPE) defVal = 0;
			else
			if (type==Double.TYPE) defVal = 0;
		}
		else if (type==BigInteger.class) defVal = BigInteger.ZERO;
		else if (type==BigDecimal.class) defVal = BigDecimal.ZERO;
		
		return defVal;
	}
	
	protected static boolean isLiteralType(Class ftype) {
		logger.debug("isLiteralType: type={}", ftype);
		boolean result = ftype.isPrimitive() || ftype.equals(Boolean.class) || ftype.equals(Byte.class) ||
						ftype.equals(Character.class) || ftype.equals(Short.class) || ftype.equals(Integer.class) ||
						ftype.equals(Long.class) || ftype.equals(Float.class) || ftype.equals(Double.class) ||
						ftype.equals(java.lang.String.class) || ftype.equals(Date.class) ||
						ftype.equals(java.math.BigInteger.class) || ftype.equals(java.math.BigDecimal.class);
		logger.debug("isLiteralType: result={}", result);
		return result;
	}
	
	protected static boolean isReferenceType(Class ftype) {
		logger.debug("isReferenceType: type={}", ftype);
		boolean result = ! isLiteralType(ftype);
		logger.debug("isReferenceType: result={}", result);
		return result;
	}
	
	protected static synchronized void analyzeObjectType(Class type) {
		logger.debug("analyzeObjectType: BEGIN: type={}", type);
		if (_analyzedTypes.containsKey(type)) {		// Class already analyzed
			PersistentClass pc = _analyzedTypes.get(type);
			if (pc==null) {		// Class is NOT persistable. Throw an exception
				throw new RdfPersistenceException("Object type is not persistable: "+type);
			}
		}
		// else class has not been yet analyzed
		
		try {
			// analyzing class...
			logger.trace("analyzeObjectType: Analyzing class...");
			PersistentClass pc = new PersistentClass(type);
			// class was analyzed successfully
			_analyzedTypes.put( type, pc );
			
			if (pc!=null) {
				// register by using RDF class (if specified)
				if (pc.rdfType!=null) {
					registerTypeWithRdfClass(pc, pc.rdfType);
				}
				
				_typePatterns.put(pc.idPattern, pc);
				logger.trace("analyzeObjectType: Type pattern registered: "+pc.idPattern.pattern());
			}
			logger.trace("analyzeObjectType: Type analyzed and registered");
			logger.debug("analyzeObjectType: END");
		} catch (RdfPersistenceException e) {
			logger.error("analyzeObjectType: EXCEPTION:_1: {}", e);
			_analyzedTypes.put( type, null );
			logger.error("analyzeObjectType: EXCEPTION: Propagating RdfPersistenceException");
			throw e;
		} catch (Exception e) {
			// class failed during analysis (it is persistable but there are other problems with it)
			logger.error("analyzeObjectType: EXCEPTION:_2: {}", e);
			_analyzedTypes.put( type, null );
			logger.error("analyzeObjectType: EXCEPTION: Wrapping {} in an RdfPersistenceException and throwing it", e.getClass().getSimpleName());
			throw new RdfPersistenceException(e);
		}
	}
	
	protected static void registerTypeWithRdfClass(PersistentClass pc, String rdfClassCSV) {
		for (String rdfClass : rdfClassCSV.split(",")) {
			rdfClass = rdfClass.trim();
			if (rdfClass.isEmpty()) continue;
			
			// checking if RDF class is non-registerable (setting in rdf-persistence.properties)
			logger.trace("registerTypeWithRdfClass: Checking RDF class '{}' for registering with java class '{}'", rdfClass, pc.type);
			logger.trace("registerTypeWithRdfClass: Checking if RDF class '{}' is non-registerable", rdfClass);
			for (Pattern pat : nonRegisterableRdfClasses) {
				logger.trace("registerTypeWithRdfClass: ...comparing to non-registerable RDF class pattern: {}", pat.pattern());
				if (pat.matcher(rdfClass).matches()) {
					logger.trace("registerTypeWithRdfClass: ...MATCH!!  This RDF class is non-registerable", rdfClass);
					rdfClass = null;
					break;
				}
			}
			if (rdfClass==null) continue;
			logger.trace("registerTypeWithRdfClass: RDF class '{}' is registerable", rdfClass);
			
			if (_typeRdfClasses.containsKey(rdfClass)) throw new RdfPersistenceException("Cannot register RDF class "+rdfClass+" to java class: "+pc.type+". RDF class has already been registered by class: "+_typeRdfClasses.get(rdfClass).type.getName()+". Possibly the same 'rdfType' annotation or preload-specification has been used in both classes");
			_typeRdfClasses.put(rdfClass, pc);
			logger.debug("registerTypeWithRdfClass: RDF class: {} registered with java type: {}", rdfClass, pc.type.getName());
			
			// Register only the first RDF class specified
			//logger.trace("registerTypeWithRdfClass: will not register any more RDF class for this java class: {}", pc.type);
			//break;
		}
	}
	
	protected static Class getJavaTypeFromRdfType(String rdfType, boolean isInstance) {
		logger.trace("getJavaTypeFromRdfType: BEGIN: rdf-type={}", rdfType);
		PersistentClass pc = _typeRdfClasses.get(rdfType);
		Class type = (pc!=null) ? pc.type : null;
		if (type==null && isInstance) {
			logger.trace("getJavaTypeFromRdfType: Checking INSTANCE-OF rdf-type={}", rdfType);
			pc = _typeRdfClasses.get("instance_of:"+rdfType);
			type = (pc!=null) ? pc.type : null;
			
			if (type==null) {
				LinkedList<String> pending = new LinkedList<String>();
				Vector<String> checked = new Vector<String>();
				
				pending.add(rdfType);
				String s;
				while ((s=pending.poll())!=null) {
					String[] parents = getRdfClassParentClasses(rdfType);
					for (String par : parents) {
						if (par==null || par.trim().isEmpty()) continue;
						par = par.trim();
						
						if (!checked.contains(par) && !pending.contains(par)) {
							//pc = _typeRdfClasses.get(par);
							//type = (pc!=null) ? pc.type : null;
							//if (type==null) {
								pc = _typeRdfClasses.get("instance_of:"+par);
								type = (pc!=null) ? pc.type : null;
							//}
							if (type!=null) {
								logger.trace("getJavaTypeFromRdfType: END: result={}", type.getName());
								return type;
							}
							pending.add(par);
						}
					}
				}
			}
		}
		/*else
		if (type==null && !isInstance) {
			// Get type's parents
			logger.trace("getJavaTypeFromRdfType: Checking SUB-CLASS-OF rdf-type={}", rdfType);
			pc = _typeRdfClasses.get(rdfType);
			type = (pc!=null) ? pc.type : null;
			
			if (type==null) {
				LinkedList<String> pending = new LinkedList<String>();
				Vector<String> checked = new Vector<String>();
				
				pending.add(rdfType);
				String s;
				while ((s=pending.poll())!=null) {
					String[] parents = getRdfClassParentClasses(rdfType);
					for (String par : parents) {
						logger.trace("getJavaTypeFromRdfType: SUB-CLASS-OF part: checking parent class: {}", par);
						logger.trace("getJavaTypeFromRdfType: SUB-CLASS-OF part: _typeRdfClasses: {}", _typeRdfClasses);
						if (par==null || par.trim().isEmpty()) continue;
						par = par.trim();
						
						if (!checked.contains(par) && !pending.contains(par)) {
							//pc = _typeRdfClasses.get(par);
							//type = (pc!=null) ? pc.type : null;
							//if (type==null) {
								pc = _typeRdfClasses.get(par);
								type = (pc!=null) ? pc.type : null;
							//}
							if (type!=null) {
								logger.trace("getJavaTypeFromRdfType: END: result={}", type.getName());
								return type;
							}
							pending.add(par);
						}
					}
				}
			}
		}*/
		logger.trace("getJavaTypeFromRdfType: END: result={}", type!=null ? type.getName() : null);
		return type;
	}
	
	protected static HashMap<String,String[]> rdfClassParentClassesCache = new HashMap<String,String[]>();
	
	protected static synchronized String[] getRdfClassParentClasses(String rdfType) {
		logger.trace("getRdfClassParentClasses: BEGIN: rdf-type={}", rdfType);
		String[] parents = rdfClassParentClassesCache.get(rdfType);
		logger.trace("getRdfClassParentClasses: cached={}", parents);
		if (parents==null) {
			SparqlServiceClient clientTmp = SparqlServiceClientFactory.getClientInstance();
			String qry = String.format("SELECT DISTINCT ?s WHERE { <%s> <%s> ?s }", rdfType, rdfsSubClassOf);
			logger.trace("getRdfClassParentClasses: rdf query={}", qry);
			List<String> results = clientTmp.queryForIds(qry, null);
			parents = results.toArray(new String[results.size()]);
			for (int i=0, n=parents.length; i<n; i++) {
				String par = parents[i];
				if (par.startsWith("<") && par.endsWith(">")) {
					par = par.substring(1,par.length()-1).trim();
					parents[i] = par;
				}
				logger.trace("getRdfClassParentClasses: LOOP: caching parent: {}", par);
			}
			rdfClassParentClassesCache.put(rdfType, parents);
		}
		logger.trace("getRdfClassParentClasses: END: results={}", parents);
		return parents;
	}
	
	// ========================================================================================
	// RdfPersistenceManager static initialization and configuration methods and fields
	
	protected static final String asep = ":_";
	protected static final String csep = ":_";
	protected static final String msep = ":_";
	protected static final String ksep = "_name";
	protected static final String vsep = "_value";
	protected static final String rdfTypeUri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	protected static final String rdfTypeUriInBrackets = "<"+rdfTypeUri+">";
	protected static final String rdfsBaseUri = "http://www.w3.org/2000/01/rdf-schema#";
	protected static final String rdfsSubClassOf = rdfsBaseUri+"subClassOf";
	protected static final String rdfsSubPropertyOf = rdfsBaseUri+"subPropertyOf";
	
	protected static String defaultNamespacePrefix;
	protected static String defaultTypesPrefix;
	protected static String nullUri;
	protected static String nullUriInBrackets;
	protected static String defaultTypeFieldUri;
	protected static String defaultTypeFieldUriInBrackets;
	
	protected static String baseUriServiceAttribute;
	protected static String baseUriServiceCategories;
	protected static String baseUriBrokerPolicy;
	
	//---------	protected static String defaultXsd2javaBindType;
	protected static String defaultXsdType = "string";
	protected static Class<?> defaultJava2xsdBindType;
	protected static String langStringBindType;
	protected static HashMap<Class,String> java2xsdBinds;
	//--------	protected static HashMap<Class,Pattern> xsd2javaBinds;
	protected static HashMap<String,Class> xsd2javaDatatypes;
	
	protected static Vector<Pattern> nonRegisterableRdfClasses;
	protected static boolean ignoreUnknownProperties;
	
	protected static HashMap<Class,PersistentClass> _analyzedTypes;
	protected static HashMap<String,PersistentClass> _typeRdfClasses;
	protected static HashMap<Pattern,PersistentClass> _typePatterns;
	
	protected static HashMap<String,Object> managedObjects;
	protected static HashMap<Object,String> managedObjectUris;
	
	// Initialize RdfPersistanceManagerImpl4 class
	static {
		try {
			logger.debug("RdfPersistenceManager: <cinit>: Initializing...");
			
			// actual initialization
			_analyzedTypes = new HashMap<Class,PersistentClass>();
			_typeRdfClasses = new HashMap<String,PersistentClass>();
			_typePatterns = new HashMap<Pattern,PersistentClass>();
			managedObjects = new HashMap<String,Object>();
			managedObjectUris = new HashMap<Object,String>();
			
			// assign default config values
			_initDefaults();
			// load config settings from config files
			_initDefaultsFromConfigFiles();
			// preload and analyze classes
			_preloadTypes(RDF_CONFIG_BASE+"/preload-types.properties");
			
			logger.debug("Defaults:\n" +
						"defaultNamespacePrefix = {}\n" +
						"defaultTypesPrefix = {}\n" +
						"nullUri = {}\n" +
						"nullUriInBrackets = {}\n" +
						"defaultTypeFieldUri = {}\n" +
						"defaultTypeFieldUriInBrackets = {}\n",
					defaultNamespacePrefix, defaultTypesPrefix, nullUri, nullUriInBrackets, 
					defaultTypeFieldUri, defaultTypeFieldUriInBrackets);
			logger.debug("Type Bindings:\n" +
						"Java-to-Xsd bindings: {}\n" +
						"Default j2x binding:  {}\n" +
						"Xsd-to-Java bindings: {}\n" +
						"Default x2j binding:  {}",
						java2xsdBinds, defaultJava2xsdBindType, 
						xsd2javaDatatypes, defaultXsdType);
			
			logger.debug("RdfPersistenceManager: <cinit>: Initializing ... completed");
		} catch (Exception e) {
			logger.error("RdfPersistenceManager: <cinit>: Exception during initialization: ", e);
			throw new RdfPersistenceException(e);
		}
	}
	
	public static String[] getDefaultUris() {
		String[] uri = new String[4];
		uri[0] = defaultNamespacePrefix;
		uri[1] = defaultTypesPrefix;
		uri[2] = baseUriServiceCategories;
		uri[3] = baseUriBrokerPolicy;
		return uri;
	}
	
	public static void setDefaultUris(String bpNs, String nsPref, String cdNs) throws IOException, ClassNotFoundException {
		defaultNamespacePrefix = nsPref;
		defaultTypesPrefix = bpNs;
		baseUriServiceCategories = cdNs;
		baseUriBrokerPolicy = bpNs;
		
		// flush cached data
		_analyzedTypes = new HashMap<Class,PersistentClass>();
		_typeRdfClasses = new HashMap<String,PersistentClass>();
		_typePatterns = new HashMap<Pattern,PersistentClass>();
		managedObjects = new HashMap<String,Object>();
		managedObjectUris = new HashMap<Object,String>();
		
		// reload and re-analyze classes
		_preloadTypes(RDF_CONFIG_BASE+"/preload-types.properties");
	}
	
	// --------------------------------------------------------------------------
	// initialize defaults
	//
	protected static void _initDefaults() {
		defaultNamespacePrefix = "http://www.brokeratcloud.eu/";
		defaultTypesPrefix = defaultNamespacePrefix + "persist/types";
		nullUri = defaultTypesPrefix + "#null";
		nullUriInBrackets = "<"+nullUri+">";
		defaultTypeFieldUri = defaultTypesPrefix + "#class";
		defaultTypeFieldUriInBrackets = "<"+defaultTypeFieldUri+">";
		defaultXsdType = "string";
		
		_initJava2xsdBindings();
		_initXsd2javaDatatypes();
	}
	
	protected static void _initJava2xsdBindings() {
		defaultJava2xsdBindType = String.class;
		langStringBindType = "\"%s\"@%s";
		
		java2xsdBinds = new HashMap<Class,String>();
		// test binds for primitives
		java2xsdBinds.put(java.lang.Boolean.TYPE, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#boolean>");
		java2xsdBinds.put(java.lang.Byte.TYPE, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#byte>");
		java2xsdBinds.put(java.lang.Character.TYPE, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#string>");
		java2xsdBinds.put(java.lang.Double.TYPE, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#double>");
		java2xsdBinds.put(java.lang.Float.TYPE, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#float>");
		java2xsdBinds.put(java.lang.Integer.TYPE, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#int>");
		java2xsdBinds.put(java.lang.Long.TYPE, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#long>");
		java2xsdBinds.put(java.lang.Short.TYPE, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#short>");
		// binds for classes
		java2xsdBinds.put(java.lang.Boolean.class, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#boolean>");
		java2xsdBinds.put(java.lang.Byte.class, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#byte>");
		java2xsdBinds.put(java.lang.Character.class, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#string>");
		java2xsdBinds.put(java.util.Date.class, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#dateTime>");
		java2xsdBinds.put(java.lang.Double.class, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#double>");
		java2xsdBinds.put(java.lang.Float.class, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#float>");
		java2xsdBinds.put(java.lang.Integer.class, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#int>");
		java2xsdBinds.put(java.lang.Long.class, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#long>");
		java2xsdBinds.put(java.lang.Short.class, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#short>");
		java2xsdBinds.put(java.lang.String.class, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#string>");
		java2xsdBinds.put(java.lang.StringBuffer.class, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#string>");
		java2xsdBinds.put(java.lang.StringBuilder.class, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#string>");
		java2xsdBinds.put(java.math.BigDecimal.class, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#decimal>");
		java2xsdBinds.put(java.math.BigInteger.class, "\"%s\"^^<http://www.w3.org/2001/XMLSchema#integer>");
	}
	
	protected static void _initXsd2javaDatatypes() {
		xsd2javaDatatypes = new HashMap<String,Class>();
		// binds for primitives
		xsd2javaDatatypes.put("boolean",java.lang.Boolean.class);
		xsd2javaDatatypes.put("byte", java.lang.Byte.class);
		//xsd2javaDatatypes.put("????", java.lang.Character.class);
		xsd2javaDatatypes.put("double", java.lang.Double.class);
		xsd2javaDatatypes.put("float", java.lang.Float.class);
		xsd2javaDatatypes.put("int", java.lang.Integer.class);
		xsd2javaDatatypes.put("long", java.lang.Long.class);
		xsd2javaDatatypes.put("short", java.lang.Short.class);
		// binds for classes
		xsd2javaDatatypes.put("dateTime", java.util.Date.class);
		xsd2javaDatatypes.put("datetime", java.util.Date.class);
		xsd2javaDatatypes.put("string", java.lang.String.class);
		xsd2javaDatatypes.put("decimal", java.math.BigDecimal.class);
		//xsd2javaDatatypes.put("integer", java.math.BigInteger.class);
		xsd2javaDatatypes.put("integer", Integer.class);			// Fuseki converts 'int' datatype to 'integer' during insert
	}
	
	// --------------------------------------------------------------------------
	// initialize from config files
	//
	protected static void _initDefaultsFromConfigFiles() throws IOException, ClassNotFoundException {
		String java2xsdTypeBindInitFile = RDF_CONFIG_BASE+"/java2xsdTypeBindings.properties";
		String xsd2javaTypeBindInitFile = RDF_CONFIG_BASE+"/xsd2javaTypeBindings.properties";
		String defaultUrisFile = RDF_CONFIG_BASE+"/defaultUris.properties";
		String rdfPersistenceFile = RDF_CONFIG_BASE+"/rdf-persistence.properties";
		logger.info("RdfPersistenceManager: Initializing from files:\ndefault URIs: {}\nJava-to-Xsd bindings: {}\nXsd-to-Java bindings: {}\nRdf Persistence: {}", defaultUrisFile, java2xsdTypeBindInitFile, xsd2javaTypeBindInitFile, rdfPersistenceFile);
		_initTypeBindingsFromFiles(java2xsdTypeBindInitFile, xsd2javaTypeBindInitFile);
		_initDefaultUris(defaultUrisFile);
		_initRdfPersistenceSettings(rdfPersistenceFile);
	}
	
	// initialize persistence settigns, default namespace, predicates (RDF properties) and URIs
	protected static void _initRdfPersistenceSettings(String settingsFile) throws IOException {
		Properties p = eu.brokeratcloud.util.Config.getConfig(settingsFile);
		
		ignoreUnknownProperties = _parseBoolean( p.getProperty(".ignore-unknown-properties") );
		
		nonRegisterableRdfClasses = new Vector<Pattern>();
		
		String exclRdfClasses = p.getProperty(".dont-register-rdf-classes");
		if (exclRdfClasses!=null) {
			for (String s : exclRdfClasses.split(",")) {
				s = s.trim();
				if (s.isEmpty()) continue;
				nonRegisterableRdfClasses.add( Pattern.compile(s) );
			}
		}
	}
	
	protected static void _initDefaultUris(String typeFile) throws IOException {
		Properties p = eu.brokeratcloud.util.Config.getConfig(typeFile);
		
		defaultNamespacePrefix = p.getProperty(".default-namespace-prefix");
		defaultTypesPrefix = p.getProperty(".default-type-prefix");
		nullUri = p.getProperty(".null-uri");
		nullUriInBrackets = "<"+nullUri+">";
		defaultTypeFieldUri = p.getProperty(".default-java-class-predicate");
		defaultTypeFieldUriInBrackets = "<"+defaultTypeFieldUri+">";
		baseUriServiceAttribute = p.getProperty(".SERVICE-ATTRIBUTE-BASE-URI");
		baseUriServiceCategories = p.getProperty(".SERVICE-CATEGORIES-BASE-URI");
		baseUriBrokerPolicy = p.getProperty(".BROKER-POLICY-BASE-URI");
	}
	
	protected static void _initTypeBindingsFromFiles(String java2xsdInitFile, String xsd2javaInitFile) throws IOException, ClassNotFoundException {
		// Initialize from java2xsdInitFile
		Properties p = eu.brokeratcloud.util.Config.getConfig(java2xsdInitFile);
		for (Object k : p.keySet()) {
			String key = ((String)k).trim();
			String value = p.getProperty(key).trim();
			Class type;
			//logger.debug("===> {} : {}", key, value);
			if (key.equals(".default")) {
				defaultJava2xsdBindType = Class.forName(value);
				continue;
			}
			else if (key.equals("boolean"))	type = Boolean.TYPE;
			else if (key.equals("byte"))	type = Byte.TYPE;
			else if (key.equals("char"))	type = Character.TYPE;
			else if (key.equals("double"))	type = Double.TYPE;
			else if (key.equals("float"))	type = Float.TYPE;
			else if (key.equals("int"))		type = Integer.TYPE;
			else if (key.equals("long"))	type = Long.TYPE;
			else if (key.equals("short"))	type = Short.TYPE;
			else type = Class.forName( key );
			
			java2xsdBinds.put(type, value);
		}
		
		// Initialize from xsd2javaInitFile
		p = eu.brokeratcloud.util.Config.getConfig(xsd2javaInitFile);
		for (Object k : p.keySet()) {
			String key = ((String)k).trim();
			String value = p.getProperty(key).trim();
			//logger.debug("===> {} : {}", key, value);
			if (key.equals(".default")) {
				defaultXsdType = value;
				continue;
			}
			Class type = Class.forName(value);
			
			xsd2javaDatatypes.put(key, type);
		}
	}
	
	// --------------------------------------------------------------------------
	// Preload and analyze classes
	//
	protected static void _preloadTypes(String preloadTypesFile) throws IOException, ClassNotFoundException {
		logger.debug("RdfPresistenceManager: Preloading and analyzing classes...");
		Properties p = eu.brokeratcloud.util.Config.getConfig(preloadTypesFile);
		HashMap<String,Class> types = new HashMap<String,Class>();
		
		// replace placeholders with actual values
		for (Object k : p.keySet()) {
			String val = p.getProperty(k.toString());
			val = val.replace("{{SERVICE-ATTRIBUTE-BASE-URI}}", baseUriServiceAttribute);
			val = val.replace("{{SERVICE-CATEGORIES-BASE-URI}}", baseUriServiceCategories);
			val = val.replace("{{BROKER-POLICY-BASE-URI}}", baseUriBrokerPolicy);
			p.setProperty(k.toString(), val);
		}
		
		// preload classes and class settings
		for (Object k : p.keySet()) {
			String key = (String)k;
			key = key.trim();
			String value = p.getProperty(key).trim();
 //logger.trace(">>>>   Key={}, Value={}", key, value);
			
			// look for class preload directives
			if (!key.startsWith("class.")) continue;
			String clssName = key.substring("class.".length());
			
			int dot = clssName.lastIndexOf(".");
			String setting = (dot+1<clssName.length()) ? clssName.substring(dot+1) : "";
			clssName = clssName.substring(0,dot);
 //logger.trace(">>>>   Class={}, Setting={}", clssName, setting);
			
			// preload and analyze class
			Class type = types.get(clssName);
 //logger.trace(">>>>   Preloaded Type={}", type);
			if (type==null) {
				logger.debug("RdfPresistenceManager: \tPreloading and analyzing class: {}", clssName);
				type = Class.forName(clssName);
				analyzeObjectType(type);
				PersistentClass pc = _analyzedTypes.get(type);
				pc.idFormatter = null;		// nullify formatter and pattern to request persistent field processing with id-formatter & id-pattern preparation,
				pc.idPattern = null;		// if they (id-formatter & id-pattern) are not provided in class preloading settings
				types.put(clssName, type);
			}
 //logger.trace(">>>>   AFTER: Preloaded Type={}", type);
			
			// override 'typeUri' with the value provided
			if (!value.isEmpty()) {
				logger.debug("RdfPresistenceManager: \tOverriding class metadata: class={}, {}={}", type, setting, value);
				PersistentClass pc = _analyzedTypes.get(type);
 //logger.trace(">>>>   PC={}", pc);
				if (pc!=null) {
					if (setting.equals("")) ;
					else if (setting.equalsIgnoreCase("uri")) pc.typeUri = value;
					else if (setting.equalsIgnoreCase("rdf-type")) pc.rdfType = value;
					else if (setting.equalsIgnoreCase("name")) pc.name = value;
					else if (setting.equalsIgnoreCase("append-name")) pc.appendName = _parseBoolean(value);
					else if (setting.equalsIgnoreCase("suppress-rdf-type")) pc.suppressRdfType = _parseBoolean(value);
					else if (setting.equalsIgnoreCase("suppress-java-type")) pc.suppressJavaType = _parseBoolean(value);
					else if (setting.equalsIgnoreCase("register-with-rdf-class")) { pc.registerWithRdfType = value; registerTypeWithRdfClass(pc, pc.registerWithRdfType); }
					else if (setting.equalsIgnoreCase("id-formatter")) pc.idFormatter = value;
					else if (setting.equalsIgnoreCase("id-pattern")) pc.idPattern = Pattern.compile(value);
					else logger.debug("RdfPresistenceManager: \tUnknown setting while preloading: {}", setting);
 //logger.trace(">>>>   AFTER PC={}", pc);
				}
 //else logger.debug(">>>>   PC is NULL: class={}", type);
			}
		}
		
		// for all preloaded type re-process persistent fields
		for (Class type : _analyzedTypes.keySet()) {
			PersistentClass pc = _analyzedTypes.get(type);
			boolean suppress = !(pc.idFormatter==null || pc.idPattern==null);
			// re-process persistent fields
			pc.initializePersistentFields(suppress);		// suppress/dont' suppress id-formatter and id-pattern preparation
		}
		
		// override 'fieldUri's with provided values
		for (Object k : p.keySet()) {
			String key = (String)k;
			key = key.trim();
			String value = p.getProperty(key).trim();
			
			// look for field override directives
			if (!key.startsWith("field.")) continue;
			key = key.substring("field.".length());
			int dot = key.lastIndexOf('.');
			String clssName = key.substring(0,dot);
			String fldName = key.substring(dot+1);
			
			// preload and analyze class
			logger.debug("RdfPresistenceManager: \tLooking for field: {}.{}", clssName, fldName);
			Class type = types.get(clssName);
			
			// override 'fieldUri' with the value provided
			if (type!=null && !value.isEmpty()) {
				for (PersistentField pf : _analyzedTypes.get(type).fields) {
					if (pf.name.equals(value)) {
						logger.debug("RdfPresistenceManager: \tOverriding field metadata: {}.{} = {}", clssName, fldName, value);
						pf.fieldUri = value;
					}
				}
			}
		}
		
		logger.debug("RdfPresistenceManager: Preloading and analyzing classes... completed");
		logger.trace("RdfPresistenceManager: Preloading and analyzing classes: results=\n{}", _analyzedTypes);
	}
	
	protected static boolean _parseBoolean(String s) {
		s = s.trim().toLowerCase();
		boolean ret = (s.equals("true") || s.equals("yes") || s.equals("on") || s.equals("1"));
		boolean ret2 = (s.equals("false") || s.equals("no") || s.equals("off") || s.equals("0") || s.equals("-1"));
		if (!(ret | ret2)) throw new IllegalArgumentException("_parseBoolean: Argument is not a boolean value: "+s);
		return ret;
	}
} // End of RdfPersistenceManagerImpl

// =============================================================================================================================


class PersistentObject {
	Object object;
	String objectUri;
	Map<String,String> data;	// persistent state data
	boolean isPersisted;
	
	public PersistentObject(Object o, Map<String,String> data) throws IllegalAccessException, InvocationTargetException {
		this.object = o;
		this.objectUri = RdfPersistenceManagerImpl._getObjectUri(o);
		this.data = data;
		this.isPersisted = (data!=null && data.size()>0);
	}
	
	public PersistentObject(String oUri, Map<String,String> data) throws IllegalAccessException, InvocationTargetException {
		this.object = null;
		this.objectUri = oUri;
		this.data = data;
		this.isPersisted = (data!=null && data.size()>0);
	}
	
	public String getFieldXsdValue(PersistentField pf) {
		if (!isPersisted) return null;
		if (!data.containsKey(pf.fieldUri)) // and not 'ignorable'
			throw new RdfPersistenceException( String.format("Missing field from objects persistent state: field name=%s, field uri=%s, object uri=%s", pf.name, pf.fieldUri, objectUri) );
		return data.get( pf.fieldUri );
	}
	
	public String getFieldXsdValue(String fieldUri) {
		if (!isPersisted) return null;
		if (!data.containsKey(fieldUri)) // and not 'ignorable'
			throw new RdfPersistenceException( String.format("Missing field from objects persistent state: field uri=%s, object uri=%s", fieldUri, objectUri) );
		return data.get( fieldUri );
	}
} // End of PersistentObject

class PersistentClass {
	private static final Logger logger = LoggerFactory.getLogger( (new Object() { }.getClass().getEnclosingClass()).getName() );
	
	Class type;
	String name;
	String typeUri;				// Used to build object instance URIs
	String rdfType;				// Used in generateObjectHeaders and findAll
	boolean appendName;
	boolean suppressRdfType;
	boolean suppressJavaType;
	String registerWithRdfType;
	PersistentField idField;
	String idFormatter;
	Pattern idPattern;
	HashSet<PersistentField> fields;
	
	public PersistentClass(Class c) throws UnsupportedEncodingException, ClassNotFoundException {
		// traverseObjectGraph should take care to avoid pass array, Map or Collection objects to this constructor
		if (c.isArray() || Map.class.isAssignableFrom(c) || Collection.class.isAssignableFrom(c)) {
			throw new RdfPersistenceException("Cannot analyze 'arrays' or objects of 'Map' or 'Collection' type");
		}
		
		// Check if class is persistable
		RdfSubject ann = (RdfSubject)c.getAnnotation(RdfSubject.class);
		if (ann==null) throw new RdfPersistenceException("Object type is not persistable: "+c);
		
		// Initialize field information
		this.type = c;
		this.name = ann.name().trim();
		if (this.name.isEmpty()) this.name = c.getName();
		this.appendName = ann.appendName();
		this.typeUri = ann.uri().trim();
		if (this.typeUri.isEmpty()) {
			String ns = ann.namespace().trim();
			if (ns.isEmpty()) ns = RdfPersistenceManagerImpl.defaultTypesPrefix;
			if (appendName) this.typeUri = ((ns.endsWith("/") || ns.endsWith("#")) ? ns : ns+"/" ) + URLEncoder.encode(this.name,"UTF-8");
			else this.typeUri = ns;
		}
		
		// Initialize rdf type (if specified)
		this.rdfType = null;
		if (ann.rdfType()!=null && !ann.rdfType().trim().isEmpty()) {
			this.rdfType = ann.rdfType().trim();
		}
		logger.trace("PersistentClass.<init>: rdf-type={}", rdfType);
		
		// Initialize suppress rdf & java type flags
		this.suppressRdfType = ann.suppressRdfType();
		this.suppressJavaType = ann.suppressJavaType();
		
		// Register this class with specified RDF type (if specified)
		this.registerWithRdfType = null;
		if (ann.registerWithRdfType()!=null && !ann.registerWithRdfType().trim().isEmpty()) {
			this.registerWithRdfType = ann.registerWithRdfType().trim();
			RdfPersistenceManagerImpl.registerTypeWithRdfClass(this, this.registerWithRdfType);
		}
		
		// Process class fields. Ignores non-persistable fields
		initializePersistentFields(false);		// don't suppress id-formatter and id-pattern preparation
	}
	
	protected void initializePersistentFields(boolean suppressIdFormatterAndPattern) throws UnsupportedEncodingException {
		// Process class fields. Ignores non-persistable fields
		this.idField = null;
		this.fields = new HashSet<PersistentField>();
		for (Field f : _getInheritedFields(this.type)) {
			// Process fields and create descriptor
			logger.trace("PersistentClass.<init>: {}: processing field: {}", this.type.getName(), f.getName());
			PersistentField pf = new PersistentField(f, this.typeUri);
			if (!pf.persistable) continue;		// ignore non-persistable fields
			
			// Check if 'id' field
			if (pf.isId && idField==null) {	// first occurence. Ok
				idField = pf;
				if (!suppressIdFormatterAndPattern) {
					// prepare id-formatter
					String sep1 = (typeUri.endsWith("#")) ? "" : "#";
					Id idAnn = pf.field.getAnnotation(Id.class);
					this.idFormatter = idAnn.formatter();
					if (idFormatter==null || idFormatter.trim().isEmpty()) {
						idFormatter = typeUri+sep1+"%s";
					} else {
						idFormatter = idFormatter.replaceAll("%URI%", typeUri).replaceAll("%HASH%", sep1);
					}
					// prepare id-pattern
					String sep2 = (typeUri.endsWith("#")) ? "" : "\\#";
					String patStr = idAnn.pattern();
					if (patStr==null || patStr.trim().isEmpty()) {
						patStr = "^"+typeUri.replace("\\","\\\\")+sep2+"([^\\#]+)$";
					} else {
						patStr = patStr.replaceAll("%URI%", typeUri).replaceAll("%HASH%", sep2);
					}
					this.idPattern = Pattern.compile(patStr);
				}
			} else
			if (pf.isId && idField!=null) {	// second occurence. Error!
				throw new RdfPersistenceException("Object type contains multiple fields annotated as '@Id'. Only one 'if' field is allowed: class="+type.getName()+", id-fields="+idField.field.getName()+","+pf.field.getName());
			}
			
			// store persistent field
			fields.add(pf);
		}
		if (idField==null) {	// check that one field is annotated as 'id'
			throw new RdfPersistenceException("Object type does not have 'id' field annotated with '@Id': class="+type.getName());
		}
	}
	
	public Set<Field> _getInheritedFields(Class startClass) {
		HashSet<Field> set = new HashSet<Field>();
		Class clss = startClass;
		while (clss!=null) {
			for (Field f : clss.getDeclaredFields()) {
				set.add(f);
			}
			clss = clss.getSuperclass();
		}
		return set;
	}
	
	public PersistentField getFieldFromUri(String fUri, String lang) {
		logger.trace("getFieldFromUri: BEGIN: Looking field descriptor for field uri={}", fUri);
		PersistentField result = null;
		try {
			for (PersistentField pf : fields) {
				result = pf;
				logger.trace("getFieldFromUri: ITERATION: checking with field descriptor: {}", pf.fieldUri);
				logger.trace("getFieldFromUri: \tis-array={}", pf.isArray);
				if (pf.isArray && fUri.startsWith(pf.fieldUri+RdfPersistenceManagerImpl.asep)) return pf;
				logger.trace("getFieldFromUri: \tis-collection={}", pf.isCollection);
				if (pf.isCollection && fUri.startsWith(pf.fieldUri+RdfPersistenceManagerImpl.csep)) return pf;
				logger.trace("getFieldFromUri: \tis-map={}", pf.isMap);
				//logger.trace("getFieldFromUri: \tIT IS MAP: fUri={}, pf.field-uri={}, msep={}", fUri, pf.fieldUri, RdfPersistenceManagerImpl.msep);
				if (pf.isMap && fUri.startsWith(pf.fieldUri+RdfPersistenceManagerImpl.msep)) return pf;
				
				boolean langMatch = (lang==null || (lang=lang.trim()).isEmpty()) ?
										( true ) : 
										( pf.lang.isEmpty() ? "en".equalsIgnoreCase(lang) : pf.lang.equalsIgnoreCase(lang) );
				logger.trace("getFieldFromUri: arg-lang={}, pf-lang={}, match={}", lang, pf.lang, langMatch);
				if (logger.isTraceEnabled()) logger.trace("getFieldFromUri: \tchecking for exact match: {}", fUri.equals(pf.fieldUri) && langMatch);
				if (fUri.equals(pf.fieldUri) && langMatch) return pf;
				logger.trace("getFieldFromUri: \tNo match");
				result = null;
			}
		} finally {
			if (result==null) logger.trace("getFieldFromUri: END: result={}", result);
		}
		logger.trace("getFieldFromUri: END: field value NOT FOUND: field-uri={}", fUri);
		return null;
	}
	
	public PersistentField getFieldByName(String fieldName) {
		logger.trace("getFieldByName: BEGIN: Looking field descriptor for field uri={}", fieldName);
		PersistentField result = null;
		try {
			for (PersistentField pf : fields) {
				result = pf;
				logger.trace("getFieldByName: ITERATION: checking with field descriptor: {}", pf.name);
				/*logger.trace("getFieldByName: \tis-array={}", pf.isArray);
				if (pf.isArray && fUri.startsWith(pf.name+RdfPersistenceManagerImpl.asep)) return pf;
				logger.trace("getFieldByName: \tis-collection={}", pf.isCollection);
				if (pf.isCollection && fUri.startsWith(pf.fieldUri+RdfPersistenceManagerImpl.csep)) return pf;
				logger.trace("getFieldByName: \tis-map={}", pf.isMap);
				//logger.trace("getFieldByName: \tIT IS MAP: fUri={}, pf.field-uri={}, msep={}", fUri, pf.fieldUri, RdfPersistenceManagerImpl.msep);
				if (pf.isMap && fUri.startsWith(pf.fieldUri+RdfPersistenceManagerImpl.msep)) return pf;*/
				if (logger.isTraceEnabled()) logger.trace("getFieldByName: \tchecking for exact match: {}", fieldName.equals(pf.name));
				if (fieldName.equals(pf.name)) return pf;
				logger.trace("getFieldByName: \tNo match");
				result = null;
			}
		} finally {
			if (result==null) logger.trace("getFieldByName: END: result={}", result);
		}
		logger.trace("getFieldByName: END: field value NOT FOUND: field-uri={}", fieldName);
		return null;
	}
	
	public String toString() {
		return "PersistentClass: type="+type+", name="+name+", type-uri="+typeUri+", rdf-type="+rdfType;
	}
} // End of PersistentClass

class PersistentField {
	Field field;
	Class type;
	String name;
	String fieldUri;
	boolean appendName;
	boolean persistable;
	boolean isId;
	boolean hasGetter;
	boolean hasSetter;
	boolean isPublic;
	Method getter;
	Method setter;
	boolean isArray;
	boolean isMap;
	boolean isCollection;
	boolean isSingleValue;
	int arrayDimensions;
	Class arrayComponentType;
	boolean cascadeRefresh;
	boolean cascadeUpdate;
	boolean cascadeDelete;
	boolean isLiteral;
	boolean isReference;
	boolean isUri;
	boolean dontSerialize;
	String lang;
	boolean omitIfNull;
	
	public PersistentField(Field f, String typeUri) throws UnsupportedEncodingException {
		// Check if field is persistable
		RdfPredicate ann = (RdfPredicate)f.getAnnotation(RdfPredicate.class);
		persistable = (ann!=null);
		if (!persistable) return;		// Don't throw exception. PersistentClass will omit it from further processing
		
		// Initialize field information
		this.field = f;
		this.type = f.getType();
		this.name = ann.name().trim();
		if (this.name.isEmpty()) this.name = f.getName();
		this.appendName = ann.appendName();
		this.fieldUri = ann.uri().trim();
		if (this.fieldUri.isEmpty()) {
			String ns = ann.namespace().trim();
			if (ns.isEmpty()) ns = typeUri.trim();
			if (appendName) this.fieldUri = ((ns.endsWith("/") || ns.endsWith("#")) ? ns : ns+"/" ) + URLEncoder.encode(this.name,"UTF-8");
			else this.fieldUri = ns;
		}
		String capName=this.name.length()>1 ? ( Character.toUpperCase(this.name.charAt(0)) + this.name.substring(1) ) : this.name.toUpperCase();
		
		// Check if field is array, Collection or Map
		isArray = type.isArray();
		isMap = Map.class.isAssignableFrom(type);
		isCollection = Collection.class.isAssignableFrom(type);
		isSingleValue = !(isArray || isMap || isCollection);
		
		if (isArray) {
			// get array dimension and component type
			Class typ = type;
			Class compType = null;
			int dim = 0;
			while ((typ=typ.getComponentType())!=null) { compType = typ; dim++; }
			arrayComponentType = compType;
			arrayDimensions = dim;
		} else
		if (isMap) {
			//throw new RdfPersistenceException("PersistentField: CURRENTLY NOT SUPPORTS MAP FIELDS");
		} else
		if (isCollection) {
			//throw new RdfPersistenceException("PersistentField: CURRENTLY NOT SUPPORTS COLLECTION FIELDS");
		} else
		{	// Not an array, Map or Collection
			
			// Check if field is 'Id'
			Id ann2 = (Id)f.getAnnotation(Id.class);
			isId = (ann2!=null);
		}
		
		// Find getter and setter methods, if available
		String getterName = ann.getter().trim();
		String setterName = ann.setter().trim();
		if (getterName.isEmpty()) getterName = "get"+capName;
		if (setterName.isEmpty()) setterName = "set"+capName;
		
		Class declClass = f.getDeclaringClass();
		try { getter = declClass.getMethod(getterName); } 
		catch (NoSuchMethodException e) {
			if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {	// for boolean fields try again for an 'is___' getter
				getterName = "is"+capName;
				try { getter = declClass.getMethod(getterName); } catch (NoSuchMethodException e2) { }
			}
		}
		try { setter = declClass.getMethod(setterName, this.type); } catch (NoSuchMethodException e) { }
		hasGetter = (getter!=null);
		hasSetter = (setter!=null);
		
		// Check if public field
		isPublic = (f.getModifiers() & Modifier.PUBLIC)!=0;
		
		// Check that field can be accessed and modified
		if (!(hasGetter && hasSetter || isPublic)) {
			throw new RdfPersistenceException("Persistent field does not have getter/setter methods and is not public: Class="+declClass.getName()+", field: "+name);
		}
		
		// Get cascade flags
		cascadeRefresh = ann.refresh().trim().equalsIgnoreCase("cascade");
		cascadeUpdate  = ann.update().trim().equalsIgnoreCase("cascade");
		cascadeDelete  = ann.delete().trim().equalsIgnoreCase("cascade");
		
		// Check if it is a Literal value or a Reference field
		this.isLiteral = RdfPersistenceManagerImpl.isLiteralType(this.type);
		this.isReference = ! this.isLiteral;
		
		// Check if it is a URI
		this.isUri = ann.isUri();
		// Check dontSerialize flag
		this.dontSerialize = ann.dontSerialize();
		// Check omitIfNull flag
		this.omitIfNull = ann.omitIfNull();
		
		// Get lang
		this.lang = ann.lang();
	}
	
	public String toString() {
		return "PersistentField: type="+type+", name="+name+", type-uri="+fieldUri;
	}
} // End of PersistentField