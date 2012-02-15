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
package jp.nyatla.nyartoolkit.markersystem.utils;

import jp.nyatla.nyartoolkit.core.transmat.NyARRectOffset;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;

/**
 * マーカ情報を格納するためのクラスです。
 */
public class TMarkerData
{
	/** 最後に認識したタイムスタンプ。*/
	public long time_stamp;
	/** ライフ値
	 * マーカ検出時にリセットされ、1フレームごとに1づつインクリメントされる値です。
	 */
	public long life;
	/** MK情報。マーカのオフセット位置。*/
	public final NyARRectOffset marker_offset=new NyARRectOffset();			
	/** 検出した矩形の格納変数。理想形二次元座標を格納します。*/
	public SquareStack.Item sq;
	/** 検出した矩形の格納変数。マーカの姿勢行列を格納します。*/
	public final NyARTransMatResult tmat=new NyARTransMatResult();
	/** 矩形の検出状態の格納変数。 連続して見失った回数を格納します。*/
	public int lost_count=Integer.MAX_VALUE;
	/** トラッキングログ用の領域*/
	public NyARIntPoint2d[] tl_vertex=NyARIntPoint2d.createArray(4);
	public NyARIntPoint2d   tl_center=new NyARIntPoint2d();
	public int tl_rect_area;
	protected TMarkerData()
	{
		this.life=0;
	}
}	