package jp.nyatla.nyartoolkit.dev.rpf.mklib;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk.NyARRealityIn;
import jp.nyatla.nyartoolkit.dev.rpf.reality.nyartk.NyARRealityTarget;
import jp.nyatla.nyartoolkit.dev.rpf.tracker.nyartk.status.NyARRectTargetStatus;
import jp.nyatla.nyartoolkit.nyidmarker.*;
import jp.nyatla.nyartoolkit.nyidmarker.data.NyIdMarkerDataEncoder_RawBit;
import jp.nyatla.nyartoolkit.nyidmarker.data.NyIdMarkerData_RawBit;

/**
 * 簡易なNyIdマーカIDデーブルです。
 * このクラスは、RawBitフォーマットドメインのNyIdマーカのIdとメタデータセットのテーブルを定義します。
 * 判別できるIdマーカは、domain=0(rawbit),model&lt;5,mask=0のもののみです。
 * <p>
 * このクラスは、NyRealityTargetをRawBitフォーマットドメインのSerialNumberマーカにエンコードする
 * 機能を提供します。
 * 使い方は、ユーザは、このクラスにIDマーカのSerialNumberとそのサイズを登録します。その後に、
 * NyRealityTargetをキーに、登録したデータからそのSerialNumberをサイズを得ることができます。
 * </p>
 * 
 * NyIdRawBitSerialNumberTable
 */
public class SimpleNyIdLibrary
{
	public class Result{
		public long serial;
		public Object tag;
		public double marker_width;
	}
	public class ResultList extends NyARObjectStack<DataSet>
	{
		public ResultList(int i_length) throws NyARException
		{
			super.initInstance(i_length,DataSet.class);
		}
		protected DataSet createElement()
		{
			return new DataSet();
		}
		public DataSet getItembySerialId(long i_serial)
		{
			for(int i=this._length-1;i>=0;i--)
			{
				DataSet s=this._items[i];
				if(i_serial<s.serial_st || i_serial>s.serial_ed){
					continue;
				}
				return s;
			}
			return null;
		}
	}
	public class DataSet
	{
		public int serial_st;
		public int serial_ed;
		public int marker_width;
		public int tag;
	}
	/**
	 * SerialIDの範囲に対するメタデータセットを、テーブルに追加します。
	 * SerialIDは、RawBitマーカのデータパケットを、[0][1]...[n]の順に並べて、64bitの整数値に変換した値です。
	 * SerialIDは、0~int64MAXまでの数値です。Model5以上のRawbitマーカは検出できません。
	 * @param i_st
	 * @param i_ed
	 */
	public void addSerialIdRange(int i_st,int i_ed)
	{
	}
	public void addSerialId(int i_st,int i_ed)
	{
	}
	public void selectSerialId()
	{
		
	}
	public SimpleNyIdLibrary(Object i_sync_object)
	{
		
	}
	ResultList _table;
	private final NyIdMarkerPickup _id_pickup = new NyIdMarkerPickup();

	private NyIdMarkerPattern _temp_nyid_info=new NyIdMarkerPattern();
	private NyIdMarkerParam _temp_nyid_param=new NyIdMarkerParam();
	
	private NyIdMarkerDataEncoder_RawBit _rb=new NyIdMarkerDataEncoder_RawBit();
	private NyIdMarkerData_RawBit _rb_dest=new NyIdMarkerData_RawBit();
	
	
	/**
	 * RealityTargetを特定します。
	 * i_targetの画像パターンをi_rtsorceから取得して、登録されているIdの中から、合致するメタデータを返します。
	 * @param i_target
	 * Realityが検出したターゲット。
	 * Unknownターゲットを指定すること。
	 * @param i_rtsorce
	 * i_targetを検出したRealitySourceインスタンス。
	 * @return
	 * 合致したデータセットを返す。
	 * @throws NyARException 
	 */
	public boolean selectTarget(NyARRealityTarget i_target,NyARRealityIn i_rtsorce,Result o_result) throws NyARException
	{
//		INyARRgbRaster raster;
//		//リコールされた内容を解析する。
//		SimpleNyIdLibrary.RealityData data;
//		//Idマーカとして解析してみる。
//		NyIdMarkerParam idparam;
		NyARDoublePoint2d[] vx=((NyARRectTargetStatus)(i_target.ref_tracktarget.ref_status)).vertex;
		if(!this._id_pickup.pickFromRaster(i_rtsorce.sourceimage,vx,this._temp_nyid_info,this._temp_nyid_param))
		{
			return false;
		}
		//受け付けられるControlDomainは0のみ
		if(this._temp_nyid_info.ctrl_domain!=0)
		{
			return false;
		}
		//受け入れられるMaskは0のみ
		if(this._temp_nyid_info.ctrl_mask!=0)
		{
			return false;
		}
		//受け入れられるModelは5未満
		if(this._temp_nyid_info.model>=5)
		{
			return false;
		}

		this._rb.createDataInstance();
		if(!this._rb.encode(this._temp_nyid_info,this._rb_dest)){
			return false;
		}
		//SerialIDの再構成
		long s=0;
        //最大4バイト繋げて１個のint値に変換
        for (int i = 0; i < this._rb_dest.length; i++)
        {
            s= (s << 8) | this._rb_dest.packet[i];
        }		
		//SerialID引きする。
        DataSet d=this._table.getItembySerialId(s);
		if(d==null){
			return false;
		}
		//戻り値を設定
		o_result.marker_width=d.marker_width;
		o_result.serial=s;
		return true;
	}
}
