/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2015 Daqri, LLC.
 *  Copyright 2006-2015 ARToolworks, Inc.
 *
 *  Author(s): Hirokazu Kato, Philip Lamb
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.core.surfacetracking;

import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

/**
 * [inner-class]
 * 生成変換+射影変換行列の積を格納する行列。
 * 理想画面上での計算関数を持ちます。
 */
public class NyARSurfaceTransMatrixSet
{
	public NyARDoubleMatrix44 ctrans=new NyARDoubleMatrix44();
	public NyARDoubleMatrix44 trans=new NyARDoubleMatrix44();
	/**
	 * dpiを計測する時のパッチサイズ。
	 * 基点から指定mm範囲の画像からdpiを計算します。
	 */
	private final static double DPI_BOX=10.0;
	/**
	 * 係数をセットする。
	 * @param i_param
	 * @param i_trans
	 */
	public void setValue(NyARDoubleMatrix44 i_projection_mat,NyARDoubleMatrix44 i_trans)
	{
		this.trans.setValue(i_trans);
		this.ctrans.mul(i_projection_mat, i_trans);
	}
	/**
	 * 理想点を計算する。
	 * @param i_coord
	 * 変換元の座標(理想点)
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
	 * 基準点のdpiを推定する。
	 * @param i_cptrans
	 * 射影変換行列. [cparam]*[trans]
	 * @param trans
	 * @param pos
	 * [2]
	 * @param o_dpi
	 * x,y方向それぞれの推定dpi
	 * @return
	 */
	public void ar2GetResolution2d(NyARNftFsetFile.NyAR2FeatureCoord i_pos, NyARDoublePoint2d o_dpi)
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
	 * x,yのうち小さい方の解像度を返す。
	 * @param i_pos
	 * 理想系座標
	 * @return
	 * 推定したdpi
	 */
	public double ar2GetMinResolution(NyARNftFsetFile.NyAR2FeatureCoord i_pos)
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
	 * なんだろうこれ。Z位置？
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