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
 * AR_TEMPLATE_MATCHING_BWと同等のルールで
 * マーカーを評価します。
 *
 */
public class NyARMatchPatt_BlackWhite implements ARMatchPatt{
    private double datapow;
    private int width;
    private int height;
    private double cf=0;
    private int dir=0;
    private int ave;
    private int[][][]	input=new int[height][width][3];
    public boolean setPatt(NyARColorPatt i_target_patt) throws NyARException
    {
	width=i_target_patt.getWidth();
	height=i_target_patt.getHeight();
	short[][][] data=i_target_patt.getPatArray(); 	
	input=new int[height][width][3];
 
        int sum = ave = 0;
        for(int i=0;i<height;i++) {//for(int i=0;i<Config.AR_PATT_SIZE_Y;i++){
            for(int i2=0;i2<width;i2++) {//for(int i2=0;i2<Config.AR_PATT_SIZE_X;i2++){
                ave += (255-data[i][i2][0])+(255-data[i][i2][1])+(255-data[i][i2][2]);
            }
        }
        ave /= (height*width*3);

        for(int i=0;i<height;i++){//for(int i=0;i<Config.AR_PATT_SIZE_Y;i++){
            for(int i2=0;i2<width;i2++){//for(int i2=0;i2<Config.AR_PATT_SIZE_X;i2++){
                input[i][i2][0] = ((255-data[i][i2][0]) + (255-data[i][i2][1]) + (255-data[i][i2][2]))/3 - ave;
                sum += input[i][i2][0]*input[i][i2][0];
            }
        }
        
        datapow = Math.sqrt( (double)sum );
        if( datapow == 0.0 ){
            return false;//            throw new NyARException();
//            dir.set(0);//*dir  = 0;
//            cf.set(-1.0);//*cf   = -1.0;
//            return -1;
        }
        return true;
    }
    public double getConfidence()
    {
	return cf;
    }
    public int getDirection()
    {
	return dir;
    }
    public void evaluate(NyARCode i_code)
    {
	short[][][] 	patBW=i_code.getPatBW();//static int    patBW[AR_PATT_NUM_MAX][4][AR_PATT_SIZE_Y*AR_PATT_SIZE_X*3];
	double[]	patpowBW=i_code.getPatPowBW();//static double patpowBW[AR_PATT_NUM_MAX][4];

	double max=0.0;
	int res=-1;
        //本家が飛ぶ。試験データで0.77767376888がが出ればOKってことで
        for(int j = 0; j < 4; j++ ) {
            int sum = 0;
            for(int i=0;i<height;i++){
                for(int i2=0;i2<width;i2++){
            	sum += input[i][i2][0]*patBW[j][i][i2];
                }
            }
            double sum2 = sum / patpowBW[j] / datapow;
            if( sum2 > max ) {
            	max = sum2;
            	res = j;
            }
        }
        dir=res;
        cf=max;       
    }
}
