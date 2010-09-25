package jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;

public class NyARNewTargetList extends NyARTargetList
{
	private NyARContourTargetStatusPool _ref_contoure_pool;
	private NyARNewTargetStatusPool _ref_new_pool;
	private int NUMBER_OF_AUTO_UPGREDE_PER_FRAME=1;
	private int COUNT_OF_IGNORE_EXPIRE=10;
	/**
	 * コンストラクタです。NewTargetステータスのNyARTargetオブジェクトをリストします。
	 * @param iMaxTarget
	 * 取り扱うターゲットの最大数。
	 * @param i_ref_contoure_pool
	 * このリストが使用するオブジェクトプール
	 * @throws NyARException
	 */
	public NyARNewTargetList(int iMaxTarget,NyARNewTargetStatusPool i_ref_new_pool,NyARContourTargetStatusPool i_ref_contoure_pool) throws NyARException
	{
		super(iMaxTarget);
		this._ref_new_pool=i_ref_new_pool;
		this._ref_contoure_pool=i_ref_contoure_pool;
	}
	/**
	 * 所有しているTargetについて、状態を更新します。
	 * @param i_clock
	 * システムクロック値を指定します。
	 * @param i_igtargetlist
	 * 遷移先のターゲットリストを指定します。
	 * @throws NyARException 
	 */
	public void upgrade(long i_clock,NyARGrayscaleRaster i_base_raster,NyARIgnoreTargetList i_igtargetlist,NyARCoordTargetList i_coordlist) throws NyARException
	{
		for(int i=this._length-1;i>=0;i--){
			NyARTarget t=this._items[i];
			if(i_clock-t.last_update>COUNT_OF_IGNORE_EXPIRE)
			{
				//一定の期間が経過したら、ignoreへ遷移
				if(i_igtargetlist.push(t)!=null){
					//成功。ステータスの切り替えて、このターゲットのリストから外す。
					t.setIgnoreStatus();
					this.removeIgnoreOrder(i);				
				}else{
					//追加失敗→何もせず
					break;
				}
			}
		}
		//アップグレード処理の回数を計算
		int lp=this._length<NUMBER_OF_AUTO_UPGREDE_PER_FRAME?this._length:NUMBER_OF_AUTO_UPGREDE_PER_FRAME;
		//newtargetのいくつかをcoordtargetへ遷移。
		for(int i=lp-1;i>=0;i--)
		{
			NyARTarget t=this._items[i];
			NyARNewTargetStatus st=(NyARNewTargetStatus)t.ref_status;
			//coordステータスを生成
			NyARContourTargetStatus c=this._ref_contoure_pool.newObject(i_base_raster,st.sampleout);
			if(i_coordlist.push(t)==null){
				//追加失敗。生成したステータスを破棄
				c.releaseObject();
				break;
			}else{
				//成功。ステータスを切り替えて、このターゲットリストから外す。
				t.setCntoureStatus(c);
				this.removeIgnoreOrder(i);				
			}
		}
	}
	/**
	 * このターゲットを、SampleStackを使って更新します。
	 * @param i_sample
	 */
	public void update(long i_clock,SampleStack i_sample)
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
			//先にステータスを作成しておく
			NyARTargetStatus st=this._ref_new_pool.newObject(s.ref_sampleout);
			if(st==null){
				//ステータスの生成に失敗
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