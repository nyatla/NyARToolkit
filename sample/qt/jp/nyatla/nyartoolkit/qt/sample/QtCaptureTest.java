/**
 * VFMキャプチャテストプログラム
 * (c)2008 A虎＠nyatla.jp
 * airmail@ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.qt.sample;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.qt.utils.*;

import java.awt.*;



public class QtCaptureTest extends Frame implements QtCaptureListener{
    public QtCaptureTest() throws NyARException
    {
        setTitle("QtCaptureTest");
        setBounds(0,0,320+64,240+64);     
        capture=new QtCameraCapture(320,240,30f);
        capture.setCaptureListener(this);
        //キャプチャイメージ用のラスタを準備
        raster=new QtNyARRaster_RGB(320,240);
    }



    private QtCameraCapture  capture;
    private QtNyARRaster_RGB raster;
    public void onUpdateBuffer(byte[] pixels)
    {
    raster.setBuffer(pixels);
	Image img=raster.createImage();
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
            QtCaptureTest mainwin = new QtCaptureTest();
            mainwin.setVisible(true);
            mainwin.startCapture();
	}catch(Exception e){
	    e.printStackTrace();
	}
        
    }

}
