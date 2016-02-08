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

import eu.brokeratcloud.common.RootObject;
import eu.brokeratcloud.common.policy.*;
import eu.brokeratcloud.persistence.annotations.*;
import javax.xml.bind.annotation.XmlAttribute;
import org.codehaus.jackson.annotate.JsonIgnore;

@RdfSubject(
	//uri = "http://www.linked-usdl.org/ns/usdl-pref#",
	appendName=false,
	registerWithRdfType="http://www.linked-usdl.org/ns/usdl-pref#hasDefaultDatatypeValue",
	suppressRdfType=true
)
public class DefaultDatatypePreferenceVariableValue extends RootObject {
	@Id
	@RdfPredicate(dontSerialize=true)
	protected String id;
	@RdfPredicate(isUri=true, uri="http://www.w3.org/2000/01/rdf-schema#subPropertyOf")
	protected String subPropertyOf = "http://www.linked-usdl.org/ns/usdl-pref#hasDefaultDatatypeValue";
	@RdfPredicate(uri="http://www.w3.org/2000/01/rdf-schema#domain", update="nocascade")
	protected PreferenceVariable domain;
	@RdfPredicate(isUri=true, uri="http://www.w3.org/2000/01/rdf-schema#range", omitIfNull=true)
	protected String range;
	
	public String getId() { return id; }
	public void setId(String s) { id = s; }
	public String getSubPropertyOf() { return subPropertyOf; }
	public void setSubPropertyOf(String s) { /*subPropertyOf = s;*/ }
	public PreferenceVariable getDomain() { return domain; }
	public void setDomain(PreferenceVariable pv) { domain = pv; }
	public String getRange() { return range; }
	public void setRange(String s) { range = s; }
	
	public String toString() {
		return getClass().getSimpleName()+": {"+
				"\n\t"+super.toString()+
				"\n\tid = "+id+
				"\n\tsubPropertyOf = "+subPropertyOf+
				"\n\tdomain = "+domain+
				"\n\trange = "+range+
				"}\n";
	}
}
