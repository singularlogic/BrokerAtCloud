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
	uri="http://www.brokeratcloud.eu/v1/opt/RECOMMENDATION-ITEM"
)
public class RecommendationItem extends BrokerObject {
	@XmlAttribute
	@RdfPredicate
	protected String suggestion;
	@XmlAttribute
	@RdfPredicate
	protected String serviceDescription;
	@XmlAttribute
	@RdfPredicate
	protected double weight;
	@XmlAttribute
	@RdfPredicate
	protected String response;
	//
	protected Object extra;
	
	public RecommendationItem() { response = "UNKNOWN"; }
	
	public String getSuggestion() { return suggestion; }
	public void setSuggestion(String s) { suggestion = s; }
	public String getServiceDescription() { return serviceDescription; }
	public void setServiceDescription(String s) { serviceDescription = s; }
	public double getWeight() { return weight; }
	public void setWeight(double w) { weight = w; }
	public String getResponse() { return response; }
	public void setResponse(String s) {
		String s0=s.trim().toUpperCase();
		if (!s0.equals("ACCEPT") && !s0.equals("REJECT") && !s0.equals("UNKNOWN")) throw new IllegalArgumentException("Invalid Recommendation Item response: "+s);
		response = s;
	}
	public Object getExtra() { return extra; }
	public void setExtra(Object o) { extra = o; }
	
	public String toString() {
		return 	"RecommendationItem: {\n"+super.toString()+
				"\tsuggestion = "+suggestion+"\n\tservice descr. = "+serviceDescription+"\n\tweight = "+weight+"\n\tresponse = "+response+"\n\textra = "+extra+"\n}\n";
	}
}
