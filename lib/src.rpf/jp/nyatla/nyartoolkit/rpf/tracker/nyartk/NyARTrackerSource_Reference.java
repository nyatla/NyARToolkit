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
package jp.nyatla.nyartoolkit.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.param.INyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.rpf.sampler.lrlabel.LowResolutionLabelingSampler;
import jp.nyatla.nyartoolkit.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;
import jp.nyatla.nyartoolkit.rpf.utils.INyARGsRasterGraphics;
import jp.nyatla.nyartoolkit.rpf.utils.NyARGsRasterGraphicsFactory;

/**
 * このクラスは、NyARTrackerSourceのリファレンス実装です。
 * 全ての画像処理を処理系のソフトウェアで実装します。
 * <p>基本的な使い方 - 
 * このクラスは、次のような手順で使います。
 * <ul>
 * <li>{@link #refBaseRaster}関数で、基本グレースケール画像を得る。
 * <li>基本グレースケール画像に、新しいイメージを書込む。
 * <li>{@link NyARTracker#progress}関数に入力する。
 * </ul>
 * インスタンスの所有するラスタ間の同期は、{@link NyARTracker#progress}がコールする{@link #makeSampleOut}関数で行います。
 * </p>
 */
public class NyARTrackerSource_Reference extends NyARTrackerSource
{
	/**
	 * 反転RobertsFilter画像のインスタンス
	 */
	private LowResolutionLabelingSampler _sampler;
	private NyARGrayscaleRaster _rb_source;
	private NegativeSqRoberts _rfilter=new NegativeSqRoberts(NyARBufferType.INT1D_GRAY_8);
	private INyARGsRasterGraphics _gs_graphics;
	/**
	 * コンストラクタです。
	 * ヒント画像
	 * @param i_number_of_sample
	 * サンプラが検出する最大数。
	 *　通常100~200以上を指定します。(QVGA画像あたり、100個を基準にします。)
	 * 数が少なすぎると、検出率が低下します。最低でも、NyARTrackerに設定するターゲット数の合計*2以上を指定してください。
	 * @param i_ref_raster_distortion
	 * 歪み矯正の為のオブジェクトを指定します。歪み矯正が必要ない時は、NULLを指定します。
	 * @param i_width
	 * ソース画像のサイズ
	 * @param i_height
	 * ソース画像のサイズ
	 * @param i_depth
	 * 解像度の深さ(1/(2^n))倍の画像として処理する。
	 * @param i_is_alloc
	 * ベースラスタのバッファを内部確保外部参照にするかのフラグです。
	 * trueの場合、バッファは内部に確保され、{@link #wrapBuffer}関数が使用できなくなります。
	 * @throws NyARException
	 */
	public NyARTrackerSource_Reference(int i_number_of_sample,INyARCameraDistortionFactor i_ref_raster_distortion,int i_width,int i_height,int i_depth,boolean i_is_alloc) throws NyARException
	{
		super((int)Math.pow(2,i_depth));
		assert(i_depth>0);
		int div=this._rob_resolution;
		//主GSラスタ
		this._base_raster=new NyARGrayscaleRaster(i_width,i_height,NyARBufferType.INT1D_GRAY_8,i_is_alloc);
		this._gs_graphics=NyARGsRasterGraphicsFactory.createDriver(this._base_raster);
		//Roberts変換ラスタ
		this._rb_source=new NyARGrayscaleRaster(i_width/div,i_height/div,NyARBufferType.INT1D_GRAY_8, true);
		//Robertsラスタは最も解像度の低いラスタと同じ
		this._rbraster=new NyARGrayscaleRaster(i_width/div,i_height/div,NyARBufferType.INT1D_GRAY_8, true);
		this._vec_reader=new NyARVectorReader_INT1D_GRAY_8(this._base_raster,i_ref_raster_distortion,this._rbraster);
		//samplerとsampleout
		this._sampler=new LowResolutionLabelingSampler(i_width, i_height,(int)Math.pow(2,i_depth));
		this._sample_out=new LowResolutionLabelingSamplerOut(i_number_of_sample);
	}
	/**
	 * GS画像をセットします。
	 * この関数を使ってセットした画像は、インスタンスから参照されます。
	 * @param i_ref_source
	 * @throws NyARException 
	 */
	public void wrapBuffer(NyARGrayscaleRaster i_ref_source) throws NyARException
	{
		//バッファのスイッチ
		this._base_raster.wrapBuffer(i_ref_source.getBuffer());
	}

	/**
	 * この関数は、基準画像と内部状態を同期します。 通常、ユーザがこの関数を使用することはありません。 
	 */
	public void syncResource() throws NyARException
	{
		//内部状態の同期
		this._gs_graphics.copyTo(0,0,this._rob_resolution,this._rb_source);
		this._rfilter.doFilter(this._rb_source,this._rbraster);
	}
	/**
	 * SampleOutを計算して返します。
	 * この関数は、NyARTrackerが呼び出します。
	 * 通常、ユーザがこの関数を使用することはありません。 
	 */
	public LowResolutionLabelingSamplerOut makeSampleOut() throws NyARException
	{
		syncResource();
		//敷居値自動調整はそのうちね。
		this._sampler.sampling(this._rbraster,220,this._sample_out);
		return this._sample_out;
	}
	

}
