package jp.nyatla.nyartoolkit.dev.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;

/**
 * マーカ位置の更新オペレータ。
 * 
 *
 */
public class MarkerTableOperator
{
	NyARTransMatResult _tmp_trans_result;
	INyARTransMat _transmat;
	private int _sirial;
	private NyARPerspectiveProjectionMatrix _ref_prjmat;
	private INyARCameraDistortionFactor _ref_dist;
	
	public MarkerTableOperator(NyARParam i_ref_param) throws NyARException
	{
		this._transmat=new NyARTransMat(i_ref_param);
		this._tmp_trans_result=new NyARTransMatResult();
		this._ref_dist=i_ref_param.getDistortionFactor();
		this._ref_prjmat=i_ref_param.getPerspectiveProjectionMatrix();
		
		this._sirial=0;
		return;
	}
	/**
	 *	o_itemのアイテムを更新します。
	 * @param i_pkindex
	 * @param i_marker
	 */
	public void updateMarker(MarkerPositionTable.Item io_item,NyARSquare i_square) throws NyARException
	{
		NyARTransMatResult trans_result=this._tmp_trans_result;
		this._transmat.transMat(i_square,io_item.offset, trans_result);
		trans_result.getZXYAngle(io_item.angle);
		double x,y,z;
		x=trans_result.m03;
		y=trans_result.m13;
		z=trans_result.m23;
		//速度を計算
/*		double vx,vy,vz;
		vx=x-io_item.trans.x;
		vy=y-io_item.trans.y;
		vz=z-io_item.trans.z;
		//現在の加速度を計算して保存
		io_item.acceleration.x=vx-io_item.velocity.x;
		io_item.acceleration.y=vy-io_item.velocity.y;
		io_item.acceleration.z=vz-io_item.velocity.z;
		//現在の速度を計算して保存
		io_item.velocity.x=vx;
		io_item.velocity.y=vy;
		io_item.velocity.z=vz;		*/
		//現在の位置を計算して保存
		io_item.trans.x=x;
		io_item.trans.y=y;
		io_item.trans.z=z;
		io_item.life=0;
//		System.out.println("---");
//		System.out.println(io_item.acceleration.x+","+io_item.acceleration.y+","+io_item.acceleration.z);
//		System.out.println(io_item.velocity.x+","+io_item.velocity.y+","+io_item.velocity.z);
//		System.out.println(io_item.trans.x+","+io_item.trans.y+","+io_item.trans.z);
		return;
	}
	/**
	 * 新しいマーカをテーブルに加えます。
	 * @param i_table
	 * @param i_square
	 * @param i_width
	 * @return
	 * @throws NyARException
	 */
	public boolean insertMarker(MarkerPositionTable i_table,NyARSquare i_square,double i_width) throws NyARException
	{
		MarkerPositionTable.Item item=i_table.selectEmptyItem();
		if(item==null){
			return false;
		}
		NyARTransMatResult trans_result=this._tmp_trans_result;
		item.offset.setSquare(i_width);
		this._transmat.transMat(i_square,item.offset, trans_result);
		trans_result.getZXYAngle(item.angle);
		item.is_empty=false;
		//現在の行列を保存
		item.trans.x=trans_result.m03;
		item.trans.y=trans_result.m13;
		item.trans.z=trans_result.m23;
		item.offset.setSquare(i_width);
		item.life=0;
		item.sirial=this._sirial;
		this._sirial++;
		return true;
	}
	/**
	 * 3次元空間の指定点の一番近くにあるアイテムを探します。
	 * @param i_table
	 * @param i_pos
	 * 探索点
	 * @param i_limit_max
	 * 探索範囲の最大値
	 * @return
	 */
	public MarkerPositionTable.Item selectNearItem3d(MarkerPositionTable i_table,NyARDoublePoint3d i_pos,double i_limit_max)
	{
		MarkerPositionTable.Item[] items=i_table.selectAllItems();
		double d=Double.MAX_VALUE;
		int index=-1;
		final double limit=3*(i_limit_max*i_limit_max);
		
		for(int i=items.length-1;i>=0;i--)
		{
			if(items[i].is_empty){
				continue;
			}
			
			NyARDoublePoint3d trans=items[i].trans;
			//distの計算
			double nd=NyARMath.sqNorm(i_pos, trans);
			if(nd>limit){
				continue;
			}
			if(d>nd){
				d=nd;
				index=i;
			}
		}
		return index==-1?null:items[index];
	}
	/**
	 * テーブルの全行を1ティック進めます。
	 * @param i_table
	 */
	public void updateTick(MarkerPositionTable i_table)
	{
		MarkerPositionTable.Item[] items=i_table.selectAllItems();
		for(int i=items.length-1;i>=0;i--)
		{
			final MarkerPositionTable.Item item=items[i];
			if(item.is_empty){
				continue;
			}
			if(item.life<10){
				item.life++;
				continue;
			}
			item.is_empty=true;
		}
	}
	NyARDoubleMatrix33 _rot_temp=new NyARDoubleMatrix33();
	private NyARDoublePoint3d _pos3d_tmp=new NyARDoublePoint3d();
	private NyARDoublePoint3d _area_temp=new NyARDoublePoint3d();
	private NyARDoublePoint2d _pos2d_tmp=new NyARDoublePoint2d();
	/**
	 * i_tableの内容から、探索対象の矩形集合を計算する。
	 * @param i_table
	 * @param o_stack
	 */
	public void estimateMarkerPosition(MarkerPositionTable i_table,NextFrameMarkerStack o_stack)
	{			
		//現在位置のテーブルから、o_stackに探索個所の一覧を作成	
		NyARDoublePoint2d pos2d=this._pos2d_tmp;
		NyARDoublePoint3d pos3d=this._pos3d_tmp;
		
		NyARPerspectiveProjectionMatrix prjmat=this._ref_prjmat;
		INyARCameraDistortionFactor dist=this._ref_dist;
		MarkerPositionTable.Item[] items=i_table.selectAllItems();

		NyARDoubleMatrix33 rot=this._rot_temp;		
		NyARDoublePoint3d area=this._area_temp;
		area.x=area.y=80.0;		//ここなんとかすれ。**************************************************
		o_stack.clear();
		for(int i=items.length-1;i>=0;i--)
		{
			MarkerPositionTable.Item current_pos=items[i];
			if(current_pos.is_empty){
				continue;
			}
			
			final NyARDoublePoint3d trans=current_pos.trans;
			
			//探索キーポイント(中心座標)のスクリーン座標を計算
			NextFrameMarkerStack.Item item=o_stack.prePush();
			prjmat.projectionConvert(trans,item.center);//[Optimaize!]
			dist.ideal2Observ(item.center, item.center);
			
			//4頂点の予測位置を計算しておく。
			for(int i2=0;i2<4;i2++){
				//方位決定のためにvertex0の計算[optimize! ここの計算関数すれば早くなる。]
				final NyARDoublePoint3d offset=current_pos.offset.vertex[i2];
				rot.setZXYAngle(current_pos.angle);
				rot.transformVertex(offset,pos3d);
				prjmat.projectionConvert(trans.x+pos3d.x,trans.y+pos3d.y,trans.z+pos3d.z,pos2d);//[Optimaize!]
				dist.ideal2Observ(pos2d, item.vertex[i2]);
			}

			//探索範囲の計算(マーカをそのままz軸上で平行移動して、中心点からの頂点距離/2を探索距離の最大値とする。)
			area.z=trans.z;
			prjmat.projectionConvert(area,pos2d);//[Optimaize!]
			dist.ideal2Observ(pos2d,pos2d);
			item.sq_norm=NyARMath.sqNorm(pos2d.x,pos2d.y,this._ref_prjmat.m02,this._ref_prjmat.m12)/2;
			//
			item.ref_item=current_pos;
		}
		return;
	}	
	
}
