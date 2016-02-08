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

import eu.brokeratcloud.common.BrokerObject;
import eu.brokeratcloud.persistence.annotations.*;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@RdfSubject(
	uri="http://www.brokeratcloud.eu/v1/opt/COMPARISON-PAIR"
)
public class ComparisonPair extends BrokerObject {
	@XmlAttribute
	@RdfPredicate
	protected String attribute1;
	@XmlAttribute
	@RdfPredicate
	protected String attribute2;
	@XmlAttribute
	@RdfPredicate
	protected String value;
	
	public ComparisonPair() { id = "CPAIR-"+java.util.UUID.randomUUID(); }
	
	public String getAttribute1() { return attribute1; }
	public void setAttribute1(String a) { attribute1 = a; }
	public String getAttribute2() { return attribute2; }
	public void setAttribute2(String a) { attribute2 = a; }
	public String getValue() { return value; }
	public void setValue(String v) { value = v; }
	
	public String toString() {
		return 	"ComparisonPair: {\n"+super.toString()+"\n\tattribute-1="+attribute1+"\n\tattribute-2="+attribute2+"\n\tvalue="+value+"\n}\n";
	}
}
