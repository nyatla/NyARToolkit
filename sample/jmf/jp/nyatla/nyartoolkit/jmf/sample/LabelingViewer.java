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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfoStack;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.NyARRasterFilter_ARToolkitThreshold;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector_Rle;

/**
 * VFM+ARToolkitテストプログラム
 * カメラから取り込んだデータからマーカーを検出して、一致度と変換行列を表示します。
 */
public class LabelingViewer extends Frame implements JmfCaptureListener
{
	private static final long serialVersionUID = 6471434231970804953L;

	private final String PARAM_FILE = "../../Data/camera_para.dat";

	private JmfCaptureDevice _capture;


	private JmfNyARRaster_RGB _raster;

	public LabelingViewer() throws NyARException
	{
		setTitle("JmfCaptureTest");
		setBounds(0, 0, 320 + 64, 240 + 64);
		//キャプチャの準備
		JmfCaptureDeviceList devlist=new JmfCaptureDeviceList();
		this._capture=devlist.getDevice(0);
		//JmfNyARRaster_RGBはYUVよりもRGBで高速に動作します。
		if(!this._capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_RGB,320, 240,15f)){
			if(!this._capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_YUV,320, 240,15f)){
				throw new NyARException("キャプチャフォーマットが見つかりません");
			}		
		}
		this._capture.setOnCapture(this);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
		//NyARToolkitの準備
		NyARParam ar_param = new NyARParam();
		ar_param.loadARParamFromFile(PARAM_FILE);
		ar_param.changeScreenSize(320, 240);
		this._raster = new JmfNyARRaster_RGB(320, 240,this._capture.getCaptureFormat());
		this._detect=new NyARSquareContourDetector_Rle(ar_param.getScreenSize());
		this._filter	= new NyARRasterFilter_ARToolkitThreshold(110, _raster.getBufferType());
		//キャプチャイメージ用のラスタを準備
		return;
	}
	private NyARSquareContourDetector_Rle _detect;
	private NyARBinRaster _bi=new NyARBinRaster(320,240);
	private NyARRasterFilter_ARToolkitThreshold _filter;
	/**
	 * detectMarkerのコールバック関数
	 */
	private class DetectSquareCB implements NyARSquareContourDetector.IDetectMarkerCallback
	{
		public DetectSquareCB()
		{
			return;
		}
		/**
		 * 矩形が見付かるたびに呼び出されます。
		 * 発見した矩形のパターンを検査して、方位を考慮した頂点データを確保します。
		 */
		public void onSquareDetect(NyARSquareContourDetector i_sender,int[] i_coordx,int[] i_coordy,int i_coor_num,int[] i_vertex_index) throws NyARException
		{			
		}
	}
	
	public void onUpdateBuffer(Buffer i_buffer)
	{
		try {
			NyARBinRaster bin = this._bi;
			//キャプチャしたバッファをラスタにセット
			this._raster.setBuffer(i_buffer);

			//キャプチャしたイメージを表示用に加工
			BufferToImage b2i = new BufferToImage((VideoFormat) i_buffer.getFormat());
			Image img = b2i.createImage(i_buffer);
			this._filter.doFilter(this._raster,bin);

			Graphics g = getGraphics();
			
			NyARParam param=new NyARParam();
			param.loadARParamFromFile(PARAM_FILE);
			param.changeScreenSize(320,240);
			try{
				this._detect.detectMarkerCB(bin,new DetectSquareCB());
			}catch(Exception e){
				e.printStackTrace();
			}
			NyARRleLabelFragmentInfoStack ls=this._detect._getFragmentStack();
			for(int i=0;i<ls.getLength();i++){
				NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo label=ls.getItem(i);
//				if(label.area==0){break;}
				Graphics g2=img.getGraphics();
				g2.setColor(Color.RED);
				g2.drawRect(label.clip_l,label.clip_t,label.clip_r-label.clip_l,label.clip_b-label.clip_t);
			}		
			
			
			g.drawImage(img,50,50,null);
		}catch(Exception e)
		{
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
			LabelingViewer mainwin = new LabelingViewer();
			mainwin.setVisible(true);
			mainwin.startCapture();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
