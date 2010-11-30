package jp.nyatla.nyartoolkit.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.*;
import jp.nyatla.nyartoolkit.rpf.sampler.lrlabel.*;

public class NyARTargetList extends NyARPointerStack<NyARTarget>
{
	public NyARTargetList(int i_max_target) throws NyARException
	{
		super.initInstance(i_max_target,NyARTarget.class);
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

		int ret=-1;
		int min_d=Integer.MAX_VALUE;

		//対角範囲の距離が、対角距離の1/2以下で、最も小さいこと。
		for(int i=this._length-1;i>=0;i--)
		{
			iitem=this._items[i];
			int d;
			d=i_item.base_area.sqDiagonalPointDiff(iitem._sample_area);	
			if(d<min_d){
				min_d=d;
				ret=i;
			}
		}
		//許容距離誤差の2乗を計算(対角線の20%以内)
		//(Math.sqrt((i_item.area.w*i_item.area.w+i_item.area.h*i_item.area.h))/5)^2
		if(min_d<(2*(i_item.base_area_sq_diagonal)/25)){
			return ret;
		}
		return -1;
	}
}
