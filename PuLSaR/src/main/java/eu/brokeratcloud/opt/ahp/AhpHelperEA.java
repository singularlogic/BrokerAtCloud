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

public class AhpHelperEA extends AhpHelper {
	public AhpHelperEA() throws NoSuchMethodException {
		super();
	}
	
	// Implementation of extend analysis
	protected double[] _extendAnalysis(TFN[][] matrix) {
		int size = matrix.length;
		double[] eigen = _calcExtendAnalysisEigenvector(matrix);
		
		double[] eigen2;
		TFN[][] matrix2 = new TFN[size][size];
		do {
			// square matrix
			_squareMatrix(matrix, matrix2);
			eigen2 = _calcExtendAnalysisEigenvector(matrix2);
			// swap matrices and eigenvectors
			double[] vtmp = eigen; eigen = eigen2; eigen2 = vtmp;
			TFN[][] mtmp = matrix; matrix = matrix2; matrix2 = mtmp;
		} while (_areVectorsDifferent(eigen, eigen2, 0.01));
		
		return eigen;
	}
	
	protected void _squareMatrix(TFN[][] M1, TFN[][] M2) {
		int size = M1.length;
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				M2[i][j] = TFN.zero();
				for (int k=0; k<size; k++) M2[i][j] = M2[i][j].add( M1[i][k].mul( M1[k][j] ) );
			}
		}
	}
	
	protected boolean _areVectorsDifferent(double[] eigen1, double[] eigen2, double threshold) {
		int size = eigen1.length;
		for (int i=0; i<size; i++) {
			double diff = eigen1[i] - eigen2[i];
			if (diff < 0) diff = -diff;
			if (logger.isTraceEnabled()) logger.trace("\tabs({} - {}) = {}", eigen1[i], eigen2[i], diff);
			if (diff>threshold) return true;
		}
		if (logger.isTraceEnabled()) logger.trace("\teigenvectors are (almost) identical");
		return false;
	}
	
	protected double[] _calcExtendAnalysisEigenvector(TFN[][] matrix) {
		// calculate fuzzy synthetic degrees D
		int N = matrix.length;
		TFN[] D = new TFN[N];	// the row sum
		TFN totSum = TFN.zero();
		for (int i=0; i<N; i++) {
			D[i] = TFN.zero();
			for (int j=0; j<N; j++) D[i] = D[i].add(matrix[i][j]);
			totSum = totSum.add(D[i]);
		}
		for (int i=0; i<N; i++) {
			D[i] = D[i].div(totSum);
		}
		
		// calculate degrees of possibilities (d) that Di is greater than all Dk, k=1..N, i<>k
		double[] d = new double[N];
		double sum = 0;
		for (int i=0; i<N; i++) {
			double min = 1;
			for (int j=0; j<N; j++) {
				double Dil = D[i].getLowerBound();
				double Djl = D[j].getLowerBound();
				double Dim = D[i].getMeanValue();
				double Djm = D[j].getMeanValue();
				double Diu = D[i].getUpperBound();
				double Dju = D[j].getUpperBound();
				double VDij;
				if (Dim >= Djm) VDij = 1;
				else if ((Dim <= Djm) && (Djl <= Diu)) {
					VDij = (Djl - Diu) / ((Dim - Diu) - (Djm - Djl));
				} else {
					VDij = 0;
				}
				if (VDij<min) min = VDij;
			}
			d[i] = min;
			sum += min;
		}
		
		// normalize d's to get eigenvector
		for (int i=0; i<N; i++) d[i] /= sum;
		
		return d;
	}
}
