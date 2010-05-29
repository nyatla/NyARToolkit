/* このソースは実験用のソースです。
 * 動いたり動かなかったりします。
 * 
 */
package jp.nyatla.nyartoolkit.sandbox.qrcode;

import javax.media.*;

import javax.media.util.BufferToImage;
import javax.media.format.*;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.utils.j2se.LabelingBufferdImage;


import java.awt.*;

import jp.nyatla.nyartoolkit.core.analyzer.raster.threshold.INyARRasterThresholdAnalyzer;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingImage;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabeling_ARToolKit;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasteranalyzer.threshold.NyARRasterThresholdAnalyzer_DiffHistogram;
import jp.nyatla.nyartoolkit.core.rasterfilter.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.gs2bin.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareStack;
import jp.nyatla.nyartoolkit.core.types.*;



public class SingleQrTest extends Frame implements JmfCaptureListener
{
	private final String camera_file = "../../Data/camera_para.dat";

	private JmfNyARRaster_RGB _raster;

	private JmfCaptureDevice capture;
	private NyARParam ap;
	public SingleQrTest() throws NyARException, NyARException
	{
		setBounds(0, 0, 640 + 64, 720 + 64);
		// キャプチャの準備
		JmfCaptureDeviceList list=new JmfCaptureDeviceList();
		capture=list.getDevice(0);
		capture.setCaptureFormat(320,240,30.0f);
		capture.setOnCapture(this);

		// キャプチャイメージ用のラスタを準備
		this._raster = new JmfNyARRaster_RGB(320, 240,capture.getCaptureFormat());
		
		// AR用カメラパラメタファイルをロード
		ap = new NyARParam();
		ap.loadARParamFromFile(camera_file);
		ap.changeScreenSize(320, 240);		
		
		
	}

	private NyARBinRaster _binraster1 = new NyARBinRaster(320, 240);

	private NyARGrayscaleRaster _gsraster1 = new NyARGrayscaleRaster(320, 240);
	private INyARRasterThresholdAnalyzer _tha=new NyARRasterThresholdAnalyzer_DiffHistogram();

	private LabelingBufferdImage _bimg = new LabelingBufferdImage(320, 240);


	public void onUpdateBuffer(Buffer i_buffer)
	{

		try {
			// キャプチャしたバッファをラスタにセット
			_raster.setBuffer(i_buffer);

			Graphics g = getGraphics();
			// キャプチャ画像
			BufferToImage b2i = new BufferToImage((VideoFormat) i_buffer.getFormat());
			Image img = b2i.createImage(i_buffer);
			this.getGraphics().drawImage(img, 32, 32, this);

			// 画像1
			INyARRasterFilter_Rgb2Gs filter_rgb2gs = new NyARRasterFilter_AveAdd();
//			INyARRasterFilter_RgbToGs filter_rgb2gs = new NyARRasterFilter_RgbMul();
			
			filter_rgb2gs.doFilter(_raster, _gsraster1);
			this._bimg.drawImage(this._gsraster1);
			this.getGraphics().drawImage(this._bimg, 32 + 320, 32, 320 + 320 + 32, 240 + 32, 0, 240, 320, 0, this);
			_tha.analyzeRaster(_gsraster1);
			NyARRasterFilter_ConstantThreshold gs2bin=new NyARRasterFilter_ConstantThreshold(_tha.getThreshold());
			

			// 画像2
			gs2bin.doFilter(_gsraster1, _binraster1);
			this._bimg.drawImage(_binraster1);
			this.getGraphics().drawImage(this._bimg, 32, 32 + 240, 320 + 32, 240 + 32 + 240, 0, 240, 320, 0, this);

			// 画像3
			NyARLabelingImage limage = new NyARLabelingImage(320, 240);
			NyARLabeling_ARToolKit labeling = new NyARLabeling_ARToolKit();
			labeling.labeling(_binraster1,limage);
			this._bimg.drawImage(this._gsraster1);

			NyARSquareStack stack = new NyARSquareStack(100);
			NyARQrCodeDetector detect = new NyARQrCodeDetector(ap.getDistortionFactor(), new NyARIntSize(320,240));
//			detect.bimg=this._bimg;

			detect.detectMarker(_binraster1, stack);
			for (int i = 0; i < stack.getLength(); i++) {
				NyARSquare[] square_ptr = stack.getArray();
				int d=square_ptr[i].direction;
				int[] xp=new int[4]; 
				int[] yp=new int[4]; 
				for(int i2=0;i2<4;i2++){
					xp[i2]=square_ptr[i].imvertex[(i2+d)%4].x;
					yp[i2]=square_ptr[i].imvertex[(i2+d)%4].y;
				}
				Graphics g2=this._bimg.getGraphics();
				g2.setColor(Color.RED);
				g2.drawPolygon(xp, yp,3);
				g2.setColor(Color.CYAN);
				g2.drawRect(square_ptr[i].imvertex[d].x, square_ptr[i].imvertex[d].y,5,5);				
			}
			this.getGraphics().drawImage(this._bimg, 32 + 320, 32 + 240, 320 + 32 + 320, 240 + 32 + 240, 0, 240, 320, 0, this);

			// 画像3
			// threshold.debugDrawHistogramMap(_workraster, _workraster2);
			// this._bimg2.setImage(this._workraster2);
			// this.getGraphics().drawImage(this._bimg2, 32+320, 32+240,320+32+320,240+32+240,0,240,320,0, this);

			// 画像4
			// NyARRasterThresholdAnalyzer_SlidePTile threshold=new NyARRasterThresholdAnalyzer_SlidePTile(15);
			// threshold.analyzeRaster(_gsraster1);
			// filter_gs2bin=new NyARRasterFilter_AreaAverage();
			// filter_gs2bin.doFilter(_gsraster1, _binraster1);
			// this._bimg.drawImage(_binraster1);

			// NyARRasterDetector_QrCodeEdge detector=new NyARRasterDetector_QrCodeEdge(10000);
			// detector.analyzeRaster(_binraster1);

			// this._bimg.overlayData(detector.geResult());

			// this.getGraphics().drawImage(this._bimg, 32, 32+480,320+32,480+32+240,0,240,320,0, this);
			// 画像5

			/*
			 * threshold2.debugDrawHistogramMap(_workraster, _workraster2); this._bimg2.drawImage(this._workraster2); this.getGraphics().drawImage(this._bimg2,
			 * 32+320, 32+480,320+32+320,480+32+240,0,240,320,0, this);
			 */

			// this.getGraphics().drawImage(this._bimg, 32, 32, this);

		} catch (Exception e) {
			e.printStackTrace();
		}

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
			SingleQrTest mainwin = new SingleQrTest();
			mainwin.setVisible(true);
			mainwin.startCapture();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
