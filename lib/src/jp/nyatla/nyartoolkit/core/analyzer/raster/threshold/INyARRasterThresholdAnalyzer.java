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
package jp.nyatla.nyartoolkit.core.analyzer.raster.threshold;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このインタフェイスは、ラスタの敷居値を分析する関数を定義します。
 */
public interface INyARRasterThresholdAnalyzer
{
	/**
	 * この関数には、ラスタ全体を調査して、敷居値を計算しまする関数を実装します。
	 * @param i_input
	 * 調査するラスタオブジェクト。
	 * @return
	 * 敷居値の値を返します。
	 * @throws NyARException
	 */
	public int analyzeRaster(INyARRaster i_input) throws NyARException;
	/**
	 * この関数には、i_areaで定義するラスタの一部を調査して、敷居値を計算する関数を実装します。
	 * @param i_input
	 * 調査するラスタオブジェクト。
	 * @param i_area
	 * 調査を行う範囲を指定です。
	 * @return
	 * 敷居値の値を返します。
	 * @throws NyARException
	 */
	public int analyzeRaster(INyARRaster i_input,NyARIntRect i_area) throws NyARException;
}
