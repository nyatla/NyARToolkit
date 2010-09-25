package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.*;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.NyARDistMap;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.*;

public class NyARTargetList extends NyARPointerStack<NyARTarget>
{
	protected class DistMap extends NyARDistMap
	{
		public DistMap(int i_max_col,int i_max_row)
		{
			super(i_max_col,i_max_row);
		}
		private int sqNorm(NyARTarget i_target,SampleStack.Item i_sample)
		{
			int s=i_sample.ref_sampleout.resolution;
			int cx=i_target.sample_area_center.x-i_sample.ref_sampleout.base_area_center.x*s;
			int cy=i_target.sample_area_center.y-i_sample.ref_sampleout.base_area_center.y*s;
			return cx*cx+cy*cy;
			
		}
		public void setPointDists(NyARTarget[] i_vertex_r,int i_row_len,SampleStack.Item[] i_vertex_c,int i_col_len)
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
					int d=sqNorm(i_vertex_r[r], i_vertex_c[c]);
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
	public NyARTargetList(int i_max_target) throws NyARException
	{
		super.initInstance(i_max_target,NyARTarget.class);
		this._index=new int[i_max_target];
		this._map=new DistMap(i_max_target,i_max_target);
	}
	/**
	 * Sampleの位置キーに一致する可能性の高い要素のインデクスを１つ返します。
	 * @param i_item
	 * @return
	 * 一致する可能性が高い要素のインデクス番号。見つからないときは-1
	 */
	public int getMatchTargetIndex(LowResolutionLabelingSamplerOut.Item i_item)
	{
		NyARTarget iitem;
		//許容距離誤差の2乗を計算(10%)
		//(Math.sqrt((i_item.area.w*i_item.area.w+i_item.area.h*i_item.area.h))/10)^2
		int dist_rate2=(i_item.base_area_sq_diagonal)/100;

		//距離は領域の10%以内の誤差、大きさは10%以内の誤差であること。
		for(int i=this._length-1;i>=0;i--)
		{
			iitem=this._items[i];
			//大きさチェック
			double ratio;
			ratio=((double)iitem.sample_area.w)/i_item.base_area.w;
			if(ratio<0.81 || 1.21<ratio){
				continue;
			}
			//距離チェック
			int d2=NyARMath.sqNorm(i_item.base_area_center,iitem.sample_area_center);
			if(d2>dist_rate2)
			{
				continue;
			}
			//多分同じ対象物
			return i;
		}
		return -1;
	}
	protected DistMap _map;
	protected int[] _index;
}
