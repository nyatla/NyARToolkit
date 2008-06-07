/**
 * NyARRaster_RGBにOpenGL向け関数を追加したもの
 * (c)2008 R.iizuka
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.gutil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import jp.nyatla.nyartoolkit.core.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.NyARRaster_RGB;

public class GLNyARRaster_RGB extends NyARRaster_RGB
{
    private NyARParam cparam;
    private GL ref_gl;
    private GLU glu;
    public GLNyARRaster_RGB(GL i_ref_gl,NyARParam i_cparam,int i_width,int i_height)
    {
        width=i_width;
        height=i_height;
	cparam=i_cparam;
	ref_gl=i_ref_gl;
	glu=new GLU();
	this.ref_buf=new byte[i_width*i_height*3];
    }
    public void setRawData(byte[] i_buf,boolean i_is_reverse)
    {
	if(i_is_reverse){
	    int length=width*3;
	    int src_idx=0;
	    int dest_idx=(height-1)*length;
	    for(int i=0;i<height;i++){
		System.arraycopy(i_buf,src_idx,ref_buf,dest_idx,length);
		src_idx+=length;
		dest_idx-=length;
	    }
	}else{
	    System.arraycopy(i_buf,0,ref_buf,0,this.ref_buf.length);
	}
    }
    /**
     * 保持してるイメージをGLに出力する。
     * @param image
     * @param zoom
     */
    public void glDispImage(double zoom)
    {
    	IntBuffer texEnvModeSave=IntBuffer.allocate(1);	
    	boolean lightingSave;
    	boolean depthTestSave;

    	// Prepare an orthographic projection, set camera position for 2D drawing, and save GL state.
    	ref_gl.glGetTexEnviv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, texEnvModeSave); // Save GL texture environment mode.
    	if (texEnvModeSave.array()[0] != GL.GL_REPLACE){
    	    ref_gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
    	}
    	lightingSave = ref_gl.glIsEnabled(GL.GL_LIGHTING);			// Save enabled state of lighting.
    	if (lightingSave == true){
    	    ref_gl.glDisable(GL.GL_LIGHTING);
    	}
    	depthTestSave = ref_gl.glIsEnabled(GL.GL_DEPTH_TEST);		// Save enabled state of depth test.
    	if (depthTestSave == true){
    	    ref_gl.glDisable(GL.GL_DEPTH_TEST);
    	}
    	ref_gl.glMatrixMode(GL.GL_PROJECTION);
    	ref_gl.glPushMatrix();
    	ref_gl.glLoadIdentity();
    	glu.gluOrtho2D(0.0,0.0,cparam.getX(),cparam.getY());
    	ref_gl.glMatrixMode(GL.GL_MODELVIEW);
     	ref_gl.glPushMatrix();
    	ref_gl.glLoadIdentity();
   	ref_gl.glRotatef(0.0f,0.0f,180f,0f);
   	arglDispImageStateful(zoom);


    	// Restore previous projection, camera position, and GL state.
        ref_gl.glMatrixMode(GL.GL_PROJECTION);
        ref_gl.glPopMatrix();
        ref_gl.glMatrixMode(GL.GL_MODELVIEW);
        ref_gl.glPopMatrix();
    	if (depthTestSave){
    	    ref_gl.glEnable(GL.GL_DEPTH_TEST);			// Restore enabled state of depth test.
    	}
    	if (lightingSave){
    	    ref_gl.glEnable(GL.GL_LIGHTING);			// Restore enabled state of lighting.
    	}
    	if (texEnvModeSave.get(0) != GL.GL_REPLACE){
    	    ref_gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, texEnvModeSave.get(0)); // Restore GL texture environment mode.
    	}
    	ref_gl.glEnd();
    }
    /**
     * arglDispImageStateful関数モドキ
     * @param image
     * @param zoom
     */
    private void arglDispImageStateful(double zoom)
    {
    	float zoomf;
    	IntBuffer params=IntBuffer.allocate(4);
    	int xsize=cparam.getX();
    	int ysize=cparam.getY();
    	zoomf = (float)zoom;
        ref_gl.glDisable(GL.GL_TEXTURE_2D);
        ref_gl.glGetIntegerv(GL.GL_VIEWPORT,params);
        ref_gl.glPixelZoom(zoomf * ((float)(params.get(2)) / (float)xsize),-zoomf * ((float)(params.get(3)) / (float)ysize));
        ref_gl.glRasterPos2i(-1,1);
        ref_gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        ByteBuffer buf=ByteBuffer.wrap(ref_buf);
        ref_gl.glDrawPixels(xsize,ysize,GL.GL_BGR,GL.GL_UNSIGNED_BYTE,buf);
    } 
}
