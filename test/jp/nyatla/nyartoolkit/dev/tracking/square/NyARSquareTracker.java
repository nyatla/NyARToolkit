package jp.nyatla.nyartoolkit.dev.tracking.square;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;

import jp.nyatla.nyartoolkit.dev.tracking.outline.NyAROutlineTrackItem;
import jp.nyatla.nyartoolkit.dev.tracking.outline.VertexBinder;

public class NyARSquareTracker
{
	/*	インラインクラス
	 */

	class SquareBinder extends VertexBinder
	{
		public SquareBinder(int i_max_col,int i_max_row)
		{
			super(i_max_col,i_max_row);
		}
		
		public void bindPoints(NyARSquareTrackSrcTable.Item[] i_vertex_r,int i_row_len,NyARSquareEstimateItem[] i_vertex_c,int i_col_len,int o_track_item[])
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
					int d=NyARMath.sqNorm(i_vertex_r[r].center,i_vertex_c[c].center);
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
	/**
	 * 未来位置の計算用
	 *
	 */
//	class EstimateSquareStack extends NyARObjectStack<EstimateSquareStack.Item>
//	{
//		public class Item
//		{
//			public NyARDoublePoint3d angle=new NyARDoublePoint3d();
//			public NyARDoublePoint3d trans=new NyARDoublePoint3d();
//			
//			public NyARIntPoint2d   center=new NyARIntPoint2d();
//			public NyARIntPoint2d[] vertex=NyARIntPoint2d.createArray(4);
//			/**
//			 * 探索距離の２乗値
//			 */
//			public double sq_dist_max;
//
//			public NyARSquareTrackItem item=new NyARSquareTrackItem();
//		}
//		protected EstimateSquareStack.Item createElement()
//		{
//			return new EstimateSquareStack.Item();
//		}
//		public EstimateSquareStack(int i_length) throws NyARException
//		{
//			super(i_length,EstimateSquareStack.Item.class);
//			return;
//		}
//		public int getNearestItem(NyARIntPoint2d i_pos)
//		{
//			double d=Double.MAX_VALUE;
//			//エリア
//			int index=-1;
//			for(int i=this._length-1;i>=0;i--)
//			{
//				NyARIntPoint2d center=this._items[i].center;
//				double nd=NyARMath.sqNorm(i_pos, center);
//				//有効範囲内？
//				if(nd>this._items[i].sq_dist_max){
//					continue;
//				}
//				if(d>nd){
//					d=nd;
//					index=i;
//				}
//			}
//			return index;
//		}	
//	}	
	
	
	class NyARSquareTrackStack extends NyARObjectStack<NyARSquareTrackItem>
	{
		protected NyARSquareTrackItem createElement()
		{
			return new NyARSquareTrackItem();
		}
		public NyARSquareTrackStack(int i_max_tracking) throws NyARException
		{
			super(i_max_tracking,NyARSquareTrackItem.class);
			return;
		}
		public int getNearestItem(NyARIntPoint2d i_pos)
		{
			double d=Double.MAX_VALUE;
			//エリア
			int index=-1;
			for(int i=this._length-1;i>=0;i--)
			{
				NyARIntPoint2d center=this._items[i].estimate.center;
				double nd=NyARMath.sqNorm(i_pos, center);
				//有効範囲内？
				if(nd>this._items[i].estimate.sq_dist_max){
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
//	private EstimateSquareStack _estimate_stack;
	private SquareBinder _binder;
	private NyARSquareTrackStack _tracker_items;

	public NyARSquareTracker(int i_max_tracking,int i_max_temp) throws NyARException
	{
		this._binder=new SquareBinder(i_max_temp,i_max_tracking);
		this._track_index=new int[i_max_tracking];
		this._tracker_items=new NyARSquareTrackStack(i_max_tracking);
	}
	
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
	NyARTransMat _transmat;	
	NyARLinear[] __temp_linear=NyARLinear.createArray(4);
	/**
	 * トラッキング対象を追加する。
	 * @param i_vertex
	 * @return
	 */
	public boolean addTrackTarget(NyAROutlineTrackItem i_item,double i_width,int i_direction) throws NyARException
	{
		NyARSquareTrackItem item=this._tracker_items.prePush();
		if(item==null){
			//あいてない。終了のお知らせ
			return false;
		}
		NyARDoublePoint2d[] vtx_ptr=item.estimate.vertex;
		for(int i=0;i<4;i++){
			int idx=(i+4 - i_direction) % 4;
			vtx_ptr[i].x=i_item.vertex[idx].x;
			vtx_ptr[i].y=i_item.vertex[idx].y;
		}

		//lineを計算()
		NyARLinear[] temp_linear=this.__temp_linear;
		NyARLinear.calculateLine(vtx_ptr[3],vtx_ptr[0], temp_linear[0]);
		NyARLinear.calculateLine(vtx_ptr[0],vtx_ptr[1], temp_linear[1]);
		NyARLinear.calculateLine(vtx_ptr[1],vtx_ptr[2], temp_linear[2]);
		NyARLinear.calculateLine(vtx_ptr[2],vtx_ptr[3], temp_linear[3]);
		//3次元位置を計算
		item.estimate.offset.setSquare(i_width);
		this._transmat.transMat(vtx_ptr,temp_linear,item.estimate.offset,item.angle,item.trans);
		//現在の行列を保存
		item.trans_v.x=item.trans_v.y=item.trans_v.z=0;
		item.angle_v.x=item.angle_v.y=item.angle_v.z=0;
		//値の引き継ぎ
		item.sirial=i_item.serial;
		item.tag=i_item.tag;
		item.life=0;
		item.estimate.center.x=i_item.center.x;
		item.estimate.center.y=i_item.center.y;
		item.estimate.sq_dist_max=item.estimate.sq_dist_max;
		return true;
	}
	public int getNumberOfSquare()
	{
		return this._tracker_items.getLength();
	}
	public NyARSquareTrackItem[] getSquares()
	{
		return this._tracker_items.getArray();
	}


	private NyARSquare __sq=new NyARSquare();
	private NyARDoublePoint3d __angle=new NyARDoublePoint3d();
	private NyARDoublePoint3d __trans=new NyARDoublePoint3d();
	private NyARDoubleMatrix33 __rot=new NyARDoubleMatrix33();
	/**
	 * データソースからトラッキング処理を実行します。
	 * @param i_datasource
	 * @param i_is_remove_target
	 */
	public void trackTarget(NyARSquareTrackSrcTable i_datasource) throws NyARException
	{
		NyARSquareTrackSrcTable.Item[] temp_items=i_datasource.getArray();
		SquareBinder binder=this._binder;
		int track_item_len= this._estimate_stack.getLength();
		
		//ワーク
		NyARSquare sq=this.__sq;
		NyARDoublePoint3d angle=this.__angle;
		NyARDoublePoint3d trans=this.__trans;
		
		//予測位置とのマッピング		
		binder.bindPoints(temp_items,i_datasource.getLength(),this._estimate_stack.getArray(),track_item_len,  this._track_index);

		for(int i=track_item_len-1;i>=0;i--)
		{
			//予想位置と一番近かったものを得る
			EstimateSquareStack.Item est_item=this._estimate_stack.getItem(i);
			if(this._track_index[i]<0){
				//見つからなかった。
				est_item.ref_item.life++;
				if(est_item.ref_item.life>10){
					//削除(順序無視の削除)
					this._tracker_items.removeIgnoreOrder(i);
				}
				continue;
			}

			NyARSquareTrackSrcTable.Item temp_item_ptr=temp_items[this._track_index[i]];
			
			//移動量が最小になる組み合わせを計算
			int dir=getNearVertexIndex(est_item.vertex,temp_item_ptr.vertex2d,4);
			for(int i2=0;i2<4;i2++){
				int idx=(i+dir) % 4;
				sq.line[i].dx=temp_item_ptr.line[idx].dx;
				sq.line[i].dy=temp_item_ptr.line[idx].dy;
				sq.line[i].c=temp_item_ptr.line[idx].c;
			}
			for (int i2 = 0; i2 < 4; i2++) {
				//直線同士の交点計算
				if(!NyARLinear.crossPos(sq.line[i],sq.line[(i + 3) % 4],sq.sqvertex[i])){
					throw new NyARException();//ここのエラー復帰するならダブルバッファにすればOK
				}
			}
			//新しい現在値を算出
			this._transmat.transMat(sq,est_item.ref_item.offset,angle,trans);
			
			//現在のパラメタを計算
			angle.x=(est_item.ref_item.angle.x+angle.x)/2;
			angle.y=(est_item.ref_item.angle.y+angle.y)/2;
			angle.z=(est_item.ref_item.angle.z+angle.z)/2;
			trans.x=(est_item.ref_item.trans.x+trans.x)/2;
			trans.y=(est_item.ref_item.trans.y+trans.y)/2;
			trans.z=(est_item.ref_item.trans.z+trans.z)/2;

			NyARDoubleMatrix33 rot=this.__rot;
			rot.setZXYAngle(angle);
			
			
			final NyARDoublePoint3d offset=current_pos.offset.vertex[i2];
			rot.transformVertex(offset,pos3d);
			//平行移動量+速度
			pos3d.x+=trans.x;//+current_pos.trans_v.x;
			pos3d.y+=trans.y;//+current_pos.trans_v.y;
			pos3d.z+=trans.z;//+current_pos.trans_v.z;
			prjmat.projectionConvert(pos3d,pos2d);//[Optimaize!]
			dist.ideal2Observ(pos2d, item.vertex[i2]);
		}			
			
			//現在のパラメタをストア
			est_item.ref_item.angle.x=angle.x;
			est_item.ref_item.angle.y=angle.y;
			est_item.ref_item.angle.z=angle.z;
			est_item.ref_item.trans.x=trans.x;
			est_item.ref_item.trans.y=trans.y;
			est_item.ref_item.trans.z=trans.z;

			//未来の位置を予測
			est_item.center.x=1;
			est_item.center.y=1;
			//distは…？
			est_item.sq_dist_max=(NyARMath.sqNorm(sq.sqvertex[0],sq.sqvertex[2])+NyARMath.sqNorm(sq.sqvertex[1],sq.sqvertex[3]))/2;
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