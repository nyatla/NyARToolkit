package jp.nyatla.nyartoolkit.dev.tracking;

import jp.nyatla.nyartoolkit.dev.tracking.detail.NyARDetailTrackItem;
import jp.nyatla.nyartoolkit.dev.tracking.outline.NyAROutlineTrackItem;

public interface INyARMarkerTrackerListener
{
	/**
	 * 対象のトラッキングを開始したことを通知する。
	 * @param i_target
	 */
	public void onEnterTracking(NyARMarkerTracker i_sender,NyARTrackItem i_target);
	/**
	 * 対象の大まかなトラッキングが行われたことを通知する。
	 * @param i_param
	 */
	public void onOutlineUpdate(NyARMarkerTracker i_sender,NyAROutlineTrackItem i_target);
	/**
	 * 対象の詳細なトラッキングが行われたことを通知する。
	 * @param i_param
	 */
	public void onDetailUpdate(NyARMarkerTracker i_sender,NyARDetailTrackItem i_target);
	/**
	 * 対象が消滅したことを通知する。
	 * @param i_target
	 */
	public void onLeaveTracking(NyARMarkerTracker i_sender,NyARTrackItem i_target);
	
}
