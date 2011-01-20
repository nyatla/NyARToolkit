package jp.nyatla.nyartoolkit.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.utils.NyARManagedObjectPool;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.NyARTarget;

/**
 * このクラスは、{@link NyARRealityTarget}のオブジェクトプールを定義します。
 * 基本的にユーザが生成することはありません。
 * {@link NyARReality}がメンバ変数としてオブジェクトを所有します。
 */
public class NyARRealityTargetPool extends NyARManagedObjectPool<NyARRealityTarget>
{
	/** 所有する{@link NyARRealityTarget}が共有する計算インスタンスの参照値*/
	public NyARPerspectiveProjectionMatrix _ref_prj_mat;
	/** 所有する{@link NyARRealityTarget}が共有する一時オブジェクト*/
	public NyARDoublePoint3d[] _wk_da3_4=NyARDoublePoint3d.createArray(4);
	public NyARDoublePoint2d[] _wk_da2_4=NyARDoublePoint2d.createArray(4);
	/**
	 * コンストラクタ。
	 * @param i_size
	 * プールの最大サイズです。
	 * @param i_ref_prj_mat
	 * 計算インスタンスの参照値です。
	 * 所有する{@link NyARRealityTarget}が射影変換をするときに使います。
	 * @throws NyARException
	 */
	public NyARRealityTargetPool(int i_size,NyARPerspectiveProjectionMatrix i_ref_prj_mat) throws NyARException
	{
		this.initInstance(i_size,NyARRealityTarget.class);
		this._ref_prj_mat=i_ref_prj_mat;
		return;
	}
	/**
	 * 要素のインスタンスを生成します。
	 */
	protected NyARRealityTarget createElement() throws NyARException
	{
		return new NyARRealityTarget(this);
	}
	/**
	 * オブジェクトプールから、新しい{@link NyARRealityTarget}を割り当てます。
	 * @param tt
	 * ラップするトラックターゲットオブジェクト
	 * @return
	 * 成功すれば初期化済みのオブジェクト。失敗するとnull
	 * @throws NyARException 
	 */
	public NyARRealityTarget newNewTarget(NyARTarget tt) throws NyARException
	{
		NyARRealityTarget ret=super.newObject();
		if(ret==null){
			return null;
		}
		ret._grab_rate=50;//開始時の捕捉レートは10%
		ret._ref_tracktarget=(NyARTarget) tt.refObject();
		ret._serial=NyARRealityTarget.createSerialId();
		ret.tag=null;
		tt.tag=ret;//トラックターゲットのタグに自分の値設定しておく。
		return ret;
	}	
}