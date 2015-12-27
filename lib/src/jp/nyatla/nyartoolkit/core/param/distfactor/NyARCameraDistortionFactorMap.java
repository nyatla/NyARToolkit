/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.param.distfactor;

import jp.nyatla.nyartoolkit.core.types.*;

/**
 * 事前に計算したマップを使った樽型歪み設定/解除クラスです。
 * 観察座標→理想系座標のみキャッシュが働きます。
 * {@link #changeScale(double, double)}は使用できません。
 */
public class NyARCameraDistortionFactorMap extends NyARCameraDistortionFactorImpl
{	
	final private INyARCameraDistortionFactor _base_factor;
	/** テーブル１行当たりのデータ数*/
	final protected int _stride;
	/** X座標の変換テーブル*/
	final protected double[] _mapx;
	/** Y座標の変換テーブル*/
	final protected double[] _mapy;
	
	public NyARCameraDistortionFactorMap(int i_screen_width,int i_screen_height,INyARCameraDistortionFactor i_base_factor)
	{
		this._base_factor=i_base_factor;
		NyARDoublePoint2d opoint=new NyARDoublePoint2d();
		
		this._mapx=new double[i_screen_width*i_screen_height];
		this._mapy=new double[i_screen_width*i_screen_height];
		this._stride=i_screen_width;
		int ptr=i_screen_height*i_screen_width-1;
		//歪みマップを構築
		for(int i=i_screen_height-1;i>=0;i--)
		{
			for(int i2=i_screen_width-1;i2>=0;i2--)
			{
				i_base_factor.observ2Ideal(i2,i, opoint);
				this._mapx[ptr]=opoint.x;
				this._mapy[ptr]=opoint.y;
				ptr--;
			}
		}
		return;
		
	}


	
	/**
	 * この関数は、座標点を理想座標系から観察座標系へ変換します。
	 * @param i_in
	 * 変換元の座標
	 * @param o_out
	 * 変換後の座標を受け取るオブジェクト
	 */
	public void ideal2Observ(double i_x,double i_y, NyARDoublePoint2d o_out)
	{
		this._base_factor.ideal2Observ(i_x,i_y, o_out);
	}
	
	/**
	 * この関数は、座標点を理想座標系から観察座標系へ変換します。
	 * @param i_x
	 * 変換元の座標
	 * @param i_y
	 * 変換元の座標
	 * @param o_out
	 * 変換後の座標を受け取るオブジェクト
	 */
	public void ideal2Observ(double i_x,double i_y, NyARIntPoint2d o_out)
	{
		this._base_factor.ideal2Observ(i_x,i_y,o_out);
	}
	
	/**
	 * この関数は、座標を観察座標系から理想座標系へ変換します。
	 * @param ix
	 * 変換元の座標
	 * @param iy
	 * 変換元の座標
	 * @param o_point
	 * 変換後の座標を受け取るオブジェクト
	 */
	public void observ2Ideal(double ix, double iy, NyARDoublePoint2d o_point)
	{
		int idx=(int)ix+(int)iy*this._stride;
		o_point.x=this._mapx[idx];
		o_point.y=this._mapy[idx];
		return;
	}
	@Override
	public void observ2Ideal(int ix, int iy, NyARDoublePoint2d o_point)
	{
		int idx=ix+iy*this._stride;
		o_point.x=this._mapx[idx];
		o_point.y=this._mapy[idx];
		return;
	}
	
	/**
	 * この関数は、複数の座標点を、一括して理想座標系から観察座標系へ変換します。
	 * i_inとo_outには、同じインスタンスを指定できます。
	 * @param i_in
	 * 変換元の座標配列
	 * @param o_out
	 * 変換後の座標を受け取る配列
	 * @param i_size
	 * 変換する座標の個数。
	 */
	public void ideal2ObservBatch(NyARDoublePoint2d[] i_in, NyARDoublePoint2d[] o_out, int i_size)
	{
		this._base_factor.ideal2ObservBatch(i_in, o_out,i_size);
	}

	/**
	 * この関数は、複数の座標点を、一括して理想座標系から観察座標系へ変換します。
	 * i_inとo_outには、同じインスタンスを指定できます。
	 * @param i_in
	 * 変換元の座標配列
	 * @param o_out
	 * 変換後の座標を受け取る配列
	 * @param i_size
	 * 変換する座標の個数。
	 */
	public void ideal2ObservBatch(NyARDoublePoint2d[] i_in, NyARIntPoint2d[] o_out, int i_size)
	{
		this._base_factor.ideal2ObservBatch(i_in, o_out,i_size);		
	}	
	
	
}
