package jp.nyatla.nyartoolkit.core.types;

/**
 * 順データ型
 */
public class NyARHistgram
{
	/**
	 * サンプリング値の格納変数
	 */
	public int[] data;
	/**
	 * 有効なサンプリング値の範囲。[0-i_length-1]
	 */
	public int length;
	/**
	 * 有効なサンプルの総数 data[i]
	 */
	public int total_of_data;
	public NyARHistgram(int i_length)
	{
		this.data=new int[i_length];
		this.length=i_length;
		this.total_of_data=0;
	}
	/**
	 * 区間i_stからi_edまでの総データ数を返します。
	 * @param i_st
	 * @param i_ed
	 * @return
	 */
	public int getTotal(int i_st,int i_ed)
	{
		assert(i_st<i_ed && i_ed<this.length);
		int result=0;
		int[] s=this.data;
		for(int i=i_st;i<=i_ed;i++){
			result+=s[i];
		}
		return result;
	}
}
