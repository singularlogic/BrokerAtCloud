package org.broker.model;


/**
 * Analysis is a kind of Notice analysing a property of the associated model.
 * An Analysis behaves exactly like a Notice, but is differently named to flag
 * an observed or inferred property of the model.  An Analysis may be used to
 * report ignored transitions in the Machine, the number of input partitions
 * in an Operation, the behaviour of Operations under different inputs, or the
 * optimisation steps taken in the reduction of the size of a TestSuite.  An
 * Analysis indicates a non-serious condition, which the user may choose to 
 * act upon if desired.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Analysis extends Notice {
	
	/**
	 * Creates an empty Analysis.
	 */
	public Analysis() {
	}
	
	/**
	 * Creates an Analysis with the given text.
	 * @param text the text.
	 */
	public Analysis(String message) {
		super(message);
	}
	
}
