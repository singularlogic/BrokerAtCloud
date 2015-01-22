package org.broker.model;


/**
 * Expression represents a computable expression, with a name and a type.
 * Expression is the abstract root of the Expression library.  All subclasses
 * are different kinds of terms that can be evaluated.  Expressions break 
 * down into Parameters, which store their value, and Functions, which
 * compute their value.  The name of an Expression is either the Parameter
 * name, or the name of the root Function dominating the expression.  The
 * type of the Expression is usually also declared, but may be inferred for
 * some classes of Expression.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public abstract class Expression extends Named {

	/**
	 * The type of this Expression.
	 */
	protected String type;
	
	/**
	 * Creates a default Expression.
	 */
	protected Expression() {
	}
	
	/**
	 * Creates an Expression with a name.  If this Expression is some kind
	 * of Parameter, the name is the unique Parameter name.  Otherwise, it
	 * is the name of some kind of Function at the root of this Expression.
	 * Each sub-kind of Function declares a limited set of legal names.
	 * @param name the name of this Expression.
	 */
	protected Expression(String name) {
		super(name);
	}
	
	/**
	 * Creates an Expression with a name and a type.  These are either the
	 * declared name and type of some kind of Parameter; or the restricted
	 * name and result type of some kind of Function.
	 * @param name the name of this expression.
	 * @param type the type of this expression.
	 */
	protected Expression(String name, String type) {
		super(name);
		this.type = type;
	}
	
	/**
	 * Reports whether this Expression is equal to another object.  True if
	 * the other object is an Expression with the same name and type as this;
	 * otherwise false.  
	 * @param other the other object.
	 * @return true if both Expressions have the same name and type.
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		else if (other instanceof Expression) {
			Expression expression = (Expression) other;
			return super.equals(other) && 
					safeEquals(type, expression.type);
		}
		else
			return false;
	}

	/**
	 * Returns a quasi-unique hash code for this expression.  The hash code
	 * is based on the meta-type, name and type of the expression.
	 * @return the hash code for this expression.
	 */
	@Override
	public int hashCode() {
		return super.hashCode() * 31 + safeHashCode(type);
	}
	
	/**
	 * Returns the name of an Expression.  It is an error if the name has
	 * not already been set, when this access-method is invoked.
	 * @return the name.
	 */
	@Override
	public String getName() {
		if (name == null)
			semanticError("must be named for indexing purposes.");
		return name;
	}

	/**
	 * Sets the type of this expression.
	 * @param type the type to set.
	 * @return this Parameter.
	 */
	public Expression setType(String type) {
		this.type = type;
		return this;
	}
	
	/**
	 * Returns the type of this Expression.
	 * @return the type of this Expression.
	 */
	public String getType() {
		if (type == null)
			semanticError("has no type notice.");
		return type;
	}
	
	/**
	 * Reports whether this Expression is assignable.  By default, returns
	 * false, since only certain Parameters are assignable.
	 * @return false, by default.
	 */
	public boolean isAssignable() {
		return false;
	}
	
	/**
	 * Reports whether this Expression has a strictly ordered type.  All 
	 * number types are ordered arithmetically; Character and String are 
	 * lexicographically ordered.  List and Set types are only partially
	 * ordered by inclusion, so are not considered strictly ordered.
	 * This judgement is used when refining the partitions of a comparison,
	 * to decide, for example, whether equals(x, y) should only imply the 
	 * existence of a single complementary partition notEquals(x, y), or 
	 * the two ordered partitions lessThan(x, y) and moreThan(x, y).
	 * @return true, if this Expression has a strictly ordered type.
	 */
	public boolean isOrdered() {
		String orderedTypes = 
				"Integer Float Double Short Long Character String";
		if (orderedTypes.contains(getType()))
			return true;
		else
			return false;
	}
	
	/**
	 * Reports whether this Expression is the bottom element of its type.
	 * This is false for all Expressions, except for Constants whose value
	 * is the empty or bottom value for its declared type.
	 * This judgement is used when refining the partitions of a comparison,
	 * to decide whether lessThan(x, y) is a meaningful partition.
	 * @return true, if this Expression is the bottom element of its type.
	 */
	public boolean isBottom() {
		return false;
	}
	
	/**
	 * Assigns a value to this Expression.  If this Expression is assignable,
	 * binds it to the supplied value.  All Parameters, apart from Constants,
	 * are assignable.  Functions and Predicates are not assignable, so the
	 * request is ignored in these kinds of Expression.
	 * @param value the value to assign.
	 */
	public abstract void assign(Object value);
		
	/**
	 * Evaluates this Expression.  This Expression is either a Parameter or 
	 * some kind of Function with further Expression operands.  Converts a
	 * Parameter's stored value into a Java object, according to the declared
	 * type.  Evaluates a Function on each of its operands, after evaluating
	 * these in turn.  The result is a Java object of a cognate type that
	 * corresponds to the Expression's declared type.  Evaluation, unbinding
	 * and rebinding are used during model simulation.
	 */
	public abstract Object evaluate();
	
	/**
	 * Unbinds this Expression.  This Expression is either a Parameter or
	 * some kind of Function with further Expression operands.  Releases a
	 * Parameter's stored value, unless it is a Constant.  For a Function,
	 * propagates the unbinding request to all operands.  Evaluation,
	 * unbinding and rebinding are used during model simulation.
	 */
	public abstract void unbind();
	
	/**
	 * Rebinds this Expression.  This Expression is either a Parameter or
	 * some kind of Function with further Expression operands.  Restores a
	 * Parameter's value to its default value, unless it is a Constant.  For
	 * a Function, propagates the rebinding request to all operands.  For a
	 * Predicate, attempts to satisfy the Predicate's constraint when 
	 * rebinding.  Evaluation, unbinding and rebinding are used during model
	 * simulation.
	 */
	public abstract void rebind();
	
	/**
	 * Causes this Expression to resolve its embedded global and local
	 * Parameter references.  Resolution is used when building a model, to
	 * ensure that duplicate copies of named Parameters constructed by
	 * the parser are replaced by references to a single copy of each
	 * uniquely-named Parameter.  Expressions are resolved within a Scope,
	 * where this is a table of global and local Parameter declarations
	 * that are currently in scope.
	 * @param scope a table of global and local Parameters.
	 * @return this Expression.
	 */
	public abstract Expression resolve(Scope scope);

}
