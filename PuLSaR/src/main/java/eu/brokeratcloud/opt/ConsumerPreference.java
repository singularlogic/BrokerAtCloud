package eu.brokeratcloud.opt;

import eu.brokeratcloud.common.BrokerObject;
import eu.brokeratcloud.persistence.annotations.*;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@RdfSubject(
	uri="http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE",
	rdfType="http://www.linked-usdl.org/ns/usdl-pref#ConsumerPreference"
)
public class ConsumerPreference extends BrokerObject {
	@XmlAttribute
	@RdfPredicate(isUri=true, uri="http://www.linked-usdl.org/ns/usdl-pref#hasPrefVariable")
	protected String prefVariable;
	@XmlAttribute
	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-pref#hasWeight")
	protected double weight;
	@XmlAttribute
	@RdfPredicate(name="isMandatory", setter="setMandatory", getter="getMandatory")
	protected boolean mandatory;
	@XmlAttribute
	@RdfPredicate(delete="cascade", uri="http://www.linked-usdl.org/ns/usdl-pref#hasPreferenceExpression", omitIfNull=true)
	protected ConsumerPreferenceExpression expression;
	
	public String getPrefVariable() { return prefVariable; }
	public void setPrefVariable(String s) { prefVariable = s; }
	public double getWeight() { return weight; }
	public void setWeight(double w) { if (w<0 || w>1) throw new IllegalArgumentException("ConsumerPreference: Weight must be between 0.0 and 1.0: Specified weight: "+w); weight = w; }
	public boolean getMandatory() { return mandatory; }
	public void setMandatory(boolean m) { mandatory = m; }
	public ConsumerPreferenceExpression getExpression() { return expression; }
	public void setExpression(ConsumerPreferenceExpression e) { expression = e; if (expression!=null) expression.setConsumerPreference(this); }
	
	public String toString() {
		return 	"ConsumerPreference: {\n"+super.toString()+
				"\tpref.var.="+prefVariable+"\n\tweight="+weight+"\n\tmandatory="+mandatory+"\n\texpression = "+expression+"\n}\n";
	}
}
