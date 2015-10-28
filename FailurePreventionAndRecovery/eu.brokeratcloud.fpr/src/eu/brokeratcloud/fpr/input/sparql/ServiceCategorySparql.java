package eu.brokeratcloud.fpr.input.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.brokeratcloud.fpr.input.local.ServiceCategoryLocal;

public class ServiceCategorySparql extends ServiceCategoryLocal {

	private static final String servicePrefix = "sp";
	private static final String servicePrefixFull = "http://www.broker-cloud.eu/service-descriptions/CAS/service-provider#";

	private static final String fcDimention = "FC";
	public static final String fcPrefix = "fc:";
	public static final String fcPrefixFull = "http://www.broker-cloud.eu/service-descriptions/CAS/categories#";

	Map<String, String> catRecord = new HashMap<String, String>();

	public List<String> getCategories() {
		this.init();
		Set<String> resultSet = new HashSet<String>();
		resultSet.addAll(catRecord.values());
		List<String> result = new ArrayList<String>();
		result.addAll(resultSet);
		return result;
	}

	@Override
	public List<String> getServices(String category) {
		List<String> result = new ArrayList<String>();
		for (String s : catRecord.keySet()) {
			if (catRecord.get(s).equals(category)) {
				result.add(s);
			}
		}
		return result;
	}

	public static String resolveService(String s) {
		String result = s;
		if (s.startsWith(servicePrefixFull)) {
			result = s.split("#")[1];
		} else if (s.startsWith(servicePrefix)) {
			result = s.split(":")[1];
		} else
			throw new RuntimeException(
					"service name should start with <http://www.broker-cloud.eu/service-descriptions/CAS/service-provider> or <sp:>");
		return result;
	}

	void init() {
		catRecord.clear();
		String q = "SELECT ?service" + "\n" + "WHERE\n" + "  {\n" + "    ?service a usdl-core:Service, cas:App; \n"
				+ "  }";
		try {
			Collection mBindings = SparqlQuery.INSTANCE.queryToJsonResults(q);
			for (Object x : mBindings) {
				Map m = (Map) x;
				Map service = (Map) m.get("service");
				String serviceName = (service.get("value").toString());
				serviceName = resolveService(serviceName);
				String catName = serviceName.substring(0, serviceName.length() - 1);
				catRecord.put(serviceName, catName);
			}

		} catch (Exception e) {
			throw new RuntimeException("Wrong query or results", e);
		}

		q = "SELECT ?fc" + "\n" + "WHERE\n" + "  {\n" + "    ?service usdl-core-cb:hasServiceModel ?model. \n"
				+ "	 ?model usdl-core-cb:hasClassificationDimension ?fc \n" + "  }";

		try {
			Collection mBindings = SparqlQuery.INSTANCE.queryToJsonResults(q);
			for (Object x : mBindings) {
				Map m = (Map) x;
				Map service = (Map) m.get("fc");
				String fcName = (service.get("value").toString());
				fcName = resolveFc(fcName);
				catRecord.put(fcName, fcDimention);
			}

		} catch (Exception e) {
			throw new RuntimeException("Wrong query or results", e);
		}
	}

	public static String resolveFc(String s) {
		String result = s;
		if (s.startsWith(fcPrefixFull)) {
			result = s.split("#")[1];
		} else if (s.startsWith(fcPrefix)) {
			result = s.split(":")[1];

		} else
			throw new RuntimeException(
					"service name should start with <http://www.broker-cloud.eu/service-descriptions/CAS/service-provider> or <sp:>");
		return fcDimention + result;
	}

}
