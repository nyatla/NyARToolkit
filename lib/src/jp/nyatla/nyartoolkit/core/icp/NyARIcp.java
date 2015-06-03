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
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResultParam;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.*;

public abstract class NyARIcp
{
	protected NyARDoubleMatrix44 _ref_matXc2U;
	
	private final int ICP_MAX_LOOP=10;
	private final double ICP_BREAK_LOOP_ERROR_THRESH=0.1;
	private final double ICP_BREAK_LOOP_ERROR_RATIO_THRESH=0.99;
	private final double ICP_BREAK_LOOP_ERROR_THRESH2=4.0;
	private final double ICP_INLIER_PROBABILITY=0.50;
	
	protected int _maxLoop;
	protected double breakLoopErrorThresh;
	protected double breakLoopErrorRatioThresh;
	protected double breakLoopErrorThresh2;
	protected double inlierProb;
	
	public NyARIcp(NyARParam i_param)
	{
	    this._ref_matXc2U=i_param.getPerspectiveProjectionMatrix();
	    this._maxLoop = ICP_MAX_LOOP;
	    this.breakLoopErrorThresh      = ICP_BREAK_LOOP_ERROR_THRESH;
	    this.breakLoopErrorRatioThresh = ICP_BREAK_LOOP_ERROR_RATIO_THRESH;
	    this.breakLoopErrorThresh2     = ICP_BREAK_LOOP_ERROR_THRESH2;
	    this.inlierProb                = ICP_INLIER_PROBABILITY;
	}
	public void setMatXc2U(NyARDoubleMatrix44 i_value)
	{
		this._ref_matXc2U.setValue(i_value);
	}
	public void setMaxLoop(int i_value)
	{
		this._maxLoop = i_value;
	}

	public void setBreakLoopErrorThresh(double i_value)
	{
	    this.breakLoopErrorThresh = i_value;
	}

	public void setBreakLoopErrorRatioThresh(double i_value)
	{
		this.breakLoopErrorRatioThresh=i_value;
	}

	public void setBreakLoopErrorThresh2(double i_value)
	{
	    this.breakLoopErrorThresh2 = i_value;
	}
	public NyARDoubleMatrix44 refMatXc2U()
	{
		return this._ref_matXc2U;
	}
	public void getMatXc2U(NyARDoubleMatrix44 o_ret)
	{
		o_ret.setValue(this._ref_matXc2U);
	}
	
	public int getMaxLoop()
	{
		return this._maxLoop;
	}

	public double getBreakLoopErrorThresh()
	{
		return this.breakLoopErrorThresh;
	}

	public double getBreakLoopErrorRatioThresh()
	{
		return this.breakLoopErrorRatioThresh;
	}
	public double getBreakLoopErrorThresh2()
	{
		return this.breakLoopErrorThresh2;
	}
	public void setInlierProbability(double i_value)
	{
		this.inlierProb=i_value;
	}
	public double getInlierProbability()
	{
		return this.inlierProb;
	}
	
	/**
	 * ICPアルゴリズムによる姿勢推定を行います。
	 * 
	 * @param screenCoord
	 * @param worldCoord
	 * @param num
	 * @param initMatXw2Xc
	 * @param o_matxw2xc
	 * @param o_result_param
	 * 結果パラメータを受け取るオブジェクト。不要な場合はnullを指定可能。
	 * @return
	 * @throws NyARException
	 */
	public abstract boolean icpPoint(NyARDoublePoint2d[] screenCoord,
			NyARDoublePoint3d[] worldCoord, int num,
			NyARDoubleMatrix44 initMatXw2Xc, NyARDoubleMatrix44 o_matxw2xc,NyARTransMatResultParam o_result_param)
			throws NyARException;	
}
