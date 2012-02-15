package jp.nyatla.nyartoolkit.markersystem.utils;

import jp.nyatla.nyartoolkit.core.transmat.NyARRectOffset;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;

/**
 * マーカ情報を格納するためのクラスです。
 */
public class TMarkerData
{
	/** 最後に認識したタイムスタンプ。*/
	public long time_stamp;
	/** ライフ値
	 * マーカ検出時にリセットされ、1フレームごとに1づつインクリメントされる値です。
	 */
	public long life;
	/** MK情報。マーカのオフセット位置。*/
	public final NyARRectOffset marker_offset=new NyARRectOffset();			
	/** 検出した矩形の格納変数。理想形二次元座標を格納します。*/
	public SquareStack.Item sq;
	/** 検出した矩形の格納変数。マーカの姿勢行列を格納します。*/
	public final NyARTransMatResult tmat=new NyARTransMatResult();
	/** 矩形の検出状態の格納変数。 連続して見失った回数を格納します。*/
	public int lost_count=Integer.MAX_VALUE;
	/** トラッキングログ用の領域*/
	public NyARIntPoint2d[] tl_vertex=NyARIntPoint2d.createArray(4);
	public NyARIntPoint2d   tl_center=new NyARIntPoint2d();
	public int tl_rect_area;
	protected TMarkerData()
	{
		this.life=0;
	}
}	