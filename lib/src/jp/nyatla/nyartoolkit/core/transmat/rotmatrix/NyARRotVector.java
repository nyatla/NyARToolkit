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
package jp.nyatla.nyartoolkit.core.transmat.rotmatrix;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;
import jp.nyatla.nyartoolkit.core.param.*;

/**
 * このクラスは、ベクトル(直線)から回転行列を計算する関数を定義します。
 * 通常、ユーザがこのクラスを使うことはありません。{@link NyARRotMatrix}クラスから使います。
 */
public class NyARRotVector
{

	/** ベクトル要素1*/
	public double v1;
	/** ベクトル要素2*/
	public double v2;
	/** ベクトル要素3*/
	public double v3;

	//privateメンバ達	
	private NyARPerspectiveProjectionMatrix _projection_mat_ref;
	private NyARDoubleMatrix44 _inv_cpara=new NyARDoubleMatrix44();

	/**
	 * コンストラクタです。
	 * 射影変換オブジェクトの参照値を設定して、インスタンスを作成します。
	 * @param i_cmat
	 * 射影変換オブジェクト。この値はインスタンスの生存中は変更しないでください。
	 * @throws NyARException
	 */
	public NyARRotVector(NyARPerspectiveProjectionMatrix i_cmat) throws NyARException
	{
		this._inv_cpara.inverse(i_cmat);
		this._projection_mat_ref = i_cmat;
	}

	/**
	 * この関数は、２直線に直交するベクトルを計算して、その３次元ベクトルをインスタンスに格納します。
	 * （多分）
	 * @param i_linear1
	 * 直線１
	 * @param i_linear2
	 * 直線２
	 */
	public void exteriorProductFromLinear(NyARLinear i_linear1, NyARLinear i_linear2)
	{
		//1行目
		final NyARPerspectiveProjectionMatrix cmat= this._projection_mat_ref;
		final double w1 = i_linear1.a * i_linear2.b - i_linear2.a * i_linear1.b;
		final double w2 = i_linear1.b * i_linear2.c - i_linear2.b * i_linear1.c;
		final double w3 = i_linear1.c * i_linear2.a - i_linear2.c * i_linear1.a;

		final double m0 = w1 * (cmat.m01 * cmat.m12 - cmat.m02 * cmat.m11) + w2 * cmat.m11 - w3 * cmat.m01;//w1 * (cpara[0 * 4 + 1] * cpara[1 * 4 + 2] - cpara[0 * 4 + 2] * cpara[1 * 4 + 1]) + w2 * cpara[1 * 4 + 1] - w3 * cpara[0 * 4 + 1];
		final double m1 = -w1 * cmat.m00 * cmat.m12 + w3 * cmat.m00;//-w1 * cpara[0 * 4 + 0] * cpara[1 * 4 + 2] + w3 * cpara[0 * 4 + 0];
		final double m2 = w1 * cmat.m00 * cmat.m11;//w1 * cpara[0 * 4 + 0] * cpara[1 * 4 + 1];
		final double w = Math.sqrt(m0 * m0 + m1 * m1 + m2 * m2);
		this.v1 = m0 / w;
		this.v2 = m1 / w;
		this.v3 = m2 / w;
		return;
	}
	/**
	 * この関数は、ARToolKitのcheck_dir関数に相当します。
	 * 詳細は不明です。(ベクトルの開始/終了座標を指定して、ベクトルの方向を調整？)
	 * @param i_start_vertex
	 * 開始位置？
	 * @param i_end_vertex
	 * 終了位置？
	 * @throws NyARException
	 */
	public void checkVectorByVertex(final NyARDoublePoint2d i_start_vertex, final NyARDoublePoint2d i_end_vertex) throws NyARException
	{
		double h;
		NyARDoubleMatrix44 inv_cpara = this._inv_cpara;
		//final double[] world = __checkVectorByVertex_world;// [2][3];
		final double world0 = inv_cpara.m00 * i_start_vertex.x * 10.0 + inv_cpara.m01 * i_start_vertex.y * 10.0 + inv_cpara.m02 * 10.0;// mat_a->m[0]*st[0]*10.0+
		final double world1 = inv_cpara.m10 * i_start_vertex.x * 10.0 + inv_cpara.m11 * i_start_vertex.y * 10.0 + inv_cpara.m12 * 10.0;// mat_a->m[3]*st[0]*10.0+
		final double world2 = inv_cpara.m20 * i_start_vertex.x * 10.0 + inv_cpara.m21 * i_start_vertex.y * 10.0 + inv_cpara.m22 * 10.0;// mat_a->m[6]*st[0]*10.0+
		final double world3 = world0 + this.v1;
		final double world4 = world1 + this.v2;
		final double world5 = world2 + this.v3;
		// </Optimize>

		final NyARPerspectiveProjectionMatrix cmat= this._projection_mat_ref;
		h = cmat.m20 * world0 + cmat.m21 * world1 + cmat.m22 * world2;
		if (h == 0.0) {
			throw new NyARException();
		}
		final double camera0 = (cmat.m00 * world0 + cmat.m01 * world1 + cmat.m02 * world2) / h;
		final double camera1 = (cmat.m10 * world0 + cmat.m11 * world1 + cmat.m12 * world2) / h;

		//h = cpara[2 * 4 + 0] * world3 + cpara[2 * 4 + 1] * world4 + cpara[2 * 4 + 2] * world5;
		h = cmat.m20 * world3 + cmat.m21 * world4 + cmat.m22 * world5;
		if (h == 0.0) {
			throw new NyARException();
		}
		final double camera2 = (cmat.m00 * world3 + cmat.m01 * world4 + cmat.m02 * world5) / h;
		final double camera3 = (cmat.m10 * world3 + cmat.m11 * world4 + cmat.m12 * world5) / h;

		final double v = (i_end_vertex.x - i_start_vertex.x) * (camera2 - camera0) + (i_end_vertex.y - i_start_vertex.y) * (camera3 - camera1);
		if (v < 0) {
			this.v1 = -this.v1;
			this.v2 = -this.v2;
			this.v3 = -this.v3;
		}
	}
	/**
	 * この関数は、ARToolKitのcheck_rotationに相当する計算をします。
	 * 詳細は不明です。(2つのベクトルの関係を調整？)
	 * @throws NyARException
	 */
	public final static void checkRotation(NyARRotVector io_vec1, NyARRotVector io_vec2) throws NyARException
	{
		double w;
		int f;

		double vec10 = io_vec1.v1;
		double vec11 = io_vec1.v2;
		double vec12 = io_vec1.v3;
		double vec20 = io_vec2.v1;
		double vec21 = io_vec2.v2;
		double vec22 = io_vec2.v3;
		
		double vec30 = vec11 * vec22 - vec12 * vec21;
		double vec31 = vec12 * vec20 - vec10 * vec22;
		double vec32 = vec10 * vec21 - vec11 * vec20;
		w = Math.sqrt(vec30 * vec30 + vec31 * vec31 + vec32 * vec32);
		if (w == 0.0) {
			throw new NyARException();
		}
		vec30 /= w;
		vec31 /= w;
		vec32 /= w;

		double cb = vec10 * vec20 + vec11 * vec21 + vec12 * vec22;
		if (cb < 0){
			cb=-cb;//cb *= -1.0;			
		}
		final double ca = (Math.sqrt(cb + 1.0) + Math.sqrt(1.0 - cb)) * 0.5;

		if (vec31 * vec10 - vec11 * vec30 != 0.0) {
			f = 0;
		} else {
			if (vec32 * vec10 - vec12 * vec30 != 0.0) {
				w = vec11;vec11 = vec12;vec12 = w;
				w = vec31;vec31 = vec32;vec32 = w;
				f = 1;
			} else {
				w = vec10;vec10 = vec12;vec12 = w;
				w = vec30;vec30 = vec32;vec32 = w;
				f = 2;
			}
		}
		if (vec31 * vec10 - vec11 * vec30 == 0.0) {
			throw new NyARException();
		}
		
		double k1,k2,k3,k4;
		double a, b, c, d;
		double p1, q1, r1;
		double p2, q2, r2;
		double p3, q3, r3;
		double p4, q4, r4;		
		
		
		k1 = (vec11 * vec32 - vec31 * vec12) / (vec31 * vec10 - vec11 * vec30);
		k2 = (vec31 * ca) / (vec31 * vec10 - vec11 * vec30);
		k3 = (vec10 * vec32 - vec30 * vec12) / (vec30 * vec11 - vec10 * vec31);
		k4 = (vec30 * ca) / (vec30 * vec11 - vec10 * vec31);

		a = k1 * k1 + k3 * k3 + 1;
		b = k1 * k2 + k3 * k4;
		c = k2 * k2 + k4 * k4 - 1;

		d = b * b - a * c;
		if (d < 0) {
			throw new NyARException();
		}
		r1 = (-b + Math.sqrt(d)) / a;
		p1 = k1 * r1 + k2;
		q1 = k3 * r1 + k4;
		r2 = (-b - Math.sqrt(d)) / a;
		p2 = k1 * r2 + k2;
		q2 = k3 * r2 + k4;
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

		if (vec31 * vec20 - vec21 * vec30 != 0.0) {
			f = 0;
		} else {
			if (vec32 * vec20 - vec22 * vec30 != 0.0) {
				w = vec21;vec21 = vec22;vec22 = w;
				w = vec31;vec31 = vec32;vec32 = w;
				f = 1;
			} else {
				w = vec20;vec20 = vec22;vec22 = w;
				w = vec30;vec30 = vec32;vec32 = w;
				f = 2;
			}
		}
		if (vec31 * vec20 - vec21 * vec30 == 0.0) {
			throw new NyARException();
		}
		k1 = (vec21 * vec32 - vec31 * vec22) / (vec31 * vec20 - vec21 * vec30);
		k2 = (vec31 * ca) / (vec31 * vec20 - vec21 * vec30);
		k3 = (vec20 * vec32 - vec30 * vec22) / (vec30 * vec21 - vec20 * vec31);
		k4 = (vec30 * ca) / (vec30 * vec21 - vec20 * vec31);

		a = k1 * k1 + k3 * k3 + 1;
		b = k1 * k2 + k3 * k4;
		c = k2 * k2 + k4 * k4 - 1;

		d = b * b - a * c;
		if (d < 0) {
			throw new NyARException();
		}
		r3 = (-b + Math.sqrt(d)) / a;
		p3 = k1 * r3 + k2;
		q3 = k3 * r3 + k4;
		r4 = (-b - Math.sqrt(d)) / a;
		p4 = k1 * r4 + k2;
		q4 = k3 * r4 + k4;
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

		double e1 = p1 * p3 + q1 * q3 + r1 * r3;
		if (e1 < 0) {
			e1 = -e1;
		}
		double e2 = p1 * p4 + q1 * q4 + r1 * r4;
		if (e2 < 0) {
			e2 = -e2;
		}
		double e3 = p2 * p3 + q2 * q3 + r2 * r3;
		if (e3 < 0) {
			e3 = -e3;
		}
		double e4 = p2 * p4 + q2 * q4 + r2 * r4;
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
