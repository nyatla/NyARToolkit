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
 * このインタフェイスは、グレイスケールラスタから画像のベクトルを読みだす関数を定義します。
 * 画像のベクトルは、n*mの領域にある画素を集計して計算する二次元のベクトルです。
 * <p>
 * このクラスは、輪郭のヒント画像(1/2^nサイズのエッジ画像)と、グレースケールの元画像を使って、
 * 画像の輪郭ベクトルを求めます。ヒント画像は、元画像を1/2^n倍したエッジ画像、又はグレースケール画像です。
 * ヒント画像のポイントに対応する元画像の領域を調査することで、その周辺のエッジ座標と、ベクトルを求めます。
 * </p>
 *
 */
public interface INyARVectorReader
{
	/**
	 * この関数は、元画像の矩形領域を集計して、その領域内のエッジ中心と、エッジの方位ベクトルを返します。
	 * 画素の集計には、3x3のカーネルを使います。
	 * 矩形領域は、例えば取得元の画像が320*240の場合、(x&gt;=0 && x&lt;=320 x+w&gt;=0 && x+w&lt;=320),(y&gt;=0 && y&lt;=240 y+h&gt;=0 && y+h&lt;=240)です。
	 * 実装クラスでは、3x3カーネルを使って、入力された矩形内の画素から、エッジ中心とベクトルを得る処理を実装します。
	 * @param ix
	 * ピクセル取得を行う位置を設定します。
	 * @param iy
	 * ピクセル取得を行う位置を設定します。
	 * @param iw
	 * ピクセル取得を行う範囲を設定します。
	 * 3以上でなければなりません。
	 * @param ih
	 * ピクセル取得を行う範囲を設定します。
	 * 3以上でなければなりません。
	 * @param o_posvec
	 * エッジ中心とベクトルを返します。
	 * @return
	 * ベクトルの強度を返します。強度値は、差分値の二乗の合計です。
	 */
	public int getAreaVector33(int ix, int iy, int iw, int ih,NyARVecLinear2d o_posvec);
	/**
	 * この関数は、{@link #getAreaVector33}のカーネルサイズ違いの関数です。
	 * 画素の集計には、2x2のカーネルを使います。
	 * この関数は実験的な関数です。実装しなくともかまいません。(その場合、0を返却ます。)
	 * 実装クラスでは、2x2カーネルを使って、入力された矩形内の画素から、エッジ中心とベクトルを得る処理を実装します。
	 * @param ix
	 * ピクセル取得を行う位置を設定します。
	 * @param iy
	 * ピクセル取得を行う位置を設定します。
	 * @param iw
	 * ピクセル取得を行う範囲を設定します。
	 * 3以上でなければなりません。
	 * @param ih
	 * ピクセル取得を行う範囲を設定します。
	 * 3以上でなければなりません。
	 * @param o_posvec
	 * エッジ中心とベクトルを返します。
	 * @return
	 * ベクトルの強度を返します。強度値は、差分値の二乗の合計です。
	 */
	public int getAreaVector22(int ix, int iy, int iw, int ih,NyARVecLinear2d o_posvec);
	/**
	 * この関数は、ヒント画像の基点から輪郭点をトレースして、元画像の輪郭ベクトルを配列に返します。
	 * 実装クラスでは、輪郭線の輪郭点毎のエッジ中心とベクトルを得る処理を実装します。
	 * @param i_th
	 * 輪郭を判定するための敷居値。0から255の範囲です。
	 * @param i_entry
	 * 輪郭点のエントリポイントです。輪郭を構成する点の一部を指定します。
	 * @param o_coord
	 * 輪郭点を出力する配列です。
	 * @return
	 * 輪郭点の抽出に成功すると、trueを返します。
	 * @throws NyARException
	 */
	public boolean traceConture(int i_th,
			NyARIntPoint2d i_entry, VecLinearCoordinates o_coord)
			throws NyARException;

	/**
	 * この関数は、元画像の点1と点2の間に線分を定義して、その線分上のベクトルを配列に得ます。
	 * 点は、画像の内側でなければなりません。 
	 * この関数はクリッピング処理を行いません。クリッピングが必要な時には、{@link #traceLineWithClip}を使います。
	 * @param i_pos1
	 * 点1の座標です。
	 * @param i_pos2
	 * 点2の座標です。
	 * @param i_edge
	 * ベクトルを検出するカーネルサイズです。(n*2+1)*(n*2+1)のカーネルになります。
	 * @param o_coord
	 * 結果を受け取るオブジェクトです。
	 * @return
	 * 抽出に成功すると、trueを返します。
	 * @throws NyARException
	 */
	public boolean traceLine(NyARIntPoint2d i_pos1, NyARIntPoint2d i_pos2,int i_edge, VecLinearCoordinates o_coord);
	/**
	 * この関数は、元画像の点1と点2の間に線分を定義して、その線分上のベクトルを配列に得ます。
	 * 点は、画像の内側でなければなりません。 
	 * この関数はクリッピング処理を行いません。クリッピングが必要な時には、{@link #traceLineWithClip}を使います。
	 * @param i_pos1
	 * 点1の座標です。
	 * @param i_pos2
	 * 点2の座標です。
	 * @param i_edge
	 * ベクトルを検出するカーネルサイズです。(n*2+1)*(n*2+1)のカーネルになります。
	 * @param o_coord
	 * 結果を受け取るオブジェクトです。
	 * @return
	 * 抽出に成功すると、trueを返します。
	 * @throws NyARException
	 */
	public boolean traceLine(NyARDoublePoint2d i_pos1,NyARDoublePoint2d i_pos2, int i_edge, VecLinearCoordinates o_coord);

	/**
	 * この関数は、カーネルサイズを指定して、ヒント画像の輪郭座標から、元画像の輪郭線のベクトルを得ます。
	 * @param i_coord
	 * ヒント画像の輪郭点
	 * @param i_pos_mag
	 * ヒント画像と元画像の倍率(1/2なら2)
	 * @param i_cell_size
	 * ベクトルの領域サイズ
	 * @param o_coord
	 * 画素ベクトルを返却するオブジェクト。
	 * @return
	 * 処理に成功するとtrue
	 */
	public boolean traceConture(NyARIntCoordinates i_coord, int i_pos_mag,int i_cell_size, VecLinearCoordinates o_coord);
	/**
	 * この関数は、クリッピング付きのライントレーサです。
	 * 元画像の点1と点2の間に線分を定義して、その線分上のベクトルを配列に得ます。
	 * 座標は、適切に画像内にクリッピングします。
	 * @param i_pos1
	 * 点1の座標です。
	 * @param i_pos2
	 * 点2の座標です。
	 * @param i_edge
	 * ベクトルを検出するカーネルサイズです。(n*2+1)*(n*2+1)のカーネルになります。
	 * @param o_coord
	 * 結果を受け取るオブジェクトです。
	 * @return
	 * 処理に成功するとtrue
	 * @throws NyARException
	 */
	public boolean traceLineWithClip(NyARDoublePoint2d i_pos1,
		NyARDoublePoint2d i_pos2, int i_edge, VecLinearCoordinates o_coord)
		throws NyARException;
}