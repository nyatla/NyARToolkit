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
package jp.nyatla.nyartoolkit.dev.pro.markersytem;

import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * こ�?�クラスは、NyARToolkitの姿勢推定アルゴリズ�?に調整したコンフィギュレーションクラスです�??
 *
 */
public class NyARNFTSystemConfig
{
	protected NyARParam _param;
	/**
	 * コンストラクタです�??
	 * 初期化済カメラパラメータからコンフィギュレーションを生成します�??
	 * @param i_param
	 * 初期化に使�?カメラパラメータオブジェクト�?�インスタンスの�?有権は、インスタンスに移ります�??
	 */
	public NyARNFTSystemConfig(NyARParam i_param)
	{
		this._param=i_param;
	}
	/**
	 * コンストラクタです�??
	 * i_ar_parama_streamからカメラパラメータファイルを読み出して、スクリーンサイズをi_width,i_heightに変形してから�?
	 * コンフィギュレーションを生成します�??
	 * @param i_ar_param_stream
	 * カメラパラメータファイルを読み出すストリー�?
	 * @param i_width
	 * スクリーンサイズ
	 * @param i_height
	 * スクリーンサイズ
	 * @throws NyARException
	 */
	public NyARNFTSystemConfig(InputStream i_ar_param_stream,int i_width,int i_height) throws NyARException
	{
		this._param=NyARParam.createFromARParamFile(i_ar_param_stream);
		this._param.changeScreenSize(i_width,i_height);
	}
	/**
	 * コンストラクタです�?�カメラパラメータにサンプル値(../Data/camera_para.dat)の値をロードして�?
	 * コンフィギュレーションを生成します�??
	 * @param i_width
	 * スクリーンサイズ
	 * @param i_height
	 * スクリーンサイズ
	 * @throws NyARException
	 */
	public NyARNFTSystemConfig(int i_width,int i_height) throws NyARException
	{
		this._param=NyARParam.createDefaultParameter();
		this._param.changeScreenSize(i_width,i_height);		
	}
	/**
	 * こ�?�値は、カメラパラメータのスクリーンサイズです�??
	 */
	public final NyARIntSize getScreenSize()
	{
		return this._param.getScreenSize();
	}
	/**
	 * @Override
	 */
	public NyARParam getNyARParam()
	{
		return 	this._param;
	}
}