package jp.nyatla.nyartoolkit.core.kpm.vision.matchers;

import java.util.Map;

import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.Keyframe;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.KeyframeMap;

import jp.nyatla.nyartoolkit.core.kpm.vision.homography_estimation.RobustHomography;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.HoughSimilarityVoting.BinLocation;
import jp.nyatla.nyartoolkit.core.kpm.vision.math.geometry;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

public class VisualDatabase
{
	private static int kHomographyInlierThreshold = 3;
	private static int kMinNumInliers = 8;
	private static double kHoughBinDelta = 1;
	private static boolean kUseFeatureIndex = true;
	
	
	public VisualDatabase(int i_width,int i_height)
	{		
		this.mHomographyInlierThreshold = kHomographyInlierThreshold;
		this.mMinNumInliers = kMinNumInliers;
		this.mUseFeatureIndex = kUseFeatureIndex;
		//
		this.mHoughSimilarityVoting=new HoughSimilarityVoting();		
		double dx = i_width + (i_width * 0.2f);
		double dy = i_height + (i_height * 0.2f);
		this.mHoughSimilarityVoting.init(-dx, dx, -dy, dy, 0, 0, 12, 10);		
		return;
	}

	
	



	/**
	 * Vote for a similarity transformation.
	 */
	private int FindHoughSimilarity(HoughSimilarityVoting hough,matchStack matches,int refWidth, int refHeight) {
//		FreakFeaturePoint[] query = new FreakFeaturePoint[matches.getLength()];
//		FreakFeaturePoint[] ref = new FreakFeaturePoint[matches.getLength()];
		FeaturePairStack feature_pair=new FeaturePairStack(matches.getLength());
		// Extract the data from the features
		for (int i = 0; i < matches.getLength(); i++) {
			FeaturePairStack.Item item=feature_pair.prePush();
			item.query	=matches.getItem(i).query;
			item.ref	=matches.getItem(i).ref;
		}
		hough.setObjectCenterInReference(refWidth >> 1, refHeight >> 1);
		hough.setRefImageDimensions(refWidth, refHeight);
		// hough.vote((float*)&query[0], (float*)&ref[0], (int)matches.size());
		hough.vote(feature_pair);

		HoughSimilarityVoting.getMaximumNumberOfVotesResult max = new HoughSimilarityVoting.getMaximumNumberOfVotesResult();
		hough.getMaximumNumberOfVotes(max);

		return (max.votes < 3) ? -1 : max.index;
	}


	public int query(FreakFeaturePointStack query_keyframe,KeyframeMap i_keymap) {
		// mMatchedInliers.clear();
		int mached_id = -1;
		int last_inliers=0;
		HomographyMat H = new HomographyMat();
		// Loop over all the images in the database
		// typename keyframe_map_t::const_iterator it = mKeyframeMap.begin();
		// for(; it != mKeyframeMap.end(); it++) {
		
		for (Map.Entry<Integer, Keyframe> i : i_keymap.entrySet()) {
			Keyframe second = i.getValue();
			FreakMatchPointSetStack ref_points = second.store();
			int first = i.getKey();
			//毎回作り直さんと行けない。
			matchStack match_result=new matchStack(query_keyframe.getLength());
			if (mUseFeatureIndex) {
				if (mMatcher.match(query_keyframe,ref_points,second.index(),match_result) < this.mMinNumInliers) {
					continue;
				}
			} else {
				if (mMatcher.match(query_keyframe,ref_points,match_result) < mMinNumInliers) {
					continue;
				}
			}

			//
			// Vote for a transformation based on the correspondences
			//
			int max_hough_index = -1;
			max_hough_index = FindHoughSimilarity(mHoughSimilarityVoting,match_result,second.width(), second.height());
			if (max_hough_index < 0) {
				continue;
			}
			FindHoughMatches(this.mHoughSimilarityVoting,match_result,max_hough_index, kHoughBinDelta);
			//
			// Estimate the transformation between the two images
			//
			if (!EstimateHomography(H, query_keyframe, ref_points, match_result,second.width(), second.height())) {
				continue;
			}

			//
			// Find the inliers
			//

			FindInliers(H, query_keyframe, ref_points, match_result,mHomographyInlierThreshold);
			if (match_result.getLength() < mMinNumInliers) {
				continue;
			}

			//
			// Use the estimated homography to find more inliers
			//

			match_result.clear();
			if (mMatcher.match(query_keyframe, ref_points, H, 10,match_result) < mMinNumInliers) {
				continue;
			}

			//
			// Vote for a similarity with new matches
			//

			// TIMED("Hough Voting (2)") {
			max_hough_index = FindHoughSimilarity(mHoughSimilarityVoting,match_result,second.width(), second.height());
			if (max_hough_index < 0) {
				continue;
			}

			FindHoughMatches(mHoughSimilarityVoting, match_result,max_hough_index, kHoughBinDelta);

			//
			// Re-estimate the homography
			//

			if (!EstimateHomography(H, query_keyframe, ref_points, match_result,second.width(), second.height())) {
				continue;
			}

			//
			// Check if this is the best match based on number of inliers
			//

			FindInliers(H, query_keyframe, ref_points, match_result,mHomographyInlierThreshold);

			// std::cout<<"inliers-"<<inliers.size()<<std::endl;
			if (match_result.getLength() >= mMinNumInliers && match_result.getLength() > last_inliers) {
//				indexing.CopyVector(mMatchedGeometry, 0, H, 0, 9);
				H.getValue(mMatchedGeometry);
				// CopyVector9(mMatchedGeometry, H);
				//現状は毎回生成してるからセットで。
				mMatchedInliers = match_result;// mMatchedInliers.swap(inliers);
				last_inliers=match_result.getLength();
				mached_id = first;
			}
		}

		return mached_id;
	}

	/**
	 * Find the inliers given a homography and a set of correspondences.
	 */
	private void FindInliers(NyARDoubleMatrix33 H, FreakFeaturePointStack p1,
			FreakMatchPointSetStack p2, matchStack matches, double threshold) {
		double threshold2 = (threshold*threshold);
		NyARDoublePoint2d xp = new NyARDoublePoint2d();// float xp[2];
		// reserve(matches.size());
		int pos=0;
		for (int i = 0; i < matches.getLength(); i++) {
			homography.MultiplyPointHomographyInhomogenous(xp, H,matches.getItem(i).ref.x,matches.getItem(i).ref.y);
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
	 * Get only the matches that are consistent based on the hough votes.
	 */
	private void FindHoughMatches(HoughSimilarityVoting hough,matchStack in_matches,
			int binIndex, double binDelta) {

		HoughSimilarityVoting.Bins bin = hough.getBinsFromIndex(binIndex);

	

		int n = (int) hough.getSubBinLocationIndices().length;
		// const float* vote_loc = hough.getSubBinLocations().data();
		BinLocation[] vote_loc = hough.getSubBinLocations();// .data();
		// ASSERT(n <= in_matches.size(), "Should be the same");
		HoughSimilarityVoting.mapCorrespondenceResult d = new HoughSimilarityVoting.mapCorrespondenceResult();
		//
		int pos=0;
		for (int i = 0; i < n; i++){
			hough.getBinDistance(d, vote_loc[i].x,
					vote_loc[i].y, vote_loc[i].angle,
					vote_loc[i].scale, bin.binX + .5f, bin.binY + .5f,
					bin.binAngle + .5f, bin.binScale + .5f);

			if (d.x < binDelta && d.y < binDelta && d.angle < binDelta && d.scale < binDelta) {
				//idxは昇順のはずだから詰める。
				int idx = hough.getSubBinLocationIndices()[i];
				in_matches.swap(idx, pos);
				pos++;
				
			}
		}
		in_matches.setLength(pos);
		return;
	}
	
	
	// Robust homography estimation
	final RobustHomography mRobustHomography=new RobustHomography();

	/**
	 * Estimate the homography between a set of correspondences.
	 */
	private boolean EstimateHomography(HomographyMat H, FreakFeaturePointStack p1,
			FreakMatchPointSetStack p2, matchStack matches, int refWidth, int refHeight) {

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
		if (!this.mRobustHomography.find(H, srcPoints, dstPoints, (int) matches.getLength(),
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
	private boolean CheckHomographyHeuristics(NyARDoubleMatrix33 H, int refWidth, int refHeight) {
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


	


	private int mMinNumInliers;
	private double mHomographyInlierThreshold;
	//
	// Set to true if the feature index is enabled
	private boolean mUseFeatureIndex;

	//
	private matchStack mMatchedInliers;
	// id_t mMatchedId;
	private double[] mMatchedGeometry = new double[9];
	//




	//
	// // Feature matcher
	final BinaryFeatureMatcher mMatcher=new BinaryFeatureMatcher();

	// Similarity voter
	final private HoughSimilarityVoting mHoughSimilarityVoting;










}
