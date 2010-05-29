/* 
 * PROJECT: NyARToolkit Java3D utilities.
 * --------------------------------------------------------------------------------
 * The MIT License
 * Copyright (c) 2008 nyatla
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/nyartoolkit/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
package jp.nyatla.nyartoolkit.java3d.utils;

import java.util.Enumeration;

import javax.media.Buffer;
import javax.media.j3d.*;
import javax.vecmath.*;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.detector.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * NyARToolkitと連動したBehaviorを返却するクラスです。
 * 提供できるBehaviorは、BackgroundとTransformgroupです。
 *
 */
public class NyARSingleMarkerBehaviorHolder implements JmfCaptureListener
{
	private NyARParam _cparam;

	private JmfCaptureDevice _capture;

	private J3dNyARRaster_RGB _nya_raster;//最大3スレッドで共有されるので、排他制御かけること。

	private NyARSingleDetectMarker _nya;

	//Behaviorホルダ
	private NyARBehavior _nya_behavior;

	public NyARSingleMarkerBehaviorHolder(NyARParam i_cparam, float i_rate, NyARCode i_ar_code, double i_marker_width) throws NyARException
	{
		this._nya_behavior = null;
		final NyARIntSize scr_size = i_cparam.getScreenSize();
		this._cparam = i_cparam;
		//キャプチャの準備
		JmfCaptureDeviceList devlist=new JmfCaptureDeviceList();
		this._capture=devlist.getDevice(0);
		this._capture.setCaptureFormat(scr_size.w, scr_size.h,15f);
		this._capture.setOnCapture(this);		
		this._nya_raster = new J3dNyARRaster_RGB(this._cparam,this._capture.getCaptureFormat());
		this._nya = new NyARSingleDetectMarker(this._cparam, i_ar_code, i_marker_width,this._nya_raster.getBufferType());
		this._nya_behavior = new NyARBehavior(this._nya, this._nya_raster, i_rate);
	}

	public Behavior getBehavior()
	{
		return this._nya_behavior;
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
		this._nya_behavior.setRelatedBackGround(i_back_ground);
	}

	/**
	 * i_trgroupの座標系をマーカーにあわせるようにBehaviorを設定します。
	 *
	 */
	public void setTransformGroup(TransformGroup i_trgroup)
	{
		//コール先で排他制御
		this._nya_behavior.setRelatedTransformGroup(i_trgroup);
	}

	/**
	 * 座標系再計算後に呼び出されるリスナです。
	 * @param i_listener
	 */
	public void setUpdateListener(NyARSingleMarkerBehaviorListener i_listener)
	{
		//コール先で排他制御
		this._nya_behavior.setUpdateListener(i_listener);
	}

	/**
	 * ラスタを更新 コールバック関数だから呼んじゃらめえ
	 */
	public void onUpdateBuffer(Buffer i_buffer)
	{
		try {
			synchronized (this._nya_raster) {
				this._nya_raster.setBuffer(i_buffer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() throws NyARException
	{
		//開始
		this._capture.start();
	}

	public void stop()
	{
		this._capture.stop();
	}
}

class NyARBehavior extends Behavior
{
	private NyARTransMatResult trans_mat_result = new NyARTransMatResult();

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
	public NyARBehavior(NyARSingleDetectMarker i_related_nya, J3dNyARRaster_RGB i_related_raster, float i_rate)
	{
		super();
		wakeup = new WakeupOnElapsedTime((int) (1000 / i_rate));
		related_nya = i_related_nya;
		trgroup = null;
		raster = i_related_raster;
		back_ground = null;
		listener = null;
		this.setSchedulingBounds(new BoundingSphere(new Point3d(), 100.0));
	}

	public void setRelatedBackGround(Background i_back_ground)
	{
		synchronized (raster) {
			back_ground = i_back_ground;
		}
	}

	public void setRelatedTransformGroup(TransformGroup i_trgroup)
	{
		synchronized (raster) {
			trgroup = i_trgroup;
		}
	}

	public void setUpdateListener(NyARSingleMarkerBehaviorListener i_listener)
	{
		synchronized (raster) {
			listener = i_listener;
		}
	}

	/**
	 * いわゆるイベントハンドラ
	 */
	public void processStimulus(Enumeration criteria)
	{
		try {
			synchronized (raster) {
				Transform3D t3d = null;
				boolean is_marker_exist = false;
				if (back_ground != null) {
					raster.renewImageComponent2D();/*DirectXモードのときの対策*/
					back_ground.setImage(raster.getImageComponent2D());
				}
				if (raster.hasBuffer()) {
					is_marker_exist = related_nya.detectMarkerLite(raster, 100);
					if (is_marker_exist)
					{
						final NyARTransMatResult src = this.trans_mat_result;
						related_nya.getTransmationMatrix(src);
//						Matrix4d matrix = new Matrix4d(src.m00, -src.m10, -src.m20, 0, -src.m01, src.m11, src.m21, 0, -src.m02, src.m12, src.m22, 0, -src.m03, src.m13, src.m23, 1);
						Matrix4d matrix = new Matrix4d(
								-src.m00, -src.m10, src.m20, 0,
								-src.m01, -src.m11, src.m21, 0,
								-src.m02, -src.m12, src.m22, 0,
							   -src.m03,-src.m13, src.m23, 1);
						matrix.transpose();
						t3d = new Transform3D(matrix);
						if (trgroup != null) {
							trgroup.setTransform(t3d);
						}
					}
				}
				if (listener != null) {
					listener.onUpdate(is_marker_exist, t3d);
				}
			}
			wakeupOn(wakeup);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
