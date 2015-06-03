/* 
 * PROJECT: NyARToolkit Professional
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2012 Ryo Iizuka
 * wm@nyatla.jp
 * http://nyatla.jp
 * 
 * This work is based on the ARToolKit4.
 * Copyright 2010-2011 ARToolworks, Inc. All rights reserved.
 *
 */
package jp.nyatla.nyartoolkit.core.icp.base;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.NyARMat;
import jp.nyatla.nyartoolkit.core.icp.NyARIcpStereo;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.*;

public class NyARIcpStereoPoint_Base extends NyARIcpStereo {

	public NyARIcpStereoPoint_Base(NyARParam i_param_l, NyARParam i_param_r,
			NyARDoubleMatrix44 i_matC2_l, NyARDoubleMatrix44 i_matC2_r)
			throws NyARException {
		super(i_param_l, i_param_r, i_matC2_l, i_matC2_r);
		throw new NyARException("This function is not checked.");
	}

	public boolean icpStereoPoint(NyARDoublePoint2d[] screenCoord_l,
			NyARDoublePoint3d[] worldCoord_l, int num_l,
			NyARDoublePoint2d[] screenCoord_r,
			NyARDoublePoint3d[] worldCoord_r, int num_r,
			NyARDoubleMatrix44 initMatXw2Xc, NyARDoubleMatrix44 matXw2Xc)
			throws NyARException {
		assert num_l + num_r >= 3;
		double err0 = 0, err1;
		// 6*2*num?
		double[][] J_U_S = new double[(num_l + num_r) * 2][6];
		double dU[] = new double[num_l + num_r * 2];
		matXw2Xc.setValue(initMatXw2Xc);
		NyARDoubleMatrix44 matXc2Ul = new NyARDoubleMatrix44();
		NyARDoubleMatrix44 matXc2Ur = new NyARDoubleMatrix44();

		matXc2Ul.mul(this._ref_matXcl2Ul, this._matC2L);
		matXc2Ur.mul(this._ref_matXcr2Ur, this._matC2R);

		NyARDoubleMatrix44 matXw2Ul = new NyARDoubleMatrix44();
		NyARDoubleMatrix44 matXw2Ur = new NyARDoubleMatrix44();
		NyARDoublePoint2d U = new NyARDoublePoint2d();
		for (int i = 0;; i++) {
			matXw2Ul.mul(matXc2Ul, matXw2Xc);
			matXw2Ur.mul(matXc2Ur, matXw2Xc);

			err1 = 0.0;
			for (int j = 0; j < num_l; j++) {
				if (!NyARIcpUtils_Base.icpGetU_from_X_by_MatX2U(U, matXw2Ul,
						worldCoord_l[j])) {
					return false;
				}
				double dx = screenCoord_l[j].x - U.x;
				double dy = screenCoord_l[j].y - U.y;
				err1 += dx * dx + dy * dy;
				dU[j * 2 + 0] = dx;
				dU[j * 2 + 1] = dy;
			}
			for (int j = 0; j < num_r; j++) {
				if (!NyARIcpUtils_Base.icpGetU_from_X_by_MatX2U(U, matXw2Ur,
						worldCoord_r[j])) {
					return false;
				}
				double dx = screenCoord_r[j].x - U.x;
				double dy = screenCoord_r[j].y - U.y;
				err1 += dx * dx + dy * dy;
				dU[(num_l + j) * 2 + 0] = dx;
				dU[(num_l + j) * 2 + 1] = dy;
			}
			err1 /= (num_l + num_r);

			if (err1 < this.breakLoopErrorThresh) {
				break;
			}
			if (i > 0 && err1 < ICP_BREAK_LOOP_ERROR_THRESH2
					&& err1 / err0 > this.breakLoopErrorRatioThresh) {
				break;
			}
			if (i == this.maxLoop) {
				break;
			}
			err0 = err1;

			for (int j = 0; j < num_l; j++) {
				if (!NyARIcpUtils_Base.icpGetJ_U_S(J_U_S, j * 2,
						this._ref_matXcl2Ul, matXw2Xc, worldCoord_l[j])) {
					return false;
				}
			}
			for (int j = 0; j < num_r; j++) {
				if (!NyARIcpUtils_Base.icpGetJ_U_S(J_U_S, j * 2,
						this._ref_matXcr2Ur, matXw2Xc, worldCoord_r[j])) {
					return false;
				}
			}
			NyARMat dS = new NyARMat(6, 1);
			if (!NyARIcpUtils_Base.icpGetDeltaS(dS, dU, J_U_S,
					(num_r + num_l) * 2)) {
				return false;
			}
			NyARIcpUtils_Base.icpUpdateMat(matXw2Xc, dS);
		}

		return false;
	}
}
