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
 * このクラスは、明点・暗点のPTail法で求めた敷居値を合算して、敷居値を計算します。
 * <p>敷居値決定のアルゴリズム - 明点・暗点両側からPTail法を用いて一定割合の画素を取り除き、その中間値を求めます。</p>
 */
public class NyARHistogramAnalyzer_SlidePTile implements INyARHistogramAnalyzer_Threshold
{
	private int _persentage;
	/**
	 * コンストラクタです。
	 * @param i_persentage
	 * 明点、暗点の両側から取り除く、画素の割合を指定します。0&lt;n&lt;50の範囲で指定します。
	 */
	public NyARHistogramAnalyzer_SlidePTile(int i_persentage)
	{
		assert (0 <= i_persentage && i_persentage <= 50);
		//初期化
		this._persentage=i_persentage;
	}
	/**
	 * この関数は、SlidePTileを用いて敷居値を1個求めます。敷居値の範囲は、i_histogram引数の範囲と同じです。
	 */	
	public int getThreshold(NyARHistogram i_histogram)
	{
		//総ピクセル数を計算
		int n=i_histogram.length;
		int sum_of_pixel=i_histogram.total_of_data;
		int[] hist=i_histogram.data;
		// 閾値ピクセル数確定
		final int th_pixcels = sum_of_pixel * this._persentage / 100;
		int th_wk;
		int th_w, th_b;

		// 黒点基準
		th_wk = th_pixcels;
		for (th_b = 0; th_b < n-2; th_b++) {
			th_wk -= hist[th_b];
			if (th_wk <= 0) {
				break;
			}
		}
		// 白点基準
		th_wk = th_pixcels;
		for (th_w = n-1; th_w > 1; th_w--) {
			th_wk -= hist[th_w];
			if (th_wk <= 0) {
				break;
			}
		}
		// 閾値の保存
		return (th_w + th_b) / 2;
	}
}
