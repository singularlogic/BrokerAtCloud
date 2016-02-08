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
package eu.brokeratcloud.opt.engine;

import eu.brokeratcloud.common.SLMEvent;
import eu.brokeratcloud.opt.RecommendationManager;
import eu.brokeratcloud.util.Stats;

class LocalLoopEventManager extends EventManager {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("eu.brokeratcloud.event.manager.local");
	
	protected Configuration config;
	protected boolean online;
	protected RecommendationManager recomMgr;
	
	public LocalLoopEventManager() {
		recomMgr = RecommendationManager.getInstance();
	}
	
	// Used in simulations to set recom. manager thread pool size
	public RecommendationManager getRecommendationManager() { return recomMgr; }
	
	public Configuration getConfiguration() { return config; }
	public void setConfiguration(Configuration cfg) { config = cfg; }
	public void startManager() { setOnline(true); }
	public void stopManager() { setOnline(false); }
	public boolean isOnline() { return online; }
	protected void setOnline(boolean b) { online = b; }
	
	public void eventReceived(String evtText) {
		try {
			SLMEvent evt = SLMEvent.parseEvent(evtText);
			eventReceived(evt);
		} catch (Exception e) {
			logger.warn( "LocalLoopEventManager.eventReceived(String): Error while parsing SLM event: {}", e );
			e.printStackTrace();
		}
	}
	
	public void eventReceived(SLMEvent evt) {
		if (evt.getType()==null || evt.getType().trim().isEmpty()) {
			logger.warn("LocalLoopEventManager.eventReceived: Missing event type: Event-Id: {}", evt.getId());
			return;
		}
		
		// Reset timers
		eu.brokeratcloud.opt.engine.LocalLoopEventManager.resetTimers();
		eu.brokeratcloud.fuseki.FusekiClient.resetTimers();
		
		// Request recommendations
		String type = evt.getType().trim().toLowerCase();
		if ("service-onboarded".equals(type)) serviceOnboarded(evt);
		else if ("service-deprecated".equals(type)) serviceDepreciated(evt);
		else if ("service-updated".equals(type)) serviceUpdated(evt);
		else logger.warn("LocalLoopEventManager.eventReceived: Unknown event type: {}, Event-Id: {}", evt.getType(), evt.getId());
		
		// Get time measurements
		logTimers();
		logger.debug( "Time measurements: {}", getTimerStats() );
		
		double totalTimeLLEM = eu.brokeratcloud.opt.engine.LocalLoopEventManager.getTotalElapsedTime() / Stats.nanoToSec;
		double totalTimeFC = eu.brokeratcloud.fuseki.FusekiClient.getTotalElapsedTime() / Stats.nanoToSec;
		double totalTimeNet = totalTimeLLEM - totalTimeFC;
		double[] stats = Stats.get().getStats(null, Stats.memToMb);
		if (stats!=null && stats.length>15) {
			logger.debug( "LLEM Dur.\tFC Dur.\tPLS Dur.\tAvg. Mem\tMax Mem\tAvg Active Threads\tMax Threads\tTotal Threads\tCPUs\tAvg OS Load\tMax OS Load\tAvg Jvm Load\tMax Jvm Load" );
			logger.debug( "{}", String.format( java.util.Locale.forLanguageTag("el"), "%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t%d\t%d\t%d\t%.3f\t%.3f\t%.3f\t%.3f\n", 
				totalTimeLLEM, totalTimeFC, totalTimeNet, stats[1], stats[4], stats[8], (long)stats[9], (long)stats[10], (int)stats[11], stats[12], stats[13], stats[14], stats[15] ) );
		}
	}
	
	public void serviceOnboarded(SLMEvent evt) {
		long startTm = System.nanoTime();
		recomMgr.requestRecommendations(evt);
		sumElapsedTime( System.nanoTime()-startTm );
	}
	
	public void serviceDepreciated(SLMEvent evt) {
		long startTm = System.nanoTime();
		recomMgr.requestRecommendations(evt);
		sumElapsedTime( System.nanoTime()-startTm );
	}
	
	public void serviceUpdated(SLMEvent evt) {
		long startTm = System.nanoTime();
		recomMgr.requestRecommendations(evt);
		sumElapsedTime( System.nanoTime()-startTm );
	}
	
	public boolean publish(SLMEvent evt) {
		// Nothing to do
		return true;
	}
	
	// Time measurement methods and variables
	protected static long cntSplits = 0;
	protected static long sumElapsedTime = 0;
	protected static long minElapsedTime = Long.MAX_VALUE;
	protected static long maxElapsedTime = 0;
	protected static StringBuilder sbStats;
	
	protected static synchronized void sumElapsedTime( long elapsedTime ) {
		cntSplits++;
		sumElapsedTime += elapsedTime;
		if (minElapsedTime>elapsedTime) minElapsedTime = elapsedTime;
		if (maxElapsedTime<=elapsedTime) maxElapsedTime = elapsedTime;
	}
	
	public static long getSplitCount() { return cntSplits; }
	public static long getTotalElapsedTime() { return sumElapsedTime; }
	public static long getMinElapsedTime() { return minElapsedTime; }
	public static long getMaxElapsedTime() { return maxElapsedTime; }
	public static void resetTimers() { cntSplits = 0; sumElapsedTime = 0; minElapsedTime = Long.MAX_VALUE; maxElapsedTime = 0; }
	
	public static void logTimers() {
		if (sbStats==null) {
			sbStats = new StringBuilder();
			sbStats.append("TIMER:\tLLEM.splits\tLLEM.totalTime\tLLEM.minTime\tLLEM.maxTime\tFC.splits\tFC.totalTime\tFC.minTime\tFC.maxTimer\tNET.totalTime\tWT.totalTime\tWT.minTime\tWT.maxTime\tWT.avg-dur\tWT.completed\tWT.fails\n");
		}
		long t1, t2;
		long[] threadStats = RecommendationManager.getWorkerThreadStats();
		sbStats.append( 
			String.format("TIMER:\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\n",
							eu.brokeratcloud.opt.engine.LocalLoopEventManager.getSplitCount(),
							t1 = eu.brokeratcloud.opt.engine.LocalLoopEventManager.getTotalElapsedTime(),
							eu.brokeratcloud.opt.engine.LocalLoopEventManager.getMinElapsedTime(),
							eu.brokeratcloud.opt.engine.LocalLoopEventManager.getMaxElapsedTime(),
							eu.brokeratcloud.fuseki.FusekiClient.getSplitCount(),
							t2 = eu.brokeratcloud.fuseki.FusekiClient.getTotalElapsedTime(),
							eu.brokeratcloud.fuseki.FusekiClient.getMinElapsedTime(),
							eu.brokeratcloud.fuseki.FusekiClient.getMaxElapsedTime(),
							t1-t2,
							threadStats[0], threadStats[1], threadStats[2], threadStats[7],		// total, min, max, average duration
							threadStats[4], threadStats[5]										// num of threads completed, failed
			)
		);
	}
	
	public static String getTimerStats() {
		return sbStats.toString();
	}
}
