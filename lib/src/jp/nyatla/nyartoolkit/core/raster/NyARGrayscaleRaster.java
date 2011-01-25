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
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、グレースケース画像を格納するラスタクラスです。
 * 外部バッファ、内部バッファの両方に対応します。
 */
public class NyARGrayscaleRaster extends NyARRaster_BasicClass
{
	private IdoFilterImpl _impl;
	/** バッファオブジェクト*/
	protected Object _buf;
	/** バッファオブジェクトがアタッチされていればtrue*/
	protected boolean _is_attached_buffer;
	/**
	 * コンストラクタです。
	 * 内部参照のバッファ（{@link NyARBufferType#INT1D_GRAY_8}形式）を持つインスタンスを生成します。
	 * @param i_width
	 * ラスタのサイズ
	 * @param i_height
	 * ラスタのサイズ
	 * @throws NyARException
	 */
	public NyARGrayscaleRaster(int i_width, int i_height) throws NyARException
	{
		super(i_width, i_height, NyARBufferType.INT1D_GRAY_8);
		if (!initInstance(this._size, NyARBufferType.INT1D_GRAY_8, true))
		{
			throw new NyARException();
		}
	}
	/**
	 * コンストラクタです。
	 * 画像のサイズパラメータとバッファ参照方式を指定して、インスタンスを生成します。
	 * バッファの形式は、{@link NyARBufferType#INT1D_GRAY_8}です。
	 * @param i_width
	 * ラスタのサイズ
	 * @param i_height
	 * ラスタのサイズ
	 * @param i_is_alloc
	 * バッファを外部参照にするかのフラグ値。
	 * trueなら内部バッファ、falseなら外部バッファを使用します。
	 * falseの場合、初期のバッファはnullになります。インスタンスを生成したのちに、{@link #wrapBuffer}を使って割り当ててください。
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
	 * コンストラクタです。
	 * 画像のサイズパラメータとバッファ形式を指定して、インスタンスを生成します。
	 * @param i_width
	 * ラスタのサイズ
	 * @param i_height
	 * ラスタのサイズ
	 * @param i_raster_type
	 * ラスタのバッファ形式。
	 * {@link NyARBufferType}に定義された定数値を指定してください。指定できる値は、以下の通りです。
	 * <ul>
	 * <li>{@link NyARBufferType#INT1D_GRAY_8}
	 * <ul>
	 * @param i_is_alloc
	 * バッファを外部参照にするかのフラグ値。
	 * trueなら内部バッファ、falseなら外部バッファを使用します。
	 * falseの場合、初期のバッファはnullになります。インスタンスを生成したのちに、{@link #wrapBuffer}を使って割り当ててください。
	 * @throws NyARException
	 */
	public NyARGrayscaleRaster(int i_width, int i_height, int i_raster_type,boolean i_is_alloc) throws NyARException
	{
		super(i_width, i_height, i_raster_type);
		if (!initInstance(this._size, i_raster_type, i_is_alloc)) {
			throw new NyARException();
		}
	}

	/**
	 * このクラスの初期化シーケンスです。コンストラクタから呼び出します。
	 * @param i_size
	 * ラスタサイズ
	 * @param i_buf_type
	 * バッファ形式
	 * @param i_is_alloc
	 * バッファ参照方法値
	 * @return
	 * 初期化に成功するとtrueを返します。
	 */
	protected boolean initInstance(NyARIntSize i_size, int i_buf_type,boolean i_is_alloc)
	{
		switch (i_buf_type) {
		case NyARBufferType.INT1D_GRAY_8:
			this._impl=new IdoFilterImpl_INT1D_GRAY_8();
			this._buf = i_is_alloc ? new int[i_size.w * i_size.h] : null;
			break;
		default:
			return false;
		}
		this._is_attached_buffer = i_is_alloc;
		return true;
	}
	/**
	 * この関数は、ラスタのバッファへの参照値を返します。
	 * バッファの形式は、コンストラクタに指定した形式と同じです。
	 */	
	public Object getBuffer()
	{
		return this._buf;
	}

	/**
	 * この関数は、インスタンスがバッファを所有するかを返します。
	 * 内部参照バッファの場合は、常にtrueです。
	 * 外部参照バッファの場合は、バッファにアクセスする前に、このパラメタを確認してください。
	 */
	public boolean hasBuffer()
	{
		return this._buf != null;
	}
	/**
	 * この関数は、ラスタに外部参照バッファをセットします。
	 * 外部参照バッファを持つインスタンスでのみ使用できます。内部参照バッファを持つインスタンスでは使用できません。
	 */
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		assert (!this._is_attached_buffer);// バッファがアタッチされていたら機能しない。
		this._buf = i_ref_buf;
	}

	/**
	 * この関数は、指定した数値でラスタを埋めます。
	 * この関数は高速化していません。
	 * @param i_value
	 * 埋める数値を指定します。0から255の数値を指定して下さい。
	 */
	public void fill(int i_value)
	{
		this._impl.fill(this,i_value);
	}

	/**
	 * この関数は、指定ラスタに、このインスタンスの内容をコピーをします。
	 * 異解像度間コピーもできますが、入力範囲とラスタサイズの間には、一定の関係が成立する必要があります。
	 * @param i_left
	 * 入力ラスタの左上点を指定します。
	 * @param i_top
	 * 入力ラスタの左上点を指定します。
	 * @param i_skip
	 * skip値。1なら等倍、2なら1/2倍、3なら1/3倍の画像を出力します。
	 * @param o_output
	 * 出力先ラスタ。このラスタの解像度は、w=(i_input.w-i_left)/i_skip,h=(i_input.h-i_height)/i_skipを満たす必要があります。
	 * 出力先ラスタと入力ラスタの画素形式は、同じである必要があります。
	 */
	public void copyTo(int i_left,int i_top,int i_skip, NyARGrayscaleRaster o_output)
	{
		assert (this.getSize().isInnerSize(i_left + o_output.getWidth() * i_skip, i_top+ o_output.getHeight() * i_skip));		
		assert (this.isEqualBufferType(o_output.getBufferType()));
		this._impl.copyTo(this, i_left, i_top, i_skip, o_output);
		return;
	}
	////////////////////////////////////////////////////////////////////////////////
	//ここからラスタドライバ
	
	/** ラスタドライバの関数を定義します。*/
	protected interface IdoFilterImpl
	{
		public void fill(NyARGrayscaleRaster i_raster,int i_value);
		public void copyTo(NyARGrayscaleRaster i_input, int i_left,int i_top,int i_skip, NyARGrayscaleRaster o_output);
	}
	/**　{@link NyARBufferType#INT1D_GRAY_8}形式のラスタドライバです。*/
	protected final class IdoFilterImpl_INT1D_GRAY_8 implements IdoFilterImpl
	{
		public void fill(NyARGrayscaleRaster i_raster,int i_value)
		{
			assert (i_raster._buffer_type == NyARBufferType.INT1D_GRAY_8);
			int[] buf = (int[]) i_raster._buf;
			for (int i = i_raster._size.h * i_raster._size.w - 1; i >= 0; i--) {
				buf[i] = i_value;
			}			
		}

		public void copyTo(NyARGrayscaleRaster i_input, int i_left,int i_top,int i_skip, NyARGrayscaleRaster o_output)
		{
			assert (i_input.getSize().isInnerSize(i_left + o_output.getWidth() * i_skip, i_top+ o_output.getHeight() * i_skip));		
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
					output[pt_dst++] = input[pt_src];
					pt_src += i_skip;
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
	
}
