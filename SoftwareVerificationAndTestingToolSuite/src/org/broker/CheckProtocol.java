package org.broker;

import java.io.File;
import java.io.IOException;

import org.broker.model.Service;
import org.jast.ast.ASTReader;
import org.jast.ast.ASTWriter;

/**
 * Program that reads an XML service specification and checks the protocol of
 * the service for input completeness.  Use this program to determine whether
 * each operation behaves deterministically and does not block under certain
 * input conditions.  Warnings are given if a given input partition triggers
 * no response (blocking) or multiple responses (non-determinism).  These
 * indicate likely faults in the specification that should be fixed.
 * 
 * Requires ASTReader from the JAST package.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class CheckProtocol {

	/**
	 * Name of the input file.  Change to suit.
	 */
	public static String inputFile = "HolidayBooking.xml";
	
	/**
	 * Name of the output file.  Change to suit.
	 */
	public static String outputFile = "HolidayVerification.xml";

	/**
	 * Reads an XML service specification from the input file, and, if no
	 * faults are found, checks the protocol of the specification for input
	 * completeness.  For every operation, creates a set of input partitions,
	 * which includes every guard and its complement.  Then, determines
	 * whether each partition triggers exactly one response.  If no response
	 * is triggered, or more than one response is triggered, this indicates
	 * a possible fault in the specification.  Uses a symbolic subsumption
	 * checking algorithm, which is conservative.  Not all warnings may 
	 * eventually be errors.
	 * @args empty.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void main(String[] args) throws IOException {
		
		System.out.println("Starting program: CheckProtocol.\n");
		
		ASTReader reader = new ASTReader(new File(inputFile));
		reader.usePackage("org.broker.model");
		Service service = (Service) reader.readDocument();
		reader.close();
		
		System.out.println("Unmarshalled the model from input file: " + inputFile);
						
		service.verifyProtocol();
		
		ASTWriter writer = new ASTWriter(new File(outputFile));
		writer.usePackage("org.broker.model");
		writer.writeDocument(service.getProtocol());
		writer.close();

		System.out.println("Marshalled the validation to output file: " + outputFile);

		System.out.println("\nProgram completed with success.");
	}
	
}
