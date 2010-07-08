package jp.nyatla.nyartoolkit.dev.hierarchicallabeling;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

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


class PixelVecFixedFloat24
{
	int dx;
	int dy;
	int x;
	int y;
}

/**
 * @todo
 * 矩形の追跡は動いてるから、位置予測機能と組み合わせて試すこと。
 *
 */

public class DetectLabel extends Frame implements MouseMotionListener
{
	private final static String SAMPLE_FILES = "../Data/test.jpg";

	private static final long serialVersionUID = -2110888320986446576L;


	private int W = 320;
	private int H = 240;
	BufferedImage _src_image;

	public DetectLabel() throws NyARException, Exception
	{
		setTitle("Estimate Edge Sample");
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
			//GS画像の描画
			BufferedImage sink = new BufferedImage(ra.getWidth(), ra.getHeight(), ColorSpace.TYPE_RGB);
			NyARRasterImageIO.copy(gs, sink);
			//分析画像の座標計算
			int mx=mouse_x-ins.left;
			int my=mouse_y-ins.top;
			//画像を分析する。
			NyARIntRect tmprect=new NyARIntRect();
			tmprect.x=mx;
			tmprect.y=my;
			tmprect.w=3;
			tmprect.h=3;
			NyARDoublePoint2d pos=new NyARDoublePoint2d();
			NyARDoublePoint2d vec=new NyARDoublePoint2d();
			NyARVectorReader_INT1D_GRAY_8 reader=new NyARVectorReader_INT1D_GRAY_8(gs);			
for(int i=0;i<80*55;i++){
			tmprect.x=4*(i%80)+1;
			tmprect.y=4*(i/80)+1;
			if(mx>0 && my>0){
				reader.getAreaVector8(tmprect,pos,vec);
			}
			//分析結果を描画
			double sin=vec.y/Math.sqrt(vec.x*vec.x+vec.y*vec.y);
			double cos=vec.x/Math.sqrt(vec.x*vec.x+vec.y*vec.y);
			Graphics g2=sink.getGraphics();
			int v=(int)Math.sqrt(vec.x*vec.x+vec.y*vec.y)/10;
			
			g2.setColor(Color.BLUE);
//			g2.drawLine((int)pos.x,(int)pos.y,(int)(pos.x+30*cos),(int)(pos.y+30*sin));
			if(v>0){
				g2.drawLine((int)pos.x,(int)pos.y,(int)(pos.x+v*cos),(int)(pos.y+v*sin));
			}
}			
//			sink.setRGB((int)pos.x,(int)pos.y,0xff0000);
			System.out.println((int)pos.x+","+(int)pos.y);
			g.drawImage(sink, ins.left, ins.top, this);

			g.drawImage(sink,ins.left,ins.top+240,ins.left+32,ins.top+240+32,mx,my,mx+8,my+8,this);

		} catch (Exception e) {
			e.printStackTrace();
		}
    }
/*
	public void startImage() throws NyARException, Exception
	{
		INyARRgbRaster ra =new NyARRgbRaster_RGB(320,240);
		BufferedImage sink = new BufferedImage(320,240, ColorSpace.TYPE_RGB);
		Graphics g = sink.getGraphics();
		
		int x=0,y=10;
		int sx=1,sy=1;
		for(;;){
			g.setColor(Color.white);
			g.fillRect(0, 0,320,240);
			x+=sx;
			y+=sy;
			if(x<1 || x>200) sx*=-1;
			if(y<1 || y>200) sy*=-1;
			g.setColor(Color.black);
			g.fillRect(x, y,16,16);
			//g.fillRect(158,158,20,20);
			NyARRasterImageIO.copy(sink,ra);
			draw(ra);
			Thread.sleep(100);			
		}
	}
*/
	public static void main(String[] args)
	{

		try {
			DetectLabel mainwin = new DetectLabel();
			mainwin.setVisible(true);
			// mainwin.startImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
}
}
