package jp.nyatla.nyartoolkit.rpf.reality.nyartk.gl;

import javax.media.opengl.GL;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.NyARReality;
import jp.nyatla.nyartoolkit.rpf.realitysource.nyartk.NyARRealitySource;
import jp.nyatla.nyartoolkit.jogl.utils.NyARGLDrawUtil;
import jp.nyatla.nyartoolkit.jogl.utils.NyARGLUtil;

/**
 * OpenGLに特化したNyARRealityクラスです。
 * @author nyatla
 */
public class NyARRealityGl extends NyARReality
{
	private double[] _gl_frustum_rh=new double[16];
	/**
	 * ARToolKitスタイルのModelView行列を、OpenGLスタイルのモデルビュー行列に変換します。
	 * @param i_ny_style_mat
	 * @param o_gl_style_mat
	 */
	public static void toGLViewMat(NyARDoubleMatrix44 i_ny_style_mat,double[] o_gl_style_mat)
	{
		NyARGLUtil.toCameraViewRH(i_ny_style_mat, 1, o_gl_style_mat);
	}
	
	public NyARRealityGl(NyARParam i_param,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target) throws NyARException
	{
		super(i_param,i_near,i_far,i_max_known_target,i_max_unknown_target);
		//カメラパラメータを取得しておく。
		this._frustum.refMatrix().getValueT(this._gl_frustum_rh);
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
		this._frustum.refMatrix().getValueT(this._gl_frustum_rh);
	}
	
	private double[] _temp=new double[16];
	/**
	 * NyARToolKitの姿勢変換行列をOpenGLスタックへロードします。
	 * @throws NyARException 
	 */
	public void glLoadModelViewMatrix(GL i_gl,NyARDoubleMatrix44 i_mat) throws NyARException
	{
		NyARGLUtil.toCameraViewRH(i_mat,1,this._temp);
		i_gl.glLoadMatrixd(this._temp,0);
		return;
	}
	
	/**
	 * projection行列をOpenGLの行列スタックへロードします。
	 */
	public void glLoadCameraFrustum(GL i_gl)
	{
		i_gl.glLoadMatrixd(this._gl_frustum_rh,0);
		return;
	}
	/**
	 * 現在のViewPortに、i_rtsourceの内容を描画します。
	 * @param i_gl
	 * OpenGLインスタンスを指定します。
	 * @param i_raster
	 * @throws NyARException
	 */
	public void glDrawRealitySource(GL i_gl,NyARRealitySource i_rtsource) throws NyARException
	{
		NyARGLDrawUtil.drawBackGround(i_gl,i_rtsource.refRgbSource(),1.0);
		return;
	}
}