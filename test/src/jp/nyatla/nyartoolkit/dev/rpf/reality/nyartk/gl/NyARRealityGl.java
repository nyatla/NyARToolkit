package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk.gl;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk.NyARReality;

/**
 * OpenGLに特化したNyARRealityクラスです。
 * @author nyatla
 */
public class NyARRealityGl extends NyARReality
{
	private double[] _gl_frustum_rh=new double[16];
	
	public NyARRealityGl(NyARParam i_param,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target) throws NyARException
	{
		super(i_param,i_near,i_far,i_max_known_target,i_max_unknown_target);
		//カメラパラメータを計算しておく
		this._frustum_rh.getValueT(this._gl_frustum_rh);
	}
	/**
	 * 透視投影行列と視錐体パラメータを元に、インスタンスを作成します。
	 * この関数は、樽型歪み矯正を外部で行うときに使います。
	 * @param i_prjmat
	 * ARToolKitスタイルのカメラパラメータです。通常は{@link NyARParam#getPerspectiveProjectionMatrix()}から得られた値を使います。
	 * @param i_screen_size
	 * スクリーン（入力画像）のサイズです。通常は{@link NyARParam#getScreenSize()}から得られた値を使います。
	 * @param i_near
	 * 視錐体のnear-pointをmm単位で指定します。
	 * default値は{@link #FRASTRAM_ARTK_NEAR}です。
	 * @param i_far
	 * 視錐体のfar-pointをmm単位で指定します。
	 * default値は{@link #FRASTRAM_ARTK_FAR}です。
	 * @param i_max_known_target
	 * @param i_max_unknown_target
	 * @throws NyARException
	 */
	public NyARRealityGl(NyARPerspectiveProjectionMatrix i_prjmat,NyARIntSize i_screen_size,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target) throws NyARException
	{
		super(i_screen_size,i_near,i_far,i_prjmat,null,i_max_known_target,i_max_unknown_target);
		//カメラパラメータを取得しておく。
		this._frustum_rh.getValueT(this._gl_frustum_rh);
	}

	/**
	 * OpenGLスタイルのProjection行列の参照値を返します。
	 * この値は、直接OpenGLの処理系へセットできます。
	 * @return
	 */
	public double[] refGLFrustumRhMatrix()
	{
		return this._gl_frustum_rh;
	}

}