package eu.brokeratcloud.opt.engine;

import eu.brokeratcloud.common.SLMEvent;
import eu.brokeratcloud.opt.ConsumerPreferenceProfile;
import eu.brokeratcloud.opt.FeedbackReporter;
import eu.brokeratcloud.opt.Recommendation;
import eu.brokeratcloud.opt.RecommendationItem;
import eu.brokeratcloud.opt.RecommendationManager;
import eu.brokeratcloud.opt.engine.sim.EventGenerator;
import eu.brokeratcloud.rest.opt.ProfileManagementService;
import eu.brokeratcloud.rest.opt.RecommendationManagementService;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import javax.xml.namespace.QName;

/**
 *	The command-line interface of PuLSaR system
 */
public class CLI {
	protected static String version = "v1.0 prototype";
	protected static String dates = "Apr 2014-Feb 2015";
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
	
	protected static EventManager eventManager;
	
	protected static boolean runEngine(String configFile, String[] args) throws Exception {
		boolean reload = false;
		Configuration config = null;
		eventManager = null;
		
		// If arguments are present then just execute command and exit (unless -stay argument is present)
		boolean hasArgs = false;
		boolean keepLooping = false;
		for (int i=0; i<args.length; i++) {
			if (args[i]==null || args[i].trim().isEmpty()) continue;
			if ("-stay".equalsIgnoreCase(args[i].trim())) {
				keepLooping = true;
				args[i] = "";
				continue;
			}
			hasArgs = true;
		}
		if (!hasArgs) keepLooping = true; 		// if no arguments are specified enter interactive mode
		
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
		
		// Initialize event manager
		eventManager = new LocalLoopEventManager();
		eventManager.setConfiguration(config);
		
		// Run event manager
		try {
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
		java.util.Scanner scan = null;
		do {
			String line;
			if (hasArgs) {
				StringBuilder sb = new StringBuilder();
				for (int i=0; i<args.length; i++) if (args[i]!=null) sb.append(" ").append(args[i]);
				line = sb.toString().trim();
				out().println("CLI: effective command-line: "+line);
				if (keepLooping) out().println("CLI: will enter interactive mode after command-line completion");
				hasArgs = false;
			} else {
				out().print("PULSAR> ");
				out().flush();
				if (scan==null) scan = new java.util.Scanner(in());
				while (!scan.hasNextLine()) Thread.sleep(200);
				line = scan.nextLine().trim();
			}
			String[] part = line.split("[ \t]+");
			String cmd = part[0].trim().toLowerCase();
			boolean ok = false;
			String helpId = null;
			if (cmd.equals("")) continue;
			else if (cmd.equals("quit") || cmd.equals("q")) break;
			else if (cmd.equals("help") || cmd.equals("?")) ok=printHelp();
			else if (cmd.equals("reload") || cmd.equals("r")) { reload = true; break; }
			else if (cmd.equals("event") || cmd.equals("e")) { ok=doEventCmd(line, eventManager); helpId = "event"; }
			else if (cmd.equals("recommendation") || cmd.equals("recom") || cmd.equals("m")) { ok=doRecomCmd(part); helpId = "recom"; }
			else if (cmd.equals("feedback") || cmd.equals("f")) { ok=doFeedbackCmd(part); helpId = "feedback"; }
			else if (cmd.equals("simulation") || cmd.equals("sim") || cmd.equals("s")) { ok=doSimulationCmd(part, eventManager); helpId = "simulation"; }
			if (!ok) {
				out().println("CLI: Invalid or unknown command: "+cmd);
				//Print context-sensitive help
				if (helpId==null) ;
				else if (helpId.equals("event")) printContextHelp( eventManagerHelp() );
				else if (helpId.equals("recom")) printContextHelp( recomManagerHelp() );
				else if (helpId.equals("feedback")) printContextHelp( feedbackReporterHelp() );
				else if (helpId.equals("simulation")) printContextHelp( simulationHelp() );
			}
		} while (keepLooping);
		
		// Terminate execution of event manager
		out().println();
		out().println("CLI: Stopping Event Manager...");
		eventManager.stopManager();
		out().println("CLI: Event Manager stopped");
		
		return reload;		// if 'true' the engine will be restarted (with the same command-line arguments) and reload configuration
	}
	
	protected static boolean printHelp() {
		out().println();
		out().println("***  PuLSaR Background Engine CLI commands  ***\n"+
				"\n(?) help    This help screen"+
				"\n(q) quit    Exits PuLSaR"+
				"\n(r) reload  Reloads config by restarting engine. Contexts are preserved"+
				"\n(e) event   Event manager usage:"+
				eventManagerHelp()+
				"\n(m) recom   Recommendation manager usage:"+
				recomManagerHelp()+
				"\n(f) feedback Feedback reporting usage:"+
				feedbackReporterHelp()+
				"\n(s) simulate Simulation feature usage:"+
				simulationHelp()+
				"");
		return true;
	}
	protected static String eventManagerHelp() {
		return	"\n             event read <FILE>     Deliver an events to PuLSaR from file"+
				"\n             event publish <TOPIC> <CONTENT>  Publish an event using Pub/Sub"+
				"\n                   if <CONTENT> starts with @ then the actual content is loaded"+
				"\n                   from the file specified after @"+
				"\n             event <on|off|status> Querying or changing event manager status"+
				"\n             event source [local | pubsub]  Display or Set event source";
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
				"\n             recom delete <RECOM-ID>        Deletes recommendation"+
				"\n             recom threads SHOW|<#threads>  Get/Set thread pool size (>=1)";
	}
	protected static String feedbackReporterHelp() {
		return	"\n             feedback report [<CFG-FILE>]   Generates user feedback reports"+
				"\n             feedback schedule <PERIOD>|OFF Schedule report generation";
	}
	protected static String simulationHelp() {
		return	"\n             simulate gen-events <period-usec> <iterations> <event-templ-file>"+
				"\n             simulate iterate <delay-usec> <iterations> <event-templ-file>";
	}
	protected static void printContextHelp(String help) {
		String prepend = "  or   ";
		help = help.replace("             ", prepend);
		out().println( "Usage: "+help.substring(prepend.length()+1) );
	}
	
	protected static boolean doEventCmd(String line, EventManager eventManager) {
		try {
			boolean showUsage = false;
			String[] part = line.split("[ \t]+", 4);
			if (part.length==2) {
				String param = part[1].trim().toUpperCase();
				if (param.equals("ON")) { eventManager.startManager(); out().println("CLI: EventManager is ONLINE. Waiting for events..."); }
				else if (param.equals("OFF")) { eventManager.stopManager(); out().println("CLI: EventManager is OFFLINE. No events will be received"); }
				else if (param.equals("STATUS")) { String stat = eventManager.isOnline() ? "ONLINE" : "OFFLINE"; out().println("CLI: EventManager status: "+stat); }
				else if (param.equals("SOURCE")) { String type = (eventManager instanceof LocalLoopEventManager) ? "local" : "pubsub"; out().println("CLI: EventManager type: "+type); }
				else showUsage = true;
			} else
			if (part.length==3) {
				String operation = part[1].trim().toLowerCase();
				if (operation.equals("read")) {
					String file = part[2].trim();
					String evtText = readFile(file);
					eventManager.eventReceived(evtText);
				} else
				if (operation.equals("source")) {
					String mgrType = part[2].trim().toLowerCase();
					EventManager newManager = null;
					if (mgrType.equals("local") && !(eventManager instanceof LocalLoopEventManager)) {
						newManager = new LocalLoopEventManager();
					} else
					if (mgrType.equals("pubsub") && !(eventManager instanceof PubsubEventManager)) {
						newManager = new PubsubEventManager();
					} else {
						out().println("CLI: Current EventManager is "+mgrType+". Nothing to do");
					}
					if (newManager!=null) {
						boolean status = eventManager.isOnline();
						if (status) { out().println("CLI: Stopping current EventManager..."); eventManager.stopManager(); }
						if (recomManager!=null) recomManager.setEventManager(null);
						newManager.setConfiguration( eventManager.getConfiguration() );
						eventManager = newManager;
						CLI.eventManager = eventManager;
						out().println("CLI: EventManager set to "+mgrType);
						if (status) { out().println("CLI: Starting "+mgrType+" EventManager..."); eventManager.startManager(); }
						if (recomManager!=null) recomManager.setEventManager(newManager);
					}
				}
				else showUsage = true;
			} else 
			if (part.length==4 && part[1].trim().equalsIgnoreCase("publish")) {
				String topic = part[2].trim();
				String content = part[3];
				String guidId = UUID.randomUUID().toString();
				Date tm = new Date();
				
				// If content specifies a file name then load content from file
				if (content.trim().startsWith("@")) {
					String fname = content.trim().substring(1).trim();
					try {
						content = readFile(fname);
						content = content.replace("%ID%", guidId.replace("-","_"));
						content = content.replace("%TIMESTAMP_UTC%", tm.toString());
						content = content.replace("%TIMESTAMP_NUM%", Long.toString(tm.getTime()));
					} catch (Exception e) {
						err().println("Error while reading event payload file '"+fname+"' : "+e);
						content = null;
					}
				}
				
				// Publish event
				if (content!=null) {
					Properties p = new Properties();
					p.setProperty(".EVENT-CONTENT", content);
					SLMEvent event = new SLMEvent(guidId, tm, topic, "-", p);
					out().println("Publishing event: "+event+")...");
					try {
						boolean r = eventManager.publish(event);
						if (r) out().println("Event publishing... SUCCESSFULL");
						else err().println("Event publishing... FAILED");
					} catch (Exception e) {
						err().println("Event publishing... CAUSED EXCEPTION: "+e);
						e.printStackTrace(err());
					}
				}
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
	
	public static String readFile(String fileName) throws java.io.IOException {
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
				recomManager.setEventManager(eventManager);
			} else
			if (rmCmd.equals("list")) {
				if (part.length<3) return false;
				boolean listProfile = (part.length>3);
				String sc = part[2].trim();						// user
				
				if (recomManager==null) recomManager = RecommendationManager.getInstance();
				recomManager.setEventManager(eventManager);
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
				recomManager.setEventManager(eventManager);
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
			} else
			if (rmCmd.equals("threads")) {
				if (recomManager==null) recomManager = RecommendationManager.getInstance();
				recomManager.setEventManager(eventManager);
				try {
					if (part[2].trim().equalsIgnoreCase("show")) {
						out().println("Thread Pool size: "+recomManager.getThreadPoolSize());
					} else {
						int tpSize = Integer.parseInt( part[2].trim() );
						if (tpSize<1) {
							err().println("Invalid thread pool size. It must be a positive integer");
							return true;
						}
						out().println("New Thread Pool size: "+tpSize);
						recomManager.setThreadPoolSize(tpSize);
					}
				} catch (Exception e) {
					err().println("Invalid parameter: "+e);
					simulationHelp();
					return true;
				}
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
	
	protected static FeedbackReporter fbReporter;
	protected static long fbReporterPeriod;
	protected static Timer scheduler;
	
	protected static boolean doFeedbackCmd(String[] part) {
		try {
			if (part.length<2) return false;
			String fbCmd = part[1].trim().toLowerCase();
			if (fbCmd.equals("report") || fbCmd.equals("rep")) {
				String file = "";
				if (part.length>=3) file = part[2].trim();		// config file
				if (file.trim().isEmpty() || file.equalsIgnoreCase("default")) {
					fbReporter = FeedbackReporter.getInstance();
				} else {
					fbReporter = FeedbackReporter.getInstance(file);
				}
				
				// Run feedback reports generation engine immediately
				fbReporter.generateFeedbackReports();
				
			} else
			if (fbCmd.equals("schedule")) {
				if (part.length<3) {
					err().println("Missing period parameter");
					feedbackReporterHelp();
					return true;
				}
				String periodStr = part[2].trim();		// period
				
				if (periodStr.isEmpty()) {
					err().println("Missing period parameter");
					feedbackReporterHelp();
					return true;
				} else
				if (periodStr.equalsIgnoreCase("OFF")) {
					fbReporterPeriod = -1;
					stopScheduler();
				} else {
					try {
						long per = Long.parseLong(periodStr);
						if (per<=0) {
							err().println("Period must be a positive integer");
							feedbackReporterHelp();
							return true;
						}
						fbReporterPeriod = per;
						startScheduler();
					} catch (NumberFormatException e) {
						err().println("Invalid period: "+periodStr);
						feedbackReporterHelp();
						return true;
					}
				}
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
	
	protected static void startScheduler() {
		if (scheduler==null) scheduler = new Timer("PuLSaR CLI scheduler", true);
		scheduler.schedule(new TimerTask() {
			public void run() {
				out().println("Scheduler: Starting feedback report generation...");
				if (fbReporter==null) fbReporter = FeedbackReporter.getInstance();
				fbReporter.generateFeedbackReports();
			}
		}, fbReporterPeriod, fbReporterPeriod);
		out().println("Scheduler started");
	}
	
	protected static void stopScheduler() {
		if (scheduler==null) return;
		scheduler.cancel();
		scheduler = null;
		out().println("Scheduler stopped");
	}
	
	protected static boolean doSimulationCmd(String[] part, EventManager evtMgr) {
		try {
			if (part.length<4) return false;
			String simCmd = part[1].trim().toLowerCase();
			if (simCmd.equals("gen-events")) {
				try {
					long period = Long.parseLong( part[2] );
					long iterations = Long.parseLong( part[3] );
					String evtTpl = readFile( part[4] );
				
					// Run feedback reports generation engine immediately
					EventGenerator evtGen = new EventGenerator(period, iterations, evtTpl, evtMgr, EventGenerator.GEN_MODE.PERIOD, out());
					evtGen.start();
					evtGen.waitToComplete();
				} catch (Exception e) {
					err().println("Invalid parameter: "+e);
					simulationHelp();
					return true;
				}
				
			} else
			if (simCmd.equals("iterate")) {
				try {
					long delay = Long.parseLong( part[2] );
					long iterations = Long.parseLong( part[3] );
					String evtTpl = readFile( part[4] );
				
					// Run feedback reports generation engine immediately
					EventGenerator evtGen = new EventGenerator(delay, iterations, evtTpl, evtMgr, EventGenerator.GEN_MODE.DELAY, out());
					evtGen.start();
					evtGen.waitToComplete();
				} catch (Exception e) {
					err().println("Invalid parameter: "+e);
					simulationHelp();
					return true;
				}
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
	
	public static Process execProcess(String cmd) throws java.io.IOException {
		// Prepare shell command
		String[] cmdParts = cmd.split("[ \t\r]+");
		
		// Run a simulation (test) in a separate system process
		ProcessBuilder pb = new ProcessBuilder(cmdParts);
		pb.inheritIO();
		Process p = pb.start();
		return p;
	}
}
