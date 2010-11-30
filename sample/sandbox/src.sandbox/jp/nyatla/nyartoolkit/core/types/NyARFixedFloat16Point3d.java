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
package jp.nyatla.nyartoolkit.core.types;

import jp.nyatla.nyartoolkit.core.types.NyARFixedFloat16Point3d;
import jp.nyatla.nyartoolkit.core.types.NyARI64Point3d;

public class NyARFixedFloat16Point3d extends NyARI64Point3d
{
	/**
	 * 配列ファクトリ
	 * @param i_number
	 * @return
	 */
	public static NyARFixedFloat16Point3d[] createArray(int i_number)
	{
		NyARFixedFloat16Point3d[] ret=new NyARFixedFloat16Point3d[i_number];
		for(int i=0;i<i_number;i++)
		{
			ret[i]=new NyARFixedFloat16Point3d();
		}
		return ret;
	}
	public void copyTo(NyARDoublePoint3d i_to)
	{
		i_to.x=(double)this.x/0x10000;
		i_to.y=(double)this.y/0x10000;
		i_to.z=(double)this.z/0x10000;
		return;
	}
	public void copyFrom(NyARDoublePoint3d i_from)
	{
		this.x=(long)(i_from.x*0x10000);
		this.y=(long)(i_from.y*0x10000);
		this.z=(long)(i_from.z*0x10000);
		return;
	}

}
