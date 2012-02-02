package jp.nyatla.nyartoolkit.core.squaredetect;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntCoordinates;

/**
 * このインタフェイスは、NyARSquareContourDetectorクラスのイベントハンドラです。
 */
public interface INyARSquareContourDetectorHandler
{
	/**
	 * この関数は、自己コールバック関数です。{@link #detectMarker}が検出結果を通知する為に使います。
	 * 実装クラスでは、ここに矩形の発見時の処理を記述してください。
	 * @param i_coord
	 * 輪郭線オブジェクト
	 * @param i_vertex_index
	 * 矩形の４頂点に対応する、輪郭線オブジェクトのインデクス番号。
	 * @throws NyARException
	 */
	public void detectMarkerCb(NyARIntCoordinates i_coord,int[] i_vertex_index)  throws NyARException;
}
