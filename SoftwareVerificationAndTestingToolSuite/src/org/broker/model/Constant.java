package org.broker.model;

import java.util.Collections;



/**
 * Constant is a kind of parameter standing for some fixed constant value.
 * Constant represents a symbolic constant that is bound to a literal value.
 * The Constant is typically named according to the bound value, such that 
 * "zero" represents the value 0, "true" represents the value true and
 * "emptyList" represents an empty list.  The bound value is supplied as an
 * XML content string and is converted to a strongly-typed bound value when 
 * the Constant is first evaluated.  Empty content is always interpreted as
 * a default bound value, according to the type of the Constant.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Constant extends Parameter {

	/**
	 * Creates a default Constant parameter.
	 */
	public Constant() {
	}

	/**
	 * Creates a Constant parameter with the given name and type.
	 * @param name the name of this Constant.
	 * @param type the type of this Constant.
	 */
	public Constant(String name, String type) {
		super(name, type);
	}
	
	/**
	 * Evaluates this Constant expression.  Returns the bound value of this
	 * Constant, based on the string content.  On first invocation, creates
	 * the bound value and caches it.  If no content is specified, performs
	 * default initialisation to false, zero, null byte, empty String, empty
	 * List, Set or Map, according to type.
	 * @return the bound value of this Constant.
	 */
	@Override
	public Object evaluate() {
		if (value == null)
			value = createValue(content, type);
		return value;
	}
	
	/**
	 * Degenerate method to unbind this Constant.  This is a null operation,
	 * since Constants are never unbound, but always keep their bound value.
	 * Likewise, the stored content String for a Constant is never deleted.
	 */
	@Override
	public void unbind() {
	}
	
	/**
	 * Reports whether this Constant is the bottom element of its type.
	 * This is true for the null Character, the empty String, Set, List
	 * or Map; and false for all other values.
	 * This judgement is used when refining the partitions of a comparison,
	 * to decide whether lessThan(x, y) is a meaningful partition.
	 * @return true, if this Constant is the bottom element of its type.
	 */
	@Override
	public boolean isBottom() {
		Object constValue = evaluate();
		return (constValue == null || constValue.equals('\0') || constValue.equals("") || 
				constValue.equals(Collections.emptySet()) || 
				constValue.equals(Collections.emptyList()) ||
				constValue.equals(Collections.emptyMap()));
	}
	
	/**
	 * Reports whether this Constant is assignable.  Returns false, since it
	 * is illegal to re-assign a new value to a Constant.
	 * @return false, always.
	 */
	@Override
	public boolean isAssignable() {
		return false;
	}
	
	/**
	 * Reports an attempt to assign to this Constant as an error.
	 */
	@Override
	public void assign(Object value) {
		semanticError("cannot be re-assigned a new value.");
	}
	
}
