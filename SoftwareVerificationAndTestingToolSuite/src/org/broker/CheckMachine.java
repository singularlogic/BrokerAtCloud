package org.broker;

import java.io.File;
import java.io.IOException;

import org.broker.model.Service;
import org.jast.ast.ASTReader;
import org.jast.ast.ASTWriter;

/**
 * Program that reads an XML service specification into memory and checks the
 * finite state machine for transition completeness.  Use this program to 
 * determine which states have missing transitions.  Missing transitions are
 * allowed, but will be interpreted during testing as ignored events that 
 * should trigger null operations.
 * 
 * Requires ASTReader from the JAST package.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class CheckMachine {

	/**
	 * Name of the input file.  Change to suit.
	 */
	public static String inputFile = "HolidayBooking.xml";
	
	/**
	 * Name of the output file.  Change to suit.
	 */
	public static String outputFile = "HolidayValidation.xml";
	
	
	/**
	 * Reads an XML service specification from the input file, and, if no
	 * faults are found, checks the finite state machine for transition
	 * completeness.  For every state, attempts to find a response to every
	 * possible event.  Reports those events for which no transition is 
	 * triggered in a given state.
	 * @param args empty.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Starting program: CheckMachine.\n");
		
		ASTReader reader = new ASTReader(new File(inputFile));
		reader.usePackage("org.broker.model");
		Service service = (Service) reader.readDocument();
		reader.close();
		
		System.out.println("Unmarshalled the model from input file: " + inputFile);
		
		service.validateMachine();
		
		ASTWriter writer = new ASTWriter(new File(outputFile));
		writer.usePackage("org.broker.model");
		writer.writeDocument(service.getMachine());
		writer.close();

		System.out.println("Marshalled the validation to output file: " + outputFile);

		System.out.println("\nProgram completed with success.");

	}

}
