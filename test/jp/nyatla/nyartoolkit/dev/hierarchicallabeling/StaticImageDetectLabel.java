package jp.nyatla.nyartoolkit.dev.hierarchicallabeling;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.Date;

import javax.imageio.ImageIO;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARLabeling_Rle;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
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
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcHolder.AreaSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContoureTargetList;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContoureTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContoureTargetList.ContoureTargetItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetList;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTracking;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTracking;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList.NewTargetItem;



class MyDetector extends HierarchyLabeling
{
	public Graphics g;
	
	public MyDetector(int i_width,int i_height,int i_depth,int i_raster_type) throws NyARException
	{
		super(i_width,i_height,i_depth,i_raster_type);
		//ソースターゲット
		this._newtargetsrc=new NewTargetSrc(10, this._area_holder);
		this._ignoretargetsrc=new IgnoreTargetSrc(10, this._area_holder);
		this._entersrc=new EnterTargetSrc(10, this._area_holder);
		//トラッキングターゲット
		this._newtarget=new NewTargetList(10, this._area_holder);
		this._ignoretarget=new IgnoreTargetList(10, this._area_holder);
		this._contouretarget= new ContoureTargetList(10, this._area_holder, this._contoure_holder);
		
		
		//データソースの準備
		this._area_holder=new AreaTargetSrcHolder(100);
		this._contoure_holder=new ContourTargetSrcHolder(100,i_width+i_height*2);
		
	}
	public void detectOutline(NyARGrayscaleRaster i_raster,int i_th) throws NyARException
	{
		//Holderのページを設定


		this._base_gs=i_raster;
		//Srcを編集
		super.detectOutline(i_raster,i_th);
		
	}
	NyARGrayscaleRaster _base_gs;


	public AreaTargetSrcHolder _area_holder;
	public ContourTargetSrcHolder _contoure_holder;
	

	
	public EnterTargetSrc _entersrc;
	
	NewTargetSrc _newtargetsrc;
	NewTargetList _newtarget;
	
	IgnoreTargetSrc _ignoretargetsrc;
	IgnoreTargetList _ignoretarget;
	
	ContoureTargetList _contouretarget;
	ContoureTargetSrc _contouretargetsrc;
	
	
	
	
	protected void onLabelFound(HierarchyRect i_imgmap,NyARGrayscaleRaster i_raster,int i_th,NyARRleLabelFragmentInfo info) throws NyARException
	{
		//領域ソースホルダに追加
		AreaTargetSrcHolder.AreaSrcItem item=this._area_holder.newSrcTarget(i_imgmap, info);
		if(item==null){
			return;
		}
		int match_index;

		
		//ログ付き輪郭トラッキング対象か確認する。
		match_index=this._contouretarget.getMatchTargetIndex(item);
		if(match_index>=0){
			//輪郭ホルダに追加
			ContourTargetSrcHolder.ContourTargetSrcItem contour_item=this._contoure_holder.newSrcTarget(item, i_imgmap, i_raster, i_th, info);
			//対象になる輪郭ソースに追加	
			if(match_index>=0){
				this._contouretargetsrc.pushSrcTarget(contour_item);
			}
			return;
		}
		//ログ付き認識待ち対象
		match_index=this._newtarget.getMatchTargetIndex(item);
		if(match_index>=0){
			this._newtargetsrc.pushSrcTarget(item);
			return;
		}
		//ログ付き無視対象であるか確認する。
		match_index=this._ignoretarget.getMatchTargetIndex(item);
		if(match_index>=0){
			this._ignoretargetsrc.pushSrcTarget(item);
			return;
		}
		//残りはすべて、ログなし対象
		if(this._entersrc.pushSrcTarget(item)==null){
			//管理できなければオブジェクトを開放
			this._area_holder.deleteObject(item);
		}
	}

	
}







/**
 * @todo
 * 矩形の追跡は動いてるから、位置予測機能と組み合わせて試すこと。
 *
 */

public class StaticImageDetectLabel extends Frame implements MouseMotionListener
{
	class TestL extends NyARLabeling_Rle
	{
		Graphics g;

		public TestL(int i_width,int i_height) throws NyARException
		{
			super(i_width,i_height);
		}
		protected void onLabelFound(NyARRleLabelFragmentInfo i_label)
		{
			g.setColor(Color.red);
			g.drawRect(
					i_label.clip_l,
					i_label.clip_t,
					(i_label.clip_r-i_label.clip_l),
					(i_label.clip_b-i_label.clip_t));			
		}
	}

	private final static String SAMPLE_FILES = "../Data/test.jpg";

	private static final long serialVersionUID = -2110888320986446576L;


	private int W = 320;
	private int H = 240;
	BufferedImage _src_image;

	public StaticImageDetectLabel() throws NyARException, Exception
	{
//		setTitle("Estimate Edge Sample");
		Insets ins = this.getInsets();
		this.setSize(1024 + ins.left + ins.right, 768 + ins.top + ins.bottom);

		_src_image = ImageIO.read(new File(SAMPLE_FILES));
		addMouseMotionListener(this);

		//トラッカー
		this._newtracking=new NewTracking(10);
		this._ignoretrack=new IgnoreTracking(10);


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
	
	private NewTracking _newtracking;
	private IgnoreTracking _ignoretrack;

	

	static long tick;
	MyDetector _psd=new MyDetector(320,240,4,NyARBufferType.BYTE1D_R8G8B8_24);
    public void update(Graphics g,BufferedImage buf)
    {
    	tick++;
		try {
			
			INyARRgbRaster ra =new NyARRgbRaster_RGB(320,240);
			NyARRasterImageIO.copy(buf,ra);
			//GS値化
			NyARGrayscaleRaster gs=new NyARGrayscaleRaster(320,240);
			NyARRasterFilter_Rgb2Gs_RgbAve filter=new NyARRasterFilter_Rgb2Gs_RgbAve(ra.getBufferType());
			filter.doFilter(ra,gs);
			//

			MyDetector psd=this._psd;
			//GS画像の描画
			BufferedImage sink = new BufferedImage(ra.getWidth(), ra.getHeight(), ColorSpace.TYPE_RGB);
			NyARRasterImageIO.copy(gs, sink);
			

			//検出とデータソースの確定
			psd.detectOutline(gs,30);
			//トラッキング
			
			this._newtracking.updateTrackTargetBySrc(tick,psd._newtargetsrc,psd._newtarget);
			this._ignoretrack.updateTrackTargetBySrc(tick,psd._ignoretargetsrc,psd._ignoretarget);
			
			//ターゲット処理
			handler(g,sink);
			
			//昇格処理
			
			
			//newtarget->ignore,coord
			for(int i=psd._newtargetsrc.getLength()-1;i>=0;i--)
			{
				//newtarget->ignoreの昇格処理(ageが100超えてもなにもされないならignore)
				if(psd._newtarget.getItem(i).age>100){
					IgnoreTargetList.IgnoreTargetItem ig=psd._ignoretarget.upgradeTarget(psd._newtarget.getItem(i));
					if(ig==null){
						//失敗リストが埋まっていたら何もしない
						continue;
					}
					psd._newtarget.deleteTarget(i);
					continue;
				}
				//newtarget->coordの昇格処理(coordに空きがあれば昇格)
				ContoureTargetList.ContoureTargetItem ct=psd._contouretarget.upgradeTarget(psd._newtarget.getItem(i));
				if(ct==null){
					//失敗リストが埋まっていたら何もしない。
					continue;
				}
				psd._newtarget.deleteTarget(i);
			}
			//coord->ignore,rect
			for(int i=psd._contouretarget.getLength()-1;i>=0;i--)
			{
				//coordのrect判定が失敗したらignoreへ
				//coordのrect判定が成功したらrectへ
			}
			//rect->Marker
			for(int i=psd._contouretarget.getLength()-1;i>=0;i--)
			{
//rectのmarker判定が成功したらmarkerへ
//rectのmarker判定が失敗したらignoreへ
			}
			
			//入力待ちソースからの入力
			for(int i=psd._entersrc.getLength()-1;i>=0;i--)
			{
				if(psd._newtarget.pushTarget(0,psd._entersrc.getItem(i))==null){
					break;
				}
			}
			
			//ソースホルダの解放
			//リストの初期化
			psd._contouretargetsrc.clear();
			psd._ignoretargetsrc.clear();
			psd._newtargetsrc.clear();
			psd._entersrc.clear();			
			//リストの初期化
//			this._current_areaholder.clear();
			
//			g.drawImage(sink,ins.left,ins.top+240,ins.left+32,ins.top+240+32,mx,my,mx+8,my+8,this);
//
//			//RO画像の描画
//			NyARRasterImageIO.copy(ro, sink);
//			g.drawImage(sink,ins.left+320,ins.top+240,ins.left+32+320,ins.top+240+32,this);

		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    private void handler(Graphics g,BufferedImage sink)
    {
		Insets ins = this.getInsets();
		
		

		
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
/*			if(mx>0 && my>0){
			reader.getAreaVector8(tmprect,pos,vec);
		}
		//分析結果を描画
		double sin=vec.y/Math.sqrt(vec.x*vec.x+vec.y*vec.y);
		double cos=vec.x/Math.sqrt(vec.x*vec.x+vec.y*vec.y);
		Graphics g2=sink.getGraphics();
		g2.setColor(Color.BLUE);
		g2.drawLine((int)pos.x,(int)pos.y,(int)(pos.x+30*cos),(int)(pos.y+30*sin));
*/
		//
		{
			Graphics g2=sink.getGraphics();
			//無視リスト描画
			g2.setColor(Color.blue);
			for(int i=0;i<this._psd._ignoretarget.getLength();i++){
				AreaSrcItem e=this._psd._newtarget.getItem(i).ref_area;
				g2.drawRect(e.area.x, e.area.y, e.area.w, e.area.h);
				g2.drawString("IGN",e.area.x, e.area.y);
			}
			//新規リスト描画
			g2.setColor(Color.red);
			for(int i=0;i<this._psd._newtarget.getLength();i++){
				
				AreaSrcItem e=this._psd._newtarget.getItem(i).ref_area;
				g2.drawRect(e.area.x, e.area.y, e.area.w, e.area.h);
				g2.drawString("NEW",e.area.x, e.area.y);
			}
			sink.setRGB((int)pos.x,(int)pos.y,0xff0000);
			//輪郭リスト描画
			g2.setColor(Color.green);
			for(int i=0;i<this._psd._contouretarget.getLength();i++){
				//coordTargetを頂点集合に変換
				ContoureTargetList.ContoureTargetItem e=this._psd._contouretarget.getItem(i);
				NyARDoublePoint2d[] ppos=NyARDoublePoint2d.createArray(e.ref_contoure.vecpos_length);
				int[] yp=new int[e.ref_contoure.vecpos_length];
				for(int i2=0;i2<e.ref_contoure.vecpos_length;i2++){
					getCrossPos(e.ref_contoure.vecpos[i2],e.ref_contoure.vecpos[(i2+1)%e.ref_contoure.vecpos_length],ppos[i2]);
				}
				GraphicsTools.drawPolygon(g2,ppos,this._psd._contouretarget.getLength());
				g2.drawRect(e.ref_area.area.x, e.ref_area.area.y, e.ref_area.area.w, e.ref_area.area.h);
			}
			sink.setRGB((int)pos.x,(int)pos.y,0xff0000);
			
			
			g.drawImage(sink, ins.left, ins.top, this);   
		}
    }
    public void getCrossPos(VectorPos vec1,VectorPos vec2,NyARDoublePoint2d o_pos)
    {
    	NyARLinear line1=new NyARLinear();
    	NyARLinear line2=new NyARLinear();
    	line1.setParam(vec1.dx,vec1.dy,vec1.x,vec1.y);
    	line1.orthogonalLine();
    	line2.setParam(vec2.dx,vec2.dy,vec2.x,vec2.y);
    	line2.orthogonalLine();
    	NyARLinear.crossPos(line1,line2, o_pos);
    	
    }

    public void mainloop() throws Exception
    {
    	for(;;){
	    	//画像取得
	//    	this._src_image
	    	//処理
	    	this.update(this.getGraphics(),this._src_image);
	    	Thread.sleep(30);
    	}
    }

	public static void main(String[] args)
	{

		try {
			StaticImageDetectLabel mainwin = new StaticImageDetectLabel();
			mainwin.setVisible(true);
			mainwin.mainloop();
			// mainwin.startImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
}
}
