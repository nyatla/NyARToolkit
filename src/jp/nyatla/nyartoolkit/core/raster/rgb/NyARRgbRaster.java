package jp.nyatla.nyartoolkit.core.raster.rgb;

import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.*;

public class NyARRgbRaster extends NyARRgbRaster_BasicClass
{
	protected Object _ref_buf;

	private INyARRgbPixelReader _reader;
	private INyARBufferReader _buffer_reader;
	private void init(NyARIntSize i_size,int i_raster_type) throws NyARException
	{
		switch(i_raster_type)
		{
			case INyARBufferReader.BUFFERFORMAT_INT1D_X8R8G8B8_32:
				this._ref_buf=new int[i_size.w*i_size.h];
				this._reader=new NyARRgbPixelReader_INT1D_X8R8G8B8_32((int[])this._ref_buf,i_size);
				break;
			default:
				throw new NyARException();
		}
		this._buffer_reader=new NyARBufferReader(this._ref_buf,i_raster_type);
	}
	public NyARRgbRaster(NyARIntSize i_size,int i_raster_type) throws NyARException
	{
		super(i_size);
		init(i_size,i_raster_type);
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
}
