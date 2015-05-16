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
package jp.nyatla.nyartoolkit.markersystem;

import jp.nyatla.nyartoolkit.core.param.NyARParam;

/**
 * 管理システムの発行するイベントを処理するインタフェイスです。
 */
public interface INyARSingleCameraSystemObserver
{
	/**
	 * カメラパラメータが更新されたことを通知します。
	 * @param i_param
	 * @param i_near
	 * @param i_far
	 */
	public void onUpdateCameraParametor(NyARParam i_param,double i_near,double i_far);
}
