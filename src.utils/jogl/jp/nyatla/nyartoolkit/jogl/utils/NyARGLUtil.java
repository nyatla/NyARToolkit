/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.jogl.utils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import jp.nyatla.nyartoolkit.core.*;
/**
 * NyARToolkit用のJOGL支援関数群
 */
public class NyARGLUtil
{
	private NyARParam ar_param;

	private javax.media.opengl.GL gl;

	private javax.media.opengl.glu.GLU glu;

	public NyARGLUtil(javax.media.opengl.GL i_gl, NyARParam i_camera_param)
	{
		this.ar_param = i_camera_param;
		this.gl = i_gl;
		this.glu = new GLU();
	}

	/**
	 * GLNyARRaster_RGBをバックグラウンドに書き出す。
	 * @param image
	 * @param zoom
	 */
	public void drawBackGround(GLNyARRaster_RGB i_raster, double i_zoom)
	{
		IntBuffer texEnvModeSave = IntBuffer.allocate(1);
		boolean lightingSave;
		boolean depthTestSave;
		javax.media.opengl.GL gl_ = this.gl;

		// Prepare an orthographic projection, set camera position for 2D drawing, and save GL state.
		gl_.glGetTexEnviv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, texEnvModeSave); // Save GL texture environment mode.
		if (texEnvModeSave.array()[0] != GL.GL_REPLACE) {
			gl_.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
		}
		lightingSave = gl_.glIsEnabled(GL.GL_LIGHTING); // Save enabled state of lighting.
		if (lightingSave == true) {
			gl_.glDisable(GL.GL_LIGHTING);
		}
		depthTestSave = gl_.glIsEnabled(GL.GL_DEPTH_TEST); // Save enabled state of depth test.
		if (depthTestSave == true) {
			gl_.glDisable(GL.GL_DEPTH_TEST);
		}
		gl_.glMatrixMode(GL.GL_PROJECTION);
		gl_.glPushMatrix();
		gl_.glLoadIdentity();
		glu.gluOrtho2D(0.0, ar_param.getX(), 0.0, ar_param.getY());
		gl_.glMatrixMode(GL.GL_MODELVIEW);
		gl_.glPushMatrix();
		gl_.glLoadIdentity();
		arglDispImageStateful(i_raster, i_zoom);

		// Restore previous projection, camera position, and GL state.
		gl_.glMatrixMode(GL.GL_PROJECTION);
		gl_.glPopMatrix();
		gl_.glMatrixMode(GL.GL_MODELVIEW);
		gl_.glPopMatrix();
		if (depthTestSave) {
			gl_.glEnable(GL.GL_DEPTH_TEST); // Restore enabled state of depth test.
		}
		if (lightingSave) {
			gl_.glEnable(GL.GL_LIGHTING); // Restore enabled state of lighting.
		}
		if (texEnvModeSave.get(0) != GL.GL_REPLACE) {
			gl_.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, texEnvModeSave.get(0)); // Restore GL texture environment mode.
		}
		gl_.glEnd();
	}

	/**
	 * arglDispImageStateful関数モドキ
	 * @param image
	 * @param zoom
	 */
	private void arglDispImageStateful(GLNyARRaster_RGB i_raster, double zoom)
	{
		javax.media.opengl.GL gl_ = this.gl;
		int width = i_raster.getWidth();
		int height = i_raster.getHeight();
		float zoomf;
		IntBuffer params = IntBuffer.allocate(4);
		zoomf = (float) zoom;
		gl_.glDisable(GL.GL_TEXTURE_2D);
		gl_.glGetIntegerv(GL.GL_VIEWPORT, params);
		gl_.glPixelZoom(zoomf * ((float) (params.get(2)) / (float) width), -zoomf * ((float) (params.get(3)) / (float) height));
		gl_.glWindowPos2f(0.0f, (float) height);
		gl_.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		ByteBuffer buf = ByteBuffer.wrap(i_raster.getGLRgbArray());
		gl_.glDrawPixels(width, height, i_raster.getGLPixelFlag(), GL.GL_UNSIGNED_BYTE, buf);
	}
}
