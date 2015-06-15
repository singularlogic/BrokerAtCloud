package diva.brokeratcloud.fpr.input.abstracts;

import java.util.List;

import diva.brokeratcloud.fpr.input.json.ServiceAttributeJson;
import diva.brokeratcloud.fpr.input.local.ServiceAttributeLocal;
import diva.brokeratcloud.fpr.input.sparql.ServiceAttributeSparql;

public abstract class ServiceAttribute {
	
	public static ServiceAttribute INSTANCE = new ServiceAttributeSparql();

	public ServiceAttribute() {
		super();
	}

	public abstract Object get(String service, String attribute);

	public abstract List<String> listCommonAttributes();

	public abstract List<String> listAttributes(String service);

}