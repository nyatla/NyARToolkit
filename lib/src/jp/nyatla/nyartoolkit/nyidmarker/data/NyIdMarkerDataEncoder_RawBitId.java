package jp.nyatla.nyartoolkit.nyidmarker.data;

import jp.nyatla.nyartoolkit.nyidmarker.*;

/**
 * このクラスは、RawbitドメインのIdマーカをLongのシリアル値にするエンコーダです。
 * Rawbitマーカのパケットを、[0][1]...[n]の順に並べて、64bitのId値を作ります。
 * コントロールドメイン0、マスク0、Model5未満のマーカを対象にします。
 */
public class NyIdMarkerDataEncoder_RawBitId extends NyIdMarkerDataEncoder_RawBit
{
	private NyIdMarkerData_RawBit _tmp=new NyIdMarkerData_RawBit();
	public boolean encode(NyIdMarkerPattern i_data,INyIdMarkerData o_dest)
	{
		//対象か調べるん
		if(i_data.ctrl_domain!=0)
		{
			return false;
		}
		//受け入れられるMaskは0のみ
		if(i_data.ctrl_mask!=0)
		{
			return false;
		}
		//受け入れられるModelは5未満
		if(i_data.model>=5)
		{
			return false;
		}
		//エンコードしてみる
		if(!super.encode(i_data,this._tmp)){
			return false;
		}
		//SerialIDの再構成
		long s=0;
        //最大4バイト繋げて１個のint値に変換
        for (int i = 0; i < this._tmp.length; i++)
        {
            s= (s << 8) | this._tmp.packet[i];
        }
        ((NyIdMarkerData_RawBitId)o_dest).marker_id=s;
		return true;
	}
	/**
	 * この関数は、{@link NyIdMarkerData_RawBitId}型のオブジェクトを生成して返します。
	 */
	public INyIdMarkerData createDataInstance()
	{
		return new NyIdMarkerData_RawBitId();
	}			
}
