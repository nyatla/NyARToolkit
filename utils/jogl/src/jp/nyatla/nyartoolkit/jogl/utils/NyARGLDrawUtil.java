package jp.nyatla.nyartoolkit.jogl.utils;

import java.awt.Color;
import java.awt.Font;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

import com.sun.opengl.util.j2d.TextRenderer;

/**
 * このクラスには、アプリケーションの為のOpenGL用のヘルパー関数を定義します。
 * NyARToolKitを使ったアプリケーションを実装するのに役立ちます。
 * ほとんどの関数はstatic宣言です。このクラスのインスタンスを作る必要はありません。
 * 
 *
 */
public class NyARGLDrawUtil
{
	private static TextRenderer _tr=new TextRenderer(new Font("SansSerif", Font.PLAIN, 10));
	private static float[][] COLORCUBE_COLOR= new float[][] { { 1.0f, 1.0f, 1.0f }, { 1.0f, 1.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { 0.0f, 1.0f, 1.0f }, { 1.0f, 0.0f, 1.0f }, { 1.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 1.0f } };
	private static float[][] CUBE_VERTICES = new float[][] { { 1.0f, 1.0f, 1.0f }, { 1.0f, -1.0f, 1.0f }, { -1.0f, -1.0f, 1.0f }, { -1.0f, 1.0f, 1.0f }, { 1.0f, 1.0f, -1.0f }, { 1.0f, -1.0f, -1.0f }, { -1.0f, -1.0f, -1.0f }, { -1.0f, 1.0f, -1.0f } };
	private static short[][] CUBE_FACE = new short[][] { { 3, 2, 1, 0 }, { 2, 3, 7, 6 }, { 0, 1, 5, 4 }, { 3, 0, 4, 7 }, { 1, 2, 6, 5 }, { 4, 5, 6, 7 } };
	public static void drawCube(GL i_gl,float i_size_per_mm,float i_r,float i_g,float i_b)
	{
		float v[]={i_r,i_g,i_b};
		float vs[][]={v,v,v,v,v,v,v,v};
		drawColorCube(i_gl,i_size_per_mm,vs);
	}
	
	/**
	 * この関数は、指定サイズの立方体を現在のビューポートへ描画します。
	 * ARToolKitのサンプルで使われているカラーキューブを描画します。
	 * @param i_gl
	 * OpenGLのインスタンス
	 * @param i_size_per_mm
	 * 立方体の辺の長さ。[mm単位]
	 * ARシステムをmmオーダーで構築していない場合は、別単位になります。
	 */
	public static void drawColorCube(GL i_gl,float i_size_per_mm)
	{
		drawColorCube(i_gl,i_size_per_mm,COLORCUBE_COLOR);
	}
	private static void drawColorCube(GL i_gl,float i_size_per_mm,float[][] i_color)
	{
		// Colour cube data.
		int polyList = 0;
		float fSize =i_size_per_mm/2f;
		int f, i;
		int cube_num_faces = 6;

		if (polyList == 0) {
			polyList = i_gl.glGenLists(1);
			i_gl.glNewList(polyList, GL.GL_COMPILE);
			i_gl.glBegin(GL.GL_QUADS);
			for (f = 0; f < cube_num_faces; f++)
				for (i = 0; i < 4; i++) {
					i_gl.glColor3f(i_color[CUBE_FACE[f][i]][0], i_color[CUBE_FACE[f][i]][1], i_color[CUBE_FACE[f][i]][2]);
					i_gl.glVertex3f(CUBE_VERTICES[CUBE_FACE[f][i]][0] * fSize, CUBE_VERTICES[CUBE_FACE[f][i]][1] * fSize, CUBE_VERTICES[CUBE_FACE[f][i]][2] * fSize);
				}
			i_gl.glEnd();
			i_gl.glColor3f(0.0f, 0.0f, 0.0f);
			for (f = 0; f < cube_num_faces; f++) {
				i_gl.glBegin(GL.GL_LINE_LOOP);
				for (i = 0; i < 4; i++)
					i_gl.glVertex3f(CUBE_VERTICES[CUBE_FACE[f][i]][0] * fSize, CUBE_VERTICES[CUBE_FACE[f][i]][1] * fSize, CUBE_VERTICES[CUBE_FACE[f][i]][2] * fSize);
				i_gl.glEnd();
			}
			i_gl.glEndList();
		}
		i_gl.glCallList(polyList); // Draw the cube.		
	}
	
	/**
	 * この関数は、{@link NyARGLDrawUtil}の描画する文字列の、フォントカラーを設定します。
	 * フォントカラーはOpenGL固有のものではなく、{@link NyARGLDrawUtil}固有のものです。
	 * @param i_c
	 * 設定する色。
	 */
	public static void setFontColor(Color i_c)
	{
		NyARGLDrawUtil._tr.setColor(i_c);
	}
	/**
	 * この関数は、{@link NyARGLDrawUtil}の描画する文字列の、フォントスタイルを設定します。
	 * フォントスタイルはOpenGL固有のものではなく、{@link NyARGLDrawUtil}固有のものです。
	 * @param i_font_name
	 * フォントの名前を指定します。デフォルト値は、"SansSerif"です。
	 * @param i_font_style
	 * フォントスタイルを指定します。デフォルト値は、{@link Font#PLAIN}です。
	 * @param i_size
	 * フォントサイズを指定します。デフォルト値は、10です。
	 */
	public static void setFontStyle(String i_font_name,int i_font_style,int i_size)
	{
		NyARGLDrawUtil._tr=new TextRenderer(new Font(i_font_name,i_font_style, i_size));
	}
	/**
	 * この関数は、文字列を描画します。
	 * この関数は、ちらつきが発生したり、あまり品質が良くありません。品質を求められる環境では、別途実装をして下さい。
	 * @param i_str
	 * 描画する文字列。
	 * @param i_scale
	 * 文字列のスケール値。
	 */
	public static void drawText(String i_str,float i_scale)
	{
		NyARGLDrawUtil._tr.begin3DRendering();
		NyARGLDrawUtil._tr.draw3D(i_str, 0f,0f,0f,i_scale);
		NyARGLDrawUtil._tr.end3DRendering();
		return;
	}
	/**
	 * この関数は、{@link INyARRaster}の内容を、現在のビューポートへ描画します。
	 * カメラ画像の背景を描画するのに使用できます。
	 * @param i_gl
	 * OpenGLのインスタンス
	 * @param i_raster
	 * 描画するラスタオブジェクト。何れかのバッファ形式である必要があります。
	 * <ol>
	 * <li>{@link NyARBufferType#BYTE1D_B8G8R8_24}
	 * <li>{@link NyARBufferType#BYTE1D_R8G8B8_24}
	 * <li>{@link NyARBufferType#BYTE1D_B8G8R8X8_32}
	 * </ol>
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
	 * この関数は、指定したラスタを現在の座標系の平面に描画します。
	 * @param i_gl
	 * @param i_x
	 * @param i_y
	 * @param i_raster
	 * @throws NyARException
	 */
	public static void drawRaster(GL i_gl,INyARRgbRaster i_raster) throws NyARException
	{
		NyARIntSize s=i_raster.getSize();
		int[] n=new int[1];
		float[] color=new float[3];
		boolean old_is_texture_2d=i_gl.glIsEnabled(GL.GL_TEXTURE_2D);
		i_gl.glGetFloatv(GL.GL_CURRENT_COLOR,color,0);//カラーの退避
		i_gl.glColor3f(1,1,1);
		i_gl.glEnable(GL.GL_TEXTURE_2D);
		i_gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		i_gl.glGenTextures(1,n,0);
		i_gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR);
		i_gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MIN_FILTER,GL.GL_LINEAR);
		switch(i_raster.getBufferType())
		{
		case NyARBufferType.BYTE1D_R8G8B8_24:
			i_gl.glTexImage2D(GL.GL_TEXTURE_2D,0, GL.GL_RGB,s.w,s.h,0,GL.GL_RGB,GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap((byte[])i_raster.getBuffer()));
			break;
		case NyARBufferType.BYTE1D_B8G8R8_24:
			i_gl.glTexImage2D(GL.GL_TEXTURE_2D,0, GL.GL_RGB,s.w,s.h,0,GL.GL_BGR,GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap((byte[])i_raster.getBuffer()));
			break;
		case NyARBufferType.BYTE1D_B8G8R8X8_32:
			i_gl.glTexImage2D(GL.GL_TEXTURE_2D,0, GL.GL_RGB,s.w,s.h,0,GL.GL_BGRA,GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap((byte[])i_raster.getBuffer()));
			break;
		case NyARBufferType.INT1D_X8R8G8B8_32:
			i_gl.glTexImage2D(GL.GL_TEXTURE_2D,0, GL.GL_RGB,s.w,s.h,0,GL.GL_BGRA,GL.GL_UNSIGNED_BYTE, IntBuffer.wrap((int[])i_raster.getBuffer()));
			break;
		default:
			throw new NyARException();
		}
		i_gl.glBegin(GL.GL_QUADS);
		i_gl.glBindTexture(GL.GL_TEXTURE_2D,n[0]);
		i_gl.glTexCoord2f(0,0);i_gl.glVertex3f(0,0,0);
		i_gl.glTexCoord2f(1,0);i_gl.glVertex3f(s.w,0,0);
		i_gl.glTexCoord2f(1,1);i_gl.glVertex3f(s.w,s.h,0);
		i_gl.glTexCoord2f(0,1);i_gl.glVertex3f(0,s.h,0);
		i_gl.glEnd();
		i_gl.glDeleteTextures(1,n,0);
		if(!old_is_texture_2d){
			i_gl.glDisable(GL.GL_TEXTURE_2D);
		}
		i_gl.glColor3fv(color,0);//カラーの復帰
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
		case NyARBufferType.INT1D_X8R8G8B8_32:
			gl.glDrawPixels(i_size.w,i_size.h,GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, IntBuffer.wrap((int[])i_buffer));
			break;
		case NyARBufferType.INT1D_GRAY_8:
			/** @bug RED screen*/
			gl.glDrawPixels(i_size.w,i_size.h,GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, IntBuffer.wrap((int[])i_buffer));
			break;
		default:
			throw new NyARException();
		}
	}
	/**
	 * この関数は、スクリーン座標系をOpenGLにロードします。
	 * スクリーンに二次元系の情報をそのまま書きこむときに使います。
	 * この関数は、PROJECTIONとMODELVIEWスタックをそれぞれ1づつpushします。
	 * スクリーン座標系を使用し終わったら、{@link #endScreenCoordinateSystem}を必ず呼び出してください。
	 * @param i_gl
	 * OpenGLのインスタンス
	 * @param i_width
	 * スクリーンの幅
	 * @param i_height
	 * スクリーンの高さ
	 * @param i_revers_y_direction
	 * Y軸の反転フラグ。trueならばtop->bottom、falseならばbottom->top方向になります。
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
	 * この関数は、ロードしたスクリーン座標系を元に戻します。
	 * {@link #beginScreenCoordinateSystem}の後に呼び出してください。
	 * @param i_gl
	 * OpenGLのインスタンス
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
