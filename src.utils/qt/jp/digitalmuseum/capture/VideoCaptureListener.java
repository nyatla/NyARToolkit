/**
 * VideoCaptureListener			1.00 08/05/30
 * 
 * Copyright (c) 2008 arc
 * http://digitalmuseum.jp/
 * All rights reserved.
 */
package jp.digitalmuseum.capture;


/**
 * キャプチャ画像を取得したいクラスが実装すべきインターフェース。
 * 
 * @version	1.01 5 June 2008
 * @author arc
 */
public interface VideoCaptureListener {

	public void imageUpdated(byte[] pixels);

}
