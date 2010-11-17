package jp.nyatla.nyartoolkit.dev.rpf.utils;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;
import jp.nyatla.nyartoolkit.core.types.NyARVecLinear2d;
import jp.nyatla.nyartoolkit.dev.rpf.utils.VecLinearCoordinates;

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
	 * 配列の前方に、似たベクトルを集めます。似たベクトルの判定基準は、2線の定義点における直線の法線上での距離の二乗和です。
	 * ベクトルの統合と位置情報の計算には、加重平均を用います。
	 * @param i_vector
	 * 編集するオブジェクトを指定します。
	 * <p>メモ:こちらのほうが正確だけど、SimpleAVEとあまりかわらないので未使用。コードを展開して最適化の余地あり。</p>
	 */
	public void margeResembleCoordsWeightAve(VecLinearCoordinates i_vector)
	{
		VecLinearCoordinates.NyARVecLinearPoint[] items=i_vector.items;
		NyARLinear l1 = this._l1;
		NyARLinear l2 = this._l2;
		NyARDoublePoint2d p = this._p;


		for (int i = i_vector.length - 1; i >= 0; i--) {
			VecLinearCoordinates.NyARVecLinearPoint target1 = items[i];
			if(target1.scalar==0){
				continue;
			}
			double rdx=target1.dx;
			double rdy=target1.dy;
			double rx=target1.x;
			double ry=target1.y;
			l1.setVector(target1);
			double s_tmp=target1.scalar;
			target1.dx*=s_tmp;
			target1.dy*=s_tmp;
			target1.x*=s_tmp;
			target1.y*=s_tmp;
			for (int i2 = i - 1; i2 >= 0; i2--) {
				VecLinearCoordinates.NyARVecLinearPoint target2 = items[i2];
				if(target2.scalar==0){
					continue;
				}
				if (target2.getVecCos(rdx,rdy) >=_SQ_ANG_TH) {
					// それぞれの代表点から法線を引いて、相手の直線との交点を計算する。
					l2.setVector(target2);
					l1.normalLineCrossPos(rx, ry,l2, p);
					double wx, wy;
					double l = 0;
					// 交点間の距離の合計を計算。lに2*dist^2を得る。
					wx = (p.x - rx);
					wy = (p.y - ry);
					l += wx * wx + wy * wy;
					l2.normalLineCrossPos(target2.x, target2.y,l2, p);
					wx = (p.x - target2.x);
					wy = (p.y - target2.y);
					l += wx * wx + wy * wy;
					// 距離が一定値以下なら、マージ
					if (l > _SQ_DIFF_DOT_TH) {
						continue;
					}
					// 似たようなベクトル発見したら、後方のアイテムに値を統合。
					s_tmp= target2.scalar;
					target1.x+= target2.x*s_tmp;
					target1.y+= target2.y*s_tmp;
					target1.dx += target2.dx*s_tmp;
					target1.dy += target2.dy*s_tmp;
					target1.scalar += s_tmp;
					//要らない子を無効化しておく。
					target2.scalar=0;
				}
			}
		}
		//前方詰め
		i_vector.removeZeroDistItem();
		//加重平均解除なう(x,y位置のみ)
		for(int i=0;i<i_vector.length;i++)
		{
			VecLinearCoordinates.NyARVecLinearPoint ptr=items[i];
			ptr.x/= ptr.scalar;
			ptr.y/= ptr.scalar;
		}
	}
/*
	public void margeResembleCoordsWeightAve(VecLinearCoordinates i_vector)
	{
		VecLinearCoordinates.NyARVecLinearPoint[] items=i_vector.items;
		NyARLinear l1 = this._l1;
		NyARLinear l2 = this._l2;
		NyARDoublePoint2d p = this._p;


		for (int i = i_vector.length - 1; i >= 0; i--) {
			VecLinearCoordinates.NyARVecLinearPoint target1 = items[i];
			if(target1.scalar==0){
				continue;
			}
			double rdx=target1.dx;
			double rdy=target1.dy;
			double rx=target1.x;
			double ry=target1.y;
			l1.setVector(target1);
			double s_tmp=target1.scalar;
			target1.dx*=s_tmp;
			target1.dy*=s_tmp;
			target1.x*=s_tmp;
			target1.y*=s_tmp;
			for (int i2 = i - 1; i2 >= 0; i2--) {
				VecLinearCoordinates.NyARVecLinearPoint target2 = items[i2];
				if(target2.scalar==0){
					continue;
				}
				if (target2.getVecCos(rdx,rdy) >=_SQ_ANG_TH) {
					// それぞれの代表点から法線を引いて、相手の直線との交点を計算する。
					l2.setVector(target2);
					l1.normalLineCrossPos(rx, ry,l2, p);
					double wx, wy;
					double l = 0;
					// 交点間の距離の合計を計算。lに2*dist^2を得る。
					wx = (p.x - rx);
					wy = (p.y - ry);
					l += wx * wx + wy * wy;
					l2.normalLineCrossPos(target2.x, target2.y,l2, p);
					wx = (p.x - target2.x);
					wy = (p.y - target2.y);
					l += wx * wx + wy * wy;
					// 距離が一定値以下なら、マージ
					if (l > _SQ_DIFF_DOT_TH) {
						continue;
					}
					// 似たようなベクトル発見したら、後方のアイテムに値を統合。
					s_tmp= target2.scalar;
					target1.x+= target2.x*s_tmp;
					target1.y+= target2.y*s_tmp;
					target1.dx += target2.dx*s_tmp;
					target1.dy += target2.dy*s_tmp;
					target1.scalar += s_tmp;
					//要らない子を無効化しておく。
					target2.scalar=0;
				}
			}
		}
		//前方詰め
		i_vector.removeZeroDistItem();
		//加重平均解除なう(x,y位置のみ)
		for(int i=0;i<i_vector.length;i++)
		{
			VecLinearCoordinates.NyARVecLinearPoint ptr=items[i];
			ptr.x/= ptr.scalar;
			ptr.y/= ptr.scalar;
		}
	}
 */
	public void margeResembleCoordsSimpleAve(VecLinearCoordinates i_vector)
	{
		VecLinearCoordinates.NyARVecLinearPoint[] items=i_vector.items;
		NyARLinear l1 = this._l1;
		NyARLinear l2 = this._l2;
		NyARDoublePoint2d p = this._p;
		//ベクトルの制限。1,2象限に集める。


		for (int i = i_vector.length - 1; i >= 0; i--) {
			VecLinearCoordinates.NyARVecLinearPoint target1 = items[i];
			if(target1.scalar==0){
				continue;
			}

			double rx=target1.x;
			double ry=target1.y;
			l1.setVector(target1);
			for (int i2 = i - 1; i2 >= 0; i2--) {
				VecLinearCoordinates.NyARVecLinearPoint target2 = items[i2];
				if(target2.scalar==0){
					continue;
				}
				if (target2.getVecCos(target1) >=_SQ_ANG_TH) {
					// それぞれの代表点から法線を引いて、相手の直線との交点を計算する。
					l2.setVector(target2);
					l1.normalLineCrossPos(rx, ry,l2, p);
					double wx, wy;
					double l = 0;
					// 交点間の距離の合計を計算。lに2*dist^2を得る。
					wx = (p.x - rx);
					wy = (p.y - ry);
					l += wx * wx + wy * wy;
					l2.normalLineCrossPos(target2.x, target2.y,l2, p);
					wx = (p.x - target2.x);
					wy = (p.y - target2.y);
					l += wx * wx + wy * wy;
					// 距離が一定値以下なら、マージ
					if (l > _SQ_DIFF_DOT_TH) {
						continue;
					}
					// 似たようなベクトル発見したら、後方のアイテムに値を統合。
					target1.x= (target1.x+target2.x)*0.5;
					target1.y= (target1.y+target2.y)*0.5;
					target1.dx +=target2.dx;
					target1.dy +=target2.dy;
					target1.scalar += target2.scalar;
					//要らない子を無効化しておく。
					target2.scalar=0;
				}
			}
		}
		//前方詰め
		i_vector.removeZeroDistItem();
	}	
	/**
	 * 配列の前方に、似たベクトルを集めます。似たベクトルの判定基準は、2線の定義点における直線の法線上での距離の二乗和です。
	 * ベクトルの統合は単純加算、位置情報は単純な平均値で計算します。
	 * @param i_vector
	 * 編集するオブジェクトを指定します。
	 * @return
	 */
	public void margeResembleCoords(VecLinearCoordinates i_vector)
	{
//		margeResembleCoordsSimpleAve(i_vector);
		margeResembleCoordsWeightAve(i_vector);
	}

}
