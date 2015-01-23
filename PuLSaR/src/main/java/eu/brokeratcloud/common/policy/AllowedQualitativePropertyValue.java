package eu.brokeratcloud.common.policy;

import eu.brokeratcloud.common.RootObject;
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
		hasOrder = (av!=null && av.length>1 && (av[0].getLesser()!=null || av[0].getGreater()!=null || av[av.length-1].getLesser()!=null || av[av.length-1].getGreater()!=null));
	}
	
	public String toString() {
		return String.format("AllowedQualitativePropertyValue: { %s, has-order=%b, values=%s }", super.toString(), hasOrder, java.util.Arrays.deepToString(allowedValues));
	}
}
