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
package jp.nyatla.nyartoolkit.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.rpf.utils.VecLinearCoordinates;

/**
 * グレイスケールラスタに対する、特殊な画素アクセス手段を提供します。
 *
 */
public interface INyARVectorReader
{
	/**
	 * RECT範囲内の画素ベクトルの合計値と、ベクトルのエッジ中心を取得します。 320*240の場合、
	 * RECTの範囲は(x>=0 && x<319 x+w>=0 && x+w<319),(y>=0 && y<239 x+w>=0 && x+w<319)となります。
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
	public int getAreaVector33(int ix, int iy, int iw, int ih,NyARVecLinear2d o_posvec);
	public int getAreaVector22(int ix, int iy, int iw, int ih,NyARVecLinear2d o_posvec);

	public boolean traceConture(int i_th,
			NyARIntPoint2d i_entry, VecLinearCoordinates o_coord)
			throws NyARException;

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
	public boolean traceLine(NyARIntPoint2d i_pos1, NyARIntPoint2d i_pos2,int i_edge, VecLinearCoordinates o_coord);

	public boolean traceLine(NyARDoublePoint2d i_pos1,NyARDoublePoint2d i_pos2, int i_edge, VecLinearCoordinates o_coord);

	/**
	 * 輪郭線を取得します。
	 * 取得アルゴリズムは、以下の通りです。
	 * 1.輪郭座標(n)の画素周辺の画素ベクトルを取得。
	 * 2.輪郭座標(n+1)周辺の画素ベクトルと比較。
	 * 3.差分が一定以下なら、座標と強度を保存
	 * 4.3点以上の集合になったら、最小二乗法で直線を計算。
	 * 5.直線の加重値を個々の画素ベクトルの和として返却。
	 */
	public boolean traceConture(NyARIntCoordinates i_coord, int i_pos_mag,int i_cell_size, VecLinearCoordinates o_coord);
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
		throws NyARException;
}