package jp.nyatla.nyartoolkit.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.rpf.sampler.lrlabel.*;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.*;

/**
 * このクラスは、LowResolutionLabelingSamplerへの入力コンテナの抽象クラスです。
 * 基本GS画像と、ヒント画像（1/nサイズのRobertsエッジ画像）を持ち、これらに対する同期APIとアクセサを定義します。
 * <p>
 * 継承クラスでは、_rbraster,_base_raster,_vec_readerメンバ変数の実体と、abstract関数を実装してください。
 * </p>
 */
public abstract class NyARTrackerSource
{
	/** ヒント画像(Roberts画像)の解像度値。1/n表記*/
	protected int _rob_resolution;
	//継承クラスで設定されるべきオブジェクト
	
	/**　ヒント画像を格納するオブジェクト。継承クラスで割り当てること。*/
	protected NyARGrayscaleRaster _rbraster;
	/**　基本GS画像を格納するオブジェクト。継承クラスで割り当てること。*/
	protected NyARGrayscaleRaster _base_raster;
	/** 基本画像に対する画素ベクトル読出しオブジェクト。継承クラスで割り当てること。*/
	protected INyARVectorReader _vec_reader;
	/** サンプリングアウトを格納するオブジェクト。継承クラスで割り当てること。*/
	protected LowResolutionLabelingSamplerOut _sample_out;
	/**
	 * コンストラクタです。
	 * ヒント画像の解像度値を指定して、インスタンスを作成します。
	 * @param i_rob_resolution
	 * ヒント画像の解像度値。
	 */
	protected NyARTrackerSource(int i_rob_resolution)
	{
		this._rob_resolution=i_rob_resolution;
	}
	/**
	 * この関数は、Vector読み取りオブジェクトを返します。
	 * このオブジェクトは、使用前にインスタンスの同期が必要な事があります。
	 * 基本GS画像に変更を加えた後に{@link NyARTracker#progress}を実行せずに使用する場合は、
	 * {@link #makeSampleOut},または{@link #syncResource}関数を実行してから使用してください。
	 * @return
	 * 基本画像、ヒント画像を元にしたVectorリーダ
	 */
	public final INyARVectorReader getBaseVectorReader()
	{
		return this._vec_reader;
	}

	/**
	 * この関数は、ヒント画像(エッジ画像)の参照値を返します。
	 * このオブジェクトは、使用前にインスタンスの同期が必要な事があります。
	 * 基本GS画像に変更を加えた後に{@link NyARTracker#progress}を実行せずに使用する場合は、
	 * {@link #makeSampleOut},または{@link #syncResource}関数を実行してから使用してください。
	 * @return
	 * ヒント画像のオブジェクト。
	 */
	public final NyARGrayscaleRaster refEdgeRaster()
	{
		return this._rbraster;
	}
	/**
	 * この関数は、基準画像の参照値を返します。
	 * @return
	 * 基本画像のオブジェクト。
	 */
	public final NyARGrayscaleRaster refBaseRaster()
	{
		return this._base_raster;
	}
	/**
	 * この関数は、{@link LowResolutionLabelingSamplerOut}オブジェクトの参照値を返します。
	 * へのポインタを返します。
	 * このオブジェクトは、使用前にインスタンスの同期が必要な事があります。
	 * 基本GS画像に変更を加えた後に{@link NyARTracker#progress}を実行せずに使用する場合は、
	 * {@link #makeSampleOut}の戻り値を使うか、{@link #syncResource}関数を実行してから使用してください。
	 * @return
	 * 現在のSampleOutオブジェクトの参照値
	 */
	public final LowResolutionLabelingSamplerOut refLastSamplerOut()
	{
		return this._sample_out;
	}
	/**
	 * この関数は、基準画像と内部状態を同期します。
	 * 通常、ユーザがこの関数を使用することはありません。
	 * 実装クラスでは、{@link #_sample_out}を更新する関数を実装してください。
	 * @throws NyARException
	 */
	public abstract void syncResource() throws NyARException;
	
	/**
	 * この関数は、インスタンスのメンバを同期した後に、SampleOutを計算して、参照値を返します。
	 * この関数は、{@link NyARTracker#progress}が呼び出します。通常、ユーザが使用することはありません。
	 * 実装クラスでは、インスタンスの同期後に、{@link #_sample_out}を更新する関数を実装してください。
	 * @throws NyARException
	 */
	public abstract LowResolutionLabelingSamplerOut makeSampleOut() throws NyARException;
}
