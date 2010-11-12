package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.NyARRasterFilter_Reverse;
import jp.nyatla.nyartoolkit.core.rasterfilter.NyARRasterFilter_Roberts;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSampler;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;

/**
 * NyARTrackerSourceのリファレンス実装です。
 * 全ての画像処理を処理系のソフトウェアで実装します。
 */
public class NyARTrackerSource_Reference extends NyARTrackerSource
{
	/**
	 * 反転RobertsFilter画像のインスタンス
	 */
	private LowResolutionLabelingSampler _sampler;
	private NyARGrayscaleRaster _rb_source;
	private NyARRasterFilter_Roberts _rfilter=new NyARRasterFilter_Roberts(NyARBufferType.INT1D_GRAY_8);
	private NyARRasterFilter_Reverse _nfilter=new NyARRasterFilter_Reverse(NyARBufferType.INT1D_GRAY_8);
	/**
	 * @param i_width
	 * ソース画像のサイズ
	 * @param i_height
	 * ソース画像のサイズ
	 * @param i_depth
	 * 解像度の深さ(1/(2^n))倍の画像として処理する。
	 * @param i_is_alloc
	 * ベースラスタのバッファを内部確保外部参照にするかのフラグです。
	 * trueの場合、バッファは内部に確保され、wrapBuffer関数が使用できなくなります。
	 * @throws NyARException
	 */
	public NyARTrackerSource_Reference(int i_width,int i_height,int i_depth,boolean i_is_alloc) throws NyARException
	{
		super((int)Math.pow(2,i_depth));
		assert(i_depth>0);
		int div=this._rob_resolution;
		//主GSラスタ
		this._base_raster=new NyARGrayscaleRaster(i_width,i_height,NyARBufferType.INT1D_GRAY_8,i_is_alloc);
		//Roberts変換ラスタ
		this._rb_source=new NyARGrayscaleRaster(i_width/div,i_height/div,NyARBufferType.INT1D_GRAY_8, true);
		//Robertsラスタは最も解像度の低いラスタと同じ
		this._rbraster=new NyARGrayscaleRaster(i_width/div,i_height/div,NyARBufferType.INT1D_GRAY_8, true);
		this._vec_reader=new NyARVectorReader_INT1D_GRAY_8(this._base_raster,this._rbraster);
		//samplerとsampleout
		this._sampler=new LowResolutionLabelingSampler(i_width, i_height,(int)Math.pow(2,i_depth));
		
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


	public void syncResource() throws NyARException
	{
		//内部状態の同期
		NyARGrayscaleRaster.copy(this._base_raster,0,0,this._rob_resolution,this._rb_source);
		this._rfilter.doFilter(this._rb_source,this._rbraster);
		this._nfilter.doFilter(this._rbraster, this._rbraster);
	}
	/**
	 * SampleOutを計算して返します。
	 * この関数は、NyARTrackerが呼び出します。
	 * @param samplerout
	 * @throws NyARException
	 */
	public void getSampleOut(LowResolutionLabelingSamplerOut samplerout) throws NyARException
	{
		syncResource();
		this._sampler.sampling(this._rbraster,samplerout);
	}
	

}
