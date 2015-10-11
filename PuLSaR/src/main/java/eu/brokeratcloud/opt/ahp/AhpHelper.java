package eu.brokeratcloud.opt.ahp;

import eu.brokeratcloud.common.RootObject;
import eu.brokeratcloud.common.ServiceDescription;
import eu.brokeratcloud.opt.ConsumerPreference;
import eu.brokeratcloud.opt.ConsumerPreferenceExpression;
import eu.brokeratcloud.opt.OptimisationAttribute;
import eu.brokeratcloud.opt.ServiceCategoryAttribute;
import eu.brokeratcloud.opt.RecommendationManager;
import eu.brokeratcloud.opt.type.TFN;
import eu.brokeratcloud.opt.type.TFuzzyInterval;
import eu.brokeratcloud.opt.type.NumericInterval;
import eu.brokeratcloud.util.*;

import java.lang.reflect.Method;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AhpHelper extends RootObject {
	private static final Logger logger = LoggerFactory.getLogger("eu.brokeratcloud.opt.rank.ahp");
	
	protected static Method _NUMERIC_INC_RI_CALC_METHOD;
	protected static Method _NUMERIC_DEC_RI_CALC_METHOD;
	protected static Method _NUMERIC_RANGE_RI_CALC_METHOD;
	protected static Method _FUZZY_INC_RI_CALC_METHOD;
	protected static Method _FUZZY_DEC_RI_CALC_METHOD;
	protected static Method _FUZZY_RANGE_RI_CALC_METHOD;
	protected static Method _BOOLEAN_RI_CALC_METHOD;
	protected static Method _UNORDERED_SET_RI_CALC_METHOD;
	protected static Method _LINGUISTIC_RI_CALC_METHOD;
	
	public AhpHelper() throws NoSuchMethodException {
		if (_NUMERIC_INC_RI_CALC_METHOD==null) {
			Class[] cArg = new Class[6];
			cArg[0] = cArg[1] = cArg[2] = cArg[5] = Object.class;
			cArg[3] = double.class;
			cArg[4] = boolean.class;
			//
			Class[] cArg2 = new Class[7];
			cArg2[0] = cArg2[1] = cArg2[2] = cArg2[5] = Object.class;
			cArg2[3] = double.class;
			cArg2[4] = cArg2[6] = boolean.class;
			//
			Class clss = getClass();
			_NUMERIC_INC_RI_CALC_METHOD = clss.getDeclaredMethod("_calcNumericIncRelativeImportance", cArg);
			_NUMERIC_DEC_RI_CALC_METHOD = clss.getDeclaredMethod("_calcNumericDecRelativeImportance", cArg);
			_NUMERIC_RANGE_RI_CALC_METHOD = clss.getDeclaredMethod("_calcNumericRangeRelativeImportance", cArg2);
			_FUZZY_INC_RI_CALC_METHOD = clss.getDeclaredMethod("_calcFuzzyIncRelativeImportance", cArg);
			_FUZZY_DEC_RI_CALC_METHOD = clss.getDeclaredMethod("_calcFuzzyDecRelativeImportance", cArg);
			_FUZZY_RANGE_RI_CALC_METHOD = clss.getDeclaredMethod("_calcFuzzyRangeRelativeImportance", cArg2);
			_BOOLEAN_RI_CALC_METHOD = clss.getDeclaredMethod("_calcBooleanRelativeImportance", cArg);
			_UNORDERED_SET_RI_CALC_METHOD = clss.getDeclaredMethod("_calcUnorderedSetRelativeImportance", cArg);
			_LINGUISTIC_RI_CALC_METHOD = clss.getDeclaredMethod("_calcLinguisticRelativeImportance", cArg);
		}
	}
	
	// Stats Counters
	protected static int spl1 = Stats.get().nextSplit("AhpHelper.rank(): getLeafNodes");
	protected static int spl2 = Stats.get().nextSplit("AhpHelper.rank(): Leafs FOR-LOOP: total");
	protected static int spl2a = Stats.get().nextSplit("AhpHelper.rank(): Leafs FOR-LOOP: getServiceCategoryAttributeFromPreference");
	protected static int spl2b = Stats.get().nextSplit("AhpHelper.rank(): Leafs FOR-LOOP: _calculateItemWeightsForAttribute");
	protected static int spl4 = Stats.get().nextSplit("AhpHelper.rank(): Matrix multiplication");
	
	public List<AhpHelper.RankedItem> rank(RecommendationManager rm, RecommendationManager.HierarchyNode<OptimisationAttribute> topLevelGoal, HashMap<String,ConsumerPreference> criteria, ServiceDescription[] items) 
	throws IllegalAccessException, java.lang.reflect.InvocationTargetException
	{
		logger.debug("Input: model={}, criteria={}, items={}", topLevelGoal, criteria, items);
		Stats.get().startSplit(spl1);
		List<RecommendationManager.HierarchyNode<OptimisationAttribute>> leafNodes = topLevelGoal.getLeafNodes();
		Stats.get().endSplit(spl1);
		int nAttrs = leafNodes.size();
		int nItems = items.length;
		logger.debug("Attr. #: {},  Item #: {}", nAttrs, nItems);
		HashMap<String,double[]> weightsPerAttr = new HashMap<String,double[]>();
		TFN[][] comparisonMatrix = new TFN[nItems][nItems];		// this is reused between iterations
		// for every lowest-level attribute...  (each one of them corresponds to a consumer preference/criterion)
		int iter = 0;
		Stats.get().startSplit(spl2);
		for (RecommendationManager.HierarchyNode<OptimisationAttribute> leaf : leafNodes) {
			logger.trace("ITER-{}: LOOP BEGIN", iter);
			// get a corresponding preference and extract TYPE, allowed values, mandatory flag etc
			String attrId = leaf.getAttribute().getId();
			logger.trace("ITER: attribute={}", attrId);
			ConsumerPreference pref = criteria.get(attrId);
			// get ServiceCategoryAttribute
			Stats.get().startSplit(spl2a);
			ServiceCategoryAttribute sca = eu.brokeratcloud.rest.opt.ServiceCategoryAttributeManagementService.getServiceCategoryAttributeFromPreference(pref);
			Stats.get().endSplit(spl2a);

			double weight = pref.getWeight();
			boolean mandatory = pref.getMandatory();
			ConsumerPreferenceExpression expr = pref.getExpression();
			if (expr!=null && ( !expr.isValid() || expr.isComment() || expr.getExpression()==null || expr.getExpression().trim().isEmpty() )) expr = null;
			logger.trace("ITER: weight={}, mandatory={}, sca={}", weight, mandatory, sca);
			
			// calculate item weights for attribute
			Stats.get().startSplit(spl2b);
			double[] w = _calculateItemWeightsForAttribute(sca, weight, mandatory, expr, items, comparisonMatrix);
			Stats.get().endSplit(spl2b);
			weightsPerAttr.put(attrId, w);
			logger.trace("ITER-{}: LOOP END", iter++);
		}
		Stats.get().endSplit(spl2);
		
		// aggregate per attribute item weights into an overall item weight vector
		logger.debug("Aggregating item weights per attribute into an overall item weights vector");
		double[][] matrix = new double[nAttrs][];
		double[] wght = new double[nAttrs];
		int ii=0;
		for (ConsumerPreference pref : criteria.values()) {
			String _id = rm._getServiceAttributeId(pref);
			logger.trace("AGGREGATION-LOOP-ITER-{}: attr-id={}", ii, _id);
			matrix[ii] = weightsPerAttr.get( _id );
			wght[ii] = pref.getWeight();		// contains the aggregated/overall weight of preference (i.e. the product of its weight and its parents' weights up to the root)
			ii++;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Item Weights Matrix:\n{}", Arrays.deepToString(matrix));
			logger.debug("Attribute Weights Vector:\n{}", Arrays.toString(wght));
		}
		// matrix multiplication
		Stats.get().startSplit(spl4);
		double[] overall = new double[nItems];
		double total = 0;
		for (int i=0; i<nItems; i++) {
			double sum = 0;
			for (int j=0; j<nAttrs; j++) {
				sum += wght[j]*matrix[j][i];
			}
			overall[i] = sum;
			total += sum;
		}
		logger.debug("Overall Weights Vector:\n{}", Arrays.toString(overall));
		// normalize result (normally this shouldn't be needed but truncation errors might result in a non-normalized vector)
		if (total>0) {
			for (int i=0; i<nItems; i++) overall[i] /= total;
		}
		logger.debug("Normalized Overall Weights Vector:\n{}", Arrays.toString(overall));
		Stats.get().endSplit(spl4);
		
		// sort items using their overall weights and rank them
		logger.debug("Sorting and ranking items...");
		ArrayList<AhpHelper.RankedItem> list = new ArrayList<AhpHelper.RankedItem>();
		for (int i=0; i<nItems; i++) list.add( new RankedItem(items[i], overall[i], i) );
		logger.trace("Items list: {}", list);
		Collections.sort(list);
		Collections.reverse(list);
		logger.trace("Sorted Items list: {}", list);
		for (int i=0; i<nItems; i++) list.get(i).order = i;
		logger.debug("Ranked Items list: {}", list);
		
		return list;
	}
	
	// Stats Counters
	protected static int splC1 = Stats.get().nextSplit("AhpHelper._calculateItemWeightsForAttribute(): IF-ELSE's");
	protected static int splC2 = Stats.get().nextSplit("AhpHelper._calculateItemWeightsForAttribute(): Extend Analysis");
	
	protected double[] _calculateItemWeightsForAttribute(ServiceCategoryAttribute sca, double weight, boolean mandatory, ConsumerPreferenceExpression expr, ServiceDescription[] items, TFN[][] comparisonMatrix) 
	throws IllegalAccessException, java.lang.reflect.InvocationTargetException
	{
		// get criterion settings (id, type, mandatory, allowed values)
		String attrId = sca.getId();		// returns apvUri
		String type = sca.getType();
		boolean higherIsBetter = sca.isHigherIsBetter();
		logger.debug("_calculateItemWeightsForAttribute: higher-is-better={}", higherIsBetter);
		logger.debug("_calculateItemWeightsForAttribute: attribute={}, type={}, weight={}, mandatory={}", attrId, type, weight, mandatory);
		
		Object allowed = null;
		Object vr = null;
		
		Stats.get().startSplit(splC1);
		// call the appropriate method for type, in order to create comparison matrix
		if (ServiceCategoryAttribute.isNumericInc(type)) {
			allowed = null;	// not used for this type
			vr = null;		// not used for this type
			_forEveryElement(_NUMERIC_INC_RI_CALC_METHOD, attrId, allowed, vr, weight, mandatory, items, comparisonMatrix, higherIsBetter);
		} else
		if (ServiceCategoryAttribute.isNumericDec(type)) {
			allowed = null;	// not used for this type
			vr = null;		// not used for this type
			_forEveryElement(_NUMERIC_DEC_RI_CALC_METHOD, attrId, allowed, vr, weight, mandatory, items, comparisonMatrix, higherIsBetter);
		} else
		if (ServiceCategoryAttribute.isNumericRange(type)) {
			NumericInterval reqInterval = null;
			String str;
			if (expr!=null) reqInterval = new NumericInterval( expr.getLowerBound(), expr.getUpperBound() );
			if (reqInterval==null && expr!=null) throw new IllegalArgumentException("Missing numeric interval constraint for attribute: "+attrId);
			allowed = null;		// not used for this type
			//
			double[] allowedVals = new double[2];
			allowedVals[0] = sca.getMin();
			allowedVals[1] = sca.getMax();
			allowed = allowedVals;
			logger.debug("Numeric Range: interval={}", reqInterval);
			_forEveryElement(_NUMERIC_RANGE_RI_CALC_METHOD, attrId, allowed, reqInterval, weight, mandatory, items, comparisonMatrix, higherIsBetter);
		} else
		//
		if (ServiceCategoryAttribute.isFuzzyInc(type)) {
			allowed = null;	// not used for this type
			vr = null;		// not used for this type
			_forEveryElement(_FUZZY_INC_RI_CALC_METHOD, attrId, allowed, vr, weight, mandatory, items, comparisonMatrix, higherIsBetter);
		} else
		if (ServiceCategoryAttribute.isFuzzyDec(type)) {
			allowed = null;	// not used for this type
			vr = null;		// not used for this type
			_forEveryElement(_FUZZY_DEC_RI_CALC_METHOD, attrId, allowed, vr, weight, mandatory, items, comparisonMatrix, higherIsBetter);
		} else
		if (ServiceCategoryAttribute.isFuzzyRange(type)) {
			TFuzzyInterval reqInterval = null;
			String str;
			if (expr!=null) reqInterval = new TFuzzyInterval( expr.getFLowerBound(), expr.getFUpperBound() );
			if (reqInterval==null && expr!=null) throw new IllegalArgumentException("Missing fuzzy interval constraint for attribute: "+attrId);
			allowed = null;		// not used for this type
			//
			TFN[] allowedVals = new TFN[2];
			allowedVals[0] = sca.getFmin();
			allowedVals[1] = sca.getFmax();
			allowed = allowedVals;
			//
			logger.debug("Fuzzy Range: interval={}", reqInterval);
			_forEveryElement(_FUZZY_RANGE_RI_CALC_METHOD, attrId, allowed, reqInterval, weight, mandatory, items, comparisonMatrix, higherIsBetter);
		} else
		//
		if (ServiceCategoryAttribute.isBoolean(type)) {
			allowed = null;		// not used for this type
			vr = null;			// not used for this type
			logger.debug("Boolean: no constraints applicable");
			_forEveryElement(_BOOLEAN_RI_CALC_METHOD, attrId, allowed, vr, weight, mandatory, items, comparisonMatrix, higherIsBetter);
		} else
		//
		if (ServiceCategoryAttribute.isUnorderedSet(type)) {
			String str;
			String[] elems = null;
			if (expr!=null) elems = expr.getElements();
			if (elems!=null) for (int i=0, n=elems.length; i<n; i++) elems[i] = elems[i].trim();
			allowed = sca.getMembers();		// not used for this type
			logger.debug("Unordered Set: elements={}", elems);
			_forEveryElement(_UNORDERED_SET_RI_CALC_METHOD, attrId, allowed, elems, weight, mandatory, items, comparisonMatrix, higherIsBetter);
		} else
		//
		if (ServiceCategoryAttribute.isLinguistic(type)) {
			HashMap<String,TFN> pairs = new HashMap<String,TFN>();
			boolean _autoCalcValues = false;
			String[] terms = sca.getTerms();
			for (int i=0,n=terms.length; i<n; i++) {
				int p = terms[i].indexOf(":");
				if (p!=-1) {
					String term = terms[i].substring(0,p).trim();
					TFN value = TFN.valueOf(terms[i].substring(p+1));
					pairs.put(term, value);
				} else {
					_autoCalcValues = true;
					break;
				}
			}
			if (_autoCalcValues) {
				pairs.clear();
				double step = 10 / terms.length;
				double mean = 1;
				for (int i=0,n=terms.length; i<n; i++) {
					int p = terms[i].indexOf(":");
					String term = (p!=-1) ? terms[i].substring(0,p).trim() : terms[i].trim();
					TFN value = new TFN(Math.max(mean-step,1), mean, Math.min(mean+step,10));
					mean += step;
					pairs.put(term, value);
				}
			}
			vr = null;	// not used for this type
			logger.debug("Linguistic terms: auto-calc-values={}, term/value pairs={}", _autoCalcValues, pairs);
			_forEveryElement(_LINGUISTIC_RI_CALC_METHOD, attrId, pairs /*allowed*/, vr, weight, mandatory, items, comparisonMatrix, higherIsBetter);
		} else {
			throw new IllegalArgumentException("Invalid Service Category Attribute type: "+sca.getType());
		}
		Stats.get().endSplit(splC1);
		// debug print
		if (logger.isDebugEnabled()) {
			logger.debug("Comparison Matrix:\n{}", Arrays.deepToString(comparisonMatrix));
		}
		
		// calculate eigenvector using extend analysis technique
		logger.trace("Extend Analysis: starting...");
		Stats.get().startSplit(splC2);
		double[] eigenvector = _extendAnalysis2(comparisonMatrix);
		Stats.get().endSplit(splC2);
		logger.debug("Extend Analysis: eigenvector: {}", Arrays.toString(eigenvector));
		
		return eigenvector;
	}
	
	//Stats Counters
	protected static int splFEE1 = Stats.get().nextSplit("AhpHelper._forEveryElement(): Inner FOR-LOOP");
	protected static int splFEE2a = Stats.get().nextSplit("AhpHelper._forEveryElement(): getServiceAttributeValue");
	protected static int splFEE2 = Stats.get().nextSplit("AhpHelper._forEveryElement(): INVOCATION");
	protected static int splFEE3 = Stats.get().nextSplit("AhpHelper._forEveryElement(): invert CM");
	
	protected void _forEveryElement(Method m, String attrId, Object allowed, Object vr, double weight, boolean mandatory, ServiceDescription[] items, TFN[][] comparisonMatrix, boolean higherIsBetter) 
	throws IllegalAccessException, java.lang.reflect.InvocationTargetException
	{
		if (logger.isTraceEnabled()) logger.trace("_forEveryElement: method={}, attr-id={}", m.getName(), attrId);
		int nItems = items.length;
		for (int i=0; i<nItems; i++) comparisonMatrix[i][i] = TFN.one();	// set the diagonal to '1'
		logger.trace("_forEveryElement: comparison matrix diagonal elements were set to fuzzy 1");
		for (int i=0, N=nItems-1; i<N; i++) {
			ServiceDescription item1 = items[i];
			Object v1 = item1.getServiceAttributeValue(attrId);
			Stats.get().startSplit(splFEE1);
			for (int j=i+1; j<nItems; j++) {
				Stats.get().startSplit(splFEE2a);
				ServiceDescription item2 = items[j];
				Object v2 = item2.getServiceAttributeValue(attrId);
				if (logger.isTraceEnabled()) logger.trace("_forEveryElement:\t elem[{}][{}]: item1={}, value1={}, item2={}, value2={}, method={}", i, j, item1.getName(), v1, item2.getName(), v2, m.getName());
				Stats.get().endSplit(splFEE2a);
				Stats.get().startSplit(splFEE2);
				if (m!=_NUMERIC_RANGE_RI_CALC_METHOD && m!=_FUZZY_RANGE_RI_CALC_METHOD) {
					comparisonMatrix[i][j] = (TFN)m.invoke(this, v1, v2, vr, weight, mandatory, allowed);
				} else {
					comparisonMatrix[i][j] = (TFN)m.invoke(this, v1, v2, vr, weight, mandatory, allowed, higherIsBetter);
				}
				Stats.get().endSplit(splFEE2);
				Stats.get().startSplit(splFEE3);
				logger.debug("_forEveryElement: comparison-matrix[{}][{}] = {}", i, j, comparisonMatrix[i][j]);
				comparisonMatrix[j][i] = comparisonMatrix[i][j].inv();
				logger.debug("_forEveryElement: comparison-matrix[{}][{}] = {}", j, i, comparisonMatrix[j][i]);
				Stats.get().endSplit(splFEE3);
			}
			Stats.get().endSplit(splFEE1);
		}
	}
	
	// Numeric Types (inc/dec/range)
	protected TFN _calcNumericIncRelativeImportance(Object val1, Object val2, Object valReq, double weight, boolean mandatory, Object allowed) {
		Double d1 = null, d2 = null;
		String str;
		if (val1!=null && !(str=val1.toString().trim()).isEmpty()) d1 = Double.valueOf(str);
		if (val2!=null && !(str=val2.toString().trim()).isEmpty()) d2 = Double.valueOf(str);
		if (d1!=null && d2!=null && d2.doubleValue()!=0) return new TFN(d1.doubleValue()/d2.doubleValue());
		else if (d1!=null && d2==null && d1.doubleValue()!=0) return new TFN(1/weight);
		else if (d1==null && d2!=null && d2.doubleValue()!=0) return new TFN(weight);
		else return TFN.one();
	}
	protected TFN _calcNumericDecRelativeImportance(Object val1, Object val2, Object valReq, double weight, boolean mandatory, Object allowed) {
		Double d1 = null, d2 = null;
		String str;
		if (val1!=null && !(str=val1.toString().trim()).isEmpty()) d1 = Double.valueOf(str);
		if (val2!=null && !(str=val2.toString().trim()).isEmpty()) d2 = Double.valueOf(str);
		if (d1!=null && d2!=null && d1.doubleValue()!=0) return new TFN(d2.doubleValue()/d1.doubleValue());
		else if (d1!=null && d2==null && d1.doubleValue()!=0) return new TFN(1/weight);
		else if (d1==null && d2!=null && d2.doubleValue()!=0) return new TFN(weight);
		else return TFN.one();
	}
	protected TFN _calcNumericRangeRelativeImportance(Object val1, Object val2, Object valReq, double weight, boolean mandatory, Object allowed, boolean higherIsBetter) {
		NumericInterval i1 = null, i2 = null, ir = (NumericInterval)valReq;
		String str;
		if (val1!=null && !(str=val1.toString().trim()).isEmpty()) i1 = NumericInterval.valueOf(str);
		if (val2!=null && !(str=val2.toString().trim()).isEmpty()) i2 = NumericInterval.valueOf(str);
		
		TFN result = null;
		if (i1!=null && i2!=null || mandatory) result = null;
		else if (i1==null && i2!=null) result = new TFN(weight);
		else if (i1!=null  && i2==null) result = new TFN(1/weight);
		else if (i1==null && i2==null) result = TFN.one();
		
		if (result!=null) {
			return result;
		}
		
		double[] vals = (double[])allowed;
		double min = vals[0];
		double max = vals[1];
			double lb1 = i1!=null ? i1.getLowerBound() : Double.NEGATIVE_INFINITY;
			double lb2 = i2!=null ? i2.getLowerBound() : Double.NEGATIVE_INFINITY;
			result = new TFN( (max-lb2)/(max-lb1) );
		return result;
	}
	// Fuzzy Types (inc/dec/range)
	protected TFN _calcFuzzyIncRelativeImportance(Object val1, Object val2, Object valReq, double weight, boolean mandatory, Object allowed) {
		TFN t1 = null, t2 = null;
		String str;
		if (val1!=null && !(str=val1.toString().trim()).isEmpty()) t1 = TFN.valueOf(str);
		if (val2!=null && !(str=val2.toString().trim()).isEmpty()) t2 = TFN.valueOf(str);
		if (t1!=null && t2!=null) return t1.div(t2);
		else if (t1!=null && t2==null) return new TFN(1/weight);
		else if (t1==null && t2!=null) return new TFN(weight);
		else return TFN.one();
	}
	protected TFN _calcFuzzyDecRelativeImportance(Object val1, Object val2, Object valReq, double weight, boolean mandatory, Object allowed) {
		TFN t1 = null, t2 = null;
		String str;
		if (val1!=null && !(str=val1.toString().trim()).isEmpty()) t1 = TFN.valueOf(str);
		if (val2!=null && !(str=val2.toString().trim()).isEmpty()) t2 = TFN.valueOf(str);
		if (t1!=null && t2!=null) return t2.div(t1);
		else if (t1!=null && t2==null) return new TFN(1/weight);
		else if (t1==null && t2!=null) return new TFN(weight);
		else return TFN.one();
	}
	protected TFN _calcFuzzyRangeRelativeImportance(Object val1, Object val2, Object valReq, double weight, boolean mandatory, Object allowed, boolean higherIsBetter) {
		TFuzzyInterval i1 = null, i2 = null, ir = (TFuzzyInterval)valReq;
		String str;
		if (val1!=null && !(str=val1.toString().trim()).isEmpty()) i1 = TFuzzyInterval.valueOf(str);
		if (val2!=null && !(str=val2.toString().trim()).isEmpty()) i2 = TFuzzyInterval.valueOf(str);
		
		TFN result = null;
		if (i1!=null && i2!=null || mandatory) result = null;
		else if (i1==null && i2!=null) result = new TFN(weight);
		else if (i1!=null  && i2==null) result = new TFN(1/weight);
		else if (i1==null && i2==null) result = TFN.one();
		
		if (result!=null) {
			return result;
		}
		
		TFN[] vals = (TFN[])allowed;
		double min = vals[0].getLowerBound();
		double max = vals[1].getUpperBound();
			double lb1 = i1!=null ? i1.getLowerBound() : Double.NEGATIVE_INFINITY;
			double lb2 = i2!=null ? i2.getLowerBound() : Double.NEGATIVE_INFINITY;
			result = new TFN( (max-lb2)/(max-lb1) );
		return result;
	}
	// Boolean Type
	protected TFN _calcBooleanRelativeImportance(Object val1, Object val2, Object valReq, double weight, boolean mandatory, Object allowed) {
		boolean b1 = false, b2 = false;
		String str;
		if (val1!=null && !(str=val1.toString().trim()).isEmpty()) b1 = Boolean.parseBoolean(str);
		if (val2!=null && !(str=val2.toString().trim()).isEmpty()) b2 = Boolean.parseBoolean(str);
		
		if (b1==b2) return TFN.one();
		else if (b2==true  && b1==false) return new TFN(weight);
		else if (b2==false && b1==true)  return new TFN(1/weight);
		else return TFN.one();
	}
	// Unordered Set Type
	protected TFN _calcUnorderedSetRelativeImportance(Object val1, Object val2, Object valReq, double weight, boolean mandatory, Object allowed) {
		String[] l1 = null, l2 = null, lr=(String[])valReq;
		String str;
		if (val1!=null && !(str=val1.toString().trim()).isEmpty()) l1 = str.split("[,]"); else l1 = new String[0];
		if (val2!=null && !(str=val2.toString().trim()).isEmpty()) l2 = str.split("[,]"); else l2 = new String[0];
		logger.trace("item1: unorder list size: {}", l1.length);
		logger.trace("item2: unorder list size: {}", l2.length);
		
		if (mandatory) {
			logger.trace("mandatory attribute: => KPI = size1/size2 = {}", l1.length/l2.length);
			return new TFN(l1.length/l2.length);
		} else {
			for (int i=0, n=l1.length; i<n; i++) l1[i] = l1[i].trim();
			for (int i=0, n=l2.length; i<n; i++) l2[i] = l2[i].trim();
			double s1 = _joinUL(l1, lr);
			double s2 = _joinUL(l2, lr);
			if (lr!=null && logger.isTraceEnabled()) {
				logger.trace("required list: {}", Arrays.toString(lr));
				logger.trace("required list size: {}", lr.length);
				logger.trace("item1 list JOIN required list - size: {}", s1);
				logger.trace("item2 list JOIN required list - size: {}", s2);
				logger.trace("NON-mandatory attribute: => KPI = join1/join2 = {} / {}", s1, s2);
			}
			if (s1>0 && s2>0) return new TFN(s1/s2);
			else if (s1==0 && s2>0) return new TFN(weight);
			else if (s1>0 && s2==0) return new TFN(1/weight);
			else return TFN.one();
		}
	}
	protected int _joinUL(String[] l1, String l2[]) {
		if (l1==null || l2==null) return 0;
		int cnt=0;
		int n1=l1.length;
		int n2=l2.length;
		for (int i=0; i<n1; i++) {
			for (int j=0; j<n2; j++) {
				if (l1[i].equalsIgnoreCase(l2[j])) cnt++;
			}
		}
		return cnt;
	}
	// Linguistic Type
	protected TFN _calcLinguisticRelativeImportance(Object val1, Object val2, Object valReq, double weight, boolean mandatory, Object allowed) {
		HashMap<String,TFN> terms = null;
		if (allowed instanceof HashMap) terms = (HashMap<String,TFN>)allowed;
		
		TFN t1 = null, t2 = null;
		String str;
		if (val1!=null && !(str=val1.toString().trim()).isEmpty()) t1 = terms.get(str);
		if (val2!=null && !(str=val2.toString().trim()).isEmpty()) t2 = terms.get(str);
		
		if (t1!=null && t2!=null) return t1.div(t2);
		else if (t1!=null && t2==null) return new TFN(1/weight);
		else if (t1==null && t2!=null) return new TFN(weight);
		else return TFN.one();
	}
	
	// Implementation of extend analysis
	protected static double[] _extendAnalysis2(TFN[][] matrix) {
		// defussify comparison matrix
		int N = matrix.length;
		double[][] matrix2 = new double[N][N];
		for (int i=0; i<N; i++) {
			for (int j=0; j<N; j++) {
				if (i==j) matrix2[i][j] = 1;
				else matrix2[i][j] = matrix[i][j].defuzzify();
			}
		}
		
		// calculate crisp eigenvector
		double[][] m1 = matrix2;
		double[] ev1 = _calcCrispEigenvector(m1);
		double[] ev2;
		int iter = 0;
		double diff;
		double maxDiff = 0.01;
		do {
			double[][] m2 = _squareCrispMatrix(m1);
			ev2 = _calcCrispEigenvector(m2);
			diff = _diffCrispVectors(ev1, ev2);
			if (diff<maxDiff) break;
			m1 = m2;
			ev1 = ev2;
			iter++;
		} while (iter<100);
		if (diff<maxDiff) return ev2;
		return null;
	}
	protected static double[] _calcCrispEigenvector(double[][] matrix) {
		int N = matrix.length;
		double[] rowsum = new double[N];
		double total = 0;
		// calculate row sums
		for (int i=0; i<N; i++) {
			double sum = 0;
			for (int j=0; j<N; j++) sum += matrix[i][j];
			rowsum[i] = sum;
			total += sum;
		}
		// normalize row sums
		for (int i=0; i<N; i++) rowsum[i] /= total;
		logger.trace(Arrays.toString(rowsum));
		return rowsum;
	}
	protected static double[][] _squareCrispMatrix(double[][] src) {
		int N = src.length;
		double[][] square = new double[N][N];
		for (int i=0; i<N; i++)
		for (int j=0; j<N; j++) {
			double sum = 0;
			for (int k=0; k<N; k++) sum += src[i][k]*src[k][j];
			square[i][j] = sum;
		}
		return square;
	}
	protected static double _diffCrispVectors(double[] v1, double[] v2) {
		int N = v1.length;
		double diff = 0;
		for (int i=0; i<N; i++) diff += Math.abs(v1[i]-v2[i]);
		return diff;
	}
	
	public static class RankedItem implements Comparable<RankedItem> {
		public ServiceDescription item;
		public double relevance;
		public int order;
		
		public RankedItem(ServiceDescription item, double relevance, int order) {
			this.item = item; this.relevance = relevance; this.order = order;
		}
		
		public int compareTo(RankedItem o) {
			double diff = this.relevance - o.relevance;
			return diff<0 ? -1 : diff>0 ? +1 : 0;
		}
		
		public String toString() {
			return new StringBuffer("{ item=").append(item).append(", relevance=").append(relevance).append(", order=").append(order).append(" }").toString();
		}
	}
}
