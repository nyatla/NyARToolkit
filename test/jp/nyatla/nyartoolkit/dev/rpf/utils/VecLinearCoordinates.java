package jp.nyatla.nyartoolkit.dev.rpf.utils;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.NyARVecLinear2d;

public class VecLinearCoordinates
{	
	/**
	 * データ型です。
	 * 輪郭ベクトルを格納します。
	 */
	public static class NyARVecLinearPoint extends NyARVecLinear2d
	{
		/**
		 * ベクトルの2乗値です。輪郭の強度値にもなります。
		 */
		public double sq_dist;
		public static NyARVecLinearPoint[] createArray(int i_length)
		{
			NyARVecLinearPoint[] r=new NyARVecLinearPoint[i_length];
			for(int i=0;i<i_length;i++){
				r[i]=new NyARVecLinearPoint();
			}
			return r;
		}
	}	
	public int length;
	public NyARVecLinearPoint items[];

	public VecLinearCoordinates(int i_length)
	{
		this.length = 0;
		this.items = NyARVecLinearPoint.createArray(i_length);
	}
	/**
	 * ベクトルを1,2象限に制限します。
	 */
	public final void limitQuadrantTo12()
	{
		for (int i = this.length - 1; i >= 0; i--) {
			VecLinearCoordinates.NyARVecLinearPoint target1 = this.items[i];
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
		NyARVecLinearPoint[] vp = this.items;
		assert (o_index.length <= this.length);
		int i;
		int out_len = o_index.length;
		int out_len_1 = out_len - 1;
		for (i = out_len - 1; i >= 0; i--) {
			o_index[i] = i;
		}
		// sqdistでソートする(B->S)
		for (i = 0; i < out_len_1;) {
			if (vp[o_index[i]].sq_dist < vp[o_index[i + 1]].sq_dist) {
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
				if (vp[i].sq_dist > vp[o_index[i2]].sq_dist) {
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
	public void getKeyCoord(NyARVecLinearPoint[] o_index)
	{
		NyARVecLinearPoint[] vp = this.items;
		assert (o_index.length <= this.length);
		int i;
		int out_len = o_index.length;
		int out_len_1 = out_len - 1;
		for (i = out_len - 1; i >= 0; i--) {
			o_index[i] = vp[i];
		}
		// sqdistでソートする(B->S)
		for (i = 0; i < out_len_1;) {
			if (o_index[i].sq_dist < o_index[i + 1].sq_dist) {
				NyARVecLinearPoint t = o_index[i];
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
				if (vp[i].sq_dist > o_index[i2].sq_dist) {
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
		NyARVecLinearPoint[] vp = this.items;
		int index = 0;
		double max_dist = vp[0].sq_dist;
		for (int i = this.length - 1; i > 0; i--) {
			if (max_dist < vp[i].sq_dist) {
				max_dist = vp[i].sq_dist;
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
			if(this.items[i].sq_dist!=0){
				idx++;
				continue;
			}
			for(i=i+1;i<len;i++){
				if(this.items[i].sq_dist!=0){
					NyARVecLinearPoint temp = this.items[i];
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
