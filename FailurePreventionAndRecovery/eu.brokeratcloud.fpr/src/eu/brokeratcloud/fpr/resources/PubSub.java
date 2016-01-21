package eu.brokeratcloud.fpr.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import eu.brokeratcloud.fpr.input.abstracts.AdaptRule;
import eu.brokeratcloud.fpr.input.sparql.ServiceCategorySparql;
import eu.brokeratcloud.fpr.model.DivaRoot;
import eu.brokeratcloud.fpr.model.Repository;

@Path("subscriptions/")
@Produces(MediaType.APPLICATION_JSON)
public class PubSub {
	
	public static Map<String, List<String>> failureRecords = new HashMap<String, List<String>>();

	protected static String latestReasons = "";

	private List<String> allcontexts = Arrays.asList("cpuOverload", "memoryOverload");

	private final String cepId = "fprcep";

	@POST
	@Path("/cep/" + cepId)
	public Object cepEvent(String event) {
		ObjectMapper mapper = new ObjectMapper();
		Object e;
		try {
			e = mapper.readValue(event, Map.class);
//			if(((Map) e).keySet().size()==1){
//				e = ((Map) e).values().iterator().next();
//			}
				
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "Wrong event format";
		}
		DivaRoot root = Repository.mainRoot;
		String result = "results:";
		for (Object entry : ((Map) e).entrySet()) {

			String key = ((Map.Entry) entry).getKey().toString();

			Map value = (Map) ((Map.Entry) entry).getValue();
			
			String serviceName = null;
			if(value.containsKey("Service"))
				serviceName = value.get("Service").toString();
			else if(value.containsKey("Offering"))
				serviceName = value.get("Offering").toString();
			 
			
			String causesText = (String) value.get("Cause");

			latestReasons = causesText;

			String[] causesArray = causesText.split(",");
			List<String> causesList = new ArrayList<String>();
			for (int i = 0; i < causesArray.length; i++)
				causesList.add(causesArray[i].trim());

			String cleanServiceName = ServiceCategorySparql.resolveService(serviceName);
			result = result + "\n" + root.updateFailureLikelihood(cleanServiceName, key);
			
			for(List<String> arr : failureRecords.values()){
				arr.remove(cleanServiceName);
			}
			
			if(!failureRecords.containsKey(key)){
				failureRecords.put(key, new ArrayList<String>());
			}
			failureRecords.get(key).add(cleanServiceName);
			
//			for (String s : allcontexts) {
//				if (causesList.contains(s))
//					result = result + "\n" + root.updateFailureLikelihood(s, "true");
//				else
//					result = result + "\n" + root.updateFailureLikelihood(s, "recovered");
//
//			}
			DivaRoot.environment.clear();
			List<String> involved = AdaptRule.INSTANCE.involvedContext();
			for(String cause: causesList){
				if(involved.contains(cause)){
					DivaRoot.environment.add(cause);
				}
			}

		}
		return result;
	}

	@GET
	@Path("/cep/ping")
	public String getPing() {
		return "pong";
	}
}
