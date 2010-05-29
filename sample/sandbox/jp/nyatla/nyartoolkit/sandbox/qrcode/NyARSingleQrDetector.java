/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.sandbox.qrcode;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.gs2bin.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareStack;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * 画像からARCodeに最も一致するマーカーを1個検出し、その変換行列を計算するクラスです。
 * 
 */
public class NyARSingleQrDetector
{
	private static final int AR_SQUARE_MAX = 100;

	private boolean _is_continue = false;
	private NyARSquareContourDetector _square_detect;

	private final NyARSquareStack _square_list = new NyARSquareStack(AR_SQUARE_MAX);

	protected INyARTransMat _transmat;

	private double _marker_width;

	// 検出結果の保存用
	private NyARSquare _detected_square;


	/**
	 * 検出するARCodeとカメラパラメータから、1個のARCodeを検出するNyARSingleDetectMarkerインスタンスを作ります。
	 * 
	 * @param i_param
	 * カメラパラメータを指定します。
	 * @param i_marker_width
	 * ARコードの物理サイズを、ミリメートルで指定します。
	 * @throws NyARException
	 */
	public NyARSingleQrDetector(NyARParam i_param, double i_marker_width) throws NyARException
	{
		final NyARIntSize scr_size=i_param.getScreenSize();		
		// 解析オブジェクトを作る
		this._square_detect = new NyARQrCodeDetector(i_param.getDistortionFactor(),scr_size);
		this._transmat = new NyARTransMat_ARToolKit(i_param);
		this._marker_width = i_marker_width;
		//２値画像バッファを作る
		this._bin_raster=new NyARBinRaster(scr_size.w,scr_size.h);
		//中間のグレースケール画像のバッファを作る
		this._gs_raster=new NyARGrayscaleRaster(scr_size.w,scr_size.h);
	}
	private NyARBinRaster _bin_raster;
	private NyARGrayscaleRaster _gs_raster;
	//画処理フィルター
	private INyARRasterFilter_Rgb2Gs _rgb2gs_filter=new NyARRasterFilter_Rgb2Gs_AveAdd();
	private INyARRasterFilter_Gs2Bin _gstobin_filter=new NyARRasterFilter_QrAreaAverage();

	/**
	 * i_imageにマーカー検出処理を実行し、結果を記録します。
	 * 
	 * @param i_raster
	 * マーカーを検出するイメージを指定します。イメージサイズは、カメラパラメータ
	 * と一致していなければなりません。
	 * @return マーカーが検出できたかを真偽値で返します。
	 * @throws NyARException
	 */
	public boolean detectMarkerLite(INyARRgbRaster i_raster,int i_threshold) throws NyARException
	{
		//サイズチェック
		if(!this._bin_raster.getSize().isEqualSize(i_raster.getSize())){
			throw new NyARException();
		}
		//グレースケールに変換
		this._rgb2gs_filter.doFilter(i_raster, this._gs_raster);
		//2値イメージに変換
		this._gstobin_filter.doFilter(this._gs_raster, this._bin_raster);		
		
		this._detected_square = null;
		NyARSquareStack l_square_list = this._square_list;
		// スクエアコードを探す
		this._square_detect.detectMarkerCB(this._bin_raster, l_square_list);
		//変換する


		int number_of_square = l_square_list.getLength();
		// コードは見つかった？
		if (number_of_square < 1) {
			return false;
		}
		this._detected_square=l_square_list.getItem(0);
		return true;
	}

	/**
	 * 検出したマーカーの変換行列を計算して、o_resultへ値を返します。
	 * 直前に実行したdetectMarkerLiteが成功していないと使えません。
	 * 
	 * @param o_result
	 * 変換行列を受け取るオブジェクトを指定します。
	 * @throws NyARException
	 */
	public void getTransmationMatrix(NyARTransMatResult o_result) throws NyARException
	{
		// 一番一致したマーカーの位置とかその辺を計算
		if (this._is_continue) {
			this._transmat.transMatContinue(this._detected_square,this._detected_square.direction,this._marker_width, o_result);
		} else {
			this._transmat.transMat(this._detected_square,this._detected_square.direction,this._marker_width, o_result);
		}
		return;
	}
	/**
	 * getTransmationMatrixの計算モードを設定します。 初期値はTRUEです。
	 * 
	 * @param i_is_continue
	 * TRUEなら、transMatCont互換の計算をします。 FALSEなら、transMat互換の計算をします。
	 */
	public void setContinueMode(boolean i_is_continue)
	{
		this._is_continue = i_is_continue;
	}
}
