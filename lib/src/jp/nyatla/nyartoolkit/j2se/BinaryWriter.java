package jp.nyatla.nyartoolkit.j2se;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BinaryWriter
{
	final static public int ENDIAN_LITTLE=1;
	final static public int ENDIAN_BIG=2;
	
	private ByteBuffer _bb;
	public BinaryWriter(int i_order,int i_initial_capacity)
	{
		this._bb=ByteBuffer.allocate(i_initial_capacity);
		this.order(i_order);
	}
	private void realloc()
	{
		ByteBuffer old=this._bb;
		this._bb=ByteBuffer.allocate(old.capacity()*2);
		this._bb.put(old.array(),0,old.position());
	}
	
	public void putInt(int v)
	{
		try{
			this._bb.putInt(v);
		}catch(BufferOverflowException e){
			this.realloc();
			this._bb.putInt(v);
		}
	}
	public void putFloat(float v)
	{
		try{
			this._bb.putFloat(v);
		}catch(BufferOverflowException e){
			this.realloc();
			this._bb.putFloat(v);
		}
	}
	public void putIntArray(int[] v) {
		for(int i=0;i<v.length;i++){
			this.putInt(v[i]);
		}
	}	
	public void putFloatArray(float[] v)
	{
		for(int i=0;i<v.length;i++){
			this.putFloat(v[i]);
		}
	}
	public void putByteArray(byte[] v)
	{
		try{
			this._bb.put(v);
		}catch(BufferOverflowException e){
			this.realloc();
			this._bb.put(v);
		}
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
	 * 書き込み位置を設定する。
	 * @param i_pos
	 */
	public void position(int i_pos)
	{
		this._bb.position(i_pos);
	}	
	/**
	 * 内容をbyte配列にして返します。
	 * @return
	 */
	public byte[] getBinary()
	{
		int p=this._bb.position();
		this._bb.position(0);
		byte[] r=new byte[p];
		this._bb.get(r);
		this._bb.position(p);
		return r;
	}

}
