package org.broker.model;

import java.util.ArrayList;
import java.util.List;


/**
 * Comparison represents an inequality expression, comparing two operands.
 * A Comparison is a kind of boolean expression that directly compares its 
 * two operands.  There are six comparison operations, whose names include:
 * equals, notEquals, lessThan, moreThan, notLessThan, and notMoreThan.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Comparison extends Predicate {
	
	/**
	 * Validates the name of a Comparison predicate and sets the expected
	 * number of operands.  All Comparisons expect two operands.
	 */
	protected void validate(String name) {
		String legalNames = 
				" equals notEquals lessThan moreThan notLessThan notMoreThan ";
		if (! legalNames.contains(" " + name + " "))
			semanticError("has an illegal operator name '" + name + "'.");
		maxOperands = 2;
	}
	
	/**
	 * Creates a default Comparison.  The result type is automatically set
	 * to "Boolean".
	 */
	public Comparison() {
	}
	
	/**
	 * Creates a Comparison with the given name.  Checks that the name is
	 * one of the legal names for a Comparison.  The result type is
	 * automatically set to "Boolean".
	 * @param name the name of this Comparison.
	 */
	public Comparison(String name) {
		super(name);
	}
	
	/**
	 * Tests whether this Comparison subsumes the other Predicate.  If
	 * the other Predicate is also a Comparison, tests whether this is a 
	 * broader Comparison than the other.  If the other Predicate is an
	 * AND-Proposition, tests whether this Comparison subsumes one of the
	 * other's conjuncts.  Otherwise, returns false.
	 * @param other the other Predicate.
	 * @return true if this Comparison subsumes the other Predicate.
	 */
	public boolean subsumes(Predicate other) {
		if (other instanceof Comparison) {
			Comparison comparison = (Comparison) other;
			return subsumesComparison(comparison) || 
					subsumesComparison(comparison.reverse());
		}
		else if (other instanceof Proposition)
			return subsumesProposition((Proposition) other);
		else
			return false;  // other is a Membership predicate
	}
	
	/**
	 * Tests whether this comparison subsumes the other comparison.  If
	 * the two conditions test the same operands, then if this condition
	 * is identical to, or broader than, the other condition, returns true;
	 * otherwise returns false.
	 * @param other the other comparison.
	 * @return true if this comparison subsumes the other one.
	 */
	protected boolean subsumesComparison(Comparison other) {
		if (getExpressions().equals(other.getExpressions())) {
			if (name.equals(other.name))
				return true;
			else if (name.equals("notEquals"))
				return (other.name.equals("lessThan") ||
						other.name.equals("moreThan"));
			else if (name.equals("notMoreThan"))
				return (other.name.equals("equals") ||
						other.name.equals("lessThan"));
			else if (name.equals("notLessThan"))
				return (other.name.equals("equals") ||
						other.name.equals("moreThan"));
			else
				return false;
		}
		else
			return false;
	}

	/**
	 * Reports whether this Comparison is consistent.  Returns false in the
	 * two cases of: lessThan(x, bottom) and moreThan(bottom, y).  Otherwise
	 * returns true.
	 * @return true if this Comparison is consistent.
	 */
	@Override
	public boolean isConsistent() {
		String name = getName();
		if (name.equals("lessThan"))
			return ! operand(1).isBottom();
		else if (name.equals("moreThan"))
			return ! operand(0).isBottom();
		else
			return true;
	}
	
	/**
	 * Returns the complement of this Comparison.  Negates the inequality
	 * relationship, but keeps the same operands.
	 */
	public Comparison negate() {
		Comparison result = new Comparison(getNegatedName());
		for (Expression operand : getExpressions()) {
			result.addExpression(operand);
		}
		return result;
	}
	
	/**
	 * Returns the symmetric reversal of this Comparison.  Reverses the 
	 * inequality relationship and reverses the order of operands.
	 */
	public Comparison reverse() {
		Comparison result = new Comparison(getReversedName());
		result.addExpression(operand(1)).addExpression(operand(0));
		return result;
	}
	
	/**
	 * Algorithm for returning the negated name of this Comparison.  Used
	 * when constructing the complement of this Comparison.
	 * @return the negated name.
	 */
	private String getNegatedName() {
		String name = getName();
		if (name.startsWith("not")) {
			if (name.equals("notEquals"))
				return "equals";
			else if (name.equals("notLessThan"))
				return "lessThan";
			else // "notMoreThan"
				return "moreThan";
		}
		else {
			if (name.equals("equals"))
				return "notEquals";
			else if (name.equals("lessThan"))
				return "notLessThan";
			else // "moreThan"
				return "notMoreThan";
		}
	}
	
	/**
	 * Algorithm for returning the reversed name of this Comparison.  Used
	 * when constructing the symmetrically reversed Comparison.
	 * @return the reversed name.
	 */
	private String getReversedName() {
		String name = getName();
		if (name.startsWith("not")) {
			if (name.equals("notLessThan"))
				return "notMoreThan";
			else if (name.equals("notMoreThan"))
				return "notLessThan";
			else // "notEquals"
				return name;
		}
		else {
			if (name.equals("lessThan"))
				return "moreThan";
			else if (name.equals("moreThan"))
				return "lessThan";
			else // "equals"
				return name;
		}
	}
	
	/**
	 * Rebinds this Comparison, so that it may be tested for satisfaction.
	 * If the first operand is an unbound Parameter, assigns it a value, such
	 * that this Comparison is satisfied with respect to the rebound value of
	 * the second operand.  Otherwise, if the second operand is an unbound
	 * Parameter, assigns it a value, such that this Comparison is satisfied
	 * with respect to the rebound value of the first operand.  Otherwise,
	 * propagates the rebinding request to both operands.
	 */
	public void rebind() {
		Expression op0 = operand(0);
		Expression op1 = operand(1);
		if (op0.isAssignable() && op0.evaluate() == null) {
			op1.rebind();
			Assignment assign = new Assignment(getName());
			assign.addExpression(op0).addExpression(op1);
			assign.evaluate();
		}
		else if (operand(1).isAssignable() && op1.evaluate() == null) {
			op0.rebind();
			Assignment assign = new Assignment(getReversedName());
			assign.addExpression(op1).addExpression(op0);
			assign.evaluate();
		}
		else 
			super.rebind();
	}
	
	/**
	 * Returns a list of refinements of this Comparison.  If this Comparison
	 * can be broken down into more atomic Comparisons, breaks it down into
	 * its atomic Comparisons, returning these as a list.  Otherwise, returns
	 * this Comparison as a singleton list.
	 * @return the refinements of this Comparison.
	 */
	public List<Predicate> refine() {
		if (name.startsWith("not") && operand(0).isOrdered()) {
			Comparison refine1 = new Comparison();
			Comparison refine2 = new Comparison();
			List<Predicate> result = new ArrayList<Predicate>();
			if (name.equals("notEquals")) {
				refine1.setName("lessThan");
				refine2.setName("moreThan");
			}
			else if (name.equals("notMoreThan")) {
				refine1.setName("equals");
				refine2.setName("lessThan");
			}
			else {  // name.equals("notLessThan")
				refine1.setName("equals");
				refine2.setName("moreThan");
			}
			for (Expression operand : expressions) {
				refine1.addExpression(operand);
				refine2.addExpression(operand);
			}
			// Exclude inconsistent comparisons against bottom
			if (refine1.isConsistent())
				result.add(refine1);
			if (refine2.isConsistent())
				result.add(refine2);
			return result;
		}
		else 
			return super.refine();
	}

	/**
	 * Executes this Comparison on its operands.  Converts both of its
	 * operands to values of the same type, then branches according to the
	 * name of the comparison operation.
	 * @return true if the comparison holds between the operands.
	 */
	public Boolean evaluate() throws SemanticError {
		// Type consistency checked here, not during model-building.
		String type0 = operand(0).getType();
		String type1 = operand(1).getType();
		if (! type0.equals(type1))
			semanticError("has operands of different types: " + 
					type0 + ", " + type1);
		if (type0.equals("String"))
			return compareString();
		else if (type0.equals("Integer"))
			return compareInteger();
		else if (type0.equals("Double"))
			return compareDouble();
		else if (type0.equals("Float"))
			return compareFloat();
		else if (type0.equals("Long"))
			return compareLong();
		else if (type0.equals("Short"))
			return compareShort();
		else if (type0.equals("Byte"))
			return compareByte();
		else if (type0.equals("Character"))
			return compareCharacter();
		else {
			return compareObject();  // Last ditch attempt
		}
	}
	
	/**
	 * Compares two String operands of this Comparison.
	 * @return true, if the comparison holds.
	 */
	protected Boolean compareString() {
		String value0 = (String) operand(0).evaluate();
		String value1 = (String) operand(1).evaluate();
		if (name.equals("equals"))
			return value0.equals(value1);
		else if (name.equals("notEquals"))
			return ! value0.equals(value1);
		else if (name.equals("lessThan"))
			return value0.compareTo(value1) < 0;
		else if (name.equals("moreThan"))
			return value0.compareTo(value1) > 0;
		else if (name.equals("notLessThan"))
			return value0.compareTo(value1) >= 0;
		else  // name.equals("notMoreThan")
			return value0.compareTo(value1) <= 0;
	}
	
	/**
	 * Compares two Boolean operands of this Comparison.
	 * @return true, if the comparison holds.
	 */
	protected Boolean compareInteger() {
		Integer value0 = (Integer) operand(0).evaluate();
		Integer value1 = (Integer) operand(1).evaluate();
		if (name.equals("equals"))
			return value0.equals(value1);
		else if (name.equals("notEquals"))
			return ! value0.equals(value1);
		else if (name.equals("lessThan"))
			return value0.compareTo(value1) < 0;
		else if (name.equals("moreThan"))
			return value0.compareTo(value1) > 0;
		else if (name.equals("notLessThan"))
			return value0.compareTo(value1) >= 0;
		else  // name.equals("notMoreThan")
			return value0.compareTo(value1) <= 0;
	}
	
	/**
	 * Compares two Double operands of this Comparison.
	 * @return true, if the comparison holds.
	 */
	protected Boolean compareDouble() {
		Double value0 = (Double) operand(0).evaluate();
		Double value1 = (Double) operand(1).evaluate();
		if (name.equals("equals"))
			return value0.equals(value1);
		else if (name.equals("notEquals"))
			return ! value0.equals(value1);
		else if (name.equals("lessThan"))
			return value0.compareTo(value1) < 0;
		else if (name.equals("moreThan"))
			return value0.compareTo(value1) > 0;
		else if (name.equals("notLessThan"))
			return value0.compareTo(value1) >= 0;
		else  // name.equals("notMoreThan")
			return value0.compareTo(value1) <= 0;
	}
	
	/**
	 * Compares two Float operands of this Comparison.
	 * @return true, if the comparison holds.
	 */
	protected Boolean compareFloat() {
		Float value0 = (Float) operand(0).evaluate();
		Float value1 = (Float) operand(1).evaluate();
		if (name.equals("equals"))
			return value0.equals(value1);
		else if (name.equals("notEquals"))
			return ! value0.equals(value1);
		else if (name.equals("lessThan"))
			return value0.compareTo(value1) < 0;
		else if (name.equals("moreThan"))
			return value0.compareTo(value1) > 0;
		else if (name.equals("notLessThan"))
			return value0.compareTo(value1) >= 0;
		else  // name.equals("notMoreThan")
			return value0.compareTo(value1) <= 0;
	}
	
	/**
	 * Compares two Long operands of this Comparison.
	 * @return true, if the comparison holds.
	 */
	protected Boolean compareLong() {
		Long value0 = (Long) operand(0).evaluate();
		Long value1 = (Long) operand(1).evaluate();
		if (name.equals("equals"))
			return value0.equals(value1);
		else if (name.equals("notEquals"))
			return ! value0.equals(value1);
		else if (name.equals("lessThan"))
			return value0.compareTo(value1) < 0;
		else if (name.equals("moreThan"))
			return value0.compareTo(value1) > 0;
		else if (name.equals("notLessThan"))
			return value0.compareTo(value1) >= 0;
		else  // name.equals("notMoreThan")
			return value0.compareTo(value1) <= 0;
	}
	
	/**
	 * Compares two Short operands of this Comparison.
	 * @return true, if the comparison holds.
	 */
	protected Boolean compareShort() {
		Short value0 = (Short) operand(0).evaluate();
		Short value1 = (Short) operand(1).evaluate();
		if (name.equals("equals"))
			return value0.equals(value1);
		else if (name.equals("notEquals"))
			return ! value0.equals(value1);
		else if (name.equals("lessThan"))
			return value0.compareTo(value1) < 0;
		else if (name.equals("moreThan"))
			return value0.compareTo(value1) > 0;
		else if (name.equals("notLessThan"))
			return value0.compareTo(value1) >= 0;
		else  // name.equals("notMoreThan")
			return value0.compareTo(value1) <= 0;
	}
	
	/**
	 * Compares two Byte operands of this Comparison.
	 * @return true, if the comparison holds.
	 */
	protected Boolean compareByte() {
		Byte value0 = (Byte) operand(0).evaluate();
		Byte value1 = (Byte) operand(1).evaluate();
		if (name.equals("equals"))
			return value0.equals(value1);
		else if (name.equals("notEquals"))
			return ! value0.equals(value1);
		else if (name.equals("lessThan"))
			return value0.compareTo(value1) < 0;
		else if (name.equals("moreThan"))
			return value0.compareTo(value1) > 0;
		else if (name.equals("notLessThan"))
			return value0.compareTo(value1) >= 0;
		else  // name.equals("notMoreThan")
			return value0.compareTo(value1) <= 0;
	}
	
	/**
	 * Compares two Character operands of this Comparison.
	 * @return true, if the comparison holds.
	 */
	protected Boolean compareCharacter() {
		Character value0 = (Character) operand(0).evaluate();
		Character value1 = (Character) operand(1).evaluate();
		if (name.equals("equals"))
			return value0.equals(value1);
		else if (name.equals("notEquals"))
			return ! value0.equals(value1);
		else if (name.equals("lessThan"))
			return value0.compareTo(value1) < 0;
		else if (name.equals("moreThan"))
			return value0.compareTo(value1) > 0;
		else if (name.equals("notLessThan"))
			return value0.compareTo(value1) >= 0;
		else  // name.equals("notMoreThan")
			return value0.compareTo(value1) <= 0;
	}
	
	/**
	 * Compares two arbitrary Object operands of this Comparison.  Allows
	 * comparison for equals, notEquals only and raises an exception if any
	 * inequation comparison is made.
	 * @return true, if the comparison holds.
	 */
	protected Boolean compareObject() {
		Object value0 = operand(0).evaluate();
		Object value1 = operand(1).evaluate();
		if (name.equals("equals"))
			return value0.equals(value1);
		else if (name.equals("notEquals"))
			return ! value0.equals(value1);
		else {
			String type0 = operand(0).getType();
			semanticError("inequations are not valid for type " + type0);
			return false;
		}
	}
	
}

