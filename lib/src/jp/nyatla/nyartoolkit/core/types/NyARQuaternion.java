/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
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
package jp.nyatla.nyartoolkit.core.types;

import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

public class NyARQuaternion
{
	public double x;
	public double y;
	public double z;
	public double w;
	
	public void setFromMatrix(NyARDoubleMatrix44 i_mat)
	{
	    // 最大成分を検索
	    double elem0 = i_mat.m00 - i_mat.m11 - i_mat.m22 + 1.0;
	    double elem1 = -i_mat.m00 + i_mat.m11 - i_mat.m22 + 1.0;
	    double elem2 = -i_mat.m00 - i_mat.m11 + i_mat.m22 + 1.0;
	    double elem3 = i_mat.m00 + i_mat.m11 + i_mat.m22 + 1.0;
		if(elem0>elem1 && elem0>elem2 && elem0>elem3){
		    double v = Math.sqrt(elem0) * 0.5;
		    double mult = 0.25 / v;
			this.x = v;
			this.y = ((i_mat.m10 + i_mat.m01) * mult);
			this.z = ((i_mat.m02 + i_mat.m20) * mult);
			this.w = ((i_mat.m21 - i_mat.m12) * mult);
		}else if(elem1>elem2 && elem1>elem3){
		    double v = Math.sqrt(elem1) * 0.5;
		    double mult = 0.25 / v;
		    this.x = ((i_mat.m10 + i_mat.m01) * mult);
		    this.y = (v);
		    this.z = ((i_mat.m21 + i_mat.m12) * mult);
		    this.w = ((i_mat.m02 - i_mat.m20) * mult);
		}else if(elem2>elem3){
		    double v = Math.sqrt(elem2) * 0.5;
		    double mult = 0.25 / v;
		    this.x =((i_mat.m02 + i_mat.m20) * mult);
		    this.y =((i_mat.m21 + i_mat.m12) * mult);
		    this.z =(v);
		    this.w =((i_mat.m10 - i_mat.m01) * mult);
		}else{
		    double v = Math.sqrt(elem3) * 0.5;
		    double mult = 0.25 / v;
		    this.x =((i_mat.m21 - i_mat.m12) * mult);
		    this.y =((i_mat.m02 - i_mat.m20) * mult);
		    this.z =((i_mat.m10 - i_mat.m01) * mult);
		    this.w =v;
		}
		return;
	}
}
