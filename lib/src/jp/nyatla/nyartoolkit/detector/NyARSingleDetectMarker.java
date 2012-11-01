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

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattDeviationColorData;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattResult;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPatt_Color_WITHOUT_PCA;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.pickup.INyARColorPatt;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.INyARRgb2GsFilterArtkTh;
import jp.nyatla.nyartoolkit.core.squaredetect.*;
import jp.nyatla.nyartoolkit.core.pickup.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.core.transmat.*;

/**
 * このクラスは、1個のマーカを取り扱うマーカ検出器を定義します。
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
public abstract class NyARSingleDetectMarker
{
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
	private NyARDoubleMatrix44 _last_input_mat;
	private NyARTransMatResultParam _last_result_param=new NyARTransMatResultParam();

	/**
	 * この関数は、検出したマーカーの変換行列を計算して、o_resultへ値を返します。
	 * 直前に実行した{@link #detectMarkerLite}が成功していないと使えません。
	 * @param o_result
	 * 変換行列を受け取るオブジェクト。{@link #setContinueMode}でtrueを設定した場合は、履歴行列としても使われます。
	 * @throws NyARException
	 */
	public void getTransmat(NyARDoubleMatrix44 o_result) throws NyARException
	{
		// 一番一致したマーカーの位置とかその辺を計算
		if (this._is_continue){
			//履歴が使えそうか判定
			if(this._last_input_mat==o_result){
				if(this._transmat.transMatContinue(this._square,this._offset,o_result, this._last_result_param.last_error,o_result, this._last_result_param)){
					return;
				}
			}
		}
		//履歴使えないor継続認識失敗
		this._transmat.transMat(this._square,this._offset,o_result,this._last_result_param);
		this._last_input_mat=o_result;
		return;
	}
	/**
	 * @deprecated
	 * {@link #getTransmat}
	 */
	public void getTransmationMatrix(NyARDoubleMatrix44 o_result) throws NyARException
	{
		this.getTransmat(o_result);
		return;
	}
	/** 参照インスタンス*/
	private INyARRgbRaster _last_input_raster=null;
	private INyARRgb2GsFilterArtkTh _bin_filter=null;
	/**
	 * この関数は、画像から登録済のマーカ検出を行います。
	 * マーカの検出に成功すると、thisのプロパティにマーカの二次元位置を記録します。
	 * 関数の成功後は、マーカの姿勢行列と、一致度を、それぞれ{@link #getTransmationMatrix}と{@link #getConfidence}から得ることができます。
	 * @param i_raster
	 * マーカーを検出する画像。画像のサイズは、コンストラクタに指定した{@link NyARParam}オブジェクトと一致していなければなりません。
	 * @param i_th
	 * 2値化敷居値。0から256までの値を指定します。
	 * @return
	 * マーカーが検出できたかを、真偽値で返します。
	 * @throws NyARException
	 */	
	public boolean detectMarkerLite(INyARRgbRaster i_raster,int i_th) throws NyARException
	{
		//サイズチェック
		if(!this._bin_raster.getSize().isEqualSize(i_raster.getSize())){
			throw new NyARException();
		}
		//最終入力ラスタを更新
		if(this._last_input_raster!=i_raster){
			this._bin_filter=(INyARRgb2GsFilterArtkTh)i_raster.createInterface(INyARRgb2GsFilterArtkTh.class);
			this._last_input_raster=i_raster;
		}
		//ラスタを２値イメージに変換する.
		this._bin_filter.doFilter(i_th,this._bin_raster);

		//コールバックハンドラの準備
		this._confidence=0;
		this._last_input_raster=i_raster;
		//
		//マーカ検出器をコール
		this.execDetectMarker();
		if(this._confidence==0){
			return false;
		}
		return true;
	}
	
	
	/** 姿勢変換行列の変換器*/
	protected INyARTransMat _transmat;
	/** マーカパターンの保持用*/
	protected INyARColorPatt _inst_patt;
	private NyARRectOffset _offset; 
	private NyARMatchPattDeviationColorData _deviation_data;
	private NyARMatchPatt_Color_WITHOUT_PCA _match_patt;
	private NyARCoord2Linear _coordline;	
	protected NyARBinRaster _bin_raster;
	/** 一致率*/
	private double _confidence=0;
	/** 認識矩形の記録用*/
	protected NyARSquare _square=new NyARSquare();
	

	protected boolean _is_continue = false;
	private final NyARMatchPattResult __detectMarkerLite_mr=new NyARMatchPattResult();

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
		if (!this._inst_patt.pickFromRaster(this._last_input_raster,vertex)){
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
	protected NyARSingleDetectMarker(NyARParam i_ref_param,NyARCode i_ref_code,double i_marker_width) throws NyARException
	{
		this._deviation_data=new NyARMatchPattDeviationColorData(i_ref_code.getWidth(),i_ref_code.getHeight());
		this._match_patt=new NyARMatchPatt_Color_WITHOUT_PCA(i_ref_code);		
		this._offset=new NyARRectOffset();
		this._offset.setSquare(i_marker_width);
		this._coordline=new NyARCoord2Linear(i_ref_param.getScreenSize(),i_ref_param.getDistortionFactor());
		//２値画像バッファを作る		
		NyARIntSize s=i_ref_param.getScreenSize();
		this._bin_raster=new NyARBinRaster(s.w,s.h);
	}
	protected abstract void execDetectMarker() throws NyARException;	
	
	/** ARToolKit互換のアルゴリズムを選択します。*/
	public final static int PF_ARTOOLKIT_COMPATIBLE=1;
	/** NyARToolKitのアルゴリズムを選択します。*/
	public final static int PF_NYARTOOLKIT=2;
	/** ARToolKit互換アルゴリズムと、NyARToolKitのアルゴリズムの混合です。2D系にNyARToolkit,3D系にARToolKitのアルゴリズムを選択します。*/
	public final static int PF_NYARTOOLKIT_ARTOOLKIT_FITTING=100;
	/** 開発用定数値*/
	public final static int PF_TEST2=201;	
	/**
	 * 処理プロファイルを指定して、{@link NyARSingleDetectoMarker}オブジェクトを生成します。
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
	 * @throws NyARException
	 */	
	public static NyARSingleDetectMarker createInstance(NyARParam i_param, NyARCode i_code, double i_marker_width,int i_profile_id) throws NyARException
	{
		switch(i_profile_id){
		case PF_ARTOOLKIT_COMPATIBLE:
			return new NyARSingleDetectMarker_ARTKv2(i_param,i_code,i_marker_width);
		case PF_NYARTOOLKIT_ARTOOLKIT_FITTING:
			return new NyARSingleDetectMarker_NyARTK_FITTING_ARTKv2(i_param,i_code,i_marker_width);
		case PF_NYARTOOLKIT://default
			return new NyARSingleDetectMarker_NyARTK(i_param,i_code,i_marker_width);
		default:
			throw new NyARException();
		}		
	}
	public static NyARSingleDetectMarker createInstance(NyARParam i_param, NyARCode i_code, double i_marker_width) throws NyARException
	{
		return createInstance(i_param,i_code,i_marker_width,PF_NYARTOOLKIT);
	}
	
}

//
//各プロファイル毎のクラス
//


class NyARSingleDetectMarker_ARTKv2 extends NyARSingleDetectMarker
{
	private ARTKDetector _square_detect;	
	
	/**
	 * ARTKラべリングを使った矩形検出機へのブリッジ
	 */
	public static class ARTKDetector extends NyARSquareContourDetector_ARToolKit implements NyARSquareContourDetector.CbHandler
	{
		private NyARSingleDetectMarker _parent;
		public ARTKDetector(NyARSingleDetectMarker i_parent,NyARIntSize i_size) throws NyARException
		{
			super(i_size);
			this._parent=i_parent;
		}
		public void detectMarkerCallback(NyARIntCoordinates i_coord,int[] i_vertex_index) throws NyARException
		{
			this._parent.updateSquareInfo(i_coord, i_vertex_index);
		}	
	}
	public NyARSingleDetectMarker_ARTKv2(NyARParam i_ref_param,NyARCode i_ref_code,double i_marker_width) throws NyARException
	{
		super(i_ref_param,i_ref_code,i_marker_width);
		this._inst_patt=new NyARColorPatt_O3(i_ref_code.getWidth(), i_ref_code.getHeight());
		this._transmat=new NyARTransMat_ARToolKit(i_ref_param);
		this._square_detect=new ARTKDetector(this,i_ref_param.getScreenSize());
	}
	protected void execDetectMarker() throws NyARException
	{
		//矩形を探す(戻り値はコールバック関数で受け取る。)
		this._square_detect.detectMarker(this._bin_raster,this._square_detect);
		
	}
}
class NyARSingleDetectMarker_NyARTK_FITTING_ARTKv2 extends NyARSingleDetectMarker
{
	protected NyARSingleDetectMarker_ARTKv2.ARTKDetector _square_detect;	
	public NyARSingleDetectMarker_NyARTK_FITTING_ARTKv2(NyARParam i_ref_param,NyARCode i_ref_code,double i_marker_width) throws NyARException
	{
		super(i_ref_param,i_ref_code,i_marker_width);
		this._inst_patt=new NyARColorPatt_Perspective(i_ref_code.getWidth(), i_ref_code.getHeight(),4,25);
		this._transmat=new NyARTransMat_ARToolKit(i_ref_param);
		this._square_detect=new NyARSingleDetectMarker_ARTKv2.ARTKDetector(this,i_ref_param.getScreenSize());
	}
	protected void execDetectMarker() throws NyARException
	{
		//矩形を探す(戻り値はコールバック関数で受け取る。)
		this._square_detect.detectMarker(this._bin_raster,this._square_detect);
	}	
}
/**
 * NyARToolkitのアルゴリズムを使用するSingleDetectMarker
 * @author nyatla
 *
 */
class NyARSingleDetectMarker_NyARTK extends NyARSingleDetectMarker
{
	private RleDetector _square_detect;
	/**
	 * RleLabelingを使った矩形検出機
	 */
	private class RleDetector extends NyARSquareContourDetector_Rle implements NyARSquareContourDetector.CbHandler
	{
		NyARSingleDetectMarker _parent;
		public RleDetector(NyARSingleDetectMarker i_parent,NyARIntSize i_size) throws NyARException
		{
			super(i_size);
			this._parent=i_parent;
		}

		public void detectMarkerCallback(NyARIntCoordinates i_coord,int[] i_vertex_index) throws NyARException
		{
			this._parent.updateSquareInfo(i_coord, i_vertex_index);
			
		}
	}
	
	public NyARSingleDetectMarker_NyARTK(NyARParam i_ref_param,NyARCode i_ref_code,double i_marker_width) throws NyARException
	{
		super(i_ref_param,i_ref_code,i_marker_width);
		this._inst_patt=new NyARColorPatt_Perspective(i_ref_code.getWidth(), i_ref_code.getHeight(),4,25);
		this._transmat=new NyARTransMat(i_ref_param);
		this._square_detect=new RleDetector(this,i_ref_param.getScreenSize());
	}	
	protected void execDetectMarker() throws NyARException
	{
		//矩形を探す(戻り値はコールバック関数で受け取る。)
		this._square_detect.detectMarker(this._bin_raster,0,this._square_detect);
		
	}
}




