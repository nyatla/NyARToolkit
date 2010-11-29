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
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.*;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.types.*;


public class NyARSquareContourDetector_Rle extends NyARSquareContourDetector
{
	private static final int AR_AREA_MAX = 100000;// #define AR_AREA_MAX 100000
	private static final int AR_AREA_MIN = 70;// #define AR_AREA_MIN 70
	private final int _width;
	private final int _height;

	private final NyARLabeling_Rle _labeling;

	private final NyARLabelOverlapChecker<NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo> _overlap_checker = new NyARLabelOverlapChecker<NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo>(32,NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo.class);
	private final NyARContourPickup _cpickup=new NyARContourPickup();
	private final NyARRleLabelFragmentInfoStack _stack;
	private final NyARCoord2SquareVertexIndexes _coord2vertex=new NyARCoord2SquareVertexIndexes();
	
	private final int _max_coord;
	private final int[] _xcoord;
	private final int[] _ycoord;
	/**
	 * 最大i_squre_max個のマーカーを検出するクラスを作成する。
	 * 
	 * @param i_param
	 */
	public NyARSquareContourDetector_Rle(NyARIntSize i_size) throws NyARException
	{
		this._width = i_size.w;
		this._height = i_size.h;
		//ラベリングのサイズを指定したいときはsetAreaRangeを使ってね。
		this._labeling = new NyARLabeling_Rle(this._width,this._height);
		this._labeling.setAreaRange(AR_AREA_MAX, AR_AREA_MIN);
		this._stack=new NyARRleLabelFragmentInfoStack(i_size.w*i_size.h*2048/(320*240)+32);//検出可能な最大ラベル数
		

		// 輪郭の最大長は画面に映りうる最大の長方形サイズ。
		int number_of_coord = (this._width + this._height) * 2;

		// 輪郭バッファ
		this._max_coord = number_of_coord;
		this._xcoord = new int[number_of_coord];
		this._ycoord = new int[number_of_coord];
		return;
	}

	private final int[] __detectMarker_mkvertex = new int[4];
	
	public void detectMarkerCB(NyARBinRaster i_raster, IDetectMarkerCallback i_callback) throws NyARException
	{
		final NyARRleLabelFragmentInfoStack flagment=this._stack;
		final NyARLabelOverlapChecker<NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo> overlap = this._overlap_checker;

		// ラベル数が0ならここまで
		final int label_num=this._labeling.labeling(i_raster, 0, i_raster.getHeight(), flagment);
		if (label_num < 1) {
			return;
		}
		//ラベルをソートしておく
		flagment.sortByArea();
		//ラベルリストを取得
		NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo[] labels=flagment.getArray();

		final int xsize = this._width;
		final int ysize = this._height;
		int[] xcoord = this._xcoord;
		int[] ycoord = this._ycoord;
		final int coord_max = this._max_coord;
		final int[] mkvertex =this.__detectMarker_mkvertex;


		//重なりチェッカの最大数を設定
		overlap.setMaxLabels(label_num);

		for (int i=0; i < label_num; i++) {
			final NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo label_pt=labels[i];
			int label_area = label_pt.area;
		
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
			
			//輪郭を取得
			int coord_num = _cpickup.getContour(i_raster,label_pt.entry_x,label_pt.clip_t, coord_max, xcoord, ycoord);
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
	/**
	 * デバック用API
	 * @return
	 */
	public NyARRleLabelFragmentInfoStack _getFragmentStack()
	{
		return this._stack;
	}

}



