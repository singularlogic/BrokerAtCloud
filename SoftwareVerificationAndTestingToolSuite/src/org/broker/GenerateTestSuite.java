package org.broker;

import java.io.File;
import java.io.IOException;

import org.broker.model.Service;
import org.broker.model.TestSuite;
import org.jast.ast.ASTReader;
import org.jast.ast.ASTWriter;

/**
 * Program that reads an XML service specification and generates a concrete
 * suite of high-level tests from the specification.  
 * 
 * Requires ASTReader from the JAST package.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class GenerateTestSuite {

	/**
	 * Name of the input file.  Change to suit.
	 */
	public static String inputFile = "HolidayBooking.xml";
	
	/**
	 * Name of the output file.  Change to suit.
	 */
	public static String outputFile = "HolidayTests.xml";
	
	/**
	 * Depth to which transitions are tested.  Change to suit.
	 */
	public static int testDepth = 3;
	public static boolean multiObjective = true;

	/**
	 * Reads an XML service specification from the input file, and, if no
	 * faults are found, generates a high-level test suite.
	 * @param args empty.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Starting program: GenerateTestSuite.\n");
						
		ASTReader reader = new ASTReader(new File(inputFile));
		reader.usePackage("org.broker.model");
		Service service = (Service) reader.readDocument();
		reader.close();
		
		System.out.println("Unmarshalled the model from input file: " + inputFile);
			
		TestSuite testSuite = service.generateTestSuite(testDepth, multiObjective);
						
		ASTWriter writer = new ASTWriter(new File(outputFile));
		writer.writeDocument(testSuite);
		writer.close();

		System.out.println("Marshalled the test suite to output file: " + outputFile);

		System.out.println("\nProgram completed with success.");
	}

}
