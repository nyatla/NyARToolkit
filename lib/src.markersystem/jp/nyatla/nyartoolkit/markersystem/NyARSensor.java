/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
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
package jp.nyatla.nyartoolkit.markersystem;



import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.INyARHistogramFromRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.INyARPerspectiveCopy;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.INyARRgb2GsFilter;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARHistogram;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;



/**
 * このクラスは、{@link NyARMarkerSystem}へ入力するセンサ情報（画像）を管理します。
 * センサ情報のスナップショットに対するアクセサ、形式変換機能を提供します。
 * 管理している情報は、元画像（カラー）、グレースケール画像、ヒストグラムです。
 * このインスタンスは{@link NyARMarkerSystem#update(NyARSensor)}関数により、{@link NyARMarkerSystem}に入力します。
 */
public class NyARSensor
{
	protected NyARHistogram _gs_hist;
	protected INyARRgbRaster _ref_raster=null;
	protected INyARGrayscaleRaster _gs_raster;
	protected long _src_ts;
	protected long _gs_id_ts;
	protected long _gs_hist_ts;
	/**
	 * 画像サイズ（スクリーンサイズ）を指定して、インスタンスを生成します。
	 * @param i_size
	 * 画像のサイズ。
	 * @throws NyARException
	 */
	public NyARSensor(NyARIntSize i_size) throws NyARException
	{
		this.initInstance(i_size);
		this._hist_drv=(INyARHistogramFromRaster) this._gs_raster.createInterface(INyARHistogramFromRaster.class);
	}
	/**
	 * この関数は、画像ドライバに依存するインスタンスを生成する。
	 * 継承クラスで上書きする。
	 * @param s
	 * @throws NyARException
	 */
	protected void initResource(NyARIntSize s) throws NyARException
	{
		this._gs_raster=new NyARGrayscaleRaster(s.w,s.h,NyARBufferType.INT1D_GRAY_8,true);
	}
	/**
	 * 
	 * @param i_size
	 * @throws NyARException
	 */
	private void initInstance(NyARIntSize i_size) throws NyARException
	{
		//リソースの生成
		this.initResource(i_size);
		this._gs_hist=new NyARHistogram(256);
		this._src_ts=0;
		this._gs_id_ts=0;
		this._gs_hist_ts=0;
	}
	/**
	 * この関数は、現在のカラー画像の射影変換ドライバを返します。
	 * この関数は、内部処理向けの関数です。
	 * @return
	 * [readonly]
	 * 射影変換ドライバのオブジェクト。
	 */
	public INyARPerspectiveCopy getPerspectiveCopy()
	{
		return this._pcopy;
	}	
	private INyARHistogramFromRaster _hist_drv=null;	
	private INyARPerspectiveCopy _pcopy;
	private INyARRgb2GsFilter _rgb2gs=null;
	/**
	 * この関数は、入力画像を元に、インスタンスの状態を更新します。
	 * この関数は、タイムスタンプをインクリメントします。
	 * @param i_input
	 * カラー画像。画像のサイズは、コンストラクタに設定したスクリーンサイズと同じである必要があります。
	 * この画像は、次回の{@link #update}まで、インスタンスから参照されます。
	 * @throws NyARException 
	 */
	public void update(INyARRgbRaster i_input) throws NyARException
	{
		//ラスタドライバの準備
		if(this._ref_raster!=i_input){
			this._rgb2gs=(INyARRgb2GsFilter) i_input.createInterface(INyARRgb2GsFilter.class);
			this._pcopy=(INyARPerspectiveCopy) i_input.createInterface(INyARPerspectiveCopy.class);
			this._ref_raster=i_input;
		}
		//ソースidのインクリメント
		this._src_ts++;
	}
	/**
	 * この関数は、タイムスタンプを強制的にインクリメントします。
	 */
	public void updateTimeStamp()
	{
		this._src_ts++;
	}
	/**
	 * この関数は、現在のタイムスタンプを返します。
	 * タイムスタンプは0から始まる整数値で、{@link #update(INyARRgbRaster)},{@link #updateTimeStamp()}
	 * 関数をコールするごとにインクリメントされます。
	 * @return
	 * タイムスタンプ値
	 */
	public long getTimeStamp()
	{
		return this._src_ts;
	}
	/**
	 * この関数は、グレースケールに変換した現在の画像を返します。
	 * @return
	 * [readonly]
	 * グレースケールに変換した現在の画像
	 * @throws NyARException 
	 */
	public INyARGrayscaleRaster getGsImage() throws NyARException
	{
		//必要に応じてグレースケール画像の生成
		if(this._src_ts!=this._gs_id_ts){
			this._rgb2gs.convert(this._gs_raster);
			this._gs_id_ts=this._src_ts;
		}
		return this._gs_raster;
		//
	}
	/**
	 * この関数は、現在のGS画像のﾋｽﾄｸﾞﾗﾑを返します。
	 * @return
	 * [readonly]
	 * 256スケールのヒストグラム。
	 * @throws NyARException 
	 */
	public NyARHistogram getGsHistogram() throws NyARException
	{
		//必要に応じてヒストグラムを生成
		if(this._gs_id_ts!=this._gs_hist_ts){
			this._hist_drv.createHistogram(4,this._gs_hist);
			this._gs_hist_ts=this._gs_id_ts;
		}
		return this._gs_hist;
	}
	/**
	 * この関数は、現在の入力画像の参照値を返します。
	 * @return
	 * [readonly]
	 * {@link #update}に最後に入力した画像。一度も{@link #update}をコールしなかったときは未定。
	 */
	public INyARRgbRaster getSourceImage()
	{
		return this._ref_raster;
	}
	
	/**
	 * この関数は、RGB画像の任意の4頂点領域を、射影変換してi_raster取得します。
	 * {@link #getPerspectiveImage(double, double, double, double, double, double, double, double, INyARRgbRaster)}
	 * のint引数版です。
	 * @param i_x1
	 * @param i_y1
	 * @param i_x2
	 * @param i_y2
	 * @param i_x3
	 * @param i_y3
	 * @param i_x4
	 * @param i_y4
	 * @param i_raster
	 * @return
	 * @throws NyARException 
	 */
	public INyARRgbRaster getPerspectiveImage(
	    int i_x1,int i_y1,
	    int i_x2,int i_y2,
	    int i_x3,int i_y3,
	    int i_x4,int i_y4,
	    INyARRgbRaster i_raster) throws NyARException
	{
		this._pcopy.copyPatt(i_x1, i_y1, i_x2, i_y2, i_x3, i_y3, i_x4, i_y4,0,0,1, i_raster);
		return i_raster;
	}
	/**
	 * この関数は、RGB画像の任意の4頂点領域を、射影変換してi_raster取得します。
	 * 出力画像の解像度は、i_rasterに一致します。
	 * @param i_x1
	 * 頂点1[pixel]
	 * @param i_y1
	 * 頂点1[pixel]
	 * @param i_x2
	 * 頂点2[pixel]
	 * @param i_y2
	 * 頂点2[pixel]
	 * @param i_x3
	 * 頂点3[pixel]
	 * @param i_y3
	 * 頂点3[pixel]
	 * @param i_x4
	 * 頂点4[pixel]
	 * @param i_y4
	 * 頂点4[pixel]
	 * @param i_raster
	 * 射影変換した画像を受け取るオブジェクト
	 * @return
	 * 結果を格納したi_rasterオブジェクト。
	 * @throws NyARException 
	 */
	public INyARRgbRaster getPerspectiveImage(
		    double i_x1,double i_y1,
		    double i_x2,double i_y2,
		    double i_x3,double i_y3,
		    double i_x4,double i_y4,
		    INyARRgbRaster i_raster) throws NyARException
		{
			this._pcopy.copyPatt(i_x1, i_y1, i_x2, i_y2, i_x3, i_y3, i_x4, i_y4,0,0,1, i_raster);
			return i_raster;
		}	
}
