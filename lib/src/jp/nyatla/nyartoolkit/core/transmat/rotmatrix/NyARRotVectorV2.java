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

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;
import jp.nyatla.nyartoolkit.core.param.*;

/**
 * このクラスは、{@link NyARRotMatrix}クラスに、ベクトル(直線)から回転行列を計算する機能を追加します。
 * 通常、ユーザがこのクラスを使うことはありません。{@link NyARRotMatrix}クラスから使います。
 */
public class NyARRotVectorV2 extends NyARRotVector
{
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
	public NyARRotVectorV2(NyARPerspectiveProjectionMatrix i_cmat) throws NyARException
	{
		super();
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
	public void checkVectorByVertex(NyARDoublePoint2d i_start_vertex,NyARDoublePoint2d i_end_vertex) throws NyARException
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
}
