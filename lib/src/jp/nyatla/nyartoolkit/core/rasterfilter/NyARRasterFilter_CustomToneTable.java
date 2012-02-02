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
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * このクラスは、色調フィルタのベースクラスです。
 * 継承クラスで{@link #table}に変換ルールを書き込む処理を実装します。 
 * <p>対応している画素形式は以下の通りです。
 * <li>{@link NyARBufferType#INT1D_GRAY_8}
 * </p>
 */
public class NyARRasterFilter_CustomToneTable
{
	private IFilter _dofilterimpl;
	/**
	 * コンストラクタです。
	 * 入力/出力ラスタの画素形式を指定して、インスタンスを生成します。
	 * @param i_raster_type
	 * ラスタ形式。
	 * @throws NyARException
	 */
	protected NyARRasterFilter_CustomToneTable() throws NyARException
	{
		this._dofilterimpl=new Filter_Blank();
	}
	protected IFilter createFilter(INyARRaster i_in,INyARRaster i_out) throws NyARException
	{
		if(i_in.getBufferType()==NyARBufferType.INT1D_GRAY_8){
			switch(i_out.getBufferType()){
			case NyARBufferType.INT1D_GRAY_8:
				return new Filter_INT1D_GRAY_8_to_INT1D_GRAY_8();
			default:
				break;
			}
		}
		throw new NyARException();
	}
	/**
	 * 変換テーブルに従って、画素値を交換した画像を出力します。
	 * 画素形式は、コンストラクタに指定した形式に合せてください。
	 * @param i_input
	 * 入力画像
	 * @param i_tone_table
	 * トーンテーブル
	 * @param i_output
	 * 出力画像
	 */
	public void doFilter(INyARRaster i_input, int[] i_tone_table,INyARRaster i_output) throws NyARException
	{
		assert (i_input.getSize().isEqualSize(i_output.getSize()) == true);
		if(!this._dofilterimpl.isSupport(i_input,i_output)){
			this._dofilterimpl=this.createFilter(i_input, i_output);
		}
		this._dofilterimpl.doFilter(i_input,i_tone_table,i_output,i_input.getSize());
	}	
	public interface IFilter
	{
		public boolean isSupport(INyARRaster i_in,INyARRaster i_out);
		public void doFilter(INyARRaster i_input, int[] i_tone_table,INyARRaster i_output,NyARIntSize i_size) throws NyARException;
	}
}

//
//フィルタ定義
//

class Filter_Blank implements NyARRasterFilter_CustomToneTable.IFilter
{
	public boolean isSupport(INyARRaster i_in,INyARRaster i_out)
	{
		return false;
	}
	public void doFilter(INyARRaster i_input, int[] i_tone_table,INyARRaster i_output,NyARIntSize i_size) throws NyARException
	{
		throw new NyARException();
	}
}
class Filter_INT1D_GRAY_8_to_INT1D_GRAY_8 implements NyARRasterFilter_CustomToneTable.IFilter
{
	public boolean isSupport(INyARRaster i_in,INyARRaster i_out)
	{
		return i_in.isEqualBufferType(NyARBufferType.INT1D_GRAY_8) && i_out.isEqualBufferType(NyARBufferType.INT1D_GRAY_8);
	}
	public void doFilter(INyARRaster i_input, int[] i_tone_table,INyARRaster i_output,NyARIntSize i_size) throws NyARException
	{
		assert(i_input.isEqualBufferType(NyARBufferType.INT1D_GRAY_8));
		
		int[] out_buf = (int[]) i_output.getBuffer();
		int[] in_buf = (int[]) i_input.getBuffer();
		for(int i=i_size.h*i_size.w-1;i>=0;i--)
		{
			out_buf[i]=i_tone_table[in_buf[i]];
		}
		return;
	}
}
