package org.broker.model;

/**
 * Input is the output parameter in the payload of an event.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Output extends Parameter {

	/**
	 * Creates a default output parameter.
	 */
	public Output() {
	}

	/**
	 * Creates an output parameter with the given name and type.
	 * @param name the name of this output.
	 * @param type the type of this output.
	 */
	public Output(String name, String type) {
		super(name, type);
	}

}
