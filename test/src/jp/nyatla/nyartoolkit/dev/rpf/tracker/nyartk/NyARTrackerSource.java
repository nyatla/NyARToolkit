package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.dev.rpf.sampler.lrlabel.LowResolutionLabelingSamplerOut;

/**
 * LowResolutionLabelingSamplerへの入力コンテナの抽象クラスです。
 * 基本GS画像と、1/nサイズのRobertsエッジ検出画像を持ち、これらに対する同期APIとアクセサを定義します。
 * <p>
 * 継承クラスでは、_rbraster,_base_raster,_vec_readerメンバ変数の実体と、abstract関数を実装してください。
 * </p>
 */
public abstract class NyARTrackerSource
{
	protected int _rob_resolution;
	//継承クラスで設定されるべきオブジェクト
	protected NyARGrayscaleRaster _rbraster;
	protected NyARGrayscaleRaster _base_raster;
	protected NyARVectorReader_INT1D_GRAY_8 _vec_reader;	
	/**
	 * Robertsエッジ画像の解像度を指定する。
	 * @param i_rob_resolution
	 */
	protected NyARTrackerSource(int i_rob_resolution)
	{
		this._rob_resolution=i_rob_resolution;
	}
	/**
	 * 基本GS画像に対するVector読み取り機を返します。
	 * このインスタンスは、基本GS画像と同期していないことがあります。
	 * 基本GS画像に変更を加えた場合は、getSampleOut,またはsyncResource関数を実行して同期してから実行してください。
	 * @return
	 */
	public final NyARVectorReader_INT1D_GRAY_8 getBaseVectorReader()
	{
		return this._vec_reader;
	}

	/**
	 * エッジ画像を返します。
	 * このインスタンスは、基本GS画像と同期していないことがあります。
	 * 基本GS画像に変更を加えた場合は、getSampleOut,またはsyncResource関数を実行して同期してから実行してください。
	 * 継承クラスでは、エッジ画像を返却してください。
	 * @return
	 */
	public final NyARGrayscaleRaster getEdgeRaster()
	{
		return this._rbraster;
	}
	/**
	 * 基準画像を返します。
	 * 継承クラスでは、基本画像を返却してください。
	 * @return
	 */
	public final NyARGrayscaleRaster getBaseRaster()
	{
		return this._base_raster;
	}
	/**
	 * 基準画像と内部状態を同期します。(通常、アプリケーションからこの関数を使用することはありません。)
	 * 継承クラスでは、基本画像とエッジ画像を同期させてください。
	 * @throws NyARException
	 */
	public abstract void syncResource() throws NyARException;
	/**
	 * SampleOutを計算して返します。
	 * この関数は、NyARTrackerが呼び出します。
	 * 継承クラスでは、エッジ画像からsampleOutを作成する関数を実装してください。
	 * @param samplerout
	 * @throws NyARException
	 */
	public abstract void getSampleOut(LowResolutionLabelingSamplerOut samplerout) throws NyARException;
}
