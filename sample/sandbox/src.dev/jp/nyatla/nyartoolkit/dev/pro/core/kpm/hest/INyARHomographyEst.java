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
package jp.nyatla.nyartoolkit.dev.pro.core.kpm.hest;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.pro.core.kpm.ann.NyARSurfAnnMatch;

public interface INyARHomographyEst
{
	/**
	 * o_point_pairの数にあわせて、最大o_point_pairの数と同じ数のサンプルセ�?トを返します�??
	 * @param preRANSAC
	 * @param o_point_pair
	 * @throws NyARRuntimeException
	 */
	public void ransacEstimation(NyARSurfAnnMatch.ResultPtr preRANSAC, NyARSurfAnnMatch.ResultPtr o_point_pair) throws NyARRuntimeException;

}
