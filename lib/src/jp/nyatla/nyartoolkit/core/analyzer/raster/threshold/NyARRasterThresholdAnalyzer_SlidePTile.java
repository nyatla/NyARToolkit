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
import jp.nyatla.nyartoolkit.core.analyzer.histogram.*;
import jp.nyatla.nyartoolkit.core.analyzer.raster.*;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、画像のヒストグラムから{@link NyARHistogramAnalyzer_SlidePTile}による敷居値検出をする機能を提供します。
 */
public class NyARRasterThresholdAnalyzer_SlidePTile implements INyARRasterThresholdAnalyzer
{
	/** ヒストグラムオブジェクト*/
	protected NyARRasterAnalyzer_Histogram _raster_analyzer;
	private NyARHistogramAnalyzer_SlidePTile _sptile;
	private NyARHistogram _histogram;
	/**
	 * この関数は、ヒストグラム作成時の行スキップ数を指定します。
	 * @param i_step
	 * 新しい行スキップ数
	 * @see NyARRasterAnalyzer_Histogram#setVerticalInterval
	 */
	public void setVerticalInterval(int i_step)
	{
		this._raster_analyzer.setVerticalInterval(i_step);
		return;
	}
	/**
	 * コンストラクタ。
	 * {@link NyARHistogramAnalyzer_SlidePTile}に渡すパラメータを指定して、敷居値分析器を作成します。
	 * @param i_persentage
	 * {@link NyARHistogramAnalyzer_SlidePTile#NyARHistogramAnalyzer_SlidePTile}へ渡す分析パラメータです。
	 * @param i_raster_format
	 * 分析するラスタの画素形式です。
	 * @param i_vertical_interval
	 * ヒストグラム作成時の行スキップ数です。
	 * @throws NyARException
	 */
	public NyARRasterThresholdAnalyzer_SlidePTile(int i_persentage,int i_raster_format,int i_vertical_interval) throws NyARException
	{
		assert (0 <= i_persentage && i_persentage <= 50);
		//初期化
		if(!initInstance(i_raster_format,i_vertical_interval)){
			throw new NyARException();
		}
		this._sptile=new NyARHistogramAnalyzer_SlidePTile(i_persentage);
		this._histogram=new NyARHistogram(256);
	}
	/**
	 * この関数は、インスタンスの初期化関数です。
	 * 継承クラスを作るときには、この関数をオーバライドします。
	 * @param i_raster_format
	 * @see NyARRasterThresholdAnalyzer_SlidePTile#NyARRasterThresholdAnalyzer_SlidePTile(int, int, int)
	 * @param i_vertical_interval
	 * @see NyARRasterThresholdAnalyzer_SlidePTile#NyARRasterThresholdAnalyzer_SlidePTile(int, int, int)
	 * @return
	 * 初期化に成功すると、trueを返します。
	 * @throws NyARException
	 */
	protected boolean initInstance(int i_raster_format,int i_vertical_interval) throws NyARException
	{
		this._raster_analyzer=new NyARRasterAnalyzer_Histogram(i_raster_format,i_vertical_interval);
		return true;
	}
	/**
	 * {@link NyARHistogramAnalyzer_SlidePTile}による敷居値分析を実行します。
	 */
	public int analyzeRaster(INyARRaster i_input) throws NyARException
	{
		this._raster_analyzer.analyzeRaster(i_input, this._histogram);
		return this._sptile.getThreshold(this._histogram);
	}
	/**
	 * {@link NyARHistogramAnalyzer_SlidePTile}による敷居値分析を実行します。
	 * この関数は未実装です。
	 */
	public int analyzeRaster(INyARRaster i_input,NyARIntRect i_area) throws NyARException
	{
		throw new NyARException();
	}
	
}
