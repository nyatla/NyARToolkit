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

import java.util.Arrays;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.NyARMat;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

public class NyARIcpPointRobust_Base extends NyARIcpPoint_Base {
	public NyARIcpPointRobust_Base(NyARParam i_param) throws NyARException {
		super(i_param);
		// n=4で作る。
	}

	protected final double K2_FACTOR = 4.0;

	public boolean icpPoint(NyARDoublePoint2d[] screenCoord,
			NyARDoublePoint3d[] worldCoord, int num,
			NyARDoubleMatrix44 initMatXw2Xc, NyARDoubleMatrix44 matXw2Xc)
			throws NyARException {
		assert num >= 4;
		int inlierNum = (int) (num * this.getInlierProbability());
		if (inlierNum < 3) {
			inlierNum = 3;
		}
		NyARDoublePoint2d U = new NyARDoublePoint2d();
		double[][] J_U_S = new double[num * 2][6];
		double[] dU = new double[num * 2];
		double[] E = new double[num];
		double[] E2 = new double[num];
		double err0 = 0, err1;

		matXw2Xc.setValue(initMatXw2Xc);
		for (int i = 0;; i++) {
			NyARDoubleMatrix44 matXw2U = new NyARDoubleMatrix44();
			matXw2U.mul(this._ref_matXc2U, matXw2Xc);
			for (int j = 0; j < num; j++) {
				if (!NyARIcpUtils_Base.icpGetU_from_X_by_MatX2U(U, matXw2U,
						worldCoord[j])) {
					return false;
				}
				double dx = screenCoord[j].x - U.x;
				double dy = screenCoord[j].y - U.y;
				dU[j * 2 + 0] = dx;
				dU[j * 2 + 1] = dy;
				E[j] = E2[j] = dx * dx + dy * dy;
			}
			Arrays.sort(E2);// qsort(E2, data->num, sizeof(ARdouble), compE);
			double K2 = E2[inlierNum] * K2_FACTOR;
			if (K2 < 16.0) {
				K2 = 16.0;
			}
			err1 = 0.0;
			for (int j = 0; j < num; j++) {
				if (E2[j] > K2) {
					err1 += K2 / 6.0;
				} else {
					err1 += K2
							/ 6.0
							* (1.0 - (1.0 - E2[j] / K2) * (1.0 - E2[j] / K2)
									* (1.0 - E2[j] / K2));
				}
			}
			err1 /= num;

			if (err1 < this.breakLoopErrorThresh) {
				break;
			}
			if (i > 0 && err1 < this.breakLoopErrorThresh2
					&& err1 / err0 > this.breakLoopErrorRatioThresh) {
				break;
			}
			if (i == this._maxLoop) {
				break;
			}
			err0 = err1;

			int k = 0;
			for (int j = 0; j < num; j++) {
				if (E[j] <= K2) {
					if (!NyARIcpUtils_Base.icpGetJ_U_S(J_U_S, k, this._ref_matXc2U,
							matXw2Xc, worldCoord[j])) {
						return false;
					}
					double W = (1.0 - E[j] / K2) * (1.0 - E[j] / K2);
					J_U_S[k][0] *= W;
					J_U_S[k][1] *= W;
					J_U_S[k][2] *= W;
					J_U_S[k][3] *= W;
					J_U_S[k][4] *= W;
					J_U_S[k][5] *= W;
					J_U_S[k + 1][0] *= W;
					J_U_S[k + 1][1] *= W;
					J_U_S[k + 1][2] *= W;
					J_U_S[k + 1][3] *= W;
					J_U_S[k + 1][4] *= W;
					J_U_S[k + 1][5] *= W;
					dU[k + 0] = dU[j * 2 + 0] * W;
					dU[k + 1] = dU[j * 2 + 1] * W;
					k += 2;
				}
			}

			if (k < 6) {
				return false;
			}

			NyARMat dS = new NyARMat(6, 1);
			if (!NyARIcpUtils_Base.icpGetDeltaS(dS, dU, J_U_S, k)) {
				return false;
			}
			NyARIcpUtils_Base.icpUpdateMat(matXw2Xc, dS);
		}
		return true;
	}
}
