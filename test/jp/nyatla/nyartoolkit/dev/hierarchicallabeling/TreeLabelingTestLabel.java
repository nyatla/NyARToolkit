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


import jp.nyatla.nyartoolkit.dev.tracking.detail.*;
import jp.nyatla.nyartoolkit.dev.tracking.detail.fixedthreshold.NyARDetailFixedThresholdTrackSrcTable;
import jp.nyatla.nyartoolkit.dev.tracking.detail.labeling.NyARDetailLabelingTrackItem;
import jp.nyatla.nyartoolkit.dev.tracking.detail.labeling.NyARDetailLabelingTrackSrcTable;
import jp.nyatla.nyartoolkit.dev.tracking.detail.labeling.NyARDetailLabelingTracker;
import jp.nyatla.nyartoolkit.dev.tracking.outline.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_BGRA;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_RGB;

/**
 * 1/4サイズ矩形を1/2づつシフトさせた、3x3マップをもつ階層矩形を保持します。
 * この矩形の解像度は1階層ごとに1/2になります。
 * 
 */
class QsHsRectMap
{
	public NyARIntSize window_size=new NyARIntSize();
	/**
	 * 解像度のインデクス(1/2^n,1/(n-1)^2,....1)の順で並んでる。
	 */
	public int[] _depth_index;
	public int[] _resolution;
	public int resulution_depth;
	public HierarchyRect top_image;
	
	/**
	 * 階層RECTの実体
	 */
	private HierarchyRect[] _buf;
	
	/**
	 * 
	 * @param i_target_w
	 * 画像サイズ
	 * @param i_target_h
	 * 画像サイズ
	 * @param i_depth
	 * 分解能値(2^n形式)
	 * @throws NyARException
	 */
	public QsHsRectMap(int i_target_w,int i_target_h,int i_depth) throws NyARException
	{
		initInstance(i_target_w,i_target_h,i_depth);
		return;
	}
	public HierarchyRect[] getRectBuf()
	{
		return this._buf;
	}

	/**
	 * 
	 * @param i_rect
	 * @param i_index
	 * 求めるRECTのindex
	 * @param i_depth
	 * @param i_a1
	 * 現階層の開始インデクス
	 * @param i_a2
	 * 次の階層の開始点
	 * @param i_d1
	 */
	private void setHierarchy(HierarchyRect i_rect,int i_index,int i_a1,int i_depth)
	{
		if(i_rect.ref_children!=null){
			return;
		}
		if(i_depth<=1){
			i_rect.ref_children=null;
			return;
		}
		int a1=this._depth_index[i_a1];
		int a2=this._depth_index[i_a1+1];
		int d1=this._resolution[i_a1];
		int d2=this._resolution[i_a1+1];
		int this_index=i_index-a1;
		i_rect.ref_children=new HierarchyRect[9];
		for(int y=0;y<3;y++){
			for(int x=0;x<3;x++){
				int idx=a2+((this_index%d1)*2+x)+((this_index/d1)*2+y)*d2;
				i_rect.ref_children[x+y*3]=this._buf[idx];
				setHierarchy(i_rect.ref_children[x+y*3],idx,i_a1+1,i_depth-1);
			}
		}
		return;
	}
	/**
	 * 任意サイズの矩形から、QuadSizeHalfShiftの矩形ツリーを作ります。画像が2の階乗でない場合、
	 * 解像度の2^(depth-1)の余剰分は切り捨てられます。
	 * @param i_target_w
	 * @param i_target_h
	 * @param i_depth
	 * 矩形階層の深さを指定します。
	 */
	private void initInstance(int i_target_w,int i_target_h,int i_depth)
	{
		this.resulution_depth=i_depth;
		this._depth_index=new int[i_depth];
		this._resolution=new int[i_depth];

		//resolutionインデクスと矩形要素の合計値を計算
		//1+9+49+・・・
		int number_of_data=0;
		int ls=1;
		int c=1;
		int div_pow=i_depth-1;
		for(int i=0;i<=div_pow;i++){
			this._resolution[i]=ls;
			this._depth_index[i]=number_of_data;
			number_of_data+=ls*ls;
			c*=2;
			ls+=c;
		}

		HierarchyRect[] buf=new HierarchyRect[number_of_data];
		for(int i=0;i<number_of_data;i++){
			buf[i]=new HierarchyRect();
			buf[i].id=i;
		}
		
		int div=(int)Math.pow(2,div_pow);
		//ターゲット範囲を決める(端数は端に分散)
		int target_w=i_target_w-i_target_w%div;
		int target_h=i_target_h-i_target_h%div;
		int target_t=i_target_w%div/2;
		int target_l=i_target_h%div/2;
		
		int window_w=target_w/div;
		int window_h=target_h/div;

		//矩形のパラメータを定義
		int ptr;
		int lc=1;
		ls=1;
		ptr=0;
		int skip_bit=div;
		for(int i=0;i<=div_pow;i++){
			for(int y=0;y<lc;y++){
				for(int x=0;x<lc;x++){
					buf[ptr].dot_skip=skip_bit;
					buf[ptr].x=target_l+(window_w*skip_bit/2)*x;
					buf[ptr].y =target_t+(window_h*skip_bit/2)*y;
					buf[ptr].w=window_w*skip_bit;
					buf[ptr].h=window_h*skip_bit;
					ptr++;
				}
			}
			skip_bit/=2;
			ls*=2;
			lc+=ls;
		}
		this._buf=buf;
		setHierarchy(this._buf[0],0,0,i_depth);
		/*
		//ツリー構造部分を計算(無駄が多いけどめんどくさいからこのまま)
		ptr=0;
		for(int i=0;i<number_of_data;i++){
			if(buf[i].dot_skip==1){
				buf[i].ref_children=null;
				continue;
			}
			//同階層のWindowを読み飛ばし
			int i2=i+1;
			for(;i2<number_of_data;i2++){
				if(buf[i].dot_skip>buf[i2].dot_skip){
					break;
				}
			}
			buf[i].ref_children=new HierarchyRect[9];
			int n=0;
			for(;i2<number_of_data;i2++){
				//自分自身に含まれる子矩形を子リストに加える。
				if(buf[i].dot_skip/2!=buf[i2].dot_skip){
					//解像度が変わったら終了
					break;
				}
				if(buf[i].isInnerRect(buf[i2])){
					buf[i].ref_children[n]=buf[i2];
					n++;
				}
			}
		}*/

		//結果を記録
		this.window_size.setValue(window_w,window_h);
		this.top_image=buf[0];
	}
}
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
			super(i_length,HierarchyRect.class);
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
			super(i_length,LabelInfo.class);
		}
		protected LabelInfo createElement()
		{
			return new LabelInfo();
		}
	}
	class Labeling extends NyARLabeling_Rle
	{
		public NextStack next_stack;		
		private LabelStack _outlabel_stack;
		HierarchyRect _target_image;
		private NyARIntSize _half_size=new NyARIntSize();
		private NyARGrayscaleRaster _gs;
		private NyARRasterFilter_Rgb2Gs_RgbAve _filter;
		private Labeling _labeling_tree;

		public Labeling(int i_width,int i_height,int i_raster_type,int i_depth) throws NyARException
		{
			super(i_width,i_height);
this.setAreaRange(999999999,0);
			this._half_size.w=i_width/3;
			this._half_size.h=i_height/3;
			
			int stack_size=i_width*i_height*2048/(320*240)+32;

			this._outlabel_stack=new LabelStack(stack_size);//検出可能な最大ラベル数
			this.next_stack=new NextStack(9);           //最大9個
			this._gs=new NyARGrayscaleRaster(i_width,i_height);
			this._filter=new NyARRasterFilter_Rgb2Gs_RgbAve(i_raster_type);
			if(i_depth>1){
				this._labeling_tree=new Labeling(i_width,i_height,i_raster_type,i_depth-1);
			}
			return;
		}
		
		public void labeling(INyARRgbRaster i_raster,HierarchyRect i_imagemap,int i_th,LabelStack o_out_stack) throws NyARException
		{
			this._target_image=i_imagemap;
			this._outlabel_stack=o_out_stack;
			//配列初期化
			this._outlabel_stack.clear();
			this.next_stack.clear();

			//GS化
			this._filter.doCutFilter(i_raster, i_imagemap.x, i_imagemap.y, i_imagemap.dot_skip, this._gs);
			//ラべリング
			super.labeling(this._gs,i_th);
			//末端の解像度なら終了
			if(i_imagemap.dot_skip==1){
				return;
			}
			HierarchyRect[] infos=this.next_stack.getArray();
			for(int i=this.next_stack.getLength()-1;i>=0;i--){
				this._labeling_tree.labeling(i_raster,infos[i],i_th,o_out_stack);
			}
		}		
		protected void onLabelFound(NyARRleLabelFragmentInfo i_label)
		{
//クリップ条件が本当にいいか確認
			HierarchyRect imagemap=this._target_image;
			int w=i_label.clip_r-i_label.clip_l+1;
			int h=i_label.clip_b-i_label.clip_t+1;
			// クリップ領域が画面の枠(4面)に接していれば除外
			if (w>=this._raster_size.w || h>=this._raster_size.h){
				return;
			}
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
				}
			}
			

			//1/4より大きいので、検索スタックにpush
			int skip=imagemap.dot_skip;
			LabelInfo item=this._outlabel_stack.prePush();
			item.l=i_label.clip_l*skip+imagemap.x;
			item.t=i_label.clip_t*skip+imagemap.y;
			item.w=w*skip;
			item.h=h*skip;
			item.dot_skip=skip;
			item.entry_x=i_label.clip_l*skip+i_label.entry_x*skip;
		}
		
	}
	private Labeling _labeling;
	private QsHsRectMap _image_map;
	public ParcialSquareDetector(int i_width,int i_height,int i_depth,int i_raster_type) throws NyARException
	{
		this._image_map=new QsHsRectMap(i_width,i_height,i_depth);
		this._labeling=new Labeling(this._image_map.window_size.w,this._image_map.window_size.h,i_raster_type,i_depth);
	}
	public void detectOutline(INyARRgbRaster i_raster,int i_th) throws NyARException
	{
		this._labeling.labeling(i_raster,this._image_map.top_image,i_th,this.ls);
	}
	public LabelStack ls=new LabelStack(1000);
}

/**
 * @todo
 * 矩形の追跡は動いてるから、位置予測機能と組み合わせて試すこと。
 *
 */

public class TreeLabelingTestLabel extends Frame implements MouseMotionListener
{

	private final String PARAM_FILE = "../Data/camera_para.dat";

	private final static String CARCODE_FILE = "../Data/patt.hiro";

	private static final long serialVersionUID = -2110888320986446576L;


	private int W = 320;

	private int H = 240;

	private ParcialSquareDetector _tr;

	public TreeLabelingTestLabel() throws NyARException
	{
		setTitle("JmfCaptureTest");
		Insets ins = this.getInsets();
		this.setSize(1024 + ins.left + ins.right, 768 + ins.top + ins.bottom);
		JmfCaptureDeviceList dl = new JmfCaptureDeviceList();
		NyARParam ar_param = new NyARParam();
		ar_param.loadARParamFromFile(PARAM_FILE);
		ar_param.changeScreenSize(W, H);

		NyARCode code = new NyARCode(9, 9);
		code.loadARPattFromFile(CARCODE_FILE);

		addMouseMotionListener(this);
		this._tr=new ParcialSquareDetector(W,H,5,NyARBufferType.BYTE1D_R8G8B8_24);

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
			this._tr.detectOutline(i_raster,110);
			
			
				
			BufferedImage sink = new BufferedImage(i_raster.getWidth(), i_raster.getHeight(), ColorSpace.TYPE_RGB);
			BufferedImage sink160x120 = new BufferedImage(160,120, ColorSpace.TYPE_RGB);
			BufferedImage sink20x15 = new BufferedImage(20,15, ColorSpace.TYPE_RGB);
			this.g2=sink.getGraphics();
			NyARRasterImageIO.copy(i_raster, sink);
			g2.setColor(Color.red);
			for(int i=this._tr.ls.getLength()-1;i>=0;i--){
				ParcialSquareDetector.LabelInfo rect=this._tr.ls.getItem(i);
				g2.drawRect(rect.l,rect.t,rect.w,rect.h);
				System.out.println(rect.l+","+rect.t+","+rect.w+","+rect.h+":"+rect.dot_skip);
				
			}
			g.drawImage(sink, ins.left, ins.top, this);
			NyARGrayscaleRaster gs=new NyARGrayscaleRaster(20,15);
			NyARRasterFilter_Rgb2Gs_RgbAve filter=new NyARRasterFilter_Rgb2Gs_RgbAve(i_raster.getBufferType());
			filter.doCutFilter(i_raster, 20,15,8,gs);
						
			NyARRasterImageIO.copy(gs, sink20x15);
			g.drawImage(sink20x15, ins.left, ins.top+240, this);
			
			
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

/**
 * @todo
 * 矩形の追跡は動いてるから、位置予測機能と組み合わせて試すこと。
 *
 */
/*
public class TreeLabelingTestLabel extends Frame implements JmfCaptureListener,MouseMotionListener
{

	private final String PARAM_FILE = "../Data/camera_para.dat";

	private final static String CARCODE_FILE = "../Data/patt.hiro";

	private static final long serialVersionUID = -2110888320986446576L;

	private JmfCaptureDevice _capture;

	private JmfNyARRaster_RGB _capraster;

	private int W = 320;

	private int H = 240;

	private ParcialSquareDetector _tr;

	public TreeLabelingTestLabel() throws NyARException
	{
		setTitle("JmfCaptureTest");
		Insets ins = this.getInsets();
		this.setSize(1024 + ins.left + ins.right, 768 + ins.top + ins.bottom);
		JmfCaptureDeviceList dl = new JmfCaptureDeviceList();
		this._capture = dl.getDevice(0);
		if (!this._capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_RGB, W, H, 30.0f)) {
			if (!this._capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_YUV, W, H, 30.0f)) {
				throw new NyARException("キャプチャフォーマットが見つかりません。");
			}
		}
		NyARParam ar_param = new NyARParam();
		ar_param.loadARParamFromFile(PARAM_FILE);
		ar_param.changeScreenSize(W, H);

		NyARCode code = new NyARCode(16, 16);
		code.loadARPattFromFile(CARCODE_FILE);
		this._capraster = new JmfNyARRaster_RGB(ar_param, this._capture.getCaptureFormat());
		this._capture.setOnCapture(this);

		addMouseMotionListener(this);
		this._tr=new ParcialSquareDetector(W,H,3,this._capraster.getBufferType());

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

	public void draw(JmfNyARRaster_RGB i_raster)
	{
		try {
			Insets ins = this.getInsets();
			Graphics g = getGraphics();
			this._tr.detectOutline(i_raster,110);
			
			
				
			BufferedImage sink = new BufferedImage(i_raster.getWidth(), i_raster.getHeight(), ColorSpace.TYPE_RGB);
			BufferedImage sink160x120 = new BufferedImage(160,120, ColorSpace.TYPE_RGB);
			BufferedImage sink80x60 = new BufferedImage(80,60, ColorSpace.TYPE_RGB);
			this.g2=sink.getGraphics();
			NyARRasterImageIO.copy(i_raster, sink);
			for(int i=this._tr.ls.getLength()-1;i>=0;i--){
				ParcialSquareDetector.LabelInfo rect=this._tr.ls.getItem(i);
				g2.drawRect(rect.l,rect.t,rect.w,rect.h);
				
			}
			g.drawImage(sink, ins.left, ins.top, this);
			NyARGrayscaleRaster gs=new NyARGrayscaleRaster(80,60);
			NyARRasterFilter_Rgb2Gs_RgbAve filter=new NyARRasterFilter_Rgb2Gs_RgbAve(i_raster.getBufferType());
			filter.doCutFilter(i_raster, 160,120,2,gs);
						
			NyARRasterImageIO.copy(gs, sink80x60);
			g.drawImage(sink80x60, ins.left, ins.top+240, this);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onUpdateBuffer(Buffer i_buffer)
	{
		try {

			{// ピックアップ画像の表示
				// 矩形抽出
				synchronized(this._capraster){
					this._capraster.setBuffer(i_buffer);
					draw(this._capraster);
				}
			}
		} catch (Exception e) {
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

	public void startImage()
	{
		try {
			// 試験イメージの読み出し(320x240 BGRAのRAWデータ)
			File f = new File(data_file);
			FileInputStream fs = new FileInputStream(data_file);
			byte[] buf = new byte[(int) f.length() * 4];
			fs.read(buf);
//			INyARRgbRaster ra = NyARRgbRaster_BGRA.wrap(buf, W, H);
//			draw(ra);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args)
	{

		try {
			TreeLabelingTestLabel mainwin = new TreeLabelingTestLabel();
			mainwin.setVisible(true);
			mainwin.startCapture();
			// mainwin.startImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
 
 */
