package jp.nyatla.nyartoolkit.qt.utils;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.markersystem.INyARMarkerSystemConfig;
import jp.nyatla.nyartoolkit.markersystem.NyARSensor;

/**
 * このクラスは、画像入力にQuicktimeカメラを使用する{@link #NyARSensor}です。
 * Quicktimeからの非同期入力で、画像を更新します。
 * 入力画像開始/停止APIを提供します。
 * 
 * 非同期更新を一時的に停止するときは、インスタンスをsynchronizedでロックしてください。
 */
public class NyARQtCamera extends NyARSensor implements QtCaptureListener
{
	private QtCameraCapture _cdev;
	private QtNyARRaster_RGB _raster;
	public NyARQtCamera(INyARMarkerSystemConfig i_config,float i_fps) throws NyARException
	{
		super(i_config);
		NyARIntSize s=i_config.getNyARParam().getScreenSize();
		this._cdev = new QtCameraCapture(s.w,s.h,i_fps);
		this._cdev.setCaptureListener(this);
		//RGBラスタの生成
		this._raster = new QtNyARRaster_RGB(s.w,s.h);
		//ラスタのセット
		this.update(this._raster);
	}
	/**
	 * この関数は、JMFの非同期更新を停止します。
	 */
	public void stop()
	{
		this._cdev.stop();
	}
	/**
	 * この関数は、JMFの非同期更新を開始します。
	 */
	public void start() throws NyARException
	{
		this._cdev.start();
		//1枚目の画像が取得され、RGBラスタにデータがセットされるまで待つ。
		while(!this._raster.hasBuffer()){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				throw new NyARException(e);
			}
		}
	}
	public void onUpdateBuffer(byte[] pixels)
	{
		try{
			synchronized(this){
				this._raster.setQtImage(pixels);
				this.updateTimeStamp();
			}
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
	}
}