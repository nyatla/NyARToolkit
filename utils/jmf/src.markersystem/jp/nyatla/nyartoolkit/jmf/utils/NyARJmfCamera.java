package jp.nyatla.nyartoolkit.jmf.utils;

import java.awt.Dimension;

import javax.media.Buffer;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.markersystem.NyARSensor;

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
	private static NyARIntSize dimension2NyARSize(Dimension d)
	{
		return new NyARIntSize(d.width,d.height);
	}
	/**
	 * キャプチャデバイスをバインド下MarkerSystemのセンサシステムを構築します。バインドしたキャプチャデバイスは、このインスタンスが操作します。
	 * 直接操作しないでください。
	 * @param i_config
	 * @param i_capdev
	 * @throws NyARException
	 */
	public NyARJmfCamera(JmfCaptureDevice i_capdev) throws NyARException
	{
		super(dimension2NyARSize(i_capdev.getCaptureFormat().getSize()));
		//RGBラスタの生成
		this._raster = new JmfNyARRGBRaster(i_capdev.getCaptureFormat());
		//ラスタのセット
		this.update(this._raster);
		i_capdev.setOnCapture(this);
		this._cdev=i_capdev;
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
				this.update(this._raster);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}