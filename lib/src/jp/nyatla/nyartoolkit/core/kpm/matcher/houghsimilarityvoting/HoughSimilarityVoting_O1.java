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
final public class HoughSimilarityVoting_O1 {
	final static private double PI=NyARMath.PI;
	private static double kHoughBinDelta = 1;	




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



	private int mA; // mNumXBins*mNumYBins
	private int mB; // mNumXBins*mNumYBins*mNumAngleBins	
	
//	this.mHoughSimilarityVoting=new HoughSimilarityVoting_O1(-dx, dx, -dy, dy, 0, 0, 12, 10);	
	public HoughSimilarityVoting_O1(double minX, double maxX, double minY, double maxY,int numAngleBins, int numScaleBins)
	{
		this(minX,maxX,minY,maxY,0,0,numAngleBins,numScaleBins);
	}
	
	public HoughSimilarityVoting_O1(double minX, double maxX, double minY, double maxY, int numXBins,int numYBins, int numAngleBins, int numScaleBins)
	{
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
	
	//workArea
	private double[] _project_dim=new double[64];

	/**
	 * Set the number of bins for translation based on the correspondences.
	 */
	private void autoAdjustXYNumBins(int max_dim,FeaturePairStack i_point_pair)
	{
		double[] projected_dim = this._project_dim;
		int l=i_point_pair.getLength();
		//prepare work area
		if(projected_dim.length<l){
			projected_dim=new double[l+10];
		}
		for (int i = l-1; i>=0 ; i--) {

			// Scale is the 3rd component
			FeaturePairStack.Item item=i_point_pair.getItem(i);
			// Project the max_dim via the scale
			double scale = SafeDivision(item.query.scale, item.ref.scale);
			projected_dim[i] = scale * max_dim;
		}

		// Find the median projected dim
		// float median_proj_dim = FastMedian<float>(&projected_dim[0],
		// (int)projected_dim.size());
		double median_proj_dim = FastMedian(projected_dim, l);

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

	private void mapVoteToBin(BinLocation fBin,double x, double y, double angle, double scale)
	{
		fBin.x = mNumXBins * SafeDivision(x - mMinX, mMaxX - mMinX);
		fBin.y = mNumYBins * SafeDivision(y - mMinY, mMaxY - mMinY);
		fBin.angle = (double) (mNumAngleBins * ((angle + PI) * (1 / (2 * PI))));
		fBin.scale = mNumScaleBins	* SafeDivision(scale - mMinScale, mMaxScale - mMinScale);
	}
	/**
	 * Get the bins locations from an index.
	 */
	private BinLocation getBinsFromIndex(int index)
	{
		//ハッシュ値から元の値を復帰
		
		int binX = ((index % mB) % mA) % mNumXBins;
		int binY = (((index - binX) % mB) % mA) / mNumXBins;
		int binAngle = ((index - binX - (binY * mNumXBins)) % mB) / mA;
		int binScale = (index - binX - (binY * mNumXBins) - (binAngle * mA))/ mB;
		BinLocation r = new BinLocation();
		r.x = binX;
		r.y = binY;
		r.angle = binAngle;
		r.scale = binScale;
		return r;

	}
	/**
	 * Get an index from the discretized bin locations.
	 */
	private int getBinIndex(int binX, int binY, int binAngle, int binScale) {
		int index = binX + (binY * mNumXBins) + (binAngle * mA) + (binScale * mB);
		return index;
	}

	final private BinLocation _fBinRet = new BinLocation();
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
	private boolean vote(BinLocation r,BinLocation i_out_sub_bin_location)
	{
		// Compute the bin location
		BinLocation fBinRet = this._fBinRet;
		mapVoteToBin(fBinRet, r.x, r.y, r.angle, r.scale);
		i_out_sub_bin_location.x=fBinRet.x;
		i_out_sub_bin_location.y=fBinRet.y;
		i_out_sub_bin_location.scale=fBinRet.scale;
		i_out_sub_bin_location.angle=fBinRet.angle;
		int binX = (int) Math.floor(i_out_sub_bin_location.x - 0.5f);
		int binY = (int) Math.floor(i_out_sub_bin_location.y - 0.5f);
		int binAngle = (int) Math.floor(i_out_sub_bin_location.angle - 0.5f);
		int binScale = (int) Math.floor(i_out_sub_bin_location.scale - 0.5f);

		binAngle = (binAngle + mNumAngleBins) % mNumAngleBins;

		// Check that we can voting to all 16 bin locations
		if (binX < 0 || (binX + 1) >= mNumXBins || binY < 0
				|| (binY + 1) >= mNumYBins || binScale < 0
				|| (binScale + 1) >= mNumScaleBins) {
			return false;
		}

		int binXPlus1 = binX + 1;
		int binYPlus1 = binY + 1;
		int binScalePlus1 = binScale + 1;
		int binAnglePlus1 = (binAngle + 1) % mNumAngleBins;

		//
		// Cast the 16 votes
		//

		// bin location
		this.mVotes.voteAtIndex(getBinIndex(binX, binY, binAngle, binScale), 1);

		// binX+1
		this.mVotes.voteAtIndex(getBinIndex(binXPlus1, binY, binAngle, binScale), 1);
		this.mVotes.voteAtIndex(getBinIndex(binXPlus1, binYPlus1, binAngle, binScale), 1);
		this.mVotes.voteAtIndex(getBinIndex(binXPlus1, binYPlus1, binAnglePlus1, binScale),	1);
		this.mVotes.voteAtIndex(getBinIndex(binXPlus1, binYPlus1, binAnglePlus1, binScalePlus1),1);
		this.mVotes.voteAtIndex(getBinIndex(binXPlus1, binYPlus1, binAngle, binScalePlus1),	1);
		this.mVotes.voteAtIndex(getBinIndex(binXPlus1, binY, binAnglePlus1, binScale), 1);
		this.mVotes.voteAtIndex(getBinIndex(binXPlus1, binY, binAnglePlus1, binScalePlus1),	1);
		this.mVotes.voteAtIndex(getBinIndex(binXPlus1, binY, binAngle, binScalePlus1), 1);

		// binY+1
		this.mVotes.voteAtIndex(getBinIndex(binX, binYPlus1, binAngle, binScale), 1);
		this.mVotes.voteAtIndex(getBinIndex(binX, binYPlus1, binAnglePlus1, binScale), 1);
		this.mVotes.voteAtIndex(getBinIndex(binX, binYPlus1, binAnglePlus1, binScalePlus1),	1);
		this.mVotes.voteAtIndex(getBinIndex(binX, binYPlus1, binAngle, binScalePlus1), 1);

		// binAngle+1
		this.mVotes.voteAtIndex(getBinIndex(binX, binY, binAnglePlus1, binScale), 1);
		this.mVotes.voteAtIndex(getBinIndex(binX, binY, binAnglePlus1, binScalePlus1), 1);

		// binScale+1
		this.mVotes.voteAtIndex(getBinIndex(binX, binY, binAngle, binScalePlus1), 1);

		return true;
	}
	private static class BinLocation
	{
		public double x;
		public double y;
		public double angle;
		public double scale;

	}
	private static class SubBinLocation extends BinLocation
	{
		public int index;
		public static SubBinLocation[] createArray(int i_length){
			SubBinLocation[] r=new SubBinLocation[i_length];
			for(int i=0;i<i_length;i++){
				r[i]=new SubBinLocation();
			}
			return r;
		}		
	}
	
	public boolean extractMatches(FeaturePairStack i_matche_resule,int refWidth, int refHeight)	
	{
		// Extract the data from the features
		// hough.vote((float*)&query[0], (float*)&ref[0], (int)matches.size());
		int size=i_matche_resule.getLength();
		if (size==0) {
			return false;
		}
		//FindHoughSimilarity
		if (mAutoAdjustXYNumBins) {
			int max_dim =refWidth>refHeight?refWidth:refHeight;//math_utils.max2(mRefImageWidth, mRefImageHeight);
			this.autoAdjustXYNumBins(max_dim,i_matche_resule);
		}
		
		this.mSubBinLocations = SubBinLocation.createArray(size);
		mVotes.clear();
		int num_of_subbin=this.vote(i_matche_resule,refWidth/2,refHeight/2);		
		
		int max_hough_index = this.getMaximumNumberOfVotes(i_matche_resule);
		if (max_hough_index < 0) {
			return false;
		}
		this.FindHoughMatches(i_matche_resule,max_hough_index, kHoughBinDelta,num_of_subbin);
		return true;
	}
	
	/**
	 * Vote for a similarity transformation.
	 */
	private int getMaximumNumberOfVotes(FeaturePairStack matches)
	{
		 //Get the bin that has the maximum number of votes
		//this.getMaximumNumberOfVotes(max);
		int max_index=-1;
		int max_votes=0;
		for (Entry<Integer, Integer> it : mVotes.entrySet()) {
			if (it.getValue() > max_votes) {
				max_index = it.getKey();
				max_votes = it.getValue();
			}
		}
		return (max_votes < 3) ? -1 : max_index;
	}



	
	final private BinLocation _d = new BinLocation();
	
	/**
	 * Get only the matches that are consistent based on the hough votes.
	 */
	private void FindHoughMatches(FeaturePairStack in_matches,int binIndex, double binDelta,int i_num_of_sbin)
	{

		HoughSimilarityVoting_O1.BinLocation bin = this.getBinsFromIndex(binIndex);
		bin.x+=0.5;
		bin.y+=0.5;
		bin.angle+=0.5;
		bin.scale+=0.5;

		int n = i_num_of_sbin;
		// const float* vote_loc = hough.getSubBinLocations().data();
		BinLocation[] vote_loc = this.mSubBinLocations;// .data();
		// ASSERT(n <= in_matches.size(), "Should be the same");
		BinLocation d = this._d;
		//
		int pos=0;
		for (int i = 0; i < n; i++){
			this.getBinDistance(d, vote_loc[i].x,
					vote_loc[i].y, vote_loc[i].angle,
					vote_loc[i].scale, bin.x, bin.y,
					bin.angle, bin.scale);

			if (d.x < binDelta && d.y < binDelta && d.angle < binDelta && d.scale < binDelta) {
				//idxは昇順のはずだから詰める。
				int idx = this.mSubBinLocations[i].index;
				in_matches.swap(idx, pos);
				pos++;
				
			}
		}
		in_matches.setLength(pos);
		return;
	}

	final private BinLocation _r = new BinLocation();
	private int vote(FeaturePairStack i_point_pair,int i_center_x,int i_center_y) 
	{
		int size=i_point_pair.getLength();


		BinLocation r = this._r;
		int num_features_that_cast_vote = 0;
		for (int i = 0; i < size; i++) {
			// Cast a vote
			// Map the correspondence to a vote
			mapCorrespondence(r,i_point_pair.getItem(i),i_center_x,i_center_y);



			// Check that the vote is within range
			if (r.x < mMinX || r.x >= mMaxX || r.y < mMinY || r.y >= mMaxY
					|| r.angle <= -PI || r.angle > PI
					|| r.scale < mMinScale || r.scale >= mMaxScale) {
				continue;
			}
			if (this.vote(r,this.mSubBinLocations[num_features_that_cast_vote]))
			{
				this.mSubBinLocations[num_features_that_cast_vote].index=i;
				num_features_that_cast_vote++;
			}
		}
		return num_features_that_cast_vote;
	}



	/**
	 * Safe division (x/y).
	 */
	private static double SafeDivision(double x, double y) {
		return x / (y == 0 ? 1 : y);
	}



	private void mapCorrespondence(BinLocation r, FeaturePairStack.Item i_item,int i_center_x,int i_center_y)
	{

		FreakFeaturePoint ins=i_item.query;
		FreakFeaturePoint ref=i_item.ref;
		
		//angle
		r.angle = ins.angle - ref.angle;
		// Map angle to (-pi,pi]
		if (r.angle <= -PI) {
			r.angle += (2 * PI);
		} else if (r.angle > PI) {
			r.angle -= (2 * PI);
		}

		double scale = SafeDivision(ins.scale, ref.scale);
		double c = (scale * Math.cos(r.angle));
		double s = (scale * Math.sin(r.angle));

		//scale
		r.scale = (double) (Math.log(scale) * mScaleOneOverLogK);
		//x,y
		r.x = c * i_center_x - s * i_center_y + (ins.x - (c * ref.x - s * ref.y));
		r.y = s * i_center_x + c * i_center_y + (ins.y - (s * ref.x + c * ref.y));
		return;
	}








	private void getBinDistance(BinLocation distbin, double insBinX,
			double insBinY, double insBinAngle, double insBinScale, double refBinX,
			double refBinY, double refBinAngle, double refBinScale) {

		// (x,y,scale)
		distbin.x = Math.abs(insBinX - refBinX);
		distbin.y = Math.abs(insBinY - refBinY);
		distbin.scale = Math.abs(insBinScale - refBinScale);


		// Angle
		double d1 = Math.abs(insBinAngle - refBinAngle);
		double d2 = (double) mNumAngleBins - d1;
		distbin.angle = d1<d2?d1:d2;

		return;
	}







	//

	private class hash_t extends HashMap<Integer, Integer>
	{
		/**
		 * Cast a vote to an similarity index
		 */
		public void voteAtIndex(int index, int weight) {

			Integer it = this.get(index);
			if (it == null) {
				this.put(index, weight);
			} else {
				this.put(index, it + weight);
			}
		}
		
	}

	private final hash_t mVotes = new hash_t();

	private SubBinLocation[] mSubBinLocations;





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
