package jp.nyatla.nyartoolkit.j2se;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;

/**
 * バイナリデータの読出しクラス
 */
public class BinaryReader
{
	final static public int ENDIAN_LITTLE=1;
	final static public int ENDIAN_BIG=2;
	
	final private byte[] _data;
	final private ByteBuffer _bb;
	public static byte[] toArray(File i_file)
	{
		try {
			return toArray(new FileInputStream(i_file));
		} catch (IOException e) {
			throw new NyARRuntimeException(e);
		}
	}
	public static byte[] toArray(InputStream i_stream)
	{
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] tmp = new byte[1024];
			int size = 0;
				while((size = i_stream.read(tmp, 0, tmp.length)) != -1) {
					baos.write(tmp, 0, size);
				}
			baos.flush();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new NyARRuntimeException(e);
		}
	}
	public BinaryReader(InputStream i_stream,int i_order) throws IOException
	{
		this(toArray(i_stream),i_order);
	}
	public BinaryReader(byte[] i_data,int i_order)
	{
		this._data=i_data;
		this._bb=ByteBuffer.wrap(this._data);
		this.order(i_order);
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
	 * 読出し位置を設定する。
	 * @param i_pos
	 */
	public void position(int i_pos)
	{
		this._bb.position(i_pos);
	}
	/**
	 * 読出し可能なサイズを返す。
	 * @return
	 */
	public int size()
	{
		return this._data.length;
	}
	public int getInt()
	{
		return this._bb.getInt();
	}
	public byte getByte()
	{
		return this._bb.get();
	}
	public byte[] getBytes(byte[] buf)
	{
		this._bb.get(buf,0,buf.length);
		return buf;
	}
	public byte[] getBytes(int i_size)
	{
		return getBytes(new byte[i_size]);
	}
	public float getFloat()
	{
		return this._bb.getFloat();
	}
	public float[] getFloats(float[] ft) {
		for(int i=0;i<ft.length;i++){
			ft[i]=this.getFloat();
		}
		return ft;
	}
	public float[] getFloats(int i_size)
	{
		return this.getFloats(new float[i_size]);
	}	
	public double getDouble()
	{
		return this._bb.getDouble();
	}
}
