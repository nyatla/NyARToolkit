package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARVecLinear2d;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.HierarchyRect;
import jp.nyatla.nyartoolkit.dev.rpf.utils.NyARObjectPool;



public class Square3dTargetSrcPool extends NyARObjectPool<Square3dTargetSrcPool.Square3dSrcItem>
{
	public static class Square3dSrcItem
	{
		public ContourDataPool.ContourTargetSrcItem _ref_contour_src;
		/**
		 * 理想頂点(OPTION)
		 */
		public NyARDoublePoint2d[] vertex=NyARDoublePoint2d.createArray(4);
//定義]変換行列(angle,t) or Q
	}
	protected Square3dTargetSrcPool.Square3dSrcItem createElement() throws NyARException
	{
		return new Square3dSrcItem();
	}
	public Square3dTargetSrcPool(int i_length) throws NyARException
	{
		super.initInstance(i_length, Square3dTargetSrcPool.Square3dSrcItem.class);
	}
	public Square3dTargetSrcPool.Square3dSrcItem newSrcTarget(ContourDataPool.ContourTargetSrcItem i_item)
	{
//処理]Square2dから3dへの変換？
		Square3dTargetSrcPool.Square3dSrcItem item=this.newObject();
		if(item==null){
			return null;
		}
		item._ref_contour_src=i_item;
		if(i_item.vecpos_length<4){
			this.deleteObject(item);
			return null;
		}
		//coordVectorから、distの大きい値のものを4個選ぶ
		
		//4つの合計が、全体の50%を超えている？
		//4つの値がそれぞれ10%を超えている？
		//インデクスでソート
		

		return item;
	}

}
