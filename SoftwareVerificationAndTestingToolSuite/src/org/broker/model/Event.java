package org.broker.model;

/**
 * Event represents a unique named event recognised by a finite state machine.
 * A finite state Machine recognises a set of uniquely named Events, known as
 * the Alphabet of the Machine.  Each Event is a symbolic request/response 
 * combination, denoting a distinct kind of expected behaviour in the finite 
 * state Machine.  An ordered presentation of Events is known as a Sequence.
 * An Event is deemed equal to another Event if they have the same name.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Event extends Named {
	
	/**
	 * Validates the name of an Event.
	 */
	protected void validate(String name) {
		if (name == null || name.length() < 3 || name.indexOf('/') == -1)
			semanticError("incorrectly named: " + name +
					"; should be like: 'request/response'");
	}
	
	/**
	 * Creates default Event.
	 */
	public Event() {
	}
	
	/**
	 * Creates a named Event.  Validates the name, to ensure that it is of the
	 * format: "request/response".
	 * @param name the name of this Event.
	 */
	public Event(String name) {
		super(name);
		validate(name);
	}
	
	/**
	 * The request-name part of this Event's name.
	 * @return the request-name.
	 */
	public String requestName() {
		return name.split("/")[0];
	}
		
	/**
	 * The response-name part of this Event's name.
	 * @return the response-name.
	 */
	public String responseName() {
		return name.split("/")[1];
	}
	
}
