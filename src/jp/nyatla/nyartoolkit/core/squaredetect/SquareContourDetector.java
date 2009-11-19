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
import jp.nyatla.nyartoolkit.core.param.NyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.param.NyARObserv2IdealMap;
import jp.nyatla.nyartoolkit.core.pca2d.INyARPca2d;
import jp.nyatla.nyartoolkit.core.pca2d.NyARPca2d_MatrixPCA_O2;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix22;




public class SquareContourDetector
{
	private final double[] _xpos;
	private final double[] _ypos;	
	private final int[] __detectMarker_mkvertex = new int[4];
	private final INyARPca2d _pca;
	private final NyARDoubleMatrix22 __getSquareLine_evec=new NyARDoubleMatrix22();
	private final double[] __getSquareLine_mean=new double[2];
	private final double[] __getSquareLine_ev=new double[2];
	private final Coord2SquareVertexIndexes _coord2vertex=new Coord2SquareVertexIndexes();
	private final NyARObserv2IdealMap _dist_factor;
	public SquareContourDetector(NyARIntSize i_size,NyARCameraDistortionFactor i_distfactor_ref)
	{
		//歪み計算テーブルを作ると、8*width/height*2の領域を消費します。
		//領域を取りたくない場合は、i_dist_factor_refの値をそのまま使ってください。
		this._dist_factor = new NyARObserv2IdealMap(i_distfactor_ref,i_size);


		// 輪郭バッファ
		this._pca=new NyARPca2d_MatrixPCA_O2();
		this._xpos=new double[i_size.w+i_size.h];//最大辺長はthis._width+this._height
		this._ypos=new double[i_size.w+i_size.h];//最大辺長はthis._width+this._height
		return;
	}

	public boolean coordToSquare(int[] i_xcoord,int[] i_ycoord,int i_coord_num,int i_label_area,NyARSquare o_square) throws NyARException
	{

		final int[] mkvertex = this.__detectMarker_mkvertex;
		// 頂点情報を取得
		if (!this._coord2vertex.getVertexIndexes(i_xcoord, i_ycoord, i_coord_num, i_label_area, mkvertex)) {
			// 頂点の取得が出来なかったので破棄
			return false;
		}
		// マーカーを検出
		if (!getSquareLine(mkvertex, i_xcoord, i_ycoord,i_coord_num-1, o_square)){
			// 矩形が成立しなかった。
			return false;
		}
		return true;
	}
	
	private boolean getSquareLine(int[] i_mkvertex, int[] i_xcoord, int[] i_ycoord,int i_cood_num, NyARSquare o_square) throws NyARException
	{
		final NyARLinear[] l_line = o_square.line;
		final NyARDoubleMatrix22 evec=this.__getSquareLine_evec;
		final double[] mean=this.__getSquareLine_mean;
		final double[] ev=this.__getSquareLine_ev;
	
		double w1;
		for (int i = 0; i < 4; i++){
			//頂点を取得
			int ver1=i_mkvertex[i];
			int ver2=i_mkvertex[(i+1)%4];
			int n,st,ed;
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
			//探索区間数を確認
			if(st<=ed){
				//探索区間は1区間
				n = ed - st + 1;
				this._dist_factor.observ2IdealBatch(i_xcoord, i_ycoord, st, n,this._xpos,this._ypos,0);
			}else{
				//探索区間は2区間
				n=ed+1+i_cood_num-st;
				this._dist_factor.observ2IdealBatch(i_xcoord, i_ycoord, st,i_cood_num-st,this._xpos,this._ypos,0);
				this._dist_factor.observ2IdealBatch(i_xcoord, i_ycoord, 0,ed+1,this._xpos,this._ypos,i_cood_num-st);
			}
			//要素数の確認
			if (n < 2) {
				// nが2以下でmatrix.PCAを計算することはできないので、エラー
				return false;
			}
			//主成分分析する。
			this._pca.pca(this._xpos,this._ypos,n,evec, ev,mean);
			final NyARLinear l_line_i = l_line[i];
			l_line_i.dy = evec.m01;// line[i][0] = evec->m[1];
			l_line_i.dx = -evec.m00;// line[i][1] = -evec->m[0];
			l_line_i.c = -(l_line_i.dy * mean[0] + l_line_i.dx * mean[1]);// line[i][2] = -(line[i][0]*mean->v[0] + line[i][1]*mean->v[1]);
		}

		final NyARDoublePoint2d[] l_sqvertex = o_square.sqvertex;
		final NyARIntPoint2d[] l_imvertex = o_square.imvertex;
		for (int i = 0; i < 4; i++) {
			//直線同士の交点計算
			if(!NyARLinear.crossPos(l_line[i],l_line[(i + 3) % 4],l_sqvertex[i])){
				return false;
			}
			// 頂点インデクスから頂点座標を得て保存
			l_imvertex[i].x = i_xcoord[i_mkvertex[i]];
			l_imvertex[i].y = i_ycoord[i_mkvertex[i]];
		}
		return true;
	}	
}