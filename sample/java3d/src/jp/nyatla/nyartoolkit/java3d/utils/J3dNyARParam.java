/**
 * NyARParamにOpenGL向け関数を追加したもの
 * (c)2008 A虎＠nyatla.jp
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.java3d.utils;

import jp.nyatla.nyartoolkit.core.*;
import javax.media.j3d.Transform3D;

public class J3dNyARParam extends NyARParam
{
    private double view_distance_min=0.01;//1cm～10.0m
    private double view_distance_max=10.0;
    private Transform3D m_projection=null;
    /**
     * 視体積の近い方をメートルで指定
     * @param i_new_value
     */
    public void setViewDistanceMin(double i_new_value)
    {
	m_projection=null;//キャッシュ済変数初期化
	view_distance_min=i_new_value;
    }
    /**
     * 視体積の遠い方をメートルで指定
     * @param i_new_value
     */
    public void setViewDistanceMax(double i_new_value)
    {
	m_projection=null;//キャッシュ済変数初期化
	view_distance_max=i_new_value;
    }
    /**
     * void arglCameraFrustumRH(const ARParam *cparam, const double focalmin, const double focalmax, GLdouble m_projection[16])
     * 関数の置き換え
     * @param focalmin
     * @param focalmax
     * @return
     */
    public Transform3D getCameraTransform()
    {
	//既に値がキャッシュされていたらそれを使う
	if(m_projection!=null){
	    return m_projection;
	}
	//無ければ計算

	NyARMat trans_mat=new NyARMat(3,4);
	NyARMat icpara_mat=new NyARMat(3,4);
        double[][]  p=new double[3][3], q=new double[4][4];
    	double      width, height;
        int      i, j;
    	
        width  = xsize;
        height = ysize;
    	
        decompMat(icpara_mat,trans_mat);

        double[][] icpara=icpara_mat.getArray();
        double[][] trans=trans_mat.getArray();
    	for (i = 0; i < 4; i++) {
            icpara[1][i] = (height - 1)*(icpara[2][i]) - icpara[1][i];
        }
    	
        for(i = 0; i < 3; i++) {
            for(j = 0; j < 3; j++) {
                p[i][j] = icpara[i][j] / icpara[2][2];
            }
        }
        //p[0][0],p[1][1]=n
        //p[0][2],p[1][2]=t+b

        //Projectionの計算
        q[0][0] = (2.0 * p[0][0] / (width - 1));
        q[0][1] = (2.0 * p[0][1] / (width - 1));
        q[0][2] = -((2.0 * p[0][2] / (width - 1))  - 1.0);
        q[0][3] = 0.0;
    	
        q[1][0] = 0.0;
        q[1][1] = -(2.0 * p[1][1] / (height - 1));
        q[1][2] = -((2.0 * p[1][2] / (height - 1)) - 1.0);
        q[1][3] = 0.0;
    	
        q[2][0] = 0.0;
        q[2][1] = 0.0;
        q[2][2] = (view_distance_max + view_distance_min)/(view_distance_min - view_distance_max);
        q[2][3] = 2.0 * view_distance_max * view_distance_min / (view_distance_min - view_distance_max);
    	
        q[3][0] = 0.0;
        q[3][1] = 0.0;
        q[3][2] = -1.0;
        q[3][3] = 0.0;
        
        q[2][2]=q[2][2]*-1;
        q[2][3]=q[2][3]*-1;
        
        double[] tmp_projection=new double[16];
        for (i = 0; i < 4; i++) { // Row.
    		// First 3 columns of the current row.
            for (j = 0; j < 3; j++) { // Column.
        	tmp_projection[i + j*4] =(
                    q[i][0] * trans[0][j] +
                    q[i][1] * trans[1][j] +
                    q[i][2] * trans[2][j]);
            }
    		// Fourth column of the current row.
            tmp_projection[i + 3*4]=
        	q[i][0] * trans[0][3] +
        	q[i][1] * trans[1][3] +
                q[i][2] * trans[2][3] +
                q[i][3];
        }
        m_projection=new Transform3D(tmp_projection);
        m_projection.transpose();
        return 	m_projection;
    }
}
