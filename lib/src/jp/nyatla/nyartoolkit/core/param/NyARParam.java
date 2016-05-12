/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.param;

import java.io.*;

import jp.nyatla.nyartoolkit.core.NyARMethodDeplecatedException;
import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.param.distfactor.INyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.param.distfactor.NyARCameraDistortionFactorLT;
import jp.nyatla.nyartoolkit.core.param.distfactor.NyARCameraDistortionFactorMap;
import jp.nyatla.nyartoolkit.core.param.distfactor.NyARCameraDistortionFactorV2;
import jp.nyatla.nyartoolkit.core.param.distfactor.NyARCameraDistortionFactorV4;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.j2se.BinaryReader;

/**
 * このクラスは、NyARToolkitの環境パラメータを格納します。
 * 環境パラメータは、ARToolKitのパラメータと同一です。
 * パラメータの要素には、以下のものがあります。
 * <ul>
 * <li>樽型歪みパラメータ - 入力画像の樽型歪みパラメータです。
 * <li>スクリーンサイズ - 入力画像の解像度です。
 * <li>透視変換パラメータ - 4x4行列です。
 * </ul>
 */
public class NyARParam
{
	final public static int DISTFACTOR_RAW=0;
	final public static int DISTFACTOR_LT_ARTK2=1;
	final public static int DISTFACTOR_LT_ARTK5=2;
	/** スクリーンサイズです。*/
	final protected NyARIntSize _screen_size;
	final private INyARCameraDistortionFactor _dist;
	final private NyARPerspectiveProjectionMatrix _projection_matrix;

	/**
	 * タイプに応じた歪み矯正クラスを生成して返します。
	 * @param i_screen_size
	 * @param i_base_factor
	 * @param i_type
	 * @return
	 */
	private static INyARCameraDistortionFactor makeDistFactor(NyARIntSize i_screen_size,INyARCameraDistortionFactor i_base_factor,int i_type)
	{
		switch(i_type){
		case DISTFACTOR_RAW:
			return i_base_factor;
		case DISTFACTOR_LT_ARTK2:
			return new NyARCameraDistortionFactorMap(i_screen_size.w,i_screen_size.h,i_base_factor);
		case DISTFACTOR_LT_ARTK5:
			return new NyARCameraDistortionFactorLT(i_screen_size.w,i_screen_size.h,NyARCameraDistortionFactorLT.AR_PARAM_LT_DEFAULT_OFFSET,i_base_factor);
		default:
			throw new NyARRuntimeException();
		}
	}
	/**
	 * テストパラメータを格納したインスタンスを生成します。
	 * テストパラメータは、ARToolKit2形式のcamera_para.datです。
	 * @param i_screen_width
	 * 画面サイズの幅です。
	 * @param i_screen_height
	 * 画面サイズの高さです。
	 * @param i_dist_map_type
	 * 歪み矯正マップの構築方法を指定します。
	 * @return
	 */
	public static NyARParam loadDefaultParams(int i_screen_width,int i_screen_height,int i_dist_map_type)
	{
		ParamLoader pm=new ParamLoader(i_screen_width,i_screen_height);
		return new NyARParam(
			pm.size,pm.pmat,
			makeDistFactor(pm.size,pm.dist_factor,i_dist_map_type));
	}
	public static NyARParam loadDefaultParams(int i_screen_width,int i_screen_height)
	{
		return loadDefaultParams(i_screen_width,i_screen_height,DISTFACTOR_LT_ARTK5);
	}
	/**
	 * i_streamからARToolkitのカメラパラメータを読み出して、格納したインスタンスを生成します。
	 * @param i_stream
	 * @param i_screen_width
	 * 画面サイズの幅です。
	 * @param i_screen_height
	 * 画面サイズの高さです。
	 * @param i_dist_map_type
	 * 歪み矯正マップの構築方法を指定します。
	 * @return
	 * @throws NyARRuntimeException
	 */
	public static NyARParam loadFromARParamFile(InputStream i_stream,int i_screen_width,int i_screen_height,int i_dist_map_type)
	{
		ParamLoader pm=new ParamLoader(i_stream,i_screen_width,i_screen_height);
		return new NyARParam(
			pm.size,pm.pmat,
			makeDistFactor(pm.size,pm.dist_factor,i_dist_map_type));
	}
	public static NyARParam loadFromARParamFile(InputStream i_stream,int i_screen_width,int i_screen_height)
	{
		return loadFromARParamFile(i_stream,i_screen_width,i_screen_height,DISTFACTOR_LT_ARTK5);
	}
	/**
	 * intrinsic matrixとdistortion coeffsパラメータでインスタンスを初期化します。
	 * @param i_size
	 * カメラパラメータのサイズ値
	 * @param i_intrinsic_matrix
	 * 3x3 matrix
	 * このパラメータは、OpenCVのcvCalibrateCamera2関数が出力するintrinsic_matrixの値と合致します。
	 * @param i_distortion_coeffs
	 * 4x1 matrix
	 * このパラメータは、OpenCVのcvCalibrateCamera2関数が出力するdistortion_coeffsの値と合致します。
	 * @param i_screen_width
	 * 画面サイズの幅です。
	 * @param i_screen_height
	 * 画面サイズの高さです。
	 * @param i_dist_map_type
	 * 歪み矯正マップの構築方法を指定します。
	 */		
	public static NyARParam loadFromCvCalibrateCamera2Result(int i_camera_width,int i_camera_height,double[] i_intrinsic_matrix,double[] i_distortion_coeffs,int i_screen_width,int i_screen_height,int i_dist_map_type)
	{
		ParamLoader pm=new ParamLoader(i_camera_width,i_camera_height,i_intrinsic_matrix,i_distortion_coeffs,i_screen_width,i_screen_height);
		return new NyARParam(
			pm.size,pm.pmat,
			makeDistFactor(pm.size,pm.dist_factor,i_dist_map_type));
	}
	public static NyARParam loadFromCvCalibrateCamera2Result(int i_camera_width,int i_camera_height,double[] i_intrinsic_matrix,double[] i_distortion_coeffs,int i_screen_width,int i_screen_height)
	{
		return loadFromCvCalibrateCamera2Result(i_camera_width,i_camera_height,i_intrinsic_matrix,i_distortion_coeffs,i_screen_width,i_screen_height,DISTFACTOR_LT_ARTK5);
	}

	
	
	
	
	/**
	 * 引数のオブジェクトでインスタンスを初期化します。
	 * @param i_screen_size
	 * パラメータを生成したスクリーンサイズ
	 * @param i_projection_mat
	 * 射影変換に使うオブジェクト
	 * @param i_dist_factor
	 * 歪み矯正オブジェクト
	 */
	public NyARParam(NyARIntSize i_screen_size,NyARPerspectiveProjectionMatrix i_projection_mat,INyARCameraDistortionFactor i_dist_factor)
	{
		this._screen_size=i_screen_size;
		this._dist=i_dist_factor;
		this._projection_matrix=i_projection_mat;
	}
	
	
	public NyARIntSize getScreenSize()
	{
		return this._screen_size;
	}
	/**
	 * この関数は、ARToolKit形式の透視変換行列を返します。
	 * @return
	 * [read only]透視変換行列を返します。
	 */
	public NyARPerspectiveProjectionMatrix getPerspectiveProjectionMatrix()
	{
		return this._projection_matrix;
	}
	/**
	 * この関数は、ARToolKit形式の歪み補正パラメータを返します。
	 * @return
	 * [read only]歪み補正パラメータオブジェクト
	 */
	public INyARCameraDistortionFactor getDistortionFactor()
	{
		return this._dist;
	}


	/**
	 * この関数は、カメラパラメータから右手系の視錐台を作ります。
	 * <p>注意 -
	 * この処理は低速です。繰り返しの使用はできるだけ避けてください。
	 * </p>
	 * @param i_dist_min
	 * 視錐台のnear point(mm指定)
	 * @param i_dist_max
	 * 視錐台のfar point(mm指定)
	 * @param o_frustum
	 * 視錐台を受け取る配列。
	 * @see NyARPerspectiveProjectionMatrix#makeCameraFrustumRH
	 */
	public void makeCameraFrustumRH(double i_dist_min,double i_dist_max,NyARDoubleMatrix44 o_frustum)
	{
		this._projection_matrix.makeCameraFrustumRH(this._screen_size.w, this._screen_size.h, i_dist_min, i_dist_max, o_frustum);
		return;
	}
	/**
	 * この関数は機能しません。
	 * @param i_stream
	 * 未定義
	 * @throws Exception
	 */
	public void saveARParam(OutputStream i_stream)
	{
		NyARRuntimeException.trap("未チェックの関数");
	}
//
//	Deprecated method
//
	
	/**
	 * @see #loadDefaultParams
	 * @deprecated 
	 */
	public static NyARParam createDefaultParameter()
	{
		throw new NyARMethodDeplecatedException("#loadDefaultParams");
	}
	/**
	 * @see #loadFromARParamFile
	 * @deprecated 
	 */
	public static NyARParam createFromARParamFile(InputStream i_stream)
	{
		throw new NyARMethodDeplecatedException("#loadFromARParamFile");
	}
	/**
	 * @see loadFromCvCalibrateCamera2Result
	 * @deprecated 
	 */
	public static NyARParam createFromCvCalibrateCamera2Result(int i_w,int i_h,double[] i_intrinsic_matrix,double[] i_distortion_coeffs)
	{
		throw new NyARMethodDeplecatedException("#loadFromCvCalibrateCamera2Result");
	}

	
	
	public static void main(String[] args)
	{
		try {
			String cparam=	"../Data/testcase/camera_para5.dat";
			NyARParam param=NyARParam.loadFromARParamFile(new FileInputStream(new File(cparam)),640,480);
			//DistfactorV4のテスト。dfとdf2の_sパラメータの値が同じか確認する。
			NyARCameraDistortionFactorV4 df=(NyARCameraDistortionFactorV4)param.getDistortionFactor();
			double[] t=new double[16];
			df.getValue(t);
//			NyARCameraDistortionFactorV4 df2=new NyARCameraDistortionFactorV4(param._screen_size.w,param._screen_size.h,new double[]{t[4],0,t[6], 0,t[5],t[7]},new double[]{t[0],t[1],t[2],t[3]},1,1);
			return;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}	
	
}








/**
 * パラメータローダーです。
 */
class ParamLoader
{
	public NyARIntSize size;
	public NyARPerspectiveProjectionMatrix pmat;
	public INyARCameraDistortionFactor dist_factor;
	/**
	 * intrinsic_matrixとdistortion_coeffsからインスタンスを初期化する。
	 * @param i_camera_width
	 * カメラパラメータ生成時の画面サイズ
	 * @param i_camera_height
	 * カメラパラメータ生成時の画面サイズ
	 * @param i_intrinsic_matrix 3x3 matrix このパラメータは、OpenCVのcvCalibrateCamera2関数が出力するintrinsic_matrixの値と合致します。
	 * @param i_distortion_coeffs 4x1 vector このパラメータは、OpenCVのcvCalibrateCamera2関数が出力するdistortion_coeffsの値と合致します。
	 */
	public ParamLoader(int i_camera_width,int i_camera_height,double[] i_intrinsic_matrix,double[] i_distortion_coeffs,int i_screen_width,int i_screen_height)
	{
		final double x_scale = (double) i_screen_width / (double) (i_camera_width);// scale = (double)xsize / (double)(source->xsize);
		final double y_scale = (double) i_screen_height / (double) (i_camera_height);// scale = (double)ysize / (double)(source->ysize);
		
		this.size=new NyARIntSize(i_camera_width,i_camera_height);
		//dist factor(倍率1倍の基準点)
		NyARCameraDistortionFactorV4 v4dist=new NyARCameraDistortionFactorV4(i_camera_width,i_camera_height,i_intrinsic_matrix,i_distortion_coeffs,x_scale,y_scale);
		double s=v4dist.getS();
		//projection matrix
		NyARDoubleMatrix33 r=new NyARDoubleMatrix33();
		r.setValue(i_intrinsic_matrix);
	    r.m00 /= s;
	    r.m01 /= s;
	    r.m10 /= s;
	    r.m11 /= s;
	    NyARPerspectiveProjectionMatrix pm=new NyARPerspectiveProjectionMatrix();
	    pm.setValue(r, new NyARDoublePoint3d());
	    pm.changeScale(x_scale, y_scale);
		this.dist_factor=v4dist;
		this.pmat=pm;
	}
	/**
	 * 標準パラメータでインスタンスを初期化します。
	 * @throws NyARRuntimeException
	 */
	public ParamLoader(int i_screen_width,int i_screen_height)
	{
		double[] df={318.5,263.5,26.2,1.0127565206658486};
		double[] pj={	700.9514702992245,0,316.5,0,
						0,726.0941816535367,241.5,0.0,
						0.0,0.0,1.0,0.0,
						0.0,0.0,0.0,1.0};
		final double x_scale = (double) i_screen_width / (double) (640);// scale = (double)xsize / (double)(source->xsize);
		final double y_scale = (double) i_screen_height / (double) (480);// scale = (double)ysize / (double)(source->ysize);
		
		this.size=new NyARIntSize(i_screen_width,i_screen_height);
		this.pmat=new NyARPerspectiveProjectionMatrix();
		this.pmat.setValue(pj);
		this.pmat.changeScale(x_scale, y_scale);
		this.dist_factor=new NyARCameraDistortionFactorV2(df,x_scale,y_scale);
	}
	/**
	 * ストリームから読み出したデータでインスタンスを初期化します。
	 * @param i_stream
	 * @throws NyARRuntimeException
	 */
	public ParamLoader(InputStream i_stream,int i_screen_width,int i_screen_height)throws NyARRuntimeException
	{
		//読み出し
		byte[] data=BinaryReader.toArray(i_stream);
		BinaryReader bis=new BinaryReader(data,BinaryReader.ENDIAN_BIG);
		//読み出したサイズでバージョンを決定
		int[] version_table={136,144,152,176};
		int version=-1;
		for(int i=0;i<version_table.length;i++){
			if(data.length%version_table[i]==0){
				version=i+1;
				break;
			}
		}
		//一致しなければ無し
		if(version==-1){
			throw new NyARRuntimeException();
		}
		int camera_width=bis.getInt();
		int camera_height=bis.getInt();
		
		//size
		this.size=new NyARIntSize(i_screen_width,i_screen_height);
		final double x_scale = (double) i_screen_width / (double) (camera_width);// scale = (double)xsize / (double)(source->xsize);
		final double y_scale = (double) i_screen_height / (double) (camera_height);// scale = (double)ysize / (double)(source->ysize);

		//projection matrix
		this.pmat=new NyARPerspectiveProjectionMatrix();
		double[] pjv=bis.getDoubleArray(new double[16],12);
		pjv[12]=pjv[13]=pjv[14]=0;pjv[15]=1;
		this.pmat.setValue(pjv);
		this.pmat.changeScale(x_scale, y_scale);
		
		//dist factor
		switch(version)
		{
		case 1://Version1
			this.dist_factor=new NyARCameraDistortionFactorV2(bis.getDoubleArray(new double[NyARCameraDistortionFactorV2.NUM_OF_FACTOR]),x_scale,y_scale);
			break;
		case 4://Version4
			this.dist_factor=new NyARCameraDistortionFactorV4(bis.getDoubleArray(new double[NyARCameraDistortionFactorV4.NUM_OF_FACTOR]),x_scale,y_scale);
			break;
		default:
			throw new NyARRuntimeException();
		}
	}


}