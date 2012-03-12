package jp.nyatla.nyartoolkit.jogl.utils;

import java.awt.image.BufferedImage;

import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.INyARPerspectiveCopy;
import jp.nyatla.nyartoolkit.jogl.utils.NyARGLUtil;
import jp.nyatla.nyartoolkit.markersystem.INyARMarkerSystemConfig;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystem;
import jp.nyatla.nyartoolkit.markersystem.NyARSensor;
import jp.nyatla.nyartoolkit.utils.j2se.NyARBufferedImageRaster;

public class NyARGlMarkerSystem extends NyARMarkerSystem
{
	
	public NyARGlMarkerSystem(INyARMarkerSystemConfig i_config) throws NyARException
	{
		super(i_config);
	}
	protected void initInstance(INyARMarkerSystemConfig i_config) throws NyARException
	{
		super.initInstance(i_config);
		this._projection_mat=new double[16];
	}

	private double[] _projection_mat;

	/**
	 * OpenGLスタイルのProjectionMatrixを返します。
	 * @param i_gl
	 * @return
	 * [readonly]
	 */
	public double[] getGlProjectionMatrix()
	{
		return this._projection_mat;
	}
	public void setProjectionMatrixClipping(double i_near,double i_far)
	{
		super.setProjectionMatrixClipping(i_near,i_far);
		NyARGLUtil.toCameraFrustumRH(this._ref_param,1,i_near,i_far,this._projection_mat);
	}
	/**
	 * この関数は、i_bufに指定idのOpenGL形式の姿勢変換行列を設定して返します。
	 * @param i_id
	 * @param i_buf
	 * @return
	 */
	public void getMarkerMatrix(int i_id,double[] i_buf)
	{
		NyARGLUtil.toCameraViewRH(this.getMarkerMatrix(i_id),1,i_buf);
	}
	/**
	 * この関数はOpenGL形式の姿勢変換行列を新規に割り当てて返します。
	 * @param i_buf
	 * @return
	 */
	public double[] getGlMarkerMatrix(int i_id)
	{
		double[] b=new double[16];
		this.getMarkerMatrix(i_id,b);
		return b;
	}	
	//
	// This reogion may be moved to NyARJ2seMarkerSystem.
	//
	
	/**
	 * {@link #addARMarker(INyARRgbRaster, int, int, double)}のラッパーです。BufferedImageからマーカパターンを作ります。
	 * 引数については、{@link #addARMarker(INyARRgbRaster, int, int, double)}を参照してください。
	 * @param i_img
	 * @param i_patt_resolution
	 * @param i_patt_edge_percentage
	 * @param i_marker_size
	 * @return
	 * @throws NyARException
	 */
	public int addARMarker(BufferedImage i_img,int i_patt_resolution,int i_patt_edge_percentage,double i_marker_size) throws NyARException
	{
		int w=i_img.getWidth();
		int h=i_img.getHeight();
		NyARBufferedImageRaster bmr=new NyARBufferedImageRaster(i_img);
		NyARCode c=new NyARCode(i_patt_resolution,i_patt_resolution);
		//ラスタからマーカパターンを切り出す。
		INyARPerspectiveCopy pc=(INyARPerspectiveCopy)bmr.createInterface(INyARPerspectiveCopy.class);
		NyARRgbRaster tr=new NyARRgbRaster(i_patt_resolution,i_patt_resolution);
		pc.copyPatt(0,0,w,0,w,h,0,h,i_patt_edge_percentage, i_patt_edge_percentage,4, tr);
		//切り出したパターンをセット
		c.setRaster(tr);
		return super.addARMarker(c,i_patt_edge_percentage,i_marker_size);
	}
	/**
	 * この関数は、{@link #getMarkerPlaneImage(int, NyARSensor, int, int, int, int, int, int, int, int, INyARRgbRaster)}
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
	 * @throws NyARException
	 */
	public void getMarkerPlaneImage(
		int i_id,
		NyARSensor i_sensor,
	    int i_x1,int i_y1,
	    int i_x2,int i_y2,
	    int i_x3,int i_y3,
	    int i_x4,int i_y4,
	    BufferedImage i_img) throws NyARException
		{
			NyARBufferedImageRaster bmr=new NyARBufferedImageRaster(i_img);
			super.getMarkerPlaneImage(i_id, i_sensor, i_x1, i_y1, i_x2, i_y2, i_x3, i_y3, i_x4, i_y4,bmr);
			return;
		}
	/**
	 * この関数は、{@link #getMarkerPlaneImage(int, NyARSensor, int, int, int, int, INyARRgbRaster)}
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
	 * @throws NyARException
	 */
	public void getMarkerPlaneImage(
		int i_id,
		NyARSensor i_sensor,
	    int i_l,int i_t,
	    int i_w,int i_h,
	    BufferedImage i_img) throws NyARException
    {
		NyARBufferedImageRaster bmr=new NyARBufferedImageRaster(i_img);
		super.getMarkerPlaneImage(i_id, i_sensor, i_l, i_t, i_w, i_h, bmr);
		this.getMarkerPlaneImage(i_id,i_sensor,i_l+i_w-1,i_t+i_h-1,i_l,i_t+i_h-1,i_l,i_t,i_l+i_w-1,i_t,bmr);
		return;
    }
	
}
