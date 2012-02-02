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
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.rasterdriver.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.NyARRasterFilter_GaussianSmooth.IFilter;


/**
 * このクラスは、ネガポジ反転フィルタです。
 * <p>対応している画素形式は以下の通りです。
 * <li>{@link NyARBufferType#INT1D_GRAY_8}
 * </p>
 */
public class NyARRasterFilter_Reverse
{
	private IFilter _do_filter_impl;
	/**
	 * コンストラクタです。
	 * @throws NyARException
	 */	
	public NyARRasterFilter_Reverse(int i_raster_type) throws NyARException
	{
		this._do_filter_impl=new Filter_Reverse_Blank();
	}
	protected IFilter createFilter(INyARRaster i_in,INyARRaster i_out) throws NyARException
	{
		if(i_in.getBufferType()==NyARBufferType.INT1D_GRAY_8){
			switch(i_out.getBufferType()){
			case NyARBufferType.INT1D_GRAY_8:
				return new Filter_Reverse_GRAY_8();
			default:
				break;
			}
		}
		throw new NyARException();
	}
	/**
	 * 入力ラスタを反転して、画素を出力ラスタへ書込みます。
	 * 画素形式は、コンストラクタに指定した形式に合せてください。
	 */	
	public void doFilter(INyARRaster i_input, INyARRaster i_output) throws NyARException
	{
		if(!this._do_filter_impl.isSupport(i_input,i_output)){
			this._do_filter_impl=this.createFilter(i_input, i_output);
		}		
		this._do_filter_impl.doFilter(i_input,i_output,i_input.getSize());
	}
	public interface IFilter
	{
		public boolean isSupport(INyARRaster i_in,INyARRaster i_out);
		public void doFilter(INyARRaster i_input,INyARRaster i_output,NyARIntSize i_size) throws NyARException;
	}	
}

//
//フィルタ定義
//
class Filter_Reverse_Blank implements NyARRasterFilter_Reverse.IFilter
{
	public final boolean isSupport(INyARRaster i_in,INyARRaster i_out)
	{
		return false;
	}
	public final void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException
	{
		throw new NyARException();
	}

}
class Filter_Reverse_GRAY_8 implements NyARRasterFilter_Reverse.IFilter
{
	public final boolean isSupport(INyARRaster i_in,INyARRaster i_out)
	{
		return i_in.isEqualBufferType(NyARBufferType.INT1D_GRAY_8) && i_out.isEqualBufferType(NyARBufferType.INT1D_GRAY_8);
	}
	public final void doFilter(INyARRaster i_input, INyARRaster i_output,NyARIntSize i_size) throws NyARException
	{
		assert (i_input.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		assert (i_output.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		int[] in_ptr =(int[])i_input.getBuffer();
		int[] out_ptr=(int[])i_output.getBuffer();

		
		int number_of_pixel=i_size.h*i_size.w;
		for(int i=0;i<number_of_pixel;i++){
			out_ptr[i]=255-in_ptr[i];
		}
		return;
	}
}
