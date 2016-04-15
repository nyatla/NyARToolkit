/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.raster.bin;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.bin.format.NyARBinRaster_INT1D_BIN_8;
import jp.nyatla.nyartoolkit.core.rasterdriver.labeling.rle.NyARLabeling_Rle;
import jp.nyatla.nyartoolkit.core.rasterdriver.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;


/**
 * このクラスは、0/1 の二値GrayscaleRasterです。
 */
public abstract class NyARBinRaster implements INyARBinRaster
{
	protected final NyARIntSize _size;	
	/** バッファオブジェクトがアタッチされていればtrue*/
	protected final boolean _is_attached_buffer;	
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
	 * @throws NyARRuntimeException
	 */	
	public static INyARBinRaster createInstance(int i_width, int i_height, int i_raster_type,boolean i_is_alloc)
	{
		switch(i_raster_type){
		case NyARBufferType.INT1D_GRAY_8:
			return new NyARBinRaster_INT1D_BIN_8(i_width,i_height,i_is_alloc);
		}
		throw new NyARRuntimeException();
	}
	/**
	 * 内部参照のバッファ（{@link NyARBufferType#INT1D_GRAY_8}形式）を持つインスタンスを生成します。
	 * @param i_width
	 * ラスタのサイズ
	 * @param i_height
	 * ラスタのサイズ
	 * @throws NyARRuntimeException
	 */
	public static INyARBinRaster createInstance(int i_width, int i_height)
	{
		return NyARBinRaster.createInstance(i_width,i_height,true);
	}
	/**
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
	 * @throws NyARRuntimeException
	 */
	public static INyARBinRaster createInstance(int i_width, int i_height, boolean i_is_alloc)
	{		
		return NyARBinRaster.createInstance(i_width,i_height,NyARBufferType.INT1D_GRAY_8, i_is_alloc);
	}	
	

	/**
	 * この関数は、ラスタの幅を返します。
	 */
	@Override
	final public int getWidth()
	{
		return this._size.w;
	}
	/**
	 * この関数は、ラスタの高さを返します。
	 */
	@Override
	final public int getHeight()
	{
		return this._size.h;
	}
	/**
	 * この関数は、ラスタのサイズを格納したオブジェクトを返します。
	 */
	@Override
	final public NyARIntSize getSize()
	{
		return this._size;
	}
	/**
	 * この関数は、ラスタのバッファへの参照値を返します。
	 * バッファの形式は、コンストラクタに指定した形式と同じです。
	 */	

	/**
	 * この関数は、ラスタの幅を返します。
	 */
	@Override
	final public boolean isEqualBufferType(int i_type_value)
	{
		return i_type_value==this.getBufferType();
	}	
	/**
	 * この関数は、インスタンスがバッファを所有するかを返します。
	 * 内部参照バッファの場合は、常にtrueです。
	 * 外部参照バッファの場合は、バッファにアクセスする前に、このパラメタを確認してください。
	 */
	final public boolean hasBuffer()
	{
		return this.getBuffer() != null;
	}


	/**
	 * 継承クラスから使うコンストラクタです。
	 * 引数に有効な値を設定してください。
	 * @param i_size
	 * @param i_is_alloc
	 */
	protected NyARBinRaster(int i_width,int i_height,boolean i_is_alloc)
	{
		this._size=new NyARIntSize(i_width,i_height);
		this._is_attached_buffer=i_is_alloc;
	}	

	@Override
	public Object createInterface(Class<?> i_iid)
	{
		if(i_iid==NyARLabeling_Rle.IRasterDriver.class){
			return NyARLabeling_Rle.RasterDriverFactory.createDriver(this);
		}
		if(i_iid==NyARContourPickup.IRasterDriver.class){
			return NyARContourPickup.ImageDriverFactory.createDriver(this);
		}
		throw new NyARRuntimeException();
	}
}
