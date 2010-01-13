/* このソースは実験用のソースです。
 * 動いたり動かなかったりします。
 * 
 */
package jp.nyatla.nyartoolkit.sandbox.x2;

import javax.media.*;

import javax.media.util.BufferToImage;
import javax.media.format.*;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.sandbox.quadx2.*;
import jp.nyatla.nyartoolkit.utils.j2se.LabelingBufferdImage;

import jp.nyatla.nyartoolkit.core.*;

import java.awt.*;

import jp.nyatla.nyartoolkit.core.analyzer.raster.threshold.INyARRasterThresholdAnalyzer;
import jp.nyatla.nyartoolkit.core.labeling.*;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingImage;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabeling_ARToolKit;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasteranalyzer.threshold.NyARRasterThresholdAnalyzer_DiffHistogram;
import jp.nyatla.nyartoolkit.core.rasterfilter.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.INyARRasterFilter_Rgb2Gs;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareStack;
import jp.nyatla.nyartoolkit.core2.rasterfilter.rgb2gs.*;
import jp.nyatla.nyartoolkit.core2.rasterfilter.gs2bin.*;
import jp.nyatla.nyartoolkit.core2.rasteranalyzer.threshold.*;



public class VisualTest extends Frame implements JmfCaptureListener
{
	private final String camera_file = "../../Data/camera_para.dat";

	private JmfNyARRaster_RGB _raster;

	private JmfCaptureDevice capture;
	private NyARParam ap;
	public VisualTest() throws NyARException, NyARException
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

	private NyARBinRaster _binraster1 = new NyARBinRaster(160,120);

	private NyARGrayscaleRaster _gsraster1 = new NyARGrayscaleRaster(320, 240);
	private INyARRasterThresholdAnalyzer _tha=new NyARRasterThresholdAnalyzer_DiffHistogram();

	private LabelingBufferdImage _bimg = new LabelingBufferdImage(320, 240);
	private LabelingBufferdImage _bimg2 = new LabelingBufferdImage(160, 120);
	private NyARRasterFilter_ARTTh_Quad _tobin_filter=new NyARRasterFilter_ARTTh_Quad(100);

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
			INyARRasterFilter_Rgb2Gs filter_rgb2gs = new NyARRasterFilter_RgbAve();
//			INyARRasterFilter_RgbToGs filter_rgb2gs = new NyARRasterFilter_RgbMul();
			
			filter_rgb2gs.doFilter(_raster, _gsraster1);
			this._bimg.drawImage(this._gsraster1);
			this.getGraphics().drawImage(this._bimg, 32 + 320, 32, 320 + 320 + 32, 240 + 32, 0, 240, 320, 0, this);
			_tha.analyzeRaster(_gsraster1);
			NyARRasterFilter_Threshold gs2bin=new NyARRasterFilter_Threshold(_tha.getThreshold());
			

			// 画像2
			_tobin_filter.doFilter(_raster, _binraster1);
			this._bimg2.drawImage(_binraster1);
			this.getGraphics().drawImage(this._bimg2, 32, 32 + 240, 320 + 32, 240 + 32 + 240, 0, 240, 320, 0, this);

			// 画像3
			NyARLabelingImage limage = new NyARLabelingImage(320, 240);
			NyARLabeling_ARToolKit labeling = new NyARLabeling_ARToolKit();
			labeling.labeling(_binraster1,limage);
			this._bimg.drawImage(this._gsraster1);

			NyARSquareStack stack = new NyARSquareStack(100);
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
			VisualTest mainwin = new VisualTest();
			mainwin.setVisible(true);
			mainwin.startCapture();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
