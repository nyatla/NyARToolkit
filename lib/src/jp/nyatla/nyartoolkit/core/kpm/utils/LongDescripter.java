package jp.nyatla.nyartoolkit.core.kpm.utils;


/**
 * LongArrayのDescripter
 *
 */
public class LongDescripter
{
	final public long[] _desc;
	final public int bits;
	public LongDescripter(int i_bits)
	{
		//64bit単位で計算
		this.bits=i_bits;
		this._desc=new long[(i_bits+63)/64];
	}
	public void setValueBe(byte[] i_desc){
		for(int i=0;i<i_desc.length;i+=8){
			this._desc[i/8]=((0xffL&i_desc[i+0])<<56)|((0xffL&i_desc[i+1])<<48)|((0xffL&i_desc[i+2])<<40)|((0xffL&i_desc[i+3])<<32)|((0xffL&i_desc[i+4])<<24)|((0xffL&i_desc[i+5])<<16)|((0xffL&i_desc[i+6])<<8)|((0xffL&i_desc[i+7]));
		}
		return;
	}
	public void setValueLe(byte[] i_desc){
		for(int i=0;i<i_desc.length;i+=8){
			this._desc[i/8]=((0xffL&i_desc[i+7])<<56)|((0xffL&i_desc[i+6])<<48)|((0xffL&i_desc[i+5])<<40)|((0xffL&i_desc[i+4])<<32)|((0xffL&i_desc[i+3])<<24)|((0xffL&i_desc[i+2])<<16)|((0xffL&i_desc[i+1])<<8)|((0xffL&i_desc[i+0]));
		}
		return;
	}
	public void setValue(LongDescripter i_src) {
		assert i_src.bits==this.bits;
		System.arraycopy(i_src._desc,0,this._desc,0,this._desc.length);
		// TODO Auto-generated method stub	
	}
	/**
	 * i_valueとの間でHammingDistanceを計算します。
	 * @param i_value
	 * @return
	 */
	public int hammingDistance(LongDescripter i_value)
	{
		int r=0;
		for(int i=0;i<i_value._desc.length;i++){
			r+=Hamming.HammingDistance64(this._desc[i],i_value._desc[i]);
		}
		return r;
	}

}
