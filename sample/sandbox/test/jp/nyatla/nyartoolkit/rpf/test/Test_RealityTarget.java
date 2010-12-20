package jp.nyatla.nyartoolkit.rpf.test;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.rpf.mklib.ARTKMarkerTable;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.NyARReality;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.NyARRealityTarget;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.NyARRealityTargetList;
import jp.nyatla.nyartoolkit.rpf.realitysource.nyartk.NyARRealitySource;
import jp.nyatla.nyartoolkit.rpf.realitysource.nyartk.NyARRealitySource_JavaImage;
import jp.nyatla.nyartoolkit.rpf.realitysource.nyartk.NyARRealitySource_Jmf;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status.NyARRectTargetStatus;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status.NyARTargetStatus;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.utils.j2se.*;





/**
 * NyARRealityのテストプログラム。動作保証なし。
 * 
 * ターゲットプロパティの取得実験用のテストコードです。
 * クリックしたマーカや、その平面周辺から、画像を取得するテストができます。
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
			NyARRealitySource_JavaImage ri=new NyARRealitySource_JavaImage(_src_image.getWidth(),_src_image.getHeight(),null,2,100);
			ri.getBufferedImage().getGraphics().drawImage(_src_image,0,0,null);
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
			NyARBufferedImageRaster bmp= new NyARBufferedImageRaster(100,100,NyARBufferType.BYTE1D_R8G8B8_24);
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
							ARTKMarkerTable.GetBestMatchTargetResult r=new ARTKMarkerTable.GetBestMatchTargetResult();
							if(this._mklib.getBestMatchTarget(rt,this._input_source.reality_in,r)){
								this._reality.changeTargetToKnown(rt,r.artk_direction,80);
							}
								//イメージピックアップの実験
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
									p[0].x=-60;p[0].y=40 ;p[0].z=0;
									p[1].x=40; p[1].y=40 ;p[1].z=0;
									p[2].x=40; p[2].y=-40;p[2].z=0;
									p[3].x=-60;p[3].y=-40;p[3].z=0;
									this._reality.getRgbPatt3d(this._input_source.reality_in,p, rt.refTransformMatrix(), 1, bmp);
									this.getGraphics().drawImage(bmp.getBufferedImage(),this.getInsets().left+100,this.getInsets().top+240,null);
								}
								//3d（Target）
								{
									NyARDoublePoint3d[] p=NyARDoublePoint3d.createArray(4);
									p[0].x=-40;p[0].y=40 ;p[0].z=0;
									p[1].x=40; p[1].y=40 ;p[1].z=0;
									p[2].x=40; p[2].y=-40;p[2].z=0;
									p[3].x=-40;p[3].y=-40;p[3].z=0;
									rt.getRgbPatt3d(this._input_source.reality_in,p,null, 1, bmp);
									this.getGraphics().drawImage(bmp.getBufferedImage(),this.getInsets().left+200,this.getInsets().top+240,null);
								}
								//3d（Target）
								{
									rt.getRgbRectPatt3d(this._input_source.reality_in,-40,-40,80,80,1, bmp);
									this.getGraphics().drawImage(bmp.getBufferedImage(),this.getInsets().left+300,this.getInsets().top+240,null);
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
	ARTKMarkerTable _mklib;	
	
	
	private NyARReality _reality;

	
	
	NyARParam _param;
	
	private final static String SAMPLE_FILES = "../../Data/320x240ABGR.png";
	private final static String PARAM_FILE = "../../Data/camera_para.dat";

	private static final long serialVersionUID = -2110888320986446576L;


	private int W = 640;
	private int H = 480;
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
//		this._input_source=new ImageSource(SAMPLE_FILES);
		addMouseListener(this);
		this._mklib= new ARTKMarkerTable(10,16,16,25,25,4);
		this._mklib.addMarkerFromARPattFile(PATT_HIRO,0,"HIRO",80,80);
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
    
	private final static String PATT_HIRO = "../../Data/patt.hiro";
    


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

