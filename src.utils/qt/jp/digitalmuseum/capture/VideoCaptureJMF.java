/**
 * VideoCaptureJMF				1.00 08/07/15
 * 
 * Copyright (c) 2008 arc
 * http://digitalmuseum.jp/
 * All rights reserved.
 */
package jp.digitalmuseum.capture;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.media.Buffer;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.control.FormatControl;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.swing.Timer;


/**
 * JMFでWebcamによるキャプチャを実行するクラス。
 * 
 * @version	1.00 15 Sep 2008
 * @author arc
 */
public class VideoCaptureJMF extends AbstractVideoCapture {

	// キャプチャに使うタイマー
	private Timer timer;

	// JMF用のあれこれ
	private String locator = null;
	private Player player;
	private FrameGrabbingControl grabber;

	/** コンストラクタ。 */
	public VideoCaptureJMF(int w, int h) { super(w, h); }

	/**
	 * 入力を指定する。
	 */
	public void prepSetInput(Object input) {
		locator = input == null ? null : input.toString();
	}

	/**
	 * Webcamの設定ダイアログを表示する。
	 */
	public void prepShowDialog() { }

	/** キャプチャするフレームレートを指定する。 */
	public boolean prepSetFramerate(float fps_) {
		fps = fps_;
		return true;
	}

	/** 指定されたフォーマットでキャプチャできるデータソースを取得する。 */
	private DataSource createDataSource(Format format) {
		DataSource ds;
		MediaLocator ml;

		// デバイス名を指定されていない場合、
		// フォーマットに適したデバイスのメディアロケータを取得する
		if (locator == null) {
			List<CaptureDeviceInfo> devices =
					CaptureDeviceManager.getDeviceList(format);
			if (devices.size() < 1) {
				System.err.println(format+"のフォーマットでキャプチャできるデバイスが見つかりませんでした。");
				return null;
			}
			ml = devices.get(0).getLocator();

		// デバイス名を指定されている場合、そのメディアロケータを取得する
		} else ml = new MediaLocator(locator);

		// デバイスをデータソースにして、出力フォーマットを合わせる
		try {
			ds = Manager.createDataSource(ml);
			ds.connect();
			if (ds instanceof CaptureDevice)
				setCaptureFormat((CaptureDevice) ds, format);
		} catch (Exception e) {
			System.err.println(e);
			return null;
		}
		return ds;
	}

	/** キャプチャデバイスの出力フォーマットを設定する。 */
	private void setCaptureFormat(CaptureDevice cdev, Format format) {
		FormatControl [] fcs = cdev.getFormatControls();
		if (fcs.length < 1) return;

		FormatControl fc = fcs[0];
		Format [] formats = fc.getSupportedFormats();
		for (int i = 0; i < formats.length; i++)
			if (formats[i].matches(format)) {
				format = formats[i].intersects(format);
				fc.setFormat(format);
				break;
			}
    }

	/** キャプチャを開始する。 */
	public void start() throws Exception {
		player = Manager.createRealizedPlayer(createDataSource(
				new VideoFormat(
						RGBFormat.RGB,					// encoding
						new Dimension(width, height),	// size
						Format.NOT_SPECIFIED,			// maxDataLength
						null,							// dataType
						(float) fps)));					// frameRate);
		player.start();
		grabber = (FrameGrabbingControl) player.getControl(
				"javax.media.control.FrameGrabbingControl");
		pixels = new byte[width * height * 3];

		// キャプチャイメージを定期的に更新するタイマー
		timer = new Timer((int) (1000/fps), this);
		timer.start();
	}

	/** タイマー処理。キャプチャイメージの更新結果をリスナに伝える。 */
	public void actionPerformed(ActionEvent event) {
		Buffer buffer = grabber.grabFrame();
		byte[] pixels_reversed = (byte[]) buffer.getData();
		if (pixels_reversed == null)
			return;

		// ピクセル値の再配列
		for (int x = 0; x < width; x ++)
			for (int y = 0; y < height; y ++) {
				pixels[(x + y * width) * 3 + 0] = pixels_reversed[(x + (height - y - 1) * width) * 3 + 2];
				pixels[(x + y * width) * 3 + 1] = pixels_reversed[(x + (height - y - 1) * width) * 3 + 1];
				pixels[(x + y * width) * 3 + 2] = pixels_reversed[(x + (height - y - 1) * width) * 3 + 0];
			}

		// 各リスナに更新されたバイト列を渡す
		update();
	}

	/** キャプチャを終了する。 */
	public void dispose() { timer.stop(); }

}
