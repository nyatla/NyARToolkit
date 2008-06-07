/**
 * NyARSingleDetectMarkerにOpenGL向け関数を追加したもの
 * (c)2008 A虎＠nyatla.jp
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.jogl.utils;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.NyARMat;
import jp.nyatla.nyartoolkit.core.NyARParam;
import jp.nyatla.nyartoolkit.detector.*;

public class GLNyARSingleDetectMarker extends NyARSingleDetectMarker
{
    private double view_scale_factor=0.025;//#define VIEW_SCALEFACTOR		0.025		// 1.0 ARToolKit unit becomes 0.025 of my OpenGL units.
    public GLNyARSingleDetectMarker(NyARParam i_param,NyARCode i_code,double i_marker_width)
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
	//座標を計算
	NyARMat mat=getTransmationMatrix();
	//行列変換
	double[][] para=mat.getArray();
	double[] result=new double[16];
	result[0 + 0*4] = para[0][0]; // R1C1
	result[0 + 1*4] = para[0][1]; // R1C2
	result[0 + 2*4] = para[0][2];
	result[0 + 3*4] = para[0][3];
	result[1 + 0*4] = -para[1][0]; // R2
	result[1 + 1*4] = -para[1][1];
    	result[1 + 2*4] = -para[1][2];
    	result[1 + 3*4] = -para[1][3];
    	result[2 + 0*4] = -para[2][0]; // R3
    	result[2 + 1*4] = -para[2][1];
    	result[2 + 2*4] = -para[2][2];
    	result[2 + 3*4] = -para[2][3];
    	result[3 + 0*4] = 0.0;
    	result[3 + 1*4] = 0.0;
    	result[3 + 2*4] = 0.0;
    	result[3 + 3*4] = 1.0;
    	if (view_scale_factor != 0.0) {
    	    result[12] *= view_scale_factor;
    	    result[13] *= view_scale_factor;
    	    result[14] *= view_scale_factor;
    	}
    	return result;
    }
}
