package eu.brokeratcloud.common.policy;

import eu.brokeratcloud.common.RootObject;
import eu.brokeratcloud.persistence.annotations.*;
import javax.xml.bind.annotation.XmlAttribute;
import org.codehaus.jackson.annotate.JsonIgnore;

@RdfSubject(
	appendName=false,
	suppressRdfType=true
)
public class BrokerPolicyProperty extends RootObject {
	@Id
	@RdfPredicate(dontSerialize=true)
	protected String id;
	@RdfPredicate(isUri=true, uri="http://www.w3.org/2000/01/rdf-schema#subPropertyOf", omitIfNull=true)
	protected String subPropertyOf;
	@RdfPredicate(isUri=true, uri="http://www.w3.org/2000/01/rdf-schema#domain", omitIfNull=true)
	protected String domain;
	@RdfPredicate(uri="http://www.w3.org/2000/01/rdf-schema#range", omitIfNull=true)
	protected AllowedPropertyValue range;
	
	public String getId() { return id; }
	public void setId(String s) { id = s; }
	public String getSubPropertyOf() { return subPropertyOf; }
	public void setSubPropertyOf(String s) { /*subPropertyOf = s;*/ }
	public String getDomain() { return domain; }
	public void setDomain(String s) { domain = s; }
	public AllowedPropertyValue getRange() { return range; }
	public void setRange(AllowedPropertyValue s) { range = s; }
	
	public String toString() {
		return getClass().getSimpleName()+": {"+
				"\n\t"+super.toString()+
				"\n\tid = "+id+
				"\n\tsubPropertyOf = "+subPropertyOf+
				"\n\tdomain = "+domain+
				"\n\trange = "+range+
				"}\n";
	}
	
// =====================================================================================
	
/*	public static void main(String[] args) throws Exception {
		System.out.print("Cleaning existing repository data... ");
		System.out.flush();
		eu.brokeratcloud.fuseki.FusekiClient client = new eu.brokeratcloud.fuseki.FusekiClient();
		client.execute("DELETE {?s ?p ?o} WHERE {?s ?p ?o}");
		System.out.println("ok");
		
		// Auditability - qualitative service attribute
		AllowedQualitativePropertyValue aav = new AllowedQualitativePropertyValue();
		aav.setId("AllowedAuditabilityValue");
		aav.setLabel("Allowed values for Auditability: high, medium, low.");
		aav.setMandatory(true);
		//String[] aavTerms = { "LOW", "OK", "HIGH" };
		//aav.setTerms( aavTerms );
		System.out.println(aav);
		
		QualitativePropertyValue aavHigh = new QualitativePropertyValue();
		aavHigh.setId("AAVhigh");
		aavHigh.setLabel("HIGH");
		aavHigh.setRdfType(aav);
		QualitativePropertyValue aavMedium = new QualitativePropertyValue();
		aavMedium.setId("AAVmedium");
		aavMedium.setLabel("MEDIUM");
		aavMedium.setRdfType(aav);
		QualitativePropertyValue aavLow = new QualitativePropertyValue();
		aavLow.setId("AAVlow");
		aavLow.setLabel("LOW");
		aavLow.setRdfType(aav);
		aavHigh.setGreater(aavMedium);
		aavMedium.setGreater(aavLow);
		
		String aavId = "hasAuditability";
		BrokerPolicyProperty aavBpp = new BrokerPolicyProperty();
		aavBpp.setId(aavId);
		aavBpp.setSubPropertyOf("http://purl.org/goodrelations/v1#qualitativeProductOrServiceProperty");
		aavBpp.setDomain("http://www.broker-cloud.eu/service-descriptions/CAS/broker#CASServiceModel");
		aavBpp.setRange(aav);
		aavBpp.setLabelEn("Auditability");
		aavBpp.setLabelDe("Auditierbarkeit");
		aavBpp.setComment("Nachvollziehbarkeit von Geschaftsvorfallen. Muss explizit gepruft werden");
		aavBpp.setMeasuredBy( "http://www.broker-cloud.eu/service-descriptions/CAS/broker#QS" );
		System.out.println("INITIAL OBJECT GRAPH=\n"+aavBpp);
		
		eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
		pm.persist(aavBpp);
		pm.persist(aavHigh);
		BrokerPolicyProperty aavBpp1 = (BrokerPolicyProperty)pm.find(aavId, BrokerPolicyProperty.class);
		System.out.println("RESULT="+aavBpp1);
		System.out.println("ALLOWED-VALUES="+aavBpp1.getRange());
		
		QualitativePropertyValue qpv = (QualitativePropertyValue)pm.find("AAVhigh", QualitativePropertyValue.class);
		System.out.println("INDIVIDUALS=");
		while (qpv!=null) { System.out.println("\t"+qpv); qpv = qpv.getGreater(); }
		
		// Availability - quantitative service attribute
		AllowedQuantitativePropertyValue aavv = new AllowedQuantitativePropertyValue();
		aavv.setId("AllowedAvailabilityValue");
		aavv.setUnitOfMeasurement( "P1" );
		aavv.setMinValue( 95.0 );
		aavv.setMaxValue( 100.0 );
		aavv.setLabel("Allowed Availability for apps is between 95 and 100%");
		aavv.setMandatory(true);
		aavv.setHigherIsBetter(true);
		System.out.println(aavv);
		
		String aavvId = "hasAvailability";
		BrokerPolicyProperty aavvBpp = new BrokerPolicyProperty();
		aavvBpp.setId(aavvId);
		aavvBpp.setSubPropertyOf("http://purl.org/goodrelations/v1#quantitativeProductOrServiceProperty");
		aavvBpp.setDomain("http://www.broker-cloud.eu/service-descriptions/CAS/broker#CASServiceModel");
		aavvBpp.setRange(aavv);
		aavvBpp.setLabelEn("Availability");
		aavvBpp.setLabelDe("Verfugbarkeit");
		aavvBpp.setComment("Hochverfugbarkeitsnachweis vor allem bei externer Schnittstelle");
		aavvBpp.setMeasuredBy( "http://www.broker-cloud.eu/service-descriptions/CAS/broker#Monitoring" );
		System.out.println("INITIAL OBJECT GRAPH=\n"+aavvBpp);
		
		//eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
		pm.persist(aavvBpp);
		BrokerPolicyProperty aavvBpp1 = (BrokerPolicyProperty)pm.find(aavvId, BrokerPolicyProperty.class);
		System.out.println("RESULT="+aavvBpp1);
		System.out.println("ALLOWED-VALUES="+aavvBpp1.getRange());
	}*/
}
