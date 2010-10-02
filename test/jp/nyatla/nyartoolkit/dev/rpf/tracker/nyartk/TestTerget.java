package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARLabeling_Rle;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.NyARRasterFilter_Rgb2Gs_RgbAve;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.utils.j2se.NyARRasterImageIO;
import jp.nyatla.nyartoolkit.core.types.*;


import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_RGB;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.*;
//import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcHolder.AreaSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaDataPool.AreaDataItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.EnterTargetSrc.EnterSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContoureTargetList;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContoureTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContoureTracking;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContoureTargetList.ContoureTargetItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetList;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTracking;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTracking;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList.NewTargetItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.square.SquareTargetList;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.square.SquareTargetSrc;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.*;



class IntRingBuffer
{
	public int max;
	public int[] data;
	public int ptr=0;
	public void addData(int i_v)
	{
		this.data[this.ptr]=i_v;
		this.ptr=(this.ptr+1)%this.data.length;
	}
}




/**
 * @todo
 * 矩形の追跡は動いてるから、位置予測機能と組み合わせて試すこと。
 *
 */

public class TestTerget extends Frame
{
	LowResolutionLabelingSampler sampler;
	LowResolutionLabelingSamplerIn samplerin;
	LowResolutionLabelingSamplerOut samplerout;
	
	NyARTracker tracker;
	NyARTrackerOut trackerout;
	
	private final static String SAMPLE_FILES = "../Data/test.jpg";

	private static final long serialVersionUID = -2110888320986446576L;


	private int W = 320;
	private int H = 240;
	BufferedImage _src_image;

	public TestTerget() throws NyARException, Exception
	{
		setTitle("Reality Platform test");
		Insets ins = this.getInsets();
		this.setSize(1024 + ins.left + ins.right, 768 + ins.top + ins.bottom);
		this._src_image = ImageIO.read(new File(SAMPLE_FILES));
		//create sampler
		this.samplerin=new LowResolutionLabelingSamplerIn(W, H, 3);
		this.samplerout=new LowResolutionLabelingSamplerOut(100);
		this.sampler=new LowResolutionLabelingSampler(W, H,2);
		
		//create tracker
		this.tracker=new NyARTracker();
		this.trackerout=new NyARTrackerOut();

		return;
	}



	
	
	public void draw(INyARRgbRaster i_raster)
	{
		
	}
	static long tick;
    public void update(BufferedImage buf)
    {
		try {
			
			INyARRgbRaster ra =new NyARRgbRaster_RGB(320,240);
			NyARRasterImageIO.copy(buf,ra);
			//GS値化
			NyARGrayscaleRaster gs=new NyARGrayscaleRaster(320,240);
			NyARRasterFilter_Rgb2Gs_RgbAve filter=new NyARRasterFilter_Rgb2Gs_RgbAve(ra.getBufferType());
			filter.doFilter(ra,gs);
			//samplerへ入力
			this.samplerin.wrapBuffer(gs);
			this.sampler.sampling(this.samplerin,this.samplerout);
			//tracker更新
			this.tracker.progress(this.samplerout,this.trackerout);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    BufferedImage _tmp_bf=new BufferedImage(320,240,BufferedImage.TYPE_INT_RGB);
    private void draw(Graphics ig,BufferedImage sink) throws NyARException
    {
    	//ウインドウの情報
		Insets ins = this.getInsets();

    	//ワーク画面
    	BufferedImage bmp=this._tmp_bf;
    	Graphics g=bmp.getGraphics();
    	g.drawImage(sink,0,0,null);
    	//Ignore,Coord,New
    	drawNewTarget(bmp,Color.green);
    	drawIgnoreTarget(bmp,Color.red);
    	drawContourTarget(bmp,Color.blue);
    	drawRectTarget(bmp,Color.cyan);
		
    	//表示
    	ig.drawImage(bmp,ins.left,ins.top,null);
    }
    //
    //描画関数
    //
    /**
     * RectTargetを表示します。
     */
    private void drawRectTarget(BufferedImage sink,Color c)
    {
    	//サンプリング結果の表示
    	Graphics g=sink.getGraphics();
    	g.setColor(c);
		for(int i=this.trackerout.recttarget.getLength()-1;i>=0;i--){
			NyARTarget t=this.trackerout.recttarget.getItem(i);
			g.drawString("RT",t.sample_area.x,t.sample_area.y);
			for(int i2=0;i2<4;i2++){
				NyARRectTargetStatus s=(NyARRectTargetStatus)t.ref_status;
//				g.fillRect((int)st.vecpos[i2].x-1, (int)st.vecpos[i2].y-1,2,2);
				g.drawLine(
					(int)s.square.sqvertex[i2].x,
					(int)s.square.sqvertex[i2].y,
					(int)s.square.sqvertex[(i2+1)%4].x,
					(int)s.square.sqvertex[(i2+1)%4].y);
			}
		}
    }

    /**
     * ContourTargetを表示します。
     */
    private void drawContourTarget(BufferedImage sink,Color c)
    {
    	//サンプリング結果の表示
    	Graphics g=sink.getGraphics();
    	g.setColor(c);
		for(int i=this.trackerout.coordtarget.getLength()-1;i>=0;i--){
			NyARTarget t=this.trackerout.coordtarget.getItem(i);
			g.drawString("CT",t.sample_area.x,t.sample_area.y);
			g.drawRect(t.sample_area.x,t.sample_area.y,t.sample_area.w,t.sample_area.h);
			NyARContourTargetStatus st=(NyARContourTargetStatus)t.ref_status;
			for(int i2=0;i2<st.vecpos_length;i2++){
				g.fillRect((int)st.vecpos[i2].x-1, (int)st.vecpos[i2].y-1,2,2);
			}
		}
    }
    
    /**
     * IgnoreTargetを表示します。
     */
    private void drawIgnoreTarget(BufferedImage sink,Color c)
    {
    	//サンプリング結果の表示
    	Graphics g=sink.getGraphics();
    	g.setColor(c);
		for(int i=this.trackerout.igtarget.getLength()-1;i>=0;i--){
			NyARTarget t=this.trackerout.igtarget.getItem(i);
			g.drawString("IG",t.sample_area.x,t.sample_area.y);
			g.drawRect(t.sample_area.x,t.sample_area.y,t.sample_area.w,t.sample_area.h);
		}
    }
        
    /**
     * Newtargetを表示します。
     */
    private void drawNewTarget(BufferedImage sink,Color c)
    {
    	//サンプリング結果の表示
    	Graphics g=sink.getGraphics();
    	g.setColor(c);
		for(int i=this.trackerout.newtarget.getLength()-1;i>=0;i--){
			NyARTarget t=this.trackerout.newtarget.getItem(i);
			g.drawString("NW",t.sample_area.x,t.sample_area.y);
			g.drawRect(t.sample_area.x,t.sample_area.y,t.sample_area.w,t.sample_area.h);
		}
    }
    
    /**
     * サンプリング結果を描画します。
     * @param sink
     * @param outg
     */
    private void drawSamplerResult(BufferedImage sink,Color c)
    {
    	//サンプリング結果の表示
    	Graphics g=sink.getGraphics();
    	g.setColor(c);
		for(int i=this.samplerout.getLength()-1;i>=0;i--){
			LowResolutionLabelingSamplerOut.Item item=this.samplerout.getArray()[i];
			g.drawRect(item.base_area.x,item.base_area.y,item.base_area.w,item.base_area.h);
		}
    }
    
    
    public void getCrossPos(NyARPointVector2d vec1,NyARPointVector2d vec2,NyARDoublePoint2d o_pos)
    {
    	NyARLinear line1=new NyARLinear();
    	NyARLinear line2=new NyARLinear();
    	line1.setVector(vec1.dx,vec1.dy,vec1.x,vec1.y);
    	line1.orthogonalLine();
    	line2.setVector(vec1.dx,vec1.dy,vec1.x,vec1.y);
    	line2.orthogonalLine();
    	NyARLinear.crossPos(line1,line2, o_pos);
    	o_pos.y=o_pos.y;
    	
    }

    public void mainloop() throws Exception
    {
    	for(;;){
	    	//処理
	    	this.update(this._src_image);
			this.draw(this.getGraphics(),this._src_image);
	    	Thread.sleep(30);
    	}
    }

	public static void main(String[] args)
	{

		try {
			TestTerget mainwin = new TestTerget();
			mainwin.setVisible(true);
			mainwin.mainloop();
			// mainwin.startImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
}
}
