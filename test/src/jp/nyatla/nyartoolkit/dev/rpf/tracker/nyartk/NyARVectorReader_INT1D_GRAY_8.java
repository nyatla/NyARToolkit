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

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.rpf.utils.VecLinearCoordinates;

/**
 * グレイスケールラスタに対する、特殊な画素アクセス手段を提供します。
 *
 */
public class NyARVectorReader_INT1D_GRAY_8
{
	private int _rob_resolution;
	private NyARGrayscaleRaster _ref_base_raster;
	private NyARGrayscaleRaster _ref_rob_raster;
	NyARCameraDistortionFactor _factor;
	/**
	 * 
	 * @param i_ref_raster
	 * 基本画像
	 * @param i_ref_raster_distortion
	 * 歪み解除オブジェクト(nullの場合歪み解除を省略)
	 * @param i_ref_rob_raster
	 * エッジ探索用のROB画像
	 * @param 
	 */
	public NyARVectorReader_INT1D_GRAY_8(NyARGrayscaleRaster i_ref_raster,NyARCameraDistortionFactor i_ref_raster_distortion,NyARGrayscaleRaster i_ref_rob_raster)
	{
		assert (i_ref_raster.getBufferType() == NyARBufferType.INT1D_GRAY_8);
		this._rob_resolution=i_ref_raster.getWidth()/i_ref_rob_raster.getWidth();
		this._ref_rob_raster=i_ref_rob_raster;
		this._ref_base_raster=i_ref_raster;
		this._coord_buf = new NyARIntCoordinates((i_ref_raster.getWidth() + i_ref_raster.getHeight()) * 4);
		this._factor=i_ref_raster_distortion;
	}

	/**
	 * 画素の4近傍の画素ベクトルを取得します。 取得可能な範囲は、Rasterの1ドット内側です。 0 ,-1, 0 0, 0, 0 0 , x,
	 * 0　+ -1, y,+1 0 ,+1, 0 0, 0, 0
	 * 
	 * @param i_raster
	 * @param x
	 * @param y
	 * @param o_v
	 */
/*	未使用につきコメントアウト
	public void getPixelVector4(int x, int y, NyARIntPoint2d o_v)
	{
		assert ((x > 0) && (y > 0) && (x) < this._ref_base_raster.getWidth() && (y) < this._ref_base_raster.getHeight());
		int[] buf = (int[])(this._ref_base_raster.getBuffer());
		int w = this._ref_base_raster.getWidth();
		int idx = w * y + x;
		o_v.x = (buf[idx + 1] - buf[idx - 1]) >> 1;
		o_v.y = (buf[idx + w] - buf[idx - w]) >> 1;
		//歪み補正どうすんの
	}
*/
	/**
	 * 画素の8近傍画素ベクトルを取得します。 取得可能な範囲は、Rasterの1ドット内側です。
	 *  -1,-2,-1　　　　-1,　0,+1
	 *   0, y,　0 +　　-2, x,+2
	 *  +1,+2,+1 　　　　-1, 0,+1
	 * 
	 * @param i_raster
	 * @param x
	 * @param y
	 * @param o_v
	 */
/*  未使用につきコメントアウト
	public void getPixelVector8(int x, int y, NyARIntPoint2d o_v) {
		assert ((x > 0) && (y > 0) && (x) < this._ref_base_raster.getWidth() && (y) < this._ref_base_raster.getHeight());
		int[] buf = (int[])this._ref_base_raster.getBuffer();
		int sw = this._ref_base_raster.getWidth();
		int idx_0 = sw * y + x;
		int idx_p1 = idx_0 + sw;
		int idx_m1 = idx_0 - sw;
		int b = buf[idx_m1 - 1];
		int d = buf[idx_m1 + 1];
		int h = buf[idx_p1 - 1];
		int f = buf[idx_p1 + 1];
		o_v.x = ((buf[idx_0 + 1] - buf[idx_0 - 1]) >> 1)
				+ ((d - b + f - h) >> 2);
		o_v.y = ((buf[idx_p1] - buf[idx_m1]) >> 1) + ((f - d + h - b) >> 2);
		//歪み補正どうするの？
	}
*/
	/**
	 * RECT範囲内の画素ベクトルの合計値と、ベクトルのエッジ中心を取得します。 320*240の場合、RECTの範囲は(x>=0 && x<319
	 * x+w>=0 && x+w<319),(y>=0 && y<239 x+w>=0 && x+w<319)となります。
	 * 
	 * @param ix
	 * ピクセル取得を行う位置を設定します。
	 * @param iy
	 * ピクセル取得を行う位置を設定します。
	 * @param iw
	 * ピクセル取得を行う範囲を設定します。
	 * @param ih
	 * ピクセル取得を行う範囲を設定します。
	 * @param o_posvec
	 * エッジ中心とベクトルを返します。
	 * @return
	 * ベクトルの強度を返します。強度値は、差分値の二乗の合計です。
	 */
	public final double getAreaVector33(int ix, int iy, int iw, int ih,NyARVecLinear2d o_posvec)
	{
		assert (ih >= 3 && iw >= 3);
		assert ((ix >= 0) && (iy >= 0) && (ix + iw) <= this._ref_base_raster.getWidth() && (iy + ih) <= this._ref_base_raster.getHeight());
		int[] buf =(int[])this._ref_base_raster.getBuffer();
		int stride =this._ref_base_raster.getWidth();
		// x=(Σ|Vx|*Xn)/n,y=(Σ|Vy|*Yn)/n
		// x=(ΣVx)^2/(ΣVx+ΣVy)^2,y=(ΣVy)^2/(ΣVx+ΣVy)^2
		int sum_x, sum_y, sum_wx, sum_wy, sum_vx, sum_vy;
		sum_x = sum_y = sum_wx = sum_wy = sum_vx = sum_vy = 0;
		int vx, vy;
		for (int i = ih - 3; i >= 0; i--) {
			for (int i2 = iw - 3; i2 >= 0; i2--) {
				// 1ビット分のベクトルを計算
				int idx_0 = stride * (i + 1 + iy) + (i2 + 1 + ix);
				int idx_p1 = idx_0 + stride;
				int idx_m1 = idx_0 - stride;
				int b = buf[idx_m1 - 1];
				int d = buf[idx_m1 + 1];
				int h = buf[idx_p1 - 1];
				int f = buf[idx_p1 + 1];
				vx = ((buf[idx_0 + 1] - buf[idx_0 - 1]) >> 1)+ ((d - b + f - h) >> 2);
				vy = ((buf[idx_p1] - buf[idx_m1]) >> 1)+ ((f - d + h - b) >> 2);

				// 加重はvectorの絶対値
				int wx = vx * vx;
				int wy = vy * vy;
				sum_wx += wx; //加重値
				sum_wy += wy; //加重値
				sum_vx += wx * vx; //加重*ベクトルの積
				sum_vy += wy * vy; //加重*ベクトルの積
				sum_x += wx * (i2 + 1);//位置
				sum_y += wy * (i + 1); //
			}
		}
		//x,dx,y,dyの計算
		double xx,yy;
		if (sum_wx == 0) {
			xx = ix + (iw >> 1);
			o_posvec.dx = 0;
		} else {
			xx = ix+(double) sum_x / sum_wx;
			o_posvec.dx = (double) sum_vx / sum_wx;
		}
		if (sum_wy == 0) {
			yy = iy + (ih >> 1);
			o_posvec.dy = 0;
		} else {
			yy = iy+(double) sum_y / sum_wy;
			o_posvec.dy = (double) sum_vy / sum_wy;
		}
		//必要なら歪みを解除
		if(this._factor!=null){
			this._factor.observ2Ideal(xx, yy, o_posvec);
		}else{
			o_posvec.x=xx;
			o_posvec.y=yy;
		}
		/*
		
		// 加重平均(posが0の場合の位置は中心)
		double xx,yy;
		if (sum_x == 0) {
			xx = ix + (iw >> 1);
			o_posvec.dx = 0;
		} else {
			xx = (double) sum_x / sum_wx;
			o_posvec.dx = (double) sum_vx / sum_wx;
		}
		if (sum_y == 0) {
			yy = iy + (ih >> 1);
			o_posvec.dy = 0;
		} else {
			yy = (double) sum_y / sum_wy;
			o_posvec.dy = (double) sum_vy / sum_wy;
		}*/
		//加重平均の分母を返却
		return sum_wx+sum_wy;
	}


	/**
	 * ワーク変数
	 */
	protected NyARIntCoordinates _coord_buf;
	private final NyARContourPickup _cpickup = new NyARContourPickup();
	protected final double _MARGE_ANG_TH = NyARMath.COS_DEG_8;

	public boolean traceConture(int i_th,
			NyARIntPoint2d i_entry, VecLinearCoordinates o_coord)
			throws NyARException
	{
		NyARIntCoordinates coord = this._coord_buf;
		// Robertsラスタから輪郭抽出
		if (!this._cpickup.getContour(this._ref_rob_raster, i_th, i_entry.x, i_entry.y,
				coord)) {
			// 輪郭線MAXならなにもできないね。
			return false;

		}
		// 輪郭線のベクトル化
		return traceConture(coord, this._rob_resolution,
				this._rob_resolution * 2, o_coord);
	}



	/**
	 * 点1と点2の間に線分を定義して、その線分上のベクトルを得ます。点は、画像の内側でなければなりません。 320*240の場合、(x>=0 &&
	 * x<320 x+w>0 && x+w<320),(y>0 && y<240 y+h>=0 && y+h<=319)となります。
	 * 
	 * @param i_pos1
	 *            点1の座標です。
	 * @param i_pos2
	 *            点2の座標です。
	 * @param i_area
	 *            ベクトルを検出するカーネルサイズです。1の場合(n*2-1)^2のカーネルになります。 点2の座標です。
	 * @param o_coord
	 *            結果を受け取るオブジェクトです。
	 * @return
	 * @throws NyARException
	 */
	public boolean traceLine(NyARIntPoint2d i_pos1, NyARIntPoint2d i_pos2,int i_edge, VecLinearCoordinates o_coord)
	{
		NyARIntCoordinates coord = this._coord_buf;
		NyARIntSize base_s=this._ref_base_raster.getSize();
		// (i_area*2)の矩形が範囲内に収まるように線を引く
		// 移動量

		// 点間距離を計算
		int dist = (int) Math.sqrt(i_pos1.sqNorm(i_pos2));
		// 最低AREA*2以上の大きさが無いなら、ラインのトレースは不可能。
		if (dist < 4) {
			return false;
		}
		// dist最大数の決定
		if (dist > 14) {
			dist = 14;
		}
		// サンプリングサイズを決定(移動速度とサイズから)
		int s = i_edge * 2 + 1;
		int dx = (i_pos2.x - i_pos1.x);
		int dy = (i_pos2.y - i_pos1.y);
		int r = base_s.w - s;
		int b = base_s.h - s;

		// 最大14点を定義して、そのうち両端を除いた点を使用する。
		for (int i = 3; i < dist - 1; i++) {
			int x = i * dx / dist + i_pos1.x - i_edge;
			int y = i * dy / dist + i_pos1.y - i_edge;
			// limit
			coord.items[i - 3].x = x < 0 ? 0 : (x >= r ? r : x);
			coord.items[i - 3].y = y < 0 ? 0 : (y >= b ? b : y);
		}

		coord.length = dist - 4;
		// 点数は10点程度を得る。
		return traceConture(coord, 1, s, o_coord);
	}

	public boolean traceLine(NyARDoublePoint2d i_pos1,NyARDoublePoint2d i_pos2, int i_edge, VecLinearCoordinates o_coord)
	{
		NyARIntCoordinates coord = this._coord_buf;
		NyARIntSize base_s=this._ref_base_raster.getSize();
		// (i_area*2)の矩形が範囲内に収まるように線を引く
		// 移動量

		// 点間距離を計算
		int dist = (int) Math.sqrt(i_pos1.sqNorm(i_pos2));
		// 最低AREA*2以上の大きさが無いなら、ラインのトレースは不可能。
		if (dist < 4) {
			return false;
		}
		// dist最大数の決定
		if (dist > 14) {
			dist = 14;
		}
		// サンプリングサイズを決定(移動速度とサイズから)
		int s = i_edge * 2 + 1;
		int dx = (int) (i_pos2.x - i_pos1.x);
		int dy = (int) (i_pos2.y - i_pos1.y);
		int r = base_s.w - s;
		int b = base_s.h - s;

		// 最大24点を定義して、そのうち両端の2個を除いた点を使用する。
		for (int i = 3; i < dist - 1; i++) {
			int x = (int) (i * dx / dist + i_pos1.x - i_edge);
			int y = (int) (i * dy / dist + i_pos1.y - i_edge);
			// limit
			coord.items[i - 3].x = x < 0 ? 0 : (x >= r ? r : x);
			coord.items[i - 3].y = y < 0 ? 0 : (y >= b ? b : y);
		}

		coord.length = dist - 4;
		// 点数は10点程度を得る。
		return traceConture(coord, 1, s, o_coord);
	}

	private VecLinearCoordinates.NyARVecLinearPoint[] _tmp_cd = VecLinearCoordinates.NyARVecLinearPoint.createArray(3);

	/**
	 * 輪郭をベクトル化します。アルゴリズムは、以下の通りです。
	 * 1.輪郭座標(n)の画素周辺の画素ベクトルを取得。
	 * 2.輪郭座標(n+1)周辺の画素ベクトルと比較。
	 * 3.差分が一定以下なら、座標と強度を保存
	 * 4.画素ベクトルの和を返却。
	 * @param i_coord
	 * @param i_pos_mag
	 * @param i_cell_size
	 * @param o_coord
	 * @return
	 */
	public boolean traceConture(NyARIntCoordinates i_coord, int i_pos_mag,
			int i_cell_size, VecLinearCoordinates o_coord) {
		// ベクトル化
		VecLinearCoordinates.NyARVecLinearPoint[] array_of_vec = o_coord.items;
		int MAX_COORD = o_coord.items.length;
		// 検出RECTは、x,yと(x+w),(y+h)の間にあるものになる。

		VecLinearCoordinates.NyARVecLinearPoint prev_vec_ptr, current_vec_ptr, tmp_ptr;
		VecLinearCoordinates.NyARVecLinearPoint[] tmp_cd = _tmp_cd;
		current_vec_ptr = tmp_cd[0];

		int i_coordlen = i_coord.length;
		NyARIntPoint2d[] coord = i_coord.items;

		int number_of_data = 1;
		int sum = 1;
		// 0個目のベクトル
		this.getAreaVector33(coord[0].x * i_pos_mag, coord[0].y * i_pos_mag,i_cell_size, i_cell_size, current_vec_ptr);
		array_of_vec[0].setValue(current_vec_ptr);
		// [2]に0番目のバックアップを取る。
		tmp_cd[2].setValue(current_vec_ptr);

		//後方探索
		int cdx = 1;
		int coord_edge=i_coordlen;
		tmp_ptr = array_of_vec[0];
		for (int i = i_coordlen-1;i>0; i--){
		// ベクトル化1:vecposに線分と直行するベクトルを格納。隣接成分と似ている場合は、連結する。
			prev_vec_ptr = current_vec_ptr;
			current_vec_ptr = tmp_cd[cdx % 2];
			cdx++;
			// ベクトル取得
			this.getAreaVector33(coord[i].x * i_pos_mag,coord[i].y * i_pos_mag, i_cell_size, i_cell_size,current_vec_ptr);

			// 類似度判定
			if (prev_vec_ptr.getVecCos(current_vec_ptr) < _MARGE_ANG_TH)
			{
				//相関なし->後方探索を確定して、前方探索へ。
				coord_edge=i;
				break;
			} else {
				// 相関あり(ベクトルの統合)
				tmp_ptr.x += current_vec_ptr.x;
				tmp_ptr.y += current_vec_ptr.y;
				tmp_ptr.dx += current_vec_ptr.dx;
				tmp_ptr.dy += current_vec_ptr.dy;
				sum++;
			}
		}
		//前方探索
		current_vec_ptr = tmp_cd[2];
		for (int i = 1;i<coord_edge; i++){
			prev_vec_ptr = current_vec_ptr;
			current_vec_ptr = tmp_cd[cdx % 2];
			cdx++;

			// ベクトル取得
			this.getAreaVector33(coord[i].x * i_pos_mag,coord[i].y * i_pos_mag, i_cell_size, i_cell_size,current_vec_ptr);

			// 類似度判定
			tmp_ptr = array_of_vec[number_of_data - 1];
			if (prev_vec_ptr.getVecCos(current_vec_ptr) < _MARGE_ANG_TH) {
				// 相関なし
				// 前回までのベクトル和の調整
				tmp_ptr.x /= sum;
				tmp_ptr.y /= sum;
				// 新しいベクトル値として保管
				array_of_vec[number_of_data].setValue(current_vec_ptr);
				// 確定したので個数を+1
				number_of_data++;
				sum = 1;
			} else {
				// 相関あり(ベクトルの統合)
				tmp_ptr.x += current_vec_ptr.x;
				tmp_ptr.y += current_vec_ptr.y;
				tmp_ptr.dx += current_vec_ptr.dx;
				tmp_ptr.dy += current_vec_ptr.dy;
				sum++;
			}
			// 輪郭中心を出すための計算
			if (number_of_data == MAX_COORD) {
				// 輪郭ベクトルバッファの最大を超えたら失敗
				return false;
			}
		}
		//閉じる。
		tmp_ptr = array_of_vec[number_of_data - 1];
		tmp_ptr.x /= sum;
		tmp_ptr.y /= sum;
		// vectorのsq_distを必要なだけ計算
		double d = 0;
		for (int i = number_of_data - 1; i >= 0; i--) {
			current_vec_ptr = array_of_vec[i];
			// ベクトルの法線を取る。
			current_vec_ptr.normalVec(current_vec_ptr);
			// sqdistを計算
			current_vec_ptr.scalar = current_vec_ptr.dx * current_vec_ptr.dx+ current_vec_ptr.dy * current_vec_ptr.dy;
			d += current_vec_ptr.scalar;
		}
		// sq_distの合計を計算
		o_coord.length = number_of_data;
		return true;
	}

	private NyARIntPoint2d[] __pt = NyARIntPoint2d.createArray(2);
	private NyARLinear __temp_l = new NyARLinear();

	/**
	 * クリッピング付きのライントレーサです。
	 * 
	 * @param i_pos1
	 * @param i_pos2
	 * @param i_edge
	 * @param o_coord
	 * @return
	 * @throws NyARException
	 */
	public boolean traceLineWithClip(NyARDoublePoint2d i_pos1,
		NyARDoublePoint2d i_pos2, int i_edge, VecLinearCoordinates o_coord)
		throws NyARException
	{
		NyARIntSize s=this._ref_base_raster.getSize();
		boolean is_p1_inside_area, is_p2_inside_area;

		NyARIntPoint2d[] pt = this.__pt;
		// 線分が範囲内にあるかを確認
		is_p1_inside_area = s.isInnerPoint(i_pos1);
		is_p2_inside_area = s.isInnerPoint(i_pos2);
		// 個数で分岐
		if (is_p1_inside_area && is_p2_inside_area) {
			// 2ならクリッピング必要なし。
			if (!this.traceLine(i_pos1, i_pos2, i_edge, o_coord)) {
				return false;
			}
			return true;

		}
		// 1,0個の場合は、線分を再定義
		if (!this.__temp_l.makeLinearWithNormalize(i_pos1, i_pos2)) {
			return false;
		}
		if (!this.__temp_l.makeSegmentLine(s.w,s.h,pt)) {
			return false;
		}
		if (is_p1_inside_area != is_p2_inside_area) {
			// 1ならクリッピング後に、外に出ていた点に近い輪郭交点を得る。

			if (is_p1_inside_area) {
				// p2が範囲外
				pt[(i_pos2.sqNorm(pt[0]) < i_pos2.sqNorm(pt[1])) ? 1 : 0]
						.setValue(i_pos1);
			} else {
				// p1が範囲外
				pt[(i_pos1.sqNorm(pt[0]) < i_pos2.sqNorm(pt[1])) ? 1 : 0]
						.setValue(i_pos2);
			}
		} else {
			// 0ならクリッピングして得られた２点を使う。
			if (!this.__temp_l.makeLinearWithNormalize(i_pos1, i_pos2)) {
				return false;
			}
			if (!this.__temp_l.makeSegmentLine(s.w,s.h, pt)) {
				return false;
			}
		}
		if (!this.traceLine(pt[0], pt[1], i_edge, o_coord)) {
			return false;
		}

		return true;
	}
}