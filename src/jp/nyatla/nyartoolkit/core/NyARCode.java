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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

import jp.nyatla.nyartoolkit.NyARException;





/**
 * ARToolKitのマーカーコードを1個保持します。
 *
 */
public class NyARCode{
    private int[][][][]	pat;//static int    pat[AR_PATT_NUM_MAX][4][AR_PATT_SIZE_Y*AR_PATT_SIZE_X*3];
    private double[]		patpow=new double[4];//static double patpow[AR_PATT_NUM_MAX][4];
    private short[][][]	patBW;//static int    patBW[AR_PATT_NUM_MAX][4][AR_PATT_SIZE_Y*AR_PATT_SIZE_X*3];
    private double[]		patpowBW=new double[4];//static double patpowBW[AR_PATT_NUM_MAX][4];	
    private int width,height;
    public int[][][][] getPat()
    {
	return pat;
    }
    public double [] getPatPow()
    {
	return patpow;
    }
    public short[][][] getPatBW()
    {
	return patBW;
    }
    public double[] getPatPowBW()
    {
	return patpowBW;
    }
    public int getWidth()
    {
	return width;
    }
    public int getHeight()
    {
	return height;
    }
    
    public NyARCode(int i_width,int i_height)
    {
	width=i_width;
	height=i_height;
	pat=new int[4][height][width][3];//static int    pat[AR_PATT_NUM_MAX][4][AR_PATT_SIZE_Y*AR_PATT_SIZE_X*3];
	patBW=new short[4][height][width];//static int    patBW[AR_PATT_NUM_MAX][4][AR_PATT_SIZE_Y*AR_PATT_SIZE_X*3];
    }
    /**
     * int arLoadPatt( const char *filename );
     * ARToolKitのパターンファイルをロードする。
     * ファイル形式はBGR形式で記録されたパターンファイルであること。
     * @param filename
     * @return
     * @throws Exception
     */
    public void loadFromARFile(String filename) throws NyARException
    {
	try {
	    loadFromARFile(new FileInputStream(filename));

	} catch (Exception e) {
	throw new NyARException(e);
	}
    }
    /**
     * 
     * @param i_stream
     * @throws NyARException
     */
    public void loadFromARFile(InputStream i_stream) throws NyARException
    {
	try{
            StreamTokenizer st=new StreamTokenizer(new InputStreamReader(i_stream));
            //パターンデータはGBRAで並んでる。
            for(int h=0; h<4; h++ ) {
                int l = 0;
                for(int i3 = 0; i3 < 3; i3++ ) {
                    for(int i2 = 0; i2 < height; i2++ ) {
                        for(int i1 = 0; i1 < width; i1++ ){
                            //数値のみ読み出す
                            switch(st.nextToken()){//if( fscanf(fp, "%d", &j) != 1 ) {
                            case StreamTokenizer.TT_NUMBER:
                        	break;
                            default:
                        	throw new NyARException();
                            }
                            short j=(short)(255-st.nval);//j = 255-j;
                            //標準ファイルのパターンはBGRでならんでるからRGBに並べなおす
                            switch(i3){
                            case 0:pat[h][i2][i1][2] = j;break;//pat[patno][h][(i2*Config.AR_PATT_SIZE_X+i1)*3+2] = j;break;
                            case 1:pat[h][i2][i1][1] = j;break;//pat[patno][h][(i2*Config.AR_PATT_SIZE_X+i1)*3+1] = j;break;
                            case 2:pat[h][i2][i1][0] = j;break;//pat[patno][h][(i2*Config.AR_PATT_SIZE_X+i1)*3+0] = j;break;
                            }
                            //pat[patno][h][(i2*Config.AR_PATT_SIZE_X+i1)*3+i3] = j;
                            if( i3 == 0 ){
                                patBW[h][i2][i1]  = j;//patBW[patno][h][i2*Config.AR_PATT_SIZE_X+i1]  = j;
                            }else{
                                patBW[h][i2][i1] += j;//patBW[patno][h][i2*Config.AR_PATT_SIZE_X+i1] += j;
                            }
                            if( i3 == 2 ){
                            	patBW[h][i2][i1] /= 3;//patBW[patno][h][i2*Config.AR_PATT_SIZE_X+i1] /= 3;
                            }
                            l += j;
                        }
                    }
                }
  
                l /= (height*width*3);
       
                int m = 0;
                for(int i = 0; i < height; i++ ) {//for( i = 0; i < AR_PATT_SIZE_Y*AR_PATT_SIZE_X*3; i++ ) {
                    for(int i2 = 0; i2 < width; i2++ ) {
                	for(int i3 = 0; i3 < 3; i3++ ) {
                	    pat[h][i][i2][i3] -= l;
                	    m += (pat[h][i][i2][i3]*pat[h][i][i2][i3]);
                	}
                    }
                }
                patpow[h] = Math.sqrt((double)m);
                if( patpow[h] == 0.0 ){
                    patpow[h] = 0.0000001;
                }
    
                m = 0;
                for(int i = 0; i < height; i++ ) {
                    for(int i2 = 0; i2 < width; i2++ ) {
                	patBW[h][i][i2] -= l;
                	m += (patBW[h][i][i2]*patBW[h][i][i2]);
                    }
                }
                patpowBW[h] = Math.sqrt((double)m);
                if(patpowBW[h] == 0.0 ){
                    patpowBW[h] = 0.0000001;
                }
            }
	}catch(Exception e){
	    throw new NyARException(e);
	}
    }
}
