package jp.nyatla.nyartoolkit.jogl2.utils;

import java.awt.image.BufferedImage;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;


import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.marker.artk.NyARCode;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.perspectivecopy.INyARPerspectiveCopy;
import jp.nyatla.nyartoolkit.j2se.NyARBufferedImageRaster;
import jp.nyatla.nyartoolkit.jogl2.utils.NyARGLUtil;
import jp.nyatla.nyartoolkit.markersystem.INyARMarkerSystemConfig;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystem;
import jp.nyatla.nyartoolkit.markersystem.NyARSensor;

public class NyARGlMarkerSystem extends NyARMarkerSystem
{
	
	public NyARGlMarkerSystem(INyARMarkerSystemConfig i_config) throws NyARRuntimeException
	{
		super(i_config);
	}


	/**
	 * この関数は、o_bufに指定idのOpenGL形式の姿勢変換行列を設定して返します。
	 * @param i_id
	 * @param o_buf
	 * @return
	 * @throws NyARRuntimeException 
	 */
	public double[] getGlTransformMatrix(int i_id,double[] o_buf)
	{
		NyARGLUtil.toCameraViewRH(this.getTransformMatrix(i_id),1,o_buf);
		return o_buf;
	}
	/**
	 * この関数はOpenGL形式の姿勢変換行列を新規に割り当てて返します。
	 * @param i_buf
	 * @return
	 * @throws NyARRuntimeException 
	 */
	public double[] getGlTransformMatrix(int i_id)
	{
		return this.getGlTransformMatrix(i_id,new double[16]);
	}	


	
	/**
	 * {@link #addARMarker(INyARRgbRaster, int, int, double)}のラッパーです。BufferedImageからマーカパターンを作ります。
	 * 引数については、{@link #addARMarker(INyARRgbRaster, int, int, double)}を参照してください。
	 * @param i_img
	 * @param i_patt_resolution
	 * @param i_patt_edge_percentage
	 * @param i_marker_size
	 * @return
	 * @throws NyARRuntimeException
	 */
	public int addARMarker(BufferedImage i_img,int i_patt_resolution,int i_patt_edge_percentage,double i_marker_size)
	{
		int w=i_img.getWidth();
		int h=i_img.getHeight();
		NyARBufferedImageRaster bmr=new NyARBufferedImageRaster(i_img);
		NyARCode c=new NyARCode(i_patt_resolution,i_patt_resolution);
		//ラスタからマーカパターンを切り出す。
		INyARPerspectiveCopy pc=(INyARPerspectiveCopy)bmr.createInterface(INyARPerspectiveCopy.class);
		INyARRgbRaster tr=NyARRgbRaster.createInstance(i_patt_resolution,i_patt_resolution);
		pc.copyPatt(0,0,w,0,w,h,0,h,i_patt_edge_percentage, i_patt_edge_percentage,4, tr);
		//切り出したパターンをセット
		c.setRaster(tr);
		return super.addARMarker(c,i_patt_edge_percentage,i_marker_size);
	}
	/**
	 * この関数は、{@link #getPlaneImage(int, NyARSensor, int, int, int, int, int, int, int, int, INyARRgbRaster)}
	 * のラッパーです。取得画像を{@link #BufferedImage}形式で返します。
	 * @param i_id
	 * @param i_sensor
	 * @param i_x1
	 * @param i_y1
	 * @param i_x2
	 * @param i_y2
	 * @param i_x3
	 * @param i_y3
	 * @param i_x4
	 * @param i_y4
	 * @param i_img
	 * @return
	 */
	public void getPlaneImage(int i_id, NyARSensor i_sensor, int i_x1, int i_y1, int i_x2, int i_y2, int i_x3,int i_y3, int i_x4, int i_y4, BufferedImage i_img)
	{
		NyARBufferedImageRaster bmr = new NyARBufferedImageRaster(i_img);
		super.getPlaneImage(i_id, i_sensor, i_x1, i_y1, i_x2, i_y2, i_x3, i_y3, i_x4, i_y4, bmr);
		return;
	}
	/**
	 * {@link #getPlaneImage}を使ってください。
	 * @deprecated
	 */
	public void getMarkerPlaneImage(int i_id, NyARSensor i_sensor, int i_x1, int i_y1, int i_x2, int i_y2, int i_x3,int i_y3, int i_x4, int i_y4, BufferedImage i_img)
	{
		this.getPlaneImage(i_id, i_sensor, i_x1, i_y1, i_x2, i_y2, i_x3, i_y3, i_x4, i_y4, i_img);
	}
	
	
	/**
	 * この関数は、{@link #getPlaneImage(int, NyARSensor, int, int, int, int, INyARRgbRaster)}
	 * のラッパーです。取得画像を{@link #BufferedImage}形式で返します。
	 * @param i_id
	 * マーカid
	 * @param i_sensor
	 * 画像を取得するセンサオブジェクト。通常は{@link #update(NyARSensor)}関数に入力したものと同じものを指定します。
	 * @param i_l
	 * @param i_t
	 * @param i_w
	 * @param i_h
	 * @param i_raster
	 * 出力先のオブジェクト
	 */
	public void getPlaneImage(
		int i_id,
		NyARSensor i_sensor,
	    int i_l,int i_t,
	    int i_w,int i_h,
	    BufferedImage i_img)
    {
		NyARBufferedImageRaster bmr=new NyARBufferedImageRaster(i_img);
		super.getPlaneImage(i_id, i_sensor, i_l, i_t, i_w, i_h, bmr);
		this.getPlaneImage(i_id,i_sensor,i_l+i_w-1,i_t+i_h-1,i_l,i_t+i_h-1,i_l,i_t,i_l+i_w-1,i_t,bmr);
		return;
    }
	/**
	 * {@link #getPlaneImage}を使用して下さい。
	 * @deprecated
	 */
	public void getMarkerPlaneImage(
			int i_id,
			NyARSensor i_sensor,
		    int i_l,int i_t,
		    int i_w,int i_h,
		    BufferedImage i_img)
	{
		this.getPlaneImage(i_id, i_sensor, i_l, i_t, i_w, i_h, i_img);
	}
	/**
	 * OpenGLスタイルカメラパラメータのワーク変数
	 */
	final private double[] _mv_mat=new double[16];
	
	
	/**
	 * i_glに、i_idで示されるマーカ平面の姿勢行列をセットします。
	 * @param i_gl
	 * @param i_id
	 * @throws NyARRuntimeException 
	 */
	public void loadTransformMatrix(GL i_gl,int i_id)
	{
		GL2 gl=i_gl.getGL2();
		int old_mode=NyARGLUtil.getGlMatrixMode(i_gl);
		if(old_mode!=GL2.GL_MODELVIEW){
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			NyARGLUtil.toCameraViewRH(this.getTransformMatrix(i_id),1,this._mv_mat);			
			gl.glLoadMatrixd(this._mv_mat, 0);
			gl.glMatrixMode(old_mode);
		}else{
			NyARGLUtil.toCameraViewRH(this.getTransformMatrix(i_id),1,this._mv_mat);
			gl.glLoadMatrixd(this._mv_mat, 0);
		}
	}	
	
}
