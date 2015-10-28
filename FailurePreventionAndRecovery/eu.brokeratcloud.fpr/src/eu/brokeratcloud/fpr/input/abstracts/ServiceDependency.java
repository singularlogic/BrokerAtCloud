package eu.brokeratcloud.fpr.input.abstracts;

import java.util.Collections;
import java.util.List;

import eu.brokeratcloud.fpr.input.json.ServiceDependencyJson;
import eu.brokeratcloud.fpr.input.local.ServiceDependencyLocal;
import eu.brokeratcloud.fpr.input.sparql.ServiceDependencySparql;

public abstract class ServiceDependency {

	public static ServiceDependency INSTANCE = new ServiceDependencySparql();

	public ServiceDependency() {
		super();
	}

	public abstract List<String> getDependency(String srv);

	public boolean isAlternative(String srv) {
		return false;
	}

	public List<String> getRequirement(String srv) {
		return Collections.EMPTY_LIST;
	}

}