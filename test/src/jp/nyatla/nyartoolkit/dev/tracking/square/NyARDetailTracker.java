package jp.nyatla.nyartoolkit.dev.tracking.square;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.core.param.*;

import jp.nyatla.nyartoolkit.dev.tracking.outline.NyAROutlineTrackItem;
import jp.nyatla.nyartoolkit.dev.tracking.outline.VertexBinder;

public class NyARDetailTracker
{
	/*	インラインクラス
	 */

	class SquareBinder extends VertexBinder
	{
		public SquareBinder(int i_max_col,int i_max_row)
		{
			super(i_max_col,i_max_row);
		}
		
		public void bindPoints(NyARDetailTrackSrcTable.Item[] i_vertex_r,int i_row_len,NyARDetailTrackItem[] i_vertex_c,int i_col_len,int o_track_item[])
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
					int d=NyARMath.sqNorm(i_vertex_r[r].center,i_vertex_c[c].estimate.center);
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

	class NyARSquareTrackStack extends NyARObjectStack<NyARDetailTrackItem>
	{
		protected NyARDetailTrackItem createElement()
		{
			return new NyARDetailTrackItem();
		}
		public NyARSquareTrackStack(int i_max_tracking) throws NyARException
		{
			super(i_max_tracking,NyARDetailTrackItem.class);
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
	private SquareBinder _binder;
	private NyARSquareTrackStack _tracker_items;

	public NyARDetailTracker(int i_max_tracking,int i_max_temp) throws NyARException
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
		NyARDetailTrackItem item=this._tracker_items.prePush();
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
	public NyARDetailTrackItem[] getSquares()
	{
		return this._tracker_items.getArray();
	}


	private NyARSquare __sq=new NyARSquare();
	private NyARDoublePoint3d __pos3d;
	private NyARDoublePoint2d __pos2d;
	private NyARPerspectiveProjectionMatrix _ref_prjmat;
	private NyARCameraDistortionFactor _ref_distfactor;
	private NyARDoublePoint3d __angle=new NyARDoublePoint3d();
	private NyARDoublePoint3d __trans=new NyARDoublePoint3d();
	private NyARDoubleMatrix33 __rot=new NyARDoubleMatrix33();

	/**
	 * データソースからトラッキング処理を実行します。
	 * @param i_datasource
	 * @param i_is_remove_target
	 */
	public void trackTarget(NyARDetailTrackSrcTable i_datasource) throws NyARException
	{
		NyARDetailTrackSrcTable.Item[] temp_items=i_datasource.getArray();
		SquareBinder binder=this._binder;
		int track_item_len= this._tracker_items.getLength();
		NyARDetailTrackItem[] track_items=this._tracker_items.getArray();
		
		//ワーク
		NyARDoublePoint3d angle=this.__angle;
		NyARDoublePoint3d trans=this.__trans;
		NyARSquare sq=this.__sq;
		NyARDoubleMatrix33 rot=this.__rot;
		NyARDoublePoint3d pos3d=this.__pos3d;
		NyARDoublePoint2d pos2d=this.__pos2d;
		NyARPerspectiveProjectionMatrix prjmat=this._ref_prjmat;
		NyARCameraDistortionFactor distfactor=this._ref_distfactor;
		
		//予測位置とのマッピング		
		binder.bindPoints(temp_items,i_datasource.getLength(),track_items,track_item_len,  this._track_index);

		for(int i=track_item_len-1;i>=0;i--)
		{
			//予想位置と一番近かったものを得る
			NyARDetailTrackItem item=track_items[i];
			NyARDetailEstimateItem est_item=this._tracker_items.getItem(i).estimate;
			if(this._track_index[i]<0){
				//見つからなかった。
				item.life++;
				if(item.life>10){
					//削除(順序無視の削除)
					this._tracker_items.removeIgnoreOrder(i);
				}
				continue;
			}

			NyARDetailTrackSrcTable.Item temp_item_ptr=temp_items[this._track_index[i]];
			
			//移動量が最小になる組み合わせを計算
			int dir=getNearVertexIndex(est_item.vertex,temp_item_ptr.ref_outline.vertex,4);
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
			this._transmat.transMat(sq,est_item.offset,angle,trans);
			
			//現在のパラメタを計算
			item.angle.x=(item.angle.x+angle.x)/2;
			item.angle.y=(item.angle.y+angle.y)/2;
			item.angle.z=(item.angle.z+angle.z)/2;
			item.trans.x=(item.trans.x+trans.x)/2;
			item.trans.y=(item.trans.y+trans.y)/2;
			item.trans.z=(item.trans.z+trans.z)/2;
/*
			//現在の速度を計算
			item.angle_v.x=angle.x-item.angle.x;
			item.angle_v.y=angle.y-item.angle.y;
			item.angle_v.z=angle.z-item.angle.z;
			item.trans_v.x=angle.x-item.trans.x;
			item.trans_v.y=angle.y-item.trans.y;
			item.trans_v.z=angle.z-item.trans.z;
			
			//未来の位置を計算(線形予測)
			angle.x+=item.angle_v.x;
			angle.y+=item.angle_v.y;
			angle.z+=item.angle_v.z;
			trans.x+=item.trans_v.x;
			trans.y+=item.trans_v.y;
			trans.z+=item.trans_v.z;
*/			
			
			//未来位置を計算

			rot.setZXYAngle(item.angle);
			double cx,cy;
			cx=cy=0;
			for(int i2=0;i2<4;i2++)
			{
				rot.transformVertex(est_item.offset.vertex[i2],pos3d);
				prjmat.projectionConvert(pos3d.x+trans.x,pos3d.y+trans.y,pos3d.z+trans.z,pos2d);//[Optimaize!]
				distfactor.ideal2Observ(pos2d, est_item.vertex[i2]);
				cx=est_item.vertex[i2].x;
				cy=est_item.vertex[i2].y;
			}
			est_item.center.x=(int)cx/4;
			est_item.center.y=(int)cy/4;
			est_item.sq_dist_max=(int)(NyARMath.sqNorm(est_item.vertex[0],est_item.vertex[2])+NyARMath.sqNorm(est_item.vertex[1],est_item.vertex[3]))/2;
		}
	}

	private static int getNearVertexIndex(NyARDoublePoint2d[] i_base_vertex,NyARIntPoint2d[] i_next_vertex,int i_length)
	{
		int min_dist=Integer.MAX_VALUE;
		int idx=-1;
		for(int i=0;i<i_length;i++){
			int dist=0;
			for(int i2=0;i2<i_length;i2++){
				dist+=NyARMath.sqNorm(i_base_vertex[i2].x,i_base_vertex[i2].y,i_next_vertex[(i2+i)%4].x,i_next_vertex[(i2+i)%4].y);
			}
			if(min_dist>dist){
				min_dist=dist;
				idx=i;
			}
		}
		return idx;
	}
	 
}