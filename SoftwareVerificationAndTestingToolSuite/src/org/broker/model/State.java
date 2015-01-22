package org.broker.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * State represents a named state in a finite state machine.  A state is
 * uniquely indexed under its name in its owning machine.  Each state has
 * a set of uniquely-named transitions from this state to another state.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class State extends Annotated {
	
	/**
	 * Indicates whether this State has been reached.
	 */
	private boolean reached = false;
	
	/**
	 * Indicates whether this is a initial-state.  False by default.
	 */
	private boolean initial = false;
	
	/**
	 * The set of labelled transitions exiting this State.
	 */
	private Map<String, Transition> transitions;

	/**
	 * Creates a default state.
	 */
	public State() {
		transitions = new LinkedHashMap<String, Transition>();
	}
	
	/**
	 * Creates a named State.
	 */
	public State(String name) {
		super(name);
		transitions = new LinkedHashMap<String, Transition>();
	}
	
	/**
	 * Returns the name of a State.  It is an error if the name has
	 * not already been set, when this access-method is invoked.
	 * @return the name.
	 */
	@Override
	public String getName() {
		if (name == null)
			semanticError("must be named for indexing purposes.");
		return name;
	}

	/**
	 * Sets whether this is a initial state.
	 * @param isInitial true if this is a initial state.
	 * @return this State.
	 */
	public State setInitial(boolean isInitial) {
		initial = isInitial;
		return this;
	}

	/**
	 * Reports whether this is the initial state.
	 * @return true if this is the initial state.
	 */
	public boolean isInitial() {
		return initial;
	}
	
	/**
	 * Optionally reports whether this is the initial state.  Used during
	 * marshalling, where saving this attribute is optional.
	 * @return the Boolean object true, or null.
	 */
	public Boolean getInitial() {
		return initial ? initial : null;
	}

	/**
	 * Adds a named exit transition to this State.  Each transition must be 
	 * uniquely named.  A duplicate transition will replace an existing 
	 * transition with the same name.
	 * @param transition the transition to add.
	 * @return this State.
	 */
	public State addTransition(Transition transition) {
		String source = transition.getSource();
		if (! source.equals(getName()))
				semanticError("name does not match Transition source.");
		transitions.put(transition.getName(), transition);
		return this;
	}
	
	/**
	 * Returns the unique exit transition with the given name.
	 * @param name the name of the transition.
	 * @return the named transition, or null.
	 */
	public Transition getTransition(String name) {
		return transitions.get(name);
	}
	
	/**
	 * Returns a list of exit transitions for this state.
	 * @return the list of exit transitions.
	 */
	public List<Transition> getTransitions() {
		return new ArrayList<Transition> (transitions.values());
	}

	/**
	 * Returns the transition whose name matches a given event.  Every
	 * transition is uniquely-named; so at most one transition may be
	 * selected.
	 * @param event the event.
	 * @return the transition matching the event, or null.
	 */
	public Transition getEnabled(Event event) {
		return transitions.get(event.getName());
	}

	/**
	 * Reports whether this State has been reached.  States are marked during
	 * the search for the state cover, when they are reached.
	 * @return true, if this State has been reached.
	 */
	public boolean isReached() {
		return reached;
	}
	
	/**
	 * Sets whether this State has been reached.  Used to mark a State during
	 * the search for the state cover; and to clear the State afterwards.
	 * @param value the true or false value to set.
	 * @return this State.
	 */
	public State setReached(boolean value) {
		reached = value;
		return this;
	}
	
	/**
	 * Takes a snapshot of this State, without its dependent Transitions.
	 * @return a clone of this State.
	 */
	public State snapshot() {
		State result = new State(getName());
		result.setInitial(isInitial());
		return result;
	}

}
