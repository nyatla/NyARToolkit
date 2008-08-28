package jp.nyatla.nyartoolkit.core.rasterreader;


import jp.nyatla.nyartoolkit.core.types.*;

public class NyARRgbPixelReader_RGB24 implements INyARRgbPixelReader
{
    private byte[] _ref_buf;
    private TNyARIntSize _size;
    public NyARRgbPixelReader_RGB24(byte[] i_buf,TNyARIntSize i_size)
    {
	this._ref_buf=i_buf;
	this._size=i_size;
    }
    public void getPixel(int i_x,int i_y,int[] o_rgb)
    {
	byte[] ref=this._ref_buf;
	int bp=(i_x+i_y*this._size.w)*4;
	o_rgb[0]=(ref[bp+2] & 0xff);//R
	o_rgb[1]=(ref[bp+1] & 0xff);//G
	o_rgb[2]=(ref[bp+0] & 0xff);//B
	return;
    }
    public void getPixelSet(int[] i_x,int i_y[],int i_num,int[] o_rgb)
    {
	int width=this._size.w;
	byte[] ref=this._ref_buf;
	int bp;
	for(int i=i_num-1;i>=0;i--){
	    bp=(i_x[i]+i_y[i]*width)*4;
	    o_rgb[i*3+0]=(ref[bp+2] & 0xff);//R
	    o_rgb[i*3+1]=(ref[bp+1] & 0xff);//G
	    o_rgb[i*3+2]=(ref[bp+0] & 0xff);//B
	}	
    }	
}