package jp.nyatla.nyartoolkit.markerar.utils;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPatt_Color_WITHOUT_PCA;


/**
 * sqメンバは、参照です。
 *
 */
public class MarkerInfoARMarker extends TMarkerData
{
	/** MK_ARの情報。比較のための、ARToolKitマーカを格納します。*/
	public final NyARMatchPatt_Color_WITHOUT_PCA matchpatt;
	/** MK_ARの情報。検出した矩形の格納変数。マーカの一致度を格納します。*/
	public double cf;
	public int patt_w;
	public int patt_h;
	/** MK_ARの情報。パターンのエッジ割合。*/
	public final int patt_edge_percentage;
	/** */
	public MarkerInfoARMarker(NyARCode i_patt,int i_patt_edge_percentage,double i_patt_size) throws NyARException
	{
		super();
		this.matchpatt=new NyARMatchPatt_Color_WITHOUT_PCA(i_patt);
		this.patt_edge_percentage=i_patt_edge_percentage;
		this.marker_offset.setSquare(i_patt_size);
		this.patt_w=i_patt.getWidth();
		this.patt_h=i_patt.getHeight();
		return;
	}		
}