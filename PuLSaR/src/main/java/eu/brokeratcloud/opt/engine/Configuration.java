package eu.brokeratcloud.opt.engine;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 *	A class managing all major configuration-dependent system components
 */
public class Configuration {
	protected Properties configuration;
	//public SANEngine engine;
	
	public Configuration(String configFile, String[] args) {
	}
	
/*	public Configuration initialize(String configFile) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Properties config = SANHelper.loadConfiguration(configFile);
		return initialize(config, null);
	}
	
	public Configuration initialize(String configFile, String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Properties config = SANHelper.loadConfiguration(configFile, args);
		return initialize(config, args);
	}
	
	public Configuration initialize(Properties config, String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		_processConfigProperties(config, args);
		
		// Get engine IO system and logger
		this.io = IOSystem.getInstance(this);
		
		// initialize SANThread class
		SANThread.defaultConfiguration = this;
		
		// Create a SAN engine instance
		this.engine = SANEngineFactory.getInstance(this);
		this.engine.setConfiguration(this);
		
		// Get a SAN repository instance
		this.repository = RepositoryFactory.getRepository(this);
		this.repository.setConfiguration(this);
		
		// Prepare global context
		this.globalContext = ContextFactory.getContextFactory(this).getGlobalContext();
		
		// Prepare CEP Engine
		this.cep = CEPEngineFactory.getCEPEngineFactory(this).getCEPEngine();
		this.cep.setConfiguration(this);
		
		// Prepare action helper object
		this.actionHelper = new ActionHelper(this);
		ActionHelper.setInstance(this.actionHelper);
		
		// initialize statistics instance
		statistics = Statistics.getInstance(this);
		stat = new Stat(statistics);
		
		return this;
	}
	
	protected void _processConfigProperties(Properties config, String[] args) {
		this.configuration = new Properties();
		Enumeration en = config.propertyNames();
		while (en.hasMoreElements()) {
			String key = (String)en.nextElement();
			String val = config.getProperty(key);
			if (key.trim().startsWith("@")) _processCommand("", (key+" "+val).trim(), args);
			else if (val.trim().startsWith("@")) _processCommand(key.trim(), val.trim(), args);
			else this.configuration.put(key, val);
		}
	}
	
	protected void _processCommand(String scope, String command, String[] args) {
		String[] part = command.split("[ \t]", 2);
		String   cmd = part[0].trim().toLowerCase();
		
		if (cmd.equals("@include")) {
			if (part.length==1) {
				System.err.println("Configuration: _processCommand: Missing argument at @include command at property '"+scope+"'. One properties file should be specified. Ignoring this property.");
			} else {
				String propFile = part[1].trim();
				try {
					System.out.println("Loading file: "+propFile);
					//Properties prop = SANHelper.loadProperties(propFile);
					Properties prop = SANHelper.loadConfiguration(propFile, args);
					if (prop==null) {
						System.err.println("Configuration: _processCommand: Could not load properties file '"+propFile+"' specified at @include command at property '"+scope+"'. Ignoring this property.");
						System.err.println("Reason: properties returned is null");
					} else {
						Enumeration en = prop.propertyNames();
						String base = (scope.equals("")) ? "" : scope+".";
						while (en.hasMoreElements()) {
							String key = (String)en.nextElement();
							String val = prop.getProperty(key);
							this.configuration.put(base+key, val);
						}
					}
				} catch (Exception ex) {
					System.err.println("Configuration: _processCommand: Could not load properties file '"+propFile+"' specified at @include command at property '"+scope+"'. Ignoring this property.");
					System.err.println("Reason: "+ex.getMessage());
				}
			}
		} else {
			System.err.println("Configuration: _processCommand: Unknown command '"+part[0].trim()+"' at property '"+scope+"'. Ignoring this property.");
		}
	}
	
	public String get(String key) {
		return configuration.getProperty(key);
	}
	
	public String get(String key, String def) {
		String v = configuration.getProperty(key);
		return (v!=null) ? v : def;
	}
	
	public boolean getBoolean(String key, boolean defValue) {
		String value = get(key);
		return checkBoolean(value, defValue);
	}
	
	public static boolean checkBoolean(String value, boolean defValue) {
		if (value==null || value.trim().equals("")) return defValue;
		value = value.trim().toLowerCase();
		if (value.equals("yes") || value.equals("true") || value.equals("on") || value.equals("1")) return true;
		if (value.equals("no") || value.equals("false") || value.equals("off") || value.equals("0")) return false;
		return defValue;
	}
	
	public Properties getScope(String scope, boolean stripPrefix) {
		scope = (scope==null || scope.trim().equals("")) ? "" : scope.trim();
		scope = scope.endsWith(".") ? scope.substring(0, scope.length()-1) : scope;
		String prefix = scope+".";
		
		Properties prop = new Properties();
		Enumeration en = this.configuration.propertyNames();
		while (en.hasMoreElements()) {
			String key = (String)en.nextElement();
			String val = this.configuration.getProperty(key);
			
			if (key.startsWith(prefix)) {
				if (stripPrefix && !key.equals(prefix)) {
					key = key.substring(prefix.length());
				}
				prop.put(key, val);
			} else
			if (key.equals(scope)) {
				if (stripPrefix) key = "";
				prop.put(key, val);
			}
		}
		return prop;
	}
	
	// Helper class for execution statistics
	public static class Stat {
		protected int cntActiveThreads, cntTotalThreads, cntActiveInstances, cntTotalInstances;
		protected int cntActiveEntities, cntTotalEntities, cntActiveWaits, cntTotalWaits;
		protected int cntTotalEvents, cntUniqueEvents, cntPublishEvents;
		protected Statistics statistics;
		
		public Stat(Statistics statistics) {
			if (statistics==null || !statistics.isActive()) return;
			this.statistics = statistics;
			this.statistics.writeOff();
			cntActiveThreads = this.statistics.createCounter("Active Threads");
			cntTotalThreads  = this.statistics.createCounter("Total Threads");
			cntActiveInstances = this.statistics.createCounter("Active SAN Instances");
			cntTotalInstances  = this.statistics.createCounter("Total SAN Instances");
			cntActiveEntities = this.statistics.createCounter("Active Entities");
			cntTotalEntities  = this.statistics.createCounter("Total Entities");
			cntActiveWaits = this.statistics.createCounter("Active Waits");
			cntTotalWaits  = this.statistics.createCounter("Total Waits");
			cntTotalEvents  = this.statistics.createCounter("Total Events");
			cntUniqueEvents = this.statistics.createCounter("Unique Events");
			cntPublishEvents = this.statistics.createCounter("Published Events");
			this.statistics.writeOn();
			this.statistics.writeHeaders();
		}
		
		public void threadStart() { if (this.statistics==null) return; this.statistics.inc(cntActiveThreads); this.statistics.inc(cntTotalThreads); }
		public void threadEnd()   { if (this.statistics==null) return; this.statistics.dec(cntActiveThreads); }
		public void instanceCreate() { if (this.statistics==null) return; this.statistics.inc(cntActiveInstances); this.statistics.inc(cntTotalInstances); }
		public void instanceEnd()    { if (this.statistics==null) return; this.statistics.dec(cntActiveInstances); }
		public void entityCreate() { if (this.statistics==null) return; this.statistics.inc(cntActiveEntities); this.statistics.inc(cntTotalEntities); }
		public void entityEnd()    { if (this.statistics==null) return; this.statistics.dec(cntActiveEntities); }
		public void waitBegin() { if (this.statistics==null) return; this.statistics.inc(cntActiveWaits); this.statistics.inc(cntTotalWaits); }
		public void waitEnd()   { if (this.statistics==null) return; this.statistics.dec(cntActiveWaits); }
		public void uniqueEvent() { if (this.statistics==null) return; this.statistics.inc(cntUniqueEvents); }
		public void newEvent()    { if (this.statistics==null) return; this.statistics.inc(cntTotalEvents); }
		public void publishEvent()    { if (this.statistics==null) return; this.statistics.inc(cntPublishEvents); }
	}*/
	
	public String toString() {
		return "Configuration: ++++++++++ TODO ++++++++++";
	}
}
