package jp.nyatla.nyartoolkit.rpf.utils;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、直線式の集合から頂点集合を計算する関数を提供します。
 */
public class LineBaseVertexDetector
{
	/**
	 * 頂点パターンテーブル
	 * 頂点用の、存在しないIDに対応した、調査テーブル。4頂点の時も使う。
	 */
	private final static int[][] _45vertextable={
			{1,2,4,3},{0,2,5,3},{0,1,5,4},{0,1,5,4},{0,2,5,3},{1,2,4,3}};
	/**
	 * 頂点パターンテーブル(6用)
	 */
	private final static int[][] _order_table={{0,1,5,4},{0,2,5,3},{1,2,4,3}};
	private NyARDoublePoint2d[] __wk_v=NyARDoublePoint2d.createArray(6);
	/**
	 * 適当に与えられた4線分から、四角形の頂点を計算する。
	 * @param i_line
	 * 4線分を格納した配列
	 * @param o_point
	 * 検出した4頂点
	 * @return
	 * 四角形を検出したらtrue
	 * @throws NyARException
	 */

	public boolean line2SquareVertex(VecLinearCoordinates.VecLinearCoordinatePoint[] i_line,NyARDoublePoint2d[] o_point) throws NyARException
	{
		
		NyARDoublePoint2d[] v=this.__wk_v;
		int number_of_vertex=0;
		int non_vertexid=0;
		int ptr=0;
		for(int i=0;i<3;i++){
			for(int i2=i+1;i2<4;i2++){
				if(i_line[i].crossPos(i_line[i2],v[ptr])){
					number_of_vertex++;
				}else{
					non_vertexid=ptr;
				}
				ptr++;
			}
		}
		int num_of_plus=-1;
		int[] target_order;
		switch(number_of_vertex){
		case 4:
		case 5:
			//正の外積の数を得る。0,4ならば、目的の図形
			num_of_plus=countPlusExteriorProduct(v,_45vertextable[non_vertexid]);
			target_order=_45vertextable[non_vertexid];
			break;
		case 6:
			//(0-5),(1-4),(2-3)の頂点ペアの組合せを試す。頂点の検索順は、(0,1,5,4),(0,2,5,3),(1,2,4,3)
			//3パターンについて、正の外積の数を得る。0,4のものがあればOK
			int order_id=-1;
			num_of_plus=-1;
			for(int i=0;i<3;i++){
				num_of_plus=countPlusExteriorProduct(v,_order_table[i]);
				if(num_of_plus%4==0){
					order_id=i;
					break;
				}
			}
			if(order_id==-1){
				return false;
			}
			target_order=_order_table[order_id];
			break;
		default:
			//他の頂点数の時はNG
			return false;
		}
		//回転方向の正規化(ここパラメータ化しようよ)
		switch(num_of_plus){
		case  0:
			//逆回転で検出した場合
			for(int i=0;i<4;i++){
				o_point[i].setValue(v[target_order[3-i]]);
			}
			break;
		case  4:
			//正回転で検出した場合
			for(int i=0;i<4;i++){
				o_point[i].setValue(v[target_order[i]]);
			}
			break;
		default:
			return false;
		}
		return true;
	}

	/**
	 * 4頂点を巡回して、正の外積の個数を数える。
	 * @param p
	 * @param order
	 * @return
	 */
	private final static int countPlusExteriorProduct(NyARDoublePoint2d[] p,int[] order)
	{
		int ret=0;
		for(int i=0;i<4;i++){
			if(0<NyARDoublePoint2d.crossProduct3Point(p[order[i+0]],p[order[(i+1)%4]],p[order[(i+2)%4]])){
				ret++;
			}
		}
		return ret;
	}
}