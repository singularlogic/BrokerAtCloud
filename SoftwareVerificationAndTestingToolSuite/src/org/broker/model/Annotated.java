package org.broker.model;

/**
 * Annotated is the ancestor of all annotated elements in the metamodel.
 * An Annotated element may have various kinds of Analysis or Warning 
 * attached to it.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Annotated extends Named {
	
	/**
	 * The Notice attached to this Annotated object, typically
	 * a Warning or Analysis node.
	 */
	protected Notice notice;
	
	/**
	 * Creates a default Annotated element.
	 */
	public Annotated() {
	}

	/**
	 * Creates a named Annotated element.
	 * @param name the name of this Annotated element.
	 */
	public Annotated(String name) {
		super(name);
	}
	
	/**
	 * Attaches the Notice to this Annotated element.
	 * @param notice the Notice to attach.
	 * @return this Annotated element.
	 */
	public Annotated addNotice(Notice notice) {
		this.notice = notice;
		return this;
	}
	
	/**
	 * Returns the Notice attached to this Annotated element.
	 * @return the Notice, or null.
	 */
	public Notice getNotice() {
		return notice;
	}

}
