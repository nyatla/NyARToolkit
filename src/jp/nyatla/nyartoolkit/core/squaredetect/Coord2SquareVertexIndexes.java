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
package jp.nyatla.nyartoolkit.core.squaredetect;

/**
 * 座標店集合（輪郭線）から、頂点リストを計算します。
 *
 */
public class Coord2SquareVertexIndexes
{
	private static final double VERTEX_FACTOR = 1.0;// 線検出のファクタ	
	private final NyARVertexCounter __getSquareVertex_wv1 = new NyARVertexCounter();
	private final NyARVertexCounter __getSquareVertex_wv2 = new NyARVertexCounter();
	public Coord2SquareVertexIndexes()
	{
		return;
	}
	/**
	 * 座標集合から、頂点候補になりそうな場所を４箇所探して、そのインデクス番号を返します。
	 * @param i_x_coord
	 * @param i_y_coord
	 * @param i_coord_num
	 * @param i_area
	 * @param o_vertex
	 * @return
	 */
	public boolean getVertexIndexes(int[] i_x_coord, int[] i_y_coord, int i_coord_num, int i_area, int[] o_vertex)
	{
		final NyARVertexCounter wv1 = this.__getSquareVertex_wv1;
		final NyARVertexCounter wv2 = this.__getSquareVertex_wv2;
		int vertex1_index=getFarPoint(i_x_coord,i_y_coord,i_coord_num,0);
		int prev_vertex_index=(vertex1_index+i_coord_num)%i_coord_num;
		int v1=getFarPoint(i_x_coord,i_y_coord,i_coord_num,vertex1_index);
		final double thresh = (i_area / 0.75) * 0.01 * VERTEX_FACTOR;

		o_vertex[0] = vertex1_index;

		if (!wv1.getVertex(i_x_coord, i_y_coord,i_coord_num, vertex1_index, v1, thresh)) {
			return false;
		}
		if (!wv2.getVertex(i_x_coord, i_y_coord,i_coord_num, v1,prev_vertex_index, thresh)) {
			return false;
		}

		int v2;
		if (wv1.number_of_vertex == 1 && wv2.number_of_vertex == 1) {
			o_vertex[1] = wv1.vertex[0];
			o_vertex[2] = v1;
			o_vertex[3] = wv2.vertex[0];
		} else if (wv1.number_of_vertex > 1 && wv2.number_of_vertex == 0) {
			//頂点位置を、起点から対角点の間の1/2にあると予想して、検索する。
			if(v1>=vertex1_index){
				v2 = (v1-vertex1_index)/2+vertex1_index;
			}else{
				v2 = ((v1+i_coord_num-vertex1_index)/2+vertex1_index)%i_coord_num;
			}
			if (!wv1.getVertex(i_x_coord, i_y_coord,i_coord_num, vertex1_index, v2, thresh)) {
				return false;
			}
			if (!wv2.getVertex(i_x_coord, i_y_coord,i_coord_num, v2, v1, thresh)) {
				return false;
			}
			if (wv1.number_of_vertex == 1 && wv2.number_of_vertex == 1) {
				o_vertex[1] = wv1.vertex[0];
				o_vertex[2] = wv2.vertex[0];
				o_vertex[3] = v1;
			} else {
				return false;
			}
		} else if (wv1.number_of_vertex == 0 && wv2.number_of_vertex > 1) {
			//v2 = (v1+ end_of_coord)/2;
			if(v1<=prev_vertex_index){
				v2 = (v1+prev_vertex_index)/2;
			}else{
				v2 = ((v1+i_coord_num+prev_vertex_index)/2)%i_coord_num;
				
			}
			if (!wv1.getVertex(i_x_coord, i_y_coord,i_coord_num, v1, v2, thresh)) {
				return false;
			}
			if (!wv2.getVertex(i_x_coord, i_y_coord,i_coord_num, v2, prev_vertex_index, thresh)) {
				return false;
			}
			if (wv1.number_of_vertex == 1 && wv2.number_of_vertex == 1) {
				o_vertex[1] = v1;
				o_vertex[2] = wv1.vertex[0];
				o_vertex[3] = wv2.vertex[0];
			} else {
				
				return false;
			}
		} else {
			return false;
		}
		return true;
	}
	/**
	 * i_pointの輪郭座標から、最も遠方にある輪郭座標のインデクスを探します。
	 * @param i_xcoord
	 * @param i_ycoord
	 * @param i_coord_num
	 * @return
	 */
	private static int getFarPoint(int[] i_coord_x, int[] i_coord_y,int i_coord_num,int i_point)
	{
		//
		final int sx = i_coord_x[i_point];
		final int sy = i_coord_y[i_point];
		int d = 0;
		int w, x, y;
		int ret = 0;
		for (int i = i_point+1; i < i_coord_num; i++) {
			x = i_coord_x[i] - sx;
			y = i_coord_y[i] - sy;
			w = x * x + y * y;
			if (w > d) {
				d = w;
				ret = i;
			}
		}
		for (int i = 0; i < i_point; i++) {
			x = i_coord_x[i] - sx;
			y = i_coord_y[i] - sy;
			w = x * x + y * y;
			if (w > d) {
				d = w;
				ret = i;
			}
		}		
		return ret;
	}	
}