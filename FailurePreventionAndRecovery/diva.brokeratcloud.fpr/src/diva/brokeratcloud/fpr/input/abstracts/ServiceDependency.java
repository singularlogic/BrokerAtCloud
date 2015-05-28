package diva.brokeratcloud.fpr.input.abstracts;

import java.util.List;

import diva.brokeratcloud.fpr.input.json.ServiceDependencyJson;
import diva.brokeratcloud.fpr.input.local.ServiceDependencyLocal;

public abstract class ServiceDependency {
	
	public static ServiceDependency INSTANCE = new ServiceDependencyLocal();

	public ServiceDependency() {
		super();
	}

	public abstract List<String> getDependency(String srv);

}