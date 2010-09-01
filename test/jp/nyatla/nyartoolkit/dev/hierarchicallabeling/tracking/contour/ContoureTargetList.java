package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ContourTargetSrcPool;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.TrackTarget;
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
		public AreaTargetSrcPool.AreaTargetSrcItem area;
		public ContourTargetSrcPool.ContourTargetSrcItem contoure;
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
		public void giveData(IgnoreTargetItem o_output)
		{
			o_output.ref_area=this.area;
			this.area=null;
		}
	}
	private AreaTargetSrcPool _ref_area_pool;
	private ContourTargetSrcPool _ref_contoure_pool;
	
	public ContoureTargetList(int i_size,AreaTargetSrcPool i_area_pool,ContourTargetSrcPool i_contoure_pool) throws NyARException
	{
		this._ref_area_pool=i_area_pool;
		this._ref_contoure_pool=i_contoure_pool;
		super.initInstance(i_size,ContoureTargetItem.class);
	}
	protected ContoureTargetItem createElement() throws NyARException
	{
		return new ContoureTargetItem();
	}
	/**
	 * NewTargetから、ContourTargetへの昇格に使います。昇格直後は一部のパラメータが不定です。
	 * @param i_item
	 * @return
	 */
	public ContoureTargetItem upgradeTarget(NewTargetItem i_item)
	{
		ContoureTargetItem item=this.prePush();
		if(item==null){
			return null;
		}
		item.age=0;
		item.last_update=i_item.last_update;
		item.serial=i_item.serial;
		item.tag=i_item.tag;

		//areaの委譲
		i_item.giveData(o_output)
		item.area   =i_item.ref_area;
		i_item.ref_area=null;
		//countoureは委譲元が存在しないので、nullを指定する。
		item.contoure=null;
		item.enable=false;
		return item;
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
		
		i_src.giveData(item);
		item.enable=true;
		return;
	}
	public void deleteTarget(int i_index)
	{
		if(this._items[i_index].area!=null){
			this._ref_contoure_pool.deleteObject(this._items[i_index].contoure);
		}
		if(this._items[i_index].contoure!=null){
			this._ref_contoure_pool.deleteObject(this._items[i_index].contoure);
		}
		super.removeIgnoreOrder(i_index);
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
	public int getMatchTargetIndex(AreaTargetSrcPool.AreaTargetSrcItem i_item)
	{
		AreaTargetSrcPool.AreaTargetSrcItem iitem;
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
