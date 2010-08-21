package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget;

import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.TrackingUtils;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.NyARDistMap;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetList.IgnoreTargetItem;

public class IgnoreTracking
{
	private class DistMap extends NyARDistMap
	{
		public DistMap(int i_max_col,int i_max_row)
		{
			super(i_max_col,i_max_row);
		}

		public void setPointDists(IgnoreTargetList.IgnoreTargetItem[] i_vertex_r,int i_row_len,IgnoreTargetSrc.NyARIgnoreSrcItem[] i_vertex_c,int i_col_len)
		{
			NyARDistMap.DistItem[] map=this._map;
			//distortionMapを作成。ついでに最小値のインデクスも取得
			int min_index=0;
			int min_dist =Integer.MAX_VALUE;
			int idx=0;
			for(int r=0;r<i_row_len;r++){
				for(int c=0;c<i_col_len;c++){
					map[idx].col=c;
					map[idx].row=r;
					//中央座標の距離？
					
					int d=TrackingUtils.rectSqNorm(i_vertex_r[r].ref_area.area,i_vertex_c[c].ref_area_src.area);
					map[idx].dist=d;
					if(min_dist>d){
						min_index=idx;
						min_dist=d;
					}
					idx++;
				}
			}
			this._min_dist=min_dist;
			this._min_dist_index=min_index;
			this._size_col=i_col_len;
			this._size_row=i_row_len;
			return;
		}			
	}
	private DistMap _distmap;
	private int[] _rowindex;
	public IgnoreTracking(int i_size)
	{
		this._distmap=new DistMap(i_size,i_size);
		this._rowindex=new int[i_size];
	}
	/**
	 * i_srcの内容で、i_targetを更新する。
	 * @param i_src
	 * @param i_target
	 */
	public void updateTrackTargetBySrc(long i_tick,IgnoreTargetSrc i_src,IgnoreTargetList i_target)
	{
		//tick限界の計算
		long tick_range=i_tick-MISS_TICK_LIMIT;
		//距離マップを作り直して再計算
		int row_len=i_target.getLength();
		this._distmap.setPointDists(i_target.getArray(),row_len,i_src.getArray(),i_src.getLength());
		this._distmap.getMinimumPair(this._rowindex);
		//割り当ててみる。
		for(int i=row_len-1;i>=0;i--){
			int idx=this._rowindex[i];
			IgnoreTargetItem item=i_target.getItem(i);
			if(idx<0){
				//指定tickよりも更新が古ければ、消す。
				if(item.last_update<tick_range){
					i_target.deleteTarget(i);
				}
				continue;
			}else if(item.age>MAX_AGE){
				//ある程度古くなったら消す。
				i_target.deleteTarget(i);
				continue;
			}
			i_target.updateTarget(i, i_tick,i_src.getItem(idx));
		}
	}
	/**
	 * ヒットミスの許容回数
	 */
	private final static int MISS_TICK_LIMIT=10;
	/**
	 * 最大寿命
	 */
	private final static int MAX_AGE=10;
}	