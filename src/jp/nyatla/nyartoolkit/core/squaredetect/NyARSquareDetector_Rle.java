package jp.nyatla.nyartoolkit.core.squaredetect;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.INyARSquareDetector;
import jp.nyatla.nyartoolkit.core.NyARSquare;
import jp.nyatla.nyartoolkit.core.NyARSquareStack;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.*;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;



public class NyARSquareDetector_Rle implements INyARSquareDetector
{
	private static final int AR_AREA_MAX = 100000;// #define AR_AREA_MAX 100000
	private static final int AR_AREA_MIN = 70;// #define AR_AREA_MIN 70
	private final int _width;
	private final int _height;

	private final NyARLabeling_Rle _labeling;

	private final LabelOverlapChecker _overlap_checker = new LabelOverlapChecker(32);
	private final SquareContourDetector _sqconvertor;
	private final ContourPickup _cpickup=new ContourPickup();
	private final RleLabelFragmentInfoStack _stack;
	
	private final int _max_coord;
	private final int[] _xcoord;
	private final int[] _ycoord;	
	/**
	 * 最大i_squre_max個のマーカーを検出するクラスを作成する。
	 * 
	 * @param i_param
	 */
	public NyARSquareDetector_Rle(NyARCameraDistortionFactor i_dist_factor_ref,NyARIntSize i_size) throws NyARException
	{
		this._width = i_size.w;
		this._height = i_size.h;
		//歪み計算テーブルを作ると、8*width/height*2の領域を消費します。
		//領域を取りたくない場合は、i_dist_factor_refの値をそのまま使ってください。
		this._labeling = new NyARLabeling_Rle(this._width);
		this._sqconvertor=new SquareContourDetector(i_size,i_dist_factor_ref);
		this._stack=new RleLabelFragmentInfoStack(1024);//検出可能な最大ラベル数
		

		// 輪郭の最大長は画面に映りうる最大の長方形サイズ。
		int number_of_coord = (this._width + this._height) * 2;

		// 輪郭バッファは頂点変換をするので、輪郭バッファの２倍取る。
		this._max_coord = number_of_coord;
		this._xcoord = new int[number_of_coord * 2];
		this._ycoord = new int[number_of_coord * 2];
		return;
	}

	/**
	 * arDetectMarker2を基にした関数
	 * この関数はNyARSquare要素のうち、directionを除くパラメータを取得して返します。
	 * directionの確定は行いません。
	 * @param i_raster
	 * 解析する２値ラスタイメージを指定します。
	 * @param o_square_stack
	 * 抽出した正方形候補を格納するリスト
	 * @throws NyARException
	 */
	public final void detectMarker(NyARBinRaster i_raster, NyARSquareStack o_square_stack) throws NyARException
	{
		final RleLabelFragmentInfoStack flagment=this._stack;

		// マーカーホルダをリセット
		o_square_stack.clear();

		// ラベリング
		this._labeling.labeling(i_raster, 0, i_raster.getHeight(), flagment);

		// ラベル数が0ならここまで
		final int label_num = flagment.getLength();
		if (label_num < 1) {
			return;
		}
		
		
		// ラベルを大きい順に整列
		flagment.sortByArea();
		RleLabelFragmentInfoStack.RleLabelFragmentInfo[] labels=flagment.getArray();

		// デカいラベルを読み飛ばし
		int i;
		for (i = 0; i < label_num; i++) {
			// 検査対象内のラベルサイズになるまで無視
			if (labels[i].area <= AR_AREA_MAX) {
				break;
			}
		}

		final int[] xcoord = this._xcoord;
		final int[] ycoord = this._ycoord;
		final int coord_max = this._max_coord;
		int coord_num;
		int label_area;

		//重なりチェッカの最大数を設定
//		overlap.setMaxlabel(label_num);

		for (; i < label_num; i++) {
			final RleLabelFragmentInfoStack.RleLabelFragmentInfo label_pt=labels[i];
			label_area = label_pt.area;
			// 検査対象サイズよりも小さくなったら終了
			if (label_area < AR_AREA_MIN) {
				break;
			}
//			// クリップ領域が画面の枠に接していれば除外
//			if (label_pt.clip_l == 1 || label_pt.clip_r == xsize - 2) {// if(wclip[i*4+0] == 1 || wclip[i*4+1] ==xsize-2){
//				continue;
//			}
//			if (label_pt.clip_t == 1 || label_pt.clip_b == ysize - 2) {// if( wclip[i*4+2] == 1 || wclip[i*4+3] ==ysize-2){
//				continue;
//			}
//			// 既に検出された矩形との重なりを確認
//			if (!overlap.check(label_pt)) {
//				// 重なっているようだ。
//				continue;
//			}
			// 輪郭を取得
			coord_num = _cpickup.getContour(i_raster,label_pt.entry_x,label_pt.clip_t, coord_max, xcoord, ycoord);
			if (coord_num == coord_max) {
				// 輪郭が大きすぎる。
				continue;
			}
			//輪郭分析用に正規化する。
			final int vertex1 = SquareContourDetector.normalizeCoord(xcoord, ycoord, coord_num);

			//ここから先が輪郭分析
			NyARSquare square_ptr = o_square_stack.prePush();
			if(!this._sqconvertor.coordToSquare(xcoord,ycoord,vertex1,coord_num,label_area,square_ptr)){
				o_square_stack.pop();// 頂点の取得が出来なかったので破棄
				continue;				
			}
			// 検出済の矩形の属したラベルを重なりチェックに追加する。
//			overlap.push(label_pt);
		}	
		return;
	}

}



