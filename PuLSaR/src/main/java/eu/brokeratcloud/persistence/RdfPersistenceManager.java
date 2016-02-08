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
