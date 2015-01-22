package org.broker.model;

/**
 * Element is the root Element of the metamodel.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Element {
	
	/**
	 * Safe equality comparison that performs the null-checks.  Objects one
	 * and two are equal if they are both null, or if they are both non-null
	 * and one.equals(two) is true.  Otherwise they are not equal.
	 * @param one the first object.
	 * @param two the second object.
	 * @return true if the objects are equal.
	 */
	protected boolean safeEquals(Object one, Object two) {
		return one == null ? two == null : one.equals(two);
	}
	
	/**
	 * Safe hash code function that performs the null-check.  The hash code
	 * for a non-null object is object.hashCode(), otherwise it is zero.
	 * @param object the object to hash.
	 * @return the hash code.
	 */
	protected int safeHashCode(Object object) {
		return object == null ? 0 : object.hashCode();
	}
	
	/**
	 * Reports a semantic error during model construction.
	 * @param text the error text.
	 * @throws SemanticError always.
	 */
	protected void semanticError(String message) throws SemanticError {
		throw new SemanticError(getClass().getSimpleName() + 
				" " + message, this);
	}

}
