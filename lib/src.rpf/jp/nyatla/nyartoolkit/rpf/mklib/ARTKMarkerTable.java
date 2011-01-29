package jp.nyatla.nyartoolkit.rpf.mklib;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.match.*;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARPerspectiveRasterReader;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.stack.NyARObjectStack;
import jp.nyatla.nyartoolkit.rpf.reality.nyartk.*;
import jp.nyatla.nyartoolkit.rpf.realitysource.nyartk.*;

/**
 * このクラスは、ARToolKitマーカパターンのテーブルです。
 * マーカパターンにID番号とメタデータを関連付けたテーブルを持ち、パターンの一致検索機能を提供します。
 * テーブルには、ARToolKitフォーマットのマーカパターンと、ID番号、ユーザ定義のデータのセットを格納します。
 * このクラスは、テーブルの操作機能と、テーブル要素の検索機能を提供します。

 */
public class ARTKMarkerTable
{
	/**
	 * 結果型です。{@link ARTKMarkerTable#getBestMatchTarget}関数の戻り値を格納します。
	 * <p>memo-
	 * 入れ子クラスの作れない処理系では、ARTKMarkerTable_GetBestMatchTargetResultとして宣言してください。
	 * </p>
	 */
	public static class GetBestMatchTargetResult
	{
		/** ユーザ定義のID値です。*/
		public int idtag;
		/** ユーザ定義の名前文字列です。*/
		public String name;
		/** マーカ幅です。単位は、mm単位です。*/
		public double marker_width;
		/** マーカ高さです。単位は、mm単位です。*/
		public double marker_height;
		/** ARToolKit準拠のマーカの方位値です。0&lt;=n&lt;=3の値が入ります。*/
		public int artk_direction;
		/** パターンの一致率です。0<&lt;n&lt;=1.0の数値が入ります。*/
		public double confidence;
	}
	/**
	 * データを格納するリストクラスを定義します。
	 */
	private class MarkerTable extends NyARObjectStack<MarkerTable.SerialTableRow>
	{
		public class SerialTableRow
		{
			public int idtag;
			public String name;
			public NyARCode code;
			public double marker_width;
			public double marker_height;
			public final void setValue(NyARCode i_code,int i_idtag,String i_name,double i_width,double i_height)
			{
				this.code=i_code;
				this.marker_height=i_height;
				this.marker_width=i_width;
				this.name=i_name;
				this.idtag=i_idtag;
			}
		}		
		public MarkerTable(int i_length) throws NyARException
		{
			super.initInstance(i_length,SerialTableRow.class);
		}
		protected SerialTableRow createElement()
		{
			return new SerialTableRow();
		}
	}
	private int _resolution_width;
	private int _resolution_height;
	private int _edge_x;
	private int _edge_y;
	private int _sample_per_pix;
	private NyARRgbRaster _tmp_raster;
	private NyARMatchPatt_Color_WITHOUT_PCA _match_patt;
	private NyARMatchPattDeviationColorData _deviation_data;
	private MarkerTable _table;
	
	/** ワーク変数*/
	private NyARMatchPattResult __tmp_patt_result=new NyARMatchPattResult();
	
	
	/**
	 * コンストラクタです。引数にあるパラメータで、空のテーブルを作ります。
	 * @param i_max
	 * 登録するアイテムの最大数です。
	 * @param i_resolution_x
	 * 登録するパターンの横解像度です。
	 * ARToolKit互換の標準値は16です。
	 * @param i_resolution_y
	 * 登録するパターンの縦解像度です。
	 * ARToolKit互換の標準値は16です。
	 * @param i_edge_x
	 * エッジ部分の割合です。0<n<100の数値を指定します。
	 * ARToolKit互換の標準値は25です。
	 * @param i_edge_y
	 * エッジ部分の割合です。0<n<100の数値を指定します。
	 * ARToolKit互換の標準値は25です。
	 * @param i_sample_per_pix
	 * 元画像からパターンを取得する時の、1ピクセル当たりのサンプリング数です。
	 * 1なら1Pixel=1,2なら1Pixel=4ピクセルの画素を、元画像からサンプリングをします。
	 * ARToolKit互換の標準値は4です。
	 * 高解像度(64以上)のパターンを用いるときは、サンプリング数を低く設定してください。
	 * @throws NyARException 
	 */
	public ARTKMarkerTable(int i_max,int i_resolution_x,int i_resolution_y,int i_edge_x,int i_edge_y,int i_sample_per_pix) throws NyARException
	{
		this._resolution_width=i_resolution_x;
		this._resolution_height=i_resolution_y;
		this._edge_x=i_edge_x;
		this._edge_y=i_edge_y;
		this._sample_per_pix=i_sample_per_pix;
		this._tmp_raster=new NyARRgbRaster(i_resolution_x,i_resolution_y,NyARBufferType.INT1D_X8R8G8B8_32);
		this._table=new MarkerTable(i_max);
		this._deviation_data=new NyARMatchPattDeviationColorData(i_resolution_x,i_resolution_y);		
		this._match_patt=new NyARMatchPatt_Color_WITHOUT_PCA(i_resolution_x,i_resolution_y);
	}
	/**
	 * テーブル操作関数です。{@link NyARCode}を元にして、テーブルにマーカパターンを追加します。
	 * @param i_code
	 * ARToolKit形式のパターンコードを格納したオブジェクト。このオブジェクトは、関数成功後はインスタンスが所有します。
	 * パターンコードの解像度は、コンストラクタに指定した高さと幅である必要があります。
	 * @param i_id
	 * このマーカを識別する、ユーザ定義のID値です。任意の値を指定できます。不要な場合は0を指定してください。
	 * @param i_name
	 * ユーザ定義の名前です。任意の値を指定できます。不要な場合はnullを指定して下さい。
	 * @param i_width
	 * 実際のマーカの高さです。[通常mm単位]
	 * @param i_height
	 * 実際のマーカの幅です。[通常mm単位]
	 * @return
	 * 登録に成功すると、trueを返します。
	 */
	public boolean addMarker(NyARCode i_code,int i_id,String i_name,double i_width,double i_height)
	{
		assert(i_code.getHeight()== this._resolution_height && i_code.getHeight()== this._resolution_width);
		MarkerTable.SerialTableRow d=this._table.prePush();
		if(d==null){
			return false;
		}
		d.setValue(i_code,i_id,i_name,i_width,i_height);
		return true;
	}
	/**
	 * テーブル操作関数です。{@link NyARRgbRaster}クラスのオブジェクトからマーカパターンを生成して、テーブルへ追加します。方位は、上方向がdirection=0になります。
	 * @param i_raster
	 * マーカパターンの元となるラスタを指定します。解像度は、コンストラクタで指定したものと同じでなければなりません。
	 * @param i_id
	 * このマーカを識別するユーザ定義のID値です。任意の値を指定できます。不要な場合は0を指定してください。
	 * @param i_name
	 * ユーザ定義の名前です。任意の値を指定できます。不要な場合はnullを指定して下さい。
	 * @param i_width
	 * 実際のマーカの高さです。[通常mm単位]
	 * @param i_height
	 * 実際のマーカの幅です。[通常mm単位]
	 * @return
	 * 登録に成功すると、trueを返します。
	 * @throws NyARException
	 */
	public boolean addMarker(NyARRgbRaster i_raster,int i_id,String i_name,double i_width,double i_height) throws NyARException
	{
		MarkerTable.SerialTableRow d=this._table.prePush();
		if(d==null){
			return false;
		}
		NyARCode c=new NyARCode(this._resolution_width,this._resolution_height);
		c.setRaster(i_raster);
		d.setValue(c,i_id,i_name,i_width,i_height);
		return true;
	}
	/**
	 * テーブル操作関数です。ARToolkit準拠のパターンファイルを読み込み、テーブルへ追加します。
	 * @param i_filename
	 * パターンファイルのファイルパスを指定します。
	 * @param i_id
	 * このマーカを識別するユーザ定義のID値です。任意の値を指定できます。不要な場合は0を指定してください。
	 * @param i_name
	 * ユーザ定義の名前です。任意の値を指定できます。不要な場合はnullを指定して下さい。
	 * @param i_width
	 * 実際のマーカの高さです。[通常mm単位]
	 * @param i_height
	 * 実際のマーカの幅です。[通常mm単位]
	 * @return
	 * 登録に成功すると、trueを返します。
	 * @throws NyARException
	 */
	public boolean addMarkerFromARPattFile(String i_filename,int i_id,String i_name,double i_width,double i_height) throws NyARException
	{
		MarkerTable.SerialTableRow d=this._table.prePush();
		if(d==null){
			return false;
		}
		NyARCode c=new NyARCode(this._resolution_width,this._resolution_height);
		c.loadARPattFromFile(i_filename);
		d.setValue(c,i_id,i_name,i_width,i_height);
		return true;
	}	
	
	/**
	 * テーブル検索関数です。
	 * {@link NyARRealityTarget}を元に{@link NyARRealitySource}からパターンのサンプリングを行い、最も一致するマーカパターンをテーブルから検索します。
	 * 検索キーとなる{@link NyARRealityTarget}は、{@link NyARReality}派生クラスから取得した物を指定します。
	 * @param i_target
	 * 検索キーとなる、Unknownステータスの{@link NyARRealityTarget}クラスのオブジェクトを指定します。
	 * @param i_rtsorce
	 * i_targetを検出した{@link NyARRealitySource}のインスタンスを指定します。関数は、このソースからパターン取得を行います。
	 * @param o_result
	 * 返却値を格納するインスタンスを設定します。
	 * @return
	 * パターンのサンプリングに成功すると、trueを返します。
	 * 返却値がtrueの場合のみ、内容が更新されています。
	 * @throws NyARException 
	 */
	public boolean getBestMatchTarget(NyARRealityTarget i_target,NyARRealitySource i_rtsorce,GetBestMatchTargetResult o_result) throws NyARException
	{
		//パターン抽出
		NyARMatchPattResult tmp_patt_result=this.__tmp_patt_result;
		NyARPerspectiveRasterReader r=i_rtsorce.refPerspectiveRasterReader();
		r.read4Point(i_rtsorce.refRgbSource(),i_target.refTargetVertex(),this._edge_x,this._edge_y,this._sample_per_pix,this._tmp_raster);
		//比較パターン生成
		this._deviation_data.setRaster(this._tmp_raster);
		int ret=-1;
		int dir=-1;
		double cf=Double.MIN_VALUE;
		for(int i=this._table.getLength()-1;i>=0;i--){
			this._match_patt.setARCode(this._table.getItem(i).code);
			this._match_patt.evaluate(this._deviation_data, tmp_patt_result);
			if(cf<tmp_patt_result.confidence){
				ret=i;
				cf=tmp_patt_result.confidence;
				dir=tmp_patt_result.direction;
			}
		}
		if(ret<0){
			return false;
		}
		//戻り値を設定
		MarkerTable.SerialTableRow row=this._table.getItem(ret);
		o_result.artk_direction=dir;
		o_result.confidence=cf;
		o_result.idtag=row.idtag;
		o_result.marker_height=row.marker_height;
		o_result.marker_width=row.marker_width;
		o_result.name=row.name;
		return true;
	}
}
