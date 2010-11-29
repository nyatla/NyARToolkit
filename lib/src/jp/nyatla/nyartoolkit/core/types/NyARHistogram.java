package jp.nyatla.nyartoolkit.core.types;

/**
 * ヒストグラムを格納するクラスです。
 */
public class NyARHistogram
{
	/**
	 * サンプリング値の格納変数
	 */
	public final int[] data;
	/**
	 * 有効なサンプリング値の範囲。[0-data.length-1]
	 */
	public int length;
	/**
	 * 有効なサンプルの総数 data[i]
	 */
	public int total_of_data;
	
	
	
	public NyARHistogram(int i_length)
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
	/**
	 * 指定したi_pos未満サンプルを０にします。
	 * @param i_pos
	 */
	public void lowCut(int i_pos)
	{
		int s=0;
		for(int i=0;i<i_pos;i++){
			s+=this.data[i];
			this.data[i]=0;
		}
		this.total_of_data-=s;
	}
	/**
	 * 指定したi_pos以上のサンプルを０にします。
	 * @param i_pos
	 */
	public void highCut(int i_pos)
	{
		int s=0;
		for(int i=this.length-1;i>=i_pos;i--){
			s+=this.data[i];
			this.data[i]=0;
		}
		this.total_of_data-=s;
	}
	/**
	 * 最小の値が格納されているサンプル番号を返します。
	 */
	public int getMinSample()
	{
		int[] data=this.data;
		int ret=this.length-1;
		int min=data[ret];
		for(int i=this.length-2;i>=0;i--)
		{
			if(data[i]<min){
				min=data[i];
				ret=i;
			}
		}
		return ret;
	}
	/**
	 * サンプルの中で最小の値を返します。
	 * @return
	 */
	public int getMinData()
	{
		return this.data[this.getMinSample()];
	}
	/**
	 * 平均値を計算します。
	 * @return
	 */
	public int getAverage()
	{
		long sum=0;
		for(int i=this.length-1;i>=0;i--)
		{
			sum+=this.data[i]*i;
		}
		return (int)(sum/this.total_of_data);
	}
	
}
