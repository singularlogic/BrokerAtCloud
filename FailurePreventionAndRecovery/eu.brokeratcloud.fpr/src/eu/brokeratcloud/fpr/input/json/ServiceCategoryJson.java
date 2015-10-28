/**
 * Copyright 2014 SINTEF <brice.morin@sintef.no>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.brokeratcloud.fpr.input.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.brokeratcloud.fpr.input.abstracts.ServiceCategory;

public class ServiceCategoryJson extends ServiceCategory {

	public List<String> prefixes = Arrays.asList("Small", "Medium", "Large");

	public List<String> profilePrefix = Arrays.asList("Monolithic", "Clustered", "Elastic", "SingleInstance", "Metered",
			"Unmetered");

	public List<String> categories = new ArrayList<String>();
	public Map<String, String> subCategories = new HashMap<String, String>();

	public static ServiceCategoryJson INSTANCE = new ServiceCategoryJson();

	public Map<String, List<String>> fakeRepo = new HashMap<String, List<String>>();

	public ServiceCategoryJson() {
		init();
	}

	// private void initFake(){
	// fakeRepo.put("Map",
	// Arrays.asList("GoogleMap", "BingMap", "AppleMap")
	// );
	// fakeRepo.put("PubTrans",
	// Arrays.asList("Metro")
	// );
	// fakeRepo.put("RoutePlan",
	// Arrays.asList("FakeRouter", "ToyRouter")
	// );
	// }

	private void init() {
		this.categories = Arrays.asList("OrbiServiceModel", "DBServiceModel");
		this.subCategories = new HashMap<String, String>();
		this.subCategories.put("GoldenOrbi", "OrbiServiceModel");
		this.subCategories.put("MonolithicDB", "DBServiceModel");
	}

	private void original_init() {
		try {
			String query = "SELECT ?x\n" + "WHERE\n" + "{\n"
					+ "	?x rdfs:isDefinedBy <http://www.broker-cloud.eu/Singular-OrbiOffering/brokerpolicy> .\n"
					+ "	?x rdfs:subClassOf usdl-core:ServiceModel ." + "}";

			Collection mBindings = SparqlRoot.INSTANCE.queryToJsonResults(query);
			for (Object x : mBindings) {
				Map m = (Map) x;
				Map service = (Map) m.get("x");
				String[] splitted = service.get("value").toString().split("#");
				String catName = splitted[1];
				categories.add(catName);
			}

			query = "SELECT ?x ?y\n" + "WHERE\n" + "{\n"
					+ "	?x rdfs:isDefinedBy <http://www.broker-cloud.eu/Singular-OrbiOffering/brokerpolicy> .\n"
					+ "	?policy rdfs:subPropertyOf usdl-sla:hasServiceLevelProfile .\n"
					+ "   ?policy rdfs:domain ?x .\n" + "   ?policy rdfs:range ?y .\n" + "}";
			mBindings = SparqlRoot.INSTANCE.queryToJsonResults(query);
			for (Object x : mBindings) {
				Map m = (Map) x;
				Map catMap = (Map) m.get("x");
				String[] splitted = catMap.get("value").toString().split("#");
				String catName = splitted[1];
				Map slpMap = (Map) m.get("y");
				String slpName = slpMap.get("value").toString().split("#")[1];

				subCategories.put(slpName, catName);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public List<String> getCategories() {

		return new ArrayList<String>(this.categories);

		// Set<String> cats = new HashSet<String>();

		// for(Object i : JsonRoot.INSTANCE.offerings){
		// Map<String, Object> m = (Map<String,Object>) i;
		// String profile = m.get(JsonRoot.ServiceLevelProfile).toString();
		// for(String s : this.profilePrefix){
		// if(profile.startsWith(s)){
		// profile = profile.substring(s.length());
		// break;
		// }
		//
		// }
		// cats.add(profile);
		// }
		// return new ArrayList<String>(cats);
	}

	@Override
	public List<String> getServices(String category) {
		Set<String> sers = new HashSet<String>();

		for (Object i : JsonRoot.INSTANCE.offerings) {
			Map<String, Object> m = (Map<String, Object>) i;
			if (category.equals(subCategories.get(m.get(JsonRoot.ServiceLevelProfile).toString())))
				sers.add(m.get(JsonRoot.OfferingName).toString());
		}
		return new ArrayList<String>(sers);
	}

	@Override
	public List<String> getGroup(String service) {
		List<String> result = new ArrayList<String>();
		Map<String, Object> offering = JsonRoot.INSTANCE.getOffering(service);
		if (offering == null)
			return result;
		String policy = offering.get(JsonRoot.ServiceLevelProfile).toString();
		for (Object i : JsonRoot.INSTANCE.offerings) {
			Map<String, Object> m = (Map<String, Object>) i;
			if (policy.equals(m.get(JsonRoot.ServiceLevelProfile)))
				result.add(m.get(JsonRoot.OfferingName).toString());
		}
		return result;

	}

}
