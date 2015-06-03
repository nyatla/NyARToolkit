package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.feature.NyARSurfaceFeatureSet;

/**
 * [inner-class]
 * 生�?�変換+�?影変換行�?��?�積を格納する行�?��??
 * �?想画面上での計算関数を持ちます�??
 */
public class NyARSurfaceTransMatrixSet
{
	public NyARDoubleMatrix44 ctrans=new NyARDoubleMatrix44();
	public NyARDoubleMatrix44 trans=new NyARDoubleMatrix44();
	/**
	 * dpiを計測する時�?�パッチサイズ�?
	 * 基点から�?定mm�?囲の画像からdpiを計算します�??
	 */
	private final static double DPI_BOX=10.0;
	/**
	 * 係数をセ�?トする�??
	 * @param i_param
	 * @param i_trans
	 */
	public void setValue(NyARDoubleMatrix44 i_projection_mat,NyARDoubleMatrix44 i_trans)
	{
		this.trans.setValue(i_trans);
		this.ctrans.mul(i_projection_mat, i_trans);
	}
	/**
	 * �?想点を計算する�??
	 * @param i_coord
	 * 変換�?の座�?(�?想点)
	 */
	public void calculate2dPos(double i_x,double i_y,NyARDoublePoint2d o_idepos)
	{
		NyARDoubleMatrix44 t=this.ctrans;
	    double h  = (t.m20 * i_x + t.m21 * i_y + t.m23);
	    double hx = (t.m00 * i_x + t.m01 * i_y + t.m03)/h;
	    double hy = (t.m10 * i_x + t.m11 * i_y + t.m13)/h;
	    o_idepos.x=hx;
	    o_idepos.y=hy;
	}	
	/**
	 * 基準点のdpiを推定する�??
	 * @param i_cptrans
	 * �?影変換行�??. [cparam]*[trans]
	 * @param trans
	 * @param pos
	 * [2]
	 * @param o_dpi
	 * x,y方向それぞれ�?�推定dpi
	 * @return
	 */
	public void ar2GetResolution2d(jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.feature.NyAR2FeatureCoord i_pos, NyARDoublePoint2d o_dpi)
	{
		NyARDoubleMatrix44 t=this.ctrans;
		//基点
	    double mx0 = i_pos.mx;
	    double my0 = i_pos.my;
	    double h0  = t.m20 * mx0 + t.m21 * my0 + t.m23;
	    double hx0 = t.m00 * mx0 + t.m01 * my0 + t.m03;
	    double hy0 = t.m10 * mx0 + t.m11 * my0 + t.m13;
	    double x0 = hx0 / h0;
	    double y0 = hy0 / h0;

	    double   h,sx,sy;
	    //+X
	    h=h0+t.m20*DPI_BOX;
	    sx = ((hx0+DPI_BOX*t.m00) / h)-x0;
	    sy = ((hy0+DPI_BOX*t.m10) / h)-y0;
	    //dpi -x
	    o_dpi.x = Math.sqrt(sx*sx+sy*sy)*2.54;
	    
	    //+Y
	    h=h0+t.m21*DPI_BOX;
	    sx = ((hx0+DPI_BOX*t.m01) / h)-x0;
	    sy = ((hy0+DPI_BOX*t.m11) / h)-y0;

	    //dpi -y
	    o_dpi.y = Math.sqrt(sx*sx+sy*sy)* 2.54;
	    return;
	}
	/**
	 * x,yの�?ち小さ�?方の解像度を返す�?
	 * @param i_pos
	 * �?想系座�?
	 * @return
	 * 推定したdpi
	 */
	public double ar2GetMinResolution(jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.feature.NyAR2FeatureCoord i_pos)
	{
		NyARDoubleMatrix44 t=this.ctrans;
		//基点
	    double mx0 = i_pos.mx;
	    double my0 = i_pos.my;
	    double h0  = t.m20 * mx0 + t.m21 * my0 + t.m23;
	    double hx0 = t.m00 * mx0 + t.m01 * my0 + t.m03;
	    double hy0 = t.m10 * mx0 + t.m11 * my0 + t.m13;
	    double x0 = hx0 / h0;
	    double y0 = hy0 / h0;

	    double   h,sx,sy;
	    //+X
	    h=h0+t.m20*DPI_BOX;
	    sx = ((hx0+DPI_BOX*t.m00) / h)-x0;
	    sy = ((hy0+DPI_BOX*t.m10) / h)-y0;
	    //dpi -x
	    double dx = Math.sqrt(sx*sx+sy*sy)*2.54;
	    
	    //+Y
	    h=h0+t.m21*DPI_BOX;
	    sx = ((hx0+DPI_BOX*t.m01) / h)-x0;
	    sy = ((hy0+DPI_BOX*t.m11) / h)-y0;

	    //dpi -y
	    double dy = Math.sqrt(sx*sx+sy*sy)* 2.54;
	    
	    return dx<dy?dx:dy;
	}
	/**
	 * なんだろうこれ�?Z位置?�?
	 * @param mx
	 * @param my
	 * @return
	 */
	public double calculateVd(double mx,double my)
	{
		NyARDoubleMatrix44 t=this.trans;
		double vd0 = t.m00 * mx+ t.m01 * my+ t.m03;
		double vd1 = t.m10 * mx+ t.m11 * my+ t.m13;
		double vd2 = t.m20 * mx+ t.m21 * my+ t.m23;
		return (vd0*t.m02 + vd1*t.m12 + vd2*t.m22)/Math.sqrt( vd0*vd0 + vd1*vd1 + vd2*vd2 );
	}
}