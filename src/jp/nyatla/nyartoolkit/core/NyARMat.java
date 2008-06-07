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

 

/**
 * 	ARMat構造体に対応するクラス
 *	typedef struct {
 *		double *m;
 *		int row;
 *		int clm;
 *	}ARMat;
 *
 */
public class NyARMat{
    private double[][] m;
    private int clm,row;
    public NyARMat(int i_row,int i_clm)
    {
	m=new double[i_row][i_clm];
	clm=i_clm;
	row=i_row;
    }
    public int getClm()
    {
	return clm;
    }
    public int getRow()
    {
	return row;
    }
    /**
     * 行列をゼロクリアする。
     */
    public void zeroClear()
    {
	for(int i=0;i<row;i++){
	    for(int i2=0;i2<clm;i2++){
		m[i][i2]=0.0;
	    }
	}
    }
    public double[] getRowArray(int i_row)
    {
	return m[i_row];
    }
    public double[][] getArray()
    {
	return m;
    }
    public NyARVec getRowVec(int i_row)
    {
	return NyARVec.wrap(m[i_row]);
    }
    /**
     * aとbの積をdestに出力する。arMatrixMul()の代替品
     * @param dest
     * @param a
     * @param b
     * @throws NyARException
     */
    public static void matrixMul(NyARMat dest, NyARMat a, NyARMat b) throws NyARException
    {
	if(a.clm != b.row || dest.row != a.row || dest.clm != b.clm){
	    throw new NyARException();
	}
	for(int r = 0; r < dest.row; r++){
	    for(int c = 0; c < dest.getClm(); c++){
		dest.m[r][c]=0.0;//dest.setARELEM0(r, c,0.0);
		for(int i = 0; i < a.getClm(); i++){
		    dest.m[r][c]+=a.m[r][i]*b.m[i][c];//ARELEM0(dest, r, c) += ARELEM0(a, r, i) * ARELEM0(b, i, c);
		}
	    }
	}
    }
    private int[] wk_nos_matrixSelfInv=new int[50];
    /**
     * i_targetを逆行列に変換する。arMatrixSelfInv()と、arMatrixSelfInv_minv()関数を合成してあります。
     * @param i_target
     * 逆行列にする行列
     * @throws NyARException
     */
    public void matrixSelfInv() throws NyARException
    {
	double[][] ap=m;
	int dimen=ap.length;
	double[] wcp,wap,wbp;
	int j,ip,nwork;
	int[] nos=wk_nos_matrixSelfInv;//この関数で初期化される。
	double epsl;
	double p,pbuf,work;

	epsl = 1.0e-10;         /* Threshold value      */
	/* check size */
	switch(dimen){
	case 0:
	    throw new NyARException();
	case 1:
	    ap[0][0]=1.0/ap[0][0];//*ap = 1.0 / (*ap);
	    return;/* 1 dimension */
	}

        for(int n = 0; n < dimen ; n++){
            nos[n] = n;
        }

        /* nyatla memo
         * ipが定まらないで計算が行われる場合があるので挿入。
         * ループ内で0初期化していいかが判らない。
         */
       	ip=0;
       	int wap_ptr,wbp_ptr;
        for(int n=0; n<dimen;n++)
        {
            wcp =ap[n];//wcp = ap + n * rowa;
            p=0.0;
            wap_ptr=0;//wap = DoublePointer.wrap(wcp);
            for(int i = n; i<dimen ; i++){//for(i = n, wap = wcp, p = 0.0; i < dimen ; i++, wap += rowa)
        	wap=ap[i];
        	if( p < ( pbuf = Math.abs(wap[0]))) {
        	    p = pbuf;
        	    ip = i;
        	}
            }
            if (p <= epsl){
                return;
            }

            nwork  = nos[ip];
            nos[ip]= nos[n];
            nos[n] = nwork;
            
            wap=ap[ip];
            wbp=wcp;
            wap_ptr=0;
            wbp_ptr=0;
            for(j=0; j< dimen ; j++){//for(j = 0, wap = ap + ip * rowa, wbp = wcp; j < dimen ; j++) {
            	work = wap[wap_ptr];               //work = *wap;
            	wap[wap_ptr]=wbp[wbp_ptr];wap_ptr++;//*wap++ = *wbp;
                wbp[wbp_ptr]=work;wbp_ptr++;     //*wbp++ = work;
            }
            
            wap=wcp;
            wap_ptr=0;
            work=wcp[0];
            for(j = 1; j < dimen ; j++){//for(j = 1, wap = wcp, work = *wcp; j < dimen ; j++, wap++)
            	wap[wap_ptr]=wap[wap_ptr+1]/work;//*wap = *(wap + 1) / work;
            	wap_ptr++;
            }
            wap[wap_ptr]=1.0/work;//*wap = 1.0 / work;

            for(int i = 0; i < dimen ; i++) {
                if(i != n) {
                    wap =ap[i];//wap = ap + i * rowa;
                    wbp =wcp;
                    wap_ptr=0;
                    wbp_ptr=0;
                    work=wap[0];
                    for(j = 1;j < dimen ; j++){//for(j = 1, wbp = wcp, work = *wap;j < dimen ; j++, wap++, wbp++)
                        wap[wap_ptr]=wap[wap_ptr+1]-work*wbp[wbp_ptr];//wap = *(wap + 1) - work * (*wbp);
                        wap_ptr++;
                        wbp_ptr++;
                    }
                    wap[wap_ptr]=-work*wbp[wbp_ptr];//*wap = -work * (*wbp);
                }
            }
        }

        for(int n = 0; n < dimen ; n++) {
            for(j = n; j < dimen ; j++){
        	if( nos[j] == n){
        	    break;
        	}
            }
            nos[j] = nos[n];
            for(int i = 0; i < dimen ;i++){//for(i = 0, wap = ap + j, wbp = ap + n; i < dimen ;i++, wap += rowa, wbp += rowa) {
        	wap=ap[i];
        	wbp=ap[i];
                work  =wap[j];//work = *wap;
                wap[j]=wbp[n];//*wap = *wbp;
                wbp[n]=work;//*wbp = work;
	    }
        }
        return;
    }
    /**
     * sourceの転置行列をdestに得る。arMatrixTrans()の代替品
     * @param dest
     * @param source
     * @return
     */
    public static void matrixTrans(NyARMat dest,NyARMat source) throws NyARException
    {
	if(dest.row != source.clm || dest.clm != source.row){
	    throw new NyARException();
	}
	NyARException.trap("未チェックのパス");
	for(int r=0;r< dest.row;r++){
	    for(int c=0;c<dest.clm;c++){
		dest.m[r][c]=source.m[c][r];
	    }
	}
    }
    /**
     * unitを単位行列に初期化する。arMatrixUnitの代替品
     * @param unit
     */
    public static void matrixUnit(NyARMat unit) throws NyARException
    {
	if(unit.row != unit.clm){
	    throw new NyARException();
	}
	NyARException.trap("未チェックのパス");
	for(int r = 0; r < unit.getRow(); r++) {
	    for(int c = 0; c < unit.getClm(); c++) {
		if(r == c) {
		    unit.m[r][c]=1.0;
		}else{
		    unit.m[r][c]=0.0;
		}
	    }
	}
    }
    /**
     * sourceの内容を自身にコピーする。
     * @param dest
     * @param source
     * @return
     */
    public void matrixDup(NyARMat source) throws NyARException
    {
	if(row != source.row || clm != source.clm)
	{
	    throw new NyARException();
	}
	
	for(int r = 0; r < row; r++){
	    for(int c = 0; c < clm; c++)
	    {
		m[r][c]=source.m[r][c];
	    }
	}
    }
    public NyARMat matrixAllocDup() throws NyARException
    {
	NyARMat result=new NyARMat(row,clm);
	result.matrixDup(this);
	return result;
    }    
    /**
     * arMatrixInv関数の代替品です。
     * destにsourceの逆行列を返します。
     * @param dest
     * @param source
     * @throws NyARException
     */
    public static void matrixInv(NyARMat dest,NyARMat source) throws NyARException
    {
	NyARException.trap("未チェックのパス");
	dest.matrixDup(source);

	NyARException.trap("未チェックのパス");
	dest.matrixSelfInv();
    }
    public NyARMat matrixAllocInv() throws NyARException
    {
	NyARException.trap("未チェックのパス");
	NyARMat result=matrixAllocDup();

	NyARException.trap("未チェックのパス");
	result.matrixSelfInv();
	return result;
    }
    /**
     * dim x dim の単位行列を作る。
     * @param dim
     * @return
     * @throws NyARException
     */
    public static NyARMat matrixAllocUnit(int dim) throws NyARException
    {
        NyARException.trap("未チェックのパス");
        NyARMat result = new NyARMat(dim, dim);
        NyARException.trap("未チェックのパス");
        NyARMat.matrixUnit(result);
        return result;
    }
    /**
     * arMatrixDispの代替品
     * @param m
     * @return
     */
    public int matrixDisp() throws NyARException
    {
	NyARException.trap("未チェックのパス");
	System.out.println(" === matrix ("+row+","+clm+") ===");//printf(" === matrix (%d,%d) ===\n", m->row, m->clm);
        for(int r = 0; r < row; r++){//for(int r = 0; r < m->row; r++) {
    	System.out.print(" |");//printf(" |");
    	for(int c = 0; c < clm; c++) {//for(int c = 0; c < m->clm; c++) {
    	    System.out.print(" "+m[r][c]);//printf(" %10g", ARELEM0(m, r, c));
    	}
    	System.out.println(" |");//printf(" |\n");
        }
        System.out.println(" ======================");//printf(" ======================\n");
        return 0;
    }
    private final static double	PCA_EPS=1e-6;		//#define     EPS             1e-6
    private final static int		PCA_MAX_ITER=100;	//#define     MAX_ITER        100
    private final static double	PCA_VZERO=1e-16;	//#define     VZERO           1e-16
    /**
     * static int EX( ARMat *input, ARVec *mean )の代替関数
     * @param input
     * @param mean
     * @return
     * @throws NyARException
     */
    private static void PCA_EX(NyARMat input, NyARVec mean) throws NyARException
    {
        double[] v;

        int     row, clm;
        
        row = input.row;
        clm = input.clm;
        if(row <= 0 || clm <= 0){
	    throw new NyARException();
        }
        if( mean.getClm() != clm ){
	    throw new NyARException();
        }
        double[] mean_array=mean.getArray();
        for(int i = 0; i < clm; i++ ){
            mean_array[i]=0.0;//mean->v[i] = 0.0;
        }
        
        v=mean.getArray();
        for(int i = 0; i < row; i++ ) {
            for(int j = 0; j < clm; j++ ){
                //*(v++) += *(m++);
                v[j]+=input.m[i][j];
            }
        }
        
        for(int i = 0; i < clm; i++ ){
        mean_array[i]/=row;//mean->v[i] /= row;
        }
    }
    /**
     * static int CENTER( ARMat *inout, ARVec *mean )の代替関数
     * @param inout
     * @param mean
     * @return
     */
    private static void PCA_CENTER(NyARMat inout, NyARVec mean) throws NyARException
    {
        double[] v;
        int     row, clm;
        
        row = inout.getRow();
        clm = inout.getClm();
        if(mean.getClm()!= clm){
	    throw new NyARException();
        }
        
        v = mean.getArray();
        for(int i = 0; i < row; i++ ) {
            for(int j = 0; j < clm; j++ ){
            	//*(m++) -= *(v++);
            	inout.m[i][j]-=v[j];
            }
        }
    }
    /**
     * int x_by_xt( ARMat *input, ARMat *output )の代替関数
     * @param input
     * @param output
     * @throws NyARException
     */
    private static void PCA_x_by_xt( NyARMat input, NyARMat output) throws NyARException
    {
	NyARException.trap("動作未チェック/配列化未チェック");
	int     row, clm;
//        double[][] out;
        double[] in1,in2;
        
        NyARException.trap("未チェックのパス");
        row = input.row;
        clm = input.clm;
        NyARException.trap("未チェックのパス");
        if( output.row != row || output.clm != row ){
	    throw new NyARException();
        }
	
//        out = output.getArray();
        for(int i = 0; i < row; i++ ) {
            for(int j = 0; j < row; j++ ) {
                if( j < i ) {
                    NyARException.trap("未チェックのパス");{
                    output.m[i][j]=output.m[j][i];//*out = output->m[j*row+i];
            	    }
                }else{
                    in1=input.getRowArray(i);//in1 = &(input->m[clm*i]);
                    in2=input.getRowArray(j);//in2 = &(input->m[clm*j]);
                    output.m[i][j]=0;//*out = 0.0;
                    for(int k = 0; k < clm; k++ ){
                        output.m[i][j]+=(in1[k]*in2[k]);//*out += *(in1++) * *(in2++);
                    }
                }
        //	            out.incPtr();
            }
        }
    }
    /**
     * static int xt_by_x( ARMat *input, ARMat *output )の代替関数
     * @param input
     * @param output
     * @throws NyARException
     */
    private static void PCA_xt_by_x(NyARMat input, NyARMat output) throws NyARException
    {
        double[] in;
        int     row, clm;
    
        row = input.row;
        clm = input.clm;
        if(output.row!= clm || output.clm != clm ){
	    throw new NyARException();
        }
    
        for(int i = 0; i < clm; i++ ) {
            for(int j = 0; j < clm; j++ ) {
                if( j < i ) {
                   output.m[i][j]=output.m[j][i];//*out = output->m[j*clm+i];
                }else{
                    output.m[i][j]=0.0;//*out = 0.0;
                    for(int k = 0; k < row; k++ ){
                        in=input.getRowArray(k);
                        output.m[i][j]+=(in[i]*in[j]);//*out += *in1 * *in2;
                    }
                }
            }
        }
    }
    /**
     * static int QRM( ARMat *a, ARVec *dv )の代替関数
     * @param a
     * @param dv
     * @throws NyARException
     */
    private static void PCA_QRM(NyARMat a, NyARVec dv) throws NyARException
    {
        double  w, t, s, x, y, c;
        int     dim, iter;
        double[] dv_array=dv.getArray();
        
        dim = a.row;
        if( dim != a.clm || dim < 2 ){
	    throw new NyARException();
        }
        if( dv.getClm() != dim ){
	    throw new NyARException();
        }
    
        NyARVec ev = new NyARVec( dim );
        double[] ev_array=ev.getArray();
        if( ev == null ){
	    throw new NyARException();
        }

        NyARVec.vecTridiagonalize(a,dv,ev,1);
    
        ev_array[0]=0.0;//ev->v[0] = 0.0;
        for(int h = dim-1; h > 0; h-- ) {
            int j = h;
            while(j>0 && Math.abs(ev_array[j]) > PCA_EPS*(Math.abs(dv_array[j-1])+Math.abs(dv_array[j]))){// while(j>0 && fabs(ev->v[j]) > EPS*(fabs(dv->v[j-1])+fabs(dv->v[j]))) j--;
                j--;
            }
            if( j == h ){
                continue;
            }
            iter = 0;
            do{
                iter++;
                if( iter > PCA_MAX_ITER ){
                	break;
                }
                w = (dv_array[h-1] - dv_array[h]) / 2;//w = (dv->v[h-1] - dv->v[h]) / 2;//ここ？
                t = ev_array[h] * ev_array[h];//t = ev->v[h] * ev->v[h];
                s = Math.sqrt(w*w+t);
                if( w < 0 ){
                	s = -s;
                }
                x=dv_array[j] - dv_array[h] + t/(w+s);//x = dv->v[j] - dv->v[h] + t/(w+s);
                y=ev_array[j+1];//y = ev->v[j+1];
                for(int k = j; k < h; k++ ){
                    if( Math.abs(x) >= Math.abs(y)){
                        if( Math.abs(x) > PCA_VZERO ) {
                    	t = -y / x;
                    	c = 1 / Math.sqrt(t*t+1);
                    	s = t * c;
                        }else{
                    	c = 1.0;
                    	s = 0.0;
                        }
                    }else{
                        t = -x / y;
                        s = 1.0 / Math.sqrt(t*t+1);
                        c = t * s;
                    }
                    w = dv_array[k] - dv_array[k+1];//w = dv->v[k] - dv->v[k+1];
                    t = (w * s + 2 * c * ev_array[k+1]) * s;//t = (w * s + 2 * c * ev->v[k+1]) * s;
                    dv_array[k]-=t;//dv->v[k]   -= t;
                    dv_array[k+1]+=t;//dv->v[k+1] += t;
                    if( k > j){
                        NyARException.trap("未チェックパス");{
                        ev_array[k]=c * ev_array[k] - s * y;//ev->v[k] = c * ev->v[k] - s * y;
                        }
                    }
                    ev_array[k+1]+=s * (c * w - 2 * s * ev_array[k+1]);//ev->v[k+1] += s * (c * w - 2 * s * ev->v[k+1]);
    
                    for(int i = 0; i < dim; i++ ){
                        x = a.m[k][i];//x = a->m[k*dim+i];
                        y = a.m[k+1][i];//y = a->m[(k+1)*dim+i];
                        a.m[k][i]=c * x - s * y;//a->m[k*dim+i]     = c * x - s * y;
                        a.m[k+1][i]=s * x + c * y;//a->m[(k+1)*dim+i] = s * x + c * y;
                    }
                    if( k < h-1 ) {
                        NyARException.trap("未チェックパス");{
                        x = ev_array[k+1];//x = ev->v[k+1];
                        y =-s*ev_array[k+2];//y = -s * ev->v[k+2];
                        ev_array[k+2]*=c;//ev->v[k+2] *= c;
                        }
                    }
                }
            }while(Math.abs(ev_array[h]) > PCA_EPS*(Math.abs(dv_array[h-1])+Math.abs(dv_array[h])));
        }
        double[] v1,v2;
        for(int k = 0; k < dim-1; k++ ) {
            int h = k;
            t=dv_array[h];//t = dv->v[h];
            for(int i = k+1; i < dim; i++ ){
                if(dv_array[i] > t ){//if( dv->v[i] > t ) {
                    h = i;
                    t=dv_array[h];//t = dv->v[h];
                }
            }
            dv_array[h]=dv_array[k];//dv->v[h] = dv->v[k];
            dv_array[k]=t;//dv->v[k] = t;
            v1=a.getRowArray(h);//v1 = &(a->m[h*dim]);
            v2=a.getRowArray(k);//v2 = &(a->m[k*dim]);
            for(int i = 0; i < dim; i++ ) {
                w=v1[i];//w = *v1;
                v1[i]=v2[i];//*v1 = *v2;
                v2[i]=w;//*v2 = w;
            }
        }
    }
    /**
     * static int EV_create( ARMat *input, ARMat *u, ARMat *output, ARVec *ev )の代替関数
     * @param input
     * @param u
     * @param output
     * @param ev
     * @throws NyARException
     */
    private static void PCA_EV_create(NyARMat input, NyARMat u, NyARMat output, NyARVec ev) throws NyARException
    {
        NyARException.trap("未チェックのパス");
        int     row, clm;
        row = input.row;//row = input->row;
        clm = input.clm;//clm = input->clm;
        if( row <= 0 || clm <= 0 ){
	    throw new NyARException();
       }
        if( u.row != row || u.clm != row ){//if( u->row != row || u->clm != row ){
	    throw new NyARException();
        }
        if( output.row != row || output.clm != clm ){//if( output->row != row || output->clm != clm ){
	    throw new NyARException();
        }
        if( ev.getClm()!= row ){//if( ev->clm != row ){
	    throw new NyARException();
        }
        double[][] m,in;
        double[]  m1,ev_array;
        double  sum, work;
    
        m =output.getArray();//m = output->m;
        in=input.getArray();
        int i;
        ev_array=ev.getArray();
        for(i = 0; i < row; i++ ) {
            NyARException.trap("未チェックのパス");
            if( ev_array[i]<PCA_VZERO ){//if( ev->v[i] < VZERO ){
        	break;
            }
            NyARException.trap("未チェックのパス");
            work = 1 / Math.sqrt(Math.abs(ev_array[i]));//work = 1 / sqrt(fabs(ev->v[i]));
            for(int j = 0; j < clm; j++ ) {
                sum = 0.0;
                m1=u.getRowArray(i);//m1 = &(u->m[i*row]);
    //	            m2=input.getPointer(j);//m2 = &(input->m[j]);
                for(int k = 0; k < row; k++ ) {
                    sum+=m1[k]+in[k][j];//sum += *m1 * *m2;
    //	                m1.incPtr();   //m1++;
    //	                m2.addPtr(clm);//m2 += clm;
                }
                m1[j]=sum * work;//*(m++) = sum * work;
    //	        {//*(m++) = sum * work;
    //	        m.set(sum * work);
    //	        m.incPtr();}
            }
        }
        for( ; i < row; i++ ) {
    	NyARException.trap("未チェックのパス");
            ev_array[i]=0.0;//ev->v[i] = 0.0;
            for(int j = 0; j < clm; j++ ){
                m[i][j]=0.0;
    //	        m.set(0.0);//*(m++) = 0.0;
    //	        m.incPtr();
            }
        }
    }
    /*static int PCA( ARMat *input, ARMat *output, ARVec *ev )*/
    private static void PCA_PCA(NyARMat input, NyARMat output, NyARVec ev) throws NyARException
    {
        NyARMat    u;
        int     row, clm, min;
        double[] ev_array=ev.getArray();

        row =input.row;//row = input->row;
        clm =input.clm;//clm = input->clm;
        min =(clm < row)? clm: row;
        if( row < 2 || clm < 2 ){
	    throw new NyARException();
        }
        if( output.clm != input.clm){//if( output->clm != input->clm ){
	    throw new NyARException();
        }
        if( output.row!= min ){//if( output->row != min ){
	    throw new NyARException();
        }
        if( ev.getClm() != min ){//if( ev->clm != min ){
	    throw new NyARException();
        }
        u =new NyARMat( min, min );
    
        if( row < clm ){
            NyARException.trap("未チェックのパス");
            PCA_x_by_xt( input, u );//if(x_by_xt( input, u ) < 0 ) {
        }else{
            PCA_xt_by_x( input, u );//if(xt_by_x( input, u ) < 0 ) {
        }
        PCA_QRM( u, ev );

        double[][] m1,m2;
        if( row < clm ) {
            NyARException.trap("未チェックのパス");{
            PCA_EV_create( input, u, output, ev );
            }
        }else{
            m1=u.m;//m1 = u->m;
            m2=output.m;//m2 = output->m;
            int i;
            for(i = 0; i < min; i++){
    	    	if( ev_array[i] < PCA_VZERO){//if( ev->v[i] < VZERO ){
    	    	    break;
    	    	}
    	    	for(int j = 0; j < min; j++ ){
    	    	    m2[i][j]=m1[i][j];//*(m2++) = *(m1++);
    	    	}
            }
            for( ; i < min; i++){//for( ; i < min; i++){
    	    	//コードを見た限りあってそうだからコメントアウト(2008/03/26)NyARException.trap("未チェックのパス");
    	    	ev_array[i]=0.0;//ev->v[i] = 0.0;
    	    	for(int j = 0; j < min; j++ ){
    	    	    m2[i][j]=0.0;//*(m2++) = 0.0;
    	    	}
            }
        }
    }
	
    /*int    arMatrixPCA( ARMat *input, ARMat *evec, ARVec *ev, ARVec *mean );*/
    public static void matrixPCA(NyARMat input, NyARMat evec, NyARVec ev, NyARVec mean) throws NyARException
    {
        NyARMat   work;
        double srow, sum;
        int     row, clm;
        int     check;
    
        row=input.row;//row = input->row;
        clm=input.clm;//clm = input->clm;
        check = (row < clm)? row: clm;
        if( row < 2 || clm < 2 ){
            throw new NyARException();
        }
        if( evec.clm != input.clm || evec.row != check ){//if( evec->clm != input->clm || evec->row != check ){
            throw new NyARException();
        }
        if( ev.getClm()   != check ){//if( ev->clm   != check ){
            throw new NyARException();
        }
        if( mean.getClm() != input.clm){//if( mean->clm != input->clm ){
            throw new NyARException();
        }
    
        work =input.matrixAllocDup();//arMatrixAllocDup( input );work = arMatrixAllocDup( input );
    
        srow = Math.sqrt((double)row);
        PCA_EX( work, mean );

        PCA_CENTER(work,mean);


        for(int i=0; i<row; i++){
            for(int j=0;j<clm;j++){
        	work.m[i][j]/=srow;//work->m[i] /= srow;
            }
        }
    
        PCA_PCA( work, evec, ev );
    
        sum = 0.0;
        double[] ev_array=ev.getArray();
        for(int i = 0; i < ev.getClm(); i++ ){//for(int i = 0; i < ev->clm; i++ ){
    		sum+=ev_array[i];//sum += ev->v[i];
        }
        for(int i = 0; i < ev.getClm(); i++ ){//for(int i = 0; i < ev->clm; i++ ){
    		ev_array[i]/=sum;//ev->v[i] /= sum;
        }
    }

    /*int    arMatrixPCA2( ARMat *input, ARMat *evec, ARVec *ev );*/
    public static void arMatrixPCA2( NyARMat input, NyARMat evec, NyARVec ev) throws NyARException
    {
	NyARException.trap("未チェックのパス");
	NyARMat   work;
	// double  srow; // unreferenced
	double  sum;
	int     row, clm;
	int     check;

        row=input.row;//row = input->row;
        clm=input.clm;//clm = input->clm;
        check = (row < clm)? row: clm;
        if( row < 2 || clm < 2 ){
            throw new NyARException();
        }
        if( evec.getClm()!= input.clm|| evec.row!=check){//if( evec->clm != input->clm || evec->row != check ){
            throw new NyARException();
        }
        if( ev.getClm() != check ){//if( ev->clm   != check ){
            throw new NyARException();
        }
        
	NyARException.trap("未チェックのパス");
	work =input.matrixAllocDup();

        NyARException.trap("未チェックパス");
        PCA_PCA( work, evec, ev );//rval = PCA( work, evec, ev );
        sum = 0.0;
        double[] ev_array=ev.getArray();
        for(int i = 0; i < ev.getClm(); i++ ){//for( i = 0; i < ev->clm; i++ ){
            NyARException.trap("未チェックパス");
            sum+=ev_array[i];//sum += ev->v[i];
        }
        for(int i = 0; i < ev.getClm(); i++ ){//for(int i = 0; i < ev->clm; i++ ){
            NyARException.trap("未チェックパス");
		ev_array[i]/=sum;//ev->v[i] /= sum;
        }
        return;
    }
    public static NyARMat matrixAllocMul(NyARMat a, NyARMat b) throws NyARException
    {
	NyARException.trap("未チェックのパス");
	NyARMat dest=new NyARMat(a.row, b.clm);
	NyARException.trap("未チェックのパス");
	matrixMul(dest, a, b);
	return dest;
    }
    /*static double mdet(double *ap, int dimen, int rowa)*/
    private static double Det_mdet(double[][] ap, int dimen, int rowa) throws NyARException
    {
        NyARException.trap("動作未チェック/配列化未チェック");
        double det = 1.0;
        double work;
        int    is = 0;
        int    mmax;
    
        for(int k = 0; k < dimen - 1; k++) {
            mmax = k;
            for(int i = k + 1; i < dimen; i++){
//         	if (Math.abs(arMatrixDet_MATRIX_get(ap, i, k, rowa)) > Math.abs(arMatrixDet_MATRIX_get(ap, mmax, k, rowa))){
                if (Math.abs(ap[i][k]) > Math.abs(ap[mmax][k])){
                    mmax = i;
                }
            }
            if(mmax != k) {
                for (int j = k; j < dimen; j++) {
                    work = ap[k][j];//work = MATRIX(ap, k, j, rowa);
                    ap[k][j]=ap[mmax][j];//MATRIX(ap, k, j, rowa) = MATRIX(ap, mmax, j, rowa);
                    ap[mmax][j]=work;//MATRIX(ap, mmax, j, rowa) = work;
                }
                is++;
            }
            for(int i = k + 1; i < dimen; i++) {
                work = ap[i][k]/ ap[k][k];//work = arMatrixDet_MATRIX_get(ap, i, k, rowa) / arMatrixDet_MATRIX_get(ap, k, k, rowa);
                for (int j = k + 1; j < dimen; j++){
                	//MATRIX(ap, i, j, rowa) -= work * MATRIX(ap, k, j, rowa);
                	ap[i][j]-=work * ap[k][j];
                }
            }
        }
        for(int i = 0; i < dimen; i++){
            det=ap[i][i];//det *= MATRIX(ap, i, i, rowa);
        }
        for(int i = 0; i < is; i++){ 
            det *= -1.0;
        }
        return det;
    }
    /*double arMatrixDet(ARMat *m);*/
    public static double arMatrixDet(NyARMat m) throws NyARException
    {
        NyARException.trap("動作未チェック/配列化未チェック");
        if(m.row != m.clm){
            return 0.0;
    	}
    	return Det_mdet(m.getArray(), m.row, m.clm);//return mdet(m->m, m->row, m->row);
    }
}