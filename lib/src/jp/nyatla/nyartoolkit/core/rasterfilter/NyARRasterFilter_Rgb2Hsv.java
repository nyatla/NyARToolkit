/* 
 * PROJECT: NyARToolkit(Extension)
 * -------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
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
package jp.nyatla.nyartoolkit.core.rasterfilter;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * このクラスは、RGB画像をHSV画像に変換するフィルタです。
 * 対応している入力画素形式は以下の通りです。
 * <p>入力画素形式
 * <ul>
 * <li>NyARBufferType.BYTE1D_B8G8R8_24
 * </ul>
 * </p>
 * 出力画素形式は、{@link NyARBufferType#INT1D_X7H9S8V8_32}形式のHSVラスタに限られます。
 *
 */
public class NyARRasterFilter_Rgb2Hsv
{
	/** 変換用ドライバのインタフェイス*/	
	public interface IFilter
	{
		public boolean isSupport(INyARRaster i_in,INyARRaster i_out);
		public void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException;		
	}

	private IFilter _do_filter_impl;
	/**
	 * コンストラクタです。
	 * @throws NyARException
	 */
	public NyARRasterFilter_Rgb2Hsv() throws NyARException
	{
		this._do_filter_impl=new Rgb2HsvFilter_Blank();
	}
	protected IFilter createFilter(INyARRaster i_in,INyARRaster i_out) throws NyARException
	{
		if(i_in.getBufferType()==NyARBufferType.INT1D_GRAY_8){
			switch(i_out.getBufferType()){
			case NyARBufferType.BYTE1D_B8G8R8_24:
				return new Rgb2HsvFilter_BYTE1D_B8G8R8_24();
			default:
				break;
			}
		}
		throw new NyARException();
	}	
	/**
	 * 入力ラスタをHSV形式に変換して、出力ラスタへ書込みます。
	 * 画素形式は、コンストラクタに指定した形式に合せてください。
	 */		
	public void doFilter(INyARRgbRaster i_input, INyARRaster i_output) throws NyARException
	{
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);
		if(!this._do_filter_impl.isSupport(i_input,i_output)){
			this._do_filter_impl=this.createFilter(i_input, i_output);
		}
		this._do_filter_impl.doFilter(i_input,i_output,i_input.getSize());
	}	
}

//
//ラスタドライバ
//

class Rgb2HsvFilter_Blank implements NyARRasterFilter_Rgb2Hsv.IFilter
{
	public final boolean isSupport(INyARRaster i_in,INyARRaster i_out)
	{
		return false;
	}
	public void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException
	{
		throw new NyARException();
	}
	
}

class Rgb2HsvFilter_BYTE1D_B8G8R8_24 implements NyARRasterFilter_Rgb2Hsv.IFilter
{
	public final boolean isSupport(INyARRaster i_in,INyARRaster i_out)
	{
		return i_in.isEqualBufferType(NyARBufferType.BYTE1D_B8G8R8_24) && i_out.isEqualBufferType(NyARBufferType.INT1D_X7H9S8V8_32);
	}
	public void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException
	{
		assert(i_input.isEqualBufferType(NyARBufferType.INT1D_X7H9S8V8_32));
		
		int[] out_buf = (int[]) i_output.getBuffer();
		byte[] in_buf = (byte[]) i_input.getBuffer();
		int s;
		for(int i=i_size.h*i_size.w-1;i>=0;i--)
		{
			int r=(in_buf[i*3+2] & 0xff);
			int g=(in_buf[i*3+1] & 0xff);
			int b=(in_buf[i*3+0] & 0xff);
			int cmax,cmin;
			//最大値と最小値を計算
			if(r>g){
				cmax=r;
				cmin=g;
			}else{
				cmax=g;
				cmin=r;
			}
			if(b>cmax){
				cmax=b;
			}
			if(b<cmin){
				cmin=b;
			}
			int h;
			if(cmax==0) {
				s=0;
				h=0;
			}else {
				s=(cmax-cmin)*255/cmax;
				int cdes=cmax-cmin;
				//H成分を計算
				if(cdes!=0){
					if(cmax==r){
						h=(g-b)*60/cdes;
					}else if(cmax==g){
						h=(b-r)*60/cdes+2*60;
					}else{
						h=(r-g)*60/cdes+4*60;
					}
				}else{
					h=0;
				}
			}
			if(h<0)
			{
				h+=360;
			}
			//hsv変換(h9s8v8)
			out_buf[i]=(0x1ff0000&(h<<16))|(0x00ff00&(s<<8))|(cmax&0xff);
		}
		return;
	}
}