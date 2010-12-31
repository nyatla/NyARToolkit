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
 * 遠近法を用いたPerspectiveパラメータを計算するクラスのテンプレートです。
 * 任意頂点四角系と矩形から、遠近法の変形パラメータを計算します。
 * このクラスはリファレンス実装のため、パフォーマンスが良くありません。実際にはNyARPerspectiveParamGenerator_O1を使ってください。
 */
public abstract class NyARPerspectiveParamGenerator
{
	protected int _local_x;
	protected int _local_y;
	/**
	 * コンストラクタです。
	 * @param i_local_x
	 * パラメータ計算の基準点を指定します。
	 * @param i_local_y
	 * パラメータ計算の基準点を指定します。
	 */
	public NyARPerspectiveParamGenerator(int i_local_x,int i_local_y)
	{
		this._local_x=i_local_x;
		this._local_y=i_local_y;
		return;
	}
	/**
	 * 遠近法のパラメータを計算します。
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
	 * 遠近法のパラメータを計算します。
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
	 * 遠近法のパラメータを計算します。
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
	 * 遠近法のパラメータを計算します。
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
	 * 遠近法のパラメータを計算します。継承クラスで実装してください。
	 * @param i_dest_w
	 * @param i_dest_h
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 * @param x4
	 * @param y4
	 * @param o_param
	 * @return
	 * @throws NyARException
	 */
	public abstract boolean getParam(int i_dest_w,int i_dest_h,double x1,double y1,double x2,double y2,double x3,double y3,double x4,double y4,double[] o_param)throws NyARException;
}
