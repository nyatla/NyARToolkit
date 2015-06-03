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
import jp.nyatla.nyartoolkit.core.icp.NyARIcp;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResultParam;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.*;

public class NyARIcpPoint_Base extends NyARIcp {

	public NyARIcpPoint_Base(NyARParam i_param) throws NyARException
	{
		super(i_param);
		// n=4で作る。
	}

	public boolean icpPoint(NyARDoublePoint2d[] screenCoord,
			NyARDoublePoint3d[] worldCoord, int num,
			NyARDoubleMatrix44 initMatXw2Xc, NyARDoubleMatrix44 o_matxw2xc,NyARTransMatResultParam o_result_param)
			throws NyARException
	{
		NyARDoublePoint2d U = new NyARDoublePoint2d();
		double err0 = 0, err1;
		assert num >= 4;

		double[][] J_U_S = new double[num * 2][6];
		double dU[] = new double[num * 2];

		o_matxw2xc.setValue(initMatXw2Xc);

		double breakLoopErrorThresh = this.getBreakLoopErrorThresh();
		double breakLoopErrorThresh2 = this.getBreakLoopErrorThresh2();
		double breakLoopErrorRatioThresh = this.getBreakLoopErrorRatioThresh();
		double maxLoop = this.getMaxLoop();

		for (int i = 0;; i++) {
			NyARDoubleMatrix44 matXw2U = new NyARDoubleMatrix44();
			matXw2U.mul(this._ref_matXc2U, o_matxw2xc);
			err1 = 0.0;
			for (int j = 0; j < num; j++) {
				if (!NyARIcpUtils_Base.icpGetU_from_X_by_MatX2U(U, matXw2U,
						worldCoord[j])) {
					return false;
				}
				double dx = screenCoord[j].x - U.x;
				double dy = screenCoord[j].y - U.y;
				err1 += dx * dx + dy * dy;
				dU[j * 2 + 0] = dx;
				dU[j * 2 + 1] = dy;
			}
			err1 /= num;
			if (err1 < breakLoopErrorThresh) {
				break;
			}
			if ((i > 0) && (err1 < breakLoopErrorThresh2)
					&& (err1 / err0 > breakLoopErrorRatioThresh)) {
				break;
			}
			if (i == maxLoop) {
				break;
			}
			err0 = err1;
			for (int j = 0; j < num; j++) {
				if (!NyARIcpUtils_Base.icpGetJ_U_S(J_U_S, j * 2, this._ref_matXc2U,
						o_matxw2xc, worldCoord[j])) {
					return false;
				}
			}
			NyARMat dS = new NyARMat(6, 1);
			if (!NyARIcpUtils_Base.icpGetDeltaS(dS, dU, J_U_S, num * 2)) {
				return false;
			}
			NyARIcpUtils_Base.icpUpdateMat(o_matxw2xc, dS);

		}
		if(o_result_param!=null){
			o_result_param.last_error=err1;
		}
		return true;
	}

}
