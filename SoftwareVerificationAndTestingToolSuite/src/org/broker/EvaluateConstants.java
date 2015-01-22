package org.broker;

import java.io.File;
import java.io.IOException;

import org.broker.model.Memory;
import org.broker.model.Parameter;
import org.jast.ast.ASTReader;

/**
 * Program that reads a Memory full of Constants and evaluates them.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class EvaluateConstants {

	/**
	 * Name of the input file.  Change to suit.
	 */
	public static String inputFile = "Memory.xml";
	
	/**
	 * Reads an XML Memory specification from the input file, and, if no
	 * faults are found, checks that every Constant in the Memory can be
	 * converted into some kind of Object value.
	 * @param args empty.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void main(String[] args) throws IOException {
		
		System.out.println("Starting program: EvaluateConstants.\n");
		
		ASTReader reader = new ASTReader(new File(inputFile));
		reader.usePackage("org.broker.model");
		Memory memory = (Memory) reader.readDocument();
		reader.close();
		
		System.out.println("Unmarshalled the model from input file: " + 
				inputFile + "\n");
		
		for (Parameter param : memory.getParameters()) {
			Object value = param.evaluate();
			String nameType = param.getName() + ":" + param.getType();
			String xmlKind = "XML " + param.getClass().getSimpleName() + " ";
			if (value != null) {
				String javaType = value.getClass().getSimpleName();
				System.out.println(xmlKind + nameType +
						" has Java value " + value + ":" + javaType);
			}
			else {
				System.out.println(xmlKind + nameType +
						" has Java value " + null);
			}
		}
		
		System.out.println("\nProgram completed with success.");


	}

}
