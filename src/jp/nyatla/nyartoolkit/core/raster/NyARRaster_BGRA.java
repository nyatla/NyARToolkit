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
        int bp=(i_x+i_y*width)*4;
        return (ref_buf[bp] & 0xff)+(ref_buf[bp+1] & 0xff)+(ref_buf[bp+2] & 0xff);
    }
    public int getWidth()
    {
        return width;
    }
    public int getHeight()
    {
	return height;
    }
    public void pickRgbArray(int i_x,int i_y,int[] i_rgb)
    {
        int bp=(i_x+i_y*width)*4;
        i_rgb[0]=(ref_buf[bp+2] & 0xff);//R
        i_rgb[1]=(ref_buf[bp+1] & 0xff);//G
        i_rgb[2]=(ref_buf[bp+0] & 0xff);//B
    }
}

