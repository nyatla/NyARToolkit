package jp.nyatla.nyartoolkit.core.kpm.matcher;





import jp.nyatla.nyartoolkit.core.icp.NyARIcpPlane;
import jp.nyatla.nyartoolkit.core.icp.NyARIcpPoint;
import jp.nyatla.nyartoolkit.core.kpm.KpmResult;
import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.DoGScaleInvariantDetector;
import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.DogFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.freak.FREAKExtractor;
import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.KeyframeMap;
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

	final private VisualDatabase freakMatcher;
	final NyARParam _ref_cparam;
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
		if(!this.freakMatcher.query(this.mQueryKeyframe,i_keymap,result)){
			return false;
		}
		return kpmUtilGetPose_binary(this._ref_cparam, result,i_result);
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
