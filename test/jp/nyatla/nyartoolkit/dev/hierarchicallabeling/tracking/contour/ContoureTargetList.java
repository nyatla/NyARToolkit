package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.*;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetList.IgnoreTargetItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetSrc.NyARIgnoreSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList.NewTargetItem;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8;
/**
 * 輪郭ソースから、輪郭を得るんだけど・・・・・。輪郭の特徴って何だ？
 * 
 * @author nyatla
 *
 */
public class ContoureTargetList extends NyARObjectStack<ContoureTargetList.ContoureTargetItem>
{
	public static class ContoureTargetItem extends TrackTarget
	{
		/**
		 * この要素が、十分なデータを持っているかを返します。このプロパティは将来削除するかも？
		 */
		public boolean enable;
		public AreaDataPool.AreaDataItem area;
		public ContourDataPool.ContourTargetSrcItem contoure;
		/**
		 * 輪郭線の中心距離を使った一致判定
		 * @param i_item
		 * @return
		 */
		public boolean isMatchContoure(ContoureTargetSrc.ContoureTargetSrcItem i_item)
		{
			//輪郭中心地の距離2乗
			int d2=NyARMath.sqNorm(i_item.contour_src.coord_center,this.contoure.coord_center);
			int max_dist=i_item.area_src.area_sq_diagonal/100;//(10%)の2乗
			//輪郭線の中央位置を比較して、一定の範囲内であれば、同じ対象の可能性があると判断する。
			if(d2>max_dist){
				return false;//範囲外
			}
			return true;
		}
		public void upgrade(IgnoreTargetItem o_output)
		{
			assert(o_output!=null);
			assert(o_output.ref_area!=null);
			
			o_output.age=0;
			o_output.last_update=o_output.last_update;
			o_output.serial=o_output.serial;
			o_output.tag=o_output.tag;
			o_output.ref_area=this.area;
			if(this.contoure!=null){
				this.contoure.deleteMe();
				this.contoure=null;
			}
			this.area=null;
			this.enable=false;
		}
		public void terminate()
		{
			if(this.contoure!=null){
				this.contoure.deleteMe();
				this.contoure=null;
			}
			this.area.deleteMe();
			this.area=null;			
		}
	}	
	public ContoureTargetList(int i_size) throws NyARException
	{
		super.initInstance(i_size,ContoureTargetItem.class);
	}
	protected ContoureTargetItem createElement() throws NyARException
	{
		return new ContoureTargetItem();
	}
	/**
	 * srcの内容でターゲットを更新します。
	 * @param i_index
	 * @param i_tick
	 * @param i_src
	 */
	public void updateTarget(int i_index,long i_tick,ContoureTargetSrc.ContoureTargetSrcItem i_src)
	{
		ContoureTargetItem item=this._items[i_index];
		item.age++;
		item.last_update=i_tick;
		item.enable=true;
		i_src.attachToTarget(item);
		return;
	}
	public int getMatchTargetIndex(ContoureTargetSrc.ContoureTargetSrcItem i_item)
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
	public int getMatchTargetIndex(AreaDataPool.AreaDataItem i_item)
	{
		AreaDataPool.AreaDataItem iitem;
		//許容距離誤差の2乗を計算(50%)
		//(Math.sqrt((i_item.area.w*i_item.area.w+i_item.area.h*i_item.area.h))/2)^2
		int dist_rate2=(i_item.area.w*i_item.area.w+i_item.area.h*i_item.area.h)/4;

		for(int i=this._length-1;i>=0;i--)
		{
			iitem=this._items[i].area;
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
