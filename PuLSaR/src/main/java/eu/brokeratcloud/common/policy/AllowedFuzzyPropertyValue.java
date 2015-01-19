package eu.brokeratcloud.common.policy;

import eu.brokeratcloud.common.RootObject;
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
