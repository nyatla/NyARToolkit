package jp.nyatla.nyartoolkit.core.kpm.vision.matchers;

import jp.nyatla.nyartoolkit.core.kpm.vision.BinaryHierarchicalClustering;
import jp.nyatla.nyartoolkit.core.kpm.vision.math.Hamming;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

public class BinaryFeatureMatcher {

	public BinaryFeatureMatcher(int i_size){
		this.mThreshold=0.7f;
		
	}
//    public:
//        
//        typedef BinaryHierarchicalClustering<FEATURE_SIZE> index_t;
//        

//        
//        /**
//         * Set the ratio threshold between the 1st and 2nd best matches.
//         */
//        void setThreshold(float tr) {
//            mThreshold = tr;
//        }
//        
//        /**
//         * @return Get the threshold
//         */
//        float threshold() const {
//            return mThreshold;
//        }
//        
        /**
         * Match two feature stores.
         * @return Number of matches
         */
        int match(
        		BinaryFeatureStore features1,
        		BinaryFeatureStore features2) {
        
        this.mMatches=new matchStack(features1.size());
        
        if(features1.size() == 0 ||
           features2.size() == 0) {
            return 0;
        }
        
//        mMatches.reserve(features1.size());
        for(int i = 0; i < features1.size(); i++) {
            int first_best = Integer.MAX_VALUE;
            int second_best = Integer.MAX_VALUE;
            int best_index = Integer.MAX_VALUE;
            
            // Search for 1st and 2nd best match
            int f1 = features1.feature(i);
            FeaturePoint p1 = features1.point(i);
            for(int j = 0; j < features2.size(); j++) {
                // Both points should be a MINIMA or MAXIMA
                if(p1.maxima != features2.point(j).maxima) {
                    continue;
                }
                
//                ASSERT(FEATURE_SIZE == 96, "Only 96 bytes supported now");
                int d = Hamming.HammingDistance(features1.features(),f1,features2.features(),features2.feature(j));
                if(d < first_best) {
                    second_best = first_best;
                    first_best = d;
                    best_index = (int)j;
                } else if(d < second_best) {
                    second_best = d;
                }
            }
            
            // Check if FIRST_BEST has been set
            if(first_best !=Integer.MAX_VALUE) {
                // If there isn't a SECOND_BEST, then always choose the FIRST_BEST.
                // Otherwise, do a ratio test.
                if(second_best == Integer.MAX_VALUE) {
//                    mMatches.push_back(match_t((int)i, best_index));
                	match_t t=this.mMatches.prePush();
                	t.set(i, best_index);
                } else {
                    // Ratio test
                    float r = (float)first_best / (float)second_best;
                    if(r < mThreshold) {
                    	match_t t=this.mMatches.prePush();
                    	t.set(i, best_index);
//                        mMatches.push_back(match_t((int)i, best_index));
                    }
                }
            }
        }
//        ASSERT(mMatches.size() <= features1->size(), "Number of matches should be lower");
        return mMatches.getLength();
    }
    
        /**
         * Match two feature stores with an index on features2.
         * @return Number of matches
         */
        int match(BinaryFeatureStore features1,
                     BinaryFeatureStore features2,
                     BinaryHierarchicalClustering index2)
 {
            this.mMatches=new matchStack(features1.size());
            
            if(features1.size() == 0 ||
               features2.size() == 0) {
                return 0;
            }
            
//            mMatches.reserve(features1.size());
            for(int i = 0; i < features1.size(); i++) {
                int first_best = Integer.MAX_VALUE;//std::numeric_limits<unsigned int>::max();
                int second_best = Integer.MAX_VALUE;//std::numeric_limits<unsigned int>::max();
                int best_index = Integer.MAX_VALUE;//std::numeric_limits<int>::max();
                
                // Perform an indexed nearest neighbor lookup
                int f1 = features1.feature(i);
                index2.query(features1.features(),f1);
                
                FeaturePoint p1 = features1.point(i);
                
                // Search for 1st and 2nd best match
                int[] v= index2.reverseIndex();
                for(int j = 0; j < v.length; j++) {
                    // Both points should be a MINIMA or MAXIMA
                    if(p1.maxima != features2.point(v[j]).maxima) {
                        continue;
                    }
                    
//                    ASSERT(FEATURE_SIZE == 96, "Only 96 bytes supported now");
                    int d = Hamming.HammingDistance(features1.features(),f1,features2.features(),features2.feature(v[j]));
                    if(d < first_best) {
                        second_best = first_best;
                        first_best = d;
                        best_index = v[j];
                    } else if(d < second_best) {
                        second_best = d;
                    }
                }
                
                // Check if FIRST_BEST has been set
                if(first_best != Integer.MAX_VALUE) {
//                    ASSERT(best_index != std::numeric_limits<size_t>::max(), "Something strange");
                    
                    // If there isn't a SECOND_BEST, then always choose the FIRST_BEST.
                    // Otherwise, do a ratio test.
                    if(second_best == Integer.MAX_VALUE) {
                    	match_t t=mMatches.prePush();
                    	t.set((int)i, best_index);
                    } else {
                        // Ratio test
                        float r = (float)first_best / (float)second_best;
                        if(r < mThreshold) {
//                            mMatches.push_back(match_t((int)i, best_index));
                        	match_t t=mMatches.prePush();
                        	t.set((int)i, best_index);
                        }
                    }
                }
            }
//            ASSERT(mMatches.size() <= features1->size(), "Number of matches should be lower");
            return mMatches.getLength();
        }
//        /**
//         * Match two feature stores given a homography from the features in store 1 to
//         * store 2. The THRESHOLD is a spatial threshold in pixels to restrict the number
//         * of feature comparisons.
//         * @return Number of matches
//         */
//        size_t match(const BinaryFeatureStore* features1,
//                     const BinaryFeatureStore* features2,
//                     const float H[9],
//                     float tr);
//        
//        /**
//         * @return Vector of matches after a call to MATCH.
//         */
//        inline const matches_t& matches() const { return mMatches; }
//        
//    private:
        
        // Vector of indices that represent matches
        private matchStack mMatches;
        
        // Threshold on the 1st and 2nd best matches
        private float mThreshold;
        


        
        /**
         * @return Vector of matches after a call to MATCH.
         */
        matchStack matches() {
        	return this.mMatches;
        }        

	
	
}
