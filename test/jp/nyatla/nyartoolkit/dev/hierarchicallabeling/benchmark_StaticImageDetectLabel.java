package jp.nyatla.nyartoolkit.dev.hierarchicallabeling;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.media.Buffer;

import com.ibm.media.codec.audio.gsm.GsmDecoder;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.labeling.NyARLabelOverlapChecker;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARLabeling_Rle;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfoPtrStack;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.pickup.INyARColorPatt;
import jp.nyatla.nyartoolkit.core.pickup.NyARColorPatt_Perspective_O2;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.NyARRasterFilter_Rgb2Gs_RgbAve;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2SquareVertexIndexes;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector_Rle;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareStack;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARRectOffset;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.utils.j2se.NyARRasterImageIO;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;


import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_RGB;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.ParcialSquareDetector.LabelInfo;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.ParcialSquareDetector.LabelStack;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.ParcialSquareDetector.Labeling;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.ParcialSquareDetector.NextStack;



/**
 * @todo
 * 矩形の追跡は動いてるから、位置予測機能と組み合わせて試すこと。
 *
 */

public class benchmark_StaticImageDetectLabel extends Frame implements MouseMotionListener
{
	class TestL extends NyARLabeling_Rle
	{
		Graphics g;
		public TestL(int i_width,int i_height) throws NyARException
		{
			super(i_width,i_height);
		}
		protected void onLabelFound(NyARRleLabelFragmentInfo i_label)
		{/*
			g.setColor(Color.red);
			g.drawRect(
					i_label.clip_l,
					i_label.clip_t,
					(i_label.clip_r-i_label.clip_l),
					(i_label.clip_b-i_label.clip_t));
*/		}
	}
	class MyDetector extends HierarchyLabeling
	{
		public Graphics g;
		public MyDetector(int i_width,int i_height,int i_depth,int i_raster_type) throws NyARException
		{
			super(i_width,i_height,i_depth,i_raster_type);
		}
		protected void onLabelFound(HierarchyRect i_imgmap,NyARGrayscaleRaster i_raster,NyARRleLabelFragmentInfo info)
		{
			//検出矩形を定義する。
			//l*skip-skip,t*skip-skip,r+skip,b+skip
			g.setColor(Color.green);

			int skip=i_imgmap.dot_skip;
			//System.out.println(i_imgmap.dot_skip+":"+i_imgmap.id+","+(info.clip_l*skip+i_imgmap.x)+","+(info.clip_t*skip+i_imgmap.y));			
			g.drawRect(
					info.clip_l*skip+i_imgmap.x,
					info.clip_t*skip+i_imgmap.y,
					(info.clip_r-info.clip_l)*skip,
					(info.clip_b-info.clip_t)*skip);
		}
	
	}	
	private final static String SAMPLE_FILES = "../Data/test.jpg";

	private static final long serialVersionUID = -2110888320986446576L;


	private int W = 320;
	private int H = 240;
	BufferedImage _src_image;

	public benchmark_StaticImageDetectLabel() throws NyARException, Exception
	{
//		setTitle("Estimate Edge Sample");
		Insets ins = this.getInsets();
		this.setSize(1024 + ins.left + ins.right, 768 + ins.top + ins.bottom);

		_src_image = ImageIO.read(new File(SAMPLE_FILES));
		addMouseMotionListener(this);

		return;
	}
	int mouse_x;
	int mouse_y;
    public void mouseMoved(MouseEvent A00)
    {
        mouse_x = A00.getX();
        mouse_y = A00.getY();
        this.paint(this.getGraphics());
    }

    public void mouseDragged(MouseEvent A00) {}
	private Graphics g2;

	public void draw(INyARRgbRaster i_raster)
	{
	}
    public void paint(Graphics g)
    {
		try {
			
			Insets ins = this.getInsets();
			INyARRgbRaster ra =new NyARRgbRaster_RGB(320,240);
			NyARRasterImageIO.copy(this._src_image,ra);
			//GS値化
			NyARGrayscaleRaster gs=new NyARGrayscaleRaster(320,240);
			NyARRasterFilter_Rgb2Gs_RgbAve filter=new NyARRasterFilter_Rgb2Gs_RgbAve(ra.getBufferType());
			filter.doFilter(ra,gs);

			MyDetector psd=new MyDetector(320,240,4,ra.getBufferType());
			//GS画像の描画
			BufferedImage sink = new BufferedImage(ra.getWidth(), ra.getHeight(), ColorSpace.TYPE_RGB);
			NyARRasterImageIO.copy(gs, sink);
			//ラべリング解析
			psd.g=sink.getGraphics();
			System.out.println("start---------");
			Date d2 = new Date();
			for (int i = 0; i < 10000; i++) {
				psd.detectOutline(gs,100);
			}
			Date d = new Date();
			System.out.println("H"+(d.getTime() - d2.getTime()));
			
			
			TestL te=new TestL(320,240);
			te.g=psd.g;
			d2 = new Date();
			for (int i = 0; i < 10000; i++) {
				te.labeling(gs,50);
			}
			d = new Date();
			System.out.println("L="+(d.getTime() - d2.getTime()));
			
			
			
			//分析画像の座標計算
			int mx=mouse_x-ins.left;
			int my=mouse_y-ins.top;
			//画像を分析する。
			NyARIntRect tmprect=new NyARIntRect();
			tmprect.x=mx;
			tmprect.y=my;
			tmprect.w=8;
			tmprect.h=8;
			NyARDoublePoint2d pos=new NyARDoublePoint2d();
			NyARDoublePoint2d vec=new NyARDoublePoint2d();
			NyARVectorReader_INT1D_GRAY_8 reader=new NyARVectorReader_INT1D_GRAY_8(gs);			
/*			if(mx>0 && my>0){
				reader.getAreaVector8(tmprect,pos,vec);
			}
			//分析結果を描画
			double sin=vec.y/Math.sqrt(vec.x*vec.x+vec.y*vec.y);
			double cos=vec.x/Math.sqrt(vec.x*vec.x+vec.y*vec.y);
			Graphics g2=sink.getGraphics();
			g2.setColor(Color.BLUE);
			g2.drawLine((int)pos.x,(int)pos.y,(int)(pos.x+30*cos),(int)(pos.y+30*sin));
*/			sink.setRGB((int)pos.x,(int)pos.y,0xff0000);
			g.drawImage(sink, ins.left, ins.top, this);

			g.drawImage(sink,ins.left,ins.top+240,ins.left+32,ins.top+240+32,mx,my,mx+8,my+8,this);

		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	public static void main(String[] args)
	{

		try {
			benchmark_StaticImageDetectLabel mainwin = new benchmark_StaticImageDetectLabel();
			mainwin.setVisible(true);
			// mainwin.startImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
}
}
