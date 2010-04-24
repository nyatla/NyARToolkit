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
import jp.nyatla.nyartoolkit.core.labeling.NyARLabelOverlapChecker;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingImage;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingLabel;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabelingLabelStack;
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.NyARLabeling_ARToolKit;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;



public class NyARSquareContourDetector_ARToolKit extends NyARSquareContourDetector
{
	private static final int AR_AREA_MAX = 100000;// #define AR_AREA_MAX 100000
	private static final int AR_AREA_MIN = 70;// #define AR_AREA_MIN 70
	private final int _width;
	private final int _height;

	private final NyARLabeling_ARToolKit _labeling;

	private final NyARLabelingImage _limage;

	private final NyARLabelOverlapChecker<NyARLabelingLabel> _overlap_checker = new NyARLabelOverlapChecker<NyARLabelingLabel>(32,NyARLabelingLabel.class);
	private final NyARContourPickup _cpickup=new NyARContourPickup();
	private final NyARCoord2SquareVertexIndexes _coord2vertex=new NyARCoord2SquareVertexIndexes();
	
	private final int _max_coord;
	private final int[] _xcoord;
	private final int[] _ycoord;	
	private final int[] __detectMarker_mkvertex = new int[4];
	/**
	 * 最大i_squre_max個のマーカーを検出するクラスを作成する。
	 * 
	 * @param i_param
	 */
	public NyARSquareContourDetector_ARToolKit(NyARIntSize i_size) throws NyARException
	{
		this._width = i_size.w;
		this._height = i_size.h;
		this._labeling = new NyARLabeling_ARToolKit();
		this._limage = new NyARLabelingImage(this._width, this._height);

		// 輪郭の最大長は画面に映りうる最大の長方形サイズ。
		int number_of_coord = (this._width + this._height) * 2;

		// 輪郭バッファは頂点変換をするので、輪郭バッファの２倍取る。
		this._max_coord = number_of_coord;
		this._xcoord = new int[number_of_coord];
		this._ycoord = new int[number_of_coord];
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
	public final void detectMarkerCB(NyARBinRaster i_raster, IDetectMarkerCallback i_callback) throws NyARException
	{
		final NyARLabelingImage limage = this._limage;

		// ラベル数が0ならここまで
		final int label_num = this._labeling.labeling(i_raster,this._limage);
		if (label_num < 1) {
			return;
		}

		final NyARLabelingLabelStack stack = limage.getLabelStack();
		//ラベルをソートしておく
		stack.sortByArea();
		//
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
		final int[] mkvertex =this.__detectMarker_mkvertex;
		
		final NyARLabelOverlapChecker<NyARLabelingLabel> overlap = this._overlap_checker;

		//重なりチェッカの最大数を設定
		overlap.setMaxLabels(label_num);
		for (; i < label_num; i++) {
			final NyARLabelingLabel label_pt = labels[i];
			final int label_area = label_pt.area;
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
			final int coord_num = _cpickup.getContour(limage,limage.getTopClipTangentX(label_pt),label_pt.clip_t, coord_max, xcoord, ycoord);
			if (coord_num == coord_max) {
				// 輪郭が大きすぎる。
				continue;
			}
			//輪郭線をチェックして、矩形かどうかを判定。矩形ならばmkvertexに取得
			if (!this._coord2vertex.getVertexIndexes(xcoord, ycoord,coord_num,label_area, mkvertex)) {
				// 頂点の取得が出来なかった
				continue;
			}
			//矩形を発見したことをコールバック関数で通知
			i_callback.onSquareDetect(this,xcoord,ycoord,coord_num,mkvertex);

			// 検出済の矩形の属したラベルを重なりチェックに追加する。
			overlap.push(label_pt);
			
		}
		return;
	}

}



