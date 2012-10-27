package jp.nyatla.nyartoolkit.jogl.utils;


import javax.media.opengl.GL;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystem;

/**
 * このクラスは、{@link NyARGlmarkerSystem}クラスの出力する値を、OpenGL関数へショートカットする関数を定義します。
 * {@link MarkerSystem}からの通知を受け取ります。
 */
public class NyARGlMarkerSystemRender extends NyARGlRender
{
	private NyARMarkerSystem _ms;

	/**
	 * コンストラクタです。マーカシステムに対応したレンダラを構築します。
	 * @param i_ms
	 */
	public NyARGlMarkerSystemRender(NyARMarkerSystem i_ms)
	{
		super(i_ms);
		this._ms=i_ms;
	}
	/**
	 * OpenGLスタイルカメラパラメータのワーク変数
	 */
	private double[] _mv_mat=new double[16];
	
	/**
	 * i_glに、i_idで示されるマーカ平面の姿勢行列をセットします。
	 * @param i_gl
	 * @param i_id
	 * @throws NyARException 
	 */
	public void loadMarkerMatrix(GL i_gl,int i_id) throws NyARException
	{
		int old_mode=this.getGlMatrixMode(i_gl);
		if(old_mode!=GL.GL_MODELVIEW){
			i_gl.glMatrixMode(GL.GL_MODELVIEW);
			NyARGLUtil.toCameraViewRH(this._ms.getMarkerMatrix(i_id),1,this._mv_mat);			
			i_gl.glLoadMatrixd(this._mv_mat, 0);
			i_gl.glMatrixMode(old_mode);
		}else{
			NyARGLUtil.toCameraViewRH(this._ms.getMarkerMatrix(i_id),1,this._mv_mat);
			i_gl.glLoadMatrixd(this._mv_mat, 0);
		}
	}	
}