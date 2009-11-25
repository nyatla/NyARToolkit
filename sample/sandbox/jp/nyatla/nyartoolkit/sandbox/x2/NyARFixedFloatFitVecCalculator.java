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

import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.types.NyARFixedFloat16Point2d;
import jp.nyatla.nyartoolkit.core.types.NyARFixedFloat16Point3d;
import jp.nyatla.nyartoolkit.core.*;

class NyARCustomMatrix extends NyARMat
{
	public NyARCustomMatrix(int i_row, int i_clm)
	{
		super(i_row,i_clm);
		return;
	}
	public void copyFrom(NyARFixedFloat16Mat i_mat) throws NyARException
	{
		long[][] ptr;
		int r,c;
		// For順変更禁止
		for (r = 0; r < this.row; r++){
			ptr=i_mat.getArray();
			for (c = 0; c < this.clm; c++){
				this._m[c][r]=(double)ptr[c][r]/0x10000;
			}
		}
		return;
	}
}
/**
 * 平行移動量を計算するクラス
 * 
 * NyARPerspectiveProjectionMatrixに直接アクセスしてる場所があるけど、
 * この辺の計算はNyARPerspectiveProjectionMatrixクラスの関数にして押し込む予定。
 *
 */
public class NyARFixedFloatFitVecCalculator
{
	private final NyARFixedFloat16Mat _mat_b = new NyARFixedFloat16Mat(3,8);//3,NUMBER_OF_VERTEX*2
	private final NyARFixedFloat16Mat _mat_a = new NyARFixedFloat16Mat(8,3);/*NUMBER_OF_VERTEX,3*/
	private final NyARFixedFloat16Mat _mat_d = new NyARFixedFloat16Mat(3,3);
	private final NyARCustomMatrix _mat_d2=new NyARCustomMatrix(3,3);
	private final NyARPerspectiveProjectionMatrix _projection_mat;
	private final NyARFixedFloatIdeal2Observ _distortionfactor;


//	private NyARDoublePoint2d[] _vertex_2d_ref;
	public NyARFixedFloatFitVecCalculator(final NyARPerspectiveProjectionMatrix i_projection_mat_ref,final NyARCameraDistortionFactor i_distortion_ref)
	{
		// 変換マトリクスdとbの準備(arGetTransMatSubの一部)
		final long[][] a_array = this._mat_a.getArray();
		final long[][] b_array = this._mat_b.getArray();

		//変換用行列のcpara固定値の部分を先に初期化してしまう。
		for (int i = 0; i < 4; i++) {
			final int x2 = i * 2;
			a_array[x2][0] = b_array[0][x2] =(long)(i_projection_mat_ref.m00*NyMath.FIXEDFLOAT16_1);// mat_a->m[j*6+0]=mat_b->m[num*0+j*2] =cpara[0][0];
			a_array[x2][1] = b_array[1][x2] =(long)(i_projection_mat_ref.m01*NyMath.FIXEDFLOAT16_1);// mat_a->m[j*6+1]=mat_b->m[num*2+j*2]=cpara[0][1];
			a_array[x2 + 1][0] = b_array[0][x2 + 1] =0;// mat_a->m[j*6+3] =mat_b->m[num*0+j*2+1]= 0.0;
			a_array[x2 + 1][1] = b_array[1][x2 + 1] =(long)(i_projection_mat_ref.m11*NyMath.FIXEDFLOAT16_1);// mat_a->m[j*6+4] =mat_b->m[num*2+j*2+1]= cpara[1][1];
		}
		this._projection_mat=i_projection_mat_ref;
		this._distortionfactor=new NyARFixedFloatIdeal2Observ(i_distortion_ref);
		return;
	}
	private final NyARFixedFloat16Point2d[] _fitsquare_vertex=NyARFixedFloat16Point2d.createArray(4);;
	private NyARFixedFloatTransOffset _offset_square;
	public void setOffsetSquare(NyARFixedFloatTransOffset i_offset)
	{
		this._offset_square=i_offset;
		return;
	}
	public NyARFixedFloat16Point2d[] getFitSquare()
	{
		return this._fitsquare_vertex;
	}
	public NyARFixedFloatTransOffset getOffsetVertex()
	{
		return this._offset_square;
	}

	/**
	 * 適合させる矩形座標を指定します。
	 * @param i_square_vertex
	 * @throws NyARException
	 */
	public void setFittedSquare(NyARFixedFloat16Point2d[] i_square_vertex) throws NyARException
	{
		final NyARFixedFloat16Point2d[] vertex=_fitsquare_vertex;
		for(int i=0;i<4;i++){
			this._distortionfactor.ideal2Observ(i_square_vertex[i], vertex[i]);
		}		
		
		final long cpara02=(long)(this._projection_mat.m02*NyMath.FIXEDFLOAT16_1);
		final long cpara12=(long)(this._projection_mat.m12*NyMath.FIXEDFLOAT16_1);		
		final NyARFixedFloat16Mat mat_d=_mat_d;
		final NyARFixedFloat16Mat mat_a=this._mat_a;
		final NyARFixedFloat16Mat mat_b=this._mat_b;
		final long[][] a_array = mat_a.getArray();
		final long[][] b_array = mat_b.getArray();
		for (int i = 0; i < 4; i++) {
			final int x2 = i * 2;	
			a_array[x2][2] = b_array[2][x2] = (long)((cpara02 - vertex[i].x));// mat_a->m[j*6+2]=mat_b->m[num*4+j*2]=cpara[0][2]-pos2d[j][0];
			a_array[x2 + 1][2] = b_array[2][x2 + 1] = (long)((cpara12 - vertex[i].y));// mat_a->m[j*6+5]=mat_b->m[num*4+j*2+1]=cpara[1][2]-pos2d[j][1];
		}
		// mat_d
		mat_d.matrixMul(mat_b, mat_a);
		this._mat_d2.copyFrom(mat_d);
		this._mat_d2.matrixSelfInv();		
		return;
	}
	private final NyARFixedFloat16Mat _mat_e = new NyARFixedFloat16Mat(3, 1);
	private final NyARFixedFloat16Mat __calculateTransferVec_mat_c = new NyARFixedFloat16Mat(8, 1);//NUMBER_OF_VERTEX * 2, 1
	private final NyARFixedFloat16Point3d[] __calculateTransfer_point3d=NyARFixedFloat16Point3d.createArray(4);	
	
	/**
	 * 現在のオフセット矩形、適合先矩形と、回転行列から、平行移動量を計算します。
	 * @param i_rotation
	 * @param o_transfer
	 * @throws NyARException
	 */
	final public void calculateTransfer(NyARFixedFloatRotMatrix i_rotation,NyARFixedFloat16Point3d o_transfer) throws NyARException
	{
		assert(this._offset_square!=null);
		final long cpara00=(long)(this._projection_mat.m00*NyMath.FIXEDFLOAT16_1);
		final long cpara01=(long)(this._projection_mat.m01*NyMath.FIXEDFLOAT16_1);
		final long cpara02=(long)(this._projection_mat.m02*NyMath.FIXEDFLOAT16_1);
		final long cpara11=(long)(this._projection_mat.m11*NyMath.FIXEDFLOAT16_1);
		final long cpara12=(long)(this._projection_mat.m12*NyMath.FIXEDFLOAT16_1);
		
		final NyARFixedFloat16Point3d[] point3d=this.__calculateTransfer_point3d;
		final NyARFixedFloat16Point3d[] vertex3d=this._offset_square.vertex;		
		final NyARFixedFloat16Point2d[] vertex2d=this._fitsquare_vertex;
		final NyARFixedFloat16Mat mat_c = this.__calculateTransferVec_mat_c;// 次処理で値をもらうので、初期化の必要は無い。
	
		final long[][] c_array = mat_c.getArray();
		
		
		//（3D座標？）を一括請求
		i_rotation.getPoint3dBatch(vertex3d,point3d,4);
		for (int i = 0; i < 4; i++) {
			final int x2 = i+i;
			final NyARFixedFloat16Point3d point3d_ptr=point3d[i];
			//透視変換？
			c_array[x2][0] =(long)((point3d_ptr.z * vertex2d[i].x - cpara00 * point3d_ptr.x - cpara01 * point3d_ptr.y - cpara02 * point3d_ptr.z)>>16);// mat_c->m[j*2+0] = wz*pos2d[j][0]-cpara[0][0]*wx-cpara[0][1]*wy-cpara[0][2]*wz;
			c_array[x2 + 1][0] =(long)((point3d_ptr.z * vertex2d[i].y - cpara11 * point3d_ptr.y - cpara12 * point3d_ptr.z)>>16);// mat_c->m[j*2+1]= wz*pos2d[j][1]-cpara[1][1]*wy-cpara[1][2]*wz;
		}
		this._mat_e.matrixMul(this._mat_b, mat_c);
		final double[][] d2=this._mat_d2.getArray();
		final long[][] e2=this._mat_e.getArray();
		
		
		//this._mat_f.matrixMul(this._mat_d, this._mat_e);

		// double[] trans=wk_arGetTransMatSub_trans;//double trans[3];
		o_transfer.x=(long)(d2[0][0]*e2[0][0]+d2[0][1]*e2[1][0]+d2[0][2]*e2[2][0]);
		o_transfer.y=(long)(d2[1][0]*e2[0][0]+d2[1][1]*e2[1][0]+d2[1][2]*e2[2][0]);
		o_transfer.z=(long)(d2[2][0]*e2[0][0]+d2[2][1]*e2[1][0]+d2[2][2]*e2[2][0]);
		return;
	}
	
	
	
}
