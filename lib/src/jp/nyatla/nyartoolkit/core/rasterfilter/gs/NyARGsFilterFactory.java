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
package jp.nyatla.nyartoolkit.core.rasterfilter.gs;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;

public class NyARGsFilterFactory
{
	public static INyARGsCustomToneTableFilter createCustomToneTableFilter(INyARGrayscaleRaster i_raster) throws NyARException
	{
		return new NyARGsCustomToneTableFilter_Any(i_raster);
	}
	public static INyARGsEqualizeHistFilter createEqualizeHistFilter(INyARGrayscaleRaster i_raster) throws NyARException
	{
		return new NyARGsEqualizeHistFilter_Any(i_raster);
	}
	public static INyARGsGaussianSmoothFilter createGaussianSmoothFilter(INyARGrayscaleRaster i_raster) throws NyARException
	{
		return new NyARGsGaussianSmoothFilter_GS8(i_raster);
	}
	public static INyARGsReverseFilter createReverseFilter(INyARGrayscaleRaster i_raster) throws NyARException
	{
		return new NyARGsReverseFilter_Any(i_raster);
	}
	public static INyARGsRobertsFilter createRobertsFilter(INyARGrayscaleRaster i_raster) throws NyARException
	{
		return new NyARGsRobertsFilter_GS8(i_raster);
	}
	public static INyARGsToneTableFilter createToneTableFilter(INyARGrayscaleRaster i_raster) throws NyARException
	{
		return new NyARGsToneTableFilter(i_raster);
	}
}

