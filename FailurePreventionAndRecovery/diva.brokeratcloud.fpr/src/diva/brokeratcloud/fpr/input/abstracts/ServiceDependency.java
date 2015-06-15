package diva.brokeratcloud.fpr.input.abstracts;

import java.util.List;

import diva.brokeratcloud.fpr.input.json.ServiceDependencyJson;
import diva.brokeratcloud.fpr.input.local.ServiceDependencyLocal;
import diva.brokeratcloud.fpr.input.sparql.ServiceDependencySparql;

public abstract class ServiceDependency {
	
	public static ServiceDependency INSTANCE = new ServiceDependencySparql();

	public ServiceDependency() {
		super();
	}

	public abstract List<String> getDependency(String srv);

}