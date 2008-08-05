/**
 * RGB形式のbyte配列をラップするNyARRasterです。
 * 保持したデータからBufferedImageを出力する機能も持ちます。
 * (c)2008 arc@dmz, A虎＠nyatla.jp
 * arc@digitalmuseum.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.qt.utils;


import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARRaster;


public class QtNyARRaster_RGB implements NyARRaster
{
    protected byte[] ref_buf;
    protected int width=0;
    protected int height=0;
	private WritableRaster raster;
	private BufferedImage image;


    /**
     * RGB形式のJMFバッファをラップするオブジェクトをつくります。
     * 生成直後のオブジェクトはデータを持ちません。
     * メンバ関数はsetBufferを実行後に使用可能になります。
     */
    public QtNyARRaster_RGB(int i_width,int i_height)
    {
	ref_buf=null;
	width=i_width;
	height=i_height;
	raster = WritableRaster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
			width, height,
			width*3, 3,
			new int[] { 0, 1, 2 }, null); 
	image = new BufferedImage(width, height,
			BufferedImage.TYPE_3BYTE_BGR);
    }
    /**
     * javax.media.Bufferを分析して、その分析結果をNyARRasterに適合する形で保持します。
     * 関数実行後に外部でi_bufferの内容変更した場合には、再度setBuffer関数を呼び出してください。
     * @param i_buffer
     * RGB形式のデータを格納したjavax.media.Bufferオブジェクトを指定してください。
     * @return
     * i_bufferをラップしたオブジェクトを返します。
     * @throws NyARException
     */
    public void setBuffer(byte[] i_buffer)
    {
        ref_buf=i_buffer;
    }
    public int getPixelTotal(int i_x,int i_y)
    {
        int bp=(i_x+i_y*width)*3;
        byte[] ref=this.ref_buf;
        return (ref[bp] & 0xff)+(ref[bp+1] & 0xff)+(ref[bp+2] & 0xff);
    }
    public void getPixelTotalRowLine(int i_row,int[] o_line)
    {
        final byte[] ref=this.ref_buf;
        int bp=(i_row+1)*this.width*3-3;
        for(int i=this.width-1;i>=0;i--){
	    o_line[i]=(ref[bp] & 0xff)+(ref[bp+1] & 0xff)+(ref[bp+2] & 0xff);
	    bp-=3;
	}
    }    
    public int getWidth()
    {
        return width;
    }
    public int getHeight()
    {
        return height;
    }
    public void getPixel(int i_x,int i_y,int[] i_rgb)
    {
        int bp=(i_x+i_y*this.width)*3;
        byte[] ref=this.ref_buf;
        i_rgb[0]=ref[bp+0];//R
        i_rgb[1]=ref[bp+1];//G
        i_rgb[2]=ref[bp+2];//B
    }
    /**
     * データを持っているかを返します。
     * @return
     */
    public boolean hasData()
    {
	return ref_buf!=null;
    }
    public void getPixelSet(int[] i_x,int i_y[],int i_num,int[] o_rgb)
    {
	int width=this.width;
	byte[] ref=this.ref_buf;
	int bp;
	for(int i=i_num-1;i>=0;i--){
	    bp=(i_x[i]+i_y[i]*width)*3;
	    o_rgb[i*3+0]=ref[bp+0];//R
	    o_rgb[i*3+1]=ref[bp+1];//G
	    o_rgb[i*3+2]=ref[bp+2];//B
	}	
	return;
    }

    /** 保持しているデータからBufferedImageを作って返します。 */
    public BufferedImage createImage() {
		raster.setDataElements(0, 0, width, height, ref_buf);
    	image.setData(raster);
    	return image;
    }
}
