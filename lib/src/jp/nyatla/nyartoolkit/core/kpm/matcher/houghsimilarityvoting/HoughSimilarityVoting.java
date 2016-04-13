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
package jp.nyatla.nyartoolkit.core.kpm.matcher.houghsimilarityvoting;

import java.util.HashMap;
import java.util.Map.Entry;

import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;
import jp.nyatla.nyartoolkit.core.kpm.matcher.FeaturePairStack;
import jp.nyatla.nyartoolkit.core.math.NyARMath;

/**
 * Hough voting for a similarity transformation based on a set of
 * correspondences.
 */
public class HoughSimilarityVoting {
	final static private double PI=NyARMath.PI;
	private static double kHoughBinDelta = 1;	
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

	final private double mScaleK;
	final private double mScaleOneOverLogK;

	private int mNumXBins;
	private int mNumYBins;
	final private int mNumAngleBins;
	final private int mNumScaleBins;

	private double mfBinX;
	private double mfBinY;
	private double mfBinAngle;
	private double mfBinScale;

	private int mA; // mNumXBins*mNumYBins
	private int mB; // mNumXBins*mNumYBins*mNumAngleBins	
	
	public HoughSimilarityVoting(double minX, double maxX, double minY, double maxY,int numAngleBins, int numScaleBins)
	{
		this(minX,maxX,minY,maxY,0,0,numAngleBins,numScaleBins);
	}	
	public HoughSimilarityVoting(double minX, double maxX, double minY, double maxY, int numXBins,int numYBins, int numAngleBins, int numScaleBins)
	{
		this.mRefImageWidth = (0);
		this.mRefImageHeight = (0);
		this.mCenterX = (0);
		this.mCenterY = (0);
		this.mfBinX = (0);
		this.mfBinY = (0);
		this.mfBinAngle = (0);
		this.mfBinScale = (0);
		this.mMinX = minX;
		this.mMaxX = maxX;
		this.mMinY = minY;
		this.mMaxY = maxY;
		this.mMinScale =-1;
		this.mMaxScale = 1;
		this.mScaleK = 10;
		this.mScaleOneOverLogK = (double) (1.0 / Math.log(mScaleK));
		this.mNumXBins = numXBins;
		this.mNumYBins = numYBins;
		this.mNumAngleBins = numAngleBins;
		this.mNumScaleBins = numScaleBins;
		this.mA = numXBins * numYBins;
		this.mB = numXBins * numYBins * numAngleBins;
		// If the number of bins for (x,y) are not set, then we adjust the
		// number of bins automatically.
		if (numXBins == 0 && numYBins == 0){
			this.mAutoAdjustXYNumBins = true;
		}
		else{
			this.mAutoAdjustXYNumBins = false;
		}
	}




	//

	private void mapVoteToBin(mapCorrespondenceResult fBin,double x, double y, double angle, double scale) {
		fBin.x = mNumXBins * SafeDivision(x - mMinX, mMaxX - mMinX);
		fBin.y = mNumYBins * SafeDivision(y - mMinY, mMaxY - mMinY);
		fBin.angle = (double) (mNumAngleBins * ((angle + PI) * (1 / (2 * PI))));
		fBin.scale = mNumScaleBins
				* SafeDivision(scale - mMinScale, mMaxScale - mMinScale);
	}

	/**
	 * Get an index from the discretized bin locations.
	 */
	private int getBinIndex(int binX, int binY, int binAngle, int binScale) {
		int index;



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
	private boolean vote(double x, double y, double angle, double scale) {
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
				|| angle <= -PI || angle > PI
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
		voteAtIndex(getBinIndex(binXPlus1, binYPlus1, binAnglePlus1, binScale),	1);
		voteAtIndex(getBinIndex(binXPlus1, binYPlus1, binAnglePlus1, binScalePlus1),1);
		voteAtIndex(getBinIndex(binXPlus1, binYPlus1, binAngle, binScalePlus1),	1);
		voteAtIndex(getBinIndex(binXPlus1, binY, binAnglePlus1, binScale), 1);
		voteAtIndex(getBinIndex(binXPlus1, binY, binAnglePlus1, binScalePlus1),	1);
		voteAtIndex(getBinIndex(binXPlus1, binY, binAngle, binScalePlus1), 1);

		// binY+1
		voteAtIndex(getBinIndex(binX, binYPlus1, binAngle, binScale), 1);
		voteAtIndex(getBinIndex(binX, binYPlus1, binAnglePlus1, binScale), 1);
		voteAtIndex(getBinIndex(binX, binYPlus1, binAnglePlus1, binScalePlus1),	1);
		voteAtIndex(getBinIndex(binX, binYPlus1, binAngle, binScalePlus1), 1);

		// binAngle+1
		voteAtIndex(getBinIndex(binX, binY, binAnglePlus1, binScale), 1);
		voteAtIndex(getBinIndex(binX, binY, binAnglePlus1, binScalePlus1), 1);

		// binScale+1
		voteAtIndex(getBinIndex(binX, binY, binAngle, binScalePlus1), 1);

		return true;
	}

	private static class BinLocation{
		public double x;
		public double y;
		public double angle;
		public double scale;
		public static BinLocation[] createArray(int i_length){
			BinLocation[] r=new BinLocation[i_length];
			for(int i=0;i<i_length;i++){
				r[i]=new BinLocation();
			}
			return r;
		}
	}
	public boolean extractMatches(FeaturePairStack i_matche_resule,int refWidth, int refHeight)	
	{
		int max_hough_index = -1;
		max_hough_index = this.FindHoughSimilarity(i_matche_resule,refWidth,refHeight);
		if (max_hough_index < 0) {
			return false;
		}
		this.FindHoughMatches(i_matche_resule,max_hough_index, kHoughBinDelta);
		return true;
	}
	
	/**
	 * Vote for a similarity transformation.
	 */
	private int FindHoughSimilarity(FeaturePairStack matches,int refWidth, int refHeight)
	{
		//this.setObjectCenterInReference(refWidth >> 1, refHeight >> 1);
		this.mCenterX = refWidth>>1;
		this.mCenterY = refHeight>>1;

		//this.setRefImageDimensions(refWidth, refHeight);
		this.mRefImageWidth = refWidth;
		this.mRefImageHeight = refHeight;
		
		// Extract the data from the features
		// hough.vote((float*)&query[0], (float*)&ref[0], (int)matches.size());
		this.vote(matches);

		HoughSimilarityVoting.getMaximumNumberOfVotesResult max = new HoughSimilarityVoting.getMaximumNumberOfVotesResult();
		this.getMaximumNumberOfVotes(max);

		return (max.votes < 3) ? -1 : max.index;
	}
	/**
	 * Get only the matches that are consistent based on the hough votes.
	 */
	private void FindHoughMatches(FeaturePairStack in_matches,int binIndex, double binDelta) {

		HoughSimilarityVoting.Bins bin = this.getBinsFromIndex(binIndex);

	

		int n = (int) this.getSubBinLocationIndices().length;
		// const float* vote_loc = hough.getSubBinLocations().data();
		BinLocation[] vote_loc = this.getSubBinLocations();// .data();
		// ASSERT(n <= in_matches.size(), "Should be the same");
		HoughSimilarityVoting.mapCorrespondenceResult d = new HoughSimilarityVoting.mapCorrespondenceResult();
		//
		int pos=0;
		for (int i = 0; i < n; i++){
			this.getBinDistance(d, vote_loc[i].x,
					vote_loc[i].y, vote_loc[i].angle,
					vote_loc[i].scale, bin.binX + .5f, bin.binY + .5f,
					bin.binAngle + .5f, bin.binScale + .5f);

			if (d.x < binDelta && d.y < binDelta && d.angle < binDelta && d.scale < binDelta) {
				//idxは昇順のはずだから詰める。
				int idx = this.getSubBinLocationIndices()[i];
				in_matches.swap(idx, pos);
				pos++;
				
			}
		}
		in_matches.setLength(pos);
		return;
	}

	
	private void vote(FeaturePairStack i_point_pair) {
		int num_features_that_cast_vote;

		int size=i_point_pair.getLength();
		mVotes.clear();
		if (size==0) {
			return;
		}

		mSubBinLocations = BinLocation.createArray(size);
		mSubBinLocationIndices = new int[size];
		if (mAutoAdjustXYNumBins) {
			this.autoAdjustXYNumBins(i_point_pair);
		}

		num_features_that_cast_vote = 0;
		for (int i = 0; i < size; i++) {



			// Map the correspondence to a vote
			mapCorrespondenceResult r = new mapCorrespondenceResult();
			mapCorrespondence(r,i_point_pair.getItem(i));

			// Cast a vote
			if (vote(r.x, r.y, r.angle, r.scale)) {
//				int ptr_bin = num_features_that_cast_vote << 2;// float* ptr_bin
				BinLocation ptr_bin=this.mSubBinLocations[num_features_that_cast_vote];
				ptr_bin.x= mfBinX;// ptr_bin[0] = mfBinX;
				ptr_bin.y = mfBinY;// ptr_bin[1] = mfBinY;
				ptr_bin.angle= mfBinAngle;// ptr_bin[2] =  mfBinAngle;
				ptr_bin.scale = mfBinScale;// ptr_bin[3] =  mfBinScale;

				mSubBinLocationIndices[num_features_that_cast_vote] = i;
				num_features_that_cast_vote++;
			}
		}

		// mSubBinLocations.resize(num_features_that_cast_vote*4);
		// mSubBinLocationIndices.resize(num_features_that_cast_vote);
		BinLocation[] n1 = new BinLocation[num_features_that_cast_vote];
		int[] n2 = new int[num_features_that_cast_vote];
		System.arraycopy(mSubBinLocations, 0, n1, 0, n1.length);
		System.arraycopy(mSubBinLocationIndices, 0, n2, 0, n2.length);
		mSubBinLocations = n1;
		mSubBinLocationIndices = n2;
		return;
	}

	private static class mapCorrespondenceResult {
		public double x, y;
		public double angle;
		public double scale;
	}

	/**
	 * Safe division (x/y).
	 */
	private double SafeDivision(double x, double y) {
		return x / (y == 0 ? 1 : y);
	}

	/**
	 * Create a similarity matrix.
	 */
	private static void Similarity2x2(double S[], double angle, double scale) {
		double c = (scale * Math.cos(angle));
		double s = (scale * Math.sin(angle));
		S[0] = c;
		S[1] = -s;
		S[2] = s;
		S[3] = c;
	}

	private void mapCorrespondence(mapCorrespondenceResult r, FeaturePairStack.Item i_item) {
		double[] S = new double[4];
		double[] tp = new double[2];
		double tx, ty;

		//
		// Angle
		//
		FreakFeaturePoint ins=i_item.query;
		FreakFeaturePoint ref=i_item.ref;
		r.angle = ins.angle - ref.angle;
		// Map angle to (-pi,pi]
		if (r.angle <= -PI) {
			r.angle += (2 * PI);
		} else if (r.angle > PI) {
			r.angle -= (2 * PI);
		}
		// ASSERT(r.angle > -KpmMath.PI, "angle out of range");
		// ASSERT(r.angle <= KpmMath.PI, "angle out of range");

		//
		// Scale
		//

		r.scale = SafeDivision(ins.scale, ref.scale);
		Similarity2x2(S, r.angle, r.scale);

		r.scale = (double) (Math.log(r.scale) * mScaleOneOverLogK);

		//
		// Position
		//

		tp[0] = S[0] * ref.x + S[1] * ref.y;
		tp[1] = S[2] * ref.x + S[3] * ref.y;

		tx = ins.x - tp[0];
		ty = ins.y - tp[1];

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
	private BinLocation[] getSubBinLocations() {
		return mSubBinLocations;
	}

	/**
	 * @return Sub-bin indices for each correspondence
	 */
	private int[] getSubBinLocationIndices() {
		return mSubBinLocationIndices;
	}

	private static class getMaximumNumberOfVotesResult {
		public double votes;
		public int index;
	}

	/**
	 * Get the bin that has the maximum number of votes
	 */
	private void getMaximumNumberOfVotes(getMaximumNumberOfVotesResult v) {
		v.votes = 0;
		v.index = -1;

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

	private void getBinDistance(mapCorrespondenceResult distbin, double insBinX,
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
		//distbin.angle = (double) math_utils.min2(d1, d2);
		distbin.angle = d1<d2?d1:d2;

		return;
	}

	private class Bins {
		public int binX;
		public int binY;
		public int binAngle;
		public int binScale;
	}

	/**
	 * Get the bins locations from an index.
	 */
	private Bins getBinsFromIndex(int index) {
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

	}



	//

	private class hash_t extends HashMap<Integer, Integer> {
	}

	private final hash_t mVotes = new hash_t();

	private BinLocation[] mSubBinLocations;
	int[] mSubBinLocationIndices;

	/**
	 * Cast a vote to an similarity index
	 */
	private void voteAtIndex(int index, int weight) {

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
	private void autoAdjustXYNumBins(FeaturePairStack i_point_pair) {
		int max_dim =mRefImageWidth>mRefImageHeight?mRefImageWidth:mRefImageHeight;//math_utils.max2(mRefImageWidth, mRefImageHeight);
		double[] projected_dim = new double[i_point_pair.getLength()];


		for (int i = 0; i < i_point_pair.getLength(); i++) {

			// Scale is the 3rd component
			FeaturePairStack.Item item=i_point_pair.getItem(i);
			double ins_scale = item.query.scale;//[ins_ptr + 3];
			double ref_scale = item.ref.scale;//[ref_ptr + 3];

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

		int t;
		t=(int) Math.ceil((mMaxX - mMinX) / bin_size);
		mNumXBins =(5>t?5:t);
//		mNumXBins = math_utils.max2(5, );
		t=(int) Math.ceil((mMaxY - mMinY) / bin_size);
		mNumYBins =(5>t?5:t);

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
