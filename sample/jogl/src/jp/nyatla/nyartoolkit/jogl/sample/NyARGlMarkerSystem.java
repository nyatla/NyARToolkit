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
	protected void initInstance(NyARParam i_ref_param) throws NyARException
	{
		super.initInstance(i_ref_param);
		this._projection_mat=new double[16];
	}

	private double[] _projection_mat;

	/**
	 * OpenGLスタイルのProjectionMatrixを返します。
	 * @param i_gl
	 * @return
	 * [readonly]
	 */
	public double[] getGlProjectionMatrix()
	{
		return this._projection_mat;
	}
	public void setProjectionMatrixClipping(double i_near,double i_far)
	{
		super.setProjectionMatrixClipping(i_near,i_far);
		NyARGLUtil.toCameraFrustumRH(this._ref_param,1,i_near,i_far,this._projection_mat);
	}
	private double[] _work=new double[16];
	/**
	 * 
	 * この関数はOpenGL形式の姿勢変換行列を返します。
	 * 返却値の有効期間は、次回の{@link #getGlMarkerTransMat()}をコールするまでです。
	 * 値を保持する場合は、{@link #getGlMarkerMatrix(double[])}を使用します。
	 * @param i_buf
	 * @return
	 * [readonly]
	 */
	public double[] getGlMarkerMatrix(int i_id)
	{
		return this.getGlMarkerMatrix(i_id,this._work);
	}
	/**
	 * この関数は、i_bufに指定idのOpenGL形式の姿勢変換行列を設定して返します。
	 * @param i_id
	 * @param i_buf
	 * @return
	 */
	public double[] getGlMarkerMatrix(int i_id,double[] i_buf)
	{
		NyARGLUtil.toCameraViewRH(this.getMarkerMatrix(i_id),1,i_buf);
		return i_buf;
	}
	
}
