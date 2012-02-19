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

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.*;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、{@link NyARLabeling_Rle}クラスを用いた矩形検出器です。
 * 検出した矩形を、自己コールバック関数{@link #onSquareDetect}へ通知します。
 * 継承クラスで自己コールバック関数{@link #onSquareDetect}を実装する必要があります。
 */
public abstract class NyARSquareContourDetector_Rle extends NyARSquareContourDetector
{	
	/** label_stackにソート後の結果を蓄積するクラス*/
	protected class Labeling extends NyARLabeling_Rle
	{
		public NyARRleLabelFragmentInfoPtrStack label_stack;
		int _right;
		int _bottom;
		
		
		public Labeling(int i_width,int i_height) throws NyARException
		{
			super(i_width,i_height);
			long t=(long)i_width*i_height*2048/(320*240)+32;//full HD support
			this.label_stack=new NyARRleLabelFragmentInfoPtrStack((int)t);//検出可能な最大ラベル数
			this._bottom=i_height-1;
			this._right=i_width-1;
			return;
		}
		public void labeling(INyARGrayscaleRaster i_raster,NyARIntRect i_area,int i_th) throws NyARException
		{
			//配列初期化
			this.label_stack.clear();
			//ラベルの検出
			super.labeling(i_raster, i_area, i_th);
			//ソート
			this.label_stack.sortByArea();
		}
		public void labeling(INyARGrayscaleRaster i_raster,int i_th) throws NyARException
		{
			//配列初期化
			this.label_stack.clear();
			//ラベルの検出
			super.labeling(i_raster,i_th);
			//ソート
			this.label_stack.sortByArea();			
		}
		
		protected void onLabelFound(NyARRleLabelFragmentInfo i_label)
		{
			// クリップ領域が画面の枠に接していれば除外
			if (i_label.clip_l == 0 || i_label.clip_r == this._right){
				return;
			}
			if (i_label.clip_t == 0 || i_label.clip_b == this._bottom){
				return;
			}
			this.label_stack.push(i_label);
		}
	}
	
	protected Labeling _labeling;
	private final NyARLabelOverlapChecker<NyARRleLabelFragmentInfo> _overlap_checker = new NyARLabelOverlapChecker<NyARRleLabelFragmentInfo>(32,NyARRleLabelFragmentInfo.class);
	private NyARContourPickup _cpickup=new NyARContourPickup();
	private final NyARCoord2SquareVertexIndexes _coord2vertex=new NyARCoord2SquareVertexIndexes();
	private final NyARIntCoordinates _coord;
	/**
	 * コンストラクタです。
	 * 入力画像のサイズを指定して、インスタンスを生成します。
	 * @param i_size
	 * 入力画像のサイズ
	 */
	public NyARSquareContourDetector_Rle(NyARIntSize i_size) throws NyARException
	{ 
		this.setupImageDriver(i_size);
		//ラベリングのサイズを指定したいときはsetAreaRangeを使ってね。
		this._coord = new NyARIntCoordinates((i_size.w + i_size.h) * 2);
		return;
	}
	/**
	 * 画像処理オブジェクトの切り替え関数。切り替える場合は、この関数を上書きすること。
	 * @param i_size
	 * @throws NyARException
	 */
	protected void setupImageDriver(NyARIntSize i_size) throws NyARException
	{
		//特性確認
		assert(NyARLabeling_Rle._sf_label_array_safe_reference);
		this._labeling=new Labeling(i_size.w,i_size.h);
		this._cpickup=new NyARContourPickup();
	}	

	private final int[] __detectMarker_mkvertex = new int[4];
	/**
	 * この関数は、ラスタから矩形を検出して、自己コールバック関数{@link #onSquareDetect}で通知します。
	 * @param i_raster
	 * 検出元のラスタ画像
	 * 入力できるラスタの画素形式は、{@link NyARLabeling_Rle#labeling(INyARRaster, int)}と同じです。
	 * @param i_area
	 * 検出する範囲。検出元のラスタの内側である必要があります。
	 * @param i_th
	 * ラベルと判定する敷居値
	 * @throws NyARException
	 */
	public void detectMarker(INyARGrayscaleRaster i_raster,NyARIntRect i_area,int i_th) throws NyARException
	{
		assert(i_area.w*i_area.h>0);
		
		final NyARRleLabelFragmentInfoPtrStack flagment=this._labeling.label_stack;
		final NyARLabelOverlapChecker<NyARRleLabelFragmentInfo> overlap = this._overlap_checker;

		// ラベル数が0ならここまで
		this._labeling.labeling(i_raster, i_area, i_th);
		final int label_num=flagment.getLength();
		if (label_num < 1) {
			return;
		}

		//ラベルリストを取得
		NyARRleLabelFragmentInfo[] labels=flagment.getArray();

		NyARIntCoordinates coord = this._coord;
		final int[] mkvertex =this.__detectMarker_mkvertex;


		//重なりチェッカの最大数を設定
		overlap.setMaxLabels(label_num);

		for (int i=0; i < label_num; i++) {
			NyARRleLabelFragmentInfo label_pt=labels[i];
			// 既に検出された矩形との重なりを確認
			if (!overlap.check(label_pt)) {
				// 重なっているようだ。
				continue;
			}
			
			//輪郭を取得
			if(!this._cpickup.getContour(i_raster,i_area, i_th,label_pt.entry_x,label_pt.clip_t,coord))
			{
				continue;
			}
			int label_area = label_pt.area;
			//輪郭線をチェックして、矩形かどうかを判定。矩形ならばmkvertexに取得
			if (!this._coord2vertex.getVertexIndexes(coord,label_area,mkvertex)){
				// 頂点の取得が出来なかった
				continue;
			}
			//矩形を発見したことをコールバック関数で通知
			this.onSquareDetect(coord,mkvertex);

			// 検出済の矩形の属したラベルを重なりチェックに追加する。
			overlap.push(label_pt);
		
		}
		return;
	}
	/**
	 * この関数は、ラスタから矩形を検出して、自己コールバック関数{@link #onSquareDetect}で通知します。
	 * ARToolKitのarDetectMarker2を基にしています。
	 * @param i_raster
	 * 検出元のラスタ画像
	 * 入力できるラスタの画素形式は、{@link NyARLabeling_Rle#labeling(NyARGrayscaleRaster, int)}と同じです。
	 * @param i_th
	 * 画素の二値判定敷居値です。この値は、ラベリングと、輪郭線追跡時に使われます。
	 */
	public void detectMarker(INyARGrayscaleRaster i_raster,int i_th) throws NyARException
	{
		final NyARRleLabelFragmentInfoPtrStack flagment=this._labeling.label_stack;
		final NyARLabelOverlapChecker<NyARRleLabelFragmentInfo> overlap = this._overlap_checker;

		// ラベル数が0ならここまで
		flagment.clear();
		this._labeling.labeling(i_raster,i_th);
		final int label_num=flagment.getLength();
		if (label_num < 1) {
			return;
		}
		//ラベルをソートしておく
		flagment.sortByArea();
		//ラベルリストを取得
		NyARRleLabelFragmentInfo[] labels=flagment.getArray();

		NyARIntCoordinates coord = this._coord;
		final int[] mkvertex =this.__detectMarker_mkvertex;


		//重なりチェッカの最大数を設定
		overlap.setMaxLabels(label_num);

		for (int i=0; i < label_num; i++) {
			final NyARRleLabelFragmentInfo label_pt=labels[i];
			int label_area = label_pt.area;
		
			// 既に検出された矩形との重なりを確認
			if (!overlap.check(label_pt)) {
				// 重なっているようだ。
				continue;
			}
			//輪郭を取得
			if(!this._cpickup.getContour(i_raster,i_th,label_pt.entry_x,label_pt.clip_t,coord)){
				continue;
			}
			//輪郭線をチェックして、矩形かどうかを判定。矩形ならばmkvertexに取得
			if (!this._coord2vertex.getVertexIndexes(coord,label_area, mkvertex)) {
				// 頂点の取得が出来なかった
				continue;
			}
			//矩形を発見したことをコールバック関数で通知
			this.onSquareDetect(coord,mkvertex);

			// 検出済の矩形の属したラベルを重なりチェックに追加する。
			overlap.push(label_pt);
		
		}
		return;
	}
	
}



