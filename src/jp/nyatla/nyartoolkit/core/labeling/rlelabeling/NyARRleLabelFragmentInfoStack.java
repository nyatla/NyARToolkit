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
import jp.nyatla.nyartoolkit.core.labeling.NyARLabelInfo;
import jp.nyatla.nyartoolkit.core.labeling.NyARLabelInfoStack;


public class NyARRleLabelFragmentInfoStack  extends NyARLabelInfoStack<NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo>
{
	public class RleLabelFragmentInfo extends NyARLabelInfo
	{
		//継承メンバ
		//int area; // フラグメントラベルの領域数
		public int entry_x; // フラグメントラベルの位置
	}	
	public NyARRleLabelFragmentInfoStack(int i_length) throws NyARException
	{
		super(i_length, NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo.class);
		return;
	}

	protected NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo createElement()
	{
		return new NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo();
	}
}