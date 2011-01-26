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
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.transmat.*;

/**
 * このクラスは、1個のマーカを取り扱うマーカ検出器です。
 * 登録した１個のARマーカに対応するマーカを入力画像から検出し、その変換行列と一致度を返します。
 * <p>簡単な使い方
 * <ol>
 * <li>インスタンスを作成します。パラメータには、計算アルゴリズムと入力画像形式、カメラパラメータ、検出するマーカがあります。
 * <li>{@link #detectMarkerLite}関数に画像と敷居値を入力して、マーカを検出します。
 * <li>マーカが見つかると、インスタンスのプロパティが更新されます。{@link #getConfidence}等の関数を使って、取得したマーカの状態を得ます。
 * <li>以降は、この処理を繰り返してマーカのパラメータを更新します。
 * </ol>
 * </p>
 */
public class NyARSingleDetectMarker extends NyARCustomSingleDetectMarker
{
	/** ARToolKit互換のアルゴリズムを選択します。*/
	public final static int PF_ARTOOLKIT_COMPATIBLE=1;
	/** NyARToolKitのアルゴリズムを選択します。*/
	public final static int PF_NYARTOOLKIT=2;
	/** ARToolKit互換アルゴリズムと、NyARToolKitのアルゴリズムの混合です。2D系にNyARToolkit,3D系にARToolKitのアルゴリズムを選択します。*/
	public final static int PF_NYARTOOLKIT_ARTOOLKIT_FITTING=100;
	/** 開発用定数値*/
	public final static int PF_TEST2=201;
	
	/**
	 * RleLabelingを使った矩形検出機
	 */
	private class RleDetector extends NyARSquareContourDetector_Rle
	{
		NyARCustomSingleDetectMarker _parent;
		public RleDetector(NyARCustomSingleDetectMarker i_parent,NyARIntSize i_size) throws NyARException
		{
			super(i_size);
			this._parent=i_parent;
		}
		protected void onSquareDetect(NyARIntCoordinates i_coord,int[] i_vertex_index) throws NyARException
		{
			this._parent.updateSquareInfo(i_coord, i_vertex_index);
		}	
	}
	/**
	 * ARTKラべリングを使った矩形検出機へのブリッジ
	 */
	class ARTKDetector extends NyARSquareContourDetector_ARToolKit
	{
		NyARCustomSingleDetectMarker _parent;
		public ARTKDetector(NyARCustomSingleDetectMarker i_parent,NyARIntSize i_size) throws NyARException
		{
			super(i_size);
			this._parent=i_parent;
		}
		protected void onSquareDetect(NyARIntCoordinates i_coord,int[] i_vertex_index) throws NyARException
		{
			this._parent.updateSquareInfo(i_coord, i_vertex_index);
		}	
	}	
	
	/**
	 * コンストラクタです。
	 * 指定した1種のマーカを1個検出するインスタンスを作ります。
	 * @param i_param
	 * カメラパラメータを指定します。このサイズは、{@link #detectMarkerLite}に入力する画像と同じである必要があります。
	 * @param i_code
	 * 検出するマーカパターンを指定します。
	 * @param i_marker_width
	 * 正方形マーカの物理サイズをmm単位で指定します。
	 * @param i_input_raster_type
	 * {@link #detectMarkerLite}に入力するラスタの画素形式を指定します。
	 * この値は、{@link INyARRgbRaster#getBufferType}関数の戻り値を利用します。
	 * @param i_profile_id
	 * 計算アルゴリズムの選択値です。以下の定数のいずれかを指定します。
	 * <ul>
	 * <li>{@link #PF_ARTOOLKIT_COMPATIBLE}
	 * <li>{@link #PF_NYARTOOLKIT}
	 * <li>{@link #PF_NYARTOOLKIT_ARTOOLKIT_FITTING}
	 * </ul>
	 * @throws NyARException
	 */
	public NyARSingleDetectMarker(NyARParam i_param, NyARCode i_code, double i_marker_width,int i_input_raster_type,int i_profile_id) throws NyARException
	{
		super();
		initialize(i_param,i_code,i_marker_width,i_input_raster_type,i_profile_id);
		return;
	}
	/**
	 * コンストラクタです。
	 * 指定した1種のマーカを1個検出するインスタンスを作ります。
	 * i_profile_idに{@link #PF_NYARTOOLKIT}を選択した{@link #NyARSingleDetectMarker(NyARParam, NyARCode, double, int, int)}と同じです。
	 * @see #NyARSingleDetectMarker(NyARParam, NyARCode, double, int, int)
	 * @param i_param
	 * Check see also
	 * @param i_code
	 * Check see also
	 * @param i_marker_width
	 * Check see also
	 * @param i_input_raster_type
	 * Check see also
	 * @throws NyARException
	 */
	public NyARSingleDetectMarker(NyARParam i_param, NyARCode i_code, double i_marker_width,int i_input_raster_type) throws NyARException
	{
		super();
		initialize(i_param,i_code,i_marker_width,i_input_raster_type,PF_NYARTOOLKIT);
		return;
	}
	/**
	 * この関数は、インスタンスを初期化します。
	 * 引数は、{@link #NyARSingleDetectMarker}の対応する引数と同じです。
	 * @see #NyARSingleDetectMarker(NyARParam, NyARCode, double, int, int)
	 * @param i_ref_param
	 * Check see also
	 * @param i_ref_code
	 * Check see also
	 * @param i_marker_width
	 * Check see also
	 * @param i_input_raster_type
	 * Check see also
	 * @param i_profile_id
	 * Check see also
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
			sqdetect_inst=new ARTKDetector(this,i_ref_param.getScreenSize());
			transmat_inst=new NyARTransMat_ARToolKit(i_ref_param);
			break;
		case PF_NYARTOOLKIT_ARTOOLKIT_FITTING:
			patt_inst=new NyARColorPatt_Perspective_O2(i_ref_code.getWidth(), i_ref_code.getHeight(),4,25,i_input_raster_type);
			sqdetect_inst=new RleDetector(this,i_ref_param.getScreenSize());
			transmat_inst=new NyARTransMat_ARToolKit(i_ref_param);
			break;
		case PF_NYARTOOLKIT://default
//			patt_inst=new NyARColorPatt_Perspective(i_ref_code.getWidth(), i_ref_code.getHeight(),4,25);
			patt_inst=new NyARColorPatt_Perspective_O2(i_ref_code.getWidth(), i_ref_code.getHeight(),4,25,i_input_raster_type);
			sqdetect_inst=new RleDetector(this,i_ref_param.getScreenSize());
			transmat_inst=new NyARTransMat(i_ref_param);
			break;
		default:
			throw new NyARException();
		}
		super.initInstance(patt_inst,sqdetect_inst,transmat_inst,th,i_ref_param,i_ref_code,i_marker_width);
		
	}
	/**
	 * この関数は、画像からマーカを検出します。
	 * 関数は、画像の二値化、ラべリング、矩形検出、パターンの一致判定処理までを行い、画像中にある最も一致したパターンを持つ矩形の座標を、thisの
	 * プロパティへ保管します。
	 * @param i_raster
	 * 検出元の画像を指定します。この画像は、コンストラクタで指定したものと同じサイズ、画素形式に限られます。
	 * @param i_threshold
	 * 二値化の敷居値を指定します。0&lt;=n<256の間で指定します。
	 * @return
	 * マーカの検出に成功すると、trueを返します。
	 * @throws NyARException
	 */
	public boolean detectMarkerLite(INyARRgbRaster i_raster,int i_threshold) throws NyARException
	{
		((NyARRasterFilter_ARToolkitThreshold)this._tobin_filter).setThreshold(i_threshold);
		return super.detectMarkerLite(i_raster);
	}
}
