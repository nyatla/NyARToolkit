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
package jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status;

import jp.nyatla.nyartoolkit.core.utils.*;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.*;


/**
 * このクラスは、トラックターゲットのステータスを格納するマネージドオブジェクトのベースクラスです。
 * トラックターゲットのステータス毎に、格納するパラメータを追加して使います。
 * <p>メモ - 
 * ST_XXXの定数値は、{@link NyARTracker}のコンストラクタ実装と密接に絡んでいます。
 * 変更するときは注意すること！
 * </p>
 *
 */
public class NyARTargetStatus extends NyARManagedObject
{
	/** 定数値。IGNOREステータスを表します。*/
	public final static int ST_IGNORE=0;
	/** 定数値。NEWステータスを表します。*/
	public final static int ST_NEW=1;
	/** 定数値。RECTステータスを表します。*/
	public final static int ST_RECT=2;
	/** 定数値。CONTUREステータスを表します。*/
	public final static int ST_CONTURE=3;
	/** 定数値。ステータスの種類を表します。*/
	public final static int MAX_OF_ST_KIND=3;
	/**
	 * コンストラクタです。
	 * 所有されるプールオブジェクトを指定して、インスタンスを生成します。
	 * @param iRefPoolOperator
	 * プールオブジェクトのコントロールインタフェイス。
	 */
	protected NyARTargetStatus(INyARManagedObjectPoolOperater iRefPoolOperator)
	{
		super(iRefPoolOperator);
	}
}
