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
public class NyARTransMat_O2 implements NyARTransMat
{
    private final static int AR_FITTING_TO_IDEAL=0;//#define  AR_FITTING_TO_IDEAL          0
    private final static int AR_FITTING_TO_INPUT=1;//#define  AR_FITTING_TO_INPUT          1
    private final static int	arFittingMode	=AR_FITTING_TO_INPUT;

    private final static int AR_GET_TRANS_MAT_MAX_LOOP_COUNT=5;//#define   AR_GET_TRANS_MAT_MAX_LOOP_COUNT         5
    private final static double AR_GET_TRANS_MAT_MAX_FIT_ERROR=1.0;//#define   AR_GET_TRANS_MAT_MAX_FIT_ERROR          1.0
    private final static double AR_GET_TRANS_CONT_MAT_MAX_FIT_ERROR=1.0;
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

    /**
     * transMat関数の初期化関数を分離したものです。
     * @param square
     * @param i_direction
     * @param i_width
     * @param o_ppos2d
     * @param o_ppos3d
     */
    private final void init_transMat_ppos(NyARSquare square,int i_direction,double i_width,double[][] o_ppos2d,double[][] o_ppos3d)
    {	
	o_ppos2d[0][0] = square.sqvertex[(4-i_direction)%4][0];
	o_ppos2d[0][1] = square.sqvertex[(4-i_direction)%4][1];
	o_ppos2d[1][0] = square.sqvertex[(5-i_direction)%4][0];
	o_ppos2d[1][1] = square.sqvertex[(5-i_direction)%4][1];
	o_ppos2d[2][0] = square.sqvertex[(6-i_direction)%4][0];
	o_ppos2d[2][1] = square.sqvertex[(6-i_direction)%4][1];
	o_ppos2d[3][0] = square.sqvertex[(7-i_direction)%4][0];
	o_ppos2d[3][1] = square.sqvertex[(7-i_direction)%4][1];
	
	double c0,c1,w_2;
	c0=center[0];
	c1=center[1];
	w_2 =i_width/2.0;
	
	o_ppos3d[0][0] = c0 - w_2;//center[0] - w/2.0;
	o_ppos3d[0][1] = c1 + w_2;//center[1] + w/2.0;
	o_ppos3d[1][0] = c0 + w_2;//center[0] + w/2.0;
	o_ppos3d[1][1] = c1 + w_2;//center[1] + w/2.0;
	o_ppos3d[2][0] = c0 + w_2;//center[0] + w/2.0;
	o_ppos3d[2][1] = c1 - w_2;//center[1] - w/2.0;
	o_ppos3d[3][0] = c0 - w_2;//center[0] - w/2.0;
	o_ppos3d[3][1] = c1 - w_2;//center[1] - w/2.0;
	return;
    }
    
    private final double[][] wk_transMat_pos3d=new double[P_MAX][3];//pos3d[P_MAX][3];
    private final double[][] wk_transMat_ppos2d=new double[4][2];
    private final double[][] wk_transMat_ppos3d=new double[4][2];
    private final double[] wk_transMat_off=new double[3];
    private final double[][] wk_transMat_pos2d=new double[P_MAX][2];//pos2d[P_MAX][2];
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
    public double transMat(NyARSquare square,int i_direction, double width,NyARTransMatResult o_result_conv)throws NyARException
    {
	double[][]  ppos2d=wk_transMat_ppos2d;
	double[][]  ppos3d=wk_transMat_ppos3d;
	double[]    off   =wk_transMat_off;
	double[][]  pos3d =wk_transMat_pos3d;
	
	//rotationの初期化
	transrot.initRot(square,i_direction);

	//ppos2dとppos3dの初期化
	init_transMat_ppos(square,i_direction,width,ppos2d,ppos3d);
	
	//arGetTransMat3の前段処理(pos3dとoffを初期化)
	double[][] pos2d=this.wk_transMat_pos2d;
	final NyARMat mat_b =this.wk_transMat_mat_b;
	final NyARMat mat_d =this.wk_transMat_mat_d;

	arGetTransMat3_initTransMat(ppos3d,ppos2d,pos2d,pos3d,off,mat_b,mat_d);
	
	double  err=-1;
	double[] trans=this.wk_transMat_mat_trans;
	for(int i=0;i<AR_GET_TRANS_MAT_MAX_LOOP_COUNT; i++ ){
	    //<arGetTransMat3>
	    err = arGetTransMatSub(pos2d, pos3d,mat_b,mat_d,trans);
//	    //</arGetTransMat3>
	    if( err < AR_GET_TRANS_MAT_MAX_FIT_ERROR ){
		break;
	    }
	}
	//マトリクスの保存
	o_result_conv.updateMatrixValue(this.transrot,off,trans);
	return err;
    }
    private final NyARTransMatResult wk_transMatContinue_result=new NyARTransMatResult();

    /**
     * double arGetTransMatCont( ARMarkerInfo *marker_info, double prev_conv[3][4],double center[2], double width, double conv[3][4] )
     *     
     * @param i_square
     * @param i_direction
     * マーカーの方位を指定する。
     * @param i_width
     * @param io_result_conv
     * 計算履歴を持つNyARTransMatResultオブジェクトを指定する。
     * 履歴を持たない場合は、transMatと同じ処理を行う。
     * @return
     * @throws NyARException
     */
    public double transMatContinue(NyARSquare i_square,int i_direction, double i_width,NyARTransMatResult io_result_conv)throws NyARException
    {
	//io_result_convが初期値なら、transMatで計算する。
	if(!io_result_conv.hasValue()){
	    return this.transMat(i_square, i_direction, i_width, io_result_conv);
	}

	double[][]  ppos2d=wk_transMat_ppos2d;
	double[][]  ppos3d=wk_transMat_ppos3d;
	double[]    off   =wk_transMat_off;
	double[][]  pos3d =wk_transMat_pos3d;
	
	//	arGetTransMatContSub計算部分
	transrot.initRotByPrevResult(io_result_conv);

	//ppos2dとppos3dの初期化
	init_transMat_ppos(i_square,i_direction,i_width,ppos2d,ppos3d);
	
	//arGetTransMat3の前段処理(pos3dとoffを初期化)
	double[][] pos2d=this.wk_transMat_pos2d;
	final NyARMat mat_b =this.wk_transMat_mat_b;
	final NyARMat mat_d =this.wk_transMat_mat_d;

	//transMatに必要な初期値を計算
	arGetTransMat3_initTransMat(ppos3d,ppos2d,pos2d,pos3d,off,mat_b,mat_d);
	
	double  err1,err2;
	int     i;

	err1=err2=-1;
	double[] trans=this.wk_transMat_mat_trans;
	for( i = 0; i < AR_GET_TRANS_MAT_MAX_LOOP_COUNT; i++ ){
	    err1 = arGetTransMatSub(pos2d, pos3d,mat_b,mat_d,trans);
	    if( err1 < AR_GET_TRANS_MAT_MAX_FIT_ERROR ){
		//十分な精度を達成できたらブレーク
		break;
	    }
	}
	//値を保存
	io_result_conv.updateMatrixValue(this.transrot,off,trans);
	
	//エラー値が許容範囲でなければTransMatをやり直し
	if(err1>AR_GET_TRANS_CONT_MAT_MAX_FIT_ERROR) {
	    NyARTransMatResult result2=this.wk_transMatContinue_result;
	    //transMatを実行(初期化値は共用)
	    transrot.initRot(i_square,i_direction);
            err2 = transMat(i_square,i_direction,i_width,result2);
	    //transmMatここまで
	    if(err2<err1){
		//良い値が取れたら、差換え
		io_result_conv.copyFrom(result2);
		err1 = err2;
	    }
	}
	return err1;
    }    
    
    
    

    private final NyARMat wk_arGetTransMat3_mat_a=new NyARMat(NUMBER_OF_VERTEX*2,3);

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
    private final void arGetTransMat3_initTransMat(double[][] i_ppos3d,double[][] i_ppos2d,double[][] o_pos2d,double[][] o_pos3d,double[] o_off,NyARMat o_mat_b,NyARMat o_mat_d)throws NyARException
    {
	double pmax0,pmax1,pmax2,pmin0,pmin1,pmin2;
	int i;
	pmax0=pmax1=pmax2 = -10000000000.0;
	pmin0=pmin1=pmin2 =  10000000000.0;
	for(i = 0; i < NUMBER_OF_VERTEX; i++ ) {
	    if( i_ppos3d[i][0] > pmax0 ){
		pmax0 = i_ppos3d[i][0];
	    }
	    if( i_ppos3d[i][0] < pmin0 ){
		pmin0 = i_ppos3d[i][0];
	    }
	    if( i_ppos3d[i][1] > pmax1 ){
		pmax1 = i_ppos3d[i][1];
	    }
	    if( i_ppos3d[i][1] < pmin1 ){
		pmin1 = i_ppos3d[i][1];
	    }
	    /*	オリジナルでもコメントアウト
	        if( ppos3d[i][2] > pmax[2] ) pmax[2] = ppos3d[i][2];
	        if( ppos3d[i][2] < pmin[2] ) pmin[2] = ppos3d[i][2];
	     */	 
	}
	o_off[0] = -(pmax0 + pmin0) / 2.0;
	o_off[1] = -(pmax1 + pmin1) / 2.0;
	o_off[2] = -(pmax2 + pmin2) / 2.0;


	double[] o_pos3d_pt;
	double[] i_pos_pd_pt;	
	for(i = 0; i < NUMBER_OF_VERTEX; i++ ) {
	    o_pos3d_pt =o_pos3d[i];
	    i_pos_pd_pt=i_ppos3d[i];
	    o_pos3d_pt[0] = i_pos_pd_pt[0] + o_off[0];
	    o_pos3d_pt[1] = i_pos_pd_pt[1] + o_off[1];
	    o_pos3d_pt[2] = 0.0;
	}
	//ココから先でarGetTransMatSubの初期化処理
	//arGetTransMatSubにあった処理。毎回おなじっぽい。pos2dに変換座標を格納する。
	
	if(arFittingMode == AR_FITTING_TO_INPUT ){
	    //arParamIdeal2Observをバッチ処理
	    param.ideal2ObservBatch(i_ppos2d,o_pos2d,NUMBER_OF_VERTEX);
	}else{
	    for(i = 0; i < NUMBER_OF_VERTEX; i++ ){
		o_pos2d[i][0] = i_ppos2d[i][0];
		o_pos2d[i][1] = i_ppos2d[i][1];
	    }
	}

	//変換マトリクスdとbの準備(arGetTransMatSubの一部)
	final double cpara[]=param.get34Array();
	final NyARMat mat_a =this.wk_arGetTransMat3_mat_a;
	final double[][] a_array=mat_a.getArray();

	//mat_bの設定
	final double[][] b_array=o_mat_b.getArray();

	int x2;
	for(i = 0; i < NUMBER_OF_VERTEX; i++ ) {
	    x2=i*2;
	    //</Optimize>
	    a_array[x2  ][0]=b_array[0][x2]=cpara[0*4+0];//mat_a->m[j*6+0] = mat_b->m[num*0+j*2] = cpara[0][0];
	    a_array[x2  ][1]=b_array[1][x2]=cpara[0*4+1];//mat_a->m[j*6+1] = mat_b->m[num*2+j*2] = cpara[0][1];
	    a_array[x2  ][2]=b_array[2][x2]=cpara[0*4+2]-o_pos2d[i][0];//mat_a->m[j*6+2] = mat_b->m[num*4+j*2] = cpara[0][2] - pos2d[j][0];
	    a_array[x2+1][0]=b_array[0][x2+1]=0.0;//mat_a->m[j*6+3] = mat_b->m[num*0+j*2+1] = 0.0;
	    a_array[x2+1][1]=b_array[1][x2+1]=cpara[1*4+1];//mat_a->m[j*6+4] = mat_b->m[num*2+j*2+1] = cpara[1][1];
	    a_array[x2+1][2]=b_array[2][x2+1]=cpara[1*4+2]-o_pos2d[i][1];//mat_a->m[j*6+5] = mat_b->m[num*4+j*2+1] = cpara[1][2] - pos2d[j][1];
	}
	
	//mat_d
	o_mat_d.matrixMul(o_mat_b,mat_a);
	o_mat_d.matrixSelfInv();	
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
    private final double arGetTransMatSub(double i_pos2d[][],double i_pos3d[][],NyARMat i_mat_b,NyARMat i_mat_d,double[] o_trans) throws NyARException
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
	    c_array[x2][0]  =wz * i_pos2d[i][0]- cpara[0*4+0]*wx - cpara[0*4+1]*wy - cpara[0*4+2]*wz;//mat_c->m[j*2+0] = wz * pos2d[j][0]- cpara[0][0]*wx - cpara[0][1]*wy - cpara[0][2]*wz;
	    c_array[x2+1][0]=wz * i_pos2d[i][1]- cpara[1*4+1]*wy - cpara[1*4+2]*wz;//mat_c->m[j*2+1] = wz * pos2d[j][1]- cpara[1][1]*wy - cpara[1][2]*wz;
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
	ret =transrot.modifyMatrix(o_trans, i_pos3d, i_pos2d);
	for( i = 0; i < NUMBER_OF_VERTEX; i++ ) {
	    x2=i*2;
	    i_pos3d_pt=i_pos3d[i];
	    wx = rot[0] * i_pos3d_pt[0]+ rot[1] * i_pos3d_pt[1]+ rot[2] * i_pos3d_pt[2];
	    wy = rot[3] * i_pos3d_pt[0]+ rot[4] * i_pos3d_pt[1]+ rot[5] * i_pos3d_pt[2];
	    wz = rot[6] * i_pos3d_pt[0]+ rot[7] * i_pos3d_pt[1]+ rot[8] * i_pos3d_pt[2];
	    c_array[x2][0]  =wz * i_pos2d[i][0]- cpara[0*4+0]*wx - cpara[0*4+1]*wy - cpara[0*4+2]*wz;//mat_c->m[j*2+0] = wz * pos2d[j][0]- cpara[0][0]*wx - cpara[0][1]*wy - cpara[0][2]*wz;
	    c_array[x2+1][0]=wz * i_pos2d[i][1]- cpara[1*4+1]*wy - cpara[1*4+2]*wz;//mat_c->m[j*2+1] = wz * pos2d[j][1]- cpara[1][1]*wy - cpara[1][2]*wz;
	}

	mat_e.matrixMul(i_mat_b, mat_c );
	mat_f.matrixMul(i_mat_d, mat_e );
	o_trans[0] = f_array[0][0];//trans[0] = mat_f->m[0];
	o_trans[1] = f_array[1][0];
	o_trans[2] = f_array[2][0];//trans[2] = mat_f->m[2];
	ret = transrot.modifyMatrix(o_trans, i_pos3d, i_pos2d);
	return ret;
    }
}
