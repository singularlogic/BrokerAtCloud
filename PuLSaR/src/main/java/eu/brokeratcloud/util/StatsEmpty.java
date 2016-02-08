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

public class StatsEmpty extends Stats {
	public synchronized int nextCounter(String s) { return -1; }
	public synchronized int nextTimer(String s) { return -1; }
	public synchronized int nextSplit(String s) { return -1; }
	
	public int currentCounter() { return -1; }
	public int currentTimer() { return -1; }
	public int currentSplit() { return -1; }
	
	public synchronized long increase() { return -1; }
	public synchronized long increase(String s) { return -1; }
	public synchronized long increase(int cnt) { return -1; }
	public synchronized long decrease() { return -1; }
	public synchronized long decrease(String s) { return -1; }
	public synchronized long decrease(int cnt) { return -1; }
	
	public synchronized long startTimer() { return -1; }
	public synchronized long startTimer(String s) { return -1; }
	public synchronized long startTimer(int tmr) { return -1; }
	public synchronized long endTimer() { return -1; }
	public synchronized long endTimer(String s) { return -1; }
	public synchronized long endTimer(int tmr) { return -1; }
	
	public synchronized long startSplit() { return -1; }
	public synchronized long startSplit(String s) { return -1; }
	public synchronized long startSplit(int spl) { return -1; }
	public synchronized long endSplit() { return -1; }
	public synchronized long endSplit(String s) { return -1; }
	public synchronized long endSplit(int spl) { return -1; }
	
	public long getCounterValue(int cnt) { return -1; }
	public long getTimerValue(int tmr) { return -1; }
	public long getSplitCount(int spl) { return -1; }
	public long getSplitDuration(int spl) { return -1; }
	public long getSplitMax(int spl) { return -1; }
	public long getSplitMin(int spl) { return -1; }
	public long getSplitAverage(int spl) { return -1; }
	
	public int getCounterByName(String s) { return -1; }
	public int getTimerByName(String s) { return -1; }
	public int getSplitByName(String s) { return -1; }
	public String getTypeByName(String s) { return null; }
	
	public int getOrCreateCounterByName(String s) { return -1; }
	public int getOrCreateTimerByName(String s) { return -1; }
	public int getOrCreateSplitByName(String s) { return -1; }
	
	// ---------------------------------------------------------------------------------------------------------------------------------
	
	public void startStats(int delay) {}
	public void stopStats() {}
	
	public double getAvgTotalMemory() { return -1; }
	public double getAvgFreeMemory() { return -1; }
	public double getAvgUsedMemory() { return -1; }
	public double getAvgMaxMemory() { return -1; }
	public double getMaxMemory() { return -1; }
	public double getMinMemory() { return -1; }
	public long getUpdateCount() { return -1; }
	public long getUpdateErrors() { return -1; }
	public double getAvgThreadCount() { return -1; }
	public double getMaxThreadCount() { return -1; }
	public double getTotalThreadCount() { return -1; }
	public int getNumOfCpus() { return -1; }
	public double getAvgOsLoad() { return -1; }
	public double getMaxOsLoad() { return -1; }
	public double getAvgJvmLoad() { return -1; }
	public double getMaxJvmLoad() { return -1; }
	
	public double[] getStats(double[] stats, int toUnit) { return null; }
	public String getStatsAsString(double[] stats, int toUnit, String unit) { return null; }
	public String getStatsCSVHeader(String sep) { return null; }
	public String getStatsAsCSV(double[] stats, int toUnit, String sep) { return null; }
	
	public void printAll(java.io.PrintStream out) {}
}