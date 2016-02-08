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
package eu.brokeratcloud.opt.ahp;

import eu.brokeratcloud.opt.type.TFN;
import java.util.Arrays;

public class AhpHelperEV extends AhpHelper {
	public AhpHelperEV() throws NoSuchMethodException {
		super();
	}
	
	protected double[] _extendAnalysis(TFN[][] matrix) {
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
}
