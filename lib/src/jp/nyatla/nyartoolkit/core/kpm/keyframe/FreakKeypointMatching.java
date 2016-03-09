package jp.nyatla.nyartoolkit.core.kpm.keyframe;



import jp.nyatla.nyartoolkit.base.attoolkit5.ARParamLT;
import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.kpm.KpmInputDataSet;
import jp.nyatla.nyartoolkit.core.kpm.KpmRefData;
import jp.nyatla.nyartoolkit.core.kpm.KpmResult;
import jp.nyatla.nyartoolkit.core.kpm.kpmMatching;
import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.DoGScaleInvariantDetector;
import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.DogFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.freak.FREAKExtractor;
import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.pyramid.BinomialPyramid32f;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FreakMatchPointSetStack;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.VisualDatabase;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.matchStack;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFreakFsetFile;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;

import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class FreakKeypointMatching {
	private final static int FREAK_SUB_DIMENSION = 96;
	final private VisualDatabase<FreakFeaturePointStack> freakMatcher;
	final ARParamLT cparamLT;
	int poseMode;
	int xsize;
	int ysize;
	int procMode;
	int detectedMaxFeature;
	KpmRefData[] refDataSet;
	KpmInputDataSet inDataSet = new KpmInputDataSet();
	public KpmResult result;
	int resultNum;
	final public static int KpmPose6DOF = 1;
	final public static int KpmPoseHomography = 2;

	public FreakKeypointMatching(ARParamLT cparamLT) {
		this(cparamLT, cparamLT.getScreenSize(), KpmPose6DOF);
	}
	private static double kLaplacianThreshold = 3;
	private static double kEdgeThreshold = 4;
	private static int kMaxNumFeatures = 300;
	private static int kMinCoarseSize = 8;

	public FreakKeypointMatching(ARParamLT cparamLT, NyARIntSize size, int poseMode) {
		this.freakMatcher = new VisualDatabase<FreakFeaturePointStack>(size.w, size.h);

		this.cparamLT = cparamLT;
		this.poseMode = poseMode;
		this.xsize = size.w;
		this.ysize = size.h;
		this.detectedMaxFeature = -1;

		this.refDataSet = null;

		this.inDataSet.coord = null;
		this.inDataSet.num = 0;

		this.result =new KpmResult();
		this.resultNum = 0;
		this.mFeatureExtractor=new FREAKExtractor();
		this.mPyramid=new BinomialPyramid32f(size.w,size.h,BinomialPyramid32f.octavesFromMinimumCoarsestSize(size.w,size.h,kMinCoarseSize));
		this.mDogDetector = new DoGScaleInvariantDetector(this.mPyramid,kLaplacianThreshold,kEdgeThreshold,kMaxNumFeatures);

	}
	final private DogFeaturePointStack _dog_feature_points = new DogFeaturePointStack(2000);// この2000は適当
	final FreakFeaturePointStack mQueryKeyframe=new FreakFeaturePointStack();
	/** Pyramid builder */
	final private BinomialPyramid32f mPyramid;
	/** Interest point detector (DoG, etc) */
	final private DoGScaleInvariantDetector mDogDetector;
	final private FREAKExtractor mFeatureExtractor;
	public boolean kpmMatching(INyARGrayscaleRaster inImage,KeyframeMap i_keymap)
	{
		FreakFeaturePointStack query_keypoint = this.mQueryKeyframe;
		query_keypoint.clear();
		//Freak Extract

		// Build the pyramid		
		this.mPyramid.build(inImage);
		// Detect feature points
		this.mDogDetector.detect(this.mPyramid,this._dog_feature_points);
		// Extract features
		this.mFeatureExtractor.extract(query_keypoint, this.mPyramid,this._dog_feature_points);		
		
		if(query_keypoint.isEmpty()){
			return false;
		}

		
		// LOG_INFO("Found %d features in query",
		// mQueryKeyframe->store().size());

		this.inDataSet.num  = (int)query_keypoint.getLength();
		this.inDataSet.coord = NyARDoublePoint2d.createArray(this.inDataSet.num);
		for (int i = 0; i < this.inDataSet.num; i++) {
			double x = query_keypoint.getItem(i).x;
			double y = query_keypoint.getItem(i).y;
			if (this.cparamLT != null) {
				NyARDoublePoint2d tmp = new NyARDoublePoint2d();
				this.cparamLT.arParamObserv2IdealLTf(x, y, tmp);
				this.inDataSet.coord[i].x = tmp.x;
				this.inDataSet.coord[i].y = tmp.y;
			} else {
				this.inDataSet.coord[i].x = x;
				this.inDataSet.coord[i].y = y;
			}
		}
		if(!this.freakMatcher.query(query_keypoint,i_keymap)){
			return false;
		}
		matchStack matches = this.freakMatcher.inliers();
		int matched_image_id = this.freakMatcher.matchedId();
		if (matched_image_id < 0){
			return false;
		}
		return kpmMatching.kpmUtilGetPose_binary(this.cparamLT, matches,i_keymap.get(matched_image_id).store(),query_keypoint, this.result);
	}



}
