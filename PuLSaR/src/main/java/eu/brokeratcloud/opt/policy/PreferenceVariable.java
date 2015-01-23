package eu.brokeratcloud.opt.policy;

import java.util.*;

import eu.brokeratcloud.common.RootObject;
import eu.brokeratcloud.common.ClassificationDimension;
import eu.brokeratcloud.common.BrokerObject;
import eu.brokeratcloud.common.policy.*;
import eu.brokeratcloud.opt.OptimisationAttribute;
import eu.brokeratcloud.persistence.annotations.*;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@RdfSubject(
	uri = "http://www.linked-usdl.org/ns/usdl-pref#",
	rdfType = "http://www.w3.org/2000/01/rdf-schema#Class, http://www.w3.org/2002/07/owl#Class",
	appendName=false
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreferenceVariable extends RootObject {
	@Id
	@RdfPredicate(dontSerialize=true)
	protected String id;
	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-pref#refToServiceAttribute")
	protected OptimisationAttribute refToServiceAttribute;
	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-pref#belongsTo")
	protected ClassificationDimension belongsTo;
	@RdfPredicate(isUri=true, uri="http://www.w3.org/2000/01/rdf-schema#subClassOf", omitIfNull=true)
	protected String subClassOf;
/*	@RdfPredicate(omitIfNull=true)
	protected BrokerPolicyProperty refToBrokerPolicyProperty;	// Property not included in USDL-PREF model. Used to support plain datatype properties
*/	
	public String getId() { return id; }
	public void setId(String s) { id = s; }
	public OptimisationAttribute getRefToServiceAttribute() { return refToServiceAttribute; }
	public void setRefToServiceAttribute(OptimisationAttribute oa) { refToServiceAttribute = oa; }
	public ClassificationDimension getBelongsTo() { return belongsTo; }
	public void setBelongsTo(ClassificationDimension cc) { belongsTo = cc; }
	public String getSubClassOf() { return subClassOf; }
	public void setSubClassOf(String s) { subClassOf = s; }

/*	public BrokerPolicyProperty getRefToBrokerPolicyProperty() { return refToBrokerPolicyProperty; }
	public void setRefToBrokerPolicyProperty(BrokerPolicyProperty bpp) { refToBrokerPolicyProperty = bpp; }
*/	
	public String toString() {
		String oaStr = refToServiceAttribute!=null ? refToServiceAttribute.getId() : "null";
		String ccStr = belongsTo!=null ? belongsTo.getId() : "null";
		return 	getClass().getSimpleName()+": {\n"+super.toString()+
				"\n\tid="+id+
				"\n\tservice-attr="+oaStr+
				"\n\tclassif.dim.="+ccStr+
				"\n\tsubClassOf="+subClassOf+
//				"\n\tpolicy-property="+refToBrokerPolicyProperty+
				"\n}\n";
	}
}
