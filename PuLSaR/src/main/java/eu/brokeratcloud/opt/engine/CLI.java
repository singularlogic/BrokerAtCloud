package eu.brokeratcloud.opt.engine;

import eu.brokeratcloud.common.SLMEvent;
import eu.brokeratcloud.opt.ConsumerPreferenceProfile;
import eu.brokeratcloud.opt.Recommendation;
import eu.brokeratcloud.opt.RecommendationItem;
import eu.brokeratcloud.opt.RecommendationManager;
import eu.brokeratcloud.rest.opt.ProfileManagementService;
import eu.brokeratcloud.rest.opt.RecommendationManagementService;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;
import javax.xml.namespace.QName;

/**
 *	The command-line entry-point to SAN Engine
 */
public class CLI {
	protected static String version = "v1.0 prototype";
	protected static String dates = "Apr-Jul 2014";
	protected static String banner = "PuLSaR:  Optimisation engine of Broker@Cloud platform\n"+
									 "Version: "+version+", "+dates+"\n"+
									 "Copyright 2014-2015, Institute of Communication and Computer Systems (ICCS)\n"+
									 "Visit us at: http://imu.ntua.gr/\n";
	
	public static void main(String[] args) throws Exception {
		Date dt;
		out().println(banner);
		
		// Load configuration
		String configFile = null;
		for (int i=0; i<args.length; i++) {
			if (args[i].trim().toLowerCase().equals("-config") && i<args.length-1) {
				configFile = args[i+1].trim();
				args[i] = args[i+1] = "";
			}
		}
		
		while (runEngine(configFile, args)) {
			out().println("\n==============================================================================\n");
			out().println("CLI: Restarting engine...\n");
		}
		
		out().println("\nBye");
	}
	
	protected static String[] defaultConfigFile = { "configuration.txt", "config"+System.getProperty("file.separator")+"configuration.txt" };
	protected static InputStream in() { return System.in; }
	protected static PrintStream out() { return System.out; }
	protected static PrintStream err() { return System.err; }
	
//XXX: IMPROVEMENT: Add log level control
/*	protected static void setLoggingLevel(String level) {
		try {
			out().println("Setting log level to "+level);
			org.apache.log4j.Level newLevel = org.apache.log4j.Level.toLevel(level, org.apache.log4j.Level.OFF);
			
			setLoggingLevelForClass( eu.brokeratcloud.opt.RecommendationManager.class, newLevel );
			setLoggingLevelForClass( eu.brokeratcloud.rest.opt.ProfileManagementService.class, newLevel );
			setLoggingLevelForClass( eu.brokeratcloud.rest.opt.RecommendationManagementService.class, newLevel );
		} catch (Exception e) {
			err().println("Exception caught will setting log level to "+level+"\n"+e);
		}
	}
	
	protected static boolean setLoggingLevelForClass(Class clss, org.apache.log4j.Level newLevel) throws Exception {
		java.lang.reflect.Field loggerField = clss.getDeclaredField("logger");
		if (loggerField==null) return true;
		
		loggerField.setAccessible(true);
		org.slf4j.Logger logger = (org.slf4j.Logger)loggerField.get(null);
		if (logger==null) return false;
		
		if (logger instanceof org.slf4j.impl.Log4jLoggerAdapter) {
			try {
				Class loggerIntrospected = logger.getClass();
				java.lang.reflect.Field fields[] = loggerIntrospected.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					String fieldName = fields[i].getName();
					if (fieldName.equals("logger")) {
						fields[i].setAccessible(true);
						org.apache.log4j.Logger loggerImpl = (org.apache.log4j.Logger) fields[i].get(logger);
						loggerImpl.setLevel( newLevel );
						// fields[i].setAccessible(false);
out().println(">>>>>>>>>  "+logger.getName()+" : "+newLevel);
						return true;
					}
				}
			} catch (Exception e) {
				err().println("An exception was thrown while changing the Logger level for class "+logger.getName()+"\n"+e);
			}
		}
		return false;
	}*/
	
	protected static boolean runEngine(String configFile, String[] args) throws Exception {
		boolean reload = false;
		Configuration config = null;
		PulsarBackgroundEngine pulsar = null;
		EventManager eventManager = null;
		
//XXX: IMPROVEMENT: Add log level control
//		setLoggingLevel("OFF");
		
		// Initialize configurator instance
		try {
			// Initialize configurator
			if (configFile!=null) {
				// User have specified a config file
				config = new Configuration(configFile, args);
			} else {
				// Try default config file locations if user have not provided one
				for (int i=0; i<defaultConfigFile.length; i++) {
					try {
						config = new Configuration(defaultConfigFile[i], args);
						configFile = defaultConfigFile[i];
						break;
					} catch (Exception ex) { configFile = null; }
				}
			}
			if (configFile!=null) {
				out().println("CLI: Configuration:      "+configFile);
			} else {
				err().println("CLI: Configuration file not found. Exiting...");
				return false;
			}
			
			// Print config information
//			out().println(config);
//			out().println();
		} catch (Exception ex) {
			err().println("CLI: Could not read configuration file. Reason:\n  "+ex.getMessage());
			return false;
		}
		
		// Instantiate PuLSaR background engine
		pulsar = new PulsarBackgroundEngine();
		
		// Initialize event manager
		eventManager = new LocalLoopEventManager();
		eventManager.setConfiguration(config);
		
		// Run PuLSaR background engine and event manager
		try {
			out().println("CLI: Starting PuLSaR...");
			pulsar.startEngine();
			out().println("CLI: PuLSaR started");
			out().println("CLI: Starting Event Manager...");
			eventManager.startManager();
			out().println("CLI: Event Manager started");
			out().println();
		} catch (Exception ex) {
			//err().println( ex.getMessage() );
			//return ;
			throw ex;
		}
		
		// Enter interactive CLI mode (i.e. offer a prompt and wait for user commands)
		boolean dontLoop = false;
		java.util.Scanner scan = null;
		if (!dontLoop)
		do {
			out().print("PULSAR> ");
			out().flush();
			if (scan==null) scan = new java.util.Scanner(in());
			while (!scan.hasNextLine()) Thread.sleep(200);
			String line = scan.nextLine().trim();
			String[] part = line.split("[ \t]+");
			String cmd = part[0].trim().toLowerCase();
			boolean ok = false;
			String helpId = null;
			if (cmd.equals("")) continue;
			else if (cmd.equals("quit") || cmd.equals("q")) break;
			else if (cmd.equals("help") || cmd.equals("?")) ok=printHelp();
			else if (cmd.equals("reload") || cmd.equals("r")) { reload = true; break; }
			else if (cmd.equals("event") || cmd.equals("e")) { ok=doEventCmd(line, eventManager); helpId = "event"; }
			else if (cmd.equals("recommendation") || cmd.equals("recom")) { ok=doRecomCmd(part); helpId = "recom"; }
			if (!ok) {
				out().println("CLI: Invalid or unknown command: "+cmd);
				//Print context-sensitive help
				if (helpId==null) ;
				else if (helpId.equals("event")) printContextHelp( eventManagerHelp() );
				else if (helpId.equals("recom")) printContextHelp( recomManagerHelp() );
			}
		} while (true);
		
		// Terminate execution of PuLSaR background engine and event manager
		out().println("CLI: Stopping Event Manager...");
		eventManager.stopManager();
		out().println("CLI: Event Manager stopped");
		out().println("CLI: Stopping PuLSaR...");
		pulsar.stopEngine();
		out().println("CLI: PuLSaR stopped");
		
		return reload;		// if 'true' the engine will be restarted (with the same command-line arguments) and reload configuration
	}
	
	protected static boolean printHelp() {
		out().println("***  PuLSaR Background Engine CLI commands  ***\n"+
				"\n(?) help    This help screen"+
				"\n(q) quit    Exits PuLSaR"+
				"\n(r) reload  Reloads config by restarting engine. Contexts are preserved"+
				"\n(e) event   Event manager usage:"+
				eventManagerHelp()+
				"\n(m) recom   Recommendation manager usage:"+
				recomManagerHelp()+
				"");
		return true;
	}
	protected static String eventManagerHelp() {
		return	"\n             event read <FILE>     Delivering events to PuLSaR from file"+
				"\n             event publish <FILE>  Publishing events to Pub/Sub from file"+
				"\n             event <on|off|status> Querying or changing event manager status";
	}
	protected static String recomManagerHelp() {
		return	"\n             recom config <FILE>    Load recom manager configuration from file"+
				"\n             recom list <USER> [<PROFILE>]  List active recoms for user/profile"+
				"\n             recom show <RECOM-ID>          Show recom data"+
				"\n             recom create <USER> <PROFILE>+ Create recoms for user profiles"+
				"\n             recom create <USER> ALL        Create recoms for all user profiles"+
				"\n             recom request <USER> <PROFILE>+ Create recoms but does not saves"+
				"\n             recom set-active <RECOM-ID>    Set as the active profile recom"+
				"\n             recom clear-active <RECOM-ID>  Unsets from active profile recom"+
				"\n             recom delete <RECOM-ID>        Deletes recommendation";
	}
	protected static void printContextHelp(String help) {
		String prepend = "  or   ";
		help = help.replace("             ", prepend);
		out().println( "Usage: "+help.substring(prepend.length()+1) );
	}
	
	protected static boolean doEventCmd(String line, EventManager eventManager) {
		try {
			boolean showUsage = false;
			String[] part = line.split("[ \t]+", 5);
/*			if (part.length==5) {
				String namespaceURI = part[1];
				String localPart = part[2];
				String prefix = part[3];
				String content = part[4];
				QName topic = new QName(namespaceURI, localPart, prefix);
				
				// Get a unique event id (GUID) if requested
				String guidId = UUID.randomUUID().toString();
				
				// If content specifies a file name then load content from file
				if (content.trim().startsWith("@")) {
					String fname = content.trim().substring(1).trim();
					try {
//						content = SANHelper.loadFile(fname);
//						content = content.replace("%ID%", guidId.replace("-","_"));
//						content = content.replace("%TIMESTAMP_UTC%", DateParser.formatW3CDateTime(new Date()));
//						content = content.replace("%TIMESTAMP_NUM%", Long.toString(System.currentTimeMillis()));
					} catch (Exception e) {
						err().println("Error while reading event payload file '"+fname+"' : "+e);
						content = null;
					}
				}
				
				if (content!=null) {
//					SLMEvent event = new SLMEvent(id, source, topic, content);
//					out().println("Publishing event (Id: "+id+")...");
//					boolean r = eventManager.eventReceived(event);
//					if (r) out().println("Event publishing... SUCCESSFULL");
//					else out().println("Event publishing... FAILED");
				}
			} else*/
			if (part.length==2) {
				String param = part[1].trim().toUpperCase();
				if (param.equals("ON")) { eventManager.setOnline(true); out().println("CLI: EventManager is ONLINE. Waiting for events..."); }
				else if (param.equals("OFF")) { eventManager.setOnline(false); out().println("CLI: EventManager is OFFLINE. No events will be received"); }
				else if (param.equals("STATUS")) { String stat = eventManager.isOnline() ? "ONLINE" : "OFFLINE"; out().println("CLI: EventManager status: "+stat); }
				else showUsage = true;
			} else
			if (part.length==3) {
				String operation = part[1].trim().toLowerCase();
				if (operation.equals("read")) {
					String file = part[2].trim();
					String evtText = readFile(file);
					eventManager.eventReceived(evtText);
				} else
				if (operation.equals("publish")) {
					//XXX: NOT YET IMPLEMENTED
					//String file = part[2].trim();
					//eventManager.publishFromFile(file);
				}
				else showUsage = true;
			} else {
				showUsage = true;
			}
			return !showUsage;
			
		} catch (Exception e) {
			err().println("CLI: An error occurred while sending event");
			e.printStackTrace(err());
			return false;
		}
	}
	
	protected static String readFile(String fileName) throws java.io.IOException {
		java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			
			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}
	
	protected static RecommendationManager recomManager;
	protected static RecommendationManagementService recomMgntWs;
	protected static ProfileManagementService profileMgntWs;
	protected static boolean includeInactive = true;

	protected static boolean doRecomCmd(String[] part) {
		try {
			if (part.length<3) return false;
			String rmCmd = part[1].trim().toLowerCase();
			if (rmCmd.equals("configure") || rmCmd.equals("config") || rmCmd.equals("cfg")) {
				String file = part[2].trim();		// config file
				if (file.equalsIgnoreCase("default")) {
					recomManager = RecommendationManager.getInstance();
				} else {
					recomManager = RecommendationManager.getInstance(file);
				}
			} else
			if (rmCmd.equals("list")) {
				if (part.length<3) return false;
				boolean listProfile = (part.length>3);
				String sc = part[2].trim();						// user
				
				if (recomManager==null) recomManager = RecommendationManager.getInstance();
				if (listProfile) {
					if (part.length<4) return false;
					String id = part[3].trim();					// profile id
					if (recomMgntWs==null) recomMgntWs = new eu.brokeratcloud.rest.opt.RecommendationManagementService();
					Recommendation[] list = recomMgntWs.getRecommendations(sc, id, includeInactive);
					if (list==null || list.length==0) out().println("No recommendations found");
					else {
						for (int i=0; i<list.length; i++) {
							String active = list[i].isActive()?"*":" ";
							out().println( String.format("%s%3d. %s", active, i+1, list[i].getId()) );
						}
						out().println(list.length+" recommendations found");
					}
				} else {
					if (recomMgntWs==null) recomMgntWs = new eu.brokeratcloud.rest.opt.RecommendationManagementService();
					Recommendation[] list = recomMgntWs.getAllRecommendations(sc, includeInactive);
					if (list==null || list.length==0) out().println("No recommendations found");
					else {
						for (int i=0; i<list.length; i++) {
							String active = list[i].isActive()?"*":" ";
							out().println( String.format("%s%3d. %s %s", active, i+1, list[i].getProfile(), list[i].getId()) );
						}
						out().println(list.length+" recommendations found");
					}
				}
			} else
			if (rmCmd.equals("show")) {
				if (part.length<3) return false;
				String recomId = part[2].trim();		// id
				
				if (recomMgntWs==null) recomMgntWs = new eu.brokeratcloud.rest.opt.RecommendationManagementService();
				Recommendation recom = recomMgntWs.getRecommendation(recomId);
				if (recom==null || recom.getId()==null || recom.getId().isEmpty()) out().println("Recommendation not found");
				else {
					out().println(	"Recommendation:"+
									"\n  Id: "+recom.getId()+
									"\n  Profile: "+recom.getProfile()+
									"\n  Creation: "+recom.getCreateTimestamp()+
									"\n  Active: "+recom.isActive()+
									"\n  Items:"
									);
					boolean empty = true;
					for (RecommendationItem rit : recom.getItems()) {
						empty = false;
						out().println("    Item Id: "+rit.getId()+
									"\n       Suggestion : "+rit.getSuggestion()+
									"\n       Service Descr : "+rit.getServiceDescription()+
									"\n       Relevance : "+rit.getWeight()+
									"\n       Response : "+rit.getResponse()
									);
					}
					if (empty) out().println("  No items in recommendation");
				}
			} else
			if (rmCmd.equals("create") || rmCmd.equals("add") || rmCmd.equals("new") || rmCmd.equals("request")) {
				if (part.length<4) return false;
				String sc = part[2].trim();		// user
				boolean all = part[3].trim().equalsIgnoreCase("ALL");
				boolean saveRecom = !rmCmd.equals("request");
				
				if (recomManager==null) recomManager = RecommendationManager.getInstance();
				if (recomMgntWs==null) recomMgntWs = new eu.brokeratcloud.rest.opt.RecommendationManagementService();
				if (all) {
					if (profileMgntWs==null) profileMgntWs = new ProfileManagementService();
					ConsumerPreferenceProfile[] profiles = profileMgntWs.getProfilesDetailed(sc);
					if (profiles!=null) {
						for (int i=0; i<profiles.length; i++) {
							String profileId = profiles[i].getId();
							if (profileId==null || profileId.isEmpty()) continue;
							Recommendation recom = recomManager.createNewRecommendation(sc, profileId);
							if (recom==null) out().println("No recommendation generated for profile "+profileId);
							else {
								out().println("New recommendation for profile '"+profileId+"' with id: "+recom.getId());
								if (saveRecom) {
									recomMgntWs.createRecommendation(recom);
									out().println("Recommendation saved");
								} else out().println("Recommendation was not saved");
							}
						}
					} else out().println("No profiles found for user "+sc);
				} else {
					for (int i=3; i<part.length; i++) {
						String profileId = part[i].trim();
						if (profileId.isEmpty()) continue;
						Recommendation recom = recomManager.createNewRecommendation(sc, profileId);
						if (recom==null) out().println("No recommendation generated for profile "+profileId);
						else {
							out().println("New recommendation for profile '"+profileId+"' with id: "+recom.getId());
							if (saveRecom) {
								recomMgntWs.createRecommendation(recom);
								out().println("Recommendation saved");
							} else out().println("Recommendation was not saved");
						}
					}
				}
			} else
			if (rmCmd.equals("deactivate") || rmCmd.equals("clear-active")) {
				String recomId = part[2].trim();		// recom id
				if (recomMgntWs==null) recomMgntWs = new eu.brokeratcloud.rest.opt.RecommendationManagementService();
				int status = recomMgntWs.clearRecommendation(recomId).getStatus();
				if (status<299) out().println("Recommendation active flag cleared"); else err().println("An error occurred. Recommendation was not changed");
			} else
			if (rmCmd.equals("activate") || rmCmd.equals("set-active")) {
				String recomId = part[2].trim();		// recom id
				if (recomMgntWs==null) recomMgntWs = new eu.brokeratcloud.rest.opt.RecommendationManagementService();
				int status = recomMgntWs.setRecommendation(recomId).getStatus();
				if (status<299) out().println("Recommendation active flag set"); else err().println("An error occurred. Recommendation was not changed");
			} else
			if (rmCmd.equals("delete")) {
				String recomId = part[2].trim();		// recom id
				if (recomMgntWs==null) recomMgntWs = new eu.brokeratcloud.rest.opt.RecommendationManagementService();
				int status = recomMgntWs.deleteRecommendation(recomId).getStatus();
				if (status<299) out().println("Recommendation deleted"); else err().println("An error occurred. Recommendation was not deleted");
			} else {
				return false;
			}
			return true;
		} catch (Exception e) {
			err().println("CLI: An error occurred");
			e.printStackTrace(err());
			return false;
		}
	}
}

class PulsarBackgroundEngine {
	public PulsarBackgroundEngine() {
	}
	public void startEngine() {}
	public void stopEngine() {}
	//public void processEvent(Event e) {}
}

abstract class EventManager {
	public abstract Configuration getConfiguration();
	public abstract void setConfiguration(Configuration cfg);
	public abstract void startManager();
	public abstract void stopManager();
	public abstract boolean isOnline();
	public abstract void setOnline(boolean b);
	public abstract void eventReceived(String evtText);
	public abstract void eventReceived(SLMEvent evt);
}

class LocalLoopEventManager extends EventManager {
	protected Configuration config;
	protected boolean online;
	protected RecommendationManager recomMgr;
	
	public LocalLoopEventManager() {
		recomMgr = RecommendationManager.getInstance();
	}
	
	public Configuration getConfiguration() { return config; }
	public void setConfiguration(Configuration cfg) { config = cfg; }
	public void startManager() { setOnline(true); }
	public void stopManager() { setOnline(false); }
	public boolean isOnline() { return online; }
	public void setOnline(boolean b) { online = b; }
	
	public void eventReceived(String evtText) {
		try {
			SLMEvent evt = SLMEvent.parseEvent(evtText);
			eventReceived(evt);
		} catch (Exception e) {
			System.err.println( "LocalLoopEventManager.eventReceived(String): Error while parsing SLM event: "+e );
			e.printStackTrace();
		}
	}
	
	public void eventReceived(SLMEvent evt) {
		if (evt.getType()==null || evt.getType().trim().isEmpty()) {
			System.err.println( String.format("LocalLoopEventManager.eventReceived: Missing event type: Event-Id: %s", evt.getId()) );
			return;
		}
		
		String type = evt.getType().trim().toLowerCase();
		if ("service-onboarded".equals(type)) serviceOnboarded(evt);
		else if ("service-depreciated".equals(type)) serviceDepreciated(evt);
		else if ("service-updated".equals(type)) serviceUpdated(evt);
		else System.err.println( String.format("LocalLoopEventManager.eventReceived: Unknown event type: %s, Event-Id: %s", evt.getType(), evt.getId()) );
	}
	
	public void serviceOnboarded(SLMEvent evt) {
		recomMgr.requestRecommendations(evt);
	}
	
	public void serviceDepreciated(SLMEvent evt) {
		recomMgr.requestRecommendations(evt);
	}
	
	public void serviceUpdated(SLMEvent evt) {
		recomMgr.requestRecommendations(evt);
	}
}