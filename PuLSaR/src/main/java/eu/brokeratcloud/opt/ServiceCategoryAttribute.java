/*
 * #%L
 * Preference-based cLoud Service Recommender (PuLSaR) - Broker@Cloud optimisation engine
 * %%
 * Copyright (C) 2014 - 2016 Information Management Unit, Institute of Communication and Computer Systems, National Technical University of Athens
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.brokeratcloud.opt;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import eu.brokeratcloud.common.BrokerObject;
import eu.brokeratcloud.common.ServiceCategory;
import eu.brokeratcloud.opt.type.TFN;
import eu.brokeratcloud.persistence.annotations.*;
import eu.brokeratcloud.rest.opt.ServiceCategoryAttributeManagementService;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import org.codehaus.jackson.annotate.JsonIgnore;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceCategoryAttribute extends BrokerObject {
	@XmlAttribute
	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-pref#hasFunctionalServiceCategory")
	protected String serviceCategory;
	@XmlAttribute
	@RdfPredicate(uri="http://www.linked-usdl.org/ns/usdl-pref#hasOptimisationAttribute")
	protected String attribute;
	@XmlAttribute
	@RdfPredicate //(uri="http://www.linked-usdl.org/ns/usdl-pref#hasDataType")
	protected String type;
	@XmlAttribute
	@RdfPredicate //(uri="http://www.linked-usdl.org/ns/usdl-pref#isMandatory")
	protected boolean mandatory;
	@XmlAttribute
	@RdfPredicate //(uri="http://www.linked-usdl.org/ns/usdl-pref#hasUnitOfMeasurement")
	protected String unit;	// unit of measurement (if any)
	@XmlAttribute
	@RdfPredicate //(uri="http://www.linked-usdl.org/ns/usdl-pref#hasUnitOfMeasurement")
	protected String measuredBy;	// unit of measurement (if any)
	
	@XmlAttribute
	//@RdfPredicate
	protected String bppName;
	
	//Allowed Value Range constraints
	@XmlAttribute
	@RdfPredicate //(uri="http://www.linked-usdl.org/ns/usdl-pref#hasMax")
	protected double max;
	@XmlAttribute
	@RdfPredicate //(uri="http://www.linked-usdl.org/ns/usdl-pref#hasMin")
	protected double min;
	@XmlAttribute
	@RdfPredicate(delete="cascade" /*, uri="http://www.linked-usdl.org/ns/usdl-pref#hasFuzzyMax"*/ )
	protected TFN fmin;
	@XmlAttribute
	@RdfPredicate(delete="cascade" /*, uri="http://www.linked-usdl.org/ns/usdl-pref#hasFuzzyMin"*/ )
	protected TFN fmax;
	@XmlAttribute
	@RdfPredicate(delete="cascade" /*, uri="http://www.linked-usdl.org/ns/usdl-pref#hasMembers"*/ )
	protected String[] members;
	@XmlAttribute
	@RdfPredicate(delete="cascade" /*, uri="http://www.linked-usdl.org/ns/usdl-pref#hasLinguisticTerms"*/ )
	protected String[] terms;
	//
	protected boolean higherIsBetter;
	
	// Types and static type checking methods
	public static enum TYPE { NUMERIC_INC, NUMERIC_DEC, NUMERIC_RANGE, FUZZY_INC, FUZZY_DEC, FUZZY_RANGE, BOOLEAN, UNORDERED_SET, LINGUISTIC };
	
	protected static String[] _allTypes = {"NUMERIC_INC", "NUMERIC_DEC", "NUMERIC_RANGE", "FUZZY_INC", "FUZZY_DEC", "FUZZY_RANGE", "BOOLEAN", "UNORDERED_SET", "LINGUISTIC"};
	protected static String[] _numericTypes = {"NUMERIC_INC", "NUMERIC_DEC", "NUMERIC_RANGE"};
	protected static String[] _fuzzyTypes = {"FUZZY_INC", "FUZZY_DEC", "FUZZY_RANGE"};
	protected static String[] _booleanTypes = {"BOOLEAN"};
	protected static String[] _setTypes = {"UNORDERED_SET", "LINGUISTIC"};
	protected static String[] _unorderedSetTypes = {"UNORDERED_SET"};
	protected static String[] _linguisticTypes = {"LINGUISTIC"};
	protected static boolean _checkType(String typ, String[] subset) {
		if (typ==null) return false; 
		if ((typ=typ.trim()).isEmpty()) return false;
		for (int i=0; i<subset.length; i++) {
			if (typ.equalsIgnoreCase(subset[i])) return true;
		}
		return false;
	}
	public static boolean isNumericType(String typ) { return _checkType(typ, _numericTypes); }
	public static boolean isFuzzyType(String typ) { return _checkType(typ, _fuzzyTypes); }
	public static boolean isBooleanType(String typ) { return _checkType(typ, _booleanTypes); }
	public static boolean isSetType(String typ) { return _checkType(typ, _setTypes); }
	public static boolean isUnorderedSetType(String typ) { return _checkType(typ, _unorderedSetTypes); }
	public static boolean isLinguisticType(String typ) { return _checkType(typ, _linguisticTypes); }
	public static boolean isUnknownType(String typ) { return !_checkType(typ, _allTypes); }
	
	public static boolean isNumericInc(String type) { return type.equals("NUMERIC_INC"); }
	public static boolean isNumericDec(String type) { return type.equals("NUMERIC_DEC"); }
	public static boolean isNumericRange(String type) { return type.equals("NUMERIC_RANGE"); }
	public static boolean isFuzzyInc(String type) { return type.equals("FUZZY_INC"); }
	public static boolean isFuzzyDec(String type) { return type.equals("FUZZY_DEC"); }
	public static boolean isFuzzyRange(String type) { return type.equals("FUZZY_RANGE"); }
	public static boolean isBoolean(String type) { return type.equals("BOOLEAN"); }
	public static boolean isUnorderedSet(String type) { return type.equals("UNORDERED_SET"); }
	public static boolean isLinguistic(String type) { return type.equals("LINGUISTIC"); }
	public static boolean isUnknown(String type) { return isUnknownType(type); }
	
	@JsonIgnore
	public boolean isNumericInc() { return type.equals("NUMERIC_INC"); }
	@JsonIgnore
	public boolean isNumericDec() { return type.equals("NUMERIC_DEC"); }
	@JsonIgnore
	public boolean isNumericRange() { return type.equals("NUMERIC_RANGE"); }
	@JsonIgnore
	public boolean isFuzzyInc() { return type.equals("FUZZY_INC"); }
	@JsonIgnore
	public boolean isFuzzyDec() { return type.equals("FUZZY_DEC"); }
	@JsonIgnore
	public boolean isFuzzyRange() { return type.equals("FUZZY_RANGE"); }
	@JsonIgnore
	public boolean isBoolean() { return type.equals("BOOLEAN"); }
	@JsonIgnore
	public boolean isUnorderedSet() { return type.equals("UNORDERED_SET"); }
	@JsonIgnore
	public boolean isLinguistic() { return type.equals("LINGUISTIC"); }
	@JsonIgnore
	public boolean isUnknown() { return isUnknownType(type); }
	
	public String getServiceCategory() { return serviceCategory; }
	public void setServiceCategory(String sc) { serviceCategory = sc; }
	public String getAttribute() { return attribute; }
	public void setAttribute(String a) { attribute = a; }
	public String getType() { return type; }
	public void setType(String t) { if (isUnknownType(t)) throw new IllegalArgumentException("Invalid Service Category Attribute type: "+t); type = t; }
	public boolean getMandatory() { return mandatory; }
	public void setMandatory(boolean m) { mandatory = m; }
	public String getUnit() { return unit; }
	public void setUnit(String u) { unit = u; }
	public String getMeasuredBy() { return measuredBy; }
	public void setMeasuredBy(String s) { measuredBy = s; }
	
	public String getBppName() { return bppName; }
	public void setBppName(String s) { bppName = s; }
	
	public double getMin() { return min; }
	public double getMax() { return max; }
	public TFN getFmin() { return fmin; }
	public TFN getFmax() { return fmax; }
	public String[] getMembers() { return members; }
	public String[] getTerms() { return terms; }
	
	public void setMin(double m) { min = m; }
	public void setMax(double m) { max = m; }
	public void setFmin(TFN t) { fmin = t; }
	public void setFmax(TFN t) { fmax = t; }
	public void setMembers(String[] m) { members = m; }
	public void setTerms(String[] t) { terms = t; }
	
	@JsonIgnore
	public boolean isHigherIsBetter() { return higherIsBetter; }
	@JsonIgnore
	public void setHigherIsBetter(boolean b) { higherIsBetter = b; }
	
	public String toString() {
		return 	"ServiceCategoryAttribute: {\n"+super.toString()+
				"\tservice-category = "+serviceCategory+"\n\tattribute = "+attribute+"\n\tbpp-name = "+bppName+
				"\n\ttype = "+type+"\n\tmandatory = "+mandatory+"\n\tunit = "+unit+"\n\tmeasured-by = "+measuredBy+
				"\n\tmin="+min+", max="+max+", f-min="+fmin+", f-max="+fmax+", members="+Arrays.deepToString(members)+", terms="+Arrays.deepToString(terms)+
				", higher-is-better="+higherIsBetter+
				"\n}\n";
	}
}
