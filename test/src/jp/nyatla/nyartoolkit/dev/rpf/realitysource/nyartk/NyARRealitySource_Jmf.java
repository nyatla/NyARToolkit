package jp.nyatla.nyartoolkit.dev.rpf.realitysource.nyartk;

import javax.media.format.VideoFormat;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.NyARRasterFilter_Rgb2Gs_RgbAve;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARPerspectiveRasterReader;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSampler;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerIn;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.jmf.utils.JmfNyARRaster_RGB;

public class NyARRealitySource_Jmf extends NyARRealitySource
{
	private LowResolutionLabelingSampler _sampler;
	protected NyARRasterFilter_Rgb2Gs_RgbAve _filter;
	protected LowResolutionLabelingSamplerIn _lrsamplerin;
	public NyARRealitySource_Jmf(VideoFormat i_fmt,int i_depth) throws NyARException
	{
		this._rgb_source=new JmfNyARRaster_RGB(i_fmt);
		this._sampler=new LowResolutionLabelingSampler(i_fmt.getSize().width,i_fmt.getSize().height,(int)Math.pow(2,i_depth));
		this._filter=new NyARRasterFilter_Rgb2Gs_RgbAve(this._rgb_source.getBufferType());
		this._lrsamplerin=new LowResolutionLabelingSamplerIn(i_fmt.getSize().width,i_fmt.getSize().height,i_depth,true);
		this._source_perspective_reader=new NyARPerspectiveRasterReader(_rgb_source.getBufferType());
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
	public final void getSampleOut(LowResolutionLabelingSamplerOut o_samplerout) throws NyARException
	{
		this._filter.doFilter(this._rgb_source,this._lrsamplerin._base_raster);
		this._lrsamplerin.syncSource();
		this._sampler.sampling(this._lrsamplerin, o_samplerout);
	}	
}