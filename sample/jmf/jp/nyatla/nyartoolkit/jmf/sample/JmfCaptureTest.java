/**
 * VFMキャプチャテストプログラム
 * (c)2008 A虎＠nyatla.jp
 * airmail@ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.jmf.sample;

import javax.media.*;

import javax.media.util.BufferToImage;
import javax.media.format.*;
import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;

import java.awt.*;



public class JmfCaptureTest extends Frame implements JmfCaptureListener{
    public JmfCaptureTest() throws NyARException
    {
        setTitle("JmfCaptureTest");
        setBounds(0,0,320+64,240+64);     
        capture=new JmfCameraCapture(320,240,30f,JmfCameraCapture.PIXEL_FORMAT_RGB);
        capture.setCaptureListener(this);
    }



    private JmfCameraCapture  capture;
    public void onUpdateBuffer(Buffer i_buffer)
    {
	BufferToImage b2i=new BufferToImage((VideoFormat)i_buffer.getFormat());
	Image img=b2i.createImage(i_buffer);
        Graphics g = getGraphics();        
        g.drawImage(img, 32, 32,this);       
    }
    private void startCapture()
    {
	try{
	    capture.start();
	}catch(Exception e){
	    e.printStackTrace();
	}
    }
    public static void main(String[] args) {
	try{
            JmfCaptureTest mainwin = new JmfCaptureTest();
            mainwin.setVisible(true);
            mainwin.startCapture();
	}catch(Exception e){
	    e.printStackTrace();
	}
        
    }

}
