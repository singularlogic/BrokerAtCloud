package org.broker.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Protocol is a model of the abstract behaviour of a software service.
 * A Protocol consists of a Memory, an abstraction over the state Variables
 * used by the Service, and a set of Operations, describing the Operations
 * offered by the Service.  Each Operation describes its signature in terms
 * of (optional) Input and Output Parameters.  Each Operation consists of
 * one or more Scenarios, where each Scenario represents one logical path
 * through the Operation.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Protocol extends Annotated {
	
	/**
	 * The Memory associated with this Protocol.  The Memory is an abstraction
	 * over the global state Variables read and written by this Protocol.
	 */
	private Memory memory = null;
	
	/**
	 * The collection of Operations specified for this Protocol.  Each 
	 * Operation is uniquely named, has optional Input and Output parameters
	 * and consists of Scenarios, representing distinct pathways through the
	 * Operation's logic.
	 */
	private Map<String, Operation> operations;
	
	/**
	 * Creates a default Protocol with no name.
	 */
	public Protocol() {
		operations = new LinkedHashMap<String, Operation>();
	}
	
	/**
	 * Creates a named Protocol.  Protocols are not required to be named, but
	 * may be given names for convenience.
	 * @param name the name of this Protocol.
	 */
	public Protocol(String name) {
		super(name);
		operations = new LinkedHashMap<String, Operation>();
	}
	
	/**
	 * Adds the Memory to this Protocol.   The Memory is an abstraction over
	 * the global state Variables read and written by this Protocol.  The 
	 * Memory consists of a set of Constants and Variables, and an initial
	 * Binding of Constant values to Variables.  When the Memory is added to
	 * this Protocol, this triggers resolution of the Variable names used in
	 * the Binding, such that all references point to declared Variables.
	 * @param memory the Memory to add.
	 * @return this Protocol.
	 */
	public Protocol addMemory(Memory memory) {
		this.memory = memory;
		getMemory().resolve(getMemory());   // Resolve global IDs in Memory Binding
		return this;
	}
	
	/**
	 * Returns the memory of this service.   The Memory is an abstraction
	 * over the global state Variables read and written by this Protocol.
	 * It is an error if the Memory has not been attached, when it is first
	 * accessed for scope resolution purposes.
	 * @return the Memory of this Protocol.
	 */
	public Memory getMemory() {
		if (memory == null)
			semanticError("must have a Memory for scope resolution");
		return memory;
	}
	
	/**
	 * Adds an Operation to this Protocol's set of Operations.  Each 
	 * Operation describes its signature in terms of (optional) Input and 
	 * Output Parameters.  Each Operation consists of one or more Scenarios,
	 * where each Scenario represents a logical path through the Operation.
	 * When an Operation is added to this Protocol, this triggers resolution
	 * of the global and local Parameters (Input, Output, or Variable) that
	 * are referenced within each Scenario, such that all references point
	 * to declared Parameters, whether global Variables in Memory, or local
	 * Inputs and Outputs declared in the Operation.
	 * @param operation the Operation to add.
	 * @return this Protocol.
	 */
	public Protocol addOperation(Operation operation) {
		operations.put(operation.getName(), operation);
		operation.resolve(getMemory());
		return this;
	}
	
	/**
	 * Returns the Operation with the specified unique name. 
	 * @param name the name of the Operation.
	 * @return the associated Operation, or null if no such Operation exists.
	 */
	public Operation getOperation(String name) {
		return operations.get(name);
	}
	
	/**
	 * Returns all the Operations of this Protocol, as a set.  Operations
	 * are uniquely named.
	 * @return the set of Operations in this Protocol.
	 */
	public Set<Operation> getOperations() {
		return new LinkedHashSet<Operation> (operations.values());
	}
	
	/**
	 * Returns the Alphabet of this Protocol.  Computes the Alphabet, the set
	 * of Events understood by this Protocol.  Iterates over the Operations
	 * and Scenarios of this Protocol, collecting the Scenario labels as a 
	 * set of unique Events.  This is used when checking that the Protocol
	 * and Machine understand the same set of Events.
	 * @return the Alphabet of this Protocol, a set of Events.
	 */
	public Alphabet getAlphabet() {
		Alphabet alphabet = new Alphabet();
		for (Operation operation : getOperations()) {
			for (Scenario scenario : operation.getScenarios()) {
				alphabet.addEvent(new Event(scenario.getName()));
			}
		}
		return alphabet;
	}
	
	/**
	 * Returns the Scenario corresponding to the supplied Event.  The Event
	 * is a label on a Transition of the Machine, which corresponds to one
	 * Scenario in an Operation of the Protocol.
	 * @param event the Event.
	 * @return the corresponding Scenario, which must exist.
	 */
	public Scenario getScenario(Event event) {
		Operation operation = getOperation(event);  // Checked
		return operation.getScenario(event);
	}
	
	/**
	 * Returns the Operation corresponding to the supplied Event.  Selects
	 * the Operation, whose name is equal to the Event's request-name.
	 * Raises a semantic error if no such Operation exists.
	 * @param event the Event.
	 * @return the corresponding Operation, which must exist.
	 */
	public Operation getOperation(Event event) {
		Operation operation = operations.get(event.requestName());
		if (operation == null) 
			semanticError("has no Operation named: " + event.requestName());
		return operation;
	}
	
	/**
	 * Resets this Protocol to its initial state.  Rebinds Memory to its
	 * initial value and unbinds all parameters in each Operation.
	 * @return
	 */
	public Protocol reset() {
		memory.rebind();
		for (Operation operation : operations.values()) {
			operation.unbind();
		}
		return this;
	}
	
	/**
	 * Reports whether this Protocol accepts an Event in its current Memory 
	 * state.  Selects the Operation corresponding to the Event's request-name
	 * and selects the Scenario from the Operation, whose name is equal to the
	 * Event's whole name.  Reports whether this Scenario may be executed in
	 * the current Memory environment.  
	 * @param event an Event.
	 * @return true, if the corresponding Scenario can be executed.
	 */
	public boolean accept(Event event) {
		Operation operation = operations.get(event.requestName());
		if (operation == null) 
			semanticError("has no Operation named: " + event.requestName());
		return operation.accept(event);		
	}
	
	/**
	 * Fires an event on this Protocol.  Selects the Operation named by the
	 * supplied Event's request-name, and fires the event on that Operation. 
	 * @param event an Event.
	 * @return true, if the Event can fire.
	 */
	public boolean fireEvent(Event event) {
		Operation operation = operations.get(event.requestName());
		if (operation == null) 
			semanticError("has no Operation named: " + event.requestName());
		return operation.fireEvent(event);
	}
	
	/**
	 * Verifies this Protocol for formal consistency and completeness.
	 * Checks for correspondence between the Scenarios in the Protocol and 
	 * the Transitions in the Machine.  Checks that the Memory is correctly 
	 * initialised.  Checks that each Operation is deterministic and complete
	 * under all input and memory conditions.  Annotates the Protocol with
	 * various Notice, Analysis and Warning nodes.
	 * @return the annotated Protocol.
	 */
	public Protocol verifyProtocol(Machine machine) {
		String name = getName() == null ? "unnamed" : getName();
		Notice topInfo = 
				new Notice("Verification report for protocol: " + name);
		addNotice(topInfo);
		Alphabet scenarios = getAlphabet();
		Alphabet alphabet = machine.getAlphabet();
		Alphabet difference = alphabet.subtract(scenarios);
		if (! difference.isEmpty()) {
			Warning eventWarning = new Warning(
					"Protocol does not handle Machine events:");
			topInfo.addNotice(eventWarning);
			for (Event event : difference.getEvents()) {
				eventWarning.addElement(event);
			}
		}
		getMemory().checkCompleteness(topInfo);
		for (Operation operation : getOperations()) {
			operation.checkCompleteness(topInfo);
		}
		return this;
	}
	
}
