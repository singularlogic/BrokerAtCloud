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
package eu.brokeratcloud.opt.engine.sim;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Arrays;
import java.text.SimpleDateFormat;
import eu.brokeratcloud.opt.engine.CLI;

/**
 *	Simulation runner class
 */
public class SimRunner {
	public static void main(String[] args) {
		try {
			_run(args);
		} catch (Exception e) {
			if (e instanceof ArrayIndexOutOfBoundsException) err("Missing argument(s)...");
			else e.printStackTrace(err());
			exit(9, null, true);
		}
	}
	
	public static void printHelp() {
		err().println("\n"+
			"Usage: SimRunner <START-SCRIPT> <STOP-SCRIPT> <RUN-A-TEST-SCRIPT>\n"+
			"                 <TEST-SET-NAME> <#ATTRS-LIST> <#SERVICES-LIST>\n"+
			"                 <#PROFILES-LIST> <#CRITERIA-LIST> <ITERATIONS> <DELAY>\n"
		);
	}
	
	public static void _run(String[] args) throws Exception {
		out("START: arguments = "+Arrays.toString(args));
		
		// Configuration
		int i=0;
		String startScript = args[i++].trim();
		String stopScript = args[i++].trim();
		String runScript = args[i++].trim();
		if (startScript.isEmpty() || stopScript.isEmpty() || runScript.isEmpty()) {
			exit(1, "Missing start/stop/run script", true);
		}
		
		// Prepare variables
		String testname = args[i++].trim();
		if (testname.isEmpty()) exit(1, "Missing Test name");
		String[] attrVals = args[i++].split("[ \\t,;:]+");
		String[] srvVals  = args[i++].split("[ \\t,;:]+");
		String[] profVals = args[i++].split("[ \\t,;:]+");
		String[] critVals = args[i++].split("[ \\t,;:]+");
		
		for (int j=0; j<attrVals.length; j++) attrVals[j]=attrVals[j].trim();
		for (int j=0; j<srvVals.length;  j++) srvVals[j] =srvVals[j].trim();
		for (int j=0; j<profVals.length; j++) profVals[j]=profVals[j].trim();
		for (int j=0; j<critVals.length; j++) critVals[j]=critVals[j].trim();
		
		int iterations = Integer.parseInt(args[i++]);
		int delay = Integer.parseInt(args[i++]);
		
		if (iterations<=0 || delay<0) exit(2, "Negative iteration or delay argument");
		
		int nComb = attrVals.length * srvVals.length * profVals.length * critVals.length;
		out( String.format("Scripts: start=%s, stop=%s, run=%s", startScript, stopScript, runScript) );
		out( String.format("Params: #attr=%d, #serv=%d, #prof=%d, #crit=%d => %d combinations", attrVals.length, srvVals.length, profVals.length, critVals.length, nComb) );
		out( String.format("Params: iteration=%d, delay=%d", iterations, delay) );
		printSeparator();
		
		// Initialize test environment
		logStart();
		String workdir = testname+"-"+new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss").format(new Date()) ;
		String logfile = String.format("%s\\LOG-OUTPUT.txt", workdir);
		Process pInit = CLI.execProcess( String.format("%s %s %s %s", startScript, testname, workdir, logfile) );
		int exitValue = pInit.waitFor();
		if (exitValue!=0) {
			exit(exitValue, "Test environment initialization failed! YOU MUST INITIALIZE IT MANUALLY");
			return;
		}
		logEnd("Initialization duration: %dms");
		printSeparator();
		
		// Run test
		for (String attr : attrVals)
		for (String srv  : srvVals)
		for (String prof : profVals)
		for (String crit : critVals) {
			if (attr.isEmpty() || srv.isEmpty() || prof.isEmpty() || crit.isEmpty()) {
				err( String.format("Invalid combination: attribute=%s, service=%s, profile=%s, criteria=%s", attr, srv, prof, crit) );
				printSeparator();
				continue;
			}
			
			String runid = String.format("%s-%s-%s-%s-%s", testname, attr, srv, prof, crit);
			String outfile = String.format("%s\\rdf-%s-%s-%s-%s.ttl", workdir, attr, srv, prof, crit);
			String descr = String.format("Test set=%s, attributes=%s, services=%s, profiles=%s, criteria=%s", testname, attr, srv, prof, crit);
			//String descr = String.format("Test_%s", runid);
			String cmdLine = String.format("%s %s %s \"%s\" %s %s %s %s %d %d %s", runScript, runid, outfile, descr, attr, srv, prof, crit, iterations, delay, logfile);
			
			out( String.format("Test Run: BEGIN: name=%s, attribute=%s, service=%s, profile=%s, criteria=%s", testname, attr, srv, prof, crit) );
			out( String.format("Test Run: CMD: %s", cmdLine) );
			logStart();
			Process pTest = CLI.execProcess( cmdLine );
			exitValue = pTest.waitFor();
			if (exitValue!=0) {
				exit(exitValue, "Test execution failed! EXITING TEST SESSION  (error code: "+exitValue+")");
				return;
			}
			logEnd("Test duration: %dms");
			out( String.format("Test Run: END: name=%s", runid) );
			printSeparator();
		}
		
		// Shutdown test environment
		logStart();
		Process pShutdown = CLI.execProcess( String.format("%s %s %s %s", stopScript, testname, workdir, logfile) );
		exitValue = pShutdown.waitFor();
		if (exitValue!=0) {
			exit(exitValue, "Test environment shutdown failed! YOU MUST TERMINATE IT MANUALLY");
			return;
		}
		logEnd("Shutdown duration: %dms");
		printSeparator();
		
		// Exit
		exitValue = 0;
		out("END: exit-value = "+exitValue);
		//exit( exitValue );
	}
	
	protected static String tag = "SimRun: ";
	
	protected static void out(String mesg) { out().println(tag+mesg); }
	protected static void err(String mesg) { err().println(tag+mesg); }
	protected static PrintStream out() { return System.out; }
	protected static PrintStream err() { return System.err; }
	
	protected static void exit(int exitValue) { exit(exitValue, null, false); }
	protected static void exit(int exitValue, String mesg) { exit(exitValue, mesg, false); }
	protected static void exit(int exitValue, String mesg, boolean help) { if (mesg!=null) err(mesg); if (help) printHelp(); System.exit( exitValue ); }
	
	protected static String sepLine = null;
	protected static void printSeparator() {
		if (sepLine==null) sepLine = new String(new char[79]).replace("\0", "-");
		out().println( sepLine );
	}
	protected static long startTm, endTm;
	protected static void logStart() { startTm = System.currentTimeMillis(); }
	protected static void logEnd(String mesg) {
		endTm = System.currentTimeMillis();
		long dur = endTm-startTm;
		out().println(String.format(mesg, dur));
	}
}