package jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk;

import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.*;

/**
 * Trackerのデータを都合よく解釈するレイヤ。
 *
 */
public class NyARReality
{
	public void progress()
	{
		NyARTrackerOut trackout=null;
		//ステータスが切り替わっちゃった既存のターゲットあるかなー？→あったらどうにかする。
		//recttargetから復旧できそうなのをさがす

		//recttargetから遷移できそうなのあるかなー？
		trackout.recttarget.getEmptyTagItem();

		//対象にしてないターゲットをPrevに放り込む
	}
	/**
	 * 非同期の探索を取得したときに呼び出される関数
	 */
	public void recall()
	{
		
	}
}
