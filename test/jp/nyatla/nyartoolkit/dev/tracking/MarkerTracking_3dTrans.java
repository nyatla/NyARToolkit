package jp.nyatla.nyartoolkit.dev.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattDeviationColorData;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattResult;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPatt_Color_WITHOUT_PCA;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.pickup.INyARColorPatt;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.INyARRasterFilter_Rgb2Bin;
import jp.nyatla.nyartoolkit.dev.tracking.MarkerPositionTable.Item;

class MarkerPositionStack extends NyARObjectStack<MarkerPositionStack.Item>
{
	public class Item
	{
		public MarkerPositionTable.Item ref_item;
		//マーカ中心
		public NyARDoublePoint2d   center=new NyARDoublePoint2d();
		//4頂点(スクリーン座標系)
		public NyARIntPoint2d[] vertex=NyARIntPoint2d.createArray(4);
		//4頂点の直線式
		public NyARLinear[] line=NyARLinear.createArray(4);
	}
	protected MarkerPositionStack.Item createElement()
	{
		return new MarkerPositionStack.Item();
	}
	
	public MarkerPositionStack(int i_length) throws NyARException
	{
		super(i_length,MarkerPositionStack.Item.class);
		return;
	}
}


class MarkerOutlineStack extends NyARObjectStack<MarkerOutlineStack.Item>
{
	class Item
	{
		//マーカ中心
		public NyARIntPoint2d   center=new NyARIntPoint2d();
		//4頂点(スクリーン座標系)
		public NyARIntPoint2d[] vertex=NyARIntPoint2d.createArray(4);
		public boolean is_empty;
	}
	protected Item createElement()
	{
		return new Item();
	}
	
	public MarkerOutlineStack(int i_length) throws NyARException
	{
		super(i_length,Item.class);
		return;
	}
}

class MarkerOutlineTable extends NyARDataTable<MarkerOutlineTable.Item>
{
	class Item
	{
		public boolean is_empty;
		//マーカ中心
		public NyARIntPoint2d   center=new NyARIntPoint2d();
		//4頂点(スクリーン座標系)
		public NyARIntPoint2d[] vertex=NyARIntPoint2d.createArray(4);
		public int sirial;
	}

	protected MarkerOutlineItem createElement()
	{
		return new MarkerOutlineItem();
	}
	
	public MarkerOutlineTable(int i_length) throws NyARException
	{
		return;
	}
	/**
	 * スタックの情報でテーブルの内容を更新する。
	 * @param i_src
	 */
	public void updateTable(MarkerOutlineStack i_src)
	{
		int number_of_stack=i_src.getLength();
		for(int i=this._items.length-1;i>=0;i--)
		{
			Item item=this._items[i];
			if(item.is_empty){
				continue;
			}
			for(int i2=number_of_stack-1;i2>=0;i2--)
			{
				
			
			}
		}
	}
}







public class MarkerTracking_3dTrans
{
	/**
	 * detectMarkerのコールバック関数
	 */
	private class DetectSquareCB implements NyARSquareContourDetector.IDetectMarkerCallback
	{
		//公開プロパティ
		public NyARSquare square=new NyARSquare();
		public EstimatePositionStack _estimate_position;
		private MarkerPositionStack _found_stack;
		private double _marker_width;
		
		//参照インスタンス
		private INyARRgbRaster _ref_raster;
		//所有インスタンス
		private INyARColorPatt _inst_patt;
		private NyARMatchPattDeviationColorData _deviation_data;
		private NyARMatchPatt_Color_WITHOUT_PCA _match_patt;
		private final NyARMatchPattResult __detectMarkerLite_mr=new NyARMatchPattResult();
		private NyARCoord2Linear _coordline;
		private MarkerPositionTable table;
		private MarkerTableOperator table_operator;

		public DetectSquareCB(
			INyARColorPatt i_inst_patt,
			NyARCode i_ref_code,
			NyARParam i_param,
			double i_marker_width) throws NyARException
		{
			this._estimate_position=new EstimatePositionStack(10);
			this._marker_width=i_marker_width;
			//
			this.table_operator=new MarkerTableOperator(i_param);
			this.table=new MarkerPositionTable(10);			
			
			//
			this._inst_patt=i_inst_patt;
			this._deviation_data=new NyARMatchPattDeviationColorData(i_ref_code.getWidth(),i_ref_code.getHeight());
			this._coordline=new NyARCoord2Linear(i_param.getScreenSize(),i_param.getDistortionFactor());
			this._match_patt=new NyARMatchPatt_Color_WITHOUT_PCA(i_ref_code);
			return;
		}
		private NyARIntPoint2d[] __tmp_vertex=NyARIntPoint2d.createArray(4);
		private NyARDoublePoint2d __tmp_point=new NyARDoublePoint2d();
		/**
		 * 矩形が見付かるたびに呼び出されます。
		 * 発見した矩形のパターンを検査して、方位を考慮した頂点データを確保します。
		 */
		public void onSquareDetect(NyARSquareContourDetector i_sender,int[] i_coordx,int[] i_coordy,int i_coor_num,int[] i_vertex_index) throws NyARException
		{
			MarkerOutlineStack outline_stack=null;
			//輪郭座標から頂点リストに変換(アウトラインスタックを共用)
			NyARIntPoint2d[] vertex=outline_stack.prePush().vertex;
			vertex[0].x=i_coordx[i_vertex_index[0]];
			vertex[0].y=i_coordy[i_vertex_index[0]];
			vertex[1].x=i_coordx[i_vertex_index[1]];
			vertex[1].y=i_coordy[i_vertex_index[1]];
			vertex[2].x=i_coordx[i_vertex_index[2]];
			vertex[2].y=i_coordy[i_vertex_index[2]];
			vertex[3].x=i_coordx[i_vertex_index[3]];
			vertex[3].y=i_coordy[i_vertex_index[3]];
			
			//マーカ中心を計算
			NyARDoublePoint2d new_center=this.__tmp_point;
			new_center.x=(vertex[0].x+vertex[1].x+vertex[2].x+vertex[3].x)/4;
			new_center.y=(vertex[0].y+vertex[1].y+vertex[2].y+vertex[3].y)/4;
			//トラッキングテーブルに存在する可能性のあるマーカをストック
			//スタックへ移動したら、アウトラインスタックからは解放。
			

			
			
			
			
			//近所のマーカを探す。[Optimize:重複計算あり]
			EstimatePositionStack.Item near_item=this._estimate_position.getNearItem(new_center);
			if(near_item==null)
			{
				//認識要求を出す。
				
				//このマーカは未登録
				NyARMatchPattResult mr=this.__detectMarkerLite_mr;
				
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
				if (0.5 > mr.confidence){
					return;
				}
				//一致率の高い矩形があれば、方位を考慮して頂点情報を作成
				NyARSquare sq=this.square;
				//directionを考慮して、squareを更新する。
				for(int i=0;i<4;i++){
					int idx=(i+4 - mr.direction) % 4;
					this._coordline.coord2Line(i_vertex_index[idx],i_vertex_index[(idx+1)%4],i_coordx,i_coordy,i_coor_num,sq.line[i]);
				}
				for (int i = 0; i < 4; i++) {
					//直線同士の交点計算
					if(!NyARLinear.crossPos(sq.line[i],sq.line[(i + 3) % 4],sq.sqvertex[i])){
						throw new NyARException();//ここのエラー復帰するならダブルバッファにすればOK
					}
				}
				System.out.println("D:"+mr.direction);
				//テーブルにマーカ情報を追加する。
				this.table_operator.insertMarker(this.table,sq,this._marker_width);							

			}else{
			//このマーカは登録済。

				//継続認識候補のリストに加える				
				MarkerPositionStack.Item item=this._found_stack.prePush();				
				for(int i=0;i<4;i++){
					item.vertex[i].x=vertex[i].x;
					item.vertex[i].y=vertex[i].y;
					this._coordline.coord2Line(i_vertex_index[i],i_vertex_index[(i+1)%4],i_coordx,i_coordy,i_coor_num,item.line[i]);
				}
			}
		}
		/**
		 * 輪郭の頂点集合i_base_vertexとi_next_vertexを比較して、i_next_vertexから、輪郭線の開始点を検出する。
		 * 検出アルゴリズムは、頂点の移動距離の最小値を得る方法
		 * @param i_base_vertex
		 * 基準となる輪郭点集合
		 * @param i_next_vertex
		 * 比較する輪郭点集合
		 * @return
		 * i_base_vertex[0]に一致するi_next_vertex中の輪郭点インデクスを返す。
		 */

		private int getNearVertexIndex(NyARIntPoint2d[] i_base_vertex,NyARIntPoint2d[] i_next_vertex,int i_length)
		{
			int min_dist=Integer.MAX_VALUE;
			int idx=-1;
			for(int i=0;i<i_length;i++){
				int dist=0;
				for(int i2=0;i2<i_length;i2++){
					dist+=NyARMath.sqNorm(i_base_vertex[i2],i_next_vertex[(i2+i)%4]);
				}
				if(min_dist>dist){
					min_dist=dist;
					idx=i;
				}
			}
			return idx;
		}
		/**
		 * コールバックハンドラの初期化
		 * @param i_raster
		 */
		
		public final void init(INyARRgbRaster i_raster)
		{
			//現在位置のテーブルから、探索予定の一覧を作成。
			this._ref_raster=i_raster;
			//継続認識候補のマーカリストをクリア
			this._found_stack.clear();
			this.table_operator.estimateMarkerPosition(this.table,this._estimate_position);
			return;
		}
		/**
		 * コールバックハンドラの終期化
		 */
		public final void finish()
		{
			//アウトラインスタックからアウトラインテーブルへ一致検索しながら移動
			
			
			
			int number_of_found=this._found_stack.getLength();
			MarkerPositionStack.Item[] found_item=this._found_stack.getArray();
			EstimatePositionStack.Item[] est_item=this._estimate_position.getArray();

			//this._existstack.getLength()-1
			//予想マーカ位置に最適な認識済矩形をマップする。
			for(int i=this._estimate_position.getLength()-1;i>=0;i--){
				for(int i2=number_of_found-1;i2>=0;i2--){
					current.getLength()					
				}
			}
			
		//	}*/
		}
		
		
		
		
	}
	private NyARSquareContourDetector _square_detect;
	protected INyARTransMat _transmat;
	//画処理用
	private NyARBinRaster _bin_raster;
	protected INyARRasterFilter_Rgb2Bin _tobin_filter;
	private DetectSquareCB _detect_cb;
	
	public IntRectStack _next_marker;
	public TransMat2MarkerRect _estimator;


	protected MarkerTracking_3dTrans()
	{
		return;
	}
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
		// 比較コードを保存
		//２値画像バッファを作る
		this._bin_raster=new NyARBinRaster(scr_size.w,scr_size.h);
		//_detect_cb
		this._detect_cb=new DetectSquareCB(i_patt_inst,i_ref_code,i_ref_param,i_marker_width);
		this._next_marker=new IntRectStack(10);
		this._estimator=new TransMat2MarkerRect(i_ref_param);


		return;
	}
	/**
	 * i_imageにマーカー検出処理を実行し、結果を記録します。
	 * 
	 * @param i_raster
	 * マーカーを検出するイメージを指定します。イメージサイズは、カメラパラメータ
	 * と一致していなければなりません。
	 * @return マーカーが検出できたかを真偽値で返します。
	 * @throws NyARException
	 */
	public int detectMarkerLite(INyARRgbRaster i_raster) throws NyARException
	{
		//サイズチェック
		if(!this._bin_raster.getSize().isEqualSize(i_raster.getSize())){
			throw new NyARException();
		}

		//ラスタを２値イメージに変換する.
		this._tobin_filter.doFilter(i_raster,this._bin_raster);

		//コールバックハンドラの準備
		this._detect_cb.init(i_raster);
		//矩形を探す(戻り値はコールバック関数で受け取る。)
		this._square_detect.detectMarkerCB(this._bin_raster,_detect_cb);
		//時間を進める。
		this._detect_cb.table_operator.updateTick(this._detect_cb.table);
		
		//矩形位置の予想
		this._next_marker.clear();
		this._estimator.convert(this._detect_cb.table.selectAllItems(), this._next_marker);
		return -1;
	}
	public Object[] _probe()
	{
		Object[] ret=new Object[10];
		ret[0]=this._detect_cb.table;
		ret[1]=this._next_marker;
		ret[2]=this._detect_cb._estimate_position;
		return ret;
	}
}