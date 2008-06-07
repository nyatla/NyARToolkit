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
package jp.nyatla.nyartoolkit.core;



import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.util.DoubleValue;



/**
 * This class calculates ARMatrix from square information and holds it.
 * --
 * 変換行列を計算して、結果を保持するクラス。
 *
 */
public class NyARTransMat_O2 implements NyARTransMat{
    private final static int AR_FITTING_TO_IDEAL=0;//#define  AR_FITTING_TO_IDEAL          0
    private final static int AR_FITTING_TO_INPUT=1;//#define  AR_FITTING_TO_INPUT          1
    private final static int	arFittingMode	=AR_FITTING_TO_INPUT;

    private final static int AR_GET_TRANS_MAT_MAX_LOOP_COUNT=5;//#define   AR_GET_TRANS_MAT_MAX_LOOP_COUNT         5
    private final static double AR_GET_TRANS_MAT_MAX_FIT_ERROR=1.0;//#define   AR_GET_TRANS_MAT_MAX_FIT_ERROR          1.0
    private final static int P_MAX=10;//頂点の数(4で十分だけどなんとなく10)//#define P_MAX       500
    private final static int NUMBER_OF_VERTEX=4;//処理対象の頂点数
    private final NyARTransRot transrot;
    private final double[] center={0.0,0.0};
    private final NyARParam param;
    private final NyARMat result_mat=new NyARMat(3,4);
    public NyARTransMat_O2(NyARParam i_param)throws NyARException
    {
	param=i_param;
	transrot=new NyARTransRot_O3(i_param,NUMBER_OF_VERTEX);

    }
    public void setCenter(double i_x,double i_y)
    {
	center[0]=i_x;
	center[1]=i_x;
    }
    public NyARMat getTransformationMatrix()
    {
	return result_mat;
    }

    private final double[][] wk_transMat_pos3d=new double[P_MAX][3];//pos3d[P_MAX][3];
    private final double[][] wk_transMat_ppos2d=new double[4][2];
    private final double[][] wk_transMat_ppos3d=new double[4][2];
    private final double[] wk_transMat_off=new double[3];
    private final double[][] wk_transMat_pos2d=new double[P_MAX][2];//pos2d[P_MAX][2];


    private final DoubleValue wk_arGetTransMatSub_a1=new DoubleValue();
    private final DoubleValue wk_arGetTransMatSub_a2=new DoubleValue();
    private final NyARMat wk_transMat_mat_a=new NyARMat(NUMBER_OF_VERTEX*2,3);
    private final NyARMat wk_transMat_mat_b=new NyARMat(3,NUMBER_OF_VERTEX*2);
    private final NyARMat wk_transMat_mat_d=new NyARMat( 3, 3 );    
    private final double[] wk_transMat_mat_trans=new double[3];

    /**
     * double arGetTransMat( ARMarkerInfo *marker_info,double center[2], double width, double conv[3][4] )
     * 演算シーケンス最適化のため、arGetTransMat3等の関数フラグメントを含みます。
     * 保持している変換行列を更新する。
     * @param square
     * 計算対象のNyARSquareオブジェクト
     * @param i_direction
     * @param width
     * @return
     * @throws NyARException
     */
    public double transMat( NyARSquare square,int i_direction, double width)throws NyARException
    {
	double[][]  ppos2d=wk_transMat_ppos2d;
	double[][]  ppos3d=wk_transMat_ppos3d;
	double[] off=wk_transMat_off;
	double[][] pos3d=wk_transMat_pos3d;


	ppos2d[0][0] = square.sqvertex[(4-i_direction)%4][0];
	ppos2d[0][1] = square.sqvertex[(4-i_direction)%4][1];
	ppos2d[1][0] = square.sqvertex[(5-i_direction)%4][0];
	ppos2d[1][1] = square.sqvertex[(5-i_direction)%4][1];
	ppos2d[2][0] = square.sqvertex[(6-i_direction)%4][0];
	ppos2d[2][1] = square.sqvertex[(6-i_direction)%4][1];
	ppos2d[3][0] = square.sqvertex[(7-i_direction)%4][0];
	ppos2d[3][1] = square.sqvertex[(7-i_direction)%4][1];
	ppos3d[0][0] = center[0] - width/2.0;
	ppos3d[0][1] = center[1] + width/2.0;
	ppos3d[1][0] = center[0] + width/2.0;
	ppos3d[1][1] = center[1] + width/2.0;
	ppos3d[2][0] = center[0] + width/2.0;
	ppos3d[2][1] = center[1] - width/2.0;
	ppos3d[3][0] = center[0] - width/2.0;
	ppos3d[3][1] = center[1] - width/2.0;

	transrot.initRot(square,i_direction);

	//arGetTransMat3の前段処理(pos3dとoffを初期化)
	arGetTransMat3_initPos3d(ppos3d,pos3d,off);


	//arGetTransMatSubにあった処理。毎回おなじっぽい。pos2dに変換座標を格納する。
	double[][] pos2d=this.wk_transMat_pos2d;
	DoubleValue a1=this.wk_arGetTransMatSub_a1;
	DoubleValue a2=this.wk_arGetTransMatSub_a2;
	if(arFittingMode == AR_FITTING_TO_INPUT ){
	    for(int i = 0; i < NUMBER_OF_VERTEX; i++ ) {
		param.ideal2Observ(ppos2d[i][0], ppos2d[i][1],a1,a2);//arParamIdeal2Observ(dist_factor, ppos2d[i][0], ppos2d[i][1],&pos2d[i][0], &pos2d[i][1]);
		pos2d[i][0]=a1.value;
		pos2d[i][1]=a2.value;
	    }
	}else{
	    for(int i = 0; i < NUMBER_OF_VERTEX; i++ ){
		pos2d[i][0] = ppos2d[i][0];
		pos2d[i][1] = ppos2d[i][1];
	    }
	}

	//変換マトリクスdとbの準備(arGetTransMatSubの一部)
	final double cpara[]=param.get34Array();
	final NyARMat mat_a =this.wk_transMat_mat_a;
	final double[][] a_array=mat_a.getArray();

	final NyARMat mat_b =this.wk_transMat_mat_b;
	final double[][] b_array=mat_b.getArray();

	int x2;
	for(int i = 0; i < NUMBER_OF_VERTEX; i++ ) {
	    x2=i*2;
	    //</Optimize>
	    a_array[x2  ][0]=b_array[0][x2]=cpara[0*4+0];//mat_a->m[j*6+0] = mat_b->m[num*0+j*2] = cpara[0][0];
	    a_array[x2  ][1]=b_array[1][x2]=cpara[0*4+1];//mat_a->m[j*6+1] = mat_b->m[num*2+j*2] = cpara[0][1];
	    a_array[x2  ][2]=b_array[2][x2]=cpara[0*4+2]-pos2d[i][0];//mat_a->m[j*6+2] = mat_b->m[num*4+j*2] = cpara[0][2] - pos2d[j][0];
	    a_array[x2+1][0]=b_array[0][x2+1]=0.0;//mat_a->m[j*6+3] = mat_b->m[num*0+j*2+1] = 0.0;
	    a_array[x2+1][1]=b_array[1][x2+1]=cpara[1*4+1];//mat_a->m[j*6+4] = mat_b->m[num*2+j*2+1] = cpara[1][1];
	    a_array[x2+1][2]=b_array[2][x2+1]=cpara[1*4+2] - pos2d[i][1];//mat_a->m[j*6+5] = mat_b->m[num*4+j*2+1] = cpara[1][2] - pos2d[j][1];
	}
	final NyARMat mat_d =this.wk_transMat_mat_d;
	mat_d.matrixMul(mat_b,mat_a);
	mat_d.matrixSelfInv();

	double  err=-1;
	double[] rot=transrot.getArray();
	double[][] conv=result_mat.getArray();
	double[] trans=this.wk_transMat_mat_trans;
	for(int i=0;i<AR_GET_TRANS_MAT_MAX_LOOP_COUNT; i++ ){
	    //<arGetTransMat3>
	    err = arGetTransMatSub(pos2d, pos3d,mat_b,mat_d,trans);
	    conv[0][0] = rot[0*3+0];
	    conv[0][1] = rot[0*3+1];
	    conv[0][2] = rot[0*3+2];
	    conv[1][0] = rot[1*3+0];
	    conv[1][1] = rot[1*3+1];
	    conv[1][2] = rot[1*3+2];
	    conv[2][0] = rot[2*3+0];
	    conv[2][1] = rot[2*3+1];
	    conv[2][2] = rot[2*3+2];
	    conv[0][3] = rot[0*3+0]*off[0] + rot[0*3+1]*off[1] + rot[0*3+2]*off[2] + trans[0];
	    conv[1][3] = rot[1*3+0]*off[0] + rot[1*3+1]*off[1] + rot[1*3+2]*off[2] + trans[1];
	    conv[2][3] = rot[2*3+0]*off[0] + rot[2*3+1]*off[1] + rot[2*3+2]*off[2] + trans[2];

	    //</arGetTransMat3>
	    if( err < AR_GET_TRANS_MAT_MAX_FIT_ERROR ){
		break;
	    }
	}
	return err;
    }
    private final double[] wk_arGetTransMat3_initPos3d_pmax=new double[3];
    private final double[] wk_arGetTransMat3_initPos3d_pmin=new double[3];
    /**
     * arGetTransMat3関数の前処理部分。i_ppos3dから、o_pos3dとoffを計算する。
     * 計算結果から再帰的に変更される可能性が無いので、切り離し。
     * @param i_ppos3d
     * 入力配列[num][3]
     * @param o_pos3d
     * 出力配列[P_MAX][3]
     * @param o_off
     * [3]
     * @throws NyARException
     */
    private final void arGetTransMat3_initPos3d(double i_ppos3d[][],double[][] o_pos3d,double[] o_off)throws NyARException
    {
	final double[] pmax=wk_arGetTransMat3_initPos3d_pmax;//new double[3];
	final double[] pmin=wk_arGetTransMat3_initPos3d_pmin;//new double[3];//double  off[3], pmax[3], pmin[3];
	int i;
	pmax[0]=pmax[1]=pmax[2] = -10000000000.0;
	pmin[0]=pmin[1]=pmin[2] =  10000000000.0;
	for(i = 0; i < NUMBER_OF_VERTEX; i++ ) {
	    if( i_ppos3d[i][0] > pmax[0] ){
		pmax[0] = i_ppos3d[i][0];
	    }
	    if( i_ppos3d[i][0] < pmin[0] ){
		pmin[0] = i_ppos3d[i][0];
	    }
	    if( i_ppos3d[i][1] > pmax[1] ){
		pmax[1] = i_ppos3d[i][1];
	    }
	    if( i_ppos3d[i][1] < pmin[1] ){
		pmin[1] = i_ppos3d[i][1];
	    }
	}
	o_off[0] = -(pmax[0] + pmin[0]) / 2.0;
	o_off[1] = -(pmax[1] + pmin[1]) / 2.0;
	o_off[2] = -(pmax[2] + pmin[2]) / 2.0;


	double[] o_pos3d_pt;
	double[] i_pos_pd_pt;	
	for(i = 0; i < NUMBER_OF_VERTEX; i++ ) {
	    o_pos3d_pt =o_pos3d[i];
	    i_pos_pd_pt=i_ppos3d[i];
	    o_pos3d_pt[0] = i_pos_pd_pt[0] + o_off[0];
	    o_pos3d_pt[1] = i_pos_pd_pt[1] + o_off[1];
	    o_pos3d_pt[2] = 0.0;
	}
    }    

    private final NyARMat wk_arGetTransMatSub_mat_c=new NyARMat(NUMBER_OF_VERTEX*2,1);
    private final NyARMat wk_arGetTransMatSub_mat_e=new NyARMat( 3, 1 );
    private final NyARMat wk_arGetTransMatSub_mat_f=new NyARMat( 3, 1 );

    /**
     * static double arGetTransMatSub( double rot[3][3], double ppos2d[][2],double pos3d[][3], int num, double conv[3][4],double *dist_factor, double cpara[3][4] )
     * Optimize:2008.04.20:STEP[1033→1004]
     * @param i_ppos2d
     * @param i_pos3d
     * @param i_mat_b
     * 演算用行列b
     * @param i_mat_d
     * 演算用行列d
     * @return
     * @throws NyARException
     */
    private final double arGetTransMatSub(double i_ppos2d[][],double i_pos3d[][],NyARMat i_mat_b,NyARMat i_mat_d,double[] o_trans) throws NyARException
    {
	double cpara[]=param.get34Array();
	NyARMat mat_c,mat_e,mat_f;//ARMat   *mat_a, *mat_b, *mat_c, *mat_d, *mat_e, *mat_f;

	double  wx, wy, wz;
	double  ret;
	int     i;

	mat_c =this.wk_arGetTransMatSub_mat_c;//次処理で値をもらうので、初期化の必要は無い。
	double[][] c_array=mat_c.getArray();
	double[] rot=transrot.getArray();
	double[] i_pos3d_pt;
	int x2;
	for( i = 0; i < NUMBER_OF_VERTEX; i++ ) {
	    x2=i*2;
	    i_pos3d_pt=i_pos3d[i];
	    wx = rot[0] * i_pos3d_pt[0]+ rot[1] * i_pos3d_pt[1]+ rot[2] * i_pos3d_pt[2];
	    wy = rot[3] * i_pos3d_pt[0]+ rot[4] * i_pos3d_pt[1]+ rot[5] * i_pos3d_pt[2];
	    wz = rot[6] * i_pos3d_pt[0]+ rot[7] * i_pos3d_pt[1]+ rot[8] * i_pos3d_pt[2];
	    c_array[x2][0]  =wz * i_ppos2d[i][0]- cpara[0*4+0]*wx - cpara[0*4+1]*wy - cpara[0*4+2]*wz;//mat_c->m[j*2+0] = wz * pos2d[j][0]- cpara[0][0]*wx - cpara[0][1]*wy - cpara[0][2]*wz;
	    c_array[x2+1][0]=wz * i_ppos2d[i][1]- cpara[1*4+1]*wy - cpara[1*4+2]*wz;//mat_c->m[j*2+1] = wz * pos2d[j][1]- cpara[1][1]*wy - cpara[1][2]*wz;
	}
	mat_e = this.wk_arGetTransMatSub_mat_e;//次処理で値をもらうので、初期化の必要は無い。
	mat_f = this.wk_arGetTransMatSub_mat_f;//次処理で値をもらうので、初期化の必要は無い。
	double[][] f_array=mat_f.getArray();

	mat_e.matrixMul(i_mat_b, mat_c );
	mat_f.matrixMul(i_mat_d, mat_e );

//	double[] trans=wk_arGetTransMatSub_trans;//double  trans[3];	
	o_trans[0] = f_array[0][0];//trans[0] = mat_f->m[0];
	o_trans[1] = f_array[1][0];
	o_trans[2] = f_array[2][0];//trans[2] = mat_f->m[2];
	ret =transrot.modifyMatrix(o_trans, i_pos3d, i_ppos2d);
	for( i = 0; i < NUMBER_OF_VERTEX; i++ ) {
	    x2=i*2;
	    i_pos3d_pt=i_pos3d[i];
	    wx = rot[0] * i_pos3d_pt[0]+ rot[1] * i_pos3d_pt[1]+ rot[2] * i_pos3d_pt[2];
	    wy = rot[3] * i_pos3d_pt[0]+ rot[4] * i_pos3d_pt[1]+ rot[5] * i_pos3d_pt[2];
	    wz = rot[6] * i_pos3d_pt[0]+ rot[7] * i_pos3d_pt[1]+ rot[8] * i_pos3d_pt[2];
	    c_array[x2][0]  =wz * i_ppos2d[i][0]- cpara[0*4+0]*wx - cpara[0*4+1]*wy - cpara[0*4+2]*wz;//mat_c->m[j*2+0] = wz * pos2d[j][0]- cpara[0][0]*wx - cpara[0][1]*wy - cpara[0][2]*wz;
	    c_array[x2+1][0]=wz * i_ppos2d[i][1]- cpara[1*4+1]*wy - cpara[1*4+2]*wz;//mat_c->m[j*2+1] = wz * pos2d[j][1]- cpara[1][1]*wy - cpara[1][2]*wz;
	}

	mat_e.matrixMul(i_mat_b, mat_c );
	mat_f.matrixMul(i_mat_d, mat_e );
	o_trans[0] = f_array[0][0];//trans[0] = mat_f->m[0];
	o_trans[1] = f_array[1][0];
	o_trans[2] = f_array[2][0];//trans[2] = mat_f->m[2];
	ret = transrot.modifyMatrix(o_trans, i_pos3d, i_ppos2d);

//	double[][] conv=result_mat.getArray();
//	for( i = 2; i >=0; i-- ) {//<Optimize/>for( j = 0; j < 3; j++ ) {
//	//<Optimize>
//	//for( i = 0; i < 3; i++ ){
//	//	conv[j][i] = rot[j][i];
//	//}
//	conv[i][0] = rot[i*3+0];
//	conv[i][1] = rot[i*3+1];
//	conv[i][2] = rot[i*3+2];
//	//</Optimize>
//	conv[i][3] = trans[i];
//	}
	return ret;
    }
}
