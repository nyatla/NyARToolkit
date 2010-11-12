package jp.nyatla.nyartoolkit.dev.rpf.realitysource.nyartk;

import java.awt.image.BufferedImage;

import javax.media.Buffer;
import javax.media.format.VideoFormat;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_RGB;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.NyARRasterFilter_Rgb2Gs_RgbAve;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARPerspectiveRasterReader;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.*;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARTrackerSource;
import jp.nyatla.nyartoolkit.jmf.utils.JmfNyARRaster_RGB;
import jp.nyatla.nyartoolkit.utils.j2se.NyARBufferedImageRaster;
import jp.nyatla.nyartoolkit.utils.j2se.NyARRasterImageIO;

/**
 * NyARRealityクラスの入力コンテナです。
 * NyARRealityへ入力する情報セットを定義します。
 * 
 * このクラスはAbstractクラスです。継承して、以下の項目を実装してください。
 * <ul>
 * <li>コンストラクタで_rgb_source,_source_perspective_readerに実体を割り当ててください。</li>
 * <li>getSampleOutに、_rgb_sourceの内容からo_sampleroutを生成する実装を書いて下さい。</li>
 * <li>getSampleOutに、_rgb_sourceの内容からo_sampleroutを生成する実装を書いて下さい。</li>
 * <li>isReadyに、現在の準備状態を返す処理を書いてください。</li>
 * </ul>
 */
public abstract class NyARRealitySource
{
	private NyARTrackerSource _tracker_source;
	public NyARTrackerSource refTrackerSource()
	{
		return this._tracker_source;
	}

	/**
	 * 内部向けの公開オブジェクト。RealitySourceの主ラスタ。継承先のコンストラクタで実体を割り当ててください。
	 */
	public INyARRgbRaster _rgb_source;
	/**
	 * 内部向けの公開オブジェクト。RealitySourceの主ラスタ要の、PerspectiveReader
	 */
	public NyARPerspectiveRasterReader _source_perspective_reader;

	
	protected NyARRealitySource(){};
	
	/**
	 * 現在のRealitySourceからLowResolutionLabelingSamplerOutの内容を書きだす処理を実装します。
	 * この関数は、NyARRealityがprogress処理を実行するときに呼び出します。
	 * @param o_sampleout
	 * @throws NyARException 
	 */
	public abstract void getSampleOut(LowResolutionLabelingSamplerOut o_samplerout) throws NyARException;

	/**
	 * このRealitySourceに対する読出し準備ができているかを返す処理を実装します。
	 */
	public abstract boolean isReady();	

	/**
	 * RGB画像から、4頂点で囲まれた領域を遠近法で矩形に変換して、o_rasterへパターンを取得します。
	 * この関数は、最後に
	 * @param i_vertex
	 * @param i_resolution
	 * 1ピクセルあたりのサンプル数です。二乗した値が実際のサンプル数になります。
	 * @param o_raster
	 * @return
	 * @throws NyARException
	 */
	public final boolean getRgbPerspectivePatt(NyARIntPoint2d[] i_vertex,int i_resolution,INyARRgbRaster o_raster) throws NyARException
	{
		return this._source_perspective_reader.read4Point(this._rgb_source,i_vertex,0,0,i_resolution, o_raster);
	}
	/**
	 * RGB画像から、4頂点で囲まれた領域を遠近法で矩形に変換して、o_rasterへパターンを取得します。
	 * @param i_vertex
	 * @param o_raster
	 * @return
	 * @throws NyARException
	 */
	public final boolean getRgbPerspectivePatt(NyARDoublePoint2d[] i_vertex,INyARRgbRaster o_raster) throws NyARException
	{
		return this._source_perspective_reader.read4Point(this._rgb_source,i_vertex,0,0,1, o_raster);
	}

}


