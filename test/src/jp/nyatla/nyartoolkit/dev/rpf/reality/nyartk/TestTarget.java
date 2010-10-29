package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_RGB;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.NyARRasterFilter_Rgb2Gs_RgbAve;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSampler;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerIn;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARTracker;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARTrackerSnapshot;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARContourTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARRectTargetStatus;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureDevice;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureDeviceList;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureListener;
import jp.nyatla.nyartoolkit.jmf.utils.JmfNyARRaster_RGB;
import jp.nyatla.nyartoolkit.utils.j2se.NyARRasterImageIO;

/**
 * 出力ソース
 * @author nyatla
 */
interface InputSource
{
	public void UpdateInput(LowResolutionLabelingSamplerIn o_input) throws NyARException;
}

class ImageSource implements InputSource
{
	private BufferedImage _src_image;

	public ImageSource(String i_filename) throws IOException
	{
		this._src_image = ImageIO.read(new File(i_filename));
	}
	public void UpdateInput(LowResolutionLabelingSamplerIn o_input) throws NyARException
	{
		INyARRgbRaster ra =new NyARRgbRaster_RGB(320,240);
		NyARRasterImageIO.copy(this._src_image,ra);
		//GS値化
		NyARGrayscaleRaster gs=new NyARGrayscaleRaster(320,240);
		NyARRasterFilter_Rgb2Gs_RgbAve filter=new NyARRasterFilter_Rgb2Gs_RgbAve(ra.getBufferType());
		filter.doFilter(ra,gs);
		//samplerへ入力
		o_input.wrapBuffer(gs);
		
	}
}
class MoveSource implements InputSource
{
	private BufferedImage _src_image=new BufferedImage(320,240,BufferedImage.TYPE_INT_RGB);
	private int sx,sy,x,y;
	private int sx2,sy2,x2,y2;

	public MoveSource()
	{
		sx=1;sy=1;x=10;y=10;
		sx2=-2;sy2=1;x2=100;y2=10;
	}
	public void UpdateInput(LowResolutionLabelingSamplerIn o_input) throws NyARException
	{
        Graphics s=_src_image.getGraphics();
        s.setColor(Color.white);
        s.fillRect(0,0,320,240);
        s.setColor(Color.black);
        //s.fillRect(x, y,50,50);
        s.fillRect(x2, y2,50,50);
        x+=sx;y+=sy;
        if(x<0 || x>200){sx*=-1;}if(y<0 || y>200){sy*=-1;}
        x2+=sx2;y2+=sy2;
        if(x2<0 || x2>200){sx2*=-1;}if(y2<0 || y2>200){sy2*=-1;}
        INyARRgbRaster ra =new NyARRgbRaster_RGB(320,240);
        NyARRasterImageIO.copy(_src_image, ra);
		//GS値化
		NyARGrayscaleRaster gs=new NyARGrayscaleRaster(320,240);
		NyARRasterFilter_Rgb2Gs_RgbAve filter=new NyARRasterFilter_Rgb2Gs_RgbAve(ra.getBufferType());
		filter.doFilter(ra,gs);
		//samplerへ入力
		o_input.wrapBuffer(gs);
		
	}
}

class LiveSource implements InputSource,JmfCaptureListener
{
	public LiveSource() throws NyARException
	{
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
		this._raster = new JmfNyARRaster_RGB(320, 240,this._capture.getCaptureFormat());
		this._filter	= new NyARRasterFilter_Rgb2Gs_RgbAve(_raster.getBufferType());
		this._capture.start();
		
		return;
		
	}
	public void UpdateInput(LowResolutionLabelingSamplerIn o_input) throws NyARException
	{
		synchronized(this._raster){
			this._filter.doFilter(this._raster,this._bi);
		}
		o_input.wrapBuffer(this._bi);
	}
	private JmfCaptureDevice _capture;
	private JmfNyARRaster_RGB _raster;
	private NyARGrayscaleRaster _bi=new NyARGrayscaleRaster(320,240);
	private NyARRasterFilter_Rgb2Gs_RgbAve _filter;
	
	public void onUpdateBuffer(javax.media.Buffer i_buffer)
	{
		try {
			//キャプチャしたバッファをラスタにセット
			synchronized(this._raster){
				this._raster.setBuffer(i_buffer);
			}
			//キャプチャしたイメージを表示用に加工
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
}



/**
 * @todo
 * 矩形の追跡は動いてるから、位置予測機能と組み合わせて試すこと。
 *
 */

public class TestTarget extends Frame
{
	NyARReality _reality;
	LowResolutionLabelingSampler sampler;
	LowResolutionLabelingSamplerIn samplerin;
	LowResolutionLabelingSamplerOut samplerout;
	
	NyARTracker tracker;
	NyARTrackerSnapshot trackerout;
	
	private final static String SAMPLE_FILES = "../Data/320x240ABGR.png";

	private static final long serialVersionUID = -2110888320986446576L;


	private int W = 320;
	private int H = 240;
//	BufferedImage _src_image;
	InputSource _input_source;
	public TestTarget() throws NyARException, Exception
	{
		setTitle("Reality Platform test");
		Insets ins = this.getInsets();
		this.setSize(1024 + ins.left + ins.right, 768 + ins.top + ins.bottom);
		
//	this._input_source=new ImageSource(SAMPLE_FILES);
//		this._input_source=new MoveSource();
		this._input_source=new LiveSource();
		//create sampler
		this.samplerin=new LowResolutionLabelingSamplerIn(W, H, 2);
		this.samplerout=new LowResolutionLabelingSamplerOut(100);
		this.sampler=new LowResolutionLabelingSampler(W, H,2);
		
		//create tracker
		this.tracker=new NyARTracker();
		this.trackerout=new NyARTrackerSnapshot();

		return;
	}



	
	
	public void draw(INyARRgbRaster i_raster)
	{
		
	}
	static long tick;
    public void update()
    {
		try {
			// マーカーを検出
			this._input_source.UpdateInput(this.samplerin);
			Date d2 = new Date();
/*			for (int i = 0; i < 1; i++) {
				//tracker更新
				this.sampler.sampling(this.samplerin,this.samplerout);
				this.tracker.progress(this.samplerout,this.trackerout);
			}*/
			Date d = new Date();
			System.out.println(d.getTime() - d2.getTime());

			Thread.sleep(30);
			
			this.sampler.sampling(this.samplerin,this.samplerout);
			//tracker更新
			this.tracker.progress(this.samplerout,this.trackerout);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    BufferedImage _tmp_bf=new BufferedImage(320,240,BufferedImage.TYPE_INT_RGB);
    private void draw(Graphics ig) throws NyARException
    {
    	//ウインドウの情報
		Insets ins = this.getInsets();

    	//ワーク画面
    	BufferedImage bmp=this._tmp_bf;
    	Graphics g=bmp.getGraphics();
    	NyARRasterImageIO.copy(this.samplerout.ref_base_raster,bmp);
    	//Ignore,Coord,New

    	//表示
    	ig.drawImage(bmp,ins.left,ins.top,null);
    	drawImage(ig,ins.left+320,ins.top,this.samplerin._rbraster);
    }
    private void drawImage(Graphics g,int x,int y,NyARGrayscaleRaster r) throws NyARException
    {
        BufferedImage _tmp_bf=new BufferedImage(r.getWidth(),r.getHeight(),BufferedImage.TYPE_INT_RGB);
    	NyARRasterImageIO.copy(r, _tmp_bf);
    	g.drawImage(_tmp_bf, x,y, null);
    }
    //
    //描画関数
    //

    
    


    public void mainloop() throws Exception
    {
    	for(;;){
	    	//処理
	    	this.update();
			this.draw(this.getGraphics());
//	    	Thread.sleep(30);
    	}
    }

	public static void main(String[] args)
	{

		try {
			TestTarget mainwin = new TestTarget();
			mainwin.setVisible(true);
			mainwin.mainloop();
			// mainwin.startImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}