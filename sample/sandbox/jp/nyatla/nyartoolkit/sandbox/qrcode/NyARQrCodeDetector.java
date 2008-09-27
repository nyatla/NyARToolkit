package jp.nyatla.nyartoolkit.sandbox.qrcode;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.INyARSquareDetector;
import jp.nyatla.nyartoolkit.core.NyARSquare;
import jp.nyatla.nyartoolkit.core.NyARSquareStack;
import jp.nyatla.nyartoolkit.core.NyARVertexCounter;
import jp.nyatla.nyartoolkit.core.labeling.INyARLabeling;
import jp.nyatla.nyartoolkit.core.labeling.NyARLabelingImage;
import jp.nyatla.nyartoolkit.core.labeling.NyARLabelingLabel;
import jp.nyatla.nyartoolkit.core.labeling.NyARLabelingLabelStack;
import jp.nyatla.nyartoolkit.core.labeling.NyARLabeling_ARToolKit;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.pca2d.INyARPca2d;
import jp.nyatla.nyartoolkit.core.pca2d.NyARPca2d_MatrixPCA_O2;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix22;
import jp.nyatla.utils.j2se.LabelingBufferdImage;

public class NyARQrCodeDetector implements INyARSquareDetector
{
	private NyARQrCodeSymbolBinder _binder;
	private static final double VERTEX_FACTOR = 2.0;// 線検出のファクタ

	private static final int AR_AREA_MAX = 10000;

	private static final int AR_AREA_MIN = 50;

	private final int _width;

	private final int _height;

	private final NyARLabeling_ARToolKit _labeling;

	private final NyARLabelingImage _limage;

	private final NyARCameraDistortionFactor _dist_factor_ref;

	/**
	 * 最大i_squre_max個のマーカーを検出するクラスを作成する。
	 * 
	 * @param i_param
	 */
	public NyARQrCodeDetector(NyARCameraDistortionFactor i_dist_factor_ref, NyARIntSize i_size) throws NyARException
	{
		this._width = i_size.w;
		this._height = i_size.h;
		this._dist_factor_ref = i_dist_factor_ref;
		this._labeling = new NyARLabeling_ARToolKit();
		this._limage = new NyARLabelingImage(this._width, this._height);
		this._labeling.attachDestination(this._limage);
		this._binder=new NyARQrCodeSymbolBinder(i_dist_factor_ref);

		// 輪郭の最大長はMAX_COORD_NUMの2倍に制限
		int number_of_coord = MAX_COORD_NUM* 2;

		// 輪郭バッファはnumber_of_coordの2倍
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
		final NyARLabelingLabel[] labels = (NyARLabelingLabel[]) stack.getArray();

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
		final int[][] buf = (int[][]) limage.getBufferReader().getBuffer();
		final int[] indextable = limage.getIndexArray();
		int coord_num;
		int label_area;
		NyARLabelingLabel label_pt;
		NyARSquareStack wk_stack=new NyARSquareStack(100);
		wk_stack.clear();

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
			// 特徴点候補であるかを確認する。
			if (!hasQrEdgeFeature(buf, indextable, label_pt)) {
				continue;
			}

			// 輪郭を取得
			coord_num = limage.getContour(i, coord_max, xcoord, ycoord);
			if (coord_num == coord_max) {
				// 輪郭が大きすぎる。
				continue;
			}
			// 頂点候補のインデクスを取得
			final int vertex1 = scanVertex(xcoord, ycoord, coord_num);

			// 頂点候補(vertex1)を先頭に並べなおした配列を作成する。
			normalizeCoord(xcoord, ycoord, vertex1, coord_num);

			// 頂点情報を取得
			if (!getSquareVertex(xcoord, ycoord, vertex1, coord_num, label_area, mkvertex)) {
				continue;
			}
			NyARSquare square=(NyARSquare)wk_stack.prePush();
			//矩形からラインと観察座標を取得
			if(!getSquareLine(mkvertex,xcoord,ycoord,square.line,square.imvertex)){
				wk_stack.pop();
				continue;
			}
		}
		//シンボルの関連付け
		bindQrcodeEdge(wk_stack,o_square_stack);
		//エッジ同士の相関関係をしらべる。

		return;
	}

	/**
	 * QRコードのエッジグループを作る
	 * @param i_square_stack
	 */
	public void bindQrcodeEdge(NyARSquareStack i_square_stack,NyARSquareStack o_square_stack) throws NyARException
	{
		NyARSquare[] group=new NyARSquare[3];
		int number_of_edge=i_square_stack.getLength();
		if(number_of_edge<3){
			return;
		}
		NyARSquare[] sa=(NyARSquare[])i_square_stack.getArray();
		for(int i=0;i<number_of_edge-2;i++)
		{	
			group[0]=sa[i];
			for(int i2=i+1;i2<number_of_edge-1;i2++)
			{
				group[1]=sa[i2];
				for(int i3=i2+1;i3<number_of_edge;i3++){
					group[2]=sa[i3];
					//3個のエッジの関連性を確認する。
					NyARSquare new_square=(NyARSquare)o_square_stack.prePush();
					if(!this._binder.composeSquare(group,new_square)){
						o_square_stack.pop();
					}
				}
			}
		}
		return;
	}
	private static int MAX_COORD_NUM=(320+240)*2;//サイズの1/2の長方形の編程度が目安(VGAなら(320+240)*2)
	private final INyARPca2d _pca=new NyARPca2d_MatrixPCA_O2(MAX_COORD_NUM);
	private final NyARDoubleMatrix22 __getSquareLine_evec=new NyARDoubleMatrix22();
	private final NyARDoublePoint2d __getSquareLine_mean=new NyARDoublePoint2d();
	private final NyARDoublePoint2d __getSquareLine_ev=new NyARDoublePoint2d();
	/**
	 * 頂点インデクスと輪郭配列から、Ideal座標系とLineを作成して変数に返す
	 * @param i_cparam
	 * @return
	 * @throws NyARException
	 */
	private boolean getSquareLine(int[] i_mkvertex, int[] i_xcoord, int[] i_ycoord, NyARLinear[] o_line,NyARIntPoint[] o_imvertex) throws NyARException
	{
		final NyARCameraDistortionFactor dist_factor=this._dist_factor_ref;  
		final NyARDoubleMatrix22 evec=this.__getSquareLine_evec;
		final NyARDoublePoint2d mean=this.__getSquareLine_mean;
		final NyARDoublePoint2d ev=this.__getSquareLine_ev;
	
		
		for (int i = 0; i < 4; i++) {
			final double w1 = (double) (i_mkvertex[i + 1] - i_mkvertex[i] + 1) * 0.05 + 0.5;
			final int st = (int) (i_mkvertex[i] + w1);
			final int ed = (int) (i_mkvertex[i + 1] - w1);
			final int n = ed - st + 1;
			if (n < 2 || n>MAX_COORD_NUM) {
				// nが2以下、又はMAX_COORD_NUM以上なら主成分分析をしない。
				return false;
			}
			//主成分分析する。
			this._pca.pcaWithDistortionFactor(i_xcoord, i_ycoord, st, n,dist_factor, evec, ev,mean);
			final NyARLinear l_line_i = o_line[i];
			l_line_i.run = evec.m01;// line[i][0] = evec->m[1];
			l_line_i.rise = -evec.m00;// line[i][1] = -evec->m[0];
			l_line_i.intercept = -(l_line_i.run * mean.x + l_line_i.rise * mean.y);// line[i][2] = -(line[i][0]*mean->v[0] + line[i][1]*mean->v[1]);
		}
		for (int i = 0; i < 4; i++) {
			final NyARLinear l_line_i = o_line[i];
			final NyARLinear l_line_2 = o_line[(i + 3) % 4];
			final double w1 = l_line_2.run * l_line_i.rise - l_line_i.run * l_line_2.rise;
			if (w1 == 0.0) {
				return false;
			}
			// 頂点インデクスから頂点座標を得て保存
			o_imvertex[i].x = i_xcoord[i_mkvertex[i]];
			o_imvertex[i].y = i_ycoord[i_mkvertex[i]];
		}
		return true;
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
			// 頂点位置を、起点から対角点の間の1/2にあると予想して、検索する。
			v2 = (v1 - i_vertex1_index) / 2 + i_vertex1_index;
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
			// v2 = (v1-i_vertex1_index+ end_of_coord-i_vertex1_index) / 2+i_vertex1_index;
			v2 = (v1 + end_of_coord) / 2;

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
	 * QRコードのシンボル特徴を持つラベルであるかを調べる
	 * @param buf
	 * @param index_table
	 * @param i_label
	 * @return
	 */
	private boolean hasQrEdgeFeature(int buf[][], int[] index_table, NyARLabelingLabel i_label)
	{
		int tx, bx;
		int w;
		int i_label_id = i_label.id;
		int[] limage_j;
		final int clip_l = i_label.clip_l;
		final int clip_b = i_label.clip_b;
		final int clip_r = i_label.clip_r;
		final int clip_t = i_label.clip_t;

		tx = bx = 0;
		// 上接点(→)
		limage_j = buf[clip_t];
		for (int i = clip_l; i <= clip_r; i++) {// for( i = clip[0]; i <=clip[1]; i++, p1++ ) {
			w = limage_j[i];
			if (w > 0 && index_table[w - 1] == i_label_id) {
				tx = i;
				break;
			}
		}
		// 下接点(←)
		limage_j = buf[clip_b];
		for (int i = clip_r; i >= clip_l; i--) {// for( i = clip[0]; i <=clip[1]; i++, p1++ ) {
			w = limage_j[i];
			if (w > 0 && index_table[w - 1] == i_label_id) {
				bx = i;
				break;
			}
		}
		final int cx = (clip_l + clip_r) / 2;
		final int cy = (clip_t + clip_b) / 2;
		// 横断チェック(中心から線を引いて、010になるかしらべる)
		if (!checkDiagonalLine(buf, cx, cy, bx, clip_b)) {
			return false;
		}
		if (!checkDiagonalLine(buf, tx, clip_t, cx, cy)) {
			return false;
		}
		return true;
	}

	/**
	 * シンボルのパターン特徴を調べる関数
	 * 対角線の一部が010になってるか調べる。
	 * 
	 * @param buf
	 * @param i_px1
	 * @param i_py1
	 * @param i_px2
	 * @param i_py2
	 * @return
	 */
	private boolean checkDiagonalLine(int[][] buf, int i_px1, int i_py1, int i_px2, int i_py2)
	{
		int sub_y = i_py2 - i_py1;
		int sub_x = i_px2 - i_px1;
		// 黒
		int i = 0;
		for (; i < sub_y; i++) {
			int yp = i_py1 + i;
			int xp = i_px1 + i * sub_x / sub_y;
			if (buf[yp][xp] == 0 && buf[yp][xp-1] == 0 && buf[yp][xp+1] == 0) {
				break;
			}

		}
		if (i == sub_y) {
			return false;
		}
		// 白
		for (; i < sub_y; i++) {
			int yp = i_py1 + i;
			int xp = i_px1 + i * sub_x / sub_y;
			if (buf[yp][xp] != 0 && buf[yp][xp-1] != 0 && buf[yp][xp+1] != 0) {
				break;
			}

		}
		if (i == sub_y) {
			return false;
		}
		// 黒
		for (; i < sub_y; i++) {
			int yp = i_py1 + i;
			int xp = i_px1 + i * sub_x / sub_y;
			if (buf[yp][xp] == 0 && buf[yp][xp-1] == 0 && buf[yp][xp+1] == 0) {
				break;
			}

		}
		if (i != sub_y) {
			return false;
		}
		// 端まで到達したらOK
		return true;
	}

}
