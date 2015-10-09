package diva.brokeratcloud.fpr.input.sparql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import diva.brokeratcloud.fpr.input.local.ServiceAttributeLocal;

public class ServiceAttributeSparql extends ServiceAttributeLocal {

	static final String attrPrefix = "cas";
	static List<String> attrNames = Arrays.asList("hasExtensibility", "hasAdaptability", "hasEaseOfDoingBusiness",
			"hasSuitability", "hasPortability", "hasRecoverability", "hasLearnability", "hasAccessibility",
			"hasOperability");
	static Map<String, Integer> attrRec = null;

	private static Map<String, List<String>> alias = new HashMap<String, List<String>>();
	private static Map<String, String> literals = new HashMap<String, String>();

	static {
		alias.put("sp:hasAvailability", Arrays.asList("Avail", "Availability", "availability", "hasAvailability"));
		alias.put("gr:hasPriceSpecification", Arrays.asList("Price", "hasPriceSpecification"));
		alias.put("cas:hasMinimumResponseTime", Arrays.asList("minimumResponseTime"));
		literals.put("sp:hasAvailability", "gr:hasValue");
		literals.put("gr:hasPriceSpecification", "gr:hasCurrencyValue");
		literals.put("cas:hasMinimumResponseTime", "gr:hasValue");
	}

	@Override
	public List<String> listCommonAttributes() {
		return attrNames;

	}

	public Object get(String service, String attribute) {
		if (attrRec == null) {
			init();
		}
		Object obj = attrRec.get(service + "-" + attribute);
		if (obj == null)
			return 0;
		else
			return obj;

	}

	void init() {
		attrRec = new HashMap<String, Integer>();
		for (String attrName : attrNames) {
			String q = "SELECT ?service ?attr ?value" + "\n" + "WHERE\n" + "  {\n"
					+ "    ?service usdl-core-cb:hasServiceModel ?model. \n" + "	 ?model " + attrPrefix + ":"
					+ attrName + " ?value \n" + "  }";
			try {
				Collection mBindings = SparqlQuery.INSTANCE.queryToJsonResults(q);
				for (Object x : mBindings) {
					Map m = (Map) x;
					String service = ((Map) m.get("service")).get("value").toString();
					String value = ((Map) m.get("value")).get("value").toString();
					service = ServiceCategorySparql.resolveService(service);
					attrRec.put(service + "-" + attrName, resolveValue(value));
				}
			} catch (Exception e) {
				throw new RuntimeException("Wrong query or results", e);
			}
		}
	}

	int resolveValue(String literal) {
		if (literal == null) {
			return 0;
		}
		if (literal.endsWith("LOW") || literal.endsWith("EASY") || literal.endsWith("GOOD")) {
			return 1;
		} else if (literal.endsWith("MEDIUM")) {
			return 2;
		} else if (literal.endsWith("HIGH") || literal.endsWith("DIFFICULT") || literal.endsWith("BAD")) {
			return 4;
		} else
			return 0;
	}

}
