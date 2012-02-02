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
package jp.nyatla.nyartoolkit.core.rasterreader;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.types.*;

public interface INyARPerspectiveReader
{
	/**
	 * この関数は、インスタンスが、入力ラスタと出力ラスタに互換性があるかを調べます。
	 * @param i_in_raster
	 * 入力ラスタを指定します。
	 * @param i_out_raster
	 * 出力ラスタを指定します。
	 * @return
	 * 互換性があるならtrueを返します。
	 */
	public boolean isCompatibleRaster(INyARRaster i_in_raster,INyARRaster i_out_raster);
	/**
	 * この関数は、入力ラスタの4頂点(i_vertexs)でかこまれた領域の画像を射影変換して、o_outへ格納します。
	 * @param i_in_raster
	 * このラスタの形式は、コンストラクタで制限したものと一致している必要があります。(制限した場合のみ)
	 * @param i_vertex
	 * 4頂点を格納した配列です。
	 * @param i_edge_x
	 * X方向のエッジ割合です。0-99の数値を指定します。
	 * @param i_edge_y
	 * Y方向のエッジ割合です。0-99の数値を指定します。
	 * @param i_resolution
	 * 出力の1ピクセルあたりのサンプリング数を指定します。例えば2を指定すると、出力1ピクセルあたり4ピクセルをサンプリングします。
	 * @param o_out
	 * 出力先のラスタです。
	 * @return
	 * パターンの取得に成功すると、trueを返します。
	 * @throws NyARException
	 */
	public boolean read4Point(INyARRgbRaster i_in_raster,NyARDoublePoint2d[] i_vertex,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster o_out)throws NyARException;
	/**
	 * この関数は、入力ラスタの4頂点(i_vertexs)でかこまれた領域の画像を射影変換して、o_outへ格納します。
	 * 2番目の引数型だけが、{@link #read4Point(INyARRgbRaster, NyARDoublePoint2d[], int, int, int, INyARRgbRaster)}と異なります。
	 * @param i_in_raster
	 * {@link #read4Point(INyARRgbRaster, NyARDoublePoint2d[], int, int, int, INyARRgbRaster)}を参照。
	 * @param i_vertex
	 * {@link #read4Point(INyARRgbRaster, NyARDoublePoint2d[], int, int, int, INyARRgbRaster)}を参照。
	 * @param i_edge_x
	 * {@link #read4Point(INyARRgbRaster, NyARDoublePoint2d[], int, int, int, INyARRgbRaster)}を参照。
	 * @param i_edge_y
	 * {@link #read4Point(INyARRgbRaster, NyARDoublePoint2d[], int, int, int, INyARRgbRaster)}を参照。
	 * @param i_resolution
	 * {@link #read4Point(INyARRgbRaster, NyARDoublePoint2d[], int, int, int, INyARRgbRaster)}を参照。
	 * @param o_out
	 * {@link #read4Point(INyARRgbRaster, NyARDoublePoint2d[], int, int, int, INyARRgbRaster)}を参照。
	 * @return
	 * {@link #read4Point(INyARRgbRaster, NyARDoublePoint2d[], int, int, int, INyARRgbRaster)}を参照。
	 * @throws NyARException
	 */
	public boolean read4Point(INyARRgbRaster i_in_raster,NyARIntPoint2d[] i_vertex,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster o_out)throws NyARException;
	/**
	 * この関数は、入力ラスタの4頂点(i_vertexs)でかこまれた領域の画像を射影変換して、o_outへ格納します。
	 * {@link #read4Point(INyARRgbRaster, NyARDoublePoint2d[], int, int, int, INyARRgbRaster)}と比較して、
	 * 4頂点を直値で指定する違いがあります。
	 * @param i_in_raster
	 * {@link #read4Point(INyARRgbRaster, NyARDoublePoint2d[], int, int, int, INyARRgbRaster)}を参照。
	 * @param i_x1
	 * 1番目の頂点の座標
	 * @param i_y1
	 * 1番目の頂点の座標
	 * @param i_x2
	 * 2番目の頂点の座標
	 * @param i_y2
	 * 2番目の頂点の座標
	 * @param i_x3
	 * 3番目の頂点の座標
	 * @param i_y3
	 * 3番目の頂点の座標
	 * @param i_x4
	 * 4番目の頂点の座標
	 * @param i_y4
	 * 4番目の頂点の座標
	 * @param i_edge_x
	 * {@link #read4Point(INyARRgbRaster, NyARDoublePoint2d[], int, int, int, INyARRgbRaster)}を参照。
	 * @param i_edge_y
	 * {@link #read4Point(INyARRgbRaster, NyARDoublePoint2d[], int, int, int, INyARRgbRaster)}を参照。
	 * @param i_resolution
	 * {@link #read4Point(INyARRgbRaster, NyARDoublePoint2d[], int, int, int, INyARRgbRaster)}を参照。
	 * @param o_out
	 * {@link #read4Point(INyARRgbRaster, NyARDoublePoint2d[], int, int, int, INyARRgbRaster)}を参照。
	 * @return
	 * {@link #read4Point(INyARRgbRaster, NyARDoublePoint2d[], int, int, int, INyARRgbRaster)}を参照。
	 * @throws NyARException
	 */
	public boolean read4Point(INyARRgbRaster i_in_raster,double i_x1,double i_y1,double i_x2,double i_y2,double i_x3,double i_y3,double i_x4,double i_y4,int i_edge_x,int i_edge_y,int i_resolution,INyARRgbRaster o_out)throws NyARException;
	
}









