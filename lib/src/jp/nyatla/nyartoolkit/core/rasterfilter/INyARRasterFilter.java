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
package jp.nyatla.nyartoolkit.core.rasterfilter;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
/**
 * このインタフェイスは、ラスタのフィルタ処理をする関数の関数を定義します。
 */
public interface INyARRasterFilter
{
	/**
	 * この関数は、入力画像にフィルタをかけて、出力画像へ書込みます。
	 * 実装クラスでは、i_inputのラスタにフィルタをかけて、i_outputへ値を出力してください。
	 * @param i_input
	 * 入力画像。
	 * @param i_output
	 * 出力画像
	 * @throws NyARException
	 */
	public void doFilter(INyARRaster i_input, INyARRaster i_output) throws NyARException;
}