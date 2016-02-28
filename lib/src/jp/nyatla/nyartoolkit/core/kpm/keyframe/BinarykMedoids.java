package jp.nyatla.nyartoolkit.core.kpm.keyframe;

import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FreakFeaturePoint;
import jp.nyatla.nyartoolkit.core.math.NyARLCGsRandomizer;

public class BinarykMedoids {

	final private NyARLCGsRandomizer _tandom;
	// Number of cluster centers
	final private int mK;

	// Number of hypotheses to evaulate
	final private int mNumHypotheses;

	// Index of each cluster center
	final private int[] mCenters;

	/** Assignment of each feature to a cluster center */
	private int[] mAssignment;
	
	public BinarykMedoids(NyARLCGsRandomizer i_rand, int i_k, int i_num_of_hypotheses) {
		this.mK = i_k;
		this.mCenters = new int[i_k];
		this.mNumHypotheses = i_num_of_hypotheses;
		this._tandom = i_rand;


	}

	public int k() {
		return mK;
	}

	/**
	 * Create a sequential vector {x0+0, x0+1, x0+2, ...}
	 */
	void SequentialVector(int[] x, int n, int x0) {
		if (n < 1) {
			return;
		}
		x[0] = x0;
		for (int i = 1; i < n; i++) {
			x[i] = x[i - 1] + 1;
		}
	}

	/**
	 * Shuffle the elements of an array.
	 * 
	 * @param[in/out] v Array of elements
	 * @param[in] pop_size Population size, or size of the array v
	 * @param[in] sample_size The first SAMPLE_SIZE samples of v will be shuffled
	 * @param[in] seed Seed for random number generator
	 */
	void ArrayShuffle(int[] v, int pop_size, int sample_size) {
		for (int i = 0; i < sample_size; i++) {
			int k = this._tandom.rand() % pop_size;// int k = FastRandom(seed)%pop_size;
			int t = v[i];
			v[i] = v[k];
			v[k] = t;
		}
	}

	/**
	 * Assign featurs to a cluster center
	 */
	public void assign(FreakFeaturePoint[] features, int[] indices, int num_indices) {


		mAssignment = new int[num_indices];
		int[] mHypAssignment = new int[num_indices];
		int[] mRandIndices = new int[num_indices];


		SequentialVector(mRandIndices, mRandIndices.length, 0);

		int best_dist = Integer.MAX_VALUE;

		for (int i = 0; i < mNumHypotheses; i++) {
			// Shuffle the first "k" indices
			ArrayShuffle(mRandIndices, (int) mRandIndices.length, mK);

			// Assign features to the centers
			int dist = assign(mHypAssignment, features, indices, num_indices, mRandIndices, mK);

			if (dist < best_dist) {
				// Move the best assignment
				// mAssignment.swap(mHypAssignment);
				int[] tmp = mAssignment;
				mAssignment = mHypAssignment;
				mHypAssignment = tmp;

				// CopyVector(&mCenters[0], &mRandIndices[0], mK);
				for (int i2 = 0; i2 < mK; i2++) {
					this.mCenters[i2] = mRandIndices[i2];
				}
				best_dist = dist;
			}
		}
		return;
	}

	private static int assign(int[] assignment, FreakFeaturePoint[] features, int[] indices, int num_indices, int[] centers,int num_centers) {



		int sum_dist = 0;

		for (int i = 0; i < num_indices; i++) {
			int best_dist = Integer.MAX_VALUE;
			// Find the closest center
			for (int j = 0; j < num_centers; j++) {
				// Compute the distance from the center
				int dist = features[indices[i]].descripter.hammingDistance(features[indices[centers[j]]].descripter);
				if (dist < best_dist) {
					assignment[i] = centers[j];
					best_dist = dist;
				}
			}
			// Sum the BEST_DIST measures
			sum_dist += best_dist;
		}

		return sum_dist;
	}

	/**
	 * @return Assignment vector
	 */
	public int[] assignment() {
		return this.mAssignment;
	}


}
