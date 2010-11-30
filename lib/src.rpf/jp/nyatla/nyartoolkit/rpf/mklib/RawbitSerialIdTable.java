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
 * 簡易な同期型NyIdマーカIDテーブルです。
 * このクラスは、RawBitフォーマットドメインのNyIdマーカのIdとメタデータセットテーブルを定義します。
 * SerialIDは、RawBitマーカのデータパケットを、[0][1]...[n]の順に並べて、64bitの整数値に変換した値です。
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
public class RawbitSerialIdTable
{
	/**
	 * selectTarget関数の戻り値を格納します。
	 * 入れ子クラスの作れない処理系では、RawbitSerialIdTable_IdentifyIdResultとして宣言してください。
	 */
	public static class IdentifyIdResult
	{
		/** ID番号です。*/
		public long id;
		/** 名前です。*/
		public String name;
		/** 登録時に設定したマーカサイズです。*/
		public double marker_width;
		/** ARToolKit準拠の、マーカの方位値です。*/
		public int artk_direction;
	}
	

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
	 * 登録するアイテムの最大数です。
	 * @throws NyARException 
	 */
	public RawbitSerialIdTable(int i_max) throws NyARException
	{
		this._table=new SerialTable(i_max);
	}
	/**
	 * IDの範囲に対するメタデータセットを、テーブルに追加します。
	 * この要素にヒットする範囲は,i_st&lt;=n&lt;=i_edになります。
	 * @param i_name
	 * このID範囲の名前を指定します。不要な場合はnullを指定します。
	 * @param i_st
	 * ヒット範囲の開始値です。
	 * @param i_ed
	 * ヒット範囲の終了値です。
	 * @param　i_width
	 * ヒットしたマーカのサイズ値を指定します。
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
	 * SerialIDに対するメタデータセットを、テーブルに追加します。
	 * @param i_serial
	 * ヒットさせるシリアルidです。
	 * @param i_width
	 * ヒットしたマーカのサイズ値です。
	 * @return
	 * 登録に成功するとtrueを返します。
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
	 * 登録に成功するとtrueです。
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
	 * i_raster上にあるi_vertexの頂点で定義される四角形のパターンから、一致するID値を特定します。
	 * @param i_vertex
	 * 4頂点の座標
	 * @param i_raster
	 * @param o_result
	 * @return
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
	 * RealityTargetに一致するID値を特定します。
	 * 複数のパターンにヒットしたときは、一番初めにヒットした項目を返します。
	 * @param i_target
	 * Realityが検出したターゲット。
	 * Unknownターゲットを指定すること。
	 * @param i_rtsorce
	 * i_targetを検出したRealitySourceインスタンス。
	 * @param o_result
	 * 返却値を格納するインスタンスを設定します。
	 * 返却値がtrueの場合のみ、内容が更新されています。
	 * @return
	 * 特定に成功すると、trueを返します。
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
