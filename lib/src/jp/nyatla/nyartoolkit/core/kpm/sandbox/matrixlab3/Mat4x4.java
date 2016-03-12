package jp.nyatla.nyartoolkit.core.kpm.sandbox.matrixlab3;

class Mat4x4{
	public double m00,m01,m02,m03;
	public double m10,m11,m12,m13;
	public double m20,m21,m22,m23;
	public double m30,m31,m32,m33;
	public double invers(){
		double a00=m00,a01=m01,a02=m02,a03=m03;
		double a10=m10,a11=m11,a12=m12,a13=m13;
		double a20=m20,a21=m21,a22=m22,a23=m23;
		double a30=m30,a31=m31,a32=m32,a33=m33;
		double v0=(a22*a33-a23*a32);//23-23
		double v1=(a21*a33-a23*a31);//23-13
		double v2=(a21*a32-a22*a31);//23-12
		double v4=(a20*a33-a23*a30);//23-03
		double v5=(a20*a32-a22*a30);//23-02
		double v7=(a20*a31-a21*a30);//23-01
		double v15=(a12*a33-a13*a32);//13-23
		double v16=(a11*a33-a13*a31);//13-13
		double v17=(a11*a32-a12*a31);//13-12
		double v19=(a10*a33-a13*a30);//13-03
		double v20=(a10*a32-a12*a30);//13-02
		double v22=(a10*a31-a11*a30);//13-01
		double v25=(a12*a23-a13*a22);//12-23
		double v26=(a11*a23-a13*a21);//12-13
		double v27=(a11*a22-a12*a21);//12-12
		double v29=(a10*a23-a13*a20);//12-03
		double v30=(a10*a22-a12*a20);//12-02
		double v32=(a10*a21-a11*a20);//12-01
		//lv2=18;mul=36,sum=18
		double v3=a11*(v0)-a12*(v1)+a13*(v2);//123-123
		double v6=a10*(v0)-a12*(v4)+a13*(v5);//123-023
		double v8=a10*(v1)-a11*(v4)+a13*(v7);//123-013
		double v9=a10*(v2)-a11*(v5)+a12*(v7);//123-012
		double v11=a01*(v0)-a02*(v1)+a03*(v2);//023-123
		double v12=a00*(v0)-a02*(v4)+a03*(v5);//023-023
		double v13=a00*(v1)-a01*(v4)+a03*(v7);//023-013
		double v14=a00*(v2)-a01*(v5)+a02*(v7);//023-012
		double v18=a01*(v15)-a02*(v16)+a03*(v17);//013-123
		double v21=a00*(v15)-a02*(v19)+a03*(v20);//013-023
		double v23=a00*(v16)-a01*(v19)+a03*(v22);//013-013
		double v24=a00*(v17)-a01*(v20)+a02*(v22);//013-012
		double v28=a01*(v25)-a02*(v26)+a03*(v27);//012-123
		double v31=a00*(v25)-a02*(v29)+a03*(v30);//012-023
		double v33=a00*(v26)-a01*(v29)+a03*(v32);//012-013
		double v34=a00*(v27)-a01*(v30)+a02*(v32);//012-012
		//lv3=16;mul=48,sum=48
		double v10=a00*(v3)-a01*(v6)+a02*(v8)-a03*(v9);//0123-0123
		//lv4=1;mul=4,sum=4
		double det=1/v10;

		m00=v3*det;
		m01=-v11*det;
		m02=v18*det;
		m03=-v28*det;
		m10=-v6*det;
		m11=v12*det;
		m12=-v21*det;
		m13=v31*det;
		m20=v8*det;
		m21=-v13*det;
		m22=v23*det;
		m23=-v33*det;
		m30=-v9*det;
		m31=v14*det;
		m32=-v24*det;
		m33=v34*det;
		//total sum=70,mul=105
		return det;
	}
}

//57ms