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
package jp.nyatla.nyartoolkit.dev;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.NyARLabelOverlapChecker;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.*;
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2SquareVertexIndexes;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.squaredetect.*;
import jp.nyatla.nyartoolkit.core.param.*;



/**
 * 
 *
 */
public class NyARSquareDetector_Vector
{
	private static final int AR_AREA_MAX = 100000;// #define AR_AREA_MAX 100000
	private static final int AR_AREA_MIN = 70;// #define AR_AREA_MIN 70
	private final int _width;
	private final int _height;
	private final int[] _xcoord;
	private final int[] _ycoord;

	private final NyARLabeling_Rle _labeling;

	private final NyARLabelOverlapChecker<NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo> _overlap_checker = new NyARLabelOverlapChecker<NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo>(32,NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo.class);
	private final SquareContourDetector_Vector _sqconvertor;
	private final NyARContourPickup _cpickup=new NyARContourPickup();
	private final NyARRleLabelFragmentInfoStack _stack;
	
	private final int _max_coord;
	/**
	 * 最大i_squre_max個のマーカーを検出するクラスを作成する。
	 * 
	 * @param i_param
	 */
	public NyARSquareDetector_Vector(NyARCameraDistortionFactor i_dist_factor_ref,NyARIntSize i_size) throws NyARException
	{
		this._width = i_size.w;
		this._height = i_size.h;
		//ラベリングのサイズを指定したいときはsetAreaRangeを使ってね。
		this._labeling = new NyARLabeling_Rle(this._width,this._height);
		this._labeling.setAreaRange(AR_AREA_MAX, AR_AREA_MIN);
		this._sqconvertor=new SquareContourDetector_Vector(i_size,i_dist_factor_ref);
		this._stack=new NyARRleLabelFragmentInfoStack(i_size.w*i_size.h*2048/(320*240)+32);//検出可能な最大ラベル数
		

		// 輪郭の最大長は画面に映りうる最大の長方形サイズ。
		int number_of_coord = (this._width + this._height) * 2;

		// 輪郭バッファ
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
	public final void detectMarker(NyARGrayscaleRaster i_gs,int i_th,NyARSquareStack o_square_stack) throws NyARException
	{
		final NyARRleLabelFragmentInfoStack flagment=this._stack;
		final NyARLabelOverlapChecker<NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo> overlap = this._overlap_checker;

		// マーカーホルダをリセット
		o_square_stack.clear();

		// ラベル数が0ならここまで
		final int label_num=this._labeling.labeling(i_gs,i_th, 0, i_gs.getHeight(), flagment);
		if (label_num < 1) {
			return;
		}
		//ラベルをソートしておく
		flagment.sortByArea();
		//ラベルリストを取得
		NyARRleLabelFragmentInfoStack.RleLabelFragmentInfo[] labels=flagment.getArray();

		final int xsize = this._width;
		final int ysize = this._height;
		final int coord_max = this._max_coord;
		int[] xcoord = this._xcoord;
		int[] ycoord = this._ycoord;


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
			final int coord_num = _cpickup.getContour(i_gs,i_th,label_pt.entry_x,label_pt.clip_t, coord_max, xcoord, ycoord);
			if (coord_num == coord_max) {
				// 輪郭が大きすぎる。
				continue;
			}

			//ここから先が輪郭分析
			NyARSquare square_ptr = o_square_stack.prePush();
			if(!this._sqconvertor.coordToSquare(i_gs,xcoord,ycoord,coord_num,label_area,square_ptr)){
				o_square_stack.pop();// 頂点の取得が出来なかったので破棄
				continue;				
			}
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
	/********************************************************************************
	 * 
	 * 追加クラス 
	 * 
	 ********************************************************************************/
	private class SquareContourDetector_Vector
	{
		private final NyARObserv2IdealMap2 _distmap;
		private final int[] __detectMarker_mkvertex = new int[4];
		private final NyARCoord2SquareVertexIndexes _coord2vertex=new NyARCoord2SquareVertexIndexes();
		public SquareContourDetector_Vector(NyARIntSize i_size,NyARCameraDistortionFactor i_distfactor_ref)
		{
			this._distmap=new NyARObserv2IdealMap2(i_distfactor_ref,i_size);
			return;
		}

		public boolean coordToSquare(NyARGrayscaleRaster i_raster,int[] i_xcoord,int[] i_ycoord,int i_coord_num,int i_label_area,NyARSquare o_square) throws NyARException
		{

			final int[] mkvertex = this.__detectMarker_mkvertex;

			// 頂点情報を取得
			if (!this._coord2vertex.getVertexIndexes(i_xcoord, i_ycoord,i_coord_num, i_label_area, mkvertex)) {
				// 頂点の取得が出来なかったので破棄
				return false;
			}		
			// マーカーを検出
			if (!getSquareLine(i_raster,mkvertex, i_xcoord, i_ycoord,i_coord_num, o_square)){
				// 矩形が成立しなかった。
				return false;
			}
			return true;
		}
		/**
		 * 指定した範囲の輪郭点ベクトル・座標を、加算する。
		 * @param i_raster
		 * @param i_xcoord
		 * @param i_ycoord
		 * @param i_st
		 * @param i_ed
		 * @param o_vecsum
		 * @param o_possum
		 * @return
		 */
		private boolean addCoordVecPos(NyARGrayscaleRaster i_raster,int[] i_xcoord, int[] i_ycoord,int i_st,int i_ed,NyARIntPoint2d io_vecsum)
		{
			int dxi,dyi;
			//ベクトル分析
			dxi=io_vecsum.x;
			dyi=io_vecsum.y;
			for(int i=i_st;i<i_ed;i++){
				//境界に設置していたら失敗する。
				if(i_xcoord[i]<1 || i_ycoord[i]<1 || i_xcoord[i]>=319 || i_ycoord[i]>=239){
					return false;
				}
				//o_vecsumをワークに流用
				i_raster.getPixelVector8(i_xcoord[i],i_ycoord[i],io_vecsum);
				dxi+=io_vecsum.x;
				dyi-=io_vecsum.y;
			}
			io_vecsum.x=dxi;
			io_vecsum.y=dyi;
			return true;		
		}
		
		
		private NyARDoublePoint2d __work_pos=new NyARDoublePoint2d();
		
		private boolean getSquareLine(NyARGrayscaleRaster i_raster,int[] i_mkvertex, int[] i_xcoord, int[] i_ycoord,int i_cood_num, NyARSquare o_square) throws NyARException
		{
			final NyARLinear[] l_line = o_square.line;
			final NyARDoublePoint2d[] l_sqvertex = o_square.sqvertex;
			final NyARIntPoint2d[] l_imvertex = o_square.imvertex;
			final NyARDoublePoint2d idealcenter=this.__work_pos;
		
			NyARIntPoint2d vecsum=new NyARIntPoint2d();
			for (int i = 0; i < 4; i++){
				//頂点を取得
				int ver1=i_mkvertex[i];
				int ver2=i_mkvertex[(i+1)%4];

				int n,st,ed;
				double w1;
				//探索区間の決定
				if(ver2>=i_mkvertex[i]){
					//頂点[i]から頂点[i+1]までの輪郭が、1区間にあるとき
					w1 = (double) (ver2 - ver1 + 1) * 0.05 + 0.5;
					//探索区間の決定
					st = (int) (ver1+w1);
					ed = (int) (ver2 - w1);
				}else{
					//頂点[i]から頂点[i+1]までの輪郭が、2区間に分かれているとき
					w1 = (double) (ver2+i_cood_num-ver1+1)%i_cood_num * 0.05 + 0.5;
					//探索区間の決定
					st = (int) (ver1+w1)%i_cood_num;
					ed = (int) (ver2+i_cood_num-w1)%i_cood_num;
				}
				vecsum.x=vecsum.y=0;
				//ベクトル分析
				if(st<=ed){
					//1区間
					n = ed - st+1;
					addCoordVecPos(i_raster,i_xcoord,i_ycoord,st,ed+1,vecsum);
					this._distmap.getIdealCoodCenter(i_xcoord, i_ycoord,st,n,idealcenter);
				}else{
					//探索区間は2区間
					double cx,cy;
					n=ed+i_cood_num-st+1;
					addCoordVecPos(i_raster,i_xcoord,i_ycoord,st,i_cood_num,vecsum);
					addCoordVecPos(i_raster,i_xcoord,i_ycoord,0,ed,vecsum);
					//輪郭の中心位置を計算
					this._distmap.getIdealCoodCenter(i_xcoord, i_ycoord,st,i_cood_num-st,idealcenter);
					cx=idealcenter.x;
					cy=idealcenter.y;
					this._distmap.getIdealCoodCenter(i_xcoord, i_ycoord,0,ed+1,idealcenter);
					idealcenter.x=(idealcenter.x+cx)/2;
					idealcenter.y=(idealcenter.y+cy)/2;
				}
				//中央値を歪修正（ほんとはピクセル単位にゆがみ矯正するべきだと思う）
				
				double l=Math.sqrt((double)(vecsum.x*vecsum.x+vecsum.y*vecsum.y));
				final NyARLinear l_line_i = l_line[i];
				//直交するベクトルを計算
				l_line_i.dy =  vecsum.x/l;
				l_line_i.dx =  -vecsum.y/l;
				//cを計算
				l_line_i.c  = -(l_line_i.dy * (idealcenter.x) + l_line_i.dx * (idealcenter.y));
				
				// 頂点インデクスから頂点座標を得て保存
				l_imvertex[i].x = i_xcoord[ver1];
				l_imvertex[i].y = i_ycoord[ver1];
			}
			//線分式から頂点を計算
			for(int i=0;i<4;i++)
			{
				if(!NyARLinear.crossPos(l_line[i],l_line[(i + 3) % 4],l_sqvertex[i])){
					return false;
				}
			}		
			return true;
		}
	}

	/**
	 * 輪郭線の中心位置を計算する関数を追加したマップクラス
	 */
	private class NyARObserv2IdealMap2 extends NyARObserv2IdealMap
	{
		public NyARObserv2IdealMap2(NyARCameraDistortionFactor i_distfactor,NyARIntSize i_screen_size)
		{
			super(i_distfactor,i_screen_size);
		}
		/**
		 * 歪み矯正した座標における、各座標の合計値を
		 * @param i_x_coord
		 * @param i_y_coord
		 * @param i_start
		 * @param i_num
		 * @param o_center
		 */
		public void getIdealCoodCenter(int[] i_x_coord, int[] i_y_coord,int i_start, int i_num,NyARDoublePoint2d o_center)
		{
			int idx;
			double x,y;
			x=y=0;
			final double[] mapx=this._mapx;
			final double[] mapy=this._mapy;
			final int stride=this._stride;
			for (int j = 0; j < i_num; j++){
				idx=i_x_coord[i_start + j]+i_y_coord[i_start + j]*stride;
				x+=mapx[idx];
				y+=mapy[idx];
			}
			o_center.x=x/(double)i_num;
			o_center.y=y/(double)i_num;
			return;
		}
	}	
}



