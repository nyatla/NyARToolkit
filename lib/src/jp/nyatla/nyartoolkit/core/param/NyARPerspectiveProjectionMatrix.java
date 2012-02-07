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
package jp.nyatla.nyartoolkit.core.param;

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、ARToolKit形式の透視変換行列を格納します。
 * 透視変換関数と、射影変換行列の生成関数を提供します。
 * このクラスは{@link NyARParam}に所有されることを前提にしており、単独の仕様は考慮されていません。
 * <p>アルゴリズム -
 * http://www.hitl.washington.edu/artoolkit/Papers/ART02-Tutorial.pdfの7ページを参照。
 * </p>
 */
final public class NyARPerspectiveProjectionMatrix extends NyARDoubleMatrix44
{
	/**
	 * コンストラクタです。空の行列を生成します。
	 */
	public NyARPerspectiveProjectionMatrix()
	{
		this.m30=this.m31=this.m32=0;
		this.m33=1;
	}
	/*
	 * static double dot( double a1, double a2, double a3,double b1, double b2,double b3 )
	 */
	private final static double dot(double a1, double a2, double a3, double b1,double b2, double b3)
	{
		return (a1 * b1 + a2 * b2 + a3 * b3);
	}

	/* static double norm( double a, double b, double c ) */
	private final static double norm(double a, double b, double c)
	{
		return Math.sqrt(a * a + b * b + c * c);
	}
	/**
	 * この関数は、ARToolKitのarParamDecompMatと同じです。
	 * 動作はよくわかりません…。
	 * @param o_cpara
	 * 詳細不明。3x4のマトリクスを指定すること。
	 * @param o_trans
	 * 詳細不明。3x4のマトリクスを指定すること。
	 */
	public void decompMat(NyARMat o_cpara, NyARMat o_trans)
	{
		double rem1, rem2, rem3;
		double c00,c01,c02,c03,c10,c11,c12,c13,c20,c21,c22,c23;
		if (this.m23>= 0) {// if( source[2][3] >= 0 ) {
			c00=this.m00;
			c01=this.m01;
			c02=this.m02;
			c03=this.m03;
			c10=this.m10;
			c11=this.m11;
			c12=this.m12;
			c13=this.m13;
			c20=this.m20;
			c21=this.m21;
			c22=this.m22;
			c23=this.m23;
		} else {
			// <Optimize>
			// for(int r = 0; r < 3; r++ ){
			// for(int c = 0; c < 4; c++ ){
			// Cpara[r][c]=-source[r][c];//Cpara[r][c] = -(source[r][c]);
			// }
			// }
			c00=-this.m00;
			c01=-this.m01;
			c02=-this.m02;
			c03=-this.m03;
			c10=-this.m10;
			c11=-this.m11;
			c12=-this.m12;
			c13=-this.m13;
			c20=-this.m20;
			c21=-this.m21;
			c22=-this.m22;
			c23=-this.m23;
		}

		double[][] cpara = o_cpara.getArray();
		double[][] trans = o_trans.getArray();
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 4; c++) {
				cpara[r][c] = 0.0;// cpara[r][c] = 0.0;
			}
		}
		cpara[2][2] = norm(c20, c21, c22);// cpara[2][2] =norm( Cpara[2][0],Cpara[2][1],Cpara[2][2]);
		trans[2][0] = c20 / cpara[2][2];// trans[2][0] = Cpara[2][0] /cpara[2][2];
		trans[2][1] = c21 / cpara[2][2];// trans[2][1] = Cpara[2][1] / cpara[2][2];
		trans[2][2] = c22 / cpara[2][2];// trans[2][2] =Cpara[2][2] /cpara[2][2];
		trans[2][3] = c23 / cpara[2][2];// trans[2][3] =Cpara[2][3] /cpara[2][2];

		cpara[1][2] = dot(trans[2][0], trans[2][1], trans[2][2], c10, c11, c12);// cpara[1][2]=dot(trans[2][0],trans[2][1],trans[2][2],Cpara[1][0],Cpara[1][1],Cpara[1][2]);
		rem1 = c10 - cpara[1][2] * trans[2][0];// rem1 =Cpara[1][0] -cpara[1][2] *trans[2][0];
		rem2 = c11 - cpara[1][2] * trans[2][1];// rem2 =Cpara[1][1] -cpara[1][2] *trans[2][1];
		rem3 = c12 - cpara[1][2] * trans[2][2];// rem3 =Cpara[1][2] -cpara[1][2] *trans[2][2];
		cpara[1][1] = norm(rem1, rem2, rem3);// cpara[1][1] = norm( rem1,// rem2, rem3 );
		trans[1][0] = rem1 / cpara[1][1];// trans[1][0] = rem1 / cpara[1][1];
		trans[1][1] = rem2 / cpara[1][1];// trans[1][1] = rem2 / cpara[1][1];
		trans[1][2] = rem3 / cpara[1][1];// trans[1][2] = rem3 / cpara[1][1];

		cpara[0][2] = dot(trans[2][0], trans[2][1], trans[2][2], c00, c01, c02);// cpara[0][2] =dot(trans[2][0], trans[2][1],trans[2][2],Cpara[0][0],Cpara[0][1],Cpara[0][2]);
		cpara[0][1] = dot(trans[1][0], trans[1][1], trans[1][2], c00, c01, c02);// cpara[0][1]=dot(trans[1][0],trans[1][1],trans[1][2],Cpara[0][0],Cpara[0][1],Cpara[0][2]);
		rem1 = c00 - cpara[0][1] * trans[1][0] - cpara[0][2]* trans[2][0];// rem1 = Cpara[0][0] - cpara[0][1]*trans[1][0]- cpara[0][2]*trans[2][0];
		rem2 = c01 - cpara[0][1] * trans[1][1] - cpara[0][2]* trans[2][1];// rem2 = Cpara[0][1] - cpara[0][1]*trans[1][1]- cpara[0][2]*trans[2][1];
		rem3 = c02 - cpara[0][1] * trans[1][2] - cpara[0][2]* trans[2][2];// rem3 = Cpara[0][2] - cpara[0][1]*trans[1][2] - cpara[0][2]*trans[2][2];
		cpara[0][0] = norm(rem1, rem2, rem3);// cpara[0][0] = norm( rem1,rem2, rem3 );
		trans[0][0] = rem1 / cpara[0][0];// trans[0][0] = rem1 / cpara[0][0];
		trans[0][1] = rem2 / cpara[0][0];// trans[0][1] = rem2 / cpara[0][0];
		trans[0][2] = rem3 / cpara[0][0];// trans[0][2] = rem3 / cpara[0][0];

		trans[1][3] = (c13 - cpara[1][2] * trans[2][3])/ cpara[1][1];// trans[1][3] = (Cpara[1][3] -cpara[1][2]*trans[2][3]) / cpara[1][1];
		trans[0][3] = (c03 - cpara[0][1] * trans[1][3] - cpara[0][2]* trans[2][3])/ cpara[0][0];// trans[0][3] = (Cpara[0][3] -cpara[0][1]*trans[1][3]-cpara[0][2]*trans[2][3]) / cpara[0][0];

		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				cpara[r][c] /= cpara[2][2];// cpara[r][c] /= cpara[2][2];
			}
		}
		return;
	}
	/**
	 * 行列にスケール値を積算します。
	 * @param i_scale
	 * スケール値
	 */
	public void changeScale(double i_scale)
	{
		this.m00=this.m00*i_scale;
		this.m10=this.m10*i_scale;
		this.m01=this.m01*i_scale;
		this.m11=this.m11*i_scale;
		this.m02=this.m02*i_scale;
		this.m12=this.m12*i_scale;
		this.m03=this.m03*i_scale;
		this.m13=this.m13*i_scale;
		//for (int i = 0; i < 4; i++) {
		//	array34[0 * 4 + i] = array34[0 * 4 + i] * scale;// newparam->mat[0][i]=source->mat[0][i]* scale;
		//	array34[1 * 4 + i] = array34[1 * 4 + i] * scale;// newparam->mat[1][i]=source->mat[1][i]* scale;
		//	array34[2 * 4 + i] = array34[2 * 4 + i];// newparam->mat[2][i] = source->mat[2][i];
		//}
		return;
	}
	
	/**
	 * 座標値を射影変換します。
	 * @param i_3dvertex
	 * 変換元の座標値
	 * @param o_2d
	 * 変換後の座標値を受け取るオブジェクト
	 */
	public final void project(NyARDoublePoint3d i_3dvertex,NyARDoublePoint2d o_2d)
	{
		double w=1/(i_3dvertex.z*this.m22);
		o_2d.x=(i_3dvertex.x*this.m00+i_3dvertex.y*this.m01+i_3dvertex.z*this.m02)*w;
		o_2d.y=(i_3dvertex.y*this.m11+i_3dvertex.z*this.m12)*w;
		return;
	}
	/**
	 * 座標値を射影変換します。
	 * @param i_x
	 * 変換元の座標値
	 * @param i_y
	 * 変換元の座標値
	 * @param i_z
	 * 変換元の座標値
	 * @param o_2d
	 * 変換後の座標値を受け取るオブジェクト
	 */
	public final void project(double i_x,double i_y,double i_z,NyARDoublePoint2d o_2d)
	{
		double w=1/(i_z*this.m22);
		o_2d.x=(i_x*this.m00+i_y*this.m01+i_z*this.m02)*w;
		o_2d.y=(i_y*this.m11+i_z*this.m12)*w;
		return;
	}	
	/**
	 * 座標値を射影変換します。
	 * @param i_3dvertex
	 * 変換元の座標値
	 * @param o_2d
	 * 変換後の座標値を受け取るオブジェクト
	 */
	public final void project(NyARDoublePoint3d i_3dvertex,NyARIntPoint2d o_2d)
	{
		double w=1/(i_3dvertex.z*this.m22);
		o_2d.x=(int)((i_3dvertex.x*this.m00+i_3dvertex.y*this.m01+i_3dvertex.z*this.m02)*w);
		o_2d.y=(int)((i_3dvertex.y*this.m11+i_3dvertex.z*this.m12)*w);
		return;
	}	
	/**
	 * 座標値を射影変換します。
	 * @param i_x
	 * 変換元の座標値
	 * @param i_y
	 * 変換元の座標値
	 * @param i_z
	 * 変換元の座標値
	 * @param o_2d
	 * 変換後の座標値を受け取るオブジェクト
	 */
	public final void project(double i_x,double i_y,double i_z,NyARIntPoint2d o_2d)
	{
		double w=1/(i_z*this.m22);
		o_2d.x=(int)((i_x*this.m00+i_y*this.m01+i_z*this.m02)*w);
		o_2d.y=(int)((i_y*this.m11+i_z*this.m12)*w);
		return;
	}
	
	/**
	 * 右手系の視錐台を作ります。
	 * この視錐台は、ARToolKitのarglCameraViewRHの作る視錐台と同じです。
	 * @param i_screen_width
	 * スクリーンサイズを指定します。
	 * @param i_screen_height
	 * スクリーンサイズを指定します。
	 * @param i_dist_min
	 * near pointを指定します(mm単位)
	 * @param i_dist_max
	 * far pointを指定します(mm単位)
	 * @param o_frustum
	 * 視錐台の格納先オブジェクトを指定します。
	 */
	public void makeCameraFrustumRH(double i_screen_width,double i_screen_height,double i_dist_min,double i_dist_max,NyARDoubleMatrix44 o_frustum)
	{
		NyARMat trans_mat = new NyARMat(3, 4);
		NyARMat icpara_mat = new NyARMat(3, 4);
		double[][] p = new double[3][3];
		int i;
		
		this.decompMat(icpara_mat, trans_mat);

		double[][] icpara = icpara_mat.getArray();
		double[][] trans = trans_mat.getArray();
		for (i = 0; i < 4; i++) {
			icpara[1][i] = (i_screen_height - 1) * (icpara[2][i]) - icpara[1][i];
		}
		p[0][0] = icpara[0][0] / icpara[2][2];
		p[0][1] = icpara[0][1] / icpara[2][2];
		p[0][2] = icpara[0][2] / icpara[2][2];

		p[1][0] = icpara[1][0] / icpara[2][2];
		p[1][1] = icpara[1][1] / icpara[2][2];
		p[1][2] = icpara[1][2] / icpara[2][2];
		
		p[2][0] = icpara[2][0] / icpara[2][2];
		p[2][1] = icpara[2][1] / icpara[2][2];
		p[2][2] = icpara[2][2] / icpara[2][2];

		double q00,q01,q02,q03,q10,q11,q12,q13,q20,q21,q22,q23,q30,q31,q32,q33;
		
		//視錐台への変換
		q00 = (2.0 * p[0][0] / (i_screen_width - 1));
		q01 = (2.0 * p[0][1] / (i_screen_width - 1));
		q02 = -((2.0 * p[0][2] / (i_screen_width - 1)) - 1.0);
		q03 = 0.0;
		o_frustum.m00 = q00 * trans[0][0] + q01 * trans[1][0] + q02 * trans[2][0];
		o_frustum.m01 = q00 * trans[0][1] + q01 * trans[1][1] + q02 * trans[2][1];
		o_frustum.m02 = q00 * trans[0][2] + q01 * trans[1][2] + q02 * trans[2][2];
		o_frustum.m03 = q00 * trans[0][3] + q01 * trans[1][3] + q02 * trans[2][3] + q03;

		q10 = 0.0;
		q11 = -(2.0 * p[1][1] / (i_screen_height - 1));
		q12 = -((2.0 * p[1][2] / (i_screen_height - 1)) - 1.0);
		q13 = 0.0;
		o_frustum.m10 = q10 * trans[0][0] + q11 * trans[1][0] + q12 * trans[2][0];
		o_frustum.m11 = q10 * trans[0][1] + q11 * trans[1][1] + q12 * trans[2][1];
		o_frustum.m12 = q10 * trans[0][2] + q11 * trans[1][2] + q12 * trans[2][2];
		o_frustum.m13 = q10 * trans[0][3] + q11 * trans[1][3] + q12 * trans[2][3] + q13;

		q20 = 0.0;
		q21 = 0.0;
		q22 = (i_dist_max + i_dist_min) / (i_dist_min - i_dist_max);
		q23 = 2.0 * i_dist_max * i_dist_min / (i_dist_min - i_dist_max);
		o_frustum.m20 = q20 * trans[0][0] + q21 * trans[1][0] + q22 * trans[2][0];
		o_frustum.m21 = q20 * trans[0][1] + q21 * trans[1][1] + q22 * trans[2][1];
		o_frustum.m22 = q20 * trans[0][2] + q21 * trans[1][2] + q22 * trans[2][2];
		o_frustum.m23 = q20 * trans[0][3] + q21 * trans[1][3] + q22 * trans[2][3] + q23;

		q30 = 0.0;
		q31 = 0.0;
		q32 = -1.0;
		q33 = 0.0;
		o_frustum.m30 = q30 * trans[0][0] + q31 * trans[1][0] + q32 * trans[2][0];
		o_frustum.m31 = q30 * trans[0][1] + q31 * trans[1][1] + q32 * trans[2][1];
		o_frustum.m32 = q30 * trans[0][2] + q31 * trans[1][2] + q32 * trans[2][2];
		o_frustum.m33 = q30 * trans[0][3] + q31 * trans[1][3] + q32 * trans[2][3] + q33;
		return;
	}	
}
