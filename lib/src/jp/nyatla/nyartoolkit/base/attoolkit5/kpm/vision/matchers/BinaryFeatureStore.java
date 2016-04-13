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






/**
 * Represents a container for features and point information.
 */
public class BinaryFeatureStore
{
	public BinaryFeatureStore(int bytesPerFeature)
	{
		this.mNumBytesPerFeature=bytesPerFeature;
		this.mPoints=null;
		this.mFeatures=null;
	}
	public BinaryFeatureStore(int bytesPerFeature,byte[] i_descripters,FeaturePointStack i_points)
	{
		this.mNumBytesPerFeature=bytesPerFeature;
		this.mPoints=i_points;
		this.mFeatures=i_descripters;
	}


    /**
     * Resize the feature store to hold NUMFEATURES.
     */
    public void resize(int numFeatures)
    {
    	byte[] old_features=this.mFeatures;
    	FeaturePointStack old_points=this.mPoints;
        this.mFeatures=new byte[this.mNumBytesPerFeature*numFeatures];
        
        this.mPoints=new FeaturePointStack(numFeatures);    		
    	if(old_features!=null){
	    	//サイズを変えるときはコピーする。
	        System.arraycopy(old_features,0,this.mFeatures,0,Math.min(old_features.length,numFeatures*this.mNumBytesPerFeature));
    	}
		if(old_points!=null){
			for(int i=0;i<old_points.getLength();i++){
				FeaturePoint fp=this.mPoints.prePush();
				fp.set(old_points.getItem(i));
			}
		}
		return;
    }
    
    /**
     * @return Number of features.
     */
    public int size()
    {
        return mPoints.getLength();
    }
    
    /**
     * Set number of bytes per feature.
     */
    public void setNumBytesPerFeature(int bytesPerFeature) {
    	this.mNumBytesPerFeature = bytesPerFeature;
    }
    
    /**
     * @return Number of bytes per feature
     */
    public int numBytesPerFeature(){
    	return this.mNumBytesPerFeature;
    }
    
    /**
     * @return Vector of features
     */
//    inline std::vector<unsigned char>& features() { return mFeatures; }
    public byte[] features(){
    	return this.mFeatures;
    }
    
    /**
     * インデクスを
     * @return Specific feature with an index
     */
//    inline unsigned char* feature(size_t i) { return &mFeatures[i*mNumBytesPerFeature]; }
    public int feature(int i) {
    	return i*this.mNumBytesPerFeature;
    }
    
    /**
     * @return Vector of feature points
     */
    public FeaturePointStack points(){
    	return this.mPoints;
    }
    
    /**
     * @return Specific point with an index
     */
    public FeaturePoint point(int i) {
    	return mPoints.getItem(i);
    }

    /**
     * DeepCopy
     * Copy a feature store.
     */
//    void copy(BinaryFeatureStore store) {
//    	
//        this.mNumBytesPerFeature = store.mNumBytesPerFeature;
//        this.mFeatures=store.mFeatures.clone();//プリミティブ型だからディープコピー相当
//        this.mPoints = (FeaturePointStack) store.mPoints.clone();
//    }
    
    //
    // Serialization
    //
    
    /*template<class Archive>
    void serialize(Archive & ar, const unsigned int version) {
        ar & mNumBytesPerFeature;
        ar & mFeatures;
        ar & mPoints;
    }*/
    


    // Number of bytes per feature
    private int mNumBytesPerFeature;
    
    // Vector of features
    private byte[] mFeatures;

    // Vector of feature points
    private FeaturePointStack mPoints;
};
