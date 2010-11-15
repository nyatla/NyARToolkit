/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.core.pca2d.NyARPca2d_MatrixPCA_O2;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.dev.rpf.utils.VecLinearCoordinates;

/**
 * グレイスケールラスタに対する、特殊な画素アクセス手段を提供します。
 *
 */
public class CopyOfNyARVectorReader_INT1D_GRAY_8 extends NyARVectorReader_INT1D_GRAY_8
{
	
	/**
	 * 
	 * @param i_ref_raster
	 * 基本画像
	 * @param i_ref_rob_raster
	 * エッジ探索用のROB画像
	 */
	public CopyOfNyARVectorReader_INT1D_GRAY_8(NyARGrayscaleRaster i_ref_raster,NyARGrayscaleRaster i_ref_rob_raster)
	{
		super(i_ref_raster,i_ref_rob_raster);
		assert (i_ref_raster.getBufferType() == NyARBufferType.INT1D_GRAY_8);
	}



	private VecLinearCoordinates.NyARVecLinearPoint[] _tmp_cd = VecLinearCoordinates.NyARVecLinearPoint.createArray(3);

	/**
	 * 輪郭線を取得します。
	 * 取得アルゴリズムは、以下の通りです。
	 * 1.輪郭座標(n)の画素周辺の画素ベクトルを取得。
	 * 2.輪郭座標(n+1)周辺の画素ベクトルと比較。
	 * 3.差分が一定以下なら、座標と強度を保存
	 * 4.3点以上の集合になったら、最小二乗法で直線を計算。
	 * 5.直線の加重値を個々の画素ベクトルの和として返却。
	 */
	public boolean traceConture(NyARIntCoordinates i_coord, int i_pos_mag,int i_cell_size, VecLinearCoordinates o_coord)
	{
NyARDoublePoint2d[] pos=NyARDoublePoint2d.createArray(i_coord.items.length);
		// ベクトル化
		int MAX_COORD = o_coord.items.length;
		// 検出RECTは、x,yと(x+w),(y+h)の間にあるものになる。

		VecLinearCoordinates.NyARVecLinearPoint prev_vec_ptr, current_vec_ptr;
		VecLinearCoordinates.NyARVecLinearPoint[] tmp_cd = _tmp_cd;
		current_vec_ptr = tmp_cd[0];

		int i_coordlen = i_coord.length;
		NyARIntPoint2d[] coord = i_coord.items;

		//0個目のライン探索
		int number_of_data = 0;
		int sum = 1;
		int sq_sum=0;
		//0番目のピクセル
		sq_sum+=this.getAreaVector33(coord[0].x * i_pos_mag, coord[0].y * i_pos_mag,i_cell_size, i_cell_size,tmp_cd[0]);
		//[2]に0を保管
		tmp_cd[2].setValue(tmp_cd[0]);

		//1点目の後方探索
		pos[0].setValue(tmp_cd[0].x,tmp_cd[0].y);
		int cdx = 1;
		//1点目だけは前方と後方、両方に探索をかける。
		//前方探索の終点
		int coord_last_edge=i_coordlen;
		//後方探索
		for (int i = i_coordlen-1; i >0; i--)
		{
			prev_vec_ptr = current_vec_ptr;
			current_vec_ptr = tmp_cd[cdx % 2];
			cdx++;

			// ベクトル取得
			sq_sum+=this.getAreaVector33(coord[i].x * i_pos_mag,coord[i].y * i_pos_mag, i_cell_size, i_cell_size,current_vec_ptr);
			// 類似度判定
			if (prev_vec_ptr.getVecCos(current_vec_ptr) < _MARGE_ANG_TH) {
				//相関なし->前方探索へ。
				coord_last_edge=i;
				break;
			} else {
				//相関あり- 点の蓄積
				pos[sum].setValue(current_vec_ptr.x,current_vec_ptr.y);
				sum++;
			}
		}
		//前方探索
		current_vec_ptr=tmp_cd[2];
		for (int i = 1; i<coord_last_edge; i++)
		{
			prev_vec_ptr = current_vec_ptr;
			current_vec_ptr = tmp_cd[cdx % 2];
			cdx++;

			// ベクトル取得
			sq_sum+=this.getAreaVector33(coord[i].x * i_pos_mag,coord[i].y * i_pos_mag, i_cell_size, i_cell_size,current_vec_ptr);
			// 類似度判定
			if (prev_vec_ptr.getVecCos(current_vec_ptr) < _MARGE_ANG_TH) {
				//相関なし->新しい要素を作る。
				if(sum>3){
					o_coord.items[number_of_data].leastSquaresWithNormalize(pos,sum);
					o_coord.items[number_of_data].scalar=sq_sum;
					number_of_data++;
				}
				pos[0].setValue(current_vec_ptr.x,current_vec_ptr.y);
				sq_sum=0;
				sum=1;
			} else {
				//相関あり- 点の蓄積
					pos[sum].setValue(current_vec_ptr.x,current_vec_ptr.y);
					sum++;
			}
			// 輪郭中心を出すための計算
			if (number_of_data == MAX_COORD) {
				// 輪郭ベクトルバッファの最大を超えたら失敗
				return false;
			}
		}
		if(sum>3){
			o_coord.items[number_of_data].leastSquaresWithNormalize(pos,sum);
			o_coord.items[number_of_data].scalar=sq_sum;
			number_of_data++;
		}		
		// ベクトル化2:最後尾と先頭の要素が似ていれば連結する。
		// sq_distの合計を計算
		o_coord.length = number_of_data;
		return true;
	}

}