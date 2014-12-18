package org.seerc.brokeratcloud.policycompletenesscompliance;

// Objects of this class are used in the complianceCheck method 
// They hold the values of the various instances of QV subclasses found in the SD
public class ValueObject {
	private String uri;
	private Number value;
	private Number minValue;
	private Number maxValue;
	private String unitOfMeasurement;
	

	public ValueObject(String uri, Number minValue, Number maxValue) {
		super();
		this.uri = uri;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	public ValueObject(String uri, Number value) {
		super();
		this.uri = uri;
		this.value = value;
	}
	public ValueObject() {
		super();
	}
	public Number getValue() {
		return value;
	}
	public void setValue(Number value) {
		this.value = value;
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
