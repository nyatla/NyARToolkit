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
package jp.nyatla.nyartoolkit.core.transmat;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.transmat.rotmatrix.NyARRotMatrix;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * NyARTransMat戻り値専用のNyARMat
 * 
 */
public class NyARTransMatResult extends NyARMat
{
	private boolean has_value = false;

	public NyARTransMatResult()
	{
		super(3, 4);
	}

	/**
	 * この関数は使えません。
	 * 
	 * @param i_row
	 * @param i_clm
	 * @throws NyARException
	 */
	public NyARTransMatResult(int i_row, int i_clm) throws NyARException
	{
		super();// ここで例外発生
	}

	/**
	 * パラメータで変換行列を更新します。
	 * 
	 * @param i_rot
	 * @param i_off
	 * @param i_trans
	 */
	public void updateMatrixValue(NyARRotMatrix i_rot, NyARDoublePoint3d i_off, NyARDoublePoint3d i_trans)
	{
		double[] pa;
		pa = this.m[0];
		pa[0] = i_rot.m00;
		pa[1] = i_rot.m01;
		pa[2] = i_rot.m02;
		pa[3] = i_rot.m00 * i_off.x + i_rot.m01 * i_off.y + i_rot.m02 * i_off.z + i_trans.x;

		pa = this.m[1];
		pa[0] = i_rot.m10;
		pa[1] = i_rot.m11;
		pa[2] = i_rot.m12;
		pa[3] = i_rot.m10 * i_off.x + i_rot.m11 * i_off.y + i_rot.m12 * i_off.z + i_trans.y;

		pa = this.m[2];
		pa[0] = i_rot.m20;
		pa[1] = i_rot.m21;
		pa[2] = i_rot.m22;
		pa[3] = i_rot.m20 * i_off.x + i_rot.m21 * i_off.y + i_rot.m22 * i_off.z + i_trans.z;

		this.has_value = true;
		return;
	}

	public void copyFrom(NyARTransMatResult i_from) throws NyARException
	{
		super.copyFrom(i_from);
		this.has_value = i_from.has_value;
	}

	public boolean hasValue()
	{
		return this.has_value;
	}
}
