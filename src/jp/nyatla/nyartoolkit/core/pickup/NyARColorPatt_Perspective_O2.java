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
 * 遠近法を使ったパースペクティブ補正をかけて、ラスタ上の四角形から
 * 任意解像度の矩形パターンを作成します。
 *
 */
public class NyARColorPatt_Perspective_O2 implements INyARColorPatt
{
	private NyARIntPoint2d _edge=new NyARIntPoint2d();
	protected int[] _patdata;
	protected int _resolution;
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
	 * コンストラクタです。エッジサイズ0,InputRaster=ANYでインスタンスを作成します。
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
	 * コンストラクタです。エッジサイズ,InputRasterTypeを指定してインスタンスを作成します。
	 * @param i_width
	 * 取得画像の解像度幅
	 * @param i_height
	 * 取得画像の解像度高さ
	 * @param i_point_per_pix
	 * 1ピクセルあたりの解像度
	 * @param i_edge_percentage
	 * エッジ幅の割合(ARToolKit標準と同じなら、25)
	 * @param i_input_raster_type
	 * 入力ラスタの種類
	 */
	public NyARColorPatt_Perspective_O2(int i_width, int i_height,int i_point_per_pix,int i_edge_percentage,int i_input_raster_type)
	{
		initializeInstance(i_width,i_height,i_point_per_pix,i_input_raster_type);
		this._edge.setValue(i_edge_percentage, i_edge_percentage);
		return;
	}	
	public void setEdgeSizeByPercent(int i_x_percent,int i_y_percent,int i_resolution)
	{
		assert(i_x_percent>=0);
		assert(i_y_percent>=0);
		this._edge.setValue(i_x_percent, i_y_percent);
		this._resolution=i_resolution;
		return;
	}

	
	public final int getWidth()
	{
		return this._size.w;
	}
	public final int getHeight()
	{
		return this._size.h;
	}
	public final NyARIntSize getSize()
	{
		return 	this._size;
	}
	public final INyARRgbPixelReader getRgbPixelReader()
	{
		return this._pixelreader;
	}
	public Object getBuffer()
	{
		return this._patdata;
	}
	public boolean hasBuffer()
	{
		return this._patdata!=null;
	}
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		NyARException.notImplement();
	}
	final public int getBufferType()
	{
		return BUFFER_FORMAT;
	}
	final public boolean isEqualBufferType(int i_type_value)
	{
		return BUFFER_FORMAT==i_type_value;
	}
	/**
	 * @see INyARColorPatt#pickFromRaster
	 */
	public boolean pickFromRaster(INyARRgbRaster image,NyARIntPoint2d[] i_vertexs)throws NyARException
	{
		//遠近法のパラメータを計算
		return this._perspective_reader.read4Point(image, i_vertexs,this._edge.x,this._edge.y,this._resolution, this);
	}

}