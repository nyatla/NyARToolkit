package jp.nyatla.nyartoolkit.markerar;

import java.io.InputStream;
import java.util.ArrayList;


import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.analyzer.histogram.*;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattDeviationColorData;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattResult;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector_Rle;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.core.types.NyARIntCoordinates;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.markerar.utils.*;
import jp.nyatla.nyartoolkit.nyidmarker.*;
import jp.nyatla.nyartoolkit.nyidmarker.data.*;



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
		this._rledetect._tracking_list.add(target);
		return (this._rledetect._idmk_list.size()-1)|IDTYPE_NYID;
	}
	public int addARMarker(NyARCode i_code,int i_patt_edge_percentage,double i_marker_size) throws NyARException
	{
		MarkerInfoARMarker target=new MarkerInfoARMarker(i_code,i_patt_edge_percentage,i_marker_size);
		if(!this._rledetect._armk_list.add(target)){
			throw new NyARException();
		}
		this._rledetect._tracking_list.add(target);
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


/**
 * 頂点マッピング用のテーブル
 */
abstract class SortLLTable<T extends SortLLTable.Item>
{
	static class Item{
		Item next;
		Item prev;
	};	
	protected abstract T createElement();
	protected int _num_of_llitem;
	protected T _llitems;
	/**
	 * i_num_of_item以上の要素を予約する。
	 * @param i_num_of_item
	 */
	public void reserv(int i_num_of_item)
	{
		if(this._num_of_llitem<i_num_of_item){
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
	}
	/**
	 * リストを1拡張する。
	 * @param i_num_of_item
	 */
	public void append()
	{
		T new_element=this.createElement();
		T tail=(T) this._llitems.prev;
		tail.next=new_element;
		new_element.next=this._llitems;
		new_element.prev=tail;
		this._llitems.prev=new_element;
		this._num_of_llitem++;
		
	}
	/**
	 * 指定個数のリンクリストを生成。
	 * @param i_num_of_item
	 */
	public SortLLTable(int i_num_of_item)
	{
		this._num_of_llitem=0;
		reserv(1);
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

class VertexSortTable extends SortLLTable<VertexSortTable.Item>
{
	static class Item extends SortLLTable.Item
	{
		int sq_dist;
		TMarkerData marker;
		int shift;
		SquareStack.Item ref_sq;
	};
	public VertexSortTable(int iNumOfItem)
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
	/**
	 * 指定したターゲットと同じマーカと同じ矩形候補を参照している
	 * @param i_topitem
	 */
	public void disableMatchItem(Item i_topitem)
	{
		Item ptr=this._llitems;
		for(int i=this._num_of_llitem-1;i>=0;i--)
		{
			if(ptr.marker!=null){
				if(ptr.marker==i_topitem.marker || ptr.marker.sq==i_topitem.ref_sq){
					ptr.marker=null;
				}
			}
			ptr=(Item) ptr.next;
		}	
	}
	public Item getTopItem()
	{
		Item ptr=this._llitems;
		for(int i=this._num_of_llitem-1;i>=0;i--)
		{
			if(ptr.marker==null){
				ptr=(Item) ptr.next;
				continue;
			}
			return ptr;
		}
		return null;
	}
}
class ARMarkerSortList extends SortLLTable<ARMarkerSortList.Item>
{
	public class Item extends SortLLTable.Item
	{
		MarkerInfoARMarker marker;
		double cf;
		int dir;
		SquareStack.Item ref_sq;
	};
	/**
	 * 指定個数のリンクリストを生成。
	 * @param i_num_of_item
	 */
	public ARMarkerSortList()
	{
		super(1);
	}
	protected Item createElement()
	{
		return new Item();
	}
	/**
	 * 挿入ポイントを返す。挿入ポイントは、i_sd_point(距離点数)が
	 * 登録済のポイントより小さい場合のみ返却する。
	 * @return
	 */
	public Item getInsertPoint(double i_cf)
	{
		Item ptr=_llitems;
		//先頭の場合
		if(ptr.cf<i_cf){
			return ptr;
		}
		//それ以降
		ptr=(Item) ptr.next;
		for(int i=this._num_of_llitem-2;i>=0;i--)
		{
			if(ptr.cf<i_cf){
				return ptr;
			}
			ptr=(Item) ptr.next;
		}
		//対象外。
		return null;		
	}		
	public void reset()
	{
		Item ptr=this._llitems;
		for(int i=this._num_of_llitem-1;i>=0;i--)
		{
			ptr.cf=0;
			ptr.marker=null;
			ptr.ref_sq=null;
			ptr=(Item) ptr.next;
		}
		
	}
	/**
	 * リストから最も高い一致率のアイテムを取得する。
	 */
	public Item getTopItem()
	{
		Item ptr=this._llitems;
		for(int i=this._num_of_llitem-1;i>=0;i--)
		{
			if(ptr.marker==null){
				ptr=(Item) ptr.next;
				continue;
			}
			return ptr;
		}
		return null;
	}
	/**
	 * リスト中の、i_itemと同じマーカIDか、同じ矩形情報を参照しているものを無効に(ptr.idを-1)する。
	 */
	public void disableMatchItem(Item i_item)
	{
		Item ptr=this._llitems;
		for(int i=this._num_of_llitem-1;i>=0;i--)
		{			
			if((ptr.marker==i_item.marker) || (ptr.ref_sq==i_item.ref_sq)){
				ptr.marker=null;
			}
			ptr=(Item) ptr.next;
		}
	}
}

class TrackingList extends ArrayList<TMarkerData>
{
	private static final long serialVersionUID = -6446466460932931830L;
	private VertexSortTable _tracking_list;
	public TrackingList() throws NyARException
	{
		this._tracking_list=new VertexSortTable(1);
	}
	public void prepare()
	{
		//トラッキングリストをリセット
		this._tracking_list.reset();
	}
	private int[] __ret=new int[2];
	public boolean update(SquareStack.Item i_new_sq) throws NyARException
	{
		int ret[]=this.__ret;
		int new_area=i_new_sq.rect_area;
		//頂点の対角距離
		int new_sq_dist=i_new_sq.vertex_area.getDiagonalSqDist();
		boolean is_dispatched=false;
		for(int i=this.size()-1;i>=0;i--)
		{
			TMarkerData target=this.get(i);
			if(target.lost_count>1){
				continue;
			}
			//面積比が急激0.8-1.2倍以外の変動なら無視
			int a_rate=new_area*100/target.tl_rect_area;
			if(a_rate<80 || 120<a_rate){
				continue;
			}
			//移動距離^2の二乗が対角線距離^2の2倍以上なら無視
			long sq_move=target.tl_center.sqDist(i_new_sq.center2d);
			if(sq_move*2/new_sq_dist>0){
				continue;
			}
			compareVertexSet(i_new_sq.ob_vertex,target.tl_vertex,ret);
			int sqdist=ret[1];
			int shift=ret[0];
			//頂点移動距離の合計が、(中心点移動距離+1)の8倍を超えてたらNG <-
			if(sqdist>(sq_move+1)*8){
				continue;
			}
			//登録可能か確認
			VertexSortTable.Item item=this._tracking_list.getInsertPoint(sqdist);
			if(item==null){
				continue;
			}
			//登録
			item=this._tracking_list.insertFromTailBefore(item);
			item.marker=target;
			item.shift=shift;
			item.sq_dist=sqdist;
			item.ref_sq=i_new_sq;
			is_dispatched=true;
		}
		return is_dispatched;
	}

	/**
     * この関数は、頂点セット同士のシフト量を計算して、配列に値を返します。
     * 並びが同じである頂点セット同士の最低の移動量を計算して、その時のシフト量と二乗移動量の合計を返します。
     * @param i_square
     * 比較対象の矩形
     * @return
     * [0]にシフト量を返します。
     * [1]に頂点移動距離の合計の二乗値を返します。
     * シフト量はthis-i_squareです。1の場合、i_v1[0]とi_v2[1]が対応点になる(shift量1)であることを示します。
     */
    public static void compareVertexSet(NyARIntPoint2d[] i_v1,NyARIntPoint2d[] i_v2,int[] ret)
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
    	ret[0]=min_index;
    	ret[1]=min_dist;
    }
    /**
     * トラッキングリストへ追加。このadd以外使わないでね。
     */
	public boolean add(TMarkerData e)
	{
		//1マーカ辺りの最大候補数
		for(int i=0;i<2;i++){
			this._tracking_list.append();
		}
		return super.add(e);
	}
	public void finish()
	{
		//一致率の最も高いアイテムを得る。
		VertexSortTable.Item top_item=this._tracking_list.getTopItem();
		//アイテムを検出できなくなるまで、一致率が高い順にアイテムを得る。
		while(top_item!=null){
			//検出したアイテムのARmarkerIndexのデータをセット
			TMarkerData target=top_item.marker;
			//検出カウンタが1以上（未検出の場合のみ検出）
			if(target.lost_count>0){
				target.lost_count=0;
				target.sq=top_item.ref_sq;
				target.sq.rotateVertexL(4-top_item.shift);
				NyARIntPoint2d.shiftCopy(top_item.ref_sq.ob_vertex,target.tl_vertex,4-top_item.shift);
				target.tl_center.setValue(top_item.ref_sq.center2d);
				target.tl_rect_area=top_item.ref_sq.rect_area;
			}
			//基準アイテムと重複するアイテムを削除する。
			this._tracking_list.disableMatchItem(top_item);
			top_item=this._tracking_list.getTopItem();
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
	public TrackingList _tracking_list;
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
		this._idmk_list=new NyIdList();
		this._tracking_list=new TrackingList();
		this._transmat=new NyARTransMat(i_param);
		//同時に判定待ちにできる矩形の数
		this._sq_stack=new SquareStack(INITIAL_MARKER_STACK_SIZE);
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
			//トラッキング対象か確認する。
			if(this._tracking_list.update(sq_tmp)){
				//トラッキング対象ならブレーク
				is_target_marker=true;
				break;
			}
			//nyIdマーカの特定(IDマーカの特定はここで完結する。)
			if(this._idmk_list.size()>0){
				if(this._idmk_list.update(this._ref_input_gs,sq_tmp)){
					is_target_marker=true;
					break;//idマーカを特定
				}
			}
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
		//準備(ミスカウンタを+1する。)
		for(int i=this._idmk_list.size()-1;i>=0;i--){
			MarkerInfoNyId target=this._idmk_list.get(i);
			if(target.lost_count<Integer.MAX_VALUE){
				target.lost_count++;
			}
		}
		this._sq_stack.clear();//矩形情報の保持スタック初期化
		this._tracking_list.prepare();
		this._idmk_list.prepare();
		this._armk_list.prepare();
		//検出処理
		this._ref_input_rfb=i_sensor.getSourceImage();
		this._ref_input_gs=i_sensor.getGsImage();
		super.detectMarker(this._ref_input_gs,i_th);

		//検出結果の反映処理
		this._tracking_list.finish();
		this._armk_list.finish();
		this._idmk_list.finish();

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
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double _configense_th=0.5;
	private final NyARMatchPattResult _patt_result=new NyARMatchPattResult();;
	private final MultiResolutionPattProvider _mpickup=new MultiResolutionPattProvider();
	private ARMarkerSortList _mkmap;
	public ARMarkerList() throws NyARException
	{
		this._mkmap=new ARMarkerSortList();//初期値1マーカ
		return;
	}
	/**
	 * このAdd以外使わないでね。
	 */
	public boolean add(MarkerInfoARMarker i_e)
	{
		//マッチテーブルのサイズを調整
		int s=this.size()+1;
		while(this._mkmap._num_of_llitem<s*s){
			this._mkmap.append();
		}
		return super.add(i_e);
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
			ARMarkerSortList.Item ip=this._mkmap.getInsertPoint(this._patt_result.confidence);
			if(ip==null){
				continue;
			}
			//マーカマップアイテムの矩形に参照値を設定する。
			ip=this._mkmap.insertFromTailBefore(ip);
			ip.cf=this._patt_result.confidence;
			ip.dir=this._patt_result.direction;
			ip.marker=target;
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
		ARMarkerSortList.Item top_item=this._mkmap.getTopItem();
		//アイテムを検出できなくなるまで、一致率が高い順にアイテムを得る。
		while(top_item!=null){
			//検出したアイテムのARmarkerIndexのデータをセット
			MarkerInfoARMarker target=top_item.marker;
			if(target.lost_count>0){
				//未割当のマーカのみ検出操作を実行。
				target.cf=top_item.cf;
				target.lost_count=0;
				target.sq=top_item.ref_sq;
				target.sq.rotateVertexL(4-top_item.dir);
				NyARIntPoint2d.shiftCopy(top_item.ref_sq.ob_vertex,target.tl_vertex,4-top_item.dir);
				target.tl_center.setValue(top_item.ref_sq.center2d);
				target.tl_rect_area=top_item.ref_sq.rect_area;
			}
			//基準アイテムと重複するアイテムを削除する。
			this._mkmap.disableMatchItem(top_item);
			top_item=this._mkmap.getTopItem();
		}
	}
}


class NyIdList extends ArrayList<MarkerInfoNyId>
{
	private static final long serialVersionUID = -6446466460932931830L;
	/**輪郭推定器*/
	private NyIdMarkerPickup _id_pickup;
	private final NyIdMarkerPattern _id_patt=new NyIdMarkerPattern();
	private final NyIdMarkerParam _id_param=new NyIdMarkerParam();
	private final NyIdMarkerDataEncoder_RawBitId _id_encoder=new NyIdMarkerDataEncoder_RawBitId();
	private final NyIdMarkerData_RawBitId _id_data=new NyIdMarkerData_RawBitId();
	public NyIdList() throws NyARException
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
			target.sq=null;
		}
	}
	public boolean update(INyARGrayscaleRaster i_raster,SquareStack.Item i_sq) throws NyARException
	{
		if(!this._id_pickup.pickFromRaster(i_raster.getGsPixelDriver(),i_sq.ob_vertex, this._id_patt, this._id_param))
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
			target.dir=this._id_param.direction;
			target.sq=i_sq;
			return true;
		}
		return false;
	}
	public void finish()
	{
		for(int i=this.size()-1;i>=0;i--)
		{
			MarkerInfoNyId target=this.get(i);
			if(target.sq==null){
				continue;
			}
			if(target.lost_count>0){
				//参照はそのままで、dirだけ調整する。
				target.lost_count=0;
				target.sq.rotateVertexL(4-target.dir);
				NyARIntPoint2d.shiftCopy(target.sq.ob_vertex,target.tl_vertex,4-target.dir);
				target.tl_center.setValue(target.sq.center2d);
				target.tl_rect_area=target.sq.rect_area;
			}
		}
	}	
}





