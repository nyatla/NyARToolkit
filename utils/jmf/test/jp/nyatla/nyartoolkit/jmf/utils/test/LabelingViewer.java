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
package jp.nyatla.nyartoolkit.jmf.utils.test;

import javax.media.*;

import javax.media.util.BufferToImage;
import javax.media.format.*;

import jp.nyatla.nyartoolkit.jmf.utils.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfoPtrStack;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.INyARRgb2GsFilter;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector_Rle;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * VFM+ARToolkitテストプログラム
 * カメラから取り込んだデータからマーカーを検出して、一致度と変換行列を表示します。
 */
public class LabelingViewer extends Frame implements JmfCaptureListener
{
	class SquareDetector extends NyARSquareContourDetector_Rle implements NyARSquareContourDetector.CbHandler
	{
		public SquareDetector(NyARIntSize i_size) throws NyARException
		{
			super(i_size);
		}
		public void detectMarkerCallback(NyARIntCoordinates i_coord,int[] i_vertex_index)  throws NyARException
		{
			
		}
		public NyARRleLabelFragmentInfoPtrStack _probe(){
			return this._labeling.label_stack;
		}
	}
	private static final long serialVersionUID = 6471434231970804953L;

	private final String PARAM_FILE = "../../Data/camera_para.dat";

	private JmfCaptureDevice _capture;


	private JmfNyARRGBRaster _raster;

	public LabelingViewer() throws NyARException,Exception
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
		NyARParam ar_param = NyARParam.createFromARParamFile(new FileInputStream(PARAM_FILE));
		ar_param.changeScreenSize(320, 240);
		this._raster = new JmfNyARRGBRaster(this._capture.getCaptureFormat());
		this._detect=new SquareDetector(ar_param.getScreenSize());
		this._filter= (INyARRgb2GsFilter) this._raster.createInterface(INyARRgb2GsFilter.class);
		//キャプチャイメージ用のラスタを準備
		return;
	}
	private SquareDetector _detect;
	private NyARGrayscaleRaster _bi=new NyARGrayscaleRaster(320,240);
	private INyARRgb2GsFilter _filter;


	
	public void onUpdateBuffer(Buffer i_buffer)
	{
		try {
			NyARGrayscaleRaster gs = this._bi;
			//キャプチャしたバッファをラスタにセット
			this._raster.setBuffer(i_buffer);

			//キャプチャしたイメージを表示用に加工
			BufferToImage b2i = new BufferToImage((VideoFormat) i_buffer.getFormat());
			Image img = b2i.createImage(i_buffer);
			this._filter.convert(gs);

			Graphics g = getGraphics();
			
			NyARParam param=NyARParam.createFromARParamFile(new FileInputStream(PARAM_FILE));
			param.changeScreenSize(320,240);
			try{
				NyARIntRect rect=new NyARIntRect();
//				rect.x=0;rect.y=0;rect.w=220;rect.h=140;
				this._detect.detectMarker(gs,110,this._detect);
			}catch(Exception e){
				e.printStackTrace();
			}
			NyARRleLabelFragmentInfoPtrStack ls=(NyARRleLabelFragmentInfoPtrStack)this._detect._probe();
			for(int i=0;i<ls.getLength();i++){
				NyARRleLabelFragmentInfo label=ls.getItem(i);
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
