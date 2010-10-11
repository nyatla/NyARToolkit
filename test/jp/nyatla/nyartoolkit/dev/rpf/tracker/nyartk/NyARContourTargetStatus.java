package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.NyARPointVector2d;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.LrlsGsRaster;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution.NyARVectorReader_INT1D_GRAY_8;

/**
 * 輪郭ソース1個を格納するクラスです。
 *
 */
public class NyARContourTargetStatus extends NyARTargetStatus
{
	private NyARContourTargetStatusPool.WorkObject _shared;
	
	public static class VectorCoords
	{
		public int length;
		public CoordData item[];
		public VectorCoords(int i_length)
		{
			this.length=0;
			this.item=CoordData.createArray(i_length);
		}
		/**
		 * 輪郭配列から、から、キーのベクトル(絶対値の大きいベクトル)を順序を壊さずに抽出します。
		 * @param i_vecpos
		 * 抽出元の
		 * @param i_len
		 * @param o_index
		 * インデクス番号を受け取る配列。受け取るインデックスの個数は、この配列の数と同じになります。
		 */
		public void getKeyCoordIndexes(int[] o_index)
		{
			getKeyCoordIndexesNoOrder(o_index);
			//idxでソート
			int out_len_1=o_index.length-1;
			for(int i=0;i<out_len_1;){
				if(o_index[i]>o_index[i+1]){
					int t=o_index[i];
					o_index[i]=o_index[i+1];
					o_index[i+1]=t;
					i=0;
					continue;
				}
				i++;
			}
			return;
		}
		/**
		 * 輪郭配列から、から、キーのベクトル(絶対値の大きいベクトル)を順序を無視して抽出します。
		 * @param i_vecpos
		 * @param i_len
		 * @param o_index
		 * インデクス番号を受け取る配列。受け取るインデックスの個数は、この配列の数と同じになります。
		 */
		public void getKeyCoordIndexesNoOrder(int[] o_index)
		{
			CoordData[] vp=this.item;
			assert(o_index.length<=this.length);
			int i;
			int out_len=o_index.length;
			int out_len_1=out_len-1;
			for(i=out_len-1;i>=0;i--){
				o_index[i]=i;			
			}
			//sqdistでソートする(B->S)
			for(i=0;i<out_len_1;){
				if(vp[o_index[i]].sq_dist<vp[o_index[i+1]].sq_dist){
					int t=o_index[i];
					o_index[i]=o_index[i+1];
					o_index[i+1]=t;
					i=0;
					continue;
				}
				i++;
			}
			//先に4個をsq_distでソートしながら格納
			for(i=out_len;i<this.length;i++){
				//配列の値と比較
				for(int i2=0;i2<out_len;i2++){
					if(vp[i].sq_dist>vp[o_index[i2]].sq_dist){				
						//値挿入の為のシフト
						for(int i3=out_len-1;i3>i2;i3--){
							o_index[i3]=o_index[i3-1];
						}
						//設定
						o_index[i2]=i;
						break;
					}
				}
			}
			return;
		}		
		/**
		 * 最も大きいベクトル成分のインデクスを返します。
		 * @return
		 */
		public int getMaxCoordIndex()
		{
			CoordData[] vp=this.item;
			int index=0;
			double max_dist=vp[0].sq_dist;
			for(int i=this.length-1;i>0;i--){
				if(max_dist<vp[i].sq_dist){
					max_dist=vp[index].sq_dist;
					index=i;
				}
			}
			return index;
		}
		/**
		 * 順番を無視して、類似する輪郭線のベクトルをまとめます。
		 * @return
		 */
		public void margeResembleCoordIgnoreOrder()
		{
			for(int i=this.length-1;i>=0;i--){
				CoordData target1=this.item[i];
				if(target1.dy<0){
					target1.dy*=-1;
					target1.dx*=-1;
				}
			}
			
			for(int i=this.length-1;i>=0;i--){
				CoordData target1=this.item[i];
				for(int i2=i-1;i2>=0;i2--){
					CoordData target2=this.item[i2];
					if(NyARPointVector2d.getVecCos(target1,target2)>=0.99){
						//それぞれの代表点から法線を引いて、相手の直線との交点を計算する。
						NyARLinear l1=new NyARLinear();
						NyARLinear l2=new NyARLinear();
						NyARLinear ol=new NyARLinear();
						NyARDoublePoint2d p=new NyARDoublePoint2d();
						l1.setVector(target1);
						l2.setVector(target2);
						ol.orthogonalLine(l1,target1.x,target1.y);
						double wx,wy;
						double l=0;
						NyARLinear.crossPos(ol,l2,p);
						//交点間の距離の合計を計算。lに2*dist^2を得る。
						wx=(p.x-target1.x);wy=(p.y-target1.y);
						l+=wx*wx+wy*wy;
						ol.orthogonalLine(l2,target2.x,target2.y);
						NyARLinear.crossPos(ol,l1,p);
						wx=(p.x-target2.x);wy=(p.y-target2.y);
						l+=wx*wx+wy*wy;
						//距離が一定値以下なら、マージ
						if(l>((5*5)*2))
						{
							continue;
						}
						//似たようなベクトル発見したら、前方のアイテムに値を統合。
						target2.x=(target1.x+target2.x)/2;
						target2.y=(target1.y+target2.y)/2;
						target2.dx+=target1.dx;
						target2.dy+=target1.dy;
						target2.sq_dist+=target1.sq_dist;
						//最後尾のアイテムと、現在のアイテムを差し替え
						this.item[i2]=this.item[this.length-1];
						this.item[this.length-1]=target2;
						this.length-=1;
					}
				}
			}
		}
	}
	
	/**
	 * データ型です。
	 * 輪郭ベクトルを格納します。
	 */
	public static class CoordData extends NyARPointVector2d
	{
		/**
		 * ベクトルの2乗値です。輪郭の強度値にもなります。
		 */
		public double sq_dist;
		public static CoordData[] createArray(int i_length)
		{
			CoordData[] r=new CoordData[i_length];
			for(int i=0;i<i_length;i++){
				r[i]=new CoordData();
			}
			return r;
		}
		
	}
	/**
	 * ベクトル要素を格納する配列です。
	 */
	public VectorCoords vecpos=new VectorCoords(100);

	
	
	//
	//制御部

	/**
	 * @param i_ref_pool_operator
	 * @param i_shared
	 * 共有ワークオブジェクトを指定します。
	 * 
	 */
	public NyARContourTargetStatus(INyARManagedObjectPoolOperater i_ref_pool_operator,NyARContourTargetStatusPool.WorkObject i_shared)
	{
		super(i_ref_pool_operator);
		this._shared=i_shared;
	}
	/**
	 * データソースから値をセットします。
	 * @param i_sample
	 * @return
	 * @throws NyARException
	 */

	public boolean setValue(LowResolutionLabelingSamplerOut.Item i_sample) throws NyARException
	{
		LrlsGsRaster r=(LrlsGsRaster)i_sample.ref_raster;
		return r.baseraster.getVectorReader().traceConture(r, i_sample.lebeling_th, i_sample.entry_pos, vecpos);
	}	
}
