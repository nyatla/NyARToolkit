package jp.nyatla.nyartoolkit.jogl.utils;

import java.awt.Color;
import java.awt.Font;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

import com.sun.opengl.util.j2d.TextRenderer;

/**
 * OpenGL向けの描画関数を提供します。
 */
public class NyARGLDrawUtil
{
	private static TextRenderer _tr=new TextRenderer(new Font("SansSerif", Font.PLAIN, 10));
	/**
	 * 立方体を描画します。
	 * @param i_gl
	 * OpenGLインスタンス
	 * @param i_size_per_mm
	 * 立方体の辺の長さを[mm単位]
	 */
	public static void drawColorCube(GL i_gl,float i_size_per_mm)
	{
		// Colour cube data.
		int polyList = 0;
		float fSize =i_size_per_mm/2f;
		int f, i;
		float[][] cube_vertices = new float[][] { { 1.0f, 1.0f, 1.0f }, { 1.0f, -1.0f, 1.0f }, { -1.0f, -1.0f, 1.0f }, { -1.0f, 1.0f, 1.0f }, { 1.0f, 1.0f, -1.0f }, { 1.0f, -1.0f, -1.0f }, { -1.0f, -1.0f, -1.0f }, { -1.0f, 1.0f, -1.0f } };
		float[][] cube_vertex_colors = new float[][] { { 1.0f, 1.0f, 1.0f }, { 1.0f, 1.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { 0.0f, 1.0f, 1.0f }, { 1.0f, 0.0f, 1.0f }, { 1.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 1.0f } };
		int cube_num_faces = 6;
		short[][] cube_faces = new short[][] { { 3, 2, 1, 0 }, { 2, 3, 7, 6 }, { 0, 1, 5, 4 }, { 3, 0, 4, 7 }, { 1, 2, 6, 5 }, { 4, 5, 6, 7 } };

		if (polyList == 0) {
			polyList = i_gl.glGenLists(1);
			i_gl.glNewList(polyList, GL.GL_COMPILE);
			i_gl.glBegin(GL.GL_QUADS);
			for (f = 0; f < cube_num_faces; f++)
				for (i = 0; i < 4; i++) {
					i_gl.glColor3f(cube_vertex_colors[cube_faces[f][i]][0], cube_vertex_colors[cube_faces[f][i]][1], cube_vertex_colors[cube_faces[f][i]][2]);
					i_gl.glVertex3f(cube_vertices[cube_faces[f][i]][0] * fSize, cube_vertices[cube_faces[f][i]][1] * fSize, cube_vertices[cube_faces[f][i]][2] * fSize);
				}
			i_gl.glEnd();
			i_gl.glColor3f(0.0f, 0.0f, 0.0f);
			for (f = 0; f < cube_num_faces; f++) {
				i_gl.glBegin(GL.GL_LINE_LOOP);
				for (i = 0; i < 4; i++)
					i_gl.glVertex3f(cube_vertices[cube_faces[f][i]][0] * fSize, cube_vertices[cube_faces[f][i]][1] * fSize, cube_vertices[cube_faces[f][i]][2] * fSize);
				i_gl.glEnd();
			}
			i_gl.glEndList();
		}
		i_gl.glCallList(polyList); // Draw the cube.
	}
	/**
	 * フォントカラーをセットします。
	 * @param i_c
	 */
	public static void setFontColor(Color i_c)
	{
		NyARGLDrawUtil._tr.setColor(i_c);
	}
	/**
	 * フォントスタイルをセットします。
	 * @param i_font_name
	 * @param i_font_style
	 * @param i_size
	 */
	public static void setFontStyle(String i_font_name,int i_font_style,int i_size)
	{
		NyARGLDrawUtil._tr=new TextRenderer(new Font(i_font_name,i_font_style, i_size));
	}
	/**
	 * 現在のフォントで、文字列を描画します。
	 * @param i_str
	 * @param i_scale
	 */
	public static void drawText(String i_str,float i_scale)
	{
		NyARGLDrawUtil._tr.begin3DRendering();
		NyARGLDrawUtil._tr.draw3D(i_str, 0f,0f,0f,i_scale);
		NyARGLDrawUtil._tr.end3DRendering();
		return;
	}
	/**
	 * INyARRasterの内容を現在のビューポートへ描画します。
	 * @param i_gl
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
		//ProjectionMatrixとModelViewMatrixを初期化
		i_gl.glMatrixMode(GL.GL_PROJECTION);
		i_gl.glPushMatrix();
		i_gl.glLoadIdentity();
		i_gl.glOrtho(0.0,rsize.w, 0.0,rsize.h,0,1);
		i_gl.glMatrixMode(GL.GL_MODELVIEW);
		i_gl.glPushMatrix();
		i_gl.glLoadIdentity();
		arglDispImageStateful(i_gl,rsize,i_raster.getBuffer(),i_raster.getBufferType(),i_zoom);
		//ProjectionMatrixとModelViewMatrixを回復
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
			/** @bug don't work*/
			gl.glDrawPixels(i_size.w,i_size.h,GL.GL_LUMINANCE, GL.GL_UNSIGNED_INT, IntBuffer.wrap((int[])i_buffer));
			break;
		default:
			throw new NyARException();
		}
	}
	/**
	 * スクリーン座標系をOpenGLにロードします。この関数は、PROJECTIONとMODELVIEWスタックをそれぞれ1づつpushします。
	 * スクリーン座標系を使用し終わったら、endScreenCoordinateSystemを呼び出してください。
	 * @param i_gl
	 * @param i_width
	 * @param i_height
	 * @param i_revers_y_direction
	 * Y軸の反転フラグです。trueならばtop->bottom、falseならばbottom->top方向になります。
	 */
	public static void beginScreenCoordinateSystem(GL i_gl,int i_width,int i_height,boolean i_revers_y_direction)
	{
		i_gl.glMatrixMode(GL.GL_PROJECTION);
		i_gl.glPushMatrix(); // Save world coordinate system.
		i_gl.glLoadIdentity();
		if(i_revers_y_direction){
			i_gl.glOrtho(0.0,i_width,i_height,0,-1,1);
		}else{
			i_gl.glOrtho(0.0,i_width,0,i_height,-1,1);
		}
		i_gl.glMatrixMode(GL.GL_MODELVIEW);
		i_gl.glPushMatrix(); // Save world coordinate system.
		i_gl.glLoadIdentity();
		return;
	}
	/**
	 * ロードしたスクリーン座標系を元に戻します。{@link #beginScreenCoordinateSystem}の後に呼び出してください。
	 * @param i_gl
	 */
	public static void endScreenCoordinateSystem(GL i_gl)
	{
		i_gl.glMatrixMode(GL.GL_PROJECTION);
		i_gl.glPopMatrix();
		i_gl.glMatrixMode(GL.GL_MODELVIEW);		
		i_gl.glPopMatrix();
		return;
	}	
}
