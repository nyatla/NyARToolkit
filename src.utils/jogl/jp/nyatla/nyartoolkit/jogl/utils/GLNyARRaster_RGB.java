/**
 * NyARRaster_RGBにOpenGL用のデータ変換機能を追加したものです。
 * 
 * (c)2008 A虎＠nyatla.jp
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.jogl.utils;

import javax.media.format.RGBFormat;
import javax.media.opengl.GL;
import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;
import jp.nyatla.nyartoolkit.core.*;

public class GLNyARRaster_RGB extends JmfNyARRaster_RGB
{
    private byte[] gl_buf;
    private int gl_flag;

    public GLNyARRaster_RGB(GL i_ref_gl,NyARParam i_param)
    {
	super(i_param.getX(),i_param.getY());
	this.gl_flag=GL.GL_RGB;
	this.gl_buf=new byte[this.width*this.height*3];
    }
    public void setBuffer(javax.media.Buffer i_buffer,boolean i_is_reverse) throws NyARException
    {
	//JMFデータでフォーマットプロパティを初期化
	initFormatProperty((RGBFormat)i_buffer.getFormat());
	
	byte[] src_buf=(byte[])i_buffer.getData();
	//GL用のデータを準備
	if(i_is_reverse){
	    int length=this.width*3;
	    int src_idx=0;
	    int dest_idx=(this.height-1)*length;
	    for(int i=0;i<this.height;i++){
		System.arraycopy(src_buf,src_idx,this.gl_buf,dest_idx,length);
		src_idx+=length;
		dest_idx-=length;
	    }
	}else{
	    System.arraycopy(src_buf,0,gl_buf,0,src_buf.length);
	}
	//GLのフラグ設定
	switch(this.pix_type){
	case GLNyARRaster_RGB.PIXEL_ORDER_BGR:
	    this.gl_flag=GL.GL_BGR;
            break;
	case GLNyARRaster_RGB.PIXEL_ORDER_RGB:
	    this.gl_flag=GL.GL_RGB;
            break;
        default:
            throw new NyARException();
	}
	//ref_bufをgl_bufに差し替える
	ref_buf=gl_buf;
    }
    /**
     * GLでそのまま描画できるRGBバッファを返す。
     * @return
     */
    public byte[] getGLRgbArray()
    {
	return this.ref_buf;
    }
    /**
     * GL用のRGBバッファのバイト並びタイプを返す。
     * @return
     */
    public int getGLPixelFlag()
    {
	return this.gl_flag;
    }
}
