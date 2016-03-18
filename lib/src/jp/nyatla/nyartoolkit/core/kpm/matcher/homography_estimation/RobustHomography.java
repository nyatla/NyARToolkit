package jp.nyatla.nyartoolkit.core.kpm.matcher.homography_estimation;


import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.utils.rand;
import jp.nyatla.nyartoolkit.core.kpm.matcher.FeaturePairStack;
import jp.nyatla.nyartoolkit.core.kpm.matcher.HomographyMat;
import jp.nyatla.nyartoolkit.core.kpm.vision.match.indexing;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;


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
	private double mCauchyScale;
	private int mMaxNumHypotheses;
	private int mMaxTrials;
	private int mChunkSize;

//	/**
//	 * Find the homography from a set of 2D correspondences. p,q,test_pointshaは0位置固定
//	 */
//	public boolean find(HomographyMat H, NyARDoublePoint2d[] p, NyARDoublePoint2d[] q, int num_points,double i_width,double i_height)
//	{
//		return PreemptiveRobustHomography(H, p, q, num_points, i_width,i_height,this.mHyp);
//	}

	class PointSuffler
	{
		public FeaturePairStack.Item[] indics;
		private int _num;
		public PointSuffler(int i_size){
			this.indics=new FeaturePairStack.Item[i_size];
			this._num=0;
		}
		public void setSequential(FeaturePairStack.Item[] i_matches,int i_num)
		{
			if(i_num>this.indics.length){
				this.indics=new FeaturePairStack.Item[i_num+10];
			}
			FeaturePairStack.Item[] b=this.indics;
			for(int i=0;i<i_num;i++){
				b[i]=i_matches[i];
			}
			this._num=i_num;
		}
		private int seed;
		public void setSeed(int i_seed){
			this.seed=i_seed;
		}

	    /**
	     * Shuffle the elements of an array.
	     */
	    public void ArrayShuffle(int i_num_of_shuffle)
	    {
	    	FeaturePairStack.Item[] b=this.indics;
	    	int seed=this.seed;
	    	int n=this._num;
	        for(int i = 0; i < i_num_of_shuffle; i++)
	        {
		        seed= (214013*seed+2531011);
		        int k=((seed>>16)&0x7FFF)%n;
		        FeaturePairStack.Item t=b[i];
	            b[i]=b[k];
	            b[k]=t;
	        }
	        this.seed=seed;
	    }
		
		
		
		
	}
	final private PointSuffler _ps=new PointSuffler(100);
	
	final private HomographySolver _homography_solver=new HomographySolver_O1();
	final private HomographyPointsCheck _homography_check=new HomographyPointsCheck_O1();
	/**
	 * Robustly solve for the homography given a set of correspondences.
	 * p=src=ref,q=dest=query
	 */
	public boolean PreemptiveRobustHomography(HomographyMat H, FeaturePairStack matches,double i_width,double i_height)
	{
		PointSuffler ps=this._ps;
//		int[] tmp_i=new int[2 * num_points];
		HomographyMat[] hyp=this.mHyp;/* 9*max_num_hypotheses */
		CostPair[] hyp_costs=mHypCosts;
		double scale=mCauchyScale;
		int max_num_hypotheses=mMaxNumHypotheses;
		int max_trials=mMaxTrials;
		int chunk_size=mChunkSize;
		
		double one_over_scale2;
		double min_cost;
		int num_hypotheses, num_hypotheses_remaining, min_index;
		int cur_chunk_size, this_chunk_end;
		int trial;
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

		one_over_scale2 = 1 / (scale*scale);

		chunk_size = chunk_size<num_points?chunk_size:num_points;


		// Fill two arrays from [0,num_points)



		// Shuffle the indices
		ps.ArrayShuffle(num_points);


		// Compute a set of hypotheses
		for (trial = 0, num_hypotheses = 0; trial < max_trials && num_hypotheses < max_num_hypotheses; trial++) {

			// Shuffle the first SAMPLE_SIZE indices
			ps.ArrayShuffle(sample_size);


			// Check if the four points are geometrically valid
			if (!Homography4PointsGeometricallyConsistent(ps.indics[0],ps.indics[1],ps.indics[2],ps.indics[3])){
				continue;
			}/*
			 * boolean SolveHomography4Points(float[] H, float[] x1, float[] x2, float[] x3, float[] x4, float[] xp1,
			 * float[] xp2, float[] xp3, float[] xp4) {
			 */
			// Compute the homography
			if (!this._homography_solver.solveHomography4Points(ps.indics[0],ps.indics[1],ps.indics[2],ps.indics[3],hyp[num_hypotheses])) {
				continue;
			}
			// Check the test points
			if(!this._homography_check.geometricallyConsistent(hyp[num_hypotheses])){
				continue;
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
			cur_chunk_size = (chunk_size<num_points - i)?chunk_size:(num_points - i);

			// End of the current chunk
			this_chunk_end = i + cur_chunk_size;

			// Score each of the remaining hypotheses
			for (int j = 0; j < num_hypotheses_remaining; j++) {
				// const T* H_cur = &hyp[hyp_costs[j].second*9];
				HomographyMat ht=hyp[hyp_costs[j].second];
				double hf=0;
				for (int k = i; k < this_chunk_end; k++) {
					hf += ht.cauchyProjectiveReprojectionCost(ps.indics[k], one_over_scale2);
				}
				hyp_costs[j].first=hf;
			}

			// Cut out half of the hypotheses
			FastMedian(hyp_costs, num_hypotheses_remaining);
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
    /**
     * Check the geometric consistency between four correspondences.
     */
//    boolean Homography4PointsGeometricallyConsistent(const T x1[2], const T x2[2], const T x3[2], const T x4[2],const T x1p[2], const T x2p[2], const T x3p[2], const T x4p[2]) {
	private static boolean Homography4PointsGeometricallyConsistent(FeaturePairStack.Item p1, FeaturePairStack.Item p2, FeaturePairStack.Item p3, FeaturePairStack.Item p4)
	{
        if(((LinePointSide(p1.ref,p2.ref, p3.ref) > 0) ^ (LinePointSide(p1.query,p2.query,p3.query) > 0)) == true)
            return false;
        if(((LinePointSide(p2.ref, p3.ref, p4.ref) > 0) ^ (LinePointSide(p2.query, p3.query, p4.query) > 0)) == true)
            return false;
        if(((LinePointSide(p3.ref, p4.ref, p1.ref) > 0) ^ (LinePointSide(p3.query, p4.query, p1.query) > 0)) == true)
            return false;
        if(((LinePointSide(p4.ref, p1.ref, p2.ref) > 0) ^ (LinePointSide(p4.query, p1.query, p2.query) > 0)) == true)
            return false;
        return true;
    }		
	private static double LinePointSide(FreakFeaturePoint A, FreakFeaturePoint B,FreakFeaturePoint C) {
//		return ((B[0]-A[0])*(C[1]-A[1])-(B[1]-A[1])*(C[0]-A[0]));
		return ((B.x-A.x)*(C.y-A.y)-(B.y-A.y)*(C.x-A.x));
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
