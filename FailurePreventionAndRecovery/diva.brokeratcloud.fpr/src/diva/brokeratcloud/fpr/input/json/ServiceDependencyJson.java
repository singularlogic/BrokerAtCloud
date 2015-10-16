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
package diva.brokeratcloud.fpr.input.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import diva.brokeratcloud.fpr.input.abstracts.ServiceDependency;

public class ServiceDependencyJson extends ServiceDependency {

	public static ServiceDependencyJson INSTANCE = new ServiceDependencyJson();

	private Map<String, List<String>> fakeRepo = new HashMap<String, List<String>>();

	private void initFake() {

	}

	public ServiceDependencyJson() {
		initFake();
	}

	@Override
	public List<String> getDependency(String srv) {
		List<String> result = new ArrayList<String>();
		Map<String, Object> offering = JsonRoot.INSTANCE.getOffering(srv);

		List<Map<String, Object>> deps = (List<Map<String, Object>>) offering.get(JsonRoot.ServiceDependencies);
		if (deps == null)
			return result;
		for (Map<String, Object> dep : deps) {
			result.add(dep.get(JsonRoot.ServiceInstanceReferenceName).toString());
		}
		return result;
	}
}
