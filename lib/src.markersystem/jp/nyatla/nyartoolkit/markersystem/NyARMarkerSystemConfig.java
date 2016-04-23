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

import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.histogram.algo.INyARHistogramAnalyzer_Threshold;
import jp.nyatla.nyartoolkit.core.histogram.algo.NyARHistogramAnalyzer_SlidePTile;
import jp.nyatla.nyartoolkit.core.icp.NyARIcpTransMat;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat_ARToolKit;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * このクラスは、NyARToolkitの姿勢推定アルゴリズムに調整したコンフィギュレーションクラスです。
 *
 */
public class NyARMarkerSystemConfig implements INyARMarkerSystemConfig
{
	/** ARToolkit v2互換のニュートン法を使った変換行列計算アルゴリズムを選択します。*/
	public final static int TM_ARTKV2=1;
	/** NyARToolKitの偏微分を使った変換行列アルゴリズムです。*/
	public final static int TM_NYARTK=2;
	/** ARToolkit v4に搭載されているICPを使った変換行列計算アルゴリズムを選択します。*/
	public final static int TM_ARTKICP=3;
	protected final NyARSingleCameraView _cview;
	private final int _transmat_algo_type;
	
	/**
	 * 
	 * @param i_view
	 * @param i_transmat_algo_type
	 */
	public NyARMarkerSystemConfig(NyARSingleCameraView i_view,int i_transmat_algo_type)
	{
		assert(1<=i_transmat_algo_type && i_transmat_algo_type<=3);
		this._cview=i_view;
		this._transmat_algo_type=i_transmat_algo_type;
	}	
	
	/**
	 * 
	 * @param i_param
	 * @param i_transmat_algo_type
	 */
	public NyARMarkerSystemConfig(NyARParam i_param,int i_transmat_algo_type)
	{
		this(new NyARSingleCameraView(i_param),i_transmat_algo_type);
	}
	/**
	 * コンストラクタです。
	 * 初期化済カメラパラメータからコンフィギュレーションを生成します。
	 * @param i_param
	 * 初期化に使うカメラパラメータオブジェクト。インスタンスの所有権は、インスタンスに移ります。
	 */
	public NyARMarkerSystemConfig(NyARParam i_param)
	{
		this(i_param,TM_ARTKICP);
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
	public NyARMarkerSystemConfig(InputStream i_ar_param_stream,int i_width,int i_height)
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
	public NyARMarkerSystemConfig(int i_width,int i_height)
	{
		this(NyARParam.loadDefaultParams(i_width, i_height));
	}
	
	/**
	 * この値は、カメラパラメータのスクリーンサイズです。
	 */
	@Override
	public final NyARIntSize getScreenSize()
	{
		return this._cview.getARParam().getScreenSize();
	}
	
	@Override
	public NyARSingleCameraView getNyARSingleCameraView() {
		return this._cview;
	}	
	@Override
	public INyARTransMat createTransmatAlgorism()
	{
		NyARParam params=this._cview.getARParam();
		switch(this._transmat_algo_type){
		case TM_ARTKV2:
			return new NyARTransMat_ARToolKit(params);
		case TM_NYARTK:
			return new NyARTransMat(params);
		case TM_ARTKICP:
			return new NyARIcpTransMat(params,NyARIcpTransMat.AL_POINT_ROBUST);
		}
		throw new InternalError();
	}
	@Override
	public INyARHistogramAnalyzer_Threshold createAutoThresholdArgorism()
	{
		return new NyARHistogramAnalyzer_SlidePTile(15);
	}


}