/**
 * NyARToolkitのBehaviorのリスナ
 * (c)2008 A虎＠nyatla.jp
 * airmail@ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.java3d.utils;

import javax.media.j3d.*;

public interface NyARSingleMarkerBehaviorListener
{
    /**
     * このリスナは、リスナにマーカーに連動してオブジェクトを操作するチャンスを与えます。
     * リスナはNyARSingleMarkerBehavior関数内のprocessStimulus関数から呼び出されます。
     * 
     * @param i_is_marker_exist
     * マーカーが存在する場合true、存在しない場合、falseです。
     * @param i_transform3d
     * マーカーが存在する場合、その変換行列が指定されます。
     * i_is_marker_existがtrueの時だけ有効です。
     */
    public void onUpdate(boolean i_is_marker_exist,Transform3D i_transform3d);
}
