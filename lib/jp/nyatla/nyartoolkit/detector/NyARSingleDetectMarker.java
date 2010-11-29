/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
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
package jp.nyatla.nyartoolkit.detector;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.pickup.INyARColorPatt;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;

import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.NyARRasterFilter_ARToolkitThreshold;
import jp.nyatla.nyartoolkit.core.squaredetect.*;
import jp.nyatla.nyartoolkit.core.pickup.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
/**
 * 画像からARCodeに最も一致するマーカーを1個検出し、その変換行列を計算するクラスです。
 * 
 */
public class NyARSingleDetectMarker extends NyARCustomSingleDetectMarker
{
	public final static int PF_ARTOOLKIT_COMPATIBLE=1;
	public final static int PF_NYARTOOLKIT=2;
	public final static int PF_NYARTOOLKIT_ARTOOLKIT_FITTING=100;
	public final static int PF_TEST2=201;
	
	/**
	 * 検出するARCodeとカメラパラメータから、1個のARCodeを検出するNyARSingleDetectMarkerインスタンスを作ります。
	 * 
	 * @param i_param
	 * カメラパラメータを指定します。
	 * @param i_code
	 * 検出するARCodeを指定します。
	 * @param i_marker_width
	 * ARコードの物理サイズを、ミリメートルで指定します。
	 * @param i_input_raster_type
	 * 入力ラスタのピクセルタイプを指定します。この値は、INyARBufferReaderインタフェイスのgetBufferTypeの戻り値を指定します。
	 * @throws NyARException
	 */
	public NyARSingleDetectMarker(NyARParam i_param, NyARCode i_code, double i_marker_width,int i_input_raster_type,int i_profile_id) throws NyARException
	{
		super();
		initialize(i_param,i_code,i_marker_width,i_input_raster_type,i_profile_id);
		return;
	}
	public NyARSingleDetectMarker(NyARParam i_param, NyARCode i_code, double i_marker_width,int i_input_raster_type) throws NyARException
	{
		super();
		initialize(i_param,i_code,i_marker_width,i_input_raster_type,PF_NYARTOOLKIT);
		return;
	}
	/**
	 * コンストラクタから呼び出す関数です。
	 * @param i_ref_param
	 * @param i_ref_code
	 * @param i_marker_width
	 * @param i_input_raster_type
	 * @param i_profile_id
	 * @throws NyARException
	 */
	private void initialize(
		NyARParam	i_ref_param,
		NyARCode	i_ref_code,
		double		i_marker_width,
		int i_input_raster_type,
		int i_profile_id) throws NyARException
	{
		final NyARRasterFilter_ARToolkitThreshold th=new NyARRasterFilter_ARToolkitThreshold(100,i_input_raster_type);
		INyARColorPatt patt_inst;
		NyARSquareContourDetector sqdetect_inst;
		INyARTransMat transmat_inst;

		switch(i_profile_id){
		case PF_ARTOOLKIT_COMPATIBLE:
			patt_inst=new NyARColorPatt_O3(i_ref_code.getWidth(), i_ref_code.getHeight());
			sqdetect_inst=new NyARSquareContourDetector_ARToolKit(i_ref_param.getScreenSize());
			transmat_inst=new NyARTransMat_ARToolKit(i_ref_param);
			break;
		case PF_NYARTOOLKIT_ARTOOLKIT_FITTING:
			patt_inst=new NyARColorPatt_Perspective_O2(i_ref_code.getWidth(), i_ref_code.getHeight(),4,25);
			sqdetect_inst=new NyARSquareContourDetector_Rle(i_ref_param.getScreenSize());
			transmat_inst=new NyARTransMat_ARToolKit(i_ref_param);
			break;
		case PF_NYARTOOLKIT://default
			patt_inst=new NyARColorPatt_Perspective_O2(i_ref_code.getWidth(), i_ref_code.getHeight(),4,25);
			sqdetect_inst=new NyARSquareContourDetector_Rle(i_ref_param.getScreenSize());
			transmat_inst=new NyARTransMat(i_ref_param);
			break;
		default:
			throw new NyARException();
		}
		super.initInstance(patt_inst,sqdetect_inst,transmat_inst,th,i_ref_param,i_ref_code,i_marker_width);
		
	}

	/**
	 * i_imageにマーカー検出処理を実行し、結果を記録します。
	 * 
	 * @param i_raster
	 * マーカーを検出するイメージを指定します。イメージサイズは、コンストラクタで指定i_paramの
	 * スクリーンサイズと一致し、かつi_input_raster_typeに指定した形式でなければいけません。
	 * @return マーカーが検出できたかを真偽値で返します。
	 * @throws NyARException
	 */
	public boolean detectMarkerLite(INyARRgbRaster i_raster,int i_threshold) throws NyARException
	{
		((NyARRasterFilter_ARToolkitThreshold)this._tobin_filter).setThreshold(i_threshold);
		return super.detectMarkerLite(i_raster);
	}
}
