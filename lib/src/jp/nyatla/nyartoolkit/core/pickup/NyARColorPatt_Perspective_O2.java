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
package jp.nyatla.nyartoolkit.core.pickup;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、高速化した{@link NyARColorPatt_Perspective}です。
 * 入力ラスタを制限することで、高速なパターン取得ができます。
 */
public class NyARColorPatt_Perspective_O2 implements INyARColorPatt
{
	private NyARIntPoint2d _edge=new NyARIntPoint2d();
	/** パターン格納用のバッファ*/
	protected int[] _patdata;
	/** サンプリング解像度*/
	protected int _resolution;
	/** このラスタのサイズ*/	
	protected NyARIntSize _size;
	private NyARRgbPixelReader_INT1D_X8R8G8B8_32 _pixelreader;
	private static final int BUFFER_FORMAT=NyARBufferType.INT1D_X8R8G8B8_32;
	private NyARPerspectiveRasterReader _perspective_reader;
	private void initializeInstance(int i_width, int i_height,int i_point_per_pix,int i_input_raster_type)
	{
		assert i_width>2 && i_height>2;
		this._resolution=i_point_per_pix;	
		this._size=new NyARIntSize(i_width,i_height);
		this._patdata = new int[i_height*i_width];
		this._pixelreader=new NyARRgbPixelReader_INT1D_X8R8G8B8_32(this._patdata,this._size);
		this._perspective_reader=new NyARPerspectiveRasterReader(i_input_raster_type);
		return;		
	}
	/**
	 * コンストラクタです。
	 * エッジサイズ0,入力ラスタタイプの制限無しでインスタンスを作成します。
	 *　高速化が必要な時は、入力ラスタタイプを制限するコンストラクタを使ってください。
	 * @param i_width
	 * 取得画像の解像度幅
	 * @param i_height
	 * 取得画像の解像度高さ
	 * @param i_point_per_pix
	 * 1ピクセルあたりの縦横サンプリング数。2なら2x2=4ポイントをサンプリングする。
	 */
	public NyARColorPatt_Perspective_O2(int i_width, int i_height,int i_point_per_pix)
	{
		initializeInstance(i_width,i_height,i_point_per_pix,NyARBufferType.NULL_ALLZERO);
		this._edge.setValue(0,0);
		return;
	}
	/**
	 * コンストラクタです。
	 * エッジサイズ,入力ラスタタイプの制限を指定してインスタンスを作成します。
	 * @param i_width
	 * 取得画像の解像度幅
	 * @param i_height
	 * 取得画像の解像度高さ
	 * @param i_point_per_pix
	 * 1ピクセルあたりの解像度
	 * @param i_edge_percentage
	 * エッジ幅の割合(ARToolKit標準と同じなら、25)
	 * @param i_input_raster_type
	 * 入力ラスタのバッファタイプ。高速化が期待できるバッファタイプについては、{@link NyARPerspectiveRasterReader#NyARPerspectiveRasterReader}
	 * を参照してください。
	 */
	public NyARColorPatt_Perspective_O2(int i_width, int i_height,int i_point_per_pix,int i_edge_percentage,int i_input_raster_type)
	{
		initializeInstance(i_width,i_height,i_point_per_pix,i_input_raster_type);
		this._edge.setValue(i_edge_percentage, i_edge_percentage);
		return;
	}
	/**
	 * 矩形領域のエッジ（枠）サイズを、割合で指定します。
	 * @param i_x_percent
	 * 左右のエッジの割合です。0から50の間の数で指定します。
	 * @param i_y_percent
	 * 上下のエッジの割合です。0から50の間の数で指定します。
	 * @param i_resolution
	 * 1ピクセルあたりの縦横サンプリング数。2なら2x2=4ポイントをサンプリングする。
	 */
	public void setEdgeSizeByPercent(int i_x_percent,int i_y_percent,int i_resolution)
	{
		assert(i_x_percent>=0);
		assert(i_y_percent>=0);
		this._edge.setValue(i_x_percent, i_y_percent);
		this._resolution=i_resolution;
		return;
	}
	/**
	 * この関数はラスタの幅を返します。
	 */
	public final int getWidth()
	{
		return this._size.w;
	}
	/**
	 * この関数はラスタの高さを返します。
	 */
	public final int getHeight()
	{
		return this._size.h;
	}
	/**
	 * この関数はラスタのサイズの参照値を返します。
	 */
	public final NyARIntSize getSize()
	{
		return 	this._size;
	}
	/**
	 * この関数は、ラスタの画素読み取りオブジェクトの参照値を返します。
	 */	
	public final INyARRgbPixelReader getRgbPixelReader()
	{
		return this._pixelreader;
	}
	/**
	 * この関数は、ラスタ画像のバッファを返します。
	 * バッファ形式は、{@link NyARBufferType#INT1D_X8R8G8B8_32}(int[])です。
	 */	
	public Object getBuffer()
	{
		return this._patdata;
	}
	/**
	 * この関数は、インスタンスがバッファを所有しているかを返します。基本的にtrueです。
	 */	
	public boolean hasBuffer()
	{
		return this._patdata!=null;
	}
	/**
	 * この関数は使用不可能です。
	 */
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		NyARException.notImplement();
	}
	/**
	 * この関数は、バッファタイプの定数を返します。
	 */
	public final int getBufferType()
	{
		return BUFFER_FORMAT;
	}
	/**
	 * この関数は、インスタンスのバッファタイプが引数のものと一致しているか判定します。
	 */	
	public final boolean isEqualBufferType(int i_type_value)
	{
		return BUFFER_FORMAT==i_type_value;
	}
	/**
	 * この関数は、ラスタのi_vertexsで定義される四角形からパターンを取得して、インスタンスに格納します。
	 */
	public boolean pickFromRaster(INyARRgbRaster image,NyARIntPoint2d[] i_vertexs)throws NyARException
	{
		//遠近法のパラメータを計算
		return this._perspective_reader.read4Point(image, i_vertexs,this._edge.x,this._edge.y,this._resolution, this);
	}

}