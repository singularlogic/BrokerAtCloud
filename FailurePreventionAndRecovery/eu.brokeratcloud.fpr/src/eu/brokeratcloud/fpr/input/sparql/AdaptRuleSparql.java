package eu.brokeratcloud.fpr.input.sparql;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.brokeratcloud.fpr.input.abstracts.AdaptRule;

public class AdaptRuleSparql extends AdaptRule {
	private Map<String, Map<String, String>> fakedRepo = new HashMap<String, Map<String, String>>();

	private void initFake() {
		Map<String, String> ms = new HashMap<String, String>();

		ms.put("rule", "ForCustomer");
		ms.put("hasExtensibility", "0");
		ms.put("hasAdaptability", "1");
		ms.put("hasEaseOfDoingBusiness", "5");
		ms.put("hasSuitability", "3");
		ms.put("hasPortability", "1");
		ms.put("hasRecoverability", "1");
		ms.put("hasLearnability", "5");
		ms.put("hasAccessibility", "2");
		ms.put("hasOperability", "1");
		ms.put("Failure", "1");
		fakedRepo.put("RuleForCustomer", ms);

		ms = new HashMap<String, String>();
		ms.put("rule", "ForDeveloper");
		ms.put("hasExtensibility", "3");
		ms.put("hasAdaptability", "2");
		ms.put("hasEaseOfDoingBusiness", "0");
		ms.put("hasSuitability", "2");
		ms.put("hasPortability", "2");
		ms.put("hasRecoverability", "2");
		ms.put("hasLearnability", "2");
		ms.put("hasAccessibility", "2");
		ms.put("hasOperability", "5");
		ms.put("Failure", "1");
		fakedRepo.put("RuleForDeveloper", ms);

		ms = new HashMap<String, String>();
		ms.put("rule", "ForAdministrator");
		ms.put("hasExtensibility", "1");
		ms.put("hasAdaptability", "5");
		ms.put("hasEaseOfDoingBusiness", "0");
		ms.put("hasSuitability", "0");
		ms.put("hasPortability", "5");
		ms.put("hasRecoverability", "1");
		ms.put("hasLearnability", "4");
		ms.put("hasAccessibility", "0");
		ms.put("hasOperability", "1");
		ms.put("Failure", "1");
		fakedRepo.put("ForAdministrator", ms);

		ms = new HashMap<String, String>();
		ms.put("rule", "ForPotenial");
		ms.put("hasExtensibility", "5");
		ms.put("hasAdaptability", "3");
		ms.put("hasEaseOfDoingBusiness", "1");
		ms.put("hasSuitability", "0");
		ms.put("hasPortability", "0");
		ms.put("hasRecoverability", "1");
		ms.put("hasLearnability", "0");
		ms.put("hasAccessibility", "1");
		ms.put("hasOperability", "1");
		ms.put("Failure", "1");
		fakedRepo.put("ForPotenial", ms);
	}

	@Override
	public List<String> involvedContext() {
		return Arrays.asList("ForCustomer", "ForDeveloper", "ForAdministrator", "ForPotenial");
	}

	public AdaptRuleSparql() {
		initFake();
	}

	@Override
	public Collection<String> allRuleNames() {
		return fakedRepo.keySet();
	}

	@Override
	public String getRule(String name) {
		return fakedRepo.get(name).get("rule");
	}

	@Override
	public int getPriority(String name, String property) {
		String res = fakedRepo.get(name).get(property);
		if (res == null)
			return 1;
		return Integer.parseInt(res);
	}

}
