package eu.brokeratcloud.opt.engine.sim;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.tokenParser.TokenParser;


/**
 *	Template-based RDF/TTL generator for simulation
 */
public class genRDF {
	public static void main(String[] args) throws Exception {
		try {
			out("PuLSaR template-based RDF/TTL file generator");
			out("Args: "+Arrays.toString(args));
			int exitValue = doTheJob(args);
			if (exitValue!=0) System.exit( exitValue );
		} catch (Exception e) {
			e.printStackTrace(err());
			System.exit( -99 );
		}
	}
	
	public static int doTheJob(String[] args) throws Exception {
		String tplFile;
		String outFile;
		String fileDescr;
		int nAttributes = 0;
		int nServices = 0;
		int nProfiles = 0;
		int nCriteria = 0;
		int i = 0;
		
		// Check command-line arguments
		if (args.length<7) {
			err("Missing arguments");
			err("Usage: java "+genRDF.class.getName()+" <TEMPLATE> <OUT-FILE> <FILE-DESCR> <#Attribs> <#Services> <#Profiles> <#Criteria>");
			return -1;
		}
		tplFile = args[i++];
		outFile = args[i++];
		fileDescr = args[i++];
		if (tplFile==null || tplFile.trim().isEmpty()) { err("Missing or invalid template file"); return -2; }
		if (outFile==null || outFile.trim().isEmpty()) { err("Missing or invalid output file"); return -3; }
		if (fileDescr==null || fileDescr.trim().isEmpty()) { err("Missing or invalid file description"); return -4; }
		try {
			nAttributes = Integer.parseInt( args[i++] );
			nServices = Integer.parseInt( args[i++] );
			nProfiles = Integer.parseInt( args[i++] );
			nCriteria = Integer.parseInt( args[i++] );
		} catch (NumberFormatException e) {
			err("Invalid #Attribs or #Services or #Profiles or #Criteria argument.\nThey must be positive integers or zero");
			return -5;
		}
		if (nAttributes<0 || nServices<0 || nProfiles<0 || nCriteria<0) {
			err("Negative #Attribs or #Services or #Profiles or #Criteria argument.\nThey must be positive integers or zero");
			return -6;
		}
		
		// Get template
		PebbleEngine engine = new PebbleEngine(new FileLoader());
		engine.addExtension(new AbstractExtension() {
			public List<TokenParser> getTokenParsers() {
				List<TokenParser> list = new Vector<TokenParser>();
				list.add(new RepeatTokenParser());
				return list;
			}
		});
		PebbleTemplate compiledTemplate = null;
		try {
			compiledTemplate = engine.getTemplate(tplFile);
		} catch (Exception e) {
			err("Could not open template file: "+tplFile+"\nReason: "+e);
			return -11;
		}
		Writer writer = new StringWriter();
		
		// Generate RDF
		try {
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("description", fileDescr);
			context.put("genDate", new Date());
			context.put("nAttributes", nAttributes);
			context.put("nServices", nServices);
			context.put("nProfiles", nProfiles);
			context.put("nCriteria", nCriteria);
			out("Context: "+context);
			out("Generating RDF file using template: "+tplFile);
			compiledTemplate.evaluate(writer, context);
			out("Done");
		} catch (Exception e) {
			err("Generation failed: "+e);
			return -12;
		}
		
		// Write compiled file into output file
		try {
			String output = writer.toString();
			//out("\n"+output);
			out("Writing output to file: "+outFile);
			if (writeFile( outFile, output )) {
				out("Output saved in "+outFile);
			} else {
				return -14;
			}
		} catch (Exception e) {
			err("Generation failed: "+e);
			return -13;
		}
		
		// Success
		return 0;
	}
	
	public static boolean writeFile(String fileName, String content) throws java.io.IOException {
		PrintWriter pw = new PrintWriter(new java.io.FileWriter(fileName, false));	// No append
		try {
			pw.print(content);
			if (pw.checkError()) { err("An error occured while openning or writing output file: "+fileName); return false; }
		} finally {
			pw.close();
			if (pw.checkError()) { err("An error occured while closing output file: "+fileName); return false; }
		}
		return true;
	}
	
	protected static void out(String mesg) { System.out.println("genRDF: "+mesg); }
	protected static void err(String mesg) { System.err.println("genRDF: "+mesg); }
	protected static PrintStream err() { return System.err; }
}