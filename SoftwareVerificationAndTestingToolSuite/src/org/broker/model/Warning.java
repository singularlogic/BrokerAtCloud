package org.broker.model;

/**
 * Warning is a kind of Notice indicating a fault in the associated model.
 * A Warning behaves exactly like a Notice, but is differently named to flag
 * a more serious condition in the model.  A Warning may be used to notify
 * a lack of correspondence, a lack of completeness, or failure to cover all
 * states and transitions.  A Warning indicates some serious condition which
 * the user should address in the model specification.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Warning extends Notice {
	
	/**
	 * Creates an empty Warning.
	 */
	public Warning() {
	}
	
	/**
	 * Creates a Warning with the given text.
	 * @param text the text.
	 */
	public Warning(String message) {
		super(message);
	}


}
