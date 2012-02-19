/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.*;
import jp.nyatla.nyartoolkit.rpf.sampler.lrlabel.*;

/**
 * このクラスは、{@link NyARTarget}参照値をリストするクラスです。
 * リストの基本関数と、検索関数を提供します。
 */
public class NyARTargetList extends NyARPointerStack<NyARTarget>
{
	public NyARTargetList(int i_max_target) throws NyARException
	{
		super.initInstance(i_max_target,NyARTarget.class);
	}
	/**
	 * この関数は、Sampleの位置キーに一致する可能性の高い要素のインデクスを１つ返します。
	 * 検索基準は、矩形同士の重なりです。
	 * @param i_item
	 * キーにするオブジェクト。
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
