package jp.nyatla.nyartoolkit.dev;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;

import javax.media.Buffer;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.detector.*;
import jp.nyatla.nyartoolkit.nyidmarker.NyIdMarkerPickup;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.pickup.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.gs2bin.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.NyARRasterFilterBuilder_RgbToBin;
import jp.nyatla.nyartoolkit.nyidmarker.*;
import jp.nyatla.nyartoolkit.utils.j2se.*;
import jp.nyatla.nyartoolkit.core.squaredetect.*;



public class PattPickupTest extends Frame implements JmfCaptureListener
{
	private final String PARAM_FILE = "../Data/camera_para.dat";

	private final static String CARCODE_FILE = "../Data/patt.hiro";

	private static final long serialVersionUID = -2110888320986446576L;

	private JmfCaptureDevice _capture;

	private JmfNyARRaster_RGB _capraster;

	private int W = 320;

	private int H = 240;

	private NyARParam _param;

	private NyARBinRaster _bin_raster;

	private NyARSquareStack _stack = new NyARSquareStack(100);

	private NyARSingleDetectMarker detect;

	public PattPickupTest() throws NyARException
	{
		setTitle("JmfCaptureTest");
		Insets ins = this.getInsets();
		this.setSize(640 + ins.left + ins.right, 480 + ins.top + ins.bottom);
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
		this._capraster = new JmfNyARRaster_RGB(W, H, this._capture.getCaptureFormat());
		this.detect = new NyARSingleDetectMarker(ar_param, code, 80, this._capraster.getBufferType());
		this._capture.setOnCapture(this);
		this._bin_raster = new NyARBinRaster(W, H);
		this._param = ar_param;
		return;
	}

	/**
	 * 矩形の矩形っぽい点数を返す。
	 * 
	 * @param i_sq
	 * @return
	 */
	private int getSQPoint(NyARSquare i_sq)
	{
		int lx1 = i_sq.imvertex[0].x - i_sq.imvertex[2].x;
		int ly1 = i_sq.imvertex[0].y - i_sq.imvertex[2].y;
		int lx2 = i_sq.imvertex[1].x - i_sq.imvertex[3].x;
		int ly2 = i_sq.imvertex[1].y - i_sq.imvertex[3].y;
		return (int) Math.sqrt((lx1 * lx1) + (ly1 * ly1)) * (int) Math.sqrt(((lx2 * lx2) + (ly2 * ly2)));
	}

	private final String data_file = "../Data/320x240ABGR.raw";

	private INyARColorPatt _patt1 = new NyARColorPatt_O3(16, 16);

	public void draw(INyARRgbRaster i_raster)
	{
		try {
			Insets ins = this.getInsets();
			Graphics g = getGraphics();

			{// ピックアップ画像の表示
				// 矩形抽出
				INyARRasterFilter_Bin to_binfilter = new NyARRasterFilterBuilder_RgbToBin(110, i_raster.getBufferType());
				to_binfilter.doFilter(i_raster, this._bin_raster);
				if (this.detect.detectMarkerLite(i_raster, 100)) {

					NyARTransMatResult res = new NyARTransMatResult();
					this.detect.getTransmationMatrix(res);
					int max_point = 0;

					// NyARSquare t=new NyARSquare();

						TransformedBitmapPickup patt2 = new TransformedBitmapPickup(this._param.getPerspectiveProjectionMatrix(), 100, 100, 1);

						BufferedImage sink = new BufferedImage(this._patt1.getWidth(), this._patt1.getHeight(), ColorSpace.TYPE_RGB);
						BufferedImage sink2 = new BufferedImage(patt2.getWidth(), patt2.getHeight(), ColorSpace.TYPE_RGB);
						patt2.pickupImage2d(i_raster,-20,-40,20,-80,res);
						/*
						 * t.imvertex[0].x=(int)483.0639377595418; t.imvertex[0].y=(int)303.17616747966747;
						 * 
						 * t.imvertex[1].x=(int)506.1019505415998; t.imvertex[1].y=(int)310.5313224526344;
						 * 
						 * t.imvertex[2].x=(int)589.3605435960492; t.imvertex[2].y=(int)258.46261716798523;
						 * 
						 * t.imvertex[3].x=(int)518.1385869954609; t.imvertex[3].y=(int)325.1434618295405;
						 */
						Graphics g1, g2, g3;
					/*	{// ARToolkit
							// 一番それっぽいパターンを取得
							this._patt1.pickFromRaster(i_raster, t.imvertex);
							Date d2 = new Date();
							for (int i = 0; i < 10000; i++) {
								this._patt1.pickFromRaster(i_raster, t.imvertex);
							}
							Date d = new Date();
							System.out.println(d.getTime() - d2.getTime());

							// パターンを書く
							NyARRasterImageIO.copy(this._patt1, sink);
							g1 = sink.getGraphics();
							g1.setColor(Color.red);
						}*/
						{// 疑似アフィン変換
							NyARRasterImageIO.copy(patt2, sink2);
							g2 = sink2.getGraphics();
							g2.setColor(Color.red);

						}
						g.drawImage(sink, ins.left + 320, ins.top, 128, 128, null);
						g.drawImage(sink2, ins.left + 320, ins.top + 128, 128, 128, null);
						// g.drawImage(sink3, ins.left + 100, ins.top + 240, this._patt3.getWidth() * 10, this._patt3.getHeight() * 10, null);
					
				}
				{// 撮影画像
					BufferedImage sink = new BufferedImage(i_raster.getWidth(), i_raster.getHeight(), ColorSpace.TYPE_RGB);
					NyARRasterImageIO.copy(i_raster, sink);
					g.drawImage(sink, ins.left, ins.top, this);
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
				this._capraster.setBuffer(i_buffer);
				draw(this._capraster);
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
			INyARRgbRaster ra = NyARRgbRaster_BGRA.wrap(buf, W, H);
			draw(ra);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args)
	{
		try {
			PattPickupTest mainwin = new PattPickupTest();
			mainwin.setVisible(true);
			mainwin.startCapture();
			// mainwin.startImage();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
