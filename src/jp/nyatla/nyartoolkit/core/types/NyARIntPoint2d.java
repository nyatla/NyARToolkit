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

public class NyARIntPoint2d
{
	public int x;

	public int y;
	/**
	 * 配列ファクトリ
	 * @param i_number
	 * @return
	 */
	public static NyARIntPoint2d[] createArray(int i_number)
	{
		NyARIntPoint2d[] ret=new NyARIntPoint2d[i_number];
		for(int i=0;i<i_number;i++)
		{
			ret[i]=new NyARIntPoint2d();
		}
		return ret;
	}
	public static void copyArray(final NyARIntPoint2d[] i_from,NyARIntPoint2d[] i_to)
	{
		for(int i=i_from.length-1;i>=0;i--)
		{
			i_to[i].x=i_from[i].x;
			i_to[i].y=i_from[i].y;
		}
		return;
	}
}
