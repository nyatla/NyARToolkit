package jp.nyatla.nyartoolkit.rpf.mklib;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.NyARReality;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.NyARRealityTarget;
import jp.nyatla.nyartoolkit.rpf.realitysource.nyartk.NyARRealitySource;


/**
 * このクラスは、非同期にIDマーカを検索するIdマーカテーブルです。(非同期な {@link RawbitSerialIdTable}と考えてください。)
 * テーブルには、IDマーカの範囲と、そのメタデータをセットで登録できます。
 * このクラスは非同期にマーカパターン一致検索を行うシーケンスのサンプルになっています。
 * 動作としては、マーカ判定を非同期なスレッドに問い合わせて、3秒後に結果を返却します。
 * <p>応用-
 * 非同期なマーカ一致探索をシーケンスを応用すると、マーカの一致探索に時間がかけられます。
 * 例として、外部サーバで高精度な画像一致探索を行うシーケンスを作ることができます。
 * 枠内にある画像を外部サーバに送信して、その画像のマーカメタデータを返すなどです。
 * 但し、毎回外部サーバに問い合わせるとパフォーマンスの劣化が激しいので、実際には結果をキャッシュ
 * するなどの対策が必要になります。
 * </p>
 */
public class ASyncIdMarkerTable
{
	/**
	 * このインタフェイスは、{@link ASyncIdMarkerTable}のコンストラクタに使います。
	 * クラスが、非同期なイベントを通知するために使います。
	 */
	public interface IResultListener
	{
		/**
		 * この関数は、マーカの認識に成功した事を知らせます。
		 * @param i_result
		 * 認識の成功/失敗を示す真偽値。
		 * @param i_serial
		 * 特定したNyARRealityTargetのシリアル番号。
		 * イベント受信者は、このシリアル番号を元に、{@link NyARReality}のAPIを使って、ターゲットのステータス遷移関数を走査します。
		 * @param i_dir
		 * ARToolKit準拠の方位定数。
		 * @param i_width
		 * マーカの縦横物理サイズ(mm)
		 * @param id
		 * {@link RawbitSerialIdTable}で定義する、IDマーカ番号。
		 */
		public void OnDetect(boolean i_result,long i_serial,int i_dir,double i_width,long id);
	}
	private RawbitSerialIdTable _mklib;
	private IResultListener _listener;
	/** 非同期一致検索クラス*/
	private class AsyncThread extends Thread
	{
		private ASyncIdMarkerTable _parent;
		private long _serial;
		private NyARRgbRaster _source;
		public AsyncThread(ASyncIdMarkerTable i_parent,long i_serial,NyARRgbRaster i_raster)
		{
			this._parent=i_parent;
			this._serial=i_serial;
			this._source=i_raster;
		}
		public void run()
		{
	      try {
	          sleep(3000);
        	  RawbitSerialIdTable.IdentifyIdResult ret=new RawbitSerialIdTable.IdentifyIdResult();
        	  boolean res;
	          synchronized(this._parent._mklib){
	        	  NyARDoublePoint2d[] vx=NyARDoublePoint2d.createArray(4);
	        	  //反時計まわり
	        	  vx[0].x=0; vx[0].y=0;
	        	  vx[1].x=99;vx[1].y=0;
	        	  vx[2].x=99;vx[2].y=99;
	        	  vx[3].x=0; vx[3].y=99;
	        	  res=this._parent._mklib.identifyId(vx,this._source,ret);
	          }
	          this._parent.callListener(res,this._serial,ret.artk_direction,ret.marker_width,ret.id);
	        } catch (Exception e){
				e.printStackTrace();
			}
			
		}
	}
	/**
	 * コンストラクタです。
	 * 非同期Idマーカ探索オブジェクトを生成します。
	 * @param i_listener
	 * 非同期イベントを受け取るオブジェクトを指定します。
	 * イベントは、このオブジェクトが作成するスレッドが呼び出します。
	 * @throws NyARException
	 */
	public ASyncIdMarkerTable(IResultListener i_listener) throws NyARException
	{
		this._mklib=new RawbitSerialIdTable(1);	
		this._mklib.addAnyItem("ANY ID",40);
		this._listener=i_listener;
	}
	private void callListener(boolean i_result,long i_serial,int i_dir,double i_width,long i_id)
	{
		//ON/OFFスイッチつけるならココ
		this._listener.OnDetect(i_result, i_serial, i_dir, i_width,i_id);
	}
	/**
	 * この関数は、非同期IDマーカ認識を実行します。
	 * この関数はサンプルなので、関数内でスレッドを生成して、三秒後に適当なサイズとDirectionを返却するだけです。
	 * i_realityとi_sourceは、i_targetがマーカパターンを取得するために使います。i_targetと正しい関係にあるオブジェクトを指定します。
	 * <p>メモ-
	 * この関数では、非同期探索を行う前に、i_realityとi_sourceの内容が変更されてもよいように、探索依頼関数内でパターンのコピーを作り、
	 * これを解析関数へ渡すようになっています。
	 * </p>
	 * @param i_reality
	 * i_targetの親になる{@link NyARReality}です。
	 * @param i_source
	 * i_realityの更新に使った{@link NyARRealitySource}です。
	 * @param i_target
	 * パターン認識を依頼する{@link NyARRealityTarget}です。
	 * 通常、Unknownターゲットを指定します。
	 * @throws NyARException 
	 */
	public void requestAsyncMarkerDetect(NyARReality i_reality,NyARRealitySource i_source,NyARRealityTarget i_target) throws NyARException
	{
		//ターゲットから画像データなどを取得するときは、スレッドからではなく、ここで同期して取得してコピーしてからスレッドに引き渡します。

		//100x100の領域を切りだして、Rasterを作る。
		NyARRgbRaster raster=new NyARRgbRaster(100,100,NyARBufferType.INT1D_X8R8G8B8_32);
		i_reality.getRgbPatt2d(i_source, i_target.refTargetVertex(),1, raster);
		//コピーしたラスタとターゲットのIDをスレッドへ引き渡す。
		Thread t=new AsyncThread(this,i_target.getSerialId(),raster);
		t.start();
		return;
	}
}
