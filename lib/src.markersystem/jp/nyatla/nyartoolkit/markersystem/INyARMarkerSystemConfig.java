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
package jp.nyatla.nyartoolkit.markersystem;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.analyzer.histogram.INyARHistogramAnalyzer_Threshold;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * このインタフェイスは、NyARMarkerSystemのコンフィギュレーションインタフェイスを定義します。
 *
 */
public interface INyARMarkerSystemConfig
{
	/**
	 * 姿勢変換アルゴリズムクラスのオブジェクトを生成して返します。
	 * @return
	 * @throws NyARException
	 */
	public INyARTransMat createTransmatAlgorism() throws NyARException;
	/**
	 * 敷居値決定クラスを生成して返します。
	 * @return
	 * @throws NyARException
	 */
	public INyARHistogramAnalyzer_Threshold createAutoThresholdArgorism() throws NyARException;
	/**
	 * ARToolKitパラメータオブジェクトを返します。
	 * @return
	 * [readonly]
	 */
	public NyARParam getNyARParam();
	/**
	 * コンフィギュレーションのスクリーンサイズを返します。
	 * @return
	 * [readonly]
	 */
	public NyARIntSize getScreenSize();
}