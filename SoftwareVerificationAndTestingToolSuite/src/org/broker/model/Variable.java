package org.broker.model;


/**
 * Variable is a state parameter representing part of the system's memory.
 * A Variable is a modifiable state parameter that is initialised to a value
 * prior to the start of a system's execution.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Variable extends Parameter {

	/**
	 * Creates a default Variable parameter.
	 */
	public Variable() {
	}

	/**
	 * Creates a variable parameter with the given name and type.
	 * @param name the name of this Variable.
	 * @param type the type of this Variable.
	 */
	public Variable(String name, String type) {
		super(name, type);
	}
	
	/**
	 * Takes a snapshot of this Variable.  Clones this Variable, and converts
	 * the bound value of the clone into a String.  Used when creating a TestStep,
	 * to store a snapshot of the Variable value.
	 * @return a clone of this Variable, with a snapshot of its value.
	 */
	@Override
	public Variable snapshot() {
		Variable result = new Variable(name, type);
		if (value != null)
			result.setContent(value.toString());
		return result;
	}

}
