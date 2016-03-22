package jp.nyatla.nyartoolkit.core.kpm.utils;


/**
 * 768bitデスクプタ
 *
 */
public class LongDescripter768 extends LongDescripter
{
	public LongDescripter768()
	{
		super(96*8);
	}

	@Override
	final public int hammingDistance(LongDescripter i_value)
	{
		assert i_value.bits==this.bits;
		long b;
		int c=0;
		for(int i=this._desc.length-1;i>=0;i--){
			b=this._desc[i]^i_value._desc[i];
	        b = (b & 0x5555555555555555L) + (b >> 1 & 0x5555555555555555L);
	        b = (b & 0x3333333333333333L) + (b >> 2 & 0x3333333333333333L);
	        b = (b & 0x0f0f0f0f0f0f0f0fL) + (b >> 4 & 0x0f0f0f0f0f0f0f0fL);
	        b = (b & 0x00ff00ff00ff00ffL) + (b >> 8 & 0x00ff00ff00ff00ffL);
	        b = (b & 0x0000ffff0000ffffL) + (b >> 16 & 0x0000ffff0000ffffL);
	        b = (b & 0x00000000ffffffffL) + (b >> 32 & 0x00000000ffffffffL);
	        c+=b;
		}
        return (int)c;
	}

}
