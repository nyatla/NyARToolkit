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

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattDeviationColorData;
import jp.nyatla.nyartoolkit.core.pixeldriver.INyARRgbPixelDriver;
import jp.nyatla.nyartoolkit.core.pixeldriver.NyARRgbPixelDriverFactory;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterdriver.*;
import jp.nyatla.nyartoolkit.core.types.*;


/**
 * このクラスは、入力サイズ制限の無いPerspectiveReaderです。
 *
 */
public class NyARColorPatt_Perspective implements INyARColorPatt
{
	private NyARIntPoint2d _edge=new NyARIntPoint2d();
	/** パターン格納用のバッファ*/
	protected int[] _patdata;
	/** サンプリング解像度*/
	protected int _sample_per_pixel;
	/** このラスタのサイズ*/	
	protected NyARIntSize _size;
	private INyARRgbPixelDriver _pixelreader;
	private static final int BUFFER_FORMAT=NyARBufferType.INT1D_X8R8G8B8_32;
	private void initInstance(int i_width, int i_height,int i_point_per_pix) throws NyARException
	{
		assert i_width>2 && i_height>2;
		this._sample_per_pixel=i_point_per_pix;	
		this._size=new NyARIntSize(i_width,i_height);
		this._patdata = new int[i_height*i_width];
		this._pixelreader=NyARRgbPixelDriverFactory.createDriver(this);
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
	 * @throws NyARException 
	 */
	public NyARColorPatt_Perspective(int i_width, int i_height,int i_point_per_pix) throws NyARException
	{
		this.initInstance(i_width,i_height,i_point_per_pix);
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
	 * @throws NyARException 
	 */
	public NyARColorPatt_Perspective(int i_width, int i_height,int i_point_per_pix,int i_edge_percentage) throws NyARException
	{
		this.initInstance(i_width,i_height,i_point_per_pix);
		this._edge.setValue(i_edge_percentage, i_edge_percentage);
		return;
	}
	/**
	 * 矩形領域のエッジ（枠）サイズを、割合で指定します。
	 * @param i_x_percent
	 * 左右のエッジの割合です。0から50の間の数で指定します。
	 * @param i_y_percent
	 * 上下のエッジの割合です。0から50の間の数で指定します。
	 * @param i_sample_per_pixel
	 * 1ピクセルあたりの縦横サンプリング数。2なら2x2=4ポイントをサンプリングする。
	 */
	public void setEdgeSizeByPercent(int i_x_percent,int i_y_percent,int i_sample_per_pixel)
	{
		assert(i_x_percent>=0);
		assert(i_y_percent>=0);
		this._edge.setValue(i_x_percent, i_y_percent);
		this._sample_per_pixel=i_sample_per_pixel;
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
	public final INyARRgbPixelDriver getRgbPixelDriver()
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
	private INyARRgbRaster _last_input_raster=null;
	private INyARPerspectiveCopy _raster_driver;
	/**
	 * この関数は、ラスタのi_vertexsで定義される四角形からパターンを取得して、インスタンスに格納します。
	 */
	public boolean pickFromRaster(INyARRgbRaster image,NyARIntPoint2d[] i_vertexs)throws NyARException
	{
		if(this._last_input_raster!=image){
			this._raster_driver=(INyARPerspectiveCopy) image.createInterface(INyARPerspectiveCopy.class);
			this._last_input_raster=image;
		}
		//遠近法のパラメータを計算
		return this._raster_driver.copyPatt(i_vertexs,this._edge.x,this._edge.y,this._sample_per_pixel, this);
	}

	@Override
	public Object createInterface(Class<?> iIid) throws NyARException
	{
		if(iIid==INyARPerspectiveCopy.class){
			return NyARPerspectiveCopyFactory.createDriver(this);
		}
		if(iIid==NyARMatchPattDeviationColorData.IRasterDriver.class){
			return NyARMatchPattDeviationColorData.RasterDriverFactory.createDriver(this);
		}		
		throw new NyARException();
	}
}