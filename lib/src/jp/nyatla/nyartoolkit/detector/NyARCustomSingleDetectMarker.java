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
import jp.nyatla.nyartoolkit.core.match.*;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.pickup.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.squaredetect.*;


/**
 * このクラスは、1個のマーカを取り扱うマーカ検出器のベースクラスです。
 * アプリケーションからは、このクラスを継承した{@link NyARSingleDetectMarker}を使います。
 * 登録した１個のARマーカに対応するマーカを入力画像から検出し、その変換行列と一致度を返します。
 */
public abstract class NyARCustomSingleDetectMarker
{
	/** 一致率*/
	private double _confidence;
	private NyARSquare _square=new NyARSquare();
	
	//参照インスタンス
	private INyARRgbRaster _ref_raster;
	//所有インスタンス
	private INyARColorPatt _inst_patt;
	private NyARMatchPattDeviationColorData _deviation_data;
	private NyARMatchPatt_Color_WITHOUT_PCA _match_patt;
	private final NyARMatchPattResult __detectMarkerLite_mr=new NyARMatchPattResult();
	private NyARCoord2Linear _coordline;
	

	private NyARIntPoint2d[] __ref_vertex=new NyARIntPoint2d[4];

	/**
	 * 内部関数です。
	 * この関数は、thisの二次元矩形情報プロパティを更新します。
	 * @param i_coord
	 * @param i_vertex_index
	 * @throws NyARException
	 */
	protected void updateSquareInfo(NyARIntCoordinates i_coord,int[] i_vertex_index) throws NyARException
	{
		NyARMatchPattResult mr=this.__detectMarkerLite_mr;
		//輪郭座標から頂点リストに変換
		NyARIntPoint2d[] vertex=this.__ref_vertex;	//C言語ならポインタ扱いで実装
		vertex[0]=i_coord.items[i_vertex_index[0]];
		vertex[1]=i_coord.items[i_vertex_index[1]];
		vertex[2]=i_coord.items[i_vertex_index[2]];
		vertex[3]=i_coord.items[i_vertex_index[3]];
	
		//画像を取得
		if (!this._inst_patt.pickFromRaster(this._ref_raster,vertex)){
			return;
		}
		//取得パターンをカラー差分データに変換して評価する。
		this._deviation_data.setRaster(this._inst_patt);
		if(!this._match_patt.evaluate(this._deviation_data,mr)){
			return;
		}
		//現在の一致率より低ければ終了
		if (this._confidence > mr.confidence){
			return;
		}
		//一致率の高い矩形があれば、方位を考慮して頂点情報を作成
		NyARSquare sq=this._square;
		this._confidence = mr.confidence;
		//directionを考慮して、squareを更新する。
		for(int i=0;i<4;i++){
			int idx=(i+4 - mr.direction) % 4;
			this._coordline.coord2Line(i_vertex_index[idx],i_vertex_index[(idx+1)%4],i_coord,sq.line[i]);
		}
		//ちょっと、ひっくり返してみようか。
		for (int i = 0; i < 4; i++) {
			//直線同士の交点計算
			if(!sq.line[i].crossPos(sq.line[(i + 3) % 4],sq.sqvertex[i])){
				throw new NyARException();//ここのエラー復帰するならダブルバッファにすればOK
			}
		}
	}

	
	private boolean _is_continue = false;
	private NyARSquareContourDetector _square_detect;
	private NyARRectOffset _offset; 
	private NyARBinRaster _bin_raster;
	private INyARTransMat _transmat;

	
	//画処理用
	protected INyARRasterFilter_Rgb2Bin _tobin_filter;


	/**
	 * コンストラクタです。
	 * 実際の初期化は、継承クラスから、{@link #initInstance}関数をコールして実行します。
	 */
	protected NyARCustomSingleDetectMarker()
	{
		return;
	}
	/**
	 * この関数は、インスタンスを初期化してます。
	 * @param i_patt_inst
	 * パターン取得オブジェクトを指定します。この解像度は、i_ref_code引数の解像度と同一である必要があります。
	 * @param i_sqdetect_inst
	 * 矩形検出器のオブジェクトを指定します。
	 * @param i_transmat_inst
	 * 変換行列計算器のオブジェクトを指定します。
	 * @param i_filter
	 * 画像の２値化オブジェクトを指定します。
	 * @param i_ref_param
	 * カメラパラメータオブジェクトの参照値を指定します。
	 * @param i_ref_code
	 * 検出するARマーカを格納したオブジェクトの参照値を指定します。
	 * @param i_marker_width
	 * マーカノ物理サイズを、mm単位で指定します。
	 * @throws NyARException
	 */
	protected void initInstance(
		INyARColorPatt i_patt_inst,
		NyARSquareContourDetector i_sqdetect_inst,
		INyARTransMat i_transmat_inst,
		INyARRasterFilter_Rgb2Bin i_filter,
		NyARParam	i_ref_param,
		NyARCode	i_ref_code,
		double		i_marker_width) throws NyARException
	{
		final NyARIntSize scr_size=i_ref_param.getScreenSize();		
		// 解析オブジェクトを作る
		this._square_detect = i_sqdetect_inst;
		this._transmat = i_transmat_inst;
		this._tobin_filter=i_filter;
		//２値画像バッファを作る
		this._bin_raster=new NyARBinRaster(scr_size.w,scr_size.h);
		//パターンの一致検索処理用
		this._inst_patt=i_patt_inst;
		this._deviation_data=new NyARMatchPattDeviationColorData(i_ref_code.getWidth(),i_ref_code.getHeight());
		this._coordline=new NyARCoord2Linear(i_ref_param.getScreenSize(),i_ref_param.getDistortionFactor());
		this._match_patt=new NyARMatchPatt_Color_WITHOUT_PCA(i_ref_code);
		//オフセットを作成
		this._offset=new NyARRectOffset();
		this._offset.setSquare(i_marker_width);
		return;
	}
	
	/**
	 * この関数は、画像から登録済のマーカ検出を行います。
	 * マーカの検出に成功すると、thisのプロパティにマーカの二次元位置を記録します。
	 * 関数の成功後は、マーカの姿勢行列と、一致度を、それぞれ{@link #getTransmationMatrix}と{@link #getConfidence}から得ることができます。
	 * @param i_raster
	 * マーカーを検出する画像。画像のサイズは、コンストラクタに指定した{@link NyARParam}オブジェクトと一致していなければなりません。
	 * @return
	 * マーカーが検出できたかを、真偽値で返します。
	 * @throws NyARException
	 */
	protected boolean detectMarkerLite(INyARRgbRaster i_raster) throws NyARException
	{
		//サイズチェック
		if(!this._bin_raster.getSize().isEqualSize(i_raster.getSize())){
			throw new NyARException();
		}

		//ラスタを２値イメージに変換する.
		this._tobin_filter.doFilter(i_raster,this._bin_raster);

		//コールバックハンドラの準備
		this._confidence=0;
		this._ref_raster=i_raster;

		//矩形を探す(戻り値はコールバック関数で受け取る。)
		this._square_detect.detectMarker(this._bin_raster);
		if(this._confidence==0){
			return false;
		}
		return true;
	}
	
	/**
	 * この関数は、検出したマーカーの変換行列を計算して、o_resultへ値を返します。
	 * 直前に実行した{@link #detectMarkerLite}が成功していないと使えません。
	 * @param o_result
	 * 変換行列を受け取るオブジェクト。
	 * @throws NyARException
	 */
	public void getTransmationMatrix(NyARTransMatResult o_result) throws NyARException
	{
		// 一番一致したマーカーの位置とかその辺を計算
		if (this._is_continue) {
			this._transmat.transMatContinue(this._square,this._offset,o_result, o_result);
		} else {
			this._transmat.transMat(this._square,this._offset, o_result);
		}
		return;
	}
	/**
	 * この関数は、マーカーの画像上の位置を格納する、{@link NyARSquare}への参照値を返します。
	 * 直前に実行した{@link #detectMarkerLite}が成功していないと使えません。
	 * 返却値の内容は、次に{@link #detectMarkerLite}を実行するまで有効です。
	 * @return
	 * 矩形情報への参照値。
	 */
	public NyARSquare refSquare()
	{
		return this._square;
	}
	/**
	 * この関数は、検出したマーカーと登録済パターンとの、一致度を返します。
	 * 直前に実行した{@link #detectMarkerLite}が成功していないと使えません。
	 * 値は、0&lt;=n<1の間の数値を取ります。
	 * 一般的に、一致度が低い場合は、マーカを誤認識しています。
	 * @return
	 * 一致度の数値。
	 */
	public double getConfidence()
	{
		return this._confidence;
	}
	/**
	 * この関数は、変換行列の計算モードを切り替えます。
	 * 通常はtrueを使用します。
	 * transMat互換の計算は、姿勢の初期値を毎回二次元座標から計算するため、負荷が安定します。
	 * transMatCont互換の計算は、姿勢の初期値に前回の結果を流用します。このモードは、姿勢の安定したマーカに対しては
	 * ジッタの減少や負荷減少などの効果がありますが、姿勢の安定しないマーカや複数のマーカを使用する環境では、
	 * 少量の負荷変動があります。
	 * @param i_is_continue
	 * TRUEなら、transMatCont互換の計算をします。 FALSEなら、transMat互換の計算をします。
	 */
	public void setContinueMode(boolean i_is_continue)
	{
		this._is_continue = i_is_continue;
	}
	/**
	 * デバック関数。
	 * privateメンバにアクセスするためのトンネルです。
	 * @return
	 * デバック用オブジェクトを格納した配列。
	 */
	public Object[] _getProbe()
	{
		Object[] r=new Object[1];
		r[0]=this._inst_patt;
		return r;
	}
}
