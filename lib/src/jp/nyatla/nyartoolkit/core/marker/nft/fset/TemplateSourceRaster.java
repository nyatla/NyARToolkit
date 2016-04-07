package jp.nyatla.nyartoolkit.core.marker.nft.fset;

import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;

/**
 * byte配列を扱うためのテンポラリラスタ。
 * @todo imgをint配列化したら削除すること最終的には削除する。
 */
public class TemplateSourceRaster extends NyARGrayscaleRaster
{
	protected int[] _buf;
	public TemplateSourceRaster(int i_width,int i_height,int[] i_buf)
	{
		super(i_width,i_height,false);
		this._buf=i_buf;
	}

	@Override
	final public Object getBuffer()
	{
		return this._buf;
	}
	@Override
	final public int getBufferType()
	{
		return NyARBufferType.BYTE1D_GRAY_8;
	}	
	/**
	 * この関数は、ラスタに外部参照バッファをセットします。
	 * 外部参照バッファを持つインスタンスでのみ使用できます。内部参照バッファを持つインスタンスでは使用できません。
	 */
	@Override
	final public void wrapBuffer(Object i_buf)
	{
		throw new UnsupportedOperationException();
	}
	@Override
	final public int[] getPixelSet(int[] i_x, int[] i_y, int i_n, int[] o_buf,int i_st_buf)
	{
		throw new UnsupportedOperationException();
	}
	@Override
	final public int getPixel(int i_x, int i_y)
	{
		final int[] buf = (int[])this._buf;
		return buf[(i_x + i_y * this._size.w)] &0xff;
	}
	@Override
	final public void setPixel(int i_x, int i_y, int i_gs)
	{
		throw new UnsupportedOperationException();
	}
	@Override
	final public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intgs)
	{
		throw new UnsupportedOperationException();
	}
}
