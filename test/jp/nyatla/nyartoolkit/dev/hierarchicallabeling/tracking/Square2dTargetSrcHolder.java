package jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARPointVector2d;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.HierarchyRect;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.utils.NyARObjectPool;



public class Square2dTargetSrcHolder extends NyARObjectPool<Square2dTargetSrcHolder.Square2dSrcItem>
{
	public static class Square2dSrcItem
	{
		/**
		 * 4頂点
		 */
		public NyARDoublePoint2d[] vertex=NyARDoublePoint2d.createArray(4);
		public AreaTargetSrcHolder.AreaSrcItem _ref_area_src;
		public ContourTargetSrcHolder.ContourTargetSrcItem _ref_contoure_src;
	}
	protected Square2dTargetSrcHolder.Square2dSrcItem createElement() throws NyARException
	{
		return new Square2dSrcItem();
	}
	public Square2dTargetSrcHolder(int i_length) throws NyARException
	{
		super.initInstance(i_length, Square2dTargetSrcHolder.Square2dSrcItem.class);
	}
	public Square2dTargetSrcHolder.Square2dSrcItem newSrcTarget(ContourTargetSrcHolder.ContourTargetSrcItem i_item)
	{
		Square2dTargetSrcHolder.Square2dSrcItem item=this.newObject();
		if(item==null){
			return null;
		}				
		item._ref_contoure_src=i_item;
		//個数チェック
		if(i_item.vecpos_length<4){
			this.deleteObject(item);
			return null;
		}
		//coordVectorから、distの大きい値のものを4個選ぶ
		//4つの合計が、全体の50%を超えている？
		//4つの値がそれぞれ10%を超えている？
		//インデクスでソート
		
//輪郭ソースから4頂点(観察系)を生成
		return item;
	}
	public class CoordItem
	{
		public int i_index;
		public NyARPointVector2d _ref;
	}
		
	
	public static void getLagestVectors(NyARPointVector2d[] i_vecpos,int i_len)
	{
		int[] idx=new int[4];
		idx[0]=0;
		idx[1]=1;
		idx[2]=2;
		idx[3]=3;
		//distでソートする
		for(int i=0;i<3;){
			if(i_vecpos[idx[i]].dist<i_vecpos[idx[i+1]].dist){
				int t=idx[i];
				idx[i]=idx[i+1];
				idx[i+1]=t;
				i=0;
			}
		}
		//先に4個をdistでソートしながら格納
		for(int i=4;i<i_len;i++){
			//配列の値と比較
			for(int i2=3;i2>=0;i2--){
				if(i_vecpos[i].dist>i_vecpos[idx[i2]].dist){				
					//値挿入の為のシフト
					for(int i3=2;i3>i2;i3--){
						idx[i3+1]=idx[i3];
					}
					//設定
					idx[i2]=i;
				}
			}
		}
		//idxでソート
		for(int i=0;i<3;){
			if(idx[i]<idx[i+1]){
				int t=idx[i];
				idx[i]=idx[i+1];
				idx[i+1]=t;
				i=0;
			}
		}
	}
}
