package jp.nyatla.nyartoolkit.core.kpm.vision;

import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.BinaryFeatureStore;


public class Keyframe {

//        typedef Keyframe<NUM_BYTES_PER_FEATURE> keyframe_t;
//        typedef BinaryHierarchicalClustering<NUM_BYTES_PER_FEATURE> index_t;
        
        public Keyframe(int i_NUM_BYTES_PER_FEATURE){
        	this.mWidth=0;
        	this.mHeight=0;
        	this.NUM_BYTES_PER_FEATURE=i_NUM_BYTES_PER_FEATURE;
        	this.mIndex=new BinaryHierarchicalClustering(this.NUM_BYTES_PER_FEATURE);
        }

        final int NUM_BYTES_PER_FEATURE;
        
        /**
         * Get/Set image width.
         */
        public  void setWidth(int width) { mWidth = width; }
        public  int width() { return mWidth; }
        
        /**
         * Get/Set image height.
         */
        public  void setHeight(int height) { mHeight = height; }
        public  int height()  { return mHeight; }
        
        /**
         * @return Feature store.
         */
        public BinaryFeatureStore store() { return mStore; }
        
        /**
         * @return Index over the features.
         */
        public BinaryHierarchicalClustering index() { return mIndex; }
        


        
        /**
         * Copy a keyframe.
         */
//        void copy(Keyframe keyframe) {
//        	assert(this.NUM_BYTES_PER_FEATURE==keyframe.NUM_BYTES_PER_FEATURE);
//            mWidth = keyframe.mWidth;
//            mHeight = keyframe.mHeight;
//            mStore.copy(keyframe.store());
//        }
        
        //
        // Serialization
        //
        
        /*template<class Archive>
        void serialize(Archive & ar, const unsigned int version) {
            ar & mWidth;
            ar & mHeight;
            ar & mStore;
        }*/
        
        
        // Image width and height
        private int mWidth;
        private int mHeight;
        
        // Feature store
        private final BinaryFeatureStore mStore=new BinaryFeatureStore();
        
        // Feature index
        private final BinaryHierarchicalClustering mIndex;
        
    }; // Keyframe
