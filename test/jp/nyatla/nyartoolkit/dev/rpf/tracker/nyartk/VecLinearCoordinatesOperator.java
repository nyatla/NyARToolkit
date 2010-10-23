package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.NyARVecLinear2d;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.VecLinearCoordinates;

public class VecLinearCoordinatesOperator
{
	/**
	 * margeResembleCoordsで使う距離敷居値の値です。
	 * 許容する((距離^2)*2)を指定します。
	 */
	private final static int _SQ_DIFF_DOT_TH=((5*5) * 2);
	/**
	 * margeResembleCoordsで使う角度敷居値の値です。
	 * Cos(n)の値です。
	 */
	private final static double _SQ_ANG_TH=0.99;

	//ワーク
	private NyARLinear _l1 = new NyARLinear();
	private NyARLinear _l2 = new NyARLinear();
	private NyARDoublePoint2d _p = new NyARDoublePoint2d();
	
	/**
	 * 配列の前方に、似たベクトルを集めます。判定基準は、もう一方のベクトルのなす角度、ベクトルの定義位置と、そこ
	 * から引いた法線ともう一方のベクトルの交点の距離の２つです。
	 * @param i_vector
	 * 編集するオブジェクトを指定します。
	 * @return
	 */
	public void margeResembleCoords(VecLinearCoordinates i_vector)
	{
		VecLinearCoordinates.CoordData[] items=i_vector.items;
		NyARLinear l1 = this._l1;
		NyARLinear l2 = this._l2;
		NyARDoublePoint2d p = this._p;
		//ベクトルの制限。1,2象限に集める。


		for (int i = i_vector.length - 1; i > 0; i--) {
			VecLinearCoordinates.CoordData target1 = items[i];
			if(target1.sq_dist==0){
				continue;
			}
			for (int i2 = i - 1; i2 >= 0; i2--) {
				VecLinearCoordinates.CoordData target2 = items[i2];
				if(target2.sq_dist==0){
					continue;
				}
				if (target1.getVecCos(target2) >= _SQ_ANG_TH) {
					// それぞれの代表点から法線を引いて、相手の直線との交点を計算する。

					l1.setVector(target1);
					l2.setVector(target2);
					l1.normalLineCrossPos(l2, target1.x, target1.y, p);
					double wx, wy;
					double l = 0;
					// 交点間の距離の合計を計算。lに2*dist^2を得る。
					wx = (p.x - target1.x);
					wy = (p.y - target1.y);
					l += wx * wx + wy * wy;
					l2.normalLineCrossPos(l2, target2.x, target2.y,p);
					wx = (p.x - target2.x);
					wy = (p.y - target2.y);
					l += wx * wx + wy * wy;
					// 距離が一定値以下なら、マージ
					if (l > _SQ_DIFF_DOT_TH) {
						continue;
					}
					// 似たようなベクトル発見したら、前方のアイテムに値を統合。
					target2.x = (target1.x + target2.x) / 2;
					target2.y = (target1.y + target2.y) / 2;
					target2.dx += target1.dx;
					target2.dy += target1.dy;
					target2.sq_dist += target1.sq_dist;
					//要らない子を無効化しておく。
					target1.sq_dist=0;
				}
			}
		}
		//前方詰め
		i_vector.removeZeroDistItem();
	}
}
