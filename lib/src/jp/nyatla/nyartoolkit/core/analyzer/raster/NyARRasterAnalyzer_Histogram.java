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
package jp.nyatla.nyartoolkit.core.analyzer.raster;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterdriver.INyARHistogramFromRaster;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、グレースケール画像のヒストグラムを計算します。
 * このクラスは、{@link INyARHistogramFromRaster}を使います。
 */
public class NyARRasterAnalyzer_Histogram
{

	/**
	 * ヒストグラム解析の縦方向スキップ数。
	 * <p> memo:継承クラスはこのライン数づつ、スキップしながらヒストグラム計算を行うこと。</p>
	 */
	protected int _vertical_skip;
	
	/**
	 * コンストラクタです。
	 * ラスタの画素形式を指定して、ヒストグラム分析器を作ります。
	 * @param i_vertical_interval
	 * 画素スキャン時の、Y軸方向のスキップ数を指定します。数値が大きいほど高速に解析できますが、精度は劣化します。0以上を指定してください。
	 * @throws NyARException
	 */
	public NyARRasterAnalyzer_Histogram(int i_vertical_interval) throws NyARException
	{
		this._vertical_skip=i_vertical_interval;
	}
	/**
	 * 画素スキャン時の、Y軸方向のスキップ数を指定します。
	 * @param i_step
	 * 新しい行スキップ数
	 */
	public void setVerticalInterval(int i_step)
	{
		assert(this._vertical_skip>0);
		this._vertical_skip=i_step;
		return;
	}
	
	private INyARRaster _last_input_raster;
	private INyARHistogramFromRaster _histdrv=null;
	/**
	 * ラスタから、ヒストグラムを計算します。
	 * @param i_input
	 * 解析するラスタオブジェクト
	 * @param o_histogram
	 * 解析結果を格納するヒストグラムオブジェクト。ヒストグラムの範囲は、256であること。
	 * @throws NyARException
	 */
	public void analyzeRaster(INyARRaster i_input,NyARHistogram o_histogram) throws NyARException
	{
		final NyARIntSize size=i_input.getSize();
		//最大画像サイズの制限
		assert(size.w*size.h<0x40000000);
		assert(o_histogram.length==256);//現在は固定
		//変換インタフェイスの準備
		if(i_input!=this._last_input_raster){
			this._histdrv=(INyARHistogramFromRaster) i_input.createInterface(INyARHistogramFromRaster.class);
			this._last_input_raster=i_input;
		}
		this._histdrv.createHistogram(this._vertical_skip,o_histogram);
		return;
	}
	/**
	 * ラスタの一部分（矩形）から、ヒストグラムを計算します。
	 * @param i_input
	 * 解析するラスタオブジェクト
	 * @param i_area
	 * 解析する矩形範囲。ラスタの内側である事が必要。
	 * @param o_histogram
	 * 解析結果を格納するヒストグラムオブジェクト。ヒストグラムの範囲は、256であること。
	 * @throws NyARException
	 */	
	public void analyzeRaster(INyARRaster i_input,NyARIntRect i_area,NyARHistogram o_histogram) throws NyARException
	{
		final NyARIntSize size=i_input.getSize();
		//最大画像サイズの制限
		assert(size.w*size.h<0x40000000);
		assert(o_histogram.length==256);//現在は固定
		//変換インタフェイスの準備
		if(i_input!=this._last_input_raster){
			this._histdrv=(INyARHistogramFromRaster) i_input.createInterface(INyARHistogramFromRaster.class);
			this._last_input_raster=i_input;
		}
		this._histdrv.createHistogram(i_area.x,i_area.y,i_area.w,i_area.h,this._vertical_skip,o_histogram);
		return;
	}
	


	/**
	 * デバック用関数
	 * @param args
	 * main関数引数
	 */
	public static void main(String[] args)
	{
		try{
			NyARRgbRaster raster=new NyARRgbRaster(100,100,NyARBufferType.WORD1D_R5G6B5_16LE);
			short[] buf=(short[])raster.getBuffer();
			for(int i=0;i<100;i++){
				for(int i2=0;i2<100;i2++){
					buf[(i*100+i2)+0]=(short)(3); //buf[(i*100+i2)*3+1]=buf[(i*100+i2)*3+2]=(byte)i2;
				}
			}
			NyARIntRect rect=new NyARIntRect();
			rect.x=2;rect.y=2;rect.h=10;rect.w=10;
//			NyARRasterAnalyzer_Histogram ha=new NyARRasterAnalyzer_Histogram(raster.getBufferType(),1);
			NyARHistogram h=new NyARHistogram(256);
//			ha.analyzeRaster(raster,rect, h);
//			ha.analyzeRaster(raster, h);
			return;
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}

//
//	Filterドライバ
//


