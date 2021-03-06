package eu.brokeratcloud.fpr.input.abstracts;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import eu.brokeratcloud.fpr.input.json.AdaptRuleJson;
import eu.brokeratcloud.fpr.input.local.AdaptRuleLocal;
import eu.brokeratcloud.fpr.input.sparql.AdaptRuleSparql;

public abstract class AdaptRule {

	public static AdaptRule INSTANCE = new AdaptRuleSparql();

	public AdaptRule() {
		super();
	}

	public abstract int getPriority(String name, String property);

	public abstract String getRule(String name);

	public abstract Collection<String> allRuleNames();

	public List<String> involvedContext() {
		return Collections.EMPTY_LIST;
	}

}