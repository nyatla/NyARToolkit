package jp.nyatla.nyartoolkit.dev.rpf.sampler.lowresolution;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;

/**
 * LowResolutionLabelingSamplerへの入力コンテナです。
 * このコンテナには、GS画像をセットできます。
 * プロパティには、解像度別のグレースケール画像があります。
 *
 */
public class LowResolutionLabelingSamplerIn
{
	private NyARGrayscaleRaster[] _raster;

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
		this._raster=new NyARGrayscaleRaster[i_depth];
		//1番目のラスタは外部バッファを参照
		this._raster[0]=new NyARGrayscaleRaster(i_width,i_height,false);
		//2番目のラスタ以降は内部バッファを参照
		int div=2;
		for(int i=1;i<i_depth;i++)
		{
			this._raster[i]=new NyARGrayscaleRaster(i_width/div,i_height/div);
			div*=2;
		}
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
	}
	/**
	 * 指定した深さのラスタを取り出します。0は元画像、1以降は、元画像の1/2^nの解像度の画像です。
	 * @param i_depth
	 * ラスタの深さ。値の範囲は、0<=n<getDepth()です。
	 * @return
	 */
	public NyARGrayscaleRaster getRasterByDepth(int i_depth)
	{
		return this._raster[i_depth];
	}
}
