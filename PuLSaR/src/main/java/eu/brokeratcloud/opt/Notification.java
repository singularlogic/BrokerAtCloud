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

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@RdfSubject(
	uri="http://www.brokeratcloud.eu/v1/opt/NOTIFICATION"
)
public class Notification extends BrokerObject {
	@XmlAttribute
	@RdfPredicate
	protected String service;
	@XmlAttribute
	@RdfPredicate
	protected String message;
	@XmlAttribute
	protected String type;
	
	public Notification() {
		setId( createId("NOTIFICATION") );
	}
	
	public String getService() { return service; }
	public void setService(String s) { service = s; }
	public String getMessage() { return message; }
	public void setMessage(String s) { message = s; }
	public String getType() { return type; }
	public void setType(String s) { type = s; }
	
	public String toString() {
		return 	"Notification: {\n"+super.toString()+
				"\tservice = "+service+"\n\ttype = "+type+"\n\tmessage = "+message+"}\n";
	}
}
