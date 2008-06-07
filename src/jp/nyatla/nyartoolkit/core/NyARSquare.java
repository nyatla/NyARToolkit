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
 * ARMarkerInfoに相当するクラス。
 * スクエア情報を保持します。
 *
 */
public class NyARSquare extends NyARMarker{
//    private NyARMarker marker;
//    public int area;
//    public double[] pos;
    public double[][] line=new double[4][3];  //double[4][3]
    public double[][] sqvertex=new double[4][2];//double[4][2];
    public NyARSquare()
    {
	super();
    }
    private final NyARMat wk_getLine_input=new NyARMat(1,2);
    private final NyARMat wk_getLine_evec=new NyARMat(2,2);
    private final NyARVec wk_getLine_ev=new NyARVec(2);
    private final NyARVec wk_getLine_mean=new NyARVec(2);
    /**
     * arGetLine(int x_coord[], int y_coord[], int coord_num,int vertex[], double line[4][3], double v[4][2])
     * arGetLine2(int x_coord[], int y_coord[], int coord_num,int vertex[], double line[4][3], double v[4][2], double *dist_factor)
     * の２関数の合成品です。
     * 格納しているマーカー情報に対して、GetLineの計算を行い、結果を返します。
     * Optimize:STEP[424->391]
     * @param i_cparam
     * @return
     * @throws NyARException
     */
    public boolean getLine(NyARParam i_cparam) throws NyARException
    {
	double   w1;
	int      st, ed, n;
	int      i;

	final double[][] l_sqvertex=this.sqvertex;
	final double[][] l_line=this.line;
	final int[] l_mkvertex=this.mkvertex;
	final int[] l_x_coord=this.x_coord;
	final int[] l_y_coord=this.y_coord;	
	final NyARVec ev     = this.wk_getLine_ev;  //matrixPCAの戻り値を受け取る
	final NyARVec mean   = this.wk_getLine_mean;//matrixPCAの戻り値を受け取る
	final double[] mean_array=mean.getArray();
	double[] l_line_i,l_line_2;

	NyARMat input=this.wk_getLine_input;//次処理で初期化される。
	NyARMat evec =this.wk_getLine_evec;//アウトパラメータを受け取るから初期化不要//new NyARMat(2,2);
	double[][] evec_array=evec.getArray();
	for( i = 0; i < 4; i++ ) {
	    w1 = (double)(l_mkvertex[i+1]-l_mkvertex[i]+1) * 0.05 + 0.5;
	    st = (int)(l_mkvertex[i]   + w1);
	    ed = (int)(l_mkvertex[i+1] - w1);
	    n = ed - st + 1;
	    if(n<2){
		//nが2以下でmatrix.PCAを計算することはできないので、エラーにしておく。
		return false;//throw new NyARException();
	    }
	    input.realloc(n,2);
	    //バッチ取得
	    i_cparam.observ2IdealBatch(l_x_coord,l_y_coord,st,n,input.getArray());
//	    for( j = 0; j < n; j++ ) {
//		i_cparam.observ2Ideal(l_x_coord[st+j], l_y_coord[st+j],dv1,dv2);//arParamObserv2Ideal( dist_factor, x_coord[st+j], y_coord[st+j],&(input->m[j*2+0]), &(input->m[j*2+1]) );
//		in_array[j][0]=dv1.value;
//		in_array[j][1]=dv2.value;
//	    }
	    input.matrixPCA(evec, ev, mean);
	    l_line_i=l_line[i];
	    l_line_i[0] =  evec_array[0][1];//line[i][0] =  evec->m[1];
	    l_line_i[1] = -evec_array[0][0];//line[i][1] = -evec->m[0];
	    l_line_i[2] = -(l_line_i[0]*mean_array[0] + l_line_i[1]*mean_array[1]);//line[i][2] = -(line[i][0]*mean->v[0] + line[i][1]*mean->v[1]);
	}

	for( i = 0; i < 4; i++ )
	{
	    l_line_i=l_line[i];
	    l_line_2=l_line[(i+3)%4];
	    w1 = l_line_2[0] * l_line_i[1] - l_line_i[0] * l_line_2[1];
	    if( w1 == 0.0 ){
		return false;
	    }
	    l_sqvertex[i][0] = (  l_line_2[1] * l_line_i[2]- l_line_i[1] * l_line_2[2] ) / w1;
	    l_sqvertex[i][1] = (  l_line_i[0] * l_line_2[2]- l_line_2[0] * l_line_i[2] ) / w1;
	}
	return true;
    }
}
