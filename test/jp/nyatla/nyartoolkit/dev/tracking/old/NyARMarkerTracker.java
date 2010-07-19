package jp.nyatla.nyartoolkit.dev.tracking.old;

import jp.nyatla.nyartoolkit.dev.tracking.detail.NyARDetailTrackItem;
import jp.nyatla.nyartoolkit.dev.tracking.old.outline.NyAROutlineTrackItem;

public abstract class NyARMarkerTracker
{
	//イベントハンドラ
	/**
	 * 対象のトラッキングを開始したことを通知する。
	 * @param i_target
	 */
	public abstract void onEnterTracking(NyARTrackItem i_target);
	/**
	 * 対象の大まかなトラッキングが行われたことを通知する。
	 * @param i_param
	 */
	public abstract void onOutlineUpdate(NyAROutlineTrackItem i_target);
	/**
	 * 対象の詳細なトラッキングが行われたことを通知する。
	 * @param i_param
	 */
	public abstract void onDetailUpdate(NyARDetailTrackItem i_target);
	/**
	 * 対象が消滅したことを通知する。
	 * @param i_target
	 */
	public abstract void onLeaveTracking(NyARTrackItem i_target);	
}
