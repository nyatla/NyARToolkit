package jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.NyARRasterFilter_Reverse;
import jp.nyatla.nyartoolkit.core.rasterfilter.NyARRasterFilter_Roberts;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;

/**
 * LowResolutionLabelingSamplerへの入力コンテナです。
 * このコンテナには、GS画像をセットできます。
 * プロパティには、解像度別のグレースケール画像があります。
 *
 */
public class LrlsSource
{
	public int _rob_resolution;
	/**
	 * 反転RobertsFilter画像のインスタンス
	 */
	public LrlsGsRaster _rbraster;
	public LrlsGsRaster _base_raster;
	public NyARVectorReader_INT1D_GRAY_8 _vec_reader;

	private LrlsGsRaster _rb_source;
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
	public LrlsSource(int i_width,int i_height,int i_depth,boolean i_is_alloc) throws NyARException
	{
		assert(i_depth>0);
		int div=(int)Math.pow(2,i_depth);
		this._rob_resolution=div;
		//主GSラスタ
		this._base_raster=new LrlsGsRaster(i_width,i_height,1,i_is_alloc);
		//Roberts変換ラスタ
		this._rb_source=new LrlsGsRaster(i_width/div,i_height/div,div, true);
		//Robertsラスタは最も解像度の低いラスタと同じ
		this._rbraster=new LrlsGsRaster(i_width/div,i_height/div,div, true);
		this._vec_reader=new NyARVectorReader_INT1D_GRAY_8(this._base_raster,this._rb_source);
	}
	/**
	 * GS画像をセットし、syncSourceで内部画像を更新します。
	 * この関数を使ってセットした画像は、インスタンスから参照されます。
	 * @param i_ref_source
	 * @throws NyARException 
	 */
	public void wrapBuffer(NyARGrayscaleRaster i_ref_source) throws NyARException
	{
		//バッファのスイッチ
		this._base_raster.wrapBuffer(i_ref_source.getBuffer());
		syncSource();
	}
	/**
	 * GS画像と他の内部画像を同期させます。this._base_rasterが内部参照の場合に、GS画像を更新した後に
	 * 呼び出してください。
	 * @param i_ref_source
	 * @throws NyARException
	 */
	public final void syncSource() throws NyARException
	{
		//GS->1/(2^n)NRBF
		//解像度を半分にしながらコピー
		NyARGrayscaleRaster.copy(this._base_raster,0,0,this._rob_resolution,this._rb_source);

		//最終解像度のエッジ検出画像を作成
		this._rfilter.doFilter(this._rb_source,this._rbraster);
		this._nfilter.doFilter(this._rbraster, this._rbraster);
	}
	

}
