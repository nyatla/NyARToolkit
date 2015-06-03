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

import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

public class NyARIcpStereo
{
	protected final int ICP_MAX_LOOP=10;
	protected final double ICP_BREAK_LOOP_ERROR_THRESH=0.1;
	protected final double ICP_BREAK_LOOP_ERROR_RATIO_THRESH=0.99;
	protected final double ICP_BREAK_LOOP_ERROR_THRESH2=4.0;
	protected final double ICP_INLIER_PROBABILITY=0.50;
	
	protected int maxLoop;
	protected double breakLoopErrorThresh;
	protected double breakLoopErrorRatioThresh;
	protected double breakLoopErrorThresh2;
	protected double inlierProb;
	
	protected NyARDoubleMatrix44 _ref_matXc2U;

	public NyARIcpStereo(NyARParam i_param_l,NyARParam i_param_r,NyARDoubleMatrix44 i_matC2_l,NyARDoubleMatrix44 i_matC2_r)
	{
	    this.maxLoop = ICP_MAX_LOOP;
	    this.breakLoopErrorThresh      = ICP_BREAK_LOOP_ERROR_THRESH;
	    this.breakLoopErrorRatioThresh = ICP_BREAK_LOOP_ERROR_RATIO_THRESH;
	    this.breakLoopErrorThresh2     = ICP_BREAK_LOOP_ERROR_THRESH2;
	    this.inlierProb                = ICP_INLIER_PROBABILITY;
	    
		this._ref_matXcl2Ul=i_param_l.getPerspectiveProjectionMatrix();
		this._ref_matXcr2Ur=i_param_r.getPerspectiveProjectionMatrix();
		this._matC2L=i_matC2_l;
		this._matC2R=i_matC2_r;
		return;
	}
	
	protected NyARDoubleMatrix44 _ref_matXcl2Ul;
	protected NyARDoubleMatrix44 _ref_matXcr2Ur;
	protected NyARDoubleMatrix44 _matC2L;
	protected NyARDoubleMatrix44 _matC2R;
}
