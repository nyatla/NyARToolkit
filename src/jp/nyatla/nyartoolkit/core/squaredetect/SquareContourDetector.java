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

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.param.NyARObserv2IdealMap;
import jp.nyatla.nyartoolkit.core.pca2d.INyARPca2d;
import jp.nyatla.nyartoolkit.core.pca2d.NyARPca2d_MatrixPCA_O2;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix22;

public class SquareContourDetector
{
	private static final double VERTEX_FACTOR = 1.0;// 線検出のファクタ	
	private final double[] _xpos;
	private final double[] _ypos;	
	private final int[] __detectMarker_mkvertex = new int[5];
	private final NyARVertexCounter __getSquareVertex_wv1 = new NyARVertexCounter();
	private final NyARVertexCounter __getSquareVertex_wv2 = new NyARVertexCounter();
	private final INyARPca2d _pca;
	private final NyARDoubleMatrix22 __getSquareLine_evec=new NyARDoubleMatrix22();
	private final double[] __getSquareLine_mean=new double[2];
	private final double[] __getSquareLine_ev=new double[2];
	private final NyARObserv2IdealMap _dist_factor;
	public SquareContourDetector(NyARIntSize i_size,NyARCameraDistortionFactor i_distfactor_ref)
	{
		//歪み計算テーブルを作ると、8*width/height*2の領域を消費します。
		//領域を取りたくない場合は、i_dist_factor_refの値をそのまま使ってください。
		this._dist_factor = new NyARObserv2IdealMap(i_distfactor_ref,i_size);


		// 輪郭バッファは頂点変換をするので、輪郭バッファの２倍取る。
		this._pca=new NyARPca2d_MatrixPCA_O2();
		this._xpos=new double[i_size.w+i_size.h];//最大辺長はthis._width+this._height
		this._ypos=new double[i_size.w+i_size.h];//最大辺長はthis._width+this._height
		return;
	}

	public boolean coordToSquare(int[] i_xcoord,int[] i_ycoord,int i_st_index,int i_coord_num,int i_label_area,NyARSquare o_square) throws NyARException
	{

		final int[] mkvertex = this.__detectMarker_mkvertex;

		// 頂点情報を取得
		if (!getSquareVertex(i_xcoord, i_ycoord, i_st_index, i_coord_num, i_label_area, mkvertex)) {
			// 頂点の取得が出来なかったので破棄
			return false;
		}
		// マーカーを検出
		if (!getSquareLine(mkvertex, i_xcoord, i_ycoord, o_square)){
			// 矩形が成立しなかった。
			return false;
		}
		return true;
	}
	
	private boolean getSquareLine(int[] i_mkvertex, int[] i_xcoord, int[] i_ycoord, NyARSquare o_square) throws NyARException
	{
		final NyARLinear[] l_line = o_square.line;
		final NyARDoubleMatrix22 evec=this.__getSquareLine_evec;
		final double[] mean=this.__getSquareLine_mean;
		final double[] ev=this.__getSquareLine_ev;
	
		
		for (int i = 0; i < 4; i++) {
			final double w1 = (double) (i_mkvertex[i + 1] - i_mkvertex[i] + 1) * 0.05 + 0.5;
			final int st = (int) (i_mkvertex[i] + w1);
			final int ed = (int) (i_mkvertex[i + 1] - w1);
			final int n = ed - st + 1;
			if (n < 2) {
				// nが2以下でmatrix.PCAを計算することはできないので、エラー
				return false;
			}
			//配列作成
			this._dist_factor.observ2IdealBatch(i_xcoord, i_ycoord, st, n,this._xpos,this._ypos);
			
			//主成分分析する。
			this._pca.pca(this._xpos,this._ypos,n,evec, ev,mean);
			final NyARLinear l_line_i = l_line[i];
			l_line_i.dy = evec.m01;// line[i][0] = evec->m[1];
			l_line_i.dx = -evec.m00;// line[i][1] = -evec->m[0];
			l_line_i.c = -(l_line_i.dy * mean[0] + l_line_i.dx * mean[1]);// line[i][2] = -(line[i][0]*mean->v[0] + line[i][1]*mean->v[1]);
		}

		final NyARDoublePoint2d[] l_sqvertex = o_square.sqvertex;
		final NyARIntPoint2d[] l_imvertex = o_square.imvertex;
		for (int i = 0; i < 4; i++) {
			final NyARLinear l_line_i = l_line[i];
			final NyARLinear l_line_2 = l_line[(i + 3) % 4];
			final double w1 = l_line_2.dy * l_line_i.dx - l_line_i.dy * l_line_2.dx;
			if (w1 == 0.0) {
				return false;
			}
			l_sqvertex[i].x = (l_line_2.dx * l_line_i.c - l_line_i.dx * l_line_2.c) / w1;
			l_sqvertex[i].y = (l_line_i.dy * l_line_2.c - l_line_2.dy * l_line_i.c) / w1;
			// 頂点インデクスから頂点座標を得て保存
			l_imvertex[i].x = i_xcoord[i_mkvertex[i]];
			l_imvertex[i].y = i_ycoord[i_mkvertex[i]];
		}
		return true;
	}	
	private boolean getSquareVertex(int[] i_x_coord, int[] i_y_coord, int i_vertex1_index, int i_coord_num, int i_area, int[] o_vertex)
	{
		final NyARVertexCounter wv1 = this.__getSquareVertex_wv1;
		final NyARVertexCounter wv2 = this.__getSquareVertex_wv2;
		final int end_of_coord = i_vertex1_index + i_coord_num - 1;
		final int sx = i_x_coord[i_vertex1_index];// sx = marker_info2->x_coord[0];
		final int sy = i_y_coord[i_vertex1_index];// sy = marker_info2->y_coord[0];
		int dmax = 0;
		int v1 = i_vertex1_index;
		for (int i = 1 + i_vertex1_index; i < end_of_coord; i++) {// for(i=1;i<marker_info2->coord_num-1;i++)
			// {
			final int d = (i_x_coord[i] - sx) * (i_x_coord[i] - sx) + (i_y_coord[i] - sy) * (i_y_coord[i] - sy);
			if (d > dmax) {
				dmax = d;
				v1 = i;
			}
		}
		final double thresh = (i_area / 0.75) * 0.01 * VERTEX_FACTOR;

		o_vertex[0] = i_vertex1_index;

		if (!wv1.getVertex(i_x_coord, i_y_coord, i_vertex1_index, v1, thresh)) { // if(get_vertex(marker_info2->x_coord,marker_info2->y_coord,0,v1,thresh,wv1,&wvnum1)<
																					// 0 ) {
			return false;
		}
		if (!wv2.getVertex(i_x_coord, i_y_coord, v1, end_of_coord, thresh)) {// if(get_vertex(marker_info2->x_coord,marker_info2->y_coord,v1,marker_info2->coord_num-1,thresh,wv2,&wvnum2)
			// < 0) {
			return false;
		}

		int v2;
		if (wv1.number_of_vertex == 1 && wv2.number_of_vertex == 1) {// if(wvnum1 == 1 && wvnum2== 1) {
			o_vertex[1] = wv1.vertex[0];
			o_vertex[2] = v1;
			o_vertex[3] = wv2.vertex[0];
		} else if (wv1.number_of_vertex > 1 && wv2.number_of_vertex == 0) {// }else if( wvnum1 > 1 && wvnum2== 0) {
			//頂点位置を、起点から対角点の間の1/2にあると予想して、検索する。
			v2 = (v1-i_vertex1_index)/2+i_vertex1_index;
			if (!wv1.getVertex(i_x_coord, i_y_coord, i_vertex1_index, v2, thresh)) {
				return false;
			}
			if (!wv2.getVertex(i_x_coord, i_y_coord, v2, v1, thresh)) {
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
			//v2 = (v1-i_vertex1_index+ end_of_coord-i_vertex1_index) / 2+i_vertex1_index;
			v2 = (v1+ end_of_coord)/2;

			if (!wv1.getVertex(i_x_coord, i_y_coord, v1, v2, thresh)) {
				return false;
			}
			if (!wv2.getVertex(i_x_coord, i_y_coord, v2, end_of_coord, thresh)) {
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
		o_vertex[4] = end_of_coord;
		return true;
	}
	
	/**
	 * 輪郭線の矩形検出開始ポイントを特定して、座標を並べ替えます。
	 * 輪郭線の先頭から、対角線が最長になる点を１点検索し、それより前の区間をバッファの後方に接続します。
	 * 戻り値は対角線が最長になった点です。関数終了後、返却値+i_coord_numの要素が有効になります。
	 * @param i_xcoord
	 * @param i_ycoord
	 * @param i_coord_num
	 * @return
	 */
	public static int normalizeCoord(int[] i_coord_x, int[] i_coord_y,int i_coord_num)
	{
		//
		final int sx = i_coord_x[0];
		final int sy = i_coord_y[0];
		int d = 0;
		int w, x, y;
		int ret = 0;
		for (int i = 1; i < i_coord_num; i++) {
			x = i_coord_x[i] - sx;
			y = i_coord_y[i] - sy;
			w = x * x + y * y;
			if (w > d) {
				d = w;
				ret = i;
			}
			// ここでうまく終了条件入れられないかな。
		}		
		// vertex1を境界にして、後方に配列を連結
		System.arraycopy(i_coord_x, 1, i_coord_x, i_coord_num, ret);
		System.arraycopy(i_coord_y, 1, i_coord_y, i_coord_num, ret);
		return ret;
	}	
	
}