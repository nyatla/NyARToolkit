package jp.nyatla.nyartoolkit.dev.tracking.old;

import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.utils.NyARMath;

/**
 * 頂点集合同士の組み合わせを、距離的な観点から計算する。
 *
 */
public class VertexBinder
{
	public class DistItem
	{
		public int row;
		public int col;
		public int dist;
	}
	protected DistItem _map[];
	public VertexBinder(int i_max_col,int i_max_row)
	{
		this._map=new DistItem[i_max_col*i_max_row];
		for(int i=0;i<i_max_col*i_max_row;i++){
			this._map[i]=new DistItem();
		}
	}
	/**
	 * 頂点配列rとcのマッピングを行い、cからの距離が最小となるrの組み合わせを計算する。
	 * i_row_len>=i_col_lenの関係を満たすようにして下さい。
	 * @param i_vertex_r
	 * @param i_row_len
	 * @param i_vertex_c
	 * @param i_col_len
	 * @param o_rowindex
	 * i_vertex_cに対するi_vertex_rの対応する点のインデクス配列。
	 * i_vertex_cに適切なものが無い場合は、-1である。
	 */
	public void bindPoints(NyARIntPoint2d[] i_vertex_r,int i_row_len,NyARIntPoint2d[] i_vertex_c,int i_col_len,int o_rowindex[])
	{
		DistItem[] map=this._map;
		//distortionMapを作成。ついでに最小値のインデクスも取得
		int min_index=0;
		int min_dist =Integer.MAX_VALUE;
		int idx=0;
		for(int r=0;r<i_row_len;r++){
			for(int c=0;c<i_col_len;c++){
				map[idx].col=c;
				map[idx].row=r;
				int d=NyARMath.sqNorm(i_vertex_r[r],i_vertex_c[c]);
				map[idx].dist=d;
				if(min_dist>d){
					min_index=idx;
					min_dist=d;
				}
				idx++;
			}
		}
		makeIndex(map,i_row_len*i_col_len,i_col_len,min_index,o_rowindex);
		return;
	}
	/**
	 * マップから頂点配列を生成する。(Integer版)
	 * @param i_map
	 * @param i_map_length
	 * @param i_col_len
	 * @param i_min_index
	 * @param o_rowindex
	 */
	protected static void makeIndex(DistItem[] i_map,int i_map_length,int i_col_len,int i_min_index,int o_rowindex[])
	{
		//[0]と差し替え
		DistItem temp_map;
		temp_map=i_map[0];
		i_map[0]=i_map[i_min_index];
		i_map[i_min_index]=temp_map;
		for(int i=0;i<o_rowindex.length;i++){
			o_rowindex[i]=-1;
		}
		if(i_map_length==0){
			return;
		}
		//値の保管
		o_rowindex[i_map[0].col]=i_map[0].row;
		
		//ソートして、0番目以降のデータを探す
		for(int i=1;i<i_col_len;i++){
			int min_index=0;
			//r,cのものを除外しながら最小値を得る。
			int reject_c=i_map[i-1].col;
			int reject_r=i_map[i-1].row;
			int min_dist=Integer.MAX_VALUE;
			if(1>=i_map_length-i_col_len){
				break;
			}
			for(int i2=i;i2<i_map_length;){
				//除外条件？
				if(i_map[i2].col==reject_c || i_map[i2].row==reject_r){
					//非検索対象→インスタンスの差し替えと、検索長の減算
					temp_map=i_map[i2];
					i_map[i2]=i_map[i_map_length-1];
					i_map[i_map_length-1]=temp_map;
					i_map_length--;
				}else{
					int d=i_map[i2].dist;
					if(min_dist>d){
						min_index=i2;
						min_dist=d;
					}
					i2++;
				}
			}
			//[i]の値の差し替え
			temp_map=i_map[i];
			i_map[i]=i_map[min_index];
			i_map[min_index]=temp_map;
			//値の保管
			o_rowindex[i_map[i].col]=i_map[i].row;
		}
		return;
	}		
}
