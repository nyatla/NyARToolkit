package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import java.awt.image.BufferedImage;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_RGB;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.NyARRasterFilter_Rgb2Gs_RgbAve;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.*;
import jp.nyatla.nyartoolkit.utils.j2se.NyARRasterImageIO;

/**
 * NyARRealityクラスの入力コンテナです。
 * NyARRealityへ入力する情報セットを定義します。
 * 
 * このクラスは、継承して使います。継承クラスで、sourceimageに実体を宣言して下さい。
 */
public abstract class NyARRealityIn
{
	public INyARRgbRaster sourceimage;
	public LowResolutionLabelingSamplerIn lrsamplerin;
	public NyARRealityIn(int i_width,int i_height) throws NyARException
	{
		this.lrsamplerin=new LowResolutionLabelingSamplerIn(i_width,i_height,2,true);
	}
}

class NyARReality_JavaImage extends NyARRealityIn
{
	private NyARRasterFilter_Rgb2Gs_RgbAve _filter;
	public NyARReality_JavaImage(int i_width,int i_height) throws NyARException
	{
		super(i_width,i_height);
		this.sourceimage=new NyARRgbRaster_RGB(i_width,i_height);
		this._filter=new NyARRasterFilter_Rgb2Gs_RgbAve(this.sourceimage.getBufferType());
		return;
	}
	/**
	 * BufferedImageの内容を、ソース画像としてセットします。
	 * @param i_image
	 * @throws NyARException
	 */
	public void setImage(BufferedImage i_image) throws NyARException
	{
		NyARRasterImageIO.copy(i_image,this.sourceimage);
		this._filter.doFilter(this.sourceimage,this.lrsamplerin._base_raster);
		return;
	}
}
class NyARReality_JmfSource extends NyARRealityIn
{
	public NyARReality_JmfSource(int i_width,int i_height) throws NyARException
	{
		super(i_width,i_height);
		return;
	}
	
}
