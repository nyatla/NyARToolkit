package jp.nyatla.nyartoolkit.core.utils;

public class NyARLCGsRandomizer
{
	protected long _rand_val;
	protected int _seed;
	public NyARLCGsRandomizer(int i_seed)
	{
		this._seed=i_seed;
		this._rand_val=i_seed;
	}
	public void setSeed(int i_seed)
	{
		this._rand_val=i_seed;
	}
	public int rand()
	{
		this._rand_val = (this._rand_val * 214013L + 2531011L);
        return (int)((this._rand_val >>16) & RAND_MAX);
		
	}
	public final static int RAND_MAX=0x7fff;
}
