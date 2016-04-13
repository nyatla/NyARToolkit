/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2013-2015 Daqri, LLC.
 *  Author(s): Chris Broaddus
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 *  Copyright (C)2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers;

import java.util.Map;

import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.BinomialPyramid32f;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.Point2d;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.Keyframe;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.KeyframeMap;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.detectors.DoGScaleInvariantDetector;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.detectors.GaussianScaleSpacePyramid;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.homography_estimation.RobustHomography;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.match.indexing;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math.geometry;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math.liner_algebr;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.math.math_utils;
import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.gs.format.NyARGsRaster_INT1D_GRAY_8;

public class VisualDatabase<STORE extends BinaryFeatureStore> {
	private static double kLaplacianThreshold = 3;
	private static double kEdgeThreshold = 4;
	private static int kMaxNumFeatures = 300;
	private static int kMinCoarseSize = 8;

	private static int kHomographyInlierThreshold = 3;
	private static int kMinNumInliers = 8;

	private static double kHoughBinDelta = 1;

	private static int kBytesPerFeature = 96;

	private static boolean kUseFeatureIndex = true;
	public final static int NUM_BYTES_PER_FEATURE = 96;

	public VisualDatabase() {
		this.mDetector.setLaplacianThreshold(kLaplacianThreshold);
		this.mDetector.setEdgeThreshold(kEdgeThreshold);
		this.mDetector.setMaxNumFeaturePoints(kMaxNumFeatures);

		this.mHomographyInlierThreshold = kHomographyInlierThreshold;
		this.mMinNumInliers = kMinNumInliers;

		this.mUseFeatureIndex = kUseFeatureIndex;
		this.mFeatureExtractor=new FREAKExtractor();
	}

	/**
	 * Find feature points in an image.
	 */
	static void FindFeatures(Keyframe keyframe, GaussianScaleSpacePyramid pyramid,
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

		FeaturePoint[] points = new FeaturePoint[detector.features()
				.getLength()];
		for (int i = 0; i < detector.features().getLength(); i++) {
			DoGScaleInvariantDetector.FeaturePoint p = detector.features()
					.getItem(i);
			points[i] = new FeaturePoint(p.x, p.y, p.angle, p.sigma,
					p.score > 0);
		}

		//
		// Extract features
		//

		extractor.extract(keyframe.store(), pyramid, points);
	}
    /**
     * @return Query store
     */
    public Keyframe queryKeyframe(){ return mQueryKeyframe; }

	public boolean query(INyARGrayscaleRaster image) {
		// Allocate pyramid
//		if (this.mPyramid.images().length == 0
		if (this.mPyramid.images()==null
				|| this.mPyramid.images()[0].getWidth() != image.getWidth()
				|| this.mPyramid.images()[0].getHeight() != image.getHeight()) {
			int num_octaves = BinomialPyramid32f.numOctaves(
					(int) image.getWidth(), (int) image.getHeight(),
					kMinCoarseSize);
			mPyramid.alloc(image.getWidth(), image.getHeight(), num_octaves);
		}
		long p=((NyARGsRaster_INT1D_GRAY_8)(image)).allPixels();
		// Build the pyramid		
		mPyramid.build(image);

		return query(mPyramid);
	}

	boolean query(GaussianScaleSpacePyramid pyramid) {
		// Allocate detector
		if (this.mDetector.width() != pyramid.images()[0].getWidth()
				|| this.mDetector.height() != pyramid.images()[0].getHeight()) {
			this.mDetector.alloc(pyramid);
		}

		// Find the features on the image
		this.mQueryKeyframe = new Keyframe(
			96,(int) pyramid.images()[0].getWidth(),(int) pyramid.images()[0].getHeight(),
			new BinaryFeatureStore(96));// .reset(new keyframe_t());
		FindFeatures(this.mQueryKeyframe, pyramid, this.mDetector,
				this.mFeatureExtractor);
		// LOG_INFO("Found %d features in query",
		// mQueryKeyframe->store().size());

		return this.query(mQueryKeyframe);
	}

	/**
	 * Vote for a similarity transformation.
	 */
	int FindHoughSimilarity(HoughSimilarityVoting hough, FeaturePointStack p1,
			FeaturePointStack p2, matchStack matches, int insWidth,
			int insHeigth, int refWidth, int refHeight) {
		double[] query = new double[4 * matches.getLength()];
		double[] ref = new double[4 * matches.getLength()];

		// Extract the data from the features
		for (int i = 0; i < matches.getLength(); i++) {
			FeaturePoint query_point = p1.getItem(matches.getItem(i).ins);
			FeaturePoint ref_point = p2.getItem(matches.getItem(i).ref);

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

	boolean query(Keyframe query_keyframe) {
		// mMatchedInliers.clear();
		this. mMatchedId = -1;
		int last_inliers=0;

		FeaturePointStack query_points = query_keyframe.store().points();
		// Loop over all the images in the database
		// typename keyframe_map_t::const_iterator it = mKeyframeMap.begin();
		// for(; it != mKeyframeMap.end(); it++) {
		for (Map.Entry<Integer, Keyframe> i : mKeyframeMap.entrySet()) {
			//MAPの順番がC++だと8->0->1->2...7だから0で比較すること
			Keyframe second = i.getValue();
			int first = i.getKey();
			// TIMED("Find Matches (1)") {
			if (mUseFeatureIndex) {
				if (mMatcher.match(query_keyframe.store(), second.store(),
						second.index()) < this.mMinNumInliers) {
					continue;
				}
			} else {
				if (mMatcher.match(query_keyframe.store(), second.store()) < mMinNumInliers) {
					continue;
				}
			}
			// }

			FeaturePointStack ref_points = second.store().points();
			// std::cout<<"ref_points-"<<ref_points.size()<<std::endl;
			// std::cout<<"query_points-"<<query_points.size()<<std::endl;

			//
			// Vote for a transformation based on the correspondences
			//

			int max_hough_index = -1;
			// TIMED("Hough Voting (1)") {
			max_hough_index = FindHoughSimilarity(mHoughSimilarityVoting,
					query_points, ref_points, mMatcher.matches(),
					query_keyframe.width(), query_keyframe.height(),
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

			double[] H = new double[9];
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
			if (mMatcher.match(query_keyframe.store(), second.store(), H, 10) < mMinNumInliers) {
				continue;
			}
			// }

			//
			// Vote for a similarity with new matches
			//

			// TIMED("Hough Voting (2)") {
			max_hough_index = FindHoughSimilarity(mHoughSimilarityVoting,
					query_points, ref_points, mMatcher.matches(),
					query_keyframe.width(), query_keyframe.height(),
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
				indexing.CopyVector(mMatchedGeometry, 0, H, 0, 9);
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
	void FindInliers(matchStack inliers, double[] H, FeaturePointStack p1,
			FeaturePointStack p2, matchStack matches, double threshold) {
		double threshold2 = math_utils.sqr(threshold);
		// reserve(matches.size());
		for (int i = 0; i < matches.getLength(); i++) {
			Point2d xp = new Point2d();// float xp[2];
			homography.MultiplyPointHomographyInhomogenous(xp, H,
					p2.getItem(matches.getItem(i).ref).x,
					p2.getItem(matches.getItem(i).ref).y);
			// float d2 = sqr(xp[0]-p1[matches[i].ins].x) +
			// sqr(xp[1]-p1[matches[i].ins].y);
			double d2 = math_utils.sqr(xp.x
					- p1.getItem(matches.getItem(i).ins).x)
					+ math_utils.sqr(xp.y
							- p1.getItem(matches.getItem(i).ins).y);
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
			FeaturePointStack p1, FeaturePointStack p2, matchStack in_matches,
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
		return;
	}

	/**
	 * Estimate the homography between a set of correspondences.
	 */
	boolean EstimateHomography(double[] H, FeaturePointStack p1,
			FeaturePointStack p2, matchStack matches, double threshold,
			RobustHomography estimator, int refWidth, int refHeight) {

		Point2d[] srcPoints = Point2d.createArray(matches.getLength());
		Point2d[] dstPoints = Point2d.createArray(matches.getLength());

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

		Point2d[] test_points = Point2d.createArray(8);
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
	boolean CheckHomographyHeuristics(double[] H, int refWidth, int refHeight) {
		Point2d p0p = new Point2d();
		Point2d p1p = new Point2d();
		Point2d p2p = new Point2d();
		Point2d p3p = new Point2d();

		double[] Hinv = new double[9];
		if (!liner_algebr.MatrixInverse3x3(Hinv, H, 1e-5f)) {
			return false;
		}

		Point2d p0 = new Point2d(0, 0);
		Point2d p1 = new Point2d((double) refWidth, 0);
		Point2d p2 = new Point2d((double) refWidth, (double) refHeight);
		Point2d p3 = new Point2d(0, (double) refHeight);

		homography.MultiplyPointHomographyInhomogenous(p0p, Hinv, p0);
		homography.MultiplyPointHomographyInhomogenous(p1p, Hinv, p1);
		homography.MultiplyPointHomographyInhomogenous(p2p, Hinv, p2);
		homography.MultiplyPointHomographyInhomogenous(p3p, Hinv, p3);

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

	//
	// /**
	// * Query the visual database.
	// */
	// bool query(const GaussianScaleSpacePyramid* pyramid) throw(Exception);
	// bool query(const keyframe_t* query_keyframe) throw(Exception);
	//
	// /**
	// * Erase an ID.
	// */
	// bool erase(id_t id);
	//
	// /**
	// * @return Keyframe
	// */
	// const keyframe_ptr_t keyframe(id_t id) {
	// typename keyframe_map_t::const_iterator it = mKeyframeMap.find(id);
	// if(it != mKeyframeMap.end()) {
	// return it->second;
	// } else {
	// return keyframe_ptr_t();
	// }
	// }
	//
	// /**
	// * @return Query store
	// */
	// const keyframe_ptr_t queryKeyframe() const { return mQueryKeyframe; }
	//
	// const size_t databaseCount() const { return mKeyframeMap.size(); }
	//
	// /**
	// * @return Matcher
	// */
	// const MATCHER& matcher() const { return mMatcher; }
	//
	// /**
	// * @return Feature extractor
	// */
	// const FEATURE_EXTRACTOR& featureExtractor() const { return
	// mFeatureExtractor; }
	//
	// /**
	// * @return Inlier
	// */
	// const matches_t& inliers() const { return mMatchedInliers; }
	//
	 /**
	 * Get the mathced id.
	 */
	public int matchedId() {
		return mMatchedId;
	}	
	
	// /**
	// * @return Matched geometry matrix
	// */
	// const float* matchedGeometry() const { return mMatchedGeometry; }
	//
	// /**
	// * Get the detector.
	// */
	// inline detector_t& detector() { return mDetector; }
	// inline const detector_t& detector() const { return mDetector; }
	//
	// /**
	// * Set/Get minimum number of inliers.
	// */
	// inline void setMinNumInliers(size_t n) { mMinNumInliers = n; }
	// inline size_t minNumInliers() const { return mMinNumInliers; }
	//
	// private:
	//
	private int mMinNumInliers;
	private double mHomographyInlierThreshold;
	//
	// Set to true if the feature index is enabled
	private boolean mUseFeatureIndex;

	//
	matchStack mMatchedInliers;
	// id_t mMatchedId;
	double[] mMatchedGeometry = new double[12];
	//
	Keyframe mQueryKeyframe;
	//
	// // Map of keyframe
	final private KeyframeMap mKeyframeMap=new KeyframeMap();
	//
	// // Pyramid builder
	final private BinomialPyramid32f mPyramid=new BinomialPyramid32f();
	//
	// Interest point detector (DoG, etc)
	private DoGScaleInvariantDetector mDetector = new DoGScaleInvariantDetector();

	// Feature Extractor (FREAK, etc).
	final FREAKExtractor mFeatureExtractor;
	//
	// // Feature matcher
	final BinaryFeatureMatcher mMatcher=new BinaryFeatureMatcher(NUM_BYTES_PER_FEATURE);

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





}
