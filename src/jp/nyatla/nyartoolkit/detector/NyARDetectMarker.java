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
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector_Rle;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;

class NyARDetectMarkerResult
{
	public int arcode_id;
	public double confidence;

	public NyARSquare square=new NyARSquare();
}


class NyARDetectMarkerResultStack extends NyARObjectStack<NyARDetectMarkerResult>
{
	public NyARDetectMarkerResultStack(int i_length) throws NyARException
	{
		super(i_length,NyARDetectMarkerResult.class);
		return;
	}
	protected NyARDetectMarkerResult createElement()
	{
		return new NyARDetectMarkerResult();
	}	
}


/**
 * 複数のマーカーを検出し、それぞれに最も一致するARコードを、コンストラクタで登録したARコードから 探すクラスです。最大300個を認識しますが、ゴミラベルを認識したりするので100個程度が限界です。
 * 
 */
public class NyARDetectMarker
{
	/**
	 * detectMarkerのコールバック関数
	 */
	private class DetectSquareCB implements NyARSquareContourDetector.IDetectMarkerCallback
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
		
		public DetectSquareCB(INyARColorPatt i_inst_patt,NyARCode[] i_ref_code,int i_num_of_code,NyARParam i_param) throws NyARException
		{
			final int cw = i_ref_code[0].getWidth();
			final int ch = i_ref_code[0].getHeight();

			this._inst_patt=i_inst_patt;
			this._coordline=new NyARCoord2Linear(i_param.getScreenSize(),i_param.getDistortionFactor());
			this._deviation_data=new NyARMatchPattDeviationColorData(cw,ch);

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
			return;
		}
		private NyARIntPoint2d[] __tmp_vertex=NyARIntPoint2d.createArray(4);
		/**
		 * 矩形が見付かるたびに呼び出されます。
		 * 発見した矩形のパターンを検査して、方位を考慮した頂点データを確保します。
		 */
		public void onSquareDetect(NyARSquareContourDetector i_sender,int[] i_coordx,int[] i_coordy,int i_coor_num,int[] i_vertex_index) throws NyARException
		{
			NyARMatchPattResult mr=this.__detectMarkerLite_mr;
			//輪郭座標から頂点リストに変換
			NyARIntPoint2d[] vertex=this.__tmp_vertex;
			vertex[0].x=i_coordx[i_vertex_index[0]];
			vertex[0].y=i_coordy[i_vertex_index[0]];
			vertex[1].x=i_coordx[i_vertex_index[1]];
			vertex[1].y=i_coordy[i_vertex_index[1]];
			vertex[2].x=i_coordx[i_vertex_index[2]];
			vertex[2].y=i_coordy[i_vertex_index[2]];
			vertex[3].x=i_coordx[i_vertex_index[3]];
			vertex[3].y=i_coordy[i_vertex_index[3]];
		
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
				this._coordline.coord2Line(i_vertex_index[idx],i_vertex_index[(idx+1)%4],i_coordx,i_coordy,i_coor_num,sq.line[i]);
			}
			for (int i = 0; i < 4; i++) {
				//直線同士の交点計算
				if(!NyARLinear.crossPos(sq.line[i],sq.line[(i + 3) % 4],sq.sqvertex[i])){
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
	private DetectSquareCB _detect_cb;
	
	
	private static final int AR_SQUARE_MAX = 300;
	private boolean _is_continue = false;
	private NyARSquareContourDetector _square_detect;
	protected INyARTransMat _transmat;
	private NyARRectOffset[] _offset;	


	/**
	 * 複数のマーカーを検出し、最も一致するARCodeをi_codeから検索するオブジェクトを作ります。
	 * 
	 * @param i_param
	 * カメラパラメータを指定します。
	 * @param i_code
	 * 検出するマーカーのARCode配列を指定します。
	 * 配列要素のインデックス番号が、そのままgetARCodeIndex関数で得られるARCodeインデックスになります。 
	 * 例えば、要素[1]のARCodeに一致したマーカーである場合は、getARCodeIndexは1を返します。
	 * @param i_marker_width
	 * i_codeのマーカーサイズをミリメートルで指定した配列を指定します。 先頭からi_number_of_code個の要素には、有効な値を指定する必要があります。
	 * @param i_number_of_code
	 * i_codeに含まれる、ARCodeの数を指定します。
	 * @param i_input_raster_type
	 * 入力ラスタのピクセルタイプを指定します。この値は、INyARBufferReaderインタフェイスのgetBufferTypeの戻り値を指定します。
	 * @throws NyARException
	 */
	public NyARDetectMarker(NyARParam i_param,NyARCode[] i_code,double[] i_marker_width, int i_number_of_code,int i_input_raster_type) throws NyARException
	{
		initInstance(i_param,i_code,i_marker_width,i_number_of_code,i_input_raster_type);
		return;
	}
	protected void initInstance(
		NyARParam	i_ref_param,
		NyARCode[]	i_ref_code,
		double[]	i_marker_width,
		int			i_number_of_code,
		int i_input_raster_type) throws NyARException
	{

		final NyARIntSize scr_size=i_ref_param.getScreenSize();
		// 解析オブジェクトを作る
		final int cw = i_ref_code[0].getWidth();
		final int ch = i_ref_code[0].getHeight();

		//detectMarkerのコールバック関数
		this._detect_cb=new DetectSquareCB(
			new NyARColorPatt_Perspective_O2(cw, ch,4,25),
			i_ref_code,i_number_of_code,i_ref_param);
		this._transmat = new NyARTransMat(i_ref_param);
		//NyARToolkitプロファイル
		this._square_detect =new NyARSquareContourDetector_Rle(i_ref_param.getScreenSize());
		this._tobin_filter=new NyARRasterFilter_ARToolkitThreshold(100,i_input_raster_type);

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

	private INyARRasterFilter_Rgb2Bin _tobin_filter;

	/**
	 * i_imageにマーカー検出処理を実行し、結果を記録します。
	 * 
	 * @param i_raster
	 * マーカーを検出するイメージを指定します。
	 * @param i_thresh
	 * 検出閾値を指定します。0～255の範囲で指定してください。 通常は100～130くらいを指定します。
	 * @return 見つかったマーカーの数を返します。 マーカーが見つからない場合は0を返します。
	 * @throws NyARException
	 */
	public int detectMarkerLite(INyARRgbRaster i_raster, int i_threshold) throws NyARException
	{
		// サイズチェック
		if (!this._bin_raster.getSize().isEqualSize(i_raster.getSize())) {
			throw new NyARException();
		}

		// ラスタを２値イメージに変換する.
		((NyARRasterFilter_ARToolkitThreshold)this._tobin_filter).setThreshold(i_threshold);
		this._tobin_filter.doFilter(i_raster, this._bin_raster);

		//detect
		this._detect_cb.init(i_raster);
		this._square_detect.detectMarkerCB(this._bin_raster,this._detect_cb);

		//見付かった数を返す。
		return this._detect_cb.result_stack.getLength();
	}

	/**
	 * i_indexのマーカーに対する変換行列を計算し、結果値をo_resultへ格納します。 直前に実行したdetectMarkerLiteが成功していないと使えません。
	 * 
	 * @param i_index
	 * マーカーのインデックス番号を指定します。 直前に実行したdetectMarkerLiteの戻り値未満かつ0以上である必要があります。
	 * @param o_result
	 * 結果値を受け取るオブジェクトを指定してください。
	 * @throws NyARException
	 */
	public void getTransmationMatrix(int i_index, NyARTransMatResult o_result) throws NyARException
	{
		final NyARDetectMarkerResult result = this._detect_cb.result_stack.getItem(i_index);
		// 一番一致したマーカーの位置とかその辺を計算
		if (_is_continue) {
			_transmat.transMatContinue(result.square, this._offset[result.arcode_id], o_result);
		} else {
			_transmat.transMat(result.square, this._offset[result.arcode_id], o_result);
		}
		return;
	}

	/**
	 * i_indexのマーカーの一致度を返します。
	 * 
	 * @param i_index
	 * マーカーのインデックス番号を指定します。 直前に実行したdetectMarkerLiteの戻り値未満かつ0以上である必要があります。
	 * @return マーカーの一致度を返します。0～1までの値をとります。 一致度が低い場合には、誤認識の可能性が高くなります。
	 * @throws NyARException
	 */
	public double getConfidence(int i_index)
	{
		return this._detect_cb.result_stack.getItem(i_index).confidence;
	}
	/**
	 * i_indexのマーカーのARCodeインデックスを返します。
	 * 
	 * @param i_index
	 * マーカーのインデックス番号を指定します。 直前に実行したdetectMarkerLiteの戻り値未満かつ0以上である必要があります。
	 * @return
	 */
	public int getARCodeIndex(int i_index)
	{
		return this._detect_cb.result_stack.getItem(i_index).arcode_id;
	}

	/**
	 * getTransmationMatrixの計算モードを設定します。
	 * 
	 * @param i_is_continue
	 * TRUEなら、transMatContinueを使用します。 FALSEなら、transMatを使用します。
	 */
	public void setContinueMode(boolean i_is_continue)
	{
		this._is_continue = i_is_continue;
	}

}
