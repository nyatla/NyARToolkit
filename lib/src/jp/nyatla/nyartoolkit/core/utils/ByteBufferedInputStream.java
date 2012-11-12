package jp.nyatla.nyartoolkit.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import jp.nyatla.nyartoolkit.core.NyARException;

/**
 * このクラスは、{@link InputStream}からバッファリングしながら読み出します。
 *
 */
public class ByteBufferedInputStream
{
	public final static int ENDIAN_LITTLE=1;
	public final static int ENDIAN_BIG=2;
	private byte[] _buf;
	private ByteBuffer _bb;
	private InputStream _stream;
	private int _read_len;
	public ByteBufferedInputStream(InputStream i_stream,int i_buf_size)
	{
		this._buf=new byte[i_buf_size];
		this._bb=ByteBuffer.wrap(this._buf);
		this._bb.order(ByteOrder.LITTLE_ENDIAN);
		this._read_len=0;
		this._stream=i_stream;
	}
	/**
	 * マルチバイト読み込み時のエンディアン.{@link #ENDIAN_BIG}か{@link #ENDIAN_LITTLE}を設定してください。
	 * @param i_order
	 */
	public void order(int i_order)
	{
		this._bb.order(i_order==ENDIAN_LITTLE?ByteOrder.LITTLE_ENDIAN:ByteOrder.BIG_ENDIAN);
	}
	/**
	 * Streamからi_sizeだけ一時バッファへ読み出します。
	 * @param i_size
	 * @return
	 * 読み出したバイト数
	 * @throws NyARException 
	 */
	public int readToBuffer(int i_size) throws NyARException
	{
		assert(this._read_len<this._buf.length);
		int len=0;
		try {
			len=this._stream.read(this._buf, 0, i_size);
		} catch (IOException e) {
			throw new NyARException(e);
		}
		this._bb.rewind();
		this._read_len=0;
		return len;
	}
	/**
	 * streamからi_bufへi_sizeだけ読み出します。
	 * @param i_buf
	 * @param i_size
	 * @return
	 * 読み出したバイト数
	 * @throws NyARException
	 */
	public int readBytes(byte[] i_buf,int i_size) throws NyARException
	{
		try {
			return this._stream.read(i_buf, 0, i_size);
		} catch (IOException e) {
			throw new NyARException(e);
		}
	}	
	public int getInt()
	{
		assert(this._read_len<this._buf.length);
		this._read_len+=4;
		return this._bb.getInt();
	}
	public byte getByte()
	{
		assert(this._read_len<this._buf.length);
		this._read_len+=1;
		return this._bb.get();
	}
	public float getFloat()
	{
		assert(this._read_len<this._buf.length);
		this._read_len+=4;
		return this._bb.getFloat();
	}
	public double getDouble()
	{
		assert(this._read_len<this._buf.length);
		this._read_len+=8;
		return this._bb.getDouble();
	}
}