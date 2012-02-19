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
package jp.nyatla.nyartoolkit.rpf.realitysource.nyartk;


import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.INyARPerspectiveCopy;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.NyARTrackerSource;

import jp.nyatla.nyartoolkit.rpf.reality.nyartk.*;
/**
 * このクラスは、{@link NyARReality}へのデータ入力コンテナです。
 * {@link NyARReality}へ入力する情報セットをメンバに持ちます。
 * このクラスは情報セットのみ定義します。関数の実体とメンバ変数の生成は、継承クラスで実装してください。
 * <p>クラスの責務 -
 * 情報セットは、{@link NyARTrackerSource},{@link INyARRgbRaster},{@link NyARPerspectiveRasterReader}
 * の３要素で構成します。クラスは、これらのメンバ間の情報同期を責務とします。同期のタイミングは、メンバ関数の動作で定義します。
 * </p>
 * <p>使い方 -
 * このクラスの派生クラスは、次のように使います。
 * <ol>
 * <li>内部RGBラスタへの書込み({@link #refRgbSource}で得られたオブジェクトへの直接書き込み、又は継承クラスで提供する関数を使った書込み。)
 * <li>{@link NyARReality}への入力。
 * </ol>
 * なお、内部RGBラスタラスタと関連オブジェクトの同期は、{@link NyARReality#progress}で行います。
 * {@link NyARReality}オブジェクトと同期した{@link NyARRealitySource}オブジェクトが完成するのは、{@link NyARReality#progress}を実行した後です。
 * </p>
 * </ul>
 */
public abstract class NyARRealitySource
{
	/** RealitySourceの主ラスタ。継承先のコンストラクタで実体を割り当ててください。*/
	protected INyARRgbRaster _rgb_source;
	/** RealitySourceの主ラスタにリンクしたPerspectiveReader。継承先のコンストラクタで実体を割り当ててください。*/
	protected INyARPerspectiveCopy _source_perspective_reader;

	/** TrackerSorceのホルダ。継承先のコンストラクタで実体を割り当ててください。*/
	protected NyARTrackerSource _tracksource;

	/**
	 * コンストラクタです。
	 * 継承クラスでオーバライドして、メンバ変数を割り当てる処理を書いてください。
	 */
	protected NyARRealitySource(){}
	

	/**
	 * この関数には、thisインスタンスの読出し準備ができているか返す処理を実装します。
	 * 読出し可能な状態とは、{@link #makeTrackSource}を実行できる状態を指します。
	 * @return
	 * trueならば、読出し可能である。
	 */
	public abstract boolean isReady();	
	/**
	 * この関数には、{@link #_rgb_source}の内容を{@link #_tracksource}のグレースケール画像へ反映する処理を書きます。
	 * {@link NyARTrackerSource}のベースラスタに書き込みを行うだけである事に注意してください。
	 * この関数は{@link NyARReality#progress}から呼び出される関数であり、ユーザが使用することはありません。
	 * @throws NyARException
	 * @return
	 * RGB画像の内容を反映した{@link NyARTrackerSource}オブジェクトへの参照値。
	 */
	public abstract NyARTrackerSource makeTrackSource() throws NyARException;
	/**
	 * この関数には、{@link #_rgb_source}の内容を{@link #_tracksource}のグレースケール画像へ反映し、{@link NyARTrackerSource#syncResource}を実行する処理を書きます。
	 * 通常、ユーザがこの関数を使用することはありません。デバックなどで、{@link NyARReality#progress}以外の方法でオブジェクトの同期を行いたいときに使用します。
	 * 継承クラスでは、{@link #_tracksource}の基本GS画像を、{@link #_rgb_source}の内容で更新してから、この関数を呼び出して同期する処理を実装をしてください。
	 * @throws NyARException 
	　*/
	public void syncResource() throws NyARException
	{
		//下位の同期
		this._tracksource.syncResource();
	}
	/**
	 * RGBソースラスタ{@link #_rgb_source}を参照する{@link INyARPerspectiveCopy}を返します。
	 * @return [read only] {@link #_rgb_source}にリンクした{@link INyARPerspectiveCopy}オブジェクト
	 */
	public INyARPerspectiveCopy refPerspectiveRasterReader()
	{
		return this._source_perspective_reader;
	}
	
	/**
	 * RGBソースラスタへの参照値を返します。
	 * @return
	 * RGBソースラスタへの参照値。
	 */
	public final INyARRgbRaster refRgbSource()
	{
		return this._rgb_source;
	}
	/**
	 * 最後に作成したTrackSourceへの参照ポインタを返します。
	 * 更新の完了した{@link NyARReality}オブジェクトから情報を得るために使います。
	 * この関数は、{@link NyARReality#progress}、または{@link #syncResource}の後に呼び出すことを想定しています。
	 * それ以外のタイミングでは、返却値の内容が同期していないことがあるので注意してください。
	 * @return
	 * [read only]現在のトラッキングオブジェクトの参照ポインタ
	 */
	public final NyARTrackerSource refLastTrackSource()
	{
		return this._tracksource;
	}
}


