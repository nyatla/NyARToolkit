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
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.homography_estimation;

import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.Point2d;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.Utils;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.match.indexing;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers.homography;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math.geometry;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math.math_utils;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math.rand;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.utils.parcial_sort;

/**
 * Robust homography estimation.
 */
public class RobustHomography {
	final static double HOMOGRAPHY_DEFAULT_CAUCHY_SCALE = 0.01f;
	final static int HOMOGRAPHY_DEFAULT_NUM_HYPOTHESES = 1024;
	final static int HOMOGRAPHY_DEFAULT_MAX_TRIALS = 1064;
	final static int HOMOGRAPHY_DEFAULT_CHUNK_SIZE = 50;

	public RobustHomography() {
		this(HOMOGRAPHY_DEFAULT_CAUCHY_SCALE,
				HOMOGRAPHY_DEFAULT_NUM_HYPOTHESES,
				HOMOGRAPHY_DEFAULT_MAX_TRIALS, HOMOGRAPHY_DEFAULT_CHUNK_SIZE);
	}

	public RobustHomography(double cauchyScale, int maxNumHypotheses,
			int maxTrials, int chunkSize) {
		this.init(cauchyScale, maxNumHypotheses, maxTrials, chunkSize);
	}

	/**
	 * Initalize the RANSAC parameters.
	 */
	private void init(double cauchyScale, int maxNumHypotheses, int maxTrials,
			int chunkSize) {
		this.mHyp = new double[9 * maxNumHypotheses];
		this.mHypCosts = CostPair.createArray(maxNumHypotheses);

		mCauchyScale = cauchyScale;
		mMaxNumHypotheses = maxNumHypotheses;
		mMaxTrials = maxTrials;
		mChunkSize = chunkSize;
	}

	//
	// private:
	//
	// // Temporary memory for RANSAC
	private double[] mHyp;
	private CostPair[] mHypCosts;
	private int[] mTmpi;

	// std::vector< std::pair<T, int> > mHypCosts;
	public static class CostPair {
		public double first;
		public int second;
		public static CostPair[] createArray(int i_size)
		{
			CostPair[] r=new CostPair[i_size];
			for(int i=0;i<i_size;i++){
				r[i]=new CostPair();
			}
			return r;
		}
		public static boolean operator_lt(CostPair _Left, CostPair _Right) { // test
																				// if
																				// _Left
																				// <
																				// _Right
																				// for
																				// pairs
			return (_Left.first < _Right.first || (!(_Right.first < _Left.first) && _Left.second < _Right.second));
		}

	}

	// RANSAC params
	double mCauchyScale;
	int mMaxNumHypotheses;
	int mMaxTrials;
	int mChunkSize;

	/**
	 * Find the homography from a set of 2D correspondences.
	 * p,q,test_pointshaは0位置固定
	 */
	public // boolean find(float H[9], const T* p, const T* q, int num_points, const T*
	// test_points, int num_test_points) {

	boolean find(double[] H, Point2d[] p, Point2d[] q, int num_points,
			Point2d[] test_points, int num_test_points) {
		mTmpi = new int[2 * num_points];
		return PreemptiveRobustHomography(H, p, q, num_points, test_points,
				num_test_points, mHyp, mTmpi, mHypCosts, mCauchyScale,
				mMaxNumHypotheses, mMaxTrials, mChunkSize);
	}

	/**
	 * Robustly solve for the homography given a set of correspondences.
	 */
	// public boolean PreemptiveRobustHomography(float H[9],
	// const T* p,const T* q,
	// int num_points,const T* test_points,int num_test_points,
	// std::vector<T> &hyp /* 9*max_num_hypotheses */,
	// std::vector<int> &tmp_i /* 2*num_points */,
	// std::vector< std::pair<T, int> > &hyp_costs /* max_num_hypotheses */,
	// T scale = HOMOGRAPHY_DEFAULT_CAUCHY_SCALE,
	// int max_num_hypotheses = HOMOGRAPHY_DEFAULT_NUM_HYPOTHESES,
	// int max_trials = HOMOGRAPHY_DEFAULT_MAX_TRIALS,
	// int chunk_size = HOMOGRAPHY_DEFAULT_CHUNK_SIZE) {
	public boolean PreemptiveRobustHomography(double[] H, Point2d[] p,
			Point2d[] q, int num_points, Point2d[] test_points,
			int num_test_points, double[] hyp /* 9*max_num_hypotheses */,
			int[] tmp_i /* 2*num_points */,
			CostPair[] hyp_costs /* max_num_hypotheses */, double scale,
			int max_num_hypotheses, int max_trials, int chunk_size) {
		int hyp_perm;// ptr表現
		int point_perm;// ptr表現
		double one_over_scale2;
		double min_cost;
		int num_hypotheses, num_hypotheses_remaining, min_index;
		int cur_chunk_size, next_chunk, this_chunk_end;
		int trial;
		int seed;
		int sample_size = 4;

		// ASSERT(hyp.size() >= 9*max_num_hypotheses,
		// "hyp vector should be of size 9*max_num_hypotheses");
		// ASSERT(tmp_i.size() >= 2*num_points,
		// "tmp_i vector should be of size 2*num_points");
		// ASSERT(hyp_costs.size() >= max_num_hypotheses,
		// "hyp_costs vector should be of size max_num_hypotheses");

		// We need at least SAMPLE_SIZE points to sample from
		if (num_points < sample_size) {
			return false;
		}
		// seed = 1234;
		rand random = new rand(1234);

		hyp_perm = 0;
		point_perm = num_points;

		one_over_scale2 = 1 / math_utils.sqr(scale);
		chunk_size = math_utils.min2(chunk_size, num_points);
		next_chunk = 0;

		// Fill two arrays from [0,num_points)
		indexing.SequentialVector(tmp_i, hyp_perm, num_points, 0);
		indexing.SequentialVector(tmp_i, point_perm, num_points, 0);

		// Shuffle the indices
		random.ArrayShuffle(tmp_i, hyp_perm, num_points, num_points);
//		random.ArrayShuffle(tmp_i, point_perm, num_points, num_points);//この行を有効化するとテスト結果値は962になる。

		// Compute a set of hypotheses
		for (trial = 0, num_hypotheses = 0; trial < max_trials
				&& num_hypotheses < max_num_hypotheses; trial++) {

			// Shuffle the first SAMPLE_SIZE indices
			random.ArrayShuffle(tmp_i, hyp_perm, num_points, sample_size);

			// Check if the four points are geometrically valid
			if (!geometry.Homography4PointsGeometricallyConsistent(
					p[tmp_i[hyp_perm + 0]], p[tmp_i[hyp_perm + 1]],
					p[tmp_i[hyp_perm + 2]], p[tmp_i[hyp_perm + 3]],
					q[tmp_i[hyp_perm + 0]], q[tmp_i[hyp_perm + 1]],
					q[tmp_i[hyp_perm + 2]], q[tmp_i[hyp_perm + 3]])) {
				continue;
			}/*
			 * boolean SolveHomography4Points(float[] H, float[] x1, float[] x2,
			 * float[] x3, float[] x4, float[] xp1, float[] xp2, float[] xp3,
			 * float[] xp4) {
			 */
			double[] Ht = new double[9];
			// Compute the homography
			if (!homography_solver.SolveHomography4Points(Ht,
					p[tmp_i[hyp_perm + 0]], p[tmp_i[hyp_perm + 1]],
					p[tmp_i[hyp_perm + 2]], p[tmp_i[hyp_perm + 3]],
					q[tmp_i[hyp_perm + 0]], q[tmp_i[hyp_perm + 1]],
					q[tmp_i[hyp_perm + 2]], q[tmp_i[hyp_perm + 3]])) {
				continue;
			}
			System.arraycopy(Ht, 0, hyp, num_hypotheses * 9, 9);

			// Check the test points
			if (num_test_points > 0) {
				double[] hyps = Utils.arraysubset(hyp, num_hypotheses * 9, 9);
				if (!homography.HomographyPointsGeometricallyConsistent(hyps,
						test_points, 0, num_test_points)) {
					continue;
				}
			}

			num_hypotheses++;
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
		cur_chunk_size = chunk_size;

		for (int i = 0; i < num_points && num_hypotheses_remaining > 2; i += cur_chunk_size) {

			// Size of the current chunk
			cur_chunk_size = math_utils.min2(chunk_size, num_points - i);

			// End of the current chunk
			this_chunk_end = i + cur_chunk_size;

			// Score each of the remaining hypotheses
			for (int j = 0; j < num_hypotheses_remaining; j++) {
				// const T* H_cur = &hyp[hyp_costs[j].second*9];
				int H_cur = hyp_costs[j].second * 9;
				for (int k = i; k < this_chunk_end; k++) {
					hyp_costs[j].first += CauchyProjectiveReprojectionCost(Utils.arraysubset(hyp, H_cur, 9), p[tmp_i[hyp_perm+ k]], q[tmp_i[hyp_perm + k]],one_over_scale2);
				}
			}

			// Cut out half of the hypotheses
			// parcial_sort.FastMedian(&hyp_costs[0], num_hypotheses_remaining);
			parcial_sort.FastMedian(hyp_costs, num_hypotheses_remaining);
			num_hypotheses_remaining = num_hypotheses_remaining >> 1;
		}

		// Find the best hypothesis
		min_index = hyp_costs[0].second;
		min_cost = hyp_costs[0].first;
		for (int i = 1; i < num_hypotheses_remaining; i++) {
			if (hyp_costs[i].first < min_cost) {
				min_cost = hyp_costs[i].first;
				min_index = hyp_costs[i].second;
			}
		}

		// Move the best hypothesis
		indexing.CopyVector(H, 0, hyp, min_index * 9, 9);
		NormalizeHomography(H);

		return true;
	}

	/**
	 * Normalize a homography such that H(3,3) = 1.
	 */
	void NormalizeHomography(double[] H) {
		double one_over =  (1. / H[8]);
		H[0] *= one_over;
		H[1] *= one_over;
		H[2] *= one_over;
		H[3] *= one_over;
		H[4] *= one_over;
		H[5] *= one_over;
		H[6] *= one_over;
		H[7] *= one_over;
		H[8] *= one_over;
	}

	double sqr(double x) {
		return x * x;
	};

	double CauchyCost(double x, double one_over_scale2) {
		return  Math.log(1. + sqr(x) * one_over_scale2);
	}

	double CauchyCost(double x0, double x1, double one_over_scale2) {
		return  Math.log(1 + (x0 * x0 + x1 * x1) * one_over_scale2);
	}

	double CauchyCost(double[] x, double one_over_scale2) {
		return CauchyCost(x[0], x[1], one_over_scale2);
	}

	/**
	 * Compute the Cauchy reprojection cost for H*p-q.
	 */
	// float CauchyProjectiveReprojectionCost(float H[9], const T p[2], const T
	// q[2], T one_over_scale2) {
	double CauchyProjectiveReprojectionCost(double[] H, Point2d p, Point2d q,
			double one_over_scale2) {
		double[] pp = new double[2];
		double[] f = new double[2];

		// homography.MultiplyPointHomographyInhomogenous(pp[0], pp[1], H, p[0],
		// p[1]);
		homography.MultiplyPointHomographyInhomogenous(pp, H, p.x, p.y);

		f[0] = pp[0] - q.x;
		f[1] = pp[1] - q.y;

		return CauchyCost(f, one_over_scale2);
	}

	/**
	 * Compute the Cauchy reprojection cost for H*p_i-q_i.
	 */
	double CauchyProjectiveReprojectionCost(double[] H, Point2d p[], Point2d q[],
			int num_points, double one_over_scale2) {
		int i;
		double total_cost;
		int ptr = 0;
		total_cost = 0;
		for (i = 0; i < num_points; i++, ptr += 1) {
			total_cost += CauchyProjectiveReprojectionCost(H, p[ptr], q[ptr],
					one_over_scale2);
		}

		return total_cost;
	}
}
