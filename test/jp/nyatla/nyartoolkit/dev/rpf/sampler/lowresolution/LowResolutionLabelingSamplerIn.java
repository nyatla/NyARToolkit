package jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution;

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
public class LowResolutionLabelingSamplerIn
{
	private LrlsGsRaster[] _raster;
	public  LrlsGsRaster _rbraster;
	private NyARRasterFilter_Roberts _rfilter=new NyARRasterFilter_Roberts(NyARBufferType.INT1D_GRAY_8);
	private NyARRasterFilter_Reverse _nfilter=new NyARRasterFilter_Reverse(NyARBufferType.INT1D_GRAY_8);

	/**
	 * ラスタの深さを返します。
	 * @return
	 */
	public int getDepth()
	{
		return this._raster.length;
	}
	/**
	 * 
	 * @param i_width
	 * @param i_height
	 * @param i_depth
	 * @throws NyARException
	 */
	public LowResolutionLabelingSamplerIn(int i_width,int i_height,int i_depth) throws NyARException
	{
		this._raster=new LrlsGsRaster[i_depth];
		//1番目のラスタは外部バッファを参照
		this._raster[0]=new LrlsGsRaster(i_width,i_height,null,1,false);
		//2番目のラスタ以降は内部バッファを参照
		int div=1;
		for(int i=1;i<i_depth;i++)
		{
			div*=2;
			this._raster[i]=new LrlsGsRaster(i_width/div,i_height/div,this._raster[0],div,true);
		}
		//Robertsラスタは最も解像度の低いラスタと同じ
		this._rbraster=new LrlsGsRaster(i_width/div,i_height/div,this._raster[0], div, true);
	}
	/**
	 * GS画像をセットします。この関数を使ってセットした画像は、インスタンスから参照されます。
	 * @param i_ref_source
	 * @throws NyARException 
	 */
	public void wrapBuffer(NyARGrayscaleRaster i_ref_source) throws NyARException
	{
		//バッファのスイッチ
		this._raster[0].wrapBuffer(i_ref_source.getBuffer());
		int len=this._raster.length;
		//解像度を半分にしながらコピー
		for(int i=1;i<len;i++){
			NyARGrayscaleRaster.copy(this._raster[i-1],0,0,2,this._raster[i]);
		}
		//最終解像度のエッジ検出画像を作成
		this._rfilter.doFilter(this._raster[this._raster.length-1],this._rbraster);
		this._nfilter.doFilter(this._rbraster, this._rbraster);
		
	}
	/**
	 * 指定した深さのラスタを取り出します。0は元画像、1以降は、元画像の1/2^nの解像度の画像です。
	 * @param i_depth
	 * ラスタの深さ。値の範囲は、0<=n<getDepth()です。
	 * @return
	 */
	public LrlsGsRaster getRasterByDepth(int i_depth)
	{
		return this._raster[i_depth];
	}
}
