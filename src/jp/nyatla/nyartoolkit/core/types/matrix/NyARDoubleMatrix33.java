package jp.nyatla.nyartoolkit.core.types.matrix;

public class NyARDoubleMatrix33 implements INyARDoubleMatrix
{
	public double m00;
	public double m01;
	public double m02;
	public double m10;
	public double m11;
	public double m12;
	public double m20;
	public double m21;
	public double m22;
	/**
	 * 遅いからあんまり使わないでね。
	 */
	public void setValue(double[] i_value)
	{
		this.m00=i_value[0];
		this.m01=i_value[1];
		this.m02=i_value[2];
		this.m10=i_value[3];
		this.m11=i_value[4];
		this.m12=i_value[5];
		this.m20=i_value[6];
		this.m21=i_value[7];
		this.m22=i_value[8];
		return;
	}
	/**
	 * 遅いからあんまり使わないでね。
	 */
	public void getValue(double[] o_value)
	{
		o_value[0]=this.m00;
		o_value[1]=this.m01;
		o_value[2]=this.m02;
		o_value[3]=this.m10;
		o_value[4]=this.m11;
		o_value[5]=this.m12;
		o_value[6]=this.m20;
		o_value[7]=this.m21;
		o_value[8]=this.m22;
		return;
	}
}
