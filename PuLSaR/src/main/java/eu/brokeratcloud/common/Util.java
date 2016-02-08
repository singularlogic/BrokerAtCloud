/*
 * #%L
 * Preference-based cLoud Service Recommender (PuLSaR) - Broker@Cloud optimisation engine
 * %%
 * Copyright (C) 2014 - 2016 Information Management Unit, Institute of Communication and Computer Systems, National Technical University of Athens
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.brokeratcloud.common;

public class Util {
	public static boolean checkBoolean(String value, boolean defValue) {
		if (value==null || value.trim().equals("")) return defValue;
		value = value.trim().toLowerCase();
		if (value.equals("yes") || value.equals("true") || value.equals("on") || value.equals("1")) return true;
		if (value.equals("no") || value.equals("false") || value.equals("off") || value.equals("0")) return false;
		return defValue;
	}
}