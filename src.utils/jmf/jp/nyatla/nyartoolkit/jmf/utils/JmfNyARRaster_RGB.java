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
import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.raster.NyARRaster;
import jp.nyatla.nyartoolkit.core.raster.NyARRaster_BasicClass;


public class JmfNyARRaster_RGB  extends NyARRaster_BasicClass
{
    public final static int PIXEL_ORDER_RGB=1;
    public final static int PIXEL_ORDER_BGR=2;
    protected int _pix_type;
    private int _red_idx;
    private int _green_idx;
    private int _blue_idx;
    protected byte[] _ref_buf;

    /**
     * RGB形式のJMFバッファをラップするオブジェクトをつくります。
     * 生成直後のオブジェクトはデータを持ちません。
     * メンバ関数はsetBufferを実行後に使用可能になります。
     */
    public JmfNyARRaster_RGB(int i_width,int i_height)
    {
	this._ref_buf=null;
	this._size.w=i_width;
	this._size.h=i_height;
    }
    public void getPixel(int i_x,int i_y,int[] i_rgb)
    {
        int bp=(i_x+i_y*this._size.w)*3;
        byte[] ref=this._ref_buf;
        i_rgb[0]=(ref[bp+this._red_idx] & 0xff);//R
        i_rgb[1]=(ref[bp+this._green_idx] & 0xff);//G
        i_rgb[2]=(ref[bp+this._blue_idx] & 0xff);//B
    }
    public void getPixelSet(int[] i_x,int i_y[],int i_num,int[] o_rgb)
    {
	int ri=this._red_idx;
	int bi=this._green_idx;
	int gi=this._blue_idx;
	int width=this._size.w;
	byte[] ref=this._ref_buf;
	int bp;
	for(int i=i_num-1;i>=0;i--){
	    bp=(i_x[i]+i_y[i]*width)*3;
	    o_rgb[i*3+0]=(ref[bp+ri] & 0xff);//R
	    o_rgb[i*3+1]=(ref[bp+gi] & 0xff);//G
	    o_rgb[i*3+2]=(ref[bp+bi] & 0xff);//B
	}	
	return;
    }
    public Object getBufferObject()
    {
	return this._ref_buf;
    }
    public int getBufferType() throws NyARException
    {
	switch(this._pix_type){
	case PIXEL_ORDER_RGB:return BUFFERFORMAT_BYTE_R8G8B8_24;
	case PIXEL_ORDER_BGR:return BUFFERFORMAT_BYTE_B8G8R8_24;
	default:
	    throw new NyARException();
	}
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
        if(this._size.w!=s.width || this._size.h !=s.height){
	    throw new NyARException();
        }
	//データ配列の確認
	this._red_idx  =i_fmt.getRedMask()-1;
	this._green_idx=i_fmt.getGreenMask()-1;
	this._blue_idx =i_fmt.getBlueMask()-1;
	
	//色配列の特定
	if(this._red_idx==0 && this._blue_idx==2){
	    this._pix_type=PIXEL_ORDER_RGB;
	}else if(this._red_idx==2 && this._blue_idx==0){
	    this._pix_type=PIXEL_ORDER_BGR;
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
        this._ref_buf=(byte[])i_buffer.getData();
    }    
    /**
     * ピクセルの順序タイプを返します。
     * @return
     * その値
     */
    public int getPixelOrder()
    {
	return this._pix_type;
    }
    /**
     * データを持っているかを返します。
     * @return
     */
    public boolean hasData()
    {
	return this._ref_buf!=null;
    }    
}
