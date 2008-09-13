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
import jp.nyatla.nyartoolkit.core.labeling.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.param.*;





/**
 * イメージから正方形候補を検出するクラス。
 * このクラスは、arDetectMarker2.cとの置き換えになります。
 * 
 */
public class NyARSquareDetector
{
	private static final double VERTEX_FACTOR = 1.0;// 線検出のファクタ

	private static final int AR_AREA_MAX = 100000;// #define AR_AREA_MAX 100000

	private static final int AR_AREA_MIN = 70;// #define AR_AREA_MIN 70
	private final int _width;
	private final int _height;

	private final NyARLabeling_ARToolKit _labeling;

	private final NyARLabelingImage _limage;

	private final OverlapChecker _overlap_checker = new OverlapChecker();
	private final NyARCameraDistortionFactor _dist_factor_ref;

	/**
	 * 最大i_squre_max個のマーカーを検出するクラスを作成する。
	 * 
	 * @param i_param
	 */
	public NyARSquareDetector(NyARCameraDistortionFactor i_dist_factor_ref,NyARIntSize i_size) throws NyARException
	{
		this._width = i_size.w;
		this._height = i_size.h;
		this._dist_factor_ref = i_dist_factor_ref;
		this._labeling = new NyARLabeling_ARToolKit();
		this._limage = new NyARLabelingImage(this._width, this._height);
		this._labeling.attachDestination(this._limage);

		// 輪郭の最大長は画面に映りうる最大の長方形サイズ。
		int number_of_coord = (this._width + this._height) * 2;

		// 輪郭バッファは頂点変換をするので、輪郭バッファの２倍取る。
		this._max_coord = number_of_coord;
		this._xcoord = new int[number_of_coord * 2];
		this._ycoord = new int[number_of_coord * 2];
	}

	private final int _max_coord;
	private final int[] _xcoord;
	private final int[] _ycoord;

	private void normalizeCoord(int[] i_coord_x, int[] i_coord_y, int i_index, int i_coord_num)
	{
		// vertex1を境界にして、後方に配列を連結
		System.arraycopy(i_coord_x, 1, i_coord_x, i_coord_num, i_index);
		System.arraycopy(i_coord_y, 1, i_coord_y, i_coord_num, i_index);
	}

	private final int[] __detectMarker_mkvertex = new int[5];

	/**
	 * ARMarkerInfo2 *arDetectMarker2( ARInt16 *limage, int label_num, int *label_ref,int *warea, double *wpos, int *wclip,int area_max, int area_min, double
	 * factor, int *marker_num ) 関数の代替品 ラベリング情報からマーカー一覧を作成してo_marker_listを更新します。 関数はo_marker_listに重なりを除外したマーカーリストを作成します。
	 * 
	 * @param i_raster
	 * 解析する２値ラスタイメージを指定します。
	 * @param o_square_stack
	 * 抽出した正方形候補を格納するリスト
	 * @throws NyARException
	 */
	public final void detectMarker(NyARBinRaster i_raster, NyARSquareStack o_square_stack) throws NyARException
	{
		final INyARLabeling labeling_proc = this._labeling;
		final NyARLabelingImage limage = this._limage;

		// 初期化

		// マーカーホルダをリセット
		o_square_stack.clear();

		// ラベリング
		labeling_proc.labeling(i_raster);

		// ラベル数が0ならここまで
		final int label_num = limage.getLabelStack().getLength();
		if (label_num < 1) {
			return;
		}

		final NyARLabelingLabelStack stack = limage.getLabelStack();
		final NyARLabelingLabel[] labels = stack.getArray();
		
		
		// ラベルを大きい順に整列
		stack.sortByArea();

		// デカいラベルを読み飛ばし
		int i;
		for (i = 0; i < label_num; i++) {
			// 検査対象内のラベルサイズになるまで無視
			if (labels[i].area <= AR_AREA_MAX) {
				break;
			}
		}

		final int xsize = this._width;
		final int ysize = this._height;
		final int[] xcoord = this._xcoord;
		final int[] ycoord = this._ycoord;
		final int coord_max = this._max_coord;
		final int[] mkvertex = this.__detectMarker_mkvertex;
		final OverlapChecker overlap = this._overlap_checker;
		int coord_num;
		int label_area;
		NyARLabelingLabel label_pt;

		//重なりチェッカの最大数を設定
		overlap.reset(label_num);

		for (; i < label_num; i++) {
			label_pt = labels[i];
			label_area = label_pt.area;
			// 検査対象サイズよりも小さくなったら終了
			if (label_area < AR_AREA_MIN) {
				break;
			}
			// クリップ領域が画面の枠に接していれば除外
			if (label_pt.clip_l == 1 || label_pt.clip_r == xsize - 2) {// if(wclip[i*4+0] == 1 || wclip[i*4+1] ==xsize-2){
				continue;
			}
			if (label_pt.clip_t == 1 || label_pt.clip_b == ysize - 2) {// if( wclip[i*4+2] == 1 || wclip[i*4+3] ==ysize-2){
				continue;
			}
			// 既に検出された矩形との重なりを確認
			if (!overlap.check(label_pt)) {
				// 重なっているようだ。
				continue;
			}

			// 輪郭を取得
			coord_num = limage.getContour(i, coord_max, xcoord, ycoord);
			if (coord_num == coord_max) {
				// 輪郭が大きすぎる。
				continue;
			}
			//頂点候補のインデクスを取得
			final int vertex1 = scanVertex(xcoord, ycoord, coord_num);

			// 頂点候補(vertex1)を先頭に並べなおした配列を作成する。
			normalizeCoord(xcoord, ycoord, vertex1, coord_num);

			// 領域を準備する。
			NyARSquare square_ptr = o_square_stack.prePush();

			// 頂点情報を取得
			if (!getSquareVertex(xcoord, ycoord, vertex1, coord_num, label_area, mkvertex)) {
				o_square_stack.pop();// 頂点の取得が出来なかったので破棄
				continue;
			}

			if (!getSquareLine(mkvertex, xcoord, ycoord, square_ptr)) {
				// 矩形が成立しなかった。
				o_square_stack.pop();
				continue;
			}
			// 検出済の矩形の属したラベルを重なりチェックに追加する。
			overlap.push(label_pt);
		}	
		return;
	}

	/**
	 * 辺からの対角線が最長になる点を対角線候補として返す。
	 * 
	 * @param i_xcoord
	 * @param i_ycoord
	 * @param i_coord_num
	 * @return
	 */
	private int scanVertex(int[] i_xcoord, int[] i_ycoord, int i_coord_num)
	{
		final int sx = i_xcoord[0];
		final int sy = i_ycoord[0];
		int d = 0;
		int w, x, y;
		int ret = 0;
		for (int i = 1; i < i_coord_num; i++) {
			x = i_xcoord[i] - sx;
			y = i_ycoord[i] - sy;
			w = x * x + y * y;
			if (w > d) {
				d = w;
				ret = i;
			}
			// ここでうまく終了条件入れられないかな。
		}
		return ret;
	}

	private final NyARVertexCounter __getSquareVertex_wv1 = new NyARVertexCounter();

	private final NyARVertexCounter __getSquareVertex_wv2 = new NyARVertexCounter();

	/**
	 * static int arDetectMarker2_check_square( int area, ARMarkerInfo2 *marker_info2, double factor ) 関数の代替関数 OPTIMIZED STEP [450->415] o_squareに頂点情報をセットします。
	 * 
	 * @param i_x_coord
	 * @param i_y_coord
	 * @param i_vertex1_index
	 * @param i_coord_num
	 * @param i_area
	 * @param o_vertex
	 * 要素数はint[4]である事
	 * @return
	 */
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

	private final NyARMat __getSquareLine_input = new NyARMat(1, 2);

	private final NyARMat __getSquareLine_evec = new NyARMat(2, 2);

	private final NyARVec __getSquareLine_ev = new NyARVec(2);

	private final NyARVec __getSquareLine_mean = new NyARVec(2);

	/**
	 * arGetLine(int x_coord[], int y_coord[], int coord_num,int vertex[], double line[4][3], double v[4][2]) arGetLine2(int x_coord[], int y_coord[], int
	 * coord_num,int vertex[], double line[4][3], double v[4][2], double *dist_factor) の２関数の合成品です。 マーカーのvertex,lineを計算して、結果をo_squareに保管します。
	 * Optimize:STEP[424->391]
	 * 
	 * @param i_cparam
	 * @return
	 * @throws NyARException
	 */
	public boolean getSquareLine(int[] i_mkvertex, int[] i_xcoord, int[] i_ycoord, NyARSquare o_square) throws NyARException
	{
		final NyARLinear[] l_line = o_square.line;
		final NyARVec ev = this.__getSquareLine_ev; // matrixPCAの戻り値を受け取る
		final NyARVec mean = this.__getSquareLine_mean;// matrixPCAの戻り値を受け取る
		final double[] mean_array = mean.getArray();
		final NyARCameraDistortionFactor dist_factor=this._dist_factor_ref;  
		final NyARMat input = this.__getSquareLine_input;// 次処理で初期化される。
		final NyARMat evec = this.__getSquareLine_evec;// アウトパラメータを受け取るから初期化不要//new NyARMat(2,2);
		final double[][] evec_array = evec.getArray();
		for (int i = 0; i < 4; i++) {
			final double w1 = (double) (i_mkvertex[i + 1] - i_mkvertex[i] + 1) * 0.05 + 0.5;
			final int st = (int) (i_mkvertex[i] + w1);
			final int ed = (int) (i_mkvertex[i + 1] - w1);
			final int n = ed - st + 1;
			if (n < 2) {
				// nが2以下でmatrix.PCAを計算することはできないので、エラー
				return false;
			}
			// pcaの準備
			input.realloc(n, 2);
			// バッチ取得
			dist_factor.observ2IdealBatch(i_xcoord, i_ycoord, st, n, input.getArray());

			// 主成分分析
			input.matrixPCA(evec, ev, mean);
			final NyARLinear l_line_i = l_line[i];
			l_line_i.run = evec_array[0][1];// line[i][0] = evec->m[1];
			l_line_i.rise = -evec_array[0][0];// line[i][1] = -evec->m[0];
			l_line_i.intercept = -(l_line_i.run * mean_array[0] + l_line_i.rise * mean_array[1]);// line[i][2] = -(line[i][0]*mean->v[0] + line[i][1]*mean->v[1]);
		}

		final NyARDoublePoint2d[] l_sqvertex = o_square.sqvertex;
		final NyARIntPoint[] l_imvertex = o_square.imvertex;
		for (int i = 0; i < 4; i++) {
			final NyARLinear l_line_i = l_line[i];
			final NyARLinear l_line_2 = l_line[(i + 3) % 4];
			final double w1 = l_line_2.run * l_line_i.rise - l_line_i.run * l_line_2.rise;
			if (w1 == 0.0) {
				return false;
			}
			l_sqvertex[i].x = (l_line_2.rise * l_line_i.intercept - l_line_i.rise * l_line_2.intercept) / w1;
			l_sqvertex[i].y = (l_line_i.run * l_line_2.intercept - l_line_2.run * l_line_i.intercept) / w1;
			// 頂点インデクスから頂点座標を得て保存
			l_imvertex[i].x = i_xcoord[i_mkvertex[i]];
			l_imvertex[i].y = i_ycoord[i_mkvertex[i]];
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

	public boolean getVertex(int[] i_x_coord, int[] i_y_coord, int st, int ed, double i_thresh)
	{
		this.number_of_vertex = 0;
		this.thresh = i_thresh;
		this.x_coord = i_x_coord;
		this.y_coord = i_y_coord;
		return get_vertex(st, ed);
	}

	/**
	 * static int get_vertex( int x_coord[], int y_coord[], int st, int ed,double thresh, int vertex[], int *vnum) 関数の代替関数
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
		int v1 = 0;
		final int[] lx_coord = this.x_coord;
		final int[] ly_coord = this.y_coord;
		final double a = ly_coord[ed] - ly_coord[st];
		final double b = lx_coord[st] - lx_coord[ed];
		final double c = lx_coord[ed] * ly_coord[st] - ly_coord[ed] * lx_coord[st];
		double dmax = 0;
		for (int i = st + 1; i < ed; i++) {
			final double d = a * lx_coord[i] + b * ly_coord[i] + c;
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

/**
 * ラベル同士の重なり（内包関係）を調べるクラスです。 ラベルリストに内包するラベルを蓄積し、それにターゲットのラベルが内包されているか を確認します。
 */
class OverlapChecker
{
	private NyARLabelingLabel[] _labels = new NyARLabelingLabel[32];

	private int _length;

	/**
	 * 最大i_max_label個のラベルを蓄積できるようにオブジェクトをリセットする
	 * 
	 * @param i_max_label
	 */
	public void reset(int i_max_label)
	{
		if (i_max_label > this._labels.length) {
			this._labels = new NyARLabelingLabel[i_max_label];
		}
		this._length = 0;
	}

	/**
	 * チェック対象のラベルを追加する。
	 * 
	 * @param i_label_ref
	 */
	public void push(NyARLabelingLabel i_label_ref)
	{
		this._labels[this._length] = i_label_ref;
		this._length++;
	}

	/**
	 * 現在リストにあるラベルと重なっているかを返す。
	 * 
	 * @param i_label
	 * @return 何れかのラベルの内側にあるならばfalse,独立したラベルである可能性が高ければtrueです．
	 */
	public boolean check(NyARLabelingLabel i_label)
	{
		// 重なり処理かな？
		final NyARLabelingLabel[] label_pt = this._labels;
		final int px1 = (int) i_label.pos_x;
		final int py1 = (int) i_label.pos_y;
		for (int i = this._length - 1; i >= 0; i--) {
			final int px2 = (int) label_pt[i].pos_x;
			final int py2 = (int) label_pt[i].pos_y;
			final int d = (px1 - px2) * (px1 - px2) + (py1 - py2) * (py1 - py2);
			if (d < label_pt[i].area / 4) {
				// 対象外
				return false;
			}
		}
		// 対象
		return true;
	}
}