package jp.nyatla.nyartoolkit.dev.tracking.old.detail.labeling;

import java.awt.Graphics;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.core.param.*;

import jp.nyatla.nyartoolkit.dev.tracking.old.*;
import jp.nyatla.nyartoolkit.dev.tracking.old.outline.*;

public class NyARDetailLabelingTracker
{
	class NyARDetailLabelingTrackStack extends NyARObjectStack<NyARDetailLabelingTrackItem>
	{
		protected NyARDetailLabelingTrackItem createElement()
		{
			return new NyARDetailLabelingTrackItem();
		}
		public NyARDetailLabelingTrackStack(int i_max_tracking) throws NyARException
		{
			super(i_max_tracking,NyARDetailLabelingTrackItem.class);
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
			if(index==-1){
				System.out.print("");
				
			}
			return index;
		}			
	}	
	protected class SquareBinder extends VertexBinder
	{
		public SquareBinder(int i_max_col,int i_max_row)
		{
			super(i_max_col,i_max_row);
		}
		
		public void bindPoints(NyARDetailLabelingTrackSrcTable.NyARContourTargetStatus[] i_vertex_r,int i_row_len,NyARDetailLabelingTrackItem[] i_vertex_c,int i_col_len,int o_track_item[])
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

	
	protected int[] _track_index;
	protected SquareBinder _binder;
	protected NyARDetailLabelingTrackStack _tracker_items;
	protected NyARMarkerTracker _parent;
	protected NyARCameraDistortionFactor _ref_distfactor;
	protected NyARIntSize _ref_scr_size;
	protected NyARPerspectiveProjectionMatrix _ref_prjmat;
	private NyARDoublePoint3d __trans=new NyARDoublePoint3d();
	private NyARDoublePoint3d __angle=new NyARDoublePoint3d();
	private NyARDoublePoint2d[] __ideal_vertex_ptr=new NyARDoublePoint2d[4];
	private NyARLinear[] __ideal_line_ptr=new NyARLinear[4];
	protected NyARTransMat _transmat;
	
	
	
	public Graphics g;
	/*	インラインクラス
	 */


	public NyARDetailLabelingTracker(NyARMarkerTracker i_parent,NyARParam i_ref_param,int i_max_tracking,int i_max_temp) throws NyARException
	{
		this._parent=i_parent;
		this._binder=new SquareBinder(i_max_temp,i_max_tracking);
		this._track_index=new int[i_max_tracking];
		this._tracker_items=new NyARDetailLabelingTrackStack(i_max_tracking);
		this._transmat=new NyARTransMat(i_ref_param);
		this._ref_prjmat=i_ref_param.getPerspectiveProjectionMatrix();
		this._ref_distfactor=i_ref_param.getDistortionFactor();
		this._ref_scr_size=i_ref_param.getScreenSize();
	}
	private NyARLinear[] __temp_linear=NyARLinear.createArray(4);
	/**
	 * トラッキング対象を追加する。
	 * @param i_vertex
	 * @return
	 */
	public boolean addTrackTarget(NyAROutlineTrackItem i_item,double i_width,int i_direction) throws NyARException
	{
		NyARDetailLabelingTrackItem item=this._tracker_items.prePush();
		if(item==null){
			//あいてない。終了のお知らせ
			return false;
		}
		item.estimate.offset.setSquare(i_width);
		item.estimate.width=i_width;
		

		/*【注意！】この関数は、カメラ歪み解除の実装をサボってる。*/
		
		NyARDoublePoint2d[] vtx_ptr=item.estimate.prev_ideal_vertex;
		for(int i=0;i<4;i++){
			int idx=(i+4 - i_direction) % 4;
			vtx_ptr[i].x=i_item.vertex[idx].x;
			vtx_ptr[i].y=i_item.vertex[idx].y;
		}

		//lineを計算()
		NyARLinear[] temp_linear=this.__temp_linear;
		temp_linear[0].calculateLineWithNormalize(vtx_ptr[0],vtx_ptr[1]);
		temp_linear[1].calculateLineWithNormalize(vtx_ptr[1],vtx_ptr[2]);
		temp_linear[2].calculateLineWithNormalize(vtx_ptr[2],vtx_ptr[3]);
		temp_linear[3].calculateLineWithNormalize(vtx_ptr[3],vtx_ptr[0]);
		//3次元位置を計算
		this._transmat.transMat(vtx_ptr,temp_linear,item.estimate.offset,item.angle,item.trans);

		
		
		//頂点群の散らばる範囲の面積を計算
		item.rect_area.setAreaRect(i_item.vertex,4);
		//値の引き継ぎ
		item.serial=i_item.serial;
		item.tag=i_item.tag;
		item.life=0;
		item.estimate.center.x=i_item.center.x;
		item.estimate.center.y=i_item.center.y;
		//探索予定地を適当に計算
		item.estimate.search_area.w=item.rect_area.w+16;
		item.estimate.search_area.h=item.rect_area.h+16;
		item.estimate.search_area.x=item.rect_area.x-8;
		item.estimate.search_area.y=item.rect_area.y-8;
		item.estimate.search_area.clip(0,0,319,239);
		item.estimate.sq_dist_max=i_item.sq_dist_max;
		item.trans_v.x=item.trans_v.y=item.trans_v.z=0;


		return true;
	}
	public int getNumberOfSquare()
	{
		return this._tracker_items.getLength();
	}
	public NyARDetailLabelingTrackItem[] getSquares()
	{
		return this._tracker_items.getArray();
	}



	/**
	 * データソースからトラッキング処理を実行します。
	 * @param i_datasource
	 * @param i_is_remove_target
	 */
	public void trackTarget(NyARDetailLabelingTrackSrcTable i_datasource) throws NyARException
	{
		NyARDetailLabelingTrackSrcTable.NyARContourTargetStatus[] temp_items=i_datasource.getArray();
		SquareBinder binder=this._binder;
		int track_item_len= this._tracker_items.getLength();
		NyARDetailLabelingTrackItem[] track_items=this._tracker_items.getArray();
		
		//予測位置とのマッピング		
		binder.bindPoints(temp_items,i_datasource.getLength(),track_items,track_item_len,  this._track_index);
		



		for(int i=track_item_len-1;i>=0;i--)
		{
			//予想位置と一番近かったものを得る
			NyARDetailLabelingTrackItem item=track_items[i];
			if(this._track_index[i]<0){
				//位置予測を試行
				item.life++;
				//寿命に達したか、予測に失敗した場合は追跡終了
				if((item.life>10) || !estimateTargetPos(item)){
					//削除イベントを発行
					this._parent.onLeaveTracking(item);
					//削除(順序無視の削除)
					this._tracker_items.removeIgnoreOrder(i);
					continue;
				}
				this._parent.onDetailUpdate(item);				
				
			}else{
				item.life=0;
				if(updateByDataSource(temp_items[this._track_index[i]],item)){
					this._parent.onDetailUpdate(item);
					continue;
				}
				item.life++;
				this._parent.onDetailUpdate(item);				
			}
		}
	}
	/**
	 * 線形予測（検出範囲だけ予測させればよくね？）
	 * @param item
	 * @throws NyARException
	 */
//	線形予測（検出範囲だけ予測させればよくね？）
	private boolean estimateTargetPos(NyARDetailLabelingTrackItem item) throws NyARException
	{
		NyARPerspectiveProjectionMatrix prjmat=this._ref_prjmat;
		NyARDetailLabelingEstimateItem est_item=item.estimate;
		//移動量が最小になる組み合わせを計算
				
		//平行移動量の未来予測
		double vx,vy,vz;
		vx=item.trans.x+item.trans_v.x;
		vy=item.trans.y+item.trans_v.y;
		vz=item.trans.z+item.trans_v.z;
//		item.trans.x+=item.trans_v.x;
//		item.trans.y+=item.trans_v.y;
//		item.trans.z+=item.trans_v.z;
		//回転量予測はしない。
		
		
		//次回のトラッキング用頂点位置を計算する。
//newのこってる
NyARDoublePoint2d[] vertex=NyARDoublePoint2d.createArray(4);	
		final double area_coefficient=1.6+NyARMath.dist(item.trans_v)/item.estimate.width;
		
//矩形前提なら2頂点で十分じゃない？
		for(int i2=0;i2<4;i2++){
			NyARDoublePoint3d v=est_item.offset.vertex[i2];
			prjmat.projectionConvert(v.x*area_coefficient+vx,v.y*area_coefficient+vy,v.z+vz,vertex[i2]);
			this._ref_distfactor.ideal2Observ(vertex[i2], vertex[i2]);
		}
		est_item.search_area.setAreaRect(vertex,4);
		est_item.search_area.clip(0,0,319,239);
		//予測位置の最小値制限
		if(est_item.search_area.w*est_item.search_area.h<256){
			//大きさが16x16を切るようなら検出対象外とする。
			return false;
		}
		return true;	
	}	
	
	
	/**
	 * データソースを使ったアップデート
	 */
	private boolean updateByDataSource(NyARDetailLabelingTrackSrcTable.NyARContourTargetStatus temp_item_ptr,NyARDetailLabelingTrackItem item) throws NyARException
	{
		NyARPerspectiveProjectionMatrix prjmat=this._ref_prjmat;
		
		NyARDoublePoint3d trans=this.__trans;
		NyARDoublePoint3d angle=this.__angle;
		NyARDoublePoint2d[] ideal_vertex=this.__ideal_vertex_ptr;
		NyARLinear[] ideal_line=this.__ideal_line_ptr;
		
		NyARDetailLabelingEstimateItem est_item=item.estimate;
		//移動量が最小になる組み合わせを計算
		int dir=getNearVertexIndex(est_item.prev_ideal_vertex,temp_item_ptr.ideal_vertex,4);
		for(int i2=0;i2<4;i2++){
			int idx=(i2+dir) % 4;
			ideal_vertex[i2]=temp_item_ptr.ideal_vertex[idx];
			ideal_line[i2]=temp_item_ptr.ideal_line[idx];
		}
		//新しい現在値を算出
		this._transmat.transMat(ideal_vertex,ideal_line,est_item.offset,angle,trans);
		
		//現在のパラメタを計算して、未来のパラメタを予測
		double vx,vy,vz;
		
		//現在位置、速度の計算
		vx=item.trans_v.x=trans.x-item.trans.x;
		vy=item.trans_v.y=trans.y-item.trans.y;
		vz=item.trans_v.z=trans.z-item.trans.z;
		//速度リミッター
		double vn=NyARMath.dist(item.trans_v);
		if(vn>item.estimate.width*2){
			//マーカのサイズの２倍以上の平行移動は許可しない。
			return false;
		}
		
		item.trans.x=(trans.x*3+item.trans.x)*0.25;
		item.trans.y=(trans.y*3+item.trans.y)*0.25;
		item.trans.z=(trans.z*3+item.trans.z)*0.25;
		

		
		
		//平行移動量の未来予測
		trans.x+=vx;
		trans.y+=vy;
		trans.z+=vz;

		
		//現在の角位置の計算。敷居値未満の時は加重平均。敷居値以上の場合はそのまま使う。（2PI/16を追従限界にする。）
		vx=(angle.x-item.angle.x);
		vx=Math.abs(vx)<0.39?vx:0;
		item.angle.x=(item.angle.x+angle.x*7)/8;
		vy=(angle.y-item.angle.y);
		vy=Math.abs(vy)<0.39?vy:0;
		item.angle.y=(item.angle.y+angle.y*7)/8;
		vz=(angle.z-item.angle.z);
		vz=Math.abs(vz)<0.39?vz:0;
		item.angle.z=(item.angle.z+angle.z*7)/8;

		//回転量の未来予測
		angle.x+=vx;
		angle.y+=vy;
		angle.z+=vz;
		
		
		//今回のエリアを記録
		item.rect_area.setAreaRect(ideal_vertex, 4);
	
		
		//次回のトラッキング用データを保存する。
		for(int i2=0;i2<4;i2++)
		{
			est_item.prev_ideal_vertex[i2].x=ideal_vertex[i2].x;
			est_item.prev_ideal_vertex[i2].y=ideal_vertex[i2].y;
		}
		est_item.center.x=(int)temp_item_ptr.center.x;
		est_item.center.y=(int)temp_item_ptr.center.y;				
		

		final double area_coefficient=1.6+vn/item.estimate.width;
		System.out.println(area_coefficient);

		//検出範囲を計算(速度により検出エリアを1.5～2倍で可変にする)
//newのこってる
		NyARDoublePoint2d[] vertex=NyARDoublePoint2d.createArray(4);
		for(int i2=0;i2<4;i2++){
			NyARDoublePoint3d v=est_item.offset.vertex[i2];
			prjmat.projectionConvert(v.x*area_coefficient+trans.x,v.y*area_coefficient+trans.y,v.z+trans.z,vertex[i2]);
			this._ref_distfactor.ideal2Observ(vertex[i2], vertex[i2]);
		}
		est_item.search_area.setAreaRect(vertex,4);
		int w=est_item.search_area.w/2;
		est_item.sq_dist_max=(w*w);
		est_item.search_area.clip(0,0,319,239);
		return true;
	}
	

	
	public boolean isTrackTarget(NyARIntPoint2d i_center)
	{
		return this._tracker_items.getNearestItem(i_center)!=-1;
	}
	private static int getNearVertexIndex(NyARDoublePoint2d[] i_base_vertex,NyARDoublePoint2d[] i_next_vertex,int i_length)
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
	public Object[] _probe()
	{
		Object[] ret=new Object[10];
		return ret;
	}	 
}