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

import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.pca2d.NyARPca2d_MatrixPCA_O2;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.rpf.utils.VecLinearCoordinates;
import jp.nyatla.nyartoolkit.dev.rpf.utils.VecLinearCoordinates.NyARVecLinearPoint;

/**
 * グレイスケールラスタに対する、特殊な画素アクセス手段を提供します。
 *
 */
public class CopyOfNyARVectorReader_INT1D_GRAY_8 extends NyARVectorReader_INT1D_GRAY_8
{
	private NyARVecLinearPoint[] _tmp_coord_pos;
	
	/**
	 * 
	 * @param i_ref_raster
	 * 基本画像
	 * @param i_ref_raster_distortion
	 * 元画像のゆがみ矯正パラメータ。不要な時はnullを指定。
	 * @param i_ref_rob_raster
	 * エッジ探索用のROB画像
	 */
	public CopyOfNyARVectorReader_INT1D_GRAY_8(NyARGrayscaleRaster i_ref_raster,NyARCameraDistortionFactor i_ref_raster_distortion,NyARGrayscaleRaster i_ref_rob_raster)
	{
		super(i_ref_raster,i_ref_raster_distortion,i_ref_rob_raster);
		assert (i_ref_raster.getBufferType() == NyARBufferType.INT1D_GRAY_8);
		//座標バッファ
		this._tmp_coord_pos=NyARVecLinearPoint.createArray(this._coord_buf.items.length);

	}
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
		NyARVecLinearPoint[] pos=this._tmp_coord_pos;
		// ベクトル化
		int MAX_COORD = o_coord.items.length;
		int i_coordlen = i_coord.length;
		NyARIntPoint2d[] coord = i_coord.items;

		//0個目のライン探索
		int number_of_data = 0;
		double sq;
		long sq_sum=0;
		//0番目のピクセル
		pos[0].scalar=sq=this.getAreaVector33(coord[0].x * i_pos_mag, coord[0].y * i_pos_mag,i_cell_size, i_cell_size,pos[0]);
		sq_sum+=sq;
		//[2]に0を保管

		//1点目だけは前方と後方、両方に探索をかける。
		//前方探索の終点
		int coord_last_edge=i_coordlen;
		//後方探索
		int sum=1;
		for (int i = i_coordlen-1; i >0; i--)
		{
			// ベクトル取得
			pos[sum].scalar=sq=this.getAreaVector33(coord[i].x * i_pos_mag,coord[i].y * i_pos_mag, i_cell_size, i_cell_size,pos[sum]);
			sq_sum+=sq;
			// 類似度判定
			if (pos[sum-1].getVecCos(pos[sum]) < NyARMath.COS_DEG_10) {
				//相関なし->前方探索へ。
				coord_last_edge=i;
				break;
			} else {
				//相関あり- 点の蓄積
				sum++;
			}
		}
		//前方探索
		for (int i = 1; i<coord_last_edge; i++)
		{
			// ベクトル取得
			sq_sum+=this.getAreaVector33(coord[i].x * i_pos_mag,coord[i].y * i_pos_mag, i_cell_size, i_cell_size,pos[sum]);
			// 類似度判定
			if (pos[sum-1].getVecCos(pos[sum]) < NyARMath.COS_DEG_10) {
				//相関なし->新しい要素を作る。
				if(this.leastSquaresWithNormalize(pos,sum,o_coord.items[number_of_data],sq_sum/(sum*20))){
					number_of_data++;
				}
				//獲得した値を0へ移動
				pos[0].setValue(pos[sum]);
				sq_sum=0;
				sum=1;
			} else {
				//相関あり- 点の蓄積
				sum++;
			}
			// 輪郭中心を出すための計算
			if (number_of_data == MAX_COORD) {
				// 輪郭ベクトルバッファの最大を超えたら失敗
				return false;
			}
		}
		if(this.leastSquaresWithNormalize(pos,sum,o_coord.items[number_of_data],sq_sum/(sum*20))){
			number_of_data++;
		}		
		// ベクトル化2:最後尾と先頭の要素が似ていれば連結する。
		// sq_distの合計を計算
		o_coord.length = number_of_data;
		return true;
	}

	/**
	 * ノイズらしいベクトルを無視しながら最小二乗法でベクトルを統合する関数
	 * @param i_points
	 * @param i_number_of_data
	 * @param o_dest
	 * @param i_scale_th
	 * @return
	 */
	private final boolean leastSquaresWithNormalize(NyARVecLinearPoint[] i_points,int i_number_of_data,NyARVecLinearPoint o_dest,double i_scale_th)
	{
		int i;
		int num=0;
		double sum_xy = 0, sum_x = 0, sum_y = 0, sum_x2 = 0;
		for (i=i_number_of_data-1; i>=0; i--){
			NyARVecLinearPoint ptr=i_points[i];
			//規定より小さいスケールは除外なう
			if(ptr.scalar<i_scale_th)
			{
				continue;
			}
			double xw=ptr.x;
			sum_xy += xw * ptr.y;
			sum_x += xw;
			sum_y += ptr.y;
			sum_x2 += xw*xw;
			num++;
		}
		if(num<3){
			return false;
		}
		double la=-(num * sum_x2 - sum_x*sum_x);
		double lb=-(num * sum_xy - sum_x * sum_y);
		double cc=(sum_x2 * sum_y - sum_xy * sum_x);
		double lc=-(la*sum_x+lb*sum_y)/num;
		//交点を計算
		final double w1 = -lb * lb - la * la;
		if (w1 == 0.0) {
			return false;
		}		
		o_dest.x=((la * lc - lb * cc) / w1);
		o_dest.y= ((la * cc +lb * lc) / w1);
		o_dest.dy=-lb;
		o_dest.dx=-la;
		o_dest.scalar=num;
		return true;
	}	
}