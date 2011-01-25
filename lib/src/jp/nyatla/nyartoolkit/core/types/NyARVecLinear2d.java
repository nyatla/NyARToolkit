/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
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
 * このクラスは、通過点とX,Yの変化量で、直線を定義します。
 */
public class NyARVecLinear2d
{
	/** 直線の通過点(X)*/
	public double x;
	/** 直線の通過点(Y)*/
	public double y;
	/** x方向の直線の変化量*/
	public double dx;
	/** y方向の直線の変化量*/
	public double dy;
	
	/**
	 * この関数は、指定サイズのオブジェクト配列を作ります。
	 * @param i_length
	 * 作成する配列の長さ
	 * @return
	 * 新しい配列。
	 */	
	public static NyARVecLinear2d[] createArray(int i_length)
	{
		NyARVecLinear2d[] r=new NyARVecLinear2d[i_length];
		for(int i=0;i<i_length;i++){
			r[i]=new NyARVecLinear2d();
		}
		return r;
	}
	/**
	 * この関数は、法線を計算します。
	 * 通過点は変更しません。
	 * @param i_src
	 * 元のインスタンスを指定します。この値には、thisを指定できます。
	 */
	public final void normalVec(NyARVecLinear2d i_src)
	{
		double w=this.dx;
		this.dx=i_src.dy;
		this.dy=-w;
	}
	/**
	 * この関数は、オブジェクトの値をインスタンスにセットします。
	 * @param i_value
	 * コピー元のオブジェクト
	 */
	public final void setValue(NyARVecLinear2d i_value)
	{
		this.dx=i_value.dx;
		this.dy=i_value.dy;
		this.x=i_value.x;
		this.y=i_value.y;
	}
	/**
	 * この関数は、この直線と引数の直線とが作るCos値を返します。
	 * @param i_v1
	 * 直線を格納したオブジェクト
	 * @return
	 * ２直線のCOS値（radian）
	 */
	public final double getVecCos(NyARVecLinear2d i_v1)
	{
		double x1=i_v1.dx;
		double y1=i_v1.dy;
		double x2=this.dx;
		double y2=this.dy;
		double d=(x1*x2+y1*y2)/Math.sqrt((x1*x1+y1*y1)*(x2*x2+y2*y2));
		return d;
	}
	/**
	 * この関数は、この直線と引数の直線とが作るCos値の絶対値を返します。
	 * @param i_v1
	 * 直線を格納したオブジェクト
	 * @return
	 * ２直線のCOS値の絶対値（radian）
	 */
	public final double getAbsVecCos(NyARVecLinear2d i_v1)
	{
		double d=getVecCos(i_v1);
		return d>=0?d:-d;
	}
	/**
	 * この関数は、この直線とベクトルが作るCos値を返します。
	 * @param i_dx
	 * ベクトルのX成分
	 * @param i_dy
	 * ベクトルのY成分
	 * @return
	 * ２直線のCOS値（radian）
	 */
	public final double getVecCos(double i_dx,double i_dy)
	{
		double x1=this.dx;
		double y1=this.dy;
		return (x1*i_dx+y1*i_dy)/Math.sqrt((x1*x1+y1*y1)*(i_dx*i_dx+i_dy*i_dy));
	}
	/**
	 * この関数は、この直線とベクトルが作るCos値の絶対値を返します。
	 * @param i_v2_x
	 * ベクトルのX成分
	 * @param i_v2_y
	 * ベクトルのY成分
	 * @return
	 * ２直線のCOS値の絶対値（radian）
	 */
	public final double getAbsVecCos(double i_v2_x,double i_v2_y)
	{
		double d=getVecCos(i_v2_x,i_v2_y);
		return d>=0?d:-d;
	}	
	/**
	 * この関数は、この直線と線分が作るCos値を返します。
	 * @param i_pos1
	 * 線分の端点１
	 * @param i_pos2
	 * 線分の端点２
	 * @return
	 * ２直線のCOS値（radian）
	 */
	public final double getVecCos(NyARDoublePoint2d i_pos1,NyARDoublePoint2d i_pos2)
	{
		return getVecCos(i_pos2.x-i_pos1.x,i_pos2.y-i_pos1.y);
	}	

	/**
	 * この関数は、この直線と線分が作るCos値の絶対値を返します。
	 * @param i_pos1
	 * 線分の端点１
	 * @param i_pos2
	 * 線分の端点２
	 * @return
	 * ２直線のCOS値の絶対値（radian）
	 */
	public final double getAbsVecCos(NyARDoublePoint2d i_pos1,NyARDoublePoint2d i_pos2)
	{
		return getAbsVecCos(i_pos2.x-i_pos1.x,i_pos2.y-i_pos1.y);
	}
	
	/**
	 * この関数は、直線との交点を求めます。
	 * @param i_vector1
	 * 交点を求める直線
	 * @param o_point
	 * 交点座標を得るオブジェクト。
	 * @return
	 * 交点が求まると、trueを返します。
	 */
	public final boolean crossPos(NyARVecLinear2d i_vector1,NyARDoublePoint2d o_point)
	{
		double a1= i_vector1.dy;
		double b1=-i_vector1.dx;
		double c1=(i_vector1.dx*i_vector1.y-i_vector1.dy*i_vector1.x);
		double a2= this.dy;
		double b2=-this.dx;
		double c2=(this.dx*this.y-this.dy*this.x);
		final double w1 = a1 * b2 - a2 * b1;
		if (w1 == 0.0) {
			return false;
		}
		o_point.x = (b1 * c2 - b2 * c1) / w1;
		o_point.y = (a2 * c1 - a1 * c2) / w1;
		return true;
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
		double sa,sb,sc;
		sa= this.dy;
		sb=-this.dx;
		sc=(this.dx*this.y-this.dy*this.x);
		

		double lc;
		double x,y,w1;
		//thisを法線に変換

		//交点を計算
		w1 = sa * (-sa) - sb * sb;
		if (w1 == 0.0) {
			return Double.POSITIVE_INFINITY;
		}
		//i_sp1と、i_linerの交点
		lc=-(sb*i_sp1.x-sa*i_sp1.y);
		x = ((sb * lc +sa * sc) / w1)-i_sp1.x;
		y = ((sb * sc - sa * lc) / w1)-i_sp1.y;
		double sqdist=x*x+y*y;

		lc=-(sb*i_sp2.x-sa*i_sp2.y);
		x = ((sb * lc + sa * sc) / w1)-i_sp2.x;
		y = ((sb * sc - sa * lc) / w1)-i_sp2.y;

		return sqdist+x*x+y*y;
	}

	/**
	 * この関数は、i_lineの直線を、インスタンスにセットします。
	 * {@link #x},{@link #y}の値は、(i_x,i_y)を通過するi_lineの法線とi_lineの交点をセットします。
	 * @param i_line
	 * セットする直線式
	 * @param i_x
	 * {@link #x},{@link #y}を確定するための、法線の通過点
	 * @param i_y
	 * {@link #x},{@link #y}を確定するための、法線の通過点
	 * @return
	 * セットに成功すると、trueを返します。
	 */
	public boolean setLinear(NyARLinear i_line,double i_x,double i_y)
	{
		double la=i_line.b;
		double lb=-i_line.a;
		double lc=-(la*i_x+lb*i_y);
		//交点を計算
		final double w1 = -lb * lb - la * la;
		if (w1 == 0.0) {
			return false;
		}		
		this.x=((la * lc - lb * i_line.c) / w1);
		this.y= ((la * i_line.c +lb * lc) / w1);
		this.dy=-lb;
		this.dx=-la;
		return true;
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
	public final boolean leastSquares(NyARDoublePoint2d[] i_points,int i_number_of_data)
	{
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
		double la=-(i_number_of_data * sum_x2 - sum_x*sum_x);
		double lb=-(i_number_of_data * sum_xy - sum_x * sum_y);
		double cc=(sum_x2 * sum_y - sum_xy * sum_x);
		double lc=-(la*sum_x+lb*sum_y)/i_number_of_data;
		//交点を計算
		final double w1 = -lb * lb - la * la;
		if (w1 == 0.0) {
			return false;
		}		
		this.x=((la * lc - lb * cc) / w1);
		this.y= ((la * cc +lb * lc) / w1);
		this.dy=-lb;
		this.dx=-la;
		return true;
	}
	/**
	 * この関数は、正規化したベクトルを出力する、{@link #leastSquares}です。
	 * @param i_points
	 * 頂点群を格納した配列。
	 * @param i_number_of_data
	 * 計算対象の頂点群の数
	 * @return
	 * 計算に成功すると、trueを返します。
	 */
	public final boolean leastSquaresWithNormalize(NyARDoublePoint2d[] i_points,int i_number_of_data)
	{
		boolean ret=this.leastSquares(i_points, i_number_of_data);
		double sq=1/Math.sqrt(this.dx*this.dx+this.dy*this.dy);
		this.dx*=sq;
		this.dy*=sq;
		return ret;
	}

}
