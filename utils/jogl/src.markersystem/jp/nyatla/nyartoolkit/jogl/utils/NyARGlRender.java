package jp.nyatla.nyartoolkit.jogl.utils;

import java.awt.image.BufferedImage;

import javax.media.opengl.GL;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.jogl.utils.NyARGLDrawUtil;
import jp.nyatla.nyartoolkit.utils.j2se.NyARBufferedImageRaster;

/**
 * この関数は、{@link NyARGlmarkerSystem}クラスの出力する値を、OpenGL関数へショートカットする関数を定義します。
 */
public class NyARGlRender
{
	private NyARGlMarkerSystem _ms;
	private int[] __wk=new int[1];
	private final int getGlMatrixMode(GL i_gl)
	{
		i_gl.glGetIntegerv(GL.GL_MATRIX_MODE,this.__wk,0);
		return this.__wk[0];
	}
	/**
	 * コンストラクタです。マーカシステムに対応したレンダラを構築します。
	 * @param i_ms
	 */
	public NyARGlRender(NyARGlMarkerSystem i_ms)
	{
		this._ms=i_ms;		
	}
	/**
	 * i_glにAR向けのprojectionMatrixを、PROJECTIONスタックへロードします。
	 * @param i_gl
	 */
	public void loadARProjectionMatrix(GL i_gl)
	{
		int old_mode=this.getGlMatrixMode(i_gl);
		i_gl.glMatrixMode(GL.GL_PROJECTION);
		i_gl.glLoadMatrixd(this._ms.getGlProjectionMatrix(), 0);
		i_gl.glMatrixMode(GL.GL_MODELVIEW);
		i_gl.glLoadIdentity();
		i_gl.glMatrixMode(old_mode);
	}
	public void loadScreenProjectionMatrix(GL i_gl,int i_width,int i_height)
	{
		int old_mode=this.getGlMatrixMode(i_gl);
		i_gl.glMatrixMode(GL.GL_PROJECTION);
		i_gl.glLoadIdentity();
		i_gl.glOrtho(0.0,i_width,i_height,0,-1,1);
		i_gl.glMatrixMode(GL.GL_MODELVIEW);
		i_gl.glLoadIdentity();
		i_gl.glMatrixMode(old_mode);
		return;		
	}

	/**
	 * i_glに、i_matをロードします。
	 * @param i_gl
	 * @param i_id
	 */
	public void loadMarkerMatrix(GL i_gl,int i_id)
	{
		int old_mode=this.getGlMatrixMode(i_gl);
		if(old_mode!=GL.GL_MODELVIEW){
			i_gl.glMatrixMode(GL.GL_MODELVIEW);
			i_gl.glLoadMatrixd(this._ms.getGlMarkerMatrix(i_id), 0);
			i_gl.glMatrixMode(old_mode);
		}else{
			i_gl.glLoadMatrixd(this._ms.getGlMarkerMatrix(i_id), 0);
		}
	}
	//
	// Graphics toolkit
	//
	public void drawBackground(GL i_gl,INyARRgbRaster i_bg_image) throws NyARException
	{
		i_gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear the buffers for new frame.
		NyARGLDrawUtil.drawBackGround(i_gl,i_bg_image, 1.0);
	}
	public void drawBackground(GL i_gl,INyARGrayscaleRaster i_bg_image) throws NyARException
	{
		i_gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear the buffers for new frame.
		NyARGLDrawUtil.drawBackGround(i_gl,i_bg_image, 1.0);
	}
	
	/**
	 * 指定位置にカラーキューブを書き込みます。
	 * @param i_gl
	 * @param i_size_per_mm
	 * @param i_x
	 * @param i_y
	 * @param i_z
	 */
	public void colorCube(GL i_gl,float i_size_per_mm,double i_x,double i_y,double i_z)
	{
		int old_mode=this.getGlMatrixMode(i_gl);
		i_gl.glMatrixMode(GL.GL_MODELVIEW);
		i_gl.glPushMatrix();
		i_gl.glTranslated(i_x,i_y,i_z);
		NyARGLDrawUtil.drawColorCube(i_gl,i_size_per_mm);
		i_gl.glPopMatrix();
		i_gl.glMatrixMode(old_mode);
	}
	
	public void setColor(GL i_gl,float r,float g,float b)
	{
		i_gl.glColor3f(r,g,b);
	}
	public void setStrokeWeight(GL i_gl,float i_width)
	{
		i_gl.glLineWidth(i_width);
	}
	/**
	 * この関数は、現在のカラーで
	 * @param i_gl
	 * @param i_x
	 * @param i_y
	 * @param i_x2
	 * @param i_y2
	 */
	public void line(GL i_gl,float i_x,double i_y,double i_x2,double i_y2)
	{
		int old_mode=this.getGlMatrixMode(i_gl);
		i_gl.glMatrixMode(GL.GL_MODELVIEW);
		i_gl.glBegin(GL.GL_LINE);
		
		i_gl.glVertex2d(i_x,i_y);
		i_gl.glEnd();
		i_gl.glMatrixMode(old_mode);
	}
	public void polygon(GL i_gl,NyARDoublePoint2d[] i_vertex)
	{
		int old_mode=this.getGlMatrixMode(i_gl);
		i_gl.glMatrixMode(GL.GL_MODELVIEW);
		i_gl.glBegin(GL.GL_LINE_LOOP);
		for(int i=0;i<i_vertex.length;i++)
		{
			i_gl.glVertex2d(i_vertex[i].x,i_vertex[i].y);
		}
		i_gl.glEnd();
		i_gl.glMatrixMode(old_mode);
	}
	public void polygon(GL i_gl,NyARIntPoint2d[] i_vertex)
	{
		int old_mode=this.getGlMatrixMode(i_gl);
		i_gl.glMatrixMode(GL.GL_MODELVIEW);
		i_gl.glBegin(GL.GL_LINE_LOOP);
		for(int i=0;i<i_vertex.length;i++)
		{
			i_gl.glVertex2d(i_vertex[i].x,i_vertex[i].y);
		}
		i_gl.glEnd();
		i_gl.glMatrixMode(old_mode);
	}
	/**
	 * ラスタを現在の表面に描画します。
	 * @param i_gl
	 * @param i_x
	 * @param i_y
	 * @param i_raster
	 * @throws NyARException
	 */
	public void drawImage2d(GL i_gl,double i_x, double i_y, NyARRgbRaster i_raster) throws NyARException
	{
		i_gl.glPushMatrix();
		try{
			i_gl.glMatrixMode(GL.GL_MODELVIEW);
			i_gl.glLoadIdentity();
			i_gl.glTranslated(i_x,i_y,0);
			NyARGLDrawUtil.drawRaster(i_gl, i_raster);
		}finally{
			i_gl.glPopMatrix();
		}
	}
	/**
	 * {@link #drawImage2d(GL, double, double, NyARRgbRaster)}のラッパーです。引数はこの関数を参照してください。
	 * 
	 * @param i_gl
	 * @param i_x
	 * @param i_y
	 * @param i_bitmap
	 * @throws NyARException
	 */
	
	public void drawImage2d(GL i_gl,double i_x, double i_y, BufferedImage i_bitmap) throws NyARException
	{
		this.drawImage2d(i_gl, i_x, i_y,new NyARBufferedImageRaster(i_bitmap));
	}

	
}