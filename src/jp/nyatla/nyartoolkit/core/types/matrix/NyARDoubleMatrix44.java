package jp.nyatla.nyartoolkit.core.types.matrix;


public class NyARDoubleMatrix44 implements INyARDoubleMatrix
{
	public double m00;
	public double m01;
	public double m02;
	public double m03;
	public double m10;
	public double m11;
	public double m12;
	public double m13;
	public double m20;
	public double m21;
	public double m22;
	public double m23;
	public double m30;
	public double m31;
	public double m32;
	public double m33;
	public static NyARDoubleMatrix44[] createArray(int i_number)
	{
		NyARDoubleMatrix44[] ret=new NyARDoubleMatrix44[i_number];
		for(int i=0;i<i_number;i++)
		{
			ret[i]=new NyARDoubleMatrix44();
		}
		return ret;
	}
	/**
	 * 遅いからあんまり使わないでね。
	 */
	public void setValue(double[] i_value)
	{
		this.m00=i_value[ 0];
		this.m01=i_value[ 1];
		this.m02=i_value[ 2];
		this.m03=i_value[ 3];
		this.m10=i_value[ 4];
		this.m11=i_value[ 5];
		this.m12=i_value[ 6];
		this.m13=i_value[ 7];
		this.m20=i_value[ 8];
		this.m21=i_value[ 9];
		this.m22=i_value[10];
		this.m23=i_value[11];
		this.m30=i_value[12];
		this.m31=i_value[13];
		this.m32=i_value[14];
		this.m33=i_value[15];
		return;
	}
	/**
	 * 遅いからあんまり使わないでね。
	 */
	public void getValue(double[] o_value)
	{
		o_value[ 0]=this.m00;
		o_value[ 1]=this.m01;
		o_value[ 2]=this.m02;
		o_value[ 3]=this.m03;
		o_value[ 4]=this.m10;
		o_value[ 5]=this.m11;
		o_value[ 6]=this.m12;
		o_value[ 7]=this.m13;
		o_value[ 8]=this.m20;
		o_value[ 9]=this.m21;
		o_value[10]=this.m22;
		o_value[11]=this.m23;
		o_value[12]=this.m30;
		o_value[13]=this.m31;
		o_value[14]=this.m32;
		o_value[15]=this.m33;
		return;
	}
}
