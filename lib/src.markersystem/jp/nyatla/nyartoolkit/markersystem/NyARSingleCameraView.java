package jp.nyatla.nyartoolkit.markersystem;

import jp.nyatla.nyartoolkit.core.param.NyARFrustum;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.stack.NyARPointerStack;

/**
 * 1視点を管理するシングルカメラのビューです。
 */
public class NyARSingleCameraView
{
	/** 定数値。視錐台のFARパラメータの初期値[mm]です。*/
	final public static double FRUSTUM_DEFAULT_FAR_CLIP=10000;
	/** 定数値。視錐台のNEARパラメータの初期値[mm]です。*/
	final public static double FRUSTUM_DEFAULT_NEAR_CLIP=10;
	
	final protected NyARParam _ref_param;
	final protected NyARFrustum _frustum;	
	public NyARSingleCameraView(NyARParam i_ref_cparam)
	{
		this._ref_param=i_ref_cparam;
		this._frustum=new NyARFrustum();
		this._observer=new ObserverList(3);
		this.setClipping(FRUSTUM_DEFAULT_NEAR_CLIP, FRUSTUM_DEFAULT_FAR_CLIP);
		return;
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
	public void setClipping(double i_near,double i_far)
	{
		NyARIntSize s=this._ref_param.getScreenSize();
		this._frustum.setValue(this._ref_param.getPerspectiveProjectionMatrix(),s.w,s.h,i_near,i_far);
		//イベントの通知
		this._observer.notifyOnUpdateCameraParametor(this._ref_param,i_near,i_far);
	}
	final protected ObserverList _observer;
	
	

	/**
	 * イベントリストクラス
	 */
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
