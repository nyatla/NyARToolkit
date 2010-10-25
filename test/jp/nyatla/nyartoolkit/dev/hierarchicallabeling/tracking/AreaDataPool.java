package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.HierarchyRect;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetSrc;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARObjectPool;



public class AreaDataPool extends NyARObjectPool<AreaDataPool.AreaDataItem>
{
	public static class AreaDataItem
	{
		public NyARIntRect    area  =new NyARIntRect();
		public NyARIntPoint2d area_center=new NyARIntPoint2d();
		/**
		 * エリア矩形の対角距離の2乗値
		 */
		public int area_sq_diagonal;
		//制御部
		private AreaDataPool _pool;
		public AreaDataItem(AreaDataPool i_pool)
		{
			this._pool=i_pool;
		}
		/**
		 * このインスタンスを開放します。
		 */
		public void deleteMe()
		{
			this._pool.deleteObject(this);
		}
	}
	protected AreaDataPool.AreaDataItem createElement() throws NyARException
	{
		return new AreaDataItem(this);
	}
	public AreaDataPool(int i_length) throws NyARException
	{
		super.initInstance(i_length, AreaDataPool.AreaDataItem.class);
	}
	public AreaDataPool.AreaDataItem newSrcTarget(HierarchyRect i_imgmap,NyARRleLabelFragmentInfo info)
	{
		AreaDataPool.AreaDataItem item=this.newObject();
		if(item==null){
			return null;
		}
		int skip=i_imgmap.dot_skip;
		item.area.x=info.clip_l*skip+i_imgmap.x;
		item.area.y=info.clip_t*skip+i_imgmap.y;
		item.area.w=(info.clip_r-info.clip_l)*skip;
		item.area.h=(info.clip_b-info.clip_t)*skip;
		item.area_center.x=item.area.x+item.area.w/2;
		item.area_center.y=item.area.y+item.area.h/2;
		item.area_sq_diagonal=item.area.w*item.area.w+item.area.h*item.area.h;
		return item;
	}	
}
