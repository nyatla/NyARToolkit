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
import jp.nyatla.nyartoolkit.core.raster.*;

/**
 * 24ビットカラーのマーカーを保持するために使うクラスです。
 * このクラスは、ARToolkitのパターンと、ラスタから取得したパターンを保持します。
 *
 */
public class NyARColorPatt
{
    public static final int AR_PATT_SAMPLE_NUM=64;//#define   AR_PATT_SAMPLE_NUM   64
    private short extpat[][][];
    private int width;
    private int height;
    public NyARColorPatt(int i_width,int i_height)
    {
	width=i_width;
	height=i_height;
	extpat=new short[i_height][i_width][3];
    }
    public short[][][] getPatArray()
    {
	return extpat;
    }
    public int getWidth()
    {
	return width;
    }
    public int getHeight()
    {
	return height;
    }
    private static void get_cpara( double world[][], double vertex[][],double para[][] ) throws NyARException
    {
        NyARMat a = new NyARMat( 8, 8 );
        double[][] a_array=a.getArray();
        NyARMat b = new NyARMat( 8, 1 );
        double[][] b_array=b.getArray();
        NyARMat c = new NyARMat( 8, 1 );
        double[][] c_array=c.getArray();
	    
        for(int i = 0; i < 4; i++ ) {
            a_array[i*2][0]=world[i][0];//a->m[i*16+0]  = world[i][0];
            a_array[i*2][1]=world[i][1];//a->m[i*16+1]  = world[i][1];
            a_array[i*2][2]=1.0;//a->m[i*16+2]  = 1.0;
            a_array[i*2][3]=0.0;//a->m[i*16+3]  = 0.0;
            a_array[i*2][4]=0.0;//a->m[i*16+4]  = 0.0;
            a_array[i*2][5]=0.0;//a->m[i*16+5]  = 0.0;
            a_array[i*2][6]=-world[i][0] * vertex[i][0];//a->m[i*16+6]  = -world[i][0] * vertex[i][0];
            a_array[i*2][7]=-world[i][1] * vertex[i][0];//a->m[i*16+7]  = -world[i][1] * vertex[i][0];
            a_array[i*2+1][0]=0.0;//a->m[i*16+8]  = 0.0;
            a_array[i*2+1][1]=0.0;//a->m[i*16+9]  = 0.0;
            a_array[i*2+1][2]=0.0;//a->m[i*16+10] = 0.0;
            a_array[i*2+1][3]=world[i][0];//a->m[i*16+11] = world[i][0];
            a_array[i*2+1][4]=world[i][1];//a->m[i*16+12] = world[i][1];
            a_array[i*2+1][5]=1.0;//a->m[i*16+13] = 1.0;
            a_array[i*2+1][6]=-world[i][0] * vertex[i][1];//a->m[i*16+14] = -world[i][0] * vertex[i][1];
            a_array[i*2+1][7]=-world[i][1] * vertex[i][1];//a->m[i*16+15] = -world[i][1] * vertex[i][1];
            b_array[i*2+0][0]=vertex[i][0];//b->m[i*2+0] = vertex[i][0];
            b_array[i*2+1][0]=vertex[i][1];//b->m[i*2+1] = vertex[i][1];
        }
//	    JartkException.trap("未チェックのパス");
        a.matrixSelfInv();
	    
//	    JartkException.trap("未チェックのパス");
        NyARMat.matrixMul( c, a, b );
        for(int i = 0; i < 2; i++ ) {
            para[i][0] = c_array[i*3+0][0];//para[i][0] = c->m[i*3+0];
            para[i][1] = c_array[i*3+1][0];//para[i][1] = c->m[i*3+1];
            para[i][2] = c_array[i*3+2][0];//para[i][2] = c->m[i*3+2];
        }
        para[2][0] = c_array[2*3+0][0];//para[2][0] = c->m[2*3+0];
        para[2][1] = c_array[2*3+1][0];//para[2][1] = c->m[2*3+1];
        para[2][2] = 1.0;//para[2][2] = 1.0;
    }
    /**
     * imageから、i_markerの位置にあるパターンを切り出して、保持します。
     * @param image
     * @param i_marker
     * @throws Exception
     */
    public void pickFromRaster(NyARRaster image, NyARMarker i_marker) throws NyARException
    {
	int[] x_coord=i_marker.x_coord;
	int[] y_coord=i_marker.y_coord;
	int[] vertex=i_marker.vertex;
	int[][][] ext_pat2=new int[height][width][3];//ARUint32  ext_pat2[AR_PATT_SIZE_Y][AR_PATT_SIZE_X][3];
	double[][] world=new double[4][2];//double    world[4][2];
	double[][] local=new double[4][2];//double    local[4][2];
        double[][] para=new double[3][3]; //double    para[3][3];
        double		d, xw, yw;
        int		xc, yc;
        int		xdiv, ydiv;
        int		xdiv2, ydiv2;
        int		lx1, lx2, ly1, ly2;
        int img_x,img_y;
        
        img_x=image.getWidth();
        img_y=image.getHeight();

        double    xdiv2_reciprocal; // [tp]
        double    ydiv2_reciprocal; // [tp]
        int       ext_pat2_x_index;
        int       ext_pat2_y_index;
        
        world[0][0] = 100.0;
        world[0][1] = 100.0;
        world[1][0] = 100.0 + 10.0;
        world[1][1] = 100.0;
        world[2][0] = 100.0 + 10.0;
        world[2][1] = 100.0 + 10.0;
        world[3][0] = 100.0;
        world[3][1] = 100.0 + 10.0;
        for(int i = 0; i < 4; i++ ) {
            local[i][0] = x_coord[vertex[i]];
            local[i][1] = y_coord[vertex[i]];
        }
        get_cpara( world, local, para );

        lx1 = (int)((local[0][0] - local[1][0])*(local[0][0] - local[1][0])+ (local[0][1] - local[1][1])*(local[0][1] - local[1][1]));
        lx2 = (int)((local[2][0] - local[3][0])*(local[2][0] - local[3][0])+ (local[2][1] - local[3][1])*(local[2][1] - local[3][1]));
        ly1 = (int)((local[1][0] - local[2][0])*(local[1][0] - local[2][0])+ (local[1][1] - local[2][1])*(local[1][1] - local[2][1]));
        ly2 = (int)((local[3][0] - local[0][0])*(local[3][0] - local[0][0])+ (local[3][1] - local[0][1])*(local[3][1] - local[0][1]));
        if( lx2 > lx1 ){
            lx1 = lx2;
        }
        if( ly2 > ly1 ){
            ly1 = ly2;
        }
        xdiv2 =width;
        ydiv2 =height;
        
        while( xdiv2*xdiv2 < lx1/4 ){
            xdiv2*=2;
        }
        while( ydiv2*ydiv2 < ly1/4 ){
            ydiv2*=2;
        }
        
        if( xdiv2 > AR_PATT_SAMPLE_NUM)
        {
            xdiv2 =AR_PATT_SAMPLE_NUM;
        }
        if( ydiv2 >AR_PATT_SAMPLE_NUM)
        {
            ydiv2 = AR_PATT_SAMPLE_NUM;
        }
        
        xdiv = xdiv2/width;//xdiv = xdiv2/Config.AR_PATT_SIZE_X;
        ydiv = ydiv2/height;//ydiv = ydiv2/Config.AR_PATT_SIZE_Y;
/*
printf("%3d(%f), %3d(%f)\n", xdiv2, sqrt(lx1), ydiv2, sqrt(ly1));
*/

        xdiv2_reciprocal = 1.0 / xdiv2;
        ydiv2_reciprocal = 1.0 / ydiv2;
        int[] rgb_tmp=new int[3];
 //   	arGetCode_put_zero(ext_pat2);//put_zero( (ARUint8 *)ext_pat2, AR_PATT_SIZE_Y*AR_PATT_SIZE_X*3*sizeof(ARUint32) );
        for(int j = 0; j < ydiv2; j++ ) {
	yw = 102.5 + 5.0 * (j+0.5) * ydiv2_reciprocal;
	for(int i = 0; i < xdiv2; i++ ) {
	    xw = 102.5 + 5.0 * (i+0.5) * xdiv2_reciprocal;
	    d = para[2][0]*xw + para[2][1]*yw + para[2][2];
	    if( d == 0 ){
		throw new NyARException();
	    }
	    xc = (int)((para[0][0]*xw + para[0][1]*yw + para[0][2])/d);
	    yc = (int)((para[1][0]*xw + para[1][1]*yw + para[1][2])/d);


	    if( xc >= 0 && xc < img_x && yc >= 0 && yc < img_y ) {
		ext_pat2_y_index = j/ydiv;
		ext_pat2_x_index = i/xdiv;
//		image_index = (yc*arUtil_c.arImXsize+xc)*Config.AR_PIX_SIZE_DEFAULT;
		image.pickRgbArray(xc, yc, rgb_tmp);

		ext_pat2[ext_pat2_y_index][ext_pat2_x_index][0] += rgb_tmp[0];//R
		ext_pat2[ext_pat2_y_index][ext_pat2_x_index][1] += rgb_tmp[1];//G
		ext_pat2[ext_pat2_y_index][ext_pat2_x_index][2] += rgb_tmp[2];//B
//					System.out.println(xc+":"+yc+":"+rgb_tmp[0]+":"+rgb_tmp[1]+":"+rgb_tmp[2]);
		}
	    }
	}
//            short[][][] ext_pat=new short[Config.AR_PATT_SIZE_Y][Config.AR_PATT_SIZE_X][3];//ARUint32  ext_pat2[AR_PATT_SIZE_Y][AR_PATT_SIZE_X][3];

        for(int j = 0; j < height; j++ ) {
            for(int i = 0; i < width; i++ ) {				// PRL 2006-06-08.
                extpat[j][i][0]=(short)(ext_pat2[j][i][0] / (xdiv*ydiv));//ext_pat[j][i][0] = (byte)(ext_pat2[j][i][0] / (xdiv*ydiv));
                extpat[j][i][1]=(short)(ext_pat2[j][i][1] / (xdiv*ydiv));//ext_pat[j][i][1] = (byte)(ext_pat2[j][i][1] / (xdiv*ydiv));
                extpat[j][i][2]=(short)(ext_pat2[j][i][2] / (xdiv*ydiv));//ext_pat[j][i][2] = (byte)(ext_pat2[j][i][2] / (xdiv*ydiv));
            }
        }
    }
}