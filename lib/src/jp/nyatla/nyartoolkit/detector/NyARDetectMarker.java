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
import jp.nyatla.nyartoolkit.core.match.*;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.pickup.*;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.INyARRgb2GsFilterArtkTh;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector_Rle;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;



/**
 * このクラスは、複数のマーカを取り扱うマーカ検出器です。
 * 登録したn個のARマーカに対応するマーカを入力画像から検出し、その変換行列と一致度を返します。
 * この関数は、画像中のm個のマーカに、登録したn個のマーカの中から、最も一致したものの番号を割り当てる動作をします。
 * そのため、同一な種類（パターン）のマーカが複数存在する場合、２つのマーカを区別することができません。
 * また、同一なマーカパターンを複数登録すると、意図しない動作をします。
 * <p>簡単な使い方
 * <ol>
 * <li>インスタンスを作成します。パラメータには、計算アルゴリズムと入力画像形式、カメラパラメータ、検出するマーカパターンテーブルがあります。
 * <li>{@link #detectMarkerLite}関数に画像と敷居値を入力して、マーカを検出します。
 * <li>検出数が得られるので、{@link #getARCodeIndex}関数を使って、検出番号をマーカのインデクス番号に変換します。
 * <li>インデクス番号を元に、{@link #getConfidence}等の関数を使って、取得したマーカの状態を得ます。
 * <li>以降は、この処理を繰り返してマーカのパラメータを更新します。
 * </ol>
 * </p>
 */
public class NyARDetectMarker
{
	/** 矩形検出器のブリッジ*/
	private class RleDetector extends NyARSquareContourDetector_Rle
	{
		//公開プロパティ
		public NyARDetectMarkerResultStack result_stack=new NyARDetectMarkerResultStack(NyARDetectMarker.AR_SQUARE_MAX);
		//参照インスタンス
		public INyARRgbRaster _ref_raster;
		//所有インスタンス
		private INyARColorPatt _inst_patt;
		private NyARMatchPattDeviationColorData _deviation_data;
		private NyARMatchPatt_Color_WITHOUT_PCA[] _match_patt;
		private final NyARMatchPattResult __detectMarkerLite_mr=new NyARMatchPattResult();
		private NyARCoord2Linear _coordline;
		
		public RleDetector(INyARColorPatt i_inst_patt,NyARCode[] i_ref_code,int i_num_of_code,NyARParam i_param) throws NyARException
		{
			super(i_param.getScreenSize());
			final int cw = i_ref_code[0].getWidth();
			final int ch = i_ref_code[0].getHeight();
			//NyARMatchPatt_Color_WITHOUT_PCA[]の作成
			this._match_patt=new NyARMatchPatt_Color_WITHOUT_PCA[i_num_of_code];
			this._match_patt[0]=new NyARMatchPatt_Color_WITHOUT_PCA(i_ref_code[0]);
			for (int i = 1; i < i_num_of_code; i++){
				//解像度チェック
				if (cw != i_ref_code[i].getWidth() || ch != i_ref_code[i].getHeight()) {
					throw new NyARException();
				}
				this._match_patt[i]=new NyARMatchPatt_Color_WITHOUT_PCA(i_ref_code[i]);
			}
			this._inst_patt=i_inst_patt;
			this._coordline=new NyARCoord2Linear(i_param.getScreenSize(),i_param.getDistortionFactor());
			this._deviation_data=new NyARMatchPattDeviationColorData(cw,ch);
			return;
		}
		private NyARIntPoint2d[] __ref_vertex=new NyARIntPoint2d[4];
		/**
		 * 矩形が見付かるたびに呼び出されます。
		 * 発見した矩形のパターンを検査して、方位を考慮した頂点データを確保します。
		 */
		protected void onSquareDetect(NyARIntCoordinates i_coord,int[] i_vertex_index) throws NyARException
		{
			NyARMatchPattResult mr=this.__detectMarkerLite_mr;
			//輪郭座標から頂点リストに変換
			NyARIntPoint2d[] vertex=this.__ref_vertex;
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

			//最も一致するパターンを割り当てる。
			int square_index,direction;
			double confidence;
			this._match_patt[0].evaluate(this._deviation_data,mr);
			square_index=0;
			direction=mr.direction;
			confidence=mr.confidence;
			//2番目以降
			for(int i=1;i<this._match_patt.length;i++){
				this._match_patt[i].evaluate(this._deviation_data,mr);
				if (confidence > mr.confidence) {
					continue;
				}
				// もっと一致するマーカーがあったぽい
				square_index = i;
				direction = mr.direction;
				confidence = mr.confidence;
			}
			//最も一致したマーカ情報を、この矩形の情報として記録する。
			final NyARDetectMarkerResult result = this.result_stack.prePush();
			result.arcode_id = square_index;
			result.confidence = confidence;

			final NyARSquare sq=result.square;
			//directionを考慮して、squareを更新する。
			for(int i=0;i<4;i++){
				int idx=(i+4 - direction) % 4;
				this._coordline.coord2Line(i_vertex_index[idx],i_vertex_index[(idx+1)%4],i_coord,sq.line[i]);
			}
			for (int i = 0; i < 4; i++) {
				//直線同士の交点計算
				if(!sq.line[i].crossPos(sq.line[(i + 3) % 4],sq.sqvertex[i])){
					throw new NyARException();//ここのエラー復帰するならダブルバッファにすればOK
				}
			}
		}
		public void init(INyARRgbRaster i_raster)
		{
			this._ref_raster=i_raster;
			this.result_stack.clear();
			
		}
	}
	/** 変換行列計算器のインスタンス*/
	private INyARTransMat _transmat;
	private static final int AR_SQUARE_MAX = 300;
	private boolean _is_continue = false;
	private RleDetector _square_detect;
	private NyARRectOffset[] _offset;	

	/**
	 * コンストラクタです。
	 * 同一でない複数のマーカを検出する検出器を作成します。
	 * @param i_param
	 * カメラパラメータを指定します。このサイズは、{@link #detectMarkerLite}に入力する画像と同じである必要があります。
	 * @param i_code
	 * 検出するマーカーパターンを格納した、{@link NyARCode}の配列を指定します。配列には、先頭から、0から始まるID番号が割り当てられます。
	 * このIDは、{@link #getARCodeIndex}で取得できるID値であり、マーカパターンの識別に使います。
	 * 配列要素の{@link NyARCode}は、全て同じ解像度にしてください。
	 * @param i_marker_width
	 * 正方形マーカの物理サイズをmm単位で指定します。
	 * @param i_number_of_code
	 * i_codeの有効な個数を指定します。
	 * @throws NyARException
	 */
	public NyARDetectMarker(NyARParam i_param,NyARCode[] i_code,double[] i_marker_width, int i_number_of_code) throws NyARException
	{
		initInstance(i_param,i_code,i_marker_width,i_number_of_code);
		return;
	}
	/**
	 * この関数は、インスタンスを初期化します。
	 * コンストラクタから呼び出します。
	 * @see NyARDetectMarker#NyARDetectMarker(NyARParam, NyARCode[], double[], int, int)
	 * @param i_ref_param
	 * Check see also
	 * @param i_ref_code
	 * Check see also
	 * @param i_marker_width
	 * Check see also
	 * @param i_number_of_code
	 * Check see also
	 * @param i_input_raster_type
	 * Check see also
	 * @throws NyARException
	 */
	protected void initInstance(
		NyARParam	i_ref_param,
		NyARCode[]	i_ref_code,
		double[]	i_marker_width,
		int			i_number_of_code) throws NyARException
	{

		final NyARIntSize scr_size=i_ref_param.getScreenSize();
		// 解析オブジェクトを作る
		final int cw = i_ref_code[0].getWidth();
		final int ch = i_ref_code[0].getHeight();

		this._transmat = new NyARTransMat(i_ref_param);
		//NyARToolkitプロファイル
		this._square_detect =new RleDetector(new NyARColorPatt_Perspective(cw, ch,4,25),i_ref_code,i_number_of_code,i_ref_param);

		//実サイズ保存
		this._offset = NyARRectOffset.createArray(i_number_of_code);
		for(int i=0;i<i_number_of_code;i++){
			this._offset[i].setSquare(i_marker_width[i]);
		}
		//２値画像バッファを作る
		this._bin_raster=new NyARBinRaster(scr_size.w,scr_size.h);
		return;		
	}
	
	private NyARBinRaster _bin_raster;

	private INyARRgb2GsFilterArtkTh _tobin_filter;
	private INyARRgbRaster _last_input_raster=null;

	/**
	 * この関数は、画像からマーカを検出します。
	 * 関数は、登録されているマーカパターンそれぞれに対し、検出したマーカから最も一致した物を探し、その一致率と位置を計算します。
	 * @param i_raster
	 * マーカーを検出するイメージを指定します。
	 * @param i_threshold
	 * 検出閾値を指定します。0～255の範囲で指定してください。 通常は100～130くらいを指定します。
	 * @return
	 * 検出したマーカーの数を返します。 マーカーが見つからない場合は0を返します。
	 * @throws NyARException
	 */
	public int detectMarkerLite(INyARRgbRaster i_raster, int i_threshold) throws NyARException
	{
		// サイズチェック
		if (!this._bin_raster.getSize().isEqualSize(i_raster.getSize())) {
			throw new NyARException();
		}
		if(this._last_input_raster!=i_raster){
			this._tobin_filter=(INyARRgb2GsFilterArtkTh) i_raster.createInterface(INyARRgb2GsFilterArtkTh.class);
			this._last_input_raster=i_raster;
		}
		this._tobin_filter.doFilter(i_threshold,this._bin_raster);
		//detect
		this._square_detect.init(i_raster);
		this._square_detect.detectMarker(this._bin_raster,0);

		//見付かった数を返す。
		return this._square_detect.result_stack.getLength();
	}

	/**
	 * この関数は、i_index番目に検出したマーカの、変換行列を計算します。
	 * 直前に実行した{@link #detectMarkerLite}が成功していないと使えません。
	 * @param i_index
	 * 検出結果のインデックス番号を指定します。 
	 * この値は、0から{@link #detectMarkerLite}関数の戻り値-1の数です。
	 * @param o_result
	 * 結果値を受け取るオブジェクト
	 * @throws NyARException
	 */
	public void getTransmationMatrix(int i_index, NyARTransMatResult o_result) throws NyARException
	{
		final NyARDetectMarkerResult result = this._square_detect.result_stack.getItem(i_index);
		// 一番一致したマーカーの位置とかその辺を計算
		if (_is_continue) {
			_transmat.transMatContinue(result.square, this._offset[result.arcode_id], o_result,o_result);
		} else {
			_transmat.transMat(result.square, this._offset[result.arcode_id], o_result);
		}
		return;
	}

	/**
	 * この関数は、i_index番目に検出したマーカの、一致度を返します。
	 * 直前に実行した{@link #detectMarkerLite}が成功していないと使えません。
	 * @param i_index
	 * 検出結果のインデックス番号を指定します。 
	 * この値は、0から{@link #detectMarkerLite}関数の戻り値-1の数です。
	 * @return
	 * マーカーの一致度を返します。0～1までの値をとります。 一致度が低い場合には、誤認識の可能性が高くなります。
	 * @throws NyARException
	 */
	public double getConfidence(int i_index)
	{
		return this._square_detect.result_stack.getItem(i_index).confidence;
	}
	/**
	 * この関数は、i_index番目に検出したマーカの、ID番号を返します。
	 * 直前に実行した{@link #detectMarkerLite}が成功していないと使えません。
	 * @param i_index
	 * 検出結果のインデックス番号を指定します。 
	 * この値は、0から{@link #detectMarkerLite}関数の戻り値-1の数です。
	 * @return
	 * ID番号です。この値は、コンストラクタでマーカパターンを登録したときに決まる、シリアル番号です。
	 * @throws NyARException
	 */
	public int getARCodeIndex(int i_index)
	{
		return this._square_detect.result_stack.getItem(i_index).arcode_id;
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
}

/** 内部クラスです。ユーザが使用することはありません*/
class NyARDetectMarkerResult
{
	public int arcode_id;
	public double confidence;

	public NyARSquare square=new NyARSquare();
}


/** 内部クラスです。ユーザが使用することはありません*/
class NyARDetectMarkerResultStack extends NyARObjectStack<NyARDetectMarkerResult>
{
	public NyARDetectMarkerResultStack(int i_length) throws NyARException
	{
		super();
		this.initInstance(i_length,NyARDetectMarkerResult.class);
		return;
	}
	protected NyARDetectMarkerResult createElement()
	{
		return new NyARDetectMarkerResult();
	}	
}