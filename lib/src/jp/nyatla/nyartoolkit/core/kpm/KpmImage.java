package jp.nyatla.nyartoolkit.core.kpm;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class KpmImage implements INyARRaster
{
	private NyARIntSize _size;
	private float[] _buf;
	public KpmImage(int i_width, int i_height)
	{
		this._size=new NyARIntSize(i_width,i_height);
		this._buf=new float[i_width*i_height];
	}
	@Override
	public int getWidth() {
		return this._size.w;
	}

	@Override
	public int getHeight() {
		return this._size.h;
	}

	@Override
	public NyARIntSize getSize() {
		return this._size;
	}

	@Override
	public Object getBuffer() {
		return this._buf;
	}

	@Override
	public int getBufferType() {
		return NyARBufferType.USER_DEFINE;
	}

	@Override
	public boolean isEqualBufferType(int i_type_value)
	{
		return false;
	}

	@Override
	public boolean hasBuffer() {
		return true;
	}

	@Override
	public void wrapBuffer(Object i_ref_buf) {
		NyARRuntimeException.notImplement();
	}

	@Override
	public Object createInterface(Class<?> i_iid)
	{
		throw new NyARRuntimeException();
	}
	
	//これどうにかしよう
	public int get(int i_row) {
		return this._size.w*i_row;
	}

}