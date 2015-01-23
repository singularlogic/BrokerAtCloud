package eu.brokeratcloud.opt;

import java.util.Iterator;
import java.util.List;
import eu.brokeratcloud.common.BrokerObject;
import eu.brokeratcloud.persistence.annotations.*;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@RdfSubject(
	uri="http://www.brokeratcloud.eu/v1/opt/RECOMMENDATION",
	rdfType="http://www.linked-usdl.org/ns/usdl-pref#Recommendation"
)
public class Recommendation extends BrokerObject {
	@XmlAttribute
	@RdfPredicate
	protected String profile;
	@XmlAttribute
	@RdfPredicate(delete="cascade")
	protected List<RecommendationItem> items;
	@XmlAttribute
	@RdfPredicate(getter="isActive")
	protected boolean active;
	
	public String getProfile() { return profile; }
	public void setProfile(String p) { profile = p; }
	public List<RecommendationItem> getItems() { return items; }
	public void setItems(List<RecommendationItem> s) { items = s; }
	public boolean isActive() { return active; }
	public void setActive(boolean b) { active = b; }
	
	public String toString() {
		String pStrL = null;
		if (items!=null) {
			StringBuffer sb = new StringBuffer("{\n");
			Iterator<RecommendationItem> it = items.iterator();
			while (it.hasNext()) { sb.append("\t\t"); sb.append(it.next()); sb.append("\n"); }
			sb.append("\t}\n"); pStrL = sb.toString();
		}
		return 	"Recommendation: {\n"+super.toString()+
				"\tprofile = "+profile+"\n\titems = "+pStrL+"\n\tactive = "+active+" }\n";
	}
}
