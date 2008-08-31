package jp.nyatla.nyartoolkit.core.rasterreader;

public interface INyARBufferReader
{
	public final static int BUFFERTYPE_NULL = 0x00000001;

	public final static int BUFFERTYPE_BYTE1D_R8G8B8 = 0x00010001;

	public final static int BUFFERTYPE_BYTE1D_B8G8R8X8 = 0x00010002;

	public final static int BUFFERTYPE_INT1D_G8 = 0x00020001;

	public final static int BUFFERTYPE_INT2D = 0x00030001;

	public final static int BUFFERTYPE_INT2D_G1 = 0x00030002;

	public Object getBuffer();

	public int getBufferType();
}
