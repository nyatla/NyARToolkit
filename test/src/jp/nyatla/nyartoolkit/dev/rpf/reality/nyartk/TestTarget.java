package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.media.format.VideoFormat;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_RGB;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.NyARRasterFilter_Rgb2Gs_RgbAve;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
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
class InputSource
{
	public NyARRealityIn reality_in;
}

class ImageSource extends InputSource
{
	public ImageSource(String i_filename) throws NyARException, IOException
	{
		BufferedImage _src_image;
		_src_image = ImageIO.read(new File(i_filename));
		NyARReality_JavaImage ri=new NyARReality_JavaImage(_src_image.getWidth(),_src_image.getHeight(),2);
		ri.setImage(_src_image);
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
		this.reality_in=new NyARReality_JmfSource(320, 240,this._capture.getCaptureFormat());
		return;
		
	}
	
	public void onUpdateBuffer(javax.media.Buffer i_buffer)
	{
		try {
			//キャプチャしたバッファをラスタにセット
			synchronized(this){
				((NyARReality_JmfSource)(this.reality_in)).setImage(i_buffer);
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




/**
 * @todo
 * 矩形の追跡は動いてるから、位置予測機能と組み合わせて試すこと。
 *
 */

public class TestTarget extends Frame implements MouseListener
{
	public void mouseClicked(MouseEvent e)
	{
		int x=e.getX()-this.getInsets().left;
		int y=e.getY()-this.getInsets().top;
		System.out.println(x+":"+y);
		synchronized(this._input_source.reality_in)
		{
			for(int i=this._reality_snapshot.target.getLength()-1;i>=0;i--)
			{
				NyARRealityTarget rt=this._reality_snapshot.target.getItem(i);
				if(rt.isInnerPoint2d(x, y))
				{
					if(e.getButton()==MouseEvent.BUTTON1){
						//左ボタンはUNKNOWN→KNOWN
						if(rt.target_type==NyARRealityTarget.RT_UNKNOWN){
							this._reality_snapshot.changeTargetToKnown(rt,0,40);
							break;
						}
					}else if(e.getButton()==MouseEvent.BUTTON3){
						//右ボタンはUNKNOWN　or KNOWN to dead
						try {
							this._reality_snapshot.changeTargetToDead(rt);
						} catch (NyARException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		}
	}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	
	
	
	private NyARReality _reality;
	private NyARRealitySnapshot _reality_snapshot;
	
	
	NyARParam _param;
	
	private final static String SAMPLE_FILES = "../Data/320x240ABGR.png";
	private final static String PARAM_FILE = "../Data/camera_para.dat";

	private static final long serialVersionUID = -2110888320986446576L;


	private int W = 320;
	private int H = 240;
	InputSource _input_source;
	public TestTarget() throws NyARException, Exception
	{
		setTitle("NyARReality test");
		Insets ins = this.getInsets();
		this.setSize(1024 + ins.left + ins.right, 768 + ins.top + ins.bottom);
		this._param=new NyARParam();
		this._param.loadARParamFromFile(PARAM_FILE);
		this._param.changeScreenSize(W,H);
		this._reality=new NyARReality(320,240,2,this._param.getPerspectiveProjectionMatrix(),10,10);
		this._reality_snapshot=new NyARRealitySnapshot(10,10);
//		this._input_source=new LiveSource();
		this._input_source=new ImageSource(SAMPLE_FILES);
		addMouseListener(this);

		return;
	}	
	public void draw(INyARRgbRaster i_raster)
	{
		
	}
    public void update()
    {
		try {
			// マーカーを検出
			Thread.sleep(30);
			synchronized(this._input_source.reality_in){
				Date d2 = new Date();
				for (int i = 0; i < 1000; i++) {
					this._reality.progress(this._input_source.reality_in,this._reality_snapshot);			
				}
				Date d = new Date();
				System.out.println(d.getTime() - d2.getTime());
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
    	NyARRasterImageIO.copy(this._input_source.reality_in.sourceimage,bmp);
    	
    	//Ignore,Coord,New

    	//表示
    	g.setColor(Color.black);
    	g.drawString("Unknown:"+this._reality_snapshot.number_of_unknown,200,200);
    	g.drawString("Known:"+this._reality_snapshot.number_of_known,200,210);
    	g.drawString("Dead:"+this._reality_snapshot.number_of_dead,200,220);
    	ig.drawImage(bmp,ins.left,ins.top,null);

    	drawImage(ig,ins.left+320,ins.top,this._input_source.reality_in.lrsamplerin._rbraster);
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