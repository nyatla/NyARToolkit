/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
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
package jp.nyatla.nyartoolkit.core.raster.rgb;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARRaster_BasicClass;
import jp.nyatla.nyartoolkit.core.rasterreader.INyARRgbPixelReader;

/**
 * NyARRasterインタフェイスの基本関数/メンバを実装したクラス
 * 
 *
 */
public abstract class NyARRgbRaster_BasicClass extends NyARRaster_BasicClass implements INyARRgbRaster 
{
    public INyARRgbPixelReader getRgbPixcelReader() throws NyARException
    {
	NyARException.notImplement();
	return null;
    }
}
