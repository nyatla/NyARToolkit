/* 
 * PROJECT: NyARToolkit QuickTime sample program.
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
package jp.nyatla.nyartoolkit.qt.sample;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.qt.utils.*;
import jp.nyatla.nyartoolkit.core.types.*;

import java.awt.*;
import java.awt.image.*;
/**
 * QuickTimeキャプチャプログラム
 *
 * On windows, You might get couldntGetRequiredComponent error.
 * If you got error try to install WinVDIG. 
 * http://www.eden.net.nz/7/20071008/
 * --
 * Windowsの場合、WinVDIGが無いとキャプチャプログラムが動かないことがあります。
 * couldntGetRequiredComponentエラーが出たら、WinVDIGをインストールしてみてね。
 * WinVIDGはとりあえずここから入手可能。http://www.eden.net.nz/7/20071008/
 * 
 *
 */
public class QtCaptureTest extends Frame implements QtCaptureListener
{
	private static final long serialVersionUID = -734697739607654631L;

	public QtCaptureTest() throws NyARException
	{
		setTitle("QtCaptureTest");
		setBounds(0, 0, 320 + 64, 240 + 64);
		capture = new QtCameraCapture(320, 240, 30f);
		capture.setCaptureListener(this);
		//キャプチャイメージ用のラスタを準備
		raster = new QtNyARRaster_RGB(320, 240);
	}

	private QtCameraCapture capture;
	private QtNyARRaster_RGB raster;
	
	public void onUpdateBuffer(byte[] pixels)
	{
		try{
			this.raster.wrapBuffer(pixels);
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
		//Imageに変換してみよう！

		
		NyARIntSize s=raster.getSize();
		WritableRaster wr = WritableRaster.createInterleavedRaster(DataBuffer.TYPE_BYTE, s.w,s.h, s.w * 3, 3, new int[] { 0, 1, 2 }, null);
		BufferedImage  bi = new BufferedImage(s.w, s.h, BufferedImage.TYPE_3BYTE_BGR);

		wr.setDataElements(0, 0, s.w, s.h,raster.getBuffer());
		bi.setData(wr);
		
		Graphics g = getGraphics();
		g.drawImage(bi, 32, 32, this);
	}

	private void startCapture()
	{
		try {
			capture.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		try {
			QtCaptureTest mainwin = new QtCaptureTest();
			mainwin.setVisible(true);
			mainwin.startCapture();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
