/**
 * NyARRaster_RGBにOpenGL向け関数を追加したもの
 * (c)2008 A虎＠nyatla.jp
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.java3d.utils;

import java.awt.image.*;
import java.awt.color.*;

import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;



import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARParam;
import jp.nyatla.nyartoolkit.jmf.utils.*;


public class J3dNyARRaster_RGB extends JmfNyARRaster_RGB
{
//    public ImageUpdater updater; 
    private ImageComponent2D imc2d;
    private byte[] i2d_buf;

    public void setBuffer(javax.media.Buffer i_buffer) throws NyARException
    {
        super.setBuffer(i_buffer);
        //メモ：この時点では、ref_dataにはi_bufferの参照値が入ってる。
        synchronized(imc2d){
            //キャプチャデータをi2dのバッファにコピーする。（これ省略したいなあ…。）
            System.arraycopy(ref_buf,0,i2d_buf,0,this.i2d_buf.length);
        }
	//ここでref_bufの参照値をref_bufへ移動
        ref_buf=i2d_buf;
    }  
    public J3dNyARRaster_RGB(NyARParam i_cparam)
    {
	super(i_cparam.getX(),i_cparam.getY());

	//RGBのラスタを作る。
//	ColorSpace cs=ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
//	ComponentColorModel cm;
//	cm=new ComponentColorModel(cs,new int[]{8,8,8},false,false,ComponentColorModel.OPAQUE,DataBuffer.TYPE_BYTE);
//	java.awt.image.WritableRaster raster=cm.createCompatibleWritableRaster(width,height);
//	i2d_buf=((DataBufferByte)raster.getDataBuffer()).getData();
//	BufferedImage background_image = new BufferedImage(cm,raster, false, null);
	BufferedImage background_image = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
	i2d_buf=((DataBufferByte)background_image.getRaster().getDataBuffer()).getData();
	//
	imc2d= new ImageComponent2D(ImageComponent2D.FORMAT_RGB, background_image, true, true);
	imc2d.setCapability(ImageComponent.ALLOW_IMAGE_WRITE);
    }
    /**
     * このオブジェクトと連動するImageComponent2Dオブジェクトの参照値を得る。
     * @return
     */
    public ImageComponent2D getImageComponent2D()
    {
	return imc2d;
    }
}
