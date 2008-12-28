/* 
 * PROJECT: NyARToolkit JMF sample program.
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
package jp.nyatla.nyartoolkit.jmf.sample;

import javax.media.*;

import javax.media.util.BufferToImage;
import javax.media.format.*;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;

import java.awt.*;

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.detector.NyARSingleDetectMarker;

/**
 * VFM+ARToolkitテストプログラム
 * カメラから取り込んだデータからマーカーを検出して、一致度と変換行列を表示します。
 */
public class NyarToolkitLinkTest extends Frame implements JmfCaptureListener
{
	private static final long serialVersionUID = 6471434231970804953L;

	private final String CARCODE_FILE = "../../Data/patt.hiro";

	private final String PARAM_FILE = "../../Data/camera_para.dat";

	private JmfCaptureDevice _capture;

	private NyARSingleDetectMarker _nya;

	private JmfNyARRaster_RGB _raster;

	private NyARTransMatResult _trans_mat_result = new NyARTransMatResult();

	public NyarToolkitLinkTest() throws NyARException, NyARException
	{
		setTitle("JmfCaptureTest");
		setBounds(0, 0, 320 + 64, 240 + 64);
		//キャプチャの準備
		JmfCaptureDeviceList devlist=new JmfCaptureDeviceList();
		_capture=devlist.getDevice(0);
		_capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_RGB,320, 240,15f);
		_capture.setOnCapture(this);

		//NyARToolkitの準備
		NyARParam ar_param = new NyARParam();
		NyARCode ar_code = new NyARCode(16, 16);
		ar_param.loadARParamFromFile(PARAM_FILE);
		ar_param.changeScreenSize(320, 240);
		this._nya = new NyARSingleDetectMarker(ar_param, ar_code, 80.0);
		ar_code.loadARPattFromFile(CARCODE_FILE);
		//キャプチャイメージ用のラスタを準備
		this._raster = new JmfNyARRaster_RGB(320, 240);
		return;
	}

	public void onUpdateBuffer(Buffer i_buffer)
	{
		try {
			//キャプチャしたバッファをラスタにセット
			this._raster.setBuffer(i_buffer);

			//キャプチャしたイメージを表示用に加工
			BufferToImage b2i = new BufferToImage((VideoFormat) i_buffer.getFormat());
			Image img = b2i.createImage(i_buffer);

			Graphics g = getGraphics();

			//マーカー検出
			boolean is_marker_exist = this._nya.detectMarkerLite(this._raster, 100);
			if (is_marker_exist) {
				//変換行列を取得
				this._nya.getTransmationMatrix(this._trans_mat_result);
			}
			//情報を画面に書く       
			g.drawImage(img, 32, 32, this);
			if (is_marker_exist) {
				g.drawString("マーカー検出:" + this._nya.getConfidence(), 32, 50);
				g.drawString("[m00]" +this._trans_mat_result.m00, 32, 50 + 16*1);
				g.drawString("[m01]" +this._trans_mat_result.m01, 32, 50 + 16*2);
				g.drawString("[m02]" +this._trans_mat_result.m02, 32, 50 + 16*3);
				g.drawString("[m03]" +this._trans_mat_result.m03, 32, 50 + 16*4);
				g.drawString("[m10]" +this._trans_mat_result.m10, 32, 50 + 16*5);
				g.drawString("[m11]" +this._trans_mat_result.m11, 32, 50 + 16*6);
				g.drawString("[m12]" +this._trans_mat_result.m12, 32, 50 + 16*7);
				g.drawString("[m13]" +this._trans_mat_result.m13, 32, 50 + 16*8);
				g.drawString("[m20]" +this._trans_mat_result.m20, 32, 50 + 16*9);
				g.drawString("[m21]" +this._trans_mat_result.m21, 32, 50 + 16*10);
				g.drawString("[m22]" +this._trans_mat_result.m22, 32, 50 + 16*11);
				g.drawString("[m23]" +this._trans_mat_result.m23, 32, 50 + 16*12);
			} else {
				g.drawString("マーカー未検出:", 32, 100);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void startCapture()
	{
		try {
			this._capture.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		try {
			NyarToolkitLinkTest mainwin = new NyarToolkitLinkTest();
			mainwin.setVisible(true);
			mainwin.startCapture();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
