package jp.nyatla.nyartoolkit.core.kpm.vision.facade;


import jp.nyatla.nyartoolkit.core.kpm.keyframe.Keyframe;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FreakFeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.FreakMatchPointSetStack;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.VisualDatabase;
import jp.nyatla.nyartoolkit.core.kpm.vision.matchers.matchStack;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;

public class VisualDatabaseFacade
{

	final private VisualDatabase<FreakFeaturePointStack> mVisualDbImpl;

    public VisualDatabaseFacade(int i_width,int i_height)
    {
    	this.mVisualDbImpl=new VisualDatabase<FreakFeaturePointStack>(i_width,i_height);

    }

    public void addFreakFeaturesAndDescriptors(FreakMatchPointSetStack featurePoints,int width,int height,int image_id)
    {

    	Keyframe keyframe=new Keyframe(
        	width,height,
        	featurePoints);

        keyframe.buildIndex();
        mVisualDbImpl.addKeyframe(keyframe, image_id);

        return;
    }

    public boolean query(INyARGrayscaleRaster grayImage){
        return mVisualDbImpl.query(grayImage);
    }
 
    
    public FreakFeaturePointStack getQueryFeaturePoints()
    {
        return mVisualDbImpl.queryKeyframe();
    }

	public matchStack inliers() {
		// TODO Auto-generated method stub
		return mVisualDbImpl.inliers();
	}

	public int matchedId() {
        return mVisualDbImpl.matchedId();
	}

	public Keyframe getKeyFeaturePoints(int matched_image_id) {
		return this.mVisualDbImpl.getKeyFeatureFrame(matched_image_id);
	}




}
