package diva.brokeratcloud.fpr.input.abstracts;

import java.util.List;

import diva.brokeratcloud.fpr.input.json.ServiceDependencyJson;

public abstract class ServiceDependency {
	
	public static ServiceDependency INSTANCE = new ServiceDependencyJson();

	public ServiceDependency() {
		super();
	}

	public abstract List<String> getDependency(String srv);

}