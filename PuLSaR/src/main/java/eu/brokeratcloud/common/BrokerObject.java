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

import java.util.Date;
import eu.brokeratcloud.persistence.annotations.*;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@RdfSubject
public abstract class BrokerObject extends RootObject {
	@XmlAttribute
	@Id
	@RdfPredicate(uri="http://purl.org/dc/terms/identifier")
	protected String id;
	@XmlAttribute
	@RdfPredicate(uri="http://purl.org/dc/terms/title", omitIfNull=true)
	protected String name;
	@XmlAttribute
	@RdfPredicate(uri="http://purl.org/dc/terms/description", omitIfNull=true)
	protected String description;
	@XmlAttribute
	@RdfPredicate(uri="http://purl.org/dc/terms/created", omitIfNull=true)
	protected Date createTimestamp;
	@XmlAttribute
	@RdfPredicate(uri="http://purl.org/dc/terms/modified", omitIfNull=true)
	protected Date lastUpdateTimestamp;
	@XmlAttribute
	@RdfPredicate(uri="http://purl.org/dc/terms/creator", omitIfNull=true)
	protected String owner;
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getName() { return name; }
	public void setName(String n) { name = n; }
	public String getDescription() { return description; }
	public void setDescription(String n) { description = n; }
	
	public Date getCreateTimestamp() { return createTimestamp; }
	public void setCreateTimestamp(Date dt) { createTimestamp = dt; }
	public Date getLastUpdateTimestamp() { return lastUpdateTimestamp; }
	public void setLastUpdateTimestamp(Date dt) { lastUpdateTimestamp = dt; }
	
	public String getOwner() { return owner; }
	public void setOwner(String c) { owner = c; }
	
	protected String createId() {
		String clss = getClass().getName();
		int p = clss.lastIndexOf('.');
		clss = (p!=-1) ? clss.substring(p+1) : clss;
		clss = clss.replaceAll("[^A-Za-z0-9_]","");
		clss = clss.replaceAll("([A-Z_])", "-$1");
		clss = clss.replaceAll("^-*", "").replaceAll("-*$","");
		clss = clss.toUpperCase();
		return createId(clss);
	}
	
	protected static String createId(String prefix) {
		return String.format("%s-%s", prefix, java.util.UUID.randomUUID().toString());
	}
	
	public String toString() {
		return 	"\tid = "+id+"\n\tname = "+name+"\n\tdescription = "+description+"\n\tcreation = "+createTimestamp+"\n\tlast-update = "+lastUpdateTimestamp+"\n\towner = "+owner+"\n";
	}
}
