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

import eu.brokeratcloud.opt.type.TFN;
import eu.brokeratcloud.persistence.annotations.*;

import org.codehaus.jackson.annotate.JsonIgnore;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://www.linked-usdl.org/ns/usdl-core/cloud-broker#FuzzyValue",
	suppressRdfType=true
)
public class AllowedFuzzyPropertyValue extends AllowedPropertyValue {
	@RdfPredicate(uri="http://purl.org/goodrelations/v1#hasUnitOfMeasurement")
	protected String unitOfMeasurement;
	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasMinSupport")
	protected double minSupport;
	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasMinKernel")
	protected double minKernel;
	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasMaxKernel")
	protected double maxKernel;
	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-core/cloud-broker#hasMaxSupport")
	protected double maxSupport;
	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-core/cloud-broker#higherIsBetter")
	protected boolean higherIsBetter;
	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-core/cloud-broker#isRange")
	protected boolean range;
	
	public AllowedFuzzyPropertyValue() { subClassOf = "http://www.linked-usdl.org/ns/usdl-core/cloud-broker#FuzzyValue"; }
	
	public String getUnitOfMeasurement() { return unitOfMeasurement; }
	public void setUnitOfMeasurement(String s) { unitOfMeasurement = s; }
	public double getMinSupport() { return minSupport; }
	public void setMinSupport(double d) { minSupport = d; }
	public double getMinKernel() { return minKernel; }
	public void setMinKernel(double d) { minKernel = d; }
	public double getMaxKernel() { return maxKernel; }
	public void setMaxKernel(double d) { maxKernel = d; }
	public double getMaxSupport() { return maxSupport; }
	public void setMaxSupport(double d) { maxSupport = d; }

	@JsonIgnore
	public TFN getFuzzyMinValue() { return new TFN(minSupport, minKernel, minKernel); }
	@JsonIgnore
	public void setFuzzyMinValue(TFN t) { minSupport = t.getLowerBound(); minKernel = t.getMeanValue(); }
	@JsonIgnore
	public TFN getFuzzyMaxValue() { return new TFN(maxKernel, maxKernel, maxSupport); }
	@JsonIgnore
	public void setFuzzyMaxValue(TFN t) { maxKernel = t.getMeanValue(); maxSupport = t.getUpperBound(); }
	
	public boolean isHigherIsBetter() { return higherIsBetter; }
	public void setHigherIsBetter(boolean b) { higherIsBetter = b; }
	public boolean isRange() { return range; }
	public void setRange(boolean b) { range = b; }
	
	public String toString() {
		return String.format("AllowedFuzzyPropertyValue: { %s, min=%f ; %f, max=%f ; %f, unit=%s, higher-is-better=%b }", super.toString(), minSupport, minKernel, maxKernel, maxSupport, unitOfMeasurement, higherIsBetter);
	}
}
