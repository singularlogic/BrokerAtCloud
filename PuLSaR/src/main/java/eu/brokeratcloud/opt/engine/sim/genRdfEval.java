package eu.brokeratcloud.opt.engine.sim;

import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class genRdfEval {
	protected static HashMap<String,Object> vars = new HashMap<String,Object>();
	
	protected static void p(String s) {
		String tmp = s;
		for (String k : vars.keySet()) {
			Object o = vars.get(k);
			String v = (o!=null) ? o.toString() : "";
			tmp = tmp.replaceAll("\\{\\{ "+k+" \\}\\}", v);
		}
		
		System.out.println(tmp);
	}
	
	public static void main(String args[]) {
		String testDescription = args[0];
		String testCreationTimestamp = new Date().toString();
		int nAttributes = Integer.parseInt(args[1]);
		int nServices = Integer.parseInt(args[2]);
		int nProfiles = Integer.parseInt(args[3]);
		int nCriteria = Integer.parseInt(args[4]);
		
		vars.put("description", testDescription);
		vars.put("genDate", testCreationTimestamp);
		vars.put("nAttributes", nAttributes);
		vars.put("nServices", nServices);
		vars.put("nProfiles", nProfiles);
		vars.put("nCriteria", nCriteria);

		p("# Description : {{ description }} ");
		p("# Generated on: {{ genDate }} ");
		p("");
		p("@prefix cas:   <http://www.broker-cloud.eu/service-descriptions/CAS/broker#> .");
		p("@prefix usdl-pref: <http://www.linked-usdl.org/ns/usdl-pref#> .");
		p("@prefix vann:  <http://purl.org/vocab/vann/> .");
		p("@prefix pref-att: <http://www.brokeratcloud.eu/v1/opt/SERVICE-ATTRIBUTE/> .");
		p("@prefix dcterms: <http://purl.org/dc/terms/> .");
		p("@prefix cpp-fld-profile: <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE-PROFILE/> .");
		p("@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .");
		p("@prefix time:  <http://www.w3.org/2006/time#> .");
		p("@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
		p("@prefix usdl-core-cb: <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#> .");
		p("@prefix cpp-profile: <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE-PROFILE#> .");
		p("@prefix dc:    <http://purl.org/dc/elements/1.1/> .");
		p("@prefix usdl-sla: <http://www.linked-usdl.org/ns/usdl-sla#> .");
		p("@prefix cpp-pref: <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE#> .");
		p("@prefix foaf:  <http://xmlns.com/foaf/0.1/> .");
		p("@prefix sp:    <http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#> .");
		p("@prefix usdl-business-roles: <http://www.linked-usdl.org/ns/usdl-business-roles#> .");
		p("@prefix xml:   <http://www.w3.org/XML/1998/namespace#> .");
		p("@prefix attr:  <http://www.brokeratcloud.eu/v1/opt/SERVICE-ATTRIBUTE#> .");
		p("@prefix vcard: <http://www.w3.org/TR/vcard-rdf/> .");
		p("@prefix fc:    <http://www.broker-cloud.eu/service-descriptions/CAS/categories#> .");
		p("@prefix usdl-core: <http://www.linked-usdl.org/ns/usdl-core#> .");
		p("@prefix cpp-fld-expr: <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE-EXPRESSION/> .");
		p("@prefix s:     <http://schema.org/> .");
		p("@prefix cpp-fld-pref: <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE/> .");
		p("@prefix usdl-sla-cb: <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#> .");
		p("@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .");
		p("@prefix owl:   <http://www.w3.org/2002/07/owl#> .");
		p("@prefix pulsar: <http://www.brokeratcloud.eu/persist/types#> .");
		p("@prefix gr:    <http://purl.org/goodrelations/v1#> .");
		p("@prefix skos:  <http://www.w3.org/2004/02/skos/core#> .");
		p("@prefix cpp-expr: <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE-EXPRESSION#> .");
		p("@prefix cas-pref-att: <http://www.broker-cloud.eu/service-descriptions/CAS/broker-pref-attr#> .");
		p("");
		p("# ========================================================================================");
		p("#  Attributes specification  (attribute count : {{ nAttributes }})");
		p("# ========================================================================================");
		p("");
		p("attr:sim-attribute-root");
		p("        a                   usdl-pref:ServiceAttribute ;");
		p("        dcterms:created     \"2015-03-10T10:15:22.898+02:00\"^^xsd:dateTime ;");
		p("        dcterms:description \"Root of Sim. Attributes\"^^xsd:string ;");
		p("        dcterms:identifier  \"sim-attribute-root\"^^xsd:string ;");
		p("        dcterms:modified    \"2015-03-10T10:15:22.898+02:00\"^^xsd:dateTime ;");
		p("        dcterms:title       \"Sim. Attribute Root\"^^xsd:string .");
		p("");
		for (int nAttr=1; nAttr<=nAttributes; nAttr++) {
			vars.put("nAttr", nAttr);
			p("### Attribute {{ nAttr }} ");
			p("attr:sim-attribute-{{ nAttr }}");
			p("        a                   usdl-pref:ServiceAttribute ;");
			p("        dcterms:created     \"2015-03-10T10:16:57.642+02:00\"^^xsd:dateTime ;");
			p("        dcterms:description \"Sim. Attribute number {{ nAttr }}\"^^xsd:string ;");
			p("        dcterms:identifier  \"sim-attribute-{{ nAttr }}\"^^xsd:string ;");
			p("        dcterms:modified    \"2015-03-10T10:16:57.642+02:00\"^^xsd:dateTime ;");
			p("        dcterms:title       \"Sim. Attribute #{{ nAttr }}\"^^xsd:string ;");
			p("        skos:broader        attr:sim-attribute-root .");
			p("");
			p("cas:hasSimAttribute{{ nAttr }}  rdfs:comment  \"Comment {{ nAttr }}\"^^xsd:string ;");
			p("        rdfs:domain         cas:CASServiceModel ;");
			p("        rdfs:label          \"Attribute {{ nAttr }} (EN)\"@en , \"Attribute {{ nAttr }} (DE)\"@de ;");
			p("        rdfs:range          cas:AllowedSimAttribute{{ nAttr }}Value ;");
			p("        rdfs:subPropertyOf  gr:qualitativeProductOrServiceProperty .");
			p("");
			p("cas:AllowedSimAttribute{{ nAttr }}Value");
			p("        rdfs:subClassOf  gr:QualitativeValue .");
			p("");
			p("	### Allowed values - BEGIN ###");
			p("		cas:SimAttribute{{ nAttr }}_LOW");
			p("				a           cas:AllowedSimAttribute{{ nAttr }}Value ;");
			p("				rdfs:label  \"LOW\"@en ;");
			p("				gr:lesser   cas:SimAttribute{{ nAttr }}_MEDIUM .");
			p("");
			p("		cas:SimAttribute{{ nAttr }}_MEDIUM");
			p("				a           cas:AllowedSimAttribute{{ nAttr }}Value ;");
			p("				rdfs:label  \"MEDIUM\"@en ;");
			p("				gr:greater  cas:SimAttribute{{ nAttr }}_LOW ;");
			p("				gr:lesser   cas:SimAttribute{{ nAttr }}_HIGH .");
			p("");
			p("		cas:SimAttribute{{ nAttr }}_HIGH");
			p("				a           cas:AllowedSimAttribute{{ nAttr }}Value ;");
			p("				rdfs:label  \"HIGH\"@en ;");
			p("				gr:greater  cas:SimAttribute{{ nAttr }}_MEDIUM .");
			p("	### Allowed values - BEGIN ###");
			p("");
			p("cas-pref-att:SimAttribute{{ nAttr }}PreferenceVariable");
			p("        rdfs:subClassOf      usdl-pref:QualitativeVariable ;");
			p("        usdl-pref:belongsTo  fc:rootConcept ;");
			p("        usdl-pref:refToServiceAttribute");
			p("                attr:sim-attribute-{{ nAttr }} .");
			p("");
			p("cas-pref-att:hasDefaultSimAttribute{{ nAttr }}");
			p("        rdfs:domain         cas-pref-att:SimAttribute{{ nAttr }}PreferenceVariable ;");
			p("        rdfs:range          cas:AllowedSimAttribute{{ nAttr }}Value ;");
			p("        rdfs:subPropertyOf  usdl-pref:hasDefaultQualitativeValue .");
			p("");
			p("##################################################################################");
			p("#cas:CASBrokerPolicy");
			p("#        cas:hasAttribute{{ nAttr }} ");
			p("#                cas:AllowedSimAttribute{{ nAttr }}Value .");
			p("##################################################################################");
			p("");
			p("### Service Levels ");
			p("cas:ServiceLevelAttribute{{ nAttr }} rdfs:subClassOf usdl-sla:ServiceLevel .");
			p("");
			p("### Service Level Expressions ");
			p("cas:SLE-Attribute{{ nAttr }} rdfs:subClassOf usdl-sla:ServiceLevelExpression . ");
			p("");
			p("### Variables ");
			p("cas:VarAttribute{{ nAttr }} rdfs:subClassOf usdl-sla:Variable .");
			p("");
			p("## for service levels  ");
			p("cas:hasServiceLevelAttribute{{ nAttr }} rdfs:subPropertyOf usdl-sla:hasServiceLevel;");
			p("	rdfs:domain cas:CASServiceLevelProfile ;");
			p("	rdfs:range cas:ServiceLevelAttribute{{ nAttr }} .");
			p("");
			p("## for service level expressions ");
			p("cas:hasSLE-Attribute{{ nAttr }} rdfs:subPropertyOf usdl-sla:hasServiceLevelExpression;");
			p("	rdfs:domain cas:ServiceLevelAttribute{{ nAttr }} ;");
			p("	rdfs:range cas:SLE-Attribute{{ nAttr }} .");
			p("");
			p("## for variables ");
			p("cas:hasVariableAttribute{{ nAttr }} rdfs:subPropertyOf usdl-sla:hasVariable; ");
			p("	rdfs:domain cas:SLE-Attribute{{ nAttr }} ;");
			p("	rdfs:range cas:VarAttribute{{ nAttr }} .");
			p("");
			p("## for Values");
			p("cas:hasDefaultAttribute{{ nAttr }} rdfs:subPropertyOf usdl-sla-cb:hasDefaultQualitativeValue;");
			p("	rdfs:domain cas:VarAttribute{{ nAttr }} ;");
			p("	rdfs:range cas:AllowedAttribute{{ nAttr }}Value.");
			p("");
			p("### Values ");
			p("cas:Attribute{{ nAttr }}High rdf:type cas:AllowedAttribute{{ nAttr }}Value;");
			p("	rdfs:label \"High Attribute{{ nAttr }} for apps is between 99,98 and 100%\";");
			p("	gr:hasUnitOfMeasurement \"P1\"^^xsd:string; #Percent");
			p("	gr:hasMinValue \"99.98\"^^xsd:int;");
			p("	gr:hasMaxValue \"100\"^^xsd:int. ");
			p("");
			p("cas:Attribute{{ nAttr }}Medium rdf:type cas:AllowedAttribute{{ nAttr }}Value;");
			p("	rdfs:label \"Medium Attribute{{ nAttr }} for apps is between 99,5 and 100%\";");
			p("	gr:hasUnitOfMeasurement \"P1\"^^xsd:string; #Percent");
			p("	gr:hasMinValue \"99.5\"^^xsd:int;");
			p("	gr:hasMaxValue \"100\"^^xsd:int. ");
			p("");
			p("cas:Attribute{{ nAttr }}Low rdf:type cas:AllowedAttribute{{ nAttr }}Value;");
			p("	rdfs:label \"Low Attribute{{ nAttr }} for apps is between 99 and 100%\";");
			p("	gr:hasUnitOfMeasurement \"P1\"^^xsd:string; #Percent");
			p("	gr:hasMinValue \"99\"^^xsd:int;");
			p("	gr:hasMaxValue \"100\"^^xsd:int. ");
			p("");
			p("# Instantiation for Gold service-level profile");
			p("cas:CASServiceLevelProfileGold ");
			p("		cas:hasServiceLevelAttribute{{ nAttr }} cas:GoldAttribute{{ nAttr }} .");
			p("");
			p("cas:GoldAttribute{{ nAttr }} a cas:ServiceLevelAttribute{{ nAttr }} ;");
			p("		cas:hasSLE-Attribute{{ nAttr }} cas:SLE-GoldAttribute{{ nAttr }} .");
			p("");
			p("cas:SLE-GoldAttribute{{ nAttr }} a cas:SLE-Attribute{{ nAttr }};");
			p("		cas:hasVariableAttribute{{ nAttr }}  cas:VarGoldAttribute{{ nAttr }} .");
			p("");
			p("cas:VarGoldAttribute{{ nAttr }} a cas:VarAttribute{{ nAttr }};");
			p("		cas:hasDefaultAttribute{{ nAttr }} cas:Attribute{{ nAttr }}Medium.");
			p("");
			p("# ----------------------------------------------------------------------------------------");
		}
		p("");
		p("# ========================================================================================");
		p("#  Service Descriptions specification  (service description count : {{ nServices }})");
		p("# ========================================================================================");
		p("");
		for (int nSrv=1; nSrv<=nServices; nSrv++) {
			vars.put("nSrv", nSrv);
			p("### SimApp{{ nSrv }} ###");
			p("sp:SimApp{{ nSrv }} a usdl-core:Service, cas:App;");
			p("	dcterms:title \"Sim App {{ nSrv }}\";");
			p("	dcterms:creator sp:CAS_Software_AG;");
			p("	dcterms:description \"Dummy App for simulation nr {{ nSrv }}\" ;");
			p("	usdl-core:hasEntityInvolvement sp:ISV1EntityInvolvement;");
			p("	usdl-core-cb:hasServiceModel sp:ServiceModelSimApp{{ nSrv }}. ");
			p("");
			p("### Service Model for the SimApp{{ nSrv }} ### ");
			p("sp:ServiceModelSimApp{{ nSrv }} a cas:CASServiceModel;");
			p("	gr:isVariantOf cas:CASBrokerPolicy;");
			p("	usdl-core-cb:hasClassificationDimension fc:projectmanagement, fc:automobile, fc:contacts ;");
			p("	### Attribute Values - BEGIN ###");
			for (int nCrit=1; nCrit<=nCriteria; nCrit++) {
				vars.put("nCrit", nCrit);
				p("		cas:hasSimAttribute{{ nCrit }} cas:SimAttribute{{ nCrit }}_HIGH ;");
			}
			p("	### Attribute Values - END   ###");
			p("	cas:hasServiceLevelProfileCAS cas:CASServiceLevelProfileGold .");
			p("");
			p("# ----------------------------------------------------------------------------------------");
		}
		p("");
		p("# ========================================================================================");
		p("#  Profiles specification  (profile count : {{ nProfiles }},  criteria per profile : {{ nCriteria }})");
		p("# ========================================================================================");
		p("");
		for (int nProf=1; nProf<=nProfiles; nProf++) {
			vars.put("nProf", nProf);
			p("cpp-profile:CPP-{{ nProf }}  a                 usdl-pref:ConsumerPreferenceProfile ;");
			p("        dcterms:created              \"2015-03-05T01:08:00+02:00\"^^xsd:dateTime ;");
			p("        dcterms:creator              \"admin\"^^xsd:string ;");
			p("        dcterms:description          \"Dummy Profile for simulation nr {{ nProf }}\"^^xsd:string ;");
			p("        dcterms:identifier           \"CPP-{{ nProf }}\"^^xsd:string ;");
			p("        dcterms:modified             \"2015-03-10T13:06:17.337+02:00\"^^xsd:dateTime ;");
			p("        dcterms:title                \"PROFILE {{ nProf }}\"^^xsd:string ;");
			p("        pulsar:class                 \"eu.brokeratcloud.opt.ConsumerPreferenceProfile\"^^xsd:string ;");
			p("        cpp-fld-profile:order        \"{{ nProf }}\"^^xsd:int ;");
			p("		### Preferences - BEGIN ###");
			p("        cpp-fld-profile:preferences  \"{{ nCriteria }}\"^^xsd:int ;");
			for (int nCrit=1; nCrit<=nCriteria; nCrit++) {
				vars.put("nCrit", nCrit);
				vars.put("nCrit-1", nCrit-1);
				p("        <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE-PROFILE/preferences:_{{ nCrit-1 }}>");
				p("                cpp-pref:PREFERENCE-{{ nProf }}-{{ nCrit }} ;");
			}
			p("		### Preferences - END ###");
			p("		### Comparison pairs - BEGIN ###");
			vars.put("numOfCombinations", nCriteria * ( nCriteria - 1 ) / 2);
			p("        cpp-fld-profile:comparisonPairs  \"{{ numOfCombinations }}\"^^xsd:int ;");
			
			int cpair = 0;
			for (int i=1; i<=nCriteria-1; i++) {
				vars.put("i", i);
				int cpair1= 0;
				for (int j=i+1; j<=nCriteria; j++) {
					vars.put("j", j);
					vars.put("cpair1_cpair", cpair1+cpair);
					p("		cpp-fld-profile:comparisonPairs:_{{ cpair1_cpair }}");
					p("			<http://www.brokeratcloud.eu/v1/opt/COMPARISON-PAIR#CPAIR-{{ nProf }}-{{ i }}-{{ j }}> ;");
					cpair1++;
				}
				cpair += (nCriteria - i);
			}
			p("		### Comparison pairs - END ###");
			p("        cpp-fld-profile:selectionPolicy");
			p("                \"TOP 1\"^^xsd:string ;");
			p("        cpp-fld-profile:serviceClassifications");
			p("                \"3\"^^xsd:int ;");
			p("        <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE-PROFILE/serviceClassifications:_0>");
			p("                fc:automobile ;");
			p("        <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE-PROFILE/serviceClassifications:_1>");
			p("                fc:projectmanagement ;");
			p("        <http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE-PROFILE/serviceClassifications:_2>");
			p("                fc:contacts ;");
			p("        cpp-fld-profile:weightCalculation");
			p("                true .");
			p("");
			p("### Preferences & Expressions ###");
			
			vars.put("invCriteria", 1 / nCriteria);
			for (int nCrit=1; nCrit<=nCriteria; nCrit++) {
				vars.put("nCrit", nCrit);
				p("cpp-pref:PREFERENCE-{{ nProf }}-{{ nCrit }} ");
				p("        a                          usdl-pref:ConsumerPreference ;");
				p("        dcterms:created            \"2015-03-05T01:08:00.000+02:00\"^^xsd:dateTime ;");
				p("        dcterms:identifier         \"PREFERENCE-{{ nProf }}-{{ nCrit }}\"^^xsd:string ;");
				p("        dcterms:modified           \"2015-03-10T13:06:17.319+02:00\"^^xsd:dateTime ;");
				p("        pulsar:class               \"eu.brokeratcloud.opt.ConsumerPreference\"^^xsd:string ;");
				p("        cpp-fld-pref:isMandatory   false ;");
				p("        usdl-pref:hasPrefVariable  cas-pref-att:SimAttribute{{ nCrit }}PreferenceVariable ;");
				p("        usdl-pref:hasPreferenceExpression");
				p("                cpp-expr:EXPRESSION-{{ nProf }}-{{ nCrit }} ;");
				p("        usdl-pref:hasWeight        \"{{ invCriteria }}\"^^xsd:double .");
				p("");
				p("cpp-expr:EXPRESSION-{{ nProf }}-{{ nCrit }}");
				p("        a                        usdl-pref:ConsumerPreferenceExpression ;");
				p("        dcterms:identifier       \"EXPRESSION-{{ nProf }}-{{ nCrit }}\"^^xsd:string ;");
				p("        pulsar:class             \"eu.brokeratcloud.opt.ConsumerPreferenceExpression\"^^xsd:string ;");
				p("        cpp-fld-expr:consumerPreference");
				p("                cpp-pref:PREFERENCE-{{ nProf }}-{{ nCrit }} ;");
				p("        cpp-fld-expr:expression  \"-\"^^xsd:string .");
				p("");
			}
			p("");
			p("### Comparison Pairs ###");
			
			for (int i=1; i<=nCriteria - 1; i++) {
			for (int j=i+1; j<=nCriteria; j++) {
				vars.put("i", i);
				vars.put("j", j);
				p("<http://www.brokeratcloud.eu/v1/opt/COMPARISON-PAIR#CPAIR-{{ nProf }}-{{ i }}-{{ j }}>");
				p("        a                   <http://www.brokeratcloud.eu/v1/opt/COMPARISON-PAIR> ;");
				p("        dcterms:identifier  \"CPAIR-{{ nProf }}-{{ i }}-{{ j }}\"^^xsd:string ;");
				p("        pulsar:class        \"eu.brokeratcloud.opt.ComparisonPair\"^^xsd:string ;");
				p("        <http://www.brokeratcloud.eu/v1/opt/COMPARISON-PAIR/attribute1>");
				p("                \"sim-attribute-{{ i }}\"^^xsd:string ;");
				p("        <http://www.brokeratcloud.eu/v1/opt/COMPARISON-PAIR/attribute2>");
				p("                \"sim-attribute-{{ j }}\"^^xsd:string ;");
				p("        <http://www.brokeratcloud.eu/v1/opt/COMPARISON-PAIR/value>");
				p("                \"0\"^^xsd:string .");
				p("");
			}
			}
			p("# ----------------------------------------------------------------------------------------");
		}
		p("");
		p("#EOF");
	}
}