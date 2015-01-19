package diva.rest.input.abstracts;

import java.util.List;

import diva.rest.input.json.ServiceAttributeJson;

public abstract class ServiceAttribute {
	
	public static ServiceAttribute INSTANCE = new ServiceAttributeJson();

	public ServiceAttribute() {
		super();
	}

	public abstract Object get(String service, String attribute);

	public abstract List<String> listCommonAttributes();

	public abstract List<String> listAttributes(String service);

}