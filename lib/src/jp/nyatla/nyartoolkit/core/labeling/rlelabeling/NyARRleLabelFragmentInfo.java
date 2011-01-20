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

import jp.nyatla.nyartoolkit.core.labeling.NyARLabelInfo;

/**
 * このクラスは、{@link NyARLabeling_Rle}の出力するフラグメント値を格納します。
 * {@link NyARLabelInfo}に追加した要素は、輪郭のエントリポイント{@link #entry_x}です。
 * {@link #entry_x}と{@link #clip_t}から、輪郭追跡の為のエントリポイントが求まります。
 */
public class NyARRleLabelFragmentInfo extends NyARLabelInfo
{
	public int entry_x;  // フラグメントラベルの位置
}
