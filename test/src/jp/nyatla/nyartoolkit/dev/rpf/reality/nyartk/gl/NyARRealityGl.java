package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk.gl;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk.NyARReality;

/**
 * OpenGLに特化したNyARRealityクラスです。
 * @author nyatla
 */
public class NyARRealityGl extends NyARReality
{
	private NyARDoubleMatrix44 _frustum_rh=new NyARDoubleMatrix44();
	private double[] _gl_frustum_rh=new double[16];
	
	public NyARRealityGl(NyARParam i_param,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target) throws NyARException
	{
		super(i_param,i_max_known_target,i_max_unknown_target);
		//カメラパラメータを計算しておく
		i_param.makeCameraFrustumRH(i_near, i_far,this._frustum_rh);
		this._frustum_rh.getValueT(this._gl_frustum_rh);
	}
	/**
	 * 透視投影行列と視錐体パラメータを元に、インスタンスを作成します。
	 * この関数は、樽型歪み矯正を外部で行うときに使えます。
	 * @param i_prjmat
	 * @param i_screen_w
	 * @param i_screen_h
	 * @param i_near
	 * @param i_far
	 * @param i_max_known_target
	 * @param i_max_unknown_target
	 * @throws NyARException
	 */
	public NyARRealityGl(NyARPerspectiveProjectionMatrix i_prjmat,int i_screen_w,int i_screen_h,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target) throws NyARException
	{
		super(null,i_prjmat,i_max_known_target,i_max_unknown_target);
		//カメラパラメータを計算しておく
		i_prjmat.makeCameraFrustumRH(i_screen_w,i_screen_h,i_near, i_far,this._frustum_rh);
		this._frustum_rh.getValueT(this._gl_frustum_rh);
		
	}

	/**
	 * OpenGLスタイルのProjection行列の参照値を返します。
	 * @return
	 */
	public double[] refGlFrastumRH()
	{
		return this._gl_frustum_rh;
	}
}