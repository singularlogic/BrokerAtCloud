package org.broker.model;

/**
 * TestStep represents a single executable test step within a test sequence.
 * Each TestStep represents an instruction to invoke a single Operation in
 * the Protocol of a Service, with particular Input values chosen to trigger
 * a particular Scenario.  One TestStep always represents the initial setup
 * or creation of the Service.  A TestStep is named according to the Scenario
 * it attempts to trigger and its response is either the response of that
 * Scenario, or "ignore[...]", indicating a refusal.  A TestStep is labelled 
 * with the name of the State reached after execution.  A TestStep may be
 * marked for verification, indicating that any grounding should generate
 * assertions to verify the response, the reached State and any outputs.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class TestStep extends Event {
	
	/**
	 * The Operation tested by this TestStep.
	 */
	private Operation operation;
	
	/**
	 * The name of the expected reached State.
	 */
	private String state;
	
	/**
	 * Flag indicating whether this TestStep outcome is to be verified.
	 */
	private boolean verify;
	
	/**
	 * Creates a default TestStep.
	 */
	public TestStep() {
		operation = new Operation();
	}
	
	/**
	 * Creates a named TestStep.
	 * @param name the name of this TestStep.
	 */
	public TestStep(String name) {
		super(name);
		operation = new Operation(requestName());
	}

	/**
	 * Sets the name of this TestStep.  Like the name of an Event, the name of
	 * this TestStep must be in the format: "request/response".  Also, sets the
	 * name of this TestStep's Operation to be equal to "request".
	 * @param name the name of this TestStep.
	 */
	@Override
	public TestStep setName(String name) {
		super.setName(name);
		operation.setName(requestName());
		return this;
	}
		
	/**
	 * Adds the Operation exercised by this TestStep.
	 * @param operation the Operation to add.
	 * @return this TestStep.
	 */
	public TestStep addOperation(Operation operation) {
		this.operation = operation;
		return this;
	}
	
	/**
	 * Returns the Operation exercised by this TestStep.
	 * @return the Operation.
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * Sets the name of the expected target State.
	 * @param state the name of the target State.
	 * @return this TestStep.
	 */
	public TestStep setState(String state) {
		this.state = state;
		return this;
	}
	
	/**
	 * Returns the name of the expected target State.
	 * @return the name of the target State.
	 */
	public String getState() {
		return state;
	}
	
	/**
	 * Sets whether this TestStep step should be verified.  Setting true means
	 * that this TestStep step should be verified in the grounding by an assertion
	 * that checks:  whether the step was accepted or ignored; what the state
	 * was after execution; and for accepted steps, whether the outputs were
	 * correct.  Setting false means that this TestStep step is part of the setup
	 * in a longer TestSequence.
	 * @param value true or false.
	 * @return this TestStep.
	 */
	public TestStep setVerify(boolean value) {
		verify = value;
		return this;
	}
	
	/**
	 * Reports whether this TestStep step is verified.
	 * @return true if this TestStep step is verified.
	 */
	public boolean isVerify() {
		return verify;
	}
	
	/**
	 * Returns whether this TestStep step is verified, as a Boolean object.
	 * @return true, or null.
	 */
	public Boolean getVerify() {
		return verify ? true : null;
	}
	
	/**
	 * Records a snapshot of the Input bindings of an Operation.
	 * After the Operation has just been attempted, takes a local snapshot
	 * of its Input bindings.
	 * @param attempted the Operation that was just attempted.
	 * @return this TestStep.
	 */
	public TestStep recordInputs(Operation attempted) {
		for (Input input : attempted.getInputs()) {
			operation.addParameter(input.snapshot());
		}
		return this;
	}

	/**
	 * Records a snapshot of the Output bindings of an Operation.
	 * After the Operation has just been executed, takes a local snapshot
	 * of its Output bindings.
	 * @param executed the Operation that was just executed.
	 * @return this TestStep.
	 */
	public TestStep recordOutputs(Operation executed) {
		for (Output output : executed.getOutputs()) {
			operation.addParameter(output.snapshot());
		}
		return this;
	}

	/**
	 * Rename this TestStep to indicate that it was accepted. Null operation.
	 * @return this TestStep.
	 */
	public TestStep recordAcceptance() {
		// Null operation
		return this;
	}
	
	/**
	 * Rename this TestStep to indicate that it was refused.
	 * @return this TestStep.
	 */
	public TestStep recordRefusal() {
		if (name == null)
			semanticError("must be named for logging purposes.");
		name = requestName() + "/ignore[" + responseName() + ']';
		return this;
	}
	
	/**
	 * Report whether this TestStep was refused.
	 * @return true, if this TestStep was refused.
	 */
	public boolean isRefused() {
		return responseName().startsWith("ignore");
	}
	
	/**
	 * Receive a Grounding generator for grounding this TestStep.  The actual
	 * generator is any class that implements the Grounding interface.  Asks
	 * the grounding reciprocally to ground this TestStep.
	 * @param grounding a Grounding generator.
	 */
	public void receive(Grounding grounding) {
		grounding.groundTestStep(this);
	}

	
	
}
