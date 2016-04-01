/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.param.distfactor;

import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、OpenCV distortion modelの樽型歪み設定/解除クラスです。
 */
public class NyARCameraDistortionFactorV4 extends NyARCameraDistortionFactorImpl
{	
	public final static int NUM_OF_FACTOR=9;
	final private double _k1;
	final private double _k2;
	final private double _p1;
	final private double _p2;
	final private double _fx;
	final private double _fy;
	final private double _x0;
	final private double _y0;
	final private double _s;
	
	/**
	 * この関数は、配列の値を歪みパラメータ値として、このインスタンスにセットします。
	 * @param i_factor
	 * 歪みパラメータ値を格納した配列。
	 */
	public NyARCameraDistortionFactorV4(double[] i_factor,double i_x_scale,double i_y_scale)
	{
		this._k1=i_factor[0];
		this._k2=i_factor[1];
		this._p1=i_factor[2];
		this._p2=i_factor[3];
		this._fx=i_factor[4]* i_x_scale;   /*  fx  */
		this._fy=i_factor[5]* i_y_scale;   /*  fy  */
		this._x0=i_factor[6]* i_x_scale;   /*  x0  */
		this._y0=i_factor[7]* i_y_scale;   /*  y0  */
		this._s =i_factor[8];
	}
	public NyARCameraDistortionFactorV4(NyARCameraDistortionFactorV4 i_copyfrom,double i_x_scale, double i_y_scale)
	{
		this(new double[]{i_copyfrom._k1,i_copyfrom._k2,i_copyfrom._p1,i_copyfrom._p2,i_copyfrom._fx,i_copyfrom._fy,i_copyfrom._x0,i_copyfrom._y0,i_copyfrom._s},i_x_scale,i_y_scale);
		return;
	}
	
	/**
	 * @param i_width
	 * パラメータを生成したスクリーンの幅。
	 * @param i_height
	 * パラメータを生成したスクリーンの高さ。
	 * @param i_intrinsic_matrix
	 * 3x3 matrix
	 * このパラメータは、OpenCVのcvCalibrateCamera2関数が出力するintrinsic_matrixの値と合致します。
	 * @param i_distortion_coeffs
	 * 4x1 vector
	 * このパラメータは、OpenCVのcvCalibrateCamera2関数が出力するdistortion_coeffsの値と合致します。
	 */
	public NyARCameraDistortionFactorV4(int i_width,int i_height,double[] i_intrinsic_matrix,double[] i_distortion_coeffs,double i_x_scale,double i_y_scale)
	{
		double k1=i_distortion_coeffs[0];
		double k2=i_distortion_coeffs[1];
		double p1=i_distortion_coeffs[2];
		double p2=i_distortion_coeffs[3];
		double fx=i_intrinsic_matrix[0*3+0];//0,0
		double fy=i_intrinsic_matrix[1*3+1];//1,1
		double x0=i_intrinsic_matrix[0*3+2];//0,2
		double y0=i_intrinsic_matrix[1*3+2];//1,2
		//sパラメータを計算するためのスケール1のインスタンス
		NyARCameraDistortionFactorV4 td=new NyARCameraDistortionFactorV4(new double[]{k1,k2,p1,p2,fx,fy,x0,y0,1},1,1);		
		this._k1=k1;
		this._k2=k2;
		this._p1=p1;
		this._p2=p2;
		this._fx=fx* i_x_scale;   /*  fx  */
		this._fy=fy* i_y_scale;   /*  fy  */
		this._x0=x0* i_x_scale;   /*  x0  */
		this._y0=y0* i_y_scale;   /*  y0  */
		this._s=td.getSizeFactor(x0, y0, i_width,i_height);
	}

	/**
	 * この関数は、パラメータ値を配列へ返します。
	 * o_factorには要素数{@link #NUM_OF_FACTOR}の
	 * @param o_factor
	 * 歪みパラメータ値の出力先配列。
	 */
	final public void getValue(double[] o_factor)
	{
		o_factor[0]=this._k1;
		o_factor[1]=this._k2;
		o_factor[2]=this._p1;
		o_factor[3]=this._p2;
		o_factor[4]=this._fx;
		o_factor[5]=this._fy;
		o_factor[6]=this._x0;
		o_factor[7]=this._y0;
		o_factor[8]=this._s;
	}
	
	
	private double getSizeFactor(double x0,double y0, int xsize, int ysize)
	{

	    double  olen, ilen;
	    double  sf1;

	    double sf = 100.0;


	    olen = x0;
	    NyARDoublePoint2d itmp=new NyARDoublePoint2d();
	    this.observ2Ideal(0, y0,itmp);
	    ilen = x0 - itmp.x;
	    //printf("Olen = %f, Ilen = %f, s = %f\n", olen, ilen, ilen / olen);
	    if( ilen > 0 ) {
	        sf1 = ilen / olen;
	        if( sf1 < sf ) sf = sf1;
	    }


	    olen = xsize - x0;
	    this.observ2Ideal(xsize, y0,itmp);
	    ilen = itmp.x - x0;
	    //printf("Olen = %f, Ilen = %f, s = %f\n", olen, ilen, ilen / olen);
	    if( ilen > 0 ) {
	        sf1 = ilen / olen;
	        if( sf1 < sf ) sf = sf1;
	    }


	    olen = y0;
	    this.observ2Ideal(x0,0,itmp);
	    ilen = y0 - itmp.y;
	    //printf("Olen = %f, Ilen = %f, s = %f\n", olen, ilen, ilen / olen);
	    if( ilen > 0 ) {
	        sf1 = ilen / olen;
	        if( sf1 < sf ) sf = sf1;
	    }


	    olen = ysize - y0;
	    this.observ2Ideal(x0, ysize,itmp);
	    ilen = itmp.y - y0;
	    //printf("Olen = %f, Ilen = %f, s = %f\n", olen, ilen, ilen / olen);
	    if( ilen > 0 ) {
	        sf1 = ilen / olen;
	        if( sf1 < sf ) sf = sf1;
	    }



	    this.observ2Ideal(0, 0,itmp);
	    ilen = x0 - itmp.x;
	    olen = x0;
	    if( ilen > 0 ) {
	        sf1 = ilen / olen;
	        if( sf1 < sf ) sf = sf1;
	    }
	    ilen = y0 - itmp.y;
	    olen = y0;
	    if( ilen > 0 ) {
	        sf1 = ilen / olen;
	        if( sf1 < sf ) sf = sf1;
	    }


	    this.observ2Ideal(xsize, 0,itmp);
	    ilen = itmp.x - x0;
	    olen = xsize - x0;
	    //printf("Olen = %f, Ilen = %f, s = %f\n", olen, ilen, ilen / olen);
	    if( ilen > 0 ) {
	        sf1 = ilen / olen;
	        if( sf1 < sf ) sf = sf1;
	    }
	    ilen = y0 - itmp.y;
	    olen = y0;
	    //printf("Olen = %f, Ilen = %f, s = %f\n", olen, ilen, ilen / olen);
	    if( ilen > 0 ) {
	        sf1 = ilen / olen;
	        if( sf1 < sf ) sf = sf1;
	    }


	    this.observ2Ideal(0, ysize,itmp);
	    ilen = x0 - itmp.x;
	    olen = x0;
	    //printf("Olen = %f, Ilen = %f, s = %f\n", olen, ilen, ilen / olen);
	    if( ilen > 0 ) {
	        sf1 = ilen / olen;
	        if( sf1 < sf ) sf = sf1;
	    }
	    ilen = itmp.y - y0;
	    olen = ysize - y0;
	    //printf("Olen = %f, Ilen = %f, s = %f\n", olen, ilen, ilen / olen);
	    if( ilen > 0 ) {
	        sf1 = ilen / olen;
	        if( sf1 < sf ) sf = sf1;
	    }

	    this.observ2Ideal(xsize, ysize,itmp);
	    ilen = itmp.x - x0;
	    olen = xsize - x0;
	    //printf("Olen = %f, Ilen = %f, s = %f\n", olen, ilen, ilen / olen);
	    if( ilen > 0 ) {
	        sf1 = ilen / olen;
	        if( sf1 < sf ) sf = sf1;
	    }
	    ilen = itmp.y - y0;
	    olen = ysize - y0;
	    //printf("Olen = %f, Ilen = %f, s = %f\n", olen, ilen, ilen / olen);
	    if( ilen > 0 ) {
	        sf1 = ilen / olen;
	        if( sf1 < sf ){
	        	sf = sf1;
	        }
	    }

	    if( sf == 100.0 ){
	    	sf = 1.0;
	    }

	    return sf;
	}

	public double getS()
	{
		return this._s;
	}






	
	/**
	 * この関数は、座標点を理想座標系から観察座標系へ変換します。
	 * @param i_in
	 * 変換元の座標
	 * @param o_out
	 * 変換後の座標を受け取るオブジェクト
	 */
	@Override
	public void ideal2Observ(double i_x,double i_y, NyARDoublePoint2d o_out)
	{
	    double k1 = this._k1;
	    double k2 = this._k2;
	    double p1 = this._p1;
	    double p2 = this._p2;
	    double fx = this._fx;
	    double fy = this._fy;
	    double x0 = this._x0;
	    double y0 = this._y0;
	    double s  = this._s;
	  
	    double x = (i_x - x0)*s/fx;
	    double y = (i_y - y0)*s/fy;
	    double l = x*x + y*y;
	    o_out.x = (x*(1.0+k1*l+k2*l*l)+2.0*p1*x*y+p2*(l+2.0*x*x))*fx+x0;
	    o_out.y = (y*(1.0+k1*l+k2*l*l)+p1*(l+2.0*y*y)+2.0*p2*x*y)*fy+y0;
	    return;
	}
	

	@Override
	public void ideal2Observ(double i_x,double i_y, NyARIntPoint2d o_out)
	{
	    double k1 = this._k1;
	    double k2 = this._k2;
	    double p1 = this._p1;
	    double p2 = this._p2;
	    double fx = this._fx;
	    double fy = this._fy;
	    double x0 = this._x0;
	    double y0 = this._y0;
	    double s  = this._s;
	  
	    double x = (i_x - x0)*s/fx;
	    double y = (i_y - y0)*s/fy;
	    double l = x*x + y*y;
	    o_out.x =(int)((x*(1.0+k1*l+k2*l*l)+2.0*p1*x*y+p2*(l+2.0*x*x))*fx+x0);
	    o_out.y =(int)((y*(1.0+k1*l+k2*l*l)+p1*(l+2.0*y*y)+2.0*p2*x*y)*fy+y0);
	    return;
	}
	

	private final static int PD_LOOP2=4;
	@Override
	public final void observ2Ideal(int ix, int iy, NyARDoublePoint2d o_point)
	{
		this.observ2Ideal((double)ix,(double)iy, o_point);
		return;
	}		
	/**
	 * この関数は、座標を観察座標系から理想座標系へ変換します。
	 * @param ix
	 * 変換元の座標
	 * @param iy
	 * 変換元の座標
	 * @param o_point
	 * 変換後の座標を受け取るオブジェクト
	 * @todo should optimize!
	 */
	@Override
	public void observ2Ideal(double ix, double iy, NyARDoublePoint2d o_point)
	{
	    // OpenCV distortion model, with addition of a scale factor so that
	    // entire image fits onscreen.

	    double k1 =this._k1;
	    double k2 =this._k2;
	    double p1 =this._p1;
	    double p2 =this._p2;
	    double fx =this._fx;
	    double fy =this._fy;
	    double x0 =this._x0;
	    double y0 =this._y0;

	    double px = (ix - x0)/fx;
	    double py = (iy - y0)/fy;

	    double x02 = px*px;
	    double y02 = py*py;
	  
	    for(int i = 1; ; i++ ) {
	        if( x02 != 0.0 || y02 != 0.0 ) {
	            px = px - ((1.0 + k1*(x02+y02) + k2*(x02+y02)*(x02+y02))*px + 2.0*p1*px*py + p2*(x02 + y02 + 2.0*x02)-((ix - x0)/fx))/(1.0+k1*(3.0*x02+y02)+k2*(5.0*x02*x02+3.0*x02*y02+y02*y02)+2.0*p1*py+6.0*p2*px);
	            py = py - ((1.0 + k1*(x02+y02) + k2*(x02+y02)*(x02+y02))*py + p1*(x02 + y02 + 2.0*y02) + 2.0*p2*px*py-((iy - y0)/fy))/(1.0+k1*(x02+3.0*y02)+k2*(x02*x02+3.0*x02*y02+5.0*y02*y02)+6.0*p1*py+2.0*p2*px);
	        }
	        else {
	          px = 0.0;
	          py = 0.0;
	          break;
	        }
	        if( i == PD_LOOP2 ){
	        	break;
	        }
	    
	        x02 = px*px;
	        y02 = py*py;
	    }
	  
	    o_point.x = px*fx/this._s + x0;
	    o_point.y = py*fy/this._s + y0;

	    return;		
	}
	
	/**
	 * この関数は、複数の座標点を、一括して理想座標系から観察座標系へ変換します。
	 * i_inとo_outには、同じインスタンスを指定できます。
	 * @param i_in
	 * 変換元の座標配列
	 * @param o_out
	 * 変換後の座標を受け取る配列
	 * @param i_size
	 * 変換する座標の個数。
	 * @todo should optimize!
	 */
	@Override
	final public void ideal2ObservBatch(NyARDoublePoint2d[] i_in, NyARDoublePoint2d[] o_out, int i_size)
	{
	    double k1 = this._k1;
	    double k2 = this._k2;
	    double p1 = this._p1;
	    double p2 = this._p2;
	    double fx = this._fx;
	    double fy = this._fy;
	    double x0 = this._x0;
	    double y0 = this._y0;
	    double s  = this._s;
	  
		for (int i = 0; i < i_size; i++) {
		    double x = (i_in[i].x - x0)*s/fx;
		    double y = (i_in[i].y - y0)*s/fy;
		    double l = x*x + y*y;
		    o_out[i].x =((x*(1.0+k1*l+k2*l*l)+2.0*p1*x*y+p2*(l+2.0*x*x))*fx+x0);
		    o_out[i].y =((y*(1.0+k1*l+k2*l*l)+p1*(l+2.0*y*y)+2.0*p2*x*y)*fy+y0);
		}
		return;
	}


	/**
	 * この関数は、複数の座標点を、一括して理想座標系から観察座標系へ変換します。
	 * i_inとo_outには、同じインスタンスを指定できます。
	 * @param i_in
	 * 変換元の座標配列
	 * @param o_out
	 * 変換後の座標を受け取る配列
	 * @param i_size
	 * 変換する座標の個数。
	 * @todo should optimize!
	 */
	@Override
	final public void ideal2ObservBatch(NyARDoublePoint2d[] i_in, NyARIntPoint2d[] o_out, int i_size)
	{
	    double k1 = this._k1;
	    double k2 = this._k2;
	    double p1 = this._p1;
	    double p2 = this._p2;
	    double fx = this._fx;
	    double fy = this._fy;
	    double x0 = this._x0;
	    double y0 = this._y0;
	    double s  = this._s;
	  
		for (int i = 0; i < i_size; i++) {
		    double x = (i_in[i].x - x0)*s/fx;
		    double y = (i_in[i].y - y0)*s/fy;
		    double l = x*x + y*y;
		    o_out[i].x =(int)((x*(1.0+k1*l+k2*l*l)+2.0*p1*x*y+p2*(l+2.0*x*x))*fx+x0);
		    o_out[i].y =(int)((y*(1.0+k1*l+k2*l*l)+p1*(l+2.0*y*y)+2.0*p2*x*y)*fy+y0);
		}
		return;
	}	
	
}
