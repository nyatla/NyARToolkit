package jp.nyatla.nyartoolkit.rpf.mklib;

import jp.nyatla.nyartoolkit.*;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.*;
/**
 * 未知の矩形を認識するサンプル。
 * 絶対的な寸法は求められないので、矩形の辺比率を推定して返します。
 * 
 *
 */

/**
 * このクラスは、未知の寸法、未知の縦横比率の矩形を認識するサンプルです。
 * 矩形認識を依頼すると、認識状態のステータスと、可能であれば、その矩形の縦横比率を計算して返します。
 * <p>アルゴリズム -
 * このクラスは、矩形を正面から撮影したときに、その縦横比率を推定します。
 * 正面かどうかは、４辺のなす角が概ねPI/2であるかと、対向する２辺が概ね同じ長さであるか
 * から判定します。
 * もし正面から撮影しない時でも、そのための「ヒント」を返却するので、ユーザに認識の仕方を教えるような使い方もできます。
 * </p>
 * <p>ヒント-
 * 既知のカードを認識したいのならば、比率推定後にターゲットの模様からその絶対サイズを特定すると良いかもしれません。
 * </p>
 */
public class CardDetect
{
	/**
	 * このクラスは、矩形比率の推定結果を記録します。
	 * {@link UnknownRectInfo#last_status}の値により、読出し可能なメンバが変わります。	
	 */
	public static class UnknownRectInfo
	{
		/** 内部使用。推定している{@link NyARRealityTarget}のシリアルID。*/
		public long _target_serial;
		/**　内部使用。成功回数のカウンタ*/
		public int _success_point;
		/**　内部使用。失敗回数のカウンタ*/
		public int _failed;
		/** 検出した矩形の縦横非推定値。0&lt;=n&lt;=100%の間。表記。*/
		public double rate;
		/** ARToolKitスタイルのdirection値*/
		public int artk_direction;
		/**　認識ステータスの値*/
		public int last_status;
		/** コンストラクタ。初期化済みのインスタンスを生成します。*/
		public UnknownRectInfo()
		{
			this._target_serial=NyARRealityTarget.INVALID_REALITY_TARGET_ID;
		}
	}
	/** {@link UnknownRectInfo}のステータス値。 このターゲットを推定するには、より正面から撮影が必要です。*/
	public final static int MORE_FRONT_CENTER=0;
	/** {@link UnknownRectInfo}のステータス値。矩形比率を推定中です。*/
	public final static int ESTIMATE_NOW=1;
	/** {@link UnknownRectInfo}のステータス値。矩形比率を推定完了。io_resultのメンバ変数が利用可能です。*/
	public final static int ESTIMATE_COMPLETE=2;
	/**　{@link UnknownRectInfo}のステータス値。推定に失敗しました。変な形のRECTだったのかも。*/
	public final static int FAILED_ESTIMATE=3;
	/** {@link UnknownRectInfo}のステータス値。推定に失敗しました。入力値が間違っている？*/
	public final static int FAILED_TARGET_MISSMATCH=4;
	/**
	 * この関数は、i_targetの矩形比率を推定します。
	 * 推定結果は、io_resultに返却します。
	 * <p>関数の使い方-
	 * 1回目に推定するときは、推定するターゲットをi_targetへ、io_resultに初期化済みのオブジェクトを入力します。関数は、i_targetの情報をio_resultへ記録します。
	 * 2回目以降は、同じシリアルID値を持つ{@link NyARRealityTarget}と{@link UnknownRectInfo}をペアにして入力します。関数は、i_targetの情報でio_resultを更新します。
	 * この関数は、i_targetを、{@link NyARRealityTarget}のシリアル番号で区別します。2回目以降は、両者のシリアルIDが一致していなければなりません。
	 * 何度か入力を繰り返すと、io_resultの{@link UnknownRectInfo#last_status}が更新されて、比率の推定が完了します。
	 * </p>
	 * <p>{@link UnknownRectInfo#last_status}のステータス値について。
	 * ステータス値の意味により、アプリケーションが何をするべきかが変わります。
	 * <ul>
	 * <li>{@link #MORE_FRONT_CENTER}
	 * 入力されたターゲットでは比率推定が難しい。より正面から撮影しなおす必要がある。
	 * <li>{@link #ESTIMATE_NOW}
	 * 入力されたターゲットで比率推定中である。推定を継続するために、次の画像を入力する。
	 * <li>{@link #ESTIMATE_COMPLETE}
	 * 比率推定に成功した。メンバ変数が読出し可能。
	 * <li>{@link #FAILED_ESTIMATE}
	 * 比率推定に失敗した。比率推定を継続できないので、アプリケーションはこのターゲットを{@link NyARRealityTarget#RT_DEAD}へ遷移させるべきである。
	 * <li>{@link #FAILED_TARGET_MISSMATCH}
	 * ２回目以降の認識で、ターゲットと記録オブジェクトのシリアルIDが一致しない。正しい組み合わせで入力するべき。
	 * </ul>
	 * </p>
	 * @param i_target
	 * 比率推定を実行する{@link NyARRealityTarget}オブジェクト。
	 * @param io_result
	 * 推定結果を受け取るオブジェクト。
	 * @throws NyARException
	 */
	public void detectCardDirection(NyARRealityTarget i_target,UnknownRectInfo io_result) throws NyARException
	{
		//成功点数が20点を超えたら推定完了。
		if(io_result._success_point>20){
			io_result.last_status=ESTIMATE_COMPLETE;
			return;
		}
		//10回失敗したら推定失敗
		if(io_result._failed>10){
			io_result.last_status=FAILED_ESTIMATE;
			return;
		}
		NyARDoublePoint2d[] pos=i_target.refTargetVertex();
		//正面から一回認識させてほしい。
		for(int i=0;i<4;i++){
			//正面判定。辺のなす角が90、または-90度の10度以内であること。
			if(getAbsSin(pos[0+i],pos[(1+i)%4],pos[(2+i)%4])<0.984){
				io_result.last_status=MORE_FRONT_CENTER;
				return;
			}
		}
		//線の長さを4本計算
		double d1=Math.sqrt(pos[0].sqDist(pos[1]));
		double d2=Math.sqrt(pos[1].sqDist(pos[2]));
		double d3=Math.sqrt(pos[2].sqDist(pos[3]));
		double d4=Math.sqrt(pos[3].sqDist(pos[0]));
		//現在の比率を計算
		double t,t2,t3;
		t=d1+d3*0.5;
		t2=d2+d4*0.5;
		t3=t/t2;
		t3=t3<1?1/t3:t3;
		if(io_result._target_serial==NyARRealityTarget.INVALID_REALITY_TARGET_ID){
			//サイクルをリセット
			io_result._target_serial=i_target.getSerialId();
			io_result.rate=t3;
			io_result._success_point=0;
			io_result._failed=0;
			io_result.artk_direction=t<t2?1:0;
		}else{
			if(io_result._target_serial!=i_target.getSerialId()){
				//ターゲットが一致しない。
				io_result.last_status=FAILED_TARGET_MISSMATCH;
				return;
			}
			if(t3/io_result.rate>0.98 && t3/io_result.rate<1.02)
			{
				io_result.rate=(io_result.rate+t3)*0.5;
				io_result._success_point++;
			}else{
				io_result._failed++;
			}
		}
		//推定中
		io_result.last_status=ESTIMATE_NOW;
		return;
	}
	/**
	 * この関数は、ベクトル(p1-&lt;p2)と(p2-&lt;p3)の作る角のsin値の絶対値を得ます。
	 * @param p1
	 * 点1
	 * @param p2
	 * 点2
	 * @param p3
	 * 点3
	 * @return
	 * sin値(0&lt;=n&lt;=1)
	 */
	public final static double getAbsSin(NyARDoublePoint2d p1,NyARDoublePoint2d p2,NyARDoublePoint2d p3)
	{
		double cp=NyARDoublePoint2d.crossProduct3Point(p1,p2,p3);
		cp/=(Math.sqrt(p1.sqDist(p2))*Math.sqrt(p2.sqDist(p3)));
		return cp>0?cp:-cp;
	}	
}
