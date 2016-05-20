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
	public int[] getIntArray(int[] it)
	{
		for(int i=0;i<it.length;i++){
			it[i]=this.getInt();
		}
		return it;
	}
	public int[] getIntArray(int i_size){
		return this.getIntArray(new int[i_size]);
	}
	public byte getByte()
	{
		return this._bb.get();
	}
	public byte[] getByteArray(byte[] buf)
	{
		this._bb.get(buf,0,buf.length);
		return buf;
	}
	public byte[] getByteArray(int i_size)
	{
		return getByteArray(new byte[i_size]);
	}
	public float getFloat()
	{
		return this._bb.getFloat();
	}
	public float[] getFloatArray(float[] ft) {
		for(int i=0;i<ft.length;i++){
			ft[i]=this.getFloat();
		}
		return ft;
	}
	public float[] getFloatArray(int i_size)
	{
		return this.getFloatArray(new float[i_size]);
	}	
	public double getDouble()
	{
		return this._bb.getDouble();
	}
	/**
	 * bにi_length個のdouble値を読み出します。
	 * 読みだした値はbの先頭から格納されます。
	 * @param b
	 * @param i_lengrh
	 * @return
	 */
	public double[] getDoubleArray(double[] b, int i_length) {
		for(int i=0;i<i_length;i++){
			b[i]=this.getDouble();
		}
		return b;
	}
	/**
	 * bにbの長さだけdouble値を読み出します。
	 * @param b
	 * @return
	 */
	public double[] getDoubleArray(double[] b)
	{
		return this.getDoubleArray(b,b.length);
	}
	/**
	 * 新たにi_lengthサイズの配列を生成して、要素数と同じ個数のdouble値を読み出します。
	 * @param i_length
	 * @return
	 */
	public double[] getDoubleArray(int i_length)
	{
		return this.getDoubleArray(new double[i_length]);
	}
}
