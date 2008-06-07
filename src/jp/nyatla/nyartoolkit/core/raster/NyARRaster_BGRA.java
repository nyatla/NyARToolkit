/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.raster;


public class NyARRaster_BGRA implements NyARRaster
{
    private byte[] ref_buf;
    private int width;
    private int height;
    public static NyARRaster_BGRA wrap(byte[] i_buffer,int i_width,int i_height)
    {
        NyARRaster_BGRA new_inst=new NyARRaster_BGRA();
        new_inst.ref_buf=i_buffer;
        new_inst.width  =i_width;
        new_inst.height =i_height;
        return new_inst;
    }
    //RGBの合計値を返す
    public int getPixelTotal(int i_x,int i_y)
    {
        int bp=(i_x+i_y*this.width)*4;
        byte[] ref=this.ref_buf;
        return (ref[bp] & 0xff)+(ref[bp+1] & 0xff)+(ref[bp+2] & 0xff);
    }
    public void getPixelTotalRowLine(int i_row,int[] o_line)
    {
        final byte[] ref=this.ref_buf;
        int bp=(i_row+1)*this.width*4-4;
        for(int i=this.width-1;i>=0;i--){
	    o_line[i]=(ref[bp] & 0xff)+(ref[bp+1] & 0xff)+(ref[bp+2] & 0xff);
	    bp-=4;
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
	byte[] ref=this.ref_buf;
        int bp=(i_x+i_y*this.width)*4;
        i_rgb[0]=(ref[bp+2] & 0xff);//R
        i_rgb[1]=(ref[bp+1] & 0xff);//G
        i_rgb[2]=(ref[bp+0] & 0xff);//B
    }
    public void getPixelSet(int[] i_x,int i_y[],int i_num,int[] o_rgb)
    {
	int width=this.width;
	byte[] ref=this.ref_buf;
	int bp;
	for(int i=i_num-1;i>=0;i--){
	    bp=(i_x[i]+i_y[i]*width)*4;
	    o_rgb[i*3+0]=(ref[bp+2] & 0xff);//R
	    o_rgb[i*3+1]=(ref[bp+1] & 0xff);//G
	    o_rgb[i*3+2]=(ref[bp+0] & 0xff);//B
	}	
    }
}

