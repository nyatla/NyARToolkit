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


import java.io.*;
import java.nio.*;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.util.DoubleValue;

/*typedef struct {
    int      xsize, ysize;
    double   mat[3][4];
    double   dist_factor[4];
} ARParam;*/
public class NyARParam{
    private static final int SIZE_OF_PARAM_SET=4+4+(3*4*8)+(4*8);
    private static final int PD_LOOP = 3;
    protected int		xsize, ysize;
    private double[][]		mat=new double[3][4];//Double2dArray	mat=new Double2dArray(3,4);
    private double[]		dist_factor=new double[4];
    public int getX()
    {
	return xsize;
    }
    public int getY()
    {
	return ysize;
    }
    public double[] getDistFactor()
    {
	return dist_factor;
    }
    public double[][] getMat()
    {
	return mat;
    }
    /**
     * ARToolKit標準ファイルから1個目の設定をロードする。
     * @param i_filename
     * @throws NyARException
     */
    public void loadFromARFile(String i_filename) throws NyARException
    {
        try {
            loadFromARFile(new FileInputStream(i_filename));
        } catch (Exception e) {
            throw new NyARException(e);
        }
    }
    public void loadFromARFile(InputStream i_stream) throws NyARException
    {
        try {
            NyARParam new_inst[] = arParamLoad(i_stream, 1);
            i_stream.close();
            xsize = new_inst[0].xsize;
            ysize = new_inst[0].ysize;
            mat = new_inst[0].mat;
            dist_factor = new_inst[0].dist_factor;
        } catch (Exception e) {
            throw new NyARException(e);
        }
    }
    /*static double dot( double a1, double a2, double a3,double b1, double b2, double b3 )*/
    private static double dot( double a1, double a2, double a3,double b1, double b2, double b3 )
    {
        return( a1 * b1 + a2 * b2 + a3 * b3 );
    }
    /*	static double norm( double a, double b, double c )*/
    private static double norm( double a, double b, double c )
    {
        return Math.sqrt( a*a + b*b + c*c );
    }
    /**
     * int  arParamDecompMat( double source[3][4], double cpara[3][4], double trans[3][4] );
     * 関数の置き換え
     * @param o_cpara
     * 戻り引数。3x4のマトリクスを指定すること。
     * @param o_trans
     * 戻り引数。3x4のマトリクスを指定すること。
     * @return
     */
    public void decompMat(NyARMat o_cpara, NyARMat o_trans)
    {
	double[][] source=mat;
        double[][] Cpara=new double[3][4];//double    Cpara[3][4];
        double    rem1, rem2, rem3;
    
        if(source[2][3]>= 0 ){//if( source[2][3] >= 0 ) {
            for(int r = 0; r < 3; r++ ){
                for(int c = 0; c < 4; c++ ){
                    Cpara[r][c]=source[r][c];//Cpara[r][c] = source[r][c];
                }
            }
        }else {
            for(int r = 0; r < 3; r++ ){
            	for(int c = 0; c < 4; c++ ){
                    Cpara[r][c]=-source[r][c];//Cpara[r][c] = -(source[r][c]);
                }
            }
        }
    
	double[][] cpara=o_cpara.getArray();
	double[][] trans=o_trans.getArray();
        for(int r = 0; r < 3; r++ ){
            for(int c = 0; c < 4; c++ ){
                cpara[r][c]=0.0;//cpara[r][c] = 0.0;
            }
        }
        cpara[2][2]=norm(Cpara[2][0],Cpara[2][1], Cpara[2][2]);//cpara[2][2] = norm( Cpara[2][0], Cpara[2][1], Cpara[2][2] );
        trans[2][0]=Cpara[2][0] / cpara[2][2];//trans[2][0] = Cpara[2][0] / cpara[2][2];
        trans[2][1]=Cpara[2][1]/ cpara[2][2];//trans[2][1] = Cpara[2][1] / cpara[2][2];
        trans[2][2]=Cpara[2][2]/ cpara[2][2];//trans[2][2] = Cpara[2][2] / cpara[2][2];
        trans[2][3]=Cpara[2][3] / cpara[2][2];//trans[2][3] = Cpara[2][3] / cpara[2][2];
    	
        cpara[1][2]=dot(trans[2][0], trans[2][1], trans[2][2],Cpara[1][0], Cpara[1][1], Cpara[1][2]);//cpara[1][2] = dot( trans[2][0], trans[2][1], trans[2][2],Cpara[1][0], Cpara[1][1], Cpara[1][2] );
        rem1=Cpara[1][0]- cpara[1][2] * trans[2][0];//rem1 = Cpara[1][0] - cpara[1][2] * trans[2][0];
        rem2=Cpara[1][1] - cpara[1][2] * trans[2][1];//rem2 = Cpara[1][1] - cpara[1][2] * trans[2][1];
        rem3=Cpara[1][2] - cpara[1][2] * trans[2][2];//rem3 = Cpara[1][2] - cpara[1][2] * trans[2][2];
        cpara[1][1]=norm(rem1, rem2, rem3 );//cpara[1][1] = norm( rem1, rem2, rem3 );
        trans[1][0]= rem1/cpara[1][1];//trans[1][0] = rem1 / cpara[1][1];
        trans[1][1]=rem2/cpara[1][1];//trans[1][1] = rem2 / cpara[1][1];
        trans[1][2]=rem3 / cpara[1][1];//trans[1][2] = rem3 / cpara[1][1];
    
        cpara[0][2]=dot(trans[2][0],trans[2][1], trans[2][2],Cpara[0][0], Cpara[0][1], Cpara[0][2] );//cpara[0][2] = dot( trans[2][0], trans[2][1], trans[2][2],Cpara[0][0], Cpara[0][1], Cpara[0][2] );
        cpara[0][1]=dot(trans[1][0],trans[1][1], trans[1][2],Cpara[0][0], Cpara[0][1], Cpara[0][2]);//cpara[0][1] = dot( trans[1][0], trans[1][1], trans[1][2],Cpara[0][0], Cpara[0][1], Cpara[0][2] );
        rem1=Cpara[0][0]- cpara[0][1]*trans[1][0] - cpara[0][2]*trans[2][0];//rem1 = Cpara[0][0] - cpara[0][1]*trans[1][0] - cpara[0][2]*trans[2][0];
        rem2 = Cpara[0][1] - cpara[0][1]*trans[1][1] - cpara[0][2]*trans[2][1];//rem2 = Cpara[0][1] - cpara[0][1]*trans[1][1] - cpara[0][2]*trans[2][1];
        rem3 = Cpara[0][2] - cpara[0][1]*trans[1][2] - cpara[0][2]*trans[2][2];//rem3 = Cpara[0][2] - cpara[0][1]*trans[1][2] - cpara[0][2]*trans[2][2];
        cpara[0][0]=norm(rem1, rem2, rem3);//cpara[0][0] = norm( rem1, rem2, rem3 );
        trans[0][0]=rem1 / cpara[0][0];//trans[0][0] = rem1 / cpara[0][0];
        trans[0][1]= rem2 / cpara[0][0];//trans[0][1] = rem2 / cpara[0][0];
        trans[0][2]= rem3 / cpara[0][0];//trans[0][2] = rem3 / cpara[0][0];
    
        trans[1][3]=(Cpara[1][3] - cpara[1][2]*trans[2][3]) / cpara[1][1];//trans[1][3] = (Cpara[1][3] - cpara[1][2]*trans[2][3]) / cpara[1][1];
        trans[0][3]=(Cpara[0][3] - cpara[0][1]*trans[1][3]- cpara[0][2]*trans[2][3]) / cpara[0][0];//trans[0][3] = (Cpara[0][3] - cpara[0][1]*trans[1][3]- cpara[0][2]*trans[2][3]) / cpara[0][0];
    
        for(int  r = 0; r < 3; r++ ){
            for(int c = 0; c < 3; c++ ){
            	cpara[r][c]/=cpara[2][2];//cpara[r][c] /= cpara[2][2];
            }
        }
    }


    /*int    arParamDisp( ARParam *param );*/
    public int paramDisp()
    {
    	System.out.println("--------------------------------------");//printf("--------------------------------------\n");
    	System.out.print("SIZE = "+xsize+", "+ysize);//printf("SIZE = %d, %d\n", param->xsize, param->ysize);
    	System.out.println("Distortion factor = "+dist_factor[0]+" "+dist_factor[1]+" "+dist_factor[2]+" "+dist_factor[3]);//printf("Distortion factor = %f %f %f %f\n", param->dist_factor[0],param->dist_factor[1], param->dist_factor[2], param->dist_factor[3] );
    	for(int j = 0; j < 3; j++ ) {//for(j = 0; j < 3; j++ ) {
    	    for(int i = 0; i < 4; i++ ){
    	    	System.out.print(mat[j][i]+" ");//printf("%7.5f ", param->mat[j][i]);
    	    }
    	    System.out.println();//    printf("\n");
    	}//}
    	System.out.println("--------------------------------------");//printf("--------------------------------------\n");
        return 0;
    }
//    /*int  arParamDecomp( ARParam *source, ARParam *icpara, double trans[3][4] );*/
//    private static int arParamDecomp( NyARParam source, NyARParam icpara, double[][] trans)
//    {
//        icpara.xsize          = source.xsize;//icpara->xsize          = source->xsize;
//        icpara.ysize          = source.ysize;//icpara->ysize          = source->ysize;
//        icpara.dist_factor[0] = source.dist_factor[0];//icpara->dist_factor[0] = source->dist_factor[0];
//        icpara.dist_factor[1] = source.dist_factor[1];// icpara->dist_factor[1] = source->dist_factor[1];
//        icpara.dist_factor[2] = source.dist_factor[2];//icpara->dist_factor[2] = source->dist_factor[2];
//        icpara.dist_factor[3] = source.dist_factor[3];//icpara->dist_factor[3] = source->dist_factor[3];
//        return arParamDecompMat(source.mat, icpara.mat, trans );
//    }
    /**
     * int arParamChangeSize( ARParam *source, int xsize, int ysize, ARParam *newparam );
     * 関数の代替関数
     * サイズプロパティをi_xsize,i_ysizeに変更します。
     * @param xsize
     * @param ysize
     * @param newparam
     * @return
     * 
     */
    public void changeSize(int i_xsize, int i_ysize)
    {
        double  scale;    
        scale = (double)i_xsize / (double)(xsize);//scale = (double)xsize / (double)(source->xsize);
    
        for(int i = 0; i < 4; i++ ) {
            mat[0][i]=mat[0][i]*scale;//newparam->mat[0][i] = source->mat[0][i] * scale;
            mat[1][i]=mat[1][i]*scale;//newparam->mat[1][i] = source->mat[1][i] * scale;
            mat[2][i]=mat[2][i];//newparam->mat[2][i] = source->mat[2][i];
        }
    
        dist_factor[0] = dist_factor[0] * scale;//newparam->dist_factor[0] = source->dist_factor[0] * scale;
        dist_factor[1] = dist_factor[1] * scale;//newparam->dist_factor[1] = source->dist_factor[1] * scale;
        dist_factor[2] = dist_factor[2] / (scale*scale);//newparam->dist_factor[2] = source->dist_factor[2] / (scale*scale);
        dist_factor[3] = dist_factor[3];//newparam->dist_factor[3] = source->dist_factor[3];
    
        xsize = i_xsize;//newparam->xsize = xsize;
        ysize = i_ysize;//newparam->ysize = ysize;
    }
    /**
     * int arParamIdeal2Observ( const double dist_factor[4], const double ix, const double iy,double *ox, double *oy )
     * 関数の代替関数
     * @param ix
     * @param iy
     * @param ox
     * @param oy
     */
    public void ideal2Observ(double ix,double iy,DoubleValue ox, DoubleValue oy)
    {
        double    x, y, d;
        
        x = (ix - dist_factor[0]) * dist_factor[3];
        y = (iy - dist_factor[1]) * dist_factor[3];
        if( x == 0.0 && y == 0.0 ) {
            ox.value=dist_factor[0];
            oy.value=dist_factor[1];
        }else{
            d = 1.0 - dist_factor[2]/100000000.0 * (x*x+y*y);
            ox.value=x * d + dist_factor[0];
            oy.value=y * d + dist_factor[1];
        }
    }
    /*int arParamObserv2Ideal( const double dist_factor[4], const double ox, const double oy,double *ix, double *iy );*/
    public int observ2Ideal(double ox,double oy,DoubleValue ix,DoubleValue iy)
    {
    	double  z02, z0, p, q, z, px, py,opttmp_1;
    	
    	px = ox - dist_factor[0];
    	py = oy - dist_factor[1];
    	p = dist_factor[2]/100000000.0;
    	z02 = px*px+py*py;
    	q = z0 = Math.sqrt(z02);//Optimize//q = z0 = Math.sqrt(px*px+ py*py);
    	
    	for(int i = 1; ; i++ ) {
            if( z0 != 0.0 ) {
        	//Optimize opttmp_1
        	opttmp_1=p*z02;
                z = z0 - ((1.0 - opttmp_1)*z0 - q) / (1.0 - 3.0*opttmp_1);
        	//Optimize opttmp_1       
                opttmp_1=z / z0;
                px = px * opttmp_1;
                py = py * opttmp_1;
            }else {
                px = 0.0;
                py = 0.0;
                break;
            }
            if( i == PD_LOOP ){
                break;
            }
            z02 = px*px+ py*py;
            z0 = Math.sqrt(z02);//Optimize//z0 = Math.sqrt(px*px+ py*py);
    	}
    	
    	ix.value=px / dist_factor[3] + dist_factor[0];
    	iy.value=py / dist_factor[3] + dist_factor[1];
    	return 0;
    }
    /**
     * int    arParamLoad( const char *filename, int num, ARParam *param, ...);
     * i_streamの入力ストリームからi_num個の設定を読み込み、パラメタを配列にして返します。
     * @param filename
     * @param num
     * @param param
     * @return
     * 	設定を格納した配列を返します。
     * @throws Exception
     * i_num個の設定が読み出せない場合、JartkExceptionを発生します。
     */
    private static NyARParam[] arParamLoad(InputStream i_stream, int i_num) throws NyARException
    {
        try{
            int read_size=SIZE_OF_PARAM_SET*i_num;
            byte[] buf=new byte[read_size];
            i_stream.read(buf);
            //返却配列を確保
            NyARParam[] result=new NyARParam[i_num];
            
            //バッファを加工
            ByteBuffer bb = ByteBuffer.wrap(buf);
            bb.order(ByteOrder.BIG_ENDIAN);
    
            //固定回数パースして配列に格納
            for(int i=0;i<i_num;i++){
                NyARParam new_param=new NyARParam();;
                new_param.xsize=bb.getInt();
                new_param.ysize=bb.getInt();
                for(int i2=0;i2<3;i2++){
                    for(int i3=0;i3<4;i3++){
                	new_param.mat[i2][i3]=bb.getDouble();
                    }
                }
    		for(int i2=0;i2<4;i2++){
                    new_param.dist_factor[i2]=bb.getDouble();
    		}
    		result[i]=new_param;
            }
            return result;
        }catch(Exception e){
            throw new NyARException(e);
        }
    }
    public static int arParamSave(String filename,int num, NyARParam param[]) throws Exception
    {
	NyARException.trap("未チェックの関数");
	byte buf[]=new byte[SIZE_OF_PARAM_SET*param.length];
	//バッファをラップ
	ByteBuffer bb = ByteBuffer.wrap(buf);
	bb.order(ByteOrder.BIG_ENDIAN);

	//書き込み
	for(int i=0;i<param.length;i++){
            bb.putInt(param[i].xsize);
            bb.putInt(param[i].ysize);
            for(int i2=0;i2<3;i2++){
                for(int i3=0;i3<4;i3++){
                    bb.putDouble(param[i].mat[i2][i3]);
                }
            }
                for(int i2=0;i2<4;i2++){
                    bb.putDouble(param[i].dist_factor[i2]);
                }
            }
	//ファイルに保存
	FileOutputStream fs=new FileOutputStream(filename);
	fs.write(buf);
	fs.close();

	return 0;
    }
}
