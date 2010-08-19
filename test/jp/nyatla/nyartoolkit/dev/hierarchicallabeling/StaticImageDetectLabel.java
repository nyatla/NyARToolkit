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
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContourTargetList;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContourTargetList.ContourTargetItem;
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
	private final NyARContourPickup _cpickup=new NyARContourPickup();
	private NyARIntPoint2d[] _coord;


	
	
	
	public MyDetector(int i_width,int i_height,int i_depth,int i_raster_type) throws NyARException
	{
		super(i_width,i_height,i_depth,i_raster_type);
		this._coord=NyARIntPoint2d.createArray((i_width+i_height)*2);
		this._newtargetsrc=new NewTargetSrc(10);
		this._newtarget=new NewTargetList(10);
		this._ignoretargetsrc=new IgnoreTargetSrc(10);
		this._ignoretarget=new IgnoreTargetList(10);
		this._contouretarget= new ContourTargetList(10);
		//データソースの準備
		for(int i=0;i<2;i++){
			this._area_holders[i]=new AreaTargetSrcHolder(100);
			this._contoure_holders[i]=new ContourTargetSrcHolder(10,i_width+i_height*2);
		}
		this._current_holder_page=0;
	}
	public void detectOutline(NyARGrayscaleRaster i_raster,int i_th) throws NyARException
	{
		//Holderのページを設定
		this._current_holder_page=(this._current_holder_page+1)%2;
		this._current_areaholder=this._area_holders[this._current_holder_page];
		this._current_contoureholder=this._contoure_holders[this._current_holder_page];
		//リストの初期化
		this._current_areaholder.clear();
		this._current_contoureholder.clear();
		this._ignoretargetsrc.clear();
		this._newtargetsrc.clear();
		this._contouretarget.clear();

		this._base_gs=i_raster;
		//Srcを編集
		super.detectOutline(i_raster,i_th);
		//アップグレード処理
		//New->Ignore(100フレーム無視が続いた場合)
		
	}
	NyARGrayscaleRaster _base_gs;

	/**
	 * 2ページ分のソースデータホルダ
	 */
	public AreaTargetSrcHolder[] _area_holders=new AreaTargetSrcHolder[2];
	public ContourTargetSrcHolder[] _contoure_holders=new ContourTargetSrcHolder[2];
	
	public AreaTargetSrcHolder _current_areaholder;
	public ContourTargetSrcHolder _current_contoureholder;
	public int _current_holder_page;

	
	
	NewTargetSrc _newtargetsrc;
	NewTargetList _newtarget;
	
	IgnoreTargetSrc _ignoretargetsrc;
	IgnoreTargetList _ignoretarget;
	
	ContourTargetList _contouretarget;
	
	
	
	
	protected void onLabelFound(HierarchyRect i_imgmap,NyARGrayscaleRaster i_raster,int i_th,NyARRleLabelFragmentInfo info) throws NyARException
	{
		//領域ソースホルダに追加
		AreaTargetSrcHolder.AppearSrcItem item=this._current_areaholder.pushSrcTarget(i_imgmap, info);
		if(item==null){
			return;
		}
		int match_index;

		
		//ログ付き輪郭トラッキング対象か確認する。
		match_index=this._contouretarget.getMatchTargetIndex(item);
		if(match_index>=0){
			//輪郭ホルダに追加
			ContourTargetSrcHolder.ContourTargetSrcItem contour_item=this._current_contoureholder.pushTarget(item, i_imgmap, i_raster, i_th, info);
			//対象になる輪郭ソースに追加	
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
	

	

	private NewTracking _newtracking=new NewTracking(10);
	private IgnoreTracking _ignoretrack=new IgnoreTracking(10);
	static long tick;
	MyDetector _psd=new MyDetector(320,240,4,NyARBufferType.BYTE1D_R8G8B8_24);
    public void update(Graphics g,BufferedImage buf)
    {
    	tick++;
		try {
			
			Insets ins = this.getInsets();
			INyARRgbRaster ra =new NyARRgbRaster_RGB(320,240);
			NyARRasterImageIO.copy(buf,ra);
			//GS値化
			NyARGrayscaleRaster gs=new NyARGrayscaleRaster(320,240);
			NyARGrayscaleRaster ro=new NyARGrayscaleRaster(320,240);
			NyARRasterFilter_Rgb2Gs_RgbAve filter=new NyARRasterFilter_Rgb2Gs_RgbAve(ra.getBufferType());
			filter.doFilter(ra,gs);
			//

			MyDetector psd=this._psd;
			//GS画像の描画
			BufferedImage sink = new BufferedImage(ra.getWidth(), ra.getHeight(), ColorSpace.TYPE_RGB);
			NyARRasterImageIO.copy(gs, sink);
			
//			psd._appeartargetsrc.clear();
//			psd._newtargetsrc.clear();
//			psd._ignoretargetsrc.clear();
			//検出とデータソースの確定
			psd.detectOutline(gs,30);
			//トラッキング
			this._newtracking.updateTrackTargetBySrc(tick,psd._newtargetsrc,psd._newtarget);
			this._ignoretrack.updateTrackTargetBySrc(tick,psd._ignoretargetsrc,psd._ignoretarget);
			
			//アップデート処理
			
			
			//newtarget->ignore,coord
			for(int i=psd._newtargetsrc.getLength()-1;i>=0;i--)
			{
				//newtarget->ignoreの昇格処理(ageが100超えてもなにもされないならignore)
				if(psd._newtarget.getItem(i).age>100){
					IgnoreTarget ig=psd._ignoretarget.pushTarget(psd._newtarget.getItem(i));
					if(ig==null){
						//失敗リストがいっぱいなら何もしない
						continue;
					}
					psd._newtarget.removeIgnoreOrder(i);
					continue;
				}
				//newtarget->coordの昇格処理(coordに空きがあれば昇格)
				ContourTargetItem ct=psd._contouretarget.pushTarget(psd._newtarget.getItem(i));
				if(ct==null){
					//失敗リストがいっぱいなら何もしない
					continue;
				}
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
			
			//ログなしからログありへの昇格
			
			
	/*		
			TestL te=new TestL(320,240);
			te.g=psd.g;
			d2 = new Date();
			for (int i = 0; i < 1000; i++) {
				te.labeling(gs,50);
			}
			d = new Date();
			System.out.println("L="+(d.getTime() - d2.getTime()));
			
	*/		
			
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
*/
			//
			{
				Graphics g2=sink.getGraphics();
				//無視リスト描画
				g2.setColor(Color.blue);
				for(int i=0;i<this._psd._ignoretarget.getLength();i++){
					NewTargetItem e=this._psd._newtarget.getItem(i);
					g2.drawRect(e.area.x, e.area.y, e.area.w, e.area.h);
				}
				//新規リスト描画
				g2.setColor(Color.red);
				for(int i=0;i<this._psd._newtarget.getLength();i++){
					
					NewTargetItem e=this._psd._newtarget.getItem(i);
					g2.drawRect(e.area.x, e.area.y, e.area.w, e.area.h);
				}
				sink.setRGB((int)pos.x,(int)pos.y,0xff0000);
				//輪郭リスト描画
				g2.setColor(Color.green);
				for(int i=0;i<this._psd._contouretarget.getLength();i++){
					
					ContourTargetItem e=this._psd._contouretarget.getItem(i);
					GraphicsTools.drawPolygon(g2,e., i_pt, i_number_of_pt)
					g2.drawPolygon(xPoints, yPoints, nPoints);
					g2.drawRect(e.area.x, e.area.y, e.area.w, e.area.h);
				}
				sink.setRGB((int)pos.x,(int)pos.y,0xff0000);
				
				
				g.drawImage(sink, ins.left, ins.top, this);
			}
//			g.drawImage(sink,ins.left,ins.top+240,ins.left+32,ins.top+240+32,mx,my,mx+8,my+8,this);
//
//			//RO画像の描画
//			NyARRasterImageIO.copy(ro, sink);
//			g.drawImage(sink,ins.left+320,ins.top+240,ins.left+32+320,ins.top+240+32,this);

		} catch (Exception e) {
			e.printStackTrace();
		}
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
