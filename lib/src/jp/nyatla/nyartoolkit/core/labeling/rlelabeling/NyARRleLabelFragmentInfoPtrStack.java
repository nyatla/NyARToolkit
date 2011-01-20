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
package jp.nyatla.nyartoolkit.core.labeling.rlelabeling;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingLabel;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabeling_ARToolKit;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;

/**
 * このクラスは、{@link NyARRleLabelFragmentInfo}の参照値の動的配列です。
 * {@link NyARLabeling_Rle}が使います。
 * {@link NyARPointerStack}からの追加機能として、配列要素のソート機能があります。
 */
public class NyARRleLabelFragmentInfoPtrStack  extends NyARPointerStack<NyARRleLabelFragmentInfo>
{
	public NyARRleLabelFragmentInfoPtrStack(int i_length) throws NyARException
	{
		this.initInstance(i_length, NyARRleLabelFragmentInfo.class);
		return;
	}

	/**
	 * この関数は、配列を{@link NyARLabelingLabel#area}でソートします。
	 */
	final public void sortByArea()
	{
		int len=this._length;
		if(len<1){
			return;
		}
		int h = len *13/10;
		NyARRleLabelFragmentInfo[] item=this._items;
		for(;;){
		    int swaps = 0;
		    for (int i = 0; i + h < len; i++) {
		        if (item[i + h].area > item[i].area) {
		            final NyARRleLabelFragmentInfo temp = item[i + h];
		            item[i + h] = item[i];
		            item[i] = temp;
		            swaps++;
		        }
		    }
		    if (h == 1) {
		        if (swaps == 0){
		        	break;
		        }
		    }else{
		        h=h*10/13;
		    }
		}		
	}	
}