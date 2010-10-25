package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARObjectPool;



public class Square2dDataPool extends NyARObjectPool<Square2dDataPool.Square2dSrcItem>
{
	public static class Square2dSrcItem
	{
		NyARSquare square;
		/**
		 * 4頂点
		 */
		public NyARDoublePoint2d[] vertex=NyARDoublePoint2d.createArray(4);
		//制御部
		private Square2dDataPool _pool;
		public Square2dSrcItem(Square2dDataPool i_pool)
		{
			this._pool=i_pool;
		}
		/**
		 * このインスタンスを開放します。
		 */
		public void deleteMe()
		{
			this._pool.deleteObject(this);
		}
	}
	protected Square2dDataPool.Square2dSrcItem createElement() throws NyARException
	{
		return new Square2dSrcItem(this);
	}
	public Square2dDataPool(int i_length) throws NyARException
	{
		super.initInstance(i_length, Square2dDataPool.Square2dSrcItem.class);
	}
	public Square2dDataPool.Square2dSrcItem newSrcTarget(ContourDataPool.ContourTargetSrcItem i_item)
	{
		Square2dDataPool.Square2dSrcItem item=this.newObject();
		if(item==null){
			return null;
		}
//		item.contoure_src=i_item;
		//個数チェック
		if(i_item.vecpos_length<4){
			this.deleteObject(item);
			return null;
		}
		//coordVectorから、distの大きい値のものを4個選ぶ
		int[] rectvec=new int[4];
		ContourDataPool.ContourTargetSrcItem.CoordData.getKeyCoordInfoIndex(i_item.vecpos,i_item.vecpos_length, rectvec);
		//ベクトルの強度がそれぞれ10%を超えている？
		final double th_val=i_item.sq_dist_sum*0.1;
		for(int i=3;i>=0;i--){
			if(i_item.vecpos[rectvec[i]].sq_dist<th_val){
				//既定の基準に達しない。
				this.deleteObject(item);
				return null;
			}
		}
		NyARSquare sq=item.square;
		//4頂点を計算する。(本当はベクトルの方向を調整してから計算するべき)
		for(int i=3;i>=0;i--){
			ContourDataPool.ContourTargetSrcItem.CoordData cv=i_item.vecpos[rectvec[i]];
			sq.line[i].setVector(cv.dx,cv.dy,cv.x,cv.y);
		}
		for(int i=3;i>=0;i--){
			if(!NyARLinear.crossPos(sq.line[i],sq.line[(i + 3) % 4],sq.sqvertex[i])){
				//四角が作れない。
				this.deleteObject(item);
				return null;
			}
		}
		return item;
	}
	

}
