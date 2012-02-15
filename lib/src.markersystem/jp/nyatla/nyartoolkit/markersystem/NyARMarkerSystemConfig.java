package jp.nyatla.nyartoolkit.markersystem;

import java.io.InputStream;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.analyzer.histogram.INyARHistogramAnalyzer_Threshold;
import jp.nyatla.nyartoolkit.core.analyzer.histogram.NyARHistogramAnalyzer_SlidePTile;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.transmat.INyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;

public class NyARMarkerSystemConfig implements INyARMarkerSystemConfig
{
	private NyARParam _param;
	public NyARMarkerSystemConfig(NyARParam i_param)
	{
		this._param=i_param;
	}
	public NyARMarkerSystemConfig(InputStream i_ar_param_stream,int i_width,int i_height) throws NyARException
	{
		this._param=new NyARParam();
		this._param.loadARParam(i_ar_param_stream);
		this._param.changeScreenSize(i_width,i_height);
	}
	/**
	 * コンストラクタです。カメラパラメータにサンプル値(../Data/camera_para.dat)をロードして、コンフィギュレーションを生成します。
	 * @param i_width
	 * @param i_height
	 * @throws NyARException
	 */
	public NyARMarkerSystemConfig(int i_width,int i_height) throws NyARException
	{
		this._param=new NyARParam();
		this._param.loadDefaultParameter();
		this._param.changeScreenSize(i_width,i_height);		
	}
	public INyARTransMat createTransmatAlgorism() throws NyARException
	{
		return new NyARTransMat(this._param);
	}
	public INyARHistogramAnalyzer_Threshold createAutoThresholdArgorism()
	{
		return new NyARHistogramAnalyzer_SlidePTile(15);
	}
	public NyARParam getNyARParam()
	{
		return 	this._param;
	}
}