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

import jp.nyatla.util.DoubleValue;
import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;


public class NyARDetectSquare{
    private NyARLabeling labeling;
    private NyARDetectMarker detect;

    private NyARSquare[] marker_info;
    private int number_of_square;
    private NyARParam param;

    /**
     * 最大i_sqaure_max個のマーカー情報を抽出できるインスタンスを作る。
     * @param i_sqaure_max
     */
    public NyARDetectSquare(int i_sqaure_max,NyARParam i_param)
    {
	param=i_param;
	marker_info=new NyARSquare[i_sqaure_max];//static ARMarkerInfo    marker_infoL[AR_SQUARE_MAX];
	//解析オブジェクトを作る
	int width=i_param.getX();
	int height=i_param.getY();

	labeling=new NyARLabeling(width,height);
	detect=new NyARDetectMarker(width,height,i_sqaure_max);
    }
    public NyARSquare[] getSquareArray()
    {
	return marker_info;
    }
    public int getSquareCount()
    {
	return number_of_square;
    }
    /**
     * 矩形を検出する。
     * @param i_marker
     * @param i_number_of_marker
     * @throws NyARException
     */
    public void detectSquare(NyARRaster i_image,int i_thresh) throws NyARException
    {
	number_of_square=0;
	labeling.labeling(i_image, i_thresh);
	if(labeling.getLabelNum()<1){
	    return;
	}
	detect.detectMarker(labeling,1.0);
	int number_of_marker=detect.getMarkerNum();


	
	int j=0;
	for (int i = 0; i <number_of_marker; i++){
	    double[][]  line	=new double[4][3];
	    double[][]  vertex	=new double[4][2];
	    NyARMarker marker=detect.getMarker(i);
	    
	    //・・・線の検出？？
            if (!getLine(marker.x_coord, marker.y_coord,marker.coord_num, marker.vertex,line,vertex))
            {
            	continue;
            }
            //markerは参照渡し。実体はdetect内のバッファを共有してるので注意
            marker_info[j]=new NyARSquare(marker,line,vertex);

            
//ここで計算するのは良くないと思うんだ	
//		marker_infoL[j].id  = id.get();
//		marker_infoL[j].dir = dir.get();
//		marker_infoL[j].cf  = cf.get();	
            j++;
            //配列数こえたらドゴォォォンしないようにループを抜ける
            if(j>=marker_info.length){
        	break;
            }
	}
	number_of_square=j;
    }
    /**
     * arGetLine(int x_coord[], int y_coord[], int coord_num,int vertex[], double line[4][3], double v[4][2])
     * arGetLine2(int x_coord[], int y_coord[], int coord_num,int vertex[], double line[4][3], double v[4][2], double *dist_factor)
     * の２関数の合成品です。
     * @param x_coord
     * @param y_coord
     * @param coord_num
     * @param vertex
     * @param line
     * @param v
     * @return
     * @throws NyARException
     */
    private boolean getLine(int x_coord[], int y_coord[], int coord_num,int vertex[], double line[][], double v[][]) throws NyARException
    {
        NyARMat    input,evec;
        NyARVec    ev,mean;
        double   w1;
        int      st, ed, n;
        int      i, j;
        DoubleValue dv1=new DoubleValue();
        DoubleValue dv2=new DoubleValue();
		
        ev     = new NyARVec(2);
        mean   = new NyARVec(2);
        evec   = new NyARMat(2,2);
        double[] mean_array=mean.getArray();
        double[][] evec_array=evec.getArray();
        for( i = 0; i < 4; i++ ) {
            w1 = (double)(vertex[i+1]-vertex[i]+1) * 0.05 + 0.5;
            st = (int)(vertex[i]   + w1);
            ed = (int)(vertex[i+1] - w1);
            n = ed - st + 1;
            if(n<2){//nが2以下でmatrix.PCAを計算することはできないので、エラーにしておく。
        	//System.err.println("NyARDetectSquare::getLine 稀に出るエラーです。このエラーが出ても例外が起こらなければ平気だと思いますが、出たらnyatlaまで連絡してください。");
        	return false;//throw new NyARException();
            }
            input  = new NyARMat( n, 2 );
            double [][] in_array=input.getArray();
            for( j = 0; j < n; j++ ) {
        	param.observ2Ideal(x_coord[st+j], y_coord[st+j],dv1,dv2);//arParamObserv2Ideal( dist_factor, x_coord[st+j], y_coord[st+j],&(input->m[j*2+0]), &(input->m[j*2+1]) );
                in_array[j][0]=dv1.value;
                in_array[j][1]=dv2.value;
            }
            NyARMat.matrixPCA(input, evec, ev, mean);
            
            line[i][0] =  evec_array[0][1];//line[i][0] =  evec->m[1];
            line[i][1] = -evec_array[0][0];//line[i][1] = -evec->m[0];
            line[i][2] = -(line[i][0]*mean_array[0] + line[i][1]*mean_array[1]);//line[i][2] = -(line[i][0]*mean->v[0] + line[i][1]*mean->v[1]);
        }
        
        for( i = 0; i < 4; i++ ) {
            w1 = line[(i+3)%4][0] * line[i][1] - line[i][0] * line[(i+3)%4][1];
            if( w1 == 0.0 ){
                return false;
            }
            v[i][0] = (  line[(i+3)%4][1] * line[i][2]- line[i][1] * line[(i+3)%4][2] ) / w1;
            v[i][1] = (  line[i][0] * line[(i+3)%4][2]- line[(i+3)%4][0] * line[i][2] ) / w1;
        }
        return true;
    }
}
