package jp.nyatla.nyartoolkit.markerar.utils;

import jp.nyatla.nyartoolkit.NyARException;




public class MarkerInfoNyId extends TMarkerData
{
	/** MK_NyIdの情報。 反応するidの開始レンジ*/
	public final long nyid_range_s;
	/** MK_NyIdの情報。 反応するidの終了レンジ*/
	public final long nyid_range_e;
	/** MK_NyIdの情報。 実際のid値*/
	public long nyid;
	public int dir;
	/**
	 * コンストラクタです。初期値から、Idマーカのインスタンスを生成します。
	 * @param i_range_s
	 * @param i_range_e
	 * @param i_patt_size
	 * @throws NyARException
	 */
	public MarkerInfoNyId(int i_nyid_range_s,int i_nyid_range_e,double i_patt_size)
	{
		this.marker_offset.setSquare(i_patt_size);
		this.nyid_range_s=i_nyid_range_s;
		this.nyid_range_e=i_nyid_range_e;
		return;
	}		
}
