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
package jp.nyatla.nyartoolkit.core.rasteranalyzer.threshold;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.analyzer.raster.threshold.INyARRasterThresholdAnalyzer;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * 微分ヒストグラム法による閾値検出
 * 
 */
public class NyARRasterThresholdAnalyzer_DiffHistogram implements INyARRasterThresholdAnalyzer
{
	private int _threshold;

	public NyARRasterThresholdAnalyzer_DiffHistogram()
	{
	}

	private int createHistogram(int[] in_buf,NyARIntSize i_size, int[] o_histogram) throws NyARException
	{
		int[][] fil1={
				{-1,-2,-1},
				{ 0, 0, 0},
				{ 1, 2, 1}};

		// ヒストグラムを作成
		for (int i = 0; i < 256; i++) {
			o_histogram[i] = 0;
		}
		int sam;
		int sam1,sam2;
		for (int y = 1; y < i_size.h-1; y++) {
			for (int x = 1; x < i_size.w-1; x++) {
				int v = in_buf[y* i_size.w+x];
				sam1=sam2=0;
				for(int yy=0;yy<3;yy++){
					for(int xx=0;xx<3;xx++){
						int v2=in_buf[(y+yy-1)* i_size.w+(x+xx-1)];
						sam1+=v2*fil1[xx][yy];
						sam2+=v2*fil1[yy][xx];
					}					
				}
				sam=sam1*sam1+sam2*sam2;
				o_histogram[v]+=sam;
			}
		}
		int th=0;
		int max=o_histogram[0];
		for(int i=1;i<256;i++){
			if(max<o_histogram[i]){
				th=i;
				max=o_histogram[i];
			}
		}
		return th;
	}

	public int analyzeRaster(INyARRaster i_input) throws NyARException
	{
		assert (i_input.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		int[] histogram = new int[256];
		return createHistogram((int[])i_input.getBuffer(),i_input.getSize(), histogram);
	}

	/**
	 * デバック用の関数です。 ヒストグラムをラスタに書き出します。
	 * 
	 * @param i_output
	 * 書き出し先のラスタオブジェクト 256ピクセル以上の幅があること。
	 */
	public void debugDrawHistogramMap(INyARRaster i_input, INyARRaster i_output) throws NyARException
	{
		assert (i_input.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		assert (i_output.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		NyARIntSize size = i_output.getSize();

		int[] out_buf = (int[]) i_output.getBuffer();
		// 0で塗りつぶし
		for (int y = 0; y < size.h; y++) {
			for (int x = 0; x < size.w; x++) {
				out_buf[y* size.w+x] = 0;
			}
		}
		// ヒストグラムを計算
		int[] histogram = new int[256];
		int threshold = createHistogram((int[])i_input.getBuffer(),i_input.getSize(), histogram);

		// ヒストグラムの最大値を出す
		int max_v = 0;
		for (int i = 0; i < 255; i++) {
			if (max_v < histogram[i]) {
				max_v = histogram[i];
			}
		}
		// 目盛り
		for (int i = 0; i < size.h; i++) {
			out_buf[i* size.w+0] = 128;
			out_buf[i* size.w+128] = 128;
			out_buf[i* size.w+255] = 128;
		}
		// スケーリングしながら描画
		for (int i = 0; i < 255; i++) {
			out_buf[(histogram[i] * (size.h - 1) / max_v)* size.w+i] = 255;
		}
		// 値
		for (int i = 0; i < size.h; i++) {
			out_buf[i* size.w+threshold] = 255;
		}
		return;
	}

	public int getThreshold()
	{
		return this._threshold;
	}
}
