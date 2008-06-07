/**
 * RGB形式のJMFバッファをラップするNyARRasterです。
 * JMFから得たラスタデータのピクセル並び順を考慮します。
 * (c)2008 A虎＠nyatla.jp
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.jmf.utils;


import javax.media.format.RGBFormat;
import java.awt.Dimension;
import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARRaster;


public class JmfNyARRaster_RGB implements NyARRaster
{
    public final static int PIXEL_ORDER_RGB=1;
    public final static int PIXEL_ORDER_BGR=2;
    protected int pix_type;
    private int red_idx;
    private int green_idx;
    private int blue_idx;
    protected byte[] ref_buf;
    protected int width=0;
    protected int height=0;

    /**
     * RGB形式のJMFバッファをラップするオブジェクトをつくります。
     * 生成直後のオブジェクトはデータを持ちません。
     * メンバ関数はsetBufferを実行後に使用可能になります。
     */
    public JmfNyARRaster_RGB(int i_width,int i_height)
    {
	ref_buf=null;
	width=i_width;
	height=i_height;
    }
    /**
     * フォーマットを解析して、インスタンスのフォーマットプロパティを初期化します。
     * 
     * @param i_buffer
     * @throws NyARException
     */
    protected void initFormatProperty(RGBFormat i_fmt) throws NyARException
    {
	//データサイズの確認
        Dimension s=i_fmt.getSize();
        if(width!=s.width || height !=s.height){
	    throw new NyARException();
        }
	//データ配列の確認
	red_idx  =i_fmt.getRedMask()-1;
	green_idx=i_fmt.getGreenMask()-1;
	blue_idx =i_fmt.getBlueMask()-1;
	
	//色配列の特定
	if(red_idx==0 && blue_idx==2){
	    pix_type=PIXEL_ORDER_RGB;
	}else if(red_idx==2 && blue_idx==0){
	    pix_type=PIXEL_ORDER_BGR;
	}else{
	    throw new NyARException("Unknown pixel order.");
	}	
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
    public void setBuffer(javax.media.Buffer i_buffer) throws NyARException
    {
	initFormatProperty((RGBFormat)i_buffer.getFormat());
        ref_buf=(byte[])i_buffer.getData();
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
        i_rgb[0]=(ref[bp+this.red_idx] & 0xff);//R
        i_rgb[1]=(ref[bp+this.green_idx] & 0xff);//G
        i_rgb[2]=(ref[bp+this.blue_idx] & 0xff);//B
    }
    /**
     * ピクセルの順序タイプを返します。
     * @return
     * その値
     */
    public int getPixelOrder()
    {
	return pix_type;
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
	int ri=this.red_idx;
	int bi=this.green_idx;
	int gi=this.blue_idx;
	int width=this.width;
	byte[] ref=this.ref_buf;
	int bp;
	for(int i=i_num-1;i>=0;i--){
	    bp=(i_x[i]+i_y[i]*width)*3;
	    o_rgb[i*3+0]=(ref[bp+ri] & 0xff);//R
	    o_rgb[i*3+1]=(ref[bp+gi] & 0xff);//G
	    o_rgb[i*3+2]=(ref[bp+bi] & 0xff);//B
	}	
	return;
    }
}
