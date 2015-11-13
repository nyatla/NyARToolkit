package jp.nyatla.nyartoolkit.core.kpm.vision.matchers;

import java.util.HashMap;
import java.util.Map;

import jp.nyatla.nyartoolkit.core.kpm.BinomialPyramid32f;
import jp.nyatla.nyartoolkit.core.kpm.vision.Keyframe;
import jp.nyatla.nyartoolkit.core.kpm.vision.KeyframeMap;
import jp.nyatla.nyartoolkit.core.kpm.vision.detectors.DoGScaleInvariantDetector;
import jp.nyatla.nyartoolkit.core.kpm.vision.detectors.GaussianScaleSpacePyramid;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;

public class VisualDatabase<FEATURE_EXTRACTOR extends FREAKExtractor,STORE extends BinaryFeatureStore,MATCHER extends BinaryFeatureMatcher>
{
    private static float kLaplacianThreshold = 3;
    private static float kEdgeThreshold = 4;
    private static int kMaxNumFeatures = 300;
    private static int kMinCoarseSize = 8;
    
    private static int kHomographyInlierThreshold = 3;
    private static int kMinNumInliers = 8;
    
    private static float kHoughBinDelta = 1;
    
    private static int kBytesPerFeature = 96;
    
    private static  boolean kUseFeatureIndex = true;	
	private final static int NUM_BYTES_PER_FEATURE=96;
	
//    public:
//        
//        typedef int id_t;
//        
//        typedef Keyframe<96> keyframe_t;
//        typedef std::shared_ptr<keyframe_t> keyframe_ptr_t;
//        typedef std::unordered_map<id_t, keyframe_ptr_t> keyframe_map_t;
//        

//        typedef DoGScaleInvariantDetector detector_t;
//        
	
public VisualDatabase()
{
    this.mDetector.setLaplacianThreshold(kLaplacianThreshold);
    this.mDetector.setEdgeThreshold(kEdgeThreshold);
    this.mDetector.setMaxNumFeaturePoints(kMaxNumFeatures);
    
    this.mHomographyInlierThreshold = kHomographyInlierThreshold;
    this.mMinNumInliers = kMinNumInliers;
    
    this.mUseFeatureIndex = kUseFeatureIndex;	
}
/**
 * Find feature points in an image.
 */
void FindFeatures(Keyframe keyframe,
                 GaussianScaleSpacePyramid pyramid,
                  DoGScaleInvariantDetector detector,
                  FEATURE_EXTRACTOR extractor) {
//    ASSERT(pyramid, "Pyramid is NULL");
//    ASSERT(detector, "Detector is NULL");
//    ASSERT(pyramid->images().size() > 0, "Pyramid is empty");
//    ASSERT(pyramid->images()[0].width() == detector->width(), "Pyramid and detector size mismatch");
//    ASSERT(pyramid->images()[0].height() == detector->height(), "Pyramid and detector size mismatch");
    
    //
    // Detect feature points
    //
    
    detector.detect(pyramid);
    
    //
    // Copy the points
    //
    
    FeaturePoint[] points=new FeaturePoint[detector.features().getLength()];
    for(int i = 0; i < detector.features().getLength(); i++) {
        DoGScaleInvariantDetector.FeaturePoint p = detector.features().getItem(i);
        points[i] = new FeaturePoint(p.x, p.y, p.angle, p.sigma, p.score > 0);
    }
    
    //
    // Extract features
    //
    
    extractor.extract(keyframe.store(), pyramid, points);
}
//        ~VisualDatabase();
//        
//        /**
//         * Add an image to the database with a specific ID.
//         */
//        void addImage(const Image& image, id_t id) throw(Exception);
//        
//        /**
//         * Add an image to the database with a specific ID.
//         */
//        void addImage(const GaussianScaleSpacePyramid* pyramid, id_t id) throw(Exception);
//        
//        /**
//         * Add a keyframe to the database.
//         */
//        void addKeyframe(keyframe_ptr_t keyframe , id_t id) throw(Exception);
//    
//        /**
//         * Query the visual database.
//         */

public boolean query(INyARGrayscaleRaster image)
{
    // Allocate pyramid
    if(this.mPyramid.images().length == 0 || this.mPyramid.images()[0].getWidth() != image.getWidth() || this.mPyramid.images()[0].getHeight() != image.getHeight())
    {
        int num_octaves = BinomialPyramid32f.numOctaves((int)image.getWidth(), (int)image.getHeight(), kMinCoarseSize);
        mPyramid.alloc(image.getWidth(), image.getHeight(), num_octaves);
    }
    
    // Build the pyramid
        mPyramid.build(image);
    
    return query(mPyramid);
}

boolean query(GaussianScaleSpacePyramid pyramid){
    // Allocate detector
    if(this.mDetector.width() != pyramid.images()[0].getWidth() ||
    		this.mDetector.height() != pyramid.images()[0].getHeight()) {
    	this.mDetector.alloc(pyramid);
    }
    
    // Find the features on the image
    this.mQueryKeyframe=new Keyframe(96);//.reset(new keyframe_t());
    this.mQueryKeyframe.setWidth((int)pyramid.images()[0].getWidth());
    this.mQueryKeyframe.setHeight((int)pyramid.images()[0].getHeight());
    FindFeatures(this.mQueryKeyframe, pyramid,this.mDetector,this.mFeatureExtractor);
//    LOG_INFO("Found %d features in query", mQueryKeyframe->store().size());
    
    return this.query(mQueryKeyframe);
}

/**
 * Vote for a similarity transformation.
 */
int FindHoughSimilarity(HoughSimilarityVoting hough,
                               FeaturePointStack p1,
                               FeaturePointStack p2,
                               matchStack matches,
                               int insWidth,
                               int insHeigth,
                               int refWidth,
                               int refHeight) {
    float[] query=new float[4*matches.getLength()];
    float[] ref=new float[4*matches.getLength()];
    
    // Extract the data from the features
    for(int i = 0; i < matches.getLength(); i++) {
        FeaturePoint query_point = p1.getItem(matches.getItem(i).ins);
        FeaturePoint ref_point = p2.getItem(matches.getItem(i).ref);
        
        int q_ptr =i*4;
        query[q_ptr+0] = query_point.x;
        query[q_ptr+1] = query_point.y;
        query[q_ptr+2] = query_point.angle;
        query[3] = query_point.scale;
        
        int r_ptr = i*4;
        ref[r_ptr+0] = ref_point.x;
        ref[r_ptr+1] = ref_point.y;
        ref[r_ptr+2] = ref_point.angle;
        ref[r_ptr+3] = ref_point.scale;
    }
    
    float dx = insWidth+(insWidth*0.2f);
    float dy = insHeigth+(insHeigth*0.2f);
    
    hough.init(-dx, dx, -dy, dy, 0, 0, 12, 10);        
    hough.setObjectCenterInReference(refWidth>>1, refHeight>>1);
    hough.setRefImageDimensions(refWidth, refHeight);
//    hough.vote((float*)&query[0], (float*)&ref[0], (int)matches.size());
    hough.vote(query,ref,matches.getLength());

    float maxVotes;
    int maxIndex;
    hough.getMaximumNumberOfVotes(maxVotes, maxIndex);
    
    return (maxVotes < 3) ? -1 : maxIndex;
}
boolean query(Keyframe query_keyframe)
{
//    mMatchedInliers.clear();
//    mMatchedId = -1;
    
    FeaturePointStack query_points = query_keyframe.store().points();
    // Loop over all the images in the database
//    typename keyframe_map_t::const_iterator it = mKeyframeMap.begin();
//    for(; it != mKeyframeMap.end(); it++) {
    for(Map.Entry<Integer, Keyframe> i : mKeyframeMap.entrySet()) {
    	Keyframe second=i.getValue();
    	int first=i.getKey();
//        TIMED("Find Matches (1)") {
            if(mUseFeatureIndex) {
                if(mMatcher.match(query_keyframe.store(), second.store(), second.index()) < this.mMinNumInliers) {
                    continue;
                }
            } else {
                if(mMatcher.match(query_keyframe.store(),second.store()) < mMinNumInliers) {
                    continue;
                }
            }
//        }
        
        FeaturePointStack ref_points = second.store().points();
        //std::cout<<"ref_points-"<<ref_points.size()<<std::endl;
        //std::cout<<"query_points-"<<query_points.size()<<std::endl;
        
        //
        // Vote for a transformation based on the correspondences
        //
        
        int max_hough_index = -1;
//        TIMED("Hough Voting (1)") {
            max_hough_index = FindHoughSimilarity(mHoughSimilarityVoting,
                                                  query_points,
                                                  ref_points,
                                                  mMatcher.matches(),
                                                  query_keyframe->width(),
                                                  query_keyframe->height(),
                                                  it->second->width(),
                                                  it->second->height());
            if(max_hough_index < 0) {
                continue;
            }
  //      }
        
        matches_t hough_matches;
        TIMED("Find Hough Matches (1)") {
            FindHoughMatches(hough_matches,
                             mHoughSimilarityVoting,
                             query_points,
                             ref_points,
                             mMatcher.matches(),
                             max_hough_index,
                             kHoughBinDelta);
        }
        
        //
        // Estimate the transformation between the two images
        //
        
        float H[9];
        TIMED("Estimate Homography (1)") {
            if(!EstimateHomography(H,
                                   query_points,
                                   ref_points,
                                   hough_matches,
                                   mHomographyInlierThreshold,
                                   mRobustHomography,
                                   it->second->width(),
                                   it->second->height())) {
                continue;
            }
        }
        
        //
        // Find the inliers
        //
        
        matches_t inliers;
        TIMED("Find Inliers (1)") {
            FindInliers(inliers, H, query_points, ref_points, hough_matches, mHomographyInlierThreshold);
            if(inliers.size() < mMinNumInliers) {
                continue;
            }
        }
        
        //
        // Use the estimated homography to find more inliers
        //
        
        TIMED("Find Matches (2)") {
            if(mMatcher.match(&query_keyframe->store(),
                              &it->second->store(),
                              H,
                              10) < mMinNumInliers) {
                continue;
            }
        }
        
        //
        // Vote for a similarity with new matches
        //
        
        TIMED("Hough Voting (2)") {
            max_hough_index = FindHoughSimilarity(mHoughSimilarityVoting,
                                                  query_points,
                                                  ref_points,
                                                  mMatcher.matches(),
                                                  query_keyframe->width(),
                                                  query_keyframe->height(),
                                                  it->second->width(),
                                                  it->second->height());
            if(max_hough_index < 0) {
                continue;
            }
        }
        
        TIMED("Find Hough Matches (2)") {
            FindHoughMatches(hough_matches,
                             mHoughSimilarityVoting,
                             query_points,
                             ref_points,
                             mMatcher.matches(),
                             max_hough_index,
                             kHoughBinDelta);
        }
        
        //
        // Re-estimate the homography
        //
        
        TIMED("Estimate Homography (2)") {
            if(!EstimateHomography(H,
                                   query_points,
                                   ref_points,
                                   hough_matches,
                                   mHomographyInlierThreshold,
                                   mRobustHomography,
                                   it->second->width(),
                                   it->second->height())) {
                continue;
            }
        }
        
        //
        // Check if this is the best match based on number of inliers
        //
        
        inliers.clear();
        TIMED("Find Inliers (2)") {
            FindInliers(inliers, H, query_points, ref_points, hough_matches, mHomographyInlierThreshold);
        }
        
        //std::cout<<"inliers-"<<inliers.size()<<std::endl;
        if(inliers.size() >= mMinNumInliers && inliers.size() > mMatchedInliers.size()) {
            CopyVector9(mMatchedGeometry, H);
            mMatchedInliers.swap(inliers);
            mMatchedId = it->first;
        }
    }
    
    return mMatchedId >= 0;
}





//        
//        /**
//         * Query the visual database.
//         */
//        bool query(const GaussianScaleSpacePyramid* pyramid) throw(Exception);
//        bool query(const keyframe_t* query_keyframe) throw(Exception);
//        
//        /**
//         * Erase an ID.
//         */
//        bool erase(id_t id);
//        
//        /**
//         * @return Keyframe
//         */
//        const keyframe_ptr_t keyframe(id_t id) {
//            typename keyframe_map_t::const_iterator it = mKeyframeMap.find(id);
//            if(it != mKeyframeMap.end()) {
//                return it->second;
//            } else {
//                return keyframe_ptr_t();
//            }
//        }
//        
//        /**
//         * @return Query store
//         */
//        const keyframe_ptr_t queryKeyframe() const { return mQueryKeyframe; }
//        
//        const size_t databaseCount() const { return mKeyframeMap.size(); }
//        
//        /**
//         * @return Matcher
//         */
//        const MATCHER& matcher() const { return mMatcher; }
//        
//        /**
//         * @return Feature extractor
//         */
//        const FEATURE_EXTRACTOR& featureExtractor() const { return mFeatureExtractor; }
//        
//        /**
//         * @return Inlier
//         */
//        const matches_t& inliers() const { return mMatchedInliers; }
//        
//        /**
//         * Get the mathced id.
//         */
//        inline id_t matchedId() const { return mMatchedId; }
//        
//        /**
//         * @return Matched geometry matrix
//         */
//        const float* matchedGeometry() const { return mMatchedGeometry; }
//        
//        /**
//         * Get the detector.
//         */
//        inline detector_t& detector() { return mDetector; }
//        inline const detector_t& detector() const { return mDetector; }
//        
//        /**
//         * Set/Get minimum number of inliers.
//         */
//        inline void setMinNumInliers(size_t n) { mMinNumInliers = n; }
//        inline size_t minNumInliers() const { return mMinNumInliers; }
//        
//    private:
//        
private int mMinNumInliers;
private float mHomographyInlierThreshold;
//        
// Set to true if the feature index is enabled
private boolean mUseFeatureIndex;

class match_t {
//    match_t() : ins(-1), ref(-1) {}
    match_t(int _ins, int _ref){
    	this.ins=_ins;
    	this.ref=_ref;
    }
    public int ins;
    public int ref;
}; // match_t


//        
        match_t[] mMatchedInliers;
//        id_t mMatchedId;
//        float mMatchedGeometry[12];
//        
        Keyframe mQueryKeyframe;
//    
//        // Map of keyframe
        KeyframeMap mKeyframeMap;
//        
//        // Pyramid builder
BinomialPyramid32f mPyramid;
//        
        // Interest point detector (DoG, etc)
private DoGScaleInvariantDetector mDetector=new DoGScaleInvariantDetector();
        
        // Feature Extractor (FREAK, etc).
        FEATURE_EXTRACTOR mFeatureExtractor;
//        
//        // Feature matcher
        MATCHER mMatcher;
        
        // Similarity voter
        HoughSimilarityVoting mHoughSimilarityVoting;
//        
//        // Robust homography estimation
//        RobustHomography<float> mRobustHomography;
}
