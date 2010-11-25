package jp.nyatla.nyartoolkit.dev.rpf.sample;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.media.format.VideoFormat;
import javax.media.opengl.GL;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.pickup.NyARColorPatt_O3;
import jp.nyatla.nyartoolkit.core.pickup.NyARColorPatt_Perspective;
import jp.nyatla.nyartoolkit.core.pickup.NyARColorPatt_Perspective_O2;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_RGB;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.NyARRasterFilter_Rgb2Gs_RgbAve;
import jp.nyatla.nyartoolkit.core.rasterreader.INyARRgbPixelReader;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk.NyARReality;
import jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk.NyARRealityTarget;
import jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk.NyARRealityTargetList;
import jp.nyatla.nyartoolkit.dev.rpf.realitysource.nyartk.NyARRealitySource;
import jp.nyatla.nyartoolkit.dev.rpf.realitysource.nyartk.NyARRealitySource_JavaImage;
import jp.nyatla.nyartoolkit.dev.rpf.realitysource.nyartk.NyARRealitySource_Jmf;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.*;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARTracker;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARContourTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARRectTargetStatus;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARTargetStatus;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.jogl.utils.NyARGLUtil;
import jp.nyatla.nyartoolkit.utils.j2se.*;





/**
 * @todo
 * 矩形の追跡は動いてるから、位置予測機能と組み合わせて試すこと。
 *
 */

public class Test_RealityTarget extends Frame implements MouseListener
{
	/**
	 * 出力ソース
	 * @author nyatla
	 */
	class InputSource
	{
		public NyARRealitySource reality_in;
	}

	class ImageSource extends InputSource
	{
		public ImageSource(String i_filename) throws NyARException, IOException
		{
			BufferedImage _src_image;
			_src_image = ImageIO.read(new File(i_filename));
			NyARRealitySource_JavaImage ri=new NyARRealitySource_JavaImage(_src_image.getWidth(),_src_image.getHeight(),2);
		//	ri.setImage(_src_image);
			this.reality_in=ri;
		}
	}

	class LiveSource extends InputSource implements JmfCaptureListener
	{
		private JmfCaptureDevice _capture;
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
			this._capture.start();
			this.reality_in=new NyARRealitySource_Jmf(this._capture.getCaptureFormat(),null,2,100);
			return;
			
		}
		
		public void onUpdateBuffer(javax.media.Buffer i_buffer)
		{
			try {
				//キャプチャしたバッファをラスタにセット
				synchronized(this.reality_in){
					((NyARRealitySource_Jmf)(this.reality_in)).setImage(i_buffer);
				}
				//キャプチャしたイメージを表示用に加工
			}catch(Exception e)
			{
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
	}
	
	public void mouseClicked(MouseEvent e)
	{
		try {
		int x=e.getX()-this.getInsets().left;
		int y=e.getY()-this.getInsets().top;
		System.out.println(x+":"+y);
		synchronized(this._input_source.reality_in)
		{
			NyARBufferedImageRaster bmp= new NyARBufferedImageRaster(64,64,NyARBufferType.BYTE1D_R8G8B8_24);
			for(int i=this._reality.refTargetList().getLength()-1;i>=0;i--)
			{
				NyARRealityTarget rt=this._reality.refTargetList().getItem(i);
				if(rt._target_type!=NyARRealityTarget.RT_UNKNOWN && rt._target_type!=NyARRealityTarget.RT_KNOWN){
					continue;
				}
				if(rt.isInnerVertexPoint2d(x, y))
				{
					if(e.getButton()==MouseEvent.BUTTON1){
						//左ボタンはUNKNOWN→KNOWN
						if(rt._target_type==NyARRealityTarget.RT_UNKNOWN){
								this._reality.changeTargetToKnown(rt,0,80);
								//イメージピックアップの実験
//								this._input_source.reality_in.getRgbPerspectivePatt(rt.refTargetVertex(),1,25,25,bmp);
								NyARIntPoint2d[] iv=NyARIntPoint2d.createArray(4);
								for(int i2=0;i2<4;i2++){
									iv[i2].x=(int)rt.refTargetVertex()[i2].x;
									iv[i2].y=(int)rt.refTargetVertex()[i2].y;
								}
								System.out.println(">>");
								NyARRgbRaster rgb=new NyARRgbRaster(320,240,NyARBufferType.BYTE1D_R8G8B8_24);
								//2d座標系
								{
									this._reality.getRgbPatt2d(this._input_source.reality_in,rt.refTargetVertex(),1,bmp);
									this.getGraphics().drawImage(bmp.getBufferedImage(),this.getInsets().left,this.getInsets().top+240,null);
								}
								//3d（カメラ）
								{
									NyARDoublePoint3d[] p=NyARDoublePoint3d.createArray(4);
									p[0].x=-40;p[0].y=-40;p[0].z=0;
									p[1].x=40; p[1].y=-40;p[1].z=0;
									p[2].x=40; p[2].y=40 ;p[2].z=0;
									p[3].x=-40;p[3].y=40 ;p[3].z=0;
									this._reality.getRgbPatt3d(this._input_source.reality_in,p, rt.refTransformMatrix(), 1, bmp);
									this.getGraphics().drawImage(bmp.getBufferedImage(),this.getInsets().left+64,this.getInsets().top+240,null);
								}
								//3d（Target）
								{
									NyARDoublePoint3d[] p=NyARDoublePoint3d.createArray(4);
									p[0].x=-40;p[0].y=-40;p[0].z=0;
									p[1].x=40; p[1].y=-40;p[1].z=0;
									p[2].x=40; p[2].y=40 ;p[2].z=0;
									p[3].x=-40;p[3].y=40 ;p[3].z=0;
									rt.getRgbPatt3d(this._input_source.reality_in,p,null, 1, bmp);
									this.getGraphics().drawImage(bmp.getBufferedImage(),this.getInsets().left+128,this.getInsets().top+240,null);
								}
								//3d（Target）
								{
									rt.getRgbRectPatt3d(this._input_source.reality_in,-80,-80,80,80,1, bmp);
									this.getGraphics().drawImage(bmp.getBufferedImage(),this.getInsets().left+192,this.getInsets().top+240,null);
								}
							break;
						}
					}else if(e.getButton()==MouseEvent.BUTTON3){
						//右ボタンはUNKNOWN　or KNOWN to dead
						try {
							this._reality.changeTargetToDead(rt);
							break;
						} catch (NyARException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		}
		} catch (NyARException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	
	
	
	private NyARReality _reality;
//	private NyARRealitySnapshot _reality_snapshot;
	
	
	NyARParam _param;
	
	private final static String SAMPLE_FILES = "../Data/320x240ABGR.png";
	private final static String PARAM_FILE = "../Data/camera_para.dat";

	private static final long serialVersionUID = -2110888320986446576L;


	private int W = 320;
	private int H = 240;
	InputSource _input_source;
	public Test_RealityTarget() throws NyARException, Exception
	{
		setTitle("NyARReality test");
		Insets ins = this.getInsets();
		this.setSize(1024 + ins.left + ins.right, 768 + ins.top + ins.bottom);
		this._param=new NyARParam();
		this._param.loadARParamFromFile(PARAM_FILE);
		this._param.changeScreenSize(W,H);
		this._reality=new NyARReality(this._param.getScreenSize(),10,1000,this._param.getPerspectiveProjectionMatrix(),null,10,10);
		this._input_source=new LiveSource();
		addMouseListener(this);

		return;
	}	

    public void update()
    {
		try {
			// マーカーを検出
			Thread.sleep(30);
			synchronized(this._input_source.reality_in){
//				Date d2 = new Date();
//				for (int i = 0; i < 1000; i++) {
					this._reality.progress(this._input_source.reality_in);			
//				}
//				Date d = new Date();
//				System.out.println(d.getTime() - d2.getTime());
			}
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
    	NyARRasterImageIO.copy(this._input_source.reality_in.refRgbSource(),bmp);
    	
    	//Ignore,Coord,New

    	//表示
    	g.setColor(Color.black);
    	g.drawString("Unknown:"+this._reality.getNumberOfUnknown(),200,200);
    	g.drawString("Known:"+this._reality.getNumberOfKnown(),200,210);
    	g.drawString("Dead:"+this._reality.getNumberOfDead(),200,220);
		NyARRealityTargetList tl=this._reality.refTargetList();
		for(int i=tl.getLength()-1;i>=0;i--){
			NyARRealityTarget t=tl.getItem(i);
			switch(t.getTargetType())
			{
			case NyARRealityTarget.RT_KNOWN:
				drawKnownRT(g,t);
				break;
			case NyARRealityTarget.RT_UNKNOWN:				
				drawUnKnownRT(g,t);
				break;
			default:
				drawDeadRT(g,t);
				break;
			}
		}    	
    	ig.drawImage(bmp,ins.left,ins.top,null);
    	

    	drawImage(ig,ins.left+320,ins.top,this._input_source.reality_in.refLastTrackSource().refEdgeRaster());
    	//

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
    private void drawKnownRT(Graphics g,NyARRealityTarget t)
    {
    	g.setColor(Color.GREEN);
    	NyARIntPoint2d b=new NyARIntPoint2d();
    	t.getTargetCenter(b);
    	NyARIntRect r=t._ref_tracktarget._sample_area;
    	g.drawString("[K]("+t.grab_rate+")",b.x,b.y);
		g.drawRect(r.x,r.y, r.w,r.h);
    	if(t._ref_tracktarget._st_type==NyARTargetStatus.ST_RECT){
        	g.drawString(">"+((NyARRectTargetStatus)(t._ref_tracktarget._ref_status)).detect_type,r.x,r.y+10);
    	}else{
     	}
    }
    private void drawUnKnownRT(Graphics g,NyARRealityTarget t)
    {
    	g.setColor(Color.YELLOW);
    	NyARIntPoint2d b=new NyARIntPoint2d();
    	t.getTargetCenter(b);
    	NyARIntRect r=t._ref_tracktarget._sample_area;
    	g.drawString("[U]("+t.grab_rate+")",b.x,b.y);
		g.drawRect(r.x,r.y, r.w,r.h);
	}
    private void drawDeadRT(Graphics g,NyARRealityTarget t)
    {
    	g.setColor(Color.RED);
    	NyARIntRect r=t._ref_tracktarget._sample_area;
    	g.drawString("[D]("+t.grab_rate+")",r.x,r.y);
    }
    
    


    public void mainloop() throws Exception
    {
    	for(;;){
	    	//処理
	    	this.update();
	    	this.draw(this.getGraphics());
    	}
    }

	public static void main(String[] args)
	{

		try {
			Test_RealityTarget mainwin = new Test_RealityTarget();
			mainwin.setVisible(true);
			mainwin.mainloop();
			// mainwin.startImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

