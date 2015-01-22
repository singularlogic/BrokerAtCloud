package org.broker.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Alphabet represents the complete set of events recognised by a machine.
 * The Alphabet of a Machine is created by exploring all the Transitions
 * of a Machine and including an Event for each labelled Transition.  The
 * Alphabet is used to construct Languages of different lengths, the basis
 * for sequences presented to a Machine.  The Alphabet of a Protocol is
 * calculated by exploring all the Operations and Scenarios in the Protocol
 * and including an Event for each labelled Scenario.  Alphabets may be
 * compared, to see whether the Machine and the Protocol handle the same
 * set of Events.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Alphabet extends Element {
	
	/**
	 * The set of events in this alphabet.
	 */
	private Set<Event> events;

	/**
	 * Creates an empty Alphabet.
	 */
	public Alphabet() {
		events = new LinkedHashSet<Event>();
	}
		
	/**
	 * Reports whether this Alphabet is empty.
	 * @return true if this Alphabet is empty.
	 */
	public boolean isEmpty() {
		return events.isEmpty();
	}
	
	/**
	 * Returns the size of this alphabet.
	 * @return the number of distinct events.
	 */
	public int size() {
		return events.size();
	}

	/**
	 * Adds an event to this alphabet.  If this alphabet does not already
	 * contain an event equal to the added event, it includes the new event
	 * in its set of events.
	 * @param event the event.
	 * @return this alphabet.
	 */
	public Alphabet addEvent(Event event) {
		events.add(event);
		return this;
	}

	/**
	 * Returns the set of events in this alphabet.
	 * @return the set of events.
	 */
	public Set<Event> getEvents() {
		return events;
	}
	
	/**
	 * Computes the difference between this and the other Alphabet.  This
	 * is used when comparing two Alphabets for consistency.  The result
	 * is a new Alphabet, containing the Events from this Alphabet that
	 * were not found in the other Alphabet.
	 * @param other the other Alphabet.
	 * @return the Alphabet representing the difference.
	 */
	public Alphabet subtract(Alphabet other) {
		Alphabet result = new Alphabet();
		for (Event event : events) {
			if (! other.events.contains(event))
				result.addEvent(event);
		}
		return result;
	}

}
