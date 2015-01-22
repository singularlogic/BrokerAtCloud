package org.broker.model;


/**
 * Assignment represents a single variable-binding expression.  Assignment is
 * both an initialisation operator and a re-assignment operator.  It can be
 * used to populate Input, Output and Variable parameters with their initial
 * value; and it can also be used to re-assign a new value to a Variable.
 * The Assignment operator name indicates whether the assigned value should
 * be exactly equal to a given value, more or less than a given value, or
 * not equal to a given value.  The valid operator names include: equals,
 * notEquals, moreThan, lessThan.  The latter two operators may also be
 * used with a single operand for unit increment or decrement operations.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Assignment extends Function {
	
	protected void validate(String name) {
		String legalNames = " equals notEquals moreThan lessThan ";
		if (! legalNames.contains(" " + name + " "))
			semanticError("has an illegal operator name '" + name + "'.");
		maxOperands = 2;
	}

	/**
	 * Creates a default Assignment.  Sets the type to "Void".
	 */
	public Assignment() {
		setType("Void");
	}
	
	/**
	 * Creates a named Assignment.  The operator name indicates what kind of
	 * assignment is intended, from: "equals, notEquals, moreThan, lessThan".
	 * Also sets the type to "Void".
	 * @param name the operator name.
	 */
	public Assignment(String name) {
		super(name, "Void");
	}
	
	/**
	 * Creates a named and typed Assignment.  The operator name indicates the
	 * kind of assignment, from: "equals, notEquals, moreThan, lessThan". 
	 * Always sets the type to "Void", no matter what type was supplied
	 * (fail-safe).
	 * @param name the assignment operator name.
	 * @param type the type of the assignment, "Void".
	 */
	public Assignment(String name, String type) {
		super(name, "Void");
	}
	
	/**
	 * Sets the type of this Assignment.  Always sets the type to "Void", no
	 * matter what type was supplied (fail-safe).
	 */
	@Override
	public Assignment setType(String type) {
		this.type = "Void";
		return this;
	}

	/**
	 * Adds an expression as an operand to this Assignment expression.
	 * @param expression the operand expression.
	 * @return this functional expression.
	 */
	public Assignment addExpression(Expression expression) {
		if (expressions.isEmpty()) { 
			if (! expression.isAssignable())
				semanticError("has a non-assignable first operand");
		}
		return (Assignment) super.addExpression(expression);
	}
	
	/**
	 * Executes this Assignment on its operands.  Expects the first operand
	 * to be an assignable Parameter.  Performs one of an assignment,  a unit
	 * increment, or unit decrement operation.  The notEquals operator is 
	 * treated as a unit increment.
	 */
	public Object evaluate() {
		Parameter parameter = (Parameter) operand(0);
		Expression expression = null;
		if (expressions.size() > 1) {
			expression = operand(1);
		}
		if (name.equals("equals")) {
			Object value = expression.evaluate();
			parameter.assign(value);
		}
		else if (name.equals("lessThan")) {
			if (expression == null)
				parameter.assign(justLessThan(parameter.evaluate()));
			else
				parameter.assign(justLessThan(expression.evaluate()));
		}
		else {  // name.equals("moreThan") || name.equals("notEquals")
			if (expression == null)
				parameter.assign(justMoreThan(parameter.evaluate()));
			else
				parameter.assign(justMoreThan(expression.evaluate()));
		}
		return parameter;
	}
	
	/**
	 * Returns a value that just precedes a given value. 
	 * @param value the reference value.
	 * @return a value that precedes this.
	 */
	private Object justLessThan(Object value) {
		String valueType = operand(0).getType();
		if (valueType.equals("String"))
			return "Atag" + (String) value;  // earlier than value
		else if (valueType.equals("Integer"))
			return ((Integer) value) - 1;
		else if (valueType.equals("Double"))
			return ((Double) value) - 0.3;
		else if (valueType.equals("Short"))
			return ((Short) value) - 1;
		else if (valueType.equals("Long"))
			return ((Long) value) - 1;
		else if (valueType.equals("Float"))
			return ((Float) value) - 0.3;
		else if (valueType.equals("Boolean")) 
			return ! (Boolean) value;
		else if (valueType.equals("Character"))
			return (Character) ((Character) value) - 1;
		else if (valueType.equals("Byte"))
			return ((Byte) value) - 1;
		else {
			semanticError("cannot decrement value of type '" + 
					valueType + "'.");
			return null;
		}
	}
	
	/**
	 * Returns a value that just succeeds a given value.
	 * @param value the reference value.
	 * @return a value that succeeds this.
	 */
	private Object justMoreThan(Object value) {
		String valueType = operand(0).getType();
		if (valueType.equals("String"))
			return ((String) value) + "Ztag";  // later than value
		else if (valueType.equals("Integer"))
			return ((Integer) value) + 1;
		else if (valueType.equals("Double"))
			return ((Double) value) + 0.3;
		else if (valueType.equals("Short"))
			return ((Short) value) + 1;
		else if (valueType.equals("Long"))
			return ((Long) value) + 1;
		else if (valueType.equals("Float"))
			return ((Float) value) + 0.3;
		else if (valueType.equals("Boolean")) 
			return ! (Boolean) value;
		else if (valueType.equals("Character"))
			return (Character) ((Character) value) + 1;
		else if (valueType.equals("Byte"))
			return ((Byte) value) + 1;
		else {
			semanticError("cannot increment value of type '" + 
					valueType + "'.");
			return null;
		}
	}
	
}
