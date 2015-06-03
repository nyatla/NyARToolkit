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

import java.util.Arrays;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResultParam;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

public class NyARIcpPointRobust extends NyARIcpPoint
{
	/**
	 * @param i_param
	 * @param i_max_points
	 * 最大頂点数
	 * @throws NyARException
	 */
	public NyARIcpPointRobust(NyARParam i_param)
	{
		super(i_param);
		// n=4で作る。
	}

	protected final double K2_FACTOR = 4.0;
	private NyARIcpUtils.JusStack __jus=new NyARIcpUtils.JusStack(16);
	private double[] __E = new double[16];
	private double[] __E2 = new double[16];
	private NyARIcpUtils.DeltaS __dS = new NyARIcpUtils.DeltaS();
	private NyARDoublePoint2d[] __du =NyARDoublePoint2d.createArray(16);
	private NyARIcpUtils.U __u = new NyARIcpUtils.U();
	private NyARDoubleMatrix44 __matXw2U = new NyARDoubleMatrix44();

	public boolean icpPoint(NyARDoublePoint2d[] screenCoord,
			NyARDoublePoint3d[] worldCoord, int num,
			NyARDoubleMatrix44 initMatXw2Xc, NyARDoubleMatrix44 o_matxw2xc,NyARTransMatResultParam o_result_param)
			throws NyARException
	{
		assert num >=4;
		double err0 = 0, err1;

		NyARIcpUtils.U u = this.__u;
		NyARIcpUtils.DeltaS dS = this.__dS;
		//ワークオブジェクトのリセット		
		if(this.__jus.getArraySize()<num){
			this.__jus=new NyARIcpUtils.JusStack(num);
			this.__E = new double[num];
			this.__E2 = new double[num];
			this.__du =NyARDoublePoint2d.createArray(num);
		}
		NyARIcpUtils.JusStack jus=this.__jus;
		double[] E=this.__E;
		double[] E2=this.__E2;
		NyARDoublePoint2d[] du=this.__du;
		NyARDoubleMatrix44 matXw2U = this.__matXw2U;
		
		int inlierNum = (int) (num * this.getInlierProbability())-1;
		if (inlierNum < 3) {
			inlierNum = 3;
		}

		o_matxw2xc.setValue(initMatXw2Xc);
		for (int i = 0;; i++) {
			matXw2U.mul(this._ref_matXc2U, o_matxw2xc);
			for (int j = 0; j < num; j++) {
				if (!u.setXbyMatX2U(matXw2U,worldCoord[j]))
				{
					return false;
				}
				double dx = screenCoord[j].x - u.x;
				double dy = screenCoord[j].y - u.y;
				du[j].x=dx;
				du[j].y=dy;
				E[j] = E2[j] = dx * dx + dy * dy;
			}
			Arrays.sort(E2,0,num);// qsort(E2, data->num, sizeof(ARdouble), compE);
			double K2 = E2[inlierNum] * K2_FACTOR;
			if (K2 < 16.0) {
				K2 = 16.0;
			}
			err1 = 0.0;
			for (int j = 0; j < num; j++) {
				if (E2[j] > K2) {
					err1 += K2 / 6.0;
				} else {
					err1 += K2 / 6.0 * (1.0 - (1.0 - E2[j] / K2) * (1.0 - E2[j] / K2) * (1.0 - E2[j] / K2));
				}
			}
			err1 /= num;

			if (err1 < this.breakLoopErrorThresh) {
				break;
			}
			if (i > 0 && err1 < this.breakLoopErrorThresh2 && err1 / err0 > this.breakLoopErrorRatioThresh) {
				break;
			}
			if (i == this._maxLoop) {
				break;
			}
			err0 = err1;
			jus.clear();			
			for (int j = 0; j < num; j++) {
				if (E[j] <= K2){
					double W = (1.0 - E[j] / K2) * (1.0 - E[j] / K2);
					if(!jus.push(this._ref_matXc2U,o_matxw2xc, worldCoord[j],du[j],W)){
						return false;
					}
				}
			}
			if (jus.getLength() < 3) {
				return false;
			}
			if (!dS.setJusArray(jus)) {
				return false;
			}
			dS.makeMat(o_matxw2xc);
		}
		if(o_result_param!=null){
			o_result_param.last_error=err1;
		}
		return true;
	}
}
