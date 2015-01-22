package org.broker;

import java.io.File;
import java.io.IOException;

import org.broker.model.Language;
import org.broker.model.Machine;
import org.broker.model.Service;
import org.jast.ast.ASTReader;
import org.jast.ast.ASTWriter;

/**
 * Program that reads an XML service specification and generates an abstract
 * set of test sequences from the specification.  The set of sequences shows
 * the paths through the finite state machine which should be accepted, or
 * rejected.  The paths form a bounded language, up to a certain length of
 * sequence.  Each sequence is a series of events that indicate the triggering
 * of particular transitions.  
 * 
 * Requires ASTReader from the JAST package.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class GenerateSequences {
	
	/**
	 * Name of the input file.  Change to suit.
	 */
	public static String inputFile = "ContactService.xml";
	
	/**
	 * Name of the output file.  Change to suit.
	 */
	public static String outputFile = "ContactSeq.xml";
	
	/**
	 * Depth to which transitions are tested.  Change to suit.
	 */
	public static int testDepth = 0;

	/**
	 * Reads an XML service specification from the input file, and, if no
	 * faults are found, generates a high-level test suite.
	 * @param args empty.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Starting program: GenerateSequences.\n");
						
		ASTReader reader = new ASTReader(new File(inputFile));
		reader.usePackage("org.broker.model");
		Service service = (Service) reader.readDocument();
		reader.close();
		
		System.out.println("Unmarshalled the model from input file: " + inputFile);
			
		Machine machine = service.getMachine();
		Language testSuite = machine.getTransitionCover(testDepth);
		
		ASTWriter writer = new ASTWriter(new File(outputFile));
		writer.writeDocument(testSuite);
		writer.close();

		System.out.println("Marshalled the sequences to output file: " + outputFile);

		System.out.println("\nProgram completed with success.");
	}

}
