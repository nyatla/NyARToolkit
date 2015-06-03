/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.markersystem;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARFrustum;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;
public class NyARSingleCameraSystem
{
	/** 定数値。視錐台のFARパラメータの初期値[mm]です。*/
	public final static double FRUSTUM_DEFAULT_FAR_CLIP=10000;
	/** 定数値。視錐台のNEARパラメータの初期値[mm]です。*/
	public final static double FRUSTUM_DEFAULT_NEAR_CLIP=10;
	
	protected NyARParam _ref_param;
	protected NyARFrustum _frustum;	
	protected NyARSingleCameraSystem(NyARParam i_ref_cparam) throws NyARException
	{
		this._observer=new ObserverList(3);
		this._ref_param=i_ref_cparam;
		this._frustum=new NyARFrustum();
		this.setProjectionMatrixClipping(FRUSTUM_DEFAULT_NEAR_CLIP, FRUSTUM_DEFAULT_FAR_CLIP);
		
	}
	/**
	 * [readonly]
	 * 現在のフラスタムオブジェクトを返します。
	 * @return
	 */
	public NyARFrustum getFrustum()
	{
		return this._frustum;
	}
    /**
     * [readonly]
     * 現在のカメラパラメータオブジェクトを返します。
     * @return
     */
    public NyARParam getARParam()
    {
        return this._ref_param;
    }
	/**
	 * 視錐台パラメータを設定します。
	 * この関数は、値を更新後、登録済の{@link IObserver}オブジェクトへ、{@link #EV_UPDATE}通知を送信します。
	 * @param i_near
	 * 新しいNEARパラメータ
	 * @param i_far
	 * 新しいFARパラメータ
	 */
	public void setProjectionMatrixClipping(double i_near,double i_far)
	{
		NyARIntSize s=this._ref_param.getScreenSize();
		this._frustum.setValue(this._ref_param.getPerspectiveProjectionMatrix(),s.w,s.h,i_near,i_far);
		//イベントの通知
		this._observer.notifyOnUpdateCameraParametor(this._ref_param,i_near,i_far);
	}	
	
	
	//
	//	イベント通知系
	//
	protected class ObserverList extends NyARPointerStack<INyARSingleCameraSystemObserver>
	{
		public ObserverList(int i_length){
			super(i_length,INyARSingleCameraSystemObserver.class);
		}
		public void notifyOnUpdateCameraParametor(NyARParam i_param,double i_near,double i_far)
		{
			for(int i=0;i<this._length;i++){
				this._items[i].onUpdateCameraParametor(i_param,i_near,i_far);
			}
		}
	}
	protected ObserverList _observer;
	/**
	 * {@link NyARSingleCameraSystem}のイベント通知リストへオブザーバを追加します。
	 * この関数は、オブザーバが起動時に使用します。ユーザが使用することは余りありません。
	 * @param i_observer
	 * 通知先のオブザーバオブジェクト
	 */
	public void addObserver(INyARSingleCameraSystemObserver i_observer)
	{
		this._observer.pushAssert(i_observer);
		NyARFrustum.FrustumParam f=this.getFrustum().getFrustumParam(new NyARFrustum.FrustumParam());
		i_observer.onUpdateCameraParametor(this._ref_param, f.near, f.far);		
	}
}
