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
import jp.nyatla.nyartoolkit.core.raster.NyARRaster;

/**
 * 24ビットカラーのマーカーを保持するために使うクラスです。
 * このクラスは、ARToolkitのパターンと、ラスタから取得したパターンを保持します。
 * 演算順序を含む最適化をしたもの
 *
 */
public class NyARColorPatt_O2 implements NyARColorPatt
{
    private static final int AR_PATT_SAMPLE_NUM=64;//#define   AR_PATT_SAMPLE_NUM   64
    private int extpat[][][];
    private int width;
    private int height;
    public NyARColorPatt_O2(int i_width,int i_height)
    {
	this.width=i_width;
	this.height=i_height;
	this.extpat=new int[i_height][i_width][3];
	this.wk_pickFromRaster_ext_pat2=new int[i_height][i_width][3];
    }
    public int[][][] getPatArray()
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
    private final NyARMat wk_get_cpara_a=new NyARMat(8,8);
    private final NyARMat wk_get_cpara_b=new NyARMat(8,1);
//    private final NyARMat wk_get_cpara_c=new NyARMat(8,1);
    /**
     * 
     * @param world
     * @param vertex
     * @param para
     * [3x3]
     * @throws NyARException
     */
    /**
     * @param world
     * @param vertex
     * @param o_para
     * @throws NyARException
     */
    private void get_cpara(double vertex_0[], double vertex_1[],NyARMat o_para) throws NyARException
    {
	double world[][]=this.wk_pickFromRaster_world;
        NyARMat a =wk_get_cpara_a;//次処理で値を設定するので、初期化不要// new NyARMat( 8, 8 );
        double[][] a_array=a.getArray();
        NyARMat b =wk_get_cpara_b;//次処理で値を設定するので、初期化不要// new NyARMat( 8, 1 );
        double[][] b_array=b.getArray();
        double[] a_pt0,a_pt1,world_pti;
	    
        for(int i = 0; i < 4; i++ ) {
            a_pt0=a_array[i*2];
            a_pt1=a_array[i*2+1];
            world_pti=world[i];
            
            a_pt0[0]=world_pti[0];//a->m[i*16+0]  = world[i][0];
            a_pt0[1]=world_pti[1];//a->m[i*16+1]  = world[i][1];
            a_pt0[2]=1.0;//a->m[i*16+2]  = 1.0;
            a_pt0[3]=0.0;//a->m[i*16+3]  = 0.0;
            a_pt0[4]=0.0;//a->m[i*16+4]  = 0.0;
            a_pt0[5]=0.0;//a->m[i*16+5]  = 0.0;
            a_pt0[6]=-world_pti[0] * vertex_0[i];//a->m[i*16+6]  = -world[i][0] * vertex[i][0];
            a_pt0[7]=-world_pti[1] * vertex_0[i];//a->m[i*16+7]  = -world[i][1] * vertex[i][0];
            a_pt1[0]=0.0;//a->m[i*16+8]  = 0.0;
            a_pt1[1]=0.0;//a->m[i*16+9]  = 0.0;
            a_pt1[2]=0.0;//a->m[i*16+10] = 0.0;
            a_pt1[3]=world_pti[0];//a->m[i*16+11] = world[i][0];
            a_pt1[4]=world_pti[1];//a->m[i*16+12] = world[i][1];
            a_pt1[5]=1.0;//a->m[i*16+13] = 1.0;
            a_pt1[6]=-world_pti[0] * vertex_1[i];//a->m[i*16+14] = -world[i][0] * vertex[i][1];
            a_pt1[7]=-world_pti[1] * vertex_1[i];//a->m[i*16+15] = -world[i][1] * vertex[i][1];
            b_array[i*2+0][0]=vertex_0[i];//b->m[i*2+0] = vertex[i][0];
            b_array[i*2+1][0]=vertex_1[i];//b->m[i*2+1] = vertex[i][1];
        }
//	    JartkException.trap("未チェックのパス");
        a.matrixSelfInv();
	    
//	    JartkException.trap("未チェックのパス");
//        NyARMat c = wk_get_cpara_c;//次処理で結果を受け取るので、初期化不要//new NyARMat( 8, 1 );
//        double[][] c_array=c.getArray();

        o_para.matrixMul(a, b);
//        para[0*3+0] = c_array[0*3+0][0];//para[i][0] = c->m[i*3+0];
//        para[0*3+1] = c_array[0*3+1][0];//para[i][1] = c->m[i*3+1];
//        para[0*3+2] = c_array[0*3+2][0];//para[i][2] = c->m[i*3+2];
//        para[1*3+0] = c_array[1*3+0][0];//para[i][0] = c->m[i*3+0];
//        para[1*3+1] = c_array[1*3+1][0];//para[i][1] = c->m[i*3+1];
//        para[i*3+2] = c_array[1*3+2][0];//para[i][2] = c->m[i*3+2];
//        para[2*3+0] = c_array[2*3+0][0];//para[2][0] = c->m[2*3+0];
//        para[2*3+1] = c_array[2*3+1][0];//para[2][1] = c->m[2*3+1];
//        para[2*3+2] = 1.0;//para[2][2] = 1.0;
        

    }

  //   private final double[] wk_pickFromRaster_para=new double[9];//[3][3];
    private int[][][] wk_pickFromRaster_ext_pat2=null;//コンストラクタでint[height][width][3]を作る
    private final double[][] wk_pickFromRaster_world={//double    world[4][2];
	    {100.0,     100.0},
	    {100.0+10.0,100.0},
	    {100.0+10.0,100.0 + 10.0},
	    {100.0,     100.0 + 10.0}
    };
    /**
     * pickFromRaster関数から使う変数です。
     *
     */
    private static void initValue_wk_pickFromRaster_ext_pat2(int[][][] i_ext_pat2,int i_width,int i_height)
    {
	int i,i2;
	int[][] pt2;
	int[]   pt1;
	for(i=i_height-1;i>=0;i--){
	    pt2=i_ext_pat2[i];
	    for(i2=i_width-1;i2>=0;i2--){
		pt1=pt2[i2];
		pt1[0]=0;
		pt1[1]=0;
		pt1[2]=0;
	    }
	}
    }
    private final double[][] wk_pickFromRaster_local=new double[2][4];
    private final int[] wk_pickFromRaster_rgb_tmp=new int[3];
    private final NyARMat wk_pickFromRaster_cpara=new NyARMat(8,1);
    /**
     * imageから、i_markerの位置にあるパターンを切り出して、保持します。
     * Optimize:STEP[769->750]
     * @param image
     * @param i_marker
     * @throws Exception
     */
    public void pickFromRaster(NyARRaster image, NyARMarker i_marker) throws NyARException
    {
	NyARMat cpara=this.wk_pickFromRaster_cpara;
	//localの計算
	int[] x_coord=i_marker.x_coord;
	int[] y_coord=i_marker.y_coord;
	int[] vertex=i_marker.mkvertex;
	double[] local_0=wk_pickFromRaster_local[0];//double    local[4][2];	
	double[] local_1=wk_pickFromRaster_local[1];//double    local[4][2];	
	for(int i = 0; i < 4; i++ ) {
	    local_0[i] = x_coord[vertex[i]];
	    local_1[i] = y_coord[vertex[i]];
	}
	//xdiv2,ydiv2の計算
	int xdiv2, ydiv2;
	int l1,l2;
	double w1,w2;

	//x計算
	w1=local_0[0] - local_0[1];
	w2=local_1[0] - local_1[1];
	l1 = (int)(w1*w1+w2*w2);
	w1=local_0[2] - local_0[3];
	w2=local_1[2] - local_1[3];
	l2 = (int)(w1*w1+w2*w2);
	if( l2 > l1 ){
	    l1 = l2;
	}
	l1=l1/4;
	xdiv2 =this.width;
	while( xdiv2*xdiv2 < l1 ){
	    xdiv2*=2;
	}
	if( xdiv2 > AR_PATT_SAMPLE_NUM)
	{
	    xdiv2 =AR_PATT_SAMPLE_NUM;
	}
	
	//y計算
	w1=local_0[1] - local_0[2];
	w2=local_1[1] - local_1[2];
	l1 = (int)(w1*w1+ w2*w2);
	w1=local_0[3] - local_0[0];
	w2=local_1[3] - local_1[0];
	l2 = (int)(w1*w1+ w2*w2);
	if( l2 > l1 ){
	    l1 = l2;
	}
	ydiv2 =this.height;
	l1=l1/4;
	while( ydiv2*ydiv2 < l1 ){
	    ydiv2*=2;
	}
	if( ydiv2 >AR_PATT_SAMPLE_NUM)
	{
	    ydiv2 = AR_PATT_SAMPLE_NUM;
	}	
	
	//cparaの計算
	get_cpara(local_0,local_1,cpara);

	int img_x=image.getWidth();
	int img_y=image.getHeight();

	/*wk_pickFromRaster_ext_pat2ワーク変数を初期化する。*/
	int[][][] ext_pat2=wk_pickFromRaster_ext_pat2;//ARUint32  ext_pat2[AR_PATT_SIZE_Y][AR_PATT_SIZE_X][3];
	int extpat_j[][],extpat_j_i[];
	int ext_pat2_j[][],ext_pat2_j_i[];

	initValue_wk_pickFromRaster_ext_pat2(ext_pat2,this.width,this.height);

	double[][] cpara_array=cpara.getArray();
	double para21_x_yw,para01_x_yw,para11_x_yw;
	double para00,para01,para02,para10,para11,para12,para20,para21;
        para00 = cpara_array[0*3+0][0];//para[i][0] = c->m[i*3+0];
        para01 = cpara_array[0*3+1][0];//para[i][1] = c->m[i*3+1];
        para02 = cpara_array[0*3+2][0];//para[i][2] = c->m[i*3+2];
        para10 = cpara_array[1*3+0][0];//para[i][0] = c->m[i*3+0];
        para11 = cpara_array[1*3+1][0];//para[i][1] = c->m[i*3+1];
        para12 = cpara_array[1*3+2][0];//para[i][2] = c->m[i*3+2];
        para20 = cpara_array[2*3+0][0];//para[2][0] = c->m[2*3+0];
        para21 = cpara_array[2*3+1][0];//para[2][1] = c->m[2*3+1];
        //para22 = 1.0;//para[2][2] = 1.0;

	
	double		d, xw, yw;
	int		xc, yc;
	int i,j;
	int[] rgb_tmp=wk_pickFromRaster_rgb_tmp;
	//   	arGetCode_put_zero(ext_pat2);//put_zero( (ARUint8 *)ext_pat2, AR_PATT_SIZE_Y*AR_PATT_SIZE_X*3*sizeof(ARUint32) );
	int xdiv = xdiv2/width;//xdiv = xdiv2/Config.AR_PATT_SIZE_X;
	int ydiv = ydiv2/height;//ydiv = ydiv2/Config.AR_PATT_SIZE_Y;
	double xdiv2_reciprocal = 1.0 / xdiv2;
	double ydiv2_reciprocal = 1.0 / ydiv2;

	for(j = 0; j < ydiv2; j++ ) {
	    yw = 102.5 + 5.0 * ((double)j+0.5) * ydiv2_reciprocal;
	    para21_x_yw=para21*yw+1.0;
	    para11_x_yw=para11*yw+para12;
	    para01_x_yw=para01*yw+para02;
	    ext_pat2_j=ext_pat2[j/ydiv];
	    for(i = 0; i < xdiv2; i++ ) {
		xw = 102.5 + 5.0 * ((double)i+0.5) * xdiv2_reciprocal;
		d = para20*xw + para21_x_yw;
		if( d == 0 ){
		    throw new NyARException();
		}
		xc = (int)((para00*xw + para01_x_yw)/d);
		yc = (int)((para10*xw + para11_x_yw)/d);


		if( xc >= 0 && xc < img_x && yc >= 0 && yc < img_y ) {
		    image.getPixel(xc, yc, rgb_tmp);
		    ext_pat2_j_i=ext_pat2_j[i/xdiv];

		    ext_pat2_j_i[0] += rgb_tmp[0];//R
		    ext_pat2_j_i[1] += rgb_tmp[1];//G
		    ext_pat2_j_i[2] += rgb_tmp[2];//B
		}
	    }
	}
	/*<Optimize>*/
	int xdiv_x_ydiv=xdiv*ydiv;
	for(j =this.height-1; j>=0; j--){
	    extpat_j=extpat[j];
	    ext_pat2_j=ext_pat2[j];
	    for(i = this.width-1; i>=0; i--){				// PRL 2006-06-08.
		ext_pat2_j_i=ext_pat2_j[i];
		extpat_j_i=extpat_j[i];
		extpat_j_i[0]=(ext_pat2_j_i[0] / xdiv_x_ydiv);//ext_pat[j][i][0] = (byte)(ext_pat2[j][i][0] / (xdiv*ydiv));
		extpat_j_i[1]=(ext_pat2_j_i[1] / xdiv_x_ydiv);//ext_pat[j][i][1] = (byte)(ext_pat2[j][i][1] / (xdiv*ydiv));
		extpat_j_i[2]=(ext_pat2_j_i[2] / xdiv_x_ydiv);//ext_pat[j][i][2] = (byte)(ext_pat2[j][i][2] / (xdiv*ydiv));
	    }
	}
	return;
    }
}