package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcHolder;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ContourTargetSrcHolder;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.TrackTarget;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.AreaTargetSrcHolder.AppearSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetList.IgnoreTargetItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetSrc.NyARIgnoreSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList.NewTargetItem;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8.NyARDoublePosVec2d;
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
		private AreaTargetSrcHolder.AppearSrcItem area_ref;
		private ContourTargetSrcHolder.ContourTargetSrcItem contour_ref;
		/**
		 * 輪郭線の中心距離を使った一致判定
		 * @param i_item
		 * @return
		 */
		public boolean isMatchContoure(ContourTargetSrcHolder.ContourTargetSrcItem i_item)
		{
			//輪郭中心地の距離2乗
			int d2=NyARMath.sqNorm(i_item.coord_center,this.contour_ref.coord_center);
			int max_dist=i_item._ref_area_src.area_sq_diagonal/100;//(10%)の2乗
			//輪郭線の中央位置を比較して、一定の範囲内であれば、同じ対象の可能性があると判断する。
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
	/**
	 * NewTargetから、ContourTargetへの昇格時に使います。昇格直後は一部のパラメータが不定です。
	 * @param i_item
	 * @return
	 */
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
		item.area_ref   =i_item._ref_area;
		item.contour_ref=null;
		return item;
	}

	public void updateTarget(int i_index,long i_tick,ContourTargetSrcHolder.ContourTargetSrcItem i_src)
	{
		ContourTargetItem item=this._items[i_index];
		item.age++;
		item.last_update=i_tick;
		item.area_ref=i_src._ref_area_src;
		item.contour_ref=i_src;
		return;
	}	
	public int getMatchTargetIndex(ContourTargetSrcHolder.ContourTargetSrcItem i_item)
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
	public int getMatchTargetIndex(AreaTargetSrcHolder.AppearSrcItem i_item)
	{
		AreaTargetSrcHolder.AppearSrcItem iitem;
		//許容距離誤差の2乗を計算(50%)
		//(Math.sqrt((i_item.area.w*i_item.area.w+i_item.area.h*i_item.area.h))/2)^2
		int dist_rate2=(i_item.area.w*i_item.area.w+i_item.area.h*i_item.area.h)/4;

		for(int i=this._length-1;i>=0;i--)
		{
			iitem=this._items[i].area_ref;
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
