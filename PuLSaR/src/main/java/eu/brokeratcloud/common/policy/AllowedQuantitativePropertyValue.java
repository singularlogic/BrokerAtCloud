package eu.brokeratcloud.common.policy;

import eu.brokeratcloud.common.RootObject;
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
