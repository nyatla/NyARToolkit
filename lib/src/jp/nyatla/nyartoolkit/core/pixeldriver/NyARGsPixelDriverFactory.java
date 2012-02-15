/* 
 * PROJECT: NyARToolkit(Extension)
 * -------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
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
package jp.nyatla.nyartoolkit.core.pixeldriver;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * このクラスは、INyARGsPixelDriverインタフェイスを持つオブジェクトを構築する手段を提供します。
 */
public class NyARGsPixelDriverFactory
{
	/**
	 * ラスタから画素ドライバを構築します。構築したラスタドライバには、i_ref_rasterをセットします。
	 * @param i_ref_raster
	 * @return
	 * @throws NyARException
	 */
	public static INyARGsPixelDriver createDriver(INyARGrayscaleRaster i_ref_raster) throws NyARException
	{
		INyARGsPixelDriver ret;
		switch(i_ref_raster.getBufferType()){
		case NyARBufferType.INT1D_GRAY_8:
		case NyARBufferType.INT1D_BIN_8:
			ret=new NyARGsPixelDriver_INT1D_GRAY_8();
			break;
		default:
			//RGBRasterインタフェイスがある場合
			if(i_ref_raster instanceof INyARRgbRaster){
				ret=new NyARGsPixelDriver_RGBX();
				break;
			}
			throw new NyARException();
		}
		ret.switchRaster(i_ref_raster);
		return ret;
	}
	public static INyARGsPixelDriver createDriver(INyARRgbRaster i_ref_raster) throws NyARException
	{
		//RGBRasterインタフェイスがある場合
		return new NyARGsPixelDriver_RGBX();
	}	
}
//
//	ピクセルドライバの定義
//



/**
 * INT1D_GRAY_8のドライバです。
 */
class NyARGsPixelDriver_INT1D_GRAY_8 implements INyARGsPixelDriver
{
	protected int[] _ref_buf;
	private NyARIntSize _ref_size;
	public NyARIntSize getSize()
	{
		return this._ref_size;
	}
	public void getPixelSet(int[] i_x,int[] i_y,int i_n,int[] o_buf,int i_st_buf)
	{
		int bp;
		final int w = this._ref_size.w;
		final int[] b = this._ref_buf;
		for (int i = i_n - 1; i >= 0; i--) {
			bp = (i_x[i] + i_y[i] * w);
			o_buf[i_st_buf+i] = (b[bp]);
		}
		return;	
	}
	public int getPixel(int i_x,int i_y)
	{
		final int[] ref_buf = this._ref_buf;
		return ref_buf[(i_x + i_y * this._ref_size.w)];
	}
	public void setPixel(int i_x, int i_y, int i_gs) throws NyARException
	{
		this._ref_buf[(i_x + i_y * this._ref_size.w)]=i_gs;
	}
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intgs) throws NyARException
	{
		int w=this._ref_size.w;
		int r[]=this._ref_buf;
		for (int i = i_num - 1; i >= 0; i--){
			r[(i_x[i] + i_y[i] * w)]=i_intgs[i];
		}
	}	
	public void switchRaster(INyARRaster i_ref_raster) throws NyARException
	{
		this._ref_buf=(int[])i_ref_raster.getBuffer();
		this._ref_size=i_ref_raster.getSize();
	}
	public boolean isCompatibleRaster(INyARRaster i_raster)
	{
		return i_raster.isEqualBufferType(NyARBufferType.INT1D_GRAY_8);
	}	
}
/**
 * 低速ドライバです。速度が必要な場合は、画素ドライバを書くこと。
 */
class NyARGsPixelDriver_RGBX implements INyARGsPixelDriver
{
	private INyARRgbPixelDriver _rgbd;
	private int[] _tmp=new int[3];
	public NyARIntSize getSize()
	{
		return this._rgbd.getSize();
	}
	public void getPixelSet(int[] i_x,int[] i_y,int i_n,int[] o_buf,int i_st_buf) throws NyARException
	{
		INyARRgbPixelDriver r=this._rgbd;
		int[] tmp=this._tmp;
		for (int i = i_n - 1; i >= 0; i--){
			r.getPixel(i_x[i], i_y[i],tmp);
			o_buf[i_st_buf+i] =(tmp[0]+tmp[1]+tmp[2])/3;
		}
		return;
	}
	public int getPixel(int i_x,int i_y) throws NyARException
	{
		int[] tmp=this._tmp;
		this._rgbd.getPixel(i_x,i_y,tmp);
		return (tmp[0]+tmp[1]+tmp[2])/3;
	}
	public void setPixel(int i_x, int i_y, int i_gs) throws NyARException
	{
		this._rgbd.setPixel(i_x, i_y, i_gs,i_gs,i_gs);
	}
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intgs) throws NyARException
	{
		INyARRgbPixelDriver r=this._rgbd;
		for (int i = i_num - 1; i >= 0; i--){
			int gs=i_intgs[i];
			r.setPixel(i_x[i], i_y[i],gs,gs,gs);
		}
	}
	public void switchRaster(INyARRaster i_ref_raster) throws NyARException
	{
		if(!(i_ref_raster instanceof INyARRgbRaster)){
			throw new NyARException();
		}
		this._rgbd=((INyARRgbRaster)i_ref_raster).getRgbPixelDriver();
	}
	public boolean isCompatibleRaster(INyARRaster i_raster)
	{
		return (i_raster instanceof INyARRgbRaster);
	}	
}
