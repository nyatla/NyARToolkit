package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.*;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.*;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.*;

public class NyARTargetList<T extends NyARTarget> extends NyARPointerStack<T>
{
	public NyARTargetList(int i_max_target) throws NyARException
	{
		super.initInstance(i_max_target,(Class<T>)NyARTarget.class);
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
}
