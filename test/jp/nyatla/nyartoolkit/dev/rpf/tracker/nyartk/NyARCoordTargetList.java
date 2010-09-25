package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;

public class NyARCoordTargetList extends NyARTargetList
{
	private int EXPIRE_COUNT=10;
	private NyARContourTargetStatusPool _ref_contoure_pool;
	/**
	 * コンストラクタです。NyARContourStatusのNyARTargetのリストクラスを生成します。
	 * @param iMaxTarget
	 * 取り扱うターゲットの最大数
	 * @param i_ref_contour_pool
	 * このリストが使用するオブジェクトプール
	 * @throws NyARException
	 */
	public NyARCoordTargetList(int iMaxTarget,NyARContourTargetStatusPool i_ref_contour_pool) throws NyARException
	{
		super(iMaxTarget);
		this._ref_contoure_pool=i_ref_contour_pool;
	}
	/**
	 * 所有しているTargetについて、状態を更新します。
	 * @param i_clock
	 * システムクロック値を指定します。
	 * @param i_igtargetlist
	 * 遷移先のターゲットリストを指定します。
	 */
	public void upgrade(long i_clock,NyARCoordTargetList i_igtargetlist)
	{
		for(int i=this._length-1;i>=0;i--){
			NyARTarget t=this._items[i];
			if(i_clock-t.last_update>EXPIRE_COUNT)
			{
				//一定の期間更新が発生しなければ、ignoreへ遷移
				if(i_igtargetlist.push(t)!=null){
					this.removeIgnoreOrder(i);				
				}
			}
		}
	}

	/**
	 * このターゲットを、SampleStackを使って更新します。
	 * @param i_sample
	 * @throws NyARException 
	 */
	public void update(long i_clock,NyARGrayscaleRaster i_base_raster,SampleStack i_sample) throws NyARException
	{
		SampleStack.Item[] source=i_sample.getArray();
		NyARTarget d_ptr;
		i_sample.getArray();
		//マップする。
		this._map.setPointDists(this._items,this._length,source,i_sample.getLength());
		//ターゲットの更新
		for(int i=this._length-1;i>=0;i--){
			d_ptr=this._items[i];
			int sample_index=this._index[i];
			//年齢を加算
			d_ptr.age++;
			if(sample_index<0){
				//このターゲットに合致するアイテムは無い。
				continue;
			}
			SampleStack.Item s=source[sample_index];
			//失敗の可能性を考慮して、Statusを先に生成しておく
			NyARTargetStatus st=this._ref_contoure_pool.newObject(i_base_raster,s.ref_sampleout);
			if(st==null){
				//失敗（作れなかった？）
				continue;
			}
			d_ptr.setValue(s.ref_sampleout);
			d_ptr.last_update=i_clock;
			//ref_statusの切り替え
			d_ptr.ref_status.releaseObject();
			d_ptr.ref_status=st;
		}
	}
}