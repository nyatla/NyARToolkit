package jp.nyatla.nyartoolkit.core.kpm.matcher;

import java.util.Map;

import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.FreakMatchPointSetStack;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.FreakMatchPointSetStack.Item;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.Keyframe;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.KeyframeMap;
import jp.nyatla.nyartoolkit.core.kpm.matcher.homography_estimation.RobustHomography;
import jp.nyatla.nyartoolkit.core.kpm.matcher.homography_estimation.geometry;
import jp.nyatla.nyartoolkit.core.kpm.matcher.homography_estimation.homography;


import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

public class VisualDatabase
{
	private static int kHomographyInlierThreshold = 3;
	private static int kMinNumInliers = 8;

	private static boolean kUseFeatureIndex = true;
	private int mMinNumInliers;
	private double mHomographyInlierThreshold;
	//
	// Set to true if the feature index is enabled
	private boolean mUseFeatureIndex;


	/** Feature matcher */
	final BinaryFeatureMatcher mMatcher=new BinaryFeatureMatcher();

	/** Similarity voter*/
	final private HoughSimilarityVoting mHoughSimilarityVoting;
	
	
	public VisualDatabase(int i_width,int i_height)
	{		
		this.mHomographyInlierThreshold = kHomographyInlierThreshold;
		this.mMinNumInliers = kMinNumInliers;
		this.mUseFeatureIndex = kUseFeatureIndex;
		//
		double dx = i_width + (i_width * 0.2f);
		double dy = i_height + (i_height * 0.2f);
		this.mHoughSimilarityVoting=new HoughSimilarityVoting(-dx, dx, -dy, dy, 0, 0, 12, 10);		
		this._tmp_pair_stack[0]=new FeaturePairStack(300);
		this._tmp_pair_stack[1]=new FeaturePairStack(300);
		return;
	}

	/**
	 * 2chの一時バッファ
	 */
	private final FeaturePairStack[] _tmp_pair_stack=new FeaturePairStack[2];

	public boolean query(FreakFeaturePointStack query_keyframe,KeyframeMap i_keymap,FeaturePairStack i_result)
	{
		// mMatchedInliers.clear();
		HomographyMat H = new HomographyMat();
		
		int num_of_query_frame=query_keyframe.getLength();
		//ワークエリアの設定
		if(num_of_query_frame>this._tmp_pair_stack[0].getArraySize()){
			this._tmp_pair_stack[0]=new FeaturePairStack(num_of_query_frame+10);
			this._tmp_pair_stack[1]=new FeaturePairStack(num_of_query_frame+10);
		}
		int tmp_ch=0;
		int last_inliers=0;
		
		for (Map.Entry<Integer, Keyframe> i : i_keymap.entrySet())
		{
			Keyframe second = i.getValue();
			FreakMatchPointSetStack ref_points = second.getFeaturePointSet();
			//新しいワークエリアを作る。
			FeaturePairStack match_result=this._tmp_pair_stack[tmp_ch];
			match_result.clear();
			if (mUseFeatureIndex) {
				if (mMatcher.match(query_keyframe,ref_points,second.getIndex(),match_result) < this.mMinNumInliers) {
					continue;
				}
			} else {
				if (mMatcher.match(query_keyframe,ref_points,match_result) < mMinNumInliers) {
					continue;
				}
			}

			// Vote for a transformation based on the correspondences
			if(!this.mHoughSimilarityVoting.extractHoughMatches(match_result,second.width(), second.height())){
				continue;
			}
			

			// Estimate the transformation between the two images
			if (!EstimateHomography(H, match_result,second.width(), second.height())) {
				continue;
			}
			
			//ここでHInv計算
			InverseHomographyMat hinv=new InverseHomographyMat();
			if(!hinv.inverse(H)){
				continue;
			}

			// Apply some heuristics to the homography
			if (!hinv.checkHomographyHeuristics(second.width(), second.height())) {
				continue;
			}
			
			//
			// Find the inliers
			//

			FindInliers(H, match_result,mHomographyInlierThreshold);
			if (match_result.getLength() < mMinNumInliers) {
				continue;
			}

			//
			// Use the estimated homography to find more inliers
			//

			match_result.clear();
			if (mMatcher.match(query_keyframe, ref_points, hinv, 10,match_result) < mMinNumInliers) {
				continue;
			}

			//
			// Vote for a similarity with new matches
			//

			// TIMED("Hough Voting (2)") {
			if(!this.mHoughSimilarityVoting.extractHoughMatches(match_result,second.width(), second.height())){
				continue;
			}

			//
			// Re-estimate the homography
			//

			if (!EstimateHomography(H, match_result,second.width(), second.height())) {
				continue;
			}
			// Apply some heuristics to the homography
			if(!hinv.inverse(H)){
				continue;
			}
			if (!hinv.checkHomographyHeuristics(second.width(), second.height())) {
				continue;
			}
			//
			// Check if this is the best match based on number of inliers
			//

			FindInliers(H, match_result,mHomographyInlierThreshold);

			//ポイント数が最小値より大きい&&最高成績ならテンポラリチャンネルを差し替える。
			if (match_result.getLength() >= mMinNumInliers && match_result.getLength() > last_inliers) {
				//出力チャンネルを切り替え
				tmp_ch=(tmp_ch+1)%2;
				last_inliers=match_result.getLength();
			}
		}
		//出力は last_inlines>0の場合に[(tmp_ch+1)%2]にある。
		if(last_inliers<=0){
			return false;
		}
		FeaturePairStack match_result=this._tmp_pair_stack[(tmp_ch+1)%2];
		FeaturePairStack.Item[] dest=match_result.getArray();
		for(int i=0;i<match_result.getLength();i++){
			FeaturePairStack.Item t=i_result.prePush();
			if(t==null){
				System.out.println("Push overflow!");
				break;
			}
			t.query=dest[i].query;
			t.ref=dest[i].ref;
		}

		return true;
	}
	// Robust homography estimation
	final RobustHomography mRobustHomography=new RobustHomography();

	/**
	 * Find the inliers given a homography and a set of correspondences.
	 */
	private void FindInliers(HomographyMat H, FeaturePairStack matches, double threshold) {
		double threshold2 = (threshold*threshold);
		NyARDoublePoint2d xp = new NyARDoublePoint2d();// float xp[2];
		//前方詰め

		int pos=0;
		for (int i = 0; i < matches.getLength(); i++) {


			MultiplyPointHomographyInhomogenous(xp, H,matches.getItem(i).ref.x,matches.getItem(i).ref.y);
			double t1=xp.x- matches.getItem(i).query.x;
			double t2=xp.y- matches.getItem(i).query.y;

			double d2 = (t1*t1)+ (t2*t2);
			if (d2 <= threshold2) {
				matches.swap(i,pos);
				pos++;
			
			}
		}
		matches.setLength(pos);
		return;
	}
    /**
     * Multiply an in-homogenous point by a similarity.
     * H[9] この関数は実装済みだからあとで消す。
     */
    private static void MultiplyPointHomographyInhomogenous(NyARDoublePoint2d v, NyARDoubleMatrix33 H, double x, double y) {
    	double w = H.m20*x + H.m21*y + H.m22;
        v.x = (H.m00*x + H.m01*y + H.m02)/w;//XP
        v.y = (H.m10*x + H.m11*y + H.m12)/w;//YP
    }

	
	

	/**
	 * Estimate the homography between a set of correspondences.
	 */
	private boolean EstimateHomography(HomographyMat H,FeaturePairStack matches, int refWidth, int refHeight) {

		NyARDoublePoint2d[] srcPoints = NyARDoublePoint2d.createArray(matches.getLength());
		NyARDoublePoint2d[] dstPoints = NyARDoublePoint2d.createArray(matches.getLength());
	
		
		//
		// Copy correspondences
		//

		for (int i = 0; i < matches.getLength(); i++) {
			dstPoints[i].x = matches.getItem(i).query.x;
			dstPoints[i].y = matches.getItem(i).query.y;
			srcPoints[i].x = matches.getItem(i).ref.x;
			srcPoints[i].y = matches.getItem(i).ref.y;
		}

		//
		// Compute the homography
		//
		// if(!estimator.find(H, (float*)&srcPoints[0], (float*)&dstPoints[0],
		// (int)matches.size(), test_points, 4)) {
//		if (!this.mRobustHomography.find(H, srcPoints, dstPoints, (int) matches.getLength(),refWidth,refHeight)) {		
		if (!this.mRobustHomography.PreemptiveRobustHomography(H, matches,refWidth,refHeight))
		{
			return false;
		}



		return true;
	}


}
