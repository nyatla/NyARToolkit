/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2013-2015 Daqri, LLC.
 *  Author(s): Chris Broaddus
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 *  Copyright (C)2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.core.kpm.matcher.homography_estimation;

import jp.nyatla.nyartoolkit.core.kpm.matcher.FeaturePairStack;
import jp.nyatla.nyartoolkit.core.kpm.matcher.HomographyMat;


/**
 * Robust homography estimation.
 */
public class RobustHomography {
	final static double HOMOGRAPHY_DEFAULT_CAUCHY_SCALE = 0.01f;
	final static int HOMOGRAPHY_DEFAULT_NUM_HYPOTHESES = 1024;
	final static int HOMOGRAPHY_DEFAULT_MAX_TRIALS = 1064;
	final static int HOMOGRAPHY_DEFAULT_CHUNK_SIZE = 50;

	public RobustHomography()
	{
		this(HOMOGRAPHY_DEFAULT_CAUCHY_SCALE, HOMOGRAPHY_DEFAULT_NUM_HYPOTHESES, HOMOGRAPHY_DEFAULT_MAX_TRIALS,HOMOGRAPHY_DEFAULT_CHUNK_SIZE);
	}

	public RobustHomography(double cauchyScale, int maxNumHypotheses, int maxTrials, int chunkSize)
	{
		this.mHyp = new HomographyMat[maxNumHypotheses];
		for(int i=0;i<this.mHyp.length;i++){
			this.mHyp[i]=new HomographyMat();
		}
		this.mHypCosts = CostPair.createArray(maxNumHypotheses);

		this.mCauchyScale = cauchyScale;
		this.mMaxNumHypotheses = maxNumHypotheses;
		this.mMaxTrials = maxTrials;
		this.mChunkSize = chunkSize;
	}



	//
	// private:
	//
	// Temporary memory for RANSAC
	final private HomographyMat[] mHyp;
	final private CostPair[] mHypCosts;


	// std::vector< std::pair<T, int> > mHypCosts;
	public static class CostPair {
		public double first;
		public int second;

		public static CostPair[] createArray(int i_size) {
			CostPair[] r = new CostPair[i_size];
			for (int i = 0; i < i_size; i++) {
				r[i] = new CostPair();
			}
			return r;
		}

		// test if_Left<_Right for pairs
		public static boolean operator_lt(CostPair _Left, CostPair _Right) {
			return (_Left.first < _Right.first || (!(_Right.first < _Left.first) && _Left.second < _Right.second));
		}

	}

	// RANSAC params
	final private double mCauchyScale;
	final private int mMaxNumHypotheses;
	final private int mMaxTrials;
	final private int mChunkSize;


	final private RansacPointTable _ps=new RansacPointTable(100);
	
	final private HomographySolver _homography_solver=new HomographySolver_O1();
	final private HomographyPointsCheck _homography_check=new HomographyPointsCheck_O1();
	/**
	 * Robustly solve for the homography given a set of correspondences.
	 * p=src=ref,q=dest=query
	 */
	public boolean PreemptiveRobustHomography(HomographyMat H, FeaturePairStack matches,double i_width,double i_height)
	{
		RansacPointTable ps=this._ps;
//		int[] tmp_i=new int[2 * num_points];
		HomographyMat[] hyp=this.mHyp;/* 9*max_num_hypotheses */
		CostPair[] hyp_costs=this.mHypCosts;
		double scale=this.mCauchyScale;
		int max_num_hypotheses=this.mMaxNumHypotheses;
		int max_trials=this.mMaxTrials;
		
		
		double one_over_scale2 = 1 / (scale*scale);

		int num_hypotheses_remaining;
		int cur_chunk_size, this_chunk_end;
		int sample_size = 4;
		this._homography_check.setTestWindow(i_width, i_height);
		
		int num_points=matches.getLength();

		// We need at least SAMPLE_SIZE points to sample from
		if (num_points < sample_size) {
			return false;
		}
		
		// seed = 1234;
		ps.setSequential(matches.getArray(),num_points);
		ps.setSeed(1234);
		// Shuffle the indices
		ps.shuffle(num_points);

		int num_hypotheses=0;
		// Compute a set of hypotheses
		for (int trial = 0; trial < max_trials; trial++) {

			// Shuffle the first SAMPLE_SIZE indices
			ps.shuffle(sample_size);

			// Check if the 4 points are geometrically valid
			if(!ps.geometricallyConsistent4Point()){
				continue;
			}

			// Compute the homography
			if (!this._homography_solver.solveHomography4Points(ps.indics[0],ps.indics[1],ps.indics[2],ps.indics[3],hyp[num_hypotheses])) {
				continue;
			}
			// Check the test points
			if(!this._homography_check.geometricallyConsistent(hyp[num_hypotheses])){
				continue;
			}
			num_hypotheses++;
			if(num_hypotheses==max_num_hypotheses){
				break;
			}
		}

		// We fail if no hypotheses could be computed
		if (num_hypotheses == 0) {
			return false;
		}

		// Initialize the hypotheses costs
		for (int i = 0; i < num_hypotheses; i++) {
			hyp_costs[i].first = 0;
			hyp_costs[i].second = i;
		}

		num_hypotheses_remaining = num_hypotheses;

		//チャンクサイズの決定
		int chunk_size=this.mChunkSize;
		if(chunk_size>num_points){
			chunk_size=num_points;
		}
		cur_chunk_size = chunk_size;

		for (int i = 0; i < num_points && num_hypotheses_remaining > 2; i += cur_chunk_size) {

			// Size of the current chunk
			cur_chunk_size = (chunk_size<num_points - i)?chunk_size:(num_points - i);

			// End of the current chunk
			this_chunk_end = i + cur_chunk_size;

			// Score each of the remaining hypotheses
			for (int j = 0; j < num_hypotheses_remaining; j++) {
				// const T* H_cur = &hyp[hyp_costs[j].second*9];
				double hf=hyp[hyp_costs[j].second].cauchyProjectiveReprojectionCostSum(ps.indics, i,this_chunk_end,one_over_scale2);
				hyp_costs[j].first=hf;
			}
			// Cut out half of the hypotheses
			FastMedian(hyp_costs, num_hypotheses_remaining);
			num_hypotheses_remaining = num_hypotheses_remaining >> 1;
		}

		// Find the best hypothesis
		int min_index = hyp_costs[0].second;
		double min_cost = hyp_costs[0].first;
		for (int i = 1; i < num_hypotheses_remaining; i++) {
			if (hyp_costs[i].first < min_cost) {
				min_cost = hyp_costs[i].first;
				min_index = hyp_costs[i].second;
			}
		}

		// Move the best hypothesis
		H.setValue(hyp[min_index]);
		H.normalizeHomography();

		return true;
	}
	
	

	
    private static RobustHomography.CostPair PartialSort(RobustHomography.CostPair a[], int n, int k) {
		int i, j, l, m, k_minus_1;
		RobustHomography.CostPair x;

       
        k_minus_1 = k-1;
		
		l=0 ; m=n-1;
		while(l<m) {
			x=a[k_minus_1];
			i=l;
			j=m;
			do {
				while(RobustHomography.CostPair.operator_lt(a[i],x)) i++;
				while(RobustHomography.CostPair.operator_lt(x,a[j])) j--;
				if(i<=j) {
					//std::swap(a[i], a[j]); // FIXME: 
					RobustHomography.CostPair t=a[i];
					a[i]=a[j];
					a[j]=t;
					i++; j--;
				}
			} while (i<=j);
			if(j<k_minus_1) l=i;
			if(k_minus_1<i) m=j;
		}
		return a[k_minus_1];
	}    
    private static RobustHomography.CostPair FastMedian(RobustHomography.CostPair a[], int n)
    {
		return PartialSort(a, n, (((n&1)==1)?((n)/2):(((n)/2)-1)));
	} 	
}
