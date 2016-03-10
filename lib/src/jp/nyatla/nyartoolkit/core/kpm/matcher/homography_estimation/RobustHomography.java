package jp.nyatla.nyartoolkit.core.kpm.matcher.homography_estimation;


import jp.nyatla.nyartoolkit.core.kpm.keyframe.utils.rand;
import jp.nyatla.nyartoolkit.core.kpm.matcher.HomographyMat;
import jp.nyatla.nyartoolkit.core.kpm.matcher.homography;
import jp.nyatla.nyartoolkit.core.kpm.vision.match.indexing;
import jp.nyatla.nyartoolkit.core.kpm.vision.math.geometry;
import jp.nyatla.nyartoolkit.core.kpm.vision.math.math_utils;
import jp.nyatla.nyartoolkit.core.kpm.vision.utils.parcial_sort;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

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

		mCauchyScale = cauchyScale;
		mMaxNumHypotheses = maxNumHypotheses;
		mMaxTrials = maxTrials;
		mChunkSize = chunkSize;
	}



	//
	// private:
	//
	// Temporary memory for RANSAC
	final private HomographyMat[] mHyp;
	private CostPair[] mHypCosts;
	private int[] mTmpi;

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
	double mCauchyScale;
	int mMaxNumHypotheses;
	int mMaxTrials;
	int mChunkSize;

	/**
	 * Find the homography from a set of 2D correspondences. p,q,test_pointshaは0位置固定
	 */
	public boolean find(HomographyMat H, NyARDoublePoint2d[] p, NyARDoublePoint2d[] q, int num_points,
			NyARDoublePoint2d[] test_points, int num_test_points) {
		mTmpi = new int[2 * num_points];
		return PreemptiveRobustHomography(H, p, q, num_points, test_points, num_test_points,this.mHyp, mTmpi, mHypCosts,
				mCauchyScale, mMaxNumHypotheses, mMaxTrials, mChunkSize);
	}

	final private HomographySolver _homography_solver=new HomographySolver_O1();
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
	private boolean PreemptiveRobustHomography(HomographyMat H, NyARDoublePoint2d[] p, NyARDoublePoint2d[] q, int num_points,
			NyARDoublePoint2d[] test_points, int num_test_points, HomographyMat[] hyp /* 9*max_num_hypotheses */,
			int[] tmp_i /* 2*num_points */, CostPair[] hyp_costs /* max_num_hypotheses */, double scale,
			int max_num_hypotheses, int max_trials, int chunk_size) {
		int hyp_perm;// ptr表現
		int point_perm;// ptr表現
		double one_over_scale2;
		double min_cost;
		int num_hypotheses, num_hypotheses_remaining, min_index;
		int cur_chunk_size, this_chunk_end;
		int trial;
		int sample_size = 4;


		// We need at least SAMPLE_SIZE points to sample from
		if (num_points < sample_size) {
			return false;
		}
		// seed = 1234;
		rand random = new rand(1234);

		hyp_perm = 0;
		point_perm = num_points;

		one_over_scale2 = 1 / (scale*scale);
//		chunk_size = math_utils.min2(chunk_size, num_points);
		chunk_size = chunk_size<num_points?chunk_size:num_points;


		// Fill two arrays from [0,num_points)
		indexing.SequentialVector(tmp_i, hyp_perm, num_points, 0);
		indexing.SequentialVector(tmp_i, point_perm, num_points, 0);

		// Shuffle the indices
		random.ArrayShuffle(tmp_i, hyp_perm, num_points, num_points);
		random.ArrayShuffle(tmp_i, point_perm, num_points, num_points);

		// Compute a set of hypotheses
		for (trial = 0, num_hypotheses = 0; trial < max_trials && num_hypotheses < max_num_hypotheses; trial++) {

			// Shuffle the first SAMPLE_SIZE indices
			random.ArrayShuffle(tmp_i, hyp_perm, num_points, sample_size);

			// Check if the four points are geometrically valid
			if (!geometry.Homography4PointsGeometricallyConsistent(p[tmp_i[hyp_perm + 0]], p[tmp_i[hyp_perm + 1]],
					p[tmp_i[hyp_perm + 2]], p[tmp_i[hyp_perm + 3]], q[tmp_i[hyp_perm + 0]], q[tmp_i[hyp_perm + 1]],
					q[tmp_i[hyp_perm + 2]], q[tmp_i[hyp_perm + 3]])) {
				continue;
			}/*
			 * boolean SolveHomography4Points(float[] H, float[] x1, float[] x2, float[] x3, float[] x4, float[] xp1,
			 * float[] xp2, float[] xp3, float[] xp4) {
			 */
			// Compute the homography
			if (!this._homography_solver.solveHomography4Points(hyp[num_hypotheses], p[tmp_i[hyp_perm + 0]], p[tmp_i[hyp_perm + 1]],
					p[tmp_i[hyp_perm + 2]], p[tmp_i[hyp_perm + 3]], q[tmp_i[hyp_perm + 0]], q[tmp_i[hyp_perm + 1]],
					q[tmp_i[hyp_perm + 2]], q[tmp_i[hyp_perm + 3]])) {
				continue;
			}
			// Check the test points
			if (num_test_points > 0) {
				NyARDoubleMatrix33 hyps=hyp[num_hypotheses];
//				double[] hyps = Utils.arraysubset(hyp, num_hypotheses * 9, 9);
				if (!homography.HomographyPointsGeometricallyConsistent(hyps, test_points, 0, num_test_points)) {
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
//			cur_chunk_size = math_utils.min2(chunk_size, num_points - i);
			cur_chunk_size = (chunk_size<num_points - i)?chunk_size:(num_points - i);

			// End of the current chunk
			this_chunk_end = i + cur_chunk_size;

			// Score each of the remaining hypotheses
			for (int j = 0; j < num_hypotheses_remaining; j++) {
				// const T* H_cur = &hyp[hyp_costs[j].second*9];
				HomographyMat ht=hyp[hyp_costs[j].second];
				double hf=0;
				for (int k = i; k < this_chunk_end; k++) {
					hf += ht.cauchyProjectiveReprojectionCost(p[tmp_i[hyp_perm + k]], q[tmp_i[hyp_perm + k]], one_over_scale2);
				}
				hyp_costs[j].first=hf;
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
		H.setValue(hyp[min_index]);
		H.normalizeHomography();

		return true;
	}
}
