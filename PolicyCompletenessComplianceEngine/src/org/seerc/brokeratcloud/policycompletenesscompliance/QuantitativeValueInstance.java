package org.seerc.brokeratcloud.policycompletenesscompliance;

//Corresponds to an instance of either gr:QuantitativeValueInteger or gr:QuantitativeValueFloat subclass found in the BP 
public class QuantitativeValueInstance {
	private String uri;
	private Number minValue;
	private Number maxValue;
	private String unitOfMeasurement;
	
	public QuantitativeValueInstance(String uri) {
		super();
		this.uri = uri;
	}
	public QuantitativeValueInstance() {
		super();
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public Number getMinValue() {
		return minValue;
	}
	public void setMinValue(Number minValue) {
		this.minValue = minValue;
	}
	public Number getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(Number maxValue) {
		this.maxValue = maxValue;
	}
	public String getUnitOfMeasurement() {
		return unitOfMeasurement;
	}
	public void setUnitOfMeasurement(String unitOfMeasurement) {
		this.unitOfMeasurement = unitOfMeasurement;
	}
}
