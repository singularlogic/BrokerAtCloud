package org.broker.model;

/**
 * SemanticError reports semantic construction errors when building the model.
 * A SemanticError is not recoverable, but indicates that the source for the
 * model must be repaired.  The Element in which the error occurred may be
 * accessed.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
@SuppressWarnings("serial")
public class SemanticError extends Error {
	
	/**
	 * The Element in which this SemanticError occurred.
	 */
	private Element element;
	
	/**
	 * Creates a SemanticError with the given error text.
	 * @param text the error text.
	 */
	SemanticError(String message) {
		super("Error: " + message);
	}
	
	/**
	 * Creates a SemanticError with the given error text and faulty Element.
	 * @param text the error text.
	 * @param element the Element in which the error occurred.
	 */
	SemanticError(String message, Element element) {
		super(message);
		this.element = element;
	}
	
	/**
	 * Returns the faulty Element in which this SemanticError occurred.
	 * @return the faulty Element.
	 */
	public Element getElement() {
		return element;
	}
 
}
