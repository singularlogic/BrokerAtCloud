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
package eu.brokeratcloud.common.policy;

import eu.brokeratcloud.persistence.annotations.*;

import org.codehaus.jackson.annotate.JsonIgnore;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://purl.org/goodrelations/v1#QualitativeValue",
	suppressRdfType=true
)
public class AllowedQualitativePropertyValue extends AllowedPropertyValue {
	// Don't serialize
	protected boolean hasOrder;
	// Don't serialize
	protected QualitativePropertyValue[] allowedValues;
	
	public AllowedQualitativePropertyValue() { subClassOf = "http://purl.org/goodrelations/v1#QualitativeValue"; }
	
	@JsonIgnore
	public boolean getHasOrder() { return hasOrder; }
	@JsonIgnore
	public void setHasOrder(boolean b) { hasOrder = b; }
	
	@JsonIgnore
	public QualitativePropertyValue[] getAllowedValues() {
		return allowedValues;
	}
	@JsonIgnore
	public synchronized String[] getAllowedValuesAsString() {
		if (allowedValues==null) return null;
		String[] av = new String[allowedValues.length];
		for (int i=0; i<av.length; i++) {
			if (allowedValues[i]==null) continue;
			av[i] = allowedValues[i].getValue();
		}
		return av;
	}
	@JsonIgnore
	public void setAllowedValues(QualitativePropertyValue[] av) {
		allowedValues = av;
		hasOrder = av!=null && av.length>1 && (av[0].getLesser()!=null || av[0].getGreater()!=null || av[av.length-1].getLesser()!=null || av[av.length-1].getGreater()!=null);
	}
	
	public String toString() {
		return String.format("AllowedQualitativePropertyValue: { %s, has-order=%b, values=%s }", super.toString(), hasOrder, java.util.Arrays.deepToString(allowedValues));
	}
}
