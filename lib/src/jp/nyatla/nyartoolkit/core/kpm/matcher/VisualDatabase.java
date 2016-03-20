package jp.nyatla.nyartoolkit.core.kpm.matcher;

import java.util.Map;

import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.FreakMatchPointSetStack;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.Keyframe;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.KeyframeMap;
import jp.nyatla.nyartoolkit.core.kpm.matcher.homography_estimation.RobustHomography;


import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;


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
	// Robust homography estimation
	final RobustHomography mRobustHomography=new RobustHomography();

	/** Similarity voter*/
	final private HoughSimilarityVoting mHoughSimilarityVoting;
	final private FindInliers _find_inliner;
	
	public VisualDatabase(int i_width,int i_height)
	{		
		this.mMinNumInliers = kMinNumInliers;
		this.mUseFeatureIndex = kUseFeatureIndex;
		//
		double dx = i_width + (i_width * 0.2f);
		double dy = i_height + (i_height * 0.2f);
		this.mHoughSimilarityVoting=new HoughSimilarityVoting(-dx, dx, -dy, dy, 0, 0, 12, 10);		
		this._tmp_pair_stack[0]=new FeaturePairStack(300);
		this._tmp_pair_stack[1]=new FeaturePairStack(300);
		this._find_inliner=new FindInliers(kHomographyInlierThreshold);
		return;
	}

	/**
	 * 2chの一時バッファ
	 */
	final private FeaturePairStack[] _tmp_pair_stack=new FeaturePairStack[2];
	final private HomographyMat _H = new HomographyMat();
	final private InverseHomographyMat _hinv=new InverseHomographyMat();

	public boolean query(FreakFeaturePointStack query_keyframe,KeyframeMap i_keymap,FeaturePairStack i_result)
	{
		// mMatchedInliers.clear();
		HomographyMat H = this._H;
		InverseHomographyMat hinv=this._hinv;
		
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
			if(!this.mHoughSimilarityVoting.extractMatches(match_result,second.width(), second.height())){
				continue;
			}
			

			// Estimate the transformation between the two images
			if (!this.mRobustHomography.PreemptiveRobustHomography(H, match_result,second.width(), second.height())) {
				continue;
			}
			
			//ここでHInv計算
			if(!hinv.inverse(H)){
				continue;
			}

			// Apply some heuristics to the homography
			if (!hinv.checkHomographyHeuristics(second.width(), second.height())) {
				continue;
			}
			
			// Find the inliers
			this._find_inliner.extructMatches(H, match_result);
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
			if(!this.mHoughSimilarityVoting.extractMatches(match_result,second.width(), second.height())){
				continue;
			}

			//
			// Re-estimate the homography
			//

			if (!this.mRobustHomography.PreemptiveRobustHomography(H, match_result,second.width(), second.height())) {
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
			this._find_inliner.extructMatches(H, match_result);

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

}
