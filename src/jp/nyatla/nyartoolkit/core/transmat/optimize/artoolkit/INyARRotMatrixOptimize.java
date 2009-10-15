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
package jp.nyatla.nyartoolkit.core.transmat.optimize.artoolkit;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.transmat.rotmatrix.*;
import jp.nyatla.nyartoolkit.core.types.*;

public interface INyARRotMatrixOptimize
{
	/**
	 * @param io_rot
	 * 初期回転行列
	 * @param i_trans
	 * 初期並進ベクトル
	 * @param i_vertex3d
	 * 初期3次元座標
	 * @param i_vertex2d
	 * 画面上の頂点群
	 * @return
	 * エラーレート
	 * @throws NyARException
	 */
//	public double optimize(NyARRotMatrix io_rotmat,NyARDoublePoint3d io_transvec,INyARTransportVectorSolver i_solver,NyARDoublePoint3d[] i_offset_3d,NyARDoublePoint2d[] i_2d_vertex) throws NyARException;
	public double modifyMatrix(NyARRotMatrix_ARToolKit io_rot, NyARDoublePoint3d i_trans, NyARDoublePoint3d[] i_vertex3d, NyARDoublePoint2d[] i_vertex2d) throws NyARException;
	
}
