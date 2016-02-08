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
	uri="http://www.brokeratcloud.eu/v1/common/SERVICE-CATEGORY",
	rdfType="http://www.linked-usdl.org/ns/usdl-pref#FunctionalServiceCategory"
)
public class ServiceCategory extends BrokerObject {
	@RdfPredicate(uri="http://www.w3.org/2004/02/skos/core#broader")
	protected ServiceCategory parent;
	
	public ServiceCategory getParent() { return parent; }
	public void setParent(ServiceCategory p) { parent = p; }
	
	public String toString() {
		return "ServiceCategory: {\n"+super.toString()+
				"\tparent = "+(parent!=null ? parent.getId() : "")+"}\n";
	}
}
