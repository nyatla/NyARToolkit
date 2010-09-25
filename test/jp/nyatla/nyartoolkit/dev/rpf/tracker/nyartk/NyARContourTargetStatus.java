package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.core.types.NyARPointVector2d;

/**
 * 輪郭ソース1個を格納するクラスです。
 *
 */
public class NyARContourTargetStatus extends NyARTargetStatus
{
	/**
	 * 輪郭ベクトル配列を格納するクラスです。
	 */
	public static class CoordData extends NyARPointVector2d
	{
		/**
		 * ベクトルの2乗値です。輪郭の強度値にもなります。
		 */
		public double sq_dist;
		public static CoordData[] createArray(int i_length)
		{
			CoordData[] r=new CoordData[i_length];
			for(int i=0;i<i_length;i++){
				r[i]=new CoordData();
			}
			return r;
		}
	}
	/**
	 * @Override
	 * 引数なしの関数は使用を禁止します。
	 */
	public NyARNewTargetStatus newObject()
	{
		return null;
	}	
	/**
	 * ベクトル要素を格納する配列です。
	 */
	public CoordData[] vecpos=CoordData.createArray(100);
	/**
	 * ベクトル配列の有効長です。
	 */
	public int vecpos_length;
	//
	//制御部
	
	/**
	 * @Override
	 */
	public NyARContourTargetStatus(INyARManagedObjectPoolOperater i_ref_pool_operator)
	{
		super(i_ref_pool_operator);
	}
	/**
	 * 輪郭配列から、から、キーのベクトル(絶対値の大きいベクトル)を順序を壊さずに抽出します。
	 * @param i_vecpos
	 * 抽出元の
	 * @param i_len
	 * @param o_index
	 * インデクス番号を受け取る配列。受け取るインデックスの個数は、この配列の数と同じになります。
	 */
	public void getKeyCoordIndexes(int i_len,int[] o_index)
	{
		CoordData[] vp=this.vecpos;
		assert(o_index.length<vp.length);
		int i;
		int out_len=o_index.length;
		int out_len_1=out_len-1;
		for(i=out_len-1;i>=0;i--){
			o_index[i]=0;			
		}
		//sqdistでソートする
		for(i=0;i<out_len_1;i++){
			if(vp[o_index[i]].sq_dist<vp[o_index[i+1]].sq_dist){
				int t=o_index[i];
				o_index[i]=o_index[i+1];
				o_index[i+1]=t;
				i=0;
			}
		}
		//先に4個をsq_distでソートしながら格納
		for(i=out_len;i<i_len;i++){
			//配列の値と比較
			for(int i2=out_len_1;i2>=0;i2--){
				if(vp[i].sq_dist>vp[o_index[i2]].sq_dist){				
					//値挿入の為のシフト
					for(int i3=out_len-2;i3>i2;i3--){
						o_index[i3+1]=o_index[i3];
					}
					//設定
					o_index[i2]=i;
				}
			}
		}
		//idxでソート
		for(i=0;i<out_len_1;i++){
			if(o_index[i]<o_index[i+1]){
				int t=o_index[i];
				o_index[i]=o_index[i+1];
				o_index[i+1]=t;
				i=0;
			}
		}
		return;
	}
}
