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





public class NyARRaster_Int2d extends NyARRaster_BasicClass
{
    protected int[][] _ref_buf;
    public NyARRaster_Int2d(int i_width,int i_height)
    {
	this._ref_buf=new int[i_height][i_width];
	this._size.w=i_width;
	this._size.h=i_height;
	
    }
    
    
    public void getPixel(int i_x,int i_y,int[] i_rgb)
    {
        int[][] ref=this._ref_buf;
        int v=ref[i_x][i_y];
        i_rgb[0]=v;
        i_rgb[1]=v;
        i_rgb[2]=v;
    }
    public void getPixelSet(int[] i_x,int i_y[],int i_num,int[] o_rgb)
    {
	int[][] ref=this._ref_buf;
	for(int i=i_num-1;i>=0;i--){
	    int v=ref[i_x[i]][i_y[i]];
	    o_rgb[i*3+0]=v;
	    o_rgb[i*3+1]=v;
	    o_rgb[i*3+2]=v;
	}	
    }
    public Object getBufferObject()
    {
	return this._ref_buf;
    }
    /**
     * バッファオブジェクトのタイプを返します。
     * @return
     */
    public int getBufferType()
    {
	return BUFFERFORMAT_INT2D;
    }    
}

