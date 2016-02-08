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
package eu.brokeratcloud.common;

import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	name="categories",
	rdfType="http://www.w3.org/2004/02/skos/core#ConceptScheme"
)
public class ClassificationDimensionScheme extends ClassificationDimension {
	@RdfPredicate(isUri=true,uri="http://www.w3.org/2004/02/skos/core#hasTopConcept", update="no-cascade", delete="no-cascade", omitIfNull=true)
	protected String hasTopConcept;
	
	public String getHasTopConcept() { return hasTopConcept; }
	public void setHasTopConcept(String s) { hasTopConcept = s; }
	
	//Override ClassificationDimension methods
	public ClassificationDimension getParent() { return null; }
	public void setParent(ClassificationDimension p) { }
	public ClassificationDimensionScheme getTopConceptOf() { return null; }
	public void setTopConceptOf(ClassificationDimensionScheme p) { }
	public ClassificationDimensionScheme getInScheme() { return null; }
	public void setInScheme(ClassificationDimensionScheme p) { }
	
	
	public String toString() {
		String tmp = super.toString();
		int p = tmp.indexOf(":");
		return tmp.substring(0, p).replace("ClassificationDimension", "ClassificationDimensionScheme") + tmp.substring(p);
	}
}
