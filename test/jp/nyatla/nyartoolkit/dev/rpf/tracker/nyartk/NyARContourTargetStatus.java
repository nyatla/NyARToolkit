package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
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
			//idxでソート
			for(i=0;i<out_len_1;){
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
	 * ベクトル結合時の、敷居値(cos(x)の値)
	 * 0.99は約?度
	 */
	public boolean setValue(LowResolutionLabelingSamplerOut.Item i_sample) throws NyARException
	{
		LrlsGsRaster r=(LrlsGsRaster)i_sample.ref_raster;
		return r.baseraster.getVectorReader().traceConture(r, i_sample.lebeling_th, i_sample.entry_pos, vecpos);
	}	
}
