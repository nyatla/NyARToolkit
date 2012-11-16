/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.param;

import java.io.*;
import java.nio.*;
import jp.nyatla.nyartoolkit.core.utils.*;
import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;

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
	/** スクリーンサイズです。*/
	protected NyARIntSize _screen_size=new NyARIntSize();
	private INyARCameraDistortionFactor _dist;
	private NyARPerspectiveProjectionMatrix _projection_matrix=new NyARPerspectiveProjectionMatrix();
	/**
	 * テストパラメータを格納したインスタンスを生成します。
	 * テストパラメータは、ARToolKit2形式のcamera_para.datです。
	 * @return
	 * @throws NyARException 
	 */
	public static NyARParam createDefaultParameter() throws NyARException
	{
		ParamLoader pm=new ParamLoader();
		return new NyARParam(pm.size,pm.pmat,pm.dist_factor);
	}
	/**
	 * i_streamからARToolkitのカメラパラメータを読み出して、格納したインスタンスを生成します。
	 * @param i_stream
	 * @return
	 * @throws NyARException
	 */
	public static NyARParam createFromARParamFile(InputStream i_stream) throws NyARException
	{
		ParamLoader pm=new ParamLoader(i_stream);
		return new NyARParam(pm.size,pm.pmat,pm.dist_factor);
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
	 */		
	public static NyARParam createFromCvCalibrateCamera2Result(int i_w,int i_h,double[] i_intrinsic_matrix,double[] i_distortion_coeffs)
	{
		ParamLoader pm=new ParamLoader(i_w,i_h,i_intrinsic_matrix,i_distortion_coeffs);
		return new NyARParam(pm.size,pm.pmat,pm.dist_factor);
	}
	public NyARParam(NyARIntSize i_screen_size,NyARPerspectiveProjectionMatrix i_projection_mat,INyARCameraDistortionFactor i_dist_factor)
	{
		this._screen_size=new NyARIntSize(i_screen_size);
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
	 * この関数は、配列から値を設定します。
	 * @param i_factor
	 * NyARCameraDistortionFactorにセットする配列を指定する。要素数は4であること。
	 * @param i_projection
	 * NyARPerspectiveProjectionMatrixセットする配列を指定する。要素数は12であること。
	 */
	public void setValue(double[] i_factor,double[] i_projection)
	{
		this._dist.setValue(i_factor);
		this._projection_matrix.setValue(i_projection);
		return;
	}
	/**
	 * この関数は、現在のスクリーンサイズを変更します。
	 * ARToolKitのarParamChangeSize関数に相当します。
	 * @param i_xsize
	 * 新しいサイズ
	 * @param i_ysize
	 * 新しいサイズ
	 */
	public void changeScreenSize(int i_xsize, int i_ysize)
	{
		final double x_scale = (double) i_xsize / (double) (this._screen_size.w);// scale = (double)xsize / (double)(source->xsize);
		final double y_scale = (double) i_ysize / (double) (this._screen_size.h);// scale = (double)xsize / (double)(source->xsize);
		//スケールを変更
		this._dist.changeScale(x_scale,y_scale);
		this._projection_matrix.changeScale(x_scale,y_scale);
		this._screen_size.w = i_xsize;// newparam->xsize = xsize;
		this._screen_size.h = i_ysize;// newparam->ysize = ysize;
		return;
	}
	/**
	 * この関数は、現在のスクリーンサイズを変更します。
	 * {@link #changeScreenSize(int, int)のラッパーです。
	 * @param i_s
	 */
	public void changeScreenSize(NyARIntSize i_s)
	{
		this.changeScreenSize(i_s.w,i_s.h);
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
	 * パラメータローダーです。
	 */
	protected static class ParamLoader
	{
		public NyARIntSize size;
		public NyARPerspectiveProjectionMatrix pmat;
		public INyARCameraDistortionFactor dist_factor;
		/**
		 * intrinsic_matrixとdistortion_coeffsからインスタンスを初期化する。
		 * @param i_w
		 * カメラパラメータ生成時の画面サイズ
		 * @param i_h
		 * カメラパラメータ生成時の画面サイズ
		 * @param i_intrinsic_matrix 3x3 matrix このパラメータは、OpenCVのcvCalibrateCamera2関数が出力するintrinsic_matrixの値と合致します。
		 * @param i_distortion_coeffs 4x1 vector このパラメータは、OpenCVのcvCalibrateCamera2関数が出力するdistortion_coeffsの値と合致します。
		 */
		public ParamLoader(int i_w,int i_h,double[] i_intrinsic_matrix,double[] i_distortion_coeffs)
		{
			this.size=new NyARIntSize(i_w,i_h);
			//dist factor
			NyARCameraDistortionFactorV4 v4dist=new NyARCameraDistortionFactorV4();
			v4dist.setValue(this.size,i_intrinsic_matrix,i_distortion_coeffs);
			double s=v4dist.getS();
			this.dist_factor=v4dist;
			//projection matrix
			this.pmat=new NyARPerspectiveProjectionMatrix();
			NyARDoubleMatrix33 r=new NyARDoubleMatrix33();
			r.setValue(i_intrinsic_matrix);
		    r.m00 /= s;
		    r.m01 /= s;
		    r.m10 /= s;
		    r.m11 /= s;
			this.pmat.setValue(r, new NyARDoublePoint3d());
		}
		/**
		 * 標準パラメータでインスタンスを初期化します。
		 * @throws NyARException
		 */
		public ParamLoader()throws NyARException
		{
			double[] df={318.5,263.5,26.2,1.0127565206658486};
			double[] pj={	700.9514702992245,0,316.5,0,
							0,726.0941816535367,241.5,0.0,
							0.0,0.0,1.0,0.0,
							0.0,0.0,0.0,1.0};
			this.size=new NyARIntSize(640,480);
			this.pmat=new NyARPerspectiveProjectionMatrix();
			this.pmat.setValue(pj);
			this.dist_factor=new NyARCameraDistortionFactorV2();
			this.dist_factor.setValue(df);
		}
		/**
		 * ストリームから読み出したデータでインスタンスを初期化します。
		 * @param i_stream
		 * @throws NyARException
		 */
		public ParamLoader(InputStream i_stream)throws NyARException
		{
			try {
				//読み出し
				ByteBufferedInputStream bis=new ByteBufferedInputStream(i_stream,512);
				int s=bis.readToBuffer(512);
				bis.order(ByteBufferedInputStream.ENDIAN_BIG);
				//読み出したサイズでバージョンを決定
				int[] version_table={136,144,152,176};
				int version=-1;
				for(int i=0;i<version_table.length;i++){
					if(s%version_table[i]==0){
						version=i+1;
						break;
					}
				}
				//一致しなければ無し
				if(version==-1){
					throw new NyARException();
				}
				//size
				this.size=new NyARIntSize();
				this.size.setValue(bis.getInt(),bis.getInt());

				//projection matrix
				this.pmat=new NyARPerspectiveProjectionMatrix();
				double[] pjv=new double[16];
				for(int i=0;i<12;i++){
					pjv[i]=bis.getDouble();
				}			
				pjv[12]=pjv[13]=pjv[14]=0;
				pjv[15]=1;
				this.pmat.setValue(pjv);
				
				//dist factor
				double[] df;
				switch(version)
				{
				case 1://Version1
					df=new double[NyARCameraDistortionFactorV2.NUM_OF_FACTOR];
					this.dist_factor=new NyARCameraDistortionFactorV2();
					break;
				case 4://Version4
					df=new double[NyARCameraDistortionFactorV4.NUM_OF_FACTOR];
					this.dist_factor=new NyARCameraDistortionFactorV4();
					break;
				default:
					throw new NyARException();
				}
				for(int i=0;i<df.length;i++){
					df[i]=bis.getDouble();
				}
				this.dist_factor.setValue(df);
			} catch (Exception e) {
				throw new NyARException(e);
			}			
		}
	}
	/**
	 * この関数は機能しません。
	 * @param i_stream
	 * 未定義
	 * @throws Exception
	 */
	public void saveARParam(OutputStream i_stream)throws Exception
	{
		NyARException.trap("未チェックの関数");
	}
}
