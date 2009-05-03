/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.pickup;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARSquare;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.utils.NyARPerspectiveParamGenerator_O1;


/**
 * 遠近法を使ったパースペクティブ補正をかけて、ラスタ上の四角形から
 * 任意解像度の矩形パターンを作成します。
 *
 */
public class NyARColorPatt_Perspective implements INyARColorPatt
{
	private int[] _patdata;
	private NyARBufferReader _buf_reader;
	private NyARRgbPixelReader_INT1D_X8R8G8B8_32 _pixelreader;
	private NyARIntSize _size;
	NyARPerspectiveParamGenerator_O1 _perspective_gen;
	private static final int LOCAL_LT=1;
	private NyARIntPoint2d _pickup_lt=new NyARIntPoint2d();	
	/**
	 * 例えば、64
	 * @param i_width
	 * 取得画像の解像度幅
	 * @param i_height
	 * 取得画像の解像度高さ
	 */
	public NyARColorPatt_Perspective(int i_width, int i_height)
	{
		//入力制限
		assert i_width>2 && i_height>2;
		
		this._size=new NyARIntSize(i_width,i_height);
		this._patdata = new int[i_height*i_width];
		this._buf_reader=new NyARBufferReader(this._patdata,NyARBufferReader.BUFFERFORMAT_INT1D_X8R8G8B8_32);
		this._pixelreader=new NyARRgbPixelReader_INT1D_X8R8G8B8_32(this._patdata,this._size);
		setEdgeSize(0,0);
		return;
	}
	/**
	 * 例えば、64
	 * @param i_width
	 * 取得画像の解像度幅
	 * @param i_height
	 * 取得画像の解像度高さ
	 * @param i_edge_percentage
	 * エッジ幅の割合(ARToolKit標準と同じなら、25)
	 */
	public NyARColorPatt_Perspective(int i_width, int i_height,int i_edge_percentage)
	{
		//入力制限
		assert i_width>2 && i_height>2;
		
		this._size=new NyARIntSize(i_width,i_height);
		this._patdata = new int[i_height*i_width];
		this._buf_reader=new NyARBufferReader(this._patdata,NyARBufferReader.BUFFERFORMAT_INT1D_X8R8G8B8_32);
		this._pixelreader=new NyARRgbPixelReader_INT1D_X8R8G8B8_32(this._patdata,this._size);
		setEdgeSizeByPercent(i_edge_percentage,i_edge_percentage);
		return;
	}	
	/**
	 * 矩形領域のエッジサイズを指定します。
	 * エッジの計算方法は以下の通りです。
	 * 1.マーカ全体を(i_x_edge*2+width)x(i_y_edge*2+height)の解像度でパラメタを計算します。
	 * 2.ピクセルの取得開始位置を(i_x_edge/2,i_y_edge/2)へ移動します。
	 * 3.開始位置から、width x height個のピクセルを取得します。
	 * 
	 * ARToolKit標準マーカの場合は、width/2,height/2を指定してください。
	 * @param i_x_edge
	 * @param i_y_edge
	 */
	public void setEdgeSize(int i_x_edge,int i_y_edge)
	{
		assert(i_x_edge>=0);
		assert(i_y_edge>=0);
		//Perspectiveパラメタ計算器を作成
		this._perspective_gen=new NyARPerspectiveParamGenerator_O1(LOCAL_LT,LOCAL_LT,i_x_edge*2+this._size.w,i_y_edge*2+this._size.h);
		//ピックアップ開始位置を計算
		this._pickup_lt.x=i_x_edge+LOCAL_LT;
		this._pickup_lt.y=i_y_edge+LOCAL_LT;
		return;
	}
	public void setEdgeSizeByPercent(int i_x_percent,int i_y_percent)
	{
		assert(i_x_percent>=0);
		assert(i_y_percent>=0);
		setEdgeSize(this._size.w*i_x_percent/50,this._size.h*i_y_percent/50);
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
	public final INyARBufferReader getBufferReader()
	{
		return this._buf_reader;
	}
	public final INyARRgbPixelReader getRgbPixelReader()
	{
		return this._pixelreader;
	}
	private final int[] __pickFromRaster_rgb_tmp = new int[3];
	/**
	 * 
	 * @param image
	 * @param i_marker
	 * @return 切り出しに失敗した
	 * @throws Exception
	 */
	public boolean pickFromRaster(INyARRgbRaster image, NyARSquare i_square)throws NyARException
	{
		//遠近法のパラメータを計算
		double[] cpara = new double[8];
		if (!this._perspective_gen.getParam(i_square.imvertex, cpara)) {
			return false;
		}
		
		int img_x = image.getWidth();
		int img_y = image.getHeight();

		int[] rgb_tmp = __pickFromRaster_rgb_tmp;

		//ピクセルリーダーを取得
		INyARRgbPixelReader reader=image.getRgbPixelReader();

		for(int iy=0;iy<this._size.h;iy++){
			for(int ix=0;ix<this._size.w;ix++){
				//1ピクセルを作成
				int cx=this._pickup_lt.x+ix;
				int cy=this._pickup_lt.y+iy;
				final double d=cpara[6]*cx+cpara[7]*cy+1.0;
				final int x=(int)((cpara[0]*cx+cpara[1]*cy+cpara[2])/d);
				final int y=(int)((cpara[3]*cx+cpara[4]*cy+cpara[5])/d);
				if (x >= 0 && x < img_x && y >= 0 && y < img_y) {
					reader.getPixel(x, y, rgb_tmp);
					this._patdata[iy*this._size.w+ix]=(((rgb_tmp[0])&0xff)<<16)|(((rgb_tmp[1])&0xff)<<8)|(((rgb_tmp[2])&0xff));
				}
			}
			//ピクセル問い合わせ
			//ピクセルセット
		}
		return true;
	}	
}