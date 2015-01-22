package org.broker.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Operation represents an operation in the public interface of a service.
 * An Operation represents a local Scope in which Input and Output variables
 * may be declared.  When included as part of a Protocol specification, an
 * Operation maintains a collection of Scenarios, each representing a
 * distinct path through the Operation.  When an Operation is included in
 * a TestStep, the Scenarios are not copied, but any Input and Output
 * parameters will be bound to specific values.  An Operation is a kind of 
 * Annotated element that may receive Warnings.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Operation extends Scope {
	
	/**
	 * The Scenarios constituting the different paths through this Operation.
	 * This field may remain null until the first Scenario is added.  Some
	 * uses of Operation do not require the Scenarios.
	 */
	private Map<String, Scenario> scenarios = null;
		
	/**
	 * Creates a default Operation.
	 */
	public Operation() {
	}
	
	/**
	 * Creates a named Operation.
	 * @param name the name of this Operation.
	 */
	public Operation(String name) {
		super(name);
	}

	/**
	 * Returns the set of Inputs in this Operation.  A convenience, to extract
	 * those Parameters from the Scope which are Inputs.
	 * @return the Inputs in this Operation.
	 */
	public Set<Input> getInputs() {
		Set<Input> result = new LinkedHashSet<Input>();
		for (Parameter parameter : parameters.values()) {
			if (parameter instanceof Input)
				result.add((Input) parameter);
		}
		return result;
	}
	
	/**
	 * Returns the set of Outputs in this Operation.  A convenience, to extract
	 * those Parameters from the Scope which are Outputs.
	 * @return the Outputs in this Operation.
	 */
	public Set<Output> getOutputs() {
		Set<Output> result = new LinkedHashSet<Output>();
		for (Parameter parameter : parameters.values()) {
			if (parameter instanceof Output)
				result.add((Output) parameter);
		}
		return result;
	}
	
	/**
	 * Cause this Operation to resolve its global/local Parameter references.
	 * Triggered when the Protocol owning this Operation is added to its 
	 * owning Service.  The Scope argument is the global Memory, which
	 * contains the global Constants and Variables.  Constructs a new Scope,
	 * which contains all the global Parameters in the argument, to which are
	 * added all the local Parameters contained in this Operation.  Delegates
	 * to each Scenario in turn, passing it the complete table of Parameters.
	 * @param scope a Scope containing global Parameters.
	 * @return this Operation.
	 */
	public Operation resolve(Scope scope) {
		for (Scenario scenario : getScenarios()) {
			Scope table = new Scope();
			table.addScope(scope);
			table.addScope(this);
			scenario.resolve(table);
		}
		return this;
	}
	
	/**
	 * Adds a Scenario to this Operation.  Each Scenario must be uniquely
	 * named using the format: "request/response".  The "request" part must
	 * be equal to the name of this Operation.  The table of Scenarios is
	 * only created when the first Scenario is added.
	 * @param scenario the Scenario to add.
	 * @return this Operation.
	 */
	public Operation addScenario(Scenario scenario) {
		if (scenarios == null)
			scenarios = new LinkedHashMap<String, Scenario>();
		scenarios.put(scenario.getName(), scenario);
		return this;
	}
	
	/**
	 * Returns the Scenario with the specified unique name, if it exists.
	 * @param name the name of the Scenario.
	 * @return the associated Scenario, or null if no such Scenario exists.
	 */
	public Scenario getScenario(String name) {
		if (scenarios == null)
			return null;
		else
			return scenarios.get(name);
	}
	
	/**
	 * Returns this Operation's Scenarios as an ordered Set.  If Scenarios
	 * were added, returns these; otherwise returns an empty set, rather than
	 * null.
	 * @return an ordered Set of this Operation's Scenarios.
	 */
	public Set<Scenario> getScenarios() {
		if (scenarios == null)
			return Collections.emptySet();
		else
			return new LinkedHashSet<Scenario>(scenarios.values());
	}
	
	/**
	 * Returns the Scenario corresponding to the supplied Event.  Selects the
	 * Scenario from this Operation, whose name is equal to the Event's name.
	 * Raises a semantic error if no such Scenario exists.
	 * @param event the Event.
	 * @return the corresponding Scenario, which must exist.
	 */
	public Scenario getScenario(Event event) {
		Scenario scenario = getScenario(event.getName());
		if (scenario == null) 
			semanticError("has no Scenario named: " + event.getName());
		return scenario;
	}
	
	/**
	 * Reports whether this Operation accepts an Event in the current Memory
	 * state.  Selects the Scenario from the Operation, whose name is equal to
	 * the Event's whole name.  Reports whether this Scenario may be executed
	 * in the current Memory environment.  
	 * @param event the Event.
	 * @return true, if the corresponding Scenario can be executed.
	 */
	public boolean accept(Event event) {
		Scenario scenario = getScenario(event.getName());
		if (scenario == null) 
			semanticError("has no Scenario named: " + event.getName());
		// DEBUG
		// System.out.println("Operation = " + this + "; testing event: " + event);
		return scenario.isEnabled();
	}
	
	/**
	 * Fires an event on this Operation.  Selects the Scenario from this
	 * Operation, whose name is equal to the Event's name.  Executes the 
	 * Scenario and returns true, if the Scenario was able to execute with
	 * the current Memory state.
	 * @param event an Event.
	 * @return true, if this Operation was able to fire.
	 */
	public boolean fireEvent(Event event) {
		Scenario scenario = getScenario(event.getName());
		if (scenario == null) 
			semanticError("has no Scenario named: " + event.getName());
		// DEBUG
		// System.out.println("Operation = " + this + "; firing event: " + event);
		return scenario.execute();
	}
	
	/**
	 * Checks this Operation for completeness under all memory and input
	 * conditions.  Seeks to determine whether there is a non-blocking and
	 * deterministic response to each partition in the symbolic input space
	 * of the Operation.
	 * @param topInfo the Notice to which global warnings are attached.
	 * @return this Operation.
	 */
	public Operation checkCompleteness(Notice topInfo) {
		Notice opInfo = new Notice(
				"Completeness check for operation: " + getName());
		addNotice(opInfo);
		// Check that input bindings are present
		checkBindings(topInfo, opInfo);
		// Compute input partitions and evaluate each scenario.
		boolean blocking = false;
		boolean nondeterministic = false;
		Set<Predicate> partitions = getPartitions();
		if (partitions.isEmpty()) {
			// No guard conditions, check for one universal scenario.
			opInfo.addNotice(new Notice(
					"Operation accepts universal input"));
			List<Analysis> list = new ArrayList<Analysis>();
			for (Scenario scenario : getScenarios()) {
				list.add(new Analysis("Scenario " +
						scenario.getName() + " accepts universal input"));
			}
			if (list.isEmpty()) {
				opInfo.addNotice( new Warning(
						"No scenarios enabled for universal input"));
				blocking = true;				
			}
			else if (list.size() == 1) {
				opInfo.addNotice(list.get(0));
			}
			else {
				Warning warning = new Warning(
						"Multiple scenarios triggered by universal input");
				opInfo.addNotice(warning);
				for (Analysis analysis : list) {
					warning.addNotice(analysis);
				}
				nondeterministic = true;
			}
		}
		else {
			// Some guard conditions, therefore check for each partition.
			Notice parInfo = new Notice(
					"Operation partitions its input space:");
			opInfo.addNotice(parInfo);
			int count = 0;
			for (Predicate input : partitions) {
				++count;
				parInfo.addNotice(new Analysis("input " + count + 
						" = " + input.toString()));
			}
			count = 0;   // reset index
			for (Predicate input : partitions) {
				++count;
				List<Analysis> list = new ArrayList<Analysis>();
				for (Scenario scenario : getScenarios()) {
					Condition condition = scenario.getCondition();
					if (condition == null || condition.accepts(input)) {
						list.add(new Analysis("Scenario " +
								scenario.getName() + " accepts input " + count));
					}
				}
				if (list.isEmpty()) {
					opInfo.addNotice( new Warning(
							"No scenarios enabled for input " + count));
					blocking = true;
				}
				else if (list.size() == 1) {
					opInfo.addNotice(list.get(0));
				}
				else {
					Warning warning = new Warning(
							"Multiple scenarios triggered by input " + count);
					opInfo.addNotice(warning);
					for (Analysis analysis : list)
						warning.addNotice(analysis);
					nondeterministic = true;
				}
			}
		}
		if (nondeterministic) {
			topInfo.addNotice(new Warning(
					"Operation is nondeterministic: " + this));			
		}
		else if (blocking) {
			topInfo.addNotice(new Warning(
					"Operation is blocking: " + this));			
		}
		else
			topInfo.addNotice(new Analysis(
					"Operation is deterministic: " + this));
		return this;
	}
	
	/**
	 * Returns the possible symbolic input partitions for this Operation.
	 * For each Scenario, extracts the atomic predicates over each pair of
	 * parameters, discovered by refining the Scenario's Condition and its
	 * complement.  Partitions these predicates into sets, by indexing each
	 * set according to the pair of parameters governed by each predicate.
	 * Converts the partitioned sets into all possible combinations, taking
	 * one predicate from each set.  Converts each combination into a logical
	 * -AND Proposition governing the Predicates in the combination, and 
	 * adds this to the resulting set of Predicates.  If an Operation has a
	 * single Scenario with no Condition, returns an empty set, indicating
	 * that the Operation accepts universal input.  Otherwise, returns a set
	 * of predicates, each representing a different partition of the input
	 * space.
	 * @return the partitions of this Operation's input space.
	 */
	protected Set<Predicate> getPartitions() {
		// First stage: calculate all atomic predicates over each pair of 
		// Parameters and index these in sets, according to the unique pair
		// of Parameters that each predicate governs.
		Map<Object, Set<Predicate>> indexedSets = 
				new LinkedHashMap<Object, Set<Predicate>>();
		for (Scenario scenario : getScenarios()) {
			for (Predicate predicate : scenario.getPartitions()) {
				Object key = predicate.getExpressions();
				if (!indexedSets.containsKey(key)) {
					indexedSets.put(key,  new LinkedHashSet<Predicate>());
				}
				indexedSets.get(key).add(predicate);
			}
		}
		// Second stage: grow the tree of atomic predicate combinations,
		// combining every permutation of predicates, indexed by the same set
		// of operands, with every other such permutation.
		List<List<Predicate>> oldMatrix = new ArrayList<List<Predicate>>();
		oldMatrix.add(new ArrayList<Predicate>());  // oldMatrix contains empty list
		for (Set<Predicate> partitions : indexedSets.values()) {
			List<List<Predicate>> newMatrix = new ArrayList<List<Predicate>>();
			for (List<Predicate> oldList : oldMatrix) {
				for (Predicate predicate : partitions) {
					List<Predicate> newList = new ArrayList<Predicate>(oldList);
					newList.add(predicate);
					newMatrix.add(newList);
				}
			}
			oldMatrix = newMatrix;
		}
		// Third stage: combine each distinct combination in a conjunction,
		// if there is more than one predicate in the combination.  If there is
		// zero or one predicate, do nothing, or add the predicate itself.
		Set<Predicate> result = new LinkedHashSet<Predicate>();
		for (List<Predicate> terms : oldMatrix) {
			if (terms.size() < 2)  // Don't create for zero, one operand
				result.addAll(terms);
			else {
				Proposition proposition = new Proposition("and");
				for (Predicate predicate : terms) {
					proposition.addExpression(predicate);
				}
				if (proposition.isConsistent())
					result.add(proposition);
		     // else
			 //	    System.out.println("REJECT INCONSISTENT: " + proposition);
			}
		}
		return result;
	}
	
	protected void checkBindings(Notice topInfo, Notice opInfo) {
		for (Scenario scenario : getScenarios()) {
			Set<Input> inputs = getInputs();
			Binding binding = scenario.getBinding();
			if (binding != null) {
				for (Assignment assign : binding.getAssignments()) {
					inputs.remove(assign.operand(0));
				}
			}
			Warning topWarn = null;
			for (Input input : inputs) {
				if (topWarn == null) {
					topWarn = new Warning(
							"Some inputs are not bound in operation: " 
						+ this);
					topInfo.addNotice(topWarn);
				}
				opInfo.addNotice(new Warning("Input: " + input 
						+ " is not bound in scenario: " + scenario));
			}
		}
	}
	
	/**
	 * Returns a printed representation of this Operation.  Returns a String
	 * in the format "name(arg1, ... argN)", representing an invocation of
	 * this Operation.
	 */
	public String toString() {
		StringBuilder buffer = new StringBuilder(name);
		buffer.append('(');
		int count = 0;
		for (Input input : getInputs()) {
			if (count > 0)
				buffer.append(", ");
			buffer.append(input);
			++count;
		}
		buffer.append(')');
		return buffer.toString();
	}

}
