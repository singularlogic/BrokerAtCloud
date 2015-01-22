package org.broker.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Membership represents a cardinality or membership predicate on a 
 * collection.  There are six membership operators:  isEmpty, notEmpty,
 * includes, excludes, includesAll, excludesAll.  The first operand is 
 * expected to be some kind of collection.  The second operand is either
 * absent (isEmpty, notEmpty), or an element (includes, excludes), or 
 * another collection (includesAll, excludesAll), or an arbitrary key 
 * (includesKey, excludesKey).
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Membership extends Predicate {

	/**
	 * Validates the name of a Membership predicate and sets the expected
	 * number of operands.  All Membership predicates expect two operands,
	 * apart from isEmpty and notEmpty, which expect one operand.
	 */
	@Override
	protected void validate(String name) {
		String legalNames = 
				" isEmpty notEmpty includes excludes " +
				"includesAll excludesAll includesKey excludesKey ";
		if (! legalNames.contains(" " + name + " "))
			semanticError("has an illegal operator name '" + name + "'.");
		if (name.equals("isEmpty") || name.equals("notEmpty"))
			maxOperands = 1;
		else
			maxOperands = 2;
	}
	
	/**
	 * Creates a default Membership predicate.
	 */
	public Membership() {
	}
	
	/**
	 * Creates a named Membership predicate.  Ensures that the name is one
	 * of the allowed membership operators.
	 * @param name the name of the membership predicate.
	 */
	public Membership(String name) {
		super(name);
	}
	
	/**
	 * Reports whether this Membership predicate is consistent.  Returns 
	 * false in the	four cases:  includes(bottom, x), includesAll(bottom, y),
	 * includesKey(bottom, z) and notEmpty(bottom), where bottom is the
	 * Constant representing an empty collection.  Otherwise returns true.
	 * @return true if this Membership predicate is consistent.
	 */
	@Override
	public boolean isConsistent() {
		String name = getName();
		if (name.startsWith("includes"))
			return ! operand(0).isBottom();
		else if (name.equals("notEmpty"))
			return ! operand(0).isBottom();
		else
			return true;
	}
	
	/**
	 * Tests whether this Membership predicate subsumes the other Predicate.
	 * If the other is also a Membership predicate, tests whether this
	 * subsumes the other.  If the other is an AND-Proposition, tests whether
	 * this Membership predicate subsumes one of the other's conjuncts.  
	 * Otherwise returns false.
	 * @param other the other Predicate.
	 * @return true if this subsumes the other Predicate.
	 */
	@Override
	public boolean subsumes(Predicate other) {
		if (other instanceof Membership)
			return subsumesMembership((Membership) other);
		else if (other instanceof Proposition)
			return subsumesProposition((Proposition) other);
		else 
			return false; // other is a Comparison predicate
	}

	/**
	 * Reports whether this Membership predicate subsumes the other.  This
	 * is only true if the predicates are equal (we cannot currently reason
	 * about includesAll subsuming includes).
	 */
	protected boolean subsumesMembership(Membership other) {
		return equals(other);
	}

	/**
	 * Returns the complement of this Membership predicate.  Converts 
	 * pairwise between the predicates isEmpty/notEmpty, includes/excludes
	 * and includesKey/excludesKey.  The two predicates includesAll and 
	 * excludesAll cannot be so simply complemented, so are wrapped in a
	 * negated Proposition.
	 * @return the logical complement of this Membership predicate.
	 */
	@Override
	public Predicate negate() {
		if (name.endsWith("All"))
			return super.negate();
		else {
			Membership result = new Membership(getNegatedName());
			for (Expression operand : expressions) {
				result.addExpression(operand);
			}
			return result;
		}
	}

	/**
	 * Algorithm for returning the negated name of this Membership.
	 * @return the negated name.
	 */
	private String getNegatedName() {
		String name = getName();
		if (name.startsWith("inc")) {
			if (name.equals("includes"))
				return "excludes";
			else if (name.equals("includesKey"))
				return "excludesKey";
			else // "includesAll"
				return "excludesAll";
		}
		else if (name.startsWith("exc")) {
			if (name.equals("excludes"))
				return "includes";
			else if (name.equals("excludesKey"))
				return "includesKey";
			else // "excludesAll"
				return "includesAll";		
		}
		else if (name.equals("isEmpty"))
			return "notEmpty";
		else // "notEmpty"
			return "isEmpty";
	}

	/**
	 * Rebinds this Membership predicate, so that it may be tested for 
	 * satisfaction.  If the first collection-typed operand is an unbound
	 * Parameter, binds it to a value such that this Membership predicate
	 * is satisfied with respect to any other rebound operands.
	 */
	public void rebind() {
		String name = getName();
		if (name.endsWith("Empty"))
			 rebindEmpty();
		else if (name.endsWith("Key"))
			 rebindKey();
		else if (name.startsWith("incl"))
			 rebindInclude();
		else // excl
			 rebindExclude();
	}
	
	/**
	 * Rebinds this empty-testing Membership predicate.  Binds the only
	 * collection-operand such that it satisfies this Membership predicate's
	 * "isEmpty" or "notEmpty" constraint.
	 */
	private void rebindEmpty() {
		Expression op0 = operand(0);
		String colType = op0.getType();
		if (op0.isAssignable() && op0.evaluate() == null) {
			op0.rebind();  // creates an empty collection
			if (getName().equals("notEmpty")) {
				Manipulation manip = new Manipulation();
				manip.addExpression(op0);
				Constant value = new Constant("value", valueType(colType));
				if (colType.startsWith("Map")) {
					manip.setName("insertAt");
					Constant key = new Constant("key", keyType(colType));
					manip.addExpression(key).addExpression(value);
				}
				else { // "Set" or "List"
					manip.setName("insert");
					manip.addExpression(value);
				}
				manip.evaluate();
			}
		}
		else
			super.rebind();
	}

	/**
	 * Rebinds this element-inclusion Membership test.  Either binds the
	 * first operand to a collection including the second operand, or binds
	 * the second operand to an included element or sub-collection, such 
	 * that it satisfies this Membership predicate's "includes" or 
	 * "includesAll" constraint.  Binds the first operand preferentially,
	 * unless it is not assignable.
	 */
	private void rebindInclude() {
		Expression op0 = operand(0);
		Expression op1 = operand(1);
		if (op0.isAssignable()) {
			// Bind op0 to some collection including op1.
			op1.rebind();
			op0.rebind();  // to an empty collection; otherwise bound
			Manipulation manip = new Manipulation();
			manip.addExpression(op0).addExpression(op1);
			if (getName().equals("includesAll"))
				manip.setName("insertAll");
			else // "includes"
				manip.setName("insert");
			manip.evaluate();
		}
		else if (op1.isAssignable()) {
			// Bind op1 to some included element or subset of op0.
			op0.rebind();
			if (op1.evaluate() == null) {
				if (getName().equals("includesAll")) {
					op1.assign(op0);
				}
				else { // "includes"
					op1.rebind();  // default value
					for (Object element : (Collection<?>) op0.evaluate()) {
						op1.assign(element);  // actual value
						break;
					}
				}
			}
		}
		else
			super.rebind();
	}
	
	/**
	 * Rebinds this element-exclusion Membership test.  Either binds the
	 * first operand to a collection excluding the second operand, or binds
	 * the second operand to an excluded element or sub-collection, such 
	 * that it satisfies this Membership predicate's "excludes" or 
	 * "excludesAll" constraint.  Binds the first operand preferentially,
	 * unless it is not assignable.  Warning: the search for an excluded
	 * element assumes that incrementing the element will eventually 
	 * discover one not present in the collection.  This may not be
	 * appropriate for some kinds of element.
	 */
	private void rebindExclude() {
		Expression op0 = operand(0);
		Expression op1 = operand(1);
		if (op0.isAssignable()) {
			// Bind op0 to some collection excluding op1.
			op1.rebind();
			op0.rebind();  // to an empty collection; otherwise bound
			Manipulation manip = new Manipulation();
			manip.addExpression(op0).addExpression(op1);
			if (getName().equals("excludesAll"))
				manip.setName("removeAll");
			else // "excludes"
				manip.setName("remove");
			manip.evaluate();
		}
		else if (op1.isAssignable()) {
			// Bind op1 to some excluded element or subset of op0.
			op0.rebind();
			if (op1.evaluate() == null) {
				if (getName().equals("excludesAll")) {
					op1.rebind();  // to empty collection
				}
				else { // "excludes"
					op1.rebind();  // default value
					Assignment assign = new Assignment("moreThan");
					assign.addExpression(op1);
					while (! evaluate())  // POSSIBLY DANGEROUS!!
						assign.evaluate();  // actual value
				}
			}
		}
		else
			super.rebind();
	}
	
	/**
	 * Rebinds this key-based Membership predicate.  Either binds the first
	 * operand to a map that includes or excludes the second key operand, or
	 * binds the second operand to an included, or excluded key, such that
	 * it satisfies this Membership predicate's "includesKey" or 
	 * "excludesKey" constraint.  Binds the first operand preferentially,
	 * unless it is not assignable.  Warning: the search for an excluded key
	 * assumes that incrementing the key will eventually discover one not
	 * present in the map.  This may not be appropriate for some kinds of
	 * key.
	 */
	private void rebindKey() {
		Expression op0 = operand(0);  // the map
		Expression op1 = operand(1);  // the key
		if (op0.isAssignable()) {
			// Bind op0 to some map including/excluding key op1.
			op1.rebind();
			op0.rebind();  // to an empty collection; otherwise bound
			Manipulation manip = new Manipulation();
			manip.addExpression(op0).addExpression(op1);
			if (getName().equals("includesKey")) {
				manip.setName("insertAt");
				Constant op2 = new Constant("value", valueType(getType()));
				manip.addExpression(op2);
			}
			else { // "excludesKey" 
				manip.setName("removeAt");
			}
			manip.evaluate();
		}
		else if (op1.isAssignable()) {
			// Bind op1 to some key included in/excluded by map op0.
			op0.rebind();
			if (getName().equals("includesKey")) {
				op1.rebind();
				for (Object key : ((Map<?,?>) op0.evaluate()).keySet()) {
					op1.assign(key);
					break;
				}
			}
			else {  // "excludesKey"
				op1.rebind();  // default value
				Assignment assign = new Assignment("moreThan");
				assign.addExpression(op1);
				while (! evaluate())  // POSSIBLY DANGEROUS!!
					assign.evaluate();  // actual value
			}
		}
		else
			super.rebind();
	}
	
	/**
	 * Extracts the element-type String from the collection-type String.
	 * Identifies the single parameter, or the second of two parameters, 
	 * between square brackets [].  If two parameters exist, they must be
	 * separated by a single space.
	 * @param collectionType the type of the collection.
	 * @return the element-type of the collection.
	 */
	private String valueType(String collectionType) {
		int left = collectionType.indexOf('[');
		int right = collectionType.indexOf(']');
		if (left == -1 || right == -1)
			semanticError("has an illegal collection type " + collectionType);
		int space = collectionType.indexOf(' ');
		int start = (space < 0 ? left : space) + 1;
		return collectionType.substring(start, right);
	}

	/**
	 * Extracts the key-type String from the map-type String.  Identifies the
	 * first of two parameters between square brackets [].  There must exist
	 * two parameters, separated by a single space.
	 * @param mapType the type of the map-collection.
	 * @return the key-type of the map.
	 */
	private String keyType(String mapType) {
		int left = mapType.indexOf('[');
		int right = mapType.indexOf(']');  // just checking
		int end = mapType.indexOf(',');
		if (left == -1 || right == -1 || end == -1)
			semanticError("has an illegal map type " + mapType);
		return mapType.substring(left + 1, end);
	}
		
	/**
	 * Executes this Membership on its operands.  Expects two operands, the
	 * first of which is some collection.  The second operand is an element
	 * of the collection for the operations: includes, excludes; or another
	 * collection, for:  includesAll, excludesAll; or a key for includesKey.  
	 * Converts the operands to suitable types, then branches according to
	 * the name of the operation.
	 * @return true, if the membership test holds between the operands.
	 */
	public Boolean evaluate() {
		String valType = operand(0).getType();
		if (valType.startsWith("List"))
			return listMembership();
		else if (valType.startsWith("Set"))
			return setMembership();
		else if (valType.startsWith("Map"))
			return mapMembership();
		else {
			semanticError("operand '" + operand(0).getName() + 
					"' has a non-collection type '" + valType + "'.");
			return false;
		}
	}
	
	/**
	 * Membership tests for a List operand.  Evaluates empty/non-empty tests
	 * on the first List operand, and inclusion/exclusion tests on both 
	 * operands.  Reports a semantic error for a key-based predicate.
	 * @return true, if this list-Membership test holds.
	 */
	@SuppressWarnings("unchecked")
	protected Boolean listMembership() {
		List<Object> value0 = (List<Object>) operand(0).evaluate();
		String opName = getName();
		if (opName.endsWith("Empty")) {
			if (opName.equals("isEmpty"))
				return value0.isEmpty();
			else  // "notEmpty"
				return ! value0.isEmpty();
		}
		else if (opName.endsWith("All")) {
			List<Object> value1 = (List<Object>) operand(1).evaluate();
			if (opName.equals("includesAll"))
				return value0.containsAll(value1);
			else { // "excludesAll"
				for (Object object : value1) {
					if (value0.contains(object))
						return false;
				}
				return true;
			}
		}
		else {
			Object value1 = operand(1).evaluate();
			if (opName.equals("includes"))
				return value0.contains(value1);
			else if (opName.equals("excludes"))
				return ! value0.contains(value1);
			else {  // "includesKey" || "excludesKey"
				semanticError("has an illegal operator name " + opName);
				return false;
			}
		}
	}
	
	/**
	 * Membership tests for a Set operand.  Evaluates empty/non-empty tests
	 * on the first Set operand, and inclusion/exclusion tests on both Set
	 * and Object operands.  Reports a semantic error for a key-based predicate.
	 * @return true, if this list-Membership test holds.
	 */
	@SuppressWarnings("unchecked")
	protected Boolean setMembership() {
		Set<Object> value0 = (Set<Object>) operand(0).evaluate();
		String opName = getName();
		if (opName.endsWith("Empty")) {
			if (opName.equals("isEmpty"))
				return value0.isEmpty();
			else  // "notEmpty"
				return ! value0.isEmpty();
		}
		else if (opName.endsWith("All")) {
			Set<Object> value1 = (Set<Object>) operand(1).evaluate();
			if (opName.equals("includesAll"))
				return value0.containsAll(value1);
			else { // "excludesAll"
				for (Object object : value1) {
					if (value0.contains(object))
						return false;
				}
				return true;
			}
		}
		else {
			Object value1 = operand(1).evaluate();
			if (opName.equals("includes"))
				return value0.contains(value1);
			else if (opName.equals("excludes"))
				return ! value0.contains(value1);
			else {  // "includesKey" || "excludesKey"
				semanticError("has an illegal operator name " + opName);
				return false;
			}
		}
	}

	/**
	 * Membership test for a Map operand.  Evaluates empty/non-empty tests
	 * on the first Map operand, inclusion/exclusion tests on both Map
	 * and entry operands and key inclusion/exclusion tests on both Map
	 * and key operands.
	 * @return true, if this map Membership test holds.
	 */
	@SuppressWarnings("unchecked")
	protected Boolean mapMembership() {
		Map<Object, Object> value0 = 
				(Map<Object, Object>) operand(0).evaluate();
		String opName = getName();
		if (opName.endsWith("Empty")) {
			if (opName.equals("isEmpty"))
				return value0.isEmpty();
			else  // "notEmpty"
				return ! value0.isEmpty();
		}
		else if (opName.endsWith("All")) {
			Map<Object, Object> value1 = 
					(Map<Object, Object>) operand(1).evaluate();
			if (opName.equals("includesAll"))
				return value0.entrySet().containsAll(value1.entrySet());
			else {  // "excludesAll"
				for (Object object : value1.entrySet()) {
					if (value0.entrySet().contains(object))
						return false;
				}
				return true;
			}
		}
		else if (opName.endsWith("Key")) {
			Object value1 = operand(1).evaluate();
			if (opName.equals("includesKey"))
				return value0.containsKey(value1);
			else  // "excludesKey"
				return ! value0.containsKey(value1);
		}
		else {
			Map.Entry<Object, Object> value1 = 
					(Map.Entry<Object, Object>) operand(1).evaluate();
			if (opName.equals("includes"))
				return value0.entrySet().contains(value1);
			else  // "excludes"
				return ! value0.entrySet().contains(value1);
		}
	}
	
}
