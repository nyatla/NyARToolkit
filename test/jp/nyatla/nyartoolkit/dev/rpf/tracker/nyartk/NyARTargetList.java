package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
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

		int ret=-1;
		int min_d=Integer.MAX_VALUE;

		//対角範囲の距離が、対角距離の1/2以下で、最も小さいこと。
		for(int i=this._length-1;i>=0;i--)
		{
			
			iitem=this._items[i];
			int d;
			d=NyARIntRect.getSqDiagonalPointDiff(iitem.sample_area,i_item.base_area);
/*			int x1,y1;
			//対角点同士の距離を計算
			x1=iitem.sample_area.x-i_item.base_area.x;
			y1=iitem.sample_area.y-i_item.base_area.y;
			d=x1*x1+y1*y1;
			x1=x1+iitem.sample_area.w-i_item.base_area.w;
			y1=y1+iitem.sample_area.h-i_item.base_area.h;
			d+=x1*x1+y1*y1;		*/	
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
