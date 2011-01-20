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
import jp.nyatla.nyartoolkit.core.analyzer.raster.NyARRasterAnalyzer_Histogram;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARHistogram;

/**
 * このクラスは、ヒストグラムの平滑化フィルタです。
 * <p>対応している画素形式は以下の通りです。
 * <li>{@link NyARBufferType#INT1D_GRAY_8}
 * </p>
 */
public class NyARRasterFilter_EqualizeHist extends NyARRasterFilter_CustomToneTable
{
	private NyARRasterAnalyzer_Histogram _hist_analyzer;
	private NyARHistogram _histogram=new NyARHistogram(256);
	/**
	 * コンストラクタです。
	 * 入出力ラスタの形式と、ヒストグラムのサンプリングパラメータを入力して、インスタンスを生成します。
	 * @param i_raster_type
	 * 入出力ラスタの画素形式。
	 * @param i_sample_interval
	 * ヒストグラム分析の間引き値。何ラインおきに画素のスキャンを行うかを指定します。
	 * 1以上の数値を指定してください。
	 * @throws NyARException
	 */
	public NyARRasterFilter_EqualizeHist(int i_raster_type,int i_sample_interval) throws NyARException
	{
		super(i_raster_type);
		this._hist_analyzer=new NyARRasterAnalyzer_Histogram(i_raster_type,i_sample_interval);
	}
	/**
	 * 入力ラスタにトーンフィルタを適応した画素を出力ラスタへ書込みます。
	 * 画素形式は、コンストラクタに指定した形式に合せてください。
	 */
	public void doFilter(INyARRaster i_input, INyARRaster i_output) throws NyARException
	{
		//ヒストグラムを得る
		NyARHistogram hist=this._histogram;
		this._hist_analyzer.analyzeRaster(i_input,hist);
		//変換テーブルを作成
		int hist_total=this._histogram.total_of_data;
		int min=hist.getMinData();
		int hist_size=this._histogram.length;
		int sum=0;
		for(int i=0;i<hist_size;i++){
			sum+=hist.data[i];
			this.table[i]=(int)((sum-min)*(hist_size-1)/((hist_total-min)));
		}
		//変換
		super.doFilter(i_input, i_output);
	}
}