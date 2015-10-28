package eu.brokeratcloud.fpr.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import eu.brokeratcloud.fpr.model.DivaRoot;
import eu.brokeratcloud.fpr.model.Repository;

@Path("subscriptions/")
@Produces(MediaType.APPLICATION_JSON)
public class PubSub {

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
			String serviceName = value.get("Service").toString();
			String causesText = (String) value.get("Cause");

			latestReasons = causesText;

			String[] causesArray = causesText.split(",");
			List<String> causesList = new ArrayList<String>();
			for (int i = 0; i < causesArray.length; i++)
				causesList.add(causesArray[i].trim());

			result = result + "\n" + root.updateFailureLikelihood(serviceName, key);
			for (String s : allcontexts) {
				if (causesList.contains(s))
					result = result + "\n" + root.updateFailureLikelihood(s, "true");
				else
					result = result + "\n" + root.updateFailureLikelihood(s, "recovered");

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
