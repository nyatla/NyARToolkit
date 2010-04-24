package jp.nyatla.nyartoolkit.dev.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattDeviationColorData;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattResult;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPatt_Color_WITHOUT_PCA;
import jp.nyatla.nyartoolkit.core.param.INyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.pickup.INyARColorPatt;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.INyARRasterFilter_Rgb2Bin;



public class MarkerTracking_3dTrans
{
	/**
	 * detectMarkerのコールバック関数
	 */
	private class DetectSquareCB implements NyARSquareContourDetector.IDetectMarkerCallback
	{
		//公開プロパティ
		public NyARSquare square=new NyARSquare();
		public final MarkerPositionTable table;
		public final MarkerTableOperator table_operator;
		public NextFrameMarkerStack _nextframe;
		private double _marker_width;
		
		//参照インスタンス
		private INyARRgbRaster _ref_raster;
		//所有インスタンス
		private INyARColorPatt _inst_patt;
		private NyARMatchPattDeviationColorData _deviation_data;
		private NyARMatchPatt_Color_WITHOUT_PCA _match_patt;
		private final NyARMatchPattResult __detectMarkerLite_mr=new NyARMatchPattResult();
		private NyARCoord2Linear _coordline;
		
		private NyARPerspectiveProjectionMatrix _prjmat;
		private INyARCameraDistortionFactor _dist;
		public DetectSquareCB(INyARColorPatt i_inst_patt,NyARCode i_ref_code,NyARParam i_param,double i_marker_width) throws NyARException
		{
			this.table_operator=new MarkerTableOperator(i_param);
			this.table=new MarkerPositionTable(10);
			this._nextframe=new NextFrameMarkerStack(10);
			this._marker_width=i_marker_width;

			
			this._prjmat=i_param.getPerspectiveProjectionMatrix();
			this._dist=i_param.getDistortionFactor();
			
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
			
			//マーカ中心を計算
			NyARDoublePoint2d new_center=this.__tmp_point;
			new_center.x=(vertex[0].x+vertex[1].x+vertex[2].x+vertex[3].x)/4;
			new_center.y=(vertex[0].y+vertex[1].y+vertex[2].y+vertex[3].y)/4;
			//近所のマーカを探す。[Optimize:重複計算あり]
			NextFrameMarkerStack.Item near_item=this.getNearItem(new_center);//[Optimize]見つけた矩形はリストから削除すべきだよね。
			if(near_item==null){
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
				MarkerPositionTable.Item item=this.table.selectItem(near_item.oid);
				//予想基準頂点に一番近い観測頂点インデクスを得る。
				int dir=getNearVertexIndex(near_item.vertex0,near_item.center,vertex,new_center);

				//一致率の高い矩形があれば、方位を考慮して頂点情報を作成
				NyARSquare sq=this.square;
				//directionを考慮して、squareを更新する。
				for(int i=0;i<4;i++){
					int idx=(i+dir) % 4;
					this._coordline.coord2Line(i_vertex_index[idx],i_vertex_index[(idx+1)%4],i_coordx,i_coordy,i_coor_num,sq.line[i]);
				}
				for (int i = 0; i < 4; i++) {
					//直線同士の交点計算
					if(!NyARLinear.crossPos(sq.line[i],sq.line[(i + 3) % 4],sq.sqvertex[i])){
						throw new NyARException();//ここのエラー復帰するならダブルバッファにすればOK
					}
				}
				this.table_operator.updateMarker(item,sq);
			}
		}
		/**
		 * 基準頂点i_baseと新規矩形情報i_sqを比較して、i_sqの適切なdirection値を計算する。
		 * (運動量で計算したら？)
		 * @param i_base_vertex
		 * @param i_base_center
		 * @param i_sq
		 * @param i_sqcenter
		 * @return
		 */
		private int getNearVertexIndex(NyARDoublePoint2d i_base_vertex,NyARDoublePoint2d i_base_center,NyARIntPoint2d[] i_sq,NyARDoublePoint2d i_sqcenter)
		{
			double bx,by;
			//観察座標の中央位置からのベクトル
			bx=i_base_vertex.x;
			by=i_base_vertex.y;

			//一番近い頂点を探す.
			double d=NyARMath.sqNorm(bx,by,i_sq[3].x,i_sq[3].y);
			int ret=3;
			for(int i=2;i>=0;i--)
			{
				//予測座標の中央位置からのベクトルの長さを計算
				double d2=NyARMath.sqNorm(bx,by,i_sq[i].x,i_sq[i].y);
				if(d2<d){
					d=d2;
					ret=i;
				}
			}
			return ret;
		}
		NyARDoubleMatrix33 _rot_temp=new NyARDoubleMatrix33();
		private NyARDoublePoint3d _pos3d_tmp=new NyARDoublePoint3d();
		private NyARDoublePoint3d _area_temp=new NyARDoublePoint3d();
		private NyARDoublePoint2d _pos2d_tmp=new NyARDoublePoint2d();
		
		public final void init(INyARRgbRaster i_raster)
		{			
			this._ref_raster=i_raster;
			//マーカの予測位置を計算しておく
			NextFrameMarkerStack stack=this._nextframe;
			NyARDoublePoint2d pos2d=this._pos2d_tmp;
			NyARDoublePoint3d pos3d=this._pos3d_tmp;
			
			NyARPerspectiveProjectionMatrix prjmat=this._prjmat;
			INyARCameraDistortionFactor dist=this._dist;
			MarkerPositionTable.Item[] items=this.table.selectAllItems();

			NyARDoubleMatrix33 rot=this._rot_temp;
			
			NyARDoublePoint3d area=this._area_temp;
			area.x=area.y=this._marker_width;
			stack.clear();
			for(int i=items.length-1;i>=0;i--)
			{
				if(items[i].is_empty){
					continue;
				}
				final NyARDoublePoint3d trans=items[i].trans;
				//pos2dに中心座標を計算
				NextFrameMarkerStack.Item item=stack.prePush();
				item.oid=i;
				prjmat.projectionConvert(trans,item.center);//[Optimaize!]
				dist.ideal2Observ(item.center, item.center);
				//方位決定のためにvertex0の計算[optimize! ここの計算関数すれば早くなる。]
				final NyARDoublePoint3d offset=items[i].offset.vertex[0];
				rot.setZXYAngle(items[i].angle);
				rot.transformVertex(offset,pos3d);
				prjmat.projectionConvert(trans.x+pos3d.x,trans.y+pos3d.y,trans.z+pos3d.z,item.vertex0);//[Optimaize!]
				dist.ideal2Observ(item.vertex0, item.vertex0);
				//探索範囲の計算
				area.z=trans.z;
				prjmat.projectionConvert(area,pos2d);//[Optimaize!]
				dist.ideal2Observ(pos2d,pos2d);
				item.dist=NyARMath.sqNorm(pos2d.x,pos2d.y,this._prjmat.m02,this._prjmat.m12)/2;
			}
			return;
		}
		
		
		
		/**
		 * 2次元空間の指定点の一番近くにあるアイテムを探します。
		 * @param i_table
		 * @param i_pos
		 * 探索点
		 * @param i_limit_max
		 * 探索範囲の最大値
		 * @return
		 */
		public NextFrameMarkerStack.Item getNearItem(NyARDoublePoint2d i_pos)
		{
			NextFrameMarkerStack.Item[] items=this._nextframe.getArray();
			
			double d=Double.MAX_VALUE;
			//エリア
			int index=-1;
			for(int i=this._nextframe.getLength()-1;i>=0;i--)
			{
				NyARDoublePoint2d center=items[i].center;
				double nd=NyARMath.sqNorm(i_pos, center);
				//有効範囲内？
				if(nd>items[i].dist){
					continue;
				}
				if(d>nd){
					d=nd;
					index=i;
				}
			}
			return index==-1?null:items[index];
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
		ret[2]=this._detect_cb._nextframe;
		return ret;
	}
}