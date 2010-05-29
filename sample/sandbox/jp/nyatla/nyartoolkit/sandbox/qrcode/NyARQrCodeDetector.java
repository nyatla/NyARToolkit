package jp.nyatla.nyartoolkit.sandbox.qrcode;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingImage;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingLabel;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingLabelStack;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabeling_ARToolKit;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareStack;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.types.*;

public class NyARQrCodeDetector extends NyARSquareContourDetector
{
	private NyARQrCodeSymbolBinder _binder;

	private static final int AR_AREA_MAX = 10000;

	private static final int AR_AREA_MIN = 50;

	private final int _width;

	private final int _height;

	private final NyARLabeling_ARToolKit _labeling;

	private final NyARLabelingImage _limage;

	private final NyARCoord2Linear _sqconvertor;
	private final NyARContourPickup _cpickup=new NyARContourPickup();
	
	/**
	 * 最大i_squre_max個のマーカーを検出するクラスを作成する。
	 * 
	 * @param i_param
	 */
	public NyARQrCodeDetector(NyARCameraDistortionFactor i_dist_factor_ref, NyARIntSize i_size) throws NyARException
	{
		this._width = i_size.w;
		this._height = i_size.h;
		this._labeling = new NyARLabeling_ARToolKit();
		this._limage = new NyARLabelingImage(this._width, this._height);
		this._binder=new NyARQrCodeSymbolBinder(i_dist_factor_ref);
		this._sqconvertor=new NyARCoord2Linear(i_size,i_dist_factor_ref);

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
		final NyARLabelingImage limage = this._limage;

		// 初期化

		// マーカーホルダをリセット
		o_square_stack.clear();

		// ラベリング
		this._labeling.labeling(i_raster,limage);

		// ラベル数が0ならここまで
		final int label_num = limage.getLabelStack().getLength();
		if (label_num < 1) {
			return;
		}

		final NyARLabelingLabelStack stack = limage.getLabelStack();
		// ラベルを大きい順に整列
		stack.sortByArea();

		final NyARLabelingLabel[] labels = stack.getArray();
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
		final int[] buf = (int[]) limage.getBuffer();
		final int[] indextable = limage.getIndexArray();

		int label_area;
		NyARLabelingLabel label_pt;
		NyARSquareStack wk_stack=new NyARSquareStack(10);
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
			final int coord_num = _cpickup.getContour(limage,limage.getTopClipTangentX(label_pt),label_pt.clip_t, coord_max, xcoord, ycoord);
			if (coord_num == coord_max) {
				// 輪郭が大きすぎる。
				continue;
			}
			//輪郭分析用に正規化する。
			final int vertex1 = NyARCoord2Linear.normalizeCoord(xcoord, ycoord, coord_num);

			//ここから先が輪郭分析
			NyARSquare square_ptr = o_square_stack.prePush();
			if(!this._sqconvertor.coordToSquare(xcoord,ycoord,vertex1,coord_num,label_area,square_ptr)){
				o_square_stack.pop();// 頂点の取得が出来なかったので破棄
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
		NyARSquare[] sa=i_square_stack.getArray();
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

	/**
	 * QRコードのシンボル特徴を持つラベルであるかを調べる
	 * @param buf
	 * @param index_table
	 * @param i_label
	 * @return
	 */
	private boolean hasQrEdgeFeature(int[] buf, int[] index_table, NyARLabelingLabel i_label)
	{
		int tx, bx;
		int w;
		int i_label_id = i_label.id;
		int limage_j_ptr;
		final int clip_l = i_label.clip_l;
		final int clip_b = i_label.clip_b;
		final int clip_r = i_label.clip_r;
		final int clip_t = i_label.clip_t;

		tx = bx = 0;
		// 上接点(→)
		limage_j_ptr = clip_t*this._width;
		for (int i = clip_l; i <= clip_r; i++) {// for( i = clip[0]; i <=clip[1]; i++, p1++ ) {
			w = buf[limage_j_ptr+i];
			if (w > 0 && index_table[w - 1] == i_label_id) {
				tx = i;
				break;
			}
		}
		// 下接点(←)
		limage_j_ptr = clip_b*this._width;
		for (int i = clip_r; i >= clip_l; i--) {// for( i = clip[0]; i <=clip[1]; i++, p1++ ) {
			w = buf[limage_j_ptr+i];
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
	private boolean checkDiagonalLine(int[] buf, int i_px1, int i_py1, int i_px2, int i_py2)
	{
		int sub_y = i_py2 - i_py1;
		int sub_x = i_px2 - i_px1;
		// 黒
		int i = 0;
		for (; i < sub_y; i++) {
			int yp = i_py1 + i;
			int xp = i_px1 + i * sub_x / sub_y;
			if (buf[yp*this._width+xp] == 0 && buf[yp*this._width+(xp-1)] == 0 && buf[yp*this._width+(xp+1)] == 0) {
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
			if (buf[yp*this._width+xp] != 0 && buf[yp*this._width+(xp-1)] != 0 && buf[yp*this._width+(xp+1)] != 0) {
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
			if (buf[yp*this._width+xp] == 0 && buf[yp*this._width+(xp-1)] == 0 && buf[yp*this._width+(xp+1)] == 0) {
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
