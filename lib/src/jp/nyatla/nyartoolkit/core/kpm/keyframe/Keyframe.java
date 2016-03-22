package jp.nyatla.nyartoolkit.core.kpm.keyframe;

import jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering.BinaryHierarchicalClusterBuilder;
import jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering.BinaryHierarchicalNode;



public class Keyframe {
	// Image width and height
	final private int mWidth;
	final private int mHeight;
	// Feature store
	private final FreakMatchPointSetStack mStore;
	// Feature index
	private final BinaryHierarchicalNode mIndex;
	
	public Keyframe(int width, int height,FreakMatchPointSetStack i_binaryFeatureStore)
	{
		this.mWidth = width;
		this.mHeight = height;
		this.mStore = i_binaryFeatureStore;
		BinaryHierarchicalClusterBuilder bhi= new BinaryHierarchicalClusterBuilder(i_binaryFeatureStore.getLength(),128,8,16);
        this.mIndex=bhi.build(this.mStore);
	}
	/**
	 * Get/Set image width.
	 */
	public int width() {
		return mWidth;
	}
	/**
	 * Get/Set image height.
	 */
	public int height() {
		return mHeight;
	}
	/**
	 * @return Feature store.
	 */
	public FreakMatchPointSetStack getFeaturePointSet()
	{
		return mStore;
	}

	/**
	 * @return Index over the features.
	 */
	public BinaryHierarchicalNode getIndex() {
		return mIndex;
	}




}; // Keyframe
