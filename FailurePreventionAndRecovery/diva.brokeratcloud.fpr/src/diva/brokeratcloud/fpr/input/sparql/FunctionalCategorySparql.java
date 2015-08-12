package diva.brokeratcloud.fpr.input.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import diva.brokeratcloud.fpr.input.local.ServiceCategoryLocal;

public class FunctionalCategorySparql extends ServiceCategoryLocal{
	
	public static final String categoryPrefix = "fc:";
	public static final String categoryPrefixFull = "http://www.broker-cloud.eu/service-descriptions/CAS/categories#";
	
	Map<String, List<String>> fcRecord = new HashMap<String, List<String>>();
	
	void init(){
		String q = 
				"SELECT ?service ?fc \n" +
				"WHERE\n" +
				"  {\n" +
				"     ?service cb:hasServiceModel ?model . \n" +
				"	  ?model cb:hasClassificationDimension ?fc \n" +
				"  }";
		try{
			Collection mBindings = SparqlQuery.INSTANCE.queryToJsonResults(q);
			for(Object x : mBindings){
				Map m = (Map) x;
				Map service = (Map)m.get("service");
				String serviceName = (service.get("value").toString()); 
				serviceName = ServiceCategorySparql.resolveService(serviceName);
				
				Map fc = (Map)m.get("fc");
				String fcName = (fc.get("value")).toString();
				fcName = resolveFc(fcName);
				
				if(fcRecord.get(serviceName)==null){
					fcRecord.put(serviceName, new ArrayList<String>());
					
				}
				
				fcRecord.get(serviceName).add(fcName);
			}
			
		}
		catch(Exception e){
			throw new RuntimeException("Wrong query or results", e);
		}
	}
	
	public List<String> getFunctionalCategories(){
		String q = 
				"SELECT ?fc \n" +
				"\n" +
				"WHERE\n" +
				"  {\n" +
				"    { ?service cb:hasServiceModel ?model ." +
				"	 { ?model cb:hasClassificationDimension ?fc:" +
				"    }\n" +
				"  }";
		Set<String> result = new HashSet<String>();
		try{
			
		}
		catch(Exception e){
			throw new RuntimeException("Wrong query or result", e);
		}
		List<String> finalResult = new ArrayList<String>();
		finalResult.addAll(result);
		return finalResult;
	}
	
	@Override
	public List<String> getServices(String category){
		String cateName = "";
		if(category.startsWith("fc:"))
			cateName = category;
		else if(category.startsWith("http://www.broker-cloud.eu/service-descriptions/CAS/categories#")){
			String name = category.split("#")[1];
			cateName = "fc:" + name;
		}
		else{
			throw new RuntimeException("category name should start with <fc:> or <http://www.broker-cloud.eu/service-descriptions/CAS/categories#");
		}
		
		String q = 
				"SELECT ?service \n" +
				"FROM NAMED <http://www.broker-cloud.eu/service-descriptions/CAS>\n" +
				"\n" +
				"WHERE\n" +
				"  {\n" +
				"    GRAPH ?src\n" +
				"    { ?service cas:hasFunctionalServiceCategory " + cateName + "\n" +
				"    }\n" +
				"  }";
		try{
			Collection mBindings = SparqlQuery.INSTANCE.queryToJsonResults(q);
			ArrayList<String> result = new ArrayList<String>();
			for(Object x : mBindings){
				Map m = (Map) x;
				Map service = (Map)m.get("service");
				result.add(service.get("value").toString()); 
			}
			return result;
		}
		catch(Exception e){
			throw new RuntimeException("Wrong query or results", e);
		}
	}
	
	public static String resolveFc(String s){
		String result = s;
		if(s.startsWith(categoryPrefixFull)){
			result = s.split("#")[1];
		}
		else if(s.startsWith(categoryPrefix)){
			result = s.split(":")[1];
			
		}
		else
			throw new RuntimeException("service name should start with <http://www.broker-cloud.eu/service-descriptions/CAS/service-provider> or <sp:>");
		return result;
	}

}
