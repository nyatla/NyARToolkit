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
package jp.nyatla.nyartoolkit.sandbox.x2;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARMat;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARFixedFloat16Matrix33;
import jp.nyatla.nyartoolkit.core.types.NyARFixedFloat16Point2d;

public class NyARFixedFloatRotVector
{
    //publicメンバ達
    public long v1;

    public long v2;

    public long v3;

    //privateメンバ達
    private NyARFixedFloat16Matrix33 _cmat = new NyARFixedFloat16Matrix33();

    private NyARPerspectiveProjectionMatrix _projection_mat_ref;

    private double[][] _inv_cpara_array_ref;

    public NyARFixedFloatRotVector(NyARPerspectiveProjectionMatrix i_cmat) throws NyARException
    {
    	final NyARMat mat_a = new NyARMat(3, 3);
        final double[][] a_array = mat_a.getArray();

        a_array[0][0] = i_cmat.m00;
        a_array[0][1] = i_cmat.m01;
        a_array[0][2] = i_cmat.m02;
        a_array[1][0] = i_cmat.m10;
        a_array[1][1] = i_cmat.m11;
        a_array[1][2] = i_cmat.m12;
        a_array[2][0] = i_cmat.m20;
        a_array[2][1] = i_cmat.m21;
        a_array[2][2] = i_cmat.m22;

        mat_a.matrixSelfInv();
        this._projection_mat_ref = i_cmat;
        //FixedFloat16にコピー
        this._cmat.m00 = (long)(i_cmat.m00 * NyMath.FIXEDFLOAT24_1);
        this._cmat.m01 = (long)(i_cmat.m01 * NyMath.FIXEDFLOAT24_1);
        this._cmat.m02 = (long)(i_cmat.m02 * NyMath.FIXEDFLOAT24_1);
        this._cmat.m10 = (long)(i_cmat.m10 * NyMath.FIXEDFLOAT24_1);
        this._cmat.m11 = (long)(i_cmat.m11 * NyMath.FIXEDFLOAT24_1);
        this._cmat.m12 = (long)(i_cmat.m12 * NyMath.FIXEDFLOAT24_1);
        this._cmat.m20 = (long)(i_cmat.m20 * NyMath.FIXEDFLOAT24_1);
        this._cmat.m21 = (long)(i_cmat.m21 * NyMath.FIXEDFLOAT24_1);
        this._cmat.m22 = (long)(i_cmat.m22 * NyMath.FIXEDFLOAT24_1);
        this._inv_cpara_array_ref = mat_a.getArray();
        //GCない言語のときは、ここで配列の所有権委譲してね！
    }

    /**
     * ２直線に直交するベクトルを計算する・・・だと思う。
     * @param i_linear1
     * @param i_linear2
     */
    public void exteriorProductFromLinear(NyARLinear i_linear1, NyARLinear i_linear2)
    {
        //1行目
        NyARPerspectiveProjectionMatrix cmat = this._projection_mat_ref;
        final double w1 = i_linear1.dy * i_linear2.dx - i_linear2.dy * i_linear1.dx;
        final double w2 = i_linear1.dx * i_linear2.c - i_linear2.dx * i_linear1.c;
        final double w3 = i_linear1.c * i_linear2.dy - i_linear2.c * i_linear1.dy;

        final double m0 = w1 * (cmat.m01 * cmat.m12 - cmat.m02 * cmat.m11) + w2 * cmat.m11 - w3 * cmat.m01;//w1 * (cpara[0 * 4 + 1] * cpara[1 * 4 + 2] - cpara[0 * 4 + 2] * cpara[1 * 4 + 1]) + w2 * cpara[1 * 4 + 1] - w3 * cpara[0 * 4 + 1];
        final double m1 = -w1 * cmat.m00 * cmat.m12 + w3 * cmat.m00;//-w1 * cpara[0 * 4 + 0] * cpara[1 * 4 + 2] + w3 * cpara[0 * 4 + 0];
        final double m2 = w1 * cmat.m00 * cmat.m11;//w1 * cpara[0 * 4 + 0] * cpara[1 * 4 + 1];
        final double w = Math.sqrt(m0 * m0 + m1 * m1 + m2 * m2);
        this.v1 = (long)(m0 * NyMath.FIXEDFLOAT16_1 / w);
        this.v2 = (long)(m1 * NyMath.FIXEDFLOAT16_1 / w);
        this.v3 = (long)(m2 * NyMath.FIXEDFLOAT16_1 / w);
        return;
    }

    /**
     * static int check_dir( double dir[3], double st[2], double ed[2],double cpara[3][4] ) Optimize:STEP[526->468]
     * ベクトルの開始/終了座標を指定して、ベクトルの方向を調整する。
     * @param i_start_vertex
     * @param i_end_vertex
     * @param cpara
     */
    public void checkVectorByVertex(NyARFixedFloat16Point2d i_start_vertex, NyARFixedFloat16Point2d i_end_vertex) throws NyARException
    {
        long h;
        final double[][] inv_cpara = this._inv_cpara_array_ref;
        final double stx = (double)(i_start_vertex.x / NyMath.FIXEDFLOAT16_1);
        final double sty = (double)(i_start_vertex.y / NyMath.FIXEDFLOAT16_1);

        //final double[] world = __checkVectorByVertex_world;// [2][3];
        final long world0 = (long)((inv_cpara[0][0] * stx + inv_cpara[0][1] * sty + inv_cpara[0][2]) * 10.0 * NyMath.FIXEDFLOAT16_1);// mat_a->m[0]*st[0]*10.0+
        final long world1 = (long)((inv_cpara[1][0] * stx + inv_cpara[1][1] * sty + inv_cpara[1][2]) * 10.0 * NyMath.FIXEDFLOAT16_1);// mat_a->m[3]*st[0]*10.0+
        final long world2 = (long)((inv_cpara[2][0] * stx + inv_cpara[2][1] * sty + inv_cpara[2][2]) * 10.0 * NyMath.FIXEDFLOAT16_1);// mat_a->m[6]*st[0]*10.0+
        final long world3 = world0 + (this.v1);
        final long world4 = world1 + (this.v2);
        final long world5 = world2 + (this.v3);
        // </Optimize>

        //final double[] camera = __checkVectorByVertex_camera;// [2][2];
        //h = cpara[2 * 4 + 0] * world0 + cpara[2 * 4 + 1] * world1 + cpara[2 * 4 + 2] * world2;
        h = (this._cmat.m20 * world0 + this._cmat.m21 * world1 + this._cmat.m22 * world2) >> 16;
        if (h == 0)
        {
            throw new NyARException();
        }
        //final double camera0 = (cpara[0 * 4 + 0] * world0 + cpara[0 * 4 + 1] * world1 + cpara[0 * 4 + 2] * world2) / h;
        //final double camera1 = (cpara[1 * 4 + 0] * world0 + cpara[1 * 4 + 1] * world1 + cpara[1 * 4 + 2] * world2) / h;
        final long camera0 = (this._cmat.m00 * world0 + this._cmat.m01 * world1 + this._cmat.m02 * world2) / h;
        final long camera1 = (this._cmat.m10 * world0 + this._cmat.m11 * world1 + this._cmat.m12 * world2) / h;

        //h = cpara[2 * 4 + 0] * world3 + cpara[2 * 4 + 1] * world4 + cpara[2 * 4 + 2] * world5;
        h = (this._cmat.m20 * world3 + this._cmat.m21 * world4 + this._cmat.m22 * world5) >> 16;
        if (h == 0)
        {
            throw new NyARException();
        }
        //final double camera2 = (cpara[0 * 4 + 0] * world3 + cpara[0 * 4 + 1] * world4 + cpara[0 * 4 + 2] * world5) / h;
        //final double camera3 = (cpara[1 * 4 + 0] * world3 + cpara[1 * 4 + 1] * world4 + cpara[1 * 4 + 2] * world5) / h;
        final long camera2 = (this._cmat.m00 * world3 + this._cmat.m01 * world4 + this._cmat.m02 * world5) / h;
        final long camera3 = (this._cmat.m10 * world3 + this._cmat.m11 * world4 + this._cmat.m12 * world5) / h;


        long v = ((i_end_vertex.x - i_start_vertex.x) * (camera2 - camera0) + (i_end_vertex.y - i_start_vertex.y) * (camera3 - camera1)) >> 16;

        if (v < 0)
        {
            this.v1 = -this.v1;
            this.v2 = -this.v2;
            this.v3 = -this.v3;
        }

        return;
    }
    private static int DIV0_CANCEL=1;
    /**
     * int check_rotation( double rot[2][3] )
     * 2つのベクトル引数の調整をする？
     * @param i_r
     * @throws NyARException
     */
	public final static void checkRotation(NyARFixedFloatRotVector io_vec1, NyARFixedFloatRotVector io_vec2) throws NyARException
	{
		long w;
		int f;

		long vec10 = io_vec1.v1;
		long vec11 = io_vec1.v2;
		long vec12 = io_vec1.v3;
		long vec20 = io_vec2.v1;
		long vec21 = io_vec2.v2;
		long vec22 = io_vec2.v3;
		
		long vec30 = (vec11 * vec22 - vec12 * vec21)>>16;
		long vec31 = (vec12 * vec20 - vec10 * vec22)>>16;
		long vec32 = (vec10 * vec21 - vec11 * vec20)>>16;
		w = NyMath.sqrtFixdFloat16((vec30 * vec30 + vec31 * vec31 + vec32 * vec32)>>16);
		if (w == 0) {
			w=1;//極小値
			//throw new NyARException();
		}
		vec30= (vec30<<16)/w;
		vec31= (vec31<<16)/w;
		vec32= (vec32<<16)/w;

		long cb = (vec10 * vec20 + vec11 * vec21 + vec12 * vec22)>>16;
		if (cb < 0){
			cb=-cb;//cb *= -1.0;			
		}
		final long ca = (NyMath.sqrtFixdFloat16(cb + NyMath.FIXEDFLOAT16_1) + NyMath.sqrtFixdFloat16(NyMath.FIXEDFLOAT16_1 - cb)) >>1;

		if (vec31 * vec10 - vec11 * vec30 != 0) {
			f = 0;
		} else {
			if (vec32 * vec10 - vec12 * vec30 != 0) {
				w = vec11;vec11 = vec12;vec12 = w;
				w = vec31;vec31 = vec32;vec32 = w;
				f = 1;
			} else {
				w = vec10;vec10 = vec12;vec12 = w;
				w = vec30;vec30 = vec32;vec32 = w;
				f = 2;
			}
		}
		if (vec31 * vec10 - vec11 * vec30 == 0) {
			throw new NyARException();
		}
		
		long k1,k2,k3,k4;
		long a, b, c, d;
		long p1, q1, r1;
		long p2, q2, r2;
		long p3, q3, r3;
		long p4, q4, r4;		
		
		
		k1 = ((vec11 * vec32 - vec31 * vec12)) / (DIV0_CANCEL+((vec31 * vec10 - vec11 * vec30)>>16));
		k2 = (vec31 * ca) / (DIV0_CANCEL+((vec31 * vec10 - vec11 * vec30)>>16));
		k3 = (vec10 * vec32 - vec30 * vec12) / (DIV0_CANCEL+((vec30 * vec11 - vec10 * vec31)>>16));
		k4 = (vec30 * ca) / (DIV0_CANCEL+((vec30 * vec11 - vec10 * vec31)>>16));

		a = ((k1 * k1 + k3 * k3)>>16) + NyMath.FIXEDFLOAT16_1;
		b = ((k1 * k2 + k3 * k4)>>16);
		c = ((k2 * k2 + k4 * k4)>>16) - NyMath.FIXEDFLOAT16_1;
		d = (b*b - a*c)>>16;
		if (d < 0) {
			//誤差で計算エラーが頻発するのでExceptionはしない
			//throw new NyARException();
		}
		r1 = ((-b + NyMath.sqrtFixdFloat16(d))<<16) / a;
		p1 = ((k1 * r1)>>16) + k2;
		q1 = ((k3 * r1)>>16) + k4;
		r2 = ((-b - NyMath.sqrtFixdFloat16(d))<<16) / a;
		p2 = ((k1 * r2)>>16) + k2;
		q2 = ((k3 * r2)>>16) + k4;
		if (f == 1) {
			w = q1;q1 = r1;r1 = w;
			w = q2;q2 = r2;r2 = w;
			w = vec11;vec11 = vec12;vec12 = w;
			w = vec31;vec31 = vec32;vec32 = w;
			f = 0;
		}
		if (f == 2) {
			w = p1;p1 = r1;r1 = w;
			w = p2;p2 = r2;r2 = w;
			w = vec10;vec10 = vec12;vec12 = w;
			w = vec30;vec30 = vec32;vec32 = w;
			f = 0;
		}

		if (vec31 * vec20 - vec21 * vec30 != 0) {
			f = 0;
		} else {
			if (vec32 * vec20 - vec22 * vec30 != 0) {
				w = vec21;vec21 = vec22;vec22 = w;
				w = vec31;vec31 = vec32;vec32 = w;
				f = 1;
			} else {
				w = vec20;vec20 = vec22;vec22 = w;
				w = vec30;vec30 = vec32;vec32 = w;
				f = 2;
			}
		}
		if (vec31 * vec20 - vec21 * vec30 == 0) {
			throw new NyARException();
		}
		k1 = (vec21 * vec32 - vec31 * vec22) / (DIV0_CANCEL+((vec31 * vec20 - vec21 * vec30)>>16));
		k2 = (vec31 * ca) / (DIV0_CANCEL+((vec31 * vec20 - vec21 * vec30)>>16));
		k3 = (vec20 * vec32 - vec30 * vec22) / ((vec30 * vec21 - vec20 * vec31)>>16);
		k4 = (vec30 * ca) / (DIV0_CANCEL+((vec30 * vec21 - vec20 * vec31)>>16));

		a = ((k1 * k1 + k3 * k3)>>16) + NyMath.FIXEDFLOAT16_1;
		b = ((k1 * k2 + k3 * k4)>>16);
		c = ((k2 * k2 + k4 * k4)>>16) - NyMath.FIXEDFLOAT16_1;

		d = (b*b - a*c)>>16;
		if (d < 0) {
			//誤差で計算エラーが頻発するのでExceptionはしない
//			throw new NyARException();
		}
		r3 = ((-b + NyMath.sqrtFixdFloat16(d))<<16) / a;
		p3 = ((k1 * r3)>>16) + k2;
		q3 = ((k3 * r3)>>16) + k4;
		r4 = ((-b - NyMath.sqrtFixdFloat16(d))<<16) / a;
		p4 = ((k1 * r4)>>16) + k2;
		q4 = ((k3 * r4)>>16) + k4;
		if (f == 1) {
			w = q3;q3 = r3;r3 = w;
			w = q4;q4 = r4;r4 = w;
			w = vec21;vec21 = vec22;vec22 = w;
			w = vec31;vec31 = vec32;vec32 = w;
			f = 0;
		}
		if (f == 2) {
			w = p3;p3 = r3;r3 = w;
			w = p4;p4 = r4;r4 = w;
			w = vec20;vec20 = vec22;vec22 = w;
			w = vec30;vec30 = vec32;vec32 = w;
			f = 0;
		}

		long e1 = (p1 * p3 + q1 * q3 + r1 * r3)>>16;
		if (e1 < 0) {
			e1 = -e1;
		}
		long e2 = (p1 * p4 + q1 * q4 + r1 * r4)>>16;
		if (e2 < 0) {
			e2 = -e2;
		}
		long e3 = (p2 * p3 + q2 * q3 + r2 * r3)>>16;
		if (e3 < 0) {
			e3 = -e3;
		}
		long e4 = (p2 * p4 + q2 * q4 + r2 * r4)>>16;
		if (e4 < 0) {
			e4 = -e4;
		}
		if (e1 < e2) {
			if (e1 < e3) {
				if (e1 < e4) {
					io_vec1.v1 = p1;
					io_vec1.v2 = q1;
					io_vec1.v3 = r1;
					io_vec2.v1 = p3;
					io_vec2.v2 = q3;
					io_vec2.v3 = r3;
				} else {
					io_vec1.v1 = p2;
					io_vec1.v2 = q2;
					io_vec1.v3 = r2;
					io_vec2.v1 = p4;
					io_vec2.v2 = q4;
					io_vec2.v3 = r4;
				}
			} else {
				if (e3 < e4) {
					io_vec1.v1 = p2;
					io_vec1.v2 = q2;
					io_vec1.v3 = r2;
					io_vec2.v1 = p3;
					io_vec2.v2 = q3;
					io_vec2.v3 = r3;
				} else {
					io_vec1.v1 = p2;
					io_vec1.v2 = q2;
					io_vec1.v3 = r2;
					io_vec2.v1 = p4;
					io_vec2.v2 = q4;
					io_vec2.v3 = r4;
				}
			}
		} else {
			if (e2 < e3) {
				if (e2 < e4) {
					io_vec1.v1 = p1;
					io_vec1.v2 = q1;
					io_vec1.v3 = r1;
					io_vec2.v1 = p4;
					io_vec2.v2 = q4;
					io_vec2.v3 = r4;
				} else {
					io_vec1.v1 = p2;
					io_vec1.v2 = q2;
					io_vec1.v3 = r2;
					io_vec2.v1 = p4;
					io_vec2.v2 = q4;
					io_vec2.v3 = r4;
				}
			} else {
				if (e3 < e4) {
					io_vec1.v1 = p2;
					io_vec1.v2 = q2;
					io_vec1.v3 = r2;
					io_vec2.v1 = p3;
					io_vec2.v2 = q3;
					io_vec2.v3 = r3;
				} else {
					io_vec1.v1 = p2;
					io_vec1.v2 = q2;
					io_vec1.v3 = r2;
					io_vec2.v1 = p4;
					io_vec2.v2 = q4;
					io_vec2.v3 = r4;
				}
			}
		}	
		return;
	}	
}
