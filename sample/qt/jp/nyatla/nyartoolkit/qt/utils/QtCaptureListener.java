/**
 * QuickTimeお手軽キャプチャ用リスナ
 * (c)2008 arc@dmz, A虎＠nyatla.jp
 * arc@digitalmuseum.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.qt.utils;


public interface  QtCaptureListener{
    public void onUpdateBuffer(byte[] i_buffer);
    
}