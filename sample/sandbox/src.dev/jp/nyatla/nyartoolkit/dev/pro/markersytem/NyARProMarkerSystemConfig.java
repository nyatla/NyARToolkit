/* 
 * PROJECT: NyARToolkit Professional Addon
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2012 Ryo Iizuka
 * wm@nyatla.jp
 * http://nyatla.jp
 * 
 */
package jp.nyatla.nyartoolkit.dev.pro.markersytem;

import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.transmat.*;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystemConfig;
import jp.nyatla.nyartoolkit.pro.core.icp.*;

public class NyARProMarkerSystemConfig extends NyARMarkerSystemConfig
{
	private int _icp_mode;
	public NyARProMarkerSystemConfig(NyARParam i_param,int i_mode)
	{
		super(i_param);
		this._icp_mode=i_mode;
	}
	public NyARProMarkerSystemConfig(InputStream i_ar_param_stream,int i_width,int i_height,int i_mode) throws NyARRuntimeException
	{
		super(i_ar_param_stream,i_width,i_height);
		this._icp_mode=i_mode;
	}
	/**
	 * コンストラクタです�?�カメラパラメータにサンプル値(../Data/camera_para.dat)をロードして、コンフィギュレーションを生成します�??
	 * @param i_width
	 * @param i_height
	 * @throws NyARRuntimeException
	 */
	public NyARProMarkerSystemConfig(int i_width,int i_height,int i_mode) throws NyARRuntimeException
	{
		super(i_width,i_height);
		this._icp_mode=i_mode;
	}
	public INyARTransMat createTransmatAlgorism() throws NyARRuntimeException
	{
		return new NyARIcpTransMat(this._param,this._icp_mode);
	}
}