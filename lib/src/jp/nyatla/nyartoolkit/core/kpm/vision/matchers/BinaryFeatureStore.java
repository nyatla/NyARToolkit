package jp.nyatla.nyartoolkit.core.kpm.vision.matchers;






/**
 * Represents a container for features and point information.
 */
public class BinaryFeatureStore
{
	public BinaryFeatureStore(int bytesPerFeature)
	{
		this.mNumBytesPerFeature=bytesPerFeature;
	}
    public BinaryFeatureStore(){
    	this(0);
    }

    /**
     * Resize the feature store to hold NUMFEATURES.
     */
    public void resize(int numFeatures)
    {
    	int[] old=this.mFeatures;
    	//サイズを変えるときはコピーする。
        this.mFeatures=new int[this.mNumBytesPerFeature*numFeatures];
        System.arraycopy(this.mFeatures,0,old,0,Math.min(old.length,numFeatures));
        this.mPoints=new FeaturePointStack(numFeatures);
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
    public int[] features(){
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
    private int[] mFeatures;

    // Vector of feature points
    private FeaturePointStack mPoints;
};
