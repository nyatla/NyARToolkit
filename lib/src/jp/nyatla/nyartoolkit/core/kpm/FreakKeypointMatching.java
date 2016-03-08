package jp.nyatla.nyartoolkit.core.kpm;



import jp.nyatla.nyartoolkit.base.attoolkit5.ARParamLT;
import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
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
	public KpmResult[] result;
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

		this.result = new KpmResult[1];
		this.result[0] = new KpmResult();
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
	public int kpmMatching(INyARGrayscaleRaster inImage)
	{
		int i;
		int ret;
		FreakFeaturePointStack query_keypoint = this.mQueryKeyframe;

		//Freak Extract

		// Build the pyramid		
		this.mPyramid.build(inImage);
		// Detect feature points
		this.mDogDetector.detect(this.mPyramid,this._dog_feature_points);
		// Extract features
		this.mFeatureExtractor.extract(query_keypoint, this.mPyramid,this._dog_feature_points);		
		
		
		// LOG_INFO("Found %d features in query",
		// mQueryKeyframe->store().size());

		this.freakMatcher.query(query_keypoint);
		this.inDataSet.num  = (int)query_keypoint.getLength();
		if (this.inDataSet.num != 0) {
			this.inDataSet.coord = NyARDoublePoint2d.createArray(this.inDataSet.num);
			for (i = 0; i < this.inDataSet.num; i++) {

				double x = query_keypoint.getItem(i).x;
				double y = query_keypoint.getItem(i).y;
//				for (j = 0; j < FREAK_SUB_DIMENSION; j++) {
//					featureVector.sf[i].v[j] = descriptors[i * FREAK_SUB_DIMENSION + j];
//				}
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

			for (int pageLoop = 0; pageLoop < this.resultNum; pageLoop++) {

				this.result[pageLoop].camPoseF = -1;
				matchStack matches = this.freakMatcher.inliers();
				int matched_image_id = this.freakMatcher.matchedId();
				if (matched_image_id < 0)
					continue;

				ret = kpmMatching.kpmUtilGetPose_binary(this.cparamLT, matches,
						this.freakMatcher.getKeyFeatureFrame(matched_image_id).store(),
						query_keypoint, this.result[pageLoop]);
				if (ret == 0) {
					this.result[pageLoop].camPoseF = 0;
					this.result[pageLoop].inlierNum = (int) matches.getLength();
				}
			}
		} else {
			for (i = 0; i < this.resultNum; i++) {
				this.result[i].camPoseF = -1;
			}
		}

		for (i = 0; i < this.resultNum; i++) {
			this.result[i].skipF = false;
		}

		return 0;
	}

	public int kpmSetRefDataSet(NyARNftFreakFsetFile i_refDataSet) {

		this.resultNum = 1;

		int db_id = 0;
		for (int k = 0; k < i_refDataSet.page_info.length; k++) {
			for (int m = 0; m < i_refDataSet.page_info[k].image_info.length; m++) {
				int image_no = i_refDataSet.page_info[k].image_info[m].image_no;
				int l=0;
				//格納予定のデータ数を数える
				for (int i = 0; i < i_refDataSet.ref_point.length; i++) {
					if (i_refDataSet.ref_point[i].refImageNo == image_no) {
						l++;
					}
				}
				FreakMatchPointSetStack fps = new FreakMatchPointSetStack(l);				
				for (int i = 0; i < i_refDataSet.ref_point.length; i++) {
					if (i_refDataSet.ref_point[i].refImageNo == image_no) {
						NyARNftFreakFsetFile.RefDataSet t = i_refDataSet.ref_point[i];
						FreakMatchPointSetStack.Item fp = fps.prePush();
						fp.x = t.coord2D.x;
						fp.y = t.coord2D.y;
						fp.angle = t.featureVec.angle;
						fp.scale = t.featureVec.scale;
						fp.maxima = t.featureVec.maxima > 0 ? true : false;
						if(i_refDataSet.ref_point[i].featureVec.v.length!=96){
							throw new NyARRuntimeException();
						}
						fp.descripter.setValueLe(i_refDataSet.ref_point[i].featureVec.v);
						fp.pos3d.x=t.coord3D.x;
						fp.pos3d.y=t.coord3D.y;
						fp.pos3d.z=0;
					}
				}
				this.freakMatcher.addFreakFeaturesAndDescriptors(fps,
						i_refDataSet.page_info[k].image_info[m].w, i_refDataSet.page_info[k].image_info[m].h, db_id++);
			}
		}
		return 0;
	}
}
