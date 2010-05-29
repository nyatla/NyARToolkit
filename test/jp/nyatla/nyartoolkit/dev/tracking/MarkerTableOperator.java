package jp.nyatla.nyartoolkit.dev.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.types.*;
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
	
	public MarkerTableOperator(NyARParam i_ref_param) throws NyARException
	{
		this._transmat=new NyARTransMat(i_ref_param);
		this._tmp_trans_result=new NyARTransMatResult();
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
		//位置を計算（ここでEstimate?）
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
//	private NyARDoublePoint3d _area_temp=new NyARDoublePoint3d();
	/**
	 * 2次元空間の指定点の一番近くにあるアイテムを探します。
	 * @param i_table
	 * @param i_pos
	 * 探索点
	 * @param i_limit_max
	 * 探索範囲の最大値
	 * @return
	 *//*
	public MarkerPositionTable.Item selectNearItem2d(MarkerPositionTable i_table,NyARDoublePoint2d i_pos,double i_limit_max,int i_limit_min_life)
	{
		MarkerPositionTable.Item[] items=i_table.selectAllItems();
		
		double d=Double.MAX_VALUE;
		NyARPerspectiveProjectionMatrix prjmat=this._prjmat;
		NyARDoublePoint2d pos2d=this._pos2d_tmp;
		//エリア
		NyARDoublePoint3d area=this._area_temp;
		area.x=area.y=i_limit_max;
		int index=-1;
		for(int i=items.length-1;i>=0;i--)
		{
			if(items[i].is_empty || items[i].life<i_limit_min_life){
				continue;
			}
			NyARDoublePoint3d trans=items[i].trans;
			//pos2dに中心座標を計算
			prjmat.projectionConvert(trans,pos2d);
			//２点間の距離^2を計算
			double x,y;
			x=pos2d.x-i_pos.x;
			y=pos2d.y-i_pos.y;
			double nd=(x*x)+(y*y);
			
			//有効距離を計算
			area.z=trans.z;
			prjmat.projectionConvert(area,pos2d);
			x=pos2d.x-this._prjmat.m02;
			y=pos2d.y-this._prjmat.m12;
			double limit_d=x*x+y*y;
			
			//有効範囲内？
			if(nd>limit_d){
				continue;
			}
			if(d>nd){
				d=nd;
				index=i;
			}
		}
		return index==-1?null:items[index];
	}*/
	
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
}
