package jp.nyatla.nyartoolkit.jogl.utils;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.nftsystem.INyARNftSystemConfig;
import jp.nyatla.nyartoolkit.nftsystem.NyARNftSystem;

public class NyARGlNftSystem  extends NyARNftSystem{

	protected NyARGlNftSystem(INyARNftSystemConfig i_ref_cparam) {
		super(i_ref_cparam);
	}
	/**
	 * この関数は、o_bufに指定idのOpenGL形式の姿勢変換行列を設定して返します。
	 * @param i_id
	 * @param o_buf
	 * @return
	 * @throws NyARRuntimeException 
	 */
	public double[] getGlTransformMatrix(int i_id,double[] o_buf)
	{
		NyARGLUtil.toCameraViewRH(this.getTransformMatrix(i_id),1,o_buf);
		return o_buf;
	}
	/**
	 * この関数はOpenGL形式の姿勢変換行列を新規に割り当てて返します。
	 * @param i_buf
	 * @return
	 * @throws NyARRuntimeException 
	 */
	public double[] getGlTransformMatrix(int i_id)
	{
		return this.getGlTransformMatrix(i_id,new double[16]);
	}	
	
}
