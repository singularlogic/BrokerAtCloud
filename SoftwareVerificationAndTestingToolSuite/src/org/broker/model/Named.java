package org.broker.model;

/**
 * Named is the ancestor of all named elements in the metamodel.  By default,
 * a named element is assumed to be uniquely identifiable by its name.  The
 * contract for equals expects two elements to have the same type and name to
 * be judged equal.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public abstract class Named extends Element {

	/**
	 * The name attribute of a Named element.
	 */
	protected String name;
	
	/**
	 * Creates a default Named element with no name.
	 */
	protected Named() {
	}
	
	/**
	 * Creates a Named element with the given name.  If a second name is 
	 * later assigned, this must be the same name.
	 * @param name the name.
	 */
	protected Named(String name) {
		this.name = name;
	}
	
	/**
	 * Reports whether this Named element is equal to another object.  True,
	 * if the other object is a Named element, whose name is equal to the 
	 * name of this object.
	 * @param other the other object.
	 * @return true, if both Named elements have the same name.
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		else if (other instanceof Named) {
			Named named = (Named) other;
			return safeEquals(name, named.name);
		}
		else
			return false;
	}
	
	/**
	 * Returns a quasi-unique hash code for this named element.  Returns the 
	 * hash code associated with the Named element's name.  This method must 
	 * be overridden in any subclass for which equality is not judged by name
	 * equality.
	 * @return the hash code for this Named element.
	 */
	@Override
	public int hashCode() {
		return safeHashCode(name);
	}
	
	/**
	 * Sets the name of a Named element.  If this Named element was named at
	 * construction, it can only be given the same name.  Named elements may
	 * not be later renamed with a different name.
	 * @param name the name to set.
	 * @return this Named element.
	 */
	public Named setName(String name) {
		if (this.name == null)
			this.name = name;
		else if (! name.equals(this.name))
			semanticError("already named '" + this.name + 
					"' is being renamed '" + name + "'.");
		return this;
	}
	
	/**
	 * Returns the name of a Named element.  Names are optional by default, but
	 * some subtypes insist on the presence of a name, for indexing purposes.
	 * @return the name, or null.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Converts this Named element to a printable representation.  By default,
	 * returns the name of this Named element.
	 * @return the String representation of this Named element.
	 */
	@Override
	public String toString() {
		return name;
	}

}
