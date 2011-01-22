/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
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
package jp.nyatla.nyartoolkit.core.transmat.solver;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;

/**
 * このインタフェイスは、姿勢変換行列の並進ベクトルを計算する関数を定義します。
 * 並進ベクトルは、３次元座標と、射影後の二次元座標を元に計算します。
 */
public interface INyARTransportVectorSolver
{
	/**
	 * この関数は、射影変換後の２次元頂点座標をセットします。
	 * 実装クラスでは、射影変換後の２次元座標を元にしたパラメータを、インスタンスに記録する処理を書いてください。
	 * @param i_ref_vertex_2d
	 * 射影変換後の頂点座標配列。
	 * @param i_number_of_vertex
	 * 頂点座標配列の要素数
	 * @throws NyARException
	 */
	public void set2dVertex(NyARDoublePoint2d[] i_ref_vertex_2d,int i_number_of_vertex) throws NyARException;
	/**
	 * 画面座標群と3次元座標群から、平行移動量を計算します。
	 * 2d座標系は、直前に実行した{@link #set2dVertex}のものを使用します。
	 * 実装クラスでは、並進ベクトルを計算して返却する処理を書いてください。
	 * @param i_vertex3d
	 * 3次元空間の座標群を設定します。頂点の順番は、画面座標群と同じ順序で格納してください。
	 * @param o_transfer
	 * 並進ベクトルを受け取るオブジェクトを指定します。
	 * @throws NyARException
	 */
	public void solveTransportVector(NyARDoublePoint3d[] i_vertex3d,NyARDoublePoint3d o_transfer) throws NyARException;
}