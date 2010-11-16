package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARLabeling_Rle;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfoPtrStack;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.NyARRasterFilter_Rgb2Gs_RgbAve;
import jp.nyatla.nyartoolkit.utils.j2se.NyARBufferedImageRaster;
import jp.nyatla.nyartoolkit.utils.j2se.NyARRasterImageIO;
import jp.nyatla.nyartoolkit.core.types.*;


import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_RGB;
//import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcHolder.AreaSrcItem;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.*;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARContourTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARRectTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARTargetStatus;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureDevice;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureDeviceList;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureListener;
import jp.nyatla.nyartoolkit.jmf.utils.JmfNyARRaster_RGB;






/**
 * @todo
 * 矩形の追跡は動いてるから、位置予測機能と組み合わせて試すこと。
 *
 */

public class TestTerget extends Frame
{
	/**
	 * 出力ソース
	 * @author nyatla
	 */
	interface InputSource
	{
		public void UpdateInput(NyARTrackerSource_Reference o_input) throws NyARException;
	}

	class ImageSource implements InputSource
	{
		private NyARBufferedImageRaster _src_image;
		NyARGrayscaleRaster gs;
		NyARRasterFilter_Rgb2Gs_RgbAve filter;

		public ImageSource(String i_filename) throws IOException, NyARException
		{
			this._src_image=new NyARBufferedImageRaster(ImageIO.read(new File(i_filename)));
			this.gs=new NyARGrayscaleRaster(this._src_image.getWidth(),this._src_image.getHeight());
			this.filter=new NyARRasterFilter_Rgb2Gs_RgbAve(this._src_image.getBufferType());
		}
		public void UpdateInput(NyARTrackerSource_Reference o_input) throws NyARException
		{
			//GS値化
			this.filter.doFilter(this._src_image,gs);
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
		public void UpdateInput(NyARTrackerSource_Reference o_input) throws NyARException
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
		public LiveSource(int W,int H) throws NyARException
		{
			//キャプチャの準備
			JmfCaptureDeviceList devlist=new JmfCaptureDeviceList();
			this._capture=devlist.getDevice(0);
			//JmfNyARRaster_RGBはYUVよりもRGBで高速に動作します。
			if(!this._capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_RGB,W, H,30f)){
				if(!this._capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_YUV,W,H,30f)){
					throw new NyARException("キャプチャフォーマットが見つかりません");
				}		
			}
			this._capture.setOnCapture(this);
			this._raster = new JmfNyARRaster_RGB(this._capture.getCaptureFormat());
			this._filter	= new NyARRasterFilter_Rgb2Gs_RgbAve(_raster.getBufferType());
			this._capture.start();
			_bi=new NyARGrayscaleRaster(W, H);
			return;
			
		}
		public void UpdateInput(NyARTrackerSource_Reference o_input) throws NyARException
		{
			synchronized(this._raster){
				this._filter.doFilter(this._raster,this._bi);
			}
			o_input.wrapBuffer(this._bi);
		}
		private JmfCaptureDevice _capture;
		private JmfNyARRaster_RGB _raster;
		private NyARGrayscaleRaster _bi;
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
	
	NyARTrackerSource_Reference tracksource;
	
	NyARTracker tracker;
	
	private final static String SAMPLE_FILES = "../Data/320x240ABGR.png";

	private static final long serialVersionUID = -2110888320986446576L;


	private int W = 320;
	private int H = 240;
	InputSource _input_source;
	public TestTerget(NyARCameraDistortionFactor p) throws NyARException, Exception
	{
		setTitle("Reality Platform test");
		Insets ins = this.getInsets();
		this.setSize(1024 + ins.left + ins.right, 768 + ins.top + ins.bottom);
		
//	this._input_source=new ImageSource(SAMPLE_FILES);
//		this._input_source=new MoveSource();
		this._input_source=new LiveSource(W,H);
		//create sampler
		this.tracksource=new NyARTrackerSource_Reference(100,p,W, H, 1,false);
		_tmp_bf=new BufferedImage(W, H,BufferedImage.TYPE_INT_RGB);
		
		//create tracker
		this.tracker=new NyARTracker(10,1,10);

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
			Date d2 = new Date();
			for (int i = 0; i < 1; i++) {
				//tracker更新
				this._input_source.UpdateInput(this.tracksource);
				this.tracker.progress(this.tracksource);
			}
			Date d = new Date();
			System.out.println(d.getTime() - d2.getTime());

			Thread.sleep(30);
			
			//tracker更新
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    BufferedImage _tmp_bf;
    private void draw(Graphics ig) throws NyARException
    {
    	//ウインドウの情報
		Insets ins = this.getInsets();

    	//ワーク画面
    	BufferedImage bmp=this._tmp_bf;
    	NyARRasterImageIO.copy(this.tracksource.getBaseRaster(),bmp);
    	//Ignore,Coord,New
    	for(int i=this.tracker._targets.getLength()-1;i>=0;i--){
    		switch(this.tracker._targets.getItem(i).st_type)
    		{
    		case NyARTargetStatus.ST_CONTURE:
            	drawContourTarget(this.tracker._targets.getItem(i),bmp,Color.blue);
    			break;
    		case NyARTargetStatus.ST_IGNORE:
            	drawIgnoreTarget(this.tracker._targets.getItem(i),bmp,Color.red);
    			break;
    		case NyARTargetStatus.ST_NEW:
            	drawNewTarget(this.tracker._targets.getItem(i),bmp,Color.green);
    			break;
    		case NyARTargetStatus.ST_RECT:
    			drawRectTarget(this.tracker._targets.getItem(i),bmp,Color.cyan);
    			break;
    		}
    	}
    	//表示
    	ig.drawImage(bmp,ins.left,ins.top,null);
    	drawImage(ig,ins.left+640,ins.top,this.tracksource.getEdgeRaster());
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
    /**
     * RectTargetを表示します。
     */
    private void drawRectTarget(NyARTarget t,BufferedImage sink,Color c)
    {
    	//サンプリング結果の表示
    	Graphics g=sink.getGraphics();
	    	g.setColor(c);
			NyARRectTargetStatus s=(NyARRectTargetStatus)t.ref_status;
			g.drawString("RT:"+t.serial+"("+s.detect_type+")"+"-"+t.delay_tick,t.sample_area.x,t.sample_area.y);
			g.drawRect((int)s.vertex[0].x-1,(int)s.vertex[0].y-1,2,2);
			for(int i2=0;i2<4;i2++){
//				g.fillRect((int)st.vecpos[i2].x-1, (int)st.vecpos[i2].y-1,2,2);
				g.drawLine(
					(int)s.vertex[i2].x,
					(int)s.vertex[i2].y,
					(int)s.vertex[(i2+1)%4].x,
					(int)s.vertex[(i2+1)%4].y);
			}/*
		   	g.setColor(Color.pink);
			for(int i2=0;i2<4;i2++){
				g.drawLine(
						(int)s.estimate_vertex[i2].x,
						(int)s.estimate_vertex[i2].y,
						(int)s.estimate_vertex[(i2+1)%4].x,
						(int)s.estimate_vertex[(i2+1)%4].y);
				}*/
    }

    /**
     * ContourTargetを表示します。
     */
    private void drawContourTarget(NyARTarget t,BufferedImage sink,Color c)
    {
    	//サンプリング結果の表示
    	Graphics g=sink.getGraphics();
    	g.setColor(c);
		g.drawString("CT",t.sample_area.x,t.sample_area.y);
		g.drawRect(t.sample_area.x,t.sample_area.y,t.sample_area.w,t.sample_area.h);
		NyARContourTargetStatus st=(NyARContourTargetStatus)t.ref_status;
		for(int i2=0;i2<st.vecpos.length;i2++){
			g.drawString(i2+":"+"-"+t.delay_tick,(int)st.vecpos.items[i2].x-1, (int)st.vecpos.items[i2].y-1);
			g.fillRect((int)st.vecpos.items[i2].x-1, (int)st.vecpos.items[i2].y-1,2,2);
		}
    }
    
    /**
     * IgnoreTargetを表示します。
     */
    private void drawIgnoreTarget(NyARTarget t,BufferedImage sink,Color c)
    {
    	//サンプリング結果の表示
    	Graphics g=sink.getGraphics();
    	g.setColor(c);
		g.drawString("IG"+"-"+t.delay_tick,t.sample_area.x,t.sample_area.y);
		g.drawRect(t.sample_area.x,t.sample_area.y,t.sample_area.w,t.sample_area.h);
    }
        
    /**
     * Newtargetを表示します。
     */
    private void drawNewTarget(NyARTarget t,BufferedImage sink,Color c)
    {
    	//サンプリング結果の表示
    	Graphics g=sink.getGraphics();
    	g.setColor(c);
		g.drawString("NW"+"-"+t.delay_tick,t.sample_area.x,t.sample_area.y);
		g.drawRect(t.sample_area.x,t.sample_area.y,t.sample_area.w,t.sample_area.h);
    }
    

    
    


    public void mainloop() throws Exception
    {
    	for(;;){
	    	//処理
	    	this.update();
			this.draw(this.getGraphics());
	    	Thread.sleep(30);
    	}
    }

	public static void main(String[] args)
	{

		try {
			TestTerget mainwin = new TestTerget(null);
			mainwin.setVisible(true);
			mainwin.mainloop();
			// mainwin.startImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
