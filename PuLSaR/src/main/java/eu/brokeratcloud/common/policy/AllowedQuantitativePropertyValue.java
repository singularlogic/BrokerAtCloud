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

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://purl.org/goodrelations/v1#QuantitativeValueFloat",
	suppressRdfType=true
)
public class AllowedQuantitativePropertyValue extends AllowedPropertyValue {
	@RdfPredicate(uri="http://purl.org/goodrelations/v1#hasUnitOfMeasurement")
	protected String unitOfMeasurement;
	@RdfPredicate(uri="http://purl.org/goodrelations/v1#hasMinValueFloat")
	protected double minValue;
	@RdfPredicate(uri="http://purl.org/goodrelations/v1#hasMaxValueFloat")
	protected double maxValue;
	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-core/cloud-broker#higherIsBetter")
	protected boolean higherIsBetter;
	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-core/cloud-broker#isRange")
	protected boolean range;
	
	public AllowedQuantitativePropertyValue() { subClassOf = "http://purl.org/goodrelations/v1#QuantitativeValueFloat"; }
	
	public String getUnitOfMeasurement() { return unitOfMeasurement; }
	public void setUnitOfMeasurement(String s) { unitOfMeasurement = s; }
	public double getMinValue() { return minValue; }
	public void setMinValue(double d) { minValue = d; }
	public double getMaxValue() { return maxValue; }
	public void setMaxValue(double d) { maxValue = d; }
	public boolean isHigherIsBetter() { return higherIsBetter; }
	public void setHigherIsBetter(boolean b) { higherIsBetter = b; }
	public boolean isRange() { return range; }
	public void setRange(boolean b) { range = b; }
	
	public String toString() {
		return String.format("AllowedQuantitativePropertyValue: { %s, min=%f, max=%f, unit=%s, higher-is-better=%b }", super.toString(), minValue, maxValue, unitOfMeasurement, higherIsBetter);
	}
}
