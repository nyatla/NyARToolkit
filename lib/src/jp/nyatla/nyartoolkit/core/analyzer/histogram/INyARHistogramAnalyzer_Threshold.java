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
package jp.nyatla.nyartoolkit.core.analyzer.histogram;

import jp.nyatla.nyartoolkit.core.types.NyARHistogram;

/**
 * このインタフェイスは、ヒストグラムから敷居値を探索する関数を定義します。
 */
public interface INyARHistogramAnalyzer_Threshold
{
	/**
	 * ヒストグラムから閾値を１個探索する関数を実装します。
	 * @param i_histogram
	 * 分析するヒストグラムオブジェクト
	 * @return
	 * 敷居値を返します。値範囲は、{@link NyARHistogram}のプロパティから決定します。
	 */
	public int getThreshold(NyARHistogram i_histogram);
}
