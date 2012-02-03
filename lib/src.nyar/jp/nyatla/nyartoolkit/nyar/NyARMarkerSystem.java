package jp.nyatla.nyartoolkit.nyar;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.analyzer.histogram.INyARHistogramAnalyzer_Threshold;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattDeviationColorData;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattResult;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPatt_Color_WITHOUT_PCA;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.pickup.NyARColorPatt_Perspective;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.NyARBinRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.NyARPerspectiveCopyFactory;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector_Rle;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARRectOffset;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntCoordinates;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.nyidmarker.NyIdMarkerParam;
import jp.nyatla.nyartoolkit.nyidmarker.NyIdMarkerPattern;
import jp.nyatla.nyartoolkit.nyidmarker.NyIdMarkerPickup;
import jp.nyatla.nyartoolkit.nyidmarker.data.NyIdMarkerDataEncoder_RawBitId;
import jp.nyatla.nyartoolkit.nyidmarker.data.NyIdMarkerData_RawBitId;



public class NyARMarkerSystem
{
	private int lost_th=5;
	private RleDetector _rledetect;
	
	public NyARMarkerSystem(NyARParam i_ref_param) throws NyARException
	{
		this.createRasterDriver(i_ref_param);
	}
	protected void createRasterDriver(NyARParam i_ref_param) throws NyARException
	{
		this._rledetect=new RleDetector(i_ref_param);
		this._hist_th=null;
	}
	
	public int addNyIdMarker(int i_id,double i_marker_size) throws NyARException
	{
		MarkerInfoNyId target=new MarkerInfoNyId(i_id,i_id,i_marker_size);
		if(!this._rledetect._idmk_list.add(target)){
			throw new NyARException();
		}
		return (this._rledetect._idmk_list.size()-1);
	}
	public int addARMarker(InputStream i_stream,int i_patt_resolution,int i_patt_edge_percentage,double i_marker_size) throws NyARException
	{
		MarkerInfoARMarker target=new MarkerInfoARMarker(i_stream,i_patt_resolution,i_patt_edge_percentage,i_marker_size);
		if(!this._rledetect._armk_list.add(target)){
			throw new NyARException();
		}
		return (this._rledetect._armk_list.size()-1)|0x0001000;
	}
	
	/** マーカがあるか取得*/
	public boolean isExistMarker(int i_id)
	{
		if((i_id & 0x0001000)!=0){
			//ARマーカ
			return this._rledetect._armk_list.get(i_id &0x00000fff).lost_count<this.lost_th;
		}else{
			//Idマーカ
			return this._rledetect._idmk_list.get(i_id &0x00000fff).lost_count<this.lost_th;
		}
	}
	/** NyId取得*/
	public void getNyId(){}
	/** ARマーカの一致度*/
	public double getConfidence(int i_id) throws NyARException
	{
		if((i_id & 0x0001000)!=0){
			//ARマーカ
			return this._rledetect._armk_list.get(i_id &0x00000fff).cf;
		}
		//Idマーカ？
		throw new NyARException();
	}
	/** マーカ表面の画像取得*/
	public void getMarkerPlaneImage(){}
	/** マーカ表面の画像取得*/
	public void getMarkerPlaneImageRect(){}
	/** */
	public void getMarkerVertex2d(){}
	public NyARDoubleMatrix44 refMarkerTransMat(int i_id)
	{
		if((i_id & 0x0001000)!=0){
			//ARマーカ
			return this._rledetect._armk_list.get(i_id &0x00000fff).tmat;
		}else{
			//Idマーカ
			return this._rledetect._idmk_list.get(i_id &0x00000fff).tmat;
		}
	}
	/** スクリーン座標をマーカ座標に変換*/
//	public void screen2MarkerPos(){}
	/** マーカ座標をスクリーン座標に変換*/
//	public void screen2MarkerPos(){}
	
	/**
	 * ARマーカの一致敷居値を設定します。
	 * @param i_val
	 * 敷居値。0.0&lt;n&lt;1.0の値を指定すること。
	 */
	public void setConfidenceThreshold(double i_val)
	{
		this._rledetect._armk_list.setConficenceTh(i_val);
	}
	
	private long _time_stamp=-1;
	private INyARHistogramAnalyzer_Threshold _hist_th;
	/**
	 * 状況を更新する。
	 * @throws NyARException 
	 */
	public void update(NyARSensor i_sensor) throws NyARException
	{
		long time_stamp=i_sensor.getTimeStamp();
		//センサのタイムスタンプが変化していなければ何もしない。
		if(this._time_stamp==time_stamp){
			return;
		}
//		int th=this._hist_th.getThreshold(i_sensor.getGsHistogram());

		//解析器にかけてマーカを抽出。
		this._rledetect.detectMarker(i_sensor, time_stamp, 102);
		//タイムスタンプを更新
		this._time_stamp=time_stamp;
	}

}
//
//local classes
//




/**
 * 複数の解像度の比較画像を保持するクラス。
 */
class MultiResolutionPattPickup
{
	private class Item
	{
		private NyARColorPatt_Perspective _pickup;
		private NyARMatchPattDeviationColorData _patt_d;
		private int _patt_edge;
		public Item(int i_resolution,int i_edge_percentage) throws NyARException
		{
			int r=1;
			while(i_resolution*r<64){
				r*=2;
			}				
			this._pickup=new NyARColorPatt_Perspective(i_resolution,i_resolution,r,i_edge_percentage);
			this._patt_d=new NyARMatchPattDeviationColorData(i_resolution,i_resolution);
			this._patt_edge=i_edge_percentage;
		}
	}
	/**
	 * インスタンスのキャッシュ
	 */
	private ArrayList<Item> items=new ArrayList<Item>();
	/**
	 * マーカにマッチした{@link NyARMatchPattDeviationColorData}インスタンスを得る。
	 * @throws NyARException 
	 */
	
	public NyARMatchPattDeviationColorData refDeviationColorData(MarkerInfoARMarker i_marker,INyARRgbRaster i_raster, NyARIntPoint2d[] i_vertex) throws NyARException
	{
		int mk_resolution=i_marker.patt_resolution;
		int mk_edge=i_marker.patt_edge_percentage;
		for(int i=this.items.size()-1;i>=0;i--)
		{
			Item ptr=this.items.get(i);
			if(!ptr._pickup.getSize().isEqualSize(mk_resolution,mk_resolution) || ptr._patt_edge!=mk_edge)
			{
				//サイズとエッジサイズが合致しない物はスルー
				continue;
			}
			//古かったら更新
			ptr._pickup.pickFromRaster(i_raster,i_vertex);
			ptr._patt_d.setRaster(ptr._pickup);
			return ptr._patt_d;
		}
		//無い。新しく生成
		Item item=new Item(mk_resolution,mk_edge);
		//タイムスタンプの更新とデータの生成
		item._pickup.pickFromRaster(i_raster,i_vertex);
		item._patt_d.setRaster(item._pickup);
		this.items.add(item);
		return item._patt_d;
	}
	
}

//
// protected class
//

/**
 * {@link MultiMarker}向けの矩形検出器です。
 */
class RleDetector extends NyARSquareContourDetector_Rle
{
	public ARMarkerList _armk_list;
	
	public NyIdList _idmk_list;
	public INyARTransMat _transmat;
	private final NyARIntPoint2d[] _vertexs=new NyARIntPoint2d[4];
	private INyARRgbRaster _ref_input_rfb;
	private INyARGrayscaleRaster _ref_input_gs;
	public RleDetector(NyARParam i_param) throws NyARException
	{
		super(i_param.getScreenSize());
		NyARCoord2Linear coordliner=new NyARCoord2Linear(i_param.getScreenSize(),i_param.getDistortionFactor());
		this._armk_list=new ARMarkerList(coordliner);
		this._idmk_list=new NyIdList(coordliner);
		this._transmat=new NyARTransMat(i_param);
	}
	protected void onSquareDetect(NyARIntCoordinates i_coord,int[] i_vertex_index) throws NyARException
	{
		//画像取得配列の生成
		for(int i2=0;i2<4;i2++){
			this._vertexs[i2]=i_coord.items[i_vertex_index[i2]];
		}
		//nyIdマーカの特定(IDマーカの特定はここで完結する。)
		if(this._idmk_list.size()>0){
			if(this._idmk_list.update(this._ref_input_gs,i_coord, i_vertex_index, this._vertexs)){
				return;//idマーカを特定
			}
		}
		//ARマーカの特定
		if(this._armk_list.size()>0){
			if(this._armk_list.update(this._ref_input_rfb,i_coord, i_vertex_index,this._vertexs)){
				return;
			}
		}
		//他のタイプはここで。
	}
	
	public void detectMarker(NyARSensor i_sensor,long i_time_stamp,int i_th) throws NyARException
	{
		//準備
		for(int i=this._idmk_list.size()-1;i>=0;i--){
			MarkerInfoNyId target=this._idmk_list.get(i);
			if(target.lost_count<Integer.MAX_VALUE){
				target.lost_count++;
			}
		}
		this._idmk_list.prepare();
		this._armk_list.prepare();
		//検出処理
		this._ref_input_rfb=i_sensor.refSourceImage();
		this._ref_input_gs=i_sensor.refGsImage();
		super.detectMarker(this._ref_input_gs,i_th);
		//検出結果の反映処理
		this._armk_list.finish();

		//各ターゲットの更新
		if(true){
			for(int i=this._armk_list.size()-1;i>=0;i--){
				MarkerInfoARMarker target=this._armk_list.get(i);
				if(target.lost_count==0){
					target.time_stamp=i_time_stamp;
					this._transmat.transMat(target.sq,target.marker_offset,target.tmat);
				}
			}
			for(int i=this._idmk_list.size()-1;i>=0;i--){
				MarkerInfoNyId target=this._idmk_list.get(i);
				if(target.lost_count==0){
					target.time_stamp=i_time_stamp;
					this._transmat.transMat(target.sq,target.marker_offset,target.tmat);
				}
			}
		}else{
			for(int i=this._armk_list.size()-1;i>=0;i--){
				MarkerInfoARMarker target=this._armk_list.get(i);
				if(target.lost_count==0){
					target.time_stamp=i_time_stamp;
					this._transmat.transMatContinue(target.sq,target.marker_offset,target.tmat,target.tmat);
				}
			}
			for(int i=this._idmk_list.size()-1;i>=0;i--){
				MarkerInfoNyId target=this._idmk_list.get(i);
				if(target.lost_count==0){
					target.time_stamp=i_time_stamp;
					this._transmat.transMatContinue(target.sq,target.marker_offset,target.tmat,target.tmat);
				}
			}
		}
	}

}

class ARMarkerList extends ArrayList<MarkerInfoARMarker>
{
	private final static int INITIAL_MARKER_STACK_SIZE=100;
	private NyARCoord2Linear _ref_coordline;		
	private double _configense_th;
	private final NyARMatchPattResult _patt_result=new NyARMatchPattResult();;
	private final MultiResolutionPattPickup _mpickup=new MultiResolutionPattPickup();
	private ARMarkerMap _mkmap;
	private SquareStack _sq_stack;
	public ARMarkerList(NyARCoord2Linear i_ref_coodline) throws NyARException
	{
		this._ref_coordline=i_ref_coodline;
		//同時に判定待ちにできる矩形の数
		this._sq_stack=new SquareStack(INITIAL_MARKER_STACK_SIZE);
		this._mkmap=new ARMarkerMap(1);//初期値1マーカ
		return;
	}

	/**
	 * マーカごとの一致率のマッピングテーブル
	 */
	private class ARMarkerMap
	{
		/**
		 * 指定個数のリンクリストを生成。
		 * @param i_num_of_item
		 */
		public ARMarkerMap(int i_num_of_item)
		{
			this._llitems=new LLItem();
			LLItem ptr=this._llitems;
			for(int i=1;i<i_num_of_item;i++){
				LLItem n=new LLItem();
				ptr.next=n;
				n.prev=ptr;
				ptr=n;
			}
			ptr.next=this._llitems;
			this._llitems.prev=ptr;
			this._num_of_llitem=i_num_of_item;
		}
		public class LLItem{
			int id;
			double cf;
			int dir;
			LLItem prev;
			LLItem next;
			NyARSquare ref_sq;
		};
		private LLItem _llitems;
		private int _num_of_llitem;
		/**
		 * 降順リストへ、最大_num_of_llitem個のアイテムを登録する。
		 * @param i_id
		 * @param i_cf
		 * @param i_dir
		 * @return
		 */
		public LLItem add(int i_id,double i_cf,int i_dir)
		{
			LLItem ptr=_llitems;
			//先頭の場合
			if(ptr.cf<i_cf){
				ptr.cf=i_cf;
				ptr.dir=i_dir;
				ptr.id=i_id;
				this._llitems=ptr.prev;
				return ptr;
			}
			//それ以降
			ptr=ptr.next;
			for(int i=this._num_of_llitem-2;i>=0;i--)
			{
				if(ptr.cf<i_cf){
					//最後尾を切り離す。
					LLItem n=this._llitems.prev;
					this._llitems.prev=n.prev;
					n.prev.next=this._llitems.prev;
					//現在の場所に切り離した要素を挿入
					n.next=ptr;
					n.prev=ptr.prev;
					ptr.prev=n;
					n.prev.next=n;
					//nに値を保存
					n.cf=i_cf;
					n.dir=i_dir;
					n.id=i_id;
					return ptr;
				}
				ptr=ptr.next;
			}
			return null;
		}
		public void reset()
		{
			LLItem ptr=this._llitems;
			for(int i=this._num_of_llitem-1;i>=0;i--)
			{
				ptr.cf=0;
				ptr.id=-1;
			}
			
		}
		/**
		 * リストから最も高い一致率のアイテムを取得する。
		 */
		public LLItem getTopItem()
		{
			LLItem ptr=this._llitems;
			for(int i=this._num_of_llitem-1;i>=0;i--)
			{
				if(ptr.id<0){
					ptr=ptr.next;
					continue;
				}
				return ptr;
			}
			return null;
		}
		/**
		 * リスト中の、i_itemと同じマーカIDか、同じ矩形情報を参照しているものを無効に(ptr.idを-1)する。
		 */
		public void disableSameItem(LLItem i_item)
		{
			LLItem ptr=this._llitems;
			for(int i=this._num_of_llitem-1;i>=0;i--)
			{
				if(ptr.id<0){
				}else if(ptr.id==i_item.id){
					ptr.id=-1;
				}else if(ptr.ref_sq==i_item.ref_sq){
					ptr.id=-1;
				}
				ptr=ptr.next;
			}
		}
		
	}
	private class SquareStack extends NyARObjectStack<NyARSquare>
	{
		public SquareStack(int i_length) throws NyARException
		{
			super.initInstance(i_length,NyARSquare.class);
		}
		protected NyARSquare createElement() throws NyARException
		{
			return new NyARSquare();
		}		
	}
	/**
	 * マーカの一致敷居値を設定する。
	 */
	public void setConficenceTh(double i_th)
	{
		this._configense_th=i_th;
	}
	/**
	 * o_targetsに、敷居値を越えたターゲットリストを返却する。
	 * @param i_raster
	 * @param i_vertex
	 * @param o_targets
	 * @return
	 * @throws NyARException 
	 */
	public boolean update(INyARRgbRaster i_raster,NyARIntCoordinates i_coord,int[] i_vertex_index,NyARIntPoint2d[] i_vertex) throws NyARException
	{
		//このパターンに最も一致するARマーカを探す
		NyARSquare sq_tmp=null;
		for(int i=this.size()-1;i>=0;i--){
			MarkerInfoARMarker target=this.get(i);
			//解像度に一致する画像を取得
			NyARMatchPattDeviationColorData diff=this._mpickup.refDeviationColorData(target, i_raster, i_vertex);
			//マーカのパターン解像度に一致したサンプリング画像と比較する。
			if(!target.matchpatt.evaluate(diff,this._patt_result)){
				continue;
			}
			//敷居値をチェック
			if(this._patt_result.confidence<this._configense_th)
			{
				continue;
			}
			//マーカマップへ登録を試行
			ARMarkerMap.LLItem llitem=this._mkmap.add(i,this._patt_result.confidence,this._patt_result.direction);
			if(llitem==null){
				//出来なければ何もしない。
				continue;
			}
			//必要に応じて矩形情報を生成。
			if(sq_tmp==null){
				//最大数を超えるとにんしきしないお
				sq_tmp=this._sq_stack.prePush();
				if(sq_tmp==null){
					continue;
				}
				for(int i2=0;i2<4;i2++){
					this._ref_coordline.coord2Line(i_vertex_index[i2],i_vertex_index[(i2+1)%4],i_coord,sq_tmp.line[i2]);
				}
				for (int i2 = 0; i2 < 4; i2++) {
					//直線同士の交点計算
					if(!sq_tmp.line[i2].crossPos(sq_tmp.line[(i2 + 3) % 4],sq_tmp.sqvertex[i2])){
						throw new NyARException();//まずない。ありえない。
					}
				}
			}
			//マーカマップアイテムの矩形に参照値を設定する。
			llitem.ref_sq=sq_tmp;
		}
		return sq_tmp!=null;
	}		
	/**
	 * @param i_num_of_markers
	 * マーカの個数
	 */
	public void prepare()
	{
		//ARマーカのマッチテーブルのサイズを調整
		if(this._mkmap._num_of_llitem<this.size()){
			//不足してるなら作っておく。
			this._mkmap=new ARMarkerMap(this.size());
		}
		//マッチングテーブルをリセット
		this._mkmap.reset();
		//必要ならスタックサイズの調整もやってね。
		
		//検出のために初期値設定
		for(int i=this.size()-1;i>=0;i--){
			MarkerInfoARMarker target=this.get(i);
			if(target.lost_count<Integer.MAX_VALUE){
				target.lost_count++;
			}
			//検出に利用する一致率のリセット
			target.cf=0;
		}			
	}
	public void finish()
	{
		//一致率の最も高いアイテムを得る。
		ARMarkerMap.LLItem top_item=this._mkmap.getTopItem();
		//アイテムを検出できなくなるまで、一致率が高い順にアイテムを得る。
		while(top_item!=null){
			//検出したアイテムのARmarkerIndexのデータをセット
			MarkerInfoARMarker target=this.get(top_item.id);
			target.cf=top_item.cf;
			target.lost_count=0;
			target.sq=top_item.ref_sq;
			target.sq.rotateVertexL(4-top_item.dir);
			//基準アイテムと重複するアイテムを削除する。
			this._mkmap.disableSameItem(top_item);
			top_item=this._mkmap.getTopItem();
		}
	}
}
class NyIdList extends ArrayList<MarkerInfoNyId>
{
	private static final long serialVersionUID = -6446466460932931830L;
	/**輪郭推定器*/
	private NyARCoord2Linear _ref_coordline;
	private NyIdMarkerPickup _id_pickup;
	private final NyIdMarkerPattern _id_patt=new NyIdMarkerPattern();
	private final NyIdMarkerParam _id_param=new NyIdMarkerParam();
	private final NyIdMarkerDataEncoder_RawBitId _id_encoder=new NyIdMarkerDataEncoder_RawBitId();
	private final NyIdMarkerData_RawBitId _id_data=new NyIdMarkerData_RawBitId();
	public NyIdList(NyARCoord2Linear i_ref_coordline) throws NyARException
	{
		this._ref_coordline=i_ref_coordline;
		this.initResource();
	}
	protected void initResource() throws NyARException
	{
		this._id_pickup = new NyIdMarkerPickup();
	}
	public void prepare()
	{
		for(int i=this.size()-1;i>=0;i--){
			MarkerInfoNyId target=this.get(i);
			if(target.lost_count<Integer.MAX_VALUE){
				target.lost_count++;
			}
		}		
	}
	public boolean update(INyARGrayscaleRaster i_raster,NyARIntCoordinates i_coord,int[] i_vertex_index,NyARIntPoint2d[] i_vertex) throws NyARException
	{
		if(!this._id_pickup.pickFromRaster(i_raster,i_vertex, this._id_patt, this._id_param))
		{
			return false;
		}
		if(!this._id_encoder.encode(this._id_patt,this._id_data)){
			return false;
		}
		//IDを検出
		long s=this._id_data.marker_id;
		for(int i=this.size()-1;i>=0;i--){
			MarkerInfoNyId target=this.get(i);
			if(target.nyid_range_s>s || s>target.nyid_range_e)
			{
				continue;
			}
			//既に認識済なら無視
			if(target.lost_count==0){
				continue;
			}
			//一致したよー。
			target.nyid=s;
			target.lost_count=0;
			int dir=this._id_param.direction;
			NyARSquare sq=target.sq;
			for(int i2=0;i2<4;i2++){
				int idx=(i2+4 - dir) % 4;
				this._ref_coordline.coord2Line(i_vertex_index[idx],i_vertex_index[(idx+1)%4],i_coord,sq.line[i2]);
			}
			for (int i2 = 0; i2 < 4; i2++) {
				//直線同士の交点計算
				if(!sq.line[i2].crossPos(sq.line[(i2 + 3) % 4],sq.sqvertex[i2])){
					throw new NyARException();//まずない。ありえない。
				}
			}
			return true;
		}
		return false;
	}
}


/**
 * マーカ情報を格納するためのクラスです。
 */
class TMarkerData
{
	/** 最後に認識したタイムスタンプ。*/
	public long time_stamp;
	/** ライフ値*/
	public long life;
	/** MK情報。マーカのオフセット位置。*/
	public final NyARRectOffset marker_offset=new NyARRectOffset();			
	/** 検出した矩形の格納変数。理想形二次元座標を格納します。*/
	public NyARSquare sq;
	/** 検出した矩形の格納変数。マーカの姿勢行列を格納します。*/
	public final NyARTransMatResult tmat=new NyARTransMatResult();
	/** 矩形の検出状態の格納変数。 連続して見失った回数を格納します。*/
	public int lost_count=Integer.MAX_VALUE;	
}	

/**
 * sqメンバは、参照です。
 *
 */
class MarkerInfoARMarker extends TMarkerData
{
	/** MK_ARの情報。比較のための、ARToolKitマーカを格納します。*/
	public final NyARMatchPatt_Color_WITHOUT_PCA matchpatt;
	/** MK_ARの情報。検出した矩形の格納変数。マーカの一致度を格納します。*/
	public double cf;
	/** MK_ARの情報。パターンの解像度。*/
	public final int patt_resolution;
	/** MK_ARの情報。パターンのエッジ割合。*/
	public final int patt_edge_percentage;
	/** */
	public MarkerInfoARMarker(InputStream i_patt,int i_patt_resolution,int i_patt_edge_percentage,double i_patt_size) throws NyARException
	{
		NyARCode c=new NyARCode(i_patt_resolution,i_patt_resolution);
		c.loadARPatt(i_patt);
		this.matchpatt=new NyARMatchPatt_Color_WITHOUT_PCA(c);
		this.patt_resolution=i_patt_resolution;
		this.patt_edge_percentage=i_patt_edge_percentage;
		this.marker_offset.setSquare(i_patt_size);
		return;
	}		
}
class MarkerInfoNyId extends TMarkerData
{
	/** MK_NyIdの情報。 反応するidの開始レンジ*/
	public final long nyid_range_s;
	/** MK_NyIdの情報。 反応するidの終了レンジ*/
	public final long nyid_range_e;
	/** MK_NyIdの情報。 実際のid値*/
	public long nyid;
	/**
	 * コンストラクタです。初期値から、Idマーカのインスタンスを生成します。
	 * @param i_range_s
	 * @param i_range_e
	 * @param i_patt_size
	 * @throws NyARException
	 */
	public MarkerInfoNyId(int i_nyid_range_s,int i_nyid_range_e,double i_patt_size)
	{
		this.sq=new NyARSquare();
		this.marker_offset.setSquare(i_patt_size);
		this.nyid_range_s=i_nyid_range_s;
		this.nyid_range_e=i_nyid_range_e;
		return;
	}		
}