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
import eu.brokeratcloud.common.BrokerObject;
import eu.brokeratcloud.persistence.annotations.*;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@RdfSubject(
	uri="http://www.brokeratcloud.eu/v1/opt/RECOMMENDATION",
	rdfType="http://www.linked-usdl.org/ns/usdl-pref#Recommendation"
)
public class Recommendation extends BrokerObject {
	@XmlAttribute
	@RdfPredicate
	protected String profile;
	@XmlAttribute
	@RdfPredicate(delete="cascade")
	protected List<RecommendationItem> items;
	@XmlAttribute
	@RdfPredicate(getter="isActive")
	protected boolean active;
	
	public String getProfile() { return profile; }
	public void setProfile(String p) { profile = p; }
	public List<RecommendationItem> getItems() { return items; }
	public void setItems(List<RecommendationItem> s) { items = s; }
	public boolean isActive() { return active; }
	public void setActive(boolean b) { active = b; }
	
	public String toString() {
		String pStrL = null;
		if (items!=null) {
			StringBuffer sb = new StringBuffer("{\n");
			Iterator<RecommendationItem> it = items.iterator();
			while (it.hasNext()) { sb.append("\t\t"); sb.append(it.next()); sb.append("\n"); }
			sb.append("\t}\n"); pStrL = sb.toString();
		}
		return 	"Recommendation: {\n"+super.toString()+
				"\tprofile = "+profile+"\n\titems = "+pStrL+"\n\tactive = "+active+" }\n";
	}
}
