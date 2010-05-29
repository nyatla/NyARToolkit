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
package jp.nyatla.nyartoolkit.sandbox.x2;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.types.NyARFixedFloat16Point2d;
import jp.nyatla.nyartoolkit.core.types.NyARFixedFloat16Point3d;

/**
 * 基本姿勢と実画像を一致するように、角度を微調整→平行移動量を再計算
 * を繰り返して、変換行列を最適化する。
 *
 */
public class NyARFixedFloatRotTransOptimize_O2
{
	private final static int AR_GET_TRANS_MAT_MAX_LOOP_COUNT = 1;// #define AR_GET_TRANS_MAT_MAX_LOOP_COUNT 5
	private final NyARPerspectiveProjectionMatrix _projection_mat_ref;
	public NyARFixedFloatRotTransOptimize_O2(NyARPerspectiveProjectionMatrix i_projection_mat_ref)
	{
		this._projection_mat_ref=i_projection_mat_ref;
		return;
	}
	
	final public double optimize(NyARFixedFloatRotMatrix io_rotmat,NyARFixedFloat16Point3d io_transvec,NyARFixedFloatFitVecCalculator i_calculator) throws NyARException
	{
		final NyARFixedFloat16Point2d[] fit_vertex=i_calculator.getFitSquare();
		final NyARFixedFloat16Point3d[] offset_square=i_calculator.getOffsetVertex().vertex;
		
		double err = -1;
		err = modifyMatrix(io_rotmat,io_transvec,offset_square,fit_vertex);
		/*ループを抜けるタイミングをARToolKitと合わせるために変なことしてます。*/
        for (int i = 0; ; i++)
        {
            // <arGetTransMat3>
            err = modifyMatrix(io_rotmat, io_transvec, offset_square, fit_vertex);
            i_calculator.calculateTransfer(io_rotmat, io_transvec);
            err = modifyMatrix(io_rotmat, io_transvec, offset_square, fit_vertex);
            // //</arGetTransMat3>
            if (err < 1.0 || i == AR_GET_TRANS_MAT_MAX_LOOP_COUNT - 1)
            {
                break;
            }
            i_calculator.calculateTransfer(io_rotmat, io_transvec);
        }
        return err;
	}
	
	private final long[][] __modifyMatrix_double1D = new long[8][3];
	private final long INITIAL_FACTOR=(long)(0x10000*5.0 * Math.PI / 180.0);
	/**
	 * arGetRot計算を階層化したModifyMatrix 896
	 * 
	 * @param nyrot
	 * @param trans
	 * @param i_vertex3d
	 * [m][3]
	 * @param i_vertex2d
	 * [n][2]
	 * @return
	 * @throws NyARException
	 */
	private double modifyMatrix(NyARFixedFloatRotMatrix io_rot,NyARFixedFloat16Point3d trans, NyARFixedFloat16Point3d[] i_vertex3d, NyARFixedFloat16Point2d[] i_vertex2d) throws NyARException
	{
		long a2, b2, c2;
		long h, x, y;
		long err, minerr = 0;
		int t1, t2, t3;
		int best_idx=0;

		long factor = INITIAL_FACTOR;
		long rot0, rot1, rot2;
		long combo00, combo01, combo02, combo03, combo10, combo11, combo12, combo13, combo20, combo21, combo22, combo23;
		long combo02_2, combo02_5, combo02_8, combo02_11;
		long combo22_2, combo22_5, combo22_8, combo22_11;
		long combo12_2, combo12_5, combo12_8, combo12_11;
		// vertex展開
		final long VX00, VX01, VX02, VX10, VX11, VX12, VX20, VX21, VX22, VX30, VX31, VX32;
		VX00 = i_vertex3d[0].x;
		VX01 = i_vertex3d[0].y;
		VX02 = i_vertex3d[0].z;
		VX10 = i_vertex3d[1].x;
		VX11 = i_vertex3d[1].y;
		VX12 = i_vertex3d[1].z;
		VX20 = i_vertex3d[2].x;
		VX21 = i_vertex3d[2].y;
		VX22 = i_vertex3d[2].z;
		VX30 = i_vertex3d[3].x;
		VX31 = i_vertex3d[3].y;
		VX32 = i_vertex3d[3].z;
		final long P2D00, P2D01, P2D10, P2D11, P2D20, P2D21, P2D30, P2D31;
		P2D00 = i_vertex2d[0].x;
		P2D01 = i_vertex2d[0].y;
		P2D10 = i_vertex2d[1].x;
		P2D11 = i_vertex2d[1].y;
		P2D20 = i_vertex2d[2].x;
		P2D21 = i_vertex2d[2].y;
		P2D30 = i_vertex2d[3].x;
		P2D31 = i_vertex2d[3].y;
		final NyARPerspectiveProjectionMatrix prjmat = this._projection_mat_ref;
		final long CP0,CP1,CP2,CP3,CP4,CP5,CP6,CP7,CP8,CP9,CP10;
		CP0 = (long)(prjmat.m00*0x10000L);
		CP1 = (long)(prjmat.m01*0x10000L);
		CP2 = (long)(prjmat.m02*0x10000L);
		CP4 = (long)(prjmat.m10*0x10000L);
		CP5 = (long)(prjmat.m11*0x10000L);
		CP6 = (long)(prjmat.m12*0x10000L);
		CP8 = (long)(prjmat.m20*0x10000L);
		CP9 = (long)(prjmat.m21*0x10000L);
		CP10 =(long)(prjmat.m22*0x10000L);

		combo03 = ((CP0 * trans.x + CP1 * trans.y + CP2 * trans.z)>>16) + (long)(prjmat.m03*0x10000L);
		combo13 = ((CP4 * trans.x + CP5 * trans.y + CP6 * trans.z)>>16) + (long)(prjmat.m13*0x10000L);
		combo23 = ((CP8 * trans.x + CP9 * trans.y + CP10 * trans.z)>>16) + (long)(prjmat.m23*0x10000L);
		long CACA, SASA, SACA, CA, SA;
		long CACACB, SACACB, SASACB, CASB, SASB;
		long SACASC, SACACBSC, SACACBCC, SACACC;
		final long[][] double1D = this.__modifyMatrix_double1D;

		final long[] a_factor = double1D[1];
		final long[] sinb = double1D[2];
		final long[] cosb = double1D[3];
		final long[] b_factor = double1D[4];
		final long[] sinc = double1D[5];
		final long[] cosc = double1D[6];
		final long[] c_factor = double1D[7];
		long w, w2;
		long wsin, wcos;
		
		final NyARFixedFloat16Point3d angle=io_rot.refAngle();// arGetAngle( rot, &a, &b, &c );
		a2 =angle.x;
		b2 =angle.y;
		c2 =angle.z;

		// comboの3行目を先に計算
		for (int i = 0; i < 10; i++) {
			minerr = 0x4000000000000000L;
			// sin-cosテーブルを計算(これが外に出せるとは…。)
			for (int j = 0; j < 3; j++) {
				w2 = factor * (j - 1);//S16
				w = a2 + w2;//S16
				a_factor[j] = w;//S16
				w = b2 + w2;//S16
				b_factor[j] = w;//S16
				sinb[j] = NyMath.sinFixedFloat24((int)w);
				cosb[j] = NyMath.cosFixedFloat24((int)w);
				w = c2 + w2;//S16
				c_factor[j] = w;//S16
				sinc[j] = NyMath.sinFixedFloat24((int)w);
				cosc[j] = NyMath.cosFixedFloat24((int)w);
			}
			//
			for (t1 = 0; t1 < 3; t1++) {
				SA = NyMath.sinFixedFloat24((int)a_factor[t1]);
				CA = NyMath.cosFixedFloat24((int)a_factor[t1]);
				// Optimize
				CACA = (CA * CA)>>24;//S24
				SASA = (SA * SA)>>24;//S24
				SACA = (SA * CA)>>24;//S24
				for (t2 = 0; t2 < 3; t2++) {
					wsin = sinb[t2];//S24
					wcos = cosb[t2];//S24
					CACACB = (CACA * wcos)>>24;//S24
					SACACB = (SACA * wcos)>>24;//S24
					SASACB = (SASA * wcos)>>24;//S24
					CASB = (CA * wsin)>>24;//S24
					SASB = (SA * wsin)>>24;//S24

					// comboの計算1
					combo02 = (CP0 * CASB + CP1 * SASB + CP2 * wcos)>>24;//S24*S16>>24=S16
					combo12 = (CP4 * CASB + CP5 * SASB + CP6 * wcos)>>24;//S24*S16>>24=S16
					combo22 = (CP8 * CASB + CP9 * SASB + CP10 * wcos)>>24;//S24*S16>>24=S16

					combo02_2 = ((combo02 * VX02)>>16) + combo03;//S16
					combo02_5 = ((combo02 * VX12)>>16) + combo03;//S16
					combo02_8 = ((combo02 * VX22)>>16) + combo03;//S16
					combo02_11 = ((combo02 * VX32)>>16) + combo03;//S16
					combo12_2 = ((combo12 * VX02)>>16) + combo13;//S16
					combo12_5 = ((combo12 * VX12)>>16) + combo13;//S16
					combo12_8 = ((combo12 * VX22)>>16) + combo13;//S16
					combo12_11 = ((combo12 * VX32)>>16) + combo13;//S16
					combo22_2 = ((combo22 * VX02)>>16) + combo23;//S16
					combo22_5 = ((combo22 * VX12)>>16) + combo23;//S16
					combo22_8 = ((combo22 * VX22)>>16) + combo23;//S16
					combo22_11 = ((combo22 * VX32)>>16) + combo23;//S16
					for (t3 = 0; t3 < 3; t3++) {
						wsin = sinc[t3];//S24
						wcos = cosc[t3];//S24
						SACASC = (SACA * wsin)>>24;//S24
						SACACC = (SACA * wcos)>>24;//S24
						SACACBSC =(SACACB * wsin)>>24;//S24;
						SACACBCC = (SACACB * wcos)>>24;//S24;

						rot0 = ((CACACB * wcos + SASA * wcos)>>24) + SACACBSC - SACASC;//S24;
						rot1 = SACACBCC - SACACC + ((SASACB * wsin + CACA * wsin)>>24);//S24;
						rot2 = (-CASB * wcos - SASB * wsin)>>24;//S24;
						combo00 = (CP0 * rot0 + CP1 * rot1 + CP2 * rot2)>>24;//S16
						combo10 = (CP4 * rot0 + CP5 * rot1 + CP6 * rot2)>>24;//S16
						combo20 = (CP8 * rot0 + CP9 * rot1 + CP10 * rot2)>>24;//S16

						rot0 = ((-CACACB * wsin - SASA * wsin)>>24) + SACACBCC - SACACC;//S24
						rot1 = -SACACBSC + SACASC + ((SASACB * wcos + CACA * wcos)>>24);//S24
						rot2 = (CASB * wsin - SASB * wcos)>>24;//S24

						combo01 =(CP0 * rot0 + CP1 * rot1 + CP2 * rot2)>>24;//S16
						combo11 =(CP4 * rot0 + CP5 * rot1 + CP6 * rot2)>>24;//S16
						combo21 =(CP8 * rot0 + CP9 * rot1 + CP10 * rot2)>>24;//S16
						//
						err =0;
						h = ((combo20 * VX00 + combo21 * VX01)>>16) + combo22_2;//S16
						x = P2D00 - ((((combo00 * VX00 + combo01 * VX01)>>16) + combo02_2)<<16) / h;//S16
						y = P2D01 - ((((combo10 * VX00 + combo11 * VX01)>>16) + combo12_2)<<16) / h;//S16
						err += ((x * x + y * y)>>16);
						h = ((combo20 * VX10 + combo21 * VX11)>>16) + combo22_5;
						x = P2D10 - ((((combo00 * VX10 + combo01 * VX11)>>16) + combo02_5)<<16) / h;//S16
						y = P2D11 - ((((combo10 * VX10 + combo11 * VX11)>>16) + combo12_5)<<16) / h;//S16
						err += ((x * x + y * y)>>16);
						h = ((combo20 * VX20 + combo21 * VX21)>>16) + combo22_8;
						x = P2D20 - ((((combo00 * VX20 + combo01 * VX21)>>16) + combo02_8)<<16) / h;//S16
						y = P2D21 - ((((combo10 * VX20 + combo11 * VX21)>>16) + combo12_8)<<16) / h;//S16
						err += ((x * x + y * y)>>16);
						h = ((combo20 * VX30 + combo21 * VX31)>>16) + combo22_11;
						x = P2D30 - ((((combo00 * VX30 + combo01 * VX31)>>16) + combo02_11)<<16) / h;//S16
						y = P2D31 - ((((combo10 * VX30 + combo11 * VX31)>>16) + combo12_11)<<16) / h;//S16
						err += ((x * x + y * y)>>16);
						if (err < minerr) {
							minerr = err;
							a2 = a_factor[t1];
							b2 = b_factor[t2];
							c2 = c_factor[t3];
							best_idx=t1+t2*3+t3*9;
						}
					}
				}
			}
			if (best_idx==(1+3+9)) {
				factor=factor>>1;
			}
		}
		io_rot.setAngle((int)a2,(int)b2,(int)c2);
		/* printf("factor = %10.5f\n", factor*180.0/MD_PI); */
		return minerr /4;//この設定値おかしくね？16bitfixedfloatなら16で割らないと。
	}	
	
	
}
