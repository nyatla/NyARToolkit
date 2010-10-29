package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARRectTargetStatus;
import jp.nyatla.nyartoolkit.nyidmarker.*;

public class NyARRealityDB
{
	public class RealityData
	{
		final static int DT_ARTK16=0;
		final static int DT_NYID=1;
		public int data_type;
		private NyIdMarkerPattern nyid_info;
		private int artk_index;
	}
	final static int RET_RECALL_FAILED=-1;
	/**
	 * recall操作に成功して、ターゲットのステータスを更新した。
	 */
	final static int RET_RECALL_SUCCESS=1;
	/**
	 * このターゲットは、非同期RECALLを実行中である。
	 */
	final static int RET_RECALL_ASYNC_WAIT=2;
	public interface IRcallResponse
	{
		public void onRecalled(long i_serial,double i_width,double i_height,int i_direction);
	}
	/**
	 * Targetリソースにアクセスする時に必要な同期オブジェクト
	 */
	public Object _ref_sync_object;
	public NyARRealityDB(Object i_sync_object)
	{
		
	}
	private final NyIdMarkerPickup _id_pickup = new NyIdMarkerPickup();

	public int recall(NyARRealityTarget i_target)
	{
		INyARRgbRaster raster;
		//リコールされた内容を解析する。
		NyARRealityDB.RealityData data;
		//Idマーカとして解析してみる。
		NyIdMarkerParam idparam;
		NyARDoublePoint2d[] vx=((NyARRectTargetStatus)(i_target.ref_ttarget.ref_status)).vertex;
		if(this._id_pickup.pickFromRaster(raster,vx,data.nyid_info,idparam)){
//id->サイズのライブラリ作れ。それまで暫定
			i_target.offset.setSquare(1);
			data.data_type=NyARRealityDB.RealityData.DT_NYID;
		}
		//ターゲットの基本パラメータを設定。
		
		//ダメならローカルパターンの一致検索をしてみる
		return false;
	}
}
