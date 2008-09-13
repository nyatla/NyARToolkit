/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core2.rasterfilter.gs2bin;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.INyARRasterFilter_GsToBin;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * 定数閾値による2値化をする。
 * 
 */
public class NyARRasterFilter_Threshold implements INyARRasterFilter_GsToBin
{
	private int _threshold;

	public NyARRasterFilter_Threshold(int i_threshold)
	{
		this._threshold = i_threshold;
	}

	public void doFilter(NyARGlayscaleRaster i_input, NyARBinRaster i_output) throws NyARException
	{
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);

		final int[][] out_buf = (int[][]) i_output.getBufferReader().getBuffer();
		final int[][] in_buf = (int[][]) i_input.getBufferReader().getBuffer();

		int bp = 0;
		NyARIntSize size = i_output.getSize();
		for (int y = 0; y < size.h - 1; y++) {
			for (int x = 0; x < size.w; x++) {
				out_buf[y][x] = in_buf[y][x] >= this._threshold ? 1 : 0;
				bp += 3;
			}
		}
		return;
	}
}
