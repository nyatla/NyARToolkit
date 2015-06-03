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
package jp.nyatla.nyartoolkit.core.icp;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.*;

public class NyARIcpStereoPoint extends NyARIcpStereo
{

	public NyARIcpStereoPoint(NyARParam i_param_l, NyARParam i_param_r,
			NyARDoubleMatrix44 i_matC2_l, NyARDoubleMatrix44 i_matC2_r)
			throws NyARException {
		super(i_param_l, i_param_r, i_matC2_l, i_matC2_r);
		throw new NyARException("This function is not checked.");
	}
	private NyARIcpUtils.JusStack __jus=new NyARIcpUtils.JusStack(16);
	private NyARDoublePoint2d[] __du=NyARDoublePoint2d.createArray(16);
	private NyARIcpUtils.DeltaS __dS = new NyARIcpUtils.DeltaS();
	private NyARIcpUtils.U __u = new NyARIcpUtils.U();

	public boolean icpStereoPoint(NyARDoublePoint2d[] screenCoord_l,
			NyARDoublePoint3d[] worldCoord_l, int num_l,
			NyARDoublePoint2d[] screenCoord_r,
			NyARDoublePoint3d[] worldCoord_r, int num_r,
			NyARDoubleMatrix44 initMatXw2Xc, NyARDoubleMatrix44 matXw2Xc)
			throws NyARException
	{
		assert num_l + num_r >= 3;
		double err0 = 0, err1;
		// 6*2*num?
		NyARIcpUtils.DeltaS dS = this.__dS;
		//ワークオブジェクトのリセット		
		if(this.__jus.getArraySize()<num_l + num_r){
			this.__jus=new NyARIcpUtils.JusStack(num_l + num_r);
			this.__du=NyARDoublePoint2d.createArray(num_l + num_r);
		}
		NyARIcpUtils.JusStack jus=this.__jus;
		NyARDoublePoint2d[] du=this.__du;
		NyARIcpUtils.U u = this.__u;
		NyARDoubleMatrix44 matXc2Ul = new NyARDoubleMatrix44();
		NyARDoubleMatrix44 matXc2Ur = new NyARDoubleMatrix44();
		NyARDoubleMatrix44 matXw2Ul = new NyARDoubleMatrix44();
		NyARDoubleMatrix44 matXw2Ur = new NyARDoubleMatrix44();

		

		matXw2Xc.setValue(initMatXw2Xc);

		matXc2Ul.mul(this._ref_matXcl2Ul, this._matC2L);
		matXc2Ur.mul(this._ref_matXcr2Ur, this._matC2R);

		for (int i = 0;; i++) {
			matXw2Ul.mul(matXc2Ul, matXw2Xc);
			matXw2Ur.mul(matXc2Ur, matXw2Xc);

			err1 = 0.0;
			for (int j = 0; j < num_l; j++) {
				if (!u.setXbyMatX2U(matXw2Ul,worldCoord_l[j])) {
					return false;
				}
				double dx = screenCoord_l[j].x - u.x;
				double dy = screenCoord_l[j].y - u.y;
				err1 += dx * dx + dy * dy;
				du[j].x=dx;
				du[j].y=dy;
			}
			for (int j = 0; j < num_r; j++) {
				if (!u.setXbyMatX2U(matXw2Ur,worldCoord_r[j])) {
					return false;
				}
				double dx = screenCoord_r[j].x - u.x;
				double dy = screenCoord_r[j].y - u.y;
				err1 += dx * dx + dy * dy;
				du[j+num_l].x=dx;
				du[j+num_l].y=dy;				
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
				if(!jus.push(this._ref_matXc2U,matXw2Xc, worldCoord_l[j],du[j],1.0)){
					return false;
				}				
			}
			for (int j = 0; j < num_r; j++) {
				if(!jus.push(this._ref_matXc2U,matXw2Xc, worldCoord_r[j],du[j],1.0)){
					return false;
				}				
			}
			if (!dS.setJusArray(jus)) {
				return false;
			}
			dS.makeMat(matXw2Xc);
		}

		return false;
	}
}
