/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core;

import jp.nyatla.nyartoolkit.NyARException;

/**
 * typedef struct { int area; double pos[2]; int coord_num; int
 * x_coord[AR_CHAIN_MAX]; int y_coord[AR_CHAIN_MAX]; int vertex[5]; }
 * ARMarkerInfo2;
 * 
 */
class NyARMarker
{
	/**
	 * メモリブロックのサイズ(32*4=128kb)
	 */
	private static final int ALLOCATE_PAGE_SIZE = 256;

	/**
	 * メモリブロックの初期サイズ
	 */
	private static final int INITIAL_SIZE = 1;

	int[] x_coord = new int[INITIAL_SIZE];

	int[] y_coord = new int[INITIAL_SIZE];

	int coord_num;

	int area;

	final double[] pos = new double[2];

	final int[] mkvertex = new int[5];

	/**
	 * coordバッファをi_chain_num以上のサイズに再アロケートする。 内容の引継ぎは行われない。
	 * 
	 * @param i_chain_num
	 */
	private void reallocCoordArray(int i_chain_num)
	{
		if (x_coord.length < i_chain_num) {
			// ALLOCATE_PAGE_SIZE単位で確保する。
			int new_size = ((i_chain_num + ALLOCATE_PAGE_SIZE - 1) / ALLOCATE_PAGE_SIZE)
					* ALLOCATE_PAGE_SIZE;
			x_coord = new int[new_size];
			y_coord = new int[new_size];
			coord_num = 0;
		}
	}

	public void setCoordXY(int i_v1, int i_coord_num, int[] i_xcoord,int[] i_ycoord)
	{
		// メモリの割り当て保障
		reallocCoordArray(i_coord_num + 1);
		// [A B C]を[B C A]に並べなおす。
		System.arraycopy(i_xcoord, i_v1, x_coord, 0, i_coord_num - i_v1);
		System.arraycopy(i_xcoord, 0, x_coord, i_coord_num - i_v1, i_v1);
		x_coord[i_coord_num] = x_coord[0];
		System.arraycopy(i_ycoord, i_v1, y_coord, 0, i_coord_num - i_v1);
		System.arraycopy(i_ycoord, 0, y_coord, i_coord_num - i_v1, i_v1);
		y_coord[i_coord_num] = y_coord[0];
		coord_num = i_coord_num + 1;
		return;
		// arGetContour関数から取り外した部分
		// int[] wx=new int[v1];//new int[Config.AR_CHAIN_MAX];
		// int[] wy=new int[v1]; //new int[Config.AR_CHAIN_MAX];
		// for(i=0;i<v1;i++) {
		// wx[i] = marker_ref.x_coord[i];//wx[i] = marker_info2->x_coord[i];
		// wy[i] = marker_ref.y_coord[i];//wy[i] = marker_info2->y_coord[i];
		// }
		// for(i=0;i<marker_ref.coord_num-v1;i++)
		// {//for(i=v1;i<marker_info2->coord_num;i++) {
		// marker_ref.x_coord[i] =
		// marker_ref.x_coord[i+v1];//marker_info2->x_coord[i-v1] =
		// marker_info2->x_coord[i];
		// marker_ref.y_coord[i] =
		// marker_ref.y_coord[i+v1];//marker_info2->y_coord[i-v1] =
		// marker_info2->y_coord[i];
		// }
		// for(i=0;i<v1;i++) {
		// marker_ref.x_coord[i-v1+marker_ref.coord_num] =
		// wx[i];//marker_info2->x_coord[i-v1+marker_info2->coord_num] = wx[i];
		// marker_ref.y_coord[i-v1+marker_ref.coord_num] =
		// wy[i];//marker_info2->y_coord[i-v1+marker_info2->coord_num] = wy[i];
		// }
		// marker_ref.x_coord[marker_ref.coord_num] =
		// marker_ref.x_coord[0];//marker_info2->x_coord[marker_info2->coord_num]
		// = marker_info2->x_coord[0];
		// marker_ref.y_coord[marker_ref.coord_num] =
		// marker_ref.y_coord[0];//marker_info2->y_coord[marker_info2->coord_num]
		// = marker_info2->y_coord[0];
		// marker_ref.coord_num++;//marker_info2->coord_num++;
	}

	private final NyARVertexCounter wk_checkSquare_wv1 = new NyARVertexCounter();

	private final NyARVertexCounter wk_checkSquare_wv2 = new NyARVertexCounter();

	/**
	 * static int arDetectMarker2_check_square( int area, ARMarkerInfo2
	 * *marker_info2, double factor ) 関数の代替関数 OPTIMIZED STEP [450->415]
	 * 
	 * @param i_area
	 * @param i_factor
	 * @return
	 */
	public boolean checkSquare(int i_area, double i_factor, double i_pos_x,double i_pos_y)
	{
		final int[] l_vertex = mkvertex;
		final int[] l_x_coord = x_coord;
		final int[] l_y_coord = y_coord;
		final NyARVertexCounter wv1 = wk_checkSquare_wv1;
		final NyARVertexCounter wv2 = wk_checkSquare_wv2;
		int sx, sy;
		int dmax, d, v1;

		int v2;// int wvnum1,wvnum2,v2;
		int i;

		final int L_coord_num_m1 = this.coord_num - 1;
		dmax = 0;
		v1 = 0;
		sx = l_x_coord[0];// sx = marker_info2->x_coord[0];
		sy = l_y_coord[0];// sy = marker_info2->y_coord[0];
		for (i = 1; i < L_coord_num_m1; i++) {// for(i=1;i<marker_info2->coord_num-1;i++)
												// {
			d = (l_x_coord[i] - sx) * (l_x_coord[i] - sx) + (l_y_coord[i] - sy)* (l_y_coord[i] - sy);
			if (d > dmax) {
				dmax = d;
				v1 = i;
			}
		}

		final double thresh = (i_area / 0.75) * 0.01 * i_factor;

		l_vertex[0] = 0;

		if (!wv1.getVertex(l_x_coord, l_y_coord, 0, v1, thresh)) { // if(get_vertex(marker_info2->x_coord,marker_info2->y_coord,0,v1,thresh,wv1,&wvnum1)< 0 ) {
			return false;
		}
		if (!wv2.getVertex(l_x_coord, l_y_coord, v1, L_coord_num_m1, thresh)) {// if(get_vertex(marker_info2->x_coord,marker_info2->y_coord,v1,marker_info2->coord_num-1,thresh,wv2,&wvnum2) < 0) {
			return false;
		}

		if (wv1.number_of_vertex == 1 && wv2.number_of_vertex == 1) {// if(wvnum1 == 1 && wvnum2== 1) {
			l_vertex[1] = wv1.vertex[0];
			l_vertex[2] = v1;
			l_vertex[3] = wv2.vertex[0];
		} else if (wv1.number_of_vertex > 1 && wv2.number_of_vertex == 0) {// }else
																			// if(
																			// wvnum1
																			// > 1 && wvnum2== 0) {
			v2 = v1 / 2;
			if (!wv1.getVertex(l_x_coord, l_y_coord, 0, v2, thresh)) {
				return false;
			}
			if (!wv2.getVertex(l_x_coord, l_y_coord, v2, v1, thresh)) {
				return false;
			}
			if (wv1.number_of_vertex == 1 && wv2.number_of_vertex == 1) {
				l_vertex[1] = wv1.vertex[0];
				l_vertex[2] = wv2.vertex[0];
				l_vertex[3] = v1;
			} else {
				return false;
			}
		} else if (wv1.number_of_vertex == 0 && wv2.number_of_vertex > 1) {
			v2 = (v1 + this.coord_num - 1) / 2;

			if (!wv1.getVertex(l_x_coord, l_y_coord, v1, v2, thresh)) {
				return false;
			}
			if (!wv2
					.getVertex(l_x_coord, l_y_coord, v2, L_coord_num_m1, thresh)) {
				return false;
			}
			if (wv1.number_of_vertex == 1 && wv2.number_of_vertex == 1) {
				l_vertex[1] = v1;
				l_vertex[2] = wv1.vertex[0];
				l_vertex[3] = wv2.vertex[0];
			} else {
				return false;
			}
		} else {
			return false;
		}
		l_vertex[4] = L_coord_num_m1;// この値使ってるの？
		//
		area = i_area;
		pos[0] = i_pos_x;
		pos[1] = i_pos_y;
		// marker_holder[marker_num2].pos[1] = wpos[i*2+1];
		return true;
	}

	private final NyARMat wk_getLine_input = new NyARMat(1, 2);

	private final NyARMat wk_getLine_evec = new NyARMat(2, 2);

	private final NyARVec wk_getLine_ev = new NyARVec(2);

	private final NyARVec wk_getLine_mean = new NyARVec(2);

	/**
	 * arGetLine(int x_coord[], int y_coord[], int coord_num,int vertex[],
	 * double line[4][3], double v[4][2]) arGetLine2(int x_coord[], int
	 * y_coord[], int coord_num,int vertex[], double line[4][3], double v[4][2],
	 * double *dist_factor) の２関数の合成品です。 マーカーのvertex,lineを計算して、結果をo_squareに保管します。
	 * Optimize:STEP[424->391]
	 * 
	 * @param i_cparam
	 * @return
	 * @throws NyARException
	 */
	public boolean getLine(NyARParam i_cparam, NyARSquare o_square)throws NyARException
	{
		double w1;
		int st, ed, n;
		int i;

		final double[][] l_line = o_square.line;
		final int[] l_mkvertex = this.mkvertex;
		final int[] l_x_coord = this.x_coord;
		final int[] l_y_coord = this.y_coord;
		final NyARVec ev = this.wk_getLine_ev; // matrixPCAの戻り値を受け取る
		final NyARVec mean = this.wk_getLine_mean;// matrixPCAの戻り値を受け取る
		final double[] mean_array = mean.getArray();
		double[] l_line_i, l_line_2;

		NyARMat input = this.wk_getLine_input;// 次処理で初期化される。
		NyARMat evec = this.wk_getLine_evec;// アウトパラメータを受け取るから初期化不要//new
											// NyARMat(2,2);
		double[][] evec_array = evec.getArray();
		for (i = 0; i < 4; i++) {
			w1 = (double) (l_mkvertex[i + 1] - l_mkvertex[i] + 1) * 0.05 + 0.5;
			st = (int) (l_mkvertex[i] + w1);
			ed = (int) (l_mkvertex[i + 1] - w1);
			n = ed - st + 1;
			if (n < 2) {
				// nが2以下でmatrix.PCAを計算することはできないので、エラーにしておく。
				return false;// throw new NyARException();
			}
			input.realloc(n, 2);
			// バッチ取得
			i_cparam.observ2IdealBatch(l_x_coord, l_y_coord, st, n, input
					.getArray());

			input.matrixPCA(evec, ev, mean);
			l_line_i = l_line[i];
			l_line_i[0] = evec_array[0][1];// line[i][0] = evec->m[1];
			l_line_i[1] = -evec_array[0][0];// line[i][1] = -evec->m[0];
			l_line_i[2] = -(l_line_i[0] * mean_array[0] + l_line_i[1]
					* mean_array[1]);// line[i][2] = -(line[i][0]*mean->v[0]
										// + line[i][1]*mean->v[1]);
		}
		// 値の保管
		final double[][] ref_sqvertex = o_square.sqvertex;
		final int[][] ref_imvertex = o_square.imvertex;
		for (i = 0; i < 4; i++) {
			l_line_i = l_line[i];
			l_line_2 = l_line[(i + 3) % 4];
			w1 = l_line_2[0] * l_line_i[1] - l_line_i[0] * l_line_2[1];
			if (w1 == 0.0) {
				return false;
			}
			//
			ref_sqvertex[i][0] = (l_line_2[1] * l_line_i[2] - l_line_i[1]* l_line_2[2])/ w1;
			ref_sqvertex[i][1] = (l_line_i[0] * l_line_2[2] - l_line_2[0]* l_line_i[2])/ w1;

			// imvertexの保管
			ref_imvertex[i][0] = x_coord[l_mkvertex[i]];
			ref_imvertex[i][1] = y_coord[l_mkvertex[i]];
		}
		return true;
	}
}

/**
 * get_vertex関数を切り離すためのクラス
 * 
 */
final class NyARVertexCounter
{
	public final int[] vertex = new int[10];// 5まで削れる

	public int number_of_vertex;

	private double thresh;

	private int[] x_coord;

	private int[] y_coord;

	public boolean getVertex(int[] i_x_coord, int[] i_y_coord, int st, int ed,double i_thresh)
	{
		this.number_of_vertex = 0;
		this.thresh = i_thresh;
		this.x_coord = i_x_coord;
		this.y_coord = i_y_coord;
		return get_vertex(st, ed);
	}

	/**
	 * static int get_vertex( int x_coord[], int y_coord[], int st, int
	 * ed,double thresh, int vertex[], int *vnum) 関数の代替関数
	 * 
	 * @param x_coord
	 * @param y_coord
	 * @param st
	 * @param ed
	 * @param thresh
	 * @return
	 */
	private boolean get_vertex(int st, int ed)
	{
		double d, dmax;
		double a, b, c;
		int i, v1 = 0;
		final int[] lx_coord = this.x_coord;
		final int[] ly_coord = this.y_coord;
		a = ly_coord[ed] - ly_coord[st];
		b = lx_coord[st] - lx_coord[ed];
		c = lx_coord[ed] * ly_coord[st] - ly_coord[ed] * lx_coord[st];
		dmax = 0;
		for (i = st + 1; i < ed; i++) {
			d = a * lx_coord[i] + b * ly_coord[i] + c;
			if (d * d > dmax) {
				dmax = d * d;
				v1 = i;
			}
		}
		if (dmax / (a * a + b * b) > thresh) {
			if (!get_vertex(st, v1)) {
				return false;
			}
			if (number_of_vertex > 5) {
				return false;
			}
			vertex[number_of_vertex] = v1;// vertex[(*vnum)] = v1;
			number_of_vertex++;// (*vnum)++;

			if (!get_vertex(v1, ed)) {
				return false;
			}
		}
		return true;
	}
}
