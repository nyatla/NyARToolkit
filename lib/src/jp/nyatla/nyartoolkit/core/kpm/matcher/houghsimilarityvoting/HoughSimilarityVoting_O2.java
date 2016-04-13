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
final public class HoughSimilarityVoting_O2 {
	final static private double PI=NyARMath.PI;
	private static double kHoughBinDelta = 1;	





	// Min/Max (x,y,scale). The angle includes all angles (-pi,pi).
	final private double mMinX;
	final private double mMaxX;
	final private double mMinY;
	final private double mMaxY;
	final private double mMinScale;
	final private double mMaxScale;

	final private double mScaleK;
	final private double mScaleOneOverLogK;

	private int mNumXBins;
	private int mNumYBins;
	final private int mNumAngleBins;
	final private int mNumScaleBins;



	
//	this.mHoughSimilarityVoting=new HoughSimilarityVoting_O1(-dx, dx, -dy, dy, 0, 0, 12, 10);	
	public HoughSimilarityVoting_O2(double minX, double maxX, double minY, double maxY,int numAngleBins, int numScaleBins)
	{
		this.mMinX = minX;
		this.mMaxX = maxX;
		this.mMinY = minY;
		this.mMaxY = maxY;
		this.mMinScale =-1;
		this.mMaxScale = 1;
		this.mScaleK = 10;
		this.mScaleOneOverLogK = (double) (1.0 / Math.log(mScaleK));
		this.mNumXBins = 0;
		this.mNumYBins = 0;
		this.mNumAngleBins = numAngleBins;
		this.mNumScaleBins = numScaleBins;

		// If the number of bins for (x,y) are not set, then we adjust the
		// number of bins automatically.

	}
	
	
	//workArea
	private double[] _project_dim=new double[64];

	/**
	 * Set the number of bins for translation based on the correspondences.
	 */
	private void autoAdjustXYNumBins(int max_dim,FeaturePairStack i_point_pair)
	{
		int l=i_point_pair.getLength();
		//prepare work area
		if(this._project_dim.length<l){
			this._project_dim=new double[l+10];
		}
		double[] projected_dim = this._project_dim;
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
		this.mNumXBins =(5>t?5:t);
		t=(int) Math.ceil((mMaxY - mMinY) / bin_size);
		this.mNumYBins =(5>t?5:t);

		return;
	}

	private void mapVoteToBin(BinLocation fBin,double x, double y, double angle, double scale)
	{
		fBin.x = mNumXBins * SafeDivision(x - mMinX, mMaxX - mMinX);
		fBin.y = mNumYBins * SafeDivision(y - mMinY, mMaxY - mMinY);
		fBin.angle = (double) (mNumAngleBins * ((angle + PI) * (1 / (2 * PI))));
		fBin.scale = mNumScaleBins	* SafeDivision(scale - mMinScale, mMaxScale - mMinScale);
	}



//	final private BinLocation _fBinRet = new BinLocation();
	/**
	 * Vote for the similarity transformation that maps the reference center to
	 * the inspection center.
	 */
	private void vote(int binX,int binY,int binScale,int binAngle)
	{
		int binXPlus1 = binX + 1;
		int binYPlus1 = binY + 1;
		int binScalePlus1 = binScale + 1;
		int binAnglePlus1 = (binAngle + 1) % mNumAngleBins;
		//
		// Cast the 16 votes
		//
//		int index = binX + mNumXBins*(binY + mNumYBins*(binAngle  + (binScale * mNumAngleBins)));

		// bin location
		this.mVotes.voteAtIndex(binX, binY, binAngle, binScale, 1);

		// binX+1
		this.mVotes.voteAtIndex(binXPlus1, binY, binAngle, binScale, 1);
		this.mVotes.voteAtIndex(binXPlus1, binYPlus1, binAngle, binScale, 1);
		this.mVotes.voteAtIndex(binXPlus1, binYPlus1, binAnglePlus1, binScale,	1);
		this.mVotes.voteAtIndex(binXPlus1, binYPlus1, binAnglePlus1, binScalePlus1,1);
		this.mVotes.voteAtIndex(binXPlus1, binYPlus1, binAngle, binScalePlus1,	1);
		this.mVotes.voteAtIndex(binXPlus1, binY, binAnglePlus1, binScale, 1);
		this.mVotes.voteAtIndex(binXPlus1, binY, binAnglePlus1, binScalePlus1,	1);
		this.mVotes.voteAtIndex(binXPlus1, binY, binAngle, binScalePlus1, 1);

		// binY+1
		this.mVotes.voteAtIndex(binX, binYPlus1, binAngle, binScale, 1);
		this.mVotes.voteAtIndex(binX, binYPlus1, binAnglePlus1, binScale, 1);
		this.mVotes.voteAtIndex(binX, binYPlus1, binAnglePlus1, binScalePlus1,	1);
		this.mVotes.voteAtIndex(binX, binYPlus1, binAngle, binScalePlus1, 1);

		// binAngle+1
		this.mVotes.voteAtIndex(binX, binY, binAnglePlus1, binScale, 1);
		this.mVotes.voteAtIndex(binX, binY, binAnglePlus1, binScalePlus1, 1);

		// binScale+1
		this.mVotes.voteAtIndex(binX, binY, binAngle, binScalePlus1, 1);

		return;
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
	private SubBinLocation[] _mSubBinLocations=SubBinLocation.createArray(50);
	public boolean extractMatches(FeaturePairStack i_matche_resule,int refWidth, int refHeight)	
	{
		// Extract the data from the features
		// hough.vote((float*)&query[0], (float*)&ref[0], (int)matches.size());
		int size=i_matche_resule.getLength();
		if (size==0) {
			return false;
		}
		//ワークエリア
		if(this._mSubBinLocations.length<size){
			this._mSubBinLocations = SubBinLocation.createArray(size+10);
		}

		//FindHoughSimilarity
		int max_dim =refWidth>refHeight?refWidth:refHeight;//math_utils.max2(mRefImageWidth, mRefImageHeight);
		this.autoAdjustXYNumBins(max_dim,i_matche_resule);

		
		this.mVotes.clear();
		int num_of_subbin=this.vote(i_matche_resule,refWidth/2,refHeight/2,this._mSubBinLocations);		
		
		int max_hough_index = this.getMaximumNumberOfVotes(i_matche_resule);
		if (max_hough_index < 0) {
			return false;
		}
		this.FindHoughMatches(i_matche_resule,max_hough_index, kHoughBinDelta,this._mSubBinLocations,num_of_subbin);
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



	
	
	/**
	 * Get only the matches that are consistent based on the hough votes.
	 */
	private void FindHoughMatches(FeaturePairStack in_matches,int binIndex, double binDelta,SubBinLocation[] i_sub_bin,int i_num_of_sbin)
	{
		/**
		 * Get the bins locations from an index.
		 */
		double ref_x,ref_y,ref_angle,ref_scale;
		{
			//ハッシュ値から元の値を復帰
			int binX = binIndex%mNumXBins;
			int binY = (binIndex/mNumXBins)%mNumYBins;
			int binAngle = (binIndex/(mNumXBins*mNumYBins))%mNumAngleBins;
			int binScale = (binIndex/(mNumXBins*mNumYBins*mNumAngleBins))%mNumScaleBins;

			ref_x = binX+0.5;
			ref_y = binY+0.5;
			ref_angle = binAngle+0.5;
			ref_scale = binScale+0.5;

		}		


		//
		int pos=0;
		for (int i = 0; i < i_num_of_sbin; i++){
			SubBinLocation ins=i_sub_bin[i];
			//getBinDistance
			double d;
			
			//x
			d=Math.abs(ins.x - ref_x);
			if(d>=binDelta){
				continue;
			}
			//y
			d= Math.abs(ins.y - ref_y);
			if(d>=binDelta){
				continue;
			}
			//scale
			d= Math.abs(ins.scale - ref_scale);
			if(d>=binDelta){
				continue;
			}
			// Angle
			double d1 = Math.abs(ins.angle - ref_angle);
			double d2 = (double) this.mNumAngleBins - d1;
			d = d1<d2?d1:d2;			
			if(d>=binDelta){
				continue;
			}

			//idxは昇順のはずだから詰める。
			int idx =ins.index;
			in_matches.swap(idx, pos);
			pos++;				
		}
		in_matches.setLength(pos);
		return;
	}


	private int vote(FeaturePairStack i_point_pair,int i_center_x,int i_center_y,SubBinLocation[] i_sub_bin_locations) 
	{
		int size=i_point_pair.getLength();


		int num_features_that_cast_vote = 0;
		for (int i = 0; i < size; i++) {
			//mapCorrespondence(r,i_point_pair.getItem(i),i_center_x,i_center_y);
			double rx,ry,rangle,rscale;
			{
				FreakFeaturePoint ins=i_point_pair.getItem(i).query;
				FreakFeaturePoint ref=i_point_pair.getItem(i).ref;
				
				//angle
				rangle = ins.angle - ref.angle;
				// Map angle to (-pi,pi]
				if (rangle <= -PI) {
					rangle += (2 * PI);
				} else if (rangle > PI) {
					rangle -= (2 * PI);
				}

				double scale = SafeDivision(ins.scale, ref.scale);
				double c = (scale * Math.cos(rangle));
				double s = (scale * Math.sin(rangle));

				//scale
				rscale = (double) (Math.log(scale) * this.mScaleOneOverLogK);
				//x,y
				rx = c * i_center_x - s * i_center_y + (ins.x - (c * ref.x - s * ref.y));
				ry = s * i_center_x + c * i_center_y + (ins.y - (s * ref.x + c * ref.y));
				// Check that the vote is within range
				if (rx < mMinX || rx >= mMaxX || ry < mMinY || ry >= mMaxY
						|| rangle <= -PI || rangle > PI
						|| rscale < mMinScale || rscale >= mMaxScale) {
					continue;
				}				
			}
			// Compute the bin location
			SubBinLocation sub_bin=i_sub_bin_locations[num_features_that_cast_vote];
			mapVoteToBin(sub_bin, rx, ry, rangle, rscale);
			int binX = (int) Math.floor(sub_bin.x - 0.5f);
			int binY = (int) Math.floor(sub_bin.y - 0.5f);
			int binScale = (int) Math.floor(sub_bin.scale - 0.5f);
			int binAngle = ((int) Math.floor(sub_bin.angle - 0.5f) + mNumAngleBins) % mNumAngleBins;

			// Check that we can voting to all 16 bin locations
			if (binX < 0 || (binX + 1) >= mNumXBins || binY < 0
					|| (binY + 1) >= mNumYBins || binScale < 0
					|| (binScale + 1) >= mNumScaleBins) {
				continue;
			}
			sub_bin.index=i;
			num_features_that_cast_vote++;			
			this.vote(binX,binY,binScale,binAngle);

		}
		return num_features_that_cast_vote;
	}

	/**
	 * Safe division (x/y).
	 */
	private static double SafeDivision(double x, double y) {
		return x / (y == 0 ? 1 : y);
	}

	private class VoteMap extends HashMap<Integer, Integer>
	{
		/**
		 * Cast a vote to an similarity index
		 */
		public void voteAtIndex(int binX, int binY, int binAngle, int binScale, int weight)
		{
			int index = binX + mNumXBins*(binY + mNumYBins*(binAngle  + (binScale * mNumAngleBins)));
			Integer it = this.get(index);
			if (it == null) {
				this.put(index, weight);
			} else {
				this.put(index, it + weight);
			}
		}

		
	}

	private final VoteMap mVotes = new VoteMap();


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
