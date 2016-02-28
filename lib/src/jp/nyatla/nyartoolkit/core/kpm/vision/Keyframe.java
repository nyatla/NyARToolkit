package jp.nyatla.nyartoolkit.core.kpm.vision;

import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FreakFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FreakMatchPointSetStack;

public class Keyframe {

	public Keyframe(int width, int height,
			FreakMatchPointSetStack i_binaryFeatureStore) {
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
	public FreakMatchPointSetStack store() {
		return mStore;
	}

	/**
	 * @return Index over the features.
	 */
	public BinaryHierarchicalClustering index() {
		return mIndex;
	}




	//
	// Serialization
	//



	// Image width and height
	final private int mWidth;
	final private int mHeight;

	// Feature store
	private final FreakMatchPointSetStack mStore;

	// Feature index
	private final BinaryHierarchicalClustering mIndex;

}; // Keyframe
