package jp.nyatla.nyartoolkit.core.kpm.vision.facade;
import java.util.*;

import jp.nyatla.nyartoolkit.core.kpm.Point3dVector;
import jp.nyatla.nyartoolkit.core.kpm.vision.Keyframe;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.BinaryFeatureMatcher;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.BinaryFeatureStore;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.VisualDatabase;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.matchStack;
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
    	public Point3dMap mPoint3d=new Point3dMap();
    }
    public VisualDatabaseFacade(){
        this.mVisualDbImpl=new VisualDatabaseImpl();
    }
    private VisualDatabaseImpl mVisualDbImpl;

    public void addFreakFeaturesAndDescriptors(FeaturePointStack featurePoints,
                                                              byte[] descriptors,
                                                              Point3dVector points3D,
                                                              int width,
                                                              int height,
                                                              int image_id){

        Keyframe keyframe=new Keyframe(
        	96,
        	width,height,
        	new BinaryFeatureStore(96,descriptors,featurePoints));

        keyframe.buildIndex();
        mVisualDbImpl.mVdb.addKeyframe(keyframe, image_id);
        mVisualDbImpl.mPoint3d.put(image_id,points3D);
        return;
    }

    public boolean query(INyARGrayscaleRaster grayImage){
        return mVisualDbImpl.mVdb.query(grayImage);
    }
 
    
    public FeaturePointStack getQueryFeaturePoints()
    {
        return mVisualDbImpl.mVdb.queryKeyframe().store().points();
    }
    public byte[] getQueryDescriptors()
    {
        return mVisualDbImpl.mVdb.queryKeyframe().store().features();
    }

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
