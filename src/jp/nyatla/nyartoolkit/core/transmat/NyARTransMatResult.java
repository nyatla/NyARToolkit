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
package jp.nyatla.nyartoolkit.core.transmat;



import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.*;

/**
 * NyARTransMat戻り値専用のNyARMat
 * 
 */
public class NyARTransMatResult extends NyARDoubleMatrix44
{
	/**
	 * この行列に1度でも行列をセットしたかを返します。
	 */
	public boolean has_value = false;
	/**
	 * 観測値とのずれを示すエラーレート値です。SetValueにより更新されます。
	 * {@link #has_value}がtrueの時に使用可能です。
	 */
	public double last_error;
	/**
	 * コンストラクタです。
	 */
	public NyARTransMatResult()
	{
		this.m30=this.m31=this.m32=0;
		this.m33=1.0;
	}
	/**
	 * 平行移動量と回転行列をセットします。この関数は、INyARTransmatインタフェイスのクラスが結果を保存するために使います。
	 * @param i_rot
	 * @param i_trans
	 */
	public final void setValue(NyARDoubleMatrix33 i_rot, NyARDoublePoint3d i_trans,double i_error)
	{
		this.m00=i_rot.m00;
		this.m01=i_rot.m01;
		this.m02=i_rot.m02;
		this.m03=i_trans.x;

		this.m10 =i_rot.m10;
		this.m11 =i_rot.m11;
		this.m12 =i_rot.m12;
		this.m13 =i_trans.y;

		this.m20 = i_rot.m20;
		this.m21 = i_rot.m21;
		this.m22 = i_rot.m22;
		this.m23 = i_trans.z;

		this.m30=this.m31=this.m32=0;
		this.m33=1.0;		
		this.has_value = true;
		this.last_error=i_error;
		return;
	}	


	
	/**
	 * 座標変換した3次元頂点をi_perspectiveで2次元座標(画面上の点)に変換して返します。
	 * @param i_x
	 * @param i_y
	 * @param i_z
	 * @param i_projectionmat
	 * 射影変換に使用するProjectionMatrix
	 * @param o_2d
	 */
	public final void transformVertex2d(double i_x,double i_y,double i_z,NyARPerspectiveProjectionMatrix i_projectionmat,NyARDoublePoint2d o_2d)
	{
		i_projectionmat.projectionConvert(
			this.m00*i_x+this.m01*i_y+this.m02*i_z+this.m03,
			this.m10*i_x+this.m11*i_y+this.m12*i_z+this.m13,
			this.m20*i_x+this.m21*i_y+this.m22*i_z+this.m23,
			o_2d);
	}
	/**
	 * 座標変換した3次元頂点をi_perspectiveで2次元座標(画面上の点)に変換して返します。
	 * @param i_x
	 * @param i_y
	 * @param i_z
	 * @param i_projectionmat
	 * 射影変換に使用するProjectionMatrix
	 * @param o_2d
	 */
	public final void transformVertex2d(double i_x,double i_y,double i_z,NyARPerspectiveProjectionMatrix i_projectionmat,NyARIntPoint2d o_2d)
	{
		i_projectionmat.projectionConvert(
			this.m00*i_x+this.m01*i_y+this.m02*i_z+this.m03,
			this.m10*i_x+this.m11*i_y+this.m12*i_z+this.m13,
			this.m20*i_x+this.m21*i_y+this.m22*i_z+this.m23,
			o_2d);
	}
	
	/**
	 * 出力型の異なるtransformParallelRect2dです。
	 * @param i_projectionmat
	 * @param i_l
	 * @param i_t
	 * @param i_w
	 * @param i_h
	 * @param o_2d
	 * @return
	 * @throws NyARException
	 */
	public final boolean transformParallelRect2d(double i_l, double i_t, double i_w,double i_h,NyARPerspectiveProjectionMatrix i_projectionmat, NyARIntPoint2d[] o_2d) throws NyARException
	{
		double cp00, cp01, cp02, cp11, cp12;
		double right=i_l+i_w;
		double bottom=i_l+i_h;
		cp00 = i_projectionmat.m00;
		cp01 = i_projectionmat.m01;
		cp02 = i_projectionmat.m02;
		cp11 = i_projectionmat.m11;
		cp12 = i_projectionmat.m12;
		//マーカと同一平面上にある矩形の4個の頂点を座標変換して、射影変換して画面上の
		//頂点を計算する。
		//[hX,hY,h]=[P][RT][x,y,z]
		
		double yt0,yt1,yt2;
		double x3, y3, z3;
		
		double m00=this.m00;
		double m10=this.m10;
		double m20=this.m20;
		
		//yとtの要素を先に計算
		yt0=this.m01 * i_t+this.m03;
		yt1=this.m11 * i_t+this.m13;
		yt2=this.m21 * i_t+this.m23;
		// l,t
		x3 = m00 * i_l + yt0;
		y3 = m10 * i_l + yt1;
		z3 = m20 * i_l + yt2;
		o_2d[0].x = (int) ((x3 * cp00 + y3 * cp01 + z3 * cp02) / z3);
		o_2d[0].y = (int) ((y3 * cp11 + z3 * cp12) / z3);
		// r,t
		x3 = m00 * right + yt0;
		y3 = m10 * right + yt1;
		z3 = m20 * right + yt2;
		o_2d[1].x = (int) ((x3 * cp00 + y3 * cp01 + z3 * cp02) / z3);
		o_2d[1].y = (int) ((y3 * cp11 + z3 * cp12) / z3);

		//yとtの要素を先に計算
		yt0=this.m01 * bottom+this.m03;
		yt1=this.m11 * bottom+this.m13;
		yt2=this.m21 * bottom+this.m23;

		// r,b
		x3 = m00 * right + yt0;
		y3 = m10 * right + yt1;
		z3 = m20 * right + yt2;
		o_2d[2].x = (int) ((x3 * cp00 + y3 * cp01 + z3 * cp02) / z3);
		o_2d[2].y = (int) ((y3 * cp11 + z3 * cp12) / z3);
		// l,b
		x3 = m00 * i_l + yt0;
		y3 = m10 * i_l + yt1;
		z3 = m20 * i_l + yt2;
		o_2d[3].x = (int) ((x3 * cp00 + y3 * cp01 + z3 * cp02) / z3);
		o_2d[3].y = (int) ((y3 * cp11 + z3 * cp12) / z3);
		return true;
	}
	/**
	 * マーカと同じ平面にある任意の2次元矩形を、i_perspectiveで2次元座標(画面上の点)に変換して、頂点座標をo_2dへ返します。
	 * <p>このコードは、4頂点を定義して、それぞれの頂点を座標変換→射影変換するコードから、0成分を削除して最適化したもの。</p>
	 * @param i_projectionmat
	 * @param i_l
	 * マーカ座標系の原点を中心とした、左上の座標(X)
	 * @param i_t
	 * マーカ座標系の原点を中心とした、左上の座標(Y)
	 * @param i_w
	 * 矩形のサイズ(X)
	 * @param i_h
	 * 矩形のサイズ(Y)
	 * @param o_2d
	 * 変換した頂点座標を格納する配列です。4要素の配列を指定してください。この配列は、LT,RT,RB,LBの順です。
	 * @return
	 * 成功するとtrueを返します。
	 * @throws NyARException
	 */
	public final boolean transformParallelRect2d(double i_l, double i_t, double i_w,double i_h,NyARPerspectiveProjectionMatrix i_projectionmat, NyARDoublePoint2d[] o_2d) throws NyARException
	{
		double cp00, cp01, cp02, cp11, cp12;
		double right=i_l+i_w;
		double bottom=i_l+i_h;
		cp00 = i_projectionmat.m00;
		cp01 = i_projectionmat.m01;
		cp02 = i_projectionmat.m02;
		cp11 = i_projectionmat.m11;
		cp12 = i_projectionmat.m12;
		//マーカと同一平面上にある矩形の4個の頂点を座標変換して、射影変換して画面上の
		//頂点を計算する。
		//[hX,hY,h]=[P][RT][x,y,z]
		
		double yt0,yt1,yt2;
		double x3, y3, z3;
		
		double m00=this.m00;
		double m10=this.m10;
		double m20=this.m20;
		
		//yとtの要素を先に計算
		yt0=this.m01 * i_t+this.m03;
		yt1=this.m11 * i_t+this.m13;
		yt2=this.m21 * i_t+this.m23;
		// l,t
		x3 = m00 * i_l + yt0;
		y3 = m10 * i_l + yt1;
		z3 = m20 * i_l + yt2;
		o_2d[0].x = (int) ((x3 * cp00 + y3 * cp01 + z3 * cp02) / z3);
		o_2d[0].y = (int) ((y3 * cp11 + z3 * cp12) / z3);
		// r,t
		x3 = m00 * right + yt0;
		y3 = m10 * right + yt1;
		z3 = m20 * right + yt2;
		o_2d[1].x = (int) ((x3 * cp00 + y3 * cp01 + z3 * cp02) / z3);
		o_2d[1].y = (int) ((y3 * cp11 + z3 * cp12) / z3);

		//yとtの要素を先に計算
		yt0=this.m01 * bottom+this.m03;
		yt1=this.m11 * bottom+this.m13;
		yt2=this.m21 * bottom+this.m23;

		// r,b
		x3 = m00 * right + yt0;
		y3 = m10 * right + yt1;
		z3 = m20 * right + yt2;
		o_2d[2].x = ((x3 * cp00 + y3 * cp01 + z3 * cp02) / z3);
		o_2d[2].y = ((y3 * cp11 + z3 * cp12) / z3);
		// l,b
		x3 = m00 * i_l + yt0;
		y3 = m10 * i_l + yt1;
		z3 = m20 * i_l + yt2;
		o_2d[3].x = ((x3 * cp00 + y3 * cp01 + z3 * cp02) / z3);
		o_2d[3].y = ((y3 * cp11 + z3 * cp12) / z3);
		return true;
	}
}
