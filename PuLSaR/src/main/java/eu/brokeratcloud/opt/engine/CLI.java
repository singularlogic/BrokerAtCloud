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
	protected static String version = "v1.1 prototype";
	protected static String dates = "Apr 2014-Feb 2016";
	protected static String banner = "PuLSaR:  Optimisation engine of Broker@Cloud platform\n"+
									 "Version: "+version+", "+dates+"\n"+
									 "Copyright 2014-2016, Institute of Communication and Computer Systems (ICCS)\n"+
									 "Visit us at: http://imu.ntua.gr/\n";
	
	public static String getVersion() { return version; }
	public static String getBanner() { return banner; }
	
	public static void main(String[] args) throws Exception {
		initializeIO();
		
		Date dt;
		out().println(banner);
		
		// Load configuration
		String configFile = null;
		for (int i=0; i<args.length; i++) {
			String s=args[i].trim().toLowerCase();
			if ((s.equals("-config") || s.equals("-cfg")) && i<args.length-1) {
				configFile = args[i+1].trim();
				args[i] = args[i+1] = "";
			}
		}
		
		while (runEngine(configFile, args)) {
			out().println("\n==============================================================================\n");
			out().println("CLI: Restarting engine...\n");
		}
		stopInput();
		
		out().println("\nBye");
	}
	
	protected static String[] defaultConfigFile = { "configuration.txt", "config"+System.getProperty("file.separator")+"configuration.txt" };
	
	// IO methods, fields and member classes
	private static InputStream _systemIn;
	private static CapturingPrintStream _systemOut;
	private static CapturingPrintStream _systemErr;
	protected static void initializeIO() {
		_systemIn = System.in;
		_systemOut = new CapturingPrintStream( System.out );
		_systemErr = new CapturingPrintStream( System.err );
	}
	static InputStream in() { return _systemIn; }
	static CapturingPrintStream out() { return _systemOut; }
	static CapturingPrintStream err() { return _systemErr; }
	
	static class CapturingPrintStream extends PrintStream {
		private java.io.PrintStream original;
		private java.io.ByteArrayOutputStream baos;
		private boolean capturing;
		public CapturingPrintStream(PrintStream ps) {
			super(ps);
			original = ps;
		}
		public void startCapturing() {
			if (capturing) return;
			if (baos==null) baos = new java.io.ByteArrayOutputStream();
			else baos.reset();
			capturing = true;
		}
		public String stopCapturing() {
			if (!capturing) return null;
			String value = baos.toString();
			baos.reset();
			capturing = false;
			return value;
		}
		public boolean isCapturing() {
			return capturing;
		}
		
		public void write(int b) {
			original.write(b);
			if (baos!=null) baos.write(b);
		}
		public void write(byte[] buff, int off, int len) {
			original.write(buff, off, len);
			if (baos!=null) baos.write(buff, off, len);
		}
		public void flush() {
			original.flush();
			if (baos!=null) try { baos.flush(); } catch (java.io.IOException e) {}
		}
	}
	
	protected static EventManager eventManager;
	
	protected static boolean runEngine(String configFile, String[] args) throws Exception {
		boolean reload = false;
		Configuration config = null;
		eventManager = null;
		
		// If arguments are present then execute command and exit (unless -stay argument is present)
		boolean hasArgs = false;
		boolean keepLooping = false;
		for (int i=0; i<args.length; i++) {
			if (args[i]==null || args[i].trim().isEmpty()) continue;
			if ("-stay".equalsIgnoreCase(args[i].trim())) {
				keepLooping = true;
				//args[i] = "";
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
			//out().println(config);
			//out().println();
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
		commandQueue = new java.util.concurrent.LinkedBlockingQueue<String>();
		java.util.Scanner scan = null;
		do {
			String line;
			if (hasArgs) {
				// Command-line arguments mode
				// Create command line by concatenating command line arguments
				StringBuilder sb = new StringBuilder();
				for (int i=0; i<args.length; i++)
					if (args[i]!=null && !args[i].trim().equalsIgnoreCase("-stay")) 
						sb.append(" ").append(args[i]);
				line = sb.toString().trim();
				out().println("CLI: effective command-line: "+line);
				if (keepLooping) out().println("CLI: will enter interactive mode after command-line execution");
				hasArgs = false;
			} else {
				// Interactive mode
				// Initialize interactive input mechanism
				if (inputThread==null) initInput();
				
				// Get next command
				line = commandQueue.take();
				if (line==null) line="";
			}
			
			// Command execution
			try {
				CMD_CODE cc = doCommand(line);
				if (cc==CMD_CODE.LOOP) ; //keepLooping=true;
				else if (cc==CMD_CODE.QUIT) keepLooping=false;
				else if (cc==CMD_CODE.RELOAD) { reload=true; keepLooping=false; }
				executingCommand = false;
				// Send captured output to command listener (if any)
				sendCapturedOutput(cc);
			} catch (Exception e) {
				e.printStackTrace( err() );
				sendCapturedOutput(CMD_CODE.EXCEPTION);
				throw e;
			}
			
		} while (keepLooping);
		
		// Terminate execution of event manager
		out().println();
		out().println("CLI: Stopping Event Manager...");
		eventManager.stopManager();
		out().println("CLI: Event Manager stopped");
		
		return reload;		// if 'true' the engine will be restarted (with the same command-line arguments) and reload configuration
	}
	
	protected static java.util.concurrent.BlockingQueue<String> commandQueue;
	protected static Thread inputThread;
	protected static boolean inputKeepLooping;
	protected static boolean executingCommand;
	
	protected static void initInput() {
		inputKeepLooping=true;
		inputThread = new Thread(new Runnable() {
			public void run() {
				java.util.Scanner scan = new java.util.Scanner( in() );
				while (inputKeepLooping) {
					try {
						out().print("PULSAR> ");
						out().flush();
						while (!scan.hasNextLine()) Thread.sleep(200);
						String line = scan.nextLine().trim();
						sendCommand(line);
						waitCommand();
					} catch (InterruptedException e) {
						out().println("Interactive input thread was interrupted");
						inputKeepLooping=false;
					}
				}
			}
		});
		inputThread.setDaemon(true);
		inputThread.start();
	}
	protected static void stopInput() {
		inputKeepLooping=false;
		inputThread=null;
	}
	protected static void waitCommand() {
		while (executingCommand) try { Thread.sleep(50); } catch (InterruptedException e) {}
	}
	
	static interface CommandListener {
		//public abstract void out(String line);
		//public abstract void err(String line);
		public abstract void commandCompleted(CMD_CODE code, String out, String err);
	}
	
	protected static CommandListener commandListener;
	
	static void sendCommand(String line) { sendCommand(line, null, null); }
	static void sendCommand(String line, CommandListener cl) { sendCommand(line, null, cl); }
	static void sendCommand(String line, String prompt) { sendCommand(line, prompt, null); }
	
	static void sendCommand(String line, String prompt, CommandListener cl) {
		executingCommand = true;
		if (prompt!=null) { out().print(prompt); out().println(line); }
		try { commandQueue.put(line); } catch (InterruptedException e) { err().println("Interrupted while sending command: "+line); }
		// Start capturing output
		if (cl!=null) {
			out().startCapturing();
			err().startCapturing();
			commandListener = cl;
		}
	}
	
	protected static void sendCapturedOutput(CMD_CODE cc) {
		if (commandListener!=null) {
			String outStr = out().stopCapturing();
			String errStr = err().stopCapturing();
			commandListener.commandCompleted(cc, outStr, errStr);
			commandListener = null;
		}
	}
	
	static enum CMD_CODE { LOOP, QUIT, RELOAD, EXCEPTION };
	
	protected static CMD_CODE doCommand(String line) {
			String[] part = line.split("[ \t]+");
			String cmd = part[0].trim().toLowerCase();
			boolean ok = false;
			String helpId = null;
			if (cmd.equals("")) return CMD_CODE.LOOP;
			else if (cmd.equals("quit") || cmd.equals("q")) return CMD_CODE.QUIT;
			else if (cmd.equals("help") || cmd.equals("?") || cmd.equals("??")) ok=printHelp(part);
			else if (cmd.equals("reload") || cmd.equals("r")) { return CMD_CODE.RELOAD; }
			else if (cmd.equals("event") || cmd.equals("e")) { ok=doEventCmd(line, eventManager); helpId = "event"; }
			else if (cmd.equals("recommendation") || cmd.equals("recom") || cmd.equals("m")) { ok=doRecomCmd(part); helpId = "recom"; }
			else if (cmd.equals("feedback") || cmd.equals("f")) { ok=doFeedbackCmd(part, eventManager); helpId = "feedback"; }
			else if (cmd.equals("simulation") || cmd.equals("sim") || cmd.equals("s")) { ok=doSimulationCmd(part, eventManager); helpId = "simulation"; }
			else if (cmd.equals("web") || cmd.equals("w")) { ok=doWebCmd(part); helpId = "web"; }
			if (!ok) {
				out().println("CLI: Invalid or unknown command: "+cmd);
				//Print context-sensitive help
				if (helpId==null) ;
				else if (helpId.equals("event")) printContextHelp( eventManagerHelp() );
				else if (helpId.equals("recom")) printContextHelp( recomManagerHelp() );
				else if (helpId.equals("feedback")) printContextHelp( feedbackReporterHelp() );
				else if (helpId.equals("simulation")) printContextHelp( simulationHelp() );
				else if (helpId.equals("web")) printContextHelp( webHelp() );
			}
			return CMD_CODE.LOOP;
	}
	
	protected static boolean printHelp(String[] part) {
		boolean all = (part[0].equals("??") || part.length>1 && part[1].trim().equalsIgnoreCase("all"));
		out().println();
		out().println("***  PuLSaR Background Engine CLI commands  ***\n"+
				"\n(?) help     This help screen"+
				"\n(??)help all Detailed help screen"+
				"\n(q) quit     Exits PuLSaR"+
				"\n(r) reload   Reloads config by restarting engine. Contexts are preserved"+
				"\n(e) event    Event manager usage:"+
				(all ? eventManagerHelp() : "")+
				"\n(m) recom    Recommendation manager usage:"+
				(all ? recomManagerHelp() : "")+
				"\n(f) feedback Feedback reporting usage:"+
				(all ? feedbackReporterHelp() : "")+
				"\n(s) simulate Simulation feature usage:"+
				(all ? simulationHelp() : "")+
				"\n(w) web      Web service usage:"+
				(all ? webHelp() : "")+
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
				"\n             recom clear <USER>             Delete all user's recommendations"+
				"\n             recom threads SHOW|<#threads>  Get/Set thread pool size (>=1)";
	}
	protected static String feedbackReporterHelp() {
		return	"\n             feedback report [<CFG-FILE>]   Generates user feedback reports"+
				"\n             feedback schedule <PERIOD>|OFF Schedule report generation";
	}
	protected static String simulationHelp() {
		return	"\n             simulate gen-events <period-usec> <iterations> <event-templ-file>"+
				"\n             simulate iterate <delay-usec> <iterations> <event-templ-file>"+
				"\n             simulate iterate2 <thread-pool-size> <delay-usec> <iterations> <event-templ-file>" ;
	}
	protected static String webHelp() {
		return	"\n             web status                     Reports Jetty server status"+
				"\n             web start [<port>]             Starts Jetty for PuLSaR web service"+
				"\n             web stop                       Stops Jetty"+
				"\n             web restart                    Restarts Jetty"+
				"\n             web config [<port> <stop-port> <stop-key>] " +
				"\n                                            Shows/Sets Jetty configuration" ;
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
				else if (param.equals("SOURCE")) { String type = (eventManager instanceof LocalLoopEventManager) ? "local" : "pubsub"; out().println("CLI: EventManager source: "+type); }
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
			if (rmCmd.equals("clear")) {
				if (part.length<3) return false;
				boolean clearProfile = (part.length>3);
				String sc = part[2].trim();						// user
				
				if (recomMgntWs==null) recomMgntWs = new eu.brokeratcloud.rest.opt.RecommendationManagementService();
				if (recomManager==null) recomManager = RecommendationManager.getInstance();
				recomManager.setEventManager(eventManager);
				int cntSucc = 0;
				int cntFail = 0;
				Recommendation[] list;
				if (clearProfile) {
					if (part.length<4) return false;
					String id = part[3].trim();					// profile id
					list = recomMgntWs.getRecommendations(sc, id, true);
				} else {
					list = recomMgntWs.getAllRecommendations(sc, true);
				}
				if (list==null || list.length==0) out().println("No recommendations found");
				else {
					out().println( String.format("Deleting %d recommendations", list.length) );
					for (int i=0; i<list.length; i++) {
						String recomId = list[i].getId();
						out().print( String.format("%3d. %s... ", i, recomId) );
						int status = recomMgntWs.deleteRecommendation(recomId).getStatus();
						String outcome = (status<299) ? "Deleted" : "An error occurred. Recommendation was not deleted";
						out().println( outcome );
						if (status<299) cntSucc++; else cntFail++;
					}
					String err = (cntFail>0) ? " and "+cntFail+" error(s) occurred" : "";
					out().println(cntSucc+" recommendations deleted"+err);
				}
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
	
	protected static boolean doFeedbackCmd(String[] part, EventManager eventManager) {
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
				
				// Set event manager
				fbReporter.setEventManager(eventManager);
				
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
				fbReporter.setEventManager(CLI.eventManager);
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
			} else
			if (simCmd.equals("iterate2")) {
				try {
					int psize = Integer.parseInt( part[2] );
					long delay = Long.parseLong( part[3] );
					long iterations = Long.parseLong( part[4] );
					String evtTpl = readFile( part[5] );
				
					// Run feedback reports generation engine immediately
					if (CLI.eventManager instanceof LocalLoopEventManager)
						((LocalLoopEventManager)CLI.eventManager).getRecommendationManager().setThreadPoolSize(psize);
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
	
	// Used by SimRunner class
	public static Process execProcess(String cmd) throws java.io.IOException {
		// Prepare shell command
		String[] cmdParts = cmd.split("[ \t\r]+");
		
		// Run a simulation (test) in a separate system process
		ProcessBuilder pb = new ProcessBuilder(cmdParts);
		//pb.directory(new File("myDir"));
		//File log = new File("log");
		//pb.redirectErrorStream(true);
		//pb.redirectOutput(Redirect.appendTo(log));
		pb.inheritIO();
		Process p = pb.start();
		//assert pb.redirectInput() == Redirect.PIPE;
		//assert pb.redirectOutput().file() == log;
		//assert p.getInputStream().read() == -1;
		return p;
	}
	
	protected static int webServerPortDefault = 9090;
	protected static int webServerPort = 9090;
	protected static Process webServerProcess = null;
	protected static int webServerStopPort = 28282;
	protected static String webServerStopKey = "stop_jetty";
	
	protected static boolean doWebCmd(String[] part) {
		try {
			if (part.length<2) return false;
			String webCmd = part[1].trim().toLowerCase();
			if (webCmd.equals("status")) {
				statusWebServer();
			} else
			if (webCmd.equals("start")) {
				int port = webServerPortDefault;
				if (part.length>=3) port = Integer.parseInt( part[2] );
				out().println("Starting web server on port "+port+"...");
				startWebServer(port);
			} else
			if (webCmd.equals("stop")) {
				out().println("Stopping web server...");
				stopWebServer();
			} else
			if (webCmd.equals("restart")) {
				stopWebServer();
				startWebServer(webServerPort);
			} else
			if (webCmd.equals("config")) {
				if (part.length>4) {
					int port = Integer.parseInt(part[2]);
					int stopPort = Integer.parseInt(part[3]);
					String stopKey = part[4].trim();
					if (stopKey.isEmpty()) throw new IllegalArgumentException("Invalid web configuration. Ports must be between 1024 and 65536 and stop key not empty");
					webServerPortDefault = port;
					webServerStopPort = stopPort;
					webServerStopKey = stopKey;
					out().println("Web configuration changed");
					out().println("New settings will take effect after web server start/restart");
				} else
				if (part.length>=3 && part.length<=4) {
					err().println("Missing web configuration parameters");
					return false;
				}
				out().println(String.format("WEB CONFIG: default-port=%d, stop-port=%d, stop-key=%s",
											webServerPortDefault, webServerStopPort, webServerStopKey));
			} else {
				return false;
			}
			return true;
		} catch (Exception e) {
			err().println("WEB: An error occurred: "+e);
			e.printStackTrace(err());
			return false;
		}
	}
	
	protected static void statusWebServer() {
		try {
			java.net.InetAddress addr;
			java.net.Socket sock = new java.net.Socket("localhost", webServerPort);
			addr = sock.getInetAddress();
			sock.close();
			out().println("Web server is running on port "+webServerPort);
		} catch (java.io.IOException e) {
			out().println("Web server is not running");
		}
	}
	
	protected static void startWebServer(int port) throws java.io.IOException {
		// Find BASE DIR using CLI.class file path
		String cwd = getCwd();
		boolean isWin = isWindows();
		//out().println("CWD: "+cwd);
		
		// Start web server
		try {
			java.util.List<String> cmdarray = new java.util.ArrayList<String>();
			cmdarray.add(getJreExecutable().toString());
			cmdarray.add("-DVERBOSE");
			cmdarray.add("-Dfile.encoding=UTF-8");
			cmdarray.add("-jar");
			if (isWin) cmdarray.add(cwd+"\\bin\\jetty-runner+ssi.jar");
			else cmdarray.add(cwd+"/bin/jetty-runner+ssi.jar");
			cmdarray.add("--port");
			cmdarray.add(""+port);
			if (isWin) cmdarray.add(cwd+"\\target\\PuLSaR");
			else cmdarray.add(cwd+"/target/PuLSaR");
			cmdarray.add("--stop-port");
			cmdarray.add(""+webServerStopPort);
			cmdarray.add("--stop-key");
			cmdarray.add(webServerStopKey);
			//out().println("COMMAND: "+cmdarray);
			
			try {
				webServerProcess = launch(cmdarray);
				out().println("Started");
				webServerPort = port;
			} catch (Exception e) {
				err().println("Failed to start web server on port " + port);
			}
		} catch(Exception e) { throw new RuntimeException(e); }
	}
	
	protected static void stopWebServer() throws java.io.IOException {
		// Find BASE DIR using CLI.class file path
		String cwd = getCwd();
		boolean isWin = isWindows();
		//out().println("CWD: "+cwd);
		
		// Stop web server
		try {
			java.util.List<String> cmdarray = new java.util.ArrayList<String>();
			cmdarray.add(getJreExecutable().toString());
			cmdarray.add("-DSTOP.PORT="+webServerStopPort);
			cmdarray.add("-DSTOP.KEY="+webServerStopKey);
			cmdarray.add("-jar");
			if (isWin) cmdarray.add(cwd+"\\bin\\start.jar");
			else cmdarray.add(cwd+"/bin/start.jar");
			cmdarray.add("--stop");
			//out().println("COMMAND: "+cmdarray);
			
			try {
				launch(cmdarray);
				out().println("Stopped");
				webServerProcess = null;
			} catch (Exception e) {
				err().println("Failed to start web server on port " + webServerPort);
			}
		} catch(Exception e) { throw new RuntimeException(e); }
	}
	
	private static String getCwd() {
		String cwd = System.getenv("PULSAR_HOME");
		if (cwd==null || cwd.trim().isEmpty()) cwd = System.getenv("BASE_DIR");
		if (cwd==null || cwd.trim().isEmpty()) cwd = System.getenv("BASEDIR");
		if (cwd==null || cwd.trim().isEmpty()) {
			java.net.URL main = CLI.class.getResource("CLI.class");
			if (!"file".equalsIgnoreCase(main.getProtocol()))
				throw new IllegalStateException("CLI class is not stored in a file.");
			java.io.File path = new java.io.File(main.getPath());
			cwd = path.toString();
			cwd = cwd.replace("\\target\\classes\\eu\\brokeratcloud\\opt\\engine\\CLI.class", "");
			cwd = cwd.replace("\\pulsar\\classes\\eu\\brokeratcloud\\opt\\engine\\CLI.class", "");
			cwd = cwd.replace("/target/classes/eu/brokeratcloud/opt/engine/CLI.class", "");
			cwd = cwd.replace("/pulsar/classes/eu/brokeratcloud/opt/engine/CLI.class", "");
		}
		return cwd;
	}
	
	private static boolean isWindows() {
		String os = System.getProperty("os.name");
		if (os == null) {
			throw new IllegalStateException("os.name");
		}
		os = os.toLowerCase();
		return os.startsWith("windows");
	}

	protected static java.io.File getJreExecutable() throws java.io.FileNotFoundException {
		String jreDirectory = System.getProperty("java.home");
		if (jreDirectory == null) {
			throw new IllegalStateException("java.home");
		}
		java.io.File exe;
		if (isWindows()) {
			exe = new java.io.File(jreDirectory, "..\\bin\\java.exe");
		} else {
			exe = new java.io.File(jreDirectory, "../bin/java");
		}
		if (!exe.isFile()) {
			throw new java.io.FileNotFoundException(exe.toString());
		}
		return exe;
	}

	protected static Process launch(java.util.List<String> cmdarray) throws java.io.IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(cmdarray);
		processBuilder.inheritIO();
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		return process;
	}
}
