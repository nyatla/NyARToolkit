package jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;

/**
 * 拡張したNyARGrayscaleRasterです。ユーザには、NyARGrayscaleRasterのインタフェイスのみを公開します。
 * このクラスは、
 *
 */
public class LrlsGsRaster extends NyARGrayscaleRaster
{
	private NyARVectorReader_INT1D_GRAY_8 _vr;
	/**
	 * このラスタの、元画像に対する解像度値です。1/nの解像度である事を示します。
	 */
	public int resolution;
	/**
	 * 
	 * @param i_width
	 * @param i_height
	 * @param i_pixelsize
	 * pixelsizeプロパティの値です。
	 * @param i_is_alloc
	 * @throws NyARException
	 */
	public LrlsGsRaster(int i_width, int i_height,int i_resolution,boolean i_is_alloc) throws NyARException
	{
		super(i_width,i_height,i_is_alloc);
		this._vr=new NyARVectorReader_INT1D_GRAY_8(this);
		this.resolution=i_resolution;
	}
	/**
	 *　追加機能-無し。
	 * @throws NyARException 
	 */
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		super.wrapBuffer(i_ref_buf);
		this._vr.switchBuffer(i_ref_buf);
	}
	public NyARVectorReader_INT1D_GRAY_8 getVectorReader()
	{
		return this._vr;
	}
}