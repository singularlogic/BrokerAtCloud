package eu.brokeratcloud.opt;

import java.util.HashMap;
import java.util.Map;
import eu.brokeratcloud.common.BrokerObject;
import eu.brokeratcloud.common.ClassificationDimension;
import eu.brokeratcloud.persistence.annotations.*;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@RdfSubject(
	uri="http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE-PROFILE",
	rdfType="http://www.linked-usdl.org/ns/usdl-pref#ConsumerPreferenceProfile"
)
public class ConsumerPreferenceProfile extends BrokerObject {
	@XmlAttribute
	@RdfPredicate(update="no")
	protected ClassificationDimension[] serviceClassifications;
	@XmlAttribute
	@RdfPredicate
	protected String selectionPolicy;
	@XmlAttribute
	@RdfPredicate
	protected int order;
	@XmlAttribute
	@RdfPredicate(delete="cascade")
	protected ConsumerPreference[] preferences;
	@XmlAttribute
	@RdfPredicate(delete="cascade")
	protected ComparisonPair[] comparisonPairs;
	@XmlAttribute
	@RdfPredicate
	protected boolean weightCalculation;
	
	public ConsumerPreferenceProfile() { preferences = new ConsumerPreference[0]; }
	
	public ClassificationDimension[] getServiceClassifications() { return serviceClassifications; }
	public void setServiceClassifications(ClassificationDimension[] sc) { serviceClassifications = sc; }
	public String getSelectionPolicy() { return selectionPolicy; }
	public void setSelectionPolicy(String sp) { selectionPolicy = sp; }
	public int getOrder() { return order; }
	public void setOrder(int o) { if (o<0) throw new IllegalArgumentException(getClass().getName()+": setOrder: order cannot be negative: given value="+o); order = o; }
	public ConsumerPreference[] getPreferences() { return preferences; }
	public void setPreferences(ConsumerPreference[] p) { preferences = p; if (preferences==null) preferences = new ConsumerPreference[0]; }
	public ComparisonPair[] getComparisonPairs() { return comparisonPairs; }
	public void setComparisonPairs(ComparisonPair[] p) { comparisonPairs = p; if (comparisonPairs==null) comparisonPairs = new ComparisonPair[0]; }
	public boolean getWeightCalculation() { return weightCalculation; }
	public void setWeightCalculation(boolean b) { weightCalculation = b; }
	
	public String toString() {
		String pStrM = null;
		if (preferences!=null) {
			StringBuffer sb = new StringBuffer("{\n");
			for (ConsumerPreference cp : preferences) { sb.append("\t\t"); sb.append(cp!=null ? cp.getId() : null); sb.append(" : "); sb.append(cp); sb.append("\n"); }
			sb.append("\t}\n"); pStrM = sb.toString();
		}
		String clsfStr = "[]";
		if (serviceClassifications!=null) {
			StringBuilder sb = new StringBuilder("[ ");
			for (ClassificationDimension cd : serviceClassifications) sb.append(cd.getId()).append(" ");
			sb.append("]");
			clsfStr = sb.toString();
		}
		return 	"ConsumerPreferenceProfile: {\n"+super.toString()+
				"\tclassifications="+clsfStr+"\n\tselection-policy="+selectionPolicy+"\n\torder="+order+"\n\tpreferences = "+pStrM+"\n\tweight calc. = "+weightCalculation+"}\n";
	}
}
