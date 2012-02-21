package jp.nyatla.nyartoolkit.qt.utils;

import jp.nyatla.nyartoolkit.core.NyARException;
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
	public NyARQtCamera(QtCameraCapture i_capture) throws NyARException
	{
		super(i_capture.getSize());
		this._cdev=i_capture;
		this._cdev.setCaptureListener(this);
		//RGBラスタの生成
		this._raster = new QtNyARRaster_RGB(i_capture.getSize().w,i_capture.getSize().h);
		
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