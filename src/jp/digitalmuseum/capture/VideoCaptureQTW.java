/**
 * VideoCaptureQT				1.00 08/05/30
 * 
 * Copyright (c) 2008 arc
 * http://digitalmuseum.jp/
 * All rights reserved.
 */
package jp.digitalmuseum.capture;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Timer;

import quicktime.QTSession;
import quicktime.QTException;
import quicktime.io.QTFile;
import quicktime.qd.PixMap;
import quicktime.qd.QDConstants;
import quicktime.qd.QDGraphics;
import quicktime.qd.QDRect;
import quicktime.std.movies.Movie;
import quicktime.std.movies.media.DataRef;
import quicktime.std.StdQTConstants;
import quicktime.std.sg.SequenceGrabber;
import quicktime.std.sg.SGVideoChannel;
import quicktime.util.RawEncodedImage;


/**
 * QuickTime for JavaでWebcamによるキャプチャを実行するクラス。
 * 
 * MacのQTJavaだとk24RGBPixelFormatの処理系統が壊れているようで、
 * おかしなデータを吐いてしまうのでWindows専用。
 * 
 * @version	1.00 30 May 2008
 * @author arc
 */
public class VideoCaptureQTW extends AbstractVideoCapture {

	// キャプチャに使うタイマー
	private Timer timer;

	// QTJava用のあれこれ
	private QDGraphics graphics;
	private QDRect bounds;
	private SequenceGrabber grabber;
	private SGVideoChannel channel;
	private RawEncodedImage rawEncodedImage;
	private Movie movie;

	/** コンストラクタ。 */
	public VideoCaptureQTW(int w, int h) { super(w, h); }

	/**
	 * 入力を指定する。
	 * 複数のWebcamが接続されていて、使いたいデバイスの名前が分かっている場合はこれを実行するとよい。
	 * ただし、使いたいデバイスと同じ名前を他のWebcamが持っていた場合、どれが使われるか分からない。
	 * また、名前でなくファイルが指定されていたら、Webcamでなくダミーとしてファイルを入力に使う。
	 */
	public void prepSetInput(Object input) throws QTException {
		QTSession.open();
		bounds = new QDRect(width, height);
		graphics = new QDGraphics(QDConstants.k24RGBPixelFormat, bounds);
		if (input != null && input.getClass().equals(File.class)) {
			movie = quicktime.std.movies.Movie.fromDataRef(
				new DataRef(new QTFile((File) input)),
				StdQTConstants.newMovieActive
			);
		} else {
			grabber = new SequenceGrabber();
			grabber.setGWorld(graphics, null);
			channel = new SGVideoChannel(grabber);
			channel.setBounds(bounds);

			// seqGrabPreview == 2, Processingでmagic numberとしてハードコートされていた…
			channel.setUsage(StdQTConstants.seqGrabPreview);

			if (input != null) {
				try {
					channel.setDevice(input.toString());
				} catch (QTException e) {
					e.printStackTrace();
					throw e;
				}
			}
		}
	}

	/**
	 * Webcamの設定ダイアログを表示する。
	 * 既定のWebcamでは駄目な場合(複数のWebcamが接続されているPCなど)ではこれを実行するとよい。
	 */
	public void prepShowDialog() throws QTException { channel.settingsDialog(); }

	/** キャプチャするフレームレートを指定する。 */
	public boolean prepSetFramerate(float fps_) {
		try {
			fps = fps_;
			channel.setFrameRate(fps);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/** キャプチャを開始する。 */
	public void start() throws QTException {
		try {
			if (movie == null) {
				grabber.prepare(true, false); // あってもなくてもよさそう
				grabber.startPreview();
			} else {
				movie.preroll(0, 1.0f);
				while (movie.maxLoadedTimeInMovie() == 0)
					movie.task(100);
				movie.setRate(1);
				movie.getPict(movie.getTime()).draw(graphics, bounds);
			}
			PixMap pixmap = graphics.getPixMap();
			rawEncodedImage = pixmap.getPixelData();

			width = rawEncodedImage.getRowBytes() / 3;
			pixels = new byte[width * height * 3];
		} catch (QTException e) {
			QTSession.close();
			throw e;
		}

		// キャプチャイメージを定期的に更新するタイマー
		timer = new Timer((int) (1000/fps), this);
		timer.start();
	}

	/** タイマー処理。キャプチャイメージの更新結果をリスナに伝える。 */
	public void actionPerformed(ActionEvent event) {

		// 画像をQTJavaのRawEncodedImageとして取得
		try {
			if (movie == null) {
				grabber.idle();
			} else {
				if (movie.isDone()) movie.goToBeginning();
				movie.getPict(movie.getTime()).draw(graphics, bounds);
			}
		} catch (QTException e) {
			QTSession.close();
			e.printStackTrace();
		}

		// RawEncodedImageをバイト列に落とし込む
		rawEncodedImage.copyToArray(0, pixels, 0, pixels.length);

		// 各リスナに更新されたバイト列を渡す
		update();
	}

	/** キャプチャを終了する。 */
	public void dispose() {
		try {
			if (movie == null) {
				grabber.stop();
				grabber.release();
				grabber.disposeChannel(channel);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			QTSession.close();
		}
		timer.stop();
	}

}
