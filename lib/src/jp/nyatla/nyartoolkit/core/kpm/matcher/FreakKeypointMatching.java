package jp.nyatla.nyartoolkit.core.kpm.matcher;





import java.util.Map;

import jp.nyatla.nyartoolkit.core.icp.NyARIcpPlane;
import jp.nyatla.nyartoolkit.core.icp.NyARIcpPoint;
import jp.nyatla.nyartoolkit.core.kpm.KpmResult;
import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.DoGScaleInvariantDetector;
import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.DogFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.freak.FREAKExtractor;
import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.FreakMatchPointSetStack;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.Keyframe;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.KeyframeMap;
import jp.nyatla.nyartoolkit.core.kpm.matcher.homography_estimation.RobustHomography;
import jp.nyatla.nyartoolkit.core.kpm.pyramid.BinomialPyramid32f;


import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

/**
 * KpmPose6DOF
 */
public class FreakKeypointMatching {

	final private NyARParam _ref_cparam;
	public KpmResult result;


	final private static double kLaplacianThreshold = 3;
	final private static double kEdgeThreshold = 4;
	final private static int kMaxNumFeatures = 300;
	final private static int kMinCoarseSize = 8;

	final private DogFeaturePointStack _dog_feature_points = new DogFeaturePointStack(2000);// この2000は適当
	final FreakFeaturePointStack mQueryKeyframe=new FreakFeaturePointStack(2000);// この2000も適当
	/** Pyramid builder */
	final private BinomialPyramid32f _pyramid;
	/** Interest point detector (DoG, etc) */
	final private DoGScaleInvariantDetector _dog_detector;
	final private FREAKExtractor mFeatureExtractor;

	private static int kHomographyInlierThreshold = 3;
	private static int kMinNumInliers = 8;
	private static boolean kUseFeatureIndex = true;

	private int mMinNumInliers;
	//
	// Set to true if the feature index is enabled
	private boolean mUseFeatureIndex;


	/** Feature matcher */
	final BinaryFeatureMatcher mMatcher=new BinaryFeatureMatcher();
	// Robust homography estimation
	final RobustHomography mRobustHomography=new RobustHomography();
	final private HomographyMat _H = new HomographyMat();
	final private InverseHomographyMat _hinv=new InverseHomographyMat();
	
	
	/** Similarity voter*/
	final private HoughSimilarityVoting mHoughSimilarityVoting;
	final private FindInliers _find_inliner;
		
	
	public FreakKeypointMatching(NyARParam i_ref_cparam)
	{
		NyARIntSize size=i_ref_cparam.getScreenSize();

		this._ref_cparam = i_ref_cparam;

		this.result =new KpmResult();
		this.mFeatureExtractor=new FREAKExtractor();
		int octerves=BinomialPyramid32f.octavesFromMinimumCoarsestSize(size.w,size.h,kMinCoarseSize);
		this._pyramid=new BinomialPyramid32f(size.w,size.h,octerves,3);
		this._dog_detector = new DoGScaleInvariantDetector(size.w,size.h,octerves,3,kLaplacianThreshold,kEdgeThreshold,kMaxNumFeatures);
		
		this.mMinNumInliers = kMinNumInliers;
		this.mUseFeatureIndex = kUseFeatureIndex;
		//

		this._tmp_pair_stack[0]=new FeaturePairStack(300);
		this._tmp_pair_stack[1]=new FeaturePairStack(300);
		this._find_inliner=new FindInliers(kHomographyInlierThreshold);		
		double dx = size.w + (size.w * 0.2f);
		double dy = size.h + (size.h * 0.2f);
		this.mHoughSimilarityVoting=new HoughSimilarityVoting(-dx, dx, -dy, dy, 0, 0, 12, 10);		
	}
	

	public void update(INyARGrayscaleRaster in_image)
	{
		FreakFeaturePointStack query_keypoint = this.mQueryKeyframe;
		//Freak Extract

		// Build the pyramid		
		this._pyramid.build(in_image);
		// Detect feature points
		this._dog_feature_points.clear();	
		this._dog_detector.detect(this._pyramid,this._dog_feature_points);

		// Extract features
		query_keypoint.clear();
		this.mFeatureExtractor.extract(this._pyramid,this._dog_feature_points,query_keypoint);		
	}
	public boolean kpmMatching(KeyframeMap i_keymap,KpmResult i_result)
	{
		FeaturePairStack result=new FeaturePairStack(100);	
		if(!this.query(this.mQueryKeyframe,i_keymap,result)){
			return false;
		}
		return kpmUtilGetPose_binary(this._ref_cparam, result,i_result);
	}
	


	/**
	 * 2chの一時バッファ
	 */
	final private FeaturePairStack[] _tmp_pair_stack=new FeaturePairStack[2];


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
			match_result.clear();
			if (mMatcher.match(query_keyframe, ref_points, hinv, 10,match_result) < mMinNumInliers) {
				continue;
			}

			//
			// Vote for a similarity with new matches
			if(!this.mHoughSimilarityVoting.extractMatches(match_result,second.width(), second.height())){
				continue;
			}

			//
			// Re-estimate the homography
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

	private static boolean kpmUtilGetPose_binary(NyARParam i_cparam, FeaturePairStack matchData,KpmResult kpmResult)
	{
		// ICPHandleT *icpHandle;
		// ICPDataT icpData;
		// ICP2DCoordT *sCoord;
		// ICP3DCoordT *wCoord;
		NyARDoubleMatrix44 initMatXw2Xc = new NyARDoubleMatrix44();
		// ARdouble err;
		int i;

		if (matchData.getLength() < 4) {
			return false;
		}
		NyARDoublePoint2d[] sCoord = NyARDoublePoint2d.createArray(matchData.getLength());
		NyARDoublePoint3d[] wCoord = NyARDoublePoint3d.createArray(matchData.getLength());
		for (i = 0; i < matchData.getLength(); i++) {
			sCoord[i].x = matchData.getItem(i).query.x;
			sCoord[i].y = matchData.getItem(i).query.y;

			wCoord[i].x = matchData.getItem(i).ref.pos3d.x;
			wCoord[i].y = matchData.getItem(i).ref.pos3d.y;
			wCoord[i].z = 0.0;
		}


		NyARIcpPlane icp_planer = new NyARIcpPlane(i_cparam.getPerspectiveProjectionMatrix());
		if (!icp_planer.icpGetInitXw2Xc_from_PlanarData(sCoord, wCoord, matchData.getLength(), initMatXw2Xc)) {
			return false;
		}
		/*
		 * printf("--- Init pose ---\n"); for( int j = 0; j < 3; j++ ) { for( i = 0; i < 4; i++ ) printf(" %8.3f",
		 * initMatXw2Xc[j][i]); printf("\n"); }
		 */
		NyARIcpPoint icp_point = new NyARIcpPoint(i_cparam.getPerspectiveProjectionMatrix());
		icp_point.icpPoint(sCoord, wCoord, matchData.getLength(), initMatXw2Xc, kpmResult.camPose,
				kpmResult.resultparams);

		if (kpmResult.resultparams.last_error > 10.0f) {
			return false;
		}

		return true;
	}	



}
