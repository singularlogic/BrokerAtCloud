package diva.rest.input.abstracts;

import java.util.Collection;

import diva.rest.input.json.AdaptRuleJson;

public abstract class AdaptRule {
	
	public static AdaptRule INSTANCE = new AdaptRuleJson();

	public AdaptRule() {
		super();
	}

	public abstract int getPriority(String name, String property);

	public abstract String getRule(String name);

	public abstract Collection<String> allRuleNames();

}