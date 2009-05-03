/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.transmat.optimize;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.transmat.fitveccalc.NyARFitVecCalculator;
import jp.nyatla.nyartoolkit.core.transmat.rotmatrix.NyARRotMatrix;
import jp.nyatla.nyartoolkit.core.types.matrix.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * 処理構造がわかる程度に展開したNyARRotTransOptimize
 * 
 */
public class NyARRotTransOptimize_Base implements INyARRotTransOptimize
{
	private final static int AR_GET_TRANS_MAT_MAX_LOOP_COUNT = 5;// #define AR_GET_TRANS_MAT_MAX_LOOP_COUNT 5

	private final static double AR_GET_TRANS_MAT_MAX_FIT_ERROR = 1.0;// #define AR_GET_TRANS_MAT_MAX_FIT_ERROR 1.0

	private final NyARPerspectiveProjectionMatrix _projection_mat_ref;

	public NyARRotTransOptimize_Base(NyARPerspectiveProjectionMatrix i_projection_mat_ref)
	{
		this._projection_mat_ref = i_projection_mat_ref;
		return;
	}

	final public double optimize(NyARRotMatrix io_rotmat, NyARDoublePoint3d io_transvec, NyARFitVecCalculator i_calculator) throws NyARException
	{
		final NyARDoublePoint2d[] fit_vertex = i_calculator.getFitSquare();
		final NyARDoublePoint3d[] offset_square = i_calculator.getOffsetVertex().vertex;

		double err = -1;
		/* ループを抜けるタイミングをARToolKitと合わせるために変なことしてます。 */
		for (int i = 0;; i++) {
			// <arGetTransMat3>
			err = modifyMatrix(io_rotmat, io_transvec, offset_square, fit_vertex);
			i_calculator.calculateTransfer(io_rotmat, io_transvec);
			err = modifyMatrix(io_rotmat, io_transvec, offset_square, fit_vertex);
			// //</arGetTransMat3>
			if (err < AR_GET_TRANS_MAT_MAX_FIT_ERROR || i == AR_GET_TRANS_MAT_MAX_LOOP_COUNT - 1) {
				break;
			}
			i_calculator.calculateTransfer(io_rotmat, io_transvec);
		}
		return err;
	}
	private double[] __createRotationMap_b_map=new double[6];
	private double[] __createRotationMap_c_map=new double[6];
	private double[] __createRotationMap_f=new double[3];
	private void createRotationMap(NyARDoublePoint3d i_angle,double i_factor,NyARDoubleMatrix33[] i_rot_matrix)
	{
		double sina,cosa,sinb,cosb,sinc,cosc;
		double CACA,SASA,SACA,SASB,CASB,SACACB,CACACB,SASACB;

		
		final double[] f=this.__createRotationMap_f;
		final double[] b_map=this.__createRotationMap_b_map;
		final double[] c_map=this.__createRotationMap_c_map;
		f[0]=-i_factor;
		f[1]=0;
		f[2]=i_factor;
		double ang1,ang2;
		//BとCのsinマップを先に作成
		for(int i=0;i<3;i++)
		{
			ang1=i_angle.y + f[i];
			b_map[i]  =Math.sin(ang1);
			b_map[i+3]=Math.cos(ang1);
			ang2=i_angle.z + f[i];
			c_map[i]  =Math.sin(ang2);
			c_map[i+3]=Math.cos(ang2);
		}
		int idx=0;
		int t1,t2,t3;
		for (t1 = 0; t1 < 3; t1++){
			ang1=i_angle.x + f[t1];
			sina = Math.sin(ang1);
			cosa = Math.cos(ang1);			
			CACA = cosa * cosa;
			SASA = sina * sina;
			SACA = sina * cosa;

			for (t2=0;t2<3;t2++){
				sinb = b_map[t2];
				cosb = b_map[t2+3];
				SASB = sina * sinb;
				CASB = cosa * sinb;
				SACACB = SACA * cosb;
                CACACB = CACA * cosb;
                SASACB = SASA * cosb;		
				for (t3=0;t3<3;t3++) {
					sinc = c_map[t3];
					cosc = c_map[t3+3];
					final NyARDoubleMatrix33 mat_ptr=i_rot_matrix[idx];
					mat_ptr.m00 = CACACB * cosc + SASA * cosc + SACACB * sinc - SACA * sinc;
					mat_ptr.m01 = -CACACB * sinc - SASA * sinc + SACACB * cosc - SACA * cosc;
					mat_ptr.m02 = CASB;
					mat_ptr.m10 = SACACB * cosc - SACA * cosc + SASACB * sinc + CACA * sinc;
					mat_ptr.m11 = -SACACB * sinc + SACA * sinc + SASACB * cosc + CACA * cosc;
					mat_ptr.m12 = SASB;
					mat_ptr.m20 = -CASB * cosc - SASB * sinc;
					mat_ptr.m21 = CASB * sinc - SASB * cosc;
					mat_ptr.m22 = cosb;
					idx++;
				}
			}
		}
		return;
	}
	private final void getNewMatrix(NyARDoubleMatrix33 i_rot, NyARDoublePoint3d i_trans, NyARDoubleMatrix34 o_combo)
	{
		double cp0,cp1,cp2,cp3;
		NyARPerspectiveProjectionMatrix cp=this._projection_mat_ref;

		cp3=cp.m03;
		cp0=cp.m00;cp1=cp.m01;cp2=cp.m02;
		o_combo.m00=cp0 * i_rot.m00 + cp1 * i_rot.m10 + cp2 * i_rot.m20;
		o_combo.m01=cp0 * i_rot.m01 + cp1 * i_rot.m11 + cp2 * i_rot.m21;
		o_combo.m02=cp0 * i_rot.m02 + cp1 * i_rot.m12 + cp2 * i_rot.m22;
		o_combo.m03=cp0 * i_trans.x + cp1 * i_trans.y + cp2 * i_trans.z +cp3;

		cp0=cp.m10;cp1=cp.m11;cp2=cp.m12;
		o_combo.m10=cp0 * i_rot.m00 + cp1 * i_rot.m10 + cp2 * i_rot.m20;
		o_combo.m11=cp0 * i_rot.m01 + cp1 * i_rot.m11 + cp2 * i_rot.m21;
		o_combo.m12=cp0 * i_rot.m02 + cp1 * i_rot.m12 + cp2 * i_rot.m22;
		o_combo.m13=cp0 * i_trans.x + cp1 * i_trans.y + cp2 * i_trans.z +cp3;

		cp0=cp.m20;cp1=cp.m21;cp2=cp.m22;
		o_combo.m20=cp0 * i_rot.m00 + cp1 * i_rot.m10 + cp2 * i_rot.m20;
		o_combo.m21=cp0 * i_rot.m01 + cp1 * i_rot.m11 + cp2 * i_rot.m21;
		o_combo.m22=cp0 * i_rot.m02 + cp1 * i_rot.m12 + cp2 * i_rot.m22;
		o_combo.m23=cp0 * i_trans.x + cp1 * i_trans.y + cp2 * i_trans.z +cp3;
		return;
	}
	private final NyARDoublePoint3d __modifyMatrix_angle = new NyARDoublePoint3d();
	private final NyARDoubleMatrix34 __modifyMatrix_combo=new NyARDoubleMatrix34();
	private final NyARDoubleMatrix33[] __modifyMatrix_next_rot_matrix=NyARDoubleMatrix33.createArray(27); 
	public final double modifyMatrix(NyARRotMatrix io_rot, NyARDoublePoint3d trans, NyARDoublePoint3d[] i_vertex3d, NyARDoublePoint2d[] i_vertex2d) throws NyARException
	{
		final NyARDoublePoint3d angle = this.__modifyMatrix_angle;
		final NyARDoubleMatrix34 combo=this.__modifyMatrix_combo;
		final NyARDoubleMatrix33[] next_rot_matrix=this.__modifyMatrix_next_rot_matrix;
		double factor;
		double hx, hy, h, x, y;
		double err, minerr = 0;
		int i,i2;
		int best_idx=0;
		angle.setValue(io_rot.refAngle());// arGetAngle( rot, &a, &b, &c );
		factor = 10.0 * Math.PI / 180.0;
		for (int j = 0; j < 10; j++){
			minerr = 1000000000.0;
			//評価用の角度マップ作成
			createRotationMap(angle,factor,next_rot_matrix);
			//評価して一番宜しいIDを保存
			best_idx=(1+1*3+1*9);
			for(i2=0;i2<27;i2++){
				this.getNewMatrix(next_rot_matrix[i2],trans,combo);
				err = 0.0;
				for (i = 0; i < 4; i++) {
					hx =  combo.m00 * i_vertex3d[i].x + combo.m01 * i_vertex3d[i].y + combo.m02 * i_vertex3d[i].z + combo.m03;
					hy = combo.m10 * i_vertex3d[i].x + combo.m11 *i_vertex3d[i].y + combo.m12 * i_vertex3d[i].z + combo.m13;
					h = combo.m20 * i_vertex3d[i].x + combo.m21 * i_vertex3d[i].y + combo.m22 * i_vertex3d[i].z + combo.m23;
					x = i_vertex2d[i].x-(hx / h);
					y = i_vertex2d[i].y-(hy / h);
					err += x*x+y*y;
				}
				if (err < minerr){
					minerr = err;
					best_idx=i2;
				}
				
			}
			if (best_idx==(1+1*3+1*9)){
				factor *= 0.5;			
			}else{
				angle.z+=factor*(best_idx%3-1);
				angle.y+=factor*((best_idx/3)%3-1);
				angle.x+=factor*((best_idx/9)%3-1);				
			}
		}
		io_rot.setAngle(angle.x,angle.y,angle.z);
		/* printf("factor = %10.5f\n", factor*180.0/MD_PI); */
		return minerr / 4;
	}




}
