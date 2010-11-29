/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
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
package jp.nyatla.nyartoolkit.core.utils;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;

/**
 * NyARPerspectiveParamGeneratorを最適化したクラスです。
 */
public class NyARPerspectiveParamGenerator_O1 extends NyARPerspectiveParamGenerator
{
	public NyARPerspectiveParamGenerator_O1(int i_local_x, int i_local_y, int i_width, int i_height)
	{
		super(i_local_x,i_local_y, i_width, i_height);
		return;
	}


	final public boolean getParam(final NyARIntPoint2d[] i_vertex, double[] o_param) throws NyARException
	{
		final double ltx = this._local_x;
		final double lty = this._local_y;
		final double rbx = ltx + this._width;
		final double rby = lty + this._height;

		double det_1;
		double a13, a14, a23, a24, a33, a34, a43, a44;
		double b11, b12, b13, b14, b21, b22, b23, b24, b31, b32, b33, b34, b41, b42, b43, b44;
		double t1, t2, t3, t4, t5, t6;
		double v1, v2, v3, v4;
		double kx0, kx1, kx2, kx3, kx4, kx5, kx6, kx7;
		double ky0, ky1, ky2, ky3, ky4, ky5, ky6, ky7;
		{
			v1 = i_vertex[0].x;
			v2 = i_vertex[1].x;
			v3 = i_vertex[2].x;
			v4 = i_vertex[3].x;
			a13 = -ltx * v1;
			a14 = -lty * v1;
			a23 = -rbx * v2;
			a24 = -lty * v2;
			a33 = -rbx * v3;
			a34 = -rby * v3;
			a43 = -ltx * v4;
			a44 = -rby * v4;

			t1 = a33 * a44 - a34 * a43;
			t4 = a34 * ltx - rbx * a44;
			t5 = rbx * a43 - a33 * ltx;
			t2 = rby * (a34 - a44);
			t3 = rby * (a43 - a33);
			t6 = rby * (rbx - ltx);

			b21 = -a23 * t4 - a24 * t5 - rbx * t1;
			b11 = (a23 * t2 + a24 * t3) + lty * t1;
			b31 = (a24 * t6 - rbx * t2) + lty * t4;
			b41 = (-rbx * t3 - a23 * t6) + lty * t5;

			t1 = a43 * a14 - a44 * a13;
			t2 = a44 * lty - rby * a14;
			t3 = rby * a13 - a43 * lty;
			t4 = ltx * (a44 - a14);
			t5 = ltx * (a13 - a43);
			t6 = ltx * (lty - rby);

			b12 = -rby * t1 - a33 * t2 - a34 * t3;
			b22 = (a33 * t4 + a34 * t5) + rbx * t1;
			b32 = (-a34 * t6 - rby * t4) + rbx * t2;
			b42 = (-rby * t5 + a33 * t6) + rbx * t3;

			t1 = a13 * a24 - a14 * a23;
			t4 = a14 * rbx - ltx * a24;
			t5 = ltx * a23 - a13 * rbx;
			t2 = lty * (a14 - a24);
			t3 = lty * (a23 - a13);
			t6 = lty * (ltx - rbx);

			b23 = -a43 * t4 - a44 * t5 - ltx * t1;
			b13 = (a43 * t2 + a44 * t3) + rby * t1;
			b33 = (a44 * t6 - ltx * t2) + rby * t4;
			b43 = (-ltx * t3 - a43 * t6) + rby * t5;

			t1 = a23 * a34 - a24 * a33;
			t2 = a24 * rby - lty * a34;
			t3 = lty * a33 - a23 * rby;
			t4 = rbx * (a24 - a34);
			t5 = rbx * (a33 - a23);
			t6 = rbx * (rby - lty);

			b14 = -lty * t1 - a13 * t2 - a14 * t3;
			b24 = a13 * t4 + a14 * t5 + ltx * t1;
			b34 = -a14 * t6 - lty * t4 + ltx * t2;
			b44 = -lty * t5 + a13 * t6 + ltx * t3;

			det_1 = (ltx * (b11 + b14) + rbx * (b12 + b13));
			if (det_1 == 0) {
				det_1=0.0001;
				//System.out.println("Could not get inverse matrix(1).");					
				//return false;
			}
			det_1 = 1 / det_1;

			kx0 = (b11 * v1 + b12 * v2 + b13 * v3 + b14 * v4) * det_1;
			kx1 = (b11 + b12 + b13 + b14) * det_1;
			kx2 = (b21 * v1 + b22 * v2 + b23 * v3 + b24 * v4) * det_1;
			kx3 = (b21 + b22 + b23 + b24) * det_1;
			kx4 = (b31 * v1 + b32 * v2 + b33 * v3 + b34 * v4) * det_1;
			kx5 = (b31 + b32 + b33 + b34) * det_1;
			kx6 = (b41 * v1 + b42 * v2 + b43 * v3 + b44 * v4) * det_1;
			kx7 = (b41 + b42 + b43 + b44) * det_1;
		}
		{
			v1 = i_vertex[0].y;
			v2 = i_vertex[1].y;
			v3 = i_vertex[2].y;
			v4 = i_vertex[3].y;
			a13 = -ltx * v1;
			a14 = -lty * v1;
			a23 = -rbx * v2;
			a24 = -lty * v2;
			a33 = -rbx * v3;
			a34 = -rby * v3;
			a43 = -ltx * v4;
			a44 = -rby * v4;

			t1 = a33 * a44 - a34 * a43;
			t4 = a34 * ltx - rbx * a44;
			t5 = rbx * a43 - a33 * ltx;
			t2 = rby * (a34 - a44);
			t3 = rby * (a43 - a33);
			t6 = rby * (rbx - ltx);

			b21 = -a23 * t4 - a24 * t5 - rbx * t1;
			b11 = (a23 * t2 + a24 * t3) + lty * t1;
			b31 = (a24 * t6 - rbx * t2) + lty * t4;
			b41 = (-rbx * t3 - a23 * t6) + lty * t5;

			t1 = a43 * a14 - a44 * a13;
			t2 = a44 * lty - rby * a14;
			t3 = rby * a13 - a43 * lty;
			t4 = ltx * (a44 - a14);
			t5 = ltx * (a13 - a43);
			t6 = ltx * (lty - rby);

			b12 = -rby * t1 - a33 * t2 - a34 * t3;
			b22 = (a33 * t4 + a34 * t5) + rbx * t1;
			b32 = (-a34 * t6 - rby * t4) + rbx * t2;
			b42 = (-rby * t5 + a33 * t6) + rbx * t3;

			t1 = a13 * a24 - a14 * a23;
			t4 = a14 * rbx - ltx * a24;
			t5 = ltx * a23 - a13 * rbx;
			t2 = lty * (a14 - a24);
			t3 = lty * (a23 - a13);
			t6 = lty * (ltx - rbx);

			b23 = -a43 * t4 - a44 * t5 - ltx * t1;
			b13 = (a43 * t2 + a44 * t3) + rby * t1;
			b33 = (a44 * t6 - ltx * t2) + rby * t4;
			b43 = (-ltx * t3 - a43 * t6) + rby * t5;

			t1 = a23 * a34 - a24 * a33;
			t2 = a24 * rby - lty * a34;
			t3 = lty * a33 - a23 * rby;
			t4 = rbx * (a24 - a34);
			t5 = rbx * (a33 - a23);
			t6 = rbx * (rby - lty);

			b14 = -lty * t1 - a13 * t2 - a14 * t3;
			b24 = a13 * t4 + a14 * t5 + ltx * t1;
			b34 = -a14 * t6 - lty * t4 + ltx * t2;
			b44 = -lty * t5 + a13 * t6 + ltx * t3;

			det_1 = (ltx * (b11 + b14) + rbx * (b12 + b13));
			if (det_1 == 0) {
				det_1=0.0001;
				//System.out.println("Could not get inverse matrix(2).");				
				//return false;
			}
			det_1 = 1 / det_1;

			ky0 = (b11 * v1 + b12 * v2 + b13 * v3 + b14 * v4) * det_1;
			ky1 = (b11 + b12 + b13 + b14) * det_1;
			ky2 = (b21 * v1 + b22 * v2 + b23 * v3 + b24 * v4) * det_1;
			ky3 = (b21 + b22 + b23 + b24) * det_1;
			ky4 = (b31 * v1 + b32 * v2 + b33 * v3 + b34 * v4) * det_1;
			ky5 = (b31 + b32 + b33 + b34) * det_1;
			ky6 = (b41 * v1 + b42 * v2 + b43 * v3 + b44 * v4) * det_1;
			ky7 = (b41 + b42 + b43 + b44) * det_1;
		}

		det_1 = kx5 * (-ky7) - (-ky5) * kx7;
		if (det_1 == 0) {
			det_1=0.0001;
			//System.out.println("Could not get inverse matrix(3).");
			//return false;
		}
		det_1 = 1 / det_1;

		double C, F;
		o_param[2] = C = (-ky7 * det_1) * (kx4 - ky4) + (ky5 * det_1) * (kx6 - ky6); // C
		o_param[5] = F = (-kx7 * det_1) * (kx4 - ky4) + (kx5 * det_1) * (kx6 - ky6); // F
		o_param[6] = kx4 - C * kx5;
		o_param[7] = kx6 - C * kx7;
		o_param[0] = kx0 - C * kx1;
		o_param[1] = kx2 - C * kx3;
		o_param[3] = ky0 - F * ky1;
		o_param[4] = ky2 - F * ky3;
		return true;
	}
}
