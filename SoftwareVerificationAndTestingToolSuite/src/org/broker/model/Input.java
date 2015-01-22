package org.broker.model;


/**
 * Input is the input parameter in the payload of an event.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Input extends Parameter {
	
	/**
	 * Creates a default input parameter.
	 */
	public Input() {
	}
	
	/**
	 * Creates an input parameter with the given name and type.
	 * @param name the name of this input.
	 * @param type the type of this input.
	 */
	public Input(String name, String type) {
		super(name, type);
	}

}
