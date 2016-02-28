package jp.nyatla.nyartoolkit.core.kpm.vision;

import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FreakFeaturePointStack;

public class Keyframe {

	// typedef Keyframe<NUM_BYTES_PER_FEATURE> keyframe_t;
	// typedef BinaryHierarchicalClustering<NUM_BYTES_PER_FEATURE> index_t;

	// public Keyframe(int i_NUM_BYTES_PER_FEATURE){
	// this.mWidth=0;
	// this.mHeight=0;
	// this.NUM_BYTES_PER_FEATURE=i_NUM_BYTES_PER_FEATURE;
	// this.mIndex=new BinaryHierarchicalClustering(this.NUM_BYTES_PER_FEATURE);
	// }

	public Keyframe(int width, int height,
			FreakFeaturePointStack i_binaryFeatureStore) {
		this.mWidth = width;
		this.mHeight = height;
		this.mStore = i_binaryFeatureStore;
		this.mIndex = new BinaryHierarchicalClustering(128,8,8,16);
	}
    public void buildIndex()
    {
//        mIndex.setNumHypotheses(128);
//        mIndex.setNumCenters(8);
//        mIndex.setMaxNodesToPop(8);
//        mIndex.setMinFeaturesPerNode(16);
        mIndex.build(this.mStore);
    }

	/**
	 * Get/Set image width.
	 */
	// public void setWidth(int width) { mWidth = width; }
	public int width() {
		return mWidth;
	}

	/**
	 * Get/Set image height.
	 */
	// public void setHeight(int height) { mHeight = height; }
	public int height() {
		return mHeight;
	}

	/**
	 * @return Feature store.
	 */
	public FreakFeaturePointStack store() {
		return mStore;
	}

	/**
	 * @return Index over the features.
	 */
	public BinaryHierarchicalClustering index() {
		return mIndex;
	}

	/**
	 * Copy a keyframe.
	 */
	// void copy(Keyframe keyframe) {
	// assert(this.NUM_BYTES_PER_FEATURE==keyframe.NUM_BYTES_PER_FEATURE);
	// mWidth = keyframe.mWidth;
	// mHeight = keyframe.mHeight;
	// mStore.copy(keyframe.store());
	// }

	//
	// Serialization
	//

	/*
	 * template<class Archive> void serialize(Archive & ar, const unsigned int
	 * version) { ar & mWidth; ar & mHeight; ar & mStore; }
	 */

	// Image width and height
	final private int mWidth;
	final private int mHeight;

	// Feature store
	private final FreakFeaturePointStack mStore;

	// Feature index
	private final BinaryHierarchicalClustering mIndex;

}; // Keyframe
