/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
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
package jp.nyatla.nyartoolkit.core.labeling.rlelabeling;


import jp.nyatla.nyartoolkit.core.labeling.NyARLabelInfo;
import jp.nyatla.nyartoolkit.core.labeling.NyARLabelInfoStack;


public class RleLabelFragmentInfoStack  extends NyARLabelInfoStack<RleLabelFragmentInfoStack.RleLabelFragmentInfo>
{
	public class RleLabelFragmentInfo extends NyARLabelInfo
	{
		//継承メンバ
		//int area; // フラグメントラベルの領域数
		public int entry_x; // フラグメントラベルの位置
	}	
	public RleLabelFragmentInfoStack(int i_length)
	{
		super(i_length, RleLabelFragmentInfoStack.RleLabelFragmentInfo.class);
		return;
	}

	protected RleLabelFragmentInfoStack.RleLabelFragmentInfo createElement()
	{
		return new RleLabelFragmentInfoStack.RleLabelFragmentInfo();
	}
}