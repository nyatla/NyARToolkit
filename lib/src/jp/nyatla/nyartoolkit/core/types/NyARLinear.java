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
package jp.nyatla.nyartoolkit.core.types;


/**
 * このクラスは、0=a*x+b*y+cのパラメータを格納します。
 * x,yの増加方向は、x=L→R,y=B→Tです。 y軸が反転しているので注意してください。
 *
 */
public class NyARLinear
{
	/** 直線式の係数 b*/
	public double b;
	/** 直線式の係数 a*/
	public double a;
	/** 直線式の係数 c*/
	public double c;
	
	/**
	 * この関数は、指定サイズのオブジェクト配列を作ります。
	 * @param i_number
	 * 作成する配列の長さ
	 * @return
	 * 新しい配列。
	 */	
	public static NyARLinear[] createArray(int i_number)
	{
		NyARLinear[] ret=new NyARLinear[i_number];
		for(int i=0;i<i_number;i++)
		{
			ret[i]=new NyARLinear();
		}
		return ret;
	}
	/**
	 * この関数は、引数値からパラメータをインスタンスへコピーします。
	 * @param i_source
	 * コピー元のオブジェクト
	 */
	public final void copyFrom(NyARLinear i_source)
	{
		this.b=i_source.b;
		this.a=i_source.a;
		this.c=i_source.c;
		return;
	}
	/**
	 * この関数は、直線の交点を計算します。
	 * @param l_line_2
	 * 交点を計算する直線式
	 * @param o_point
	 * 交点座標を格納するオブジェクト
	 * @return
	 * 交点が求まればtrue
	 */
	public final boolean crossPos(NyARLinear l_line_2,NyARDoublePoint2d o_point)
	{
		final double w1 = this.a * l_line_2.b - l_line_2.a * this.b;
		if (w1 == 0.0) {
			return false;
		}
		o_point.x = (this.b * l_line_2.c - l_line_2.b * this.c) / w1;
		o_point.y = (l_line_2.a * this.c - this.a * l_line_2.c) / w1;
		return true;
	}
	/**
	 * この関数は、直線の交点を計算します。
	 * @param i_a
	 * 交点を求める直線式の係数a
	 * @param i_b
	 * 交点を求める直線式の係数b
	 * @param i_c
	 * 交点を求める直線式の係数c
	 * @param o_point
	 * 交点座標を格納するオブジェクト
	 * @return
	 * 交点が求まればtrue
	 */
	public final boolean crossPos(double i_a,double i_b,double i_c,NyARDoublePoint2d o_point)
	{
		final double w1 = this.a * i_b - i_a * this.b;
		if (w1 == 0.0) {
			return false;
		}
		o_point.x = (this.b * i_c - i_b * this.c) / w1;
		o_point.y = (i_a * this.c - this.a * i_c) / w1;
		return true;
	}
	/**
	 * この関数は、直線の交点を計算します。
	 * @param i_a
	 * 交点を求める直線式の係数a
	 * @param i_b
	 * 交点を求める直線式の係数b
	 * @param i_c
	 * 交点を求める直線式の係数c
	 * @param o_point
	 * 交点座標を格納するオブジェクト
	 * @return
	 * 交点が求まればtrue
	 */
	public final boolean crossPos(double i_a,double i_b,double i_c,NyARIntPoint2d o_point)
	{
		final double w1 = this.a * i_b - i_a * this.b;
		if (w1 == 0.0) {
			return false;
		}
		o_point.x = (int)((this.b * i_c - i_b * this.c) / w1);
		o_point.y = (int)((i_a * this.c - this.a * i_c) / w1);
		return true;
	}
	/**
	 * この関数は、2直線が交差しているかを返します。
	 * @param l_line_2
	 * 交差しているか確認するオブジェクト
	 * @return
	 * 交差していればtrue
	 */
	public final boolean isCross(NyARLinear l_line_2)
	{
		final double w1 = this.a * l_line_2.b - l_line_2.a * this.b;
		return (w1 == 0.0)?false:true;
	}
	
	/**
	 * この関数は、2点を結ぶ直線式を計算して、インスタンスに格納します。
	 * 式の係数値は、正規化されます。
	 * @param i_point1
	 * 点１
	 * @param i_point2
	 * 点２
	 * @return
	 * 直線式が求まれば、true
	 */
	public final boolean makeLinearWithNormalize(NyARIntPoint2d i_point1,NyARIntPoint2d i_point2)
	{
		return makeLinearWithNormalize(i_point1.x,i_point1.y,i_point2.x,i_point2.y);
	}
	/**
	 * この関数は、2点を結ぶ直線式を計算して、インスタンスに格納します。
	 * 式の係数値は、正規化されます。
	 * @param i_point1
	 * 点１
	 * @param i_point2
	 * 点２
	 * @return
	 * 直線式が求るとtrueを返します。
	 */
	public final boolean makeLinearWithNormalize(NyARDoublePoint2d i_point1,NyARDoublePoint2d i_point2)
	{
		return makeLinearWithNormalize(i_point1.x,i_point1.y,i_point2.x,i_point2.y);
	}
	/**
	 * この関数は、2点を結ぶ直線式を計算して、インスタンスに格納します。
	 * 式の係数値は、正規化されます。
	 * @param x1
	 * 点1の座標(X)
	 * @param y1
	 * 点1の座標(Y)
	 * @param x2
	 * 点2の座標(X)
	 * @param y2
	 * 点2の座標(Y)
	 * @return
	 * 直線式が求るとtrueを返します。
	 */
	public final boolean makeLinearWithNormalize(double x1,double y1,double x2,double y2)
	{
		double dx=y2-y1;
		double dy=x1-x2;
		double sq=Math.sqrt(dx*dx+dy*dy);
		if(sq==0){
			return false;
		}
		sq=1/sq;
		this.a=dx*sq;
		this.b=dy*sq;
		this.c=(x1*(y1-y2)+y1*(x2-x1))*sq;
		return true;
	}
	/**
	 * この関数は、傾きと通過点から直線式を計算して、インスタンスへセットします。
	 * @param i_dx
	 * Xの傾き
	 * @param i_dy
	 * Yの傾き
	 * @param i_x
	 * 通過点の座標X
	 * @param i_y
	 * 通過点の座標Y
	 */
	public final void setVector(double i_dx,double i_dy,double i_x,double i_y)
	{
		this.a= i_dy;
		this.b=-i_dx;
		this.c=(i_dx*i_y-i_dy*i_x);
		return;
	}
	/**
	 * この関数は、{@link NyARVecLinear2d}を直線式に変換して、インスタンスへセットします。
	 * @param i_vector
	 * セットするオブジェクト
	 */
	public final void setVector(NyARVecLinear2d i_vector)
	{
		this.a= i_vector.dy;
		this.b=-i_vector.dx;
		this.c=(i_vector.dx*i_vector.y-i_vector.dy*i_vector.x);
		return;		
	}
	/**
	 * この関数は、{@link NyARVecLinear2d}を正規化された直線式に変換して、インスタンスへセットします。
	 * @param i_vector
	 * セットするオブジェクト
	 */
	public final boolean setVectorWithNormalize(NyARVecLinear2d i_vector)
	{
		double dx=i_vector.dx;
		double dy=i_vector.dy;
		double sq=Math.sqrt(dx*dx+dy*dy);
		if(sq==0){
			return false;
		}
		sq=1/sq;
		this.a= dy*sq;
		this.b=-dx*sq;
		this.c=-(this.a*i_vector.x+this.b*i_vector.y);		
		return true;
	}
	/**
	 * この関数は、i_x,i_yを通過する、i_linearの法線を計算して、インスタンスへ格納します。
	 * @param i_x
	 * 通過点X
	 * @param i_y
	 * 通過点Y
	 * @param i_linear
	 * 法線を計算する直線式(この引数にはthisを指定できます。)
	 */
	public final void normalLine(double i_x,double i_y,NyARLinear i_linear)
	{
		double la=i_linear.a;
		double lb=i_linear.b;
		this.a=lb;
		this.b=-la;
		this.c=-(lb*i_x-la*i_y);
	}
	/**
	 * この関数は、i_x,i_yを通るこの直線の法線と、i_linearが交わる点を返します。
	 * @param i_x
	 * 法線が通過する点X
	 * @param i_y
	 * 法線が通過する点Y
	 * @param i_linear
	 * 交点を計算する直線式
	 * @param o_point
	 * 交点を返却するオブジェクト
	 * @return
	 * 交点が求まれば、trueを返します。
	 */
	public final boolean normalLineCrossPos(double i_x,double i_y,NyARLinear i_linear,NyARDoublePoint2d o_point)
	{
		//thisを法線に変換
		double la=this.b;
		double lb=-this.a;
		double lc=-(la*i_x+lb*i_y);
		//交点を計算
		final double w1 = i_linear.a * lb - la * i_linear.b;
		if (w1 == 0.0) {
			return false;
		}
		o_point.x = ((i_linear.b * lc - lb * i_linear.c) / w1);
		o_point.y = ((la * i_linear.c - i_linear.a * lc) / w1);
		return true;
	}
//	/**
//	 * i_x,i_yを通るこの直線の法線上での、この直線とi_linearの距離の二乗値を返します。
//	 * i_x,i_yに直線上の点を指定すると、この直線の垂線上での、もう一方の直線との距離の二乗値が得られます。
//	 * @param i_linear
//	 * @param i_x
//	 * @param i_y
//	 * @param o_point
//	 * @return
//	 * 交点が無い場合、無限大を返します。
//	 */
//	public final double sqDistWithLinear(NyARLinear i_linear, double i_x,double i_y)
//	{
//		//thisを法線に変換
//		double la=this.b;
//		double lb=-this.a;
//		double lc=-(la*i_x+lb*i_y);
//		//交点を計算
//		final double w1 = i_linear.a * lb - la * i_linear.b;
//		if (w1 == 0.0) {
//			return Double.POSITIVE_INFINITY;
//		}
//		double x=i_x-((i_linear.b * lc - lb * i_linear.c) / w1);
//		double y=i_y-((la * i_linear.c - i_linear.a * lc) / w1);
//		return x*x+y*y;
//	}

	/**
	 * この関数は、直線を0,0基点(左上)の矩形でクリッピングしたときの、端点を計算します。
	 * @param i_width
	 * 矩形の幅
	 * @param i_height
	 * 矩形の高さ
	 * @param o_point
	 * 端点を返すオブジェクト配列。2要素である必要があります。
	 * @return
	 * 端点が求まればtrue
	 */
	public final boolean makeSegmentLine(int i_width,int i_height,NyARIntPoint2d[] o_point)
	{	
		int idx=0;
		NyARIntPoint2d ptr=o_point[0];
		if(this.crossPos(0,-1,0,ptr) && ptr.x>=0 && ptr.x<i_width)
		{
			//y=rect.yの線
			idx++;
			ptr=o_point[idx];
		}
		if(this.crossPos(0,-1,i_height-1,ptr) && ptr.x>=0 && ptr.x<i_width)
		{
			//y=(rect.y+rect.h-1)の線
			idx++;
			if(idx==2){
				return true;
			}
			ptr=o_point[idx];
		}
		if(this.crossPos(-1,0,0,ptr) && ptr.y>=0 && ptr.y<i_height)
		{
			//x=i_leftの線
			idx++;
			if(idx==2){
				return true;
			}
			ptr=o_point[idx];
		}
		if(this.crossPos(-1,0,i_width-1, ptr) && ptr.y>=0 && ptr.y<i_height)
		{
			//x=i_right-1の線
			idx++;
			if(idx==2){
				return true;
			}
		}
		return false;
	}
	/**
	 * この関数は、直線を矩形でクリッピングしたときの、端点を計算します。
	 * @param i_left
	 * 矩形の左上座標(X)
	 * @param i_top
	 * 矩形の左上座標(Y)
	 * @param i_width
	 * 矩形の幅
	 * @param i_height
	 * 矩形の高さ
	 * @param o_point
	 * 端点を返すオブジェクト配列。2要素である必要があります。
	 * @return
	 * 端点が求まればtrue
	 */
	public final boolean makeSegmentLine(int i_left,int i_top,int i_width,int i_height,NyARIntPoint2d[] o_point)
	{	
		int bottom=i_top+i_height;
		int right=i_left+i_width;
		int idx=0;
		NyARIntPoint2d ptr=o_point[0];
		if(this.crossPos(0,-1,i_top,ptr) && ptr.x>=i_left && ptr.x<right)
		{
			//y=rect.yの線
			idx++;
			ptr=o_point[idx];
		}
		if(this.crossPos(0,-1,bottom-1,ptr) && ptr.x>=i_left && ptr.x<right)
		{
			//y=(rect.y+rect.h-1)の線
			idx++;
			if(idx==2){
				return true;
			}
			ptr=o_point[idx];
		}
		if(this.crossPos(-1,0,i_left,ptr) && ptr.y>=i_top && ptr.y<bottom)
		{
			//x=i_leftの線
			idx++;
			if(idx==2){
				return true;
			}
			ptr=o_point[idx];
		}
		if(this.crossPos(-1,0,right-1, ptr) && ptr.y>=i_top && ptr.y<bottom)
		{
			//x=i_right-1の線
			idx++;
			if(idx==2){
				return true;
			}
		}
		return false;
	}
	/**
	 * この関数は、この直線と、i_sp1とi_sp2の作る線分との、二乗距離値の合計を返します。
	 * 計算方法は、線分の端点を通過する直線の法線上での、端点と直線の距離の合計です。
	 * 線分と直線の類似度を判定する数値になります。
	 * @param i_sp1
	 * 線分の端点1
	 * @param i_sp2
	 * 線分の端点2
	 * @return
	 * 二乗距離値の合計。距離が取れないときは無限大です。
	 */
	public final double sqDistBySegmentLineEdge(NyARDoublePoint2d i_sp1,NyARDoublePoint2d i_sp2)
	{
		double la,lb,lc;
		double x,y,w1;
		//thisを法線に変換
		la=this.b;
		lb=-this.a;

		//交点を計算
		w1 = this.a * lb - la * this.b;
		if (w1 == 0.0) {
			return Double.POSITIVE_INFINITY;
		}
		//i_sp1と、i_linerの交点
		lc=-(la*i_sp1.x+lb*i_sp1.y);
		x = ((this.b * lc - lb * this.c) / w1)-i_sp1.x;
		y = ((la * this.c - this.a * lc) / w1)-i_sp1.y;
		double sqdist=x*x+y*y;

		lc=-(la*i_sp2.x+lb*i_sp2.y);
		x = ((this.b * lc - lb * this.c) / w1)-i_sp2.x;
		y = ((la * this.c - this.a * lc) / w1)-i_sp2.y;

		return sqdist+x*x+y*y;
	}
	/**
	 * この関数は、頂点群から最小二乗法を使用して直線を計算します。
	 * @param i_points
	 * 頂点群を格納した配列。
	 * @param i_number_of_data
	 * 計算対象の頂点群の数
	 * @return
	 * 計算に成功すると、trueを返します。
	 */
	public boolean leastSquares(NyARDoublePoint2d[] i_points,int i_number_of_data)
	{
		assert(i_number_of_data>1);
		int i;
		double sum_xy = 0, sum_x = 0, sum_y = 0, sum_x2 = 0;
		for (i=0; i<i_number_of_data; i++){
			NyARDoublePoint2d ptr=i_points[i];
			double xw=ptr.x;
			sum_xy += xw * ptr.y;
			sum_x += xw;
			sum_y += ptr.y;
			sum_x2 += xw*xw;
		}
		this.b =-(i_number_of_data * sum_x2 - sum_x*sum_x);
		this.a = (i_number_of_data * sum_xy - sum_x * sum_y);
		this.c = (sum_x2 * sum_y - sum_xy * sum_x);
		return true;
	}
}
