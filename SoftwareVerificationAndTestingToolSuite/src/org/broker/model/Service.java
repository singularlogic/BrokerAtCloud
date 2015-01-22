package org.broker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service represents a a model or specification of a software service. 
 * The model consists of two parts, a Protocol, describing the Memory and
 * Operations of the Service, and a Machine, describing the States and
 * Transitions of the Service.  Both aspects are used to simulate the
 * behaviour of the Service.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Service extends Annotated {
		
	/**
	 * The Protocol of this Service.  The Protocol describes the global 
	 * Memory and set of Operations for the Service.  It describes the
	 * detailed abstract behaviour of each Operation, in terms of its
	 * inputs, outputs, precondition and effect upon Memory.
	 */
	private Protocol protocol;
	
	/**
	 * The finite state Machine for this service.  The Machine models the
	 * state transition diagram for the Service.  In particular, it models
	 * modes in which particular Operations are available, or unavailable. 
	 */
	private Machine machine;
	
	/**
	 * Creates a default Service with no name.
	 */
	public Service() {
	}
	
	/**
	 * Creates a named Service.  A Service is typically named, where the name
	 * describes succinctly what the Service is, or what it does.
	 * @param name the name of this Service.
	 */
	public Service(String name) {
		super(name);
	}
	
	/**
	 * Adds the Protocol to this service.  The Protocol describes the global 
	 * Memory and set of Operations for the Service.  It describes the
	 * detailed abstract behaviour of each Operation, in terms of its
	 * inputs, outputs, precondition and effect upon Memory.
	 * @param protocol the Protocol to add.
	 * @return this Service.
	 */
	public Service addProtocol(Protocol protocol) {
		this.protocol = protocol;
		return this;
	}
	
	/**
	 * Returns the Protocol of this service.  The Protocol describes the 
	 * global Memory and set of Operations for the Service.  It describes 
	 * the detailed abstract behaviour of each Operation, in terms of its
	 * inputs, outputs, precondition and effect upon Memory.
	 * @return the Protocol of this service.
	 */
	public Protocol getProtocol() {
		return protocol;
	}
	
	/**
	 * Adds the finite state Machine of this Service.  The Machine models
	 * the state transition diagram for the Service.  In particular, it 
	 * models modes in which particular Operations are available, or 
	 * unavailable.
	 * @param machine the finite state Machine to add.
	 * @return this Service.
	 */
	public Service addMachine(Machine machine) {
		this.machine = machine;
		return this;
	}
	
	/**
	 * Returns the finite state Machine of this Service.  The Machine models 
	 * the state transition diagram for the Service.  In particular, it 
	 * models modes in which particular Operations are available, or 
	 * unavailable.
	 * @return the finite state Machine of this Service.
	 */
	public Machine getMachine() {
		return machine;
	}
	
	/**
	 * Calculates the state cover for this Service.  This is the smallest 
	 * Language containing the shortest Sequences that will reach every
	 * State in this Service's Machine, via Transitions whose guard 
	 * conditions are also satisfied in this Service's Protocol.  Performs
	 * a breadth-first search, starting with the empty Sequence, and 
	 * extending this on each iteration with every Event from the Alphabet,
	 * until every State has been visited once.  Times out if the search 
	 * eventually fails to reach every State.
	 * @return the state cover Language for this Service.
	 */
	public Language getStateCover() {
		return getStateCover(machine.getAlphabet());
	}
	
	/**
	 * Calculates the state cover for this Service.  This is the smallest 
	 * Language containing the shortest Sequences that will reach every
	 * State in this Service's Machine, via Transitions whose guard 
	 * conditions are also satisfied in this Service's Protocol.  Performs
	 * a breadth-first search, starting with the empty Sequence, and 
	 * extending this on each iteration with every Event from the Alphabet,
	 * until every State has been visited once.  Times out if the search 
	 * eventually fails to reach every State.
	 * @param alphabet the Alphabet for this Service's Machine.
	 * @return the state cover Language for this Service.
	 */
	protected Language getStateCover(Alphabet alphabet) {
		int DELAY = 5000;
		long timeout = System.currentTimeMillis() + DELAY;	// DELAY milliseconds
		Language stateCover = new Language();
		Set<State> toFind = machine.getStates();			// the states to find
		List<Sequence> paths = new ArrayList<Sequence>();	// paths to explore
		paths.add(new Sequence());							// contains empty sequence
		while (! toFind.isEmpty() && System.currentTimeMillis() < timeout) {
			// Explore one sequence from the search space
			machine.reset();
			protocol.reset();
			Sequence sequence = paths.remove(0);
			boolean expand = true;							// true for empty sequence
			for (Event event : sequence.getEvents()) {
				boolean enabled = machine.accept(event);
				boolean triggered = protocol.accept(event);
				if (enabled && triggered) {
					machine.fireEvent(event);
					protocol.fireEvent(event);
					expand = true; 							// do expand this path
				}
				else
					expand = false;							// do not expand this path
			}
			State currentState = machine.getCurrentState();
			if (toFind.remove(currentState)) {
				sequence.setState(currentState.getName());
				stateCover.addSequence(sequence);
			}
			if (expand && !toFind.isEmpty())
				for (Event event : alphabet.getEvents()) {
					paths.add(new Sequence(sequence).addEvent(event));
				}
		}
		return stateCover;
	}
	
	/**
	 * Calculates the n-transition cover for this Service.  This is a Language
	 * consisting of Sequences that will reach every State and then explore 
	 * every Transition path of length k = 0..n, starting from each State.
	 * The result includes the state cover, and is computed by taking the
	 * product of the state cover and the bounded language Ln*, which is the
	 * set of all Sequences of length 0..n chosen from the Machine's Alphabet.
	 * @param length the maximum Transition path length to explore.
	 * @return the n-transition cover Language.
	 */
	public Language getTransitionCover(int length) {
		return getTransitionCover(machine.getAlphabet(), 
				length < 0 ? 0 : length); 
	}
	
	/**
	 * Calculates the n-transition cover for this Service.  This is a Language
	 * consisting of Sequences that will reach every State and then explore 
	 * every Transition path of length k = 0..n, starting from each State.
	 * The result includes the state cover, and is computed by taking the
	 * product of the state cover and the bounded language Ln*, which is the
	 * set of all Sequences of length 0..n chosen from the Machine's Alphabet.
	 * @param alphabet the Alphabet of the associated Machine.
	 * @param length the maximum Transition path length to explore.
	 * @return the n-transition cover Language.
	 */
	protected Language getTransitionCover(Alphabet alphabet, int length) {
		Language cover = getStateCover(alphabet);		// the state cover
		Language trans = Language.createBoundedStar(alphabet, length);
		return cover.product(trans);					// the transition cover
	}

	/**
	 * Validates the states and transitions of the associated Machine.
	 * Checks for correspondence between the Transitions in this Machine and
	 * the Scenarios in the Protocol.  Checks for the existence of an initial
	 * state and that all states are reachable in the Machine (notwithstanding
	 * any guards in the Protocol).  Checks the completeness of each State 
	 * under all Events from the Alphabet.  Annotates the Machine with 
	 * various Notice, Analysis and Warning nodes.
	 * @return the annotated Machine.
	 */
	public Machine validateMachine() {
		return machine.validateMachine(getProtocol());
	}
	
	/**
	 * Verifies the Protocol of this Service for consistency and completeness.
	 * Checks for correspondence between the Scenarios in the Protocol and 
	 * the Transitions in the Machine.  Checks that the Memory is correctly 
	 * initialised.  Checks that each Operation is deterministic and complete
	 * under all input and memory conditions.  Annotates the Protocol with
	 * various Notice, Analysis and Warning nodes.
	 * @return the annotated Protocol.
	 */
	public Protocol verifyProtocol() {
		return protocol.verifyProtocol(getMachine());
	}

	/**
	 * Generates the TestSuite for this Service, up to a bounded path length.
	 * This is the main method for generating a TestSuite.  It works by
	 * generating an abstract Language that will explore all paths up to the
	 * given length, starting from every State in the Machine.  This language
	 * is then presented to the Machine and Protocol to see whether each Event
	 * Sequence is accepted or ignored.  Sequences accepted by both Machine
	 * and Protocol are positive test cases that the implementation should
	 * accept; Sequences accepted by the Protocol and rejected by the Machine
	 * are negative test cases that the implementation should ignore.  
	 * Sequences rejected by the Protocol are infeasible under the given
	 * memory and input conditions, so cannot be executed, and are pruned.
	 * If compression is requested, the resulting TestSuite is compressed to
	 * produce a more compact set of multi-objective tests.
	 * @param length the maximum path length explored from each state.
	 * @param compress whether to compress the generated TestSuite.
	 * @return the TestSuite specified by the input length.
	 */
	public TestSuite generateTestSuite(int length, boolean compress) {
		if (length < 0) length = 0;
		TestSuite testSuite = new TestSuite(getName(), length);
		Notice topInfo = 
				new Notice("Generated test suite for service: " + getName());
		testSuite.addNotice(topInfo);
		topInfo.addNotice(
				new Analysis("Exploring all paths up to length: " + length));
		Language cover = getTransitionCover(length);
		int theoretical = cover.size();  // Theoretical number of sequences
		topInfo.addNotice(
				new Analysis("Number of theoretical sequences: " + theoretical));
		machine.clear();  				// to remove all traces of execution
		int redundant = 0;
		int infeasible = 0;
		for (Sequence sequence : cover.getSequences()) {
			TestSequence testSequence = generateTestSequence(sequence);
			if (testSequence == null) 
				++infeasible;
			else {
				int priorSize = testSuite.size();
				testSuite.addTestSequence(testSequence);
				if (testSuite.size() == priorSize)
					++redundant;
			}
		}
		int actual = testSuite.size();  // Actual number of feasible sequences
		topInfo.addNotice(
				new Analysis("Number of infeasible sequences: " + infeasible));
		topInfo.addNotice(
				new Analysis("Number of redundant sequences: " + redundant));
		topInfo.addNotice(
				new Analysis("Number of executable sequences: " + actual));
		if (compress) {
			testSuite.compress();
			int compressed = testSuite.size();
			topInfo.addNotice(
					new Analysis("Number of multi-objective sequences: " + 
							compressed));
		}
		Warning stateWarning = null;
		for (State state : machine.getStates()) {
			if (! state.isReached()) {
				if (stateWarning == null)
					stateWarning = new Warning("These states were not explored:");
				stateWarning.addElement(state.snapshot());  // copy without transitions
			}
		}
		Warning transWarning = null;
		for (Transition transition : machine.getTransitions()) {
			if (! transition.isFired()) {
				if (transWarning == null)
					transWarning = new Warning("These transitions were not tested:");
				transWarning.addElement(transition);
			}
		}
		if (stateWarning != null || transWarning != null) {
			Warning coverWarning = new Warning(
					"Specification is not fully covered by the test suite");
			if (length < 3)
				coverWarning.addNotice(
						new Analysis("Suggest increasing the path length"));
			else
				coverWarning.addNotice(
						new Analysis("Suggest introducing a new scenario"));
			topInfo.addNotice(coverWarning);
		}
		if (stateWarning != null)
			topInfo.addNotice(stateWarning);  // add when created
		if (transWarning != null)
			topInfo.addNotice(transWarning);  // add when created
		return testSuite;
	}
	
	/**
	 * Generates a single TestSequence from an abstract Sequence of Events.
	 * If the abstract Sequence is accepted by the Protocol and the Machine,
	 * generates a positive case.  If the Sequence is accepted by the Protocol
	 * but refused by the Machine, generates a negative case.  If the Sequence
	 * is refused by the Protocol, it is infeasible and so no TestSequence can 
	 * be generated.  This is valid, since any complementary Sequence accepted 
	 * by the Protocol will also constitute a successful negative test of the 
	 * infeasible Sequence.  TestSequences are truncated after the first
	 * refusal, to avoid generating TestSequences with nullops in the prefix.
	 * @param sequence the Sequence of Events.
	 * @return a TestSequence, or null.
	 */
	protected TestSequence generateTestSequence(Sequence sequence) {
		machine.reset();
		protocol.reset();
		TestSequence testSequence = new TestSequence(sequence);
		
		// Standard creation or initialisation instruction
		TestStep initialStep = new TestStep("create/ok");
		Operation createOp = initialStep.getOperation();
		createOp.addParameter(new Output("system", getName()));
		
		initialStep.recordAcceptance();
		initialStep.setState(machine.getInitialState().getName());
		testSequence.addEvent(initialStep);
		// Append sequence of TestSteps, according to the path length
		for (Event event : sequence.getEvents()) {
			TestStep testCase = generateTest(event);
			if (testCase == null)  // infeasible - delete TestSequence
				return null;
			else
				testSequence.addEvent(testCase);
			if (testCase.isRefused())
				break;  // truncate TestSequence after first refusal
		}
		testSequence.doVerify();  // assert outcome of the TestSequence
		return testSequence;
	}
	
	/**
	 * Generates a single TestStep from an abstract Event.  If the Event is
	 * accepted both by the Protocol and the Machine, fires the event and
	 * creates a positive TestStep (to be accepted).  If the Event is only
	 * accepted by the Protocol, but refused by the Machine, generates a
	 * negative TestStep (to be ignored).  If the Protocol refuses the Event,
	 * it is infeasible given the current memory and inputs, so no TestStep
	 * is created.  The TestStep is labelled with the reached State (after
	 * firing) or the unchanged State (after refusal).
	 * @param event the Event.
	 * @return a TestStep, or null.
	 */
	protected TestStep generateTest(Event event) {
		Operation operation = protocol.getOperation(event);
		TestStep testCase = new TestStep(event.getName());
		boolean enabled = machine.accept(event);
		boolean triggered = protocol.accept(event);
		if (triggered && enabled) {
			// Protocol and Machine both accept the event.
			machine.fireEvent(event);
			protocol.fireEvent(event);
			testCase.recordInputs(operation);
			testCase.recordOutputs(operation);
			testCase.recordAcceptance();
		}
		else if (triggered && !enabled) {
			// Machine refused the event as a missing transition.  The test
			// case is still feasible, given the input and memory.
			testCase.recordInputs(operation);
			testCase.recordRefusal();
		}
		else  // Protocol (and possibly Machine) refused the event.  The test
			  // case cannot be triggered with this input, so is infeasible.
			return null;
		testCase.setState(machine.getCurrentState().getName());
		return testCase;
	}
	
}

