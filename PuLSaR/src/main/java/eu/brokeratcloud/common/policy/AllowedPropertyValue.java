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
package eu.brokeratcloud.common.policy;

import eu.brokeratcloud.common.RootObject;
import eu.brokeratcloud.persistence.annotations.*;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@RdfSubject(
	appendName=false,
	suppressRdfType=true
)
@JsonIgnoreProperties(ignoreUnknown=true)	// Ignore AllowedQuantitativePropertyValue specific fields, during JSON serialization/unserialization
public class AllowedPropertyValue extends RootObject {
	@Id
	@RdfPredicate(dontSerialize=true)
	protected String id;
	@RdfPredicate(isUri=true, uri="http://www.w3.org/2000/01/rdf-schema#subClassOf", omitIfNull=true)
	protected String subClassOf;	// Read-Only
	@RdfPredicate(isUri=true, omitIfNull=true)	// Use object's uri (not a fixed one in the USDL-CORE-CB model)
	protected String measuredBy;
	
	public String getId() { return id; }
	public void setId(String s) { id = s; }
	public String getSubClassOf() { return subClassOf; }
	public void setSubClassOf(String s) { /* It is a Read-Only field. MUST BE SET in subclasses' constructors */ }
	public boolean isMandatory() { return false; /*mandatory;*/ }
	public void setMandatory(boolean b) { /*mandatory = b;*/ }
	public String getMeasuredBy() { return measuredBy; }
	public void setMeasuredBy(String s) { measuredBy = s; }
	
	public String toString() {
		return String.format("id=%s, sub-class-of=%s, measured-by=%s, %s", id, subClassOf, measuredBy, super.toString());
	}
}
