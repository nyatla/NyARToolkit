package jp.nyatla.nyartoolkit.jmf.utils;

import javax.media.Buffer;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.nyar.NyARSensor;

/**
 * このクラスは、画像入力にJMFカメラを使用する{@link #NyARSensor}です。
 * JMFからの非同期入力で、画像を更新します。
 * 入力画像開始/停止/キープAPIを提供します。
 * 
 * 非同期更新を一時的に停止するときは、インスタンスをsynchronizedでロックしてください。
 */
public class NyARJmfCamera extends NyARSensor implements JmfCaptureListener
{
	private JmfCaptureDevice _cdev;
	private JmfNyARRGBRaster _raster;
	public NyARJmfCamera(NyARParam i_param,float i_fps) throws NyARException
	{
		super(i_param);
		JmfCaptureDeviceList devlist = new JmfCaptureDeviceList();
		JmfCaptureDevice d = devlist.getDevice(0);
		NyARIntSize s=i_param.getScreenSize();
		if (!d.setCaptureFormat(s.w,s.h,i_fps)) {
			throw new NyARException();
		}
		//RGBラスタの生成
		this._raster = new JmfNyARRGBRaster(d.getCaptureFormat());
		//ラスタのセット
		this.update(this._raster);
		d.setOnCapture(this);
		this._cdev=d;
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
	public void onUpdateBuffer(Buffer i_buffer)
	{
		//ロックされていなければ、RGBラスタを更新する。
		synchronized(this){
			try{
				this._raster.setBuffer(i_buffer);
				this.updateTimeStamp();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}