package eu.brokeratcloud.util;

import java.util.Properties;

public abstract class Stats {
	protected static final String propertiesFile = "stats.properties";
	protected static Properties properties;
	private static Stats statsInstance;
	private static int statsUpdateDelay;
	
	private static void initInstance() {
		// Load properties
		try {
			properties = new Properties();
			properties.load( Stats.class.getClassLoader().getResourceAsStream(propertiesFile) );
		} catch (Exception e) {}
		// Instantiate Stats singleton object
		try {
			//instance = new StatsImpl();
			String clssName = properties.getProperty("stats.class");
			Class clss = Class.forName(clssName);
			statsInstance = (Stats)clss.newInstance();
		} catch (Exception e) {
			statsInstance = new StatsEmpty();
		}
		// Get Stats update delay
		try {
			statsUpdateDelay = Integer.parseInt( properties.getProperty("stats.update-delay", "1000") );
		} catch (Exception e) {}
	}
	
	public static Stats get() {
		if (statsInstance==null) initInstance();
		return statsInstance;
	}
	
	public abstract int nextCounter(String s);
	public abstract int nextTimer(String s);
	public abstract int nextSplit(String s);
	
	public abstract int currentCounter();
	public abstract int currentTimer();
	public abstract int currentSplit();
	
	public abstract long increase();
	public abstract long increase(String s);
	public abstract long increase(int cnt);
	public abstract long decrease();
	public abstract long decrease(String s);
	public abstract long decrease(int cnt);
	
	public abstract long startTimer();
	public abstract long startTimer(String s);
	public abstract long startTimer(int tmr);
	public abstract long endTimer();
	public abstract long endTimer(String s);
	public abstract long endTimer(int tmr);
	
	public abstract long startSplit();
	public abstract long startSplit(String s);
	public abstract long startSplit(int spl);
	public abstract long endSplit();
	public abstract long endSplit(String s);
	public abstract long endSplit(int spl);
	
	public abstract long getCounterValue(int cnt);
	public abstract long getTimerValue(int tmr);
	public abstract long getSplitCount(int spl);
	public abstract long getSplitDuration(int spl);
	public abstract long getSplitMax(int spl);
	public abstract long getSplitMin(int spl);
	public abstract long getSplitAverage(int spl);
	
	public abstract int getCounterByName(String s);
	public abstract int getTimerByName(String s);
	public abstract int getSplitByName(String s);
	public abstract String getTypeByName(String s);
	
	public abstract int getOrCreateCounterByName(String s);
	public abstract int getOrCreateTimerByName(String s);
	public abstract int getOrCreateSplitByName(String s);
	
	// ---------------------------------------------------------------------------------------------------------------------------------
	// Memory, CPU and threads related variables and methods
	public static final int memToBytes = 1;
	public static final int memToKb = 1024*1024;
	public static final int memToMb = 1024*1024;
	public static final int memToGb = 1024*1024*1024;
	
	public static final double nanoToSec = 1000000000;
	
	public abstract void startStats(int delay);
	public abstract void stopStats();
	
	public abstract double getAvgTotalMemory();
	public abstract double getAvgFreeMemory();
	public abstract double getAvgUsedMemory();
	public abstract double getAvgMaxMemory();
	public abstract double getMaxMemory();
	public abstract double getMinMemory();
	public abstract long getUpdateCount();
	public abstract long getUpdateErrors();
	public abstract double getAvgThreadCount();
	public abstract double getMaxThreadCount();
	public abstract double getTotalThreadCount();
	public abstract int getNumOfCpus();
	public abstract double getAvgOsLoad();
	public abstract double getMaxOsLoad();
	public abstract double getAvgJvmLoad();
	public abstract double getMaxJvmLoad();
	
	public abstract double[] getStats(double[] stats, int toUnit);
	public abstract String getStatsAsString(double[] stats, int toUnit, String unit);
	public abstract String getStatsCSVHeader(String sep);
	public abstract String getStatsAsCSV(double[] stats, int toUnit, String sep);
	
	public abstract void printAll(java.io.PrintStream out);
}