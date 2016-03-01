package jp.nyatla.nyartoolkit.core.kpm.vision.matchers;

import java.util.Map;

import jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid.DoGScaleInvariantDetector;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.Keyframe;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.KeyframeMap;
import jp.nyatla.nyartoolkit.core.kpm.pyramid.BinomialPyramid32f;
import jp.nyatla.nyartoolkit.core.kpm.pyramid.GaussianScaleSpacePyramid;
import jp.nyatla.nyartoolkit.core.kpm.vision.homography_estimation.RobustHomography;
import jp.nyatla.nyartoolkit.core.kpm.vision.math.geometry;
import jp.nyatla.nyartoolkit.core.kpm.vision.math.math_utils;
import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

public class VisualDatabase<STORE extends FreakFeaturePointStack>
{
	private static double kLaplacianThreshold = 3;
	private static double kEdgeThreshold = 4;
	private static int kMaxNumFeatures = 300;
	private static int kMinCoarseSize = 8;

	private static int kHomographyInlierThreshold = 3;
	private static int kMinNumInliers = 8;

	private static double kHoughBinDelta = 1;


	private static boolean kUseFeatureIndex = true;


	/** Pyramid builder */
	final private BinomialPyramid32f mPyramid;
	/** Interest point detector (DoG, etc) */
	final private DoGScaleInvariantDetector mDetector;
	
	
	
	public VisualDatabase(int i_width,int i_height)
	{
		this.mPyramid=new BinomialPyramid32f(
			i_width,i_height,
			BinomialPyramid32f.octavesFromMinimumCoarsestSize(i_width,i_height,kMinCoarseSize));
		this.mDetector = new DoGScaleInvariantDetector(this.mPyramid,kLaplacianThreshold,kEdgeThreshold,kMaxNumFeatures);
//		this.mDetector.setLaplacianThreshold(kLaplacianThreshold);
//		this.mDetector.setEdgeThreshold(kEdgeThreshold);
//		this.mDetector.setMaxNumFeaturePoints(kMaxNumFeatures);

		this.mHomographyInlierThreshold = kHomographyInlierThreshold;
		this.mMinNumInliers = kMinNumInliers;

		this.mUseFeatureIndex = kUseFeatureIndex;
		this.mFeatureExtractor=new FREAKExtractor();
	}

	/**
	 * Find feature points in an image.
	 */
	static void FindFeatures(FreakFeaturePointStack keyframe, GaussianScaleSpacePyramid pyramid,
			DoGScaleInvariantDetector detector, FREAKExtractor extractor) {
		// ASSERT(pyramid, "Pyramid is NULL");
		// ASSERT(detector, "Detector is NULL");
		// ASSERT(pyramid->images().size() > 0, "Pyramid is empty");
		// ASSERT(pyramid->images()[0].width() == detector->width(),
		// "Pyramid and detector size mismatch");
		// ASSERT(pyramid->images()[0].height() == detector->height(),
		// "Pyramid and detector size mismatch");

		//
		// Detect feature points
		//

		detector.detect(pyramid);

		//
		// Copy the points
		//

//		FeaturePoint[] points = new FeaturePoint[detector.features()
//				.getLength()];
//		for (int i = 0; i < detector.features().getLength(); i++) {
//			DoGScaleInvariantDetector.DogFeaturePoint p = detector.features().getItem(i);
//			points[i] = new FeaturePoint(p.x, p.y, p.angle, p.sigma,
//					p.score > 0);
//		}

		//
		// Extract features
		//

		extractor.extract(keyframe, pyramid, detector.features());
	}
    /**
     * @return Query store
     */
    public FreakFeaturePointStack queryKeyframe(){ return mQueryKeyframe; }

	public boolean query(INyARGrayscaleRaster image) {
		// Allocate pyramid
		if(!image.getSize().isEqualSize(this.mPyramid.images()[0].getWidth(),this.mPyramid.images()[0].getHeight())){
			throw new NyARRuntimeException();
		}
		// Build the pyramid		
		mPyramid.build(image);

		return query(mPyramid);
	}

	boolean query(GaussianScaleSpacePyramid pyramid) {
		// Allocate detector
		if (this.mDetector.width() != pyramid.images()[0].getWidth()
				|| this.mDetector.height() != pyramid.images()[0].getHeight()) {
			throw new NyARRuntimeException();
		}

		// Find the features on the image
		this.mQueryKeyframe = new FreakFeaturePointStack();// .reset(new keyframe_t());
		FindFeatures(this.mQueryKeyframe, pyramid, this.mDetector,
				this.mFeatureExtractor);
		// LOG_INFO("Found %d features in query",
		// mQueryKeyframe->store().size());

		return this.query(mQueryKeyframe,pyramid.images()[0].getSize());
	}

	/**
	 * Vote for a similarity transformation.
	 */
	int FindHoughSimilarity(HoughSimilarityVoting hough, FreakFeaturePointStack p1,
			FreakMatchPointSetStack p2, matchStack matches, int insWidth,
			int insHeigth, int refWidth, int refHeight) {
		double[] query = new double[4 * matches.getLength()];
		double[] ref = new double[4 * matches.getLength()];

		// Extract the data from the features
		for (int i = 0; i < matches.getLength(); i++) {
			FreakFeaturePoint query_point = p1.getItem(matches.getItem(i).ins);
			FreakFeaturePoint ref_point = p2.getItem(matches.getItem(i).ref);

			int q_ptr = i * 4;
			query[q_ptr + 0] = query_point.x;
			query[q_ptr + 1] = query_point.y;
			query[q_ptr + 2] = query_point.angle;
			query[q_ptr + 3] = query_point.scale;

			int r_ptr = i * 4;
			ref[r_ptr + 0] = ref_point.x;
			ref[r_ptr + 1] = ref_point.y;
			ref[r_ptr + 2] = ref_point.angle;
			ref[r_ptr + 3] = ref_point.scale;
		}

		double dx = insWidth + (insWidth * 0.2f);
		double dy = insHeigth + (insHeigth * 0.2f);

		hough.init(-dx, dx, -dy, dy, 0, 0, 12, 10);
		hough.setObjectCenterInReference(refWidth >> 1, refHeight >> 1);
		hough.setRefImageDimensions(refWidth, refHeight);
		// hough.vote((float*)&query[0], (float*)&ref[0], (int)matches.size());
		hough.vote(query, ref, matches.getLength());

		HoughSimilarityVoting.getMaximumNumberOfVotesResult max = new HoughSimilarityVoting.getMaximumNumberOfVotesResult();
		hough.getMaximumNumberOfVotes(max);

		return (max.votes < 3) ? -1 : max.index;
	}

	final static int SIZEDEF_matchStack = 9999;

	boolean query(FreakFeaturePointStack query_keyframe,NyARIntSize i_size) {
		// mMatchedInliers.clear();
		this. mMatchedId = -1;
		int last_inliers=0;

		FreakFeaturePointStack query_points = query_keyframe;
		// Loop over all the images in the database
		// typename keyframe_map_t::const_iterator it = mKeyframeMap.begin();
		// for(; it != mKeyframeMap.end(); it++) {
		for (Map.Entry<Integer, Keyframe> i : mKeyframeMap.entrySet()) {
			Keyframe second = i.getValue();
			int first = i.getKey();
			// TIMED("Find Matches (1)") {
			if (mUseFeatureIndex) {
				if (mMatcher.match(query_keyframe, second.store(),second.index()) < this.mMinNumInliers) {
					continue;
				}
			} else {
				if (mMatcher.match(query_keyframe, second.store()) < mMinNumInliers) {
					continue;
				}
			}
			// }

			FreakMatchPointSetStack ref_points = second.store();
			// std::cout<<"ref_points-"<<ref_points.size()<<std::endl;
			// std::cout<<"query_points-"<<query_points.size()<<std::endl;

			//
			// Vote for a transformation based on the correspondences
			//

			int max_hough_index = -1;
			// TIMED("Hough Voting (1)") {
			max_hough_index = FindHoughSimilarity(mHoughSimilarityVoting,
					query_points, ref_points, mMatcher.matches(),
					i_size.w, i_size.h,
					second.width(), second.height());
			if (max_hough_index < 0) {
				continue;
			}
			// }

			matchStack hough_matches = new matchStack(SIZEDEF_matchStack);
			// TIMED("Find Hough Matches (1)") {
			FindHoughMatches(hough_matches, mHoughSimilarityVoting,
					query_points, ref_points, mMatcher.matches(),
					max_hough_index, kHoughBinDelta);
			// }

			//
			// Estimate the transformation between the two images
			//

			HomographyMat H = new HomographyMat();
			// TIMED("Estimate Homography (1)") {
			if (!EstimateHomography(H, query_points, ref_points, hough_matches,
					mHomographyInlierThreshold, mRobustHomography,
					second.width(), second.height())) {
				continue;
			}
			// }

			//
			// Find the inliers
			//

			matchStack inliers = new matchStack(SIZEDEF_matchStack);
			// TIMED("Find Inliers (1)") {
			FindInliers(inliers, H, query_points, ref_points, hough_matches,
					mHomographyInlierThreshold);
			if (inliers.getLength() < mMinNumInliers) {
				continue;
			}
			// }

			//
			// Use the estimated homography to find more inliers
			//

			// TIMED("Find Matches (2)") {
			if (mMatcher.match(query_keyframe, second.store(), H, 10) < mMinNumInliers) {
				continue;
			}
			// }

			//
			// Vote for a similarity with new matches
			//

			// TIMED("Hough Voting (2)") {
			max_hough_index = FindHoughSimilarity(mHoughSimilarityVoting,
					query_points, ref_points, mMatcher.matches(),
					i_size.w,i_size.h,
					second.width(), second.height());
			if (max_hough_index < 0) {
				continue;
			}
			// }

			// TIMED("Find Hough Matches (2)") {
			FindHoughMatches(hough_matches, mHoughSimilarityVoting,
					query_points, ref_points, mMatcher.matches(),
					max_hough_index, kHoughBinDelta);
			// }

			//
			// Re-estimate the homography
			//

			// TIMED("Estimate Homography (2)") {
			if (!EstimateHomography(H, query_points, ref_points, hough_matches,
					mHomographyInlierThreshold, mRobustHomography,
					second.width(), second.height())) {
				continue;
			}
			// }

			//
			// Check if this is the best match based on number of inliers
			//

			inliers.clear();
			// TIMED("Find Inliers (2)") {
			FindInliers(inliers, H, query_points, ref_points, hough_matches,
					mHomographyInlierThreshold);
			// }

			// std::cout<<"inliers-"<<inliers.size()<<std::endl;
			if (inliers.getLength() >= mMinNumInliers && inliers.getLength() > last_inliers) {
//				indexing.CopyVector(mMatchedGeometry, 0, H, 0, 9);
				H.getValue(mMatchedGeometry);
				// CopyVector9(mMatchedGeometry, H);
				//現状は毎回生成してるからセットで。
				mMatchedInliers = inliers;// mMatchedInliers.swap(inliers);
				last_inliers=inliers.getLength();
				mMatchedId = first;
			}
		}

		return mMatchedId >= 0;
	}

	public int mMatchedId;

	/**
	 * Find the inliers given a homography and a set of correspondences.
	 */
	void FindInliers(matchStack inliers, NyARDoubleMatrix33 H, FreakFeaturePointStack p1,
			FreakMatchPointSetStack p2, matchStack matches, double threshold) {
		double threshold2 = (threshold*threshold);
		NyARDoublePoint2d xp = new NyARDoublePoint2d();// float xp[2];
		// reserve(matches.size());
		for (int i = 0; i < matches.getLength(); i++) {
			homography.MultiplyPointHomographyInhomogenous(xp, H,p2.getItem(matches.getItem(i).ref).x,p2.getItem(matches.getItem(i).ref).y);
			double t1=xp.x- p1.getItem(matches.getItem(i).ins).x;
			double t2=xp.y- p1.getItem(matches.getItem(i).ins).y;

			double d2 = (t1*t1)+ (t2*t2);
			if (d2 <= threshold2) {
				match_t t = inliers.prePush();
				t.set(matches.getItem(i));
			}
		}
	}

	/**
	 * Get only the matches that are consistent based on the hough votes.
	 */
	void FindHoughMatches(matchStack out_matches, HoughSimilarityVoting hough,
			FreakFeaturePointStack p1, FreakMatchPointSetStack p2, matchStack in_matches,
			int binIndex, double binDelta) {

		HoughSimilarityVoting.Bins bin = hough.getBinsFromIndex(binIndex);

		out_matches.clear();

		int n = (int) hough.getSubBinLocationIndices().length;
		// const float* vote_loc = hough.getSubBinLocations().data();
		double[] vote_loc = hough.getSubBinLocations();// .data();
		int vote_ptr = 0;
		// ASSERT(n <= in_matches.size(), "Should be the same");
		HoughSimilarityVoting.mapCorrespondenceResult d = new HoughSimilarityVoting.mapCorrespondenceResult();
		for (int i = 0; i < n; i++, vote_ptr += 4) {
			hough.getBinDistance(d, vote_loc[vote_ptr + 0],
					vote_loc[vote_ptr + 1], vote_loc[vote_ptr + 2],
					vote_loc[vote_ptr + 3], bin.binX + .5f, bin.binY + .5f,
					bin.binAngle + .5f, bin.binScale + .5f);

			if (d.x < binDelta && d.y < binDelta && d.angle < binDelta
					&& d.scale < binDelta) {
				int idx = hough.getSubBinLocationIndices()[i];
				// out_matches.push_back(in_matches[idx]);
				match_t t = out_matches.prePush();
				t.set(in_matches.getItem(idx));
			}
		}
	}

	/**
	 * Estimate the homography between a set of correspondences.
	 */
	boolean EstimateHomography(HomographyMat H, FreakFeaturePointStack p1,
			FreakMatchPointSetStack p2, matchStack matches, double threshold,
			RobustHomography estimator, int refWidth, int refHeight) {

		NyARDoublePoint2d[] srcPoints = NyARDoublePoint2d.createArray(matches.getLength());
		NyARDoublePoint2d[] dstPoints = NyARDoublePoint2d.createArray(matches.getLength());

		//
		// Copy correspondences
		//

		for (int i = 0; i < matches.getLength(); i++) {
			dstPoints[i].x = p1.getItem(matches.getItem(i).ins).x;
			dstPoints[i].y = p1.getItem(matches.getItem(i).ins).y;
			srcPoints[i].x = p2.getItem(matches.getItem(i).ref).x;
			srcPoints[i].y = p2.getItem(matches.getItem(i).ref).y;
		}

		//
		// Create test points for geometric verification
		//

		NyARDoublePoint2d[] test_points = NyARDoublePoint2d.createArray(8);
		test_points[0].x = 0;
		test_points[0].y = 0;
		test_points[1].x = refWidth;
		test_points[1].y = 0;
		test_points[2].x = refWidth;
		test_points[2].y = refHeight;
		test_points[3].x = 0;
		test_points[3].y = refHeight;

		//
		// Compute the homography
		//
		// if(!estimator.find(H, (float*)&srcPoints[0], (float*)&dstPoints[0],
		// (int)matches.size(), test_points, 4)) {
		if (!estimator.find(H, srcPoints, dstPoints, (int) matches.getLength(),
				test_points, 4)) {
			return false;
		}

		//
		// Apply some heuristics to the homography
		//

		if (!CheckHomographyHeuristics(H, refWidth, refHeight)) {
			return false;
		}

		return true;
	}

	/**
	 * Check if a homography is valid based on some heuristics.
	 */
	// boolean CheckHomographyHeuristics(float H[9], int refWidth, int
	// refHeight) {
	boolean CheckHomographyHeuristics(NyARDoubleMatrix33 H, int refWidth, int refHeight) {
		NyARDoublePoint2d p0p = new NyARDoublePoint2d();
		NyARDoublePoint2d p1p = new NyARDoublePoint2d();
		NyARDoublePoint2d p2p = new NyARDoublePoint2d();
		NyARDoublePoint2d p3p = new NyARDoublePoint2d();

		HomographyMat hinv=new HomographyMat();
		if(!hinv.inverse(H)){
			return false;
		}
		hinv.multiplyPointHomographyInhomogenous(0,0,p0p);
		hinv.multiplyPointHomographyInhomogenous(refWidth,0,p1p);
		hinv.multiplyPointHomographyInhomogenous(refWidth,refHeight,p2p);
		hinv.multiplyPointHomographyInhomogenous(0,refHeight,p3p);

		double tr = refWidth * refHeight * 0.0001f;
		if (geometry.SmallestTriangleArea(p0p, p1p, p2p, p3p) < tr) {
			return false;
		}

		if (!geometry.QuadrilateralConvex(p0p, p1p, p2p, p3p)) {
			return false;
		}

		return true;
	}
	public matchStack inliers() {
		return this.mMatchedInliers;
	}

	 /**
	 * Get the mathced id.
	 */
	public int matchedId() {
		return mMatchedId;
	}	
	


	private int mMinNumInliers;
	private double mHomographyInlierThreshold;
	//
	// Set to true if the feature index is enabled
	private boolean mUseFeatureIndex;

	//
	matchStack mMatchedInliers;
	// id_t mMatchedId;
	double[] mMatchedGeometry = new double[9];
	//
	FreakFeaturePointStack mQueryKeyframe;
	//
	// // Map of keyframe
	final private KeyframeMap mKeyframeMap=new KeyframeMap();



	// Feature Extractor (FREAK, etc).
	final FREAKExtractor mFeatureExtractor;
	//
	// // Feature matcher
	final BinaryFeatureMatcher mMatcher=new BinaryFeatureMatcher();

	// Similarity voter
	HoughSimilarityVoting mHoughSimilarityVoting=new HoughSimilarityVoting();

	// Robust homography estimation
	final RobustHomography mRobustHomography=new RobustHomography();

	public void addKeyframe(Keyframe keyframe, int image_id)
	{
//        typename keyframe_map_t::iterator it = mKeyframeMap.find(id);
//        if(it != mKeyframeMap.end()) {
//            throw EXCEPTION("ID already exists");
//        }
		if(this.mKeyframeMap.containsKey(image_id)){
			throw new NyARRuntimeException();
		}
        
        mKeyframeMap.put(image_id,keyframe);
	}

	public Keyframe getKeyFeatureFrame(int image_id)
	{
		return this.mKeyframeMap.get(image_id);
	}





}
