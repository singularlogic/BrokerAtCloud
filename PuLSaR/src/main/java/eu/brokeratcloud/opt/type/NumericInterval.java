package eu.brokeratcloud.opt.type;

import eu.brokeratcloud.persistence.annotations.*;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import org.codehaus.jackson.annotate.JsonIgnore;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@RdfSubject(
	uri="http://www.brokeratcloud.eu/v1/opt/NUMERIC-INTERVAL",
	rdfType="http://www.linked-usdl.org/ns/usdl-pref#NumericIntervalVariable"
)
public class NumericInterval {
	@XmlAttribute
	@RdfPredicate
	@Id
	protected String id;		// Not actually needed, but an 'id' is required from persistence framework
	
	@XmlAttribute
	@RdfPredicate
	protected double lowerBound;
	@XmlAttribute
	@RdfPredicate
	protected double upperBound;
	
	protected NumericInterval() { id = "NUM-INTERVAL-"+java.util.UUID.randomUUID(); }
	
	public NumericInterval(double l, double u) {
		this();
		if (l>u) throw new IllegalArgumentException("Invalid numeric interval bounds: lower="+l+", upper="+u);
		lowerBound = l; upperBound = u;
	}
	
	public double getLowerBound() { return lowerBound; }
	public double getUpperBound() { return upperBound; }
	public double length() { return upperBound - lowerBound; }
	
	public NumericInterval join(NumericInterval i) {
		double m = Math.max(lowerBound, i.getLowerBound());
		double M = Math.min(upperBound, i.getUpperBound());
		if (m>M) return null;
		return new NumericInterval(m, M);
	}
	
	public static NumericInterval valueOf(String s) {
		s=s.trim();
		if (s.startsWith("[") && s.endsWith("]")) s=s.substring(1,s.length()-1);
		String[] p = s.split("[,-]",2);
		if (p.length==2) {
			return new NumericInterval( Double.parseDouble(p[0].trim()), Double.parseDouble(p[1].trim()) );
		} else {
			double l = Double.parseDouble(p[0].trim());
			return new NumericInterval(l, l);
		}
	}
	
	public String toString() {
		return new StringBuffer("[").append(lowerBound).append(", ")
									.append(upperBound).append("]").toString();
	}
}