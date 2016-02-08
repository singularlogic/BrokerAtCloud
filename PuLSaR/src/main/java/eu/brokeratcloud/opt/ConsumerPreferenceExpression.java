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

import eu.brokeratcloud.common.BrokerObject;
import eu.brokeratcloud.common.policy.*;
import eu.brokeratcloud.opt.policy.*;
import eu.brokeratcloud.opt.type.*;
import eu.brokeratcloud.persistence.annotations.*;
import eu.brokeratcloud.rest.opt.ServiceCategoryAttributeManagementService;

import java.util.Arrays;
import java.util.Map;
import java.util.Vector;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonIgnore;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@RdfSubject(
	uri="http://www.brokeratcloud.eu/v1/opt/CONSUMER-PREFERENCE-EXPRESSION",
	rdfType="http://www.linked-usdl.org/ns/usdl-pref#ConsumerPreferenceExpression"
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsumerPreferenceExpression extends BrokerObject {
	@XmlAttribute
	@RdfPredicate
	protected String expression;
	//@XmlAttribute  MUST NOT BE SET FOR THIS FIELD IN ORDER TO AVOID INFINITE RECURSION DURING "JSON SERIALIZATION"
	@RdfPredicate
	protected ConsumerPreference consumerPreference;
	
	public ConsumerPreferenceExpression() {
		setId("EXPRESSION-"+java.util.UUID.randomUUID().toString());
	}
	
	public String getExpression() { return expression; }
	public void setExpression(String e) { expression = e; _parseExpression(); }
	
	@JsonIgnore
	public ConsumerPreference getConsumerPreference() { return consumerPreference; }
	@JsonIgnore
	public void setConsumerPreference(ConsumerPreference pref) { consumerPreference = pref; }
	
	// Supported OPERATOR types
	public static enum OPERATOR { EQ, NEQ, LT, LE, GT, GE, IN, NOT_IN, COMMENT, UNKNOWN, ERROR };
	
	// transient variables - not stored in peristent store or serialized/unserialized
	protected OPERATOR operator;
	protected double limit, lowerBound, upperBound;
	protected TFN fLimit, fLowerBound, fUpperBound;
	protected boolean requiredBooleanValue;
	protected String[] elements;
	protected String termLimit;

	// transient variable setter/getter methods
	public OPERATOR getOperator() { return operator; }
	public double getLimit() { return limit; }
	public double getLowerBound() { return lowerBound; }
	public double getUpperBound() { return upperBound; }
	public TFN getFLimit() { return fLimit; }
	public TFN getFLowerBound() { return fLowerBound; }
	public TFN getFUpperBound() { return fUpperBound;}
	public boolean getRequiredBooleanValue() { return requiredBooleanValue; }
	public String[] getElements() { return elements; }
	public String getTermLimit() { return termLimit; }
	
	// object state flags
	protected boolean _isComment = false;
	protected boolean _isValid = false;
	protected boolean _initialized = false;
	
	// object state flags getter methods
	@JsonIgnore
	public boolean isInitialized() { return _initialized; }
	@JsonIgnore
	public boolean isValid() { return _isValid; }
	@JsonIgnore
	public boolean isComment() { return _isComment; }
	@JsonIgnore
	public boolean isEmpty() { return _isValid && (_isComment || expression==null); }
	
	// Parser settings
	public static final String[] validBooleanTrueValues  = { "true",  "yes", "on", "ok", "1" };
	public static final String[] validBooleanFalseValues = { "false", "no", "off", "nok", "0", "-1" };
	public static boolean missingValueResult = true;	// ACCEPT (used if attribute is optional)
	
	protected void _parseExpression() {
		logger.debug("_parseExpression: BEGIN: state=\n{}", this);
		_initialized = _isValid = _isComment = false;
		String errMsg = null;
		operator = OPERATOR.UNKNOWN;
		limit = lowerBound = upperBound = 0;
		fLimit = fLowerBound = fUpperBound = null;
		elements = null;
		requiredBooleanValue = false;
		termLimit = null;
		
		try{
				logger.trace("_parseExpression: preference={}", consumerPreference);
				if (consumerPreference==null) return;
				logger.trace("_parseExpression: preference variable={}", consumerPreference.getPrefVariable());
				if (consumerPreference.getPrefVariable()==null) return;
				
				String pvUri = consumerPreference.getPrefVariable();
				ServiceCategoryAttributeManagementService.PolicyObjects po = ServiceCategoryAttributeManagementService.getBrokerPolicyObjects(pvUri, false);
				logger.trace("_parseExpression: policy objects={}", po);
				if (po==null) return;
				PreferenceVariable pv = po.pv;
				logger.trace("_parseExpression: pref.var.-serv.attr.={}", pv!=null?pv.getRefToServiceAttribute():null);
				String tmp;
				if (pv==null || (tmp=pv.getRefToServiceAttribute().getId())==null || tmp.trim().isEmpty()) return;
				_initialized = true;
				
				logger.trace("_parseExpression: expression={}", expression);
				if (expression==null) { _isValid = true; return; }
				expression = expression.trim();
				if (expression.isEmpty()) { expression = null; _isValid = true; return; }
				
				// Deduce preference variable type
				BrokerPolicyProperty bpp = po.bpp;
				AllowedPropertyValue apv = po.apv;
				logger.trace("_parseExpression: Pref.Var.={}, Broker Policy Property={}", pv!=null?pv.getClass():null, bpp!=null?bpp.getClass():null);
				
				boolean isNumeric = bpp instanceof BrokerPolicyQuantitativeProperty;
				boolean isSet = bpp instanceof BrokerPolicyQualitativeProperty;
				boolean isFuzzy = bpp instanceof BrokerPolicyFuzzyProperty;
				boolean isBoolean = bpp instanceof BrokerPolicyDatatypeProperty && ((BrokerPolicyDatatypeProperty)bpp).isBoolean();
				boolean isRange = isNumeric ? ((AllowedQuantitativePropertyValue)bpp.getRange()).isRange() :
								  isFuzzy ? ((AllowedFuzzyPropertyValue)bpp.getRange()).isRange() : false;
				boolean hasOrder = isSet ? ((AllowedQualitativePropertyValue)bpp.getRange()).getHasOrder() : false;
				logger.trace("_parseExpression: Type flags: is-num={}, is-set={}, is-fuzzy={}, is-boolean={}, is-range={}, has-order={}", isNumeric, isSet, isFuzzy, isBoolean, isRange, hasOrder);
				
				logger.trace("_parseExpression: CALLING _parseOperator");
				int p = _parseOperator();
				logger.trace("_parseExpression: AFTER _parseOperator: p = {}", p);
				if (_isComment) {			// comment
					logger.trace("_parseExpression: IS COMMENT");
					_isValid = true;
				} else
				if (isNumeric && isRange==false) {
					logger.trace("_parseExpression: IS NUMERIC");
					if (operator!=OPERATOR.EQ && operator!=OPERATOR.NEQ && operator!=OPERATOR.LT && operator!=OPERATOR.LE && operator!=OPERATOR.GT && operator!=OPERATOR.GE) {
						_isValid = false;
						errMsg = "Invalid numeric type operator: "+operator;
					} else {
						double lim = Double.parseDouble( expression.substring(p).trim() );
						AllowedQuantitativePropertyValue aqpv = (AllowedQuantitativePropertyValue)bpp.getRange();
						_isValid = (aqpv.getMinValue()<=lim && lim<=aqpv.getMaxValue());		// constraint limit/threshold is in allowed values range
						if (_isValid) limit = lim;
						else errMsg = "Not allowed expression value: "+lim+"\nAllowed values: ["+aqpv.getMinValue()+".."+aqpv.getMaxValue()+"]";
					}
					logger.trace("_parseExpression: IS NUMERIC : valid={}", _isValid);
				} else
				if (isNumeric && isRange==true) {
					logger.trace("_parseExpression: IS RANGE");
					if (operator!=OPERATOR.EQ && operator!=OPERATOR.IN && operator!=OPERATOR.NOT_IN && operator!=OPERATOR.UNKNOWN) {
						_isValid = false;
						errMsg = "Invalid numeric range type operator: "+operator;
					} else {
						if (operator==OPERATOR.EQ || operator==OPERATOR.UNKNOWN) operator = OPERATOR.IN;
						NumericInterval interval = NumericInterval.valueOf( expression.substring(p).trim() );
						lowerBound = interval.getLowerBound();
						upperBound = interval.getUpperBound();
						AllowedQuantitativePropertyValue aqpv = (AllowedQuantitativePropertyValue)bpp.getRange();
						_isValid = (aqpv.getMinValue()<=lowerBound && upperBound<=aqpv.getMaxValue());		// constraint range in allowed values range
						if (!_isValid) errMsg = "Not allowed expression value: "+interval+"\nAllowed values: ["+aqpv.getMinValue()+".."+aqpv.getMaxValue()+"]";
					}
					logger.trace("_parseExpression: IS RANGE: valid={}", _isValid);
				} else
				if (isFuzzy && isRange==false) {
					logger.trace("_parseExpression: IS FUZZY");
					if (operator!=OPERATOR.EQ && operator!=OPERATOR.NEQ && operator!=OPERATOR.LT && operator!=OPERATOR.LE && operator!=OPERATOR.GT && operator!=OPERATOR.GE) {
						_isValid = false;
						errMsg = "Invalid fuzzy type operator: "+operator;
					} else {
						TFN lim = TFN.valueOf( expression.substring(p).trim() );
						AllowedFuzzyPropertyValue afpv = (AllowedFuzzyPropertyValue)bpp.getRange();
						_isValid = (afpv.getMinSupport()<=lim.getLowerBound() && lim.getUpperBound()<=afpv.getMaxSupport());		// constraint limit/threshold in allowed values range
						if (_isValid) fLimit = lim;
						else errMsg = "Not allowed expression value: "+lim+"\nAllowed values: ["+afpv.getFuzzyMinValue()+".."+afpv.getFuzzyMaxValue()+"]";
					}
					logger.trace("_parseExpression: IS FUZZY: valid={}", _isValid);
				} else
				if (isFuzzy && isRange==true) {
					logger.trace("_parseExpression: IS FUZZY RANGE");
					if (operator!=OPERATOR.EQ && operator!=OPERATOR.IN && operator!=OPERATOR.NOT_IN && operator!=OPERATOR.UNKNOWN) {
						_isValid = false;
						errMsg = "Invalid fuzzy range type operator: "+operator;
					} else {
						if (operator==OPERATOR.EQ || operator==OPERATOR.UNKNOWN) operator = OPERATOR.IN;
						TFuzzyInterval interval = TFuzzyInterval.valueOf( expression.substring(p).trim() );
						fLowerBound = interval.getLowerFuzzyBound();
						fUpperBound = interval.getUpperFuzzyBound();
						AllowedFuzzyPropertyValue afpv = (AllowedFuzzyPropertyValue)bpp.getRange();
						_isValid = (afpv.getMinSupport()<=fLowerBound.getLowerBound() && fUpperBound.getUpperBound()<=afpv.getMaxSupport());		// constraint range in allowed values range
						if (!_isValid) errMsg = "Not allowed expression value: "+interval+"\nAllowed values: ["+afpv.getFuzzyMinValue()+".."+afpv.getFuzzyMaxValue()+"]";
					}
					logger.trace("_parseExpression: IS FUZZY RANGE: valid={}", _isValid);
				} else
				if (isBoolean) {
					logger.trace("_parseExpression: IS BOOLEAN");
					if (operator!=OPERATOR.EQ && operator!=OPERATOR.UNKNOWN) {
						_isValid = false;
						errMsg = "Invalid boolean type operator: "+operator;
					} else {
						if (operator==OPERATOR.UNKNOWN) operator = OPERATOR.EQ;
						requiredBooleanValue = _parseBoolean( expression.substring(p).trim() );
						_isValid = true;
					}
					logger.trace("_parseExpression: IS BOOLEAN: valid={}", _isValid);
				} else
				if (isSet && hasOrder==false) {
					logger.trace("_parseExpression: IS UNORDERED SET");
					if (operator!=OPERATOR.EQ && operator!=OPERATOR.IN && operator!=OPERATOR.NOT_IN && operator!=OPERATOR.UNKNOWN) {
						_isValid = false;
						errMsg = "Invalid unordered set type operator: "+operator;
					} else {
						if (operator==OPERATOR.EQ || operator==OPERATOR.UNKNOWN) operator = OPERATOR.IN;
						String expr = expression.substring(p).trim();
						if (expr.startsWith("[") && expr.endsWith("]")) expr = expr.substring(1,expr.length()-1);
						else if (expr.startsWith("{") && expr.endsWith("}")) expr = expr.substring(1,expr.length()-1);
						String[] elem = expr.split("[\t,;]+");
						QualitativePropertyValue[] avs = ((AllowedQualitativePropertyValue)bpp.getRange()).getAllowedValues();
						_isValid = true;
						Vector<String> constr = new Vector<String>();
						for (int i=0; i<elem.length && _isValid; i++) {		// check that constraint elements exist
							_isValid = false;
							elem[i] = elem[i].trim();
							if (elem[i].isEmpty()) { _isValid=true; continue; }		// just ignore empty elements (e.g. a, , , b)
							if (avs!=null && avs.length>0) {
								for (int j=0; j<avs.length; j++) {
									if (avs[j].getValue().equalsIgnoreCase(elem[i])) {
										_isValid = true;
										constr.add(avs[j].getValue());
										break;
									}
								}
								if (!_isValid) { errMsg = "Element not found: "+elem[i]; break; }
							} else {				// allowed values set (in APV) is empty
								constr.add(elem[i]);	// just accept this value element
								_isValid = true;
							}
						}
						if (_isValid) elements = constr.toArray(new String[constr.size()]);
					}
					logger.trace("_parseExpression: IS UNORDERED SET: valid={}", _isValid);
				} else
				if (isSet && hasOrder==true) {
					logger.trace("_parseExpression: IS LINGUISTIC");
					if (operator!=OPERATOR.EQ && operator!=OPERATOR.NEQ && operator!=OPERATOR.LT && operator!=OPERATOR.LE && operator!=OPERATOR.GT && operator!=OPERATOR.GE) {
						_isValid = false;
						errMsg = "Invalid linguistic type operator: "+operator;
					} else {
						String tlim = expression.substring(p).trim();	// tlim: term limit
						QualitativePropertyValue[] avs = ((AllowedQualitativePropertyValue)bpp.getRange()).getAllowedValues();
						if (avs!=null && avs.length>0) {
							for (int i=0; i<avs.length; i++) {	// check that constraint term exists
								String s = avs[i].getValue().trim();		// format: 'term'  or  'term:TFN_Value' e.g. 'OK:(3,4,5)'
								int x = s.lastIndexOf(':');
								String term = (x==-1) ? s : s.substring(0,x);
								if (term.equalsIgnoreCase(tlim)) {
									TFN fval = (x==-1 || x>-1 && x+1<s.length()) ? new TFN(i, i+1, i+2) : TFN.valueOf( s.substring(x+1) );
									limit = i;
									fLimit = fval;
									termLimit = term;
									_isValid = true;
									break;
								}
							}
							// if not found, _isValid remains false
							if (!_isValid) errMsg = "Linguistic term not found: "+tlim;
						} else {	// no linguistic terms has been set (in BPP)
							errMsg = "PARSE: Linguistic terms set has not been set. Check preference variable: "+pv.getId()+"  of attribute: "+pv.getRefToServiceAttribute().getId()+"  and service classification: "+pv.getBelongsTo().getTitle();
							_isValid = false;
						}
					}
					logger.trace("_parseExpression: IS LINGUISTIC: valid={}", _isValid);
				} else {
					logger.trace("_parseExpression: INVALID TYPE: {}", pv.getClass().getName());
					_isValid = false;
					errMsg = "Invalid type '"+pv.getClass().getName()+"' in pref. variable of preference: "+consumerPreference.getId();
				}
				if (!_isValid) {
					throw new IllegalArgumentException("Constraint expression is not valid: "+expression+"\nReason: "+errMsg);
				}
		} finally {
			logger.trace("_parseExpression: FINALLY: error-message={}", errMsg);
		}
	}
	
	protected int _parseOperator() {
		char a = expression.charAt(0);
		char b = expression.length()>1 ? expression.charAt(1) : '\0';
		int nextPos = 2;
		if (a=='-') {		// comment
			_isComment = true;
			operator = OPERATOR.COMMENT;
			nextPos = 1;
		} else
		if (a=='<') {
			if (b=='=') operator = OPERATOR.LE;						// '<='
			else if (b=='>') operator = OPERATOR.NEQ;				// '<>'
			else { operator = OPERATOR.LT; nextPos = 1; }			// '<'
		} else
		if (a=='>') {
			if (b=='=') operator = OPERATOR.GE;						// '>='
			else { operator = OPERATOR.GT; nextPos = 1; }			// '>'
		} else
		if (a=='=') {
			if (b=='<') operator = OPERATOR.LE;						// '=<'
			else if (b=='>') operator = OPERATOR.GE;				// '=>'
			else operator = OPERATOR.EQ; nextPos = 1; 				// '='
		} else
		if (a=='@') {
			operator = OPERATOR.IN; nextPos = 1; 					// '@'
		} else
		if (a=='!') {
			if (b=='=') operator = OPERATOR.NEQ;					// '!='
			else { operator = OPERATOR.NOT_IN; nextPos = 1; }		// '!'
		} else {
			// else: it's a range or boolean or unordered set type, or it's an error
			nextPos = 0;
			operator = OPERATOR.UNKNOWN;
		}
		return nextPos;
	}
	
	protected boolean _parseBoolean(String b) {
		String s = b.trim().toLowerCase();
		for (int i=0; i<validBooleanTrueValues.length; i++) if (s.equals(validBooleanTrueValues[i])) return true;
		for (int i=0; i<validBooleanFalseValues.length; i++) if (s.equals(validBooleanFalseValues[i])) return false;
		throw new IllegalArgumentException("Invalid boolean value: "+b);
	}
	
	public boolean evaluate(Object value) {
		if (!_initialized) _parseExpression();
		if (!_isValid) throw new IllegalStateException("Constraint expression is not valid: "+expression+"\nPreference: "+consumerPreference.getId());
		
		String pvUri = consumerPreference.getPrefVariable();
		ServiceCategoryAttributeManagementService.PolicyObjects po = ServiceCategoryAttributeManagementService.getBrokerPolicyObjects(pvUri, false);
		logger.trace("evaluate: policy objects={}", po);
		if (po==null) throw new IllegalArgumentException("Policy objects NOT FOUND for pref.var. uri="+pvUri+"\nPreference: "+consumerPreference.getId());
		PreferenceVariable pv = po.pv;
		logger.trace("evaluate: pref.var.-serv.attr.={}", pv!=null?pv.getRefToServiceAttribute():null);
		String tmp;
		if (pv==null || (tmp=pv.getRefToServiceAttribute().getId())==null || tmp.trim().isEmpty()) throw new IllegalArgumentException("Pref.Var. is NOT VALID: uri="+pvUri+"\nPreference: "+consumerPreference.getId()+", Pref.Var.="+pv);
		boolean mandatory = consumerPreference.getMandatory();
		String errMsg = null;
		
		// Deduce preference variable type
		BrokerPolicyProperty bpp = po.bpp;
		boolean isNumeric = bpp instanceof BrokerPolicyQuantitativeProperty;
		boolean isSet = bpp instanceof BrokerPolicyQualitativeProperty;
		boolean isFuzzy = bpp instanceof BrokerPolicyFuzzyProperty;
		boolean isBoolean = bpp instanceof BrokerPolicyDatatypeProperty && ((BrokerPolicyDatatypeProperty)bpp).isBoolean();
		boolean isRange = isNumeric ? ((AllowedQuantitativePropertyValue)bpp.getRange()).isRange() :
						  isFuzzy ? ((AllowedFuzzyPropertyValue)bpp.getRange()).isRange() : false;
		boolean hasOrder = isSet ? ((AllowedQualitativePropertyValue)bpp.getRange()).getHasOrder() : false;
		
		String valStr;
		if (value==null) valStr = "";
		else if (value.getClass().isArray() && value.getClass().getComponentType().isPrimitive()) {
			Class type = value.getClass().getComponentType();
			valStr = null;
			if (type==Boolean.TYPE)			valStr = Arrays.toString((boolean[])value);
			else if (type==Byte.TYPE)		valStr = Arrays.toString((byte[])value);
			else if (type==Short.TYPE)		valStr = Arrays.toString((short[])value);
			else if (type==Integer.TYPE)	valStr = Arrays.toString((int[])value);
			else if (type==Long.TYPE)		valStr = Arrays.toString((long[])value);
			else if (type==Float.TYPE)		valStr = Arrays.toString((float[])value);
			else if (type==Double.TYPE)		valStr = Arrays.toString((double[])value);
			else if (type==Character.TYPE)	valStr = Arrays.toString((char[])value);
			else errMsg = "** IMPLEMENTATION ERROR ** : Non-primitive type found: "+type;
			if (valStr!=null) valStr = valStr.substring(1,valStr.length()-1).trim();
		} else if (value.getClass().isArray()) {
			valStr = Arrays.deepToString((Object[])value);
			valStr = valStr.substring(1,valStr.length()-1).trim();
		} else valStr = value.toString().trim();
		
		if (errMsg!=null) ; 		// don't do anything
		else
		if (valStr.isEmpty()) {		// existence check (if mandatory)
			if (mandatory) {
				errMsg = "No attribute value provided for mandatory attribute: "+pv.getRefToServiceAttribute();
			} else {
				return missingValueResult;
			}
		} else
		if (isNumeric && isRange==false) {
			double val = Double.parseDouble( valStr );
			AllowedQuantitativePropertyValue aqpv = (AllowedQuantitativePropertyValue)bpp.getRange();
			if (aqpv.getMinValue()<=val && val<=aqpv.getMaxValue()) {
				if (expression==null || _isComment) return true;	// if no constraint, evaluate 'true'
				switch (operator) {
					case EQ:  return (val==limit);
					case NEQ: return (val!=limit);
					case LT:  return (val<limit);
					case LE:  return (val<=limit);
					case GT:  return (val>limit);
					case GE:  return (val>=limit);
					default: errMsg = "** IMPLEMENTATION ERROR OR CORRUPTED DATA **: Invalid numeric type operator: "+operator;
				}
			} else {
				errMsg = "Not allowed attribute value: "+val+"\nAllowed values: ["+aqpv.getMinValue()+".."+aqpv.getMaxValue()+"]";
			}
		} else
		if (isNumeric && isRange==true) {
			NumericInterval val = NumericInterval.valueOf( valStr );
			AllowedQuantitativePropertyValue aqpv = (AllowedQuantitativePropertyValue)bpp.getRange();
			if (aqpv.getMinValue()<=val.getLowerBound() && val.getUpperBound()<=aqpv.getMaxValue()) {
				if (expression==null || _isComment) return true;	// if no constraint, evaluate 'true'
				NumericInterval constr = new NumericInterval(lowerBound, upperBound);
				switch (operator) {
					case IN:  return (val.join(constr)!=null);
					case NOT_IN: return (val.join(constr)==null);
					default: errMsg = "** IMPLEMENTATION ERROR OR CORRUPTED DATA **: Invalid numeric range type operator: "+operator;
				}
			} else {
				errMsg = "Not allowed attribute value: "+val+"\nAllowed values: ["+aqpv.getMinValue()+".."+aqpv.getMaxValue()+"]";
			}
		} else
		if (isFuzzy && isRange==false) {
			TFN val = TFN.valueOf( valStr );
			AllowedFuzzyPropertyValue afpv = (AllowedFuzzyPropertyValue)bpp.getRange();
			if (afpv.getMinSupport()<=val.getLowerBound() && val.getUpperBound()<=afpv.getMaxSupport()) {
				if (expression==null || _isComment) return true;	// if no constraint, evaluate 'true'
				switch (operator) {
					case EQ:  return (val.eq(fLimit));
					case NEQ: return (val.ne(fLimit));
					case LT:  return (val.lt(fLimit));
					case LE:  return (val.le(fLimit));
					case GT:  return (val.gt(fLimit));
					case GE:  return (val.ge(fLimit));
					default: errMsg = "** IMPLEMENTATION ERROR OR CORRUPTED DATA **: Invalid fuzzy type operator: "+operator;
				}
			} else {
				errMsg = "Not allowed attribute value: "+val+"\nAllowed values: ["+afpv.getFuzzyMinValue()+".."+afpv.getFuzzyMaxValue()+"]";
			}
		} else
		if (isFuzzy && isRange==true) {
			TFuzzyInterval val = TFuzzyInterval.valueOf( valStr );
			AllowedFuzzyPropertyValue afpv = (AllowedFuzzyPropertyValue)bpp.getRange();
			if (afpv.getMinSupport()<=val.getLowerBound() && val.getUpperBound()<=afpv.getMaxSupport()) {
				if (expression==null || _isComment) return true;	// if no constraint, evaluate 'true'
				TFuzzyInterval constr = new TFuzzyInterval(fLowerBound, fUpperBound);
				switch (operator) {
					case IN:  return (val.join(constr)!=null);
					case NOT_IN: return (val.join(constr)==null);
					default: errMsg = "** IMPLEMENTATION ERROR OR CORRUPTED DATA **: Invalid fuzzy range type operator: "+operator;
				}
			} else {
				errMsg = "Not allowed attribute value: "+val+"\nAllowed values: ["+afpv.getFuzzyMinValue()+".."+afpv.getFuzzyMaxValue()+"]";
			}
		} else
		if (isBoolean) {
			boolean val = _parseBoolean( valStr );
			//if (true) {
				if (expression==null || _isComment) return true;	// if no constraint, evaluate 'true'
				switch (operator) {
					case EQ: return val==requiredBooleanValue;
					default: errMsg = "** IMPLEMENTATION ERROR OR CORRUPTED DATA **: Invalid boolean type operator: "+operator;
				}
			//}
		} else
		if (isSet && hasOrder==false) {
			boolean foundAll = false;
			if (valStr.startsWith("[") && valStr.endsWith("]") && valStr.length()>2) valStr = valStr.substring(1,valStr.length()-1);
			else if (valStr.equals("[]")) valStr = "";
			String[] valElem = valStr.split("[\t,;]+");
			String[] members = ((AllowedQualitativePropertyValue)bpp.getRange()).getAllowedValuesAsString();
			int cntValid=0;
			if (members!=null && members.length>0) {
				// check if given value set elements are in allowed values set
				for (int i=0; i<valElem.length; i++) {
					String ve = (valElem[i]=valElem[i].trim());
					if (ve.isEmpty()) continue;		// ignore empty elements (e.g. a, , ,b)
					foundAll = false;
					for (String e : members) {
						if (e.equalsIgnoreCase(ve)) { foundAll = true; break; }
					}
					if (!foundAll) {
						errMsg = "Not allowed attribute value: "+ve+"\nAllowed values: "+java.util.Arrays.toString(members);
						break;
					} else cntValid++;
				}
			} else {	// no allowed values set has been provided
				for (int i=0; i<valElem.length; i++) {
					String ve = (valElem[i]=valElem[i].trim());
					if (ve.isEmpty()) continue;		// ignore empty elements (e.g. a, , ,b)
					cntValid++;
				}
				foundAll = true;
			}
			
			if (errMsg!=null) ;		// don't do anything
			else
			if (cntValid==0) {
				if (mandatory) {
					errMsg = "No attribute value provided for mandatory attribute: "+pv.getRefToServiceAttribute();
				} else {
					return missingValueResult;
				}
			} else
			if (foundAll) {	// cntValid > 0
				if (expression==null || _isComment) return true;	// if no constraint, evaluate 'true'
				if (operator==OPERATOR.IN && cntValid<elements.length) return false;	// 'value' cannot meet constraint since it's shorter !
				// find how many elements (attribute) 'value' and (constraint) 'elements' have in common
				int cnt = 0;
				for (String ve : valElem) {
					if (ve.isEmpty()) continue;		// ignore empty elements
					for (String ce : elements) {
						if (ce.equalsIgnoreCase(ve)) { cnt++; break; }	// i.e. constraint element == value element ??
					}
					switch (operator) {
						case IN: 
								if (cnt>=elements.length) return true;		// we've found all 'elements' elements. no need to keep searching
								else break;
						case NOT_IN: 
								if (cnt>0) return false;					// at least one common element between 'value' and 'elements' exist. no need to keep searching
								else break;
						default:
								break;		// error message will be set below
					}
				}
				switch (operator) {
					case IN: 
							return (cnt>=elements.length);		// ASSERT: false   because we expect that 'cnt<elements.length' at this point
					case NOT_IN: 
							return (cnt==0);					// ASSERT: true    because we expect that 'cnt==0' at this point
					default: errMsg = "** IMPLEMENTATION ERROR OR CORRUPTED DATA **: Invalid unordered set type operator: "+operator;
				}
			}
		} else
		if (isSet && hasOrder==true) {
			String[] terms = ((AllowedQualitativePropertyValue)bpp.getRange()).getAllowedValuesAsString();
			if (terms!=null && terms.length>0) {
				for (int i=0; i<terms.length; i++) {
					String s = terms[i].trim();		// format: 'term'  or  'term:TFN_Value' e.g. 'OK:(3,4,5)'
					int x = s.indexOf(':');
					String term = (x==-1) ? s : s.substring(0,x);
					if (term.equalsIgnoreCase(valStr)) {
						if (expression==null || _isComment) return true;	// if no constraint, evaluate 'true'
						switch (operator) {
							case EQ:  return (i==limit);
							case NEQ: return (i!=limit);
							case LT:  return (i<limit);
							case LE:  return (i<=limit);
							case GT:  return (i>limit);
							case GE:  return (i>=limit);
							default: errMsg = "** IMPLEMENTATION ERROR OR CORRUPTED DATA **: Invalid linguistic type operator: "+operator;
						}
						break;
					}
				}
				if (errMsg==null) errMsg = "Not allowed attribute value: "+valStr+"\nAllowed values: "+java.util.Arrays.toString(terms);
			} else {	// linguistic terms set is empty (in PV)
				errMsg = "Linguistic terms set has not been set. Check service category attribute: "+pv.getId()+"  of attribute: "+pv.getRefToServiceAttribute()+"  and service category: "+pv.getBelongsTo().getTitle();
			}
		} else {
			errMsg = "Invalid constraint type: "+pv.getClass().getName();
		}
		if (errMsg!=null) {
			throw new IllegalArgumentException(errMsg+"\nPreference: "+consumerPreference.getId()+"\nConstraint: "+expression+"\nAttribute value: "+valStr);
		}
		return false;
	}
	
	@JsonIgnore
	public String[] getVariableNames() {
		throw new RuntimeException("Method NOT IMPLEMENTED : ConsumerPreferenceExpression.getVariableNames()");
	}
	
	public String toString() {
		return 	"ConsumerPreferenceExpression: {\n"+super.toString()+
				"\tpreference="+(consumerPreference!=null ? consumerPreference.getId() : null)+"\n\texpression="+expression+"\n}\n";
	}
}
