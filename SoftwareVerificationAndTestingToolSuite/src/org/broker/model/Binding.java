package org.broker.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * Binding represents a particular set of value-bindings to parameters.
 * A Binding contains a list of Assignments, which each bind a value to a
 * Parameter.  The kinds of Parameter which can be bound include Input, 
 * Output and Variable.  The kinds of value to be bound can be expressed 
 * exactly, or as an inequality in relation to some other value.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Binding extends Element {

	/**
	 * The set of Assignments in this Binding.  Each Assignment is indexed by
	 * the unique name of the Parameter that it binds.
	 */
	protected Map<String, Assignment> assignments;

	/**
	 * Creates a default Binding.
	 */
	public Binding() {
		assignments = new LinkedHashMap<String, Assignment>();
	}
	
	/**
	 * Adds an Assignment to this Binding.  Each Assignment in this Binding
	 * must bind a unique Parameter.
	 * @param assignment the Assignment to add.
	 * @return this Binding.
	 */
	public Binding addAssignment(Assignment assignment) {
		String paramName = assignment.operand(0).getName();
		if (assignments.containsKey(paramName))
			semanticError("redundantly updating Parameter " + paramName);
		assignments.put(paramName, assignment);
		return this;
	}

	/**
	 * Returns the Assignment to a named Parameter stored in this Binding.
	 * @param name the name of the assigned Parameter.
	 * @return the Assignment, or null.
	 */
	public Assignment getAssignment(String name) {
		return assignments.get(name);
	}

	/**
	 * Returns all the Assignments associated with this Binding.
	 * @return the list of Assignments.
	 */
	public Set<Assignment> getAssignments() {
		return new LinkedHashSet<Assignment>(assignments.values());
	}
	
	/**
	 * Causes this Binding to resolve its global/local Parameter references.
	 * For each Assignment in this Binding, checks whether the operands refer
	 * to Parameters already declared in the supplied Scope.  If so, replaces
	 * duplicate copies of the same Paramter by a reference to the original
	 * Parameter.
	 * @param scope a Scope declaring global/local Parameters.
	 * @return this Binding.
	 */
	public Binding resolve(Scope scope) {
		for (Assignment assignment : assignments.values()) {
			assignment.resolve(scope);
		}
		return this;
	}
	
	
	/**
	 * Executes all of the Assignments associated with this Binding.
	 */
	public Binding execute() {
		for (Assignment assignment : assignments.values()) {
			// DEBUG
			// System.out.println("Assignment: " + assignment);
			assignment.evaluate();
		}
		return this;
	}

}
