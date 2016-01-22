package eu.brokeratcloud.fpr.resources;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import diva.Configuration;
import diva.Score;
import eu.brokeratcloud.fpr.input.abstracts.ConsumerProfile;
import eu.brokeratcloud.fpr.input.abstracts.ServiceDependency;
import eu.brokeratcloud.fpr.input.json.ConsumerProfileJson;
import eu.brokeratcloud.fpr.input.sparql.ServiceCategorySparql;
import eu.brokeratcloud.fpr.model.ConfigurationsPool;
import eu.brokeratcloud.fpr.model.DivaRoot;
import eu.brokeratcloud.fpr.model.RecommendationHistory;
import eu.brokeratcloud.fpr.model.Repository;
import eu.brokeratcloud.fpr.model.RecommendationHistory.HistoryItem;

/**
 * 
 * @author Hui Song
 *
 */
@Path("fpr/recommendations/")
@Produces(MediaType.APPLICATION_JSON)
public class Recommendation {
	@GET
	public Object getRoot() {
		return Arrays.asList("hello", "world");
	}

	/**
	 * curl http://127.0.0.1:8089/fpr/recommendations/sc/abc/profile/001
	 * 
	 * @param scId
	 * @param profileId
	 * @return
	 */
	@Path("sc/{scId}/profile/{profileId}/")
	@GET
	public List<String> getRecommList(@PathParam("scId") String scId, @PathParam("profileId") String profileId) {

		String combinedId = scId + "-" + profileId;
		DivaRoot root = Repository.mainRoot.fork();
		root.updateOnRequest(scId, profileId);
		Repository.registerRoot(combinedId, root);
		root.runSimulation();
		try {
			// This is odd: without this sleep, curl gets a "empty result", even
			// if res is not null
			Thread.sleep(10);
		} catch (Exception e) {
			// This sleep seems to be interrupted every time (by whom I don't
			// know).
		}
		List<String> res = root.getConfigurationPool().queryScProfile(scId, profileId);

		return res;
	}

	/**
	 * curl http://127.0.0.1:8089/fpr/recommendations/sc/abc/profile/001/full
	 * 
	 * @param scId
	 * @param profileId
	 * @return
	 */
	@Path("sc/{scId}/profile/{profileId}/full")
	@GET
	public Map getRecommListFull(@PathParam("scId") String scId, @PathParam("profileId") String profileId) {
		Map<String, Object> res = new TreeMap<String, Object>();
		List<String> lst = this.getRecommList(scId, profileId);
		for (String s : lst) {
			Object singleConfig = getRecommConfig(s);
			if (!res.containsValue(singleConfig))
				res.put(s, getRecommConfig(s));
		}
		return res;
	}

	/**
	 * curl
	 * http://127.0.0.1:8089/fpr/recommendations/sc/abc/profile/001/provider
	 * 
	 * @param scId
	 * @param profileId
	 * @return
	 */
	@Path("sc/{scId}/profile/{profileId}/provider")
	@GET
	public Object getRecommListForProvider(@PathParam("scId") String scId, @PathParam("profileId") String profileId) {
		try {
			Map<String, Object> res = new TreeMap<String, Object>();

			String combinedId = scId + "-" + profileId;
			DivaRoot root = Repository.mainRoot.fork();
			root.updateOnRequest(scId, profileId);
			Repository.registerRoot(combinedId, root);
			root.runSimulation(true);
			try {
				// This is odd: without this sleep, curl gets a "empty result",
				// even if res is not null
				Thread.sleep(10);
			} catch (Exception e) {
				// This sleep seems to be interrupted every time (by whom I
				// don't know).
			}
			List<String> lst = root.getConfigurationPool().queryScProfile(scId, profileId);

			for (String s : lst) {
				res.put(s, getRecommConfig(s));
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return "wrong";
		}
	}

	/*
	 * curl http://127.0.0.1:8089/fpr/recommendations/abc-001--0/config This
	 * query should be invoked after one {@link #getRecommList} invocation,
	 * otherwise there is no "abc-001--0" id.
	 * 
	 */
	@Path("{recommId}/config")
	@GET
	public Object getRecommConfig(@PathParam("recommId") String recommId) {
		try {
			List<String> res = new ArrayList<String>();

			ConfigurationsPool pool = this.getConfigPool(recommId);

			Collection<String> configured = pool.getConfNames(recommId);
			Collection<String> used = ConsumerProfile.INSTANCE.getCurrentServices(this.getUserProfileId(recommId));

			Set<String> toAdd = new HashSet<String>(configured);
			Set<String> toRemove = new HashSet<String>(used);

			toAdd.removeAll(used);
			toRemove.removeAll(configured);

			for (String s : toAdd) {
				res.add("+" + s);
			}

			for (String s : toRemove) {
				res.add("-" + s);
			}

			Map<String, Object> finalRes = new HashMap<String, Object>();
			finalRes.put("recommendation", res);
			// List<String> allReasons = Arrays.asList("cpuOverload",
			// "memoryOverload");
			// List<String> reasons = new ArrayList<String>();
			// for(String i : allReasons){
			// if(Boolean.TRUE.equals(ConsumerProfileJson.INSTANCE.publicStatus.get(i)))
			// reasons.add(i);
			// }
			finalRes.put("reasons", PubSub.latestReasons);

			// res.add("Score: "+pool.getConf(recommId).getTotalScore());
			pool.addQueriedString(recommId + " - " + finalRes.toString());

			return finalRes;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * curl http://127.0.0.1:8089/fpr/recommendations/abc-001--0/reason
	 * 
	 * @param recommId
	 * @return
	 */
	@Path("{recommID}/reason")
	@GET
	public Object getRecommReason(@PathParam("recommID") String recommId) {
		try {
			List<String> res = new ArrayList<String>();
			Configuration config = this.getConfigPool(recommId).getConf(recommId);
			res.add("Total: " + String.valueOf(config.getTotalScore()));
			for (Score score : config.getScore()) {
				res.add(score.toString());
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * curl http://127.0.0.1:8089/fpr/recommendations/abc-001--0/response
	 * 
	 * @param recommId
	 * @return
	 */
	@Path("{recommID}/response")
	@GET
	public Object getResponse(@PathParam("recommID") String recommId) {
		System.out.println("here " + "recommID");
		return this.getConfigPool(recommId).getResponse(recommId);
	}

	/**
	 * curl -X PUT -d "yes"
	 * http://127.0.0.1:8089/fpr/recommendations/abc-001--0/response
	 * 
	 * @param recommId
	 * @param accepted
	 */
	@Path("{recommID}/response/")
	@PUT
	public void putResponse(@PathParam("recommID") String recommId, String accepted) {
		System.out.println("here " + recommId + accepted);
		this.getConfigPool(recommId).setResponse(recommId, "yes".equals(accepted));
	}

	@Path("history")
	@GET
	public Object getHistory() {
		List<Object> res = new LinkedList<Object>();

		for (DivaRoot root : Repository.historyRoots) {

			Map<String, Object> item = new LinkedHashMap<String, Object>();

			item.put("request", root.getCombinedId());
			List<String> configs = new LinkedList<String>();
			ConfigurationsPool pool = root.getConfigurationPool();
			for (String id : pool.listAllQueried()) {
				configs.add(id);
			}

			item.put("configs", configs);
			item.put("time", root.getTimeQueried().toLocaleString());
			item.put("responses", pool.getFullResponseRepr());

			res.add(item);
		}

		return res;
	}

	@Path("recommended")
	@GET
	public Object getRecommended(@QueryParam("service") String service, @QueryParam("timestamp") String timestamp) {
		String decodedService = service;
		try {
			decodedService = java.net.URLDecoder.decode(service, "UTF-8").trim();
			System.out.println(decodedService);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		List<String> result = new ArrayList<String>();
		try {
			Date date = fmt.parse(timestamp);
			List<HistoryItem> items = RecommendationHistory.INSTANCE.after(date);
			for (HistoryItem item : items) {
				if (item.removed.contains(decodedService) && !result.contains(item.customer))
					result.add(item.customer);
			}
			Map<String, List<String>> finalResult = new HashMap<String, List<String>>();
			finalResult.put("recommended", result);
			return finalResult;
		} catch (ParseException e) {
			return String.format("%s is not a valid W3C datetime representation", timestamp);
		}

	}

	@Path("query")
	@GET
	public Object getRecommendationQuery(@QueryParam("consumer") String consumer,
			@QueryParam("service") List<String> services) {

		Map<String, Object> result = new TreeMap<String, Object>();
		result.put("consumer", consumer);
		List<String> add = new ArrayList<String>();
		List<String> remove = new ArrayList<String>();

		List<String> cleanServices = new ArrayList<String>();
		for (String srv : services) {
			cleanServices.add(ServiceCategorySparql.resolveService(srv));
		}
		DivaRoot root = Repository.mainRoot.fork();
		List<String> res = root.getRecommQuery(consumer, cleanServices);
		System.out.println("Get result"+res.toString());
		try {
			// This is odd: without this sleep, curl gets a "empty result", even
			// if res is not null
			Thread.sleep(10);
		} catch (Exception e) {
			// This sleep seems to be interrupted every time (by whom I don't
			// know).
		}

		for (String s : cleanServices) {
			if (!res.contains(s))
				remove.add( s);
		}

		for (String s : res)
			if (!cleanServices.contains(s))
				add.add( s);

		System.out.println("Get added and removed"+add.toString()+remove.toString());
		// for(String s : services){
		// if(s.endsWith("1")){
		// String newservice = s.substring(0, s.length()-1)+"2";
		// if(!services.contains(newservice))
		// add.add(newservice);
		// remove.add(s);
		// }
		// }
		String nextQuery = "consumer="+consumer;
		for(String s : res){
			nextQuery = nextQuery + "&service=sp:"+s;
		}
		System.out.println("Get next query"+nextQuery);
		Map<String, Object> reason = new TreeMap<String, Object>();
		reason.put("cause",  PubSub.latestReasons);
		Map<String,List<String>> depends = new HashMap<String,List<String>>();
		for(String s : res){
			if(!cleanServices.contains(s)){
				boolean dep = false;
				for(String s1 : cleanServices){
					try{
					if(ServiceDependency.INSTANCE.getDependency(s1).contains(s)){
						if(depends.get(s1)==null){
							depends.put(s1, new ArrayList<String>());
						}
						depends.get(s1).add(s);
					}
					}
					catch(Exception e){
						//e.printStackTrace();
					}
				}
				
			}
		}
		System.out.println("Get dependencies"+depends);
		if(!depends.isEmpty()){
			reason.put("dependency", depends);
		}
		reason.put("failures", PubSub.failureRecords);
		
		result.put("add", add);
		result.put("remove", remove);
		result.put("reason", reason);
		result.put("nextQuery", nextQuery);
		
		RecommendationHistory.INSTANCE.addItemNow(add, remove, consumer);
		System.out.println("Get result"+result.toString());
//		if(true){
//			throw new RuntimeException("no reason");
//		}
		return result;
	}

	private ConfigurationsPool getConfigPool(String recommId) {
		String prefix = getUserProfileId(recommId);
		ConfigurationsPool pool = Repository.divaRoots.get(prefix).getConfigurationPool();
		return pool;
	}

	private String getUserProfileId(String recommId) {
		return recommId.split("--")[0];
	}

}
