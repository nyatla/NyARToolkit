package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AppearTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.TrackTarget;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AppearTargetSrc.AppearSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetList.IgnoreTarget;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetSrc.NyARIgnoreSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList.NewTargetItem;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8;
class VectorPos
{
	int x;
	int y;
	int dx;
	int dy;
	public static VectorPos[] createArray(int i_length)
	{
		VectorPos[] r=new VectorPos[i_length];
		for(int i=0;i<i_length;i++){
			r[i]=new VectorPos();
		}
		return r;
	}
}
/**
 * 輪郭ソースから、輪郭を得るんだけど・・・・・。輪郭の特徴って何だ？
 * 
 * @author nyatla
 *
 */
public class ContourTargetList extends NyARObjectStack<ContourTargetList.ContourTargetItem>
{
	public static class ContourTargetItem extends TrackTarget
	{
		public NyARIntRect area=new NyARIntRect();
		public NyARIntPoint2d area_center=new NyARIntPoint2d();
		public int area_sq_diagonal;
		//
		public NyARIntPoint2d coord_center=new NyARIntPoint2d();
		public boolean isMatchContoure(ContourTargetSrc.ContourTargetSrcItem i_item)
		{
			//輪郭中心地の距離2乗
			int d2=NyARMath.sqNorm(i_item.coord_center,coord_center);
			int max_dist=i_item.area_sq_diagonal/100;//(10%)の2乗
			//輪郭線の中央位置を比較して、一定の範囲内であれば、同じ輪郭線であるとする。
			if(d2>max_dist){
				return false;//範囲外
			}
			return true;
		}
		
	}
	public ContourTargetList(int i_size) throws NyARException
	{
		super.initInstance(i_size,ContourTargetItem.class);
	}
	protected ContourTargetItem createElement() throws NyARException
	{
		return new ContourTargetItem();
	}	
	public ContourTargetItem pushTarget(NewTargetItem i_item)
	{
		ContourTargetItem item=this.prePush();
		if(item==null){
			return null;
		}
		item.age=0;
		item.last_update=i_item.last_update;
		item.serial=i_item.serial;
		item.tag=i_item.tag;
		item.area.x=i_item.area.x;
		item.area.y=i_item.area.y;
		item.area.w=i_item.area.w;
		item.area.h=i_item.area.h;
		item.area_center.x=i_item.area_center.x;
		item.area_center.y=i_item.area_center.y;
		item.area_sq_diagonal=i_item.area_sq_diagonal;
		return item;
	}

	public void updateTarget(INyARRaster i_raster,int i_index,long i_tick,ContourTargetSrc.ContourTargetSrcItem i_src)
	{
		ContourTargetItem item=this._items[i_index];
		item.age++;
		item.last_update=i_tick;
		item.area.x=i_src.area.x;
		item.area.y=i_src.area.y;
		item.area.w=i_src.area.w;
		item.area.h=i_src.area.h;
		item.area_center.x=i_src.area_center.x;
		item.area_center.y=i_src.area_center.y;
		item.coord_center.x=i_src.coord_center.x;
		item.coord_center.y=i_src.coord_center.y;
		
		//
		int n;
		VectorPos[] vp=VectorPos.createArray(10);
		NyARIntRect tmprect=new NyARIntRect();
		

		//輪郭線を出す
//		n=this._cpickup.getContour(i_raster,i_th,info.entry_x,info.clip_t,this._coord);
		//元画像からベクトルを拾う。
		NyARVectorReader_INT1D_GRAY_8 reader=new NyARVectorReader_INT1D_GRAY_8(i_raster);
		int skip=i_src.skip;
		tmprect.w=skip*2;
		tmprect.h=skip*2;

		//ベクトル配列を作る

		int number_of_data=1;
		//0個目のベクトル
		tmprect.x=i_src.image_lt.x+(i_src.coord[0].x-1)*skip;
		tmprect.y=i_src.image_lt.y+(i_src.coord[0].y-1)*skip;
		reader.getAreaVector8(tmprect,pva[0]);
		//ベクトルデータを作成
		for(int i=1;i<n;i++){
//ベクトル定義矩形を作る。
			tmprect.x=i_src.image_lt.x+(i_src.coord[i].x-1)*skip;
			tmprect.y=i_src.image_lt.y+(i_src.coord[i].y-1)*skip;
//矩形の位置をずらさないとね
//クリップ位置の補正
			//ベクトル取得
			reader.getAreaVector8(tmprect,pva[number_of_data]);
			g.fillRect((int)pva[number_of_data].x,(int)pva[number_of_data].y,1,1);
			
			//類似度判定
			if(getVecCos(pva[number_of_data-1],pva[number_of_data])<0.99){
				//相関なし
				number_of_data++;
			}else{
				//相関あり
				pva[number_of_data-1].x=(pva[number_of_data-1].x+pva[number_of_data].x)/2;
				pva[number_of_data-1].y=(pva[number_of_data-1].y+pva[number_of_data].y)/2;
				pva[number_of_data-1].dx=(pva[number_of_data-1].dx+pva[number_of_data].dx);
				pva[number_of_data-1].dy=(pva[number_of_data-1].dy+pva[number_of_data].dy);
			}
		}
		//ベクトルの描画
		for(int i=0;i<number_of_data;i++){
			double sin=pva[i].dy/Math.sqrt(pva[i].dx*pva[i].dx+pva[i].dy*pva[i].dy);
			double cos=pva[i].dx/Math.sqrt(pva[i].dx*pva[i].dx+pva[i].dy*pva[i].dy);
			double l=Math.sqrt(pva[i].dx*pva[i].dx+pva[i].dy*pva[i].dy)/16;
			g.setColor(Color.BLUE);
			g.drawLine((int)pva[i].x,(int)pva[i].y,(int)(pva[i].x+l*cos),(int)(pva[i].y+l*sin));				
		}		
		
		
		return;
	}	
	public int getMatchTargetIndex(ContourTargetSrc.ContourTargetSrcItem i_item)
	{
		for(int i=this._length-1;i>=0;i--)
		{
			if(this._items[i].isMatchContoure(i_item)){
				return i;
			}
		}
		return -1;
	}
	/**
	 * 一致する矩形を検索する。一致する矩形の判定は、Areaの重なり具合
	 * @param i_item
	 * @return
	 */
	public int getMatchTargetIndex(AppearTargetSrc.AppearSrcItem i_item)
	{
		ContourTargetItem iitem;
		//許容距離誤差の2乗を計算(50%)
		//(Math.sqrt((i_item.area.w*i_item.area.w+i_item.area.h*i_item.area.h))/2)^2
		int dist_rate2=(i_item.area.w*i_item.area.w+i_item.area.h*i_item.area.h)/4;

		for(int i=this._length-1;i>=0;i--)
		{
			iitem=this._items[i];
			//大きさチェック(50%誤差)
			double ratio;
			ratio=((double)iitem.area.w)/i_item.area.w;
			if(ratio<0.25 || 2.25<ratio){
				continue;
			}
			//距離チェック
			int d=NyARMath.sqNorm(i_item.area_center,iitem.area_center);
			if(d>dist_rate2)
			{
				continue;
			}
			//多分同じ対象物
			return i;
		}
		return -1;
	}

}
