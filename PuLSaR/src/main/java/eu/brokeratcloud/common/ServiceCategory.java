package eu.brokeratcloud.common;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import eu.brokeratcloud.persistence.annotations.*;

@RdfSubject(
	uri="http://www.brokeratcloud.eu/v1/common/SERVICE-CATEGORY",
	rdfType="http://www.linked-usdl.org/ns/usdl-pref#FunctionalServiceCategory"
)
public class ServiceCategory extends BrokerObject {
	@RdfPredicate(uri="http://www.w3.org/2004/02/skos/core#broader")
	protected ServiceCategory parent;
	
	public ServiceCategory getParent() { return parent; }
	public void setParent(ServiceCategory p) { parent = p; }
	
	public String toString() {
		return "ServiceCategory: {\n"+super.toString()+
				"\tparent = "+(parent!=null ? parent.getId() : "")+"}\n";
	}
}
