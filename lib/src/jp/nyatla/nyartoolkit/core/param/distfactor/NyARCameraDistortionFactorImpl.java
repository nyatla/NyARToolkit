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
 * 共通実装
 *
 */
public abstract class NyARCameraDistortionFactorImpl implements INyARCameraDistortionFactor
{

	/**
	 * この関数は、座標点を理想座標系から観察座標系へ変換します。
	 * @param i_in
	 * 変換元の座標
	 * @param o_out
	 * 変換後の座標を受け取るオブジェクト
	 */
	@Override
	public final void ideal2Observ(NyARDoublePoint2d i_in, NyARDoublePoint2d o_out)
	{
		this.ideal2Observ(i_in.x,i_in.y, o_out);
		return;
	}
	/**
	 * この関数は、座標点を理想座標系から観察座標系へ変換します。
	 */
	@Override
	public final void ideal2Observ(NyARDoublePoint2d i_in, NyARIntPoint2d o_out)
	{
		this.ideal2Observ(i_in.x,i_in.y,o_out);
		return;
	}
	/**
	 * {@link #observ2Ideal(double, double, NyARDoublePoint2d)}のラッパーです。
	 */	
	@Override
	public final void observ2Ideal(NyARDoublePoint2d i_in, NyARDoublePoint2d o_point)
	{
		this.observ2Ideal(i_in.x,i_in.y,o_point);
	}
	
	
	@Override
	public void ideal2ObservBatch(NyARDoublePoint2d[] i_in,NyARDoublePoint2d[] o_out, int i_size) {
		for(int i=i_size-1;i>=0;i--){
			this.ideal2Observ(i_in[i].x,i_in[i].y,o_out[i]);
		}
		return;		
	}
	@Override
	public void ideal2ObservBatch(NyARDoublePoint2d[] i_in,NyARIntPoint2d[] o_out, int i_size)
	{
		for(int i=i_size-1;i>=0;i--){
			this.ideal2Observ(i_in[i].x,i_in[i].y,o_out[i]);
		}
		return;		
	}	
	
	
	/**
	 * 座標配列全てに対して、{@link #observ2Ideal(double, double, NyARIntPoint2d)}を適応します。
	 * 必要に応じて継承先で高速化してください。
	 */
	@Override
	public void observ2IdealBatch(NyARIntPoint2d[] i_in, NyARDoublePoint2d[] o_out, int i_size)
	{
		for(int i=i_size-1;i>=0;i--){
			this.observ2Ideal(i_in[i].x,i_in[i].y,o_out[i]);
		}
		return;		
	}
	/**
	 * 座標配列全てに対して、{@link #observ2Ideal(double, double, NyARDoublePoint2d)}を適応します。
	 * 必要に応じて継承先で高速化してください。
	 */
	@Override
	public void observ2IdealBatch(NyARDoublePoint2d[] i_in, NyARDoublePoint2d[] o_out, int i_size)
	{
		for(int i=i_size-1;i>=0;i--){
			this.observ2Ideal(i_in[i].x,i_in[i].y,o_out[i]);
		}
		return;
	}		
	
	
	
}
