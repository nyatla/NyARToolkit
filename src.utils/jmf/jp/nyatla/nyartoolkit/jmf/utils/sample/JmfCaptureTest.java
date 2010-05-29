/*
 * PROJECT: NyARToolkit JMF utilities.
 * --------------------------------------------------------------------------------
 * The MIT License
 * Copyright (c) 2008 nyatla
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

package jp.nyatla.nyartoolkit.jmf.utils.sample;

import javax.media.*;

import javax.media.util.BufferToImage;
import javax.media.format.*;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;

import java.awt.*;

/**
 * VFMキャプチャテストプログラム
 */
public class JmfCaptureTest extends Frame implements JmfCaptureListener
{
	private static final long serialVersionUID = -2110888320986446576L;
	private JmfCaptureDevice _capture;
	public JmfCaptureTest() throws NyARException
	{
		setTitle("JmfCaptureTest");
		setBounds(0, 0, 320 + 64, 240 + 64);
		JmfCaptureDeviceList dl=new JmfCaptureDeviceList();
		this._capture=dl.getDevice(0);
		if(!this._capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_RGB,320,240,30.0f)){
			if(!this._capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_YUV,320,240,30.0f)){
				throw new NyARException("キャプチャフォーマットが見つかりません。");
			}
		}
		this._capture.setOnCapture(this);
	}


	public void onUpdateBuffer(Buffer i_buffer)
	{
		BufferToImage b2i = new BufferToImage((VideoFormat) i_buffer.getFormat());
		Image img = b2i.createImage(i_buffer);
		Graphics g = getGraphics();
		g.drawImage(img, 32, 32, this);
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
			JmfCaptureTest mainwin = new JmfCaptureTest();
			mainwin.setVisible(true);
			mainwin.startCapture();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
