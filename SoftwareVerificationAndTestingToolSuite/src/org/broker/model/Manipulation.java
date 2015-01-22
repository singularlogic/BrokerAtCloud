package org.broker.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Manipulation represents an expression manipulating a collection of items.
 * The first operand is expected to be some kind of collection, whether a
 * Set, List or Map.  The remaining operands may be an element, an index,
 * a key, or another collection.  All Manipulations are functional, in that
 * they do not modify an operand directly, but return a modified copy.  If
 * a side-effect is intended, the result must be reassigned to the same
 * Parameter that stored the original collection, before the operation.
 * There are nine Manipulation operations, whose names include:  size, 
 * insert, remove, insertAll, removeAll, searchAt, replaceAt, insertAt
 * and removeAt.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Manipulation extends Function {

	/**
	 * Validates the name of a Manipulation expression and sets the expected
	 * number of operands.  Manipulations expect one to three operands, the
	 * first of which is always a collection of items (whether List, Set or
	 * Map).  The operation: size expects this single collection operand.  
	 * The operations: insert, remove expect the second operand to be an
	 * element of the collection.  The operations:  insertAll, removeAll 
	 * expect the second operand to be another collection.  The operations:
	 * searchAt, removeAt expect the second operand to be a key or index.  The
	 * operations: insertAt, replaceAt expect a similar second operand and an
	 * element as the third operand. 
	 */
	@Override
	protected void validate(String name) {
		String legalNames = 
				" size insert remove insertAll removeAll " +
				"searchAt replaceAt insertAt removeAt ";
		if (! legalNames.contains(" " + name + " "))
			semanticError("has an illegal operator name '" + name + "'.");
		if (name.equals("size"))
			maxOperands = 1;
		else if (name.equals("replaceAt") || name.equals("insertAt"))
			maxOperands = 3;
		else
			maxOperands = 2;
	}

	/**
	 * Creates a default Manipulation expression.
	 */
	public Manipulation() {
	}

	/**
	 * Creates a named Manipulation expression.
	 * @param name the arithmetic operator name.
	 */
	public Manipulation(String name) {
		super(name);
	}

	/**
	 * Creates a named and typed Manipulation expression.
	 * @param name the name of the arithmetic operator.
	 * @param type the result type of the expression.
	 */
	public Manipulation(String name, String type) {
		super(name, type);
	}
	
	/**
	 * Returns the result type of this Manipulation.  Returns the explicitly
	 * labelled type, if given, otherwise determines the type by heuristic.
	 * Examines the name of the Manipulation operator.  If this is "size", the
	 * type is Integer; if this is "searchAt", the type is the element-type of
	 * the collection; otherwise the type is the collection-type itself.
	 * Caches the retrieved type as the type of this Manipulation.
	 */
	@Override
	public String getType() {
		if (type == null) {
			type = operand(0).getType();  // default guess
			if (name.equals("size"))
				type = "Integer";
			else if (name.equals("searchAt")) {
				int start = type.indexOf('[');
				int end = type.indexOf(']');
				type = type.substring(start+1, end);
				if (type.contains(", ")) {
					start = type.indexOf(' ');
					type = type.substring(start+1);
				}
			}
		}
		return type;
	}

	/**
	 * Executes this Manipulation on its operands.  Converts the first 
	 * operand into a collection of the declared type.  Converts any second
	 * operand into an element, collection or key index, according to the
	 * operator name.  Converts any third operand into an element.  Branches
	 * to distinct subroutines according to the type of collection, whether
	 * List, Set or Map.  Executing this Manipulation returns an Integer
	 * (for size), an element (for searchAt) or a new collection, a modified
	 * copy of the original collection argument.
	 * @return the result, an integer, an element or a new collection.
	 */
	public Object evaluate() {
		if (getName().equals("size")) {
			Collection<?> coll = (Collection<?>) operand(0).evaluate();
			return (Integer) coll.size();
		}
		else {
			String colType = operand(0).getType();
			if (colType.startsWith("Set"))
				return manipulateSet();
			else if (colType.startsWith("List"))
				return manipulateList();
			else if (colType.startsWith("Map"))
				return manipulateMap();
			else {
				semanticError("operand '" + operand(0).getName() + 
						"' has a non-collection type '" + colType);
				return null;
			}
		}
	}
	
	/**
	 * Performs a set manipulation.  The first operand must be some kind of
	 * Set type.  Only the operators to insert and remove elements or sets
	 * are valid.
	 * @return the result of the manipulation.
	 */
	@SuppressWarnings("unchecked")
	protected Object manipulateSet() {
		Set<Object> value0 = (Set<Object>) operand(0).evaluate();
		Set<Object> result = new HashSet<Object>(value0);
		if (name.endsWith("All")) {
			Collection<Object> value1 = 
					(Collection<Object>) operand(1).evaluate();
			if (name.equals("insertAll"))
				result.addAll(value1);
			else // name.equals("removeAll")
				result.removeAll(value1);
		}
		else {
			Object value1 = operand(1).evaluate();
			if (name.equals("insert"))
				result.add(value1);
			else if (name.equals("remove"))
				result.remove(value1);
			else
				semanticError("operator '" + name + "' is illegal for a Set.");
		}
		return result;
	}

	/**
	 * Performs a list manipulation.  The first operand must be some kind of
	 * List type.  All the operators are valid, but expect the operands to be
	 * objects of suitable types.  In particular, any key index must be an
	 * Integer, and indexing of a List runs from 1..n.
	 * @return the result of the manipulation.
	 */
	@SuppressWarnings("unchecked")
	protected Object manipulateList() {
		List<Object> value0 = (List<Object>) operand(0).evaluate();
		List<Object> result = new ArrayList<Object>(value0);
		if (name.endsWith("All")) {
			Collection<Object> value1 = 
					(Collection<Object>) operand(1).evaluate();
			if (name.equals("insertAll"))
				result.addAll(value1);
			else // name.equals("removeAll")
				result.removeAll(value1);
		}
		else if (name.endsWith("At")) {
			Integer value1 = (Integer) operand(1).evaluate();
			if (name.equals("searchAt"))
				return value0.get(value1.intValue() -1);  // skip out
			else if (name.equals("removeAt")) 
				result.remove(value1.intValue() -1);  // BUGFIX: -1 
			else if (name.equals("insertAt")) 			
				result.add(value1.intValue() -1, operand(2).evaluate());		
			else // name.equals("replaceAt")
				result.set(value1.intValue() -1, operand(2).evaluate());
		}
		else {
			Object value1 = operand(1).evaluate();
			if (name.equals("insert"))
				result.add(value1);
			else // name.equals("remove")
				result.remove(value1);
		}
		return result;
	}

	/**
	 * Performs a map manipulation.  The first operand must be some kind of
	 * Map type.  All the operators are valid, but expect the operands to be
	 * objects of suitable types.  In particular, the insert and remove
	 * operators expect a pair-type (Map.Entry in java).
	 * @return the result of the manipulation.
	 */
	@SuppressWarnings("unchecked")
	protected Object manipulateMap() {
		Map<Object, Object> value0 = 
				(Map<Object, Object>) operand(0).evaluate();
		Map<Object, Object> result = new HashMap<Object, Object>(value0);
		if (name.endsWith("All")) {
			Map<Object, Object> value1 = 
					(Map<Object, Object>) operand(0).evaluate();
			if (name.equals("insertAll"))
				result.entrySet().addAll(value1.entrySet());
			else // name.equals("removeAll")
				result.entrySet().removeAll(value1.entrySet());
		}
		else if (name.endsWith("At")) {
			Object value1 = operand(1).evaluate();
			if (name.equals("searchAt"))
				return value0.get(value1);  // skip out
			else if (name.equals("removeAt")) 
				result.remove(value1);
			else // name.equals("replaceAt") || name.equals("insertAt")
				result.put(value1, operand(2).evaluate());
		}
		else {
			Object value1 = operand(1).evaluate();
			if (name.equals("insert"))
				result.entrySet().add((Map.Entry<Object, Object>) value1);
			else // name.equals("remove")
				result.entrySet().remove((Map.Entry<Object, Object>) value1);
		}
		return result;
	}

}
