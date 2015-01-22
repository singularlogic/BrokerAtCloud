package org.broker.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * Scope represents a scope within which parameters may be declared.  There
 * are two specific kinds of Scope:  Memory introduces Parameters declared
 * in global scope; whereas Operation introduces Parameters declared in a
 * local scope.  Scope defines the table of Parameters used by both of these
 * descendants, and also provides a means of merging global and local Scopes.
 * Scope is a kind of Annotated element, since both Memory and Operation 
 * nodes may receive Warning notices during checking.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Scope extends Annotated {
	
	/**
	 * The set of Parameters in Scope.  A Parameter may be a Constant, a 
	 * Variable, an Input or an Output.
	 */
	protected Map<String, Parameter> parameters;

	/**
	 * Creates an unnamed Scope.
	 */
	public Scope() {
		parameters = new LinkedHashMap<String, Parameter>();
	}
	
	/**
	 * Creates a named Scope.
	 * @param name the name of this Scope.
	 */
	protected Scope(String name) {
		super(name);
		parameters = new LinkedHashMap<String, Parameter>();
	}

	/**
	 * Returns the named Parameter stored in this Scope.
	 * @param name the name of the Parameter.
	 * @return the Parameter, or null.
	 */
	public Parameter getParameter(String name) {
		return parameters.get(name);
	}
	
	/**
	 * Returns the set of all Parameters, both Constants and Variables.
	 * @return the set of all Parameters in this Scope.
	 */
	public Set<Parameter> getParameters() {
		Set<Parameter> result = 
				new LinkedHashSet<Parameter>(parameters.values());
		return result;
	}
	
	/**
	 * Adds a Parameter to this Scope.
	 * @param parameter the Parameter to add.
	 * @return this Scope.
	 */
	public Scope addParameter(Parameter parameter) {
		parameters.put(parameter.getName(), parameter);
		return this;
	}
	
	/**
	 * Adds the contents of another Scope to this Scope.  This is used when
	 * building a Scope table of all the global Memory Parameters and local
	 * Operation Parameters to pass to a Scenario, when resolving the scopes
	 * of references to these Parameters.
	 * @param scope another Scope.
	 * @return this Scope.
	 */
	public Scope addScope(Scope scope) {
		for (Parameter parameter : scope.getParameters())
			addParameter(parameter);
		return this;
	}
	
	/**
	 * Unbinds the variable Parameters in this Scope.  For every Parameter
	 * that is not a Constant, releases the current binding.  The bound
	 * value of Variable, Input and Output parameters will be set to null.
	 * @return this Scope.
	 */
	public Scope unbind() {
		for (Parameter param : parameters.values()) {
			param.unbind();
		}
		return this;
	}

}
