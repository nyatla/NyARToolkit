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
package jp.nyatla.nyartoolkit.sandbox.x2;
import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.*;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.squaredetect.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.param.*;



/**
 * イメージから正方形候補を検出するクラス。
 * このクラスは、arDetectMarker2.cとの置き換えになります。
 * 
 */
public class NyARSquareDetector_X2 extends NyARSquareContourDetector
{
	private final int _width;
	private final int _height;

	private final NyARLabelOverlapChecker<NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo> _overlap_checker = new NyARLabelOverlapChecker<NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo>(32,NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo.class);
	private final SquareContourDetector_X2 _sqconvertor;
	private final NyARContourPickup _cpickup=new NyARContourPickup();
	private final NyARRleLabelFragmentInfoStack _stack;

	
	
	private final NyARLabeling_Rle _labeling;
	/**
	 * 最大i_squre_max個のマーカーを検出するクラスを作成する。
	 * 
	 * @param i_param
	 */
	public NyARSquareDetector_X2(NyARCameraDistortionFactor i_dist_factor_ref,NyARIntSize i_size) throws NyARException
	{
		this._width = i_size.w;
		this._height = i_size.h;
		this._labeling = new NyARLabeling_Rle(this._width,this._height);
		this._sqconvertor=new SquareContourDetector_X2(i_size,i_dist_factor_ref);
		this._stack=new NyARRleLabelFragmentInfoStack(i_size.w*i_size.h*2048/(320*240)+32);//検出可能な最大ラベル数
		

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
		final NyARRleLabelFragmentInfoStack flagment=this._stack;
		final NyARLabelOverlapChecker<NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo> overlap = this._overlap_checker;

		// 初期化

		// マーカーホルダをリセット
		o_square_stack.clear();

		// ラベル数が0ならここまで(Labeling内部でソートするようにした。)
		final int label_num=this._labeling.labeling(i_raster, 0, i_raster.getHeight(), flagment);
		if (label_num < 1) {
			return;
		}
		//ラベルをソートしておく
		flagment.sortByArea();
		NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo[] labels=flagment.getArray();


		final int xsize = this._width;
		final int ysize = this._height;
		final int[] xcoord = this._xcoord;
		final int[] ycoord = this._ycoord;
		final int coord_max = this._max_coord;

		//重なりチェッカの最大数を設定
		overlap.setMaxLabels(label_num);

		for (int i=0; i < label_num; i++) {
			final NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo label_pt=labels[i];
			final int label_area = label_pt.area;

			// クリップ領域が画面の枠に接していれば除外
			if (label_pt.clip_l == 0 || label_pt.clip_r == xsize-1){
				continue;
			}
			if (label_pt.clip_t == 0 || label_pt.clip_b == ysize-1){
				continue;
			}
			// 既に検出された矩形との重なりを確認
			if (!overlap.check(label_pt)) {
				// 重なっているようだ。
				continue;
			}
			
			// 輪郭を取得
			final int coord_num = _cpickup.getContour(i_raster,label_pt.entry_x,label_pt.clip_t, coord_max, xcoord, ycoord);
			if (coord_num == coord_max) {
				// 輪郭が大きすぎる。
				continue;
			}
			//輪郭分析用に正規化する。
			final int vertex1 = SquareContourDetector_X2.normalizeCoord(xcoord, ycoord, coord_num);

			//ここから先が輪郭分析
			NyARSquare square_ptr = o_square_stack.prePush();
			if(!this._sqconvertor.coordToSquare(xcoord,ycoord,vertex1,coord_num,label_area,square_ptr)){
				o_square_stack.pop();// 頂点の取得が出来なかったので破棄
				continue;				
			}
			// 検出済の矩形の属したラベルを重なりチェックに追加する。
			overlap.push(label_pt);
		}
		return;		
	}
}


