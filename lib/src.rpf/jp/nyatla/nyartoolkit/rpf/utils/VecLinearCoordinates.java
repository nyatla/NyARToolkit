package jp.nyatla.nyartoolkit.rpf.utils;

import jp.nyatla.nyartoolkit.core.types.NyARVecLinear2d;

public class VecLinearCoordinates
{	
	/**
	 * データ型です。
	 * 輪郭ベクトルを格納します。
	 */
	public static class VecLinearCoordinatePoint extends NyARVecLinear2d
	{
		/**
		 * ベクトルの2乗値です。輪郭の強度値にもなります。
		 */
		public double scalar;
		public static VecLinearCoordinatePoint[] createArray(int i_length)
		{
			VecLinearCoordinatePoint[] r=new VecLinearCoordinatePoint[i_length];
			for(int i=0;i<i_length;i++){
				r[i]=new VecLinearCoordinatePoint();
			}
			return r;
		}
	}	
	public int length;
	public VecLinearCoordinatePoint[] items;

	public VecLinearCoordinates(int i_length)
	{
		this.length = 0;
		this.items = VecLinearCoordinatePoint.createArray(i_length);
	}
	/**
	 * ベクトルを1,2象限に制限します。
	 */
	public final void limitQuadrantTo12()
	{
		for (int i = this.length - 1; i >= 0; i--) {
			VecLinearCoordinates.VecLinearCoordinatePoint target1 = this.items[i];
			if (target1.dy < 0) {
				target1.dy *= -1;
				target1.dx *= -1;
			}
		}
	}
	

	/**
	 * 輪郭配列から、から、キーのベクトル(絶対値の大きいベクトル)を順序を壊さずに抽出します。
	 * 
	 * @param i_vecpos
	 *            抽出元の
	 * @param i_len
	 * @param o_index
	 *            インデクス番号を受け取る配列。受け取るインデックスの個数は、この配列の数と同じになります。
	 */
	public void getOrderdKeyCoordIndexes(int[] o_index)
	{
		getKeyCoordIndexes(o_index);
		// idxでソート
		int out_len_1 = o_index.length - 1;
		for (int i = 0; i < out_len_1;) {
			if (o_index[i] > o_index[i + 1]) {
				int t = o_index[i];
				o_index[i] = o_index[i + 1];
				o_index[i + 1] = t;
				i = 0;
				continue;
			}
			i++;
		}
		return;
	}
	public void getKeyCoordIndexes(int[] o_index)
	{
		VecLinearCoordinatePoint[] vp = this.items;
		assert (o_index.length <= this.length);
		int i;
		int out_len = o_index.length;
		int out_len_1 = out_len - 1;
		for (i = out_len - 1; i >= 0; i--) {
			o_index[i] = i;
		}
		// sqdistでソートする(B->S)
		for (i = 0; i < out_len_1;) {
			if (vp[o_index[i]].scalar < vp[o_index[i + 1]].scalar) {
				int t = o_index[i];
				o_index[i] = o_index[i + 1];
				o_index[i + 1] = t;
				i = 0;
				continue;
			}
			i++;
		}
		// 先に4個をsq_distでソートしながら格納
		for (i = out_len; i < this.length; i++) {
			// 配列の値と比較
			for (int i2 = 0; i2 < out_len; i2++) {
				if (vp[i].scalar > vp[o_index[i2]].scalar) {
					// 値挿入の為のシフト
					for (int i3 = out_len - 1; i3 > i2; i3--) {
						o_index[i3] = o_index[i3 - 1];
					}
					// 設定
					o_index[i2] = i;
					break;
				}
			}
		}
		return;
	}
	public void getKeyCoord(VecLinearCoordinatePoint[] o_index)
	{
		VecLinearCoordinatePoint[] vp = this.items;
		assert (o_index.length <= this.length);
		int i;
		int out_len = o_index.length;
		int out_len_1 = out_len - 1;
		for (i = out_len - 1; i >= 0; i--) {
			o_index[i] = vp[i];
		}
		// sqdistでソートする(B->S)
		for (i = 0; i < out_len_1;) {
			if (o_index[i].scalar < o_index[i + 1].scalar) {
				VecLinearCoordinatePoint t = o_index[i];
				o_index[i] = o_index[i + 1];
				o_index[i + 1] = t;
				i = 0;
				continue;
			}
			i++;
		}
		// 先に4個をsq_distでソートしながら格納
		for (i = out_len; i < this.length; i++) {
			// 配列の値と比較
			for (int i2 = 0; i2 < out_len; i2++) {
				if (vp[i].scalar > o_index[i2].scalar) {
					// 値挿入の為のシフト
					for (int i3 = out_len - 1; i3 > i2; i3--) {
						o_index[i3] = o_index[i3 - 1];
					}
					// 設定
					o_index[i2] = vp[i];
					break;
				}
			}
		}
		return;
	} 	
	
	/**
	 * 最も大きいベクトル成分のインデクスを返します。
	 * 
	 * @return
	 */
	public final int getMaxCoordIndex()
	{
		VecLinearCoordinatePoint[] vp = this.items;
		int index = 0;
		double max_dist = vp[0].scalar;
		for (int i = this.length - 1; i > 0; i--) {
			if (max_dist < vp[i].scalar) {
				max_dist = vp[i].scalar;
				index = i;
			}
		}
		return index;
	}


	/**
	 * ノイズレベルを指定して、ノイズ（だと思われる）ベクトルを削除します。
	 */
	/**
	 * 大きさ(sq_dist)が0のベクトルを削除して、要素を前方に詰めます。
	 */
	public void removeZeroDistItem()
	{
		//前方詰め
		int idx=0;
		final int len=this.length;
		for(int i=0;i<len;i++){
			if(this.items[i].scalar!=0){
				idx++;
				continue;
			}
			for(i=i+1;i<len;i++){
				if(this.items[i].scalar!=0){
					VecLinearCoordinatePoint temp = this.items[i];
					this.items[i]=this.items[idx];
					this.items[idx]=temp;
					idx++;
					i--;
					break;
				}
			}
		}
		this.length=idx;
		return;
	}
}
