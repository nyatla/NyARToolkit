package jp.nyatla.nyartoolkit.dev.rpf.realitysource.nyartk;

import javax.media.format.VideoFormat;

import jp.nyatla.nyartoolkit.NyARException;
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
	 * @param i_depth
	 * エッジ画像のサイズを1(1^n)で指定します。
	 * @param i_number_of_sample
	 * サンプリングするターゲット数を指定します。大体100以上をしておけばOKです。具体的な計算式は、{@link NyARTrackerSource_Reference#NyARTrackerSource_Reference}を参考にして下さい。
	 * @throws NyARException
	 */
	public NyARRealitySource_Jmf(VideoFormat i_fmt,int i_depth,int i_number_of_sample) throws NyARException
	{
		this._rgb_source=new JmfNyARRaster_RGB(i_fmt);
		this._filter=new NyARRasterFilter_Rgb2Gs_RgbAve(this._rgb_source.getBufferType());
		this._source_perspective_reader=new NyARPerspectiveRasterReader(_rgb_source.getBufferType());
		this._tracksource=new NyARTrackerSource_Reference(i_number_of_sample,i_fmt.getSize().width,i_fmt.getSize().height,i_depth,true);
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
		this._filter.doFilter(this._rgb_source,this._tracksource.getBaseRaster());
		super.syncResource();
	}
	public final NyARTrackerSource makeTrackSource() throws NyARException
	{
		this._filter.doFilter(this._rgb_source,this._tracksource.getBaseRaster());		
		return this._tracksource;
	}
}