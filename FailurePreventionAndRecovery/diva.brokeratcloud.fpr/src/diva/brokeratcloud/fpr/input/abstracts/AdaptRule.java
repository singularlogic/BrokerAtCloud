package diva.brokeratcloud.fpr.input.abstracts;

import java.util.Collection;

import diva.brokeratcloud.fpr.input.json.AdaptRuleJson;
import diva.brokeratcloud.fpr.input.local.AdaptRuleLocal;
import diva.brokeratcloud.fpr.input.sparql.AdaptRuleSparql;

public abstract class AdaptRule {
	
	public static AdaptRule INSTANCE = new AdaptRuleSparql();

	public AdaptRule() {
		super();
	}

	public abstract int getPriority(String name, String property);

	public abstract String getRule(String name);

	public abstract Collection<String> allRuleNames();

}