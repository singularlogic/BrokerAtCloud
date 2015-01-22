package org.broker.model;


/**
 * Arithmetic represents a numerical function with an arithmetic operator
 * root node.  The operands are expected to be numbers.  There are six 
 * arithmetic operators, whose names include:  plus, minus, times, divide,
 * modulo and negate.  There are six legal numeric types, including: Byte,
 * Short, Long, Integer, Float and Double.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Arithmetic extends Function {

	/**
	 * Validates the name of an Arithmetic expression and sets the expected
	 * number of operands.  All arithmetic operators expect two operands,
	 * apart from "negate", which expects one operand.
	 */
	protected void validate(String name) {
		String legalNames = " plus minus times divide modulo negate ";
		if (! legalNames.contains(" " + name + " "))
			semanticError("has an illegal operator name '" + name + "'.");
		if (name.equals("negate"))
			maxOperands = 1;
		else
			maxOperands = 2;
	}
	
	/**
	 * Creates a default Arithmetic expression.
	 */
	public Arithmetic() {
	}

	/**
	 * Creates a named Arithmetic expression.
	 * @param name the arithmetic operator name.
	 */
	public Arithmetic(String name) {
		super(name);
	}

	/**
	 * Creates a named and typed Arithmetic expression.
	 * @param name the name of the arithmetic operator.
	 * @param type the result type of the expression.
	 */
	public Arithmetic(String name, String type) {
		super(name, type);
	}

	/**
	 * Executes this Arithmetic on its operands.  Expects one operand for a
	 * negation, and two operands otherwise.  Converts all arguments into
	 * the expected numerical result type and branches according to the
	 * name of the arithmetic operation.
	 * @return the arithmetical result, as a Number.
	 */
	public Number evaluate() {
		// Type consistency checked here, not during model-building.
		String type0 = operand(0).getType();
		String type1 = operand(1).getType();
		if (! type0.equals(type1))
			semanticError("has operands of different types: " + 
					type0 + ", " + type1);
		if (getType().equals("Integer"))
			return evaluateInteger();
		else if (getType().equals("Double"))
			return evaluateDouble();
		else if (getType().equals("Long"))
			return evaluateLong();
		else if (getType().equals("Short"))
			return evaluateShort();
		else if (getType().equals("Byte"))
			return evaluateByte();
		else {
			semanticError("has an illegal numerical type " + getType());
			return null;
		}
	}
	
	protected Integer evaluateInteger() {
		Integer value0 = (Integer) operand(0).evaluate();
		if (name.equals("negate"))
			return -value0;
		else {
			Integer value1 = (Integer) operand(1).evaluate();
			if (name.equals("plus"))
				return value0 + value1;
			else if (name.equals("minus"))
				return value0 - value1;
			else if (name.equals("times"))
				return value0 * value1;
			else if (name.equals("divide"))
				return value0 / value1;
			else // name.equals("modulo")
				return value0 % value1;
		}
	}

	protected Double evaluateDouble() {
		Double value0 = (Double) operand(0).evaluate();
		if (name.equals("negate"))
			return -value0;
		else {
			Double value1 = (Double) operand(1).evaluate();
			if (name.equals("plus"))
				return value0 + value1;
			else if (name.equals("minus"))
				return value0 - value1;
			else if (name.equals("times"))
				return value0 * value1;
			else if (name.equals("divide"))
				return value0 / value1;
			else // name.equals("modulo")
				return value0 % value1;
		}
	}

	protected Float evaluateFloat() {
		Float value0 = (Float) operand(0).evaluate();
		if (name.equals("negate"))
			return -value0;
		else {
			Float value1 = (Float) operand(1).evaluate();
			if (name.equals("plus"))
				return value0 + value1;
			else if (name.equals("minus"))
				return value0 - value1;
			else if (name.equals("times"))
				return value0 * value1;
			else if (name.equals("divide"))
				return value0 / value1;
			else // name.equals("modulo")
				return value0 % value1;
		}
	}
	
	protected Long evaluateLong() {
		Long value0 = (Long) operand(0).evaluate();
		if (name.equals("negate"))
			return -value0;
		else {
			Long value1 = (Long) operand(1).evaluate();
			if (name.equals("plus"))
				return value0 + value1;
			else if (name.equals("minus"))
				return value0 - value1;
			else if (name.equals("times"))
				return value0 * value1;
			else if (name.equals("divide"))
				return value0 / value1;
			else // name.equals("modulo")
				return value0 % value1;
		}
	}

	/**
	 * Evaluates the operands as though they were Shorts.  The result is
	 * coerced back to a Short, which may result in overflow.
	 * @return the arithmetical result.
	 */
	protected Short evaluateShort() {
		Short value0 = (Short) operand(0).evaluate();
		if (name.equals("negate"))
			return (short) -value0;
		else {
			Short value1 = (Short) operand(1).evaluate();
			if (name.equals("plus"))
				return (short) (value0 + value1);
			else if (name.equals("minus"))
				return (short) (value0 - value1);
			else if (name.equals("times"))
				return (short) (value0 * value1);
			else if (name.equals("divide"))
				return (short) (value0 / value1);
			else // name.equals("modulo")
				return (short) (value0 % value1);
		}
	}

	/**
	 * Evaluates the operands as though they were Bytes.  The result is
	 * coerced back to a Byte, which may result in overflow.
	 * @return the arithmetical result.
	 */
	protected Byte evaluateByte() {
		Byte value0 = (Byte) operand(0).evaluate();
		if (name.equals("negate"))
			return (byte) -value0;
		else {
			Byte value1 = (Byte) operand(1).evaluate();
			if (name.equals("plus"))
				return (byte) (value0 + value1);
			else if (name.equals("minus"))
				return (byte) (value0 - value1);
			else if (name.equals("times"))
				return (byte) (value0 * value1);
			else if (name.equals("divide"))
				return (byte) (value0 / value1);
			else // name.equals("modulo")
				return (byte) (value0 % value1);
		}
	}

}
