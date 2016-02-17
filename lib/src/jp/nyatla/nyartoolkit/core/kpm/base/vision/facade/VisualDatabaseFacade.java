package jp.nyatla.nyartoolkit.core.kpm.base.vision.facade;
import java.util.*;

import jp.nyatla.nyartoolkit.core.kpm.base.Point3dVector;
import jp.nyatla.nyartoolkit.core.kpm.base.vision.Keyframe;
import jp.nyatla.nyartoolkit.core.kpm.base.vision.matchers.BinaryFeatureMatcher;
import jp.nyatla.nyartoolkit.core.kpm.base.vision.matchers.BinaryFeatureStore;
import jp.nyatla.nyartoolkit.core.kpm.base.vision.matchers.FREAKExtractor;
import jp.nyatla.nyartoolkit.core.kpm.base.vision.matchers.FeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.base.vision.matchers.VisualDatabase;
import jp.nyatla.nyartoolkit.core.kpm.base.vision.matchers.matchStack;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;

public class VisualDatabaseFacade
{
	class Point3dMap extends LinkedHashMap<Integer,Point3dVector>{
		
	}
    class VisualDatabaseImpl{
    	public VisualDatabaseImpl(){
    		mVdb=new VisualDatabase<BinaryFeatureStore>();
    	}
    	public VisualDatabase<BinaryFeatureStore> mVdb;

//        std::unique_ptr<vdb_t> mVdb;
        public Point3dMap mPoint3d=new Point3dMap();
    }
    public VisualDatabaseFacade(){
        this.mVisualDbImpl=new VisualDatabaseImpl();
    }
    private VisualDatabaseImpl mVisualDbImpl;
/*

    
    void addImage(unsigned char* grayImage,
                                        size_t width,
                                        size_t height,
                                        int image_id) {
        Image img;
        img.deepCopy(Image(grayImage,IMAGE_UINT8,width,height,(int)width,1));
        mVisualDbImpl->mVdb->addImage(img, image_id);
    }
*/   
    public void addFreakFeaturesAndDescriptors(FeaturePointStack featurePoints,
                                                              byte[] descriptors,
                                                              Point3dVector points3D,
                                                              int width,
                                                              int height,
                                                              int image_id){
//        std::shared_ptr<Keyframe<96> > keyframe(new Keyframe<96>());
        Keyframe keyframe=new Keyframe(
        	96,
        	width,height,
        	new BinaryFeatureStore(96,descriptors,featurePoints));
//        keyframe.setWidth(width);
//        keyframe.setHeight(height);
//        keyframe.store().setNumBytesPerFeature(96);
//        keyframe.store().points().resize(featurePoints.getLength());
//        keyframe.store().points() = featurePoints;
//        keyframe.store().features().resize(descriptors.size());
//        keyframe.store().features() = descriptors;
        keyframe.buildIndex();
        mVisualDbImpl.mVdb.addKeyframe(keyframe, image_id);
        mVisualDbImpl.mPoint3d.put(image_id,points3D);
        return;
    }
/*    
    void computeFreakFeaturesAndDescriptors(unsigned char* grayImage,
                                                                  size_t width,
                                                                  size_t height,
                                                                  std::vector<FeaturePoint>& featurePoints,
                                                                  std::vector<unsigned char>& descriptors){
        Image img = Image(grayImage,IMAGE_UINT8,width,height,(int)width,1);
        std::unique_ptr<vdb_t> tmpDb(new vdb_t());
        tmpDb->addImage(img, 1);
        featurePoints = tmpDb->keyframe(1)->store().points();
        descriptors = tmpDb->keyframe(1)->store().features();
    }
   */ 
    public boolean query(INyARGrayscaleRaster grayImage){
        return mVisualDbImpl.mVdb.query(grayImage);
    }
    /*
    boolean erase(int image_id){
        return mVisualDbImpl->mVdb->erase(image_id);
    }
    
    const size_t VisualDatabaseFacade::databaseCount(){
        return mVisualDbImpl->mVdb->databaseCount();
    }
    

    
    const float* matchedGeometry(){
        return mVisualDbImpl->mVdb->matchedGeometry();
    }
    
    const std::vector<FeaturePoint>& getFeaturePoints(int image_id) const{
        return mVisualDbImpl->mVdb->keyframe(image_id)->store().points();
    }
    
    const std::vector<unsigned char> &VisualDatabaseFacade::getDescriptors(int image_id) const{
        return mVisualDbImpl->mVdb->keyframe(image_id)->store().features();
    }
    
    const std::vector<vision::Point3d<float> >& VisualDatabaseFacade::get3DFeaturePoints(int image_id) const{
        return mVisualDbImpl->mPoint3d[image_id];
    }*/
    
    public FeaturePointStack getQueryFeaturePoints()
    {
        return mVisualDbImpl.mVdb.queryKeyframe().store().points();
    }
    public byte[] getQueryDescriptors()
    {
        return mVisualDbImpl.mVdb.queryKeyframe().store().features();
    }
    /*
    
    int getWidth(int image_id) const{
        return mVisualDbImpl->mVdb->keyframe(image_id)->width();
    }
    int getHeight(int image_id) const{
        return mVisualDbImpl->mVdb->keyframe(image_id)->height();
    }*/

	public matchStack inliers() {
		// TODO Auto-generated method stub
		return mVisualDbImpl.mVdb.inliers();
	}

	public int matchedId() {
        return mVisualDbImpl.mVdb.matchedId();
	}

    public Point3dVector get3DFeaturePoints(int image_id)
    {
        return mVisualDbImpl.mPoint3d.get(image_id);
    }	


}
