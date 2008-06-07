package jp.nyatla.nyartoolkit.base;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARMat;




public class Param {
    private static final int AR_PARAM_NMIN=6;//#define   AR_PARAM_NMIN         6
    private static final int AR_PARAM_NMAX=1000;//#define   AR_PARAM_NMAX      1000
    private static final double AR_PARAM_C34=100.0;//#define   AR_PARAM_C34        100.0

    /*
	typedef struct {
	    int      xsize, ysize;
	    double   matL[3][4];
	    double   matR[3][4];
	    double   matL2R[3][4];
	    double   dist_factorL[4];
	    double   dist_factorR[4];
	} ARSParam;*/
/*	static class ARSParam{
	    int      xsize, ysize;
	    Double2dArray   matL=new Double2dArray(3,4);
	    Double2dArray   matR=new Double2dArray(3,4);
	    Double2dArray   matL2R=new Double2dArray(3,4);
	    double[]   dist_factorL=new double[4];
	    double[]   dist_factorR=new double[4];
	}*/
	private static final int arParamGet_AR_PARAM_CDMIN = 12;
	/*int  arParamGet( double global[][3], double screen[][2], int data_num,double mat[3][4] );*/
	public static int arParamGet( double global[][], double[][] screen, int num,double[][] mat) throws NyARException
	{
        	NyARMat     mat_a, mat_at, mat_r;//ARMat     *mat_a, *mat_at, *mat_r, mat_cpara;
        	NyARMat     mat_wm1,mat_wm2;//ARMat     *mat_wm1, *mat_wm2;
        	int i;
        	
        	if(num < AR_PARAM_NMIN){
        		return( -1 );
        	}
        	if(num > AR_PARAM_NMAX){
        		return( -1 );
        	}
        	NyARException.trap("未チェックのパス");{
        	mat_a = new NyARMat(2*num,arParamGet_AR_PARAM_CDMIN-1 );//mat_a = arMatrixAlloc( 2*num, AR_PARAM_CDMIN-1 );
		mat_at =new NyARMat(arParamGet_AR_PARAM_CDMIN-1, 2*num );//mat_at = arMatrixAlloc( AR_PARAM_CDMIN-1, 2*num );
		mat_r = new NyARMat(2*num,1);//mat_r = arMatrixAlloc( 2*num, 1 );
		mat_wm1 =new NyARMat(arParamGet_AR_PARAM_CDMIN-1, arParamGet_AR_PARAM_CDMIN-1 );//mat_wm1 = arMatrixAlloc( AR_PARAM_CDMIN-1, AR_PARAM_CDMIN-1 );
		mat_wm2 =new NyARMat(arParamGet_AR_PARAM_CDMIN-1, 2*num );//mat_wm2 = arMatrixAlloc( AR_PARAM_CDMIN-1, 2*num );
        	}
/*
        	mat_a = Matrix.arMatrixAlloc( 2*num, arParamGet_AR_PARAM_CDMIN-1 );//mat_a = arMatrixAlloc( 2*num, AR_PARAM_CDMIN-1 );
		if( mat_a == null){
		   return -1;
		}
		mat_at =Matrix.arMatrixAlloc(arParamGet_AR_PARAM_CDMIN-1, 2*num );//mat_at = arMatrixAlloc( AR_PARAM_CDMIN-1, 2*num );
		if(mat_at == null){
		   return -1;
		}
		mat_r = Matrix.arMatrixAlloc( 2*num, 1 );//mat_r = arMatrixAlloc( 2*num, 1 );
		if(mat_r ==null){
		   return -1;
		}
		mat_wm1 = Matrix.arMatrixAlloc(arParamGet_AR_PARAM_CDMIN-1, arParamGet_AR_PARAM_CDMIN-1 );//mat_wm1 = arMatrixAlloc( AR_PARAM_CDMIN-1, AR_PARAM_CDMIN-1 );
		if( mat_wm1 == null) {
		   return -1;
		}
		mat_wm2 = Matrix.arMatrixAlloc(arParamGet_AR_PARAM_CDMIN-1, 2*num );//mat_wm2 = arMatrixAlloc( AR_PARAM_CDMIN-1, 2*num );
		if( mat_wm2 == null ) {
			return -1;
		}
*/
		/* Initializing array */
		mat_a.zeroClear();//Javaではゼロクリアされるので不要
//		pa1=mat_a.getPointer();//pa1 = mat_a->m;
//		for(i = 0; i < 2 * num * (arParamGet_AR_PARAM_CDMIN-1); i++){
//			{//*pa1++ = 0.0;
//			pa1.set(0.0);
//			pa1.incPtr();}
//		}
		double[][] pa1	=mat_a.getArray();
		double[][] pa2 =mat_a.getArray();
		/* Calculate A,R matrix */
		double[][] pr=mat_r.getArray();
		int pr_ptr_col=0;
		for(i = 0; i < num; i++) {//for(i = 0, pr = mat_r->m; i < num; i++) {
		    int pa1_ptr_row	=2*i;  //pa1 = &(mat_a->m[ (2*i)   * (AR_PARAM_CDMIN-1)
		    int pa2_ptr_row	=2*i+1;//pa2 = &(mat_a->m[ (2*i+1) * (AR_PARAM_CDMIN-1) + 4]);


//		    pa1=mat_a.getPointer((2*i)*(arParamGet_AR_PARAM_CDMIN-1));    ]);
//		    pa2=mat_a.getPointer((2*i+1)*(arParamGet_AR_PARAM_CDMIN-1) + 4);
		    //*pa1++ = global[i][0]; *pa1++ = global[i][1];
		    pa1[pa1_ptr_row][ 0]=global[i][0];
		    pa1[pa1_ptr_row][ 1]=global[i][1];
		    //*pa1++ = global[i][2]; *pa1++  = 1.0;
		    pa1[pa1_ptr_row][ 2]=global[i][2];
		    pa1[pa1_ptr_row][ 3]=1.0;
		    //*pa2++ = global[i][0]; *pa2++ = global[i][1];
		    pa2[pa2_ptr_row][ 4]=global[i][0];
		    pa2[pa2_ptr_row][ 5]=global[i][1];
		    //*pa2++ = global[i][2]; *pa2++ = 1.0;
		    pa2[pa2_ptr_row][ 6]=global[i][2];
		    pa2[pa2_ptr_row][ 7]=1.0;
		    //pa1 += 4;
		    //*pa1++ = -global[i][0] * screen[i][0];
		    pa1[pa1_ptr_row][ 8]=-global[i][0] * screen[i][0];
		    //*pa1++ = -global[i][1] * screen[i][0];
		    pa1[pa1_ptr_row][ 9]=-global[i][1] * screen[i][0];
		    //*pa1   = -global[i][2] * screen[i][0];
		    pa1[pa1_ptr_row][10]=-global[i][2]* screen[i][0];
		    //*pa2++ = -global[i][0] * screen[i][1];
		    pa2[pa2_ptr_row][ 8]=-global[i][0] * screen[i][1];
		    //*pa2++ = -global[i][1] * screen[i][1];
		    pa2[pa2_ptr_row][ 9]=-global[i][1] * screen[i][1];
		    //*pa2   = -global[i][2] * screen[i][1];
		    pa2[pa2_ptr_row][10]=-global[i][2] * screen[i][1];
		    //*pr++  = screen[i][0] * AR_PARAM_C34;
		    pr[0][pr_ptr_col]=screen[i][0] * AR_PARAM_C34;pr_ptr_col++;
		    //*pr++  = screen[i][1] * AR_PARAM_C34;
		    pr[0][pr_ptr_col]=screen[i][1] * AR_PARAM_C34;pr_ptr_col++;
		}

		NyARException.trap("未チェックのパス");
		NyARMat.matrixTrans(mat_at, mat_a );//if( arMatrixTrans( mat_at, mat_a ) < 0 ){

		NyARException.trap("未チェックのパス");
		mat_wm1.matrixMul(mat_at, mat_a );//if( arMatrixMul( mat_wm1, mat_at, mat_a ) < 0 ) {
		NyARException.trap("未チェックのパス");
		mat_wm1.matrixSelfInv();//if( arMatrixSelfInv( mat_wm1 ) < 0 ) {

		NyARException.trap("未チェックのパス");
		mat_wm2.matrixMul(mat_wm1, mat_at );//if( arMatrixMul( mat_wm2, mat_wm1, mat_at ) < 0 ) {

		//mat_cpara.row = AR_PARAM_CDMIN-1;//mat_cpara.row = AR_PARAM_CDMIN-1;
		//mat_cpara.clm = 1;
		//mat_cpara.m = &(mat[0][0]);
		/*1次元行列から3x4行列に転写。高負荷なところじゃないから地道に転写でOK*/
		NyARMat mat_cpara=new NyARMat(arParamGet_AR_PARAM_CDMIN-1,1);
		double[][] mat_cpara_array=mat_cpara.getArray();
		NyARException.trap("未チェックのパス");
		mat_cpara.matrixMul(mat_wm2, mat_r );//if( arMatrixMul( &mat_cpara, mat_wm2, mat_r ) < 0 ) {

		for(int i2=0;i<3;i++){
		    for(int i3=0;i3<4;i3++){
			mat[i2][i3]=mat_cpara_array[i2*4+i3][0];
		    }
		}
		//ARMat.wrap(mat.array(),arParamGet_AR_PARAM_CDMIN-1,1);}

		mat[2][3]=AR_PARAM_C34;//mat[2][3] = AR_PARAM_C34;

		return 0;
	}

/*	//int    arsParamChangeSize( ARSParam *source, int xsize, int ysize, ARSParam *newparam );
	public static int arsParamChangeSize( ARSParam source, int xsize, int ysize, ARSParam newparam)
	{
	    double  scale;

	    newparam.xsize=xsize;//newparam->xsize = xsize;
	    newparam.ysize=ysize;//newparam->ysize = ysize;

	    scale=(double)xsize/ (double)(source.xsize);//scale = (double)xsize / (double)(source->xsize);
	    for(int i = 0; i < 4; i++ ){
	    	newparam.matL.set(0,i,source.matL.get(0,i)*scale);//newparam->matL[0][i] = source->matL[0][i] * scale;
	        newparam.matL.set(1,i,source.matL.get(1,i)*scale);//newparam->matL[1][i] = source->matL[1][i] * scale;
	        newparam.matL.set(2,i,source.matL.get(2,i));//newparam->matL[2][i] = source->matL[2][i];
	    }
	    for(int i = 0; i < 4; i++ ) {
	    	newparam.matR.set(0,i,source.matR.get(0,i)*scale);//newparam->matR[0][i] = source->matR[0][i] * scale;
	    	newparam.matR.set(1,i,source.matR.get(1,i)*scale);//newparam->matR[1][i] = source->matR[1][i] * scale;
	    	newparam.matR.set(2,i,source.matR.get(2,i));//newparam->matR[2][i] = source->matR[2][i];
	    }
	    for(int i = 0; i < 4; i++ ) {
	    	newparam.matL2R.set(0,i,source.matL2R.get(0,i));//newparam->matL2R[0][i] = source->matL2R[0][i];
	    	newparam.matL2R.set(1,i,source.matL2R.get(1,i));//newparam->matL2R[1][i] = source->matL2R[1][i];
	    	newparam.matL2R.set(2,i,source.matL2R.get(2,i));//newparam->matL2R[2][i] = source->matL2R[2][i];
	    }

	    newparam.dist_factorL[0] = source.dist_factorL[0] * scale;//newparam->dist_factorL[0] = source->dist_factorL[0] * scale;
	    newparam.dist_factorL[1] = source.dist_factorL[1] * scale;//newparam->dist_factorL[1] = source->dist_factorL[1] * scale;
	    newparam.dist_factorL[2] = source.dist_factorL[2] / (scale*scale);//newparam->dist_factorL[2] = source->dist_factorL[2] / (scale*scale);
	    newparam.dist_factorL[3] = source.dist_factorL[3];//newparam->dist_factorL[3] = source->dist_factorL[3];

	    newparam.dist_factorR[0] = source.dist_factorR[0] * scale;//newparam->dist_factorR[0] = source->dist_factorR[0] * scale;
	    newparam.dist_factorR[1] = source.dist_factorR[1] * scale;//newparam->dist_factorR[1] = source->dist_factorR[1] * scale;
	    newparam.dist_factorR[2] = source.dist_factorR[2] / (scale*scale);//newparam->dist_factorR[2] = source->dist_factorR[2] / (scale*scale);
	    newparam.dist_factorR[3] = source.dist_factorR[3];//newparam->dist_factorR[3] = source->dist_factorR[3];

	    return 0;
	}*/
/*	//int arsParamSave( char *filename, ARSParam *sparam );
	public static int arsParamSave(String filename, ARSParam sparam) throws Exception
	{   
	    //int      xsize, ysize;
	    //Double2dArray   matL=new Double2dArray(3,4);
	    //Double2dArray   matR=new Double2dArray(3,4);
	    //Double2dArray   matL2R=new Double2dArray(3,4);
	    //double   dist_factorL[]=new double[4];
	    //double   dist_factorR[]=new double[4];

		byte[] buf=new byte[(4+4+(3*4*8)*3+(4*8)*2)];
		
		//バッファをラップ
		ByteBuffer bb = ByteBuffer.wrap(buf);
		bb.order(ByteOrder.BIG_ENDIAN);

		//書き込み
		bb.putInt(sparam.xsize);
		bb.putInt(sparam.ysize);
		for(int i=0;i<3;i++){
			for(int i2=0;i2<4;i2++){
				bb.putDouble(sparam.matL.get(i, i2));
			}
		}
		for(int i=0;i<3;i++){
			for(int i2=0;i2<4;i2++){
				bb.putDouble(sparam.matR.get(i, i2));
			}
		}
		for(int i=0;i<3;i++){
			for(int i2=0;i2<4;i2++){
				bb.putDouble(sparam.matL2R.get(i, i2));
			}
		}
		for(int i=0;i<4;i++){
			bb.putDouble(sparam.dist_factorL[i]);
		}
		for(int i=0;i<4;i++){
			bb.putDouble(sparam.dist_factorR[i]);
		}
		//ファイルに保存
		FileOutputStream fs=new FileOutputStream(filename);
		fs.write(buf);
		fs.close();
		
		return 0;
	}*/
/*	//int    arsParamLoad( char *filename, ARSParam *sparam );
	public static int arsParamLoad(String filename, ARSParam sparam ) throws Exception
	{
		//ファイルを読んどく
		FileInputStream fs=new FileInputStream(filename);
		File f=new File(filename);
		int file_size=(int)f.length();
		byte[] buf=new byte[file_size];
		fs.read(buf);
		fs.close();
		
		//バッファを加工
		ByteBuffer bb = ByteBuffer.wrap(buf);
		bb.order(ByteOrder.BIG_ENDIAN);
		
		//固定回数パースして配列に格納		
		sparam.xsize=bb.getInt();
		sparam.ysize=bb.getInt();
		for(int i=0;i<3;i++){
			for(int i2=0;i2<4;i2++){
				sparam.matL.set(i,i2,bb.getDouble());
			}
		}
		for(int i=0;i<3;i++){
			for(int i2=0;i2<4;i2++){
				sparam.matR.set(i,i2,bb.getDouble());
			}
		}
		for(int i=0;i<3;i++){
			for(int i2=0;i2<4;i2++){
				sparam.matL2R.set(i,i2,bb.getDouble());
			}
		}
		for(int i=0;i<3;i++){
			sparam.dist_factorL[i]=bb.getDouble();
		}
		for(int i=0;i<3;i++){
			sparam.dist_factorR[i]=bb.getDouble();
		}
		return 0;
	}*/
/*
	//	int    arsParamGetMat( double matL[3][4], double matR[3][4],double cparaL[3][4], double cparaR[3][4], double matL2R[3][4] );
	public static int arsParamGetMat(double[][] matL, double[][] matR,double[][] cparaL, double[][] cparaR, double[][] matL2R) throws JartkException
	{
	    ARMat t1,t2,t3;//ARMat    *t1, *t2, *t3;
	    //double   transL[3][4], transR[3][4];
	    Double2dArray transL=new Double2dArray(3,4);
	    Double2dArray transR=new Double2dArray(3,4);
	
	    arParamDecompMat( matL,cparaL,transL);
	    arParamDecompMat( matR,cparaR,transR);
	    JartkException.trap("未チェックパス");{
	    t1=new ARMat(4,4);//t1 = arMatrixAlloc( 4, 4 );
	    t2=new ARMat(4,4);//t2 = arMatrixAlloc( 4, 4 );
	    }
	    double[][] t1_array=t1.getArray();
	    double[][] t2_array=t2.getArray();
	    for(int j = 0; j < 3; j++ ){
		for(int i = 0; i < 4; i++ ) {
		    JartkException.trap("未チェックパス");{
		    t1_array[j][i]=transL.get(j,i);//t1->m[j*4+i] = transL[j][i];
	            t2_array[j][i]=transL.get(j,i);//t2->m[j*4+i] = transR[j][i];
		    }
	        }
	    }
	    JartkException.trap("未チェックパス");{
	    t1_array[3][0]=t1_array[3][1]=t1_array[3][2]=0.0;//t1->m[12] = t1->m[13] = t1->m[14] = 0.0;
	    t1_array[3][3]=1.0;//t1->m[15] = 1.0;
	    t2_array[3][0]=t2_array[3][1]=t2_array[3][2]=0.0;//t2->m[12] = t2->m[13] = t2->m[14] = 0.0;
	    t2_array[3][3]=1.0;//t2->m[15] = 1.0;
	    }
	    JartkException.trap("未チェックのパス");
	    t1.matrixSelfInv();//if( arMatrixSelfInv(t1) != 0 ) {

	    JartkException.trap("未チェックのパス");	    
	    t3 =ARMat.matrixAllocMul(t2, t1);//t3 = arMatrixAllocMul(t2, t1);
	    double[][] t3_array=t3.getArray();
	    if(t3==null){
	        return -1;
	    }
	
	    for(int j = 0; j < 3; j++ ) {
	       for(int i = 0; i < 4; i++ ) {
		   JartkException.trap("未チェックパス");
	    	   matL2R[j][i]=t3_array[j][i];//matL2R[j][i] = t3->m[j*4+i];
	        }
	    }
	    return 0;
	}
*/	//int arsParamDisp( ARSParam *sparam )
/*	public static int arsParamDisp( ARSParam sparam)
	{
		System.out.println("--------------------------------------");//printf("--------------------------------------\n");
		System.out.println("SIZE = "+sparam.xsize+", "+sparam.ysize);// printf("SIZE = %d, %d\n", sparam->xsize, sparam->ysize);
		System.out.println("-- Left --");//printf("-- Left --\n");
		System.out.println("Distotion factor = "+sparam.dist_factorL[0]+" "+sparam.dist_factorL[1]+" "+sparam.dist_factorL[2]+" "+sparam.dist_factorL[3]);//printf("Distotion factor = %f %f %f %f\n", sparam->dist_factorL[0],sparam->dist_factorL[1], sparam->dist_factorL[2], sparam->dist_factorL[3] );
	    for(int j = 0; j < 3; j++ ) {
	        for(int i = 0; i < 4; i++ ){
	        	System.out.print(sparam.matL.get(j,i)+" ");//printf("%7.5f ", sparam->matL[j][i]);
	        }
	        System.out.println();//printf("\n");
	    }
	
	    System.out.println("-- Right --");//printf("-- Right --\n");
	    System.out.println("Distotion factor = "+sparam.dist_factorR[0]+" "+sparam.dist_factorR[1]+" "+sparam.dist_factorR[2]+" "+sparam.dist_factorR[3]);//printf("Distotion factor = %f %f %f %f\n", sparam->dist_factorR[0],sparam->dist_factorR[1], sparam->dist_factorR[2], sparam->dist_factorR[3]);
	    for(int j = 0; j < 3; j++ ){
	        for(int i = 0; i < 4; i++ ){
	        	System.out.println(sparam.matR.get(j,i)+" ");//printf("%7.5f ", sparam->matR[j][i]);
	        }
	        System.out.println();//printf("\n");
	    }
	
	    System.out.println("-- Left => Right --");//printf("-- Left => Right --\n");
	    for(int j = 0; j < 3; j++ ) {
	        for(int i = 0; i < 4; i++ ){
	        	//printf("%7.5f ", sparam->matL2R[j][i]);
	        }
	        System.out.println();//printf("\n");
	    }
	
	    System.out.println("--------------------------------------");//printf("--------------------------------------\n");
	
	    return 0;		
	}*/
}
