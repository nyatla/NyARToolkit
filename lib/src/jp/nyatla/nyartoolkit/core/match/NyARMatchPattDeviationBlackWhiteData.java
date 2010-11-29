/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.match;


import jp.nyatla.nyartoolkit.core.raster.*;


/**
 * INyARMatchPattのColor差分ラスタを格納するクラスです。
 *
 */
public class NyARMatchPattDeviationBlackWhiteData
{
	private int _data[];
	private double _pow;
	//
	private int _number_of_pixels;
	public int[] refData()
	{
		return this._data;
	}
	public double getPow()
	{
		return this._pow;
	}
	                  
	public NyARMatchPattDeviationBlackWhiteData(int i_width,int i_height)
	{
		this._number_of_pixels=i_height*i_width;
		this._data=new int[this._number_of_pixels];
		return;
	}
	/**
	 * XRGB[width*height]の配列から、パターンデータを構築。
	 * @param i_buffer
	 */
	public void setRaster(INyARRaster i_raster)
	{
		//i_buffer[XRGB]→差分[BW]変換			
		int i;
		int ave;//<PV/>
		int rgb;//<PV/>
		final int[] linput=this._data;//<PV/>
		final int[] buf=(int[])i_raster.getBuffer();

		// input配列のサイズとwhも更新// input=new int[height][width][3];
		final int number_of_pixels=this._number_of_pixels;

		//<平均値計算(FORの1/8展開)/>
		ave = 0;
		for(i=number_of_pixels-1;i>=0;i--){
			rgb = buf[i];
			ave += ((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff);
		}
		ave=(number_of_pixels*255*3-ave)/(3*number_of_pixels);
		//
		int sum = 0,w_sum;
		
		//<差分値計算/>
		for (i = number_of_pixels-1; i >= 0;i--) {
			rgb = buf[i];
			w_sum =((255*3-(rgb & 0xff)-((rgb >> 8) & 0xff)-((rgb >> 16) & 0xff))/3)-ave;
			linput[i] = w_sum;
			sum += w_sum * w_sum;
		}
		final double p=Math.sqrt((double) sum);
		this._pow=p!=0.0?p:0.0000001;
		return;
	}
}
