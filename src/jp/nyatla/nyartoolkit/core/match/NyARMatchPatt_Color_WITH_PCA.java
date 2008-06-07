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
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.NyARColorPatt;

/**
 * AR_TEMPLATE_MATCHING_COLORかつAR_MATCHING_WITH_PCAと同等のルールで
 * マーカーを評価します。
 *
 */
public class NyARMatchPatt_Color_WITH_PCA implements NyARMatchPatt{
    private final int		EVEC_MAX=10;//#define   EVEC_MAX     10
    private int		evec_dim;//static int    evec_dim;
    private int[][][]	input;
    private double[][][][]	evec;//static double evec[EVEC_MAX][AR_PATT_SIZE_Y*AR_PATT_SIZE_X*3];
    private double[][]		epat=new double[4][EVEC_MAX];//static double epat[AR_PATT_NUM_MAX][4][EVEC_MAX];
    private int ave;
    private double datapow;

    private int width;
    private int height;
    private double cf=0;
    private int dir=0;//向きか！
    public double getConfidence(){
	return cf;
    }
    public int getDirection(){
	return dir;
    }
    public boolean setPatt(NyARColorPatt i_target_patt) throws NyARException
    {
	width=i_target_patt.getWidth();
	height=i_target_patt.getHeight();
	int[][][] data=i_target_patt.getPatArray();
 	
	input=new int[height][width][3];
	evec=new double[EVEC_MAX][height][width][3];//static double evec[EVEC_MAX][AR_PATT_SIZE_Y*AR_PATT_SIZE_X*3];
        int sum;

        sum = ave = 0;
        for(int i=0;i<height;i++) {//for(int i=0;i<Config.AR_PATT_SIZE_Y;i++){
            for(int i2=0;i2<width;i2++) {//for(int i2=0;i2<Config.AR_PATT_SIZE_X;i2++){
                ave += (255-data[i][i2][0])+(255-data[i][i2][1])+(255-data[i][i2][2]);
            }
        }
        ave /= (height*width*3);

        for(int i=0;i<height;i++){//for(int i=0;i<Config.AR_PATT_SIZE_Y;i++){
            for(int i2=0;i2<width;i2++){//for(int i2=0;i2<Config.AR_PATT_SIZE_X;i2++){
                for(int i3=0;i3<3;i3++){
                    input[i][i2][i3] = (255-data[i][i2][i3]) - ave;
                    sum += input[i][i2][i3]*input[i][i2][i3];
                }
            }
        }
        datapow = Math.sqrt( (double)sum );
        if(datapow == 0.0){
            return false;//throw new NyARException();
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
        double[]	invec=new double[EVEC_MAX];

        double max = 0.0; // fix VC7 compiler warning: uninitialized variable
       //確認
	for(int i = 0; i < evec_dim; i++ ) {
            invec[i] = 0.0;
            for(int j = 0; j <height; j++ ){//for(int j = 0; j <Config.AR_PATT_SIZE_Y; j++ ){
                for(int j2 = 0; j2 <width; j2++ ){
                    for(int j3 = 0; j3 <3; j3++ ){
            	    	invec[i] += evec[i][j][j2][j3] * input[j][j2][j3];//invec[i] += evec[i][j] * input[j];
                    }
            	}
            }
            invec[i] /= datapow;
        }

        double min = 10000.0;
        int res=-1;
        for(int j = 0; j < 4; j++ ) {
            double sum2 = 0;
            for(int i = 0; i < evec_dim; i++ ) {
                sum2 += (invec[i] - epat[j][i]) * (invec[i] - epat[j][i]);
            }
            if( sum2 < min ) {
            	min = sum2;
            	res = j;
//            	res2 = k;//kは常にインスタンスを刺すから、省略可能
            }
        }

        int sum = 0;
        for(int i=0;i<height;i++){//for(int i=0;i<Config.AR_PATT_SIZE_Y;i++){
            for(int i2=0;i2<width;i2++){//for(int i2=0;i<Config.AR_PATT_SIZE_X;i2++){
                for(int i3=0;i3<3;i3++){
                    sum += input[i][i2][i3]*pat[res][i][i2][i3];//sum += input[i][i2][i3]*pat[res2][res][i][i2][i3];
                }
            }
        }
        max = sum / patpow[res] / datapow;
        dir=res;
        cf=max;
    }
}
