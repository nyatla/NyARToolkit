package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

/**
 * 視錐台とこれを使った演算関数を定義します。
 * @author nyatla
 *
 */
public class NyARFrustum
{
	/** frastum行列*/
	protected NyARDoubleMatrix44 _frustum_rh=new NyARDoubleMatrix44();
	/** frastum逆行列*/
	protected NyARDoubleMatrix44 _inv_frustum_rh=new NyARDoubleMatrix44();	
	protected NyARIntSize _screen_size=new NyARIntSize();
	/**
	 * コンストラクタです。ARToolkitの射影変換行列から、インスタンスを作ります。
	 * @param i_projection
	 * @param i_width
	 * スクリーンサイズです。
	 * @param i_height
	 * スクリーンサイズです。
	 * @param i_near
	 * 近平面までの距離です。単位はmm
	 * @param i_far
	 * 遠平面までの距離です。単位はmm
	 */
	public NyARFrustum(NyARPerspectiveProjectionMatrix i_projection,int i_width,int i_height,double i_near,double i_far)
	{
		this.setValue(i_projection, i_width, i_height, i_near, i_far);
	}
	
	public void setValue(NyARPerspectiveProjectionMatrix i_projection,int i_width,int i_height,double i_near,double i_far)
	{
		i_projection.makeCameraFrustumRH(i_width, i_height, i_near, i_far,this._frustum_rh);
		this._frustum_rh.inverse(this._inv_frustum_rh);
		this._screen_size.setValue(i_width,i_height);
	}
	/**
	 * 画像上の座標を、撮像点座標に変換します。
	 * この座標は、カメラ座標系です。
	 * @param ix
	 * 画像上の座標
	 * @param iy
	 * 画像上の座標
	 * @param o_point_on_screen
	 * 撮像点座標
	 * <p>
	 * この関数は、gluUnprojectのビューポートとモデルビュー行列を固定したものです。
	 * 公式は、以下の物使用。
	 * http://www.opengl.org/sdk/docs/man/xhtml/gluUnProject.xml
	 * ARToolKitの座標系に合せて計算するため、OpenGLのunProjectとはix,iyの与え方が違います。画面上の座標をそのまま与えてください。
	 * </p>
	 */
	public void unProject(double ix,double iy,NyARDoublePoint3d o_point_on_screen)
	{
		double n=(this._frustum_rh.m23/(this._frustum_rh.m22-1));
		NyARDoubleMatrix44 m44=this._inv_frustum_rh;
		double v1=(this._screen_size.w-ix-1)*2/this._screen_size.w-1.0;//ARToolKitのFrustramに合せてる。
		double v2=(this._screen_size.h-iy-1)*2/this._screen_size.h-1.0;
		double v3=2*n-1.0;
		double b=1/(o_point_on_screen.z=m44.m30*v1+m44.m31*v2+m44.m32*v3+m44.m33);
		o_point_on_screen.x=(m44.m00*v1+m44.m01*v2+m44.m02*v3+m44.m03)*b;
		o_point_on_screen.y=(m44.m10*v1+m44.m11*v2+m44.m12*v3+m44.m13)*b;
		o_point_on_screen.z=(m44.m20*v1+m44.m21*v2+m44.m22*v3+m44.m23)*b;
		return;
	}
	/**
	 * 画面上の点と原点を結ぶ直線と任意姿勢の平面の交差点を、カメラの座標系で取得します。
	 * この座標は、カメラ座標系です。
	 * @param ix
	 * @param iy
	 * @param i_mat
	 * 平面の姿勢行列です。
	 * @param o_pos
	 */
	public void unProjectOnCamera(int ix,int iy,NyARDoubleMatrix44 i_mat,NyARDoublePoint3d o_pos)
	{
		//画面→撮像点
		this.unProject(ix,iy,o_pos);
		//撮像点→カメラ座標系
		double nx=i_mat.m02;
		double ny=i_mat.m12;
		double nz=i_mat.m22;
		double mx=i_mat.m03;
		double my=i_mat.m13;
		double mz=i_mat.m23;
		double t=(nx*mx+ny*my+nz*mz)/(nx*o_pos.x+ny*o_pos.y+nz*o_pos.z);
		o_pos.x=t*o_pos.x;
		o_pos.y=t*o_pos.y;
		o_pos.z=t*o_pos.z;
	}	
	/**
	 * 画面上の点と原点を結ぶ直線と任意姿勢の平面の交差点を、平面の座標系で取得します。
	 * ARToolKitの本P175周辺の実装と同じです。
	 * @param ix
	 * @param iy
	 * @param i_mat
	 * 平面の姿勢行列です。
	 * @param o_pos
	 * @return
	 * <p>
	 * このAPIは繰り返し使用には最適化されていません。同一なi_matに繰り返しアクセスするときは、展開してください。
	 * </p>
	 */
	public boolean unProjectOnMatrix(int ix,int iy,NyARDoubleMatrix44 i_mat,NyARDoublePoint3d o_pos)
	{
		//交点をカメラ座標系で計算
		unProjectOnCamera(ix,iy,i_mat,o_pos);
		//座標系の変換
		NyARDoubleMatrix44 m=new NyARDoubleMatrix44();
		if(!m.inverse(i_mat)){
			return false;
		}
		m.transform3d(o_pos, o_pos);
		return true;
	}
	/**
	 * 透視変換行列を返します。
	 * @return
	 */
	public final NyARDoubleMatrix44 refMatrix()
	{
		return this._frustum_rh;
	}
	/**
	 * 透視変換行列の逆行列を返します。
	 * @return
	 */
	public final NyARDoubleMatrix44 refInvMatrix()
	{
		return this._inv_frustum_rh;
	}
	
}