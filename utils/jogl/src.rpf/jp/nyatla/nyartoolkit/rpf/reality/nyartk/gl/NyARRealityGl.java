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
 * このクラスは、OpenGLに特化した{@link NyARReality}クラスです。
 * OpenGL形式の値をそのまま扱う関数を定義しています。
 */
public class NyARRealityGl extends NyARReality
{
	private double[] _gl_frustum_rh=new double[16];
	/**
	 * この関数は、 ARToolKitスタイルのModelView行列を、OpenGLスタイルのモデルビュー行列に変換します。
	 * @param i_ny_style_mat
	 * NyARToolkitスタイルの行列を指定します。
	 * @param o_gl_style_mat
	 * OpenGL形式の行列を受け取る配列を指定します。16要素である必要があります。
	 */
	public static void toGLViewMat(NyARDoubleMatrix44 i_ny_style_mat,double[] o_gl_style_mat)
	{
		NyARGLUtil.toCameraViewRH(i_ny_style_mat, 1, o_gl_style_mat);
	}
	/**
	 * コンストラクタです。
	 * カメラパラメータ、視錐体パラメータを元に、インスタンスを作成します。
	 * @param i_param
	 * カメラパラメータを指定します。
	 * @param i_near
	 * 視錐体のnear-pointをmm単位で指定します。
	 * 標準値は{@link #FRASTRAM_ARTK_NEAR}です。
	 * @param i_far
	 * 視錐体のfar-pointをmm単位で指定します。
	 * 標準値は{@link #FRASTRAM_ARTK_FAR}です。
	 * @param i_max_known_target
	 * KnownステータスのRTターゲットの最大数を指定します。
	 * @param i_max_unknown_target
	 * UnKnownステータスのRTターゲットの最大数を指定します。
	 */
	public NyARRealityGl(NyARParam i_param,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target) throws NyARException
	{
		super(i_param,i_near,i_far,i_max_known_target,i_max_unknown_target);
		//カメラパラメータを取得しておく。
		this._frustum.refMatrix().getValueT(this._gl_frustum_rh);
	}
	/**
	 * この関数は、透視投影行列と視錐体パラメータを元に、インスタンスを作成します。
	 * 初期状態のインスタンスを生成します。
	 * この関数は、樽型歪み矯正を行わないインスタンスを生成できます。
	 * @param i_prjmat
	 * ARToolKit形式の射影変換パラメータを指定します。
	 * @param i_screen_size
	 * スクリーン(入力画像)のサイズを指定します。
	 * @param i_near
	 * {@link NyARReality#NyARReality(NyARParam i_param,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target)}を参照
	 * @param i_far
	 * {@link NyARReality#NyARReality(NyARParam i_param,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target)}を参照
	 * @param i_max_known_target
	 * {@link NyARReality#NyARReality(NyARParam i_param,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target)}を参照
	 * @param i_max_unknown_target
	 * {@link NyARReality#NyARReality(NyARParam i_param,double i_near,double i_far,int i_max_known_target,int i_max_unknown_target)}を参照
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
	 * この関数は、NyARToolKitの姿勢変換行列をOpenGL行列スタックへロードします。
	 * @param i_gl
	 * OpenGLのインスタンス
	 * @param i_mat
	 * NyARToolkit形式の姿勢変換行列
	 * @throws NyARException
	 */
	public void glLoadModelViewMatrix(GL i_gl,NyARDoubleMatrix44 i_mat) throws NyARException
	{
		NyARGLUtil.toCameraViewRH(i_mat,1,this._temp);
		i_gl.glLoadMatrixd(this._temp,0);
		return;
	}
	
	/**
	 * この関数は、NyARToolKitの射影変換行列をOpenGL行列スタックへロードします。
	 * @param i_gl
	 * OpenGLのインスタンス
	 */
	public void glLoadCameraFrustum(GL i_gl)
	{
		i_gl.glLoadMatrixd(this._gl_frustum_rh,0);
		return;
	}
	/**
	 * この関数は、現在のViewPortに、i_rtsourceの元画像の内容を描画します。
	 * 背景画像の描画に使います。
	 * @param i_gl
	 * OpenGLインスタンスを指定します。
	 * @param i_rtsource
	 * 描画するRealitySource
	 * @throws NyARException
	 */
	public void glDrawRealitySource(GL i_gl,NyARRealitySource i_rtsource) throws NyARException
	{
		NyARGLDrawUtil.drawBackGround(i_gl,i_rtsource.refRgbSource(),1.0);
		return;
	}
}