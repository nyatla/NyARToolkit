package jp.nyatla.nyartoolkit.nyar;

import java.io.InputStream;
import java.util.ArrayList;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.analyzer.histogram.INyARHistogramAnalyzer_Threshold;
import jp.nyatla.nyartoolkit.core.analyzer.histogram.NyARHistogramAnalyzer_SlidePTile;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattDeviationColorData;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattResult;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPatt_Color_WITHOUT_PCA;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.pickup.NyARColorPatt_Perspective;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector_Rle;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARRectOffset;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.NyARIntCoordinates;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.nyidmarker.NyIdMarkerParam;
import jp.nyatla.nyartoolkit.nyidmarker.NyIdMarkerPattern;
import jp.nyatla.nyartoolkit.nyidmarker.NyIdMarkerPickup;
import jp.nyatla.nyartoolkit.nyidmarker.data.NyIdMarkerDataEncoder_RawBitId;
import jp.nyatla.nyartoolkit.nyidmarker.data.NyIdMarkerData_RawBitId;



public class NyARMarkerSystem
{
	private static int MASK_IDTYPE=0xfffff000;
	private static int MASK_IDNUM =0x00000fff;
	private static int IDTYPE_ARTK=0x00000000;
	private static int IDTYPE_NYID=0x00001000;
	private int lost_th=5;
	private RleDetector _rledetect;
	protected NyARParam _ref_param;
	
	public NyARMarkerSystem(NyARParam i_ref_param) throws NyARException
	{
		this._ref_param=i_ref_param;
		this.createRasterDriver(i_ref_param);
	}
	protected void createRasterDriver(NyARParam i_ref_param) throws NyARException
	{
		this._rledetect=new RleDetector(i_ref_param);
		this._hist_th=new NyARHistogramAnalyzer_SlidePTile(15);
	}
	
	public int addNyIdMarker(int i_id,double i_marker_size) throws NyARException
	{
		MarkerInfoNyId target=new MarkerInfoNyId(i_id,i_id,i_marker_size);
		if(!this._rledetect._idmk_list.add(target)){
			throw new NyARException();
		}
		return (this._rledetect._idmk_list.size()-1);
	}
	public int addARMarker(NyARCode i_code,int i_patt_edge_percentage,double i_marker_size) throws NyARException
	{
		MarkerInfoARMarker target=new MarkerInfoARMarker(i_code,i_patt_edge_percentage,i_marker_size);
		if(!this._rledetect._armk_list.add(target)){
			throw new NyARException();
		}
		return (this._rledetect._armk_list.size()-1)| IDTYPE_ARTK;
	}
	
	public int addARMarker(InputStream i_stream,int i_patt_resolution,int i_patt_edge_percentage,double i_marker_size) throws NyARException
	{
		NyARCode c=new NyARCode(i_patt_resolution,i_patt_resolution);
		c.loadARPatt(i_stream);
		return this.addARMarker(c, i_patt_edge_percentage, i_marker_size);
	}
	public int addARMarker(String i_file_name,int i_patt_resolution,int i_patt_edge_percentage,double i_marker_size) throws NyARException
	{
		NyARCode c=new NyARCode(i_patt_resolution,i_patt_resolution);
		c.loadARPattFromFile(i_file_name);
		return this.addARMarker(c,i_patt_edge_percentage, i_marker_size);
	}
	
	/** マーカがあるか取得*/
	public boolean isExistMarker(int i_id)
	{
		if((i_id & MASK_IDTYPE)==IDTYPE_ARTK){
			//ARマーカ
			return this._rledetect._armk_list.get(i_id & MASK_IDNUM).lost_count<this.lost_th;
		}else{
			//Idマーカ
			return this._rledetect._idmk_list.get(i_id & MASK_IDNUM).lost_count<this.lost_th;
		}
	}
	/** NyId取得*/
	public void getNyId(){}
	/** ARマーカの一致度*/
	public double getConfidence(int i_id) throws NyARException
	{
		if((i_id & MASK_IDTYPE)==IDTYPE_ARTK){
			//ARマーカ
			return this._rledetect._armk_list.get(i_id &MASK_IDNUM).cf;
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
	public NyARDoubleMatrix44 getMarkerTransMat(int i_id)
	{
		if((i_id & MASK_IDTYPE)==IDTYPE_ARTK){
			//ARマーカ
			return this._rledetect._armk_list.get(i_id &MASK_IDNUM).tmat;
		}else{
			//Idマーカ
			return this._rledetect._idmk_list.get(i_id &MASK_IDNUM).tmat;
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
		int th=this._hist_th.getThreshold(i_sensor.getGsHistogram());

		//解析器にかけてマーカを抽出。
		this._rledetect.detectMarker(i_sensor, time_stamp, th);
		//タイムスタンプを更新
		this._time_stamp=time_stamp;
	}

}
//
//local classes
//




/**
 * このクラスは、複数の異なる解像度の比較画像を保持します。
 */
class MultiResolutionPattProvider
{
	private class Item
	{
		private NyARColorPatt_Perspective _pickup;
		private NyARMatchPattDeviationColorData _patt_d;
		private int _patt_edge;
		public Item(int i_patt_w,int i_patt_h,int i_edge_percentage) throws NyARException
		{
			int r=1;
			//解像度は幅を基準にする。
			while(i_patt_w*r<64){
				r*=2;
			}				
			this._pickup=new NyARColorPatt_Perspective(i_patt_w,i_patt_h,r,i_edge_percentage);
			this._patt_d=new NyARMatchPattDeviationColorData(i_patt_w,i_patt_h);
			this._patt_edge=i_edge_percentage;
		}
	}
	/**
	 * インスタンスのキャッシュ
	 */
	private ArrayList<Item> items=new ArrayList<Item>();
	/**
	 * [readonly]マーカにマッチした{@link NyARMatchPattDeviationColorData}インスタンスを得る。
	 * @throws NyARException 
	 */
	public NyARMatchPattDeviationColorData getDeviationColorData(MarkerInfoARMarker i_marker,INyARRgbRaster i_raster, NyARIntPoint2d[] i_vertex) throws NyARException
	{
		int mk_edge=i_marker.patt_edge_percentage;
		for(int i=this.items.size()-1;i>=0;i--)
		{
			Item ptr=this.items.get(i);
			if(!ptr._pickup.getSize().isEqualSize(i_marker.patt_w,i_marker.patt_h) || ptr._patt_edge!=mk_edge)
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
		Item item=new Item(i_marker.patt_w,i_marker.patt_h,mk_edge);
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
 * 頂点マッピング用のテーブル
 */
abstract class SortLinkedList<T extends SortLinkedList.Item>
{
	static class Item{
		Item next;
		Item prev;
	};	
	protected abstract T createElement();
	protected int _num_of_llitem;
	protected T _llitems;
	/**
	 * 指定個数のリンクリストを生成。
	 * @param i_num_of_item
	 */
	public SortLinkedList(int i_num_of_item)
	{
		this._llitems=this.createElement();
		T ptr=this._llitems;
		for(int i=1;i<i_num_of_item;i++){
			T n=this.createElement();
			ptr.next=n;
			n.prev=ptr;
			ptr=n;
		}
		ptr.next=this._llitems;
		this._llitems.prev=ptr;
		this._num_of_llitem=i_num_of_item;
	}

	/**
	 * 最後尾のリストを削除して、リストのi_itemの直前に要素を追加する。
	 * @param i_id
	 * @param i_cf
	 * @param i_dir
	 * @return
	 */
	public T insertFromTailBefore(T i_item)
	{
		T ptr=this._llitems;
		//先頭の場合
		if(ptr==i_item){
			//リストを後方にシフトする。
			ptr=(T) ptr.prev;
			this._llitems=(T)ptr;
			return this._llitems;
		}
		//最後尾なら、そのまま返す
		if(i_item==this._llitems.prev){
			return i_item;
		}
		//最後尾切り離し
		T n=(T) this._llitems.prev;
		n.prev.next=this._llitems;
		this._llitems.prev=n.prev;
		
		n.next=i_item;
		n.prev=i_item.prev;
		i_item.prev=n;
		n.prev.next=n;
		return n;
	}
}

class VertexSortList extends SortLinkedList<VertexSortList.Item>
{
	static class Item extends SortLinkedList.Item
	{
		int sq_dist;
		int mk_id;
	};
	public VertexSortList(int iNumOfItem)
	{
		super(iNumOfItem);
	}
	final protected Item createElement()
	{
		return new Item();
	}
	public void reset()
	{
		Item ptr=this._llitems;
		for(int i=this._num_of_llitem-1;i>=0;i--)
		{
			ptr.sq_dist=Integer.MAX_VALUE;
			ptr=(Item) ptr.next;
		}
	}
	/**
	 * 挿入ポイントを返す。挿入ポイントは、i_sd_point(距離点数)が
	 * 登録済のポイントより小さい場合のみ返却する。
	 * @return
	 */
	public Item getInsertPoint(int i_sd_point)
	{
		Item ptr=_llitems;
		//先頭の場合
		if(ptr.sq_dist>i_sd_point){
			return ptr;
		}
		//それ以降
		ptr=(Item) ptr.next;
		for(int i=this._num_of_llitem-2;i>=0;i--)
		{
			if(ptr.sq_dist>i_sd_point){
				return ptr;
			}
			ptr=(Item) ptr.next;
		}
		//対象外。
		return null;		
	}
}
class ARMarkerSortList extends SortLinkedList<ARMarkerSortList.LLItem>
{
	public class LLItem extends SortLinkedList.Item{
		int id;
		double cf;
		int dir;
		SquareStack.Item ref_sq;
	};
	/**
	 * 指定個数のリンクリストを生成。
	 * @param i_num_of_item
	 */
	public ARMarkerSortList(int i_num_of_item)
	{
		super(i_num_of_item);
	}
	protected LLItem createElement()
	{
		return new LLItem();
	}
	/**
	 * 挿入ポイントを返す。挿入ポイントは、i_sd_point(距離点数)が
	 * 登録済のポイントより小さい場合のみ返却する。
	 * @return
	 */
	public LLItem getInsertPoint(double i_cf)
	{
		LLItem ptr=_llitems;
		//先頭の場合
		if(ptr.cf<i_cf){
			return ptr;
		}
		//それ以降
		ptr=(LLItem) ptr.next;
		for(int i=this._num_of_llitem-2;i>=0;i--)
		{
			if(ptr.cf<i_cf){
				return ptr;
			}
			ptr=(LLItem) ptr.next;
		}
		//対象外。
		return null;		
	}		
	public void reset()
	{
		LLItem ptr=this._llitems;
		for(int i=this._num_of_llitem-1;i>=0;i--)
		{
			ptr.cf=0;
			ptr.id=-1;
			ptr.ref_sq=null;
			ptr=(LLItem) ptr.next;
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
				ptr=(LLItem) ptr.next;
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
			ptr=(LLItem) ptr.next;
		}
	}
}
	


/**
 * {@link MultiMarker}向けの矩形検出器です。
 */
class RleDetector extends NyARSquareContourDetector_Rle
{
	private final static int INITIAL_MARKER_STACK_SIZE=10;
	private NyARCoord2Linear _coordline;		
	
	private SquareStack _sq_stack;
	
	public ARMarkerList _armk_list;
	
	public NyIdList _idmk_list;
	public INyARTransMat _transmat;
	private INyARRgbRaster _ref_input_rfb;
	private INyARGrayscaleRaster _ref_input_gs;
	public RleDetector(NyARParam i_param) throws NyARException
	{
		super(i_param.getScreenSize());
		this._coordline=new NyARCoord2Linear(i_param.getScreenSize(),i_param.getDistortionFactor());
		this._armk_list=new ARMarkerList();
		this._idmk_list=new NyIdList(this._coordline);
		this._transmat=new NyARTransMat(i_param);
		//同時に判定待ちにできる矩形の数
		this._sq_stack=new SquareStack(INITIAL_MARKER_STACK_SIZE);
	}
	class VertexComp
	{
		public int sqdest;
		public int shift_l;
	    /**
	     * この関数は、頂点セット同士のシフト量を計算して、インスタンスに結果をセットします。
	     * 並びが同じである頂点セット同士の最低の移動量を計算して、その時のシフト量と二乗移動量の合計を返します。
	     * @param i_square
	     * 比較対象の矩形
	     * @return
	     * {@link #shift_l}にシフト量を返します。
	     * {@link #sqdest}に頂点移動距離の合計の二乗値を返します。
	     * シフト量はthis-i_squareです。1の場合、i_v1[0]とi_v2[1]が対応点になる(shift量1)であることを示します。
	     */
	    public void compareVertexSet(NyARIntPoint2d[] i_v1,NyARIntPoint2d[] i_v2)
	    {
	    	//3-0番目
	    	int min_dist=Integer.MAX_VALUE;
	    	int min_index=0;
	    	int xd,yd;
	    	for(int i=3;i>=0;i--){
	    		int d=0;
	    		for(int i2=3;i2>=0;i2--){
	    			xd= (int)(i_v1[i2].x-i_v2[(i2+i)%4].x);
	    			yd= (int)(i_v1[i2].y-i_v2[(i2+i)%4].y);
	    			d+=xd*xd+yd*yd;
	    		}
	    		if(min_dist>d){
	    			min_dist=d;
	    			min_index=i;
	    		}
	    	}
	    	this.shift_l=min_index;
	    	this.sqdest=min_dist;
	    }	
		
	}
	
	
	private void tracking(SquareStack.Item i_new_sq)
	{
		int new_area=i_new_sq.rect_area;
		int new_sq_dist=i_new_sq.vertex_area.getDiagonalSqDist();
		for(int i=0;i<this._armk_list.size();i++)
		{
			TMarkerData target=this._armk_list.get(i);
//			int cd=target.tl_center.sqDist(i_new_sq.center2d);
			//面積比が急激0.8-1.2倍以外の変動なら無視
			int a_rate=new_area*100/target.tl_rect_area;
			if(a_rate<80 || 120<a_rate){
				continue;
			}
			//移動距離の二乗が対角線距離の二乗の1/(2^2)以上なら無視
			int sq_move=target.tl_center.sqDist(i_new_sq.center2d);
			if(sq_move*10/new_sq_dist>4){
				continue;
			}
			//頂点移動距離の合計を計算
			VertexComp vc=new VertexComp();
			vc.compareVertexSet(i_new_sq.ob_vertex,target.tl_vertex);
			//頂点移動距離の合計が、中心点移動距離の8倍を超えてたらNG
			if(vc.sqdest>sq_move*8){
				continue;
			}
			//対象。距離評価数をマーカidと共に登録。
		}
	}
	protected void onSquareDetect(NyARIntCoordinates i_coord,int[] i_vertex_index) throws NyARException
	{
		//とりあえずSquareスタックを予約
		SquareStack.Item sq_tmp=this._sq_stack.prePush();
		//観測座標点の記録
		for(int i2=0;i2<4;i2++){
			sq_tmp.ob_vertex[i2].setValue(i_coord.items[i_vertex_index[i2]]);
		}
		//頂点分布を計算
		sq_tmp.vertex_area.setAreaRect(sq_tmp.ob_vertex,4);
		//頂点座標の中心を計算
		sq_tmp.center2d.setCenterPos(sq_tmp.ob_vertex,4);
		//矩形面積
		sq_tmp.rect_area=sq_tmp.vertex_area.w*sq_tmp.vertex_area.h;

		boolean is_target_marker=false;
		for(;;){
			//現在検出中のマーカと同じような場所にあるか確認
	//		tracking(sq_tmp);
	//		//nyIdマーカの特定(IDマーカの特定はここで完結する。)
	//		if(this._idmk_list.size()>0){
	//			if(this._idmk_list.update(this._ref_input_gs,i_coord, i_vertex_index, this._vertexs)){
	//				return;//idマーカを特定
	//			}
	//		}
			//ARマーカの特定
			if(this._armk_list.size()>0){
				if(this._armk_list.update(this._ref_input_rfb,sq_tmp)){
					is_target_marker=true;
					break;
				}
			}
			break;
		}
		//この矩形が検出対象なら、矩形情報を精密に再計算
		if(is_target_marker){
			//矩形は検出対象にマークされている。
			for(int i2=0;i2<4;i2++){
				this._coordline.coord2Line(i_vertex_index[i2],i_vertex_index[(i2+1)%4],i_coord,sq_tmp.line[i2]);
			}
			for (int i2 = 0; i2 < 4; i2++) {
				//直線同士の交点計算
				if(!sq_tmp.line[i2].crossPos(sq_tmp.line[(i2 + 3) % 4],sq_tmp.sqvertex[i2])){
					throw new NyARException();//まずない。ありえない。
				}
			}
		}else{
			//この矩形は検出対象にマークされなかったので、解除
			this._sq_stack.pop();
		}
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
		this._sq_stack.clear();//矩形情報の保持スタック初期化
		
		this._idmk_list.prepare();
		this._armk_list.prepare();
		//検出処理
		this._ref_input_rfb=i_sensor.getSourceImage();
		this._ref_input_gs=i_sensor.getGsImage();
		super.detectMarker(this._ref_input_gs,i_th);
		//検出結果の反映処理
		this._armk_list.finish();

		//各ターゲットの更新
		if(true){
			for(int i=this._armk_list.size()-1;i>=0;i--){
				MarkerInfoARMarker target=this._armk_list.get(i);
				if(target.lost_count==0){
					target.time_stamp=i_time_stamp;
					System.out.print(target.tmat.m00);
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

class SquareStack extends NyARObjectStack<SquareStack.Item>
{
	public class Item extends NyARSquare
	{
		NyARIntPoint2d center2d=new NyARIntPoint2d();
		/** 検出座標系の値*/
		NyARIntPoint2d[] ob_vertex=NyARIntPoint2d.createArray(4);
		/** 頂点の分布範囲*/
		NyARIntRect vertex_area=new NyARIntRect();
		/** rectの面積*/
		int rect_area;
	}
	public SquareStack(int i_length) throws NyARException
	{
		super.initInstance(i_length,SquareStack.Item.class);
	}
	protected SquareStack.Item createElement() throws NyARException
	{
		return new SquareStack.Item();
	}		
}
class ARMarkerList extends ArrayList<MarkerInfoARMarker>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double _configense_th;
	private final NyARMatchPattResult _patt_result=new NyARMatchPattResult();;
	private final MultiResolutionPattProvider _mpickup=new MultiResolutionPattProvider();
	private ARMarkerSortList _mkmap;
	public ARMarkerList() throws NyARException
	{
		this._mkmap=new ARMarkerSortList(1);//初期値1マーカ
		return;
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
	public boolean update(INyARRgbRaster i_raster,SquareStack.Item i_sq) throws NyARException
	{
		//sq_tmpに値を生成したかのフラグ
		boolean is_ganalated_sq=false;
		for(int i=this.size()-1;i>=0;i--){
			MarkerInfoARMarker target=this.get(i);
			//解像度に一致する画像を取得
			NyARMatchPattDeviationColorData diff=this._mpickup.getDeviationColorData(target, i_raster,i_sq.ob_vertex);
			//マーカのパターン解像度に一致したサンプリング画像と比較する。
			if(!target.matchpatt.evaluate(diff,this._patt_result)){
				continue;
			}
			//敷居値をチェック
			if(this._patt_result.confidence<this._configense_th)
			{
				continue;
			}
			//マーカマップへの追加対象か調べる。
			ARMarkerSortList.LLItem ip=this._mkmap.getInsertPoint(this._patt_result.confidence);
			if(ip==null){
				continue;
			}
			//マーカマップアイテムの矩形に参照値を設定する。
			ip=this._mkmap.insertFromTailBefore(ip);
			ip.cf=this._patt_result.confidence;
			ip.dir=this._patt_result.direction;
			ip.id=i;
			ip.ref_sq=i_sq;
			is_ganalated_sq=true;
		}
		return is_ganalated_sq;
	}		
	/**
	 * @param i_num_of_markers
	 * マーカの個数
	 */
	public void prepare()
	{
		//ARマーカのマッチテーブルのサイズを調整
		if(this._mkmap._num_of_llitem<this.size()*this.size()){
			//不足してるなら作っておく。
			this._mkmap=new ARMarkerSortList(this.size()*this.size());
		}
		//マッチングテーブルをリセット
		this._mkmap.reset();
		
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
		ARMarkerSortList.LLItem top_item=this._mkmap.getTopItem();
		//アイテムを検出できなくなるまで、一致率が高い順にアイテムを得る。
		while(top_item!=null){
			//検出したアイテムのARmarkerIndexのデータをセット
			MarkerInfoARMarker target=this.get(top_item.id);
			target.cf=top_item.cf;
			target.lost_count=0;
			target.sq=top_item.ref_sq;
			target.sq.rotateVertexL(4-top_item.dir);
			//トラッキング用のログをコピー。
			NyARIntPoint2d.copyArray(top_item.ref_sq.ob_vertex,target.tl_vertex);
			target.tl_center.setValue(top_item.ref_sq.center2d);
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
	/** トラッキングログ用の領域*/
	public NyARIntPoint2d[] tl_vertex=NyARIntPoint2d.createArray(4);
	public NyARIntPoint2d   tl_center=new NyARIntPoint2d();
	public int tl_rect_area;
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
	public int patt_w;
	public int patt_h;
	/** MK_ARの情報。パターンのエッジ割合。*/
	public final int patt_edge_percentage;
	/** */
	public MarkerInfoARMarker(NyARCode i_patt,int i_patt_edge_percentage,double i_patt_size) throws NyARException
	{
		this.matchpatt=new NyARMatchPatt_Color_WITHOUT_PCA(i_patt);
		this.patt_edge_percentage=i_patt_edge_percentage;
		this.marker_offset.setSquare(i_patt_size);
		this.patt_w=i_patt.getWidth();
		this.patt_h=i_patt.getHeight();
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