package jp.nyatla.nyartoolkit.core.kpm;

/**
 * 84bitデスクリプタ
 *
 */
public class LongDescripter
{
	final public long[] _desc;
	public LongDescripter(int i_bits)
	{
		//64bit単位で計算
		this._desc=new long[(i_bits+63)/64];
	}
	public void setValueBe(byte[] i_desc){
		for(int i=0;i<i_desc.length;i+=8){
			this._desc[i/8]=((0xffL&i_desc[i+0])<<56)|((0xffL&i_desc[i+1])<<48)|((0xffL&i_desc[i+2])<<40)|((0xffL&i_desc[i+3])<<32)|((0xffL&i_desc[i+4])<<24)|((0xffL&i_desc[i+5])<<16)|((0xffL&i_desc[i+6])<<8)|((0xffL&i_desc[i+7]));
		}
	}
	public void setValueLe(byte[] i_desc){
		for(int i=0;i<i_desc.length;i+=8){
			this._desc[i/8]=((0xffL&i_desc[i+7])<<56)|((0xffL&i_desc[i+6])<<48)|((0xffL&i_desc[i+5])<<40)|((0xffL&i_desc[i+4])<<32)|((0xffL&i_desc[i+3])<<24)|((0xffL&i_desc[i+2])<<16)|((0xffL&i_desc[i+1])<<8)|((0xffL&i_desc[i+0]));
		}
	}
}
