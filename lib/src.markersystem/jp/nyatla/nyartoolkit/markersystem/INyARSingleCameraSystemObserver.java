package jp.nyatla.nyartoolkit.markersystem;

import jp.nyatla.nyartoolkit.core.param.NyARParam;

/**
 * 管理システムの発行するイベントを処理するインタフェイスです。
 */
public interface INyARSingleCameraSystemObserver
{
	/**
	 * カメラパラメータが更新されたことを通知します。
	 * @param i_param
	 * @param i_near
	 * @param i_far
	 */
	public void onUpdateCameraParametor(NyARParam i_param,double i_near,double i_far);
}
