package diva.brokeratcloud.fpr.input.abstracts;

import java.util.List;

import diva.brokeratcloud.fpr.input.json.ServiceCategoryJson;
import diva.brokeratcloud.fpr.input.local.ServiceCategoryLocal;
import diva.brokeratcloud.fpr.input.sparql.ServiceCategorySparql;

public abstract class ServiceCategory {

	public static ServiceCategory INSTANCE = new ServiceCategorySparql();
	
	public ServiceCategory() {
		super();
	}

	public abstract List<String> getServices(String category);

	public abstract List<String> getCategories();
	
	public abstract List<String> getGroup(String service);

}