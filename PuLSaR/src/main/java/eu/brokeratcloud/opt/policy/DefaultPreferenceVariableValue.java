package eu.brokeratcloud.opt.policy;

import eu.brokeratcloud.common.RootObject;
import eu.brokeratcloud.common.policy.*;
import eu.brokeratcloud.persistence.annotations.*;
import javax.xml.bind.annotation.XmlAttribute;
import org.codehaus.jackson.annotate.JsonIgnore;

@RdfSubject(
//	uri = "http://www.linked-usdl.org/ns/usdl-pref#",
	appendName=false,
	suppressRdfType=true
)
public class DefaultPreferenceVariableValue extends RootObject {
	@Id
	@RdfPredicate(dontSerialize=true)
	protected String id;
	@RdfPredicate(isUri=true, uri="http://www.w3.org/2000/01/rdf-schema#subPropertyOf")
	protected String subPropertyOf;
	@RdfPredicate(uri="http://www.w3.org/2000/01/rdf-schema#domain", update="nocascade")
	protected PreferenceVariable domain;
	@RdfPredicate(uri="http://www.w3.org/2000/01/rdf-schema#range", update="nocascade", omitIfNull=true)
	protected AllowedPropertyValue range;
	
	public String getId() { return id; }
	public void setId(String s) { id = s; }
	public String getSubPropertyOf() { return subPropertyOf; }
	public void setSubPropertyOf(String s) { /*subPropertyOf = s;*/ }
	public PreferenceVariable getDomain() { return domain; }
	public void setDomain(PreferenceVariable pv) { domain = pv; }
	public AllowedPropertyValue getRange() { return range; }
	public void setRange(AllowedPropertyValue s) { range = s; }
	
	protected boolean quantitative, qualitative, fuzzy;
	
	@JsonIgnore public boolean isQuantitative() { return quantitative; }
	@JsonIgnore public boolean isQualitative() { return qualitative; }
	@JsonIgnore public boolean isFuzzy() { return fuzzy; }
	@JsonIgnore public void setQuantitative() { quantitative=qualitative=fuzzy=false; quantitative=true; subPropertyOf = "http://www.linked-usdl.org/ns/usdl-pref#hasDefaultQuantitativeValue"; }
	@JsonIgnore public void setQualitative() { quantitative=qualitative=fuzzy=false; qualitative=true; subPropertyOf = "http://www.linked-usdl.org/ns/usdl-pref#hasDefaultQualitativeValue"; }
	@JsonIgnore public void setFuzzy() { quantitative=qualitative=fuzzy=false; fuzzy=true; subPropertyOf = "http://www.linked-usdl.org/ns/usdl-pref#hasDefaultFuzzyValue"; }
	
	public String toString() {
		return getClass().getSimpleName()+": {"+
				"\n\t"+super.toString()+
				"\n\tid = "+id+
				"\n\tsubPropertyOf = "+subPropertyOf+
				"\n\tdomain = "+domain+
				"\n\trange = "+range+
				"}\n";
	}
}
