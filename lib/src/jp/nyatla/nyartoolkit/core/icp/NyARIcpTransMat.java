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
import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.rasterdriver.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

/**
 * This class provides ARToolkitV4 style fitting algorism.
 * 
 */
public class NyARIcpTransMat implements INyARTransMat
{
	/** This value indicates POINT algorism. */
	public final static int AL_POINT=0;
	/** This value indicates POINT_ROBUST algorism. */
	public final static int AL_POINT_ROBUST=1;

	private NyARIcpPoint _icpp;
	private NyARIcpPlane _icpc;
	/**
	 * The constructor.
	 * @param i_param
	 * ARToolkit parameter object that finished setup.
	 * @param i_al_mode
	 * fitting algorism type.
	 * @throws NyARRuntimeException
	 */
	public NyARIcpTransMat(NyARParam i_param,int i_al_mode)
	{
		NyARDoubleMatrix44 projection_mat=i_param.getPerspectiveProjectionMatrix();
		this._icpc=new NyARIcpPlane(projection_mat);
		switch(i_al_mode){
		case AL_POINT:
			this._icpp=new NyARIcpPoint(projection_mat);
			break;
		case AL_POINT_ROBUST:
			this._icpp=new NyARIcpPointRobust(projection_mat);
			break;
		default:
			throw new IllegalArgumentException();
		}
		return;
	}
	/**
	 * Make tansform matrix by ICP algorism.
	 */
	public boolean transMat(NyARSquare i_square, NyARRectOffset i_offset,NyARDoubleMatrix44 i_result,NyARTransMatResultParam o_param)
	{
		if(this._icpc.icpGetInitXw2Xc_from_PlanarData(i_square.sqvertex,i_offset.vertex,4,i_result)){
			if(this._icpp.icpPoint(i_square.sqvertex,i_offset.vertex,4,i_result,i_result,o_param)){
				return true;
			}
		}
		return false;
	}
	/**
	 * Make tansform matrix by ICP algorism.
	 * i_prev_result parameter is not effective. should set 0.
	 */
	public boolean transMatContinue(NyARSquare i_square, NyARRectOffset i_offset, NyARDoubleMatrix44 i_prev_result, double i_prev_err,
			NyARDoubleMatrix44 o_result, NyARTransMatResultParam o_param)
	{
		if(this._icpp.icpPoint(i_square.sqvertex,i_offset.vertex,4,o_result,o_result,o_param)){
			return true;
		}
		return false;
	}
}
