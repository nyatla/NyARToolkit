/**
 * VideoCaptureDummy			1.00 08/07/15
 * 
 * Copyright (c) 2008 arc
 * http://digitalmuseum.jp/
 * All rights reserved.
 */
package jp.digitalmuseum.capture;

import javax.swing.Timer;


/**
 * Webcamによるキャプチャを代替するダミークラス。
 * 
 * @version	1.00 12 Sep 2008
 * @author arc
 */
public class VideoCaptureDummy extends AbstractVideoCapture {

	// キャプチャに使うタイマー
	private Timer timer;

	/** コンストラクタ。 */
	public VideoCaptureDummy(int w, int h) { super(w, h); }

	/**
	 * 入力を指定する。
	 */
	public void prepSetInput(Object input) { }

	/**
	 * Webcamの設定ダイアログを表示する。
	 */
	public void prepShowDialog() { }

	/** キャプチャするフレームレートを指定する。 */
	public boolean prepSetFramerate(float fps_) {
		fps = fps_;
		return true;
	}

	/** キャプチャを開始する。 */
	public void start() {
		pixels = new byte[width * height * 3];
		timer = new Timer((int) (1000/fps), this);
		timer.start();
	}

	/** キャプチャを終了する。 */
	public void dispose() { timer.stop(); }

}
