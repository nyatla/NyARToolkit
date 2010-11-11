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
import jp.nyatla.nyartoolkit.jmf.utils.JmfNyARRaster_RGB;
import jp.nyatla.nyartoolkit.utils.j2se.NyARBufferedImageRaster;
import jp.nyatla.nyartoolkit.utils.j2se.NyARRasterImageIO;

/**
 * NyARRealityクラスの入力コンテナです。
 * NyARRealityへ入力する情報セットを定義します。
 * 
 * このクラスは、継承して使います。継承クラスで、sourceimageに実体を宣言して下さい。
 * <p>
 * データの同期タイミングについて-sourceimageにセットした情報とlrsamplerの同期は、getSampleOutの段階で実行します。
 * その為、lrsamplerの内容を参照する関数を実装するときには、注意が必要です。(当面実装を禁止します。)
 * </p>
 */
public abstract class NyARRealitySource
{
	private NyARRasterFilter_Rgb2Gs_RgbAve _filter;
	private LowResolutionLabelingSampler _sampler;
	/**
	 * 内部向けの公開オブジェクト。
	 */
	public NyARPerspectiveRasterReader _source_perspective_reader;
	public INyARRgbRaster _rgb_source;
	public LowResolutionLabelingSamplerIn lrsamplerin;
	public NyARRealitySource(int i_width,int i_height,int i_depth) throws NyARException
	{
		this._sampler=new LowResolutionLabelingSampler(i_width,i_height,i_depth);
		this.lrsamplerin=new LowResolutionLabelingSamplerIn(i_width,i_height,i_depth,true);
		this._source_perspective_reader=new NyARPerspectiveRasterReader(_rgb_source.getBufferType());
		this._filter=new NyARRasterFilter_Rgb2Gs_RgbAve(this._rgb_source.getBufferType());
	}
	/**
	 * データソースのサンプリング結果を、o_sampleroutへ格納します。
	 * この関数は、NyARRealityがprogress処理を実行するときに呼び出します。
	 * @param o_sampleout
	 * @throws NyARException 
	 */
	public final void getSampleOut(LowResolutionLabelingSamplerOut o_samplerout) throws NyARException
	{
		this._filter.doFilter(this._rgb_source,this.lrsamplerin._base_raster);
		this.lrsamplerin.syncSource();
		this._sampler.sampling(this.lrsamplerin, o_samplerout);
	}	
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
	/**
	 * このRealitySourceに対する読出し準備ができているかを返す。
	 */
	public abstract boolean isReady();
}

class NyARRealitySource_JavaImage extends NyARRealitySource
{
	public NyARRealitySource_JavaImage(int i_width,int i_height,int i_depth) throws NyARException
	{
		super(i_width,i_height,i_depth);
		this._rgb_source=new NyARBufferedImageRaster(i_width,i_height,NyARBufferType.BYTE1D_X8R8G8B8_32);
		return;
	}
	public NyARRealitySource_JavaImage(BufferedImage i_bmp,int i_depth) throws NyARException
	{
		super(i_bmp.getWidth(),i_bmp.getHeight(),i_depth);
		this._rgb_source=new NyARBufferedImageRaster(i_bmp);
	}
	/**
	 * BufferedImageの内容を、ソース画像としてセットします。
	 * @param i_image
	 * @throws NyARException
	 */
	public void setImage(BufferedImage i_image) throws NyARException
	{
		NyARRasterImageIO.copy(i_image,this._rgb_source);
		this.lrsamplerin.syncSource();
		return;
	}
	public final boolean isReady()
	{
		return true;
	}
}
class NyARRealitySource_Jmf extends NyARRealitySource
{
	public NyARRealitySource_Jmf(VideoFormat i_fmt) throws NyARException
	{
		super(i_fmt.getSize().width,i_fmt.getSize().height,2);
		this._rgb_source=new JmfNyARRaster_RGB(i_fmt);
		return;
	}
	/**
	 * Jmfのバッファをセットします。
	 * @param i_buffer
	 * @throws NyARException
	 */
	public void setImage(javax.media.Buffer i_buffer) throws NyARException
	{
		((JmfNyARRaster_RGB)(this._rgb_source)).setBuffer(i_buffer);
		return;
	}
	public final boolean isReady()
	{
		return ((JmfNyARRaster_RGB)this._rgb_source).hasBuffer();
	}
}
