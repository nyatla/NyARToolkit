package jp.nyatla.nyartoolkit.core.raster.rgb;

import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.*;

public class NyARRgbRaster extends NyARRgbRaster_BasicClass
{
	protected Object _buf;
	protected INyARRgbPixelReader _reader;
	protected INyARBufferReader _buffer_reader;
	public NyARRgbRaster(NyARIntSize i_size,int i_raster_type) throws NyARException
	{
		super(i_size);
		if(!initInstance(i_size,i_raster_type)){
			throw new NyARException();
		}
		return;
	}
	public INyARRgbPixelReader getRgbPixelReader()
	{
		return this._reader;
	}
	public INyARBufferReader getBufferReader()
	{
		return this._buffer_reader;
	}
	protected boolean initInstance(NyARIntSize i_size,int i_raster_type)
	{
		switch(i_raster_type)
		{
			case INyARBufferReader.BUFFERFORMAT_INT1D_X8R8G8B8_32:
				this._buf=new int[i_size.w*i_size.h];
				this._reader=new NyARRgbPixelReader_INT1D_X8R8G8B8_32((int[])this._buf,i_size);
				break;
			default:
				return false;
		}
		this._buffer_reader=new NyARBufferReader(this._buf,i_raster_type);
		return true;
	}	
}
