package jp.nyatla.nyartoolkit.core.kpm.matcher;





import jp.nyatla.nyartoolkit.core.kpm.KpmInputDataSet;
import jp.nyatla.nyartoolkit.core.kpm.KpmResult;
import jp.nyatla.nyartoolkit.core.kpm.kpmMatching;
import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.DoGScaleInvariantDetector;
import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.DogFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.freak.FREAKExtractor;
import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.KeyframeMap;
import jp.nyatla.nyartoolkit.core.kpm.pyramid.BinomialPyramid32f;


import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;

import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * KpmPose6DOF
 */
public class FreakKeypointMatching {

	final private VisualDatabase freakMatcher;
	final NyARParam _ref_cparam;
	public KpmResult result;


	final private static double kLaplacianThreshold = 3;
	final private static double kEdgeThreshold = 4;
	final private static int kMaxNumFeatures = 300;
	final private static int kMinCoarseSize = 8;

	public FreakKeypointMatching(NyARParam i_ref_cparam)
	{
		NyARIntSize size=i_ref_cparam.getScreenSize();
		this.freakMatcher = new VisualDatabase(size.w, size.h);
		this._ref_cparam = i_ref_cparam;

		this.result =new KpmResult();
		this.mFeatureExtractor=new FREAKExtractor();
		int octerves=BinomialPyramid32f.octavesFromMinimumCoarsestSize(size.w,size.h,kMinCoarseSize);
		this._pyramid=new BinomialPyramid32f(size.w,size.h,octerves,3);
		this._dog_detector = new DoGScaleInvariantDetector(size.w,size.h,octerves,3,kLaplacianThreshold,kEdgeThreshold,kMaxNumFeatures);
	}
	
	final private DogFeaturePointStack _dog_feature_points = new DogFeaturePointStack(2000);// この2000は適当
	final FreakFeaturePointStack mQueryKeyframe=new FreakFeaturePointStack(2000);// この2000も適当
	/** Pyramid builder */
	final private BinomialPyramid32f _pyramid;
	/** Interest point detector (DoG, etc) */
	final private DoGScaleInvariantDetector _dog_detector;
	final private FREAKExtractor mFeatureExtractor;
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
	public void kpmMatching(KeyframeMap i_keymap,KpmResult i_result)
	{

	}
	public boolean kpmMatching(INyARGrayscaleRaster inImage,KeyframeMap i_keymap)
	{
		FreakFeaturePointStack query_keypoint = this.mQueryKeyframe;
		//Freak Extract

		// Build the pyramid		
		this._pyramid.build(inImage);
		// Detect feature points
		this._dog_feature_points.clear();	
		this._dog_detector.detect(this._pyramid,this._dog_feature_points);

		// Extract features
		query_keypoint.clear();
		this.mFeatureExtractor.extract(this._pyramid,this._dog_feature_points,query_keypoint);
		
		if(query_keypoint.isEmpty()){
			return false;
		}
		
		int matched_image_id=this.freakMatcher.query(query_keypoint,i_keymap);
		if(matched_image_id<0){
			return false;
		}

		FeaturePairStack matches = this.freakMatcher.inliers();
		return kpmMatching.kpmUtilGetPose_binary(this._ref_cparam, matches, this.result);

	}



}
