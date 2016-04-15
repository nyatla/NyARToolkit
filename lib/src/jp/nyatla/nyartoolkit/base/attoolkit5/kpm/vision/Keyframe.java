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
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision;

import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers.BinaryFeatureStore;

public class Keyframe {

	// typedef Keyframe<NUM_BYTES_PER_FEATURE> keyframe_t;
	// typedef BinaryHierarchicalClustering<NUM_BYTES_PER_FEATURE> index_t;

	// public Keyframe(int i_NUM_BYTES_PER_FEATURE){
	// this.mWidth=0;
	// this.mHeight=0;
	// this.NUM_BYTES_PER_FEATURE=i_NUM_BYTES_PER_FEATURE;
	// this.mIndex=new BinaryHierarchicalClustering(this.NUM_BYTES_PER_FEATURE);
	// }

	public Keyframe(int i_NUM_BYTES_PER_FEATURE, int width, int height,
			BinaryFeatureStore i_binaryFeatureStore) {
		this.NUM_BYTES_PER_FEATURE = i_NUM_BYTES_PER_FEATURE;
		this.mWidth = width;
		this.mHeight = height;
		this.mStore = i_binaryFeatureStore;
		this.mIndex = new BinaryHierarchicalClustering(i_NUM_BYTES_PER_FEATURE,128,8,8,16);
	}
    public void buildIndex()
    {
//        mIndex.setNumHypotheses(128);
//        mIndex.setNumCenters(8);
//        mIndex.setMaxNodesToPop(8);
//        mIndex.setMinFeaturesPerNode(16);
        mIndex.build(this.mStore.features(), (int)mStore.size());
    }
	final int NUM_BYTES_PER_FEATURE;

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
	public BinaryFeatureStore store() {
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
	private final BinaryFeatureStore mStore;

	// Feature index
	private final BinaryHierarchicalClustering mIndex;

}; // Keyframe
