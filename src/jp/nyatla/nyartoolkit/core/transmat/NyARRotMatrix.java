package jp.nyatla.nyartoolkit.core.transmat;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.*;
/**
 * 回転行列計算用の、3x3行列
 *
 */
public class NyARRotMatrix
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
	 * インスタンスを準備します。
	 * 
	 * @param i_param
	 * nullを指定した場合、一部の関数が使用不能になります。
	 */
	public NyARRotMatrix(NyARParam i_param) throws NyARException
	{
		this.__initRot_vec1=new NyARRotVector(i_param);
		this.__initRot_vec2=new NyARRotVector(i_param);
		return;
	}
	final private NyARRotVector __initRot_vec1;
	final private NyARRotVector __initRot_vec2;

	

	public final void initRotByPrevResult(NyARTransMatResult i_prev_result)
	{
		double[][] prev_array = i_prev_result.getArray();
		double[] pt;
		pt = prev_array[0];
		this.m00=pt[0];
		this.m01=pt[1];
		this.m02=pt[2];
		pt = prev_array[1];
		this.m10=pt[0];
		this.m11=pt[1];
		this.m12=pt[2];
		pt = prev_array[2];
		this.m20=pt[0];
		this.m21=pt[1];
		this.m22=pt[2];
	}	
	
	
	public final void initRotBySquare(final NyARLinear[] i_linear,final NyARDoublePoint2d[] i_sqvertex) throws NyARException
	{
		final NyARRotVector vec1=this.__initRot_vec1;
		final NyARRotVector vec2=this.__initRot_vec2;

		//向かい合った辺から、２本のベクトルを計算
		
		//軸１
		vec1.exteriorProductFromLinear(i_linear[0], i_linear[2]);
		vec1.checkVectorByVertex(i_sqvertex[0], i_sqvertex[1]);

		//軸２
		vec2.exteriorProductFromLinear(i_linear[1], i_linear[3]);
		vec2.checkVectorByVertex(i_sqvertex[3], i_sqvertex[1]);

		//回転の最適化？
		NyARRotVector.checkRotation(vec1,vec2);

		this.m00 =vec1.v1;
		this.m10 =vec1.v2;
		this.m20 =vec1.v3;
		this.m01 =vec2.v1;
		this.m11 =vec2.v2;
		this.m21 =vec2.v3;
		
		//最後の軸を計算
		this.m02 = vec1.v2 * vec2.v3 - vec1.v3 * vec2.v2;
		this.m12 = vec1.v3 * vec2.v1 - vec1.v1 * vec2.v3;
		this.m22 = vec1.v1 * vec2.v2 - vec1.v2 * vec2.v1;
		final double w = Math.sqrt(this.m02 * this.m02 + this.m12 * this.m12 + this.m22 * this.m22);
		this.m02 /= w;
		this.m12 /= w;
		this.m22 /= w;
		return;
	}

	

	/**
	 * int arGetAngle( double rot[3][3], double *wa, double *wb, double *wc )
	 * Optimize:2008.04.20:STEP[481→433]
	 * 3x3変換行列から、回転角を復元して返します。
	 * @param o_angle
	 * @return
	 */
	protected final void getAngle(NyARDoublePoint3d o_angle)
	{
		double a,b,c;
		double sina, cosa, sinb,cosb,sinc, cosc;
		
		if (this.m22 > 1.0) {// <Optimize/>if( rot[2][2] > 1.0 ) {
			this.m22 = 1.0;// <Optimize/>rot[2][2] = 1.0;
		} else if (this.m22 < -1.0) {// <Optimize/>}else if( rot[2][2] < -1.0 ) {
			this.m22 = -1.0;// <Optimize/>rot[2][2] = -1.0;
		}
		cosb =this.m22;// <Optimize/>cosb = rot[2][2];
		b = Math.acos(cosb);
		sinb =Math.sin(b);
		final double rot02=this.m02;
		final double rot12=this.m12;
		if (b >= 0.000001 || b <= -0.000001) {
			cosa = rot02 / sinb;// <Optimize/>cosa = rot[0][2] / sinb;
			sina = rot12 / sinb;// <Optimize/>sina = rot[1][2] / sinb;
			if (cosa > 1.0) {
				/* printf("cos(alph) = %f\n", cosa); */
				cosa = 1.0;
				sina = 0.0;
			}
			if (cosa < -1.0) {
				/* printf("cos(alph) = %f\n", cosa); */
				cosa = -1.0;
				sina = 0.0;
			}
			if (sina > 1.0) {
				/* printf("sin(alph) = %f\n", sina); */
				sina = 1.0;
				cosa = 0.0;
			}
			if (sina < -1.0) {
				/* printf("sin(alph) = %f\n", sina); */
				sina = -1.0;
				cosa = 0.0;
			}
			a = Math.acos(cosa);
			if (sina < 0) {
				a = -a;
			}
			// <Optimize>
			// sinc = (rot[2][1]*rot[0][2]-rot[2][0]*rot[1][2])/
			// (rot[0][2]*rot[0][2]+rot[1][2]*rot[1][2]);
			// cosc = -(rot[0][2]*rot[2][0]+rot[1][2]*rot[2][1])/
			// (rot[0][2]*rot[0][2]+rot[1][2]*rot[1][2]);
			final double tmp = (rot02 * rot02 + rot12 * rot12);
			sinc = (this.m21 * rot02 - this.m20 * rot12) / tmp;
			cosc = -(rot02 * this.m20 + rot12 * this.m21) / tmp;
			// </Optimize>

			if (cosc > 1.0) {
				/* printf("cos(r) = %f\n", cosc); */
				cosc = 1.0;
				sinc = 0.0;
			}
			if (cosc < -1.0) {
				/* printf("cos(r) = %f\n", cosc); */
				cosc = -1.0;
				sinc = 0.0;
			}
			if (sinc > 1.0) {
				/* printf("sin(r) = %f\n", sinc); */
				sinc = 1.0;
				cosc = 0.0;
			}
			if (sinc < -1.0) {
				/* printf("sin(r) = %f\n", sinc); */
				sinc = -1.0;
				cosc = 0.0;
			}
			c = Math.acos(cosc);
			if (sinc < 0) {
				c = -c;
			}
		} else {
			a = b = 0.0;
			cosa = cosb = 1.0;
			sina = sinb = 0.0;
			cosc=this.m00;//cosc = rot[0];// <Optimize/>cosc = rot[0][0];
			sinc=this.m01;//sinc = rot[1];// <Optimize/>sinc = rot[1][0];
			if (cosc > 1.0) {
				/* printf("cos(r) = %f\n", cosc); */
				cosc = 1.0;
				sinc = 0.0;
			}
			if (cosc < -1.0) {
				/* printf("cos(r) = %f\n", cosc); */
				cosc = -1.0;
				sinc = 0.0;
			}
			if (sinc > 1.0) {
				/* printf("sin(r) = %f\n", sinc); */
				sinc = 1.0;
				cosc = 0.0;
			}
			if (sinc < -1.0) {
				/* printf("sin(r) = %f\n", sinc); */
				sinc = -1.0;
				cosc = 0.0;
			}
			c = Math.acos(cosc);
			if (sinc < 0) {
				c = -c;
			}
		}
		o_angle.x = a;// wa.value=a;//*wa = a;
		o_angle.y = b;// wb.value=b;//*wb = b;
		o_angle.z = c;// wc.value=c;//*wc = c;
		return;
	}
	/**
	 * 回転角から回転行列を計算してセットします。
	 * @param i_x
	 * @param i_y
	 * @param i_z
	 */
	protected void setRot(double i_x, double i_y, double i_z)
	{
		final double sina = Math.sin(i_x);
		final double cosa = Math.cos(i_x);
		final double sinb = Math.sin(i_y);
		final double cosb = Math.cos(i_y);
		final double sinc = Math.sin(i_z);
		final double cosc = Math.cos(i_z);
		// Optimize
		final double CACA = cosa * cosa;
		final double SASA = sina * sina;
		final double SACA = sina * cosa;
		final double SASB = sina * sinb;
		final double CASB = cosa * sinb;
		final double SACACB = SACA * cosb;

		this.m00 = CACA * cosb * cosc + SASA * cosc + SACACB * sinc - SACA * sinc;
		this.m01 = -CACA * cosb * sinc - SASA * sinc + SACACB * cosc - SACA * cosc;
		this.m02 = CASB;
		this.m10 = SACACB * cosc - SACA * cosc + SASA * cosb * sinc + CACA * sinc;
		this.m11 = -SACACB * sinc + SACA * sinc + SASA * cosb * cosc + CACA * cosc;
		this.m12 = SASB;
		this.m20 = -CASB * cosc - SASB * sinc;
		this.m21 = CASB * sinc - SASB * cosc;
		this.m22 = cosb;
		return;
	}
	/**
	 * i_in_pointを変換行列で座標変換する。
	 * @param i_in_point
	 * @param i_out_point
	 */
	protected void getPoint3d(final NyARDoublePoint3d i_in_point,final NyARDoublePoint3d i_out_point)
	{
		i_out_point.x=this.m00 * i_in_point.x + this.m01 * i_in_point.y + this.m02 * i_in_point.z;
		i_out_point.y=this.m10 * i_in_point.x + this.m11 * i_in_point.y + this.m12 * i_in_point.z;
		i_out_point.z=this.m20 * i_in_point.x + this.m21 * i_in_point.y + this.m22 * i_in_point.z;
		return;
	}
	
}
