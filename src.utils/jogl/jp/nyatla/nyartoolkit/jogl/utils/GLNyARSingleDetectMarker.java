/**
 * NyARSingleDetectMarkerにOpenGL向け関数を追加したもの
 * (c)2008 A虎＠nyatla.jp
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.jogl.utils;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.NyARParam;
import jp.nyatla.nyartoolkit.core.NyARTransMatResult;
import jp.nyatla.nyartoolkit.detector.*;

public class GLNyARSingleDetectMarker extends NyARSingleDetectMarker
{
    private NyARTransMatResult trans_mat_result=new NyARTransMatResult();
    private double view_scale_factor=0.025;//#define VIEW_SCALEFACTOR		0.025		// 1.0 ARToolKit unit becomes 0.025 of my OpenGL units.
    public GLNyARSingleDetectMarker(NyARParam i_param,NyARCode i_code,double i_marker_width) throws NyARException
    {
	super(i_param,i_code,i_marker_width);	
    }
    public void setScaleFactor(double i_new_value)
    {
	view_scale_factor=i_new_value;
    }
    //    public static void arglCameraViewRH(const double para[3][4], GLdouble m_modelview[16], const double scale)
    public double[] getCameraViewRH() throws NyARException
    {
	double[] result=new double[16];
	getCameraViewRH(result);
	return result;
    }
    /**
     * 
     * @param o_result
     * 結果値を格納する配列を指定してください。double[16]以上が必要です。
     * @throws NyARException
     */
    public void getCameraViewRH(double[] o_result) throws NyARException
    {
	//座標を計算
	this.getTransmationMatrix(this.trans_mat_result);
	//行列変換
	double[][] para=this.trans_mat_result.getArray();
	o_result[0 + 0*4] = para[0][0]; // R1C1
	o_result[0 + 1*4] = para[0][1]; // R1C2
	o_result[0 + 2*4] = para[0][2];
	o_result[0 + 3*4] = para[0][3];
	o_result[1 + 0*4] = -para[1][0]; // R2
	o_result[1 + 1*4] = -para[1][1];
	o_result[1 + 2*4] = -para[1][2];
	o_result[1 + 3*4] = -para[1][3];
	o_result[2 + 0*4] = -para[2][0]; // R3
	o_result[2 + 1*4] = -para[2][1];
	o_result[2 + 2*4] = -para[2][2];
	o_result[2 + 3*4] = -para[2][3];
	o_result[3 + 0*4] = 0.0;
	o_result[3 + 1*4] = 0.0;
	o_result[3 + 2*4] = 0.0;
	o_result[3 + 3*4] = 1.0;
	if (view_scale_factor != 0.0) {
	    o_result[12] *= view_scale_factor;
	    o_result[13] *= view_scale_factor;
	    o_result[14] *= view_scale_factor;
	}
	return;
    }
}
