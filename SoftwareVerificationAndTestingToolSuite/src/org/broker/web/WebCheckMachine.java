package org.broker.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import org.broker.model.Service;
import org.jast.ast.ASTError;
import org.jast.ast.ASTReader;
import org.jast.ast.ASTWriter;

/**
 * WebCheckMachine is a CGI program to check the state machine of a cloud 
 * service.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class WebCheckMachine {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BufferedReader input = null;
		String postData = null;
		Map<String, String> form = new LinkedHashMap<String, String>();
		try {
			input = new BufferedReader(new InputStreamReader(System.in));
			postData = URLDecoder.decode(input.readLine(), "UTF-8");
		    for (String entry : postData.split("&")) {
		        int pos = entry.indexOf("=");
		        if (pos != -1) {
		          String key = entry.substring(0, pos);
		          String val = entry.substring(pos + 1);
		          form.put(key, val);
		        }
		        else 
		        	throw new IOException("Badly formatted web form data");
		    }
		    URL url = new URL(form.get("url"));
		    ASTReader reader = new ASTReader(url, "UTF-8");
			reader.usePackage("org.broker.model");
			Service service = (Service) reader.readDocument();
			reader.close();
			
			service.validateMachine();
			
			PrintWriter printer = new PrintWriter(System.out);
			ASTWriter writer = new ASTWriter(printer, "UTF-8");
			writer.usePackage("org.broker.model");
	        printer.println("Content-Type: text/xml\n");
			writer.writeDocument(service.getMachine());
			writer.close();
			
		}
		catch (ASTError ex) {
			usageError(ex);
		}
		catch (Throwable ex) {
			serviceError(ex);
		}
	}
	
	  /**
	   * Prints a service error text in HTML.
	   */
	  private static void serviceError(Throwable ex) {
	        System.out.println("Content-Type: text/html\n");
	        System.out.println("<html><head>");
	        System.out.println("<title>Service Error</title>");
	        System.out.println("</head><body>");
	        System.out.println("<h2>Service Error</h2>");
	        System.out.println("<p>The web service failed while processing" +
	        		" your request.  The reason for the failure is given" +
	        		" below as a stack backtrace.</p>");
	        System.out.println("<pre>");
	        ex.printStackTrace();
	        System.out.println("</pre>");
	        System.out.println("</body></html>");
	  }
	  
	  /**
	   * Prints a validation error text in HTML.
	   */
	  private static void usageError(Error ex) {
	        System.out.println("Content-Type: text/html\n");
	        System.out.println("<html><head>");
	       System.out.println("<title>Usage Error</title>");
	        System.out.println("</head><body>");
	        System.out.println("<h2>Usage Error</h2>");
	        System.out.println("<p>" + ex.getMessage() + "</p>");
	        System.out.println("<p>The web service determined that the" +
	        		" input specification was invalid.  Please correct" +
	        		" the input specification and resubmit it.</p>");
	        System.out.println("</body></html>");
	  }

}
