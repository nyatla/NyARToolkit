/**
 * AbstractVideoCapture			1.02 08/07/15
 * 
 * Copyright (c) 2008 arc
 * http://digitalmuseum.jp/
 * All rights reserved.
 */
package jp.digitalmuseum.capture;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;


/**
 * Webcamによるキャプチャを実行するクラスが拡張すべき抽象クラス。
 * 
 * @version	1.01 5 June 2008
 * @author	arc
 */
public abstract class AbstractVideoCapture implements ActionListener {

	protected int width;					// キャプチャ幅(指定した値から微妙にずれる)
	protected int height;					// キャプチャ高さ
	protected float fps = 30;				// キャプチャ画像を取得するfps

	protected byte[] pixels;				// キャプチャ画像の実データを保持するバイト型配列

	// リスナ
	public ArrayList<VideoCaptureListener> listeners = new ArrayList<VideoCaptureListener>();

	/** リスナを登録する */
	public void addVideoCaptureListener(VideoCaptureListener listener) { listeners.add(listener); }
	/** リスナを削除する */
	public void removeVideoCaptureListener(VideoCaptureListener listener) { listeners.remove(listener); }

	/** 指定した幅、高さでインスタンスを初期化するコンストラクタ。 */
	public AbstractVideoCapture(int w, int h) { setSize(w, h); }

	/** 指定した幅、高さでのキャプチャを指示する。start()でキャプチャを開始した後は使えない。 */
	public void setSize(int w, int h) { width = w; height = h; }

	/**
	 * Webcamの設定ダイアログを表示する。
	 * 既定のWebcamでは駄目な場合(複数のWebcamが接続されているPCなど)ではこれを実行するとよい。
	 */
	public abstract void prepShowDialog() throws Exception;

	/**
	 * キャプチャに使う入力デバイスを指定する。
	 * デバイスの代わりにダミー(デバッグ用)としてFileオブジェクトも渡せる実装にしておくべきである。
	 */
	public abstract void prepSetInput(Object device) throws Exception;
	/** キャプチャするフレームレートを指定する。 */
	public abstract boolean prepSetFramerate(float fps);

	/** キャプチャを始める */
	public abstract void start() throws Exception;
	/** キャプチャを終わる */
	public abstract void dispose();

	/** タイマー処理。キャプチャイメージの更新結果をリスナに伝える。 */
	public void actionPerformed(ActionEvent event) { update(); }

	/** リスナにキャプチャ結果を伝える */
	public void update() {
		if (pixels != null)
			for (VideoCaptureListener listener : listeners)
				listener.imageUpdated(pixels);
	}

	/** キャプチャしている画像の幅を取得する。 */
	public int getWidth() { return width; }
	/** キャプチャしている画像の高さを取得する。 */
	public int getHeight() { return height; }

}
