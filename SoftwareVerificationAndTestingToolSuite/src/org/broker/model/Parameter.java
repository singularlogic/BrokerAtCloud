package org.broker.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parameter is a kind of expression denoting a named and typed parameter.  
 * Parameter is the ancestor of Constant, Variable, Input and Output, which
 * are the actual kinds of Parameter used in model specifications.  A 
 * Parameter may be supplied with an XML content string, which is converted 
 * to a strongly-typed object when the Parameter is first evaluated.  With 
 * the exception of Constant, all other kinds of Parameter may either be 
 * bound or unbound.  When a Parameter is unbound, its value is null and
 * evaluating will return null.  When a Parameter is bound, evaluating will
 * always return a non-null strongly-typed object.  This will either be the
 * cached value, or a value created from the XML content string, possibly
 * using default initialisation rules if no content exists.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public abstract class Parameter extends Expression {
	
	/**
	 * Volatile flag attribute denoting whether evaluation should perform
	 * default initialisation, or not.  Always true upon first creation, but
	 * may be made false by unbinding, and true by assignment.
	 */
	private boolean isBound = true;
	
	/**
	 * The strongly-typed bound value of this Parameter, or null.
	 */
	protected Object value;
	
	/**
	 * The textual printed representation of the value, or null.
	 */
	protected String content;
	
	/**
	 * Creates a default parameter.
	 */
	public Parameter() {
	}
		
	/**
	 * Creates a parameter of the given name and type.
	 * @param name the name of this parameter.
	 * @param type the type of this parameter.
	 */
	public Parameter(String name, String type) {
		super(name, type);
	}
	
	/**
	 * Reports whether this Parameter is re-assignable.  By default, the
	 * result is true, but in Constant, the result is false.  Technically,
	 * Input and Output may only be initialised, rather than re-assigned.
	 * @return true, by default.
	 */
	public boolean isAssignable() {
		return true;
	}
	
	/**
	 * Evaluates this Parameter expression.  Returns the bound value of this
	 * Parameter, if it is currently bound.  After creation, a Parameter is
	 * considered bound, but it may become unbound during model reasoning, 
	 * or rebound by assignment to a new value.  
	 * @return the bound value of this Parameter.
	 */
	@Override
	public Object evaluate() {
		if (value == null && isBound)
			value = createValue(content, type);
		return value;
	}
	
	/**
	 * Unbinds this Parameter.  Sets the bound flag to false, and deletes 
	 * any cached copy of this Parameter's strongly-typed bound value.  This
	 * is invoked during protocol checking, when searching for values that
	 * may satisfy a compound condition.
	 */
	public void unbind() {
		isBound = false;
		value = null;
	}
	
	/**
	 * Rebinds this Parameter to its default value.  Sets the bound flag to
	 * true, so that any subsequent evaluation will create and cache a 
	 * default value, according to type.  This is invoked during protocol
	 * checking, when searching for values that may satisfy a compound
	 * condition.
	 */
	public void rebind() {
		isBound = true;
	}
	
	/**
	 * Degenerate method to resolve Parameter references in this Parameter.
	 * This is a null operation, since a Parameter is an atomic expression 
	 * and contains no further embedded Parameter references that need to
	 * be resolved.
	 * @param scope a table of global and local Parameters currently in 
	 * scope.
	 */
	public Parameter resolve(Scope scope) {
		return this;
	}
	
	/**
	 * Assigns a new bound value to this Parameter.  The value is the result
	 * of evaluating some other Expression, which is now being bound to this
	 * Parameter.  The value is cached and the bound flag is set to true.
	 * This method is overridden in Constant, which may not reassign values.
	 * @param value the value to assign.
	 */
	@Override
	public void assign(Object value) {
		isBound = true;
		this.value = value;
	}

	/**
	 * The printable representation of a parameter is its name.
	 * @return the name of this parameter.
	 */
	public String toString() {
		return getName();
	}

	/**
	 * Sets the content of this Parameter.
	 * @param content the content, as a String.
	 * @return this Parameter.
	 */
	public Parameter setContent(String content) {
		this.content = content;
		return this;
	}

	/**
	 * Returns the content of this Parameter.
	 * @return the content of this Parameter.
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * Takes a snapshot of this Parameter, fixing its current bound value.
	 * Clones this Parameter, whether it is a Constant, Variable, Input or
	 * output, creating a true copy.  Converts its current bound value back
	 * to a String value, so that the snapshot is ready to be marshalled as
	 * XML data.  This is used during test generation, to create snapshots
	 * of Parameters that are shared by other test cases, before they are
	 * rebound in the next test case.
	 * @return a clone of this Parameter, with a snapshot of its value.
	 */
	public Parameter snapshot() {
		Parameter result = null;
		try {
			result = getClass().newInstance();
			result.setName(getName());
			result.setType(getType());
			if (value != null)
				result.setContent(value.toString());
		} 
		catch (InstantiationException e) {
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Converts this Parameter's weakly-typed content into a strongly-typed
	 * value.  Conversion is performed according to the declared type, which
	 * may be a basic type:  Boolean, Byte, Character, String, Integer,
	 * Short, Long, Float, Double; or a collection type:  List[T], Set[T], or
	 * Map[K, V].  If no initial content string was supplied, creates a
	 * default value according to the type:  false, null byte, null char,
	 * empty String, integral zero, decimal zero, or an empty List, Set or 
	 * Map.  If the wildcard "*" is supplied as the content of a List, Set
	 * or Map, creates a non-empty collection with one default element.
	 * @param value the content of this Parameter, as a String.
	 * @param valueType type type of this Parameter, as a String.
	 * @return a Java object representing a value of this type.
	 */
	protected Object createValue(String value, String valueType) {
		if (valueType.equals("String"))
			return value == null ? "" : value;
		else if (valueType.equals("Integer"))
			return new Integer(value == null ? "0" : value);
		else if (valueType.equals("Double"))
			return new Double(value == null ? "0.0" : value);
		else if (valueType.equals("Short"))
			return new Short(value == null ? "0" : value);
		else if (valueType.equals("Long"))
			return new Long(value == null ? "0" : value);
		else if (valueType.equals("Float"))
			return new Float(value == null ? "0.0" : value);
		else if (valueType.equals("Boolean"))
			return new Boolean(value == null ? "false" : value);
		else if (valueType.equals("Character"))
			return new Character(value == null ? '0' : value.charAt(0));
		else if (valueType.startsWith("List"))
			return createList(value, valueType);
		else if (valueType.startsWith("Set"))
			return createSet(value, valueType);
		else if (valueType.startsWith("Map"))
			return createMap(value, valueType);
		
		else {
			semanticError("has an unknown type: " + valueType);
			return null;
		}
	}

	/**
	 * Converts the content of this Parameter into a List value.  The List may 
	 * be empty, or have one or more elements.  The elements are surrounded by
	 * square brackets and separated by a comma and single space (Java format).
	 * The list type has the form "List[T]" for some element-type T.
	 * @param listValue the list elements, as a space-separated String.
	 * @param listType the parametric type of the list, as a String.
	 * @return a List of Objects representing the content of this Parameter.
	 */
	protected List<Object> createList(String listValue, String listType) {
		if (listType.contains(" ") || listType.contains(","))
			semanticError("has an illegal List type: " + listType);
		List<Object> result = new ArrayList<Object>();
		if (listValue == null)
			return result;
		else if (listValue.equals("*")) {
			result.add(createValue(null, valueType(listType)));
			return result;
		}
		else {
			if (! (listValue.startsWith("[") && listValue.endsWith("]")))
				semanticError("has an illegal List value: " + listValue);
			String items = listValue.substring(1, listValue.length() - 1);
			if (items.isEmpty())
				return result;
			for (String item : items.split(", ")) {
				result.add(createValue(item, valueType(listType)));
			}
			return result;
		}
	}

	/**
	 * Converts the content of this Parameter into a Set value.  The Set may 
	 * be empty, or have one or more elements.  The elements are surrounded by
	 * square brackets and separated by a comma and single space (Java format).
	 * The set type has the form "Set[T]" for some element-type T.
	 * @param setValue the set elements, as a space-separated String.
	 * @param listType the parametric type of the set, as a String.
	 * @return a Set of Objects representing the content of this Parameter.
	 */
	protected Set<Object> createSet(String setValue, String setType) {
		if (setType.contains(" ") || setType.contains(","))
			semanticError("has an illegal Set type: " + setType);
		Set<Object> result = new HashSet<Object>();
		if (setValue == null)
			return result;
		else if (setValue.equals("*")) {
			result.add(createValue(null, valueType(setType)));
			return result;
		}
		else {
			if (! (setValue.startsWith("[") && setValue.endsWith("]")))
				semanticError("has an illegal Set value: " + setValue);
			String items = setValue.substring(1, setValue.length() - 1);
			if (items.isEmpty())
				return result;
			for (String item : items.split(", ")) {
				result.add(createValue(item, valueType(setType)));
			}
			return result;
		}
	}

	/**
	 * Converts the content of this Parameter into a Map value.  The Map may 
	 * be empty, or have one or more maplets.  The elements are surrounded by
	 * curly braces and separated by a comma and single space, and each
	 * element is a maplet of the form "key=value" (Java format).
	 * The map type has the form "Map[K, V]" for some key-type K and 
	 * value-type V.
	 * @param mapValue the list maplets, as a space-separated String.
	 * @param listType the parametric type of the list, as a String.
	 * @return a Map of Object keys to values, representing the content.
	 */
	protected Map<Object, Object> createMap(String mapValue, String mapType) {
		if (! mapType.contains(", "))
			semanticError("has an illegal Map type: " + mapType);
		Map<Object, Object> result = new HashMap<Object, Object>();
		if (mapValue == null)
			return result;
		else if (mapValue.equals("*")) {
			result.put(createValue(null, keyType(mapType)),
					createValue(null, valueType(mapType)));
			return result;
		}
		else {
			if (! (mapValue.startsWith("{") && mapValue.endsWith("}")))
				semanticError("has an illegal Map value: " + mapValue);
			String items = mapValue.substring(1, mapValue.length() - 1);
			if (items.isEmpty())
				return result;
			for (String item : items.split(", ")) {
				if (! item.contains("="))
					semanticError("has an illegal maplet: " + item);
				String[] pair = item.split("=");
				if (pair.length != 2)
					semanticError("has an illegal maplet: " + item);
				result.put(createValue(pair[0], keyType(mapType)),
						createValue(pair[1], valueType(mapType)));
			}
			return result;
		}
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
	
}
