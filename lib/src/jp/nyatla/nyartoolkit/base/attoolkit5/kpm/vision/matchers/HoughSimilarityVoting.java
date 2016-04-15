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
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers;

import java.util.HashMap;
import java.util.Map.Entry;

import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math.math_utils;

/**
 * Hough voting for a similarity transformation based on a set of
 * correspondences.
 */
public class HoughSimilarityVoting {

	// typedef std::unordered_map<unsigned int, unsigned int> hash_t;
	// typedef std::pair<int /*size*/, int /*index*/> vote_t;
	// typedef std::vector<vote_t> vote_vector_t;
	//
	public HoughSimilarityVoting() {
		mRefImageWidth = (0);
		mRefImageHeight = (0);
		mCenterX = (0);
		mCenterY = (0);
		mAutoAdjustXYNumBins = (true);
		mMinX = (0);
		mMaxX = (0);
		mMinY = (0);
		mMaxY = (0);
		mMinScale = (0);
		mMaxScale = (0);
		mScaleK = (0);
		mScaleOneOverLogK = (0);
		mNumXBins = (0);
		mNumYBins = (0);
		mNumAngleBins = (0);
		mNumScaleBins = (0);
		mfBinX = (0);
		mfBinY = (0);
		mfBinAngle = (0);
		mfBinScale = (0);
		mA = (0);
		mB = (0);
	}

	// ~HoughSimilarityVoting();
	//
	/**
         *
         */
	void init(double minX, double maxX, double minY, double maxY, int numXBins,
			int numYBins, int numAngleBins, int numScaleBins) {
		mMinX = minX;
		mMaxX = maxX;
		mMinY = minY;
		mMaxY = maxY;
		mMinScale = -1;
		mMaxScale = 1;

		mNumXBins = numXBins;
		mNumYBins = numYBins;
		mNumAngleBins = numAngleBins;
		mNumScaleBins = numScaleBins;

		mA = mNumXBins * mNumYBins;
		mB = mNumXBins * mNumYBins * mNumAngleBins;

		mScaleK = 10;
		mScaleOneOverLogK = (double) (1.f / Math.log(mScaleK));

		// If the number of bins for (x,y) are not set, then we adjust the
		// number of bins automatically.
		if (numXBins == 0 && numYBins == 0)
			mAutoAdjustXYNumBins = true;
		else
			mAutoAdjustXYNumBins = false;

		mVotes.clear();
	}

	/**
	 * The location of the center of the object in the reference image.
	 */
	public void setObjectCenterInReference(double x, double y) {
		mCenterX = x;
		mCenterY = y;
	}

	/**
	 * Set the dimensions fo the reference image
	 */
	public void setRefImageDimensions(int width, int height) {
		mRefImageWidth = width;
		mRefImageHeight = height;
	}

	//
	// /**
	// * Set the min/max of (x,y) for voting. Since we vote for the center of
	// the
	// * object. Sometimes the object center may be off the inspection image.
	// */
	// inline void setMinMaxXY(float minX, float maxX, float minY, float maxY) {
	// mMinX = minX;
	// mMaxX = maxX;
	// mMinY = minY;
	// mMaxY = maxY;
	// mVotes.clear();
	// }
	//
	// /**
	// * Get the distance of two bin locations for each parameter.
	// */
	// inline void getBinDistance(float& distBinX,
	// float& distBinY,
	// float& distBinAngle,
	// float& distBinScale,
	// float insBinX,
	// float insBinY,
	// float insBinAngle,
	// float insBinScale,
	// float refBinX,
	// float refBinY,
	// float refBinAngle,
	// float refBinScale) const;
	//

	void mapVoteToBin(mapCorrespondenceResult fBin,
	// float& fBinX,
	// float& fBinY,
	// float& fBinAngle,
	// float& fBinScale,
			double x, double y, double angle, double scale) {
		fBin.x = mNumXBins * SafeDivision(x - mMinX, mMaxX - mMinX);
		fBin.y = mNumYBins * SafeDivision(y - mMinY, mMaxY - mMinY);
		fBin.angle = (double) (mNumAngleBins * ((angle + math_utils.PI) * (1 / (2 * math_utils.PI))));
		fBin.scale = mNumScaleBins
				* SafeDivision(scale - mMinScale, mMaxScale - mMinScale);
	}

	/**
	 * Get an index from the discretized bin locations.
	 */
	private int getBinIndex(int binX, int binY, int binAngle, int binScale) {
		int index;

		// ASSERT(binX >= 0, "binX out of range");
		// ASSERT(binX < mNumXBins, "binX out of range");
		// ASSERT(binY >= 0, "binY out of range");
		// ASSERT(binY < mNumYBins, "binY out of range");
		// ASSERT(binAngle >= 0, "binAngle out of range");
		// ASSERT(binAngle < mNumAngleBins, "binAngle out of range");
		// ASSERT(binScale >= 0, "binScale out of range");
		// ASSERT(binScale < mNumScaleBins, "binScale out of range");

		index = binX + (binY * mNumXBins) + (binAngle * mA) + (binScale * mB);

		// ASSERT(index <= (binX + binY*mNumXBins + binAngle*mNumXBins*mNumYBins
		// + binScale*mNumXBins*mNumYBins*mNumAngleBins), "index out of range");

		return index;
	}

	/**
	 * Vote for the similarity transformation that maps the reference center to
	 * the inspection center.
	 * 
	 * ins_features = S*ref_features where
	 * 
	 * S = [scale*cos(angle), -scale*sin(angle), x; scale*sin(angle),
	 * scale*cos(angle), y; 0, 0, 1];
	 * 
	 * @param[in] x translation in x
	 * @param[in] y translation in y
	 * @param[in] angle (-pi,pi]
	 * @param[in] scale
	 */
	boolean vote(double x, double y, double angle, double scale) {
		int binX;
		int binY;
		int binAngle;
		int binScale;

		int binXPlus1;
		int binYPlus1;
		int binAnglePlus1;
		int binScalePlus1;

		// Check that the vote is within range
		if (x < mMinX || x >= mMaxX || y < mMinY || y >= mMaxY
				|| angle <= -math_utils.PI || angle > math_utils.PI
				|| scale < mMinScale || scale >= mMaxScale) {
			return false;
		}

		// ASSERT(x >= mMinX, "x out of range");
		// ASSERT(x < mMaxX, "x out of range");
		// ASSERT(y >= mMinY, "y out of range");
		// ASSERT(y < mMaxY, "y out of range");
		// ASSERT(angle > -PI, "angle out of range");
		// ASSERT(angle <= PI, "angle out of range");
		// ASSERT(scale >= mMinScale, "scale out of range");
		// ASSERT(scale < mMaxScale, "scale out of range");

		// Compute the bin location
		mapCorrespondenceResult fBinRet = new mapCorrespondenceResult();
		mapVoteToBin(fBinRet, x, y, angle, scale);
		this.mfBinX=fBinRet.x;
		this.mfBinY=fBinRet.y;
		this.mfBinScale=fBinRet.scale;
		this.mfBinAngle=fBinRet.angle;
		binX = (int) Math.floor(mfBinX - 0.5f);
		binY = (int) Math.floor(mfBinY - 0.5f);
		binAngle = (int) Math.floor(mfBinAngle - 0.5f);
		binScale = (int) Math.floor(mfBinScale - 0.5f);

		binAngle = (binAngle + mNumAngleBins) % mNumAngleBins;

		// Check that we can voting to all 16 bin locations
		if (binX < 0 || (binX + 1) >= mNumXBins || binY < 0
				|| (binY + 1) >= mNumYBins || binScale < 0
				|| (binScale + 1) >= mNumScaleBins) {
			return false;
		}

		binXPlus1 = binX + 1;
		binYPlus1 = binY + 1;
		binScalePlus1 = binScale + 1;
		binAnglePlus1 = (binAngle + 1) % mNumAngleBins;

		//
		// Cast the 16 votes
		//

		// bin location
		voteAtIndex(getBinIndex(binX, binY, binAngle, binScale), 1);

		// binX+1
		voteAtIndex(getBinIndex(binXPlus1, binY, binAngle, binScale), 1);
		voteAtIndex(getBinIndex(binXPlus1, binYPlus1, binAngle, binScale), 1);
		voteAtIndex(getBinIndex(binXPlus1, binYPlus1, binAnglePlus1, binScale),
				1);
		voteAtIndex(
				getBinIndex(binXPlus1, binYPlus1, binAnglePlus1, binScalePlus1),
				1);
		voteAtIndex(getBinIndex(binXPlus1, binYPlus1, binAngle, binScalePlus1),
				1);
		voteAtIndex(getBinIndex(binXPlus1, binY, binAnglePlus1, binScale), 1);
		voteAtIndex(getBinIndex(binXPlus1, binY, binAnglePlus1, binScalePlus1),
				1);
		voteAtIndex(getBinIndex(binXPlus1, binY, binAngle, binScalePlus1), 1);

		// binY+1
		voteAtIndex(getBinIndex(binX, binYPlus1, binAngle, binScale), 1);
		voteAtIndex(getBinIndex(binX, binYPlus1, binAnglePlus1, binScale), 1);
		voteAtIndex(getBinIndex(binX, binYPlus1, binAnglePlus1, binScalePlus1),
				1);
		voteAtIndex(getBinIndex(binX, binYPlus1, binAngle, binScalePlus1), 1);

		// binAngle+1
		voteAtIndex(getBinIndex(binX, binY, binAnglePlus1, binScale), 1);
		voteAtIndex(getBinIndex(binX, binY, binAnglePlus1, binScalePlus1), 1);

		// binScale+1
		voteAtIndex(getBinIndex(binX, binY, binAngle, binScalePlus1), 1);

		return true;
	}

	void vote(double[] ins, double[] ref, int size) {
		int num_features_that_cast_vote;

		mVotes.clear();
		if (size == 0) {
			return;
		}

		mSubBinLocations = new double[size * 4];
		mSubBinLocationIndices = new int[size];
		if (mAutoAdjustXYNumBins) {
			autoAdjustXYNumBins(ins, ref, size);
		}

		num_features_that_cast_vote = 0;
		for (int i = 0; i < size; i++) {
			// const float* ins_ptr = &ins[i<<2];
			// const float* ref_ptr = &ref[i<<2];
			int ins_ptr = i << 2;
			int ref_ptr = i << 2;

			// Map the correspondence to a vote
			mapCorrespondenceResult r = new mapCorrespondenceResult();
			mapCorrespondence(r, ins[ins_ptr + 0], ins[ins_ptr + 1],
					ins[ins_ptr + 2], ins[ins_ptr + 3], ref[ref_ptr + 0],
					ref[ref_ptr + 1], ref[ref_ptr + 2], ref[ref_ptr + 3]);

			// Cast a vote
			if (vote(r.x, r.y, r.angle, r.scale)) {
				int ptr_bin = num_features_that_cast_vote << 2;// float* ptr_bin
																// =
																// &mSubBinLocations[num_features_that_cast_vote<<2];
				mSubBinLocations[ptr_bin + 0] = mfBinX;// ptr_bin[0] = mfBinX;
				mSubBinLocations[ptr_bin + 1] = mfBinY;// ptr_bin[1] = mfBinY;
				mSubBinLocations[ptr_bin + 2] = mfBinAngle;// ptr_bin[2] =
															// mfBinAngle;
				mSubBinLocations[ptr_bin + 3] = mfBinScale;// ptr_bin[3] =
															// mfBinScale;

				mSubBinLocationIndices[num_features_that_cast_vote] = i;
				num_features_that_cast_vote++;
			}
		}

		// mSubBinLocations.resize(num_features_that_cast_vote*4);
		// mSubBinLocationIndices.resize(num_features_that_cast_vote);
		double[] n1 = new double[num_features_that_cast_vote * 4];
		int[] n2 = new int[num_features_that_cast_vote];
		System.arraycopy(mSubBinLocations, 0, n1, 0, n1.length);
		System.arraycopy(mSubBinLocationIndices, 0, n2, 0, n2.length);
		mSubBinLocations = n1;
		mSubBinLocationIndices = n2;
	}

	static class mapCorrespondenceResult {
		double x, y, angle, scale;
	}

	/**
	 * Safe division (x/y).
	 */
	double SafeDivision(double x, double y) {
		return x / (y == 0 ? 1 : y);
	}

	/**
	 * Create a similarity matrix.
	 */
	void Similarity2x2(double S[], double angle, double scale) {
		double c = (scale * Math.cos(angle));
		double s = (scale * Math.sin(angle));
		S[0] = c;
		S[1] = -s;
		S[2] = s;
		S[3] = c;
	}

	void mapCorrespondence(mapCorrespondenceResult r, double ins_x, double ins_y,
			double ins_angle, double ins_scale, double ref_x, double ref_y,
			double ref_angle, double ref_scale) {
		double[] S = new double[4];
		double[] tp = new double[2];
		double tx, ty;

		//
		// Angle
		//

		r.angle = ins_angle - ref_angle;
		// Map angle to (-pi,pi]
		if (r.angle <= -math_utils.PI) {
			r.angle += (2 * math_utils.PI);
		} else if (r.angle > math_utils.PI) {
			r.angle -= (2 * math_utils.PI);
		}
		// ASSERT(r.angle > -KpmMath.PI, "angle out of range");
		// ASSERT(r.angle <= KpmMath.PI, "angle out of range");

		//
		// Scale
		//

		r.scale = SafeDivision(ins_scale, ref_scale);
		Similarity2x2(S, r.angle, r.scale);

		r.scale = (double) (Math.log(r.scale) * mScaleOneOverLogK);

		//
		// Position
		//

		tp[0] = S[0] * ref_x + S[1] * ref_y;
		tp[1] = S[2] * ref_x + S[3] * ref_y;

		tx = ins_x - tp[0];
		ty = ins_y - tp[1];

		r.x = S[0] * mCenterX + S[1] * mCenterY + tx;
		r.y = S[2] * mCenterX + S[3] * mCenterY + ty;
	}

	//
	// /**
	// * Get the bins that have at least THRESHOLD number of votes.
	// */
	// void getVotes(vote_vector_t& votes, int threshold) const;
	//
	/**
	 * @return Sub-bin locations for each correspondence
	 */
	public double[] getSubBinLocations() {
		return mSubBinLocations;
	}

	/**
	 * @return Sub-bin indices for each correspondence
	 */
	public int[] getSubBinLocationIndices() {
		return mSubBinLocationIndices;
	}

	static public class getMaximumNumberOfVotesResult {
		public double votes;
		public int index;
	}

	/**
	 * Get the bin that has the maximum number of votes
	 */
	void getMaximumNumberOfVotes(getMaximumNumberOfVotesResult v) {
		v.votes = 0;
		v.index = -1;
		// for(hash_t::const_iterator it = mVotes.begin(); it != mVotes.end();
		// it++) {
		// if(it->second > maxVotes) {
		// v.index = it->first;
		// v.votes = it->second;
		// }
		// }
		for (Entry<Integer, Integer> it : mVotes.entrySet()) {
			if (it.getValue() > v.votes) {
				v.index = it.getKey();
				v.votes = it.getValue();
			}
		}
	}

	//
	// /**
	// * Map the similarity index to a transformation.
	// */
	// void getSimilarityFromIndex(float& x, float& y, float& angle, float&
	// scale, int index) const;
	//

	public void getBinDistance(mapCorrespondenceResult distbin, double insBinX,
			double insBinY, double insBinAngle, double insBinScale, double refBinX,
			double refBinY, double refBinAngle, double refBinScale) {
		//
		// (x,y,scale)
		//

		distbin.x = Math.abs(insBinX - refBinX);
		distbin.y = Math.abs(insBinY - refBinY);
		distbin.scale = Math.abs(insBinScale - refBinScale);

		//
		// Angle
		//

		double d1 = Math.abs(insBinAngle - refBinAngle);
		double d2 = (double) mNumAngleBins - d1;
		distbin.angle = (double) math_utils.min2(d1, d2);

		// ASSERT(distBinAngle >= 0, "distBinAngle must not be negative");
	}

	class Bins {
		public int binX;
		public int binY;
		public int binAngle;
		public int binScale;
	}

	/**
	 * Get the bins locations from an index.
	 */
	Bins getBinsFromIndex(int index) {
		int binX = ((index % mB) % mA) % mNumXBins;
		int binY = (((index - binX) % mB) % mA) / mNumXBins;
		int binAngle = ((index - binX - (binY * mNumXBins)) % mB) / mA;
		int binScale = (index - binX - (binY * mNumXBins) - (binAngle * mA))
				/ mB;
		Bins r = new Bins();
		r.binX = binX;
		r.binY = binY;
		r.binAngle = binAngle;
		r.binScale = binScale;
		return r;

		// ASSERT(binX >= 0, "binX out of range");
		// ASSERT(binX < mNumXBins, "binX out of range");
		// ASSERT(binY >= 0, "binY out of range");
		// ASSERT(binY < mNumYBins, "binY out of range");
		// ASSERT(binAngle >= 0, "binAngle out of range");
		// ASSERT(binAngle < mNumAngleBins, "binAngle out of range");
		// ASSERT(binScale >= 0, "binScale out of range");
		// ASSERT(binScale < mNumScaleBins, "binScale out of range");

		// index = binX + (binY*mNumXBins) + (binAngle*A) + (binScale*B)
	}

	//
	// /**
	// * Get the sub-bin location from the voting parameters.
	// */
	// inline void mapVoteToBin(float& fBinX,
	// float& fBinY,
	// float& fBinAngle,
	// float& fBinScale,
	// float x,
	// float y,
	// float angle,
	// float scale) const;
	//
	// /**
	// * Compute the similarity vote from a correspondence.
	// *
	// * @param[out] x
	// * @param[out] y
	// * @param[out] angle range (-pi,pi]
	// * @param[out] scale exponential of the scale such that scale =
	// log(s)/log(k)
	// * @param[in] ins_x
	// * @param[in] ins_y
	// * @param[in] ins_angle
	// * @param[in] ins_scale
	// * @param[in] ref_x
	// * @param[in] ref_y
	// * @param[in] ref_angle
	// * @param[in] ref_scale
	// * @see voteWithCorrespondences for description
	// */
	// inline void mapCorrespondence(float& x,
	// float& y,
	// float& angle,
	// float& scale,
	// float ins_x,
	// float ins_y,
	// float ins_angle,
	// float ins_scale,
	// float ref_x,
	// float ref_y,
	// float ref_angle,
	// float ref_scale) const;
	//

	// Dimensions of reference image
	private int mRefImageWidth;
	private int mRefImageHeight;

	// Center of object in reference image
	private double mCenterX;
	private double mCenterY;

	// Set to true if the XY number of bins should be adjusted
	private boolean mAutoAdjustXYNumBins;

	// Min/Max (x,y,scale). The angle includes all angles (-pi,pi).
	private double mMinX;
	private double mMaxX;
	private double mMinY;
	private double mMaxY;
	private double mMinScale;
	private double mMaxScale;

	private double mScaleK;
	private double mScaleOneOverLogK;

	private int mNumXBins;
	private int mNumYBins;
	private int mNumAngleBins;
	private int mNumScaleBins;

	private double mfBinX;
	private double mfBinY;
	private double mfBinAngle;
	private double mfBinScale;

	private int mA; // mNumXBins*mNumYBins
	private int mB; // mNumXBins*mNumYBins*mNumAngleBins
	//

	class hash_t extends HashMap<Integer, Integer> {
	}

	final hash_t mVotes = new hash_t();

	double[] mSubBinLocations;
	int[] mSubBinLocationIndices;

	/**
	 * Cast a vote to an similarity index
	 */
	private void voteAtIndex(int index, int weight) {
		// ASSERT(index >= 0, "index out of range");
		// const hash_t::iterator it = mVotes.find(index);
		// if(it == mVotes.end()) {
		// mVotes.insert(std::pair<unsigned int, unsigned int>(index, weight));
		// } else {
		// it->second += weight;
		// }
		Integer it = mVotes.get(index);
		if (it == null) {
			mVotes.put(index, weight);
		} else {
			mVotes.put(index, it + weight);
		}
	}

	/**
	 * Set the number of bins for translation based on the correspondences.
	 */
	private void autoAdjustXYNumBins(double[] ins, double[] ref, int size) {
		int max_dim = math_utils.max2(mRefImageWidth, mRefImageHeight);
		double[] projected_dim = new double[size];

		// ASSERT(size > 0, "size must be positive");
		// ASSERT(mRefImageWidth > 0, "width must be positive");
		// ASSERT(mRefImageHeight > 0, "height must be positive");

		for (int i = 0; i < size; i++) {
			int ins_ptr = i << 2;
			int ref_ptr = i << 2;
			// const float* ins_ptr = &ins[i<<2];
			// const float* ref_ptr = &ref[i<<2];

			// Scale is the 3rd component
			double ins_scale = ins[ins_ptr + 3];
			double ref_scale = ref[ref_ptr + 3];

			// Project the max_dim via the scale
			double scale = SafeDivision(ins_scale, ref_scale);
			projected_dim[i] = scale * max_dim;
		}

		// Find the median projected dim
		// float median_proj_dim = FastMedian<float>(&projected_dim[0],
		// (int)projected_dim.size());
		double median_proj_dim = FastMedian(projected_dim, projected_dim.length);

		// Compute the bin size a fraction of the median projected dim
		double bin_size = 0.25f * median_proj_dim;

		mNumXBins = math_utils
				.max2(5, (int) Math.ceil((mMaxX - mMinX) / bin_size));
		mNumYBins = math_utils
				.max2(5, (int) Math.ceil((mMaxY - mMinY) / bin_size));

		mA = mNumXBins * mNumYBins;
		mB = mNumXBins * mNumYBins * mNumAngleBins;
	}

	/**
	 * Find the median of an array.
	 */
	private double FastMedian(double a[], int n) {
		// return PartialSort(a, n, (((n)&1)?((n)/2):(((n)/2)-1)));
		return PartialSort(a, n, ((((n) & 1) == 1) ? ((n) / 2)
				: (((n) / 2) - 1)));
	}

	/**
	 * Perform a partial sort of an array. This algorithm is based on Niklaus
	 * Wirth's k-smallest.
	 * 
	 * @param[in/out] a array of elements
	 * @param[in] n size of a
	 * @param[in] k kth element starting from 1, i.e. 1st smallest, 2nd
	 *            smallest, etc.
	 */
	private double PartialSort(double[] a, int n, int k) {
		int i, j, l, m, k_minus_1;
		double x;

		// ASSERT(n > 0, "n must be positive");
		// ASSERT(k > 0, "k must be positive");

		k_minus_1 = k - 1;

		l = 0;
		m = n - 1;
		while (l < m) {
			x = a[k_minus_1];
			i = l;
			j = m;
			do {
				while (a[i] < x)
					i++;
				while (x < a[j])
					j--;
				if (i <= j) {
					// std::swap<T>(a[i],a[j]); // FIXME:
					double t = a[i];
					a[i] = a[j];
					a[j] = t;
					// std::swap(a[i], a[j]);
					i++;
					j--;
				}
			} while (i <= j);
			if (j < k_minus_1)
				l = i;
			if (k_minus_1 < i)
				m = j;
		}
		return a[k_minus_1];
	}

}
