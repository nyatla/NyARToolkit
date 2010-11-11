package jp.nyatla.nyartoolkit.dev.rpf.realitysource.nyartk;

import java.awt.image.BufferedImage;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.utils.j2se.NyARBufferedImageRaster;

public class NyARRealitySource_JavaImage extends NyARRealitySource
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
	 * 入力ラスタとリンクしたBufferedImageを返します。
	 * @return
	 */
	public BufferedImage getImageImage()
	{
		return ((NyARBufferedImageRaster)this._rgb_source).getBufferedImage();
	}
	public final boolean isReady()
	{
		return true;
	}
}
