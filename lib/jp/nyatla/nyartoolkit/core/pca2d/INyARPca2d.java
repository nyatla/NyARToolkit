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
package jp.nyatla.nyartoolkit.core.pca2d;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix22;

public interface INyARPca2d
{
	/**
	 * 通常のPCA
	 * @param i_v1
	 * @param i_v2
	 * @param i_start
	 * @param i_number_of_point
	 * @param o_evec
	 * 要素2の変数を指定してください。
	 * @param o_ev
	 * 要素2の変数を指定してください。
	 * @param o_mean
	 * @throws NyARException
	 */
	public void pca(double[] i_v1,double[] i_v2,int i_number_of_point,NyARDoubleMatrix22 o_evec, double[] o_ev,double[] o_mean) throws NyARException;
	/**
	 * カメラ歪み補正つきのPCA
	 * @param i_x
	 * @param i_y
	 * @param i_start
	 * @param i_number_of_point
	 * @param i_factor
	 * @param o_evec
	 * @param o_mean
	 * @throws NyARException
	 */
//	public void pcaWithDistortionFactor(int[] i_x,int[] i_y,int i_start,int i_number_of_point,INyARCameraDistortionFactor i_factor,NyARDoubleMatrix22 o_evec,NyARDoublePoint2d o_ev, NyARDoublePoint2d o_mean) throws NyARException;
}
