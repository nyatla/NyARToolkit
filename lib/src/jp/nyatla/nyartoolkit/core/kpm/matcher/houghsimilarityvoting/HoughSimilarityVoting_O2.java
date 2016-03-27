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
		this.mA = 0;
		this.mB = 0 * numAngleBins;
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



//	final private BinLocation _fBinRet = new BinLocation();
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
	private boolean vote(int binX,int binY,int binScale,int binAngle)
	{
		int binXPlus1 = binX + 1;
		int binYPlus1 = binY + 1;
		int binScalePlus1 = binScale + 1;
		int binAnglePlus1 = (binAngle + 1) % mNumAngleBins;

		//
		// Cast the 16 votes
		//

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
		int max_dim =refWidth>refHeight?refWidth:refHeight;//math_utils.max2(mRefImageWidth, mRefImageHeight);
		this.autoAdjustXYNumBins(max_dim,i_matche_resule);

		
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



	
	
	/**
	 * Get only the matches that are consistent based on the hough votes.
	 */
	private void FindHoughMatches(FeaturePairStack in_matches,int binIndex, double binDelta,int i_num_of_sbin)
	{
		/**
		 * Get the bins locations from an index.
		 */
		double ref_x,ref_y,ref_angle,ref_scale;
		{
			//ハッシュ値から元の値を復帰
			
			int binX = ((binIndex % mB) % mA) % mNumXBins;
			int binY = (((binIndex - binX) % mB) % mA) / mNumXBins;
			int binAngle = ((binIndex - binX - (binY * mNumXBins)) % mB) / mA;
			int binScale = (binIndex - binX - (binY * mNumXBins) - (binAngle * mA))/ mB;
			ref_x = binX+0.5;
			ref_y = binY+0.5;
			ref_angle = binAngle+0.5;
			ref_scale = binScale+0.5;

		}		

		int n = i_num_of_sbin;
		// const float* vote_loc = hough.getSubBinLocations().data();
		BinLocation[] vote_loc = this.mSubBinLocations;// .data();


		//
		int pos=0;
		for (int i = 0; i < n; i++){
			BinLocation ins=vote_loc[i];
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
			int idx = this.mSubBinLocations[i].index;
			in_matches.swap(idx, pos);
			pos++;				
		}
		in_matches.setLength(pos);
		return;
	}


	private int vote(FeaturePairStack i_point_pair,int i_center_x,int i_center_y) 
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
			BinLocation sub_bin=this.mSubBinLocations[num_features_that_cast_vote];
			mapVoteToBin(sub_bin, rx, ry, rangle, rscale);
			int binX = (int) Math.floor(sub_bin.x - 0.5f);
			int binY = (int) Math.floor(sub_bin.y - 0.5f);
			int binScale = (int) Math.floor(sub_bin.scale - 0.5f);
			int binAngle = (int) Math.floor(sub_bin.angle - 0.5f);
			binAngle = (binAngle + mNumAngleBins) % mNumAngleBins;

			// Check that we can voting to all 16 bin locations
			if (binX < 0 || (binX + 1) >= mNumXBins || binY < 0
					|| (binY + 1) >= mNumYBins || binScale < 0
					|| (binScale + 1) >= mNumScaleBins) {
				continue;
			}
			if (this.vote(binX,binY,binScale,binAngle))
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










	//

	private class hash_t extends HashMap<Integer, Integer>
	{
		/**
		 * Cast a vote to an similarity index
		 */
		public void voteAtIndex(int binX, int binY, int binAngle, int binScale, int weight)
		{
			int index = binX + (binY * mNumXBins) + (binAngle * mA) + (binScale * mB);
			
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
