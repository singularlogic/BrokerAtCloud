package eu.brokeratcloud.opt;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import eu.brokeratcloud.common.BrokerObject;
import eu.brokeratcloud.persistence.annotations.*;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@RdfSubject(
	uri="http://www.brokeratcloud.eu/v1/opt/SERVICE-ATTRIBUTE",
	rdfType="http://www.linked-usdl.org/ns/usdl-pref#ServiceAttribute"
)
public class OptimisationAttribute extends BrokerObject {
	@XmlAttribute
	@RdfPredicate(uri="http://www.w3.org/2004/02/skos/core#broader", update="no-cascade", delete="no-cascade", omitIfNull=true)
	protected OptimisationAttribute parent;
	
	public OptimisationAttribute getParent() { return parent; }
	public void setParent(OptimisationAttribute p) { parent = p; }
	
	public String toString() {
		return 	"OptimisationAttribute: {\n"+super.toString()+
				"\tparent = "+(parent!=null ? parent.getId() : "")+
				"}\n";
	}
}
