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
package jp.nyatla.nyartoolkit.core.raster;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * 1枚のグレースケール画像を定義するクラスです。画像データは内部保持/外部保持が選択可能です。
 */
public class NyARGrayscaleRaster extends NyARRaster_BasicClass
{
	private NyARVectorReader_INT1D_GRAY_8 _vr;
	protected Object _buf;
	/**
	 * バッファオブジェクトがアタッチされていればtrue
	 */
	protected boolean _is_attached_buffer;

	public NyARGrayscaleRaster(int i_width, int i_height) throws NyARException
	{
		super(i_width, i_height, NyARBufferType.INT1D_GRAY_8);
		if (!initInstance(this._size, NyARBufferType.INT1D_GRAY_8, true))
		{
			throw new NyARException();
		}
	}
	/**
	 * 
	 * @param i_width
	 * @param i_height
	 * @param i_is_alloc
	 * 画像バッファを内部保持にするかのフラグ値。trueなら、インスタンスがバッファを確保します。falseなら、
	 * 画像バッファは外部参照になり、wrapBuffer関数を使用できます。
	 * @throws NyARException
	 */
	public NyARGrayscaleRaster(int i_width, int i_height, boolean i_is_alloc)
			throws NyARException
	{
		super(i_width, i_height, NyARBufferType.INT1D_GRAY_8);
		if (!initInstance(this._size, NyARBufferType.INT1D_GRAY_8, i_is_alloc))
		{
			throw new NyARException();
		}
	}

	/**
	 * @param i_width
	 * @param i_height
	 * @param i_raster_type
	 *            NyARBufferTypeに定義された定数値を指定してください。
	 * @param i_is_alloc
	 * @throws NyARException
	 */
	public NyARGrayscaleRaster(int i_width, int i_height, int i_raster_type,
			boolean i_is_alloc) throws NyARException
	{
		super(i_width, i_height, i_raster_type);
		if (!initInstance(this._size, i_raster_type, i_is_alloc)) {
			throw new NyARException();
		}
	}

	/**
	 * このクラスの初期化シーケンスです。コンストラクタから呼び出します。
	 * @param i_size
	 * @param i_buf_type
	 * @param i_is_alloc
	 * @return
	 */
	protected boolean initInstance(NyARIntSize i_size, int i_buf_type,boolean i_is_alloc)
	{
		switch (i_buf_type) {
		case NyARBufferType.INT1D_GRAY_8:
			this._buf = i_is_alloc ? new int[i_size.w * i_size.h] : null;
			break;
		default:
			return false;
		}
		this._is_attached_buffer = i_is_alloc;
		this._vr=new NyARVectorReader_INT1D_GRAY_8(this);
		return true;
	}
	public NyARVectorReader_INT1D_GRAY_8 getVectorReader()
	{
		return this._vr;
	}
	public Object getBuffer()
	{
		return this._buf;
	}

	/**
	 * インスタンスがバッファを所有するかを返します。 コンストラクタでi_is_allocをfalseにしてラスタを作成した場合、
	 * バッファにアクセスするまえに、バッファの有無をこの関数でチェックしてください。
	 * @return
	 */
	public boolean hasBuffer()
	{
		return this._buf != null;
	}
	/**
	 *　追加機能-無し。
	 */
	public void wrapBuffer(Object i_ref_buf)
	{
		assert (!this._is_attached_buffer);// バッファがアタッチされていたら機能しない。
		this._buf = i_ref_buf;
	}

	/**
	 * 指定した数値でラスタを埋めます。
	 * この関数は高速化していません。
	 * @param i_value
	 */
	public void fill(int i_value) {
		assert (this._buffer_type == NyARBufferType.INT1D_GRAY_8);
		int[] buf = (int[]) this._buf;
		for (int i = this._size.h * this._size.w - 1; i >= 0; i--) {
			buf[i] = i_value;
		}
	}

	/**
	 * ラスタの異解像度間コピーをします。(このAPIは暫定実装です。)
	 * @param i_input
	 * 入力画像
	 * @param i_top
	 * @param i_left
	 * @param i_skip
	 * @param o_output
	 * 出力先ラスタ。このラスタの解像度は、w=(i_input.w-i_left)/i_skip,h=(i_input.h-i_height)/i_skipを満たす必要があります。
	 */
	public static void copy(NyARGrayscaleRaster i_input, int i_left,int i_top,int i_skip, NyARGrayscaleRaster o_output)
	{
		assert (i_input.getSize().isInnerSize(i_left + o_output.getWidth() * i_skip, i_top
				+ o_output.getHeight() * i_skip));		
		final int[] input = (int[]) i_input.getBuffer();
		final int[] output = (int[]) o_output.getBuffer();
		int pt_src, pt_dst;
		NyARIntSize dest_size = o_output.getSize();
		NyARIntSize src_size = i_input.getSize();
		int skip_src_y = (src_size.w - dest_size.w * i_skip) + src_size.w * (i_skip - 1);
		final int pix_count = dest_size.w;
		final int pix_mod_part = pix_count - (pix_count % 8);
		// 左上から1行づつ走査していく
		pt_dst = 0;
		pt_src = (i_top * src_size.w + i_left);
		for (int y = dest_size.h - 1; y >= 0; y -= 1) {
			int x;
			for (x = pix_count - 1; x >= pix_mod_part; x--) {
				try{
				output[pt_dst++] = input[pt_src];
				pt_src += i_skip;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			for (; x >= 0; x -= 8) {
				output[pt_dst++] = input[pt_src];
				pt_src += i_skip;
				output[pt_dst++] = input[pt_src];
				pt_src += i_skip;
				output[pt_dst++] = input[pt_src];
				pt_src += i_skip;
				output[pt_dst++] = input[pt_src];
				pt_src += i_skip;
				output[pt_dst++] = input[pt_src];
				pt_src += i_skip;
				output[pt_dst++] = input[pt_src];
				pt_src += i_skip;
				output[pt_dst++] = input[pt_src];
				pt_src += i_skip;
				output[pt_dst++] = input[pt_src];
				pt_src += i_skip;
			}
			// スキップ
			pt_src += skip_src_y;
		}
		return;

	}
}
