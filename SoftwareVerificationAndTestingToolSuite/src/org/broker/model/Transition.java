package org.broker.model;

/**
 * Transition represents a named transition in a finite state machine.  A
 * transition is uniquely indexed under its name in its owning state, but
 * but is otherwise identified by the unique combination of source, target
 * and transition names.  A transition is associated with an event of the
 * same name.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Transition extends Annotated {
	
	/**
	 * Whether this Transition has been fired.
	 */
	private boolean fired;

	/**
	 * The name of the source state.
	 */
	private String source;
	
	/**
	 * The name of the target state.
	 */
	private String target;
	
	/**
	 * Creates a default transition.
	 */
	public Transition() {
	}
	
	/**
	 * Creates a named transition.
	 * @param name the name of this transition.
	 */
	public Transition(String name) {
		super(name);
	}
	
	/**
	 * Reports whether this transition is equal to another object.  True,
	 * if the other object is a transition with the same name, source and
	 * target states as this transition.
	 * @param other the other object.
	 * @return true, if both objects are transitions with the same name,
	 * source and target.
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		else if (other instanceof Transition) {
			Transition transition = (Transition) other;
			return safeEquals(name, transition.name) && 
					safeEquals(source, transition.source) &&
					safeEquals(target, transition.target);
		}
		else
			return false;
	}

	/**
	 * Returns a quasi-unique hash code for this transition.  Returns the 
	 * hash code associated with this transition's name, source and target
	 * state names.
	 * @return the hash code for this transition.
	 */
	@Override
	public int hashCode() {
		return (super.hashCode() * 31 + safeHashCode(source)) * 31
				+ safeHashCode(target);
	}
	
	/**
	 * Returns the name of a Transition.  It is an error if the name has
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
	 * Sets the name of the source state.
	 * @param source the name of the source state.
	 * @return this Transition.
	 */
	public Transition setSource(String source) {
		this.source = source;
		return this;
	}
	
	/**
	 * Returns the name of the source state.
	 * @return the name of the source state.
	 */
	public String getSource() {
		if (source == null)
			semanticError("has no source state.");
		return source;
	}
	
	/**
	 * Sets the name of the target state.
	 * @param target the name of the target state.
	 * @return this Transition.
	 */
	public Transition setTarget(String target) {
		this.target = target;
		return this;
	}
	
	/**
	 * Returns the name of the target state.
	 * @return the name of the target state.
	 */
	public String getTarget() {
		if (target == null)
			semanticError("has no target state.");
		return target;
	}
	
	/**
	 * Reports whether this Transition has been fired.  Transitions are
	 * marked during the simulated execution of a test suite, to show that
	 * they have been fired.
	 * @return true, if this Transition has been fired.
	 */
	public boolean isFired() {
		return fired;
	}
	
	/**
	 * Used to mark a Transition during the simulated execution of a test 
	 * suite; and to clear the Transition afterwards.
	 * @param value the true or false value to set.
	 * @return this Transition
	 */
	public Transition setFired(boolean value) {
		fired = value;
		return this;
	}

}
