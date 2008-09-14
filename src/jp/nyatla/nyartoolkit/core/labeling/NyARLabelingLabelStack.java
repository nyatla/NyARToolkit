/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.labeling;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.utils.*;

/**
 * NyLabelの予約型動的配列
 * 
 */
public class NyARLabelingLabelStack extends NyObjectStack
{
	protected NyARLabelingLabelStack(NyARLabelingLabel[] i_label_array)
	{
		super(i_label_array);		
	}
	public NyARLabelingLabelStack(int i_max_array_size)
	{
		super(new NyARLabelingLabel[i_max_array_size]);
	}

	protected void onReservRequest(int i_start, int i_end, Object[] i_buffer)
	{
		for (int i = i_start; i < i_end; i++) {
			i_buffer[i] = new NyARLabelingLabel();
		}
	}

	/**
	 * エリアの大きい順にラベルをソートします。
	 */
	final public void sortByArea()
	{
		int len=this._length;
		int h = len *13/10;
		NyARLabelingLabel[] item=(NyARLabelingLabel[])this._items;
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
	
