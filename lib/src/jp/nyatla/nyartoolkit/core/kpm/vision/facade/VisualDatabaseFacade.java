package jp.nyatla.nyartoolkit.core.kpm.vision.facade;
import java.util.*;

import jp.nyatla.nyartoolkit.core.kpm.Point3dVector;
import jp.nyatla.nyartoolkit.core.kpm.vision.Keyframe;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FreakFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.VisualDatabase;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.matchStack;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;

public class VisualDatabaseFacade
{
	class Point3dMap extends LinkedHashMap<Integer,Point3dVector>{
		
	}
	final private VisualDatabase<FreakFeaturePointStack> mVisualDbImpl;
	final private Point3dMap mPoint3d;

    public VisualDatabaseFacade(int i_width,int i_height)
    {
    	this.mVisualDbImpl=new VisualDatabase<FreakFeaturePointStack>(i_width,i_height);
    	this.mPoint3d=new Point3dMap();
    }

    public void addFreakFeaturesAndDescriptors(FreakFeaturePointStack featurePoints,
                                                              Point3dVector points3D,
                                                              int width,
                                                              int height,
                                                              int image_id){

        Keyframe keyframe=new Keyframe(
        	width,height,
        	featurePoints);

        keyframe.buildIndex();
        mVisualDbImpl.addKeyframe(keyframe, image_id);
        mPoint3d.put(image_id,points3D);
        return;
    }

    public boolean query(INyARGrayscaleRaster grayImage){
        return mVisualDbImpl.query(grayImage);
    }
 
    
    public FreakFeaturePointStack getQueryFeaturePoints()
    {
        return mVisualDbImpl.queryKeyframe().store();
    }
//    public byte[] getQueryDescriptors()
//    {
//        return mVisualDbImpl.queryKeyframe().store().features();
//    }

	public matchStack inliers() {
		// TODO Auto-generated method stub
		return mVisualDbImpl.inliers();
	}

	public int matchedId() {
        return mVisualDbImpl.matchedId();
	}

    public Point3dVector get3DFeaturePoints(int image_id)
    {
        return mPoint3d.get(image_id);
    }	


}
