/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2015 Daqri, LLC.
 *  Copyright 2006-2015 ARToolworks, Inc.
 *
 *  Author(s): Hirokazu Kato, Philip Lamb
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.core.surfacetracking.rasterdriver;

import jp.nyatla.nyartoolkit.core.surfacetracking.NyARTemplatePatchImage;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;


/**
 * INyARGrayscaleRasterのテンプレート検索ドライバ
 * @author nyatla
 *
 */
public interface INyARTemplateMatchingDriver
{
	/**
	 * N個の基準点から、最もテンプレートに一致した座標を返却する。
	 * 検索範囲は、{@link #setSearchArea}で与えたpx,pyを元に定義した矩形。
	 * i_pointsそれぞれについて検索する。
	 * @param i_template
	 * 検索するテンプレート
	 * @param ry
	 * @param i_points
	 * 検索する座標セット。(近い場所の場合に、同一条件の探索をキャンセルできる？)
	 * @param o_obs_point
	 * 観察座標系での一致点。returnが0の場合は無効。
	 * @return
	 * 一致率(値範囲調査中)
	 * 0の場合は一致せず。
	 * @throws NyARException
	 */
	public double ar2GetBestMatching(NyARTemplatePatchImage i_template, NyARIntPoint2d[] i_points,int i_number_of_point,
			NyARDoublePoint2d o_obs_point);
}