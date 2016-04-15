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
package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.facade;
import java.util.*;

import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.Point3dVector;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.Keyframe;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers.BinaryFeatureMatcher;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers.BinaryFeatureStore;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers.FREAKExtractor;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers.FeaturePointStack;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers.VisualDatabase;
import jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers.matchStack;
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
