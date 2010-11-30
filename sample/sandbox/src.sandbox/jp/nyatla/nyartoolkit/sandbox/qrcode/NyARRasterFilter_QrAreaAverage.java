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
package jp.nyatla.nyartoolkit.sandbox.qrcode;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.gs2bin.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * 平均移動法を使った２値化フィルタ
 * 
 */
public class NyARRasterFilter_QrAreaAverage implements INyARRasterFilter_Gs2Bin
{
	private int _area = 8;

	public void doFilter(NyARGrayscaleRaster i_input, NyARBinRaster i_output) throws NyARException
	{
		final NyARIntSize size = i_output.getSize();
		final int[] out_buf = (int[]) i_output.getBuffer();
		final int[] in_buf = (int[]) i_input.getBuffer();
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);
		assert (size.h % 8 == 0 && size.w % 8 == 0);//暫定実装なので。

		final int area = this._area;
		int y1 = area;
		int x1 = area;
		int y2 = size.h - area;
		int x2 = size.w - area;

		for (int y = y1; y < y2; y++) {
			int sum, nn;
			sum = nn = 0;
			for (int yy = y - area; yy < y + area + 1; yy++) {
				for (int xx = x1 - area; xx < x1 + area; xx++) {
					sum += in_buf[yy*size.w+xx];
					nn++;
				}
			}
			int th;
			boolean first = true;
			th=0;
			for (int x = area; x < x2; x++) {
				if (!first) {
					for (int yy = y - area; yy < y + area; yy++) {
						sum += in_buf[yy*size.w+x + area];
						sum -= in_buf[yy*size.w+x - area];
					}
				}
				first = false;
				th = (sum / nn);
				int g = in_buf[y*size.w+x];
				out_buf[y*size.w+x] = th < g ? 1 : 0;
			}
		}
		return;
	}

}
