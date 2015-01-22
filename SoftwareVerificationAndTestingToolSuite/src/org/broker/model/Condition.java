package org.broker.model;

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Condition represents a logical guard, or a precondition.  A Condition 
 * represents the logical precondition under which a particular Scenario is
 * triggered.  A Condition always contains a single Predicate, a logical 
 * expression, which may be defined in terms of the values of the Inputs of 
 * the Operation and of state Variables in Memory.
 * 
 * The Condition's single Predicate is the formal constraint which must be
 * satisfied for some Scenario to be triggered.  A Condition may compare this
 * Predicate symbolically against other Predicates, to test whether they are
 * subsumed by the Condition's own Predicate.  In this way, it can evaluate
 * which symbolic constraints might be acceptable to its guard.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Condition extends Element {
	
	/**
	 * The predicate expression.
	 */
	private Predicate predicate;
	
	/**
	 * Creates a default Condition.
	 */
	public Condition() {
	}
		
	/**
	 * Adds the guard Predicate to this Condition.
	 * @param predicate the Predicate to attach.
	 * @return this Condition.
	 */
	public Condition addPredicate(Predicate predicate) {
		this.predicate = predicate;
		return this;
	}
			
	/**
	 * Returns the Predicate guard.
	 * @return the Predicate guard.
	 */
	public Predicate getPredicate() {
		if (predicate == null)
			semanticError("has no Proposition, Membership or Comparison.");
		return predicate;
	}
	
	/**
	 * Returns the complement of the guard Predicate.
	 * @return the negation of the guard Predicate.
	 */
	public Predicate getComplement() {
		return getPredicate().negate();
	}
	
	/**
	 * Returns the set of all possible atomic predicates and their negations
	 * that can be derived from this Condition's guard.  The returned set is
	 * not structured in any way.  The structuring of partitions is performed
	 * by Operation.
	 * @return a set of Predicates.
	 */
	public Set<Predicate> getPartitions() {
		Set<Predicate> result = new LinkedHashSet<Predicate>();
		result.addAll(getPredicate().refine());
		result.addAll(getComplement().refine());
		return result;
	}
	
	/**
	 * Determines whether this Condition would be satisfied by some actual 
	 * constraint.  The actual constraint must be defined in terms of the
	 * same Parameters as this Condition's guard.  Returns true, if the
	 * guard subsumes the actual constraint.
	 * @param actual a Predicate representing an actual constraint.
	 * @return true if this Condition's formal Predicate subsumes the actual
	 * Predicate.
	 */
	public boolean accepts(Predicate actual) {
		return getPredicate().subsumes(actual);
	}
	
	/**
	 * Causes this Condition to resolve its global/local Parameter references.
	 * @param scope a table of global and local Parameters.
	 * @return this Condition.
	 */
	public Condition resolve(Scope scope) {
		getPredicate().resolve(scope);
		return this;
	}
	
	/**
	 * Evaluates this guard Condition, returning true or false.  Evaluates
	 * the Predicate stored in this Condition.  The Predicate may be any kind
	 * of boolean-valued expression, and may depend on bound Parameters, such
	 * as Variables, Inputs and Constants.  Variables will have been bound at
	 * the start in Memory, and may also be modified by Effects.  Inputs will
	 * have been bound at the start of the Scenario owning this Condition.
	 * @return true, if this Condition is satisfied.
	 */
	public boolean evaluate() {
		// FAILSAFE EVALUATION MODE - UNSATISFIABLE GUARDS WITH NULL EXPRS
		try {
			return getPredicate().evaluate();
		}
		catch (NullPointerException ex) {
			System.out.println("WARNING: Condition contains undefined expression.");
			return false;
		}
	}

}
