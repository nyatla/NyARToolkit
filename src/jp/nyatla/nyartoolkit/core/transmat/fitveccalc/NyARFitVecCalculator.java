package jp.nyatla.nyartoolkit.core.transmat.fitveccalc;

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransOffset;
import jp.nyatla.nyartoolkit.core.transmat.rotmatrix.NyARRotMatrix;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.*;
/**
 * 平行移動量を計算するクラス
 *
 */
public class NyARFitVecCalculator
{
	private final NyARMat _mat_b = new NyARMat(3,8);//3,NUMBER_OF_VERTEX*2
	private final NyARMat _mat_a = new NyARMat(8,3);/*NUMBER_OF_VERTEX,3*/
	private final NyARMat _mat_d = new NyARMat(3,3);
	private final NyARParam _cparam;


//	private NyARDoublePoint2d[] _vertex_2d_ref;
	public NyARFitVecCalculator(final NyARParam i_param)
	{
		// 変換マトリクスdとbの準備(arGetTransMatSubの一部)
		final double cpara[] = i_param.get34Array();
		final double[][] a_array = this._mat_a.getArray();
		final double[][] b_array = this._mat_b.getArray();

		//変換用行列のcpara固定値の部分を先に初期化してしまう。
		for (int i = 0; i < 4; i++) {
			final int x2 = i * 2;
			a_array[x2][0] = b_array[0][x2] = cpara[0 * 4 + 0];// mat_a->m[j*6+0]=mat_b->m[num*0+j*2] =cpara[0][0];
			a_array[x2][1] = b_array[1][x2] = cpara[0 * 4 + 1];// mat_a->m[j*6+1]=mat_b->m[num*2+j*2]=cpara[0][1];
			//a_array[x2][2] = b_array[2][x2] = cpara[0 * 4 + 2] - o_marker_vertex_2d[i].x;// mat_a->m[j*6+2]=mat_b->m[num*4+j*2]=cpara[0][2]-pos2d[j][0];
			a_array[x2 + 1][0] = b_array[0][x2 + 1] = 0.0;// mat_a->m[j*6+3] =mat_b->m[num*0+j*2+1]= 0.0;
			a_array[x2 + 1][1] = b_array[1][x2 + 1] = cpara[1 * 4 + 1];// mat_a->m[j*6+4] =mat_b->m[num*2+j*2+1]= cpara[1][1];
			//a_array[x2 + 1][2] = b_array[2][x2 + 1] = cpara[1 * 4 + 2] - o_marker_vertex_2d[i].y;// mat_a->m[j*6+5]=mat_b->m[num*4+j*2+1]=cpara[1][2]-pos2d[j][1];
		}
		this._cparam=i_param;
		this._fitsquare_vertex=NyARDoublePoint2d.createArray(4);
		return;
	}
	private final NyARDoublePoint2d[] _fitsquare_vertex;
	private NyARTransOffset _offset_square;
	public void setOffsetSquare(NyARTransOffset i_offset)
	{
		this._offset_square=i_offset;
		return;
	}
	public NyARDoublePoint2d[] getFitSquare()
	{
		return this._fitsquare_vertex;
	}
	public NyARTransOffset getOffsetVertex()
	{
		return this._offset_square;
	}

	/**
	 * 適合させる矩形座標を指定します。
	 * @param i_square_vertex
	 * @throws NyARException
	 */
	public void setFittedSquare(NyARDoublePoint2d[] i_square_vertex) throws NyARException
	{
		final NyARDoublePoint2d[] vertex=_fitsquare_vertex;
//		int i;
//		if (arFittingMode == AR_FITTING_TO_INPUT) {
//			// arParamIdeal2Observをバッチ処理
		this._cparam.ideal2ObservBatch(i_square_vertex, vertex,4);
//		} else {
//			for (i = 0; i < NUMBER_OF_VERTEX; i++) {
//				o_marker_vertex_2d[i].x = i_square_vertex[i].x;
//				o_marker_vertex_2d[i].y = i_square_vertex[i].y;
//			}
//		}		
		
		
		
		final NyARMat mat_d=_mat_d;
		final NyARMat mat_a=this._mat_a;
		final NyARMat mat_b=this._mat_b;
		final double[] cparam_array=this._cparam.get34Array();
		final double[][] a_array = mat_a.getArray();
		final double[][] b_array = mat_b.getArray();
		final double cpara02=cparam_array[0*4+2];
		final double cpara12=cparam_array[1*4+2];
		for (int i = 0; i < 4; i++) {
			final int x2 = i * 2;	
			a_array[x2][2] = b_array[2][x2] = cpara02 - vertex[i].x;// mat_a->m[j*6+2]=mat_b->m[num*4+j*2]=cpara[0][2]-pos2d[j][0];
			a_array[x2 + 1][2] = b_array[2][x2 + 1] = cpara12 - vertex[i].y;// mat_a->m[j*6+5]=mat_b->m[num*4+j*2+1]=cpara[1][2]-pos2d[j][1];
		}
		// mat_d
		mat_d.matrixMul(mat_b, mat_a);
		mat_d.matrixSelfInv();		
		return;
	}
	private final NyARMat __calculateTransferVec_mat_c = new NyARMat(8, 1);//NUMBER_OF_VERTEX * 2, 1
	private final NyARMat _mat_e = new NyARMat(3, 1);
	private final NyARMat _mat_f = new NyARMat(3, 1);
	private final NyARDoublePoint3d[] __calculateTransfer_point3d=NyARDoublePoint3d.createArray(4);	
	
	/**
	 * 現在のオフセット矩形、適合先矩形と、回転行列から、平行移動量を計算します。
	 * @param i_rotation
	 * @param o_transfer
	 * @throws NyARException
	 */
	final public void calculateTransfer(NyARRotMatrix i_rotation,NyARDoublePoint3d o_transfer) throws NyARException
	{
		assert(this._offset_square!=null);
		final double[] cparam_array=this._cparam.get34Array();
		final double cpara00=cparam_array[0*4+0];
		final double cpara01=cparam_array[0*4+1];
		final double cpara02=cparam_array[0*4+2];
		final double cpara11=cparam_array[1*4+1];
		final double cpara12=cparam_array[1*4+2];
		
		final NyARDoublePoint3d[] point3d=this.__calculateTransfer_point3d;
		final NyARDoublePoint3d[] vertex3d=this._offset_square.vertex;		
		final NyARDoublePoint2d[] vertex2d=this._fitsquare_vertex;
		final NyARMat mat_c = this.__calculateTransferVec_mat_c;// 次処理で値をもらうので、初期化の必要は無い。
	
		final double[][] f_array = this._mat_f.getArray();
		final double[][] c_array = mat_c.getArray();
		
		
		//（3D座標？）を一括請求
		i_rotation.getPoint3dBatch(vertex3d,point3d,4);
		for (int i = 0; i < 4; i++) {
			final int x2 = i+i;
			final NyARDoublePoint3d point3d_ptr=point3d[i];
//			i_rotation.getPoint3d(vertex3d[i],point3d);
			//透視変換？
			c_array[x2][0] = point3d_ptr.z * vertex2d[i].x - cpara00 * point3d_ptr.x - cpara01 * point3d_ptr.y - cpara02 * point3d_ptr.z;// mat_c->m[j*2+0] = wz*pos2d[j][0]-cpara[0][0]*wx-cpara[0][1]*wy-cpara[0][2]*wz;
			c_array[x2 + 1][0] = point3d_ptr.z * vertex2d[i].y - cpara11 * point3d_ptr.y - cpara12 * point3d_ptr.z;// mat_c->m[j*2+1]= wz*pos2d[j][1]-cpara[1][1]*wy-cpara[1][2]*wz;
		}
		this._mat_e.matrixMul(this._mat_b, mat_c);
		this._mat_f.matrixMul(this._mat_d, this._mat_e);

		// double[] trans=wk_arGetTransMatSub_trans;//double trans[3];
		o_transfer.x= f_array[0][0];// trans[0] = mat_f->m[0];
		o_transfer.y= f_array[1][0];
		o_transfer.z= f_array[2][0];// trans[2] = mat_f->m[2];
		return;
	}
	
	
	
}
