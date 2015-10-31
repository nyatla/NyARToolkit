package jp.nyatla.nyartoolkit.core.kpm.vision;

public class VisualDatabase<FEATURE_EXTRACTOR,STORE,MATCHER> {
    public:
        
        typedef int id_t;
        
        typedef Keyframe<96> keyframe_t;
        typedef std::shared_ptr<keyframe_t> keyframe_ptr_t;
        typedef std::unordered_map<id_t, keyframe_ptr_t> keyframe_map_t;
        
        typedef BinomialPyramid32f pyramid_t;
        typedef DoGScaleInvariantDetector detector_t;
        
        VisualDatabase();
        ~VisualDatabase();
        
        /**
         * Add an image to the database with a specific ID.
         */
        void addImage(const Image& image, id_t id) throw(Exception);
        
        /**
         * Add an image to the database with a specific ID.
         */
        void addImage(const GaussianScaleSpacePyramid* pyramid, id_t id) throw(Exception);
        
        /**
         * Add a keyframe to the database.
         */
        void addKeyframe(keyframe_ptr_t keyframe , id_t id) throw(Exception);
    
        /**
         * Query the visual database.
         */
        bool query(const Image& image) throw(Exception);
        
        /**
         * Query the visual database.
         */
        bool query(const GaussianScaleSpacePyramid* pyramid) throw(Exception);
        bool query(const keyframe_t* query_keyframe) throw(Exception);
        
        /**
         * Erase an ID.
         */
        bool erase(id_t id);
        
        /**
         * @return Keyframe
         */
        const keyframe_ptr_t keyframe(id_t id) {
            typename keyframe_map_t::const_iterator it = mKeyframeMap.find(id);
            if(it != mKeyframeMap.end()) {
                return it->second;
            } else {
                return keyframe_ptr_t();
            }
        }
        
        /**
         * @return Query store
         */
        const keyframe_ptr_t queryKeyframe() const { return mQueryKeyframe; }
        
        const size_t databaseCount() const { return mKeyframeMap.size(); }
        
        /**
         * @return Matcher
         */
        const MATCHER& matcher() const { return mMatcher; }
        
        /**
         * @return Feature extractor
         */
        const FEATURE_EXTRACTOR& featureExtractor() const { return mFeatureExtractor; }
        
        /**
         * @return Inlier
         */
        const matches_t& inliers() const { return mMatchedInliers; }
        
        /**
         * Get the mathced id.
         */
        inline id_t matchedId() const { return mMatchedId; }
        
        /**
         * @return Matched geometry matrix
         */
        const float* matchedGeometry() const { return mMatchedGeometry; }
        
        /**
         * Get the detector.
         */
        inline detector_t& detector() { return mDetector; }
        inline const detector_t& detector() const { return mDetector; }
        
        /**
         * Set/Get minimum number of inliers.
         */
        inline void setMinNumInliers(size_t n) { mMinNumInliers = n; }
        inline size_t minNumInliers() const { return mMinNumInliers; }
        
    private:
        
        size_t mMinNumInliers;
        float mHomographyInlierThreshold;
        
        // Set to true if the feature index is enabled
        bool mUseFeatureIndex;
        
        matches_t mMatchedInliers;
        id_t mMatchedId;
        float mMatchedGeometry[12];
        
        keyframe_ptr_t mQueryKeyframe;
    
        // Map of keyframe
        keyframe_map_t mKeyframeMap;
        
        // Pyramid builder
        pyramid_t mPyramid;
        
        // Interest point detector (DoG, etc)
        detector_t mDetector;
        
        // Feature Extractor (FREAK, etc).
        FEATURE_EXTRACTOR mFeatureExtractor;
        
        // Feature matcher
        MATCHER mMatcher;
        
        // Similarity voter
        HoughSimilarityVoting mHoughSimilarityVoting;
        
        // Robust homography estimation
        RobustHomography<float> mRobustHomography;
}
