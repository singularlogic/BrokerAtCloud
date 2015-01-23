package eu.brokeratcloud.opt;

import eu.brokeratcloud.common.BrokerObject;
import eu.brokeratcloud.common.policy.*;
import eu.brokeratcloud.opt.policy.*;
import eu.brokeratcloud.opt.type.*;
import eu.brokeratcloud.persistence.annotations.*;
import eu.brokeratcloud.rest.opt.ServiceCategoryAttributeManagementServiceNEW;

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
//XXX: SOS:  @XmlAttribute  MUST NOT BE SET FOR THIS FIELD IN ORDER TO AVOID INFINITE RECURSION DURING "JSON SERIALIZATION"
//	@XmlAttribute
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
	//protected ConsumerPreference consumerPreference;
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
				/*if (consumerPreference==null || consumerPreference.getCriterion()==null) {
					return;
				}*/
				logger.trace("_parseExpression: preference={}", consumerPreference);
				if (consumerPreference==null) return;
//XXX:2014-11-21
				logger.trace("_parseExpression: preference variable={}", consumerPreference.getPrefVariable());
				if (consumerPreference.getPrefVariable()==null) return;
				
//XXX:2014-11-21: DEL+++++
				/*PreferenceVariable pv = consumerPreference.getCriterion();
				
				String tmp;
				logger.trace("_parseExpression: PV={}, is-unknown={}", pv!=null?pv.getRefToServiceAttribute():null, "-" /*sca.isUnknown()* /);
				if (pv==null || pv.getRefToServiceAttribute()==null || (tmp=pv.getRefToServiceAttribute().getId())==null || tmp.trim().isEmpty() /*|| sca.isUnknown()* /) {
					return;
				}*/
//XXX:2014-11-21
				String pvUri = consumerPreference.getPrefVariable();
				ServiceCategoryAttributeManagementServiceNEW.PolicyObjects po = ServiceCategoryAttributeManagementServiceNEW.getBrokerPolicyObjects(pvUri, false);
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
				BrokerPolicyProperty bpp = po.bpp;	//XXX:2014-11-21: pv.getRefToBrokerPolicyProperty();
				AllowedPropertyValue apv = po.apv;	//XXX:2014-11-21
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
//						_isValid = (sca.getFmin().getLowerBound()<=lim.getLowerBound() && lim.getUpperBound()<=sca.getFmax().getUpperBound());		// constraint limit/threshold in allowed values range
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
//						_isValid = (sca.getFmin().getLowerBound()<=fLowerBound.getLowerBound() && fUpperBound.getUpperBound()<=sca.getFmax().getUpperBound());		// constraint range in allowed values range
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
					if (operator!=OPERATOR.EQ && operator!=OPERATOR.NOT_IN && operator!=OPERATOR.UNKNOWN) {
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
//									TFN fval = (x==-1 && x-1<s.length()) ? new TFN(i, i+1, i+2) : TFN.valueOf( s.substring(x+1) );
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
		//if (!_initialized) throw new IllegalStateException("Constraint expression has not been initialized: Consumer preference has not been set or it is invalid: id="+consumerPreference.getId());
		if (!_initialized) _parseExpression();
		if (!_isValid) throw new IllegalStateException("Constraint expression is not valid: "+expression+"\nPreference: "+consumerPreference.getId());
		
//		ServiceCategoryAttribute sca = consumerPreference.getCriterion();
//XXX:2014-11-21
//		PreferenceVariable pv = consumerPreference.getCriterion();
		String pvUri = consumerPreference.getPrefVariable();
		ServiceCategoryAttributeManagementServiceNEW.PolicyObjects po = ServiceCategoryAttributeManagementServiceNEW.getBrokerPolicyObjects(pvUri, false);
		logger.trace("evaluate: policy objects={}", po);
		if (po==null) throw new IllegalArgumentException("Policy objects NOT FOUND for pref.var. uri="+pvUri+"\nPreference: "+consumerPreference.getId());
		PreferenceVariable pv = po.pv;
		logger.trace("evaluate: pref.var.-serv.attr.={}", pv!=null?pv.getRefToServiceAttribute():null);
		String tmp;
		if (pv==null || (tmp=pv.getRefToServiceAttribute().getId())==null || tmp.trim().isEmpty()) throw new IllegalArgumentException("Pref.Var. is NOT VALID: uri="+pvUri+"\nPreference: "+consumerPreference.getId()+", Pref.Var.="+pv);
		boolean mandatory = consumerPreference.getMandatory();
		String errMsg = null;
		
		// Deduce preference variable type
		BrokerPolicyProperty bpp = po.bpp;	//XXX:2014-11-21: pv.getRefToBrokerPolicyProperty();
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
				return missingValueResult;		// XXX: What if expression is also empty??  DISCUSS with Vergi
												// XXX: e.g.  return (expression==null);	i.e. 'true' if expr. is empty and 'false' if not
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
			if (true) {
				if (expression==null || _isComment) return true;	// if no constraint, evaluate 'true'
				switch (operator) {
					case EQ: return val==requiredBooleanValue;
					default: errMsg = "** IMPLEMENTATION ERROR OR CORRUPTED DATA **: Invalid boolean type operator: "+operator;
				}
			}
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
					return missingValueResult;		// XXX: What if expression is also empty??  DISCUSS with Vergi
													// XXX: e.g.  return (expression==null);	i.e. 'true' if expr. is empty and 'false' if not
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
				errMsg = "EVAL: Linguistic terms set has not been set. Check service category attribute: "+pv.getId()+"  of attribute: "+pv.getRefToServiceAttribute()+"  and service category: "+pv.getBelongsTo().getTitle();
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
	
	public boolean evaluate(Object[] values) {
		// Extended version of evaluate() for multi-variable expressions...
		throw new RuntimeException("Method NOT IMPLEMENTED : ConsumerPreferenceExpression.evaluate(Object[])");
	}
	
	public boolean evaluate(Map<String,Object> values) {
		// Extended version of evaluate() for multi-variable expressions...
		throw new RuntimeException("Method NOT IMPLEMENTED : ConsumerPreferenceExpression.evaluate(Map[])");
	}
	
	public String toString() {
		return 	"ConsumerPreferenceExpression: {\n"+super.toString()+
				"\tpreference="+(consumerPreference!=null ? consumerPreference.getId() : null)+"\n\texpression="+expression+"\n}\n";
	}
	
//XXX: UNIT TESTS - REMOVE !!!
//XXX: AFTER REMODELING LINKED-USDL EXT. FOR BROKER@CLOUD & ASSOCAITED CODE, THE FOLLOWING UNIT TESTS ARE OBSOLETE
/*	protected static int cntTestsPassed;
	protected static int cntTestsFailed;
	protected static int cntTestsNotChecked;
	
	public static void main(String[] args) throws Exception {
		ServiceCategoryAttribute sca = new ServiceCategoryAttribute();
		sca.setId( "SCA-"+java.util.UUID.randomUUID().toString() );
		sca.setAttribute( "attribute1" );
		
		ConsumerPreference pref = new ConsumerPreference();
		pref.setId( "PREF-"+java.util.UUID.randomUUID().toString() );
		pref.setCriterion( sca );
		
		ConsumerPreferenceExpression expr = new ConsumerPreferenceExpression();
		expr.setId( "EXPR-"+java.util.UUID.randomUUID().toString() );
		//expr.setConsumerPreference( pref );
		pref.setExpression( expr );
		
		// Tests...
		String exprStr = null;
		Object[] values = null;
		String[] expected = null;	// expected test outcome (ACCEPT, REJECT, EXCEPTION (any), <exception class>
		boolean result;
		
		cntTestsPassed = cntTestsFailed = 0;
		
		// UNINITIALIZED EXPRESSION tests
		logger.info("===============================================================================");
		logger.info("==              * * * *  UNINITIALIZED EXPRESSION TESTS  * * * *             ==");
		logger.info("===============================================================================");
		logger.info("Test group: type={}, min={}, max={}, mandatory={}", sca.getType(), sca.getMin(), sca.getMax(), sca.getMandatory());
		values = _setArr( 1, 2 );
		expected = _setStringArr( "IllegalStateException", "IllegalStateException" );
		try { expr.setConsumerPreference(null); } catch (Exception e) { logger.error("Exception while setting consumer preference: {}: {}\nSuppressing exception propagation", e.getClass().getName(), e.getMessage()); }
		runTest("UNINITIALIZED expression", sca, expr, true, "- comment", values, expected, true);
		logger.info("Restoring consumer preference");
		try { expr.setConsumerPreference(pref); } catch (Exception e) { logger.error("Exception while setting consumer preference: {}: {}\nSuppressing exception propagation", e.getClass().getName(), e.getMessage()); }
		expected = null;
		
		// COMMENT tests
		logger.info("===============================================================================");
		logger.info("==                      * * * *  COMMENT TESTS  * * * *                      ==");
		logger.info("===============================================================================");
		sca.setType( "NUMERIC_INC" );
		sca.setMin( 0 );
		sca.setMax( 10 );
		logger.info("Test group: type={}, min={}, max={}, mandatory={}", sca.getType(), sca.getMin(), sca.getMax(), sca.getMandatory());
		values = _setArr( "8", new Integer(18), 9, null, 5d, "patata" );
		expected = _setStringArr( "ACCEPT", "IllegalArgumentException", "ACCEPT", "ACCEPT", "ACCEPT", "NumberFormatException" );
		runTest("with COMMENT expression", sca, expr, false, "- comment", values, expected, true);
		expected = _setStringArr( "ACCEPT", "IllegalArgumentException", "ACCEPT", "IllegalArgumentException", "ACCEPT", "NumberFormatException" );
		runTest("with COMMENT expression", sca, expr, true, "- comment", values, expected, true);
		expected = null;
		
		// NUMERIC_INC tests
		logger.info("===============================================================================");
		logger.info("==                    * * * *  NUMERIC_INC TESTS  * * * *                    ==");
		logger.info("===============================================================================");
		sca.setType( "NUMERIC_INC" );
		sca.setMin( 0 );
		sca.setMax( 10 );
		logger.info("Test group: type={}, min={}, max={}, mandatory={}", sca.getType(), sca.getMin(), sca.getMax(), sca.getMandatory());
		values = _setArr( "8", new Integer(18), 9, null, 5d, "patata" );
		expected = _setStringArr( "ACCEPT", "IllegalArgumentException", "ACCEPT", "ACCEPT", "ACCEPT", "NumberFormatException" );
		runTest("empty expression", sca, expr, false, "", values, expected, false);
		expected = _setStringArr( "ACCEPT", "IllegalArgumentException", "ACCEPT", "IllegalArgumentException", "ACCEPT", "NumberFormatException" );
		runTest("empty expression", sca, expr, true, "", values, expected, true);
		expected = _setStringArr( "REJECT", "IllegalArgumentException", "REJECT", "IllegalArgumentException", "ACCEPT", "NumberFormatException" );
		runTest("WITH expression", sca, expr, true, "<7", values, expected, true);
		expected = _setStringArr( "REJECT", "IllegalArgumentException", "REJECT", "ACCEPT", "ACCEPT", "NumberFormatException" );
		runTest("WITH expression", sca, expr, false, "<7", values, expected, true);
		expected = _setStringArr( "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException" );
		runTest("with NOT ALLOWED VALUE expression", sca, expr, false, ">25", values, expected, true);
		runTest("NO OPERATOR expression", sca, expr, false, "5", values, expected, true);
		runTest("WRONG OPERATOR expression", sca, expr, false, "!25", values, expected, true);
		runTest("INVALID expression", sca, expr, false, "XXXXXXXXXXXXX", values, expected, true);
		expected = null;
		
		// NUMERIC_RANGE tests
		logger.info("===============================================================================");
		logger.info("==                   * * * *  NUMERIC_RANGE TESTS  * * * *                   ==");
		logger.info("===============================================================================");
		sca.setType( "NUMERIC_RANGE" );
		sca.setMin( 0 );
		sca.setMax( 10 );
		logger.info("Test group: type={}, min={}, max={}, mandatory={}", sca.getType(), sca.getMin(), sca.getMax(), sca.getMandatory());
		values = _setArr("[8-10]", "3-5", "[8,18]", new NumericInterval(4d,6d), null, 7d, "patata");
		expected = _setStringArr( "ACCEPT", "ACCEPT", "IllegalArgumentException", "ACCEPT", "ACCEPT", "ACCEPT", "NumberFormatException" );
		runTest("empty expression", sca, expr, false, "", values, expected, false);
		expected = _setStringArr( "ACCEPT", "ACCEPT", "IllegalArgumentException", "ACCEPT", "IllegalArgumentException", "ACCEPT", "NumberFormatException" );
		runTest("empty expression", sca, expr, true, "", values, expected, true);
		expected = _setStringArr( "ACCEPT", "REJECT", "IllegalArgumentException", "REJECT", "IllegalArgumentException", "ACCEPT", "NumberFormatException" );
		runTest("WITH expression", sca, expr, true, "[7-10]", values, expected, true);
		expected = _setStringArr( "ACCEPT", "REJECT", "IllegalArgumentException", "REJECT", "ACCEPT", "ACCEPT", "NumberFormatException" );
		runTest("WITH expression", sca, expr, false, "[7-10]", values, expected, true);
		expected = _setStringArr( "REJECT", "ACCEPT", "IllegalArgumentException", "REJECT", "ACCEPT", "REJECT", "NumberFormatException" );
		runTest("with NOT expression", sca, expr, false, "![6-10]", values, expected, true);
		values = _setArr(3, 5.9, 6, 8, 10, 11, null);
		expected = _setStringArr( "ACCEPT", "ACCEPT", "REJECT", "REJECT", "REJECT", "IllegalArgumentException", "ACCEPT" );
		runTest("with NOT expression, NUMERIC values", sca, expr, false, "![6-10]", values, expected, true);
		values = _setArr(3, new TFN(4), "true");
		expected = _setStringArr( "ACCEPT", "NumberFormatException", "NumberFormatException" );
		runTest("with NOT expression, MIXED-TYPE values", sca, expr, false, "![6-10]", values, expected, true);
		expected = _setStringArr( "IllegalStateException", "IllegalStateException", "IllegalStateException" );
		runTest("with NOT ALLOWED VALUE expression", sca, expr, false, "[6-11]", values, expected, true);
		runTest("WRONG OPERATOR expression", sca, expr, false, "<[15-3]", values, expected, true);
		runTest("INVALID expression", sca, expr, false, "invalid_expr", values, expected, true);
		expected = null;
		
		// FUZZY_DEC tests
		logger.info("===============================================================================");
		logger.info("==                     * * * *  FUZZY_DEC TESTS  * * * *                     ==");
		logger.info("===============================================================================");
		sca.setType( "FUZZY_DEC" );
		sca.setFmin( new TFN(0) );
		sca.setFmax( new TFN(10) );
		logger.info("Test group: type={}, min={}, max={}, mandatory={}", sca.getType(), sca.getFmin(), sca.getFmax(), sca.getMandatory());
		values = _setArr( "(7.5,8,9)", "3.5, 4; 4.5", "18 18;19", new TFN(4d,5d,6d), null, 7d, "patata", "A, 8, 9" );
		expected = _setStringArr( "ACCEPT", "ACCEPT", "IllegalArgumentException", "ACCEPT", "ACCEPT", "ACCEPT", "NumberFormatException", "NumberFormatException" );
		runTest("empty expression", sca, expr, false, "", values, expected, false);
		expected = _setStringArr( "ACCEPT", "ACCEPT", "IllegalArgumentException", "ACCEPT", "IllegalArgumentException", "ACCEPT", "NumberFormatException", "NumberFormatException" );
		runTest("empty expression", sca, expr, true, "", values, expected, true);
		expected = _setStringArr( "REJECT", "ACCEPT", "IllegalArgumentException", "REJECT", "IllegalArgumentException", "REJECT", "NumberFormatException", "NumberFormatException" );
		runTest("WITH expression", sca, expr, true, "<=3;4;5", values, expected, true);
		expected = _setStringArr( "REJECT", "ACCEPT", "IllegalArgumentException", "REJECT", "ACCEPT", "REJECT", "NumberFormatException", "NumberFormatException" );
		runTest("WITH expression", sca, expr, false, "<=3;4;5", values, expected, true);
		expected = _setStringArr( "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException" );
		runTest("with NOT ALLOWED VALUE expression", sca, expr, false, "<=-1;0;.5", values, expected, true);
		runTest("INVALID expression", sca, expr, false, "<=1 2", values, expected, true);
		expected = null;
		
		// FUZZY_RANGE tests
		logger.info("===============================================================================");
		logger.info("==                  * * * *  FUZZY_RANGE TESTS  * * * *                      ==");
		logger.info("===============================================================================");
		sca.setType( "FUZZY_RANGE" );
		sca.setFmin( new TFN(0) );
		sca.setFmax( new TFN(10) );
		logger.info("Test group: type={}, min={}, max={}, mandatory={}", sca.getType(), sca.getFmin(), sca.getFmax(), sca.getMandatory());
		values = _setArr( "[7.5,8,9,10]", "(3.5, 4; 4.5	5)", "8,9,10", "18 18;19,20", "8 9;10,10.1", new TFN(4d,5d,6d), new TFuzzyInterval(1,2,3,4), null, 7d, "patata", "A, 8, 9" );
		expected = _setStringArr( "ACCEPT", "ACCEPT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "ACCEPT", "ACCEPT", "ACCEPT", "NumberFormatException", "NumberFormatException", "NumberFormatException" );
		runTest("empty expression", sca, expr, false, "", values, expected, false);
		expected = _setStringArr( "ACCEPT", "ACCEPT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "ACCEPT", "ACCEPT", "IllegalArgumentException", "NumberFormatException", "NumberFormatException", "NumberFormatException" );
		runTest("empty expression", sca, expr, true, "", values, expected, true);
		expected = _setStringArr( "ACCEPT", "REJECT", "REJECT", "IllegalArgumentException", "IllegalArgumentException", "REJECT", "REJECT", "IllegalArgumentException", "NumberFormatException", "NumberFormatException", "NumberFormatException" );
		runTest("WITH expression", sca, expr, true, "[6,7,8,9]", values, expected, true);
		expected = _setStringArr( "ACCEPT", "REJECT", "REJECT", "IllegalArgumentException", "IllegalArgumentException", "REJECT", "REJECT", "ACCEPT", "NumberFormatException", "NumberFormatException", "NumberFormatException" );
		runTest("WITH expression", sca, expr, false, "[6,7,8,9]", values, expected, true);
		expected = _setStringArr( "REJECT", "ACCEPT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "ACCEPT", "ACCEPT", "ACCEPT", "NumberFormatException", "NumberFormatException", "NumberFormatException" );
		runTest("with NOT expression", sca, expr, false, "![6,7,8,9]", values, expected, true);
		expected = _setStringArr( "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException" );
		runTest("with NOT ALLOWED VALUE expression", sca, expr, false, "[6,7,8,11]", values, expected, true);
		runTest("INVALID expression", sca, expr, false, "(6,7)", values, expected, true);
		expected = null;
		
		// BOOLEAN tests
		logger.info("===============================================================================");
		logger.info("==                    * * * *  BOOLEAN TESTS  * * * *                        ==");
		logger.info("===============================================================================");
		sca.setType( "BOOLEAN" );
		logger.info("Test group: type={}, allowed values=[true, false], mandatory={}", sca.getType(), sca.getMandatory());
		values = _setArr( "true", "false", true, new Boolean(false), "on", "-1", new TFuzzyInterval(1,2,3,4), null, "patata" );
		expected = _setStringArr( "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "IllegalArgumentException", "ACCEPT", "IllegalArgumentException" );
		runTest("empty expression", sca, expr, false, "", values, expected, false);
		expected = _setStringArr( "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "IllegalArgumentException" );
		runTest("empty expression", sca, expr, true, "", values, expected, true);
		expected = _setStringArr( "ACCEPT", "REJECT", "ACCEPT", "REJECT", "ACCEPT", "REJECT", "IllegalArgumentException", "IllegalArgumentException", "IllegalArgumentException" );
		runTest("WITH expression", sca, expr, true, "true", values, expected, true);
		expected = _setStringArr( "ACCEPT", "REJECT", "ACCEPT", "REJECT", "ACCEPT", "REJECT", "IllegalArgumentException", "ACCEPT", "IllegalArgumentException" );
		runTest("WITH expression", sca, expr, false, "true", values, expected, true);
		expected = _setStringArr( "REJECT", "ACCEPT", "REJECT", "ACCEPT", "REJECT", "ACCEPT", "IllegalArgumentException", "ACCEPT", "IllegalArgumentException" );
		runTest("with FALSE expression", sca, expr, false, "false", values, expected, true);
		expected = _setStringArr( "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException" );
		runTest("INVALID expression", sca, expr, false, "ZZZZZZZ", values, expected, true);
		expected = null;
		
		// UNORDERED_SET tests
		logger.info("===============================================================================");
		logger.info("==                 * * * *  UNORDERED SET TESTS  * * * *                     ==");
		logger.info("===============================================================================");
		sca.setType( "UNORDERED_SET" );
		sca.setMembers( _setStringArr("A", "B", "C") );
		logger.info("Test group: type={}, allowed values set={}, mandatory={}", sca.getType(), Arrays.deepToString(sca.getMembers()), sca.getMandatory());
		char[] primitiveArr = new char[2];
		primitiveArr[0]='C'; primitiveArr[1]='B';
		values = _setArr( "A, B", "A", _setArr("A","C"), null, "F", "[A, F]", primitiveArr, "[]", "" );
		expected = _setStringArr( "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "ACCEPT", "ACCEPT", "ACCEPT" );
		runTest("empty expression", sca, expr, false, "", values, expected, false);
		expected = _setStringArr( "ACCEPT", "ACCEPT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "IllegalArgumentException", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException" );
		runTest("empty expression", sca, expr, true, "", values, expected, true);
		expected = _setStringArr( "REJECT", "REJECT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "IllegalArgumentException", "REJECT", "IllegalArgumentException", "IllegalArgumentException" );
		runTest("WITH expression", sca, expr, true, "A;C", values, expected, true);
		expected = _setStringArr( "REJECT", "REJECT", "ACCEPT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "REJECT", "ACCEPT", "ACCEPT" );
		runTest("WITH expression", sca, expr, false, "A;C", values, expected, true);
		expected = _setStringArr( "REJECT", "ACCEPT", "REJECT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "REJECT", "ACCEPT", "ACCEPT" );
		runTest("with NOT expression", sca, expr, false, "![B;C]", values, expected, true);
		expected = _setStringArr( "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException" );
		runTest("NOT ALLOWED VALUE expression", sca, expr, false, "A;F", values, expected, true);
		expected = _setStringArr( "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "ACCEPT", "ACCEPT", "ACCEPT" );
		runTest("DELIMITERS ONLY expression", sca, expr, false, ";,\t ", values, expected, true);
		sca.setMembers( null );
		expected = _setStringArr( "ACCEPT", "REJECT", "REJECT", "ACCEPT", "REJECT", "REJECT", "REJECT", "ACCEPT", "ACCEPT" );
		runTest("NO allowed values", sca, expr, false, "A, B", values, expected, true);
		expected = _setStringArr( "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT" );
		runTest("NO allowed values, NO expression", sca, expr, false, "", values, expected, true);
		expected = null;
		
		// LINGUISTIC tests WITHOUT fuzzy mappings
		logger.info("===============================================================================");
		logger.info("==      * * * *  LINGUISTIC TESTS (without fuzzy mappings)  * * * *          ==");
		logger.info("===============================================================================");
		sca.setType( "LINGUISTIC" );
		sca.setTerms( _setStringArr("BAD", "OK", "GOOD") );
		logger.info("Test group: type={}, terms={}, mandatory={}", sca.getType(), Arrays.deepToString(sca.getTerms()), sca.getMandatory());
		primitiveArr = new char[2];
		primitiveArr[0]='O'; primitiveArr[1]='K';
		values = _setArr( "BAD", "OK", "GOOD", _setArr("OK"), null, "XXX", primitiveArr, " " );
		expected = _setStringArr( "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "ACCEPT" );
		runTest("empty expression", sca, expr, false, "", values, expected, false);
		expected = _setStringArr( "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "IllegalArgumentException", "IllegalArgumentException" );
		runTest("empty expression", sca, expr, true, "", values, expected, true);
		expected = _setStringArr( "REJECT", "ACCEPT", "ACCEPT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "IllegalArgumentException", "IllegalArgumentException" );
		runTest("WITH expression", sca, expr, true, ">=OK", values, expected, true);
		expected = _setStringArr( "REJECT", "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "ACCEPT" );
		runTest("WITH expression", sca, expr, false, ">=OK", values, expected, true);
		expected = _setStringArr( "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException" );
		runTest("NOT ALLOWED VALUE expression", sca, expr, false, ">=XXX", values, expected, true);
		runTest("NO OPERATOR expression", sca, expr, false, "OK", values, expected, true);
		runTest("WRONG OPERATOR expression", sca, expr, false, "!OK", values, expected, true);
		sca.setTerms( null );
		runTest("NO TERMS", sca, expr, false, ">OK", values, expected, true);
		expected = null;
		
		// LINGUISTIC tests WITH fuzzy mappings
		logger.info("===============================================================================");
		logger.info("==       * * * *  LINGUISTIC TESTS (with fuzzy mappings)  * * * *            ==");
		logger.info("===============================================================================");
		sca.setType( "LINGUISTIC" );
		sca.setTerms( _setStringArr("BAD:(1,2,3)", "OK:(2,3,4)", "GOOD:(3,4,5)") );
		logger.info("Test group: type={}, terms={}, mandatory={}", sca.getType(), Arrays.deepToString(sca.getTerms()), sca.getMandatory());
		primitiveArr = new char[2];
		primitiveArr[0]='O'; primitiveArr[1]='K';
		values = _setArr( "BAD", "OK", "GOOD", _setArr("OK"), null, "XXX", primitiveArr, " " );
		expected = _setStringArr( "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "ACCEPT" );
		runTest("empty expression", sca, expr, false, "", values, expected, true);
		expected = _setStringArr( "ACCEPT", "ACCEPT", "ACCEPT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "IllegalArgumentException", "IllegalArgumentException" );
		runTest("empty expression", sca, expr, true, "", values, expected, true);
		expected = _setStringArr( "REJECT", "REJECT", "ACCEPT", "REJECT", "IllegalArgumentException", "IllegalArgumentException", "IllegalArgumentException", "IllegalArgumentException" );
		runTest("WITH expression", sca, expr, true, ">OK", values, expected, true);
		expected = _setStringArr( "REJECT", "REJECT", "ACCEPT", "REJECT", "ACCEPT", "IllegalArgumentException", "IllegalArgumentException", "ACCEPT" );
		runTest("WITH expression", sca, expr, false, ">OK", values, expected, true);
		expected = _setStringArr( "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException", "IllegalStateException" );
		runTest("NOT ALLOWED VALUE expression", sca, expr, false, ">XXX", values, expected, true);
		runTest("NO OPERATOR expression", sca, expr, false, "OK", values, expected, true);
		runTest("WRONG OPERATOR expression", sca, expr, false, "!OK", values, expected, true);
		sca.setTerms( null );
		runTest("NO TERMS", sca, expr, false, ">=OK", values, expected, true);
		expected = null;

		logger.info("Done testing!");
		logger.info("");
		logger.info("Tests Passed:      {}", cntTestsPassed);
		logger.info("Tests Failed:      {}", cntTestsFailed);
		logger.info("Tests not checked: {}", cntTestsNotChecked);
		logger.info("Total Tests:       {}", cntTestsPassed+cntTestsFailed+cntTestsNotChecked);
	}
	
	protected static Object[] _setArr(Object... args) {
		return args;
	}
	
	protected static String[] _setStringArr(String... args) {
		return args;
	}
	
	protected static int stepwiseTesting = 0;
	
//	protected static void runTest(String title, ServiceCategoryAttribute sca, ConsumerPreferenceExpression expr, boolean mandatory, Object expression, Object[] values, boolean suppressExceptions) {
//		runTest(title, sca, expr, mandatory, expression, values, null, suppressExceptions);
//	}
	
	protected static void runTest(String title, ServiceCategoryAttribute sca, ConsumerPreferenceExpression expr, boolean mandatory, Object expression, Object[] values, String[] expected, boolean suppressExceptions) {
		if (expected!=null && values.length!=expected.length) throw new IllegalArgumentException("runTest: 'values' and 'expected' arrays are of different size");
		
		logger.info("===============================================================================");
		logger.info("Test: {} attribute, {}", mandatory?"mandatory":"optional", title);
		sca.setMandatory( mandatory );
		logger.debug("SCA: {}", sca);
		String exprStr = expression!=null ? expression.toString().trim() : "";
		logger.info("Setting expression to: {}", exprStr);
		if (!exprStr.isEmpty()) {
			if (!suppressExceptions) {
				expr.setExpression( exprStr );
			} else {
				try {
					expr.setExpression( exprStr ); 
				} catch (Exception e) {
					logger.error("    Exception while setting expression: {}", e.getClass().getName());
					logger.error("    Message: {}\n    Suppressing exception propagation", e.getMessage().replace("\n","\n\t     "));
				}
			}
		} else expr.setExpression( null );
		logger.debug("EXPR: {}", expr);
		logger.info("OPERATOR: {}", expr.getOperator());
		logger.info("Running tests:");
		_doEvals(expr, values, expected);
		if (stepwiseTesting>=1) pauseStep();
	}
	
	protected static void pauseStep() { try { System.in.read(); System.in.read(); } catch (Exception e) {} }
	
	protected static void _doEvals(ConsumerPreferenceExpression expr, Object[] values, String[] expected) {
		int i=0;
		for (Object v : values) {
			try {
				if (stepwiseTesting>=2) pauseStep();
				Object value = v;
				logger.info("Test-{}: Evaluating with value: {}  (type: {})", i+1, value, value!=null ? value.getClass().getName() : "-");
				boolean result = expr.evaluate( value );
				String resultStr = result ? "ACCEPT" : "REJECT";
				logger.info("     Result: {}", resultStr );
				if (expected!=null) {
					boolean cmp = expected[i].equalsIgnoreCase(resultStr);
					if (cmp) cntTestsPassed++; else cntTestsFailed++;
					logger.info("     Test: {}", cmp ? "PASSED" : "FAILED   expected: "+expected[i]);
				} else cntTestsNotChecked++;
			} catch (Exception e) {
				logger.error("     Result: EXCEPTION: {}", e.getClass().getName());
				logger.error("     REASON   : {}", e.getMessage().replace("\n","\n\t\t"));
				if (expected!=null) {
					String excStr = e.getClass().getName();
					int p = excStr.lastIndexOf('.');
					excStr = excStr.substring(p+1);
					boolean cmp = expected[i].equalsIgnoreCase("EXCEPTION") || expected[i].equalsIgnoreCase(excStr);
					if (cmp) cntTestsPassed++; else cntTestsFailed++;
					logger.error("     Test: {}", cmp ? "PASSED" : "FAILED   expected: "+expected[i]);
				} else cntTestsNotChecked++;
				logger.debug("Stack trace:\n{}", e);
			} finally {
				i++;
			}
			//logger.info("");
		}
	}
*/
}
