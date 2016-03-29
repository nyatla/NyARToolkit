package jp.nyatla.nyartoolkit.nftsystem;

import java.util.ArrayList;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.KeyframeMap;
import jp.nyatla.nyartoolkit.core.kpm.matcher.FreakKeypointMatching;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftDataSet;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.markersystem.NyARSensor;
import jp.nyatla.nyartoolkit.markersystem.NyARSingleCameraSystem;

public class NyARNftSystem extends NyARSingleCameraSystem{

	protected NyARNftSystem(NyARParam i_ref_cparam) {
		super(i_ref_cparam);
		this._kpm_thread=new KpmThread(i_ref_cparam,this._nftdatalist);
	}
	private long _last_time_stamp=-1;
	private class NftTarget{
		/** 参照定義セット*/
		NyARNftDataSet dataset;
		NyARDoubleMatrix44 transmat=new NyARDoubleMatrix44();
		public NftTarget(NyARNftDataSet i_dataset)
		{
			this.dataset=i_dataset;
		}
	}

	private class NyARNftTargetList extends ArrayList<NftTarget>{
		private static final long serialVersionUID = 2150347966734138642L;	
	}
	final private KpmThread _kpm_thread;
	final private NyARNftTargetList _nftdatalist=new NyARNftTargetList();
	/**
	 * この関数は、入力したセンサ入力値から、インスタンスの状態を更新します。
	 * 関数は、センサオブジェクトから画像を取得して、マーカ検出、一致判定、トラッキング処理を実行します。
	 * @param i_sensor
	 * {@link MarkerSystem}に入力する画像を含むセンサオブジェクト。
	 * @throws NyARRuntimeException 
	 */
	public void update(NyARSensor i_sensor)
	{
		long time_stamp=i_sensor.getTimeStamp();
		//センサのタイムスタンプが変化していなければ何もしない。
		if(this._last_time_stamp==time_stamp){
			return;
		}
		//タイムスタンプの更新
		this._last_time_stamp=time_stamp;
		this._kpm_thread.updateInputImage(i_sensor.getGsImage());
		

		
		//トラッキング検出器に設定
		
	}

	/**
	 * NFTの特徴点データセットを追加します。
	 * @param i_dataset
	 * @return
	 */
	public int addNftTarget(NyARNftDataSet i_dataset){
		if(!this._nftdatalist.add(new NftTarget(i_dataset)))
		{
			throw new NyARRuntimeException();
		}
		return this._nftdatalist.size()-1;
	}
	
	/**
	 * この関数は指定NFTターゲットの変換行列を返します。
	 * @para i_id
	 * キーマップID（ハンドル）値。
	 * @return
	 * [readonly]
	 * 姿勢行列を格納したオブジェクト。座標系は、ARToolKit座標系です。
	 */
	public NyARDoubleMatrix44 getTransformMatrix(int i_id)
	{	
	}	
	/**
	 * キーポイントマッチングスレッド
	 *
	 */
	class KpmThread extends Thread
	{
		public boolean running_flag=false;
		final private FreakKeypointMatching _attached_matcher;
		final private NyARNftTargetList _ref_nftdatalist;
		public KpmThread(NyARParam i_ref_cparam,NyARNftTargetList i_attached_nftdatalist)
		{
			this._attached_matcher=new FreakKeypointMatching(i_ref_cparam);
			this._ref_nftdatalist=i_attached_nftdatalist;
			this.start();
		}
		public boolean updateInputImage(INyARGrayscaleRaster i_input)
		{
			synchronized(this){
				if(!this.running_flag){
					this._attached_matcher.updateInputImage(i_input);
					this.running_flag=true;
				}
			}
			return true;
		}
		public void addKeyMap(KeyframeMap i_key){
			synchronized(this._ref_nftdatalist){
				
			}
		}
		public void run()
		{
			for(;;){
				//マッチング開始待ち
				if(!this.running_flag){
					Thread.yield();
					continue;
				}
				//全てのキーマップをチェック
				this._attached_matcher.updateFeatureSet();
				synchronized(this._ref_nftdatalist){
					for(int i=this._ref_nftdatalist.size()-1;i>=0;i--){
						NftTarget nt=this._ref_nftdatalist.get(i);
						this._attached_matcher.kpmMatching(nt.dataset.freak_fset,nt.transmat,null);
						//ここで親に検出したキーポイントを通知する。
					}
				}
				//マッチング待ちに戻す
				synchronized(this){
					this.running_flag=false;
				}
			}
		}
		
	}
}
