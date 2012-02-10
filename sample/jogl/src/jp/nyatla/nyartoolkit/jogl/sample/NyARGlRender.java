package jp.nyatla.nyartoolkit.jogl.sample;

import javax.media.opengl.GL;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.jogl.utils.NyARGLDrawUtil;
import jp.nyatla.nyartoolkit.jogl.utils.NyARGLUtil;

public class NyARGlRender
{
	private int[] __wk=new int[1];
	private double[] _projection_mat=new double[16];
	private final int getGlMatrixMode(GL i_gl)
	{
		i_gl.glGetIntegerv(GL.GL_MATRIX_MODE,this.__wk,0);
		return this.__wk[0];
	}
	public NyARGlRender(NyARParam i_param)
	{
		NyARGLUtil.toCameraFrustumRH(i_param,1,10,10000,this._projection_mat);		
	}
	/**
	 * i_glにAR向けのprojectionMatrixを、PROJECTIONスタックへロードします。
	 * @param i_gl
	 */
	public void glLoadProjectionMatrix(GL i_gl)
	{
		int old_mode=this.getGlMatrixMode(i_gl);
		if(old_mode!=GL.GL_PROJECTION){
			i_gl.glMatrixMode(GL.GL_PROJECTION);
			i_gl.glLoadMatrixd(this._projection_mat, 0);
			i_gl.glMatrixMode(old_mode);
		}else{
			i_gl.glLoadMatrixd(this._projection_mat, 0);
		}
	}
	public void drawBackground(GL i_gl,INyARRgbRaster i_bg_image) throws NyARException
	{
		i_gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear the buffers for new frame.
		NyARGLDrawUtil.drawBackGround(i_gl,i_bg_image, 1.0);
	}

	private double[] _mv_mat=new double[16];
	/**
	 * i_glに、i_matをロードします。
	 * @param i_gl
	 * @param i_mat
	 */
	public void glLoadMatrix(GL i_gl,NyARDoubleMatrix44 i_mat)
	{
		NyARGLUtil.toCameraViewRH(i_mat,1,this._mv_mat);
	}
}