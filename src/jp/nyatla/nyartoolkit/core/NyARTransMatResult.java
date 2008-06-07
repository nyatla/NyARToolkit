/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
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
package jp.nyatla.nyartoolkit.core;

import jp.nyatla.nyartoolkit.*;
/**
 * NyARTransMat戻り値専用のNyARMat
 *
 */
public class NyARTransMatResult extends NyARMat
{
    private boolean has_value=false;
    public NyARTransMatResult()
    {
	super(3,4);
    }
    /**
     * この関数は使えません。
     * @param i_row
     * @param i_clm
     * @throws NyARException
     */
    public NyARTransMatResult(int i_row,int i_clm) throws NyARException
    {
	super();//ここで例外発生
    }
    /**
     * パラメータで変換行列を更新します。
     * @param i_rot
     * @param i_off
     * @param i_trans
     */
    public void updateMatrixValue(NyARTransRot i_rot,double[] i_off,double[] i_trans)
    {
	double[] pa;
	double[] rot=i_rot.getArray();

	pa=this.m[0];
        pa[0] = rot[0*3+0];
        pa[1] = rot[0*3+1];
        pa[2] = rot[0*3+2];
        pa[3] = rot[0*3+0]*i_off[0] + rot[0*3+1]*i_off[1] + rot[0*3+2]*i_off[2] + i_trans[0];

        pa=this.m[1];
        pa[0] = rot[1*3+0];
        pa[1] = rot[1*3+1];
        pa[2] = rot[1*3+2];
        pa[3] = rot[1*3+0]*i_off[0] + rot[1*3+1]*i_off[1] + rot[1*3+2]*i_off[2] + i_trans[1];

        pa=this.m[2];
        pa[0] = rot[2*3+0];
        pa[1] = rot[2*3+1];
        pa[2] = rot[2*3+2];
        pa[3] = rot[2*3+0]*i_off[0] + rot[2*3+1]*i_off[1] + rot[2*3+2]*i_off[2] + i_trans[2];


        this.has_value=true;
        return;
    }
    public void copyFrom(NyARTransMatResult i_from) throws NyARException
    {
	super.copyFrom(i_from);
        this.has_value=i_from.has_value;
    }
    public boolean hasValue()
    {
	return this.has_value;
    }
}
