package org.broker.model;

import java.util.Collections;
import java.util.List;


/**
 * Predicate represents a boolean valued function with a predicate root node.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public abstract class Predicate extends Function {

	/**
	 * Creates a default Predicate.  Sets the type to "Boolean".
	 */
	public Predicate() {
		setType("Boolean");
	}

	/**
	 * Creates a named Predicate.  Also sets the type to "Boolean".
	 * @param name the name of this Predicate.
	 */
	public Predicate(String name) {
		super(name, "Boolean");
	}
	
	/**
	 * Creates a named and typed Predicate.  Always sets the type to 
	 * "Boolean", no matter what type was supplied (fail-safe).
	 * @param name the name of this Predicate.
	 */
	public Predicate(String name, String type) {
		super(name, "Boolean");
	}
	
	/**
	 * Sets the type of this Predicate.  Always sets the type to "Boolean",
	 * no matter what type was supplied (fail-safe).
	 */
	@Override
	public Predicate setType(String type) {
		this.type = "Boolean";
		return this;
	}
	
	/**
	 * Evaluates this Predicate.  The result of evaluating any Predicate is
	 * always going to be a Boolean, rather than an Object type.  Overrides
	 * the abstract method signature in Expression.
	 * @return true or false.
	 */
	@Override
	public abstract Boolean evaluate();
	
	/**
	 * Reports whether this Predicate is consistent.  Returns true by 
	 * default.  Some kinds of predicate are able to determine if they
	 * are logically inconsistent, in which case this returns false.
	 * @return true if this Predicate is consistent.
	 */
	public boolean isConsistent() {
		return true;
	}

	/**
	 * Returns the complement of this Predicate.  By default, returns the
	 * constructed Proposition not(X) wrapping this Predicate X.  Subclasses
	 * may have more specific and direct ways of expressing the complement, 
	 * such as reversing an inequality.
	 * @return the complement of this Predicate.
	 */
	public Predicate negate() {
		Predicate result = new Proposition("not");
		result.addExpression(this);
		return result;
	}
	
	/**
	 * Returns this Predicate in normal form.  By default, returns this
	 * Predicate unchanged.  In the Proposition subclass, simplifies the
	 * logical operators within a Proposition tree. 
	 * @return the normalised form of this Predicate.
	 */
	public Predicate normalise() {
		return this;
	}

	/**
	 * Returns a list of refinements of this Predicate.  By default, returns
	 * the singleton list containing this Predicate.  Subclasses may have
	 * more subtle refinements, such as notEquals being converted into both
	 * lessThan and moreThan.  This is used when calculating the symbolic
	 * partitions of the input space for an Operation.
	 * @return the refinements of this Predicate.
	 */
	public List<Predicate> refine() {
		return Collections.singletonList(this);
	}

	/**
	 * Tests whether this Predicate subsumes the other Predicate.  Determines
	 * whether this Predicate is more general than the other Predicate, by a
	 * process of symbolic evaluation.  Each of Comparison, Membership and 
	 * Proposition uses a different subsumption algorithm.  This is used when
	 * checking the guard conditions in every Scenario for possible blocking
	 * or non-determinism.
	 * @param other the other Predicate.
	 * @return true if this is more general than the other.
	 */
	public abstract boolean subsumes(Predicate other);
	
	/**
	 * Tests whether this Predicate subsumes the other Proposition.  This is
	 * useful in a number of subclasses of Predicate.  Converts the other
	 * Proposition into normal form.  If the result is an AND-Proposition,
	 * tests whether this Predicate subsumes one of the conjuncts.  If the
	 * result is an OR-Proposition, tests whether this Predicate subsumes
	 * all of the disjuncts.  If the result is a NOT-Proposition, reverses
	 * polarity and tests whether the negation of the other subsumes the 
	 * negation of this.  Returns false if this would lead to infinite
	 * regression.
	 * @param other the other Proposition.
	 * @return true, if this Predicate subsumes the other Proposition.
	 */
	protected boolean subsumesProposition(Proposition other) {
		String otherName = other.getName();
		if (otherName.equals("equals") || otherName.equals("implies"))
			return subsumesProposition(other.normalise());
		else if (otherName.equals("and"))
			return subsumesOneConjunctOf(other);
		else if (otherName.equals("or"))
			return subsumesAllDisjunctsOf(other);
		else { // otherName.equals("not")
			Predicate negatedThis = negate();
			if (negatedThis.getName().equals("not"))
				return false;  // halt infinite regression
			else  // reverse polarity
				return other.negate().subsumes(negatedThis);
		}
	}
	
	/**
	 * Tests whether this Predicate subsumes one conjunct of the other
	 * AND-Proposition.  Uses the AND-splitting rule to determine whether
	 * this Predicate subsumes just one conjunct.
	 * @param other an AND-Proposition.
	 * @return true if this Predicate subsumes one conjunct.
	 */
	protected boolean subsumesOneConjunctOf(Proposition other) {
		for (Predicate conjunct : other.getPredicates()) {
			if (subsumes(conjunct))
				return true;
		}
		return false;
	}
	
	/**
	 * Tests whether this Predicate subsumes all disjuncts of the other 
	 * OR-Proposition.  Uses the OR-merging rule to determine whether this
	 * Predicate subsumes all the disjuncts.
	 * @param other an OR-Proposition.
	 * @return true if this Predicate subsumes all disjuncts.
	 */
	protected boolean subsumesAllDisjunctsOf(Proposition other) {
		for (Predicate disjunct : other.getPredicates()) {
			if (! subsumes(disjunct))
				return false;
		}
		return true;
	}

}
