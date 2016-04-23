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
package jp.nyatla.nyartoolkit.nftsystem;

import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;

import jp.nyatla.nyartoolkit.core.param.NyARParam;

import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.markersystem.NyARSingleCameraView;

/**
 * このクラスは、NyARToolkitの姿勢推定アルゴリズムに調整したコンフィギュレーションクラスです。
 *
 */
public class NyARNftSystemConfig implements INyARNftSystemConfig
{
	protected final NyARSingleCameraView _cview;

	public NyARNftSystemConfig(NyARSingleCameraView i_ref_view)
	{
		this._cview=i_ref_view;
	}
	/**
	 * 
	 * @param i_param
	 * @param i_transmat_algo_type
	 */
	public NyARNftSystemConfig(NyARParam i_param)
	{
		this(new NyARSingleCameraView(i_param));
	}

	/**
	 * コンストラクタです。
	 * i_ar_parama_streamからカメラパラメータファイルを読み出して、スクリーンサイズをi_width,i_heightに変形してから、
	 * コンフィギュレーションを生成します。
	 * @param i_ar_param_stream
	 * カメラパラメータファイルを読み出すストリーム
	 * @param i_width
	 * スクリーンサイズ
	 * @param i_height
	 * スクリーンサイズ
	 * @throws NyARRuntimeException
	 */
	public NyARNftSystemConfig(InputStream i_ar_param_stream,int i_width,int i_height)
	{
		this(NyARParam.loadFromARParamFile(i_ar_param_stream, i_width, i_height));
	}
	/**
	 * コンストラクタです。カメラパラメータにサンプル値(../Data/camera_para.dat)の値をロードして、
	 * コンフィギュレーションを生成します。
	 * @param i_width
	 * スクリーンサイズ
	 * @param i_height
	 * スクリーンサイズ
	 * @throws NyARRuntimeException
	 */
	public NyARNftSystemConfig(int i_width,int i_height)
	{
		this(NyARParam.loadDefaultParams(i_width, i_height));
	}
	/**
	 * この値は、カメラパラメータのスクリーンサイズです。
	 */
	public final NyARIntSize getScreenSize()
	{
		return this._cview.getARParam().getScreenSize();
	}


	@Override
	public NyARSingleCameraView getNyARSingleCameraView() {
		// TODO Auto-generated method stub
		return this._cview;
	}
}