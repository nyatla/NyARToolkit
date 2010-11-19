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
import jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk.*;
/**
 * NyARRealityクラスの入力コンテナです。
 * NyARRealityへ入力する情報セットを定義します。
 * 
 * このクラスは、元画像、元画像に対するPerspectiveReader,元画像からのSampleOutを提供します。
 * </ul>
 */
public abstract class NyARRealitySource
{
	/**
	 * RealitySourceの主ラスタ。継承先のコンストラクタで実体を割り当ててください。
	 */
	protected INyARRgbRaster _rgb_source;
	/**
	 * RealitySourceの主ラスタにリンクしたPerspectiveReader。継承先のコンストラクタで実体を割り当ててください。
	 */
	protected NyARPerspectiveRasterReader _source_perspective_reader;

	/**
	 * TrackerSorceのホルダ。継承先のコンストラクタで実体を割り当ててください。
	 */
	protected NyARTrackerSource _tracksource;

	
	protected NyARRealitySource(){};
	

	/**
	 * このRealitySourceに対する読出し準備ができているかを返します。
	 * @return
	 * trueならば、{@link #makeTrackSource}が実行可能。
	 */
	public abstract boolean isReady();	
	/**
	 * 現在のRGBラスタを{@link NyARTrackerSource}の基本ラスタに書込み、その参照値を返します。
	 * この関数は、{@link NyARReality#progress}が呼び出します。
	 * この関数は、{@link NyARTrackerSource}内の基本ラスタに書き込みを行うだけで、その内容を同期しません。
	 * 継承クラスでは、{@link #_tracksource}の基本GS画像を、{@link #_rgb_source}の内容で更新する実装をしてください。
	 * @throws NyARException 
	 */
	public abstract NyARTrackerSource makeTrackSource() throws NyARException;
	/**
	 * 現在のRGBラスタを{@link NyARTrackerSource}の基本ラスタに書込み、{@link NyARTrackerSource}も含めて同期します。
	 * 通常、この関数は使用することはありません。デバックなどで、{@link NyARReality#progress}以外の方法でインスタンスの同期を行いたいときに使用してください。
	 * 継承クラスでは、{@link #_tracksource}の基本GS画像を、{@link #_rgb_source}の内容で更新してから、この関数を呼び出して同期する処理を実装をしてください。
	 * @throws NyARException 
	　*/
	public void syncResource() throws NyARException
	{
		//下位の同期
		this._tracksource.syncResource();
	}
	
	/**
	 * 元画像への参照値を返します。
	 * @return
	 */
	public final INyARRgbRaster refRgbSource()
	{
		return this._rgb_source;
	}
	/**
	 * 最後に作成したTrackSourceへのポインタを返します。
	 * この関数は、{@link NyARReality#progress}、または{@link #syncResource}の後に呼び出すことを想定しています。
	 * それ以外のタイミングでは、返却値の内容が同期していないことがあるので注意してください。
	 * @return
	 */
	public final NyARTrackerSource refLastTrackSource()
	{
		return this._tracksource;
	}
	
	/**
	 * 4頂点で囲まれた領域を遠近法で矩形に変換して、RGB画像からo_rasterへパターンを取得します。
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
	 *　4頂点で囲まれた領域を遠近法で矩形に変換して、RGB画像からo_rasterへパターンを取得します。
	 * @param i_vertex
	 * @param i_resolution
	 * @param o_raster
	 * @return
	 * @throws NyARException
	 */
	public final boolean getRgbPerspectivePatt(NyARDoublePoint2d[] i_vertex,int i_resolution,INyARRgbRaster o_raster) throws NyARException
	{
		return this._source_perspective_reader.read4Point(this._rgb_source,i_vertex,0,0,1, o_raster);
	}
	/**
	 * 4頂点で囲まれたエッジ付きの領域を遠近法で矩形に変換して、RGB画像からo_rasterへパターンを取得します。
	 * @param i_vertex
	 * @param i_edge_x
	 * @param i_edge_y
	 * @param i_resolution
	 * @param o_raster
	 * @return
	 * @throws NyARException
	 */
	public final boolean getRgbPerspectivePatt(NyARDoublePoint2d[] i_vertex,int i_resolution,int i_edge_x,int i_edge_y,INyARRgbRaster o_raster) throws NyARException
	{
		return this._source_perspective_reader.read4Point(this._rgb_source,i_vertex,i_edge_x,i_edge_y,1, o_raster);
	}
	public final boolean getRgbPerspectivePatt(NyARIntPoint2d[] i_vertex,int i_resolution,int i_edge_x,int i_edge_y,INyARRgbRaster o_raster) throws NyARException
	{
		return this._source_perspective_reader.read4Point(this._rgb_source,i_vertex,i_edge_x,i_edge_y,1, o_raster);
	}


}


