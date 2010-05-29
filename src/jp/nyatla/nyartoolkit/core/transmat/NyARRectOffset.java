/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.transmat;

import jp.nyatla.nyartoolkit.core.types.*;

/**
 * 矩形の頂点情報を格納します。
 */
final public class NyARRectOffset
{
	public final NyARDoublePoint3d[] vertex=NyARDoublePoint3d.createArray(4);
	public static NyARRectOffset[] createArray(int i_number)
	{
		NyARRectOffset[] ret=new NyARRectOffset[i_number];
		for(int i=0;i<i_number;i++)
		{
			ret[i]=new NyARRectOffset();
		}
		return ret;
	}	
	/**
	 * 中心位置と辺長から、オフセット情報を作成して設定する。
	 * @param i_width
	 */
	public void setSquare(double i_width)
	{
		final double w_2 = i_width / 2.0;
		
		NyARDoublePoint3d vertex3d_ptr;
		vertex3d_ptr= this.vertex[0];
		vertex3d_ptr.x = -w_2;
		vertex3d_ptr.y =  w_2;
		vertex3d_ptr.z = 0.0;
		vertex3d_ptr= this.vertex[1];
		vertex3d_ptr.x = w_2;
		vertex3d_ptr.y = w_2;
		vertex3d_ptr.z = 0.0;
		vertex3d_ptr= this.vertex[2];
		vertex3d_ptr.x =  w_2;
		vertex3d_ptr.y = -w_2;
		vertex3d_ptr.z = 0.0;
		vertex3d_ptr= this.vertex[3];
		vertex3d_ptr.x = -w_2;
		vertex3d_ptr.y = -w_2;
		vertex3d_ptr.z = 0.0;
		
		return;
	}
}
