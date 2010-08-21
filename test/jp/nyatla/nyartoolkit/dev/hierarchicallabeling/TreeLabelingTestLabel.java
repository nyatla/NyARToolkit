/*	
 * 
 */
package jp.nyatla.nyartoolkit.dev.hierarchicallabeling;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
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


/**
 * label_stackにソート後の結果を蓄積するクラス
 */




class ParcialSquareDetector
{
	public class LabelInfo
	{
		public int l;
		public int t;
		public int w;
		public int h;
		public int entry_x;
		public int dot_skip;
	}
	class NextStack extends NyARPointerStack<HierarchyRect>
	{
		public NextStack(int i_length) throws NyARException
		{
			super.initInstance(i_length,HierarchyRect.class);
		}
		protected HierarchyRect createElement()
		{
			return new HierarchyRect();
		}
		/**
		 * 指定した矩形が含まれるアイテムがあるか返す。
		 * @param i_x
		 * @param i_y
		 * @param i_w
		 * @param i_h
		 * @return
		 */
		public boolean hasInnerItem(int i_x,int i_y,int i_w,int i_h)
		{
			for(int i=this._length-1;i>=0;i--)
			{
				if(this._items[i].isInnerRect(i_x, i_y, i_w, i_h)){
					return true;
				}
			}
			return false;
		}
	}
	class LabelStack extends NyARObjectStack<LabelInfo>
	{
		public LabelStack(int i_length) throws NyARException
		{
			super.initInstance(i_length,LabelInfo.class);
		}
		protected LabelInfo createElement()
		{
			return new LabelInfo();
		}
	}
	class Labeling extends NyARLabeling_Rle
	{
		public NextStack next_stack;		
		HierarchyRect _target_image;
		private NyARIntSize _half_size=new NyARIntSize();
		private NyARGrayscaleRaster _gs;
		private Labeling _labeling_tree;
		private ParcialSquareDetector _parent;

		public Labeling(ParcialSquareDetector i_parent,int i_width,int i_height,int i_raster_type,int i_depth) throws NyARException
		{
			super(i_width,i_height);
this.setAreaRange(999999999,1);
			this._half_size.w=i_width/3;
			this._half_size.h=i_height/3;
			this._parent=i_parent;
			this.next_stack=new NextStack(9);           //子の最大9個
			this._gs=new NyARGrayscaleRaster(i_width,i_height);
			if(i_depth>1){
				this._labeling_tree=new Labeling(i_parent,i_width,i_height,i_raster_type,i_depth-1);
			}
			return;
		}
		
		public void labeling(NyARGrayscaleRaster i_raster,HierarchyRect i_imagemap,int i_th) throws NyARException
		{
			this._target_image=i_imagemap;
			//配列初期化
			this.next_stack.clear();

			//GS化
			NyARGrayscaleRaster.copy(i_raster, i_imagemap.x, i_imagemap.y, i_imagemap.dot_skip, this._gs);
			//ラべリング
			super.labeling(this._gs,i_th);
			//末端の解像度なら終了
			if(i_imagemap.dot_skip==1){
				return;
			}
			HierarchyRect[] infos=this.next_stack.getArray();
			for(int i=this.next_stack.getLength()-1;i>=0;i--){
				this._labeling_tree.labeling(i_raster,infos[i],i_th);
			}
		}		
		protected void onLabelFound(NyARRleLabelFragmentInfo i_label)
		{
//クリップ条件が本当にいいか確認
			HierarchyRect imagemap=this._target_image;
			int w=i_label.clip_r-i_label.clip_l+1;
			int h=i_label.clip_b-i_label.clip_t+1;
			
			// クリップ領域が画面の枠(4面)に接していれば除外
			if (i_label.clip_l==0 || i_label.clip_t==0 || i_label.clip_r-1==this._parent._image_map.window_size.w || i_label.clip_b-1==this._parent._image_map.window_size.h){
				return;
			}
			System.out.println(i_label.clip_l+":"+i_label.clip_t+":"+i_label.clip_r+":"+i_label.clip_b);
			// 1/4より小さければ、下位の検出に回す
			if(this._half_size.isInnerSize(w,h)){
				int skip=imagemap.dot_skip;
				//skip値を考慮して、存在位置を補正(事前計算できそう？)
				int tl=i_label.clip_l*skip+imagemap.x-skip;
				if(tl<0){tl=0;}
				int tt=i_label.clip_t*skip+imagemap.y-skip;
				if(tt<0){tt=0;}
				int tw=w*skip+skip;
				if(tw>imagemap.w){tw=imagemap.w;}
				int th=h*skip+skip;
				if(th<0){th=0;}
				if(th>imagemap.h){th=imagemap.h;}
				//既に下位に検出依頼を出している？
				if(this.next_stack.hasInnerItem(tl,tt,tw,th))
				{
					return;
				}
				//追加するマップを検索
				HierarchyRect next_map=imagemap.getInnerChild(tl,tt,tw,th);
				if(next_map!=null){
					this.next_stack.push(next_map);
					return;
				}
			}
			//矩形を検出した。情報その他を関数に通知
			this._parent.onLabelFound(imagemap,this._gs,i_label);
		}

		
	}
	private Labeling _labeling;
	private QsHsHierachyRectMap _image_map;
	private NyARRasterFilter_Rgb2Gs_RgbAve _filter;	
	public ParcialSquareDetector(int i_width,int i_height,int i_depth,int i_raster_type) throws NyARException
	{
		this._image_map=new QsHsHierachyRectMap(i_width,i_height,i_depth);
		this._labeling=new Labeling(this,this._image_map.window_size.w,this._image_map.window_size.h,i_raster_type,i_depth);
		this._filter=new NyARRasterFilter_Rgb2Gs_RgbAve(i_raster_type);
	}
	public void detectOutline(INyARRgbRaster i_raster,int i_th) throws NyARException
	{
		NyARGrayscaleRaster gs=new NyARGrayscaleRaster(i_raster.getWidth(),i_raster.getHeight());

		this._filter.doFilter(i_raster, gs);
		this._labeling.labeling(gs,this._image_map.top_image,i_th);
	}
	public LabelStack ls=new LabelStack(1000);
	protected void onLabelFound(HierarchyRect i_imgmap,NyARGrayscaleRaster i_raster,NyARRleLabelFragmentInfo info)
	{
		//depth補正とかやろうか。
	}	
}

/**
 * @todo
 * 矩形の追跡は動いてるから、位置予測機能と組み合わせて試すこと。
 *
 */

public class TreeLabelingTestLabel extends Frame implements MouseMotionListener
{
	class MyDetector extends ParcialSquareDetector
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
			g.setColor(Color.red);

			int skip=i_imgmap.dot_skip;
			g.drawRect(
					info.clip_l*skip+i_imgmap.x,
					info.clip_t*skip+i_imgmap.y,
					(info.clip_r-info.clip_l)*skip,
					(info.clip_b-info.clip_t)*skip);
		}
	
	}
	private final String PARAM_FILE = "../Data/camera_para.dat";

	private final static String CARCODE_FILE = "../Data/patt.hiro";

	private static final long serialVersionUID = -2110888320986446576L;


	private int W = 320;

	private int H = 240;

	private MyDetector _tr;

	public TreeLabelingTestLabel() throws NyARException
	{
		setTitle("JmfCaptureTest");
		Insets ins = this.getInsets();
		this.setSize(1024 + ins.left + ins.right, 768 + ins.top + ins.bottom);
		JmfCaptureDeviceList dl = new JmfCaptureDeviceList();
		NyARParam ar_param = new NyARParam();


		addMouseMotionListener(this);
		this._tr=new MyDetector(W,H,3,NyARBufferType.BYTE1D_R8G8B8_24);

		return;
	}
	int mouse_x;
	int mouse_y;
    public void mouseMoved(MouseEvent A00)
    {
        mouse_x = A00.getX();
        mouse_y = A00.getY();
    }

    public void mouseDragged(MouseEvent A00) {}



	private final String data_file = "../Data/320x240ABGR.raw";
	private void drawPolygon(Graphics g,NyARDoublePoint2d[] i_vertex,int i_len)
	{
		int[] x=new int[i_len];
		int[] y=new int[i_len];
		for(int i=0;i<i_len;i++)
		{
			x[i]=(int)i_vertex[i].x;
			y[i]=(int)i_vertex[i].y;
		}
		g.drawPolygon(x,y,i_len);
	}
	private void drawPolygon(Graphics g,NyARIntPoint2d[] i_vertex,int i_len)
	{
		int[] x=new int[i_len];
		int[] y=new int[i_len];
		for(int i=0;i<i_len;i++)
		{
			x[i]=(int)i_vertex[i].x;
			y[i]=(int)i_vertex[i].y;
		}
		g.drawPolygon(x,y,i_len);
	}

	
	
	private Graphics g2;

	public void draw(INyARRgbRaster i_raster)
	{
		try {
			Insets ins = this.getInsets();
			Graphics g = getGraphics();
			
			
				
			BufferedImage sink = new BufferedImage(i_raster.getWidth(), i_raster.getHeight(), ColorSpace.TYPE_RGB);
			this._tr.g=sink.getGraphics();
			NyARRasterImageIO.copy(i_raster, sink);
			this._tr.detectOutline(i_raster,110);
			g.drawImage(sink, ins.left, ins.top, this);
						
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


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

	public static void main(String[] args)
	{

		try {
			TreeLabelingTestLabel mainwin = new TreeLabelingTestLabel();
			mainwin.setVisible(true);
			mainwin.startImage();
			// mainwin.startImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
}
}
