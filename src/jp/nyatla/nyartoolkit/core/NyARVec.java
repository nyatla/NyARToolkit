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



public class NyARVec
{
    private int clm;
    private NyARVec(double[] i_array)
    {
        v=i_array;
        clm=v.length;
    }
    public NyARVec(int i_clm)
    {
        v=new double[i_clm];
        clm=i_clm;
    }
    private double[] v;
    public int getClm()
    {
        return clm;
    }
    public double[] getArray()
    {
        return v;
    }
    public static NyARVec wrap(double[] i_array)
    {
    	return new NyARVec(i_array);
    }
    /**
     * arVecDispの代替品
     * @param v
     * @return
     */
    public int arVecDisp() throws NyARException
    {
	NyARException.trap("未チェックのパス");
	System.out.println(" === vector ("+clm+") ===\n");//printf(" === vector (%d) ===\n", v->clm);
        System.out.print(" |");//printf(" |");
        for(int c = 0; c < clm; c++ ){//for( c = 0; c < v->clm; c++ ){
        	System.out.print(" "+v[c]);//printf( " %10g", v->v[c] );
        }
        System.out.println(" |");//printf(" |\n");
        System.out.println(" ===================");//printf(" ===================\n");
        return 0;
    }
    /**
     * arVecInnerproduct関数の代替品
     * @param x
     * @param y
     * @param i_start
     * 演算開始列(よくわからないけどarVecTridiagonalizeの呼び出し元でなんかしてる)
     * @return
     * @throws NyARException
     */
    private static double vecInnerproduct(NyARVec x,NyARVec y,int i_start) throws NyARException
    {
        NyARException.trap("この関数は動作確認できていません。");    
        double result = 0.0;
//        double[] x_array=x.v;.getArray();
//        double[] y_array=y.getArray();
    
        if(x.clm!= y.clm){
            throw new NyARException();//exit();
        }
        for(int i = i_start; i < x.clm; i++ ) {
            NyARException.trap("未チェックのパス");
            result += x.v[i] * y.v[i];//result += x->v[i] * y->v[i];
        }    
        return result;
    }
    /**
     * double arVecHousehold関数の代替品
     * @param x
     * @param i_start
     * 演算開始列(よくわからないけどarVecTridiagonalizeの呼び出し元でなんかしてる)
     * @return
     * @throws NyARException
     */
    private static double vecHousehold(NyARVec x,int i_start) throws NyARException
    {
        NyARException.trap("この関数は動作確認できていません。");
        double s, t;
        s = Math.sqrt(vecInnerproduct(x,x,i_start));
//        double[] x_array=x.getArray();
        if( s != 0.0 ){
            NyARException.trap("未チェックのパス");
            if(x.v[i_start]< 0){
        	s = -s;
            }
            NyARException.trap("未チェックのパス");{
            x.v[i_start]+=s;//x->v[0] += s;
            t = 1 / Math.sqrt(x.v[i_start]* s);//t = 1 / sqrt(x->v[0] * s);
    	    }
            for(int i = i_start; i < x.clm; i++){
                NyARException.trap("未チェックのパス");
                x.v[i]*=t;//x->v[i] *= t;
            }
        }
        return -s;
    }
    /**
     * arVecTridiagonalize関数の代替品
     * a,d,e間で演算をしてる。何をどうしているかはさっぱりさっぱり
     * @param a
     * @param d
     * @param e
     * @param i_e_start
     * 演算開始列(よくわからないけどarVecTridiagonalizeの呼び出し元でなんかしてる)
     * @return
     * @throws NyARException
     */
    public static void vecTridiagonalize(NyARMat a, NyARVec d, NyARVec e,int i_e_start) throws NyARException
    {
	NyARVec vec,vec2;
	double[][] a_array=a.getArray();
	double  s, t, p, q;
	int     dim;

	if(a.getClm()!=a.getRow()){
	    throw new NyARException();
	}
	if(a.getClm() != d.clm){
	    throw new NyARException();
	}
	if(a.getClm() != e.clm){
	    throw new NyARException();
	}
	dim = a.getClm();

	for(int k = 0; k < dim-2; k++ ){
	    vec=a.getRowVec(k);
//	    double[] vec_array=vec.getArray();
	    NyARException.trap("未チェックパス"); 
	    d.v[k]=vec.v[k];//d.set(k,v.get(k));    //d->v[k] = v[k];

	    //wv1.clm = dim-k-1;
	    //wv1.v = &(v[k+1]);
	    NyARException.trap("未チェックパス"); 
	    e.v[k+i_e_start]=vecHousehold(vec,k+1);//e->v[k] = arVecHousehold(&wv1);
	    if(e.v[k+i_e_start]== 0.0 ){
        	continue;
	    }

	    for(int i = k+1; i < dim; i++ ){
		s = 0.0;
		for(int j = k+1; j < i; j++ ) {
		    NyARException.trap("未チェックのパス");
		    s += a_array[j][i] * vec.v[j];//s += a.get(j*dim+i) * v.get(j);//s += a->m[j*dim+i] * v[j];
		}
		for(int j = i; j < dim; j++ ) {
		    NyARException.trap("未チェックのパス");
		    s += a_array[i][j] * vec.v[j];//s += a.get(i*dim+j) * v.get(j);//s += a->m[i*dim+j] * v[j];
		}
		NyARException.trap("未チェックのパス");
		d.v[i]=s;//d->v[i] = s;
	    }
        

	    //wv1.clm = wv2.clm = dim-k-1;
	    //wv1.v = &(v[k+1]);
	    //wv2.v = &(d->v[k+1]);
	    vec=a.getRowVec(k);
//	    vec_array=vec.getArray();
	    NyARException.trap("未チェックパス"); 
	    t = vecInnerproduct(vec,d,k+1)/ 2;
	    for(int i = dim-1; i > k; i-- ) {
		NyARException.trap("未チェックパス"); 
		p = vec.v[i];//p = v.get(i);//p = v[i];
		d.v[i]-=t*p;q=d.v[i];//q = d->v[i] -= t*p；
		for(int j = i; j < dim; j++ ){
		    NyARException.trap("未チェックパス"); 
		    a_array[i][j]-=p*(d.v[j] + q*vec.v[j]);//a->m[i*dim+j] -= p*(d->v[j]) + q*v[j];
		}
	    }
	}

	if( dim >= 2) {
	    d.v[dim-2]=a_array[dim-2][dim-2];//d->v[dim-2] = a->m[(dim-2)*dim+(dim-2)];
	    e.v[dim-2+i_e_start]=a_array[dim-2][dim-1];//e->v[dim-2] = a->m[(dim-2)*dim+(dim-1)];
	}

	if( dim >= 1 ){
	    d.v[dim-1]=a_array[dim-1][dim-1];//d->v[dim-1] = a->m[(dim-1)*dim+(dim-1)];
	}

	for(int k = dim-1; k >= 0; k--) {
	    vec=a.getRowVec(k);//v = a.getPointer(k*dim);//v = &(a->m[k*dim]);
	    if( k < dim-2 ) {
		for(int i = k+1; i < dim; i++ ){
		    //wv1.clm = wv2.clm = dim-k-1;
		    //wv1.v = &(v[k+1]);
		    //wv2.v = &(a->m[i*dim+k+1]);
		    vec2=a.getRowVec(i);
		    
		    t = vecInnerproduct(vec,vec2,k+1);
		    for(int j = k+1; j < dim; j++ ){
			NyARException.trap("未チェックパス"); 
			a_array[i][j]-=t*vec.v[j];//a.subValue(i*dim+j,t*v.get(j));//a->m[i*dim+j] -= t * v[j];
		    }
		}
	    }
	    for(int i = 0; i < dim; i++ ){
		vec.v[i]=0.0;//v.set(i,0.0);//v[i] = 0.0;
	    }
	    vec.v[k]=1;//v.set(k,1);//v[k] = 1;
	}
    }
}
