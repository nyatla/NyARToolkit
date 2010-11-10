/* 
 * PROJECT: NyARToolkit JOGL utilities.
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.jogl.utils;

import java.nio.*;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
/**
 * OpenGLの支援機能を提供するクラスです。
 */
public class NyARGLUtil
{
	/**
	 * BufferType値から、OpenGLのピクセルタイプ値を計算します。
	 * @param i_buffer_type
	 * BufferType値を指定します。
	 * @return
	 * OpenGLのピクセルタイプ値
	 * @throws NyARException
	 */
	final static private int getGlPixelFormat(int i_buffer_type) throws NyARException
	{
		switch(i_buffer_type){
		case NyARBufferType.BYTE1D_B8G8R8_24:
			return GL.GL_BGR;
		case NyARBufferType.BYTE1D_R8G8B8_24:
			return GL.GL_RGB;
		default:
			throw new NyARException();
		}
	}
	
	/**
	 * 
	 * @param glu
	 * @param i_raster
	 * @param i_zoom
	 * @throws NyARException
	 */
	public static void drawBackGround(GLU glu,INyARRgbRaster i_raster, double i_zoom) throws NyARException
	{
		IntBuffer texEnvModeSave = IntBuffer.allocate(1);
		boolean lightingSave;
		boolean depthTestSave;
		javax.media.opengl.GL gl = GLU.getCurrentGL();
		final NyARIntSize rsize=i_raster.getSize();

		// Prepare an orthographic projection, set camera position for 2D drawing, and save GL state.
		gl.glGetTexEnviv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, texEnvModeSave); // Save GL texture environment mode.
		if (texEnvModeSave.array()[0] != GL.GL_REPLACE) {
			gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
		}
		lightingSave = gl.glIsEnabled(GL.GL_LIGHTING); // Save enabled state of lighting.
		if (lightingSave == true) {
			gl.glDisable(GL.GL_LIGHTING);
		}
		depthTestSave = gl.glIsEnabled(GL.GL_DEPTH_TEST); // Save enabled state of depth test.
		if (depthTestSave == true) {
			gl.glDisable(GL.GL_DEPTH_TEST);
		}
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		glu.gluOrtho2D(0.0,rsize.w, 0.0,rsize.h);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		arglDispImageStateful(gl,i_raster, i_zoom);

		// Restore previous projection, camera position, and GL state.
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix();
		if (depthTestSave) {
			gl.glEnable(GL.GL_DEPTH_TEST); // Restore enabled state of depth test.
		}
		if (lightingSave) {
			gl.glEnable(GL.GL_LIGHTING); // Restore enabled state of lighting.
		}
		if (texEnvModeSave.get(0) != GL.GL_REPLACE) {
			gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, texEnvModeSave.get(0)); // Restore GL texture environment mode.
		}
		gl.glEnd();
	}

	/**
	 * arglDispImageStateful関数モドキ
	 * @param image
	 * @param zoom
	 */
	private static void arglDispImageStateful(GL gl_,INyARRgbRaster i_raster, double zoom) throws NyARException
	{
		final NyARIntSize rsize = i_raster.getSize();
		float zoomf;
		IntBuffer params = IntBuffer.allocate(4);
		zoomf = (float) zoom;
		gl_.glDisable(GL.GL_TEXTURE_2D);
		gl_.glGetIntegerv(GL.GL_VIEWPORT, params);
		gl_.glPixelZoom(zoomf * ((float) (params.get(2)) / (float) rsize.w), -zoomf * ((float) (params.get(3)) / (float) rsize.h));
		gl_.glWindowPos2f(0.0f, (float) rsize.h);
		gl_.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		ByteBuffer buf = ByteBuffer.wrap((byte[])i_raster.getBuffer());
		gl_.glDrawPixels(rsize.w,rsize.h,getGlPixelFormat(i_raster.getBufferType()), GL.GL_UNSIGNED_BYTE, buf);
	}
	
	private static double view_scale_factor = 0.025;
	private static double view_distance_min = 0.1;//#define VIEW_DISTANCE_MIN		0.1			// Objects closer to the camera than this will not be displayed.
	private static double view_distance_max = 100.0;//#define VIEW_DISTANCE_MAX		100.0		// Objects further away from the camera than this will not be displayed.

	public static void setScaleFactor(double i_new_value)
	{
		NyARGLUtil.view_scale_factor = i_new_value;
	}

	public static void setViewDistanceMin(double i_new_value)
	{
		NyARGLUtil.view_distance_min = i_new_value;
	}

	public static void setViewDistanceMax(double i_new_value)
	{
		NyARGLUtil.view_distance_max = i_new_value;
	}


	
	
	/**
	 * void arglCameraFrustumRH(const ARParam *cparam, const double focalmin, const double focalmax, GLdouble m_projection[16])
	 * 関数の置き換え
	 * NyARParamからOpenGLのProjectionを作成します。
	 * @param i_arparam
	 * @param o_gl_projection
	 * double[16]を指定して下さい。
	 */
	public static void toCameraFrustumRH(NyARParam i_arparam,double[] o_gl_projection)
	{
		NyARDoubleMatrix44 m=new NyARDoubleMatrix44();
		i_arparam.makeCameraFrustumRH(view_distance_min,view_distance_max,m);
		//OpenGLの並びに直してセット。
		m.getValueT(o_gl_projection);
		return;
	}
	
	
	
	/**
	 * NyARTransMatResultをOpenGLの行列へ変換します。
	 * @param i_ny_result
	 * @param o_gl_result
	 * @throws NyARException
	 */
	public static void toCameraViewRH(NyARTransMatResult i_ny_result, double[] o_gl_result) throws NyARException
	{
		o_gl_result[0 + 0 * 4] = i_ny_result.m00; 
		o_gl_result[0 + 1 * 4] = i_ny_result.m01;
		o_gl_result[0 + 2 * 4] = i_ny_result.m02;
		o_gl_result[0 + 3 * 4] = i_ny_result.m03;
		o_gl_result[1 + 0 * 4] = -i_ny_result.m10;
		o_gl_result[1 + 1 * 4] = -i_ny_result.m11;
		o_gl_result[1 + 2 * 4] = -i_ny_result.m12;
		o_gl_result[1 + 3 * 4] = -i_ny_result.m13;
		o_gl_result[2 + 0 * 4] = -i_ny_result.m20;
		o_gl_result[2 + 1 * 4] = -i_ny_result.m21;
		o_gl_result[2 + 2 * 4] = -i_ny_result.m22;
		o_gl_result[2 + 3 * 4] = -i_ny_result.m23;
		o_gl_result[3 + 0 * 4] = 0.0;
		o_gl_result[3 + 1 * 4] = 0.0;
		o_gl_result[3 + 2 * 4] = 0.0;
		o_gl_result[3 + 3 * 4] = 1.0;
		if (view_scale_factor != 0.0) {
			o_gl_result[12] *= view_scale_factor;
			o_gl_result[13] *= view_scale_factor;
			o_gl_result[14] *= view_scale_factor;
		}
		return;
	}	
	
}
