package org.broker.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Machine represents a named finite state Machine.  A Machine describes the
 * high-level behaviour of a software Service, in terms of its expected 
 * control States and Transitions.  The detailed behaviour of a Service
 * is described by the Operations in its Protocol.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Machine extends Annotated {

	/**
	 * The top Analysis node relating to missed state or transition 
	 * coverage.  This contains further Analysis nodes with meta-data 
	 * related to the analysed Machine.
	 */
	private Notice information;
	
	/**
	 * The set of states, indexed by their unique names. 
	 */
	private Map<String, State> states;
	
	/**
	 * The initial State; a volatile variable.  Stores a cached reference to
	 * the first added State that was identified as an initial State.
	 */
	private State initial;
	
	/**
	 * The current State; a volatile variable.  Initially, and after a reset,
	 * this refers to the initial State.  Subsequently, it refers to the
	 * target State reached after firing a Transition.
	 */
	private State current;
	
	/**
	 * Creates a default finite state Machine.
	 */
	public Machine() {
		states = new LinkedHashMap<String,State>();
	}
	
	/**
	 * Creates a named finite state Machine.
	 * @param name the name of the Machine.
	 */
	public Machine(String name) {
		super(name);
		states = new LinkedHashMap<String,State>();
	}

	/**
	 * Adds the top Analysis node to this Machine.  This node contains
	 * other Analysis nodes with meta-data, or or Warning nodes listing
	 * missed transition or state coverage.
	 * @param information the Analysis to add.
	 * @return this Machine.
	 */
	public Machine addInformation(Notice information) {
		this.information = information;
		return this;
	}
	
	/**
	 * Returns the top Analysis node added to this Machine.  This contains
	 * other Analysis nodes containing meta-data, or Warning nodes with
	 * reports about missed state or transition coverage.
	 * @return the top Analysis node.
	 */
	public Notice getInformation() {
		return information;
	}
	
	/**
	 * Adds a named state to the Machine.  Each state must be uniquely named.
	 * A duplicate state will replace an existing state with the same name.
	 * This method may be used to add all the desired states to a default 
	 * Machine.  Any state declared to be a initial-state will be remembered.
	 * The current state will be set to the initial-state.
	 * @param state the state to add.
	 * @return this Machine.
	 */
	public Machine addState(State state) {
		states.put(state.getName(), state);
		if (state.isInitial()) {
			if (initial != null)
				semanticError("already has an initial state: " + 
						initial.getName());
			initial = state;
			current = state;
		}
		return this;
	}
	
	/**
	 * Returns the unique state with the given name.
	 * @param name the name of the state.
	 * @return the state, or null.
	 */
	public State getState(String name) {
		return states.get(name);
	}
	
	/**
	 * Returns the set of states belonging to this Machine.  Creates a new
	 * set containing all the unique states of this Machine. 
	 * @return the set of states.
	 */
	public Set<State> getStates() {
		return new LinkedHashSet<State> (states.values());
	}
	
	/**
	 * Returns the set of transitions belonging to this Machine.  Creates a
	 * new set containing all the unique transitions of this Machine.
	 * @return the set of transitions.
	 */
	public Set<Transition> getTransitions() {
		Set<Transition> result = new LinkedHashSet<Transition>();
		for (State state : getStates()) {
			result.addAll(state.getTransitions());
		}
		return result;
	}
		
	/**
	 * Returns the initial state.
	 * @return the initial state.
	 */
	public State getInitialState() {
		if (initial == null)
			semanticError("has no initial state.");
		return initial;
	}
	
	/**
	 * Returns the current state;
	 * @return the current state.
	 */
	public State getCurrentState() {
		if (current == null)
			semanticError("has no current state.");
		return current;
	}
	
	/**
	 * Resets this Machine to its initial state.  The current State of this
	 * Machine is reset to the Machine's initial state.  This method is
	 * invoked at the start of executing a fresh Sequence.
	 * @return this Machine.
	 */
	public Machine reset() {
		current = getInitialState();
		current.setReached(true);
		return this;
	}
	
	/**
	 * Resets every State and Transition in this Machine to its unmarked
	 * status.  Whenever a Transition fires, both the Transition and the
	 * reached State are marked, to indicate that they have been covered.
	 * This method is invoked before a new TestSuite is created, to reset
	 * the traces in the model.
	 * @return this Machine.
	 */
	public Machine clear() {
		for (Transition transition : getTransitions()) {
			transition.setFired(false);
		}
		for (State state : getStates()) {
			state.setReached(false);
		}
		return this;
	}
	
	/**
	 * Reports whether this Machine accepts an Event in its current State.
	 * Reports whether this Machine's current State has an exit Transition 
	 * labelled with the same name as the Event.
	 * @param event the Event.
	 * @return true, if an enabled Transition exists for this Event.
	 */
	public boolean accept(Event event) {
		return current.getEnabled(event) != null;
	}
	
	/**
	 * Fires an event on this Machine.  Applies the Event to this Machine in
	 * its current State.  If an enabled Transition exists, the Transition is
	 * marked as having been fired, and this Machine's current State is set
	 * to the destination State indicated by the Transition.  If no enabled
	 * Transition exists, does nothing.
	 * @param event the event.
	 * @return true, if the Event is accepted by this Machine.
	 */
	public boolean fireEvent(Event event) {
		Transition transition = getCurrentState().getEnabled(event);
		if (transition != null) {
			transition.setFired(true);
			current = getState(transition.getTarget());
			current.setReached(true);
			return true;
		}
		else 
			return false;
	}
	
	/**
	 * Returns the Alphabet of this Machine.  Computes the Alphabet, the set
	 * of Events understood by this Machine.  Iterates over the Transitions
	 * of this Machine and collects the Transition labels uniquely in a set.
	 * @return the Alphabet of this Machine, the set of Events.
	 */
	public Alphabet getAlphabet() {
		Alphabet alphabet = new Alphabet();
		for (Transition transition : getTransitions()) {
			alphabet.addEvent(new Event(transition.getName()));
		}
		return alphabet;
	}

	/**
	 * Calculates the state cover for this Machine.  This is the smallest
	 * Language containing the shortest Sequences that will reach every State
	 * in this Machine.  Performs a breadth-first search, starting with the
	 * empty Sequence, and extending this on each iteration with every Event
	 * from the Alphabet, until every State has been visited once.  Times out
	 * if the search eventually fails to reach every State.
	 * @param alphabet the Alphabet of this Machine.
	 * @return the state cover Language.
	 */
	protected Language getStateCover(Alphabet alphabet) {
		long DELAY = 5000;							// timeout 5 seconds
		long timeout = System.currentTimeMillis() + DELAY;
		Language stateCover = new Language();		// the state cover
		Set<State> toFind = getStates();			// the states to find
		List<Sequence> paths = new ArrayList<Sequence>(); // paths to explore
		paths.add(new Sequence());
		clear();									// reset any tracer marks
		while (! toFind.isEmpty() && System.currentTimeMillis() < timeout) {
			Sequence sequence = paths.remove(0);
			reset();								// checks for initial state
			for (Event event : sequence.getEvents())
				fireEvent(event);					// either succeeds or fails
			if (toFind.remove(getCurrentState())) {
				sequence.setState(getCurrentState().getName());
				stateCover.addSequence(sequence);
			}
			if (! toFind.isEmpty())
				for (Event event : alphabet.getEvents()) {
					paths.add(new Sequence(sequence).addEvent(event));
				}
		}
		return stateCover;
	}
	
	/**
	 * Calculates the state cover for this Machine.  This is the smallest
	 * Language containing the shortest Sequences that will reach every State
	 * in this Machine.  Performs a breadth-first search, starting with the
	 * empty Sequence, and extending this on each iteration with every Event
	 * from the Alphabet, until every State has been visited once.  Times out
	 * if the search eventually fails to reach every State.
	 * @return the state cover Language.
	 */
	public Language getStateCover() {
		return getStateCover(getAlphabet());
	}
	
	/**
	 * Calculates the n-transition cover for this Machine.  This is a Language
	 * consisting of Sequences that will reach every State and then explore 
	 * every Transition path of length k = 0..n, starting from each State.
	 * The result includes the state cover, and is computed by taking the
	 * product of the state cover and the bounded language Ln*, which is the
	 * set of all Sequences of length 0..n chosen from the Alphabet.
	 * @param length the maximum Transition path length to explore.
	 * @return the n-transition cover Language.
	 */
	public Language getTransitionCover(int length) {
		if (length < 0) length = 0;
		Alphabet alphabet = getAlphabet();				// the event alphabet
		Language cover = getStateCover(alphabet);		// the state cover
		Language trans = Language.createBoundedStar(alphabet, length);
		return cover.product(trans);					// the transition cover
	}
	
	/**
	 * Validates the states and transitions of this Machine for completeness.
	 * Checks for correspondence between the Transitions in this Machine and
	 * the Scenarios in the Protocol.  Checks for the existence of an initial
	 * state and that all states are reachable in the Machine (notwithstanding
	 * any guards in the Protocol).  Checks the completeness of each State 
	 * under all Events from the Alphabet.  Annotates this Machine with 
	 * different Notice, Analysis and Warning nodes.
	 * @return this annotated Machine.
	 */
	public Machine validateMachine(Protocol protocol) {
		String name = getName() == null ? "unnamed" : getName();
		Notice topInfo = 
				new Notice("Validation report for machine: " + name);
		addNotice(topInfo);
		Alphabet alphabet = getAlphabet();
		Alphabet scenarios = protocol.getAlphabet();
		Alphabet difference = scenarios.subtract(alphabet);
		if (! difference.isEmpty()) {
			Warning eventWarning = new Warning(
					"Machine does not handle Protocol events:");
			topInfo.addNotice(eventWarning);
			for (Event event : difference.getEvents()) {
				eventWarning.addElement(event);
			}
		}
		try {
			getStateCover(alphabet);  // purely to exercise for reachability
		}
		catch (SemanticError error) {
			String message = error.getMessage();
			if (message.contains("initial"))
				topInfo.addNotice(new Warning(
						"Machine has no initial state; cannot simulate"));
			else if (message.contains("target")) {
				Warning warning = new Warning(
						"Transition has no target state; cannot fire");
				warning.addElement(error.getElement());
				topInfo.addNotice(warning);
			}
		}
		Warning unreached = null;
		for (State state : getStates()) {
			Notice stateInfo = new Notice(
					"Completeness check for state: " + state.getName());
			state.addNotice(stateInfo);
			if (! state.isReached()) {
				if (unreached == null) {
					unreached = new Warning(
							"Exploration failed to reach the states:");
					topInfo.addNotice(unreached);
				}
				unreached.addElement(state.snapshot());
				stateInfo.addNotice(new Warning(
						"State is not reachable"));
			}
			Analysis ignored = null;
			for (Event event : alphabet.getEvents()) {
				if (state.getEnabled(event) == null) {
					if (ignored == null) {
						ignored = new Analysis(
								"State ignores the events:");
						stateInfo.addNotice(ignored);
						topInfo.addNotice(new Analysis(
								"Events are ignored in state: " + 
										state.getName()));
					}
					ignored.addElement(event);
				}
			}
		}
		return this;
	}
	
}
