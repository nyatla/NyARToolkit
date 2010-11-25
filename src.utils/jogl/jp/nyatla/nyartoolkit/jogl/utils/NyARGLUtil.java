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

import java.awt.Font;
import java.nio.*;
import javax.media.opengl.GL;

import com.sun.opengl.util.j2d.TextRenderer;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.*;
/**
 * OpenGLの支援機能を提供するクラスです。
 */
public class NyARGLUtil
{
	/**
	 * NyARToolKit 2.53以前のコードと互換性を持たせるためのスケール値。{@link #toCameraFrustumRH}のi_scaleに設定することで、
	 * 以前のバージョンの数値系と互換性を保ちます。
	 */
	final static double SCALE_FACTOR_toCameraFrustumRH_NYAR2=1.0;
	/**
	 * NyARToolKit 2.53以前のコードと互換性を持たせるためのスケール値。{@link #toCameraViewRH}のi_scaleに設定することで、
	 * 以前のバージョンの数値系と互換性を保ちます。
	 */
	final static double SCALE_FACTOR_toCameraViewRH_NYAR2=0.025;
	
	/**
	 * OpenGLにおける背景描画を支援します。i_rasterを現在のビューポートへ描画します。
	 * @param glu
	 * @param i_raster
	 * @param i_zoom
	 * @throws NyARException
	 */
	public static void drawBackGround(javax.media.opengl.GL i_gl,INyARRaster i_raster, double i_zoom) throws NyARException
	{
		IntBuffer texEnvModeSave = IntBuffer.allocate(1);
		boolean lightingSave;
		boolean depthTestSave;
		final NyARIntSize rsize=i_raster.getSize();
		// Prepare an orthographic projection, set camera position for 2D drawing, and save GL state.
		i_gl.glGetTexEnviv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, texEnvModeSave); // Save GL texture environment mode.
		if (texEnvModeSave.array()[0] != GL.GL_REPLACE) {
			i_gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
		}
		lightingSave = i_gl.glIsEnabled(GL.GL_LIGHTING); // Save enabled state of lighting.
		if (lightingSave == true) {
			i_gl.glDisable(GL.GL_LIGHTING);
		}
		depthTestSave = i_gl.glIsEnabled(GL.GL_DEPTH_TEST); // Save enabled state of depth test.
		if (depthTestSave == true) {
			i_gl.glDisable(GL.GL_DEPTH_TEST);
		}
		i_gl.glMatrixMode(GL.GL_PROJECTION);
		i_gl.glPushMatrix();
		i_gl.glLoadIdentity();
		i_gl.glOrtho(0.0,rsize.w, 0.0,rsize.h,0,1);
		i_gl.glMatrixMode(GL.GL_MODELVIEW);
		i_gl.glPushMatrix();
		i_gl.glLoadIdentity();
		arglDispImageStateful(i_gl,rsize,i_raster.getBuffer(),i_raster.getBufferType(),i_zoom);

		// Restore previous projection, camera position, and GL state.
		i_gl.glMatrixMode(GL.GL_PROJECTION);
		i_gl.glPopMatrix();
		i_gl.glMatrixMode(GL.GL_MODELVIEW);
		i_gl.glPopMatrix();
		if (depthTestSave) {
			i_gl.glEnable(GL.GL_DEPTH_TEST); // Restore enabled state of depth test.
		}
		if (lightingSave) {
			i_gl.glEnable(GL.GL_LIGHTING); // Restore enabled state of lighting.
		}
		if (texEnvModeSave.get(0) != GL.GL_REPLACE) {
			i_gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, texEnvModeSave.get(0)); // Restore GL texture environment mode.
		}
		i_gl.glEnd();
	}

	/**
	 * arglDispImageStateful関数モドキ
	 * @param image
	 * @param zoom
	 */
	private static void arglDispImageStateful(GL gl,NyARIntSize i_size,Object i_buffer,int i_buffer_type, double zoom) throws NyARException
	{
		float zoomf;
		IntBuffer params = IntBuffer.allocate(4);
		zoomf = (float) zoom;
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glGetIntegerv(GL.GL_VIEWPORT, params);
		gl.glPixelZoom(zoomf * ((float) (params.get(2)) / (float) i_size.w), -zoomf * ((float) (params.get(3)) / (float) i_size.h));
		gl.glWindowPos2f(0.0f, (float) i_size.h);
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		//BufferTypeの変換
		switch(i_buffer_type)
		{
		case NyARBufferType.BYTE1D_B8G8R8_24:
			gl.glDrawPixels(i_size.w,i_size.h,GL.GL_BGR, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap((byte[])i_buffer));
			break;
		case NyARBufferType.BYTE1D_R8G8B8_24:
			gl.glDrawPixels(i_size.w,i_size.h,GL.GL_RGB, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap((byte[])i_buffer));
			break;
		case NyARBufferType.BYTE1D_B8G8R8X8_32:
			gl.glDrawPixels(i_size.w,i_size.h,GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap((byte[])i_buffer));
			break;
		case NyARBufferType.INT1D_GRAY_8:
			gl.glDrawPixels(i_size.w,i_size.h,GL.GL_LUMINANCE, GL.GL_UNSIGNED_INT, IntBuffer.wrap((int[])i_buffer));
			break;
		default:
			throw new NyARException();
		}
	}
	
	
	/**
	 * ARToolKitスタイルのカメラパラメータから、 CameraFrustamを計算します。
	 * @param i_arparam
	 * @param i_scale
	 * スケール値を指定します。1=1mmです。10ならば1=1cm,1000ならば1=1mです。
	 * 2.53以前のNyARToolkitと互換性を持たせるときは、{@link #SCALE_FACTOR_toCameraFrustumRH_NYAR2}を指定してください。
	 * @param i_near
	 * 視錐体のnearPointを指定します。単位は、i_scaleに設定した値で決まります。
	 * @param i_far
	 * 視錐体のfarPointを指定します。単位は、i_scaleに設定した値で決まります。
	 * @param o_gl_projection
	 */
	public static void toCameraFrustumRH(NyARParam i_arparam,double i_scale,double i_near,double i_far,double[] o_gl_projection)
	{
		toCameraFrustumRH(i_arparam.getPerspectiveProjectionMatrix(),i_arparam.getScreenSize(),i_scale,i_near,i_far,o_gl_projection);
		return;
	}
	/**
	 * ARToolKitスタイルのProjectionMatrixから、 CameraFrustamを計算します。
	 * @param i_promat
	 * @param i_size
	 * スクリーンサイズを指定します。
	 * @param i_scale
	 * {@link #toCameraFrustumRH(NyARParam i_arparam,double i_scale,double i_near,double i_far,double[] o_gl_projection)}を参照。
	 * @param i_near
	 * {@link #toCameraFrustumRH(NyARParam i_arparam,double i_scale,double i_near,double i_far,double[] o_gl_projection)}を参照。
	 * @param i_far
	 * {@link #toCameraFrustumRH(NyARParam i_arparam,double i_scale,double i_near,double i_far,double[] o_gl_projection)}を参照。
	 * @param o_gl_projection
	 * {@link #toCameraFrustumRH(NyARParam i_arparam,double i_scale,double i_near,double i_far,double[] o_gl_projection)}を参照。
	 */
	public static void toCameraFrustumRH(NyARPerspectiveProjectionMatrix i_promat,NyARIntSize i_size,double i_scale,double i_near,double i_far,double[] o_gl_projection)
	{
		NyARDoubleMatrix44 m=new NyARDoubleMatrix44();
		i_promat.makeCameraFrustumRH(i_size.w,i_size.h,i_near*i_scale,i_far*i_scale,m);
		m.getValueT(o_gl_projection);
		return;
	}
	/**
	 * NyARTransMatResultをOpenGLの行列へ変換します。
	 * @param mat
	 * 変換元の行列
	 * @param i_scale
	 * 座標系のスケール値を指定します。1=1mmです。10ならば1=1cm,1000ならば1=1mです。
	 * 2.53以前のNyARToolkitと互換性を持たせるときは、{@link #SCALE_FACTOR_toCameraViewRH_NYAR2}を指定してください。
	 * @param o_gl_result
	 */
	public static void toCameraViewRH(NyARDoubleMatrix44 mat,double i_scale, double[] o_gl_result)
	{
		o_gl_result[0 + 0 * 4] = mat.m00; 
		o_gl_result[1 + 0 * 4] = -mat.m10;
		o_gl_result[2 + 0 * 4] = -mat.m20;
		o_gl_result[3 + 0 * 4] = 0.0;
		o_gl_result[0 + 1 * 4] = mat.m01;
		o_gl_result[1 + 1 * 4] = -mat.m11;
		o_gl_result[2 + 1 * 4] = -mat.m21;
		o_gl_result[3 + 1 * 4] = 0.0;
		o_gl_result[0 + 2 * 4] = mat.m02;
		o_gl_result[1 + 2 * 4] = -mat.m12;
		o_gl_result[2 + 2 * 4] = -mat.m22;
		o_gl_result[3 + 2 * 4] = 0.0;
		
		double scale=1/i_scale;
		o_gl_result[0 + 3 * 4] = mat.m03*scale;
		o_gl_result[1 + 3 * 4] = -mat.m13*scale;
		o_gl_result[2 + 3 * 4] = -mat.m23*scale;
		o_gl_result[3 + 3 * 4] = 1.0;
		return;
	}

	
}
