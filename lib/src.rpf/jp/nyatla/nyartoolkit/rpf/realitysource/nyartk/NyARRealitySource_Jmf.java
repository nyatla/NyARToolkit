package jp.nyatla.nyartoolkit.dev.rpf.realitysource.nyartk;

import javax.media.format.VideoFormat;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.NyARRasterFilter_Rgb2Gs_RgbAve;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARPerspectiveRasterReader;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSampler;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARTracker;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARTrackerSource;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.NyARTrackerSource_Reference;
import jp.nyatla.nyartoolkit.jmf.utils.JmfNyARRaster_RGB;

/**
 * このクラスは、JMFと互換性のあるNyARRealitySourceです。
 * @author nyatla
 *
 */
public class NyARRealitySource_Jmf extends NyARRealitySource
{
	protected NyARRasterFilter_Rgb2Gs_RgbAve _filter;
	/**
	 * コンストラクタです。
	 * @param i_fmt
	 * JMFの入力フォーマットを指定します。
	 * @param i_ref_raster_distortion
	 * 歪み矯正の為のオブジェクトを指定します。歪み矯正が必要ない時は、NULLを指定します。
	 * @param i_depth
	 * エッジ画像のサイズを1/(2^n)で指定します。(例:QVGA画像で1を指定すると、エッジ検出画像は160x120になります。)
	 * 数値が大きいほど高速になり、検出精度は低下します。実用的なのは、1<=n<=3の範囲です。標準値は2です。
	 * @param i_number_of_sample
	 * サンプリングするターゲット数を指定します。大体100以上をしておけばOKです。具体的な計算式は、{@link NyARTrackerSource_Reference#NyARTrackerSource_Reference}を参考にして下さい。
	 * @throws NyARException
	 */
	public NyARRealitySource_Jmf(VideoFormat i_fmt,NyARCameraDistortionFactor i_ref_raster_distortion,int i_depth,int i_number_of_sample) throws NyARException
	{
		this._rgb_source=new JmfNyARRaster_RGB(i_fmt);
		this._filter=new NyARRasterFilter_Rgb2Gs_RgbAve(this._rgb_source.getBufferType());
		this._source_perspective_reader=new NyARPerspectiveRasterReader(_rgb_source.getBufferType());
		this._tracksource=new NyARTrackerSource_Reference(i_number_of_sample,i_ref_raster_distortion,i_fmt.getSize().width,i_fmt.getSize().height,i_depth,true);
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
	public final void syncResource() throws NyARException
	{
		this._filter.doFilter(this._rgb_source,this._tracksource.refBaseRaster());
		super.syncResource();
	}
	public final NyARTrackerSource makeTrackSource() throws NyARException
	{
		this._filter.doFilter(this._rgb_source,this._tracksource.refBaseRaster());		
		return this._tracksource;
	}
}