package eu.brokeratcloud.opt.type;

import eu.brokeratcloud.persistence.annotations.*;

import java.util.Locale;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import org.codehaus.jackson.annotate.JsonIgnore;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@RdfSubject(
	uri="http://www.brokeratcloud.eu/v1/opt/TRIANGULAR-FUZZY-NUMBER",
	rdfType="http://www.linked-usdl.org/ns/usdl-pref#FuzzyNumberVariable"
)
public class TFN {
	@XmlAttribute
	@RdfPredicate
	@Id
	protected String id;		// Not actually needed, but an 'id' is required from persistence framework
	
	@XmlAttribute
	@RdfPredicate
	protected double lowerBound;
	@XmlAttribute
	@RdfPredicate
	protected double meanValue;
	@XmlAttribute
	@RdfPredicate
	protected double upperBound;
//XXX: 2015-01-19: addition
	protected boolean crisp;
	
	public TFN() { id = "TFN-"+java.util.UUID.randomUUID(); }
	
	public TFN(double v) {
		this();
		lowerBound = meanValue = upperBound = v;
//XXX: 2015-01-19: addition
		_updateState();
	}
	public TFN(double l, double m, double u) {
		this();
		lowerBound = l; meanValue = m; upperBound = u;
//XXX: 2015-01-19: addition
		_updateState();
	}
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public double getLowerBound() { return lowerBound; }
	public void setLowerBound(double l) { this.lowerBound = l; /*XXX: 2015-01-19: addition */ _updateState(); }
	public double getMeanValue() { return meanValue; }
	public void setMeanValue(double m) { this.meanValue = m; /*XXX: 2015-01-19: addition */ _updateState(); }
	public double getUpperBound() { return upperBound; }
	public void setUpperBound(double u) { this.upperBound = u; /*XXX: 2015-01-19: addition */ _updateState(); }
	
//XXX: 2015-01-19: addition
	protected void _updateState() {
		crisp = lowerBound==meanValue && meanValue==upperBound;
	}
	
	protected int defaultPrecision = 3;
	
	public String toString() {
		return toString(defaultPrecision);
	}
	public String toString(int precision) {
		String fmtPattern;
		if (precision<=0) fmtPattern = "(%%f, %%f, %%f)";
		else fmtPattern = String.format("(%%%df, %%%df, %%%df)", precision, precision, precision);
		return 	String.format(Locale.ROOT, fmtPattern, lowerBound, meanValue, upperBound);
	}
	
	// Triangular Fuzzy Number operations
	
	public TFN add(TFN t) { return new TFN(this.lowerBound+t.getLowerBound(), this.meanValue+t.getMeanValue(), this.upperBound+t.getUpperBound()); }
	public TFN neg() { return new TFN(-this.upperBound, -this.meanValue, -this.lowerBound); }
	public TFN sub(TFN t) { return add(t.neg()); }
//XXX: 2015-01-19: commented out to introduce a new TFN operations implementations
/*	public TFN inv() { return new TFN(1/upperBound, 1/meanValue, 1/lowerBound); }
	public TFN mul(TFN t) { return new TFN(this.lowerBound * t.getLowerBound(), this.meanValue*t.getMeanValue(), this.upperBound * t.getUpperBound()); }
	public TFN div(TFN t) { return this.mul(t.inv()); }
*/
//XXX: 2015-01-19: new TFN operations implementations
	public TFN inv() {
		if (this.crisp) return new TFN(1/meanValue);
		else return new TFN(1/upperBound, 1/meanValue, 1/lowerBound);
	}
	public TFN mul(double k) {
		if (k>0) return new TFN(k * lowerBound, k * meanValue, k * upperBound);
		else return new TFN(k * upperBound, k * meanValue, k * lowerBound);
	}
	public TFN mul(TFN t) {
		if (this.crisp && t.isCrisp()) return new TFN(this.meanValue * t.getMeanValue());
		else if (this.crisp) return t.mul( this.meanValue );
		else if (t.isCrisp()) return mul( t.getMeanValue() );
		else {
			double p1 = this.lowerBound * t.getLowerBound();
			double p2 = this.lowerBound * t.getUpperBound();
			double p3 = this.upperBound * t.getLowerBound();
			double p4 = this.upperBound * t.getUpperBound();
			double lb = Math.min(p1, Math.min(p2, Math.min(p3, p4)));
			double ub = Math.max(p1, Math.max(p2, Math.max(p3, p4)));
			return new TFN(lb, this.meanValue*t.getMeanValue(), ub);
		}
	}
	public TFN div(TFN t) { return this.mul(t.inv()); }
	
	public double defuzzify() { return (lowerBound+4*meanValue+upperBound)/6; }
	public double defuzzify2() { return (lowerBound+2*meanValue+upperBound)/4; }
	
	@JsonIgnore
	public boolean isStrictPositive() { return lowerBound>0; }
	@JsonIgnore
	public boolean isStrictNegative() { return upperBound<0; }
	@JsonIgnore
	public boolean isStrictZero() { return lowerBound==0 && meanValue==0 && upperBound==0; }
	@JsonIgnore
	public boolean isPositive() { return defuzzify()>0; }
	@JsonIgnore
	public boolean isNegative() { return defuzzify()<0; }
	@JsonIgnore
	public boolean isZero() { return defuzzify()==0; }
//XXX: 2015-01-19: addition
	@JsonIgnore
	public boolean isCrisp() { return crisp; }
	
	public boolean eq(TFN t) { return defuzzify()==t.defuzzify(); }
	public boolean ne(TFN t) { return !eq(t); }
	public boolean lt(TFN t) { return defuzzify()<t.defuzzify(); }
	public boolean le(TFN t) { return defuzzify()<=t.defuzzify(); }
	public boolean gt(TFN t) { return defuzzify()>t.defuzzify(); }
	public boolean ge(TFN t) { return defuzzify()>=t.defuzzify(); }
	
	public boolean eqStrict(TFN t) { return lowerBound==t.getLowerBound() && meanValue==t.getMeanValue() && upperBound==t.getUpperBound(); }
	public boolean neStrict(TFN t) { return !eqStrict(t); }
	public boolean ltStrict(TFN t) { return upperBound<t.getLowerBound(); }
	public boolean leStrict(TFN t) { return upperBound<=t.getLowerBound(); }
	public boolean gtStrict(TFN t) { return lowerBound>t.getUpperBound(); }
	public boolean geStrict(TFN t) { return lowerBound>=t.getUpperBound(); }
	
	public boolean equals(Object o) { return eq((TFN)o); }
	
	@JsonIgnore
	public static TFN zero() { return new TFN(0,0,0); }
	@JsonIgnore
	public static TFN one() { return new TFN(1,1,1); }
	
	public static TFN valueOf(String s) {
		s = s.trim();
		if (s.startsWith("(") && s.endsWith(")")) s = s.substring(1,s.length()-1);
		String[] p = s.split("[ \t,;]+",3);
		if (p.length==1) {		// crisp value
			return new TFN( Double.parseDouble(p[0].trim()) );
		} else {
			try {
				p[0]=p[0].trim(); p[1]=p[1].trim(); p[2]=p[2].trim();
				return new TFN( Double.valueOf(p[0]), Double.valueOf(p[1]), Double.valueOf(p[2]) );
			} catch (ArrayIndexOutOfBoundsException e) {
				throw (NumberFormatException)(new NumberFormatException("Invalid TFN format: "+s).initCause(e));
			}
		}
	}
}
