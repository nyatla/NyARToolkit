package jp.nyatla.nyartoolkit.core;

import jp.nyatla.nyartoolkit.NyARException;

interface NyARTransRot
{
    public double[] getArray();
    /**
     * 
     * @param trans
     * @param vertex
     * @param pos2d
     * [n*2]配列
     * @return
     * @throws NyARException
     */
    public double modifyMatrix(double trans[],double vertex[][], double pos2d[][]) throws NyARException;
    public void initRot(NyARSquare marker_info,int i_direction) throws NyARException;    
}

/**
 * NyARTransRot派生クラスで共通に使いそうな関数類をまとめたもの。
 * @author atla
 *
 */
abstract class NyARTransRot_OptimizeCommon implements NyARTransRot
{
    protected final int number_of_vertex;
    protected final double[] array=new double[9];
    protected final NyARParam cparam;
    public final double[] getArray()
    {
	return this.array;
    }
    /**
     * インスタンスを準備します。
     * @param i_param
     * nullを指定した場合、一部の関数が使用不能になります。
     */
    public NyARTransRot_OptimizeCommon(NyARParam i_param,int i_number_of_vertex) throws NyARException
    {
	number_of_vertex=i_number_of_vertex;
	cparam=i_param;
    }

    private final double[] wk_check_dir_world=new double[6];
    private final double[] wk_check_dir_camera=new double[4];
    private final NyARMat wk_check_dir_NyARMat=new NyARMat( 3, 3 );
    /**
     * static int check_dir( double dir[3], double st[2], double ed[2],double cpara[3][4] )
     * Optimize:STEP[526->468]
     * @param dir
     * @param st
     * @param ed
     * @param cpara
     * 
     * @throws NyARException
     */
    protected final void check_dir( double dir[], double st[], double ed[],double cpara[]) throws NyARException
    {
	double    h;
	int       i, j;

	NyARMat mat_a = this.wk_check_dir_NyARMat;//ここ、事前に初期化できそう
	double[][] a_array=mat_a.getArray();
	for(j=0;j<3;j++){
	    for(i=0;i<3;i++){
		a_array[j][i]=cpara[j*4+i];//m[j*3+i] = cpara[j][i];
	    }
	    
	}
	//	JartkException.trap("未チェックのパス");
	mat_a.matrixSelfInv();
	double[] world=wk_check_dir_world;//[2][3];
	//<Optimize>
	//world[0][0] = a_array[0][0]*st[0]*10.0+ a_array[0][1]*st[1]*10.0+ a_array[0][2]*10.0;//mat_a->m[0]*st[0]*10.0+ mat_a->m[1]*st[1]*10.0+ mat_a->m[2]*10.0;
	//world[0][1] = a_array[1][0]*st[0]*10.0+ a_array[1][1]*st[1]*10.0+ a_array[1][2]*10.0;//mat_a->m[3]*st[0]*10.0+ mat_a->m[4]*st[1]*10.0+ mat_a->m[5]*10.0;
	//world[0][2] = a_array[2][0]*st[0]*10.0+ a_array[2][1]*st[1]*10.0+ a_array[2][2]*10.0;//mat_a->m[6]*st[0]*10.0+ mat_a->m[7]*st[1]*10.0+ mat_a->m[8]*10.0;
	//world[1][0] = world[0][0] + dir[0];
	//world[1][1] = world[0][1] + dir[1];
	//world[1][2] = world[0][2] + dir[2];
	world[0] = a_array[0][0]*st[0]*10.0+ a_array[0][1]*st[1]*10.0+ a_array[0][2]*10.0;//mat_a->m[0]*st[0]*10.0+ mat_a->m[1]*st[1]*10.0+ mat_a->m[2]*10.0;
	world[1] = a_array[1][0]*st[0]*10.0+ a_array[1][1]*st[1]*10.0+ a_array[1][2]*10.0;//mat_a->m[3]*st[0]*10.0+ mat_a->m[4]*st[1]*10.0+ mat_a->m[5]*10.0;
	world[2] = a_array[2][0]*st[0]*10.0+ a_array[2][1]*st[1]*10.0+ a_array[2][2]*10.0;//mat_a->m[6]*st[0]*10.0+ mat_a->m[7]*st[1]*10.0+ mat_a->m[8]*10.0;
	world[3] = world[0] + dir[0];
	world[4] = world[1] + dir[1];
	world[5] = world[2] + dir[2];
	//</Optimize>

	double[] camera=wk_check_dir_camera;//[2][2];
	for( i = 0; i < 2; i++ ) {
	    h = cpara[2*4+0] * world[i*3+0]+ cpara[2*4+1] * world[i*3+1]+ cpara[2*4+2] * world[i*3+2];
	    if( h == 0.0 ){
		throw new NyARException();
	    }
	    camera[i*2+0] = (cpara[0*4+0] * world[i*3+0]+ cpara[0*4+1] * world[i*3+1]+ cpara[0*4+2] * world[i*3+2]) / h;
	    camera[i*2+1] = (cpara[1*4+0] * world[i*3+0]+ cpara[1*4+1] * world[i*3+1]+ cpara[1*4+2] * world[i*3+2]) / h;
	}
	//<Optimize>
	//v[0][0] = ed[0] - st[0];
	//v[0][1] = ed[1] - st[1];
	//v[1][0] = camera[1][0] - camera[0][0];
	//v[1][1] = camera[1][1] - camera[0][1];
	double v=(ed[0]-st[0])*(camera[2]-camera[0])+(ed[1]-st[1])*(camera[3]-camera[1]);
	//</Optimize>
	if(v<0) {//if( v[0][0]*v[1][0] + v[0][1]*v[1][1] < 0 ) {
	    dir[0] = -dir[0];
	    dir[1] = -dir[1];
	    dir[2] = -dir[2];
	}
    }
    /*int check_rotation( double rot[2][3] )*/
    protected final static void check_rotation( double rot[][] ) throws NyARException
    {
	double[]  v1=new double[3], v2=new double[3], v3=new double[3];
	double  ca, cb, k1, k2, k3, k4;
	double  a, b, c, d;
	double  p1, q1, r1;
	double  p2, q2, r2;
	double  p3, q3, r3;
	double  p4, q4, r4;
	double  w;
	double  e1, e2, e3, e4;
	int     f;

	v1[0] = rot[0][0];
	v1[1] = rot[0][1];
	v1[2] = rot[0][2];
	v2[0] = rot[1][0];
	v2[1] = rot[1][1];
	v2[2] = rot[1][2];
	v3[0] = v1[1]*v2[2] - v1[2]*v2[1];
	v3[1] = v1[2]*v2[0] - v1[0]*v2[2];
	v3[2] = v1[0]*v2[1] - v1[1]*v2[0];
	w = Math.sqrt( v3[0]*v3[0]+v3[1]*v3[1]+v3[2]*v3[2] );
	if( w == 0.0 ){
	    throw new NyARException();
	}
	v3[0] /= w;
	v3[1] /= w;
	v3[2] /= w;

	cb = v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2];
	if( cb < 0 ) cb *= -1.0;
	ca = (Math.sqrt(cb+1.0) + Math.sqrt(1.0-cb)) * 0.5;

	if( v3[1]*v1[0] - v1[1]*v3[0] != 0.0 ) {
	    f = 0;
	}
	else {
	    if( v3[2]*v1[0] - v1[2]*v3[0] != 0.0 ) {
		w = v1[1]; v1[1] = v1[2]; v1[2] = w;
		w = v3[1]; v3[1] = v3[2]; v3[2] = w;
		f = 1;
	    }
	    else {
		w = v1[0]; v1[0] = v1[2]; v1[2] = w;
		w = v3[0]; v3[0] = v3[2]; v3[2] = w;
		f = 2;
	    }
	}
	if( v3[1]*v1[0] - v1[1]*v3[0] == 0.0 ){
	    throw new NyARException();
	}
	k1 = (v1[1]*v3[2] - v3[1]*v1[2]) / (v3[1]*v1[0] - v1[1]*v3[0]);
	k2 = (v3[1] * ca) / (v3[1]*v1[0] - v1[1]*v3[0]);
	k3 = (v1[0]*v3[2] - v3[0]*v1[2]) / (v3[0]*v1[1] - v1[0]*v3[1]);
	k4 = (v3[0] * ca) / (v3[0]*v1[1] - v1[0]*v3[1]);

	a = k1*k1 + k3*k3 + 1;
	b = k1*k2 + k3*k4;
	c = k2*k2 + k4*k4 - 1;

	d = b*b - a*c;
	if( d < 0 ){
	    throw new NyARException();
	}
	r1 = (-b + Math.sqrt(d))/a;
	p1 = k1*r1 + k2;
	q1 = k3*r1 + k4;
	r2 = (-b - Math.sqrt(d))/a;
	p2 = k1*r2 + k2;
	q2 = k3*r2 + k4;
	if( f == 1 ) {
	    w = q1; q1 = r1; r1 = w;
	    w = q2; q2 = r2; r2 = w;
	    w = v1[1]; v1[1] = v1[2]; v1[2] = w;
	    w = v3[1]; v3[1] = v3[2]; v3[2] = w;
	    f = 0;
	}
	if( f == 2 ) {
	    w = p1; p1 = r1; r1 = w;
	    w = p2; p2 = r2; r2 = w;
	    w = v1[0]; v1[0] = v1[2]; v1[2] = w;
	    w = v3[0]; v3[0] = v3[2]; v3[2] = w;
	    f = 0;
	}

	if( v3[1]*v2[0] - v2[1]*v3[0] != 0.0 ) {
	    f = 0;
	}else {
	    if( v3[2]*v2[0] - v2[2]*v3[0] != 0.0 ) {
		w = v2[1]; v2[1] = v2[2]; v2[2] = w;
		w = v3[1]; v3[1] = v3[2]; v3[2] = w;
		f = 1;
	    }
	    else {
		w = v2[0]; v2[0] = v2[2]; v2[2] = w;
		w = v3[0]; v3[0] = v3[2]; v3[2] = w;
		f = 2;
	    }
	}
	if( v3[1]*v2[0] - v2[1]*v3[0] == 0.0 ){
	    throw new NyARException();
	}
	k1 = (v2[1]*v3[2] - v3[1]*v2[2]) / (v3[1]*v2[0] - v2[1]*v3[0]);
	k2 = (v3[1] * ca) / (v3[1]*v2[0] - v2[1]*v3[0]);
	k3 = (v2[0]*v3[2] - v3[0]*v2[2]) / (v3[0]*v2[1] - v2[0]*v3[1]);
	k4 = (v3[0] * ca) / (v3[0]*v2[1] - v2[0]*v3[1]);

	a = k1*k1 + k3*k3 + 1;
	b = k1*k2 + k3*k4;
	c = k2*k2 + k4*k4 - 1;

	d = b*b - a*c;
	if( d < 0 ){
	    throw new NyARException();
	}
	r3 = (-b + Math.sqrt(d))/a;
	p3 = k1*r3 + k2;
	q3 = k3*r3 + k4;
	r4 = (-b - Math.sqrt(d))/a;
	p4 = k1*r4 + k2;
	q4 = k3*r4 + k4;
	if( f == 1 ) {
	    w = q3; q3 = r3; r3 = w;
	    w = q4; q4 = r4; r4 = w;
	    w = v2[1]; v2[1] = v2[2]; v2[2] = w;
	    w = v3[1]; v3[1] = v3[2]; v3[2] = w;
	    f = 0;
	}
	if( f == 2 ) {
	    w = p3; p3 = r3; r3 = w;
	    w = p4; p4 = r4; r4 = w;
	    w = v2[0]; v2[0] = v2[2]; v2[2] = w;
	    w = v3[0]; v3[0] = v3[2]; v3[2] = w;
	    f = 0;
	}

	e1 = p1*p3+q1*q3+r1*r3;
	if( e1 < 0 ){
	    e1 = -e1;
	}
	e2 = p1*p4+q1*q4+r1*r4;
	if( e2 < 0 ){
	    e2 = -e2;
	}
	e3 = p2*p3+q2*q3+r2*r3;
	if( e3 < 0 ){
	    e3 = -e3;
	}
	e4 = p2*p4+q2*q4+r2*r4;
	if( e4 < 0 ){
	    e4 = -e4;
	}
	if( e1 < e2 ) {
	    if( e1 < e3 ) {
		if( e1 < e4 ) {
		    rot[0][0] = p1;
		    rot[0][1] = q1;
		    rot[0][2] = r1;
		    rot[1][0] = p3;
		    rot[1][1] = q3;
		    rot[1][2] = r3;
		}
		else {
		    rot[0][0] = p2;
		    rot[0][1] = q2;
		    rot[0][2] = r2;
		    rot[1][0] = p4;
		    rot[1][1] = q4;
		    rot[1][2] = r4;
		}
	    }
	    else {
		if( e3 < e4 ) {
		    rot[0][0] = p2;
		    rot[0][1] = q2;
		    rot[0][2] = r2;
		    rot[1][0] = p3;
		    rot[1][1] = q3;
		    rot[1][2] = r3;
		}
		else {
		    rot[0][0] = p2;
		    rot[0][1] = q2;
		    rot[0][2] = r2;
		    rot[1][0] = p4;
		    rot[1][1] = q4;
		    rot[1][2] = r4;
		}
	    }
	}
	else {
	    if( e2 < e3 ) {
		if( e2 < e4 ) {
		    rot[0][0] = p1;
		    rot[0][1] = q1;
		    rot[0][2] = r1;
		    rot[1][0] = p4;
		    rot[1][1] = q4;
		    rot[1][2] = r4;
		}
		else {
		    rot[0][0] = p2;
		    rot[0][1] = q2;
		    rot[0][2] = r2;
		    rot[1][0] = p4;
		    rot[1][1] = q4;
		    rot[1][2] = r4;
		}
	    }
	    else {
		if( e3 < e4 ) {
		    rot[0][0] = p2;
		    rot[0][1] = q2;
		    rot[0][2] = r2;
		    rot[1][0] = p3;
		    rot[1][1] = q3;
		    rot[1][2] = r3;
		}
		else {
		    rot[0][0] = p2;
		    rot[0][1] = q2;
		    rot[0][2] = r2;
		    rot[1][0] = p4;
		    rot[1][1] = q4;
		    rot[1][2] = r4;
		}
	    }
	}
    }  
    /**
     * パラメタa,b,cからrotを計算してインスタンスに保存する。
     * rotを1次元配列に変更
     * Optimize:2008.04.20:STEP[253→186]
     * @param a
     * @param b
     * @param c
     * @param o_rot
     */
    protected final static void arGetRot( double a, double b, double c,double[] o_rot)
    {
	double   sina, sinb, sinc;
	double   cosa, cosb, cosc;

	sina = Math.sin(a);
	cosa = Math.cos(a);
	sinb = Math.sin(b);
	cosb = Math.cos(b);
	sinc = Math.sin(c);
	cosc = Math.cos(c);
	//Optimize
	double CACA,SASA,SACA,SASB,CASB;
	CACA=cosa*cosa;
	SASA=sina*sina;
	SACA=sina*cosa;
	SASB=sina*sinb;
	CASB=cosa*sinb;

	o_rot[0] = CACA*cosb*cosc+SASA*cosc+SACA*cosb*sinc-SACA*sinc;
	o_rot[1] = -CACA*cosb*sinc-SASA*sinc+SACA*cosb*cosc-SACA*cosc;
	o_rot[2] = CASB;
	o_rot[3] = SACA*cosb*cosc-SACA*cosc+SASA*cosb*sinc+CACA*sinc;
	o_rot[4] = -SACA*cosb*sinc+SACA*sinc+SASA*cosb*cosc+CACA*cosc;
	o_rot[5] = SASB;
	o_rot[6] = -CASB*cosc-SASB*sinc;
	o_rot[7] = CASB*sinc-SASB*cosc;
	o_rot[8] = cosb;
    }
    /**
     * int arGetAngle( double rot[3][3], double *wa, double *wb, double *wc )
     * Optimize:2008.04.20:STEP[481→433]
     * @param rot
     * 2次元配列を1次元化してあります。
     * @param o_abc
     * @return
     */
    protected final int arGetAngle(double[] o_abc)
    {
	double      a, b, c,tmp;
	double      sina, cosa, sinb, cosb, sinc, cosc;
	double[] rot=array;
	if( rot[8] > 1.0 ) {//<Optimize/>if( rot[2][2] > 1.0 ) {
	    rot[8] = 1.0;//<Optimize/>rot[2][2] = 1.0;
	}else if( rot[8] < -1.0 ) {//<Optimize/>}else if( rot[2][2] < -1.0 ) {
	    rot[8] = -1.0;//<Optimize/>rot[2][2] = -1.0;
	}
	cosb = rot[8];//<Optimize/>cosb = rot[2][2];
	b = Math.acos( cosb );
	sinb = Math.sin( b );
	if( b >= 0.000001 || b <= -0.000001) {
	    cosa = rot[2] / sinb;//<Optimize/>cosa = rot[0][2] / sinb;
	    sina = rot[5] / sinb;//<Optimize/>sina = rot[1][2] / sinb;
	    if( cosa > 1.0 ) {
		/* printf("cos(alph) = %f\n", cosa); */
		cosa = 1.0;
		sina = 0.0;
	    }
	    if( cosa < -1.0 ) {
		/* printf("cos(alph) = %f\n", cosa); */
		cosa = -1.0;
		sina =  0.0;
	    }
	    if( sina > 1.0 ) {
		/* printf("sin(alph) = %f\n", sina); */
		sina = 1.0;
		cosa = 0.0;
	    }
	    if( sina < -1.0 ) {
		/* printf("sin(alph) = %f\n", sina); */
		sina = -1.0;
		cosa =  0.0;
	    }
	    a = Math.acos( cosa );
	    if( sina < 0 ){
		a = -a;
	    }
	    //<Optimize>
	    //sinc =  (rot[2][1]*rot[0][2]-rot[2][0]*rot[1][2])/ (rot[0][2]*rot[0][2]+rot[1][2]*rot[1][2]);
	    //cosc =  -(rot[0][2]*rot[2][0]+rot[1][2]*rot[2][1])/ (rot[0][2]*rot[0][2]+rot[1][2]*rot[1][2]);
	    tmp = (rot[2]*rot[2]+rot[5]*rot[5]);
	    sinc =  (rot[7]*rot[2]-rot[6]*rot[5])/ tmp;
	    cosc =  -(rot[2]*rot[6]+rot[5]*rot[7])/ tmp;
	    //</Optimize>

	    if( cosc > 1.0 ) {
		/* printf("cos(r) = %f\n", cosc); */
		cosc = 1.0;
		sinc = 0.0;
	    }
	    if( cosc < -1.0 ) {
		/* printf("cos(r) = %f\n", cosc); */
		cosc = -1.0;
		sinc =  0.0;
	    }
	    if( sinc > 1.0 ) {
		/* printf("sin(r) = %f\n", sinc); */
		sinc = 1.0;
		cosc = 0.0;
	    }
	    if( sinc < -1.0 ) {
		/* printf("sin(r) = %f\n", sinc); */
		sinc = -1.0;
		cosc =  0.0;
	    }
	    c = Math.acos( cosc );
	    if( sinc < 0 ){
		c = -c;
	    }
	}else {
	    a = b = 0.0;
	    cosa = cosb = 1.0;
	    sina = sinb = 0.0;
	    cosc = rot[0];//<Optimize/>cosc = rot[0][0];
	    sinc = rot[1];//<Optimize/>sinc = rot[1][0];
	    if( cosc > 1.0 ) {
		/* printf("cos(r) = %f\n", cosc); */
		cosc = 1.0;
		sinc = 0.0;
	    }
	    if( cosc < -1.0 ) {
		/* printf("cos(r) = %f\n", cosc); */
		cosc = -1.0;
		sinc =  0.0;
	    }
	    if( sinc > 1.0 ) {
		/* printf("sin(r) = %f\n", sinc); */
		sinc = 1.0;
		cosc = 0.0;
	    }
	    if( sinc < -1.0 ) {
		/* printf("sin(r) = %f\n", sinc); */
		sinc = -1.0;
		cosc =  0.0;
	    }
	    c = Math.acos( cosc );
	    if( sinc < 0 ){
		c = -c;
	    }
	}
	o_abc[0]=a;//wa.value=a;//*wa = a;
	o_abc[1]=b;//wb.value=b;//*wb = b;
	o_abc[2]=c;//wc.value=c;//*wc = c;
	return 0;
    }    
}

/**
 * NyARModifyMatrixの最適化バージョン1
 * 配列の１次元化、計算ステップの圧縮等の最適化をしてみた。
 *
 */
class NyARTransRot_O1 extends NyARTransRot_OptimizeCommon
{
    public NyARTransRot_O1(NyARParam i_param,int i_number_of_vertex) throws NyARException
    {
	super(i_param,i_number_of_vertex);
    }
    /**
     * int arGetInitRot( ARMarkerInfo *marker_info, double cpara[3][4], double rot[3][3] )
     * Optimize:2008.04.20:STEP[716→698]
     * @param marker_info
     * @param i_direction
     * @param i_param
     * @throws NyARException
     */
    public final void initRot(NyARSquare marker_info,int i_direction) throws NyARException
    {
	double cpara[]=	cparam.get34Array();
	double[][]  wdir=new double[3][3];
	double  w, w1, w2, w3;
	int     dir;
	int     j;

	dir = i_direction;

	for( j = 0; j < 2; j++ ) {
	    w1 = marker_info.line[(4-dir+j)%4][0] * marker_info.line[(6-dir+j)%4][1]- marker_info.line[(6-dir+j)%4][0] * marker_info.line[(4-dir+j)%4][1];
	    w2 = marker_info.line[(4-dir+j)%4][1] * marker_info.line[(6-dir+j)%4][2]- marker_info.line[(6-dir+j)%4][1] * marker_info.line[(4-dir+j)%4][2];
	    w3 = marker_info.line[(4-dir+j)%4][2] * marker_info.line[(6-dir+j)%4][0]- marker_info.line[(6-dir+j)%4][2] * marker_info.line[(4-dir+j)%4][0];

	    wdir[j][0] =  w1*(cpara[0*4+1]*cpara[1*4+2]-cpara[0*4+2]*cpara[1*4+1])+  w2*cpara[1*4+1]-  w3*cpara[0*4+1];
	    wdir[j][1] = -w1*cpara[0*4+0]*cpara[1*4+2]+  w3*cpara[0*4+0];
	    wdir[j][2] =  w1*cpara[0*4+0]*cpara[1*4+1];
	    w = Math.sqrt( wdir[j][0]*wdir[j][0]+ wdir[j][1]*wdir[j][1]+ wdir[j][2]*wdir[j][2] );
	    wdir[j][0] /= w;
	    wdir[j][1] /= w;
	    wdir[j][2] /= w;
	}

	//以下3ケースは、計算エラーのときは例外が発生する。
	check_dir(wdir[0], marker_info.sqvertex[(4-dir)%4],marker_info.sqvertex[(5-dir)%4], cpara);

	check_dir(wdir[1], marker_info.sqvertex[(7-dir)%4],marker_info.sqvertex[(4-dir)%4], cpara);

	check_rotation(wdir);


	wdir[2][0] = wdir[0][1]*wdir[1][2] - wdir[0][2]*wdir[1][1];
	wdir[2][1] = wdir[0][2]*wdir[1][0] - wdir[0][0]*wdir[1][2];
	wdir[2][2] = wdir[0][0]*wdir[1][1] - wdir[0][1]*wdir[1][0];
	w = Math.sqrt( wdir[2][0]*wdir[2][0]+ wdir[2][1]*wdir[2][1]+ wdir[2][2]*wdir[2][2] );
	wdir[2][0] /= w;
	wdir[2][1] /= w;
	wdir[2][2] /= w;
	/*
        if( wdir[2][2] < 0 ) {
            wdir[2][0] /= -w;
            wdir[2][1] /= -w;
            wdir[2][2] /= -w;
        }
        else {
            wdir[2][0] /= w;
            wdir[2][1] /= w;
            wdir[2][2] /= w;
        }
	 */
	//<Optimize>
	//rot[0][0] = wdir[0][0];
	//rot[1][0] = wdir[0][1];
	//rot[2][0] = wdir[0][2];
	//rot[0][1] = wdir[1][0];
	//rot[1][1] = wdir[1][1];
	//rot[2][1] = wdir[1][2];
	//rot[0][2] = wdir[2][0];
	//rot[1][2] = wdir[2][1];
	//rot[2][2] = wdir[2][2];
	double[] rot=this.array;
	rot[0] = wdir[0][0];
	rot[3] = wdir[0][1];
	rot[6] = wdir[0][2];
	rot[1] = wdir[1][0];
	rot[4] = wdir[1][1];
	rot[7] = wdir[1][2];
	rot[2] = wdir[2][0];
	rot[5] = wdir[2][1];
	rot[8] = wdir[2][2];
	//</Optimize>    
    }
    private final double[] wk_arModifyMatrix_combo=new double[12];//[3][4];
    private final double[] wk_arModifyMatrix_abc=new double[3];
    private final double[] wk_arModifyMatrix_rot=new double[9];
    /**
     * Optimize:2008.04.20:STEP[456→-]
     * @param rot
     * [3x3]配列
     * @param trans
     * @param vertex
     * @param pos2d
     * @param num
     * @return
     */
    public final double modifyMatrix(double trans[],double vertex[][], double pos2d[][]) throws NyARException
    {
	int num=this.number_of_vertex;
	double    factor;
	double    a1, b1, c1;
	double    a2, b2, c2;
	double    ma = 0.0, mb = 0.0, mc = 0.0;
	double    hx, hy, h, x, y;
	double    err, minerr=0;
	int       t1, t2, t3;
	int       s1 = 0, s2 = 0, s3 = 0;
	int       i, j;
	double[] combo=this.wk_arModifyMatrix_combo;//arGetNewMatrixで初期化されるので初期化不要//new double[3][4];
	double[] abc=wk_arModifyMatrix_abc;
	double[] rot=wk_arModifyMatrix_rot;

	arGetAngle(abc);//arGetAngle( rot, &a, &b, &c );
	a2 = abc[0];
	b2 = abc[1];
	c2 = abc[2];
	factor = 10.0*Math.PI/180.0;
	for( j = 0; j < 10; j++ ) {
	    minerr = 1000000000.0;
	    for(t1=-1;t1<=1;t1++) {
		for(t2=-1;t2<=1;t2++) {
		    for(t3=-1;t3<=1;t3++) {
                        a1 = a2 + factor*t1;
                        b1 = b2 + factor*t2;
			c1 = c2 + factor*t3;
			arGetRot( a1, b1, c1,rot);
			arGetNewMatrix(rot,trans, null, combo );
			err = 0.0;
			for( i = 0; i < num; i++ ) {
			    hx = combo[0] * vertex[i][0]+ combo[1] * vertex[i][1]+ combo[2] * vertex[i][2]+ combo[3];
			    hy = combo[4] * vertex[i][0]+ combo[5] * vertex[i][1]+ combo[6] * vertex[i][2]+ combo[7];
			    h  = combo[8] * vertex[i][0]+ combo[9] * vertex[i][1]+ combo[10] * vertex[i][2]+ combo[11];
			    x = hx / h;
			    y = hy / h;
			    err += (pos2d[i][0] - x) * (pos2d[i][0] - x)+ (pos2d[i][1] - y) * (pos2d[i][1] - y);
			}
			if( err < minerr ) {
			    minerr = err;
			    ma = a1;
			    mb = b1;
			    mc = c1;
			    s1 = t1;
			    s2 = t2;
			    s3 = t3;
			}
		    }
		}
	    }
	    if( s1 == 0 && s2 == 0 && s3 == 0 ){
		factor *= 0.5;
	    }
	    a2 = ma;
	    b2 = mb;
	    c2 = mc;
	}
	arGetRot(ma, mb, mc,this.array);
	/*  printf("factor = %10.5f\n", factor*180.0/MD_PI); */
	return minerr/num;
    }
    private final double[] wk_cpara2_arGetNewMatrix=new double[12];//[3][4];
    /**
     * Optimize:2008.04.20:STEP[569->432]
     * @param i_rot
     * [9]
     * @param trans
     * @param trans2
     * @param ret
     * double[3x4]配列
     * @return
     */
    private final int arGetNewMatrix(double[] i_rot,double trans[], double trans2[][], double ret[]) throws NyARException
    {
	final double cpara[]=cparam.get34Array();
	final double[] cpara2;	//この関数で初期化される。
	int j,j_idx;
//	double[] cpara_pt;
	//cparaの2次元配列→1次元に変換して計算
	if( trans2 != null ) {
	    cpara2=wk_cpara2_arGetNewMatrix;	//この関数で初期化される。

	    for( j = 0; j < 3; j++ ) {
//		Optimize(使わないから最適化してない)
		NyARException.trap("未チェックのパス");
		cpara2[j*4+0] = cpara[j*4+0] * trans2[0][0]+ cpara[j*4+1] * trans2[1][0]+ cpara[j*4+2] * trans2[2][0];
		cpara2[j*4+1] = cpara[j*4+0] * trans2[0][1]+ cpara[j*4+1] * trans2[1][1]+ cpara[j*4+2] * trans2[2][1];
		cpara2[j*4+2] = cpara[j*4+0] * trans2[0][2]+ cpara[j*4+1] * trans2[1][2]+ cpara[j*4+2] * trans2[2][2];
		cpara2[j*4+3] = cpara[j*4+0] * trans2[0][3]+ cpara[j*4+1] * trans2[1][3]+ cpara[j*4+2] * trans2[2][3];
	    }
	}else{
	    cpara2=cpara;//cparaの値をそのまま使う
	}
	for( j = 0; j < 3; j++ ) {
	    //cpara2_pt=cpara2[j];
	    j_idx=j*4;
	    //<Optimize>
	    //ret[j][0] = cpara2_pt[0] * rot[0][0]+ cpara2_pt[1] * rot[1][0]+ cpara2_pt[2] * rot[2][0];
	    //ret[j][1] = cpara2_pt[0] * rot[0][1]+ cpara2_pt[1] * rot[1][1]+ cpara2_pt[2] * rot[2][1];
	    //ret[j][2] = cpara2_pt[0] * rot[0][2]+ cpara2_pt[1] * rot[1][2]+ cpara2_pt[2] * rot[2][2];
	    //ret[j][3] = cpara2_pt[0] * trans[0]+ cpara2_pt[1] * trans[1]+ cpara2_pt[2] * trans[2]+ cpara2_pt[3];
	    ret[j_idx+0] = cpara2[j_idx+0] * i_rot[0]+ cpara2[j_idx+1] * i_rot[3]+ cpara2[j_idx+2] * i_rot[6];
	    ret[j_idx+1] = cpara2[j_idx+0] * i_rot[1]+ cpara2[j_idx+1] * i_rot[4]+ cpara2[j_idx+2] * i_rot[7];
	    ret[j_idx+2] = cpara2[j_idx+0] * i_rot[2]+ cpara2[j_idx+1] * i_rot[5]+ cpara2[j_idx+2] * i_rot[8];
	    ret[j_idx+3] = cpara2[j_idx+0] * trans[0]+ cpara2[j_idx+1] * trans[1]+ cpara2[j_idx+2] * trans[2]+ cpara2[j_idx+3];
	    //</Optimize>
	}
	return(0);
    }    
}

/**
 * NyARModifyMatrixの最適化バージョン2
 * 計算手順の変更、構造変更など含む最適化をしたもの
 *
 */
class NyARTransRot_O2 extends NyARTransRot_OptimizeCommon
{
    public NyARTransRot_O2(NyARParam i_param,int i_number_of_vertex) throws NyARException
    {
	super(i_param,i_number_of_vertex);
    }  
    
    //private double CACA,SASA,SACA,CA,SA;    
    private double CACA,SASA,SACA,CA,SA;
    final public void arGetRotA( double a)
    {
	double   sina,cosa;
	sina = Math.sin(a);
	cosa = Math.cos(a);
	//Optimize
	CACA=cosa*cosa;
	SASA=sina*sina;
	SACA=sina*cosa;
	CA=cosa;
	SA=sina;
    }
    private double CACACB,SACACB,SASACB,CASB,SASB;
    final public void arGetRotB(double b,double[] o_rot)
    {
	double   sinb,cosb;
	sinb = Math.sin(b);
	cosb = Math.cos(b);
	CACACB=CACA*cosb;
	SACACB=SACA*cosb;
	SASACB=SASA*cosb;
	CASB=CA*sinb;
	SASB=SA*sinb;
	o_rot[2] = CASB;
	o_rot[5] = SASB;
	o_rot[8] = cosb;
    }
    /**
     * 分割arGetRot
     * @param c
     */
    public final void arGetRotC(double c,double[] o_rot)
    {
	double   sinc,cosc;
	sinc = Math.sin(c);
	cosc = Math.cos(c);
	double SACASC,SACACBSC,SACACBCC,SACACC;
	SACASC=SACA*sinc;
	SACACC=SACA*cosc;
	SACACBSC=SACACB*sinc;
	SACACBCC=SACACB*cosc;
	o_rot[0] = CACACB*cosc+SASA*cosc+SACACBSC-SACASC;
	o_rot[1] = -CACACB*sinc-SASA*sinc+SACACBCC-SACACC;
	o_rot[3] = SACACBCC-SACACC+SASACB*sinc+CACA*sinc;
	o_rot[4] = -SACACBSC+SACASC+SASACB*cosc+CACA*cosc;
	o_rot[6] = -CASB*cosc-SASB*sinc;
	o_rot[7] = CASB*sinc-SASB*cosc;
    }
    private final double[][] wk_initRot_wdir=new double[3][3];
    /**
     * int arGetInitRot( ARMarkerInfo *marker_info, double cpara[3][4], double rot[3][3] )
     * Optimize:2008.04.20:STEP[716→698]
     * @param marker_info
     * @param i_direction
     * @param i_param
     * @throws NyARException
     */
    public void initRot(NyARSquare marker_info,int i_direction) throws NyARException
    {
	double cpara[]=	cparam.get34Array();
	double[][]  wdir=wk_initRot_wdir;//この関数で初期化される
	double  w, w1, w2, w3;
	int     dir;
	int     j;

	dir = i_direction;

	for( j = 0; j < 2; j++ ) {
	    w1 = marker_info.line[(4-dir+j)%4][0] * marker_info.line[(6-dir+j)%4][1]- marker_info.line[(6-dir+j)%4][0] * marker_info.line[(4-dir+j)%4][1];
	    w2 = marker_info.line[(4-dir+j)%4][1] * marker_info.line[(6-dir+j)%4][2]- marker_info.line[(6-dir+j)%4][1] * marker_info.line[(4-dir+j)%4][2];
	    w3 = marker_info.line[(4-dir+j)%4][2] * marker_info.line[(6-dir+j)%4][0]- marker_info.line[(6-dir+j)%4][2] * marker_info.line[(4-dir+j)%4][0];

	    wdir[j][0] =  w1*(cpara[0*4+1]*cpara[1*4+2]-cpara[0*4+2]*cpara[1*4+1])+  w2*cpara[1*4+1]-  w3*cpara[0*4+1];
	    wdir[j][1] = -w1*cpara[0*4+0]*cpara[1*4+2]+  w3*cpara[0*4+0];
	    wdir[j][2] =  w1*cpara[0*4+0]*cpara[1*4+1];
	    w = Math.sqrt( wdir[j][0]*wdir[j][0]+ wdir[j][1]*wdir[j][1]+ wdir[j][2]*wdir[j][2] );
	    wdir[j][0] /= w;
	    wdir[j][1] /= w;
	    wdir[j][2] /= w;
	}

	//以下3ケースは、計算エラーのときは例外が発生する。
	check_dir(wdir[0], marker_info.sqvertex[(4-dir)%4],marker_info.sqvertex[(5-dir)%4], cpara);

	check_dir(wdir[1], marker_info.sqvertex[(7-dir)%4],marker_info.sqvertex[(4-dir)%4], cpara);

	check_rotation(wdir);


	wdir[2][0] = wdir[0][1]*wdir[1][2] - wdir[0][2]*wdir[1][1];
	wdir[2][1] = wdir[0][2]*wdir[1][0] - wdir[0][0]*wdir[1][2];
	wdir[2][2] = wdir[0][0]*wdir[1][1] - wdir[0][1]*wdir[1][0];
	w = Math.sqrt( wdir[2][0]*wdir[2][0]+ wdir[2][1]*wdir[2][1]+ wdir[2][2]*wdir[2][2] );
	wdir[2][0] /= w;
	wdir[2][1] /= w;
	wdir[2][2] /= w;
	//<Optimize>
	//rot[0][0] = wdir[0][0];
	//rot[1][0] = wdir[0][1];
	//rot[2][0] = wdir[0][2];
	//rot[0][1] = wdir[1][0];
	//rot[1][1] = wdir[1][1];
	//rot[2][1] = wdir[1][2];
	//rot[0][2] = wdir[2][0];
	//rot[1][2] = wdir[2][1];
	//rot[2][2] = wdir[2][2];
	double[] rot=this.array;
	rot[0] = wdir[0][0];
	rot[3] = wdir[0][1];
	rot[6] = wdir[0][2];
	rot[1] = wdir[1][0];
	rot[4] = wdir[1][1];
	rot[7] = wdir[1][2];
	rot[2] = wdir[2][0];
	rot[5] = wdir[2][1];
	rot[8] = wdir[2][2];
	//</Optimize>    
    }
    private final double[] wk_arModifyMatrix_combo=new double[12];//[3][4];
    private final double[] wk_arModifyMatrix_abc=new double[3];
    private final double[] wk_arModifyMatrix_rot=new double[9];    
    /**
     * arGetRot計算を階層化したModifyMatrix
     * @param nyrot
     * @param trans
     * @param vertex
     * @param pos2d
     * @param num
     * @return
     * @throws NyARException
     */
    public double modifyMatrix(double trans[],double vertex[][], double pos2d[][]) throws NyARException
    {
	int num=this.number_of_vertex;
	double    factor;
	double    a1, b1, c1;
	double    a2, b2, c2;
	double    ma = 0.0, mb = 0.0, mc = 0.0;
	double    hx, hy, h, x, y;
	double    err, minerr=0;
	int       t1, t2, t3;
	int       s1 = 0, s2 = 0, s3 = 0;
	int       i, j;
	final double[] combo=this.wk_arModifyMatrix_combo;//arGetNewMatrixで初期化されるので初期化不要//new double[3][4];
	final double[] abc=wk_arModifyMatrix_abc;
	double[] rot=wk_arModifyMatrix_rot;

	arGetAngle(abc);//arGetAngle( rot, &a, &b, &c );
	a2 = abc[0];
	b2 = abc[1];
	c2 = abc[2];
	factor = 10.0*Math.PI/180.0;

	nyatla_arGetNewMatrix_row3(trans,combo);//comboの3行目を先に計算
	for( j = 0; j < 10; j++ ) {
	    minerr = 1000000000.0;
	    for(t1=-1;t1<=1;t1++) {
                a1 = a2 + factor*t1;
		arGetRotA(a1);
		for(t2=-1;t2<=1;t2++) {
                    b1 = b2 + factor*t2;
                    arGetRotB(b1,rot);
		    for(t3=-1;t3<=1;t3++) {
			c1 = c2 + factor*t3;
			arGetRotC(c1,rot);
			//comboの0-2行目を計算
			nyatla_arGetNewMatrix_row012(rot,trans,combo);//第二パラメタは常にnull//arGetNewMatrix(trans, null, combo );
			err = 0.0;
			for( i = 0; i < num; i++ ) {
			    hx = combo[0] * vertex[i][0]+ combo[1] * vertex[i][1]+ combo[2] * vertex[i][2]+ combo[3];
			    hy = combo[4] * vertex[i][0]+ combo[5] * vertex[i][1]+ combo[6] * vertex[i][2]+ combo[7];
			    h  = combo[8] * vertex[i][0]+ combo[9] * vertex[i][1]+ combo[10] * vertex[i][2]+ combo[11];
			    x = hx / h;
			    y = hy / h;
			    err += (pos2d[i][0] - x) * (pos2d[i][0] - x)+ (pos2d[i][1] - y) * (pos2d[i][1] - y);
			}
			if( err < minerr ) {
			    minerr = err;
			    ma = a1;
			    mb = b1;
			    mc = c1;
			    s1 = t1;
			    s2 = t2;
			    s3 = t3;
			}
		    }
		}
	    }
	    if( s1 == 0 && s2 == 0 && s3 == 0 ){
		factor *= 0.5;
	    }
	    a2 = ma;
	    b2 = mb;
	    c2 = mc;
	}
	arGetRot(ma,mb,mc,this.array);
	/*  printf("factor = %10.5f\n", factor*180.0/MD_PI); */
	return minerr/num;
    }
    /**
     * arGetNewMatrixの0-2行目を初期化する関数
     * Optimize:2008.04.20:STEP[569->144]
     * @param i_rot
     * @param trans
     * @param trans2
     * @param ret
     * double[3x4]配列
     * @return
     */
    private final void nyatla_arGetNewMatrix_row012(double i_rot[],double trans[],double ret[]) throws NyARException
    {
	int j;
	double c0,c1,c2;
	final double cpara2[]=cparam.get34Array();
	for( j = 0; j < 3; j++ ) {
	    //cpara2_pt=cpara2[j];
	    c0=cpara2[j*4+0];
	    c1=cpara2[j*4+1];
	    c2=cpara2[j*4+2];
	    ret[j*4+0] = c0 * i_rot[0]+ c1 * i_rot[3]+ c2 * i_rot[6];
	    ret[j*4+1] = c0 * i_rot[1]+ c1 * i_rot[4]+ c2 * i_rot[7];
	    ret[j*4+2] = c0 * i_rot[2]+ c1 * i_rot[5]+ c2 * i_rot[8];
	    //</Optimize>
	}
	return;
    }
    /**
     * arGetNewMatrixの3行目を初期化する関数
     * @param trans
     * @param ret
     * @throws NyARException
     */
    private final void nyatla_arGetNewMatrix_row3(double trans[],double ret[]) throws NyARException
    {
	final double cpara2[]=cparam.get34Array();
	int j,j_idx;
	for( j = 0; j < 3; j++ ) {
	    j_idx=j*4;
	    ret[j_idx+3] = cpara2[j_idx+0] * trans[0]+ cpara2[j_idx+1] * trans[1]+ cpara2[j_idx+2] * trans[2]+ cpara2[j_idx+3];
	}
	return;
    }          
}


/**
 * NyARModifyMatrixの最適化バージョン3
 * O3版の演算テーブル版
 * 計算速度のみを追求する
 *
 */
class NyARTransRot_O3 extends NyARTransRot_OptimizeCommon
{
    public NyARTransRot_O3(NyARParam i_param,int i_number_of_vertex) throws NyARException
    {
	super(i_param,i_number_of_vertex);
	if(i_number_of_vertex!=4){
	    //4以外の頂点数は処理しない
	    throw new NyARException();
	}
    }  
    
    //private double CACA,SASA,SACA,CA,SA;    
    private final double[][] wk_initRot_wdir=new double[3][3];
    /**
     * int arGetInitRot( ARMarkerInfo *marker_info, double cpara[3][4], double rot[3][3] )
     * Optimize:2008.04.20:STEP[716→698]
     * @param marker_info
     * @param i_direction
     * @param i_param
     * @throws NyARException
     */
    public void initRot(NyARSquare marker_info,int i_direction) throws NyARException
    {
	double cpara[]=	cparam.get34Array();
	double[][]  wdir=wk_initRot_wdir;//この関数で初期化される
	double  w, w1, w2, w3;
	int     dir;
	int     j;

	dir = i_direction;

	for( j = 0; j < 2; j++ ) {
	    w1 = marker_info.line[(4-dir+j)%4][0] * marker_info.line[(6-dir+j)%4][1]- marker_info.line[(6-dir+j)%4][0] * marker_info.line[(4-dir+j)%4][1];
	    w2 = marker_info.line[(4-dir+j)%4][1] * marker_info.line[(6-dir+j)%4][2]- marker_info.line[(6-dir+j)%4][1] * marker_info.line[(4-dir+j)%4][2];
	    w3 = marker_info.line[(4-dir+j)%4][2] * marker_info.line[(6-dir+j)%4][0]- marker_info.line[(6-dir+j)%4][2] * marker_info.line[(4-dir+j)%4][0];

	    wdir[j][0] =  w1*(cpara[0*4+1]*cpara[1*4+2]-cpara[0*4+2]*cpara[1*4+1])+  w2*cpara[1*4+1]-  w3*cpara[0*4+1];
	    wdir[j][1] = -w1*cpara[0*4+0]*cpara[1*4+2]+  w3*cpara[0*4+0];
	    wdir[j][2] =  w1*cpara[0*4+0]*cpara[1*4+1];
	    w = Math.sqrt( wdir[j][0]*wdir[j][0]+ wdir[j][1]*wdir[j][1]+ wdir[j][2]*wdir[j][2] );
	    wdir[j][0] /= w;
	    wdir[j][1] /= w;
	    wdir[j][2] /= w;
	}

	//以下3ケースは、計算エラーのときは例外が発生する。
	check_dir(wdir[0], marker_info.sqvertex[(4-dir)%4],marker_info.sqvertex[(5-dir)%4], cpara);

	check_dir(wdir[1], marker_info.sqvertex[(7-dir)%4],marker_info.sqvertex[(4-dir)%4], cpara);

	check_rotation(wdir);


	wdir[2][0] = wdir[0][1]*wdir[1][2] - wdir[0][2]*wdir[1][1];
	wdir[2][1] = wdir[0][2]*wdir[1][0] - wdir[0][0]*wdir[1][2];
	wdir[2][2] = wdir[0][0]*wdir[1][1] - wdir[0][1]*wdir[1][0];
	w = Math.sqrt( wdir[2][0]*wdir[2][0]+ wdir[2][1]*wdir[2][1]+ wdir[2][2]*wdir[2][2] );
	wdir[2][0] /= w;
	wdir[2][1] /= w;
	wdir[2][2] /= w;
	double[] rot=this.array;
	rot[0] = wdir[0][0];
	rot[3] = wdir[0][1];
	rot[6] = wdir[0][2];
	rot[1] = wdir[1][0];
	rot[4] = wdir[1][1];
	rot[7] = wdir[1][2];
	rot[2] = wdir[2][0];
	rot[5] = wdir[2][1];
	rot[8] = wdir[2][2];
	//</Optimize>    
    }
    private final double[][] wk_arModifyMatrix_double1D=new double[8][3];
    /**
     * arGetRot計算を階層化したModifyMatrix
     * 896
     * @param nyrot
     * @param trans
     * @param vertex
     * [m][3]
     * @param pos2d
     * [n][2]
     * @return
     * @throws NyARException
     */
    public double modifyMatrix(double trans[],double vertex[][], double pos2d[][]) throws NyARException
    {
	double    factor;
	double    a2, b2, c2;
	double    ma = 0.0, mb = 0.0, mc = 0.0;
	double    h, x, y;
	double    err, minerr=0;
	int       t1, t2, t3;
	int       s1 = 0, s2 = 0, s3 = 0;

	factor = 10.0*Math.PI/180.0;
	double rot0,rot1,rot3,rot4,rot6,rot7;
	double combo00,combo01,combo02,combo03,combo10,combo11,combo12,combo13,combo20,combo21,combo22,combo23;
	double combo02_2,combo02_5,combo02_8,combo02_11;
	double combo22_2,combo22_5,combo22_8,combo22_11;
	double combo12_2,combo12_5,combo12_8,combo12_11;
	//vertex展開
	final double VX00,VX01,VX02,VX10,VX11,VX12,VX20,VX21,VX22,VX30,VX31,VX32;
	double[] d_pt;
	d_pt=vertex[0];VX00=d_pt[0];VX01=d_pt[1];VX02=d_pt[2];
	d_pt=vertex[1];VX10=d_pt[0];VX11=d_pt[1];VX12=d_pt[2];
	d_pt=vertex[2];VX20=d_pt[0];VX21=d_pt[1];VX22=d_pt[2];
	d_pt=vertex[3];VX30=d_pt[0];VX31=d_pt[1];VX32=d_pt[2];
	final double P2D00,P2D01,P2D10,P2D11,P2D20,P2D21,P2D30,P2D31;
	d_pt=pos2d[0];P2D00=d_pt[0];P2D01=d_pt[1];
	d_pt=pos2d[1];P2D10=d_pt[0];P2D11=d_pt[1];
	d_pt=pos2d[2];P2D20=d_pt[0];P2D21=d_pt[1];
	d_pt=pos2d[3];P2D30=d_pt[0];P2D31=d_pt[1];
	final double cpara[]=cparam.get34Array();
	final double CP0,CP1,CP2,CP3,CP4,CP5,CP6,CP7,CP8,CP9,CP10;
	CP0=cpara[0];CP1=cpara[1];CP2=cpara[2];CP3=cpara[3];
	CP4=cpara[4];CP5=cpara[5];CP6=cpara[6];CP7=cpara[7];
	CP8=cpara[8];CP9=cpara[9];CP10=cpara[10];
	combo03 = CP0 * trans[0]+ CP1 * trans[1]+ CP2 * trans[2]+ CP3;
	combo13 = CP4 * trans[0]+ CP5 * trans[1]+ CP6 * trans[2]+ CP7;
	combo23 = CP8 * trans[0]+ CP9 * trans[1]+ CP10 * trans[2]+ cpara[11];
	double CACA,SASA,SACA,CA,SA;
	double CACACB,SACACB,SASACB,CASB,SASB;
	double SACASC,SACACBSC,SACACBCC,SACACC;        
	final double[][] double1D=this.wk_arModifyMatrix_double1D;

	final double[] abc     =double1D[0];
	final double[] a_factor=double1D[1];
	final double[] sinb    =double1D[2];
	final double[] cosb    =double1D[3];
	final double[] b_factor=double1D[4];
	final double[] sinc    =double1D[5];
	final double[] cosc    =double1D[6];
	final double[] c_factor=double1D[7];
	double w,w2;
	double wsin,wcos;

	arGetAngle(abc);//arGetAngle( rot, &a, &b, &c );
	a2 = abc[0];
	b2 = abc[1];
	c2 = abc[2];
	
	//comboの3行目を先に計算
	for(int i = 0; i < 10; i++ ) {
	    minerr = 1000000000.0;
	    //sin-cosテーブルを計算(これが外に出せるとは…。)
	    for(int j=0;j<3;j++){
		w2=factor*(j-1);
		w= a2 + w2;
		a_factor[j]=w;
		w= b2 + w2;
		b_factor[j]=w;
		sinb[j]=Math.sin(w);
		cosb[j]=Math.cos(w);
		w= c2 + w2;
		c_factor[j]=w;
		sinc[j]=Math.sin(w);
		cosc[j]=Math.cos(w);
	    }
	    //
	    for(t1=0;t1<3;t1++) {
		SA = Math.sin(a_factor[t1]);
		CA = Math.cos(a_factor[t1]);
		//Optimize
		CACA=CA*CA;
		SASA=SA*SA;
		SACA=SA*CA;
		for(t2=0;t2<3;t2++) {
		    wsin=sinb[t2];
		    wcos=cosb[t2];
		    CACACB=CACA*wcos;
		    SACACB=SACA*wcos;
		    SASACB=SASA*wcos;
		    CASB=CA*wsin;
		    SASB=SA*wsin;
		    //comboの計算1
		    combo02 = CP0 * CASB+ CP1 * SASB+ CP2 * wcos;
		    combo12 = CP4 * CASB+ CP5 * SASB+ CP6 * wcos;
		    combo22 = CP8 * CASB+ CP9 * SASB+ CP10 * wcos;

		    combo02_2 =combo02 * VX02 + combo03;
		    combo02_5 =combo02 * VX12 + combo03;
		    combo02_8 =combo02 * VX22 + combo03;
		    combo02_11=combo02 * VX32 + combo03;
		    combo12_2 =combo12 * VX02 + combo13;
		    combo12_5 =combo12 * VX12 + combo13;
		    combo12_8 =combo12 * VX22 + combo13;
		    combo12_11=combo12 * VX32 + combo13;
		    combo22_2 =combo22 * VX02 + combo23;
		    combo22_5 =combo22 * VX12 + combo23;
		    combo22_8 =combo22 * VX22 + combo23;
		    combo22_11=combo22 * VX32 + combo23;	    
		    for(t3=0;t3<3;t3++){
			wsin=sinc[t3];
			wcos=cosc[t3];			
			SACASC=SACA*wsin;
			SACACC=SACA*wcos;
			SACACBSC=SACACB*wsin;
			SACACBCC=SACACB*wcos;

			rot0 = CACACB*wcos+SASA*wcos+SACACBSC-SACASC;
			rot3 = SACACBCC-SACACC+SASACB*wsin+CACA*wsin;
			rot6 = -CASB*wcos-SASB*wsin;

			combo00 = CP0 * rot0+ CP1 * rot3+ CP2 * rot6;
			combo10 = CP4 * rot0+ CP5 * rot3+ CP6 * rot6;
			combo20 = CP8 * rot0+ CP9 * rot3+ CP10 * rot6;

			rot1 = -CACACB*wsin-SASA*wsin+SACACBCC-SACACC;
			rot4 = -SACACBSC+SACASC+SASACB*wcos+CACA*wcos;
			rot7 = CASB*wsin-SASB*wcos;
			combo01 = CP0 * rot1+ CP1 * rot4+ CP2 * rot7;
			combo11 = CP4 * rot1+ CP5 * rot4+ CP6 * rot7;
			combo21 = CP8 * rot1+ CP9 * rot4+ CP10 * rot7;
			//
			err = 0.0;
			h  = combo20 * VX00+ combo21 * VX01+ combo22_2;
			x = P2D00 - (combo00 * VX00+ combo01 * VX01+ combo02_2) / h;
			y = P2D01 - (combo10 * VX00+ combo11 * VX01+ combo12_2) / h;
			err += x*x+y*y;
			h  = combo20 * VX10+ combo21 * VX11+ combo22_5;
			x = P2D10 - (combo00 * VX10+ combo01 * VX11+ combo02_5) / h;
			y = P2D11 - (combo10 * VX10+ combo11 * VX11+ combo12_5) / h;
			err += x*x+y*y;
			h  = combo20 * VX20+ combo21 * VX21+ combo22_8;
			x = P2D20 - (combo00 * VX20+ combo01 * VX21+ combo02_8) / h;
			y = P2D21 - (combo10 * VX20+ combo11 * VX21+ combo12_8) / h;
			err += x*x+y*y;
			h  = combo20 * VX30+ combo21 * VX31+ combo22_11;
			x = P2D30 - (combo00 * VX30+ combo01 * VX31+ combo02_11) / h;
			y = P2D31 - (combo10 * VX30+ combo11 * VX31+ combo12_11) / h;
			err += x*x+y*y;
			if( err < minerr ) {
			    minerr = err;
			    ma = a_factor[t1];
			    mb = b_factor[t2];
			    mc = c_factor[t3];
			    s1 = t1-1;
			    s2 = t2-1;
			    s3 = t3-1;
			}
		    }
		}
	    }
	    if( s1 == 0 && s2 == 0 && s3 == 0 ){
		factor *= 0.5;
	    }
	    a2 = ma;
	    b2 = mb;
	    c2 = mc;
	}
	arGetRot(ma,mb,mc,this.array);
	/*  printf("factor = %10.5f\n", factor*180.0/MD_PI); */
	return minerr/4;
    }                       
}

