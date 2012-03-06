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
package jp.nyatla.nyartoolkit.markersystem.utils;

import java.util.ArrayList;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;


public class TrackingList extends ArrayList<TMarkerData>
{
	private static final long serialVersionUID = -6446466460932931830L;
	private VertexSortTable _tracking_list;
	public TrackingList() throws NyARException
	{
		this._tracking_list=new VertexSortTable(1);
	}
	public void prepare()
	{
		//トラッキングリストをリセット
		this._tracking_list.reset();
	}
	private int[] __ret=new int[2];
	public boolean update(SquareStack.Item i_new_sq) throws NyARException
	{
		int[] ret=this.__ret;
		int new_area=i_new_sq.rect_area;
		//頂点の対角距離
		int new_sq_dist=i_new_sq.vertex_area.getDiagonalSqDist();
		boolean is_dispatched=false;
		for(int i=this.size()-1;i>=0;i--)
		{
			TMarkerData target=this.get(i);
			if(target.lost_count>1){
				continue;
			}
			//面積比が急激0.8-1.2倍以外の変動なら無視
			int a_rate=new_area*100/target.tl_rect_area;
			if(a_rate<50 || 150<a_rate){
				continue;
			}
			//移動距離^2の二乗が対角線距離^2の4倍以上なら無視
			long sq_move=target.tl_center.sqDist(i_new_sq.center2d);
			if(sq_move*4/new_sq_dist>0){
				continue;
			}
			compareVertexSet(i_new_sq.ob_vertex,target.tl_vertex,ret);
			int sqdist=ret[1];
			int shift=ret[0];
			//頂点移動距離の合計が、(中心点移動距離+4)の10倍を超えてたらNG <-
			if(sqdist>(sq_move+8)*10){
				continue;
			}
			//登録可能か確認
			VertexSortTable.Item item=this._tracking_list.getInsertPoint(sqdist);
			if(item==null){
				continue;
			}
			//登録
			item=this._tracking_list.insertFromTailBefore(item);
			item.marker=target;
			item.shift=shift;
			item.sq_dist=sqdist;
			item.ref_sq=i_new_sq;
			is_dispatched=true;
		}
		return is_dispatched;
	}

	/**
     * この関数は、頂点セット同士のシフト量を計算して、配列に値を返します。
     * 並びが同じである頂点セット同士の最低の移動量を計算して、その時のシフト量と二乗移動量の合計を返します。
     * @param i_square
     * 比較対象の矩形
     * @return
     * [0]にシフト量を返します。
     * [1]に頂点移動距離の合計の二乗値を返します。
     * シフト量はthis-i_squareです。1の場合、i_v1[0]とi_v2[1]が対応点になる(shift量1)であることを示します。
     */
    public static void compareVertexSet(NyARIntPoint2d[] i_v1,NyARIntPoint2d[] i_v2,int[] ret)
    {
    	//3-0番目
    	int min_dist=Integer.MAX_VALUE;
    	int min_index=0;
    	int xd,yd;
    	for(int i=3;i>=0;i--){
    		int d=0;
    		for(int i2=3;i2>=0;i2--){
    			xd= (int)(i_v1[i2].x-i_v2[(i2+i)%4].x);
    			yd= (int)(i_v1[i2].y-i_v2[(i2+i)%4].y);
    			d+=xd*xd+yd*yd;
    		}
    		if(min_dist>d){
    			min_dist=d;
    			min_index=i;
    		}
    	}
    	ret[0]=min_index;
    	ret[1]=min_dist;
    }
    /**
     * トラッキングリストへ追加。このadd以外使わないでね。
     */
	public boolean add(TMarkerData e)
	{
		//1マーカ辺りの最大候補数
		for(int i=0;i<2;i++){
			this._tracking_list.append();
		}
		return super.add(e);
	}
	public void finish()
	{
		//一致率の最も高いアイテムを得る。
		VertexSortTable.Item top_item=this._tracking_list.getTopItem();
		//アイテムを検出できなくなるまで、一致率が高い順にアイテムを得る。
		while(top_item!=null){
			//検出したアイテムのARmarkerIndexのデータをセット
			TMarkerData target=top_item.marker;
			//検出カウンタが1以上（未検出の場合のみ検出）
			if(target.lost_count>0){
				target.lost_count=0;
				target.life++;
				target.sq=top_item.ref_sq;
				target.sq.rotateVertexL(4-top_item.shift);
				NyARIntPoint2d.shiftCopy(top_item.ref_sq.ob_vertex,target.tl_vertex,4-top_item.shift);
				target.tl_center.setValue(top_item.ref_sq.center2d);
				target.tl_rect_area=top_item.ref_sq.rect_area;
			}
			//基準アイテムと重複するアイテムを削除する。
			this._tracking_list.disableMatchItem(top_item);
			top_item=this._tracking_list.getTopItem();
		}
	}	
}
