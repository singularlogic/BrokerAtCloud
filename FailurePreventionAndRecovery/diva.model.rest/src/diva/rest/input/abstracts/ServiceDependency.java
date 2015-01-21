package diva.rest.input.abstracts;

import java.util.List;

import diva.rest.input.json.ServiceDependencyJson;

public abstract class ServiceDependency {
	
	public static ServiceDependency INSTANCE = new ServiceDependencyJson();

	public ServiceDependency() {
		super();
	}

	public abstract List<String> getDependency(String srv);

}