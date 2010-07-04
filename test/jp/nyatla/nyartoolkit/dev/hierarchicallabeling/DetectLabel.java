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
/*
class PS2 extends ParcialSquareDetector
{
	private void getAreaVector(NyARIntRect i_area,NyARDoublePoint2d i_pos,NyARDoublePoint2d i_vec)
	{
		NyARIntPoint2d p=new NyARIntPoint2d();
		NyARVectorReader_INT1D_GRAY_8 reader=new NyARVectorReader_INT1D_GRAY_8(null);
		//x=(Σ|Vx|*Xn)/n,y=(Σ|Vy|*Yn)/n
		//x=(ΣVx)^2/(ΣVx+ΣVy)^2,y=(ΣVy)^2/(ΣVx+ΣVy)^2
		int sum_x,sum_y,sum_px,sum_py=0;
		sum_x=sum_y=sum_px=sum_py=0;
		for(int i=i_area.h-1;i>=0;i--){
			for(int i2=i_area.w-1;i2>=0;i2--){
				reader.getPixelVector8(i2, i,p);
				sum_px+=p.x;
				sum_py+=p.y;
				sum_x+=p.x*(i2+i_area.x);
				sum_y+=p.y*(i+i_area.y);
			}
		}
		//加重平均
		sum_x/=sum_px;
		sum_y/=sum_py;
	}
	private final NyARLabelOverlapChecker<NyARRleLabelFragmentInfo> _overlap_checker = new NyARLabelOverlapChecker<NyARRleLabelFragmentInfo>(32,NyARRleLabelFragmentInfo.class);
	private final NyARContourPickup _cpickup=new NyARContourPickup();

	private final NyARCoord2SquareVertexIndexes _coord2vertex=new NyARCoord2SquareVertexIndexes();
	
	private final int _max_coord;
	private final NyARIntPoint2d[] _coord;	
	protected void onLabelFound(HierarchyRect i_imgmap,NyARGrayscaleRaster i_raster,NyARRleLabelFragmentInfo info)
	{
		NyARIntPoint2d[] coord = this._coord;
		final int coord_max = this._max_coord;

		//ラベルが通知されてくる
		NyARIntRect rect=new NyARIntRect();
		rect.setLtrb(info.clip_l,info.clip_t,info.clip_r,info.clip_b);
		
		//まずは輪郭線を検出
		int coord_num = _cpickup.getContour(i_raster,rect,110,info.entry_x,info.clip_t, coord);
		if (coord_num == coord_max) {
			// 輪郭が大きすぎる。
			continue;
		}
		//ベクトル配列を生成
		for(int i=0;i<)
		
		//解像度に応じて、Coordベクトルを計算
		
		
		int label_area = label_pt.area;
		//輪郭線をチェックして、矩形かどうかを判定。矩形ならばmkvertexに取得
		if (!this._coord2vertex.getVertexIndexes(coord,coord_num,label_area, mkvertex)) {
			// 頂点の取得が出来なかった
			continue;
		}
*/		
		/*
		 coodの解像度に合せて、輪郭のベクトルを計算
		 */
		/*
		 輪郭ベクトルのクラスタリング
		 */
		/*
		 矩形化
		 */
/*	}	
}
*/
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
			tmprect.w=8;
			tmprect.h=8;
			NyARDoublePoint2d pos=new NyARDoublePoint2d();
			NyARDoublePoint2d vec=new NyARDoublePoint2d();
			if(mx>0 && my>0){
				this.getAreaVector(gs,tmprect,pos,vec);
			}
			//分析結果を描画
			double sin=vec.y/Math.sqrt(vec.x*vec.x+vec.y*vec.y);
			double cos=vec.x/Math.sqrt(vec.x*vec.x+vec.y*vec.y);
			Graphics g2=sink.getGraphics();
			g2.setColor(Color.BLUE);
			g2.drawLine((int)pos.x,(int)pos.y,(int)(pos.x+30*cos),(int)(pos.y+30*sin));
			sink.setRGB((int)pos.x,(int)pos.y,0xff0000);
			System.out.println((int)pos.x+","+(int)pos.y);
			g.drawImage(sink, ins.left, ins.top, this);

			g.drawImage(sink,ins.left,ins.top+240,ins.left+32,ins.top+240+32,mx,my,mx+8,my+8,this);

		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	private void getAreaVector(NyARGrayscaleRaster i_gs,NyARIntRect i_area,NyARDoublePoint2d i_pos,NyARDoublePoint2d i_vec)
	{
		NyARIntPoint2d p=new NyARIntPoint2d();
		NyARVectorReader_INT1D_GRAY_8 reader=new NyARVectorReader_INT1D_GRAY_8(i_gs);
		//x=(Σ|Vx|*Xn)/n,y=(Σ|Vy|*Yn)/n
		//x=(ΣVx)^2/(ΣVx+ΣVy)^2,y=(ΣVy)^2/(ΣVx+ΣVy)^2
		int sum_x,sum_y,sum_wx,sum_wy,sum_vx,sum_vy;
		sum_x=sum_y=sum_wx=sum_wy=sum_vx=sum_vy=0;
		for(int i=i_area.h-1;i>=0;i--){
			for(int i2=i_area.w-1;i2>=0;i2--){
				reader.getPixelVector8(i2+i_area.x, i+i_area.y,p);
				//加重はvectorの絶対値
				int wx=p.x*p.x;
				int wy=p.y*p.y;
				sum_wx+=wx;
				sum_wy+=wy;
				sum_vx+=wx*p.x;
				sum_vy+=wy*p.y;
				sum_x+=wx*(i2+i_area.x);
				sum_y+=wy*(i+i_area.y);
			}
		}
		//加重平均
		i_pos.x=(double)sum_x/sum_wx;
		i_pos.y=(double)sum_y/sum_wy;
		i_vec.x=(double)sum_vx/sum_wx;
		i_vec.y=(double)sum_vy/sum_wy;
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
