/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
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
package jp.nyatla.nyartoolkit.core.labeling.artoolkit;



import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

/**
 * このクラスは、NyARLabelingLabelの動的配列です。
 * {@link NyARLabeling_ARToolKit}が使います。
 * {@link NyARObjectStack}からの追加機能として、配列要素のソート機能があります。
 */
public class NyARLabelingLabelStack extends NyARObjectStack<NyARLabelingLabel>
{
	/**
	 * コンストラクタです。
	 * @param i_max_array_size
	 * 配列の最大サイズ。
	 * @throws NyARException
	 */
	public NyARLabelingLabelStack(int i_max_array_size) throws NyARException
	{
		super();
		super.initInstance(i_max_array_size,NyARLabelingLabel.class);
	}
	/** {@link NyARLabelingLabel}要素を返します。*/
	protected NyARLabelingLabel createElement()
	{
		return new NyARLabelingLabel();
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
		NyARLabelingLabel[] item=this._items;
		for(;;){
		    int swaps = 0;
		    for (int i = 0; i + h < len; i++) {
		        if (item[i + h].area > item[i].area) {
		            final NyARLabelingLabel temp = item[i + h];
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
	
