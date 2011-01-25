/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.utils;

import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;

/**
 * このクラスは、距離マップを利用した、頂点集合同士のマッチング処理機能を提供します。
 * 2つの点集合同士を比較して、集合の各点同士の距離が最も近くになる組み合わせを計算します。
 * <p>アルゴリズム - 
 * 2つの点集合の総当たりの距離マップを作り、その距離が小さいのものから順に抽出することで、
 * 其々の点の移動距離が最小になる組み合わせを計算します。
 * </p>
 * <p>使い方　-
 * このクラスは、まず距離マップを作るために距離値をセットして、次に組み合わせを得る手順で使います。
 * 距離マップには行と列があります。列を基準値、行を比較値として、その距離値を格納します。
 * 行と列の距離マップを作り終えたら、組合せを計算します。
 * <ol>
 * <li>{@link #setMapSize}関数で、マップサイズ（比較する頂点数）を設定する。
 * <li>{@link #setDist},または{@link #setPointDists}で、距離マップに全ての値を書き込む。
 * <li>
 * </ol>
 * </p>
 */
public class NyARDistMap
{
	/** 処理用のデータ型*/
	protected class DistItem
	{
		public int row;
		public int col;
		public int dist;
	}
	/**　距離マップ用の配列*/
	protected DistItem[] _map;
	/**　ワーク変数*/
	protected int _min_dist;
	/**　ワーク変数*/
	protected int _min_dist_index;
	/**　ワーク変数*/
	protected int _size_row;
	/**　ワーク変数*/
	protected int _size_col;
	/**
	 * コンストラクタです。
	 * マップの最大サイズを指定して、インスタンスを作成します。
	 * @param i_max_col
	 * マップの最大列数
	 * @param i_max_row
	 * マップの最大行数
	 */
	public NyARDistMap(int i_max_col,int i_max_row)
	{
		this._min_dist=Integer.MAX_VALUE;
		this._min_dist_index=0;
		this._size_col=i_max_col;
		this._size_row=i_max_row;
		this._map=new DistItem[i_max_col*i_max_row];
		for(int i=0;i<i_max_col*i_max_row;i++){
			this._map[i]=new DistItem();
		}
	}
	/**
	 * この関数は、マップのサイズを指定します。
	 * 値は初期化されません。
	 * @param i_col
	 * 新しい列数。
	 * @param i_row
	 * 新しい行数。
	 */
	public void setMapSize(int i_col,int i_row)
	{
		this._size_row=i_row;
		this._size_col=i_col;
	}
	/**
	 * この関数は、列と行を指定して、距離値1個をマップにセットします。
	 * <p>注意 -
	 * このAPIは低速です。性能が必要な時は、{@link #setPointDists}を参考に、一括書込みする関数を検討してください。
	 * </p>
	 * @param i_col
	 * 列のインデクスを指定します。
	 * @param i_row
	 * 行のインデクスを指定します。
	 * @param i_dist
	 * その行と列の距離値を指定します。
	 */
	public void setDist(int i_col,int i_row,int i_dist)
	{
		this._min_dist_index=this._size_col*i_row+i_col;
		DistItem item=this._map[this._min_dist_index];
		item.col=i_col;
		item.row=i_row;
		item.dist=i_dist;
		//最小値・最大値の再計算
		if(i_dist<this._min_dist){
			this._min_dist=i_dist;
		}
		return;
	}
	/**
	 * この関数は、２つの座標点配列同士の距離値を一括してマップにセットします。
	 * <p>
	 * 実装メモ - 
	 * 点のフォーマットが合わない実装されている場合は、この関数参考にオーバーロードしてください。
	 * </p>
	 * @param i_vertex_r
	 * 比較する頂点群を格納した配列。
	 * @param i_row_len
	 * i_vertex_rの有効な要素数
	 * @param i_vertex_c
	 * 基準となる頂点群を格納した配列
	 * @param i_col_len
	 * i_vertex_cの有効な要素数
	 */
	public void setPointDists(NyARIntPoint2d[] i_vertex_r,int i_row_len,NyARIntPoint2d[] i_vertex_c,int i_col_len)
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
				int d=i_vertex_r[r].sqDist(i_vertex_c[c]);
				map[idx].dist=d;
				if(min_dist>d){
					min_index=idx;
					min_dist=d;
				}
				idx++;
			}
		}
		this._min_dist=min_dist;
		this._min_dist_index=min_index;
		this._size_col=i_col_len;
		this._size_row=i_row_len;
		return;
	}
	/**
	 * この関数は、現在の距離マップから、col要素に対するrow要素の組合せを計算します。
	 * colに対して適したrow要素が見つからない場合は、o_rowindexの該当要素に-1を設定します。
	 * この関数は内部データを不可逆に変更します。計算後は、距離マップの再セットが必要です。
	 * @param o_rowindex
	 * 組合せを受け取る配列です。
	 * col[n]に対するrow[m]のインデクス番号mを、o_rowindex[n]に返します。
	 */
	public void getMinimumPair(int[] o_rowindex)
	{
		DistItem[] map=this._map;
		int map_length=this._size_col*this._size_row;
		int col_len=this._size_col;
		//[0]と差し替え
		DistItem temp_map;
		temp_map=map[0];
		map[0]=map[this._min_dist_index];
		map[this._min_dist_index]=temp_map;
		for(int i=0;i<o_rowindex.length;i++){
			o_rowindex[i]=-1;
		}
		if(map_length==0){
			return;
		}
		//値の保管
		o_rowindex[map[0].col]=map[0].row;
		
		//ソートして、0番目以降のデータを探す
		for(int i=1;i<col_len;i++){
			int min_index=0;
			//r,cのものを除外しながら最小値を得る。
			int reject_c=map[i-1].col;
			int reject_r=map[i-1].row;
			int min_dist=Integer.MAX_VALUE;
			if(1>=map_length-col_len){
				break;
			}
			for(int i2=i;i2<map_length;){
				//除外条件？
				if(map[i2].col==reject_c || map[i2].row==reject_r){
					//非検索対象→インスタンスの差し替えと、検索長の減算
					temp_map=map[i2];
					map[i2]=map[map_length-1];
					map[map_length-1]=temp_map;
					map_length--;
				}else{
					int d=map[i2].dist;
					if(min_dist>d){
						min_index=i2;
						min_dist=d;
					}
					i2++;
				}
			}
			//[i]の値の差し替え
			temp_map=map[i];
			map[i]=map[min_index];
			map[min_index]=temp_map;
			//値の保管
			o_rowindex[map[i].col]=map[i].row;
		}
		return;		
	}
}