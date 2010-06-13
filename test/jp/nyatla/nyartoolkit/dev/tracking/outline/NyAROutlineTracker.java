package jp.nyatla.nyartoolkit.dev.tracking.outline;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.tracking.*;
import jp.nyatla.nyartoolkit.dev.tracking.detail.*;
import jp.nyatla.nyartoolkit.dev.tracking.detail.fixedthreshold.NyARFixedThresholdDetailTracker;
import jp.nyatla.nyartoolkit.dev.tracking.detail.labeling.NyARDetailLabelingTracker;




public class NyAROutlineTracker
{
	class OutlineBinder extends VertexBinder
	{
		public OutlineBinder(int i_max_col,int i_max_row)
		{
			super(i_max_col,i_max_row);
		}
		
		public void bindPoints(NyAROutlineTrackSrcTable.Item[] i_vertex_r,int i_row_len,NyAROutlineTrackItem[] i_vertex_c,int i_col_len,int o_track_item[])
		{
			VertexBinder.DistItem[] map=this._map;
			//distortionMapを作成。ついでに最小値のインデクスも取得
			int min_index=0;
			int min_dist =Integer.MAX_VALUE;
			int idx=0;
			for(int r=0;r<i_row_len;r++){
				for(int c=0;c<i_col_len;c++){
					map[idx].col=c;
					map[idx].row=r;
					int d=(int)NyARMath.sqNorm(i_vertex_r[r].ideal_center,i_vertex_c[c].center);
					map[idx].dist=d;
					if(min_dist>d){
						min_index=idx;
						min_dist=d;
					}
					idx++;
				}
			}
			makeIndex(map,i_row_len*i_col_len,i_col_len,min_index,o_track_item);
			return;
		}		
	}
	class NyAROutlineTrackInternalItem extends NyAROutlineTrackItem
	{
		public boolean can_upgrade;
		public int direction;
		public double width;	
		public void setUpgradeInfo(double i_width,int i_direction)
		{
			this.can_upgrade=true;
			this.direction=i_direction;
			this.width=i_width;
		}
	}	
	class NyAROutlineTrackStack extends NyARObjectStack<NyAROutlineTrackItem>
	{
		protected NyAROutlineTrackItem createElement()
		{
			return new NyAROutlineTrackInternalItem();
		}
		public NyAROutlineTrackStack(int i_max_tracking) throws NyARException
		{
			super(i_max_tracking,NyAROutlineTrackItem.class);
			return;
		}
		public int getNearestItem(NyARIntPoint2d i_pos)
		{
			double d=Double.MAX_VALUE;
			//エリア
			int index=-1;
			for(int i=this._length-1;i>=0;i--)
			{
				NyARIntPoint2d center=this._items[i].center;
				double nd=NyARMath.sqNorm(i_pos, center);
				//有効範囲内？
				if(nd>this._items[i].sq_dist_max){
					continue;
				}
				if(d>nd){
					d=nd;
					index=i;
				}
			}
			return index;
		}	
	}
	
	private int[] _track_index;
	private OutlineBinder _binder;
	private NyARMarkerTracker _parent;
	public NyAROutlineTracker(NyARMarkerTracker i_parent,int i_max_tracking,int i_max_temp) throws NyARException
	{
		this._parent=i_parent;
		this._binder=new OutlineBinder(i_max_temp,i_max_tracking);
		this._track_index=new int[i_max_tracking];
		this._tracker_items=new NyAROutlineTrackStack(i_max_tracking);
	}	
	private int _sirial_counter=0;
	private NyAROutlineTrackStack _tracker_items;
	/**
	 * トラッキングターゲットになりそうか判定
	 * @param i_vertex0
	 * @param i_vertex1
	 * @param i_vertex2
	 * @param i_vertex3
	 * @param i_tracker
	 * 
	 * @return
	 */
	public boolean isTrackTarget(NyARIntPoint2d i_center)
	{
		return this._tracker_items.getNearestItem(i_center)!=-1;
	}
	/**
	 * トラッキング対象を追加する。
	 * @param i_vertex
	 * @return
	 */
	public boolean addTrackTarget(NyAROutlineTrackSrcRefTable i_datasource)
	{
		NyAROutlineTrackSrcTable.Item[] items=i_datasource.getArray();
		for(int i=i_datasource.getLength()-1;i>=0;i--)
		{
			NyAROutlineTrackInternalItem item=(NyAROutlineTrackInternalItem)this._tracker_items.prePush();
			if(item==null){
				//あいてない。終了のお知らせ
				return false;
			}
			NyARIntPoint2d[] vertex=items[i].vertex;
			//トラッキング対象の矩形とするかの判定。
			int sd1=NyARMath.sqNorm(vertex[0],vertex[2]);
			int sd2=NyARMath.sqNorm(vertex[1],vertex[3]);
			//1:3 - 3:1くらいまでの矩形を対象とする。
			int sq_propotion=sd1*10/(sd2+1);//+1はzero保証
			if(sq_propotion<1 || sq_propotion>90){
				return false;
			}
			//トラッキング対象の生成
			//
			
			//追加処理
			for(int i2=0;i2<4;i2++){
				item.vertex[i2].x=vertex[i2].x;
				item.vertex[i2].y=vertex[i2].y;
			}
			//中央位置
			item.center.x=items[i].ideal_center.x;
			item.center.y=items[i].ideal_center.y;
			item.life =0;
			
			//探索距離の計算
			item.sq_dist_max=(sd1+sd2)/2;
			//シリアル番号の設定			
			item.serial=(this._sirial_counter++);
			//アップグレード不能に
			item.can_upgrade=false;
			this._parent.onEnterTracking(item);
		}
		return true;
	}
	public int getNumberOfOutline()
	{
		return this._tracker_items.getLength();
	}
	public NyAROutlineTrackItem[] getOutlines()
	{
		return this._tracker_items.getArray();
	}
	
	/**
	 * データソースからトラッキング処理を実行します。
	 * @param i_datasource
	 * @param i_is_remove_target
	 */
	public void trackTarget(NyAROutlineTrackSrcRefTable i_datasource)
	{
		NyAROutlineTrackSrcTable.Item[] temp_items=i_datasource.getArray();
		OutlineBinder binder=this._binder;
		int track_item_len= this._tracker_items.getLength();

		//トラッキングを実行
		
		binder.bindPoints(temp_items,i_datasource.getLength(),this._tracker_items.getArray(),track_item_len,  this._track_index);

		for(int i=track_item_len-1;i>=0;i--)
		{
			NyAROutlineTrackItem item=this._tracker_items.getItem(i);
			if(this._track_index[i]<0){
				//見つからなかった。
				item.life++;
				if(item.life>10){
					this._parent.onLeaveTracking(item);
					//削除(順序無視の削除)
					this._tracker_items.removeIgnoreOrder(i);
				}
				continue;
			}
			NyAROutlineTrackSrcTable.Item temp_item_ptr=temp_items[this._track_index[i]];

			//探した一時情報でテーブルを更新
			item.center.x=temp_item_ptr.ideal_center.x;
			item.center.y=temp_item_ptr.ideal_center.y;

			//移動量が最小になる組み合わせを計算
			int vertex=getNearVertexIndex(item.vertex,temp_item_ptr.vertex,4);
			//頂点情報を更新
			for(int i2=0;i2<4;i2++)
			{
				item.vertex[i2].x=temp_item_ptr.vertex[(i2+vertex)%4].x;
				item.vertex[i2].y=temp_item_ptr.vertex[(i2+vertex)%4].y;
			}
			//対角線の平均を元に矩形の大体1/4 2*n/((sqrt(2)*4)*2)=n/5を計算
			item.sq_dist_max=(NyARMath.sqNorm(item.vertex[0],item.vertex[2])+NyARMath.sqNorm(item.vertex[1],item.vertex[3]))/10;
			item.life=0;
			//[位置更新のイベントの上位通知のタイミングはここ]
			this._parent.onOutlineUpdate(item);

		}
	}
	/**
	 * アップグレードを試行する。
	 * @param o_detail_tracker
	 * @throws NyARException
	 */
	public void charangeUpgrade(NyARFixedThresholdDetailTracker o_detail_tracker) throws NyARException
	{
		NyAROutlineTrackItem[] items=this._tracker_items.getArray();
		for(int i=this._tracker_items.getLength()-1;i>=0;i--)
		{
			NyAROutlineTrackInternalItem item=(NyAROutlineTrackInternalItem)items[i];
			if(item.can_upgrade){
				if(!o_detail_tracker.addTrackTarget(item,item.width,item.direction)){
					break;
				}
				this._tracker_items.removeIgnoreOrder(i);
			}
		}
	}
	public void charangeUpgrade(NyARDetailLabelingTracker o_detail_tracker) throws NyARException
	{
		NyAROutlineTrackItem[] items=this._tracker_items.getArray();
		for(int i=this._tracker_items.getLength()-1;i>=0;i--)
		{
			NyAROutlineTrackInternalItem item=(NyAROutlineTrackInternalItem)items[i];
			if(item.can_upgrade){
				if(!o_detail_tracker.addTrackTarget(item,item.width,item.direction)){
					break;
				}
				this._tracker_items.removeIgnoreOrder(i);
			}
		}
	}
	
	private static int getNearVertexIndex(NyARIntPoint2d[] i_base_vertex,NyARIntPoint2d[] i_next_vertex,int i_length)
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
	 
}
