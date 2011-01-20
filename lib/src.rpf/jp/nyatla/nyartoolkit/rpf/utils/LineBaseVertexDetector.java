package jp.nyatla.nyartoolkit.rpf.utils;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * このクラスは、4本の直線式から、凸包の頂点を計算する機能を提供します。
 * <p>アルゴリズム -
 * 4直線の交差点0から6の間で変動します。4以上の交差点があるときに、以下の手順で求めます。
 * <ol>
 * <li>4直線に、AからDまでの番号を振る。
 * <li>AとBの交点を0として、AB→AC→AD→BC→BD→CDの順に、交点番号を振る。 
 * <li>0-5,1-4,2-3の頂点を通る直線は存在しない事が判る。
 * <li>{0,1,5,4},{0,2,5,3},{1,2,4,3}の順で頂点を辿った時、4個の外積が0か4になるものが凸包。
 * </ol>
 * 4,5頂点の場合も、頂点順序を辿る始点を変えることで、求められます。
 * </p>
 */
public class LineBaseVertexDetector
{
	/** 頂点の組合せテーブル(4,5頂点用)*/
	private final static int[][] _45vertextable={
			{1,2,4,3},{0,2,5,3},{0,1,5,4},{0,1,5,4},{0,2,5,3},{1,2,4,3}};
	/** 頂点組合せテーブル。(6頂点用)*/
	private final static int[][] _order_table={{0,1,5,4},{0,2,5,3},{1,2,4,3}};
	/** ワーク変数*/
	private NyARDoublePoint2d[] __wk_v=NyARDoublePoint2d.createArray(6);
	/**
	 * ４直線の交点から、凸包の頂点座標を計算します。
	 * 頂点の回転方向は、時計回りに正規化されます。
	 * @param i_line
	 * 直線式の配列です。要素数は4である必要があります。
	 * @param o_point
	 * 検出した頂点の座標です。要素数は4である必要があります。
	 * @return
	 * 凸包を計算できると、trueを返します。
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
	 * 4頂点を巡回して、正の外積数を数えます。
	 * @param p
	 * 頂点配列。4要素である事。
	 * @param order
	 * 頂点のインデクス配列。4要素である事。
	 * @return
	 * 正の外積数
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