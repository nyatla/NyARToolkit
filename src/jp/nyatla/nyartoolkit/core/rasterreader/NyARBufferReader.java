package jp.nyatla.nyartoolkit.core.rasterreader;

public class NyARBufferReader implements INyARBufferReader
{
	protected Object _buffer;
	protected int _buffer_type;
	public NyARBufferReader(Object i_buffer,int i_buffer_type)
	{
		this._buffer=i_buffer;
		this._buffer_type=i_buffer_type;
	}
	public Object getBuffer()
	{
		return this._buffer;
	}
	public int getBufferType()
	{
		return _buffer_type;
	}
	public boolean isEqualBufferType(int i_type_value)
	{
		return this._buffer_type==i_type_value;
	}
}
