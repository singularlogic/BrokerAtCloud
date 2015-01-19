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
package diva.rest.input.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import diva.rest.input.abstracts.ServiceAttribute;

public class ServiceAttributeJson extends ServiceAttribute {

	public static ServiceAttributeJson INSTANCE = new ServiceAttributeJson();
	
	Random random = new Random();
	
	private Map<String, Object> fakedRepo = new HashMap<String, Object>();
	private Map<String, Double> fakeRange = new HashMap<String, Double>();
	
	private void initFake(){

		fakeRange.put("HTTPResponseTimeMin", 1000.0);
		fakeRange.put("HTTPResponseTimeMax", 3000.0);

		fakeRange.put("MemoryLoadMin", 90.0);
		fakeRange.put("MemoryLoadMax", 100.0);
		
		fakeRange.put("StorageLoadMin", 90.0);
		fakeRange.put("StorageLoadMax", 100.0);
		
		fakeRange.put("RequestPerSecondMin", 500000.0);
		fakeRange.put("RequestPerSecondMax", 1000000.0);

		fakeRange.put("UptimeMin", 0.0);
		fakeRange.put("UptimeMax", 100.0);
		
		fakeRange.put("ThreadsMin", 1.0);
		fakeRange.put("ThreadsMax", 10.0);
		
		fakeRange.put("QuestionsMin", 10000.0);
		fakeRange.put("QuestionsMax", 100000.0);
		
		fakeRange.put("SlowQueriesMin", 1000.0);
		fakeRange.put("SlowQueriesMax", 10000.0);
		
		fakeRange.put("AvgOfQueriesPecSecondMin", 1000.0);
		fakeRange.put("AvgOfQueriesPecSecondMax", 10000.0);
		
		fakeRange.put("TotalAccessesMin", 10000.0);
		fakeRange.put("TotalAccessesMax", 100000.0);
		
		fakeRange.put("CPUSpeedMin", 1000.0);
		fakeRange.put("CPUSpeedMax", 2000.0);

		fakeRange.put("CPUCoresMin", 1.0);
		fakeRange.put("CPUCoresMax", 8.0);

		fakeRange.put("CPULoadAvgPerCoreMin", 0.5);
		fakeRange.put("CPULoadAvgPerCoreMax", 1.0);

		fakeRange.put("MemoryFreeMin", 1000000000.0);
		fakeRange.put("MemoryFreeMax", 8000000000.0);

		fakeRange.put("MemoryTotalMin", 1000000000.0);
		fakeRange.put("MemoryTotalMax", 8000000000.0);
	
	}
	
	public ServiceAttributeJson(){
		initFake();
	}
	
	@Override
	public List<String> listAttributes(String service){
		return Collections.EMPTY_LIST;
	}
	
	public void printMinMax(){
		for(Object i : JsonRoot.INSTANCE.offerings){
			Map<String, Object> m = (Map<String, Object>) i;
			List<Map<String, Object>> varspace = (List<Map<String, Object>>) m.get(JsonRoot.VariableSpace);
			for(Map<String, Object> var : varspace){
				System.out.println(String.format(
						"fakeRange.put(\"%s\", %.1f);", 
						var.get(JsonRoot.VarName)+"Max",
						0.0)
					);
				System.out.println(String.format(
						"fakeRange.put(\"%s\", %.1f);", 
						var.get(JsonRoot.VarName)+"Min",
						0.0)
					);
				System.out.println("");
			}
		}
	}
	
	@Override
	public List<String> listCommonAttributes(){
		Set<String> vars = new HashSet<String>();
		for(Object i : JsonRoot.INSTANCE.offerings){
			Map<String, Object> m = (Map<String, Object>) i;
			List<Map<String, Object>> varspace = (List<Map<String, Object>>) m.get(JsonRoot.VariableSpace);
			for(Map<String, Object> var : varspace){
				vars.add(var.get(JsonRoot.VarName).toString());
			}
		}
		return new ArrayList<String>(vars);
	}
	

	
	
	@Override
	public Object get(String service, String attribute){
		Map<String, Object> offering = JsonRoot.INSTANCE.getOffering(service);
		List<Map<String, Object>> varspace = (List<Map<String, Object>>) offering.get(JsonRoot.VariableSpace);
		for(Map<String, Object> var : varspace){
			if(attribute.equals(var.get(JsonRoot.VarName))){
				boolean isAscent = false;
				Object value = var.get(JsonRoot.MaxAcceptableThreshold);
				if(value == null){
					value = var.get(JsonRoot.MinAcceptableThreshold);
					isAscent = true;
				}
				if(value != null)
					return convert(
							value.toString(), 
							var.get(JsonRoot.VarName).toString(),
							isAscent
						);
				
			}		
		}
		return 0;
	}
	
	private int convert(String value, String name, boolean isAscent){
		try{
			double d = Double.valueOf(value).doubleValue();
			double min = fakeRange.get(name+"Min");
			double max = fakeRange.get(name+"Max");
			if(isAscent)
				return (int) ((d - min) / (max - min) * 10);
			else 
				return (int) ((d - max) / (max - min) * 10);
		}
		catch(Exception e){
			//e.printStackTrace();
			return 0;
		}
		
	}
	
	
}


