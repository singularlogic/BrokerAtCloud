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

import eu.brokeratcloud.persistence.annotations.RdfPredicate;
import javax.xml.bind.annotation.XmlAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RootObject {
	protected final static Logger logger;
	static {
		logger = LoggerFactory.getLogger("eu.brokeratcloud");
		// debugging
		if (logger.isDebugEnabled() || logger.isTraceEnabled())
			logger.debug("RootObject.<static>: Debug messages are 'on'");
	}
	
	@XmlAttribute
	@RdfPredicate(lang="en", uri="http://www.w3.org/2000/01/rdf-schema#label", omitIfNull=true)
	protected String labelEn;
	@XmlAttribute
	@RdfPredicate(lang="de", uri="http://www.w3.org/2000/01/rdf-schema#label", omitIfNull=true)
	protected String labelDe;
	@XmlAttribute
	@RdfPredicate(uri="http://www.w3.org/2000/01/rdf-schema#comment", omitIfNull=true)
	protected String comment;
	
	public String getLabel() { return labelEn; }
	public void setLabel(String s) { labelEn = s; }
	public String getLabelEn() { return labelEn; }
	public void setLabelEn(String s) { labelEn = s; }
	public String getLabelDe() { return labelDe; }
	public void setLabelDe(String s) { labelDe = s; }
	public String getComment() { return comment; }
	public void setComment(String s) { comment = s; }
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (labelEn!=null) sb.append("label@en=").append(labelEn);
		if (labelDe!=null) {
			if (sb.length()>0) sb.append(", ");
			sb.append("label@de=").append(labelDe);
		}
		if (comment!=null) {
			if (sb.length()>0) sb.append(", ");
			sb.append("comment=").append(comment);
		}
		if (sb.length()>0) sb.insert(0, "RootObject: ");
		return sb.toString();
	}
}
