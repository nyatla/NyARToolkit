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
package jp.nyatla.nyartoolkit.core.param;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

/**
 * このクラスは、視錐台と、これを使った演算関数を定義します。
 * クラスのメンバには、視錐台行列、その逆行列があります。
 * 提供する機能は、視錐台を使った演算です。
 */
public class NyARFrustum
{
	/** frustum行列*/
	protected NyARDoubleMatrix44 _frustum_rh=new NyARDoubleMatrix44();
	/** frustum逆行列*/
	protected NyARDoubleMatrix44 _inv_frustum_rh=new NyARDoubleMatrix44();
	/** 撮像画面のサイズ*/
	protected NyARIntSize _screen_size=new NyARIntSize();
	/**
	 * コンストラクタです。
	 * ARToolkitの射影変換行列から、インスタンスを作ります。
	 * @param i_projection
	 * @param i_width
	 * スクリーンサイズです。
	 * @param i_height
	 * スクリーンサイズです。
	 * @param i_near
	 * 近平面までの距離です。単位はmm
	 * @param i_far
	 * 遠平面までの距離です。単位はmm
	 */
	public NyARFrustum(NyARPerspectiveProjectionMatrix i_projection,int i_width,int i_height,double i_near,double i_far)
	{
		this.setValue(i_projection, i_width, i_height, i_near, i_far);
	}
	/**
	 * この関数は、ARToolKitスタイルの射影変換行列から視錐台を作成してセットします。
	 * @param i_projection
	 * ARToolKitスタイルの射影変換行列
	 * @param i_width
	 * スクリーンサイズです。
	 * @param i_height
	 * スクリーンサイズです。
	 * @param i_near
	 * nearポイントをmm単位で指定します。
	 * @param i_far
	 * farポイントをmm単位で指定します。
	 */
	public void setValue(NyARPerspectiveProjectionMatrix i_projection,int i_width,int i_height,double i_near,double i_far)
	{
		i_projection.makeCameraFrustumRH(i_width, i_height, i_near, i_far,this._frustum_rh);
		this._inv_frustum_rh.inverse(this._frustum_rh);
		this._screen_size.setValue(i_width,i_height);
	}
	/**
	 * このスクリーン座標を、撮像点座標に変換します。
	 * 撮像点の座標系は、カメラ座標系になります。
	 * <p>公式 - 
	 * この関数は、gluUnprojectのビューポートとモデルビュー行列を固定したものです。
	 * 公式は、以下の物使用しました。
	 * http://www.opengl.org/sdk/docs/man/xhtml/gluUnProject.xml
	 * ARToolKitの座標系に合せて計算するため、OpenGLのunProjectとはix,iyの与え方が違います。画面上の座標をそのまま与えてください。
	 * </p>
	 * @param ix
	 * スクリーン上の座標
	 * @param iy
	 * 画像上の座標
	 * @param o_point_on_screen
	 * 撮像点座標
	 */
	public final void unProject(double ix,double iy,NyARDoublePoint3d o_point_on_screen)
	{
		double n=(this._frustum_rh.m23/(this._frustum_rh.m22-1));
		NyARDoubleMatrix44 m44=this._inv_frustum_rh;
		double v1=(this._screen_size.w-ix-1)*2/this._screen_size.w-1.0;//ARToolKitのFrustramに合せてる。
		double v2=(this._screen_size.h-iy-1)*2/this._screen_size.h-1.0;
		double v3=2*n-1.0;
		double b=1/(m44.m30*v1+m44.m31*v2+m44.m32*v3+m44.m33);
		o_point_on_screen.x=(m44.m00*v1+m44.m01*v2+m44.m02*v3+m44.m03)*b;
		o_point_on_screen.y=(m44.m10*v1+m44.m11*v2+m44.m12*v3+m44.m13)*b;
		o_point_on_screen.z=(m44.m20*v1+m44.m21*v2+m44.m22*v3+m44.m23)*b;
		return;
	}
	/**
	 * この関数は、スクリーン上の点と原点を結ぶ直線と、任意姿勢の平面の交差点を、カメラの座標系で取得します。
	 * この座標は、カメラ座標系です。
	 * @param ix
	 * スクリーン上の座標
	 * @param iy
	 * スクリーン上の座標
	 * @param i_mat
	 * 平面の姿勢行列です。
	 * @param o_pos
	 * 結果を受け取るオブジェクトです。
	 */
	public final void unProjectOnCamera(double ix,double iy,NyARDoubleMatrix44 i_mat,NyARDoublePoint3d o_pos)
	{
		//画面→撮像点
		this.unProject(ix,iy,o_pos);
		//撮像点→カメラ座標系
		double nx=i_mat.m02;
		double ny=i_mat.m12;
		double nz=i_mat.m22;
		double mx=i_mat.m03;
		double my=i_mat.m13;
		double mz=i_mat.m23;
		double t=(nx*mx+ny*my+nz*mz)/(nx*o_pos.x+ny*o_pos.y+nz*o_pos.z);
		o_pos.x=t*o_pos.x;
		o_pos.y=t*o_pos.y;
		o_pos.z=t*o_pos.z;
	}	
	/**
	 * 画面上の点と原点を結ぶ直線と任意姿勢の平面の交差点を、平面の座標系で取得します。
	 * ARToolKitの本P175周辺の実装と同じです。
	 * <p>
	 * このAPIは繰り返し使用には最適化されていません。同一なi_matに繰り返しアクセスするときは、展開してください。
	 * </p>
	 * @param ix
	 * スクリーン上の座標
	 * @param iy
	 * スクリーン上の座標
	 * @param i_mat
	 * 平面の姿勢行列です。
	 * @param o_pos
	 * 結果を受け取るオブジェクトです。
	 * @return
	 * 計算に成功すると、trueを返します。
	 */
	public final boolean unProjectOnMatrix(double ix,double iy,NyARDoubleMatrix44 i_mat,NyARDoublePoint3d o_pos)
	{
		//交点をカメラ座標系で計算
		unProjectOnCamera(ix,iy,i_mat,o_pos);
		//座標系の変換
		NyARDoubleMatrix44 m=new NyARDoubleMatrix44();
		if(!m.inverse(i_mat)){
			return false;
		}
		m.transform3d(o_pos, o_pos);
		return true;
	}
	/**
	 * カメラ座標系の点を、スクリーン座標の点へ変換します。
	 * @param i_x
	 * カメラ座標系の点
	 * @param i_y
	 * カメラ座標系の点
	 * @param i_z
	 * カメラ座標系の点
	 * @param o_pos2d
	 * 結果を受け取るオブジェクトです。
	 */
	public final void project(double i_x,double i_y,double i_z,NyARDoublePoint2d o_pos2d)
	{
		NyARDoubleMatrix44 m=this._frustum_rh;
		double v3_1=1/i_z*m.m32;
		double w=this._screen_size.w;
		double h=this._screen_size.h;
		o_pos2d.x=w-(1+(i_x*m.m00+i_z*m.m02)*v3_1)*w/2;
		o_pos2d.y=h-(1+(i_y*m.m11+i_z*m.m12)*v3_1)*h/2;
		return;
	}
	/**
	 * 透視変換行列の参照値を返します。
	 * この値は読出し専用です。変更しないでください。
	 * @return
	 * [read only]透視変換行列を格納したオブジェクト
	 */
	public final NyARDoubleMatrix44 refMatrix()
	{
		return this._frustum_rh;
	}
	/**
	 * 透視変換行列の逆行列を返します。
	 * この値は読出し専用です。変更しないでください。
	 * @return
	 * [read only]透視変換行列の逆行列を格納したオブジェクト
	 */
	public final NyARDoubleMatrix44 refInvMatrix()
	{
		return this._inv_frustum_rh;
	}
	
}