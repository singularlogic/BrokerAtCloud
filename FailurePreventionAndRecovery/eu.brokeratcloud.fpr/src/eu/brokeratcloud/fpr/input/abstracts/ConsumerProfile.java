package eu.brokeratcloud.fpr.input.abstracts;

import java.util.Collection;

import eu.brokeratcloud.fpr.input.json.ConsumerProfileJson;
import eu.brokeratcloud.fpr.input.local.ConsumerProfileLocal;
import eu.brokeratcloud.fpr.input.sparql.ConsumerProfileSparql;

public abstract class ConsumerProfile {

	public static ConsumerProfile INSTANCE = new ConsumerProfileSparql();

	public ConsumerProfile() {
		super();
	}

	public abstract Collection<String> getCurrentServices(String consumerProfile);

	public abstract Collection<String> getCurrentServices(String consumer, String profile);

	protected abstract String combineIds(String consumer, String profile);

	public abstract Object getRequired(String consumer, String profile);

}