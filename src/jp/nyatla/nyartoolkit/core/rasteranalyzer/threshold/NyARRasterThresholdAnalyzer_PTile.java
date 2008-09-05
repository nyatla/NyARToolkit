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
package jp.nyatla.nyartoolkit.core.rasteranalyzer.threshold;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasterreader.INyARBufferReader;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * Pタイル法による閾値検出
 * 
 */
public class NyARRasterThresholdAnalyzer_PTile implements INyARRasterThresholdAnalyzer
{
	private int _persentage;

	private int _threshold;

	/**
	 * @param i_persentage
	 * -100<=0<=100であること。 正の場合、黒点を基準にします。 負の場合、白点を基準にします。
	 * (CMOSカメラの場合、基準点は白点の方が良い)
	 */
	public NyARRasterThresholdAnalyzer_PTile(int i_persentage)
	{
		assert (-100 <= i_persentage && i_persentage <= 100);
		this._persentage = i_persentage;
	}

	private int createHistgram(INyARBufferReader i_reader,NyARIntSize i_size, int[] o_histgram) throws NyARException
	{
		int[][] in_buf = (int[][]) i_reader.getBuffer();
		int[] histgram = o_histgram;

		// ヒストグラムを作成
		for (int i = 0; i < 256; i++) {
			histgram[i] = 0;
		}
		int sum = 0;
		for (int y = 0; y < i_size.h; y++) {
			int sum2 = 0;
			for (int x = 0; x < i_size.w; x++) {
				int v = in_buf[y][x];
				histgram[v]++;
				sum2 += v;
			}
			sum = sum + sum2 / i_size.w;
		}
		// 閾値ピクセル数確定
		int th_pixcels = i_size.w * i_size.h * this._persentage / 100;

		// 閾値判定
		int i;
		if (th_pixcels > 0) {
			// 黒点基準
			for (i = 0; i < 254; i++) {
				th_pixcels -= histgram[i];
				if (th_pixcels <= 0) {
					break;
				}
			}
		} else {
			// 白点基準
			for (i = 255; i > 1; i--) {
				th_pixcels += histgram[i];
				if (th_pixcels >= 0) {
					break;
				}
			}
		}
		// 閾値の保存
		return i;
	}

	public void analyzeRaster(INyARRaster i_input) throws NyARException
	{
		final INyARBufferReader buffer_reader=i_input.getBufferReader();	
		assert (buffer_reader.isEqualBufferType(INyARBufferReader.BUFFERFORMAT_INT2D_GLAY_8));
		int[] histgram = new int[256];
		this._threshold = createHistgram(buffer_reader,i_input.getSize(), histgram);
	}

	/**
	 * デバック用の関数です。 ヒストグラムをラスタに書き出します。
	 * 
	 * @param i_output
	 * 書き出し先のラスタオブジェクト 256ピクセル以上の幅があること。
	 */
	public void debugDrawHistgramMap(INyARRaster i_input, INyARRaster i_output) throws NyARException
	{
		INyARBufferReader in_buffer_reader=i_input.getBufferReader();	
		INyARBufferReader out_buffer_reader=i_output.getBufferReader();	
		assert (in_buffer_reader.isEqualBufferType(INyARBufferReader.BUFFERFORMAT_INT2D_GLAY_8));
		assert (out_buffer_reader.isEqualBufferType(INyARBufferReader.BUFFERFORMAT_INT2D_GLAY_8));
		NyARIntSize size = i_output.getSize();

		int[][] out_buf = (int[][]) out_buffer_reader.getBuffer();
		// 0で塗りつぶし
		for (int y = 0; y < size.h; y++) {
			for (int x = 0; x < size.w; x++) {
				out_buf[y][x] = 0;
			}
		}
		// ヒストグラムを計算
		int[] histgram = new int[256];
		int threshold = createHistgram(in_buffer_reader,i_input.getSize(), histgram);

		// ヒストグラムの最大値を出す
		int max_v = 0;
		for (int i = 0; i < 255; i++) {
			if (max_v < histgram[i]) {
				max_v = histgram[i];
			}
		}
		// 目盛り
		for (int i = 0; i < size.h; i++) {
			out_buf[i][0] = 128;
			out_buf[i][128] = 128;
			out_buf[i][255] = 128;
		}
		// スケーリングしながら描画
		for (int i = 0; i < 255; i++) {
			out_buf[histgram[i] * (size.h - 1) / max_v][i] = 255;
		}
		// 値
		for (int i = 0; i < size.h; i++) {
			out_buf[i][threshold] = 255;
		}
		return;
	}

	public int getThreshold()
	{
		return this._threshold;
	}

	public int getThreshold(int i_x, int i_y)
	{
		return this._threshold;
	}
}
