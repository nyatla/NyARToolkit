package jp.nyatla.nyartoolkit.core.surfacetracking.transmat;

import jp.nyatla.nyartoolkit.core.icp.NyARIcpPlane;
import jp.nyatla.nyartoolkit.core.icp.NyARIcpPoint;
import jp.nyatla.nyartoolkit.core.icp.NyARIcpPointRobust;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResultParam;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;


public class NyARNftTransMatUtils
{
	private final static double AR2_DEFAULT_TRACKING_THRESH = 2.0;
	private NyARIcpPoint _icp;
	private NyARIcpPointRobust _icp_r;
	private NyARIcpPlane _icp_plane;
	
	private double _surface_threshold;	
	private NyARDoublePoint2d[] _kpm_pos2d_ref;
	private NyARDoublePoint3d[] _kpm_pos3d_ref;
	
	
	

	public NyARNftTransMatUtils(NyARParam i_ref_param,int i_max_kpm_pointset,double i_tracking_threshold)
	{
		this._surface_threshold=i_tracking_threshold;
		this._icp = new NyARIcpPoint(i_ref_param.getPerspectiveProjectionMatrix());
		this._icp_r = new NyARIcpPointRobust(i_ref_param.getPerspectiveProjectionMatrix());
		this._last_inliner_probability=0;
		this._icp_plane=new NyARIcpPlane(i_ref_param.getPerspectiveProjectionMatrix());
		//KPM points
		this._kpm_pos2d_ref=new NyARDoublePoint2d[i_max_kpm_pointset];
		this._kpm_pos3d_ref=new NyARDoublePoint3d[i_max_kpm_pointset];
		

	}
	private double _last_inliner_probability;
	public static NyARDoublePoint3d centerOffset(NyARDoublePoint3d[] i_pos3d,int i_num,NyARDoublePoint3d i_result)
	{
		double dx,dy,dz;
	    dx = dy = dz = 0.0f;
	    for(int i = 0; i < i_num; i++ ) {
	        dx += i_pos3d[i].x;
	        dy += i_pos3d[i].y;
	        dz += i_pos3d[i].z;
	    }
	    i_result.x=dx/i_num;
	    i_result.y=dy/i_num;
	    i_result.z=dz/i_num;
		return i_result;
	}
	
	/**
	 * ARToolKitV5で追加されていた補正
	 * @param initConv
	 * @param i_pos3d
	 * @param i_offset
	 * @param i_num
	 */
	public static void modifyInputOffset(NyARDoubleMatrix44 initConv,NyARDoublePoint3d[] i_pos3d,int i_num,NyARDoublePoint3d i_offset)
	{
	    double dx=i_offset.x;
	    double dy=i_offset.y;
	    double dz=i_offset.z;
	    for(int i = 0; i < i_num; i++ ) {
	    	i_pos3d[i].x  = i_pos3d[i].x - dx;
	    	i_pos3d[i].y  = i_pos3d[i].y - dy;
	    	i_pos3d[i].z  = i_pos3d[i].z - dz;
	    }
	    initConv.m03=(initConv.m00 * dx + initConv.m01 * dy + initConv.m02 * dz + initConv.m03);
	    initConv.m13=(initConv.m10 * dx + initConv.m11 * dy + initConv.m12 * dz + initConv.m13);
	    initConv.m23=(initConv.m20 * dx + initConv.m21 * dy + initConv.m22 * dz + initConv.m23);
	    return;
	}
	public static void restoreOutputOffset(NyARDoubleMatrix44 conv,NyARDoublePoint3d i_offset)
	{
	    double dx=i_offset.x;
	    double dy=i_offset.y;
	    double dz=i_offset.z;
	    conv.m03 = (conv.m03 - conv.m00 * dx - conv.m01 * dy - conv.m02 * dz);
	    conv.m13 = (conv.m13 - conv.m10 * dx - conv.m11 * dy - conv.m12 * dz);
	    conv.m23 = (conv.m23 - conv.m20 * dx - conv.m21 * dy - conv.m22 * dz);
	    return;
	}
	/**
	 * AR2Trackingの出力した頂点セットについて、変換行列を求めます。
	 * @param initConv
	 * @param i_pos2d
	 * 理想座標点セット
	 * @param i_pos3d
	 * 姿勢情報セット。i_pos2dに対応している必要があります。
	 * @param i_num
	 * 点セットの個数
	 * @param conv
	 * 計算結果の出力行列
	 * @param o_ret_param
	 * 返却値のパラメータ
	 * @return
	 * @throws NyARException 
	 */
	public boolean surfaceTrackingTransmat(NyARDoubleMatrix44 initConv, NyARDoublePoint2d[] i_pos2d, NyARDoublePoint3d[] i_pos3d, int i_num, NyARDoubleMatrix44 conv,NyARTransMatResultParam o_ret_param)
	{
		
		this._icp.setInlierProbability(this._last_inliner_probability);
		this._icp.icpPoint(i_pos2d,i_pos3d, i_num,initConv, conv, o_ret_param);
		if (o_ret_param.last_error > this._surface_threshold) {
			this._icp_r.setInlierProbability(0.8);
			this._icp_r.icpPoint(i_pos2d,i_pos3d, i_num, conv, conv, o_ret_param);
			if (o_ret_param.last_error > this._surface_threshold) {
				this._icp_r.setInlierProbability(0.6);
				this._icp_r.icpPoint(i_pos2d,i_pos3d, i_num, conv, conv, o_ret_param);
				if (o_ret_param.last_error> this._surface_threshold) {
					this._icp_r.setInlierProbability(0.4);
					this._icp_r.icpPoint(i_pos2d,i_pos3d, i_num, conv, conv, o_ret_param);
					if (o_ret_param.last_error > this._surface_threshold) {
						this._icp_r.setInlierProbability(0.0);
						this._icp_r.icpPoint(i_pos2d,i_pos3d, i_num, conv, conv, o_ret_param);
						if (o_ret_param.last_error > this._surface_threshold)
						{
							this._last_inliner_probability=0;
							return false;
						}
						this._last_inliner_probability=0;
					}
					this._last_inliner_probability=0.4;
				}
				this._last_inliner_probability=0.6;
			}
			this._last_inliner_probability=0.8;
		}
		return true;
	}


}