package eu.brokeratcloud.fpr.input.abstracts;

import java.util.List;

import eu.brokeratcloud.fpr.input.json.ServiceCategoryJson;
import eu.brokeratcloud.fpr.input.local.ServiceCategoryLocal;
import eu.brokeratcloud.fpr.input.sparql.ServiceCategorySparql;

public abstract class ServiceCategory {

	public static ServiceCategory INSTANCE = new ServiceCategorySparql();

	public ServiceCategory() {
		super();
	}

	public abstract List<String> getServices(String category);

	public abstract List<String> getCategories();

	public abstract List<String> getGroup(String service);

}