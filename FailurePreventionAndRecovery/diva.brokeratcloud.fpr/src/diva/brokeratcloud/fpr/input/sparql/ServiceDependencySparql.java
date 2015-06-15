package diva.brokeratcloud.fpr.input.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import diva.brokeratcloud.fpr.input.local.ServiceDependencyLocal;

public class ServiceDependencySparql extends ServiceDependencyLocal {
	
	Map<String, List<String>> depRecord = new HashMap<String, List<String>>();
	
	@Override
	public List<String> getDependency(String srv){
		if(depRecord == null)
			init();
		return depRecord.get(srv);
	}
	
	void init(){
		depRecord = new HashMap<String, List<String>>();
		String q = 
				"SELECT ?service ?fc \n" +
				"WHERE\n" +
				"  {\n" +
				"     ?service usdl-core-cb:hasServiceModel ?model . \n" +
				"	  ?model usdl-core-cb:hasClassificationDimension ?fc \n" +
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
				fcName = ServiceCategorySparql.resolveFc(fcName);
				
				addDep(serviceName, fcName);
			}
			
		}
		catch(Exception e){
			throw new RuntimeException("Wrong query or results", e);
		}
		
		
		q = 
				"SELECT ?service ?dep \n" +
				"WHERE\n" +
				"  {\n" +
				"     ?service cb:dependsOn ?dep . \n" +
				"  }";
		
		try{
			Collection mBindings = SparqlQuery.INSTANCE.queryToJsonResults(q);
			for(Object x : mBindings){
				Map m = (Map) x;
				Map service = (Map)m.get("service");
				String serviceName = (service.get("value").toString()); 
				serviceName = ServiceCategorySparql.resolveService(serviceName);
				
				Map dep = (Map)m.get("dep");
				String depName = (dep.get("value")).toString();
				depName = ServiceCategorySparql.resolveFc(depName);
				
				addDep(serviceName, depName);
			}
			
		}
		catch(Exception e){
			throw new RuntimeException("Wrong query or results", e);
		}
		
		
		
	}
	
	void addDep(String service, String dep){
		if(depRecord.get(service)==null){
			depRecord.put(service, new ArrayList<String>());
			
		}
		
		depRecord.get(service).add(dep);
	}
}
