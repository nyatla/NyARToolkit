package jp.nyatla.nyartoolkit.markerar.utils;

import java.util.ArrayList;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattDeviationColorData;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattResult;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.INyARPerspectiveCopy;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;


public class ARMarkerList extends ArrayList<MarkerInfoARMarker>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double _configense_th=0.5;
	private final NyARMatchPattResult _patt_result=new NyARMatchPattResult();;
	private final MultiResolutionPattProvider _mpickup=new MultiResolutionPattProvider();
	private ARMarkerSortList _mkmap;
	public ARMarkerList() throws NyARException
	{
		this._mkmap=new ARMarkerSortList();//初期値1マーカ
		return;
	}
	/**
	 * このAdd以外使わないでね。
	 */
	public boolean add(MarkerInfoARMarker i_e)
	{
		//マッチテーブルのサイズを調整
		int s=this.size()+1;
		while(this._mkmap.getLength()<s*s){
			this._mkmap.append();
		}
		return super.add(i_e);
	}
	/**
	 * マーカの一致敷居値を設定する。
	 */
	public void setConficenceTh(double i_th)
	{
		this._configense_th=i_th;
	}
	/**
	 * o_targetsに、敷居値を越えたターゲットリストを返却する。
	 * @param i_pix_drv
	 * @param i_vertex
	 * @param o_targets
	 * @return
	 * @throws NyARException 
	 */
	public boolean update(INyARPerspectiveCopy i_pix_drv,SquareStack.Item i_sq) throws NyARException
	{
		//sq_tmpに値を生成したかのフラグ
		boolean is_ganalated_sq=false;
		for(int i=this.size()-1;i>=0;i--){
			MarkerInfoARMarker target=this.get(i);
			//解像度に一致する画像を取得
			NyARMatchPattDeviationColorData diff=this._mpickup.getDeviationColorData(target, i_pix_drv,i_sq.ob_vertex);
			//マーカのパターン解像度に一致したサンプリング画像と比較する。
			if(!target.matchpatt.evaluate(diff,this._patt_result)){
				continue;
			}
			//敷居値をチェック
			if(this._patt_result.confidence<this._configense_th)
			{
				continue;
			}
			//マーカマップへの追加対象か調べる。
			ARMarkerSortList.Item ip=this._mkmap.getInsertPoint(this._patt_result.confidence);
			if(ip==null){
				continue;
			}
			//マーカマップアイテムの矩形に参照値を設定する。
			ip=this._mkmap.insertFromTailBefore(ip);
			ip.cf=this._patt_result.confidence;
			ip.dir=this._patt_result.direction;
			ip.marker=target;
			ip.ref_sq=i_sq;
			is_ganalated_sq=true;
		}
		return is_ganalated_sq;
	}		
	/**
	 * @param i_num_of_markers
	 * マーカの個数
	 */
	public void prepare()
	{
		//マッチングテーブルをリセット
		this._mkmap.reset();
		
		//検出のために初期値設定
		for(int i=this.size()-1;i>=0;i--){
			MarkerInfoARMarker target=this.get(i);
			if(target.life>0){
				target.lost_count++;
			}
			//検出に利用する一致率のリセット
			target.cf=0;
		}			
	}
	public void finish()
	{
		//一致率の最も高いアイテムを得る。
		ARMarkerSortList.Item top_item=this._mkmap.getTopItem();
		//アイテムを検出できなくなるまで、一致率が高い順にアイテムを得る。
		while(top_item!=null){
			//検出したアイテムのARmarkerIndexのデータをセット
			MarkerInfoARMarker target=top_item.marker;
			if(target.lost_count>0){
				//未割当のマーカのみ検出操作を実行。
				target.cf=top_item.cf;
				target.lost_count=0;//消失カウンタをリセット
				target.life++;      //ライフ値を加算
				target.sq=top_item.ref_sq;
				target.sq.rotateVertexL(4-top_item.dir);
				NyARIntPoint2d.shiftCopy(top_item.ref_sq.ob_vertex,target.tl_vertex,4-top_item.dir);
				target.tl_center.setValue(top_item.ref_sq.center2d);
				target.tl_rect_area=top_item.ref_sq.rect_area;
			}
			//基準アイテムと重複するアイテムを削除する。
			this._mkmap.disableMatchItem(top_item);
			top_item=this._mkmap.getTopItem();
		}
		//消失カウンタが敷居値を越えたら、lifeを0にする。
	}
}