/* 
 * PROJECT: NyARToolkit Java3D utilities.
 * --------------------------------------------------------------------------------
 * The MIT License
 * Copyright (c) 2008 nyatla
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/nyartoolkit/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
package jp.nyatla.nyartoolkit.java3d.utils;

import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.param.INyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

import javax.media.j3d.Transform3D;
/**
 * NyARParamにJava3D向け関数を追加したもの
 */
public class J3dNyARParam extends NyARParam
{
	private double view_distance_min = 0.01;//1cm～10.0m
	private double view_distance_max = 10.0;
	private Transform3D m_projection = null;
	/**
	 * テストパラメータを格納したインスタンスを生成する。
	 * @return
	 * @throws NyARException 
	 */
	public static J3dNyARParam loadDefaultParameter() throws NyARException
	{
		ParamLoader pm=new ParamLoader();
		return new J3dNyARParam(pm.size,pm.pmat,pm.dist_factor);
	}
	/**
	 * i_streamからARToolkitのカメラパラメータを読み出して、インスタンスに格納して返します。
	 * @param i_stream
	 * @return
	 * @throws NyARException
	 */
	public static J3dNyARParam loadARParamFile(InputStream i_stream) throws NyARException
	{
		ParamLoader pm=new ParamLoader(i_stream);
		return new J3dNyARParam(pm.size,pm.pmat,pm.dist_factor);
	}
	public static J3dNyARParam createFromCvCalibrateCamera2Result(int i_w,int i_h,double[] i_intrinsic_matrix,double[] i_distortion_coeffs)
	{
		ParamLoader pm=new ParamLoader(i_w,i_h,i_intrinsic_matrix,i_distortion_coeffs);
		return new J3dNyARParam(pm.size,pm.pmat,pm.dist_factor);
	}
	
	public J3dNyARParam(NyARIntSize i_screen_size,NyARPerspectiveProjectionMatrix i_projection_mat,INyARCameraDistortionFactor i_dist_factor)
	{
		super(i_screen_size,i_projection_mat,i_dist_factor);
	}	

	/**
	 * 視体積の近い方をメートルで指定
	 * @param i_new_value
	 */
	public void setViewDistanceMin(double i_new_value)
	{
		m_projection = null;//キャッシュ済変数初期化
		view_distance_min = i_new_value;
	}

	/**
	 * 視体積の遠い方をメートルで指定
	 * @param i_new_value
	 */
	public void setViewDistanceMax(double i_new_value)
	{
		m_projection = null;//キャッシュ済変数初期化
		view_distance_max = i_new_value;
	}

	/**
	 * void arglCameraFrustumRH(const ARParam *cparam, const double focalmin, const double focalmax, GLdouble m_projection[16])
	 * 関数の置き換え
	 * @param focalmin
	 * @param focalmax
	 * @return
	 */
	public Transform3D getCameraTransform()
	{
		//既に値がキャッシュされていたらそれを使う
		if (m_projection != null) {
			return m_projection;
		}		
		//無ければ計算
		NyARDoubleMatrix44 tmp=new NyARDoubleMatrix44();
		this.makeCameraFrustumRH(view_distance_min, view_distance_max,tmp);
		this.m_projection =new Transform3D(new double[]{
			tmp.m00,tmp.m01,tmp.m02,tmp.m03,
			tmp.m10,tmp.m11,tmp.m12,tmp.m13,
			-tmp.m20,-tmp.m21,-tmp.m22,-tmp.m23,
			tmp.m30,tmp.m31,tmp.m32,tmp.m33
			});

		return m_projection;
	}
}
