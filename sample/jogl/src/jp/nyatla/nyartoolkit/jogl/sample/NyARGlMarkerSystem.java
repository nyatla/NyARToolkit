package jp.nyatla.nyartoolkit.jogl.sample;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.jogl.utils.NyARGLUtil;
import jp.nyatla.nyartoolkit.markerar.NyARMarkerSystem;

public class NyARGlMarkerSystem extends NyARMarkerSystem
{
	
	public NyARGlMarkerSystem(NyARParam iRefParam) throws NyARException
	{
		super(iRefParam);
	}

	private double[] _projection_mat=new double[16];

	/**
	 * [readonly]
	 * OpenGLスタイルのProjectionMatrixを返します。
	 * @param i_gl
	 * @return
	 */
	public double[] getGlProjectionMatrix()
	{
		NyARGLUtil.toCameraFrustumRH(this._ref_param,1,10,10000,this._projection_mat);
		return this._projection_mat;
	}
	
	private double[] _work=new double[16];
	/**
	 * [readonly]
	 * この関数はOpenGL形式の姿勢変換行列を返します。
	 * 返却値の有効期間は、次回の{@link #getGlMarkerTransMat()}をコールするまでです。
	 * 値を保持する場合は、{@link #getGlMarkerTransMat(double[])}を使用します。
	 * @param i_buf
	 * @return
	 */
	public double[] getGlMarkerTransMat(int i_id)
	{
		return this.getGlMarkerTransMat(i_id,this._work);
	}
	/**
	 * この関数は、i_bufに指定idのOpenGL形式の姿勢変換行列を設定して返します。
	 * @param i_id
	 * @param i_buf
	 * @return
	 */
	public double[] getGlMarkerTransMat(int i_id,double[] i_buf)
	{
		NyARGLUtil.toCameraViewRH(this.getMarkerTransMat(i_id),1,i_buf);
		return i_buf;
	}
	
}
