package org.broker.model;



/**
 * Effect represents a particular set of value-modifications to parameters.
 * An Effect is a kind of Binding that occurs as a consequence of executing
 * a given Scenario in an Operation.  Whereas a Binding sets the initial
 * variable bindings, the Effect sets the consequential bindings.  A Effect
 * may bind an Output or a Variable, only.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Effect extends Binding {
	
	/**
	 * Creates a default Effect.
	 */
	public Effect() {
	}
	
	/**
	 * Adds an Assignment to this Effect.  Each Assignment in this Effect
	 * must bind a unique Parameter.
	 * @param assignment the Assignment to add.
	 * @return this Effect.
	 */
	public Effect addAssignment(Assignment assignment) {
		return (Effect) super.addAssignment(assignment);
	}
	
}

