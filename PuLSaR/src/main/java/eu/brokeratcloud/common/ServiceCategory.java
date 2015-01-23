//
//XXX: DELETE THIS CLASS
//XXX: CHECK THE CODE DOESN'T BREAK
//
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
/*	@RdfPredicate(delete="cascade")
	protected List<ServiceCategory> children;*/
	
//	public ServiceCategory() { children = new LinkedList<ServiceCategory>(); }
	
	public ServiceCategory getParent() { return parent; }
	public void setParent(ServiceCategory p) { parent = p; }
/*	public List<ServiceCategory> getChildren() { return children; }
	public void setChildren(List<ServiceCategory> c) { children = c; }
	
	/*public void addChild(ServiceCategory c) {
		c.setParent(this);
		children.add(c);
	}
	public void removeChild(ServiceCategory c) {
		if (children.contains(c)) {
			c.setParent(null);
			children.remove(c);
		}
	}
	public void setChild(ServiceCategory cOld, ServiceCategory cNew) {
		if (children.contains(cOld)) {
			int p = children.indexOf(cOld);
			cOld.setParent(null);
			children.set(p, cNew);
		} else {
			addChild(cNew);
		}
	}*/
	
	public String toString() {
/*		String pStrL = null;
		if (children!=null) {
			StringBuffer sb = new StringBuffer("{\n");
			Iterator<ServiceCategory> it = children.iterator();
			while (it.hasNext()) { sb.append("\t\t"); sb.append(it.next()); sb.append("\n"); }
			sb.append("\t}\n"); pStrL = sb.toString();
		}*/
		return "ServiceCategory: {\n"+super.toString()+
				"\tparent = "+(parent!=null ? parent.getId() : "")+/*"\n\tchildren = "+pStrL+*/"}\n";
	}
}
