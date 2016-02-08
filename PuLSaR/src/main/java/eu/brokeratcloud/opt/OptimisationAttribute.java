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
package eu.brokeratcloud.opt;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import eu.brokeratcloud.common.BrokerObject;
import eu.brokeratcloud.persistence.annotations.*;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@RdfSubject(
	uri="http://www.brokeratcloud.eu/v1/opt/SERVICE-ATTRIBUTE",
	rdfType="http://www.linked-usdl.org/ns/usdl-pref#ServiceAttribute"
)
public class OptimisationAttribute extends BrokerObject {
	@XmlAttribute
	@RdfPredicate(uri="http://www.w3.org/2004/02/skos/core#broader", update="no-cascade", delete="no-cascade", omitIfNull=true)
	protected OptimisationAttribute parent;
	
	public OptimisationAttribute getParent() { return parent; }
	public void setParent(OptimisationAttribute p) { parent = p; }
	
	public String toString() {
		return 	"OptimisationAttribute: {\n"+super.toString()+
				"\tparent = "+(parent!=null ? parent.getId() : "")+
				"}\n";
	}
}
