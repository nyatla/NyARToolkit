/**
 * NyARRaster_RGBにOpenGL向け関数を追加したものです。
 * 
 * (c)2008 R.iizuka
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.gutil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARParam;
import jp.nyatla.nyartoolkit.jmfutil.*;

public class GLNyARRaster_RGB extends JmfNyARRaster_RGB
{
    private NyARParam cparam;
    private GL ref_gl;
    private GLU glu;
    private byte[] gl_buf;
    private int gl_flag;

    public GLNyARRaster_RGB(GL i_ref_gl,NyARParam i_cparam)
    {
	super(i_cparam.getX(),i_cparam.getY());
	gl_flag=GL.GL_RGB;
	cparam=i_cparam;
	ref_gl=i_ref_gl;
	glu=new GLU();
	this.gl_buf=new byte[width*height*3];
    }
    public void setBuffer(javax.media.Buffer i_buffer,boolean i_is_reverse) throws NyARException
    {
	super.setBuffer(i_buffer);
	//メモ：この時点では、ref_dataにはi_bufferの参照値が入ってる。
	
	//GL用のデータを準備
	if(i_is_reverse){
	    int length=width*3;
	    int src_idx=0;
	    int dest_idx=(height-1)*length;
	    for(int i=0;i<height;i++){
		System.arraycopy(ref_buf,src_idx,gl_buf,dest_idx,length);
		src_idx+=length;
		dest_idx-=length;
	    }
	}else{
	    System.arraycopy(ref_buf,0,gl_buf,0,this.ref_buf.length);
	}
	//GLのフラグ設定
	switch(this.pix_type){
	case GLNyARRaster_RGB.PIXEL_ORDER_BGR:
            gl_flag=GL.GL_BGR;
            break;
	case GLNyARRaster_RGB.PIXEL_ORDER_RGB:
            gl_flag=GL.GL_RGB;
            break;
        default:
            throw new NyARException();
	}
	//ref_bufをgl_bufに差し替える
	ref_buf=gl_buf;
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
    	zoomf = (float)zoom;
        ref_gl.glDisable(GL.GL_TEXTURE_2D);
        ref_gl.glGetIntegerv(GL.GL_VIEWPORT,params);
        ref_gl.glPixelZoom(zoomf * ((float)(params.get(2)) / (float)width),-zoomf * ((float)(params.get(3)) / (float)height));
        ref_gl.glRasterPos2i(-1,1);
        ref_gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        ByteBuffer buf=ByteBuffer.wrap(ref_buf);
        ref_gl.glDrawPixels(width,height,gl_flag,GL.GL_UNSIGNED_BYTE,buf);
    } 
}
