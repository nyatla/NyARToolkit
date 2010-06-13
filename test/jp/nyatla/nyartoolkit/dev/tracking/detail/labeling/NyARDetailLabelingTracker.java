package jp.nyatla.nyartoolkit.dev.tracking.detail.labeling;

import java.awt.Graphics;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.matrix.*;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.core.param.*;

import jp.nyatla.nyartoolkit.dev.tracking.detail.*;
import jp.nyatla.nyartoolkit.dev.tracking.outline.*;
import jp.nyatla.nyartoolkit.dev.tracking.*;

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
				if(nd>this._items[i].estimate.ideal_sq_dist_max){
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
	protected class SquareBinder extends VertexBinder
	{
		public SquareBinder(int i_max_col,int i_max_row)
		{
			super(i_max_col,i_max_row);
		}
		
		public void bindPoints(NyARDetailLabelingTrackSrcTable.Item[] i_vertex_r,int i_row_len,NyARDetailTrackItem[] i_vertex_c,int i_col_len,int o_track_item[])
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
					int d=NyARMath.sqNorm(i_vertex_r[r].ideal_center,i_vertex_c[c].estimate.center);
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
	private NyARDoublePoint3d __pos3d=new NyARDoublePoint3d();
	protected NyARPerspectiveProjectionMatrix _ref_prjmat;
	private NyARDoublePoint3d __trans=new NyARDoublePoint3d();
	private NyARDoublePoint3d __angle=new NyARDoublePoint3d();
	private NyARDoubleMatrix33 __rot=new NyARDoubleMatrix33();
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

		/*【注意！】この関数は、カメラ歪み解除の実装をサボってる。*/
		
		NyARDoublePoint2d[] vtx_ptr=item.estimate.ideal_vertex;
		for(int i=0;i<4;i++){
			int idx=(i+4 - i_direction) % 4;
			vtx_ptr[i].x=i_item.vertex[idx].x;
			vtx_ptr[i].y=i_item.vertex[idx].y;
		}

		//lineを計算()
		NyARLinear[] temp_linear=this.__temp_linear;
		temp_linear[0].calculateLine(vtx_ptr[0],vtx_ptr[1]);
		temp_linear[1].calculateLine(vtx_ptr[1],vtx_ptr[2]);
		temp_linear[2].calculateLine(vtx_ptr[2],vtx_ptr[3]);
		temp_linear[3].calculateLine(vtx_ptr[3],vtx_ptr[0]);
		//3次元位置を計算
		this._transmat.transMat(vtx_ptr,temp_linear,item.estimate.offset,item.angle,item.trans);

		
		
		//値の引き継ぎ
		item.serial=i_item.serial;
		item.tag=i_item.tag;
		item.life=0;
		item.estimate.center.x=i_item.center.x;
		item.estimate.center.y=i_item.center.y;
		item.estimate.ideal_sq_dist_max=i_item.sq_dist_max;
		item.trans_v.x=item.trans_v.y=item.trans_v.z=0;
		//頂点群の散らばる範囲の面積を計算
		item.rect_area.wrapVertex(i_item.vertex,4);

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
		NyARDetailLabelingTrackSrcTable.Item[] temp_items=i_datasource.getArray();
		SquareBinder binder=this._binder;
		int track_item_len= this._tracker_items.getLength();
		NyARDetailTrackItem[] track_items=this._tracker_items.getArray();
		NyARPerspectiveProjectionMatrix prjmat=this._ref_prjmat;
		
		//ワーク
		NyARDoublePoint3d trans=this.__trans;
		NyARDoublePoint3d angle=this.__angle;
		NyARDoubleMatrix33 rot=this.__rot;
		NyARDoublePoint3d pos3d=this.__pos3d;
		NyARDoublePoint2d[] ideal_vertex=this.__ideal_vertex_ptr;
		NyARLinear[] ideal_line=this.__ideal_line_ptr;
		
		//予測位置とのマッピング		
		binder.bindPoints(temp_items,i_datasource.getLength(),track_items,track_item_len,  this._track_index);


		for(int i=track_item_len-1;i>=0;i--)
		{
			//予想位置と一番近かったものを得る
			NyARDetailTrackItem item=track_items[i];
			NyARDetailEstimateItem est_item=this._tracker_items.getItem(i).estimate;
			if(this._track_index[i]<0){
				//見つからなかった。
//位置予定だけ追加？
				item.life++;
				if(item.life>10){
					//削除イベントを発行					
					this._parent.onLeaveTracking(item);
					//削除(順序無視の削除)
					this._tracker_items.removeIgnoreOrder(i);
				}else{
					//過去の値でイベント呼ぶ
					this._parent.onDetailUpdate(item);
				}
				continue;
			}

			NyARDetailLabelingTrackSrcTable.Item temp_item_ptr=temp_items[this._track_index[i]];
			//移動量が最小になる組み合わせを計算
			int dir=getNearVertexIndex(est_item.ideal_vertex,temp_item_ptr.ideal_vertex,4);
			for(int i2=0;i2<4;i2++){
				int idx=(i2+dir) % 4;
				ideal_vertex[i2]=temp_item_ptr.ideal_vertex[idx];
				ideal_line[i2]=temp_item_ptr.ideal_line[idx];
			}
			//新しい現在値を算出
			this._transmat.transMat(ideal_vertex,ideal_line,est_item.offset,angle,trans);
			
			//

			//現在のパラメタを計算して、未来のパラメタを予測
			double vx,vy,vz;
			
			//現在位置、速度の計算
			vx=item.trans_v.x=trans.x-item.trans.x;
			vy=item.trans_v.y=trans.y-item.trans.y;
			vz=item.trans_v.z=trans.z-item.trans.z;
			item.trans.x=(trans.x+item.trans.x)*0.5;
			item.trans.y=(trans.y+item.trans.y)*0.5;
			item.trans.z=(trans.z+item.trans.z)*0.5;
			trans.x=item.trans.x+vx;
			trans.y=item.trans.y+vy;
			trans.z=item.trans.z+vz;
			
			
			//現在の角位置の計算。敷居値未満の時は加重平均。敷居値以上の場合はそのまま使う。（2PI/16に追従限界を仕掛ける）
			vx=(angle.x-item.angle.x);
			item.angle.x=(-0.39<vx && vx<0.39)?(angle.x+item.angle.x)*0.5:angle.x;
			vy=(angle.y-item.angle.y);
			item.angle.y=(-0.39<vy && vy<0.39)?(angle.y+item.angle.y)*0.5:angle.y;			
			vz=(angle.z-item.angle.z);
			item.angle.z=(-0.39<vz && vz<0.39)?(angle.z+item.angle.z)*0.5:angle.z;

System.out.println(vx+":"+vy+":"+vz);			
			
			//理想系での未来位置を計算(angle,transは観測値からの予想値)
			rot.setZXYAngle(item.angle);
			double cx,cy;
			cx=cy=0;
			for(int i2=0;i2<4;i2++)
			{
				rot.transformVertex(est_item.offset.vertex[i2],pos3d);
				prjmat.projectionConvert(pos3d.x+trans.x,pos3d.y+trans.y,pos3d.z+trans.z,est_item.ideal_vertex[i2]);//[Optimaize!]
				cx+=(int)est_item.ideal_vertex[i2].x;
				cy+=(int)est_item.ideal_vertex[i2].y;
			}
			//中央値を計算して、理想位置から画面位置に変換（マイナスの時は無かったことにする。）
			cx/=4;
			cy/=4;
			if(this._ref_scr_size.isInsideRact((int)cx,(int)cy)){
				this._ref_distfactor.ideal2Observ(cx, cy, est_item.center);
			}else{
				est_item.center.x=(int)cx;
				est_item.center.y=(int)cy;				
			}

			
			//対角線の平均を元に矩形の大体半分 2*n/((sqrt(2)*2)*2)=n/5を計算
			est_item.ideal_sq_dist_max=(int)(NyARMath.sqNorm(est_item.ideal_vertex[0],est_item.ideal_vertex[2])+NyARMath.sqNorm(est_item.ideal_vertex[1],est_item.ideal_vertex[3]))/5;
			//更新
			this._parent.onDetailUpdate(item);
		}
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