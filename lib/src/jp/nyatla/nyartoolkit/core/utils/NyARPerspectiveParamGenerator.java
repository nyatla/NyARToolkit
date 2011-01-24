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
package jp.nyatla.nyartoolkit.core.utils;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、遠近法を用いたPerspectiveパラメータを計算する抽象クラスです。
 * 任意頂点の四角形と矩形から、遠近法の変形パラメータを計算します。
 * パラメータは、8個の定数値です。配列に返却します。
 * 継承クラスで、{@link #getParam}関数を実装してください。
 */
public abstract class NyARPerspectiveParamGenerator
{
	/** 射影の基点(X)*/
	protected int _local_x;
	/** 射影の基点(Y)*/
	protected int _local_y;
	/**
	 * コンストラクタです。
	 * 変換先の基準点を設定してインスタンスを作成します。
	 * @param i_local_x
	 * パラメータ計算の基準点を指定します。デフォルト値は1です。
	 * @param i_local_y
	 * パラメータ計算の基準点を指定します。デフォルト値は1です。
	 */
	public NyARPerspectiveParamGenerator(int i_local_x,int i_local_y)
	{
		this._local_x=i_local_x;
		this._local_y=i_local_y;
		return;
	}
	/**
	 * この関数は、遠近法のパラメータを計算して、返却します。
	 * @param i_size
	 * 変換先の矩形のサイズを指定します。
	 * @param i_vertex
	 * 変換元の頂点を指定します。要素数は4でなければなりません。
	 * @param o_param
	 * 射影変換パラメータの出力インスタンスを指定します。要素数は8でなければなりません。
	 * @return
	 * 成功するとtrueを返します。
	 * @throws NyARException
	 */
	public final boolean getParam(NyARIntSize i_size,NyARIntPoint2d[] i_vertex,double[] o_param)throws NyARException
	{
		assert(i_vertex.length==4);
		return this.getParam(i_size.w,i_size.h,i_vertex[0].x,i_vertex[0].y,i_vertex[1].x,i_vertex[1].y,i_vertex[2].x,i_vertex[2].y,i_vertex[3].x,i_vertex[3].y, o_param);
	}
	/**
	 * この関数は、遠近法のパラメータを計算して、返却します。
	 * @param i_size
	 * 変換先の矩形のサイズを指定します。
	 * @param i_vertex
	 * 変換元の頂点を指定します。要素数は4でなければなりません。
	 * @param o_param
	 * 射影変換パラメータの出力インスタンスを指定します。要素数は8でなければなりません。
	 * @return
	 * 成功するとtrueを返します。
	 * @throws NyARException
	 */
	public final boolean getParam(NyARIntSize i_size,NyARDoublePoint2d[] i_vertex,double[] o_param)throws NyARException
	{
		return this.getParam(i_size.w,i_size.h,i_vertex[0].x,i_vertex[0].y,i_vertex[1].x,i_vertex[1].y,i_vertex[2].x,i_vertex[2].y,i_vertex[3].x,i_vertex[3].y, o_param);
	}
	/**
	 * この関数は、遠近法のパラメータを計算して、返却します。
	 * @param i_width
	 * 変換先の矩形のサイズを指定します。
	 * @param i_height
	 * 変換先の矩形のサイズを指定します。
	 * @param i_vertex
	 * 変換元の頂点を指定します。要素数は4でなければなりません。
	 * @param o_param
	 * 射影変換パラメータの出力インスタンスを指定します。要素数は8でなければなりません。
	 * @return
	 * 成功するとtrueを返します。
	 * @throws NyARException
	 */
	public final boolean getParam(int i_width,int i_height,NyARDoublePoint2d[] i_vertex,double[] o_param)throws NyARException
	{
		return this.getParam(i_width,i_height,i_vertex[0].x,i_vertex[0].y,i_vertex[1].x,i_vertex[1].y,i_vertex[2].x,i_vertex[2].y,i_vertex[3].x,i_vertex[3].y, o_param);
	}
	/**
	 * この関数は、遠近法のパラメータを計算して、返却します。
	 * @param i_width
	 * 変換先の矩形のサイズを指定します。
	 * @param i_height
	 * 変換先の矩形のサイズを指定します。
	 * @param i_vertex
	 * 変換元の頂点を指定します。要素数は4でなければなりません。
	 * @param o_param
	 * 射影変換パラメータの出力インスタンスを指定します。要素数は8でなければなりません。
	 * @return
	 * 成功するとtrueを返します。
	 * @throws NyARException
	 */
	public final boolean getParam(int i_width,int i_height,NyARIntPoint2d[] i_vertex,double[] o_param)throws NyARException
	{
		return this.getParam(i_width,i_height,i_vertex[0].x,i_vertex[0].y,i_vertex[1].x,i_vertex[1].y,i_vertex[2].x,i_vertex[2].y,i_vertex[3].x,i_vertex[3].y, o_param);
	}
	/**
	 * この関数は、遠近法のパラメータを計算して、返却します。
	 * 実装クラスでは、遠近法のパラメータを計算する処理を実装してください。
	 * @param i_dest_w
	 * 出力先矩形の幅を指定します。
	 * @param i_dest_h
	 * 出力先矩形の高さを指定します。
	 * @param x1
	 * 変換元四角形の頂点1のX座標です。
	 * @param y1
	 * 変換元四角形の頂点1のY座標です。
	 * @param x2
	 * 変換元四角形の頂点2のX座標です。
	 * @param y2
	 * 変換元四角形の頂点2のY座標です。
	 * @param x3
	 * 変換元四角形の頂点3のX座標です。
	 * @param y3
	 * 変換元四角形の頂点3のY座標です。
	 * @param x4
	 * 変換元四角形の頂点4のX座標です。
	 * @param y4
	 * 変換元四角形の頂点4のY座標です。
	 * @param o_param
	 *　結果を受け取る配列を指定します。
	 * @return
	 * 計算に成功するとtrueを返します。
	 * @throws NyARException
	 */
	public abstract boolean getParam(int i_dest_w,int i_dest_h,double x1,double y1,double x2,double y2,double x3,double y3,double x4,double y4,double[] o_param)throws NyARException;
}
