/* 
 * PROJECT: NyARToolkit JOGL utilities.
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
package jp.nyatla.nyartoolkit.jogl.utils;

import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;
/**
 * このクラスは、NyARToolkitのデータ型と、OpenGLデータ型を変換する関数を定義します。
 * 描画系関数は{@link NyARGLDrawUtil}を参照してください。
 */
public class NyARGLUtil
{
	/**
	 * NyARToolKit 2.53以前のコードと互換性を持たせるためのスケール値。
	 * {@link #toCameraFrustumRH}のi_scaleに設定することで、以前のバージョンの数値系と互換性を保ちます。
	 */
	public final static double SCALE_FACTOR_toCameraFrustumRH_NYAR2=1.0;
	/**
	 * NyARToolKit 2.53以前のコードと互換性を持たせるためのスケール値。
	 * {@link #toCameraViewRH}のi_scaleに設定することで、以前のバージョンの数値系と互換性を保ちます。
	 */
	public final static double SCALE_FACTOR_toCameraViewRH_NYAR2=1/0.025;

	private NyARGLUtil()
    {//生成の禁止
    }	

	
	/**
	 * この関数は、ARToolKitスタイルのカメラパラメータから、 CameraFrustamを計算します。
	 * カメラパラメータの要素のうち、ProjectionMatrix成分のみを使います。
	 * @param i_arparam
	 * ARToolKitスタイルのカメラパラメータ。
	 * @param i_scale
	 * スケール値を指定します。1=1mmです。10ならば1=1cm,1000ならば1=1mです。
	 * 2.53以前のNyARToolkitと互換性を持たせるときは、{@link #SCALE_FACTOR_toCameraFrustumRH_NYAR2}を指定してください。
	 * @param i_near
	 * 視錐体のnearPointを指定します。単位は、i_scaleに設定した値で決まります。
	 * @param i_far
	 * 視錐体のfarPointを指定します。単位は、i_scaleに設定した値で決まります。
	 * @param o_gl_projection
	 * OpenGLスタイルのProjectionMatrixです。double[16]を指定します。
	 */
	public static void toCameraFrustumRH(NyARParam i_arparam,double i_scale,double i_near,double i_far,double[] o_gl_projection)
	{
		toCameraFrustumRH(i_arparam.getPerspectiveProjectionMatrix(),i_arparam.getScreenSize(),i_scale,i_near,i_far,o_gl_projection);
		return;
	}
	/**
	 * この関数は、ARToolKitスタイルのProjectionMatrixから、 CameraFrustamを計算します。
	 * @param i_promat
	 * @param i_size
	 * スクリーンサイズを指定します。
	 * @param i_scale
	 * {@link #toCameraFrustumRH(NyARParam i_arparam,double i_scale,double i_near,double i_far,double[] o_gl_projection)}を参照。
	 * @param i_near
	 * {@link #toCameraFrustumRH(NyARParam i_arparam,double i_scale,double i_near,double i_far,double[] o_gl_projection)}を参照。
	 * @param i_far
	 * {@link #toCameraFrustumRH(NyARParam i_arparam,double i_scale,double i_near,double i_far,double[] o_gl_projection)}を参照。
	 * @param o_gl_projection
	 * {@link #toCameraFrustumRH(NyARParam i_arparam,double i_scale,double i_near,double i_far,double[] o_gl_projection)}を参照。
	 */
	public static void toCameraFrustumRH(NyARPerspectiveProjectionMatrix i_promat,NyARIntSize i_size,double i_scale,double i_near,double i_far,double[] o_gl_projection)
	{
		NyARDoubleMatrix44 m=new NyARDoubleMatrix44();
		i_promat.makeCameraFrustumRH(i_size.w,i_size.h,i_near*i_scale,i_far*i_scale,m);
		m.getValueT(o_gl_projection);
		return;
	}
	/**
	 * この関数は、NyARTransMatResultをOpenGLのModelView行列へ変換します。
	 * @param mat
	 * 変換元の行列
	 * @param i_scale
	 * 座標系のスケール値を指定します。1=1mmです。10ならば1=1cm,1000ならば1=1mです。
	 * 2.53以前のNyARToolkitと互換性を持たせるときは、{@link #SCALE_FACTOR_toCameraViewRH_NYAR2}を指定してください。
	 * @param o_gl_result
	 * OpenGLスタイルのProjectionMatrixです。double[16]を指定します。
	 */
	public static void toCameraViewRH(NyARDoubleMatrix44 mat,double i_scale, double[] o_gl_result)
	{
		o_gl_result[0 + 0 * 4] = mat.m00; 
		o_gl_result[1 + 0 * 4] = -mat.m10;
		o_gl_result[2 + 0 * 4] = -mat.m20;
		o_gl_result[3 + 0 * 4] = 0.0;
		o_gl_result[0 + 1 * 4] = mat.m01;
		o_gl_result[1 + 1 * 4] = -mat.m11;
		o_gl_result[2 + 1 * 4] = -mat.m21;
		o_gl_result[3 + 1 * 4] = 0.0;
		o_gl_result[0 + 2 * 4] = mat.m02;
		o_gl_result[1 + 2 * 4] = -mat.m12;
		o_gl_result[2 + 2 * 4] = -mat.m22;
		o_gl_result[3 + 2 * 4] = 0.0;
		
		double scale=1/i_scale;
		o_gl_result[0 + 3 * 4] = mat.m03*scale;
		o_gl_result[1 + 3 * 4] = -mat.m13*scale;
		o_gl_result[2 + 3 * 4] = -mat.m23*scale;
		o_gl_result[3 + 3 * 4] = 1.0;
		return;
	}


	
}
