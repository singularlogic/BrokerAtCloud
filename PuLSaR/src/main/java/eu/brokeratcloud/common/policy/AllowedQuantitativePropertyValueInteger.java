package eu.brokeratcloud.common.policy;

import eu.brokeratcloud.common.RootObject;
import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	appendName=false,
	registerWithRdfType="http://purl.org/goodrelations/v1#QuantitativeValueInteger",
	suppressRdfType=true
)
public class AllowedQuantitativePropertyValueInteger extends AllowedQuantitativePropertyValue {
	@RdfPredicate(uri="http://purl.org/goodrelations/v1#hasMinValueInteger")
	protected double minValue;
	@RdfPredicate(uri="http://purl.org/goodrelations/v1#hasMaxValueInteger")
	protected double maxValue;
	
	public AllowedQuantitativePropertyValueInteger() { subClassOf = "http://purl.org/goodrelations/v1#QuantitativeValueInteger"; }
	
	public double getMinValue() { return minValue; }
	public void setMinValue(double d) { minValue = d; }
	public double getMaxValue() { return maxValue; }
	public void setMaxValue(double d) { maxValue = d; }
	
	public String toString() {
		return String.format("AllowedQuantitativePropertyValueInteger: { %s, min=%f, max=%f, unit=%s, higher-is-better=%b }", super.toString(), minValue, maxValue, unitOfMeasurement, higherIsBetter);
	}
}
