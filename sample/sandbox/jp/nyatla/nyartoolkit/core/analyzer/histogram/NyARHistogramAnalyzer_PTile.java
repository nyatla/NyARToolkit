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


import jp.nyatla.nyartoolkit.core.types.*;


/**
 * Pタイル法による閾値検出
 * 
 */
public class NyARHistogramAnalyzer_PTile
{
	private int _persentage;
	public NyARHistogramAnalyzer_PTile(int i_persentage)
	{
		//初期化
		this._persentage=i_persentage;
	}	
	public int getThreshold(NyARHistogram i_histogram)
	{
		// 閾値ピクセル数確定
		int th_pixcels = i_histogram.total_of_data * this._persentage / 100;

		// 閾値判定
		int i;
		if (th_pixcels > 0) {
			// 黒点基準
			for (i = 0; i < 254; i++) {
				th_pixcels -= i_histogram.data[i];
				if (th_pixcels <= 0) {
					break;
				}
			}
		} else {
			// 白点基準
			for (i = 255; i > 1; i--) {
				th_pixcels += i_histogram.data[i];
				if (th_pixcels >= 0) {
					break;
				}
			}
		}
		// 閾値の保存
		return i;
	}
}

