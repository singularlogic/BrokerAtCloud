package eu.brokeratcloud.persistence;

import java.util.List;

public interface RdfPersistenceManager {
	public void attach(Object o) throws Exception;
	public void detach(Object o) throws Exception;
	public void clear() throws Exception;
	
	public void persist(Object o) throws Exception;
	public void merge(Object o) throws Exception;
	public void refresh(Object o) throws Exception;
	public void remove(Object o) throws Exception;
	
	public List<Object> findAll(Class type) throws Exception;
	public List<Object> findByQuery(String queryStr) throws Exception;
	public List<Object> findByQuery(Class returnType, String queryStr) throws Exception;
	public Object find(Object id, Class type) throws Exception;
	public Object find(String oUri) throws Exception;
	public Object find(Class returnType, String oUri) throws Exception;
	
	public boolean exist(Object o) throws Exception;
	public boolean exist(Object id, Class type) throws Exception;
	public boolean exist(String oUri) throws Exception;
	public boolean exist(Class returnType, String oUri) throws Exception;
	
	public String getObjectUri(Object o) throws Exception;
	public String getObjectUri(Object idValue, Class clss) throws Exception;
	public String getClassRdfType(Class type);
	public String getFieldUri(Class type, String fieldName);
	
	public void insertRdfTriple(String s, String p, String o);
	public void removeRdfTriple(String s, String p, String o);
}
