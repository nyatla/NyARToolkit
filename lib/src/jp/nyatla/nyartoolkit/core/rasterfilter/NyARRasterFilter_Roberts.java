/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
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
package jp.nyatla.nyartoolkit.core.rasterfilter;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.NyARRasterFilter_Rgb2Hsv.IFilter;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * このクラスは、Roberts法で勾配画像を作ります。
 * 右端と左端の1ピクセルは、常に0が入ります。
 * <p>対応している画素形式は以下の通りです。
 * <li>{@link NyARBufferType#INT1D_GRAY_8}
 * </p>
 * <pre>
 * X=|-1, 0|  Y=|0,-1|
 *   | 0, 1|    |1, 0|
 * V=sqrt(X^2+Y+2)/2
 * </pre>
 */
public class NyARRasterFilter_Roberts
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
	public NyARRasterFilter_Roberts() throws NyARException
	{
		this._do_filter_impl=new RobertsFilter_Blank();
	}
	protected IFilter createFilter(INyARRaster i_in,INyARRaster i_out) throws NyARException
	{
		if(i_in.getBufferType()==NyARBufferType.INT1D_GRAY_8){
			switch(i_out.getBufferType()){
			case NyARBufferType.INT1D_GRAY_8:
				return new RobertsFilter_GRAY_8();
			default:
				break;
			}
		}
		throw new NyARException();
	}	
	/**
	 * 入力ラスタのRoberts勾配を出力ラスタへ書込みます。
	 * 画素形式は、コンストラクタに指定した形式に合せてください。
	 */		
	public void doFilter(INyARRaster i_input, INyARRaster i_output) throws NyARException
	{
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);
		if(!this._do_filter_impl.isSupport(i_input,i_output)){
			this._do_filter_impl=this.createFilter(i_input, i_output);
		}
		this._do_filter_impl.doFilter(i_input,i_output,i_input.getSize());
	}
}

//
//Raster driver
//

class RobertsFilter_Blank implements NyARRasterFilter_Roberts.IFilter
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

class RobertsFilter_GRAY_8 implements NyARRasterFilter_Roberts.IFilter
{
	public final boolean isSupport(INyARRaster i_in,INyARRaster i_out)
	{
		return i_in.isEqualBufferType(NyARBufferType.INT1D_GRAY_8) && i_out.isEqualBufferType(NyARBufferType.INT1D_GRAY_8);
	}	
	public void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException
	{
		assert (i_input.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		assert (i_output.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		int[] in_ptr =(int[])i_input.getBuffer();
		int[] out_ptr=(int[])i_output.getBuffer();
		int width=i_size.w;
		int idx=0;
		int idx2=width;
		int fx,fy;
		int mod_p=(width-2)-(width-2)%8;
		for(int y=i_size.h-2;y>=0;y--){
			int p00=in_ptr[idx++];
			int p10=in_ptr[idx2++];
			int p01,p11;
			int x=width-2;
			for(;x>=mod_p;x--){
				p01=in_ptr[idx++];p11=in_ptr[idx2++];
				fx=p11-p00;fy=p10-p01;
				out_ptr[idx-2]=((fx<0?-fx:fx)+(fy<0?-fy:fy))>>1;
				p00=p01;
				p10=p11;
			}
			for(;x>=0;x-=4){
				p01=in_ptr[idx++];p11=in_ptr[idx2++];
				fx=p11-p00;
				fy=p10-p01;
				out_ptr[idx-2]=((fx<0?-fx:fx)+(fy<0?-fy:fy))>>1;
				p00=p01;p10=p11;

				p01=in_ptr[idx++];p11=in_ptr[idx2++];
				fx=p11-p00;
				fy=p10-p01;
				out_ptr[idx-2]=((fx<0?-fx:fx)+(fy<0?-fy:fy))>>1;
				p00=p01;p10=p11;
				p01=in_ptr[idx++];p11=in_ptr[idx2++];
				
				fx=p11-p00;
				fy=p10-p01;
				out_ptr[idx-2]=((fx<0?-fx:fx)+(fy<0?-fy:fy))>>1;
				p00=p01;p10=p11;

				p01=in_ptr[idx++];p11=in_ptr[idx2++];
				fx=p11-p00;
				fy=p10-p01;
				out_ptr[idx-2]=((fx<0?-fx:fx)+(fy<0?-fy:fy))>>1;
				p00=p01;p10=p11;

			}
			out_ptr[idx-1]=0;
		}
		for(int x=width-1;x>=0;x--){
			out_ptr[idx++]=0;
		}
		return;
	}
}
