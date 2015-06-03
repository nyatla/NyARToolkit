package jp.nyatla.nyartoolkit.dev.pro.core.transmat;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResultParam;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.pro.core.icp.NyARIcpPlane;
import jp.nyatla.nyartoolkit.pro.core.icp.NyARIcpPoint;
import jp.nyatla.nyartoolkit.pro.core.icp.NyARIcpPointRobust;
import jp.nyatla.nyartoolkit.pro.core.kpm.ann.NyARSurfAnnMatch;


public class NyARNftTransMatUtils
{
	private final static double AR2_DEFAULT_TRACKING_THRESH = 2.0;
	private NyARIcpPoint _icp;
	private NyARIcpPointRobust _icp_r;
	private NyARIcpPlane _icp_plane;
	
	private double _surface_threshold;	
	private NyARDoublePoint2d[] _kpm_pos2d_ref;
	private NyARDoublePoint3d[] _kpm_pos3d_ref;
	
	
	
	
	public NyARNftTransMatUtils(NyARParam i_ref_param,int i_max_kpm_pointset) throws NyARException
	{
		this._surface_threshold=AR2_DEFAULT_TRACKING_THRESH;
		this._icp = new NyARIcpPoint(i_ref_param);
		this._icp_r = new NyARIcpPointRobust(i_ref_param);
		this._last_inliner_probability=0;
		this._icp_plane=new NyARIcpPlane(i_ref_param);
		//KPM points
		this._kpm_pos2d_ref=new NyARDoublePoint2d[i_max_kpm_pointset];
		this._kpm_pos3d_ref=new NyARDoublePoint3d[i_max_kpm_pointset];
		

	}
	private double _last_inliner_probability;
	/**
	 * AR2Trackingの出力した�?�点セ�?トにつ�?て、変換行�?�を求めます�??
	 * @param initConv
	 * @param i_pos2d
	 * �?想座標点セ�?�?
	 * @param i_pos3d
	 * 姿勢�?報セ�?ト�?�i_pos2dに対応して�?る�?要があります�??
	 * @param i_num
	 * 点セ�?ト�?�個数
	 * @param conv
	 * 計算結果の出力行�??
	 * @param o_ret_param
	 * 返却値のパラメータ
	 * @return
	 * @throws NyARException 
	 */
	public boolean surfaceTrackingTransmat(NyARDoubleMatrix44 initConv, NyARDoublePoint2d[] i_pos2d, NyARDoublePoint3d[] i_pos3d, int i_num, NyARDoubleMatrix44 conv,NyARTransMatResultParam o_ret_param) throws NyARException
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
	/**
	 * KPMの出力�?�座標変換をする�?�ー
	 * @param i_pos2d
	 * @param i_pos3d
	 * @param i_num
	 * @param camPose
	 * @return
	 * @throws NyARException
	 */
	
	
	
	public boolean kpmTransmat(NyARSurfAnnMatch.ResultPtr match_items, NyARDoubleMatrix44 o_mat) throws NyARException
	{
		int l=match_items.getLength();
		if (l < 4) {
			return false;
		}
		NyARDoublePoint2d[] pos2d=this._kpm_pos2d_ref;
		NyARDoublePoint3d[] pos3d=this._kpm_pos3d_ref;
		
		NyARSurfAnnMatch.ResultItem[] items=match_items.getArray();
		for(int i=l-1;i>=0;i--){
			pos2d[i]=items[i].key;
			pos3d[i]=items[i].feature.coord3D;
		}
		this._icp.setInlierProbability(0);
		if (!this._icp_plane.icpGetInitXw2Xc_from_PlanarData(pos2d, pos3d, l, o_mat)) {
			return false;
		}
		if (!this._icp.icpPoint(pos2d,pos3d, l, o_mat, o_mat, null)) {
			return false;
		}
		return true;
	}	
}