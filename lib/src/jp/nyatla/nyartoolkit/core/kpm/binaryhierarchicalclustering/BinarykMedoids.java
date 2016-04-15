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
package jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering;

import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;
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
	final private int[] mAssignment;
	final private int[] mHypAssignment;
	final private int[] _rand_index;
	
	public BinarykMedoids(int i_max_feature_num,NyARLCGsRandomizer i_rand, int i_k, int i_num_of_hypotheses)
	{
		this.mK = i_k;
		this.mCenters = new int[i_k];
		this.mNumHypotheses = i_num_of_hypotheses;
		this._tandom = i_rand;
		this.mAssignment=new int[i_max_feature_num];
		this.mHypAssignment=new int[i_max_feature_num];
		this._rand_index=new int[i_max_feature_num];
	}

	public int k() {
		return mK;
	}

	/**
	 * Create a sequential vector {x0+0, x0+1, x0+2, ...}
	 */
	private static void SequentialVector(int[] x, int n, int x0) {
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
	private void ArrayShuffle(int[] v, int pop_size, int sample_size) {
		for (int i = 0; i < sample_size; i++) {
			int k = this._tandom.rand() % pop_size;// int k = FastRandom(seed)%pop_size;
			int t = v[i];
			v[i] = v[k];
			v[k] = t;
		}
	}

	/**
	 * Assign featurs to a cluster center
	 * @param features
	 * @param indices
	 * @param i_num_indices
	 * @return
	 * num_indicesと同数の値を格納した配列を返します。
	 */
	public int[] assign(FreakFeaturePoint[] features, int[] indices, int i_num_indices)
	{
		int[] rand_indics = this._rand_index;
		int[] assignment=this.mAssignment;
		int[] hyp_assignment=this.mHypAssignment;


		SequentialVector(rand_indics, i_num_indices, 0);

		int best_dist = Integer.MAX_VALUE;

		for (int i = 0; i < this.mNumHypotheses; i++) {
			// Shuffle the first "k" indices
			ArrayShuffle(rand_indics, i_num_indices, this.mK);

			// Assign features to the centers
			int dist = assign(hyp_assignment, features, indices, i_num_indices, rand_indics, this.mK);

			if (dist < best_dist) {
				// Move the best assignment
				// mAssignment.swap(mHypAssignment);
				int[] tmp = assignment;
				assignment = hyp_assignment;
				hyp_assignment = tmp;

				// CopyVector(&mCenters[0], &mRandIndices[0], mK);
				for (int i2 = 0; i2 < this.mK; i2++) {
					this.mCenters[i2] = rand_indics[i2];
				}
				best_dist = dist;
			}
		}
		return assignment;
	}

	private static int assign(int[] assignment, FreakFeaturePoint[] features, int[] indices, int num_indices, int[] centers,int num_centers)
	{
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


}
