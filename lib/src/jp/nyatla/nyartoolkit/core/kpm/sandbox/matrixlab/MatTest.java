package jp.nyatla.nyartoolkit.core.kpm.sandbox.matrixlab;

public class MatTest {
	static public void main(String[] args){
		double m00=3,m01=1,m02=1,m03=2;
		double m10=5,m11=1,m12=3,m13=4;
		double m20=2,m21=0,m22=1,m23=0;
		double m30=1,m31=3,m32=2,m33=1;
	
		double det=m00*(m11*(m22*m33-m23*m32)-m12*(m21*m33-m23*m31)+m13*(m21*m32-m22*m31))-m01*(m10*(m22*m33-m23*m32)-m12*(m20*m33-m23*m30)+m13*(m20*m32-m22*m30))+m02*(m10*(m21*m33-m23*m31)-m11*(m20*m33-m23*m30)+m13*(m20*m31-m21*m30))-m03*(m10*(m21*m32-m22*m31)-m11*(m20*m32-m22*m30)+m12*(m20*m31-m21*m30));
		double M00=(m11*(m22*m33-m23*m32)-m12*(m21*m33-m23*m31)+m13*(m21*m32-m22*m31))/det;
		double M10=(m10*(m23*m32-m22*m33)-m12*(m23*m30-m20*m33)+m13*(m22*m30-m20*m32))/det;
		double M20=(m10*(m21*m33-m23*m31)-m11*(m20*m33-m23*m30)+m13*(m20*m31-m21*m30))/det;
		double M30=(m10*(m22*m31-m21*m32)-m11*(m22*m30-m20*m32)+m12*(m21*m30-m20*m31))/det;
		double M01=(m01*(m23*m32-m22*m33)-m02*(m23*m31-m21*m33)+m03*(m22*m31-m21*m32))/det;
		double M11=(m00*(m22*m33-m23*m32)-m02*(m20*m33-m23*m30)+m03*(m20*m32-m22*m30))/det;
		double M21=(m00*(m23*m31-m21*m33)-m01*(m23*m30-m20*m33)+m03*(m21*m30-m20*m31))/det;
		double M31=(m00*(m21*m32-m22*m31)-m01*(m20*m32-m22*m30)+m02*(m20*m31-m21*m30))/det;
		double M02=(m01*(m12*m33-m13*m32)-m02*(m11*m33-m13*m31)+m03*(m11*m32-m12*m31))/det;
		double M12=(m00*(m13*m32-m12*m33)-m02*(m13*m30-m10*m33)+m03*(m12*m30-m10*m32))/det;
		double M22=(m00*(m11*m33-m13*m31)-m01*(m10*m33-m13*m30)+m03*(m10*m31-m11*m30))/det;
		double M32=(m00*(m12*m31-m11*m32)-m01*(m12*m30-m10*m32)+m02*(m11*m30-m10*m31))/det;
		double M03=(m01*(m13*m22-m12*m23)-m02*(m13*m21-m11*m23)+m03*(m12*m21-m11*m22))/det;
		double M13=(m00*(m12*m23-m13*m22)-m02*(m10*m23-m13*m20)+m03*(m10*m22-m12*m20))/det;
		double M23=(m00*(m13*m21-m11*m23)-m01*(m13*m20-m10*m23)+m03*(m11*m20-m10*m21))/det;
		double M33=(m00*(m11*m22-m12*m21)-m01*(m10*m22-m12*m20)+m02*(m10*m21-m11*m20))/det;
		System.out.println(String.format("%f,%f,%f,%f",M00,M01,M02,M03));
		System.out.println(String.format("%f,%f,%f,%f",M10,M11,M12,M13));
		System.out.println(String.format("%f,%f,%f,%f",M20,M21,M22,M23));
		System.out.println(String.format("%f,%f,%f,%f",M30,M31,M32,M33));

		return;
	}
}
