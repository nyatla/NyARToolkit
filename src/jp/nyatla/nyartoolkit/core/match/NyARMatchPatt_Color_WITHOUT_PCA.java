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
package jp.nyatla.nyartoolkit.core.match;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.*;

/**
 * AR_TEMPLATE_MATCHING_COLORかつAR_MATCHING_WITHOUT_PCAと同等のルールで
 * マーカーを評価します。
 *
 */
public class NyARMatchPatt_Color_WITHOUT_PCA implements NyARMatchPatt{
    private int[][][]	input=new int[1][1][3];
    private double datapow;

    private int width =1;
    private int height=1;
    private double cf=0;
    private int dir=0;
    public double getConfidence(){
	return cf;
    }
    public int getDirection(){
	return dir;
    }
    /**
     * input配列サイズを必要に応じて再アロケートする。
     *
     * @param i_width
     * @param i_height
     */
    private void reallocInputArray(int i_width,int i_height)
    {
        if(this.input.length<i_height || this.input[0].length<i_width){
            //配列が十分なサイズでなければ取り直す
            this.input=new int[i_height][i_width][3];		
        }
        this.height=i_height;
        this.width =i_width;
    }
    public boolean setPatt(NyARColorPatt i_target_patt) throws NyARException
    { 
	int i,k;
	int[][][] data,linput;
	
	//input配列のサイズとwhも更新//	input=new int[height][width][3];
	reallocInputArray(i_target_patt.getWidth(),i_target_patt.getHeight());
	int lwidth =this.width;
	int lheight=this.height;
	linput=this.input;
	data=i_target_patt.getPatArray();

	int sum=0,l_ave=0,w_sum;
        int[][] data_i,input_i;
        int[] data_i_k,input_i_k;
        for(i=lheight-1;i>=0;i--){//<Optimize/>for(int i=0;i<height;i++) {//for(int i=0;i<Config.AR_PATT_SIZE_Y;i++){
            data_i=data[i];
            for(k=lwidth-1;k>=0;k--) {//<Optimize/>for(int i2=0;i2<Config.AR_PATT_SIZE_X;i2++){
        	//<Optimize/>l_ave += (255-data[i][i2][0])+(255-data[i][i2][1])+(255-data[i][i2][2]);
        	data_i_k=data_i[k];
        	l_ave += 255*3-data_i_k[0]-data_i_k[1]-data_i_k[2];
            }
        }
        l_ave /= (lheight*lwidth*3);
        for(i=lheight-1;i>=0;i--){//for(i=0;i<height;i++){//for(int i=0;i<Config.AR_PATT_SIZE_Y;i++){
            input_i=linput[i];
            data_i=data[i];
            for(k=lwidth-1;k>=0;k--){//for(i2=0;i2<width;i2++){//for(int i2=0;i2<Config.AR_PATT_SIZE_X;i2++){
                //<Optimize>
                //for(int i3=0;i3<3;i3++){
                //    input[i][i2][i3] = (255-data[i][i2][i3]) - l_ave;
                //    sum += input[i][i2][i3]*input[i][i2][i3];
                //}
        	data_i_k =data_i[k];
        	input_i_k=input_i[k];
        	w_sum=(255-data_i_k[0]) - l_ave;
        	input_i_k[0]=w_sum;
                sum += w_sum*w_sum;
                
                w_sum=(255-data_i_k[1]) - l_ave;
                input_i_k[1]=w_sum;
                sum += w_sum*w_sum;
                
                w_sum=(255-data_i_k[2]) - l_ave;
                input_i_k[2]=w_sum;
                sum+=w_sum*w_sum;
                //</Optimize>
            }
        }
        datapow = Math.sqrt( (double)sum );
        if(datapow == 0.0){
            return false;//            throw new NyARException();
//            dir.set(0);//*dir  = 0;
//            cf.set(-1.0);//*cf   = -1.0;
//            return -1;
        }
        return true;
    }
    /**
     * public int pattern_match(short[][][] data,IntPointer dir,DoublePointer cf)

     */
    public void evaluate(NyARCode i_code)
    {
	int[][][][] pat=i_code.getPat();
	double[] patpow=i_code.getPatPow();
	int res= -1;
	double max=0.0;
	int[][][] pat_j,linput;
	int[][] pat_j_i,input_i;
	int[] pat_j_i_k,input_i_k;
	int l_width=this.width;
	int l_height=this.height;
	linput=this.input;
	for(int j = 0; j < 4; j++ ) {
	    int sum = 0;
	    pat_j=pat[j];
	    for(int i=l_height-1;i>=0;i--){//for(int i=0;i<Config.AR_PATT_SIZE_Y;i++){
		input_i=linput[i];
		pat_j_i=pat_j[i];
		for(int k=l_width-1;k>=0;k--){
		    pat_j_i_k=pat_j_i[k];
		    input_i_k=input_i[k];
//		    for(int i3=0;i3<3;i3++){
		    sum += input_i_k[0]*pat_j_i_k[0];//sum += input[i][i2][i3]*pat[k][j][i][i2][i3];
		    sum += input_i_k[1]*pat_j_i_k[1];//sum += input[i][i2][i3]*pat[k][j][i][i2][i3];
		    sum += input_i_k[2]*pat_j_i_k[2];//sum += input[i][i2][i3]*pat[k][j][i][i2][i3];
//		    }
		}
	    }
	    double sum2 = sum / patpow[j] / datapow;//sum2 = sum / patpow[k][j] / datapow;
	    if( sum2 > max ){
		max = sum2;
		res = j;
	    }
	}
	dir=res;
	cf=max;
    }
}