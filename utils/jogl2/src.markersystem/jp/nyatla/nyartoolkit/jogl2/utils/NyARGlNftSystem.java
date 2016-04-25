package jp.nyatla.nyartoolkit.jogl2.utils;



import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.jogl2.utils.NyARGLUtil;
import jp.nyatla.nyartoolkit.nftsystem.INyARNftSystemConfig;
import jp.nyatla.nyartoolkit.nftsystem.NyARNftSystem;

public class NyARGlNftSystem  extends NyARNftSystem{

	public NyARGlNftSystem(INyARNftSystemConfig i_ref_cparam) {
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
	/**
	 * i_glに、i_idで示されるマーカ平面の姿勢行列をセットします。
	 * @param i_gl
	 * @param i_id
	 * @throws NyARRuntimeException 
	 */
	public void glLoadTransformMatrix(GL i_gl,int i_id)
	{
		GL2 gl=i_gl.getGL2();
		int old_mode=NyARGLUtil.getGlMatrixMode(i_gl);
		if(old_mode!=GL2.GL_MODELVIEW){
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			NyARGLUtil.toCameraViewRH(this.getTransformMatrix(i_id),1,this._mv_mat);			
			gl.glLoadMatrixd(this._mv_mat, 0);
			gl.glMatrixMode(old_mode);
		}else{
			NyARGLUtil.toCameraViewRH(this.getTransformMatrix(i_id),1,this._mv_mat);
			gl.glLoadMatrixd(this._mv_mat, 0);
		}
	}	
	/**
	 * OpenGLスタイルカメラパラメータのワーク変数
	 */
	final private double[] _mv_mat=new double[16];
	
	
	/**
	 * i_glに、i_idで示されるマーカ平面の姿勢行列をセットします。
	 * @param i_gl
	 * @param i_id
	 * @throws NyARRuntimeException 
	 */
	public void loadTransformMatrix(GL i_gl,int i_id)
	{
		GL2 gl=i_gl.getGL2();		
		int old_mode=NyARGLUtil.getGlMatrixMode(i_gl);
		if(old_mode!=GL2.GL_MODELVIEW){
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			NyARGLUtil.toCameraViewRH(this.getTransformMatrix(i_id),1,this._mv_mat);			
			gl.glLoadMatrixd(this._mv_mat, 0);
			gl.glMatrixMode(old_mode);
		}else{
			NyARGLUtil.toCameraViewRH(this.getTransformMatrix(i_id),1,this._mv_mat);
			gl.glLoadMatrixd(this._mv_mat, 0);
		}
	}	
}
