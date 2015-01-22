package org.broker.model;

import java.io.IOException;

/**
 * Grounding is the abstract interface satisfied by concrete test generators.
 * A Grounding is any class implementing this interface, which encapsulates an
 * algorithm for transforming a high-level TestSuite into a concrete test 
 * suite (e.g. JUnit, or SOAP, or REST ...) that can be executed in some live
 * environment.  The Grounding interface is designed according to the Visitor
 * Design Pattern.  Each of TestSuite, TestSequence and TestStep may receive
 * a Grounding object, which then provides the algorithm to ground that kind
 * of node.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public interface Grounding {
	
	/**
	 * Top level grounding method.  This should generate code for the entire
	 * TestSuite.  The method should open and close the file that contains the
	 * resulting generated concrete test suite; and ask every TestSequence in 
	 * the TestSuite to receive this Grounding, in order to generate the rest
	 * of the grounded test suite.
	 * @param testSuite the high-level test suite.
	 */
	public abstract void groundTestSuite(TestSuite testSuite) 
			throws IOException;
	
	/**
	 * Intermediate grounding method.  This should generate code for a single
	 * TestSequence.  This method should generate the skeleton code for the 
	 * TestSequence and then ask every TestStep contained in this TestSequence
	 * to receive this Grounding, in order to generate the instruction steps
	 * in the grounded test sequence.
	 * @param sequence the high-level test sequence.
	 */
	public abstract void groundTestSequence(TestSequence sequence);
	
	/**
	 * Low level grounding method.  This should generate code for a single 
	 * TestStep.  This method should convert the operation call into concrete
	 * code with concrete arguments and generate a variable to hold the result.
	 * If the TestStep is to be verified, this method should generate suitable
	 * assertions that check (1) that the triggered operation was the expected
	 * one; (2) that the reached state was the expected one; and (3) that any
	 * return value was equal to the expected output.
	 * @param testStep the high-level test step.
	 */
	public abstract void groundTestStep(TestStep testStep);
	
}
