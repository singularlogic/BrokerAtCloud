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
	uri="http://www.brokeratcloud.eu/v1/opt/FUZZY-INTERVAL",
	rdfType="http://www.linked-usdl.org/ns/usdl-pref#FuzzyIntervalVariable"
)
public class TFuzzyInterval {
	@XmlAttribute
	@RdfPredicate
	@Id
	protected String id;		// Not actually needed, but an 'id' is required from persistence framework
	
	@XmlAttribute
	@RdfPredicate
	protected double lowerBound;
	@XmlAttribute
	@RdfPredicate
	protected double minCoreValue;
	@XmlAttribute
	@RdfPredicate
	protected double maxCoreValue;
	@XmlAttribute
	@RdfPredicate
	protected double upperBound;
	
	protected TFuzzyInterval() { id = "FUZZY-INTERVAL-"+java.util.UUID.randomUUID(); }
	
	public TFuzzyInterval(double a, double b, double c, double d) {
		this();
		if (a>b || a>c || a>d || b>c || b>c || c>d) throw new IllegalArgumentException("Invalid fuzzy interval: a="+a+", b="+b+", c="+c+", d="+d);
		lowerBound = a; upperBound = d;
		minCoreValue = b; maxCoreValue = c;
	}
	
	public TFuzzyInterval(TFN lower, TFN upper) {
		this(lower.getLowerBound(), lower.getMeanValue(), upper.getMeanValue(), upper.getUpperBound());
	}
	
	public double getLowerBound() { return lowerBound; }
	public double getUpperBound() { return upperBound; }
	public double getMinCoreValue() { return minCoreValue; }
	public double getMaxCoreValue() { return maxCoreValue; }
	public TFN length() { return new TFN(maxCoreValue-minCoreValue, maxCoreValue-minCoreValue, upperBound-lowerBound); }
	
	public TFN getLowerFuzzyBound() { return new TFN(lowerBound, minCoreValue, minCoreValue); }
	public TFN getUpperFuzzyBound() { return new TFN(maxCoreValue, maxCoreValue, upperBound); }
	
	public TFuzzyInterval join(TFuzzyInterval i) {
		double m = Math.max(minCoreValue, i.getMinCoreValue());
		double M = Math.min(maxCoreValue, i.getMaxCoreValue());
		double l = Math.min(lowerBound, i.getLowerBound());
		double u = Math.max(upperBound, i.getUpperBound());
		if (l>m || l>M || l>u || m>M ||m>u || M>u) return null;
		return new TFuzzyInterval(l, m, M, u);
	}
	
	public static TFuzzyInterval valueOf(String s) {
		s=s.trim();
		if (s.startsWith("[") && s.endsWith("]")) s=s.substring(1,s.length()-1);
		else if (s.startsWith("(") && s.endsWith(")")) s=s.substring(1,s.length()-1);
		String[] p = s.split("[ ,\t;]+",4);
		try {
			if (p.length==3) {
				double mean;
				return new TFuzzyInterval( Double.parseDouble(p[0].trim()), mean=Double.parseDouble(p[1].trim()), mean, Double.parseDouble(p[2].trim()) );
			} else {
				return new TFuzzyInterval( Double.parseDouble(p[0].trim()), Double.parseDouble(p[1].trim()), Double.parseDouble(p[2].trim()), Double.parseDouble(p[3].trim()) );
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw (NumberFormatException)(new NumberFormatException("Invalid TFuzzyInterval format: "+s).initCause(e));
		}
	}
	
	public String toString() {
		return new StringBuffer("[").append(lowerBound).append(", ")
									.append(minCoreValue).append(", ")
									.append(maxCoreValue).append(", ")
									.append(upperBound).append("]").toString();
	}
}