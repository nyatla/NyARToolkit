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
package jp.nyatla.nyartoolkit.rpf.utils;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;

public interface INyARGsRasterGraphics
{
	/**
	 * この関数は、指定した数値でラスタを埋めます。
	 * この関数は高速化していません。
	 * @param i_value
	 * 埋める数値を指定します。0から255の数値を指定して下さい。
	 */
	public void fill(int i_value);
	/**
	 * この関数は、出力先に一定間隔で間引いた画像を出力します。
	 * 例えば、i_skipが1の場合には等倍、2なら1/2倍、3なら1/3の画像を出力します。
	 * @param i_left
	 * 入力ラスタの左上点を指定します。
	 * @param i_top
	 * 入力ラスタの左上点を指定します。
	 * @param i_skip
	 * skip値。1なら等倍、2なら1/2倍、3なら1/3倍の画像を出力します。
	 * @param o_output
	 * 出力先ラスタ。このラスタの解像度は、w=(i_input.w-i_left)/i_skip,h=(i_input.h-i_height)/i_skipを満たす必要があります。
	 * 出力先ラスタと入力ラスタの画素形式は、同じである必要があります。
	 */
	public void copyTo(int i_left,int i_top,int i_skip, INyARGrayscaleRaster o_output) throws NyARException;
	
}

