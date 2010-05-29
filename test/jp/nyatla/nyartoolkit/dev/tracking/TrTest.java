package jp.nyatla.nyartoolkit.dev.tracking;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.media.Buffer;
import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.pickup.INyARColorPatt;
import jp.nyatla.nyartoolkit.core.pickup.NyARColorPatt_Perspective_O2;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector_Rle;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareStack;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureDevice;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureDeviceList;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.utils.j2se.NyARRasterImageIO;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * @todo
 * 矩形の追跡は動いてるから、位置予測機能と組み合わせて試すこと。
 *
 */
class Tracking extends MarkerTracking_3dTrans
{
	Tracking(NyARParam i_param,NyARCode i_code,double i_marker_width,int i_input_raster_type) throws NyARException
	{
		super();
		initInstance(
				new NyARColorPatt_Perspective_O2(i_code.getWidth(), i_code.getHeight(),4,25),
				new NyARSquareContourDetector_Rle(i_param.getScreenSize()),
				new NyARTransMat(i_param),
				new NyARRasterFilter_ARToolkitThreshold(120,i_input_raster_type),
				i_param,
				i_code,
				i_marker_width);
	}
}

public class TrTest extends Frame implements JmfCaptureListener,MouseMotionListener
{


	private final String PARAM_FILE = "../Data/camera_para.dat";

	private final static String CARCODE_FILE = "../Data/patt.hiro";

	private static final long serialVersionUID = -2110888320986446576L;

	private JmfCaptureDevice _capture;

	private JmfNyARRaster_RGB _capraster;

	private int W = 320;

	private int H = 240;

	private Tracking _tr;
	TransMat2MarkerRect _trm;

	public TrTest() throws NyARException
	{
		setTitle("JmfCaptureTest");
		Insets ins = this.getInsets();
		this.setSize(1024 + ins.left + ins.right, 768 + ins.top + ins.bottom);
		JmfCaptureDeviceList dl = new JmfCaptureDeviceList();
		this._capture = dl.getDevice(0);
		if (!this._capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_RGB, W, H, 30.0f)) {
			if (!this._capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_YUV, W, H, 30.0f)) {
				throw new NyARException("キャプチャフォーマットが見つかりません。");
			}
		}
		NyARParam ar_param = new NyARParam();
		ar_param.loadARParamFromFile(PARAM_FILE);
		ar_param.changeScreenSize(W, H);

		NyARCode code = new NyARCode(16, 16);
		code.loadARPattFromFile(CARCODE_FILE);
		this._capraster = new JmfNyARRaster_RGB(ar_param, this._capture.getCaptureFormat());
		this._capture.setOnCapture(this);

		addMouseMotionListener(this);
		this._tr=new Tracking(ar_param,code,80,this._capraster.getBufferType());
		this._trm=new TransMat2MarkerRect(ar_param);
		return;
	}
	int mouse_x;
	int mouse_y;
    public void mouseMoved(MouseEvent A00)
    {
        mouse_x = A00.getX();
        mouse_y = A00.getY();
    }

    public void mouseDragged(MouseEvent A00) {}



	private final String data_file = "../Data/320x240ABGR.raw";

	

	public void draw(JmfNyARRaster_RGB i_raster)
	{
		try {
			Insets ins = this.getInsets();
			Graphics g = getGraphics();
			this._tr.detectMarkerLite(i_raster);
			Object[] probe=this._tr._probe();
			MarkerPositionTable mpt=(MarkerPositionTable)probe[0];
			NextFrameMarkerStack fs=(NextFrameMarkerStack)probe[2];
			
			{// ピックアップ画像の表示
				// 矩形抽出
//				INyARRasterFilter_RgbToBin to_binfilter = NyARRasterFilterBuilder_ARToolkitThreshold.createFilter(110, i_raster.getBufferReader().getBufferType());
//				to_binfilter.doFilter(i_raster, this._bin_raster);
//				if (this.detect.detectMarkerLite(i_raster, 100)) {
//
//					NyARTransMatResult res = new NyARTransMatResult();
//					this.detect.getTransmationMatrix(res);
//				}
				{// 撮影画像
					
//					INyARRasterFilter_RgbToBin filter=new NyARRasterFilter_ARToolkitThreshold(110,i_raster.getBufferReader().getBufferType());
//					NyARSquareStack stack=new NyARSquareStack(10);
					
					BufferedImage sink = new BufferedImage(i_raster.getWidth(), i_raster.getHeight(), ColorSpace.TYPE_RGB);

					{//元画像
						NyARRasterImageIO.copy(i_raster, sink);
						Graphics g2=sink.getGraphics();
						g2.setColor(Color.RED);
						MarkerPositionTable.Item[] item=mpt.selectAllItems();
						for(int i=0;i<item.length;i++){
							if(item[i].is_empty){
								continue;
							}
							NyARIntRect re=new NyARIntRect();
							this._trm.convert(item[i], re);
							g2.drawRect(re.x,re.y,re.w,re.h);
							g2.drawString(Integer.toString(item[i].sirial),re.x,re.y);
						}
						g2.setColor(Color.CYAN);
						for(int i=0;i<fs.getLength();i++){
							g2.drawRect((int)(fs.getItem(i).vertex0.x-1),(int)(fs.getItem(i).vertex0.y-1),2,2);
						}
						g.drawImage(sink, ins.left, ins.top, this);
					}
				}

				{// 信号取得テスト

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onUpdateBuffer(Buffer i_buffer)
	{
		try {

			{// ピックアップ画像の表示
				// 矩形抽出
				synchronized(this._capraster){
					this._capraster.setBuffer(i_buffer);
					draw(this._capraster);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startCapture()
	{
		try {
			this._capture.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startImage()
	{
		try {
			// 試験イメージの読み出し(320x240 BGRAのRAWデータ)
			File f = new File(data_file);
			FileInputStream fs = new FileInputStream(data_file);
			byte[] buf = new byte[(int) f.length() * 4];
			fs.read(buf);
//			INyARRgbRaster ra = NyARRgbRaster_BGRA.wrap(buf, W, H);
//			draw(ra);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args)
	{

		try {
			TrTest mainwin = new TrTest();
			mainwin.setVisible(true);
			mainwin.startCapture();
			// mainwin.startImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
