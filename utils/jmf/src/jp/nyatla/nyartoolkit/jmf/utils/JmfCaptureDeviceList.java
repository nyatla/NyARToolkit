package jp.nyatla.nyartoolkit.jmf.utils;

import java.awt.Dimension;
import java.util.Vector;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.format.*;
import jp.nyatla.nyartoolkit.*;



/**
 * JMFキャプチャデバイスのリストを保持します。
 * 複数のカメラが接続された環境では、最も初めに認識したカメラの実がアクティブになるため、
 * このクラスで実際に認識できるカメラは１個だけです。
 *
 */
public class JmfCaptureDeviceList
{
	private Vector<CaptureDeviceInfo> _devices;

	public JmfCaptureDeviceList() throws NyARException
	{ 
		this._devices = (Vector<CaptureDeviceInfo>)(CaptureDeviceManager.getDeviceList(null).clone());
		// ビデオソースのデバイスだけ残す
		try {

			for (int i = 0; i < this._devices.size();) {
				CaptureDeviceInfo cdi =this._devices.elementAt(i);
				// VideoFormatもってるかな？
				if (!isCaptureDevice(cdi)) {
					this._devices.remove(i);
					continue;
				}
				i++;
			}
		} catch (Exception e) {
			throw new NyARException(e);
		}
		return;
	}

	/**
	 * i_cdiがビデオキャプチャデバイスかを調べる。ようなことをする。
	 * 
	 * @param i_cdi
	 * @return
	 */
	private static boolean isCaptureDevice(CaptureDeviceInfo i_cdi)
	{
		Format[] fms = i_cdi.getFormats();
		for (int i = 0; i < fms.length; i++) {
			Format f = fms[i];
			if (f instanceof VideoFormat) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 使用できるキャプチャデバイスの数を数える
	 * @return
	 * キャプチャデバイスの数を返却する。
	 */
	public int getCount()
	{
		return this._devices.size();
	}
	/**
	 * i_index番目のキャプチャデバイスを得る。
	 * @param i_index
	 * @return
	 * @throws NyARException
	 */
	public JmfCaptureDevice getDevice(int i_index) throws NyARException
	{
		return new JmfCaptureDevice((CaptureDeviceInfo) this._devices.elementAt(i_index));
	}

	public static void main(String[] args)
	{
		//テストケース
		try {
			JmfCaptureDeviceList j = new JmfCaptureDeviceList();
			System.out.println(j.getCount());
			JmfCaptureDevice d = j.getDevice(0);
			d.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_RGB, new Dimension(320, 240), 15.0f);
//			YUVFormat f=(YUVFormat)d.getCaptureFormat();
			d.start();
			d.stop();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

}
