package jp.nyatla.nyartoolkit.core.analyzer.histogram;

import jp.nyatla.nyartoolkit.core.types.NyARHistogram;



/**
 * 明点と暗点を双方向からPタイル法でカウントして、その中央値を閾値とする。
 * 
 * 
 */
public class NyARHistogramAnalyzer_SlidePTile implements INyARHistogramAnalyzer_Threshold
{
	private int _persentage;
	public NyARHistogramAnalyzer_SlidePTile(int i_persentage)
	{
		assert (0 <= i_persentage && i_persentage <= 50);
		//初期化
		this._persentage=i_persentage;
	}	
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
