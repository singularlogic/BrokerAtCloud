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
package eu.brokeratcloud.util;

import java.util.Vector;

public class StatsImpl extends Stats {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("eu.brokeratcloud.stats");
	
	public StatsImpl() {
		startStats(1000);
	}
	public StatsImpl(int delay) {
		if (delay<100) throw new IllegalArgumentException(getClass().getName()+" : Statistics update delay cannot be less than 100ms. Value provided: delay="+delay);
		startStats(delay);
	}
	
	// Named Counter, Timer and Split variables and methods
	private int nCounter = 0;
	private int nTimer = 0;
	private int nSplit = 0;
	
	private int currCounter = -1;
	private int currTimer = -1;
	private int currSplit = -1;
	
	private Vector<String> counters = new Vector<String>();
	private Vector<String> timers = new Vector<String>();
	private Vector<String> splits = new Vector<String>();
	private Vector<Integer> overall = new Vector<Integer>();
	private Vector<String> overallType = new Vector<String>();
	
	private Vector<Long> _counters = new Vector<Long>();
	private Vector<Long> _timersStart = new Vector<Long>();
	private Vector<Long> _timers = new Vector<Long>();
	private Vector<Long> _splitsCount = new Vector<Long>();
	private Vector<Long> _splitsDur = new Vector<Long>();
	private Vector<Long> _splitsMax = new Vector<Long>();
	private Vector<Long> _splitsMin = new Vector<Long>();
	private Vector<Long> _splitsCurr = new Vector<Long>();
	
	public synchronized int nextCounter(String s) { counters.add(s); overall.add(nCounter); overallType.add("C"); _counters.add(0L); currCounter = nCounter; return nCounter++; }
	public synchronized int nextTimer(String s) { timers.add(s); overall.add(nTimer); overallType.add("T"); _timers.add(-1L); _timersStart.add(-1L); currTimer = nTimer; return nTimer++; }
	public synchronized int nextSplit(String s) { splits.add(s); overall.add(nSplit); overallType.add("S"); _splitsCount.add(0L); _splitsDur.add(0L); _splitsMax.add(-1L); _splitsMin.add(Long.MAX_VALUE); _splitsCurr.add(-1L); currSplit = nSplit; return nSplit++; }
	
	public int currentCounter() { return currCounter; }
	public int currentTimer() { return currTimer; }
	public int currentSplit() { return currSplit; }
	
	public synchronized long increase() { return increase(currCounter); }
	public synchronized long increase(String s) { int p = getCounterByName(s); if (p<0) p = nextCounter(s); return increase( p ); }
	public synchronized long increase(int cnt) {
		long newVal;
		_counters.set(cnt, newVal = _counters.get(cnt)+1);
		currCounter = cnt;
		return newVal;
	}
	public synchronized long decrease() { return decrease(currCounter); }
	public synchronized long decrease(String s) { int p = getCounterByName(s); if (p<0) p = nextCounter(s); return decrease( p ); }
	public synchronized long decrease(int cnt) {
		long newVal;
		_counters.set(cnt, newVal = _counters.get(cnt)-1);
		currCounter = cnt;
		return newVal;
	}
	
	public synchronized long startTimer() { return startTimer(currTimer); }
	public synchronized long startTimer(String s) { int p = getTimerByName(s); if (p<0) p = nextTimer(s); return startTimer( p ); }
	public synchronized long startTimer(int tmr) {
		long tm = System.nanoTime();
		_timersStart.set(tmr, tm);
		currTimer = tmr;
		return tm;
	}
	public synchronized long endTimer() { return endTimer(currTimer); }
	public synchronized long endTimer(String s) { int p = getTimerByName(s); if (p<0) return -1; return endTimer( p ); }
	public synchronized long endTimer(int tmr) {
		long tm = System.nanoTime();
		long dur = tm - _timersStart.get(tmr);
		_timers.set(tmr, dur);
		_timersStart.set(tmr, -1L);
		return tm;
	}
	
	public synchronized long startSplit() { return startSplit(currSplit); }
	public synchronized long startSplit(String s) { int p = getSplitByName(s); if (p<0) p = nextSplit(s); return startSplit( p ); }
	public synchronized long startSplit(int spl) {
		long tm = System.nanoTime();
		_splitsCurr.set(spl, tm);
		currSplit = spl;
		return tm;
	}
	public synchronized long endSplit() { return endSplit(currSplit); }
	public synchronized long endSplit(String s) { int p = getSplitByName(s); if (p<0) return -1; return endSplit( p ); }
	public synchronized long endSplit(int spl) {
		long tm = System.nanoTime();
		long dur = tm - _splitsCurr.get(spl);
		_splitsCount.set(spl, _splitsCount.get(spl)+1);
		_splitsDur.set(spl, _splitsDur.get(spl) + dur);
		if (dur > _splitsMax.get(spl)) _splitsMax.set(spl, dur);
		if (dur < _splitsMin.get(spl)) _splitsMin.set(spl, dur);
		_splitsCurr.set(spl, -1L);
		return tm;
	}
	
	public long getCounterValue(int cnt) { return _counters.get(cnt); }
	public long getTimerValue(int tmr) { return _timers.get(tmr); }
	public long getSplitCount(int spl) { return _splitsCount.get(spl); }
	public long getSplitDuration(int spl) { return _splitsDur.get(spl); }
	public long getSplitMax(int spl) { return _splitsMax.get(spl); }
	public long getSplitMin(int spl) { return _splitsMin.get(spl); }
	public long getSplitAverage(int spl) { return _splitsDur.get(spl) / _splitsCount.get(spl); }
	
	public int getCounterByName(String s) { return counters.indexOf(s); }
	public int getTimerByName(String s) { return timers.indexOf(s); }
	public int getSplitByName(String s) { return splits.indexOf(s); }
	public String getTypeByName(String s) { int p = overall.indexOf(s); if (p==-1) return null; else return overallType.get(p); }
	
	public int getOrCreateCounterByName(String s) { int p = counters.indexOf(s); if (p<0) p = nextCounter(s); return p; }
	public int getOrCreateTimerByName(String s) { int p = timers.indexOf(s); if (p<0) p = nextTimer(s); return p; }
	public int getOrCreateSplitByName(String s) { int p = splits.indexOf(s); if (p<0) p = nextSplit(s); return p; }
	
	// ---------------------------------------------------------------------------------------------------------------------------------
	
	protected Runtime runtime = Runtime.getRuntime();
	protected Object updateLock = new Object();
	protected int updateDelay = -1;
	protected boolean updateKeepRunning = false;
	protected java.lang.management.ThreadMXBean threadMxBean = java.lang.management.ManagementFactory.getThreadMXBean();
	protected com.sun.management.OperatingSystemMXBean osMxBean = (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
	
	protected long updateCount;
	protected long updateErrors;
	protected long totalMemSum;
	protected long freeMemSum;
	protected long usedMemSum;
	protected long maxMemSum;
	protected long maxMem;
	protected long minMem;
	protected long threadCountSum;
	protected long maxThreads;
	protected long totalThreads;
	protected int numOfCpus;
	protected double osLoadSum;
	protected double osLoadMax;
	protected double jvmLoadSum;
	protected double jvmLoadMax;
	
	public void startStats(int delay) {
		if (delay<=0) throw new IllegalArgumentException("Stats.startStats 'delay' argument must be positive number");
		updateDelay = delay;
		updateCount = 0;
		updateErrors = 0;
		totalMemSum = 0;
		freeMemSum = 0;
		usedMemSum = 0;
		maxMemSum = 0;
		maxMem = 0;
		minMem = Long.MAX_VALUE;
		threadCountSum = 0;
		maxThreads = 0;
		totalThreads = 0;
		numOfCpus = osMxBean.getAvailableProcessors();
		osLoadSum = 0;
		osLoadMax = 0;
		jvmLoadSum = 0;
		jvmLoadMax = 0;
		
		updateKeepRunning = true;
		Thread runner = new Thread() {
			public void run() {
				while (updateKeepRunning) {
					try { updateStats(); } catch (Exception e) { updateErrors++; logger.error("{}", e); }
					try { Thread.currentThread().sleep(updateDelay); } catch (InterruptedException e) {}
				}
			}
		};
		runner.setDaemon(true);
		runner.start();
	}
	
	public void stopStats() {
		updateKeepRunning = false;
	}
	
	protected void updateStats() {
		synchronized (updateLock) {
			updateCount++;
			
			long total = runtime.totalMemory();
			long free = runtime.freeMemory();
			long max = runtime.maxMemory();
			long used = total - free;
			
			totalMemSum += total;
			freeMemSum += free;
			usedMemSum += used;
			maxMemSum += max;
			if (used<minMem) minMem = used;
			if (used>maxMem) maxMem = used;
			
			threadCountSum += threadMxBean.getThreadCount();
			if (threadMxBean.getPeakThreadCount()>maxThreads) maxThreads = threadMxBean.getPeakThreadCount();
			totalThreads = threadMxBean.getTotalStartedThreadCount();
			
			double osLoad = osMxBean.getSystemCpuLoad();
			double jvmLoad = osMxBean.getProcessCpuLoad();
			osLoadSum += osLoad;
			jvmLoadSum += jvmLoad;
			if (osLoad>osLoadMax) osLoadMax = osLoad;
			if (jvmLoad>jvmLoadMax) jvmLoadMax = jvmLoad;
		}
	}
	
	public double getAvgTotalMemory() { return ((double)totalMemSum) / updateCount; }
	public double getAvgFreeMemory() { return ((double)freeMemSum) / updateCount; }
	public double getAvgUsedMemory() { return ((double)usedMemSum) / updateCount; }
	public double getAvgMaxMemory() { return ((double)maxMemSum) / updateCount; }
	public double getMaxMemory() { return maxMem; }
	public double getMinMemory() { return minMem; }
	public long getUpdateCount() { return updateCount; }
	public long getUpdateErrors() { return updateErrors; }
	public double getAvgThreadCount() { return ((double)threadCountSum) / updateCount; }
	public double getMaxThreadCount() { return maxThreads; }
	public double getTotalThreadCount() { return totalThreads; }
	public int getNumOfCpus() { return numOfCpus; }
	public double getAvgOsLoad() { return osLoadSum / updateCount; }
	public double getMaxOsLoad() { return osLoadMax; }
	public double getAvgJvmLoad() { return jvmLoadSum / updateCount; }
	public double getMaxJvmLoad() { return jvmLoadMax; }
	
	public double[] getStats(double[] stats, int toUnit) {
		int size = 16;
		if (stats==null) stats = new double[size];
		if (stats.length<size) throw new IllegalArgumentException("Stats.getStats 'stats' argument must be array of doubles of length at least 8");
		if (toUnit<=0) throw new IllegalArgumentException("Stats.getStats 'toUnit' argument must a positive number");
		synchronized (updateLock) {
			int i=0;
			stats[i++] = getAvgTotalMemory() / toUnit;
			stats[i++] = getAvgUsedMemory() / toUnit;
			stats[i++] = getAvgFreeMemory() / toUnit;
			stats[i++] = getAvgMaxMemory() / toUnit;
			stats[i++] = getMaxMemory() / toUnit;
			stats[i++] = getMinMemory() / toUnit;
			stats[i++] = getUpdateCount();
			stats[i++] = getUpdateErrors();
			
			stats[i++] = getAvgThreadCount();
			stats[i++] = getMaxThreadCount();
			stats[i++] = getTotalThreadCount();
			
			stats[i++] = getNumOfCpus();
			stats[i++] = getAvgOsLoad();
			stats[i++] = getMaxOsLoad();
			stats[i++] = getAvgJvmLoad();
			stats[i++] = getMaxJvmLoad();
		}
		return stats;
	}
	public String getStatsAsString(double[] stats, int toUnit, String unit) {
		stats = getStats(stats, toUnit);
		return String.format(java.util.Locale.US, "Avg. Total Mem: %.3f%s, Avg Used Mem: %.3f%s, Avg Free Mem: %.3f%s, Avg Max Mem: %.3f%s, Max Mem: %.3f%s, Min Mem: %.3f%s, Update Count: %d, Update Errors: %d, Avg. Active Threads: %.3f, Max Threads: %d, Total Threads: %d, CPUs: %d, Avg. OS Load: %.3f, Max OS Load: %.3f, Avg. JVM Load: %.3f, Max JVM Load: %.3f", 
						stats[0], unit, stats[1], unit, stats[2], unit, stats[3], unit, stats[4], unit, stats[5], unit, (long)stats[6], (long)stats[7],
						stats[8], (long)stats[9], (long)stats[10], (int)stats[11], stats[12], stats[13], stats[14], stats[15]
					);
	}
	public String getStatsCSVHeader(String sep) {
		return String.format(java.util.Locale.US, "Avg. Total Mem%sAvg Used Mem%sAvg Free Mem%sAvg Max Mem%sMax Mem%sMin Mem%sUpdate Count%sUpdate Errors%sAvg Active Threads%sMax Threads%sTotal Threads%sCPUs%sAvg OS Load%sMax OS Load%sAvg JVM Load%sMax JVM Load", 
						sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep
					);
	}
	public String getStatsAsCSV(double[] stats, int toUnit, String sep) {
		stats = getStats(stats, toUnit);
		return String.format(java.util.Locale.US, "%.3f%s%.3f%s%.3f%s%.3f%s%.3f%s%.3f%s%d%s%d%s%.3f%s%d%s%d%s%d%s%.3f%s%.3f%s%.3f%s%.3f", 
						stats[0], sep, stats[1], sep, stats[2], sep, stats[3], sep, stats[4], sep, stats[5], sep, (long)stats[6], sep, (long)stats[7], sep, 
						stats[8], sep, (long)stats[9], sep, (long)stats[10], sep, (int)stats[11], sep, stats[12], sep, stats[13], sep, stats[14], sep, stats[15]
					);
	}
	
	public void printAll(java.io.PrintStream out) {
		out.println("************************");
		out.println("** Statistics - BEGIN **");
		for (int i=0, n=overall.size(); i<n; i++) {
			int idx = overall.get(i);
			String type = overallType.get(i);
			long c, d;
			if ("C".equals(type)) { out.println(String.format("COUNTER: %s: %d", counters.get(idx), _counters.get(idx))); }
			else if ("T".equals(type)) { out.println(String.format("TIMER: %s: %f", timers.get(idx), _timers.get(idx)/nanoToSec)); }
			else if ("S".equals(type)) { c=_splitsCount.get(idx); d=_splitsDur.get(idx); double v = ((double)d)/((double)c);
					out.println(String.format("SPLIT: %s: count=%d, sum=%f, avg=%f, max=%f, min=%f", splits.get(idx), c, d/nanoToSec, v/nanoToSec, _splitsMax.get(idx)/nanoToSec, _splitsMin.get(idx)/nanoToSec)); }
		}
		out.println("************************");
		out.println("** Memory statistics  **");
		out.println( getStatsAsString(null, Stats.memToMb, "mb") );
		out.println("** Statistics - END   **");
		out.println("************************");
	}
}