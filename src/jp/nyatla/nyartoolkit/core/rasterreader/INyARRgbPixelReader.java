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
package jp.nyatla.nyartoolkit.core.rasterreader;

import jp.nyatla.nyartoolkit.NyARException;

/**
 * R8G8B8でピクセルを読み出すインタフェイス
 * 
 */
public interface INyARRgbPixelReader
{
	/**
	 * 1ピクセルをint配列にして返します。
	 * 
	 * @param i_x
	 * @param i_y
	 * @param i_rgb
	 */
	public void getPixel(int i_x, int i_y, int[] i_rgb) throws NyARException;

	/**
	 * 複数のピクセル値をint配列に返します。
	 * 配列には、[R1][G1][B1][R2][G2][B2]の順でピクセル値が格納されます。
	 * 
	 * @param i_x
	 * xのインデックス配列
	 * @param i_y
	 * yのインデックス配列
	 */
	public void getPixelSet(int i_x[], int i_y[], int i_num, int[] i_intrgb) throws NyARException;
}
