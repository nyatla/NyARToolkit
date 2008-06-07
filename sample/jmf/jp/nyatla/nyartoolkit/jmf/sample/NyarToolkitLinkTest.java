/**
 * VFM+ARToolkitテストプログラム
 * カメラから取り込んだデータからマーカーを検出して、一致度と変換行列を表示します。
 * (c)2008 A虎＠nyatla.jp
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.jmf.sample;

import javax.media.*;

import javax.media.util.BufferToImage;
import javax.media.format.*;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;

import java.awt.*;

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.detector.*;






public class NyarToolkitLinkTest extends Frame implements JmfCaptureListener
{
    private final String CARCODE_FILE ="../../Data/patt.hiro";
    private final String PARAM_FILE   ="../../Data/camera_para.dat";
    private JmfCameraCapture capture;
    NyARSingleDetectMarker nya;
    JmfNyARRaster_RGB raster;

    public NyarToolkitLinkTest() throws NyARException,NyARException
    {
        setTitle("JmfCaptureTest");
        setBounds(0,0,320+64,240+64);     
        //キャプチャの準備
        capture=new JmfCameraCapture(320,240,30f,JmfCameraCapture.PIXEL_FORMAT_RGB);
        capture.setCaptureListener(this);
        
        //NyARToolkitの準備
        NyARParam ar_param=new NyARParam();
        NyARCode ar_code  =new NyARCode(16,16);
        ar_param.loadFromARFile(PARAM_FILE);
        ar_param.changeSize(320,240);
        nya=new NyARSingleDetectMarker(ar_param,ar_code,80.0);
        ar_code.loadFromARFile(CARCODE_FILE);
        //キャプチャイメージ用のラスタを準備
        raster=new JmfNyARRaster_RGB(320,240);
    }



    public void onUpdateBuffer(Buffer i_buffer)
    {
	try{
            //キャプチャしたバッファをラスタにセット
	    raster.setBuffer(i_buffer);

            //キャプチャしたイメージを表示用に加工
            BufferToImage b2i=new BufferToImage((VideoFormat)i_buffer.getFormat());
            Image img=b2i.createImage(i_buffer);

            Graphics g = getGraphics();            
            double[][] atm=null;

            //マーカー検出
            boolean is_marker_exist=nya.detectMarkerLite(raster,100);
            if(is_marker_exist){
                //変換行列を取得
                atm=nya.getTransmationMatrix().getArray();
            }
            //情報を画面に書く       
            g.drawImage(img, 32, 32,this);
            if(is_marker_exist){
                g.drawString("マーカー検出:"+nya.getConfidence(),32,50);
                for(int i=0;i<3;i++){
                    for(int i2=0;i2<4;i2++){
                	g.drawString("["+i+"]["+i2+"]"+atm[i][i2],32,50+(1+i2*3+i)*16);
                    }
                    
                }
            }else{
                g.drawString("マーカー未検出:",32,100);
            }
	}catch(Exception e){
	    e.printStackTrace();
	}
       
        
        
        
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
            NyarToolkitLinkTest mainwin = new NyarToolkitLinkTest();
            mainwin.setVisible(true);
            mainwin.startCapture();
	}catch(Exception e){
	    e.printStackTrace();
	}
        
    }

}
