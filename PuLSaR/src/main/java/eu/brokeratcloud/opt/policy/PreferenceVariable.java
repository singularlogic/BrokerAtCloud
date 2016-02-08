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
package eu.brokeratcloud.opt.policy;

import java.util.*;

import eu.brokeratcloud.common.RootObject;
import eu.brokeratcloud.common.ClassificationDimension;
import eu.brokeratcloud.common.BrokerObject;
import eu.brokeratcloud.common.policy.*;
import eu.brokeratcloud.opt.OptimisationAttribute;
import eu.brokeratcloud.persistence.annotations.*;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@RdfSubject(
	uri = "http://www.linked-usdl.org/ns/usdl-pref#",
	rdfType = "http://www.w3.org/2000/01/rdf-schema#Class, http://www.w3.org/2002/07/owl#Class",
	appendName=false
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreferenceVariable extends RootObject {
	@Id
	@RdfPredicate(dontSerialize=true)
	protected String id;
	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-pref#refToServiceAttribute")
	protected OptimisationAttribute refToServiceAttribute;
	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-pref#belongsTo")
	protected ClassificationDimension belongsTo;
	@RdfPredicate(isUri=true, uri="http://www.w3.org/2000/01/rdf-schema#subClassOf", omitIfNull=true)
	protected String subClassOf;
	
	public String getId() { return id; }
	public void setId(String s) { id = s; }
	public OptimisationAttribute getRefToServiceAttribute() { return refToServiceAttribute; }
	public void setRefToServiceAttribute(OptimisationAttribute oa) { refToServiceAttribute = oa; }
	public ClassificationDimension getBelongsTo() { return belongsTo; }
	public void setBelongsTo(ClassificationDimension cc) { belongsTo = cc; }
	public String getSubClassOf() { return subClassOf; }
	public void setSubClassOf(String s) { subClassOf = s; }

	public String toString() {
		String oaStr = refToServiceAttribute!=null ? refToServiceAttribute.getId() : "null";
		String ccStr = belongsTo!=null ? belongsTo.getId() : "null";
		return 	getClass().getSimpleName()+": {\n"+super.toString()+
				"\n\tid="+id+
				"\n\tservice-attr="+oaStr+
				"\n\tclassif.dim.="+ccStr+
				"\n\tsubClassOf="+subClassOf+
				"\n}\n";
	}
}
