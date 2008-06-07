/**
 * NyARToolkitのBehaviorホルダー
 * (c)2008 A虎＠nyatla.jp
 * airmail@ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.java3d.utils;

import java.util.Enumeration;

import javax.media.Buffer;
import javax.media.j3d.*;
import javax.vecmath.*;

import jp.nyatla.nyartoolkit.core.NyARParam;
import jp.nyatla.nyartoolkit.detector.NyARSingleDetectMarker;
import jp.nyatla.nyartoolkit.jmf.*;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCameraCapture;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureListener;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.*;

/**
 * NyARToolkitと連動したBehaviorを返却するクラスです。
 * 提供できるBehaviorは、BackgroundとTransformgroupです。
 *
 */
public class NyARSingleMarkerBehaviorHolder implements JmfCaptureListener
{
    private NyARParam cparam;
    private JmfCameraCapture capture;
    private J3dNyARRaster_RGB nya_raster;//最大3スレッドで共有されるので、排他制御かけること。
    private NyARSingleDetectMarker nya;
    //Behaviorホルダ
    private NyARBehavior nya_behavior;
    public NyARSingleMarkerBehaviorHolder(NyARParam i_cparam,float i_rate,NyARCode i_ar_code,double i_marker_width) throws NyARException
    {
	nya_behavior=null;
	cparam=i_cparam;
	capture=new JmfCameraCapture(cparam.getX(),cparam.getY(),i_rate,JmfCameraCapture.PIXEL_FORMAT_RGB);
	capture.setCaptureListener(this);
	nya_raster=new J3dNyARRaster_RGB(cparam);	
	nya=new NyARSingleDetectMarker(cparam,i_ar_code,i_marker_width);
	nya_behavior=new NyARBehavior(nya,nya_raster,i_rate);
    }
    public Behavior getBehavior()
    {
	return nya_behavior;
    }
    /**
     * i_back_groundにキャプチャ画像を転送するようにBehaviorを設定します。
     * i_back_groungはALLOW_IMAGE_WRITE属性を持つものである必要があります。
     * @param i_back_groung
     * @return
     */
    public void setBackGround(Background i_back_ground)
    {
	//コール先で排他制御
        nya_behavior.setRelatedBackGround(i_back_ground);
    }
    /**
     * i_trgroupの座標系をマーカーにあわせるようにBehaviorを設定します。
     *
     */
    public void setTransformGroup(TransformGroup i_trgroup)
    {
	//コール先で排他制御
        nya_behavior.setRelatedTransformGroup(i_trgroup);
    }
    /**
     * 座標系再計算後に呼び出されるリスナです。
     * @param i_listener
     */
    public void setUpdateListener(NyARSingleMarkerBehaviorListener i_listener)
    {
	//コール先で排他制御
	nya_behavior.setUpdateListener(i_listener);
    }
    /**
     * ラスタを更新 コールバック関数だから呼んじゃらめえ
     */
    public void onUpdateBuffer(Buffer i_buffer)
    {
	try{
	    synchronized(nya_raster){
		nya_raster.setBuffer(i_buffer);
	    }
	}catch(Exception e){
	    e.printStackTrace();
	}
    }
    public void start() throws NyARException
    {
	//開始
	capture.start();
    }
    public void stop()
    {
	capture.stop();
    }
}


class NyARBehavior extends Behavior
{
    private NyARSingleDetectMarker related_nya;
    private TransformGroup trgroup;
    private Background back_ground;
    private J3dNyARRaster_RGB raster;
    private WakeupCondition wakeup;
    private NyARSingleMarkerBehaviorListener listener;
    public void initialize()
    {
	wakeupOn(wakeup);
    }
    /**
     * i_related_ic2dの内容で定期的にi_back_groundを更新するBehavior
     * @param i_back_ground
     * @param i_related_ic2d
     */
    public NyARBehavior(NyARSingleDetectMarker i_related_nya,J3dNyARRaster_RGB i_related_raster,float i_rate)
    {
	super();
	wakeup=new WakeupOnElapsedTime((int)(1000/i_rate));
	related_nya=i_related_nya;
	trgroup    =null;
	raster     =i_related_raster;
        back_ground=null;
        listener=null;
        this.setSchedulingBounds(new BoundingSphere(new Point3d(), 100.0));
   }
    public void setRelatedBackGround(Background i_back_ground)
    {
	synchronized(raster){
	    back_ground=i_back_ground;
	}
    }
    public void setRelatedTransformGroup(TransformGroup i_trgroup)
    {
	synchronized(raster){
	    trgroup=i_trgroup;
	}	
    }
    public void setUpdateListener(NyARSingleMarkerBehaviorListener i_listener)
    {
	synchronized(raster){
	    listener=i_listener;
	}
    }

    /**
     * いわゆるイベントハンドラ
     */
    public void processStimulus(Enumeration criteria)
    {
	try{
            synchronized(raster){
        	Transform3D t3d=null;
        	boolean is_marker_exist=false;
        	if(back_ground!=null){
                    back_ground.setImage(raster.getImageComponent2D());        	    
        	}
                if(raster.hasData()){
                    is_marker_exist=related_nya.detectMarkerLite(raster, 100);
                    if(is_marker_exist){
                        NyARMat nymat=related_nya.getTransmationMatrix();
                        double[][] src=nymat.getArray();
                        Matrix4d matrix=new Matrix4d(
                             src[0][0],-src[1][0],-src[2][0],0,
                            -src[0][1], src[1][1], src[2][1],0,
                            -src[0][2], src[1][2], src[2][2],0,
                            -src[0][3], src[1][3], src[2][3],1);
                        matrix.transpose();
                        t3d=new Transform3D(matrix);
                        if(trgroup!=null){
                            trgroup.setTransform(t3d);
                        }
                    }
                }
                if(listener!=null)
                {
                    listener.onUpdate(is_marker_exist, t3d);
                }
            }
            wakeupOn(wakeup);            
	}catch(Exception e){
	    e.printStackTrace();
	}
    }
}

