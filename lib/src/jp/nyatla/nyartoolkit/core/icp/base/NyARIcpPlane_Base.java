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

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.icp.NyARIcpPoint;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.transmat.rotmatrix.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;

public class NyARIcpPlane_Base {
	protected NyARIcpPoint _icp_point;

	public NyARIcpPlane_Base(NyARParam i_param) throws NyARException {
		this.initInstance(new NyARIcpPoint(i_param));
		// n=4で作る。
	}

	public NyARIcpPlane_Base(NyARParam i_param, NyARIcpPoint i_icp_point)
			throws NyARException {
		this.initInstance(i_icp_point);
	}

	protected void initInstance(NyARIcpPoint i_icp_point) {
		this._icp_point = i_icp_point;
		return;
	}

	public boolean icpGetInitXw2Xc_from_PlanarData(
			NyARDoublePoint2d screenCoord[], NyARDoublePoint3d worldCoord[],
			int i_num, NyARDoubleMatrix44 initMatXw2Xc) throws NyARException {

		if (i_num < 4) {
			throw new NyARException();
		}
		// nを元に配列の準備
		NyARMat matA = new NyARMat(i_num * 2, 8);
		NyARMat matB = new NyARMat(i_num * 2, 1);
		double[][] bufa = matA.getArray();
		double[][] bufb = matB.getArray();
		for (int i = 0; i < i_num; i++) {
			int idx = i * 2;
			bufa[idx][0] = worldCoord[i].x; // matA->m[i*16+0] =
											// worldCoord[i].x;
			bufa[idx][1] = worldCoord[i].y; // =matA->m[i*16+1] =
											// worldCoord[i].y;
			bufa[idx][2] = 1.0;// matA->m[i*16+2] = 1.0;
			bufa[idx][3] = 0.0;// matA->m[i*16+3] = 0.0;
			bufa[idx][4] = 0.0;// matA->m[i*16+4] = 0.0;
			bufa[idx][5] = 0.0;// matA->m[i*16+5] = 0.0;
			bufa[idx][6] = 0.0 - (worldCoord[i].x) * (screenCoord[i].x);// matA->m[i*16+6]
																		// =
																		// -(worldCoord[i].x)*(screenCoord[i].x);
			bufa[idx][7] = -(worldCoord[i].y) * (screenCoord[i].x);// matA->m[i*16+7]
																	// =
																	// -(worldCoord[i].y)*(screenCoord[i].x);
			idx = i * 2 + 1;
			bufa[idx][0] = 0;// matA->m[i*16+8] = 0.0;
			bufa[idx][1] = 0;// matA->m[i*16+9] = 0.0;
			bufa[idx][2] = 0;// matA->m[i*16+10] = 0.0;
			bufa[idx][3] = worldCoord[i].x;// matA->m[i*16+11] =
											// worldCoord[i].x;
			bufa[idx][4] = worldCoord[i].y;// matA->m[i*16+12] =
											// worldCoord[i].y;
			bufa[idx][5] = 1.0;// matA->m[i*16+13] = 1.0;
			bufa[idx][6] = -(worldCoord[i].x) * (screenCoord[i].y);// matA->m[i*16+14]
																	// =
																	// -(worldCoord[i].x)*(screenCoord[i].y);
			bufa[idx][7] = -(worldCoord[i].y) * (screenCoord[i].y);// matA->m[i*16+15]
																	// =
																	// -(worldCoord[i].y)*(screenCoord[i].y);

			bufb[i * 2][0] = screenCoord[i].x;// matB->m[i*2+0] =
												// screenCoord[i].x;
			bufb[i * 2 + 1][0] = screenCoord[i].y;// matB->m[i*2+1] =
													// screenCoord[i].y;
		}
		NyARMat matAt = new NyARMat(8, i_num * 2);
		NyARMat matBt = new NyARMat(1, i_num * 2);
		matAt.transpose(matA);
		matBt.transpose(matB);

		NyARMat matAtA = new NyARMat(i_num * 2, i_num * 2);
		NyARMat matAtB = new NyARMat(8, 1);
		matAtA.mul(matAt, matA);
		matAtB.mul(matAt, matB);

		if (!matAtA.inverse()) {
			return false;
		}

		NyARMat matC = new NyARMat(8, 1);
		matC.mul(matAtA, matAtB);

		NyARRotVector vec0 = new NyARRotVector();
		NyARRotVector vec1 = new NyARRotVector();
		double t0, t1, t2;
		double[][] bufc = matC.getArray();

		NyARDoubleMatrix44 matxc = this._icp_point.refMatXc2U();

		vec0.v3 = (bufc[6][0]);
		vec0.v2 = (bufc[3][0] - matxc.m12 * vec0.v3) / matxc.m11;
		vec0.v1 = (bufc[0][0] - matxc.m02 * vec0.v3 - matxc.m01 * vec0.v2)
				/ matxc.m00;
		vec1.v3 = (bufc[7][0]);
		vec1.v2 = (bufc[4][0] - matxc.m12 * vec1.v3) / matxc.m11;
		vec1.v1 = (bufc[1][0] - matxc.m02 * vec1.v3 - matxc.m01 * vec1.v2)
				/ matxc.m00;
		t2 = 1.0;
		t1 = (bufc[5][0] - matxc.m12 * t2) / matxc.m11;
		t0 = (bufc[2][0] - matxc.m02 * t2 - matxc.m01 * t1) / matxc.m00;

		double l1 = Math.sqrt(vec0.v1 * vec0.v1 + vec0.v2 * vec0.v2 + vec0.v3
				* vec0.v3);
		double l2 = Math.sqrt(vec1.v1 * vec1.v1 + vec1.v2 * vec1.v2 + vec1.v3
				* vec1.v3);
		vec0.v1 /= l1;
		vec0.v2 /= l1;
		vec0.v3 /= l1;
		vec1.v1 /= l2;
		vec1.v2 /= l2;
		vec1.v3 /= l2;
		t0 /= (l1 + l2) / 2.0;
		t1 /= (l1 + l2) / 2.0;
		t2 /= (l1 + l2) / 2.0;
		if (t2 < 0.0) {
			vec0.v1 = -vec0.v1;
			vec0.v2 = -vec0.v2;
			vec0.v3 = -vec0.v3;
			vec1.v1 = -vec1.v1;
			vec1.v2 = -vec1.v2;
			vec1.v3 = -vec1.v3;
			t0 = -t0;
			t1 = -t1;
			t1 = -t2;
		}
		// ここまで

		if(!NyARRotVector.checkRotation(vec0, vec1)){
			return false;
		}
		double v20 = vec0.v2 * vec1.v3 - vec0.v3 * vec1.v2;
		double v21 = vec0.v3 * vec1.v1 - vec0.v1 * vec1.v3;
		double v22 = vec0.v1 * vec1.v2 - vec0.v2 * vec1.v1;
		l1 = Math.sqrt(v20 * v20 + v21 * v21 + v22 * v22);
		v20 /= l1;
		v21 /= l1;
		v22 /= l1;

		initMatXw2Xc.m00 = vec0.v1;
		initMatXw2Xc.m10 = vec0.v2;
		initMatXw2Xc.m20 = vec0.v3;
		initMatXw2Xc.m01 = vec1.v1;
		initMatXw2Xc.m11 = vec1.v2;
		initMatXw2Xc.m21 = vec1.v3;
		initMatXw2Xc.m02 = v20;
		initMatXw2Xc.m12 = v21;
		initMatXw2Xc.m22 = v22;
		initMatXw2Xc.m03 = t0;
		initMatXw2Xc.m13 = t1;
		initMatXw2Xc.m23 = t2;

		icpGetInitXw2XcSub(initMatXw2Xc, screenCoord, worldCoord, i_num,
				initMatXw2Xc);
		return true;
	}

	private void icpGetInitXw2XcSub(NyARDoubleMatrix44 rot,
			NyARDoublePoint2d[] pos2d, NyARDoublePoint3d[] ppos3d, int num,
			NyARDoubleMatrix44 conv) throws NyARException {
		double[] off = new double[3];

		NyARMat mat_d = new NyARMat(3, 3);
		NyARMat mat_e = new NyARMat(3, 1);

		double[] pmin = new double[3];
		double[] pmax = new double[3];
		pmax[0] = pmax[1] = pmax[2] = Double.NEGATIVE_INFINITY;
		pmin[0] = pmin[1] = pmin[2] = Double.POSITIVE_INFINITY;
		for (int i = 0; i < num; i++) {
			if (ppos3d[i].x > pmax[0]) {
				pmax[0] = ppos3d[i].x;
			}
			if (ppos3d[i].x < pmin[0]) {
				pmin[0] = ppos3d[i].x;
			}
			if (ppos3d[i].y > pmax[1]) {
				pmax[1] = ppos3d[i].y;
			}
			if (ppos3d[i].y < pmin[1]) {
				pmin[1] = ppos3d[i].y;
			}
			if (ppos3d[i].z > pmax[2]) {
				pmax[2] = ppos3d[i].z;
			}
			if (ppos3d[i].z < pmin[2]) {
				pmin[2] = ppos3d[i].z;
			}
		}
		off[0] = -(pmax[0] + pmin[0]) / 2.0;
		off[1] = -(pmax[1] + pmin[1]) / 2.0;
		off[2] = -(pmax[2] + pmin[2]) / 2.0;

		NyARDoublePoint3d pos3d[] = NyARDoublePoint3d.createArray(num);
		for (int i = 0; i < num; i++) {
			pos3d[i].x = ppos3d[i].x + off[0];
			pos3d[i].y = ppos3d[i].y + off[1];
			pos3d[i].z = ppos3d[i].z + off[2];
		}

		NyARDoubleMatrix44 cpara = this._icp_point.refMatXc2U();
		NyARMat mat_a = new NyARMat(num * 2, 3);
		NyARMat mat_b = new NyARMat(3, num * 2);
		NyARMat mat_c = new NyARMat(num * 2, 1);
		double[][] bufa = mat_a.getArray();
		double[][] bufb = mat_b.getArray();
		double[][] bufc = mat_c.getArray();
		for (int j = 0; j < num; j++) {
			double wx = rot.m00 * pos3d[j].x + rot.m01 * pos3d[j].y + rot.m02
					* pos3d[j].z;
			double wy = rot.m10 * pos3d[j].x + rot.m11 * pos3d[j].y + rot.m12
					* pos3d[j].z;
			double wz = rot.m20 * pos3d[j].x + rot.m21 * pos3d[j].y + rot.m22
					* pos3d[j].z;
			bufa[j * 2][0] = bufb[0][j * 2] = cpara.m00;
			bufa[j * 2][1] = bufb[1][j * 2] = cpara.m01;
			bufa[j * 2][2] = bufb[2][j * 2] = cpara.m02 - pos2d[j].x;

			bufa[j * 2 + 1][0] = bufb[0][j * 2 + 1] = 0.0;
			bufa[j * 2 + 1][1] = bufb[1][j * 2 + 1] = cpara.m11;
			bufa[j * 2 + 1][2] = bufb[2][j * 2 + 1] = cpara.m12 - pos2d[j].y;
			bufc[j * 2][0] = wz * pos2d[j].x - cpara.m00 * wx - cpara.m01 * wy
					- cpara.m02 * wz;
			bufc[j * 2 + 1][0] = wz * pos2d[j].y - cpara.m11 * wy - cpara.m12
					* wz;
		}
		mat_d.mul(mat_b, mat_a);
		mat_e.mul(mat_b, mat_c);
		mat_d.inverse();
		NyARMat mat_f = new NyARMat(3, 1);
		mat_f.mul(mat_d, mat_e);
		double[][] buff = mat_f.getArray();

		conv.setValue(rot);
		conv.m03 = buff[0][0];
		conv.m13 = buff[1][0];
		conv.m23 = buff[2][0];

		conv.m03 = conv.m00 * off[0] + conv.m01 * off[1] + conv.m02 * off[2]
				+ conv.m03;
		conv.m13 = conv.m10 * off[0] + conv.m11 * off[1] + conv.m12 * off[2]
				+ conv.m13;
		conv.m23 = conv.m20 * off[0] + conv.m21 * off[1] + conv.m22 * off[2]
				+ conv.m23;

		return;
	}
}
