package jp.nyatla.nyartoolkit.dev;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;

import javax.media.Buffer;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import java.awt.image.*;
import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.nyidmarker.NyARIdMarkerPickup;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.pickup.*;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.*;
import jp.nyatla.utils.j2se.*;
import jp.nyatla.nyartoolkit.nyidmarker.*;

public class PattPickupTest extends Frame implements JmfCaptureListener
{
	private final String PARAM_FILE = "../Data/camera_para.dat";

	private static final long serialVersionUID = -2110888320986446576L;

	private JmfCaptureDevice _capture;

	private JmfNyARRaster_RGB _capraster;

	private NyARSquareDetector _detector;

	protected INyARRasterFilter_RgbToBin _tobin_filter;

	private NyARBinRaster _bin_raster;

	private NyARSquareStack _stack = new NyARSquareStack(100);

	public PattPickupTest() throws NyARException
	{
		setTitle("JmfCaptureTest");
		Insets ins = this.getInsets();
		this.setSize(640 + ins.left + ins.right, 480 + ins.top + ins.bottom);
		JmfCaptureDeviceList dl = new JmfCaptureDeviceList();
		this._capture = dl.getDevice(0);
		if (!this._capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_RGB, 320, 240, 30.0f)) {
			if (!this._capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_YUV, 320, 240, 30.0f)) {
				throw new NyARException("キャプチャフォーマットが見つかりません。");
			}
		}
		NyARParam ar_param = new NyARParam();
		ar_param.loadARParamFromFile(PARAM_FILE);
		ar_param.changeScreenSize(320, 240);
		this._capraster = new JmfNyARRaster_RGB(320, 240, this._capture.getCaptureFormat());
		this._capture.setOnCapture(this);
		this._detector = new NyARSquareDetector(ar_param.getDistortionFactor(), ar_param.getScreenSize());
		this._bin_raster = new NyARBinRaster(320, 240);
		this._tobin_filter = new NyARRasterFilter_ARToolkitThreshold(110);
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

	private INyARColorPatt _patt1 = new NyARColorPatt_O3(64, 64);

	private INyARColorPatt _patt2 = new NyARColorPatt_Perspective(100,100);

	private NyARIdMarkerPickup _patt3 = new NyARIdMarkerPickup();

	public void onUpdateBuffer(Buffer i_buffer)
	{
		try {
			Insets ins = this.getInsets();
			Graphics g = getGraphics();

			{// ピックアップ画像の表示
				// 矩形抽出
				this._capraster.setBuffer(i_buffer);
				this._tobin_filter.doFilter(this._capraster, this._bin_raster);
				this._detector.detectMarker(this._bin_raster, this._stack);

				int max_point = 0;
				NyARSquare t = null;
				// ど れ に し よ う か なー
				for (int i = this._stack.getLength() - 1; i >= 0; i--) {
					NyARSquare sq = (NyARSquare) this._stack.getItem(i);
					int wp = getSQPoint(sq);
					if (wp < max_point) {
						continue;
					}
					t = sq;
				}
				if (t != null) {

					BufferedImageSink sink = new BufferedImageSink(this._patt1.getWidth(), this._patt1.getHeight());
					BufferedImageSink sink2 = new BufferedImageSink(this._patt2.getWidth(), this._patt2.getHeight());
//					BufferedImageSink sink3 = new BufferedImageSink(this._patt3.getWidth(), this._patt3.getHeight());
					Graphics g1,g2,g3;
					{// ARToolkit
						// 一番それっぽいパターンを取得
						this._patt1.pickFromRaster(this._capraster, t);
						// パターンを書く
						sink.sinkFromRaster(this._patt1);
						g1=sink.getGraphics();
						g1.setColor(Color.red);
						g1.drawLine(this._patt1.getWidth()/2,0,this._patt1.getWidth()/2,this._patt1.getHeight());
						g1.drawLine(0,this._patt1.getHeight()/2,this._patt1.getWidth(),this._patt1.getHeight()/2);
					}
					{// 疑似アフィン変換
						// 一番それっぽいパターンを取得
						this._patt2.pickFromRaster(this._capraster, t);
						// パターンを書く
						sink2.sinkFromRaster(this._patt2);
						g2=sink2.getGraphics();
						g2.setColor(Color.red);
						g2.drawLine(this._patt1.getWidth()/2,0,this._patt1.getWidth()/2,this._patt1.getHeight());
						g2.drawLine(0,this._patt1.getHeight()/2,this._patt1.getWidth(),this._patt1.getHeight()/2);
					}
					{// IDマーカ
						NyARIdMarkerData data =new NyARIdMarkerData();
						NyARIdMarkerParam param =new NyARIdMarkerParam();
						
						// 一番それっぽいパターンを取得
						this._patt3.pickFromRaster(this._capraster, t,data,param);
						System.out.println("model="+data.model);
						System.out.println("domain="+data.ctrl_domain);
						System.out.println("maskl="+data.ctrl_mask);
						System.out.println("data= "+data.data[0]+","+data.data[1]+","+data.data[2]);
						// パターンを書く
/*						sink3.sinkFromRaster(this._patt3);
						g3=sink.getGraphics();
						g3.setColor(Color.red);
						g2.drawRect(10,10,10,10);
						g2.drawRect(80,10,10,10);
						g2.drawRect(10,80,10,10);
						g2.drawRect(80,80,10,10);*/
//						g2.drawLine(this._patt3.sv[0]-1,this._patt3.sv[1],this._patt3.sv[0]+1,this._patt3.sv[1]);
//						g2.drawLine(this._patt3.sv[0],this._patt3.sv[1]-1,this._patt3.sv[0],this._patt3.sv[1]+1);
//						g2.drawLine(this._patt3.sv[2]-1,this._patt3.sv[3],this._patt3.sv[2]+1,this._patt3.sv[3]);
//						g2.drawLine(this._patt3.sv[2],this._patt3.sv[3]-1,this._patt3.sv[2],this._patt3.sv[3]+1);
						
						BufferedImage img=new BufferedImage(45,256,	BufferedImage.TYPE_INT_RGB);

						g.drawImage(img, ins.left, ins.top+240,45,256, null);
/*
						g2.setColor(Color.blue);
						for (int i = 0; i < 225*4; i++) {
							g2.drawRect(this._patt3.vertex_x[i]-1,this._patt3.vertex_y[i]-1, 2, 2);
						}
*/						g2.setColor(Color.red);
						for (int i = 0; i <4; i++) {
							g2.drawRect(this._patt3.vertex2_x[i]-1,this._patt3.vertex2_y[i]-1, 2, 2);
						}
					}
					g.drawImage(sink, ins.left + 320, ins.top, 128, 128, null);
					g.drawImage(sink2, ins.left + 320, ins.top + 128, 400, 400, null);
//					g.drawImage(sink3, ins.left + 100, ins.top + 240, this._patt3.getWidth() * 10, this._patt3.getHeight() * 10, null);
				}

				{// 撮影画像
					BufferToImage b2i = new BufferToImage((VideoFormat) i_buffer.getFormat());
					Image img = b2i.createImage(i_buffer);
					g.drawImage(img, ins.left, ins.top, this);
					g.setColor(Color.blue);
					for (int i = 0; i < 225*4; i++) {
						g.drawRect(ins.left+this._patt3.vertex_x[i]-1,ins.top+this._patt3.vertex_y[i]-1, 2, 2);
					}
				
				}
				/*
				 * { //輪郭パターン NyARSquare s=new NyARSquare(); for(int i=0;i<4;i++){ s.imvertex[i].x=(int)t.sqvertex[i].x; s.imvertex[i].y=(int)t.sqvertex[i].y; }
				 * //一番それっぽいパターンを取得 this._patt1.pickFromRaster(this._capraster,s); //パターンを書く BufferedImageSink sink=new
				 * BufferedImageSink(this._patt1.getWidth(),this._patt1.getHeight()); sink.sinkFromRaster(this._patt1);
				 * g.drawImage(sink,ins.left+320,ins.top+128,128,128,null); }
				 */
				{// 信号取得テスト

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

	public static void main(String[] args)
	{
		try {
			PattPickupTest mainwin = new PattPickupTest();
			mainwin.setVisible(true);
			mainwin.startCapture();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
