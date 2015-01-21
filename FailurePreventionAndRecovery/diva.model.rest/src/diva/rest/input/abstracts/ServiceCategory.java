package diva.rest.input.abstracts;

import java.util.List;

import diva.rest.input.json.ServiceCategoryJson;

public abstract class ServiceCategory {

	public static ServiceCategory INSTANCE = new ServiceCategoryJson();
	
	public ServiceCategory() {
		super();
	}

	public abstract List<String> getServices(String category);

	public abstract List<String> getCategories();
	
	public abstract List<String> getGroup(String service);

}