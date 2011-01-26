package jp.nyatla.nyartoolkit.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.rpf.sampler.lrlabel.*;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status.*;

/**
 * このクラスは、RECTトラックターゲットのリストです。
 * {@link NyARTargetList}クラスの矩形検索関数を置き換えます。
 *
 */
public class NyARRectTargetList extends NyARTargetList
{
	/**
	 * コンストラクタです。
	 * リストの最大サイズを指定して、インスタンスを生成します。
	 * @param iMaxTarget
	 * リストの最大サイズ
	 * @throws NyARException
	 */
	public NyARRectTargetList(int iMaxTarget) throws NyARException
	{
		super(iMaxTarget);
	}
	/**
	 * この関数は、サンプルアイテムに最も近いアイテムのリストインデクスを検索します。
	 * ベースクラスの関数との違いは、予想矩形位置も探索範囲に含む事です。
	 */
	public int getMatchTargetIndex(LowResolutionLabelingSamplerOut.Item i_item)
	{
		//1段目:通常の検索
		int ret=super.getMatchTargetIndex(i_item);
		if(ret>=0){
			return ret;
		}
		//2段目:予測位置から検索
		NyARRectTargetStatus iitem;
		int min_d=Integer.MAX_VALUE;

		//対角範囲の距離が、対角距離の1/2以下で、最も小さいこと。
		for(int i=this._length-1;i>=0;i--)
		{
			iitem=(NyARRectTargetStatus)this._items[i]._ref_status;
			int d;
			d=i_item.base_area.sqDiagonalPointDiff(iitem.estimate_rect);	
			if(d<min_d){
				min_d=d;
				ret=i;
			}
		}
		//許容距離誤差の2乗を計算(対角線の20%以内)
		//(Math.sqrt((i_item.area.w*i_item.area.w+i_item.area.h*i_item.area.h))/5)^2
		if(min_d<(2*(i_item.base_area_sq_diagonal)/25)){
			return ret;
		}
		return -1;
	}

	
}