package jp.nyatla.nyartoolkit.nftsystem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.KeyframeMap;
import jp.nyatla.nyartoolkit.core.kpm.matcher.FreakKeypointMatching;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftDataSet;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.surfacetracking.NyARSurfaceTracker;
import jp.nyatla.nyartoolkit.core.surfacetracking.transmat.NyARSurfaceTrackingTransmatUtils;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResultParam;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.markersystem.NyARSensor;
import jp.nyatla.nyartoolkit.markersystem.NyARSingleCameraSystem;

public class NyARNftSystem extends NyARSingleCameraSystem{

	protected NyARNftSystem(INyARNftSystemConfig i_config) {
		super(i_config.getNyARParam());
		this._kpm_thread=new KpmThread(i_config.getNyARParam(),this._nftdatalist);
		this._surface_tracker=new NyARSurfaceTracker(i_config.getNyARParam(),16);
		this._sftrackingutils=new NyARSurfaceTrackingTransmatUtils(i_config.getNyARParam(),5.0);
		this._kpm_thread.start();
	}
	private long _last_time_stamp=-1;
	private class NftTarget{
		/** KPMによる初期検出対象*/
		final private static int ST_KPM_SEARCH	=1;
		final private static int ST_KPM_FOUND 	=2;
		/** AR2SURFACEトラッキングによる検出状態維持*/
		final static int ST_AR2_TRACKING=3;
		public int stage;
		/** 参照定義セット*/
		NyARNftDataSet dataset;
		/** KPMスレッドの出力結果*/
		NyARDoubleMatrix44 back_transmat=new NyARDoubleMatrix44();
		/** transmat_backが更新されたかのフラグ*/
		public boolean back_has_result;

		/** メインスレッド用の出力結果*/
		public NyARDoubleMatrix44 front_transmat=new NyARDoubleMatrix44();


		public NftTarget(NyARNftDataSet i_dataset)
		{
			this.dataset=i_dataset;
			this.stage=ST_KPM_SEARCH;
			this.back_has_result=false;
		}
	}

	private class NyARNftTargetList extends ArrayList<NftTarget>{
		private static final long serialVersionUID = 2150347966734138642L;	
	}
	final private KpmThread _kpm_thread;
	final private NyARNftTargetList _nftdatalist=new NyARNftTargetList();
	final private NyARSurfaceTrackingTransmatUtils _sftrackingutils;
	
	final private NyARSurfaceTracker _surface_tracker;	
	
	//WORK AREA
	final private NyARDoublePoint2d[] _pos2d=NyARDoublePoint2d.createArray(16);
	final private NyARDoublePoint3d[] _pos3d=NyARDoublePoint3d.createArray(16);	
	final private NyARTransMatResultParam result_param=new NyARTransMatResultParam();
	
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

		//ステータス遷移

		
		NyARDoublePoint2d[] pos2d=this._pos2d;
		NyARDoublePoint3d[] pos3d=this._pos3d;
		INyARGrayscaleRaster gs=i_sensor.getGsImage();
		
		//KPMスレッドによる更新()
		this._kpm_thread.updateInputImage(gs);

		//SurfaceTrackingによるfrontデータの更新
		for(NftTarget target:this._nftdatalist){
			if(target.stage<NftTarget.ST_KPM_FOUND){
System.out.println("NOTHING");
				//KPM検出前なら何もしない。
				continue;
			}
			switch(target.stage){
			case NftTarget.ST_AR2_TRACKING:
				//NftTarget.ST_AR2_TRACKING以降				
				//front_transmatに作る。
				NyARSurfaceTracker st=this._surface_tracker;
				int nop=st.tracking(gs,target.dataset.surface_dataset,target.front_transmat, this._pos2d, pos3d,16);
				if(nop==0){
					//失敗
					target.stage=NftTarget.ST_KPM_SEARCH;
System.out.println("ST_KPM_SEARCH");
					continue;
				}
				//Transmatの試験
				NyARDoublePoint3d off=NyARSurfaceTrackingTransmatUtils.centerOffset(pos3d,nop,new NyARDoublePoint3d());
				NyARSurfaceTrackingTransmatUtils.modifyInputOffset(target.front_transmat, pos3d,nop,off);//ARTK5の補正
				if(!this._sftrackingutils.surfaceTrackingTransmat(target.front_transmat, pos2d, pos3d, nop,target.front_transmat,this.result_param)){
					//失敗
					target.stage=NftTarget.ST_KPM_SEARCH;
System.out.println("ST_KPM_SEARCH");
					continue;
				}
				NyARSurfaceTrackingTransmatUtils.restoreOutputOffset(target.front_transmat,off);//ARTK5の補正
				break;
			case NftTarget.ST_KPM_FOUND:
				target.stage=NftTarget.ST_AR2_TRACKING;
System.out.println("ST_AR2_TRACKING");
				break;
			}
		}
	}
	public int addNftTarget(String i_fileset_prefix)
	{
		return this.addNftTarget(NyARNftDataSet.loadFromNftFiles(i_fileset_prefix));
	}

	/**
	 * NFTの特徴点データセットを追加します。
	 * @param i_dataset
	 * @return
	 * 特徴点セットのID値
	 */
	public int addNftTarget(NyARNftDataSet i_dataset)
	{
		//KPMスレッドが待機中になるまで待つ
		while(this._kpm_thread.getState()!=Thread.State.WAITING){
			Thread.yield();
		}
		//追加
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
		NftTarget target=this._nftdatalist.get(i_id);
		return target.front_transmat;
	}
	public boolean isExistTarget(int i_id)
	{
		return this._nftdatalist.get(i_id).stage>NftTarget.ST_KPM_FOUND;
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
			super();
			this._attached_matcher=new FreakKeypointMatching(i_ref_cparam);
			this._ref_nftdatalist=i_attached_nftdatalist;
		}
		/** この関数はメインクラスのupdateから実行します。
		 * 
		 */
		public boolean updateInputImage(INyARGrayscaleRaster i_input)
		{
			synchronized(this){
				Thread.State st=this.getState();
				//スレッドが無期限待機中でなければなにもしない。
				if(st!=Thread.State.WAITING){
					return false;
				}
				//計算結果のコピー
				for(NftTarget target : this._ref_nftdatalist){
					//検出フラグの更新
					if(target.back_has_result && target.stage==NftTarget.ST_KPM_SEARCH){
						//見つかった時だけ更新
System.out.println("ST_KPM_FOUND");
						
						target.stage=NftTarget.ST_KPM_FOUND;
						target.front_transmat.setValue(target.back_transmat);
						target.back_has_result=false;
					}
				}
				//KPMスレッドの再開
				this._attached_matcher.updateInputImage(i_input);
				this.notify();
			}
			return true;
		}
		public void addKeyMap(KeyframeMap i_key){
			synchronized(this._ref_nftdatalist){
			}
		}
		@Override
		public void run()
		{
			try {
				for(;;){
					//開始待ち
					synchronized(this){
						this.wait();
					}
					//全てのキーマップをチェック
					this._attached_matcher.updateFeatureSet();
					for(NftTarget nt : this._ref_nftdatalist){
						//検出ステージチェック(別スレッドからの更新と衝突した場合は1フレーム無駄になる。)
						if(nt.stage>NftTarget.ST_KPM_SEARCH){
System.out.println("TR:ST_KPM_SEARCH");
							continue;
						}
						//N番目のNFTターゲットのバックグラウンドに書き込み
						nt.back_has_result=this._attached_matcher.kpmMatching(nt.dataset.freak_fset,nt.back_transmat);
System.out.println("TR:ST_KPM_SEARCH2");
					}
				}
			} catch (InterruptedException e) {
				//Interrupted!
				return;
			}
		}
	}
	public void shutdown()
	{
		try {
			this._kpm_thread.interrupt();
			this._kpm_thread.join();
		} catch (InterruptedException e) {
		}
	}
	public static void main(String[] args)
	{
		String img_file="../Data/testcase/test.raw";
		String cparam=	"../Data/testcase/camera_para5.dat";
		String nftdataset="../Data/testcase/pinball";
		//カメラパラメータ
		try {
			INyARRgbRaster rgb=NyARRgbRaster.createInstance(640,480,NyARBufferType.BYTE1D_B8G8R8X8_32);
			FileInputStream fs = new FileInputStream(img_file);
			fs.read((byte[])rgb.getBuffer());

			NyARParam param = NyARParam.loadFromARParamFile(new FileInputStream(cparam),640,480,NyARParam.DISTFACTOR_LT_ARTK5);
			NyARSensor sensor=new NyARSensor(640,480);
			NyARNftSystem ms=new NyARNftSystem(new NyARNftSystemConfig(param));
			int id=ms.addNftTarget(NyARNftDataSet.loadFromNftFiles(nftdataset));
			System.out.println(id);
			sensor.update(rgb);
			for(int i=0;i<10;i++){
				sensor.updateTimeStamp();
				ms.update(sensor);
				Thread.sleep(100);
			}
			ms.shutdown();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
