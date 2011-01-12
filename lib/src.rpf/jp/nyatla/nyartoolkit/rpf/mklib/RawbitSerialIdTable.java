package jp.nyatla.nyartoolkit.rpf.mklib;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.NyARRealityTarget;
import jp.nyatla.nyartoolkit.rpf.realitysource.nyartk.NyARRealitySource;
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status.NyARRectTargetStatus;
import jp.nyatla.nyartoolkit.nyidmarker.*;
import jp.nyatla.nyartoolkit.nyidmarker.data.NyIdMarkerDataEncoder_RawBit;
import jp.nyatla.nyartoolkit.nyidmarker.data.NyIdMarkerData_RawBit;

/**
 * このクラスは、簡易な同期型NyIdマーカのテーブルです。
 * マーカのID番号とメタデータを関連付けたテーブルを持ち、ID番号の検索機能を提供します。
 * このクラスは、RawBitフォーマットドメインのNyIdマーカのIdとメタデータセットテーブルを定義します。
 * SerialIDは、RawBitマーカのデータパケットを、[0][1]...[n]の順に並べて、64bitの整数値に変換した値です。
 * 判別できるNyIdマーカは、domain=0(rawbitドメイン),model&lt;5,mask=0のもののみです。
 * <p>使い方
 * <ol>
 * <li>ユーザは、このクラスにIDマーカのSerialNumberとそのサイズを登録します。
 * <li>{@link NyARRealityTarget}をキーにしてテーブルを検索します。
 * <li>一致するSerialNumberがあれば、戻り値から、そのサイズ、メタデータを得ることができます。
 * </p>
 */
public class RawbitSerialIdTable
{
	/**
	 * このクラスは、{@link RawbitSerialIdTable#identifyId}関数の戻り値を格納します。
	 * 入れ子クラスの作れない処理系では、RawbitSerialIdTable_IdentifyIdResultとして宣言してください。
	 */
	public static class IdentifyIdResult
	{
		/** ユーザ定義のID番号です。*/
		public long id;
		/** ユーザ定義の名前です。*/
		public String name;
		/** 登録時に設定したマーカサイズです。*/
		public double marker_width;
		/** ARToolKit準拠の、マーカの方位値です。*/
		public int artk_direction;
	}
	/** テーブルのクラス*/
	private class SerialTable extends NyARObjectStack<SerialTable.SerialTableRow>
	{
		public class SerialTableRow
		{
			public long id_st;
			public long id_ed;
			public double marker_width;
			public String name;
			public final void setValue(String i_name,long i_st,long i_ed,double i_width)
			{
				this.id_ed=i_ed;
				this.id_st=i_st;
				this.marker_width=i_width;
				this.name=i_name;
			}
		}		
		public SerialTable(int i_length) throws NyARException
		{
			super.initInstance(i_length,SerialTableRow.class);
		}
		protected SerialTableRow createElement()
		{
			return new SerialTableRow();
		}
		public SerialTableRow getItembySerialId(long i_serial)
		{
			for(int i=this._length-1;i>=0;i--)
			{
				SerialTableRow s=this._items[i];
				if(i_serial<s.id_st || i_serial>s.id_ed){
					continue;
				}
				return s;
			}
			return null;
		}
	}

	private SerialTable _table;
	private final NyIdMarkerPickup _id_pickup = new NyIdMarkerPickup();
	private NyIdMarkerPattern _temp_nyid_info=new NyIdMarkerPattern();
	private NyIdMarkerParam _temp_nyid_param=new NyIdMarkerParam();
	
	private NyIdMarkerDataEncoder_RawBit _rb=new NyIdMarkerDataEncoder_RawBit();
	private NyIdMarkerData_RawBit _rb_dest=new NyIdMarkerData_RawBit();

	/**
	 * コンストラクタです。
	 * @param i_max
	 * IDテーブルのサイズ。Idマーカ範囲の最大登録数です。
	 * @throws NyARException 
	 */
	public RawbitSerialIdTable(int i_max) throws NyARException
	{
		this._table=new SerialTable(i_max);
	}
	/**
	 * この関数は、SerialIDの範囲に対応するメタデータを、テーブルに追加します。
	 * ヒットする範囲は,i_st&lt;=n&lt;=i_edになります。
	 * 例えば、10～20番までのマーカノサイズを一括して登録するときに役立ちます。
	 * @param i_name
	 * このID範囲の名前を指定します。不要な場合はnullを指定します。
	 * @param i_st
	 * ヒット範囲の開始値です。
	 * @param i_ed
	 * ヒット範囲の終了値です。
	 * @param　i_width
	 * マーカのサイズ値を指定します。
	 * @return
	 * 登録の成否を真偽値で返します。
	 */
	public boolean addSerialIdRangeItem(String i_name,long i_st,long i_ed,double i_width)
	{
		SerialTable.SerialTableRow d=this._table.prePush();
		if(d==null){
			return false;
		}
		d.setValue(i_name,i_st,i_ed,i_width);
		return true;
	}
	/**
	 * この関数は、1個のSerialIDに対応するメタデータを、テーブルに追加します。
	 * @param i_serial
	 * ヒットさせるシリアルidです。
	 * @param i_width
	 * マーカのサイズ値です。
	 * @return
	 * 登録の成否を真偽値で返します。
	 */
	public boolean addSerialIdItem(String i_name,long i_serial,double i_width)
	{
		SerialTable.SerialTableRow d=this._table.prePush();
		if(d==null){
			return false;
		}
		d.setValue(i_name,i_serial,i_serial,i_width);
		return true;
	}
	/**
	 * 全てのSerialIDにヒットするメタデータセットを、テーブルに追加します。
	 * @param i_width
	 * ヒットしたマーカのサイズ値です。
	 * @return
	 * 登録の成否を真偽値で返します。
	 */
	public boolean addAnyItem(String i_name,double i_width)
	{
		SerialTable.SerialTableRow d=this._table.prePush();
		if(d==null){
			return false;
		}
		d.setValue(i_name,0,Long.MAX_VALUE,i_width);
		return true;
	}	
	/**
	 * この関数は、任意の四角形をIdマーカとして検査し、一致するメタデータを返します。
	 * i_rasterからi_vertexの頂点で定義される四角形のパターンを取得し、NyAIdマーカとして評価し、一致するID値をテーブルから取得します。
	 * @param i_vertex
	 * 4頂点の座標
	 * @param i_raster
	 * パターンを取得するラスタオブジェクト。
	 * @param o_result
	 * 取得結果を受け取るオブジェクト。
	 * @return
	 * テーブルから一致したIDが見つかればtrue
	 * 戻り値がtrueの時のみ有効です。
	 * @throws NyARException
	 */
	public final boolean identifyId(NyARDoublePoint2d[] i_vertex,INyARRgbRaster i_raster,IdentifyIdResult o_result) throws NyARException
	{
		if(!this._id_pickup.pickFromRaster(i_raster,i_vertex,this._temp_nyid_info,this._temp_nyid_param))
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
        SerialTable.SerialTableRow d=this._table.getItembySerialId(s);
		if(d==null){
			return false;
		}
		//戻り値を設定
		o_result.marker_width=d.marker_width;
		o_result.id=s;
		o_result.artk_direction=this._temp_nyid_param.direction;
		o_result.name=d.name;
		return true;		
	}
	/**
	 * この関数は、{@link NyARRealityTarget}のターゲットに一致するメタデータを返します。
	 * 複数のパターンにヒットしたときは、一番初めにヒットした項目を返します。
	 * @param i_target
	 * 検索キーとなる、Unknownステータスの{@link NyARRealityTarget}クラスのオブジェクトを指定します。
	 * @param i_rtsorce
	 * i_targetを検出した{@link NyARRealitySource}のインスタンスを指定します。関数は、このソースからパターン取得を行います。
	 * @param o_result
	 * 返却値を格納するインスタンスを設定します。
	 * 戻り値がtrueの時のみ有効です。
	 * @return
	 * テーブルから一致したIDが見つかればtrue
	 * @throws NyARException 
	 */
	public boolean identifyId(NyARRealityTarget i_target,NyARRealitySource i_rtsorce,IdentifyIdResult o_result) throws NyARException
	{
		//NyARDoublePoint2d[] i_vertex,NyARRgbRaster i_raster,SelectResult o_result
		return this.identifyId(
			((NyARRectTargetStatus)(i_target._ref_tracktarget._ref_status)).vertex,
			i_rtsorce.refRgbSource(),
			o_result);
	}
	//指定したIDとパターンが一致するか確認するAPIも用意するか？
}
