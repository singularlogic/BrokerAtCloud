package org.broker.model;

import java.util.ArrayList;
import java.util.List;


/**
 * Function represents a functional expression with a function root node.  A
 * Function represents any kind of executable expression that is dominated by
 * a principal operator that acts on zero or more operands.  Subclasses of 
 * Function cater to different kinds of operation.  The name of a Function is
 * validated, and may be used to constrain the legal number of operands.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public abstract class Function extends Expression {
	
	/**
	 * The maximum number of operands, initially inactive when set to -1.
	 */
	protected int maxOperands = -1;
	
	/**
	 * The operands of this function.
	 */
	protected List<Expression> expressions;
	
	/**
	 * Validates the chosen name for this Function and sets the maximum 
	 * number of operands.
	 * @param name the name for this Function.
	 * @throws SemanticError if the name is illegal.
	 */
	protected abstract void validate(String name) throws SemanticError;
	
	/**
	 * Creates a default functional expression.
	 */
	public Function() {
		expressions = new ArrayList<Expression>();
	}

	/**
	 * Creates a named functional expression.
	 * @param name the name of the function.
	 */
	public Function(String name) {
		super(name);
		expressions = new ArrayList<Expression>();
		validate(name);
	}

	/**
	 * Creates a named and typed functional expression.
	 * @param name the name of this functional expression.
	 * @param type the type of this functional expression.
	 */
	public Function(String name, String type) {
		super(name, type);
		expressions = new ArrayList<Expression>();
		validate(name);
	}
	
	/**
	 * Reports whether this function is equal to another object.  True if the
	 * other object is a function with the same name, type and operands as
	 * this; otherwise false.
	 * @param other the other object.
	 * @return true if both functions have the same name, type and operands.
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		else if (other instanceof Function) {
			Function function = (Function) other;
			return super.equals(other) &&
					safeEquals(expressions, function.expressions);
		}
		else
			return false;
	}

	/**
	 * Returns a quasi-unique hash code for this function.  The hash code is
	 * computed from the hash codes for the meta-type, name, type and operand
	 * expressions.
	 */
	@Override
	public int hashCode() {
		return super.hashCode() * 31 + safeHashCode(expressions);
	}

	/**
	 * Sets the name of this Function.  Validates the name.
	 */
	public Function setName(String name) {
		super.setName(name);
		validate(name);
		return this;
	}
	
	/**
	 * Returns the result type of this Function.  Returns the explicitly
	 * labelled type, if given, otherwise determines the type by heuristic.
	 * The default heuristic returns the type of the first operand, which
	 * works for most cases.  Caches the retrieved type as the type of this
	 * Function.
	 * @return the type of this Function.
	 */
	@Override
	public String getType() {
		if (type == null) {
			type = operand(0).getType();
		}
		return type;
	}
	
	/**
	 * Adds an expression as an operand to this functional expression.
	 * @param expression the operand expression.
	 * @return this functional expression.
	 */
	public Function addExpression(Expression expression) 
			throws SemanticError {
		if (maxOperands < 0 || expressions.size() < maxOperands)
			expressions.add(expression);
		else
			semanticError("has too many operands.");
		return this;
	}
	
	/**
	 * Returns a list of the operand expressions governed by this functional 
	 * expression.
	 * @return a list of this functional expression's expressions.
	 */
	public List<Expression> getExpressions() {
		return expressions;
	}
	
	/**
	 * Returns the given operand Expression at an index.
	 * @param index the index.
	 * @return the operand Expression.
	 */
	public Expression operand(int index) {
		return expressions.get(index);
	}
	
	/**
	 * Reports an attempt to assign to this Function as an error.
	 */
	@Override
	public void assign(Object value) {
		semanticError("cannot be re-assigned a new value.");
	}
	
	/**
	 * Unbinds the operands of this Function.  Propagates the request to each
	 * operand Expression.  Non-constant Parameters will release their bound
	 * values, and Functions will forward the request, recursively.
	 */
	@Override
	public void unbind() {
		for (Expression operand : getExpressions()) {
			operand.unbind();
		}
	}
	
	/**
	 * Rebinds the unbound operands of this Function.  Propagates the request
	 * to each operand Expression.  Non-constant Parameters will be reset to 
	 * their default values, and Functions will forward the request, 
	 * recursively.
	 */
	@Override
	public void rebind() {
		for (Expression operand : getExpressions()) {
			operand.rebind();
			// TODO: Needs work in specific Function subclasses to avoid nulls
			// DEBUG
			/*
			if (operand.evaluate() == null) {
				System.out.println("INSIDE: " + this);
				System.out.println("ERROR: " + operand + " still null after rebinding.");
				if (operand instanceof Function) {
					Function func = (Function) operand;
					for (Expression arg : func.getExpressions()) {
						System.out.println("-- " + arg + " = " + arg.evaluate());
					}
				}
			}
			*/
		}
	}
		
	/**
	 * Causes this Function to resolve its global/local Parameter references.
	 * The Expressions that are this Function's operands may contain arbitrary
	 * Functions or Parameters.  All Parameter references must be resolved to 
	 * declared Parameters in global Memory scope, or local Operation scope.
	 * This method ensures that all Parameter references actually correspond
	 * to some declared Parameter.  Substitutes all duplicate copies of such
	 * Parameters by a reference to the original copy.
	 */
	public Function resolve(Scope scope) {
		for (int i = 0; i < expressions.size(); ++i) {
			Expression current = operand(i);
			if (current instanceof Parameter) {
				Parameter original = scope.getParameter(current.getName());
				if (original == null)
					semanticError("refers to an undeclared Parameter: " 
							+ current.getName());
				else {
					if (current != original) {
						expressions.set(i, original);
					}
				}
			}
			else 
				current.resolve(scope);
		}
		return this;
	}
	
	/**
	 * Converts this function to a printable string.  The output is a string
	 * of the form: function(node1, node2) giving the name of the function
	 * and the recursive print-forms of the operands.
	 */
	public String toString() {
		StringBuilder buffer = new StringBuilder(name);
		buffer.append('(');
		int count = 0;
		for (Expression operand : getExpressions()) {
			if (count > 0)
				buffer.append(", ");
			buffer.append(operand.toString());
			++count;
		}
		buffer.append(')');
		return buffer.toString();
	}

}
