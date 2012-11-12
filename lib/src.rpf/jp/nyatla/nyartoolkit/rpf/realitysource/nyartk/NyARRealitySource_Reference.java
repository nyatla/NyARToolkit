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
package jp.nyatla.nyartoolkit.rpf.realitysource.nyartk;



import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.param.INyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.INyARPerspectiveCopy;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.INyARRgb2GsFilter;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.*;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.NyARTrackerSource;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.NyARTrackerSource_Reference;

/**
 * このクラスは、入力ソースに{@link NyARRgbRaster}クラスを使う{@link NyARRealitySource}です。
 * 環境に依存しない、基本機能のみを実装しています。
 * <p>使い方-
 * <ul>
 * <li>ラスタのバッファタイプとサイズを指定して、インスタンスを生成します。
 * <li>{@link #refRgbSource()}関数でラスタオブジェクトを得て、そこへ画像を書き込みます。
 * <li>{@link NyARReality#progress}関数に入力します。
 * </ul>
 * </p>
 */
public class NyARRealitySource_Reference extends NyARRealitySource
{
	/** 二値化用のフィルタオジェクト。*/
	protected INyARRgb2GsFilter _filter;
	/**
	 * コンストラクタです。
	 * thisが所有するラスタの情報と、トラッカの特性値を指定して、インスタンスを生成します。
	 * @param i_width
	 * ラスタのサイズを指定します。
	 * @param i_height
	 * ラスタのサイズを指定します。
	 * @param i_ref_raster_distortion
	 * 樽型歪み矯正オブジェクトを指定します。
	 * 樽型歪み矯正が必要ない時は、NULLを指定します。
	 * @param i_depth
	 * エッジ画像のサイズを1/(2^n)で指定します。(例:QVGA画像で1を指定すると、エッジ検出画像は160x120になります。)
	 * 数値が大きいほど高速になり、検出精度は低下します。(接近したエッジを検出できなくなる。)
	 * 実用的なのは、1&lt;=n&lt;=3の範囲です。標準値は2です。
	 * @param i_number_of_sample
	 * 所有するトラッカがサンプリングする、トラックターゲット数を指定します。
	 * 大体100以上をしておけばOKです。
	 * 具体的な計算式は、{@link NyARTrackerSource_Reference#NyARTrackerSource_Reference}を参考にして下さい。
	 * @param i_raster_type
	 * ソースラスタのバッファタイプ。使用できる値に制限があります。
	 * 標準値は、{@link NyARBufferType#BYTE1D_B8G8R8X8_32}です。
	 * @throws NyARException
	 */
	public NyARRealitySource_Reference(int i_width,int i_height,INyARCameraDistortionFactor i_ref_raster_distortion,int i_depth,int i_number_of_sample,int i_raster_type) throws NyARException
	{
		this._rgb_source=new NyARRgbRaster(i_width,i_height,i_raster_type);
		this._filter=(INyARRgb2GsFilter) this._rgb_source.createInterface(INyARRgb2GsFilter.class);
		this._source_perspective_reader=(INyARPerspectiveCopy)this._rgb_source.createInterface(INyARPerspectiveCopy.class);
		this._tracksource=new NyARTrackerSource_Reference(i_number_of_sample,i_ref_raster_distortion,i_width,i_height,i_depth,true);
		return;
	}
	/**
	 * 所有するラスタが読出し可能であるかを返します。このクラスの場合には、常にtrueです。
	 * @see NyARRealitySource#isReady()
	 */
	public final boolean isReady()
	{
		return this._rgb_source.hasBuffer();
	}
	/**
	 * RGBソースとトラッカのリソースとの内容の同期を取ります。
	 * @see NyARRealitySource#syncResource()
	 */
	public final void syncResource() throws NyARException
	{
		this._filter.convert(this._tracksource.refBaseRaster());
		super.syncResource();
	}
	/**
	 * RGBソースとトラッカのグレースケール画像との同期を取ります。
	 * @see NyARRealitySource#makeTrackSource()
	 */
	public final NyARTrackerSource makeTrackSource() throws NyARException
	{
		this._filter.convert(this._tracksource.refBaseRaster());		
		return this._tracksource;
	}

}
