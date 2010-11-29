package jp.nyatla.nyartoolkit.core.raster.rgb;

import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.raster.*;

public class NyARRgbRaster extends NyARRgbRaster_BasicClass
{
	protected Object _buf;
	protected INyARRgbPixelReader _reader;
	/**
	 * バッファオブジェクトがアタッチされていればtrue
	 */
	protected boolean _is_attached_buffer;
	/**
	 * 
	 * @param i_width
	 * @param i_height
	 * @param i_raster_type
	 * NyARBufferTypeに定義された定数値を指定してください。
	 * @param i_is_alloc
	 * @throws NyARException
	 */
	public NyARRgbRaster(int i_width, int i_height,int i_raster_type,boolean i_is_alloc) throws NyARException
	{
		super(i_width,i_height,i_raster_type);
		if(!initInstance(this._size,i_raster_type,i_is_alloc)){
			throw new NyARException();
		}
	}
	/**
	 * 
	 * @param i_width
	 * @param i_height
	 * @param i_raster_type
	 * NyARBufferTypeに定義された定数値を指定してください。
	 * @throws NyARException
	 */
	public NyARRgbRaster(int i_width, int i_height,int i_raster_type) throws NyARException
	{
		super(i_width,i_height,i_raster_type);
		if(!initInstance(this._size,i_raster_type,true)){
			throw new NyARException();
		}
	}
	protected boolean initInstance(NyARIntSize i_size,int i_raster_type,boolean i_is_alloc)
	{
		switch(i_raster_type)
		{
			case NyARBufferType.INT1D_X8R8G8B8_32:
				this._buf=i_is_alloc?new int[i_size.w*i_size.h]:null;
				this._reader=new NyARRgbPixelReader_INT1D_X8R8G8B8_32((int[])this._buf,i_size);
				break;
			case NyARBufferType.BYTE1D_B8G8R8X8_32:
				this._buf=i_is_alloc?new byte[i_size.w*i_size.h*4]:null;
				this._reader=new NyARRgbPixelReader_BYTE1D_B8G8R8X8_32((byte[])this._buf,i_size);
				break;
			case NyARBufferType.BYTE1D_R8G8B8_24:
				this._buf=i_is_alloc?new byte[i_size.w*i_size.h*3]:null;
				this._reader=new NyARRgbPixelReader_BYTE1D_R8G8B8_24((byte[])this._buf,i_size);
				break;
			default:
				return false;
		}
		this._is_attached_buffer=i_is_alloc;
		return true;
	}
	public INyARRgbPixelReader getRgbPixelReader() throws NyARException
	{
		return this._reader;
	}
	public Object getBuffer()
	{
		return this._buf;
	}
	public boolean hasBuffer()
	{
		return this._buf!=null;
	}
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		assert(!this._is_attached_buffer);//バッファがアタッチされていたら機能しない。
		this._buf=i_ref_buf;
		//ピクセルリーダーの参照バッファを切り替える。
		this._reader.switchBuffer(i_ref_buf);
	}
}
